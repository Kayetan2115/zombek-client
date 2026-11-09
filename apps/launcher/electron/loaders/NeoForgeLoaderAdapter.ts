import * as fs from 'node:fs';
import * as path from 'node:path';
import * as cp from 'node:child_process';
import { EnginePaths } from '../engine/paths.js';
import { DownloadManager } from '../engine/DownloadManager.js';
import { JavaRuntimeManager } from '../engine/JavaRuntimeManager.js';
import { VersionManifestService } from '../engine/VersionManifestService.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { NeoForgeMetaService } from './NeoForgeMetaService.js';
import type { ModLoaderAdapter } from './ModLoaderAdapter.js';
import type {
  FabricLoaderVersion,
  NeoForgeVersionInfo,
  InstanceRecord,
} from '@zombek/shared-types';
import type { MojangVersionPackage, MojangLibrary, DownloadTask } from '../engine/types.js';

export interface NeoForgeInstallCallbacks {
  onStage?: (stage: string, current: number, total: number) => void;
  onLog?: (line: string) => void;
}

export class NeoForgeLoaderAdapter implements ModLoaderAdapter {
  private static instance: NeoForgeLoaderAdapter | null = null;
  public readonly loaderType = 'neoforge' as const;

  public static getInstance(): NeoForgeLoaderAdapter {
    if (!this.instance) {
      this.instance = new NeoForgeLoaderAdapter();
    }
    return this.instance;
  }

  /**
   * Checks if the given Minecraft version is supported by NeoForge.
   * NeoForge supports 1.20.1 (transition) and 1.20.2+ (modern).
   */
  public async isVersionSupported(minecraftVersion: string): Promise<boolean> {
    const match = NeoForgeMetaService.getNeoForgePrefix(minecraftVersion);
    return match !== null;
  }

  /**
   * Adapts NeoForge versions to the generic ModLoaderAdapter contract.
   */
  public async getLoaderVersions(minecraftVersion: string): Promise<FabricLoaderVersion[]> {
    const neoVersions = await this.getNeoForgeVersions(minecraftVersion);
    return neoVersions.map((v) => {
      const parts = v.rawVersion.split('.');
      const buildNum = parseInt(parts[parts.length - 1] || '0', 10);
      return {
        version: v.version,
        build: isNaN(buildNum) ? 0 : buildNum,
        maven: v.artifactCoordinates,
        separator: '.',
        stable: Boolean(v.isRecommended),
      };
    });
  }

  /**
   * Returns full NeoForgeVersionInfo list for a Minecraft version.
   */
  public async getNeoForgeVersions(
    minecraftVersion: string,
    forceRefresh: boolean = false
  ): Promise<NeoForgeVersionInfo[]> {
    return NeoForgeMetaService.getNeoForgeVersionsForGame(minecraftVersion, forceRefresh);
  }

  /**
   * Resolves the version profile for NeoForge launch.
   * Merges base Vanilla version package with installed NeoForge version JSON.
   */
  public async resolveProfile(
    gameVersion: string,
    loaderVersion?: string
  ): Promise<MojangVersionPackage> {
    let rawVersion = loaderVersion;
    if (!rawVersion) {
      const versions = await this.getNeoForgeVersions(gameVersion);
      if (!versions || versions.length === 0) {
        throw new Error(`Brak dostępnych wersji NeoForge dla Minecraft ${gameVersion}`);
      }
      const rec = versions.find((v) => v.isRecommended);
      rawVersion = rec ? rec.rawVersion : versions[0].rawVersion;
    }

    const versionsDir = EnginePaths.getVersionsDir();
    // Candidate profile folder names
    const candidates = [
      path.join(versionsDir, `neoforge-${rawVersion}`, `neoforge-${rawVersion}.json`),
      path.join(versionsDir, `${gameVersion}-forge-${rawVersion}`, `${gameVersion}-forge-${rawVersion}.json`),
      path.join(versionsDir, `${gameVersion}-neoforge-${rawVersion}`, `${gameVersion}-neoforge-${rawVersion}.json`),
      path.join(versionsDir, rawVersion, `${rawVersion}.json`),
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
        `Profil wersji NeoForge dla '${gameVersion}' (${rawVersion}) nie został jeszcze zainstalowany. Kliknij 'Zainstaluj NeoForge' w zakładce Versions.`
      );
    }

