import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import { GameLauncher } from '../engine/GameLauncher.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { ModRepository } from './ModRepository.js';
import { ModpackBackupManager } from './ModpackBackupManager.js';
import { ModpackLockfileManager } from './ModpackLockfileManager.js';
import { ModDependencyResolver } from './ModDependencyResolver.js';
import { ModrinthApiClient } from '../modrinth/ModrinthApiClient.js';
import { ModrinthDownloader, type DownloadProgressInfo } from '../modrinth/ModrinthDownloader.js';
import type {
  ModTransactionApplyOptions,
  ModTransactionResult,
  TransactionProgressInfo,
  ModrinthVersion,
  ModrinthVersionFile,
  ModRecord,
} from '@zombek/shared-types';

export type TransactionProgressCallback = (progress: TransactionProgressInfo) => void;

export class ModpackTransactionManager {
  private static activeTransactions = new Set<string>();

  /**
   * Applies updates or dependency installations transactionally with atomic rollback support.
   */
  public static async applyTransaction(
    options: ModTransactionApplyOptions,
    onProgress?: TransactionProgressCallback
  ): Promise<ModTransactionResult> {
    const { instanceId, updates, createBackup = true, backupDescription } = options;

    if (!updates || updates.length === 0) {
      return { success: true, appliedCount: 0 };
    }

    // 1. Guard against concurrent transactions on same instance
    if (this.activeTransactions.has(instanceId)) {
      return {
        success: false,
        error: 'Dla tej instalacji trwa już inna operacja modyfikacji modpacka. Poczekaj na jej zakończenie.',
      };
    }

    // 2. Critical Safety: Verify Minecraft is NOT running on this instance
    const gameStatus = GameLauncher.getInstance().getStatus();
    if (
      gameStatus.currentInstanceId === instanceId &&
      (gameStatus.state === 'running' || gameStatus.pid !== null)
    ) {
      return {
        success: false,
        error: 'Gra Minecraft jest aktualnie uruchomiona na tej instalacji. Zamknij grę przed aktualizacją modów.',
      };
    }

    this.activeTransactions.add(instanceId);

    const txId = `tx-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
    const stagingDir = path.join(EnginePaths.getTempTransactionsDir(), txId);

    let createdBackupId: string | undefined = undefined;

    try {
      await fs.promises.mkdir(stagingDir, { recursive: true });

      const instance = await InstallationRepository.getById(instanceId);
      if (!instance) {
        throw new Error(`Instalacja '${instanceId}' nie istnieje.`);
      }

      onProgress?.({
        stage: 'checking-game',
        message: 'Sprawdzanie stanu gry i blokada instalacji...',
        currentStep: 1,
        totalSteps: 6,
      });

      // 3. Load currently installed mods and lockfile
      const installedMods = await ModRepository.getModsForInstance(instanceId);
      const currentLockfile = await ModpackLockfileManager.getLockfile(instanceId);

      onProgress?.({
        stage: 'resolving-deps',
        message: 'Rozwiązywanie drzewa zależności i sprawdzanie konfliktów...',
        currentStep: 2,
        totalSteps: 6,
      });

      // 4. Resolve candidate versions and their recursive dependencies
      const allVersionsToInstall = new Map<string, ModrinthVersion>();
      const oldFileNamesToRemove = new Set<string>();

      for (const update of updates) {
        if (update.fileName) {
          oldFileNamesToRemove.add(update.fileName);
        }

        const targetVer = await ModrinthApiClient.getVersion(update.versionId);
        if (!targetVer) {
          throw new Error(`Nie odnaleziono wersji Modrinth o ID '${update.versionId}'.`);
        }

        allVersionsToInstall.set(targetVer.id, targetVer);

        // Resolve dependencies recursively
        const plan = await ModDependencyResolver.resolvePlan({
          targetVersion: targetVer,
          installedMods,
          targetInstance: {
            loader: instance.loader,
            minecraftVersion: instance.minecraftVersion,
            loaderVersion: instance.loaderVersion,
            instanceId,
          },
        });

        if (!plan.canProceed) {
          const conflictMsgs = plan.conflicts.map((c) => c.description).join('; ');
          const manualMsgs = plan.manualActionRequired.map((m) => m.reason).join('; ');
          throw new Error(
            `Zablokowano aktualizację: Wykryto nierozwiązywalne konflikty lub brakujące zależności (${conflictMsgs || manualMsgs})`
          );
        }

        // Add resolved required dependencies into install map
        for (const depItem of plan.toInstall) {
          allVersionsToInstall.set(depItem.version.id, depItem.version);
        }
      }

      // 5. Stage & Download all files
      onProgress?.({
        stage: 'downloading',
        message: `Pobieranie plików modów (${allVersionsToInstall.size} plików)...`,
        currentStep: 3,
        totalSteps: 6,
      });

      const downloadedStagedFiles: Array<{
        tempPath: string;
        targetFileName: string;
        version: ModrinthVersion;
      }> = [];

      for (const version of allVersionsToInstall.values()) {
        const primaryFile: ModrinthVersionFile | undefined =
          version.files.find((f) => f.primary) ||
          version.files.find((f) => f.filename.endsWith('.jar')) ||
          version.files[0];

        if (!primaryFile) {
          throw new Error(`Wydanie '${version.name}' nie zawiera pliku instalacyjnego .jar.`);
        }

        const stagedFilePath = await ModrinthDownloader.downloadAndVerify(
          primaryFile,
          (dlInfo: DownloadProgressInfo) => {
            onProgress?.({
              stage: 'downloading',
              message: `Pobieranie: ${primaryFile.filename} (${dlInfo.percent}%)`,
              currentStep: 3,
              totalSteps: 6,
              downloadProgress: dlInfo,
            });
          }
        );

        downloadedStagedFiles.push({
          tempPath: stagedFilePath,
          targetFileName: path.basename(primaryFile.filename),
          version,
        });
      }

      // 6. Create Backup of files to be modified
      if (createBackup) {
        onProgress?.({
          stage: 'backing-up',
          message: 'Tworzenie migawki i kopii zapasowej przed zmianami...',
          currentStep: 4,
          totalSteps: 6,
        });

        const backupInfo = await ModpackBackupManager.createBackup({
          instanceId,
          description:
            backupDescription ||
            `Kopia zapasowa przed aktualizacją (${updates.length} modów) - ${new Date().toLocaleString()}`,
          fileNamesToBackup: Array.from(oldFileNamesToRemove),
          lockfileSnapshot: currentLockfile,
        });

        createdBackupId = backupInfo.id;
      }

      // 7. Apply Changes Atomically
      onProgress?.({
        stage: 'applying',
        message: 'Stosowanie zmian w katalogu mods...',
        currentStep: 5,
        totalSteps: 6,
      });

      const modsDir = EnginePaths.getInstanceModsDir(instanceId);
      if (!fs.existsSync(modsDir)) {
        await fs.promises.mkdir(modsDir, { recursive: true });
      }

      // Remove superseded old versions
      for (const oldFileName of oldFileNamesToRemove) {
        const oldPath = path.join(modsDir, oldFileName);
        if (fs.existsSync(oldPath)) {
          await fs.promises.unlink(oldPath);
        }
      }

      // Copy new files from staging to instance mods dir
      for (const item of downloadedStagedFiles) {
        const destPath = path.join(modsDir, item.targetFileName);
        await fs.promises.copyFile(item.tempPath, destPath);
      }

      // 8. Update Lockfile
      onProgress?.({
        stage: 'updating-lockfile',
        message: 'Aktualizacja pliku modpack.lock.json...',
        currentStep: 6,
        totalSteps: 6,
      });

      const updatedInstalledMods = await ModRepository.getModsForInstance(instanceId);
      await ModpackLockfileManager.syncFromInstalledMods(instanceId, updatedInstalledMods);

      // 9. Clean up staging directory
      if (fs.existsSync(stagingDir)) {
        await fs.promises.rm(stagingDir, { recursive: true, force: true });
      }

      onProgress?.({
        stage: 'completed',
        message: 'Zakończono pomyślnie!',
        currentStep: 6,
        totalSteps: 6,
      });

      return {
        success: true,
        appliedCount: downloadedStagedFiles.length,
        backupId: createdBackupId,
      };
    } catch (err: any) {
      console.error(`[TransactionManager] Błąd transakcji ${txId}:`, err);

      // Automatic Rollback
      let rolledBack = false;
      let rollbackError: string | undefined = undefined;

      if (createdBackupId) {
        onProgress?.({
          stage: 'rolling-back',
          message: 'Wycofywanie zmian i przywracanie stanu z kopii zapasowej...',
          currentStep: 0,
          totalSteps: 6,
        });

        const rbRes = await ModpackBackupManager.rollbackBackup(instanceId, createdBackupId);
        rolledBack = rbRes.success;
        rollbackError = rbRes.error;
      } else {
        rolledBack = true;
      }

      // Clean up staging directory
      if (fs.existsSync(stagingDir)) {
        try {
          await fs.promises.rm(stagingDir, { recursive: true, force: true });
        } catch {
          // ignore
        }
      }

      onProgress?.({
        stage: 'failed',
        message: `Błąd: ${err.message}`,
        currentStep: 0,
        totalSteps: 6,
      });

      return {
        success: false,
        error: err.message,
        rolledBack,
        rollbackError,
      };
    } finally {
      this.activeTransactions.delete(instanceId);
    }
  }
}
