import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import type { QuiltGameVersion, QuiltLoaderVersion, QuiltLoaderItem } from '@zombek/shared-types';
import type { MojangVersionPackage } from '../engine/types.js';

export const QUILT_META_BASE_URL = 'https://meta.quiltmc.org/v3';
export const QUILT_USER_AGENT = 'ZombekClient/0.1.0 (desktop launcher)';

interface QuiltMetaCache {
  gameVersions?: QuiltGameVersion[];
  loaderVersions?: Record<string, QuiltLoaderVersion[]>;
  lastUpdated?: string;
}

export class QuiltMetaService {
  private static cachedGameVersions: QuiltGameVersion[] | null = null;
  private static cachedLoaderVersions: Map<string, QuiltLoaderVersion[]> = new Map();

  public static clearMemoryCache(): void {
    this.cachedGameVersions = null;
    this.cachedLoaderVersions.clear();
  }

  public static getCacheFilePath(): string {
    return path.join(EnginePaths.getGameDataDir(), 'quilt_meta_cache.json');
  }

  private static async readDiskCache(): Promise<QuiltMetaCache> {
    const cacheFile = this.getCacheFilePath();
    if (!fs.existsSync(cacheFile)) return {};

    try {
      const raw = await fs.promises.readFile(cacheFile, 'utf-8');
      return JSON.parse(raw) as QuiltMetaCache;
    } catch {
      return {};
    }
  }

  private static async writeDiskCache(cache: QuiltMetaCache): Promise<void> {
    try {
      EnginePaths.ensureAllBaseDirs();
      const cacheFile = this.getCacheFilePath();
      await fs.promises.writeFile(cacheFile, JSON.stringify(cache, null, 2), 'utf-8');
    } catch {
      // Ignore cache write errors
    }
  }

  /**
   * Fetches compatible Minecraft versions supported by Quilt.
   */
  public static async getGameVersions(forceRefresh: boolean = false): Promise<QuiltGameVersion[]> {
    if (!forceRefresh && this.cachedGameVersions && this.cachedGameVersions.length > 0) {
      return this.cachedGameVersions;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.gameVersions && diskCache.gameVersions.length > 0) {
      this.cachedGameVersions = diskCache.gameVersions;
      return diskCache.gameVersions;
    }

    try {
      const res = await fetch(`${QUILT_META_BASE_URL}/versions/game`, {
        headers: {
          'User-Agent': QUILT_USER_AGENT,
        },
      });
      if (!res.ok) {
        throw new Error(`Quilt Meta API zwróciło błąd HTTP ${res.status}`);
      }

      const list = (await res.json()) as QuiltGameVersion[];
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
      throw new Error(`Nie udało się pobrać wersji gier Quilt: ${err.message}`);
    }
  }

  /**
   * Fetches available Quilt loader versions for a specific Minecraft version.
   */
  public static async getLoaderVersions(
    gameVersion: string,
    forceRefresh: boolean = false
  ): Promise<QuiltLoaderVersion[]> {
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
      const res = await fetch(
        `${QUILT_META_BASE_URL}/versions/loader/${encodeURIComponent(gameVersion)}`,
        {
          headers: {
            'User-Agent': QUILT_USER_AGENT,
          },
        }
      );
      if (!res.ok) {
        throw new Error(`Quilt Meta API zwróciło błąd HTTP ${res.status} dla wersji ${gameVersion}`);
      }

      const items = (await res.json()) as QuiltLoaderItem[];
      const loaderVersions: QuiltLoaderVersion[] = items.map((item) => item.loader);

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
      throw new Error(`Nie udało się pobrać wersji Quilt Loader dla ${gameVersion}: ${err.message}`);
    }
  }

  /**
   * Fetches official Quilt profile JSON for given game and loader version.
   */
  public static async getProfileJson(
    gameVersion: string,
    loaderVersion: string
  ): Promise<MojangVersionPackage> {
    const profileId = `quilt-loader-${loaderVersion}-${gameVersion}`;
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

    const url = `${QUILT_META_BASE_URL}/versions/loader/${encodeURIComponent(gameVersion)}/${encodeURIComponent(loaderVersion)}/profile/json`;
    const res = await fetch(url, {
      headers: {
        'User-Agent': QUILT_USER_AGENT,
      },
    });
    if (!res.ok) {
      throw new Error(`Nie udało się pobrać profilu Quilt z ${url}: HTTP ${res.status}`);
    }

    const profileData = (await res.json()) as MojangVersionPackage;

    if (!fs.existsSync(versionDir)) {
      await fs.promises.mkdir(versionDir, { recursive: true });
    }
    await fs.promises.writeFile(profileJsonPath, JSON.stringify(profileData, null, 2), 'utf-8');

    return profileData;
  }
}
