import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import type {
  ModrinthProject,
  ModrinthSearchResult,
  ModrinthVersion,
  ModrinthCategoryTag,
  ModrinthLoaderTag,
  ModrinthGameVersionTag,
  ModrinthSearchParams,
} from '@zombek/shared-types';

export class ModrinthRateLimitError extends Error {
  public readonly resetSeconds: number;

  constructor(resetSeconds: number, message?: string) {
    super(
      message ||
        `Przekroczono limit zapytań Modrinth API (Rate limit exceeded). Spróbuj ponownie za ${resetSeconds}s.`
    );
    this.name = 'ModrinthRateLimitError';
    this.resetSeconds = resetSeconds;
  }
}

interface CacheItem<T> {
  data: T;
  expiresAt: number;
  etag?: string;
}

export class ModrinthApiClient {
  public static readonly BASE_URL = 'https://api.modrinth.com/v2';
  private static userAgent = 'ZombekClient/0.1.0 (https://github.com/zombekclient/zombekclient)';

  // Rate Limiting state
  private static rateLimitLimit: number = 300;
  private static rateLimitRemaining: number = 300;
  private static rateLimitResetTimeMs: number = 0;

  // In-memory cache
  private static memoryCache = new Map<string, CacheItem<any>>();

  /**
   * Configures the User-Agent header for Modrinth API compliance.
   */
  public static setUserAgent(contactOrUrl: string, appVersion: string = '0.1.0'): void {
    this.userAgent = `ZombekClient/${appVersion} (${contactOrUrl})`;
  }

  public static getUserAgent(): string {
    return this.userAgent;
  }

  public static getRateLimitStatus(): {
    remaining: number;
    limit: number;
    resetTimeMs: number;
    isLimited: boolean;
  } {
    const isLimited = this.rateLimitRemaining <= 0 && Date.now() < this.rateLimitResetTimeMs;
    return {
      remaining: this.rateLimitRemaining,
      limit: this.rateLimitLimit,
      resetTimeMs: this.rateLimitResetTimeMs,
      isLimited,
    };
  }

  /**
   * Clears in-memory cache (primarily used in tests).
   */
  public static clearCache(): void {
    this.memoryCache.clear();
    this.rateLimitResetTimeMs = 0;
    this.rateLimitRemaining = 300;
  }

  /**
   * Builds the Modrinth `facets` JSON query parameter from search params.
   * Modrinth Facet Rule:
   * - Inner array elements are combined with OR
   * - Outer array elements are combined with AND
   */
  public static buildFacets(params: ModrinthSearchParams = {}): string[][] {
    const facets: string[][] = [];

    // 1. Always restrict project type to 'mod'
    facets.push(['project_type:mod']);

    // 2. Loader filter (e.g. categories:fabric)
    if (params.loader && params.loader !== 'all' && params.loader !== 'vanilla') {
      facets.push([`categories:${params.loader.toLowerCase()}`]);
    }

    // 3. Minecraft Version filter (e.g. versions:1.20.4)
    if (params.gameVersion && params.gameVersion !== 'all') {
      facets.push([`versions:${params.gameVersion}`]);
    }

    // 4. Category filter (e.g. categories:optimization)
    if (params.category && params.category !== 'all') {
      facets.push([`categories:${params.category.toLowerCase()}`]);
    }

    // 5. Client environment filter (avoid showing purely server-side mods by default)
    if (params.environment === 'client') {
      facets.push(['client_side:required', 'client_side:optional']);
    } else if (params.environment === 'server') {
      facets.push(['server_side:required', 'server_side:optional']);
    } else {
      // Default: exclude mods that are completely unsupported on client
      facets.push(['client_side:required', 'client_side:optional', 'client_side:unknown']);
    }

    return facets;
  }

