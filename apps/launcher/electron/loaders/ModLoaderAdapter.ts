import type { MinecraftLoaderType, InstanceRecord, FabricLoaderVersion } from '@zombek/shared-types';
import type { MojangVersionPackage } from '../engine/types.js';
import type { DownloadManager } from '../engine/DownloadManager.js';

export interface ModLoaderAdapter {
  readonly loaderType: MinecraftLoaderType;

  /**
   * Checks if the given Minecraft version is supported by this loader.
   */
  isVersionSupported(gameVersion: string): Promise<boolean>;

  /**
   * Retrieves available loader versions for a given game version.
   */
  getLoaderVersions(gameVersion: string): Promise<FabricLoaderVersion[]>;

  /**
   * Resolves and merges the version package (including libraries and mainClass)
   * ready for downloading and launching.
   */
  resolveProfile(gameVersion: string, loaderVersion?: string): Promise<MojangVersionPackage>;

  /**
   * Installs loader metadata and libraries for an instance.
   */
  install(
    instance: InstanceRecord,
    loaderVersion: string,
    downloadManager?: DownloadManager
  ): Promise<{ versionId: string; profile: MojangVersionPackage }>;
}
