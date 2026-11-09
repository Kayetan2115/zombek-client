import * as fs from 'node:fs';
import * as path from 'node:path';
import type { ModLoaderAdapter } from './ModLoaderAdapter.js';
import { FabricMetaService } from './FabricMetaService.js';
import { VersionManifestService } from '../engine/VersionManifestService.js';
import { GameInstaller } from '../engine/GameInstaller.js';
import { DownloadManager } from '../engine/DownloadManager.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { EnginePaths } from '../engine/paths.js';
import type { MinecraftLoaderType, InstanceRecord, FabricLoaderVersion } from '@zombek/shared-types';
import type { MojangVersionPackage } from '../engine/types.js';

export class FabricLoaderAdapter implements ModLoaderAdapter {
  private static instance: FabricLoaderAdapter;
  public readonly loaderType: MinecraftLoaderType = 'fabric';

  public static getInstance(): FabricLoaderAdapter {
    if (!FabricLoaderAdapter.instance) {
      FabricLoaderAdapter.instance = new FabricLoaderAdapter();
    }
    return FabricLoaderAdapter.instance;
  }

  public async isVersionSupported(gameVersion: string): Promise<boolean> {
    try {
      const versions = await FabricMetaService.getGameVersions();
      return versions.some((v) => v.version === gameVersion);
    } catch {
      // In case offline, assume standard release versions are supported
      return true;
    }
  }

  public async getLoaderVersions(gameVersion: string): Promise<FabricLoaderVersion[]> {
    return FabricMetaService.getLoaderVersions(gameVersion);
  }

  /**
   * Resolves the complete version package for Fabric, merging Vanilla base with Fabric loader profile.
   */
  public async resolveProfile(
    gameVersion: string,
    loaderVersion?: string
  ): Promise<MojangVersionPackage> {
    let selectedLoaderVersion = loaderVersion;

    if (!selectedLoaderVersion) {
      const loaders = await this.getLoaderVersions(gameVersion);
      if (!loaders || loaders.length === 0) {
        throw new Error(`Brak dostępnych wersji Fabric Loader dla Minecraft ${gameVersion}`);
      }
      const stable = loaders.find((l) => l.stable);
      selectedLoaderVersion = stable ? stable.version : loaders[0].version;
    }

    // 1. Fetch Fabric profile
    const fabricProfile = await FabricMetaService.getProfileJson(gameVersion, selectedLoaderVersion);

    // 2. Fetch base Vanilla version package
    const vanillaPackage = await VersionManifestService.getVersionPackage(gameVersion);

    // 3. Merge libraries without duplicates
    const libraryMap = new Map<string, any>();
    for (const lib of vanillaPackage.libraries || []) {
      libraryMap.set(lib.name, lib);
    }
    for (const lib of fabricProfile.libraries || []) {
      libraryMap.set(lib.name, lib);
    }

    // 4. Merge JVM and Game arguments
    const mergedJvmArgs = [
      ...(vanillaPackage.arguments?.jvm || []),
      ...(fabricProfile.arguments?.jvm || []),
    ];
    const mergedGameArgs = [
      ...(vanillaPackage.arguments?.game || []),
      ...(fabricProfile.arguments?.game || []),
    ];

    const mergedPackage: MojangVersionPackage = {
      ...vanillaPackage,
      id: fabricProfile.id,
      inheritsFrom: gameVersion,
      mainClass: fabricProfile.mainClass || 'net.fabricmc.loader.impl.launch.knot.KnotClient',
      arguments: {
        jvm: mergedJvmArgs,
        game: mergedGameArgs,
      },
      libraries: Array.from(libraryMap.values()),
    };

    return mergedPackage;
  }

  /**
   * Prepares the instance directories and installs Fabric dependencies.
   */
  public async install(
    instance: InstanceRecord,
    loaderVersion: string,
    downloadManager?: DownloadManager
  ): Promise<{ versionId: string; profile: MojangVersionPackage }> {
    const profile = await this.resolveProfile(instance.minecraftVersion, loaderVersion);

    // Ensure instance directory structure
    const instanceDir = EnginePaths.getInstanceDir(instance.id);
    const subDirs = ['saves', 'resourcepacks', 'config', 'mods', 'disabled-mods', 'screenshots', 'natives', 'logs'];
    for (const sub of subDirs) {
      const targetDir = path.join(instanceDir, sub);
      if (!fs.existsSync(targetDir)) {
        await fs.promises.mkdir(targetDir, { recursive: true });
      }
    }

    // Perform downloads if downloadManager is supplied
    if (downloadManager) {
      await GameInstaller.installVersion(profile, instance.id, downloadManager);
    }

    // Update instance record
    await InstallationRepository.update(instance.id, {
      loader: 'fabric',
      loaderVersion,
      resolvedVersionId: profile.id,
      status: 'ready',
    });

    return {
      versionId: profile.id,
      profile,
    };
  }

  /**
   * Ensures Fabric API mod JAR is present in instance mods/ directory.
   */
  public async ensureFabricApiForInstance(instanceId: string, gameVersion: string = '1.20.4'): Promise<boolean> {
    try {
      const instance = await InstallationRepository.getById(instanceId);
      if (!instance) return false;
      const modsDir = path.join(instance.instanceDir, 'mods');
      if (!fs.existsSync(modsDir)) {
        fs.mkdirSync(modsDir, { recursive: true });
      }

      const files = fs.readdirSync(modsDir);
      const hasFabricApi = files.some(
        (f) => f.toLowerCase().includes('fabric-api') && f.toLowerCase().endsWith('.jar')
      );
      if (hasFabricApi) {
        return true;
      }

      const cacheDir = path.join(EnginePaths.getGameDataDir(), 'loader-assets', 'fabric-api');
      if (!fs.existsSync(cacheDir)) {
        fs.mkdirSync(cacheDir, { recursive: true });
      }
      const cacheFileName = `fabric-api-0.97.0+1.20.4.jar`;
      const cachedFilePath = path.join(cacheDir, cacheFileName);
      const targetFilePath = path.join(modsDir, cacheFileName);

      if (fs.existsSync(cachedFilePath)) {
        fs.copyFileSync(cachedFilePath, targetFilePath);
        return true;
      }

      // Download from Fabric Maven
      const downloadUrl = 'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.97.0+1.20.4/fabric-api-0.97.0+1.20.4.jar';
      const res = await fetch(downloadUrl);
      if (res.ok) {
        const buffer = Buffer.from(await res.arrayBuffer());
        fs.writeFileSync(cachedFilePath, buffer);
        fs.writeFileSync(targetFilePath, buffer);
        return true;
      }
      return false;
    } catch {
      return false;
    }
  }
}
