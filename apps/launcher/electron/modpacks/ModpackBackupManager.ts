import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import { ModRepository } from './ModRepository.js';
import type { ModpackBackupInfo, ModpackLockfile } from '@zombek/shared-types';

export class ModpackBackupManager {
  public static readonly MAX_BACKUPS_PER_INSTANCE = 10;

  /**
   * Creates a backup snapshot of specified files and the lockfile prior to an update.
   */
  public static async createBackup(params: {
    instanceId: string;
    description: string;
    fileNamesToBackup: string[];
    lockfileSnapshot?: ModpackLockfile | null;
  }): Promise<ModpackBackupInfo> {
    const { instanceId, description, fileNamesToBackup, lockfileSnapshot } = params;

    const backupsBaseDir = EnginePaths.getInstanceBackupsDir(instanceId);
    const backupId = `backup-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
    const backupDir = path.join(backupsBaseDir, backupId);

    await fs.promises.mkdir(backupDir, { recursive: true });

    const modsDir = EnginePaths.getInstanceModsDir(instanceId);
    const backedUpFiles: string[] = [];
    let totalSizeBytes = 0;

    for (const fileName of fileNamesToBackup) {
      const srcPath = path.join(modsDir, fileName);
      if (fs.existsSync(srcPath)) {
        const destPath = path.join(backupDir, fileName);
        await fs.promises.copyFile(srcPath, destPath);
        const stat = await fs.promises.stat(destPath);
        totalSizeBytes += stat.size;
        backedUpFiles.push(fileName);
      }
    }

    // Save lockfile snapshot in the backup directory if provided
    if (lockfileSnapshot) {
      const lockPath = path.join(backupDir, 'modpack.lock.json');
      await fs.promises.writeFile(lockPath, JSON.stringify(lockfileSnapshot, null, 2), 'utf-8');
    }

    const backupInfo: ModpackBackupInfo = {
      id: backupId,
      instanceId,
      timestamp: new Date().toISOString(),
      description,
      filesCount: backedUpFiles.length,
      totalSizeBytes,
      modNames: backedUpFiles,
      lockfileSnapshot: lockfileSnapshot || undefined,
    };

    // Save manifest.json
    const manifestPath = path.join(backupDir, 'manifest.json');
    await fs.promises.writeFile(manifestPath, JSON.stringify(backupInfo, null, 2), 'utf-8');

    // Apply retention policy: remove older backups exceeding limit
    await this.pruneOldBackups(instanceId);

    return backupInfo;
  }

  /**
   * Lists all available backups for an instance, sorted newest first.
   */
  public static async listBackups(instanceId: string): Promise<ModpackBackupInfo[]> {
    const backupsBaseDir = EnginePaths.getInstanceBackupsDir(instanceId);
    if (!fs.existsSync(backupsBaseDir)) return [];

    const entries = await fs.promises.readdir(backupsBaseDir, { withFileTypes: true });
    const result: ModpackBackupInfo[] = [];

    for (const entry of entries) {
      if (entry.isDirectory() && entry.name.startsWith('backup-')) {
        const manifestPath = path.join(backupsBaseDir, entry.name, 'manifest.json');
        if (fs.existsSync(manifestPath)) {
          try {
            const raw = await fs.promises.readFile(manifestPath, 'utf-8');
            result.push(JSON.parse(raw) as ModpackBackupInfo);
          } catch {
            // Ignore broken backup manifest
          }
        }
      }
    }

    return result.sort(
      (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
  }

  /**
   * Restores files and lockfile from a backup (Rollback).
   */
  public static async rollback(
    instanceId: string,
    backupId: string
  ): Promise<{ success: boolean; restoredCount?: number; error?: string }> {
    return this.rollbackBackup(instanceId, backupId);
  }

  public static async rollbackBackup(
    instanceId: string,
    backupId: string
  ): Promise<{ success: boolean; restoredCount?: number; error?: string }> {
    const backupsBaseDir = EnginePaths.getInstanceBackupsDir(instanceId);
    const backupDir = path.join(backupsBaseDir, backupId);

    if (!fs.existsSync(backupDir)) {
      return { success: false, error: `Kopia zapasowa o ID '${backupId}' nie istnieje.` };
    }

    const manifestPath = path.join(backupDir, 'manifest.json');
    if (!fs.existsSync(manifestPath)) {
      return { success: false, error: 'Kopia zapasowa nie zawiera pliku manifest.json.' };
    }

    try {
      const raw = await fs.promises.readFile(manifestPath, 'utf-8');
      const manifest = JSON.parse(raw) as ModpackBackupInfo;
      const modsDir = EnginePaths.getInstanceModsDir(instanceId);

      if (!fs.existsSync(modsDir)) {
        await fs.promises.mkdir(modsDir, { recursive: true });
      }

      let restoredCount = 0;

      // Remove any extraneous jar files in modsDir that were not present in the backup snapshot
      const currentFilesInModsDir = await fs.promises.readdir(modsDir);
      for (const curFile of currentFilesInModsDir) {
        if (
          !manifest.modNames.includes(curFile) &&
          (curFile.endsWith('.jar') || curFile.endsWith('.jar.disabled'))
        ) {
          try {
            await fs.promises.unlink(path.join(modsDir, curFile));
          } catch {
            // ignore
          }
        }
      }

      // Restore each mod file from backup
      for (const fileName of manifest.modNames) {
        const srcPath = path.join(backupDir, fileName);
        if (fs.existsSync(srcPath)) {
          const destPath = path.join(modsDir, fileName);
          await fs.promises.copyFile(srcPath, destPath);
          restoredCount++;
        }
      }

      // Restore lockfile if present in backup
      const backupLockPath = path.join(backupDir, 'modpack.lock.json');
      if (fs.existsSync(backupLockPath)) {
        const destLockPath = EnginePaths.getInstanceLockfilePath(instanceId);
        await fs.promises.copyFile(backupLockPath, destLockPath);
      }

      // Invalidate ModRepository cache
      await ModRepository.invalidateCache(instanceId);

      return { success: true, restoredCount };
    } catch (err: any) {
      return {
        success: false,
        error: `Błąd podczas przywracania kopii zapasowej: ${err.message}`,
      };
    }
  }

  /**
   * Prunes older backups beyond MAX_BACKUPS_PER_INSTANCE.
   */
  private static async pruneOldBackups(instanceId: string): Promise<void> {
    try {
      const backups = await this.listBackups(instanceId);
      if (backups.length <= this.MAX_BACKUPS_PER_INSTANCE) return;

      const backupsToDelete = backups.slice(this.MAX_BACKUPS_PER_INSTANCE);
      const backupsBaseDir = EnginePaths.getInstanceBackupsDir(instanceId);

      for (const b of backupsToDelete) {
        const dir = path.join(backupsBaseDir, b.id);
        if (fs.existsSync(dir)) {
          await fs.promises.rm(dir, { recursive: true, force: true });
        }
      }
    } catch (err) {
      console.warn(`[BackupManager] Błąd podczas czyszczenia starych kopii:`, err);
    }
  }
}
