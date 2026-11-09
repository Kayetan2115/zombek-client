import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from './paths.js';
import type { MojangVersionPackage } from './types.js';
import type { MojangVersionManifest, MojangVersionItem } from '@zombek/shared-types';

export const MOJANG_MANIFEST_URL = 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json';

export class VersionManifestService {
  private static cachedManifest: MojangVersionManifest | null = null;

  public static getCachePath(): string {
    return path.join(EnginePaths.getVersionsDir(), 'version_manifest_cache.json');
  }

  public static async getManifest(forceRefresh: boolean = false): Promise<MojangVersionManifest> {
    if (!forceRefresh && this.cachedManifest) {
      return this.cachedManifest;
    }

    const cacheFile = this.getCachePath();

    // Check disk cache first if not forced
    if (!forceRefresh && fs.existsSync(cacheFile)) {
      try {
        const data = await fs.promises.readFile(cacheFile, 'utf-8');
        const parsed = JSON.parse(data) as MojangVersionManifest;
        if (parsed.versions && parsed.versions.length > 0) {
          this.cachedManifest = parsed;
          return parsed;
        }
      } catch {
        // invalid disk cache, proceed to fetch
      }
    }

    // Fetch from Mojang
    try {
      const res = await fetch(MOJANG_MANIFEST_URL);
      if (!res.ok) {
        throw new Error(`Mojang API responded with HTTP ${res.status}`);
      }

      const json = (await res.json()) as any;
      const manifest: MojangVersionManifest = {
        latest: json.latest,
        versions: (json.versions || []).map((v: any): MojangVersionItem => ({
          id: v.id,
          type: v.type,
          url: v.url,
          time: v.time,
          releaseTime: v.releaseTime,
          sha1: v.sha1,
          complianceLevel: v.complianceLevel
        })),
        lastUpdated: new Date().toISOString()
      };

      this.cachedManifest = manifest;

      // Save to disk cache
      EnginePaths.ensureAllBaseDirs();
      await fs.promises.writeFile(cacheFile, JSON.stringify(manifest, null, 2), 'utf-8');

      return manifest;
    } catch (err: any) {
      // Offline fallback: try reading older cache
      if (fs.existsSync(cacheFile)) {
        try {
          const data = await fs.promises.readFile(cacheFile, 'utf-8');
          const fallback = JSON.parse(data) as MojangVersionManifest;
          this.cachedManifest = fallback;
          return fallback;
        } catch {
          // ignore
        }
      }
      throw new Error(`Nie udało się pobrać manifestu wersji Mojang: ${err.message}. Sprawdź połączenie z internetem.`);
    }
  }

  public static async getVersionPackage(versionId: string, manifestUrl?: string): Promise<MojangVersionPackage> {
    EnginePaths.assertSafeFilename(versionId);
    const versionDir = path.join(EnginePaths.getVersionsDir(), versionId);
    const versionJsonPath = path.join(versionDir, `${versionId}.json`);

    if (fs.existsSync(versionJsonPath)) {
      try {
        const raw = await fs.promises.readFile(versionJsonPath, 'utf-8');
        const parsed = JSON.parse(raw) as MojangVersionPackage;
        if (parsed.id === versionId && parsed.downloads?.client) {
          return this.resolveInheritance(parsed);
        }
      } catch {
        // invalid cache, re-fetch
      }
    }

    // Resolve URL if not directly passed
    let url = manifestUrl;
    if (!url) {
      const manifest = await this.getManifest();
      const entry = manifest.versions.find((v) => v.id === versionId);
      if (!entry) {
        throw new Error(`Wersja Minecraft '${versionId}' nie została znaleziona w manifeście Mojang.`);
      }
      url = entry.url;
    }

    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`Nie udało się pobrać metadanych wersji ${versionId}: HTTP ${res.status}`);
    }

    const versionData = (await res.json()) as MojangVersionPackage;

    if (!fs.existsSync(versionDir)) {
      await fs.promises.mkdir(versionDir, { recursive: true });
    }
    await fs.promises.writeFile(versionJsonPath, JSON.stringify(versionData, null, 2), 'utf-8');

    return this.resolveInheritance(versionData);
  }

  private static async resolveInheritance(versionPkg: MojangVersionPackage): Promise<MojangVersionPackage> {
    if (!versionPkg.inheritsFrom) {
      return versionPkg;
    }

    // Inherits from parent version (e.g. Fabric or OptiFine metadata inheriting from Vanilla)
    const parent = await this.getVersionPackage(versionPkg.inheritsFrom);

    return {
      ...parent,
      ...versionPkg,
      libraries: [...(parent.libraries || []), ...(versionPkg.libraries || [])],
      arguments: {
        game: [...(parent.arguments?.game || []), ...(versionPkg.arguments?.game || [])],
        jvm: [...(parent.arguments?.jvm || []), ...(versionPkg.arguments?.jvm || [])]
      }
    };
  }
}
