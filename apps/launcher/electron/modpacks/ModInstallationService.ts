import * as fs from 'node:fs';
import * as path from 'node:path';
import { JarArchiveReader } from './JarArchiveReader.js';
import { ModMetadataReader } from './ModMetadataReader.js';
import { ModCompatibilityChecker } from './ModCompatibilityChecker.js';
import { ModDependencyAnalyzer } from './ModDependencyAnalyzer.js';
import { ModFileManager } from './ModFileManager.js';
import { ModRepository } from './ModRepository.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import type { ModValidationResult, ModRecord } from '@zombek/shared-types';

export class ModInstallationService {
  /**
   * Validates a candidate JAR file from disk before installation into a target instance.
   */
  public static async validateCandidate(
    sourcePath: string,
    targetInstanceId: string
  ): Promise<ModValidationResult> {
    const fileName = path.basename(sourcePath);

    // 1. Check file existence & extension
    if (!fs.existsSync(sourcePath)) {
      return {
        valid: false,
        fileName,
        sha256: '',
        fileSize: 0,
        metadata: ModMetadataReader.createUnknownMetadata(sourcePath),
        compatibilityStatus: 'Broken File',
        compatibilityDetails: 'Plik nie istnieje na dysku.',
        missingDependencies: [],
        conflictingMods: [],
        error: `Plik nie istnieje: ${sourcePath}`,
      };
    }

    if (!fileName.toLowerCase().endsWith('.jar')) {
      return {
        valid: false,
        fileName,
        sha256: '',
        fileSize: 0,
        metadata: ModMetadataReader.createUnknownMetadata(sourcePath),
        compatibilityStatus: 'Broken File',
        compatibilityDetails: 'Nieprawidłowe rozszerzenie pliku (wymagany .jar).',
        missingDependencies: [],
        conflictingMods: [],
        error: 'Wybrany plik nie ma rozszerzenia .jar.',
      };
    }

    // 2. Validate archive structure & calculate SHA-256
    let sha256 = '';
    let fileSize = 0;
    try {
      const stat = await fs.promises.stat(sourcePath);
      fileSize = stat.size;
      sha256 = await ModFileManager.calculateSha256(sourcePath);
      await JarArchiveReader.listEntries(sourcePath);
    } catch (err: any) {
      return {
        valid: false,
        fileName,
        sha256,
        fileSize,
        metadata: ModMetadataReader.createUnknownMetadata(sourcePath),
        compatibilityStatus: 'Broken File',
        compatibilityDetails: `Uszkodzone archiwum JAR: ${err.message}`,
        missingDependencies: [],
        conflictingMods: [],
        error: `Plik jest uszkodzony lub nie jest poprawnym archiwum ZIP/JAR: ${err.message}`,
      };
    }

    // 3. Read metadata
    const metadata = await ModMetadataReader.readMetadata(sourcePath);

    // 4. Check target instance
    const instance = await InstallationRepository.getById(targetInstanceId);
    if (!instance) {
      return {
        valid: false,
        fileName,
        sha256,
        fileSize,
        metadata,
        compatibilityStatus: 'Incompatible',
        compatibilityDetails: `Instancja ${targetInstanceId} nie istnieje.`,
        missingDependencies: [],
        conflictingMods: [],
        error: `Instancja ${targetInstanceId} nie istnieje.`,
      };
    }

    // 5. Check duplicates in target instance
    const existingMods = await ModRepository.getModsForInstance(targetInstanceId);
    const sameHashMod = existingMods.find((m) => m.sha256 === sha256);
    const sameNameMod = existingMods.find((m) => m.fileName.toLowerCase() === fileName.toLowerCase());

    const isDuplicate = Boolean(sameHashMod || sameNameMod);
    const duplicateFileName = sameHashMod?.fileName || sameNameMod?.fileName;

    // 6. Check dependencies against installed mods
    const enabledMetas = existingMods.filter((m) => m.enabled).map((m) => m.metadata);
    const analysis = ModDependencyAnalyzer.analyzeDependencies(
      metadata,
      enabledMetas,
      {
        loader: instance.loader,
        minecraftVersion: instance.minecraftVersion,
        loaderVersion: instance.loaderVersion,
      }
    );

    return {
      valid: analysis.status !== 'Broken File' && analysis.status !== 'Incompatible',
      fileName,
      sha256,
      fileSize,
      metadata,
      compatibilityStatus: analysis.status,
      compatibilityDetails: analysis.details || 'Mod jest kompatybilny.',
      missingDependencies: analysis.missingDependencies,
      conflictingMods: analysis.conflictingMods,
      isDuplicate,
      duplicateFileName,
    };
  }

  /**
   * Installs a verified candidate JAR into the target instance.
   */
  public static async installMod(
    sourcePath: string,
    targetInstanceId: string,
    overwrite: boolean = false
  ): Promise<{ success: boolean; mod?: ModRecord; error?: string }> {
    try {
      const copyRes = await ModFileManager.copyModFile(sourcePath, targetInstanceId, overwrite);
      const allMods = await ModRepository.getModsForInstance(targetInstanceId);
      const installedMod = allMods.find((m) => m.fileName === copyRes.fileName);

      return {
        success: true,
        mod: installedMod,
      };
    } catch (err: any) {
      return {
        success: false,
        error: err.message,
      };
    }
  }
}