  /**
   * Searches projects in the Modrinth repository using `GET /search`.
   */
  public static async searchProjects(params: ModrinthSearchParams = {}): Promise<ModrinthSearchResult> {
    const url = new URL(`${this.BASE_URL}/search`);

    if (params.query?.trim()) {
      url.searchParams.set('query', params.query.trim());
    }

    const facets = this.buildFacets(params);
    url.searchParams.set('facets', JSON.stringify(facets));

    if (params.index) {
      url.searchParams.set('index', params.index);
    } else {
      url.searchParams.set('index', 'downloads'); // Default: most downloaded
    }

    const limit = Math.min(Math.max(params.limit || 20, 1), 100);
    const offset = Math.max(params.offset || 0, 0);
    url.searchParams.set('limit', String(limit));
    url.searchParams.set('offset', String(offset));

    const cacheKey = `search:${url.search}`;
    const cached = this.getFromCache<ModrinthSearchResult>(cacheKey);
    if (cached) return cached;

    const data = await this.executeRequest<ModrinthSearchResult>(url.toString());
    this.putInCache(cacheKey, data, 2 * 60 * 1000); // 2 minutes TTL
    return data;
  }

  /**
   * Retrieves full details for a single project using `GET /project/{id|slug}`.
   */
  public static async getProject(projectIdOrSlug: string): Promise<ModrinthProject> {
    const sanitized = encodeURIComponent(projectIdOrSlug.trim());
    if (!sanitized) throw new Error('ID lub slug projektu nie może być pusty.');

    const cacheKey = `project:${sanitized.toLowerCase()}`;
    const cached = this.getFromCache<ModrinthProject>(cacheKey);
    if (cached) return cached;

    const url = `${this.BASE_URL}/project/${sanitized}`;
    const data = await this.executeRequest<ModrinthProject>(url);
    this.putInCache(cacheKey, data, 10 * 60 * 1000); // 10 minutes TTL
    return data;
  }

  /**
   * Retrieves versions for a project using `GET /project/{id|slug}/version`.
   */
  public static async getProjectVersions(
    projectIdOrSlug: string,
    options: {
      loaders?: string[];
      gameVersions?: string[];
      includeChangelog?: boolean;
    } = {}
  ): Promise<ModrinthVersion[]> {
    const sanitized = encodeURIComponent(projectIdOrSlug.trim());
    if (!sanitized) throw new Error('ID lub slug projektu nie może być pusty.');

    const url = new URL(`${this.BASE_URL}/project/${sanitized}/version`);

    if (options.loaders && options.loaders.length > 0) {
      url.searchParams.set('loaders', JSON.stringify(options.loaders));
    }
    if (options.gameVersions && options.gameVersions.length > 0) {
      url.searchParams.set('game_versions', JSON.stringify(options.gameVersions));
    }
    if (options.includeChangelog !== undefined) {
      url.searchParams.set('include_changelog', String(options.includeChangelog));
    } else {
      url.searchParams.set('include_changelog', 'false');
    }

    const cacheKey = `versions:${sanitized.toLowerCase()}:${url.search}`;
    const cached = this.getFromCache<ModrinthVersion[]>(cacheKey);
    if (cached) return cached;

    const data = await this.executeRequest<ModrinthVersion[]>(url.toString());
    this.putInCache(cacheKey, data, 5 * 60 * 1000); // 5 minutes TTL
    return data;
  }

  /**
   * Retrieves details for a specific version using `GET /version/{id}`.
   */
  public static async getVersion(versionId: string): Promise<ModrinthVersion> {
    const sanitized = encodeURIComponent(versionId.trim());
    if (!sanitized) throw new Error('ID wersji nie może być puste.');

    const cacheKey = `version:${sanitized}`;
    const cached = this.getFromCache<ModrinthVersion>(cacheKey);
    if (cached) return cached;

    const url = `${this.BASE_URL}/version/${sanitized}`;
    const data = await this.executeRequest<ModrinthVersion>(url);
    this.putInCache(cacheKey, data, 15 * 60 * 1000); // 15 minutes TTL
    return data;
  }

  /**
   * Retrieves multiple versions at once using `GET /versions?ids=[...]`.
   */
  public static async getVersions(versionIds: string[]): Promise<ModrinthVersion[]> {
    if (!versionIds || versionIds.length === 0) return [];

    const url = new URL(`${this.BASE_URL}/versions`);
    url.searchParams.set('ids', JSON.stringify(versionIds));

    return await this.executeRequest<ModrinthVersion[]>(url.toString());
  }