    // Deduplicate and combine libraries
    const libMap = new Map<string, MojangLibrary>();
    for (const lib of vanillaPackage.libraries || []) {
      const key = lib.name || JSON.stringify(lib);
      libMap.set(key, lib);
    }

    for (const lib of versionData.libraries || []) {
      const key = lib.name || JSON.stringify(lib);
      libMap.set(key, lib);
    }

    // Combine JVM arguments
    const jvmArgs: (string | any)[] = [];
    if (vanillaPackage.arguments?.jvm) {
      jvmArgs.push(...vanillaPackage.arguments.jvm);
    }
    if (versionData.arguments?.jvm) {
      jvmArgs.push(...versionData.arguments.jvm);
    }

    // Combine Game arguments
    const gameArgs: (string | any)[] = [];
    if (vanillaPackage.arguments?.game) {
      gameArgs.push(...vanillaPackage.arguments.game);
    }
    if (versionData.arguments?.game) {
      gameArgs.push(...versionData.arguments.game);
    }

    return {
      id: versionData.id,
      inheritsFrom: gameVersion,
      type: versionData.type || vanillaPackage.type,
      time: versionData.time || vanillaPackage.time,
      releaseTime: versionData.releaseTime || vanillaPackage.releaseTime,
      mainClass: versionData.mainClass || 'cpw.mods.bootstraplauncher.BootstrapLauncher',
      arguments: {
        jvm: jvmArgs,
        game: gameArgs,
      },
      libraries: Array.from(libMap.values()),
      assetIndex: vanillaPackage.assetIndex,
      assets: vanillaPackage.assets,
      downloads: vanillaPackage.downloads,
      javaVersion: versionData.javaVersion || vanillaPackage.javaVersion,
      complianceLevel: vanillaPackage.complianceLevel,
    };
  }

  /**
   * Installs NeoForge client for an instance across 9 defined stages.
   */
  public async install(
    instance: InstanceRecord,
    neoForgeVersion: string,
    downloadManager?: DownloadManager,
    callbacks?: NeoForgeInstallCallbacks
  ): Promise<{ versionId: string; profile: MojangVersionPackage }> {
    const gameVersion = instance.minecraftVersion;

    // 1. Preparing installation
    callbacks?.onStage?.('Przygotowywanie instalacji NeoForge...', 1, 9);
    callbacks?.onLog?.(`Rozpoczęcie przygotowywania środowiska NeoForge dla instancji ${instance.id}...`);

    const versions = await this.getNeoForgeVersions(gameVersion);
    if (!versions || versions.length === 0) {
      throw new Error(`Brak kompatybilnych wydań NeoForge dla Minecraft ${gameVersion}`);
    }

    const selected = neoForgeVersion
      ? versions.find((v) => v.version === neoForgeVersion || v.rawVersion === neoForgeVersion)
      : versions.find((v) => v.isRecommended) || versions[0];

    if (!selected) {
      throw new Error(`Wersja NeoForge '${neoForgeVersion}' nie jest dostępna dla Minecraft ${gameVersion}`);
    }

    const versionStr = selected.version;
    callbacks?.onLog?.(`Wybrana wersja NeoForge: ${versionStr} (generacja: ${selected.generation})`);

    // Prepare isolated staging directory
    const isolatedDir = path.join(EnginePaths.getGameDataDir(), 'neoforge-staging');
    if (fs.existsSync(isolatedDir)) {
      try {
        await fs.promises.rm(isolatedDir, { recursive: true, force: true });
      } catch {
        // ignore
      }
    }
    await fs.promises.mkdir(isolatedDir, { recursive: true });

    // 2. Downloading NeoForge installer
    callbacks?.onStage?.('Pobieranie oficjalnego instalatora NeoForge...', 2, 9);
    callbacks?.onLog?.(`Pobieranie instalatora z adresu: ${selected.installerUrl}`);

    const installerJarName =
      selected.generation === 'transition'
        ? `forge-${versionStr}-installer.jar`
        : `neoforge-${versionStr}-installer.jar`;
    const installerJarPath = path.join(isolatedDir, installerJarName);

    const dm = downloadManager || new DownloadManager(4);
    const dlTask: DownloadTask = {
      id: `neoforge-installer-${versionStr}`,
      url: selected.installerUrl,
      destination: installerJarPath,
      description: `Instalator NeoForge (${versionStr})`,
    };
    await dm.downloadBatch([dlTask], `Pobieranie instalatora NeoForge ${versionStr}...`);

    // 3. Verifying installer
    callbacks?.onStage?.('Weryfikacja integralności instalatora...', 3, 9);
    if (!fs.existsSync(installerJarPath)) {
      throw new Error(`Nie udało się pobrać instalatora NeoForge z adresu ${selected.installerUrl}`);
    }

    const stat = await fs.promises.stat(installerJarPath);
    if (stat.size < 100_000) {
      throw new Error(`Pobrany plik instalatora NeoForge jest uszkodzony lub zbyt mały (${stat.size} bajtów).`);
    }

    // 4. Preparing base Minecraft files in staging
    callbacks?.onStage?.('Przygotowywanie plików bazowych Minecraft...', 4, 9);
    const stagingVersionsDir = path.join(isolatedDir, 'versions', gameVersion);
    await fs.promises.mkdir(stagingVersionsDir, { recursive: true });

    // Write minimal launcher_profiles.json in staging so installer accepts the directory
    await fs.promises.writeFile(
      path.join(isolatedDir, 'launcher_profiles.json'),
      JSON.stringify({ profiles: {} }, null, 2),
      'utf-8'
    );

    // Ensure vanilla client jar and json exist in staging
    const vanillaPkg = await VersionManifestService.getVersionPackage(gameVersion);
    const vanillaJarDest = path.join(stagingVersionsDir, `${gameVersion}.jar`);
    const vanillaJsonDest = path.join(stagingVersionsDir, `${gameVersion}.json`);

    await fs.promises.writeFile(vanillaJsonDest, JSON.stringify(vanillaPkg, null, 2), 'utf-8');

    const globalVanillaJar = path.join(EnginePaths.getVersionsDir(), gameVersion, `${gameVersion}.jar`);
    if (fs.existsSync(globalVanillaJar)) {
      await fs.promises.copyFile(globalVanillaJar, vanillaJarDest);
    } else if (vanillaPkg.downloads?.client) {
      const clientDlTask: DownloadTask = {
        id: `client-jar-${gameVersion}`,
        url: vanillaPkg.downloads.client.url,
        destination: vanillaJarDest,
        sha1: vanillaPkg.downloads.client.sha1,
        size: vanillaPkg.downloads.client.size,
        description: `Minecraft Client JAR (${gameVersion})`,
      };
      await dm.downloadBatch([clientDlTask], 'Pobieranie bazowego klienta Minecraft...');
    }

    // 5. Installing NeoForge via official installer
    callbacks?.onStage?.('Instalowanie NeoForge i wykonywanie procesorów...', 5, 9);
    callbacks?.onLog?.('Uruchamianie oficjalnego procesu instalacji NeoForge z parametrem --installClient...');

    // Detect Java runtime based on requirements (Java 21 for 1.20.5+, Java 17 for 1.20.1-1.20.4)
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
          callbacks?.onLog?.(`[NEOFORGE INSTALLER]: ${text}`);
        }
      });

      child.stderr.on('data', (d) => {
        const text = d.toString('utf-8').trim();
        if (text) {
          callbacks?.onLog?.(`[NEOFORGE STDERR]: ${text}`);
        }
      });

      child.on('error', (err) => {
        reject(new Error(`Nie udało się uruchomić instalatora NeoForge: ${err.message}`));
      });

      child.on('close', (code) => {
        if (code === 0) {
          callbacks?.onLog?.('Instalator NeoForge zakończył instalację pomyślnie.');
          resolve();
        } else {
          reject(new Error(`Instalator NeoForge zakończył się kodem błędu: ${code}`));
        }
      });
    });

    // 6. Resolving dependencies & synchronizing libraries
    callbacks?.onStage?.('Rozwiązywanie zależności i bibliotek...', 6, 9);
    callbacks?.onLog?.('Kopiowanie bibliotek i profilu ze środowiska tymczasowego...');

    const stagingLibsDir = path.join(isolatedDir, 'libraries');
    const globalLibsDir = EnginePaths.getLibrariesDir();

    if (fs.existsSync(stagingLibsDir)) {
      await this.copyDirectoryRecursive(stagingLibsDir, globalLibsDir);
    }

    // Copy generated version json
    const stagingVersions = path.join(isolatedDir, 'versions');
    let generatedVersionId: string | null = null;

    if (fs.existsSync(stagingVersions)) {
      const subdirs = await fs.promises.readdir(stagingVersions);
      for (const sub of subdirs) {
        if (sub !== gameVersion) {
          generatedVersionId = sub;
          const srcDir = path.join(stagingVersions, sub);
          const dstDir = path.join(EnginePaths.getVersionsDir(), sub);
          await this.copyDirectoryRecursive(srcDir, dstDir);
          break;
        }
      }
    }

    if (!generatedVersionId) {
      generatedVersionId =
        selected.generation === 'transition'
          ? `${gameVersion}-forge-${versionStr}`
          : `neoforge-${versionStr}`;
    }

    // 7. Preparing launch profile
    callbacks?.onStage?.('Przygotowywanie profilu uruchomieniowego...', 7, 9);
    const resolvedProfile = await this.resolveProfile(gameVersion, versionStr);
    callbacks?.onLog?.(`Profil uruchomieniowy dla ${versionStr} został pomyślnie zweryfikowany.`);

    // 8. Verifying installation & instance directories
    callbacks?.onStage?.('Weryfikacja instalacji i izolacja instancji...', 8, 9);
    const instanceDir = path.join(EnginePaths.getInstancesDir(), instance.id);
    const subdirsToEnsure = ['mods', 'disabled-mods', 'config', 'saves', 'resourcepacks', 'screenshots', 'natives', 'logs'];
    for (const sub of subdirsToEnsure) {
      const p = path.join(instanceDir, sub);
      if (!fs.existsSync(p)) {
        await fs.promises.mkdir(p, { recursive: true });
      }
    }

    // Update instance record
    await InstallationRepository.update(instance.id, {
      loader: 'neoforge',
      loaderVersion: versionStr,
      lastPlayed: new Date().toISOString(),
    });

    // 9. Completed
    callbacks?.onStage?.('Instalacja NeoForge zakończona pomyślnie.', 9, 9);
    callbacks?.onLog?.(`ZombekClient: NeoForge (${versionStr}) dla instancji ${instance.id} gotowy do gry!`);

    // Clean up staging directory
    try {
      await fs.promises.rm(isolatedDir, { recursive: true, force: true });
    } catch {
      // ignore
    }

    return {
      versionId: generatedVersionId,
      profile: resolvedProfile,
    };
  }

  private async copyDirectoryRecursive(source: string, destination: string): Promise<void> {
    if (!fs.existsSync(destination)) {
      await fs.promises.mkdir(destination, { recursive: true });
    }

    const entries = await fs.promises.readdir(source, { withFileTypes: true });
    for (const entry of entries) {
      const srcPath = path.join(source, entry.name);
      const dstPath = path.join(destination, entry.name);

      if (entry.isDirectory()) {
        await this.copyDirectoryRecursive(srcPath, dstPath);
      } else {
        const dstDir = path.dirname(dstPath);
        if (!fs.existsSync(dstDir)) {
          await fs.promises.mkdir(dstDir, { recursive: true });
        }
        await fs.promises.copyFile(srcPath, dstPath);
      }
    }
  }
}
