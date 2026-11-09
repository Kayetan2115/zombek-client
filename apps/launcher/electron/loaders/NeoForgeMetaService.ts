import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import type { NeoForgeVersionInfo } from '@zombek/shared-types';

export const NEOFORGE_MAVEN_MODERN_METADATA_URL =
  'https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml';
export const NEOFORGE_MAVEN_TRANSITION_METADATA_URL =
  'https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml';
export const NEOFORGE_MAVEN_BASE = 'https://maven.neoforged.net/releases';

interface NeoForgeMetaCache {
  modernMavenVersions?: string[];
  transitionMavenVersions?: string[];
  versionsByGame?: Record<string, NeoForgeVersionInfo[]>;
  lastUpdated?: string;
}

export interface NeoForgePrefixMatch {
  generation: 'modern' | 'transition';
  artifactCoordinates: 'net.neoforged:neoforge' | 'net.neoforged:forge';
  prefix: string;
}

export class NeoForgeMetaService {
  private static cachedModernVersions: string[] | null = null;
  private static cachedTransitionVersions: string[] | null = null;
  private static cachedVersionsByGame: Map<string, NeoForgeVersionInfo[]> = new Map();

  public static clearMemoryCache(): void {
    this.cachedModernVersions = null;
    this.cachedTransitionVersions = null;
    this.cachedVersionsByGame.clear();
  }

  public static getCacheFilePath(): string {
    return path.join(EnginePaths.getGameDataDir(), 'neoforge_meta_cache.json');
  }

  private static async readDiskCache(): Promise<NeoForgeMetaCache> {
    const cacheFile = this.getCacheFilePath();
    if (!fs.existsSync(cacheFile)) return {};

    try {
      const raw = await fs.promises.readFile(cacheFile, 'utf-8');
      return JSON.parse(raw) as NeoForgeMetaCache;
    } catch {
      return {};
    }
  }

  private static async writeDiskCache(cache: NeoForgeMetaCache): Promise<void> {
    try {
      EnginePaths.ensureAllBaseDirs();
      const cacheFile = this.getCacheFilePath();
      await fs.promises.writeFile(cacheFile, JSON.stringify(cache, null, 2), 'utf-8');
    } catch {
      // Ignore cache write errors
    }
  }

  /**
   * Resolves whether the Minecraft version is supported by NeoForge and its corresponding prefix/generation.
   * NeoForge exists only for Minecraft 1.20.1 (transition under net.neoforged:forge)
   * and 1.20.2+ (modern under net.neoforged:neoforge).
   */
  public static getNeoForgePrefix(gameVersion: string): NeoForgePrefixMatch | null {
    const parts = gameVersion.split('.').map(Number);
    if (parts.length >= 2 && parts[0] === 1) {
      const major = parts[1]; // 20 or 21
      const minor = parts[2] !== undefined ? parts[2] : 0;

      // 1.20.1 transition generation
      if (major === 20 && minor === 1) {
        return {
          generation: 'transition',
          artifactCoordinates: 'net.neoforged:forge',
          prefix: '1.20.1-47.1.',
        };
      }

      // Modern generation 1.20.2+ (e.g. 20.2.x, 20.4.x)
      if (major === 20 && minor >= 2) {
        return {
          generation: 'modern',
          artifactCoordinates: 'net.neoforged:neoforge',
          prefix: `20.${minor}.`,
        };
      }

      // Modern generation 1.21+ (e.g. 21.0.x, 21.1.x, 21.4.x)
      if (major >= 21) {
        return {
          generation: 'modern',
          artifactCoordinates: 'net.neoforged:neoforge',
          prefix: `${major}.${minor}.`,
        };
      }
    }

    return null;
  }

  /**
   * Fetches modern Maven metadata versions (net.neoforged:neoforge)
   */
  public static async getModernMavenVersions(forceRefresh: boolean = false): Promise<string[]> {
    if (!forceRefresh && this.cachedModernVersions && this.cachedModernVersions.length > 0) {
      return this.cachedModernVersions;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.modernMavenVersions && diskCache.modernMavenVersions.length > 0) {
      this.cachedModernVersions = diskCache.modernMavenVersions;
      return diskCache.modernMavenVersions;
    }

    try {
      const res = await fetch(NEOFORGE_MAVEN_MODERN_METADATA_URL);
      if (!res.ok) {
        throw new Error(`Błąd pobierania modern maven-metadata NeoForge: HTTP ${res.status}`);
      }
      const xml = await res.text();
      const versions = this.parseVersionsFromXml(xml);

      this.cachedModernVersions = versions;
      diskCache.modernMavenVersions = versions;
      diskCache.lastUpdated = new Date().toISOString();
      await this.writeDiskCache(diskCache);

      return versions;
    } catch (err: any) {
      if (diskCache.modernMavenVersions && diskCache.modernMavenVersions.length > 0) {
        this.cachedModernVersions = diskCache.modernMavenVersions;
        return diskCache.modernMavenVersions;
      }
      throw new Error(`Nie udało się pobrać metadanych Maven NeoForge: ${err.message}`);
    }
  }

