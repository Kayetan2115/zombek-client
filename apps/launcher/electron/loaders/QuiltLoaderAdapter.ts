import * as fs from 'node:fs';
import * as path from 'node:path';
import type { ModLoaderAdapter } from './ModLoaderAdapter.js';
import { QuiltMetaService } from './QuiltMetaService.js';
import { VersionManifestService } from '../engine/VersionManifestService.js';
import { GameInstaller } from '../engine/GameInstaller.js';
import { DownloadManager } from '../engine/DownloadManager.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { EnginePaths } from '../engine/paths.js';
import type { MinecraftLoaderType, InstanceRecord, FabricLoaderVersion, QuiltLoaderVersion } from '@zombek/shared-types';
import type { MojangVersionPackage } from '../engine/types.js';

export class QuiltLoaderAdapter implements ModLoaderAdapter {
  private static instance: QuiltLoaderAdapter;
  public readonly loaderType: MinecraftLoaderType = 'quilt';

  public static getInstance(): QuiltLoaderAdapter {
    if (!QuiltLoaderAdapter.instance) {
      QuiltLoaderAdapter.instance = new QuiltLoaderAdapter();
    }
    return QuiltLoaderAdapter.instance;
  }

  public async isVersionSupported(gameVersion: string): Promise<boolean> {
    try {
      const versions = await QuiltMetaService.getGameVersions();
      return versions.some((v) => v.version === gameVersion);
    } catch {
      // In case offline, assume standard release versions are supported
      return true;
    }
  }

  public async getLoaderVersions(gameVersion: string): Promise<FabricLoaderVersion[]> {
    const quiltVersions = await QuiltMetaService.getLoaderVersions(gameVersion);
    return quiltVersions.map((v) => ({
      separator: v.separator,
      build: v.build,
      maven: v.maven,
      version: v.version,
      stable: Boolean(v.stable),
    }));
  }

  public async getQuiltLoaderVersions(gameVersion: string): Promise<QuiltLoaderVersion[]> {
    return QuiltMetaService.getLoaderVersions(gameVersion);
  }

  /**
   * Resolves the complete version package for Quilt, merging Vanilla base with Quilt loader profile.
   * NOTE: In accordance with QuiltMC's December 2025 deprecation of QSL (Quilt Standard Libraries),
   * standard Quilt installation does NOT inject QSL as a mandatory dependency. Only the official
   * loader libraries provided in the profile are used.
   */
  public async resolveProfile(
    gameVersion: string,
    loaderVersion?: string
  ): Promise<MojangVersionPackage> {
    let selectedLoaderVersion = loaderVersion;

    if (!selectedLoaderVersion) {
      const loaders = await this.getQuiltLoaderVersions(gameVersion);
      if (!loaders || loaders.length === 0) {
        throw new Error(`Brak dostępnych wersji Quilt Loader dla Minecraft ${gameVersion}`);
      }
      const stable = loaders.find((l) => l.stable);
      selectedLoaderVersion = stable ? stable.version : loaders[0].version;
    }

    // 1. Fetch Quilt profile from official Quilt Meta v3
    const quiltProfile = await QuiltMetaService.getProfileJson(gameVersion, selectedLoaderVersion);

    // 2. Fetch base Vanilla version package
    const vanillaPackage = await VersionManifestService.getVersionPackage(gameVersion);

    // 3. Merge libraries without duplicates
    const libraryMap = new Map<string, any>();
    for (const lib of vanillaPackage.libraries || []) {
      libraryMap.set(lib.name, lib);
    }
    for (const lib of quiltProfile.libraries || []) {
      libraryMap.set(lib.name, lib);
    }

    // 4. Merge JVM and Game arguments
    const mergedJvmArgs = [
      ...(vanillaPackage.arguments?.jvm || []),
      ...(quiltProfile.arguments?.jvm || []),
    ];
    const mergedGameArgs = [
      ...(vanillaPackage.arguments?.game || []),
      ...(quiltProfile.arguments?.game || []),
    ];

    const mergedPackage: MojangVersionPackage = {
      ...vanillaPackage,
      id: quiltProfile.id,
      inheritsFrom: gameVersion,
      mainClass: quiltProfile.mainClass || 'org.quiltmc.loader.impl.launch.knot.KnotClient',
      arguments: {
        jvm: mergedJvmArgs,
        game: mergedGameArgs,
      },
      libraries: Array.from(libraryMap.values()),
    };

    return mergedPackage;
  }

  /**
   * Prepares the instance directories and installs Quilt dependencies.
   */
  public async install(
    instance: InstanceRecord,
    loaderVersion: string,
    downloadManager?: DownloadManager
  ): Promise<{ versionId: string; profile: MojangVersionPackage }> {
    const profile = await this.resolveProfile(instance.minecraftVersion, loaderVersion);

    // Ensure instance directory structure with isolated subdirectories
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
      loader: 'quilt',
      loaderVersion,
      resolvedVersionId: profile.id,
      status: 'ready',
    });

    return {
      versionId: profile.id,
      profile,
    };
  }
}
