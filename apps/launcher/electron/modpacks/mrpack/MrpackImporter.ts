import * as fs from 'node:fs';
import * as path from 'node:path';
import * as crypto from 'node:crypto';
import * as https from 'node:https';
import { EnginePaths } from '../../engine/paths.js';
import { InstallationRepository } from '../../engine/InstallationRepository.js';
import { MrpackArchiveReader, MrpackZipEntry } from './MrpackArchiveReader.js';
import { MrpackManifestValidator } from './MrpackManifestValidator.js';
import { MrpackPathValidator } from './MrpackPathValidator.js';
import { MrpackOverrideManager } from './MrpackOverrideManager.js';
import { ModpackLockfileManager } from '../ModpackLockfileManager.js';
import { ModRepository } from '../ModRepository.js';
import type {
  MrpackInspectionResult,
  MrpackImportOptions,
  MrpackImportProgress,
  MrpackImportResult,
  MrpackIndex,
  MrpackIndexFile,
  MinecraftLoaderType,
  InstanceRecord,
} from '@zombek/shared-types';

export class MrpackImporter {
  public static readonly APPROVED_HOSTS = new Set([
    'cdn.modrinth.com',
    'edge.modrinth.com',
    'github.com',
    'raw.githubusercontent.com',
    'gitlab.com',
  ]);

  public static isWhitelistedHost(urlStr: string): boolean {
    try {
      const parsed = new URL(urlStr);
      if (parsed.protocol !== 'https:') return false;
      const host = parsed.hostname.toLowerCase();
      if (
        host === 'localhost' ||
        host.startsWith('127.') ||
        host.startsWith('192.168.') ||
        host.startsWith('10.') ||
        /^172\.(1[6-9]|2[0-9]|3[0-1])\./.test(host)
      ) {
        return false;
      }
      return this.APPROVED_HOSTS.has(host);
    } catch {
      return false;
    }
  }

