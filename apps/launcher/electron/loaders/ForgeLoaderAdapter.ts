import * as fs from 'node:fs';
import * as path from 'node:path';
import * as cp from 'node:child_process';
import type { ModLoaderAdapter } from './ModLoaderAdapter.js';
import { ForgeMetaService, FORGE_MAVEN_BASE } from './ForgeMetaService.js';
import { ForgeProcessorManager } from './ForgeProcessorManager.js';
import { VersionManifestService } from '../engine/VersionManifestService.js';
import { GameInstaller } from '../engine/GameInstaller.js';
import { DownloadManager } from '../engine/DownloadManager.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { EnginePaths } from '../engine/paths.js';
import { JavaRuntimeManager } from '../engine/JavaRuntimeManager.js';
import type {
  MinecraftLoaderType,
  InstanceRecord,
  ForgeVersionInfo,
  ForgeInstallProfile,
  FabricLoaderVersion,
} from '@zombek/shared-types';
import type { MojangVersionPackage, DownloadTask } from '../engine/types.js';

export interface ForgeInstallCallbacks {
  onStage?: (stage: string, step: number, totalSteps: number) => void;
  onLog?: (line: string) => void;
}

export class ForgeLoaderAdapter implements ModLoaderAdapter {
  private static instance: ForgeLoaderAdapter;
  public readonly loaderType: MinecraftLoaderType = 'forge';

  public static getInstance(): ForgeLoaderAdapter {
    if (!ForgeLoaderAdapter.instance) {
      ForgeLoaderAdapter.instance = new ForgeLoaderAdapter();
    }
    return ForgeLoaderAdapter.instance;
  }

  public async isVersionSupported(gameVersion: string): Promise<boolean> {
    try {
      const versions = await ForgeMetaService.getForgeVersionsForGame(gameVersion);
      return versions.length > 0;
    } catch {
      return false;
    }
  }

  public async getForgeVersions(gameVersion: string): Promise<ForgeVersionInfo[]> {
    return ForgeMetaService.getForgeVersionsForGame(gameVersion);
  }

  /**
   * Adapter compatibility method for generic ModLoaderAdapter
   */
  public async getLoaderVersions(gameVersion: string): Promise<FabricLoaderVersion[]> {
    const forgeVersions = await this.getForgeVersions(gameVersion);
    return forgeVersions.map((f, idx) => ({
      version: f.rawVersion,
      stable: f.isRecommended,
      maven: `net.minecraftforge:forge:${f.version}`,
      build: idx,
      separator: '-',
    }));
  }

  /**
   * Resolves full version string from raw build or full version
   */
  public resolveFullVersion(gameVersion: string, rawVersion: string): string {
    if (rawVersion.startsWith(`${gameVersion}-`)) {
      return rawVersion;
    }
    return `${gameVersion}-${rawVersion}`;
  }

  /**
   * Resolves the version profile for Forge launching
   */
  public async resolveProfile(
    gameVersion: string,
    loaderVersion?: string
  ): Promise<MojangVersionPackage> {
    let rawVersion = loaderVersion;
    if (!rawVersion) {
      const versions = await this.getForgeVersions(gameVersion);
      if (!versions || versions.length === 0) {
        throw new Error(`Brak dostępnych wersji Forge dla Minecraft ${gameVersion}`);
      }
      const rec = versions.find((v) => v.isRecommended);
      rawVersion = rec ? rec.rawVersion : versions[0].rawVersion;
    }

    const fullVersion = this.resolveFullVersion(gameVersion, rawVersion);
    const forgeVersionId = `${gameVersion}-forge-${rawVersion}`;

    // Look for installed Forge version JSON in versions/
    const versionsDir = EnginePaths.getVersionsDir();
    const candidates = [
      path.join(versionsDir, forgeVersionId, `${forgeVersionId}.json`),
      path.join(versionsDir, fullVersion, `${fullVersion}.json`),
    ];

    let versionData: MojangVersionPackage | null = null;
    for (const cand of candidates) {
      if (fs.existsSync(cand)) {
        try {
          const raw = await fs.promises.readFile(cand, 'utf-8');
          versionData = JSON.parse(raw) as MojangVersionPackage;
          break;
        } catch {
          // ignore corrupted json
        }
      }
    }

    // Base Vanilla version package
    const vanillaPackage = await VersionManifestService.getVersionPackage(gameVersion);

    if (!versionData) {
      throw new Error(
        `Profil wersji Forge '${forgeVersionId}' nie został jeszcze zainstalowany. Kliknij 'Zainstaluj Forge' w zakładce Versions.`
      );
    }

    // Merge libraries
    const libraryMap = new Map<string, any>();
    for (const lib of vanillaPackage.libraries || []) {
      libraryMap.set(lib.name, lib);
    }
    for (const lib of versionData.libraries || []) {
      libraryMap.set(lib.name, lib);
    }

    const mergedJvmArgs = [
      ...(vanillaPackage.arguments?.jvm || []),
      ...(versionData.arguments?.jvm || []),
    ];
    const mergedGameArgs = [
      ...(vanillaPackage.arguments?.game || []),
      ...(versionData.arguments?.game || []),
    ];

    return {
      ...vanillaPackage,
      ...versionData,
      id: versionData.id || forgeVersionId,
      inheritsFrom: gameVersion,
      mainClass: versionData.mainClass || 'net.minecraftforge.bootstrap.ForgeBootstrap',
      arguments: {
        jvm: mergedJvmArgs,
        game: mergedGameArgs,
      },
      libraries: Array.from(libraryMap.values()),
    };
  }

