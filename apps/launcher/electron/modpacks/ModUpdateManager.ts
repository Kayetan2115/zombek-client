import * as path from 'node:path';
import * as fs from 'node:fs';
import { EnginePaths } from '../engine/paths.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { ModRepository } from './ModRepository.js';
import { ModpackLockfileManager } from './ModpackLockfileManager.js';
import { ModrinthApiClient } from '../modrinth/ModrinthApiClient.js';
import { ModrinthDownloader } from '../modrinth/ModrinthDownloader.js';
import type {
  ModUpdateInfo,
  ModUpdateCheckResult,
  ModRecord,
  ModrinthVersion,
  ModUpdatePolicy,
  ModUpdateStatus,
} from '@zombek/shared-types';

export class ModUpdateManager {
  /**
   * Scans all installed mods in an instance and queries Modrinth API for available updates.
   */
  public static async checkUpdates(
    instanceId: string,
    forceRefresh = false
  ): Promise<ModUpdateCheckResult> {
    const instance = await InstallationRepository.getById(instanceId);
    if (!instance) {
      throw new Error(`Instalacja '${instanceId}' nie została odnaleziona.`);
    }

    const installedMods = await ModRepository.getModsForInstance(instanceId);
    const lockfile = await ModpackLockfileManager.syncFromInstalledMods(instanceId, installedMods);

    if (installedMods.length === 0) {
      return {
        updates: [],
        upToDateCount: 0,
        pinnedCount: 0,
        manualCount: 0,
        conflictsCount: 0,
      };
    }

    // 1. Calculate SHA-512 hashes for all installed JAR files
    const modHashMap = new Map<string, { mod: ModRecord; sha512: string; sha1?: string }>();
    const sha512List: string[] = [];

    for (const mod of installedMods) {
      if (!fs.existsSync(mod.filePath)) continue;
      try {
        const sha512 = await ModrinthDownloader.calculateSha512(mod.filePath);
        const sha1 = await ModrinthDownloader.calculateSha1(mod.filePath);
        modHashMap.set(sha512.toLowerCase(), { mod, sha512, sha1 });
        sha512List.push(sha512);
      } catch (err) {
        console.warn(`[ModUpdateManager] Nie udało się obliczyć hasha dla ${mod.fileName}:`, err);
      }
    }

    // 2. Query Modrinth API in batch via POST /version_files/update
    const loadersQuery =
      instance.loader === 'quilt' ? ['quilt', 'fabric'] : [instance.loader.toLowerCase()];

    let modrinthUpdates: Record<string, ModrinthVersion> = {};
    try {
      modrinthUpdates = await ModrinthApiClient.getLatestUpdatesForFiles({
        hashes: sha512List,
        algorithm: 'sha512',
        loaders: loadersQuery,
        game_versions: [instance.minecraftVersion],
        version_types: ['release', 'beta'],
      });
    } catch (err: any) {
      console.warn(`[ModUpdateManager] Błąd zapytania POST /version_files/update:`, err.message);
    }

    // 3. For any files not recognized in updates, check if they exist in Modrinth at all
    const unrecognizedHashes = sha512List.filter((h) => !modrinthUpdates[h.toLowerCase()]);
    let recognizedFiles: Record<string, ModrinthVersion> = {};
    if (unrecognizedHashes.length > 0) {
      try {
        recognizedFiles = await ModrinthApiClient.getVersionFiles(unrecognizedHashes, 'sha512');
      } catch (err: any) {
        console.warn(`[ModUpdateManager] Błąd zapytania POST /version_files:`, err.message);
      }
    }

    // 4. Map results and build ModUpdateInfo array
    const updates: ModUpdateInfo[] = [];
    let upToDateCount = 0;
    let pinnedCount = 0;
    let manualCount = 0;
    let conflictsCount = 0;

    for (const [hashKey, item] of modHashMap.entries()) {
      const { mod, sha512 } = item;
      const lockEntry = lockfile.mods.find(
        (m) => m.fileName.toLowerCase() === mod.fileName.toLowerCase()
      );

      const isPinned = !!lockEntry?.pinned;
      const policy: ModUpdatePolicy = lockEntry?.updatePolicy || 'stable-only';

      if (isPinned) {
        pinnedCount++;
      }

      const targetVersion: ModrinthVersion | undefined = modrinthUpdates[hashKey];

      if (!targetVersion) {
        // Not returned as having an update.
        // Check if recognized in Modrinth:
        const currentVersionInfo = recognizedFiles[hashKey];
        if (currentVersionInfo) {
          // File is recognized on Modrinth and is already on latest compatible version!
          upToDateCount++;
          updates.push({
            fileName: mod.fileName,
            modName: mod.metadata.name || mod.fileName,
            currentVersion: mod.metadata.version || currentVersionInfo.version_number,
            projectId: currentVersionInfo.project_id,
            currentVersionId: currentVersionInfo.id,
            targetVersion: currentVersionInfo,
            updateType: currentVersionInfo.version_type,
            isPinned,
            updatePolicy: policy,
            sizeDiffBytes: 0,
            currentSizeBytes: mod.fileSize,
            targetSizeBytes: mod.fileSize,
            publishedAt: currentVersionInfo.date_published,
            changelog: currentVersionInfo.changelog,
            dependencies: currentVersionInfo.dependencies || [],
            status: isPinned ? 'pinned' : 'up-to-date',
            statusReason: isPinned ? 'Mod jest przypięty do bieżącej wersji' : 'Najnowsza kompatybilna wersja',
          });
        } else {
          // Unrecognized file (custom / local JAR)
          manualCount++;
          const dummyTarget: ModrinthVersion = {
            id: 'local',
            project_id: 'local',
            author_id: 'local',
            name: mod.metadata.name || mod.fileName,
            version_number: mod.metadata.version || '1.0.0',
            dependencies: [],
            game_versions: [instance.minecraftVersion],
            version_type: 'release',
            loaders: [instance.loader],
            date_published: mod.dateAdded || new Date().toISOString(),
            downloads: 0,
            files: [],
          };

          updates.push({
            fileName: mod.fileName,
            modName: mod.metadata.name || mod.fileName,
            currentVersion: mod.metadata.version || 'lokalna',
            targetVersion: dummyTarget,
            updateType: 'release',
            isPinned,
            updatePolicy: policy,
            sizeDiffBytes: 0,
            currentSizeBytes: mod.fileSize,
            targetSizeBytes: mod.fileSize,
            publishedAt: mod.dateAdded || new Date().toISOString(),
            dependencies: [],
            status: isPinned ? 'pinned' : 'manual-action',
            statusReason: isPinned
              ? 'Wersja przypięta (plik lokalny)'
              : 'Plik dodany ręcznie (brak w oficjalnej bazie Modrinth)',
          });
        }
        continue;
      }

      // We have a target update from Modrinth!
      const targetFile =
        targetVersion.files.find((f) => f.primary) ||
        targetVersion.files.find((f) => f.filename.endsWith('.jar')) ||
        targetVersion.files[0];

      const targetSize = targetFile?.size || mod.fileSize;
      const sizeDiffBytes = targetSize - mod.fileSize;

      // Filter by update policy
      let status: ModUpdateStatus = 'available';
      let statusReason = `Dostępna nowa wersja ${targetVersion.version_number}`;

      if (isPinned) {
        status = 'pinned';
        statusReason = `Wersja przypięta (zablokowano automatyczną aktualizację)`;
      } else if (policy === 'stable-only' && targetVersion.version_type !== 'release') {
        // User policy restricts to stable only
        status = 'up-to-date';
        statusReason = `Dostępna wersja ${targetVersion.version_type.toUpperCase()}, ale polityka wymaga wydania stabilnego (Release)`;
      }

      updates.push({
        fileName: mod.fileName,
        modName: mod.metadata.name || mod.fileName,
        currentVersion: mod.metadata.version || 'unknown',
        projectId: targetVersion.project_id,
        targetVersion,
        updateType: targetVersion.version_type,
        isPinned,
        updatePolicy: policy,
        sizeDiffBytes,
        currentSizeBytes: mod.fileSize,
        targetSizeBytes: targetSize,
        publishedAt: targetVersion.date_published,
        changelog: targetVersion.changelog,
        dependencies: targetVersion.dependencies || [],
        status,
        statusReason,
      });
    }

    return {
      updates,
      upToDateCount,
      pinnedCount,
      manualCount,
      conflictsCount,
    };
  }
}