  /**
   * Inspects a .mrpack archive without modifying any game state or extracting files to disk.
   */
  public static async inspect(archivePath: string): Promise<MrpackInspectionResult> {
    const errors: string[] = [];
    const warnings: string[] = [];

    // 1. Validate file
    const fileVal = MrpackArchiveReader.validateArchiveFile(archivePath);
    if (!fileVal.valid) {
      return {
        valid: false,
        archivePath,
        loaderType: 'vanilla',
        minecraftVersion: '',
        totalFilesCount: 0,
        clientFilesCount: 0,
        optionalFilesCount: 0,
        serverOnlyFilesCount: 0,
        overridesCount: 0,
        clientOverridesCount: 0,
        serverOverridesCount: 0,
        totalDownloadSizeBytes: 0,
        files: [],
        overridesPreview: [],
        warnings: [],
        errors: [fileVal.error || 'Nieprawidłowy plik archiwum.'],
      };
    }

    try {
      // 2. Read ZIP entries
      const entries = await MrpackArchiveReader.listEntries(archivePath);

      // 3. Read & validate manifest
      const manifest = await MrpackArchiveReader.readManifest(archivePath);
      const manifestVal = MrpackManifestValidator.validate(manifest);

      if (!manifestVal.valid) {
        errors.push(...manifestVal.errors);
      }
      warnings.push(...manifestVal.warnings);

      // 4. Analyze overrides in ZIP
      let baseOverridesCount = 0;
      let clientOverridesCount = 0;
      let serverOverridesCount = 0;
      const overridesPreview: string[] = [];

      for (const e of entries) {
        if (e.isDirectory) continue;
        const norm = e.normalizedPath;
        if (norm.startsWith('overrides/')) {
          baseOverridesCount++;
          if (overridesPreview.length < 50) {
            overridesPreview.push(norm);
          }
        } else if (norm.startsWith('client-overrides/')) {
          clientOverridesCount++;
          if (overridesPreview.length < 50) {
            overridesPreview.push(norm);
          }
        } else if (norm.startsWith('server-overrides/')) {
          serverOverridesCount++;
          if (overridesPreview.length < 50) {
            overridesPreview.push(norm);
          }
        }
      }

      // 5. Analyze files to download
      const filesSummary: MrpackInspectionResult['files'] = [];
      let totalDownloadSizeBytes = 0;
      let clientFilesCount = 0;
      let optionalFilesCount = 0;
      let serverOnlyFilesCount = 0;

      for (const file of manifest.files || []) {
        const envClient = file.env?.client || 'required';
        const envServer = file.env?.server || 'required';
        const isOptional = envClient === 'optional';
        const isUnsupported = envClient === 'unsupported';
        const isServerOnly = isUnsupported && envServer === 'required';

        if (isServerOnly) {
          serverOnlyFilesCount++;
        } else if (!isUnsupported) {
          clientFilesCount++;
          if (isOptional) {
            optionalFilesCount++;
          }
          totalDownloadSizeBytes += file.fileSize || 0;
        }

        const primaryUrl = file.downloads && file.downloads.length > 0 ? file.downloads[0] : '';

        // Check if download host is approved
        if (primaryUrl) {
          try {
            const parsedUrl = new URL(primaryUrl);
            if (!this.APPROVED_HOSTS.has(parsedUrl.hostname.toLowerCase())) {
              warnings.push(
                `Plik [${file.path}] pochodzi z nieoficjalnego hosta: ${parsedUrl.hostname}.`
              );
            }
          } catch {
            warnings.push(`Nieprawidłowy format URL dla pliku [${file.path}]: ${primaryUrl}`);
          }
        }

        filesSummary.push({
          path: file.path,
          fileName: path.basename(file.path),
          fileSize: file.fileSize || 0,
          envClient,
          isOptional,
          isUnsupported,
          primaryDownloadUrl: primaryUrl,
          sha512: file.hashes?.sha512 || '',
          sha1: file.hashes?.sha1 || '',
        });
      }

      return {
        valid: errors.length === 0,
        archivePath,
        index: manifestVal.valid ? manifest : undefined,
        loaderType: manifestVal.loaderType,
        loaderVersion: manifestVal.loaderVersion,
        minecraftVersion: manifestVal.minecraftVersion,
        totalFilesCount: manifest.files?.length || 0,
        clientFilesCount,
        optionalFilesCount,
        serverOnlyFilesCount,
        overridesCount: baseOverridesCount,
        clientOverridesCount,
        serverOverridesCount,
        totalDownloadSizeBytes,
        files: filesSummary,
        overridesPreview,
        warnings,
        errors,
      };
    } catch (err: any) {
      return {
        valid: false,
        archivePath,
        loaderType: 'vanilla',
        minecraftVersion: '',
        totalFilesCount: 0,
        clientFilesCount: 0,
        optionalFilesCount: 0,
        serverOnlyFilesCount: 0,
        overridesCount: 0,
        clientOverridesCount: 0,
        serverOverridesCount: 0,
        totalDownloadSizeBytes: 0,
        files: [],
        overridesPreview: [],
        warnings,
        errors: [err.message || 'Błąd podczas inspekcji archiwum .mrpack.'],
      };
    }
  }

