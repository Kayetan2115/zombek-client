import * as fs from 'node:fs';
import * as path from 'node:path';
import { ModFileManager } from './ModFileManager.js';
import { ModMetadataReader } from './ModMetadataReader.js';
import { ModDependencyAnalyzer } from './ModDependencyAnalyzer.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { EnginePaths } from '../engine/paths.js';
import type { ModRecord, UniversalModMetadata } from '@zombek/shared-types';

interface ModCacheEntry {
  sha256: string;
  fileSize: number;
  metadata: UniversalModMetadata;
}

export class ModRepository {
  private static memoryCache: Map<string, ModCacheEntry> = new Map();

  /**
   * Scans an instance's mods and disabled-mods directories, extracts metadata,
   * evaluates compatibility and dependencies, and returns the full list of ModRecord.
   */
  public static async getModsForInstance(instanceId: string): Promise<ModRecord[]> {
    const instance = await InstallationRepository.getById(instanceId);
    if (!instance) {
      throw new Error(`Instancja o ID '${instanceId}' nie istnieje.`);
    }

    const { enabled, disabled } = await ModFileManager.listModFiles(instanceId);
    const { modsDir, disabledModsDir } = await ModFileManager.ensureDirectories(instanceId);

    // Read cache from instance directory
    const diskCache = await this.readDiskCache(instanceId);

    const modRecords: ModRecord[] = [];
    const allCandidateMetas: { fileName: string; meta: UniversalModMetadata; enabled: boolean }[] = [];

    // Helper to process a single mod file
    const processFile = async (fileName: string, isEnabled: boolean) => {
      const fullPath = path.join(isEnabled ? modsDir : disabledModsDir, fileName);
      try {
        const stat = await fs.promises.stat(fullPath);
        const sha256 = await ModFileManager.calculateSha256(fullPath);

        let metadata: UniversalModMetadata;
        const cached = this.memoryCache.get(sha256) || diskCache[sha256];

        if (cached && cached.fileSize === stat.size) {
          metadata = cached.metadata;
        } else {
          metadata = await ModMetadataReader.readMetadata(fullPath);
          const entry: ModCacheEntry = { sha256, fileSize: stat.size, metadata };
          this.memoryCache.set(sha256, entry);
          diskCache[sha256] = entry;
        }

        allCandidateMetas.push({ fileName, meta: metadata, enabled: isEnabled });

        const dateAdded = stat.birthtime ? stat.birthtime.toISOString().substring(0, 10) : 'Dzisiaj';

        modRecords.push({
          fileName,
          filePath: fullPath,
          fileSize: stat.size,
          sha256,
          enabled: isEnabled,
          metadata,
          compatibilityStatus: isEnabled ? 'Compatible' : 'Disabled',
          dateAdded,
        });
      } catch (err: any) {
        // Broken file
        modRecords.push({
          fileName,
          filePath: fullPath,
          fileSize: 0,
          sha256: 'corrupted',
          enabled: isEnabled,
          metadata: ModMetadataReader.createUnknownMetadata(fullPath),
          compatibilityStatus: 'Broken File',
          compatibilityDetails: `Uszkodzony plik JAR: ${err.message}`,
          dateAdded: 'Błąd',
        });
      }
    };

    for (const f of enabled) {
      await processFile(f, true);
    }
    for (const f of disabled) {
      await processFile(f, false);
    }

    // Save updated disk cache
    await this.writeDiskCache(instanceId, diskCache);

    // Run dependency & conflict analysis across enabled mods
    const enabledMetas = allCandidateMetas.filter((c) => c.enabled).map((c) => c.meta);

    for (const rec of modRecords) {
      if (rec.compatibilityStatus === 'Broken File') continue;

      const analysis = ModDependencyAnalyzer.analyzeDependencies(
        rec.metadata,
        enabledMetas,
        {
          loader: instance.loader,
          minecraftVersion: instance.minecraftVersion,
          loaderVersion: instance.loaderVersion,
        }
      );

      if (rec.enabled) {
        rec.compatibilityStatus = analysis.status;
      } else {
        rec.compatibilityStatus = 'Disabled';
      }

      rec.compatibilityDetails = analysis.details;
      rec.missingDependencies = analysis.missingDependencies;
      rec.conflictingMods = analysis.conflictingMods;
    }

    // Sort by name alphabetically
    return modRecords.sort((a, b) => a.metadata.name.localeCompare(b.metadata.name));
  }

  /**
   * Toggles a mod between active (mods/) and inactive (disabled-mods/).
   */
  public static async toggleMod(
    instanceId: string,
    fileName: string,
    enabled: boolean
  ): Promise<ModRecord> {
    await ModFileManager.toggleMod(instanceId, fileName, enabled);
    const allMods = await this.getModsForInstance(instanceId);
    const updated = allMods.find((m) => m.fileName === fileName);
    if (!updated) {
      throw new Error(`Mod "${fileName}" nie został znaleziony po zmianie stanu.`);
    }
    return updated;
  }

  /**
   * Deletes a mod from an instance.
   */
  public static async deleteMod(instanceId: string, fileName: string): Promise<boolean> {
    return ModFileManager.deleteModFile(instanceId, fileName);
  }

  /**
   * Invalidates memory and disk cache for an instance.
   */
  public static async invalidateCache(instanceId?: string): Promise<void> {
    this.memoryCache.clear();
    if (instanceId) {
      const p = this.getCachePath(instanceId);
      if (fs.existsSync(p)) {
        try {
          await fs.promises.unlink(p);
        } catch {
          // ignore
        }
      }
    }
  }

  // --- Disk Cache Helpers ---

  private static getCachePath(instanceId: string): string {
    return path.join(EnginePaths.getInstanceDir(instanceId), '.mods-cache.json');
  }

  private static async readDiskCache(instanceId: string): Promise<Record<string, ModCacheEntry>> {
    try {
      const p = this.getCachePath(instanceId);
      if (!fs.existsSync(p)) return {};
      const raw = await fs.promises.readFile(p, 'utf-8');
      return JSON.parse(raw);
    } catch {
      return {};
    }
  }

  private static async writeDiskCache(
    instanceId: string,
    cache: Record<string, ModCacheEntry>
  ): Promise<void> {
    try {
      const p = this.getCachePath(instanceId);
      await fs.promises.writeFile(p, JSON.stringify(cache, null, 2), 'utf-8');
    } catch {
      // ignore
    }
  }
}
