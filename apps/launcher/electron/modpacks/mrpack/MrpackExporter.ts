import * as fs from 'node:fs';
import * as path from 'node:path';
import * as crypto from 'node:crypto';
import { EnginePaths } from '../../engine/paths.js';
import { InstallationRepository } from '../../engine/InstallationRepository.js';
import { ModRepository } from '../ModRepository.js';
import { ModpackLockfileManager } from '../ModpackLockfileManager.js';
import { MrpackArchiveWriter } from './MrpackArchiveWriter.js';
import { MrpackOverrideManager } from './MrpackOverrideManager.js';
import type {
  MrpackExportOptions,
  MrpackExportResult,
  MrpackIndex,
  MrpackIndexFile,
  MrpackDependencies,
  ModRecord,
} from '@zombek/shared-types';

export class MrpackExporter {
  /**
   * Exports an existing Minecraft instance to a standard-compliant .mrpack archive.
   */
  public static async export(options: MrpackExportOptions): Promise<MrpackExportResult> {
    const {
      instanceId,
      outputFilePath,
      name,
      summary,
      versionId = '1.0.0',
      includeCustomJars = false,
      selectedOverrides = { config: true, optionsTxt: true, resourcepacks: true, shaderpacks: true },
    } = options;

    const instance = await InstallationRepository.getById(instanceId);
    if (!instance) {
      throw new Error(`Instalacja o ID '${instanceId}' nie została odnaleziona.`);
    }

    const instanceDir = EnginePaths.getInstanceDir(instanceId);
    if (!fs.existsSync(instanceDir)) {
      throw new Error(`Katalog instalacji nie istnieje: ${instanceDir}`);
    }

    const warningMessages: string[] = [];

    // 1. Build dependencies
    const dependencies: MrpackDependencies = {
      minecraft: instance.minecraftVersion,
    };

    if (instance.loader === 'fabric') {
      dependencies['fabric-loader'] = instance.loaderVersion || '0.15.11';
    } else if (instance.loader === 'forge') {
      dependencies['forge'] = instance.loaderVersion || '47.2.0';
    } else if (instance.loader === 'neoforge') {
      dependencies['neoforge'] = instance.loaderVersion || '20.4.80-beta';
    } else if (instance.loader === 'quilt') {
      dependencies['quilt-loader'] = instance.loaderVersion || '0.25.0';
    }
    // Vanilla has no loader dependency key

    // 2. Read installed mods and lockfile
    const installedMods = await ModRepository.getModsForInstance(instanceId);
    const lockfile = await ModpackLockfileManager.getLockfile(instanceId);

    const files: MrpackIndexFile[] = [];
    const customJarFileNames: string[] = [];

    for (const mod of installedMods) {
      if (!mod.enabled) continue; // Skip disabled mods
      if (!fs.existsSync(mod.filePath)) continue;

      const lockEntry = lockfile?.mods.find(
        (m) => m.fileName.toLowerCase() === mod.fileName.toLowerCase()
      );

      const fileData = await fs.promises.readFile(mod.filePath);
      const sha512 = crypto.createHash('sha512').update(fileData).digest('hex');
      const sha1 = crypto.createHash('sha1').update(fileData).digest('hex');
      const stat = await fs.promises.stat(mod.filePath);

      // Check if mod is from Modrinth (or has a known Modrinth version/project)
      const isModrinth = lockEntry?.source === 'modrinth' && !!lockEntry?.versionId;

      if (isModrinth) {
        // Construct Modrinth CDN download URL
        const primaryUrl = `https://cdn.modrinth.com/data/${lockEntry.projectId || 'unknown'}/versions/${lockEntry.versionId}/${encodeURIComponent(mod.fileName)}`;

        files.push({
          path: `mods/${mod.fileName}`,
          hashes: {
            sha1,
            sha512,
          },
          env: {
            client: 'required',
            server: 'unsupported',
          },
          downloads: [primaryUrl],
          fileSize: stat.size,
        });
      } else {
        // Custom or local JAR file
        if (includeCustomJars) {
          customJarFileNames.push(mod.fileName);
          warningMessages.push(
            `Plik autorski '${mod.fileName}' został spakowany bezpośrednio do folderu overrides/mods/. Upewnij się, że posiadasz prawa do jego redystrybucji.`
          );
        } else {
          warningMessages.push(
            `Pominięto plik lokalny '${mod.fileName}' (brak opcji dołączania plików autorskich JAR).`
          );
        }
      }
    }

    // 3. Build manifest
    const manifest: MrpackIndex = {
      formatVersion: 1,
      game: 'minecraft',
      versionId: versionId.trim() || '1.0.0',
      name: name.trim() || instance.name,
      summary: summary?.trim() || undefined,
      files,
      dependencies,
    };

    // 4. Collect overrides
    const overrideFiles = await MrpackOverrideManager.collectOverridesForExport({
      instanceDir,
      selectedOptions: selectedOverrides,
      includeCustomJars,
      customJarFileNames,
    });

    // 5. Build ZIP archive
    const result = await MrpackArchiveWriter.createArchive({
      destinationPath: outputFilePath,
      manifest,
      overrideFiles,
    });

    return {
      success: true,
      outputPath: outputFilePath,
      filesCount: files.length,
      overridesCount: overrideFiles.length,
      totalSizeBytes: result.totalBytes,
      customJarsCount: customJarFileNames.length,
      warningMessages,
    };
  }
}