  /**
   * Executes transactional import of a .mrpack modpack.
   */
  public static async import(
    options: MrpackImportOptions,
    onProgress?: (progress: MrpackImportProgress) => void
  ): Promise<MrpackImportResult> {
    const { archivePath, instanceName, selectedOptionalPaths = [], targetInstanceId } = options;

    onProgress?.({
      stage: 'reading',
      message: 'Odczytywanie archiwum .mrpack...',
      percent: 5,
    });

    // 1. Inspect & Validate
    const inspection = await this.inspect(archivePath);
    if (!inspection.valid || !inspection.index) {
      throw new Error(
        `Walidacja paczki nie powiodła się: ${inspection.errors.join('; ')}`
      );
    }

    const manifest = inspection.index;

    onProgress?.({
      stage: 'validating',
      message: 'Weryfikacja manifestu i modloadera...',
      percent: 15,
    });

    // 2. Determine target instance
    let targetInstance: InstanceRecord;
    let isNewInstanceCreated = false;

    if (targetInstanceId) {
      const existing = await InstallationRepository.getById(targetInstanceId);
      if (!existing) {
        throw new Error(`Docelowa instalacja '${targetInstanceId}' nie istnieje.`);
      }
      targetInstance = existing;
    } else {
      // Create new isolated instance
      onProgress?.({
        stage: 'installing-engine',
        message: 'Konfigurowanie nowej instalacji Minecraft...',
        percent: 25,
      });

      const finalName = instanceName?.trim() || manifest.name || `Modpack-${Date.now()}`;
      targetInstance = await InstallationRepository.create({
        name: finalName,
        minecraftVersion: inspection.minecraftVersion,
        loader: inspection.loaderType,
        loaderVersion: inspection.loaderVersion,
        allocatedRamMb: 4096,
        customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
        resolutionWidth: 1280,
        resolutionHeight: 720,
        fullscreen: false,
      });
      isNewInstanceCreated = true;
    }

    const instanceDir = EnginePaths.getInstanceDir(targetInstance.id);
    const tempDownloadDir = path.join(EnginePaths.getTempMrpackDir(), `import-${Date.now()}`);

    try {
      await fs.promises.mkdir(tempDownloadDir, { recursive: true });

      // 3. Filter files to download (client only + selected optionals)
      const optionalSet = new Set(selectedOptionalPaths.map((p) => p.toLowerCase()));
      const filesToDownload: MrpackIndexFile[] = [];

      for (const file of manifest.files || []) {
        const envClient = file.env?.client || 'required';
        if (envClient === 'unsupported') continue;
        if (envClient === 'optional' && !optionalSet.has(file.path.toLowerCase())) continue;
        filesToDownload.push(file);
      }

      onProgress?.({
        stage: 'downloading-files',
        message: `Pobieranie plików modpacka (${filesToDownload.length} plików)...`,
        percent: 30,
        totalFiles: filesToDownload.length,
        completedFiles: 0,
      });

      // 4. Download and verify each file into temp directory
      const downloadedFilesStaging: Array<{ tempPath: string; relTargetPath: string; file: MrpackIndexFile }> = [];
      let completedCount = 0;

      for (const file of filesToDownload) {
        const safeRel = MrpackPathValidator.validateRelativePath(file.path);
        if (!safeRel.valid || !safeRel.normalized) {
          throw new Error(`Niebezpieczna ścieżka pliku [${file.path}]: ${safeRel.error}`);
        }

        const fileName = path.basename(safeRel.normalized);
        const tempDest = path.join(tempDownloadDir, `${completedCount}-${fileName}`);

        onProgress?.({
          stage: 'downloading-files',
          message: `Pobieranie: ${fileName} (${completedCount + 1}/${filesToDownload.length})`,
          percent: 30 + Math.floor((completedCount / filesToDownload.length) * 40),
          currentFile: fileName,
          totalFiles: filesToDownload.length,
          completedFiles: completedCount,
        });

        // Download with fallback URLs
        await this.downloadWithFallback(file.downloads, tempDest, file.fileSize);

        // Verify SHA-512 and SHA-1
        onProgress?.({
          stage: 'verifying-hashes',
          message: `Weryfikacja sumy kontrolnej: ${fileName}`,
          percent: 30 + Math.floor(((completedCount + 0.5) / filesToDownload.length) * 40),
          currentFile: fileName,
          totalFiles: filesToDownload.length,
          completedFiles: completedCount,
        });

        this.verifyFileHashes(tempDest, file.hashes, file.fileSize);

        downloadedFilesStaging.push({
          tempPath: tempDest,
          relTargetPath: safeRel.normalized,
          file,
        });

        completedCount++;
      }

      // 5. Copy downloaded files to destination instance directory
      for (const item of downloadedFilesStaging) {
        const finalDest = MrpackPathValidator.assertSafePath(instanceDir, item.relTargetPath);
        const parentDir = path.dirname(finalDest);
        if (!fs.existsSync(parentDir)) {
          await fs.promises.mkdir(parentDir, { recursive: true });
        }
        await fs.promises.copyFile(item.tempPath, finalDest);
      }

      // 6. Apply overrides (overrides/ and client-overrides/)
      onProgress?.({
        stage: 'applying-overrides',
        message: 'Kopiowanie plików konfiguracji (overrides)...',
        percent: 85,
      });

      const overrideReport = await MrpackOverrideManager.applyOverrides({
        archivePath,
        targetInstanceDir: instanceDir,
      });

      // 7. Generate initial lockfile (modpack.lock.json)
      onProgress?.({
        stage: 'generating-lockfile',
        message: 'Tworzenie pliku modpack.lock.json...',
        percent: 95,
      });

      const installedMods = await ModRepository.getModsForInstance(targetInstance.id);
      const lockfile = await ModpackLockfileManager.syncFromInstalledMods(
        targetInstance.id,
        installedMods
      );

      // Enhance lockfile with Modrinth file info
      for (const item of downloadedFilesStaging) {
        const fn = path.basename(item.relTargetPath).toLowerCase();
        const lockMod = lockfile.mods.find((m) => m.fileName.toLowerCase() === fn);
        if (lockMod) {
          lockMod.source = 'modrinth';
          lockMod.sha512 = item.file.hashes.sha512;
          lockMod.sha1 = item.file.hashes.sha1;
        }
      }
      await ModpackLockfileManager.saveLockfile(targetInstance.id, lockfile);

      // Clean up temp staging directory
      if (fs.existsSync(tempDownloadDir)) {
        await fs.promises.rm(tempDownloadDir, { recursive: true, force: true });
      }

      onProgress?.({
        stage: 'completed',
        message: 'Pomyślnie zaimportowano modpack!',
        percent: 100,
        totalFiles: filesToDownload.length,
        completedFiles: filesToDownload.length,
      });

      return {
        success: true,
        instanceId: targetInstance.id,
        instanceName: targetInstance.name,
        installedFilesCount: downloadedFilesStaging.length,
        appliedOverridesCount: overrideReport.appliedFiles.length,
      };
    } catch (err: any) {
      // Rollback on failure
      onProgress?.({
        stage: 'rollback',
        message: `Błąd importu: ${err.message}. Wycofywanie zmian...`,
        percent: 0,
      });

      if (fs.existsSync(tempDownloadDir)) {
        try {
          await fs.promises.rm(tempDownloadDir, { recursive: true, force: true });
        } catch {}
      }

      if (isNewInstanceCreated && targetInstance?.id) {
        try {
          await InstallationRepository.delete(targetInstance.id);
        } catch {}
      }

      onProgress?.({
        stage: 'failed',
        message: `Import nie powiódł się: ${err.message}`,
        percent: 0,
      });

      return {
        success: false,
        error: err.message || 'Wystąpił nieoczekiwany błąd podczas importu modpacka.',
      };
    }
  }

