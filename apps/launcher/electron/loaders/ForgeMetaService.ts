import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import type { ForgeVersionInfo } from '@zombek/shared-types';

export const FORGE_PROMOTIONS_URL = 'https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json';
export const FORGE_MAVEN_METADATA_URL = 'https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml';
export const FORGE_MAVEN_BASE = 'https://maven.minecraftforge.net/net/minecraftforge/forge';

interface ForgeMetaCache {
  promos?: Record<string, string>;
  mavenVersions?: string[];
  versionsByGame?: Record<string, ForgeVersionInfo[]>;
  lastUpdated?: string;
}

export class ForgeMetaService {
  private static cachedPromos: Record<string, string> | null = null;
  private static cachedMavenVersions: string[] | null = null;
  private static cachedVersionsByGame: Map<string, ForgeVersionInfo[]> = new Map();

  public static clearMemoryCache(): void {
    this.cachedPromos = null;
    this.cachedMavenVersions = null;
    this.cachedVersionsByGame.clear();
  }

  public static getCacheFilePath(): string {
    return path.join(EnginePaths.getGameDataDir(), 'forge_meta_cache.json');
  }

  private static async readDiskCache(): Promise<ForgeMetaCache> {
    const cacheFile = this.getCacheFilePath();
    if (!fs.existsSync(cacheFile)) return {};

    try {
      const raw = await fs.promises.readFile(cacheFile, 'utf-8');
      return JSON.parse(raw) as ForgeMetaCache;
    } catch {
      return {};
    }
  }

  private static async writeDiskCache(cache: ForgeMetaCache): Promise<void> {
    try {
      EnginePaths.ensureAllBaseDirs();
      const cacheFile = this.getCacheFilePath();
      await fs.promises.writeFile(cacheFile, JSON.stringify(cache, null, 2), 'utf-8');
    } catch {
      // Ignore cache write errors
    }
  }

  /**
   * Fetches the promotions slim JSON mapping e.g. "1.20.4-latest" -> "49.2.13"
   */
  public static async getPromotions(forceRefresh: boolean = false): Promise<Record<string, string>> {
    if (!forceRefresh && this.cachedPromos) {
      return this.cachedPromos;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.promos && Object.keys(diskCache.promos).length > 0) {
      this.cachedPromos = diskCache.promos;
      return diskCache.promos;
    }

    try {
      const res = await fetch(FORGE_PROMOTIONS_URL);
      if (!res.ok) {
        throw new Error(`Błąd pobierania promocji Forge: HTTP ${res.status}`);
      }
      const data = (await res.json()) as { promos?: Record<string, string> };
      const promos = data.promos || {};
      this.cachedPromos = promos;

      diskCache.promos = promos;
      diskCache.lastUpdated = new Date().toISOString();
      await this.writeDiskCache(diskCache);

      return promos;
    } catch (err: any) {
      if (diskCache.promos && Object.keys(diskCache.promos).length > 0) {
        this.cachedPromos = diskCache.promos;
        return diskCache.promos;
      }
      throw new Error(`Nie udało się pobrać metadanych promocji Forge: ${err.message}`);
    }
  }

  /**
   * Fetches all available Forge version strings from official maven-metadata.xml
   */
  public static async getMavenVersions(forceRefresh: boolean = false): Promise<string[]> {
    if (!forceRefresh && this.cachedMavenVersions && this.cachedMavenVersions.length > 0) {
      return this.cachedMavenVersions;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.mavenVersions && diskCache.mavenVersions.length > 0) {
      this.cachedMavenVersions = diskCache.mavenVersions;
      return diskCache.mavenVersions;
    }

    try {
      const res = await fetch(FORGE_MAVEN_METADATA_URL);
      if (!res.ok) {
        throw new Error(`Błąd pobierania maven-metadata Forge: HTTP ${res.status}`);
      }
      const xml = await res.text();
      const versions: string[] = [];
      const regex = /<version>([^<]+)<\/version>/g;
      let match: RegExpExecArray | null;

      while ((match = regex.exec(xml)) !== null) {
        if (match[1]) {
          versions.push(match[1]);
        }
      }

      this.cachedMavenVersions = versions;
      diskCache.mavenVersions = versions;
      diskCache.lastUpdated = new Date().toISOString();
      await this.writeDiskCache(diskCache);

      return versions;
    } catch (err: any) {
      if (diskCache.mavenVersions && diskCache.mavenVersions.length > 0) {
        this.cachedMavenVersions = diskCache.mavenVersions;
        return diskCache.mavenVersions;
      }
      throw new Error(`Nie udało się pobrać metadanych Maven Forge: ${err.message}`);
    }
  }

