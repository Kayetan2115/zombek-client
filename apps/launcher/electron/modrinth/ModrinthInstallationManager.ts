import * as fs from 'node:fs';
import * as path from 'node:path';
import { ModrinthApiClient } from './ModrinthApiClient.js';
import { ModrinthDownloader, type DownloadProgressCallback } from './ModrinthDownloader.js';
import { ModFileManager } from '../modpacks/ModFileManager.js';
import { ModRepository } from '../modpacks/ModRepository.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import type {
  ModrinthInstallOptions,
  ModrinthInstallResult,
  ModrinthVersion,
  ModrinthVersionFile,
  ModRecord,
} from '@zombek/shared-types';

export class ModrinthInstallationManager {
  /**
   * Installs a specific Modrinth version into a chosen Minecraft instance.
   */
  public static async installVersion(
    options: ModrinthInstallOptions,
    onProgress?: DownloadProgressCallback
  ): Promise<ModrinthInstallResult> {
    try {
      // 1. Verify target instance exists
      const instance = await InstallationRepository.getById(options.instanceId);
      if (!instance) {
        return {
          success: false,
          error: `Instalacja docelowa '${options.instanceId}' nie istnieje.`,
        };
      }

      // 2. Fetch version details from Modrinth API
      const version = await ModrinthApiClient.getVersion(options.versionId);
      if (!version) {
        return {
          success: false,
          error: `Wydanie Modrinth o ID '${options.versionId}' nie zostało odnalezione.`,
        };
      }

      // 3. Compatibility check (loader & game version)
      const loaderCompatible = this.isLoaderCompatible(version.loaders, instance.loader);
      if (!loaderCompatible) {
        return {
          success: false,
          error: `Niezgodność modloadera: Wydanie moda obsługuje [${version.loaders.join(', ')}], a Twoja instalacja to [${instance.loader.toUpperCase()}].`,
        };
      }

      const versionCompatible = version.game_versions.includes(instance.minecraftVersion);
      if (!versionCompatible) {
        return {
          success: false,
          error: `Niezgodność wersji gry: Wydanie moda przeznaczone jest dla [${version.game_versions.join(', ')}], a Twoja instalacja to ${instance.minecraftVersion}.`,
        };
      }

      // 4. Select the primary file to download
      const targetFile = this.selectPrimaryFile(version, options.targetFileName);
      if (!targetFile) {
        return {
          success: false,
          error: 'Wydanie nie zawiera odpowiedniego pliku modyfikacji Java (.jar).',
        };
      }

      const fileName = path.basename(targetFile.filename);
      ModFileManager.assertSafeName(fileName);

      // 5. Pre-check for duplicate files in instance
      const existingMods = await ModRepository.getModsForInstance(options.instanceId);
      const duplicateMod = existingMods.find(
        (m) => m.fileName.toLowerCase() === fileName.toLowerCase()
      );

      if (duplicateMod && !(options.overwrite || options.overwriteDuplicate)) {
        return {
          success: false,
          fileName,
          error: `Mod '${fileName}' jest już zainstalowany w tej instalacji. Potwierdź nadpisanie pliku.`,
        };
      }

      // 6. Analyze dependencies from Modrinth metadata
      const missingDependencies = await this.detectMissingDependencies(
        version,
        existingMods
      );

      // 7. Download and verify hash in temp location
      const tempDownloadedFile = await ModrinthDownloader.downloadAndVerify(
        targetFile,
        onProgress
      );

      try {
        // 8. Copy file safely to instance mods directory
        const copyRes = await ModFileManager.copyModFile(
          tempDownloadedFile,
          options.instanceId,
          Boolean(options.overwrite)
        );

        // 9. Refresh instance mod repository
        const updatedMods = await ModRepository.getModsForInstance(options.instanceId);
        const installedModRecord = updatedMods.find((m) => m.fileName === copyRes.fileName);

        return {
          success: true,
          fileName: copyRes.fileName,
          filePath: copyRes.destinationPath,
          modRecord: installedModRecord,
          missingDependencies,
        };
      } finally {
        // Always clean up temp file
        if (fs.existsSync(tempDownloadedFile)) {
          try {
            fs.unlinkSync(tempDownloadedFile);
          } catch {
            // ignore
          }
        }
      }
    } catch (err: any) {
      return {
        success: false,
        error: err.message || 'Wystąpił nieoczekiwany błąd podczas instalacji moda.',
      };
    }
  }

  /**
   * Evaluates if version loaders match the instance loader (with Quilt -> Fabric compatibility).
   */
  public static isLoaderCompatible(versionLoaders: string[], instanceLoader: string): boolean {
    const inst = instanceLoader.toLowerCase();
    const vLoaders = versionLoaders.map((l) => l.toLowerCase());

    if (vLoaders.includes(inst)) {
      return true;
    }

    // Quilt can run Fabric mods
    if (inst === 'quilt' && vLoaders.includes('fabric')) {
      return true;
    }

    // NeoForge 1.20.1 can run some Forge mods, but generally loaders are distinct
    return false;
  }

  /**
   * Selects the most appropriate JAR file to download from version files.
   */
  public static selectPrimaryFile(
    version: ModrinthVersion,
    preferredFileName?: string
  ): ModrinthVersionFile | null {
    if (!version.files || version.files.length === 0) {
      return null;
    }

    const jarFiles = version.files.filter((f) => f.filename.toLowerCase().endsWith('.jar'));
    if (jarFiles.length === 0) {
      return null;
    }

    // 1. If preferred name matches
    if (preferredFileName) {
      const match = jarFiles.find((f) => f.filename.toLowerCase() === preferredFileName.toLowerCase());
      if (match) return match;
    }

    // 2. Primary flag
    const primary = jarFiles.find((f) => f.primary);
    if (primary) return primary;

    // 3. Fallback: first jar file
    return jarFiles[0];
  }

  /**
   * Identifies missing required dependencies from Modrinth version metadata.
   */
  private static async detectMissingDependencies(
    version: ModrinthVersion,
    installedMods: ModRecord[]
  ): Promise<string[]> {
    const missing: string[] = [];

    const requiredDeps = (version.dependencies || []).filter(
      (d) => d.dependency_type === 'required'
    );

    if (requiredDeps.length === 0) {
      return [];
    }

    // Index installed mods by ID and name for fast matching
    const installedIds = new Set<string>();
    for (const m of installedMods) {
      if (m.metadata.id) installedIds.add(m.metadata.id.toLowerCase());
      if (m.metadata.name) installedIds.add(m.metadata.name.toLowerCase());
      installedIds.add(m.fileName.toLowerCase());
    }

    for (const dep of requiredDeps) {
      let isPresent = false;

      if (dep.file_name && installedIds.has(dep.file_name.toLowerCase())) {
        isPresent = true;
      }

      if (dep.project_id && installedIds.has(dep.project_id.toLowerCase())) {
        isPresent = true;
      }

      if (!isPresent) {
        missing.push(dep.file_name || dep.project_id || 'Nieznana biblioteka wymagana');
      }
    }

    return missing;
  }
}
