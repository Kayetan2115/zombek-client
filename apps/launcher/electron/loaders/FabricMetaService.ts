import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import type { FabricGameVersion, FabricLoaderVersion, FabricLoaderItem } from '@zombek/shared-types';
import type { MojangVersionPackage } from '../engine/types.js';

export const FABRIC_META_BASE_URL = 'https://meta.fabricmc.net/v2';

interface FabricMetaCache {
  gameVersions?: FabricGameVersion[];
  loaderVersions?: Record<string, FabricLoaderVersion[]>;
  lastUpdated?: string;
}

export class FabricMetaService {
  private static cachedGameVersions: FabricGameVersion[] | null = null;
  private static cachedLoaderVersions: Map<string, FabricLoaderVersion[]> = new Map();

  public static clearMemoryCache(): void {
    this.cachedGameVersions = null;
    this.cachedLoaderVersions.clear();
  }

  public static getCacheFilePath(): string {
    return path.join(EnginePaths.getGameDataDir(), 'fabric_meta_cache.json');
  }

  private static async readDiskCache(): Promise<FabricMetaCache> {
    const cacheFile = this.getCacheFilePath();
    if (!fs.existsSync(cacheFile)) return {};

    try {
      const raw = await fs.promises.readFile(cacheFile, 'utf-8');
      return JSON.parse(raw) as FabricMetaCache;
    } catch {
      return {};
    }
  }

  private static async writeDiskCache(cache: FabricMetaCache): Promise<void> {
    try {
      EnginePaths.ensureAllBaseDirs();
      const cacheFile = this.getCacheFilePath();
      await fs.promises.writeFile(cacheFile, JSON.stringify(cache, null, 2), 'utf-8');
    } catch {
      // Ignore cache write errors
    }
  }

  /**
   * Fetches compatible Minecraft versions supported by Fabric.
   */
  public static async getGameVersions(forceRefresh: boolean = false): Promise<FabricGameVersion[]> {
    if (!forceRefresh && this.cachedGameVersions && this.cachedGameVersions.length > 0) {
      return this.cachedGameVersions;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.gameVersions && diskCache.gameVersions.length > 0) {
      this.cachedGameVersions = diskCache.gameVersions;
      return diskCache.gameVersions;
    }

    try {
      const res = await fetch(`${FABRIC_META_BASE_URL}/versions/game`);
      if (!res.ok) {
        throw new Error(`Fabric Meta API zwróciło błąd HTTP ${res.status}`);
      }

      const list = (await res.json()) as FabricGameVersion[];
      this.cachedGameVersions = list;

      diskCache.gameVersions = list;
      diskCache.lastUpdated = new Date().toISOString();
      await this.writeDiskCache(diskCache);

      return list;
    } catch (err: any) {
      if (diskCache.gameVersions && diskCache.gameVersions.length > 0) {
        this.cachedGameVersions = diskCache.gameVersions;
        return diskCache.gameVersions;
      }
      throw new Error(`Nie udało się pobrać wersji gier Fabric: ${err.message}`);
    }
  }

  /**
   * Fetches available Fabric loader versions for a specific Minecraft version.
   */
  public static async getLoaderVersions(
    gameVersion: string,
    forceRefresh: boolean = false
  ): Promise<FabricLoaderVersion[]> {
    if (!forceRefresh && this.cachedLoaderVersions.has(gameVersion)) {
      return this.cachedLoaderVersions.get(gameVersion)!;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.loaderVersions?.[gameVersion]) {
      const cached = diskCache.loaderVersions[gameVersion];
      this.cachedLoaderVersions.set(gameVersion, cached);
      return cached;
    }

    try {
      const res = await fetch(`${FABRIC_META_BASE_URL}/versions/loader/${encodeURIComponent(gameVersion)}`);
      if (!res.ok) {
        throw new Error(`Fabric Meta API zwróciło błąd HTTP ${res.status} dla wersji ${gameVersion}`);
      }

      const items = (await res.json()) as FabricLoaderItem[];
      const loaderVersions: FabricLoaderVersion[] = items.map((item) => item.loader);

      this.cachedLoaderVersions.set(gameVersion, loaderVersions);

      if (!diskCache.loaderVersions) {
        diskCache.loaderVersions = {};
      }
      diskCache.loaderVersions[gameVersion] = loaderVersions;
      await this.writeDiskCache(diskCache);

      return loaderVersions;
    } catch (err: any) {
      if (diskCache.loaderVersions?.[gameVersion]) {
        const cached = diskCache.loaderVersions[gameVersion];
        this.cachedLoaderVersions.set(gameVersion, cached);
        return cached;
      }
      throw new Error(`Nie udało się pobrać wersji Fabric Loader dla ${gameVersion}: ${err.message}`);
    }
  }

  /**
   * Fetches official Fabric profile JSON for given game and loader version.
   */
  public static async getProfileJson(
    gameVersion: string,
    loaderVersion: string
  ): Promise<MojangVersionPackage> {
    const profileId = `fabric-loader-${loaderVersion}-${gameVersion}`;
    const versionDir = path.join(EnginePaths.getVersionsDir(), profileId);
    const profileJsonPath = path.join(versionDir, `${profileId}.json`);

    // Check disk first
    if (fs.existsSync(profileJsonPath)) {
      try {
        const raw = await fs.promises.readFile(profileJsonPath, 'utf-8');
        return JSON.parse(raw) as MojangVersionPackage;
      } catch {
        // re-fetch if corrupted
      }
    }

    const url = `${FABRIC_META_BASE_URL}/versions/loader/${encodeURIComponent(gameVersion)}/${encodeURIComponent(loaderVersion)}/profile/json`;
    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`Nie udało się pobrać profilu Fabric z ${url}: HTTP ${res.status}`);
    }

    const profileData = (await res.json()) as MojangVersionPackage;

    if (!fs.existsSync(versionDir)) {
      await fs.promises.mkdir(versionDir, { recursive: true });
    }
    await fs.promises.writeFile(profileJsonPath, JSON.stringify(profileData, null, 2), 'utf-8');

    return profileData;
  }
}
