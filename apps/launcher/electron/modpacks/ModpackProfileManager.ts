import * as fs from 'node:fs';
import * as path from 'node:path';
import { ModpackRepository } from './ModpackRepository.js';
import { ModFileManager } from './ModFileManager.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { EnginePaths } from '../engine/paths.js';
import type { ModpackRecord, InstanceRecord } from '@zombek/shared-types';

export class ModpackProfileManager {
  /**
   * Duplicates a modpack profile creating a genuinely independent copy of all data and mod files.
   */
  public static async duplicateModpack(
    modpackId: string,
    newName?: string
  ): Promise<ModpackRecord> {
    const original = await ModpackRepository.getById(modpackId);
    if (!original) {
      throw new Error(`Modpack o ID '${modpackId}' nie istnieje.`);
    }

    const name = newName || `${original.name} (Kopia)`;

    // 1. If original was assigned to an instance, create a new cloned instance to hold cloned mods
    let newInstanceId: string | undefined;
    if (original.instanceId) {
      const origInstance = await InstallationRepository.getById(original.instanceId);
      if (origInstance) {
        const clonedInstance = await InstallationRepository.create({
          name: `${origInstance.name} (Kopia Modpacka)`,
          minecraftVersion: origInstance.minecraftVersion,
          loader: origInstance.loader,
          loaderVersion: origInstance.loaderVersion,
          allocatedRamMb: origInstance.allocatedRamMb,
          customJvmArgs: origInstance.customJvmArgs,
          resolutionWidth: origInstance.resolutionWidth,
          resolutionHeight: origInstance.resolutionHeight,
          fullscreen: origInstance.fullscreen,
          assignedAccountId: origInstance.assignedAccountId,
        });
        newInstanceId = clonedInstance.id;

        // Copy files from original instance mods and config
        await this.copyInstanceModFiles(origInstance.id, clonedInstance.id);
      }
    }

    // 2. Create the duplicated modpack record
    const duplicated = await ModpackRepository.create({
      name,
      description: original.description,
      minecraftVersion: original.minecraftVersion,
      loader: original.loader,
      loaderVersion: original.loaderVersion,
      instanceId: newInstanceId,
      isDefault: false,
    });

    await ModpackRepository.update(duplicated.id, {
      modFiles: [...original.modFiles],
      modsCount: original.modsCount,
      compatibilityStatus: original.compatibilityStatus,
    });

    return duplicated;
  }

  /**
   * Deep-copies mod files, disabled-mod files, and configs between two instances.
   */
  public static async copyInstanceModFiles(sourceInstanceId: string, targetInstanceId: string): Promise<void> {
    const sourceDirs = await ModFileManager.ensureDirectories(sourceInstanceId);
    const targetDirs = await ModFileManager.ensureDirectories(targetInstanceId);

    const copyDir = async (src: string, dst: string) => {
      if (!fs.existsSync(src)) return;
      if (!fs.existsSync(dst)) {
        await fs.promises.mkdir(dst, { recursive: true });
      }
      const files = await fs.promises.readdir(src);
      for (const f of files) {
        const srcFile = path.join(src, f);
        const dstFile = path.join(dst, f);
        const stat = await fs.promises.stat(srcFile);
        if (stat.isFile()) {
          await fs.promises.copyFile(srcFile, dstFile);
        } else if (stat.isDirectory()) {
          await copyDir(srcFile, dstFile);
        }
      }
    };

    await copyDir(sourceDirs.modsDir, targetDirs.modsDir);
    await copyDir(sourceDirs.disabledModsDir, targetDirs.disabledModsDir);
    await copyDir(sourceDirs.configDir, targetDirs.configDir);
  }

  /**
   * Applies a modpack profile to an instance.
   */
  public static async applyModpackToInstance(modpackId: string, instanceId: string): Promise<boolean> {
    const pack = await ModpackRepository.getById(modpackId);
    if (!pack) {
      throw new Error(`Modpack o ID '${modpackId}' nie istnieje.`);
    }

    const instance = await InstallationRepository.getById(instanceId);
    if (!instance) {
      throw new Error(`Instancja o ID '${instanceId}' nie istnieje.`);
    }

    if (instance.loader !== pack.loader) {
      throw new Error(
        `Niezgodność modloadera: Modpack wymaga ${pack.loader.toUpperCase()}, a wybrana instalacja to ${instance.loader.toUpperCase()}.`
      );
    }

    // Associate pack with instance
    await ModpackRepository.update(modpackId, { instanceId });

    // Write instance-level modpack.json metadata
    const instanceModpackJson = path.join(EnginePaths.getInstanceDir(instanceId), 'modpack.json');
    await fs.promises.writeFile(
      instanceModpackJson,
      JSON.stringify(
        {
          modpackId: pack.id,
          name: pack.name,
          minecraftVersion: pack.minecraftVersion,
          loader: pack.loader,
          loaderVersion: pack.loaderVersion,
          appliedDate: new Date().toISOString(),
        },
        null,
        2
      ),
      'utf-8'
    );

    return true;
  }
}