  /**
   * Fetches transition Maven metadata versions (net.neoforged:forge)
   */
  public static async getTransitionMavenVersions(forceRefresh: boolean = false): Promise<string[]> {
    if (!forceRefresh && this.cachedTransitionVersions && this.cachedTransitionVersions.length > 0) {
      return this.cachedTransitionVersions;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.transitionMavenVersions && diskCache.transitionMavenVersions.length > 0) {
      this.cachedTransitionVersions = diskCache.transitionMavenVersions;
      return diskCache.transitionMavenVersions;
    }

    try {
      const res = await fetch(NEOFORGE_MAVEN_TRANSITION_METADATA_URL);
      if (!res.ok) {
        throw new Error(`Błąd pobierania transition maven-metadata NeoForge: HTTP ${res.status}`);
      }
      const xml = await res.text();
      const versions = this.parseVersionsFromXml(xml);

      this.cachedTransitionVersions = versions;
      diskCache.transitionMavenVersions = versions;
      diskCache.lastUpdated = new Date().toISOString();
      await this.writeDiskCache(diskCache);

      return versions;
    } catch (err: any) {
      if (diskCache.transitionMavenVersions && diskCache.transitionMavenVersions.length > 0) {
        this.cachedTransitionVersions = diskCache.transitionMavenVersions;
        return diskCache.transitionMavenVersions;
      }
      throw new Error(`Nie udało się pobrać metadanych Transition Maven NeoForge: ${err.message}`);
    }
  }

  private static parseVersionsFromXml(xml: string): string[] {
    const versions: string[] = [];
    const regex = /<version>([^<]+)<\/version>/g;
    let match: RegExpExecArray | null;
    while ((match = regex.exec(xml)) !== null) {
      if (match[1]) {
        versions.push(match[1]);
      }
    }
    return versions;
  }

  /**
   * Sorts NeoForge version strings descending (newest builds at the top).
   */
  public static sortVersionsDescending(versions: string[]): string[] {
    return [...versions].sort((a, b) => {
      return b.localeCompare(a, undefined, { numeric: true, sensitivity: 'base' });
    });
  }

  /**
   * Returns list of NeoForge versions compatible with a given Minecraft version.
   */
  public static async getNeoForgeVersionsForGame(
    gameVersion: string,
    forceRefresh: boolean = false
  ): Promise<NeoForgeVersionInfo[]> {
    if (!forceRefresh && this.cachedVersionsByGame.has(gameVersion)) {
      return this.cachedVersionsByGame.get(gameVersion)!;
    }

    const diskCache = await this.readDiskCache();
    if (!forceRefresh && diskCache.versionsByGame?.[gameVersion]) {
      const cached = diskCache.versionsByGame[gameVersion];
      this.cachedVersionsByGame.set(gameVersion, cached);
      return cached;
    }

    const matchInfo = this.getNeoForgePrefix(gameVersion);
    if (!matchInfo) {
      // Version is not supported by NeoForge (e.g. 1.19.4 or 1.16.5)
      this.cachedVersionsByGame.set(gameVersion, []);
      return [];
    }

    try {
      let rawVersions: string[] = [];
      if (matchInfo.generation === 'transition') {
        rawVersions = await this.getTransitionMavenVersions(forceRefresh);
      } else {
        rawVersions = await this.getModernMavenVersions(forceRefresh);
      }

      // Filter by prefix
      const matchingRaw = rawVersions.filter((v) => v.startsWith(matchInfo.prefix));
      const sorted = this.sortVersionsDescending(matchingRaw);

      if (sorted.length === 0) {
        this.cachedVersionsByGame.set(gameVersion, []);
        return [];
      }

      // Find recommended version: latest non-beta, non-alpha, non-snapshot build
      const stableCandidate = sorted.find(
        (v) => !v.includes('beta') && !v.includes('alpha') && !v.includes('pre') && !v.includes('snapshot')
      );
      const recommendedVersion = stableCandidate || sorted[0];
      const latestVersion = sorted[0];

      const result: NeoForgeVersionInfo[] = sorted.map((v) => {
        let installerUrl: string;
        if (matchInfo.generation === 'transition') {
          installerUrl = `${NEOFORGE_MAVEN_BASE}/net/neoforged/forge/${v}/forge-${v}-installer.jar`;
        } else {
          installerUrl = `${NEOFORGE_MAVEN_BASE}/net/neoforged/neoforge/${v}/neoforge-${v}-installer.jar`;
        }

        return {
          version: v,
          rawVersion: v,
          minecraftVersion: gameVersion,
          artifactCoordinates: matchInfo.artifactCoordinates,
          installerUrl,
          generation: matchInfo.generation,
          isRecommended: v === recommendedVersion,
          isLatest: v === latestVersion,
        };
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
      throw new Error(`Błąd wyszukiwania wersji NeoForge dla Minecraft ${gameVersion}: ${err.message}`);
    }
  }
}