  /**
   * Batch checks for updates for multiple files using `POST /version_files/update`.
   * Returns a map of original file hash -> latest compatible ModrinthVersion.
   */
  public static async getLatestUpdatesForFiles(params: {
    hashes: string[];
    algorithm?: 'sha512' | 'sha1';
    loaders: string[];
    game_versions: string[];
    version_types?: ('release' | 'beta' | 'alpha')[];
  }): Promise<Record<string, ModrinthVersion>> {
    if (!params.hashes || params.hashes.length === 0) return {};

    const url = `${this.BASE_URL}/version_files/update`;
    const payload = {
      hashes: params.hashes,
      algorithm: params.algorithm || 'sha512',
      loaders: params.loaders,
      game_versions: params.game_versions,
      version_types: params.version_types || ['release'],
    };

    return await this.executeRequest<Record<string, ModrinthVersion>>(url, {
      method: 'POST',
      body: payload,
    });
  }

  /**
   * Looks up files by hash using `POST /version_files`.
   * Maps hash -> ModrinthVersion.
   */
  public static async getVersionFiles(
    hashes: string[],
    algorithm: 'sha512' | 'sha1' = 'sha512'
  ): Promise<Record<string, ModrinthVersion>> {
    if (!hashes || hashes.length === 0) return {};

    const url = `${this.BASE_URL}/version_files`;
    return await this.executeRequest<Record<string, ModrinthVersion>>(url, {
      method: 'POST',
      body: {
        hashes,
        algorithm,
      },
    });
  }

  /**
   * Retrieves available category tags using `GET /tag/category`.
   */
  public static async getCategories(): Promise<ModrinthCategoryTag[]> {
    const cacheKey = 'tags:categories';
    const cached = this.getFromCache<ModrinthCategoryTag[]>(cacheKey);
    if (cached) return cached;

    const diskCached = await this.readDiskCache<ModrinthCategoryTag[]>('categories.json');
    if (diskCached) {
      this.putInCache(cacheKey, diskCached, 24 * 60 * 60 * 1000);
      return diskCached;
    }

    const url = `${this.BASE_URL}/tag/category`;
    const data = await this.executeRequest<ModrinthCategoryTag[]>(url);
    this.putInCache(cacheKey, data, 24 * 60 * 60 * 1000); // 24 hours TTL
    await this.writeDiskCache('categories.json', data);
    return data;
  }

  /**
   * Retrieves available mod loaders using `GET /tag/loader`.
   */
  public static async getLoaders(): Promise<ModrinthLoaderTag[]> {
    const cacheKey = 'tags:loaders';
    const cached = this.getFromCache<ModrinthLoaderTag[]>(cacheKey);
    if (cached) return cached;

    const diskCached = await this.readDiskCache<ModrinthLoaderTag[]>('loaders.json');
    if (diskCached) {
      this.putInCache(cacheKey, diskCached, 24 * 60 * 60 * 1000);
      return diskCached;
    }

    const url = `${this.BASE_URL}/tag/loader`;
    const data = await this.executeRequest<ModrinthLoaderTag[]>(url);
    this.putInCache(cacheKey, data, 24 * 60 * 60 * 1000);
    await this.writeDiskCache('loaders.json', data);
    return data;
  }

  /**
   * Retrieves supported Minecraft game versions using `GET /tag/game_version`.
   */
  public static async getGameVersions(): Promise<ModrinthGameVersionTag[]> {
    const cacheKey = 'tags:game_versions';
    const cached = this.getFromCache<ModrinthGameVersionTag[]>(cacheKey);
    if (cached) return cached;

    const diskCached = await this.readDiskCache<ModrinthGameVersionTag[]>('game_versions.json');
    if (diskCached) {
      this.putInCache(cacheKey, diskCached, 24 * 60 * 60 * 1000);
      return diskCached;
    }

    const url = `${this.BASE_URL}/tag/game_version`;
    const data = await this.executeRequest<ModrinthGameVersionTag[]>(url);
    this.putInCache(cacheKey, data, 24 * 60 * 60 * 1000);
    await this.writeDiskCache('game_versions.json', data);
    return data;
  }