  /**
   * Downloads a file from an array of download URLs with automatic fallback,
   * validating HTTPS, allowed host whitelist, and redirect safety.
   */
  private static async downloadWithFallback(
    urls: string[],
    destPath: string,
    expectedSize?: number
  ): Promise<void> {
    let lastError: Error | null = null;

    for (const url of urls) {
      try {
        await this.downloadSingleUrl(url, destPath, expectedSize);
        return; // Success
      } catch (err: any) {
        lastError = err;
        // Clean up partial file before fallback
        if (fs.existsSync(destPath)) {
          try {
            await fs.promises.unlink(destPath);
          } catch {}
        }
      }
    }

    throw new Error(
      `Nie udało się pobrać pliku z żadnego z podanych adresów (${urls.join(', ')}): ${lastError?.message}`
    );
  }

  /**
   * Downloads a single URL with redirect following and host whitelist validation.
   */
  private static async downloadSingleUrl(
    urlString: string,
    destPath: string,
    expectedSize?: number,
    redirectCount = 0
  ): Promise<void> {
    if (redirectCount > 3) {
      throw new Error(`Zbyt wiele przekierowań HTTP (> 3) dla adresu: ${urlString}`);
    }

    const parsed = new URL(urlString);

    // Protocol check
    if (parsed.protocol !== 'https:') {
      throw new Error(`Zabroniony protokół [${parsed.protocol}]. Dozwolony jest wyłącznie HTTPS.`);
    }

    // Host check
    const hostname = parsed.hostname.toLowerCase();
    if (!this.APPROVED_HOSTS.has(hostname)) {
      throw new Error(`Niedozwolony host pobierania: '${hostname}'. Dozwolone są wyłącznie oficjalne serwery CDN.`);
    }

    // Block private IPs / localhost
    if (
      hostname === 'localhost' ||
      hostname === '127.0.0.1' ||
      hostname === '::1' ||
      /^10\./.test(hostname) ||
      /^192\.168\./.test(hostname) ||
      /^172\.(1[6-9]|2[0-9]|3[0-1])\./.test(hostname) ||
      /^169\.254\./.test(hostname)
    ) {
      throw new Error(`Zablokowano próbę połączenia z adresem lokalnym / prywatnym: ${hostname}`);
    }

    return new Promise((resolve, reject) => {
      const fileStream = fs.createWriteStream(destPath);

      const req = https.get(
        urlString,
        {
          headers: {
            'User-Agent': 'ZombekClient/0.1.0 (https://github.com/zombekclient/zombekclient)',
          },
        },
        (res) => {
          // Handle HTTP 3xx redirects
          if (res.statusCode && res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
            fileStream.close();
            const nextUrl = new URL(res.headers.location, urlString).toString();
            this.downloadSingleUrl(nextUrl, destPath, expectedSize, redirectCount + 1)
              .then(resolve)
              .catch(reject);
            return;
          }

          if (res.statusCode !== 200) {
            fileStream.close();
            reject(new Error(`Serwer zwrócił kod błędu HTTP ${res.statusCode} dla adresu: ${urlString}`));
            return;
          }

          res.pipe(fileStream);

          fileStream.on('finish', () => {
            fileStream.close();
            resolve();
          });
        }
      );

      req.on('error', (err) => {
        fileStream.close();
        reject(err);
      });

      req.setTimeout(30000, () => {
        req.destroy(new Error(`Przekroczono limit czasu pobierania (30s) dla: ${urlString}`));
      });
    });
  }