  /**
   * Returns list of Forge versions compatible with a given Minecraft version.
   */
  public static async getForgeVersionsForGame(
    gameVersion: string,
    forceRefresh: boolean = false
  ): Promise<ForgeVersionInfo[]> {
    if (!forceRefresh && this.cachedVersionsByGame.has(gameVersion)) {
      return this.cachedVersionsByGame.get(gameVersion)!;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.versionsByGame?.[gameVersion]) {
      const cached = diskCache.versionsByGame[gameVersion];
      this.cachedVersionsByGame.set(gameVersion, cached);
      return cached;
    }

    try {
      let promos: Record<string, string> = {};
      try {
        promos = await this.getPromotions(forceRefresh);
      } catch {
        // Soft-fail if promotions endpoint is temporarily unreachable
      }

      const recPromo = promos[`${gameVersion}-recommended`];
      const latestPromo = promos[`${gameVersion}-latest`];

      let allMavenVersions: string[] = [];
      try {
        allMavenVersions = await this.getMavenVersions(forceRefresh);
      } catch {
        // Soft-fail if maven metadata is temporarily unreachable
      }

      // Filter versions starting with `${gameVersion}-`
      const prefix = `${gameVersion}-`;
      const matchingRaw = allMavenVersions.filter((v) => v.startsWith(prefix));

      // Build version list
      const rawBuilds = new Set<string>();
      for (const full of matchingRaw) {
        const build = full.slice(prefix.length);
        rawBuilds.add(build);
      }

      if (recPromo) rawBuilds.add(recPromo);
      if (latestPromo) rawBuilds.add(latestPromo);

      const result: ForgeVersionInfo[] = [];

      for (const build of rawBuilds) {
        const fullVersion = `${gameVersion}-${build}`;
        const isRecommended = build === recPromo;
        const isLatest = build === latestPromo;
        const installerUrl = `${FORGE_MAVEN_BASE}/${fullVersion}/forge-${fullVersion}-installer.jar`;

        result.push({
          version: fullVersion,
          rawVersion: build,
          minecraftVersion: gameVersion,
          isRecommended,
          isLatest,
          installerUrl,
        });
      }

      // Sort: Recommended first, then Latest, then descending build numbers
      result.sort((a, b) => {
        if (a.isRecommended && !b.isRecommended) return -1;
        if (!a.isRecommended && b.isRecommended) return 1;
        if (a.isLatest && !b.isLatest) return -1;
        if (!a.isLatest && b.isLatest) return 1;
        return b.rawVersion.localeCompare(a.rawVersion, undefined, { numeric: true, sensitivity: 'base' });
      });

      this.cachedVersionsByGame.set(gameVersion, result);

      if (!diskCache.versionsByGame) {
        diskCache.versionsByGame = {};
      }
      diskCache.versionsByGame[gameVersion] = result;
      await this.writeDiskCache(diskCache);

      return result;
    } catch (err: any) {
      if (diskCache.versionsByGame?.[gameVersion]) {
        const cached = diskCache.versionsByGame[gameVersion];
        this.cachedVersionsByGame.set(gameVersion, cached);
        return cached;
      }
      throw new Error(`Błąd wyszukiwania wersji Forge dla Minecraft ${gameVersion}: ${err.message}`);
    }
  }
}