  /**
   * Internal HTTP execution with headers, rate-limiting, and error handling.
   */
  private static async executeRequest<T>(
    url: string,
    options?: {
      method?: string;
      body?: any;
      headers?: Record<string, string>;
    }
  ): Promise<T> {
    // Check if we are currently rate-limited
    const now = Date.now();
    if (this.rateLimitResetTimeMs > now && this.rateLimitRemaining <= 0) {
      const waitSeconds = Math.ceil((this.rateLimitResetTimeMs - now) / 1000);
      throw new ModrinthRateLimitError(
        waitSeconds,
        `Zablokowano zapytanie: Przekroczono limit Modrinth API. Pozostało ${waitSeconds}s do odblokowania.`
      );
    }

    const headers: Record<string, string> = {
      'User-Agent': this.userAgent,
      Accept: 'application/json',
      ...(options?.headers || {}),
    };

    if (options?.body) {
      headers['Content-Type'] = 'application/json';
    }

    const fetchOptions: RequestInit = {
      method: options?.method || (options?.body ? 'POST' : 'GET'),
      headers,
      body: options?.body ? JSON.stringify(options.body) : undefined,
    };

    const res = await fetch(url, fetchOptions);

    // Track rate limit headers
    const limitHeader = res.headers.get('x-ratelimit-limit');
    const remainingHeader = res.headers.get('x-ratelimit-remaining');
    const resetHeader = res.headers.get('x-ratelimit-reset');

    if (limitHeader) this.rateLimitLimit = parseInt(limitHeader, 10) || this.rateLimitLimit;
    if (remainingHeader) this.rateLimitRemaining = parseInt(remainingHeader, 10) || 0;
    if (resetHeader) {
      const resetSec = parseInt(resetHeader, 10) || 60;
      this.rateLimitResetTimeMs = Date.now() + resetSec * 1000;
    }

    // Handle 429 Too Many Requests
    if (res.status === 429) {
      const resetSec = parseInt(resetHeader || '60', 10) || 60;
      this.rateLimitResetTimeMs = Date.now() + resetSec * 1000;
      this.rateLimitRemaining = 0;
      throw new ModrinthRateLimitError(resetSec);
    }

    if (!res.ok) {
      const errText = await res.text().catch(() => '');
      throw new Error(`Modrinth API error (HTTP ${res.status}): ${errText || res.statusText}`);
    }

    return (await res.json()) as T;
  }

  private static getFromCache<T>(key: string): T | null {
    const item = this.memoryCache.get(key);
    if (!item) return null;
    if (Date.now() > item.expiresAt) {
      this.memoryCache.delete(key);
      return null;
    }
    return item.data as T;
  }

  private static putInCache<T>(key: string, data: T, ttlMs: number): void {
    this.memoryCache.set(key, {
      data,
      expiresAt: Date.now() + ttlMs,
    });
  }

  private static async readDiskCache<T>(filename: string): Promise<T | null> {
    try {
      const cacheDir = EnginePaths.getModrinthCacheDir();
      const filePath = path.join(cacheDir, filename);
      if (!fs.existsSync(filePath)) return null;

      const stat = await fs.promises.stat(filePath);
      const ageMs = Date.now() - stat.mtimeMs;
      // 24 hours expiry on disk cache
      if (ageMs > 24 * 60 * 60 * 1000) {
        return null;
      }

      const raw = await fs.promises.readFile(filePath, 'utf-8');
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }

  private static async writeDiskCache<T>(filename: string, data: T): Promise<void> {
    try {
      const cacheDir = EnginePaths.getModrinthCacheDir();
      if (!fs.existsSync(cacheDir)) {
        await fs.promises.mkdir(cacheDir, { recursive: true });
      }
      const filePath = path.join(cacheDir, filename);
      await fs.promises.writeFile(filePath, JSON.stringify(data, null, 2), 'utf-8');
    } catch {
      // Ignore disk cache write errors
    }
  }
}