  /**
   * Verifies SHA-512, SHA-1, and fileSize of a downloaded file.
   */
  private static verifyFileHashes(
    filePath: string,
    hashes: { sha512?: string; sha1?: string },
    expectedSize?: number
  ): void {
    if (!fs.existsSync(filePath)) {
      throw new Error(`Plik nie istnieje na dysku: ${filePath}`);
    }

    const stat = fs.statSync(filePath);
    if (expectedSize && stat.size !== expectedSize) {
      try {
        fs.unlinkSync(filePath);
      } catch {}
      throw new Error(
        `Niezgodność rozmiaru pliku (oczekiwano ${expectedSize} B, pobrano ${stat.size} B). Plik usunięto.`
      );
    }

    const data = fs.readFileSync(filePath);

    if (hashes.sha512) {
      const actualSha512 = crypto.createHash('sha512').update(data).digest('hex');
      if (actualSha512.toLowerCase() !== hashes.sha512.toLowerCase()) {
        try {
          fs.unlinkSync(filePath);
        } catch {}
        throw new Error(
          `Błąd sumy kontrolnej SHA-512 dla pobranego pliku. Plik usunięto ze względów bezpieczeństwa.`
        );
      }
    }

    if (hashes.sha1) {
      const actualSha1 = crypto.createHash('sha1').update(data).digest('hex');
      if (actualSha1.toLowerCase() !== hashes.sha1.toLowerCase()) {
        try {
          fs.unlinkSync(filePath);
        } catch {}
        throw new Error(
          `Błąd sumy kontrolnej SHA-1 dla pobranego pliku. Plik usunięto ze względów bezpieczeństwa.`
        );
      }
    }
  }
}