  /**
   * Installs Forge using the official installer in an isolated directory
   */
  public async install(
    instance: InstanceRecord,
    forgeVersion: string,
    downloadManager?: DownloadManager,
    callbacks?: ForgeInstallCallbacks
  ): Promise<{ versionId: string; profile: MojangVersionPackage }> {
    const gameVersion = instance.minecraftVersion;
    const fullVersion = this.resolveFullVersion(gameVersion, forgeVersion);
    const forgeVersionId = `${gameVersion}-forge-${forgeVersion}`;
    const dm = downloadManager || new DownloadManager(8);

    callbacks?.onLog?.(`Rozpoczynanie instalacji Minecraft Forge ${fullVersion}...`);

    // 1. Preparing installation & directory structure
    callbacks?.onStage?.('Przygotowywanie struktury katalogów instalacji...', 1, 9);
    const instanceDir = EnginePaths.getInstanceDir(instance.id);
    const subDirs = ['saves', 'resourcepacks', 'config', 'mods', 'disabled-mods', 'screenshots', 'natives', 'logs'];
    for (const sub of subDirs) {
      const p = path.join(instanceDir, sub);
      if (!fs.existsSync(p)) {
        await fs.promises.mkdir(p, { recursive: true });
      }
    }

    // Prepare isolated staging directory inside game-data to isolate installer
    const isolatedDir = path.join(EnginePaths.getGameDataDir(), 'forge-staging');
    if (!fs.existsSync(isolatedDir)) {
      await fs.promises.mkdir(isolatedDir, { recursive: true });
    }
    // Forge installer requires launcher_profiles.json
    const mockProfilesJson = path.join(isolatedDir, 'launcher_profiles.json');
    if (!fs.existsSync(mockProfilesJson)) {
      await fs.promises.writeFile(mockProfilesJson, JSON.stringify({ profiles: {} }), 'utf-8');
    }

    // 2. Downloading Forge installer
    callbacks?.onStage?.('Pobieranie oficjalnego instalatora Forge...', 2, 9);
    const installerUrl = `${FORGE_MAVEN_BASE}/${fullVersion}/forge-${fullVersion}-installer.jar`;
    const installerJarPath = path.join(isolatedDir, `forge-${fullVersion}-installer.jar`);

    const dlTask: DownloadTask = {
      id: `forge-installer-${fullVersion}`,
      url: installerUrl,
      destination: installerJarPath,
      description: `Instalator Forge (${fullVersion})`,
    };
    await dm.downloadBatch([dlTask], `Pobieranie instalatora Forge ${fullVersion}...`);

    // 3. Verifying installer
    callbacks?.onStage?.('Weryfikacja integralności instalatora...', 3, 9);
    if (!fs.existsSync(installerJarPath)) {
      throw new Error(`Nie udało się pobrać instalatora Forge z adresu ${installerUrl}`);
    }

    // Extract install_profile.json and version.json to verify
    const extractTemp = path.join(isolatedDir, 'temp-extract');
    if (!fs.existsSync(extractTemp)) {
      await fs.promises.mkdir(extractTemp, { recursive: true });
    }

    let installProfile: ForgeInstallProfile | null = null;
    let versionJson: any = null;

    try {
      // Use tar to unpack install_profile.json
      await new Promise<void>((resolve, reject) => {
        const proc = cp.spawn(
          'tar.exe',
          ['-xf', installerJarPath, '-C', extractTemp, 'install_profile.json', 'version.json'],
          { stdio: ['ignore', 'pipe', 'pipe'], windowsHide: true }
        );
        proc.on('close', (code) => {
          if (code === 0) resolve();
          else reject(new Error(`Błąd rozpakowania instalatora Forge (kod: ${code})`));
        });
        proc.on('error', reject);
      });

      const ipPath = path.join(extractTemp, 'install_profile.json');
      if (fs.existsSync(ipPath)) {
        const raw = await fs.promises.readFile(ipPath, 'utf-8');
        installProfile = JSON.parse(raw) as ForgeInstallProfile;
      }

      const vjPath = path.join(extractTemp, 'version.json');
      if (fs.existsSync(vjPath)) {
        const raw = await fs.promises.readFile(vjPath, 'utf-8');
        versionJson = JSON.parse(raw);
      }
    } catch {
      // If direct extraction fails, installer may be legacy or packed differently
    }

    // 4. Preparing base Minecraft files in staging
    callbacks?.onStage?.('Przygotowywanie plików bazowych Minecraft...', 4, 9);
    const stagingVersionsDir = path.join(isolatedDir, 'versions', gameVersion);
    if (!fs.existsSync(stagingVersionsDir)) {
      await fs.promises.mkdir(stagingVersionsDir, { recursive: true });
    }

    // Ensure vanilla client jar and json exist in staging
    const vanillaPkg = await VersionManifestService.getVersionPackage(gameVersion);
    const vanillaJarDest = path.join(stagingVersionsDir, `${gameVersion}.jar`);
    const vanillaJsonDest = path.join(stagingVersionsDir, `${gameVersion}.json`);

    await fs.promises.writeFile(vanillaJsonDest, JSON.stringify(vanillaPkg, null, 2), 'utf-8');

    // Copy or download vanilla client jar
    const globalVanillaJar = path.join(EnginePaths.getVersionsDir(), gameVersion, `${gameVersion}.jar`);
    if (fs.existsSync(globalVanillaJar)) {
      await fs.promises.copyFile(globalVanillaJar, vanillaJarDest);
    } else if (vanillaPkg.downloads?.client) {
      await dm.downloadBatch(
        [
          {
            id: `client-jar-${gameVersion}`,
            url: vanillaPkg.downloads.client.url,
            destination: vanillaJarDest,
            sha1: vanillaPkg.downloads.client.sha1,
            size: vanillaPkg.downloads.client.size,
            description: `Minecraft Client JAR (${gameVersion})`,
          },
        ],
        'Pobieranie bazowego klienta Minecraft...'
      );
    }

    // 5. Downloading Forge libraries
    callbacks?.onStage?.('Pobieranie wymaganych bibliotek Forge...', 5, 9);
    // Link or sync global libraries folder to isolated staging libraries
    const stagingLibrariesDir = path.join(isolatedDir, 'libraries');
    if (!fs.existsSync(stagingLibrariesDir)) {
      await fs.promises.mkdir(stagingLibrariesDir, { recursive: true });
    }

    // 6. Running Forge processors / official installer
    callbacks?.onStage?.('Wykonywanie instalatora i procesorów Forge...', 6, 9);
    callbacks?.onLog?.('Uruchamianie oficjalnego procesu instalacji Forge z parametrem --installClient...');

    // Detect Java runtime
    const javaResult = await JavaRuntimeManager.detectJavaExecutable();
    const javaExecutable = javaResult.valid ? javaResult.path : 'java';

    const jvmArgs = [
      '-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT',
      '-jar',
      installerJarPath,
      '--installClient',
      isolatedDir,
    ];

    await new Promise<void>((resolve, reject) => {
      const child = cp.spawn(javaExecutable, jvmArgs, {
        cwd: isolatedDir,
        stdio: ['ignore', 'pipe', 'pipe'],
      });

      child.stdout.on('data', (d) => {
        const text = d.toString('utf-8').trim();
        if (text) {
          callbacks?.onLog?.(`[FORGE INSTALLER]: ${text}`);
        }
      });

      child.stderr.on('data', (d) => {
        const text = d.toString('utf-8').trim();
        if (text) {
          callbacks?.onLog?.(`[FORGE STDERR]: ${text}`);
        }
      });

      child.on('error', (err) => {
        reject(new Error(`Nie udało się uruchomić instalatora Forge: ${err.message}`));
      });

      child.on('close', (code) => {
        if (code === 0) {
          callbacks?.onLog?.('Instalator Forge zakończył instalację pomyślnie.');
          resolve();
        } else {
          reject(new Error(`Instalator Forge zakończył się kodem błędu: ${code}`));
        }
      });
    });

    // 7. Generating launch profile & synchronizing artifacts
    callbacks?.onStage?.('Generowanie profilu uruchomieniowego Forge...', 7, 9);
    // Copy generated version json into global versions directory
    const candidates = [
      path.join(isolatedDir, 'versions', forgeVersionId, `${forgeVersionId}.json`),
      path.join(isolatedDir, 'versions', fullVersion, `${fullVersion}.json`),
    ];

    let foundVersionJsonPath: string | null = null;
    for (const c of candidates) {
      if (fs.existsSync(c)) {
        foundVersionJsonPath = c;
        break;
      }
    }

    if (!foundVersionJsonPath && versionJson) {
      // Fallback: save versionJson extracted from installer
      foundVersionJsonPath = path.join(isolatedDir, 'versions', forgeVersionId, `${forgeVersionId}.json`);
      await fs.promises.mkdir(path.dirname(foundVersionJsonPath), { recursive: true });
      await fs.promises.writeFile(foundVersionJsonPath, JSON.stringify(versionJson, null, 2), 'utf-8');
    }

    if (!foundVersionJsonPath) {
      throw new Error(`Instalator Forge nie wygenerował pliku wersji w ${isolatedDir}`);
    }

    // Read and copy to global versions/
    const versionContent = await fs.promises.readFile(foundVersionJsonPath, 'utf-8');
    const targetVersionDir = path.join(EnginePaths.getVersionsDir(), forgeVersionId);
    if (!fs.existsSync(targetVersionDir)) {
      await fs.promises.mkdir(targetVersionDir, { recursive: true });
    }
    await fs.promises.writeFile(path.join(targetVersionDir, `${forgeVersionId}.json`), versionContent, 'utf-8');

    // Synchronize generated libraries (e.g. patched client, universal jar, etc.) to global libraries
    if (fs.existsSync(stagingLibrariesDir)) {
      await this.syncDirectory(stagingLibrariesDir, EnginePaths.getLibrariesDir());
    }

    // 8. Verifying installation
    callbacks?.onStage?.('Weryfikacja kompletności instalacji Forge...', 8, 9);
    const resolvedProfile = await this.resolveProfile(gameVersion, forgeVersion);

    // Update instance record in repository
    await InstallationRepository.update(instance.id, {
      loader: 'forge',
      loaderVersion: forgeVersion,
      resolvedVersionId: forgeVersionId,
      status: 'ready',
    });

    // 9. Completed
    callbacks?.onStage?.('Instalacja Forge zakończona pomyślnie!', 9, 9);
    callbacks?.onLog?.(`Profil Forge ${fullVersion} jest gotowy do uruchomienia.`);

    // Cleanup temp extract
    try {
      if (fs.existsSync(extractTemp)) {
        await fs.promises.rm(extractTemp, { recursive: true, force: true });
      }
    } catch {
      // ignore
    }

    return {
      versionId: forgeVersionId,
      profile: resolvedProfile,
    };
  }

  /**
   * Recursively copies files from source directory to target directory without overwriting newer files
   */
  private async syncDirectory(src: string, dest: string): Promise<void> {
    if (!fs.existsSync(src)) return;
    if (!fs.existsSync(dest)) {
      await fs.promises.mkdir(dest, { recursive: true });
    }

    const entries = await fs.promises.readdir(src, { withFileTypes: true });
    for (const entry of entries) {
      const srcPath = path.join(src, entry.name);
      const destPath = path.join(dest, entry.name);

      if (entry.isDirectory()) {
        await this.syncDirectory(srcPath, destPath);
      } else if (entry.isFile()) {
        if (!fs.existsSync(destPath)) {
          await fs.promises.copyFile(srcPath, destPath);
        }
      }
    }
  }
}
