import * as fs from 'node:fs';
import * as path from 'node:path';
import { MrpackPathValidator } from './MrpackPathValidator.js';
import { MrpackArchiveReader, MrpackZipEntry } from './MrpackArchiveReader.js';

export interface OverrideApplyReport {
  baseOverridesCount: number;
  clientOverridesCount: number;
  serverOverridesSavedCount: number;
  appliedFiles: string[];
}

export class MrpackOverrideManager {
  private static readonly FORBIDDEN_EXPORT_DIRS = new Set([
    'saves',
    'screenshots',
    'logs',
    'crash-reports',
    'backups',
    'natives',
    'accounts',
    'runtime',
    'runtimes',
    '.fabric',
    '.quilt',
    '.cache',
    'temp-transactions',
  ]);

  private static readonly FORBIDDEN_EXPORT_FILES = new Set([
    'usercache.json',
    'usernamecache.json',
    'latest.log',
    'launcher_accounts.json',
    'modpack.lock.json',
  ]);

  /**
   * Applies overrides from a .mrpack archive into the target instance directory.
   * Order: overrides/ applied first, client-overrides/ applied second (overwriting overrides/).
   * server-overrides/ are stored in an isolated preservation folder, NOT installed into the game.
   */
  public static async applyOverrides(params: {
    archivePath: string;
    targetInstanceDir: string;
    entries?: MrpackZipEntry[];
  }): Promise<OverrideApplyReport> {
    const { archivePath, targetInstanceDir } = params;

    const entries = params.entries || (await MrpackArchiveReader.listEntries(archivePath));
    const archiveBuffer = await fs.promises.readFile(archivePath);

    const baseOverrides: MrpackZipEntry[] = [];
    const clientOverrides: MrpackZipEntry[] = [];
    const serverOverrides: MrpackZipEntry[] = [];

    for (const e of entries) {
      if (e.isDirectory) continue;
      const norm = e.normalizedPath;

      if (norm.startsWith('overrides/')) {
        baseOverrides.push(e);
      } else if (norm.startsWith('client-overrides/')) {
        clientOverrides.push(e);
      } else if (norm.startsWith('server-overrides/')) {
        serverOverrides.push(e);
      }
    }

    const appliedFiles: string[] = [];

    // 1. Apply base overrides/
    for (const entry of baseOverrides) {
      const relPath = entry.normalizedPath.slice('overrides/'.length);
      const safeDest = MrpackPathValidator.assertSafePath(targetInstanceDir, relPath);

      const parentDir = path.dirname(safeDest);
      if (!fs.existsSync(parentDir)) {
        await fs.promises.mkdir(parentDir, { recursive: true });
      }

      const fileData = await MrpackArchiveReader.readEntryBuffer(archivePath, entry.fileName);
      if (fileData) {
        await fs.promises.writeFile(safeDest, fileData);
        appliedFiles.push(relPath);
      }
    }

    // 2. Apply client-overrides/ (taking precedence over base overrides)
    for (const entry of clientOverrides) {
      const relPath = entry.normalizedPath.slice('client-overrides/'.length);
      const safeDest = MrpackPathValidator.assertSafePath(targetInstanceDir, relPath);

      const parentDir = path.dirname(safeDest);
      if (!fs.existsSync(parentDir)) {
        await fs.promises.mkdir(parentDir, { recursive: true });
      }

      const fileData = await MrpackArchiveReader.readEntryBuffer(archivePath, entry.fileName);
      if (fileData) {
        await fs.promises.writeFile(safeDest, fileData);
        appliedFiles.push(relPath);
      }
    }

    // 3. Preserve server-overrides/ in an isolated storage directory
    const serverPreserveDir = path.join(targetInstanceDir, '.mrpack-preserved', 'server-overrides');
    let serverCount = 0;

    if (serverOverrides.length > 0) {
      for (const entry of serverOverrides) {
        const relPath = entry.normalizedPath.slice('server-overrides/'.length);
        const safeDest = MrpackPathValidator.assertSafePath(serverPreserveDir, relPath);

        const parentDir = path.dirname(safeDest);
        if (!fs.existsSync(parentDir)) {
          await fs.promises.mkdir(parentDir, { recursive: true });
        }

        const fileData = await MrpackArchiveReader.readEntryBuffer(archivePath, entry.fileName);
        if (fileData) {
          await fs.promises.writeFile(safeDest, fileData);
          serverCount++;
        }
      }
    }

    return {
      baseOverridesCount: baseOverrides.length,
      clientOverridesCount: clientOverrides.length,
      serverOverridesSavedCount: serverCount,
      appliedFiles,
    };
  }

  /**
   * Applies in-memory or custom override entries directly to an instance directory.
   */
  public static async applyOverridesToInstance(params: {
    instanceDir: string;
    entries: Array<{ name: string; content?: Buffer }>;
    readEntryBuffer?: (name: string) => Buffer | null;
  }): Promise<{ appliedFiles: string[]; serverOverridesSavedCount: number }> {
    const { instanceDir, entries, readEntryBuffer } = params;
    const appliedFiles: string[] = [];
    let serverOverridesSavedCount = 0;

    const baseOverrides: Array<{ relPath: string; content: Buffer }> = [];
    const clientOverrides: Array<{ relPath: string; content: Buffer }> = [];
    const serverOverrides: Array<{ relPath: string; content: Buffer }> = [];

    for (const entry of entries) {
      const norm = entry.name.replace(/\\/g, '/');
      const content = entry.content || (readEntryBuffer ? readEntryBuffer(entry.name) : null);
      if (!content) continue;

      if (norm.startsWith('overrides/')) {
        baseOverrides.push({ relPath: norm.substring('overrides/'.length), content });
      } else if (norm.startsWith('client-overrides/')) {
        clientOverrides.push({ relPath: norm.substring('client-overrides/'.length), content });
      } else if (norm.startsWith('server-overrides/')) {
        serverOverrides.push({ relPath: norm.substring('server-overrides/'.length), content });
      }
    }

    // Apply base overrides
    for (const item of baseOverrides) {
      const dest = MrpackPathValidator.assertSafePath(instanceDir, item.relPath);
      fs.mkdirSync(path.dirname(dest), { recursive: true });
      fs.writeFileSync(dest, item.content);
      appliedFiles.push(item.relPath);
    }

    // Apply client overrides (overwriting base)
    for (const item of clientOverrides) {
      const dest = MrpackPathValidator.assertSafePath(instanceDir, item.relPath);
      fs.mkdirSync(path.dirname(dest), { recursive: true });
      fs.writeFileSync(dest, item.content);
      if (!appliedFiles.includes(item.relPath)) {
        appliedFiles.push(item.relPath);
      }
    }

    // Preserve server overrides
    const preservedDir = path.join(instanceDir, '.mrpack-preserved', 'server-overrides');
    if (serverOverrides.length > 0) {
      fs.mkdirSync(preservedDir, { recursive: true });
      for (const item of serverOverrides) {
        const dest = MrpackPathValidator.assertSafePath(preservedDir, item.relPath);
        fs.mkdirSync(path.dirname(dest), { recursive: true });
        fs.writeFileSync(dest, item.content);
        serverOverridesSavedCount++;
      }
    }

    return { appliedFiles, serverOverridesSavedCount };
  }

  /**
   * Scans an instance directory and gathers candidate override files for export,
   * strictly excluding private data, logs, saves, account tokens, and system binaries.
   */
  public static async collectOverridesForExport(params: {
    instanceDir: string;
    selectedOptions?: {
      config?: boolean;
      optionsTxt?: boolean;
      resourcepacks?: boolean;
      shaderpacks?: boolean;
      defaultconfigs?: boolean;
      scripts?: boolean;
    };
    includeCustomJars?: boolean;
    customJarFileNames?: string[];
  }): Promise<Array<{ relativePath: string; fullPath: string; prefix: 'overrides' | 'client-overrides' | 'server-overrides' }>> {
    const {
      instanceDir,
      selectedOptions = { config: true, optionsTxt: true, resourcepacks: true, shaderpacks: true },
      includeCustomJars = false,
      customJarFileNames = [],
    } = params;

    const filesToInclude: Array<{
      relativePath: string;
      fullPath: string;
      prefix: 'overrides' | 'client-overrides' | 'server-overrides';
    }> = [];

    if (!fs.existsSync(instanceDir)) {
      return filesToInclude;
    }

    // 1. options.txt (Client-specific override)
    if (selectedOptions.optionsTxt !== false) {
      const optionsTxtPath = path.join(instanceDir, 'options.txt');
      if (fs.existsSync(optionsTxtPath)) {
        filesToInclude.push({
          relativePath: 'options.txt',
          fullPath: optionsTxtPath,
          prefix: 'client-overrides',
        });
      }
    }

    // 2. Scan standard directories (config, resourcepacks, shaderpacks, defaultconfigs, scripts)
    const scanDirIfSelected = async (subDirName: string, selected: boolean) => {
      if (!selected) return;
      const fullSubDir = path.join(instanceDir, subDirName);
      if (!fs.existsSync(fullSubDir)) return;

      const walk = async (currentDir: string, relBase: string) => {
        const items = await fs.promises.readdir(currentDir, { withFileTypes: true });
        for (const item of items) {
          const itemFull = path.join(currentDir, item.name);
          const itemRel = path.join(relBase, item.name).replace(/\\/g, '/');

          if (item.isDirectory()) {
            if (!this.FORBIDDEN_EXPORT_DIRS.has(item.name.toLowerCase())) {
              await walk(itemFull, itemRel);
            }
          } else if (item.isFile()) {
            if (!this.FORBIDDEN_EXPORT_FILES.has(item.name.toLowerCase())) {
              filesToInclude.push({
                relativePath: itemRel,
                fullPath: itemFull,
                prefix: 'overrides',
              });
            }
          }
        }
      };

      await walk(fullSubDir, subDirName);
    };

    await scanDirIfSelected('config', selectedOptions.config !== false);
    await scanDirIfSelected('resourcepacks', selectedOptions.resourcepacks !== false);
    await scanDirIfSelected('shaderpacks', selectedOptions.shaderpacks !== false);
    await scanDirIfSelected('defaultconfigs', selectedOptions.defaultconfigs === true);
    await scanDirIfSelected('scripts', selectedOptions.scripts === true);

    // 3. Custom JAR files if explicitly approved by user
    if (includeCustomJars && customJarFileNames.length > 0) {
      const modsDir = path.join(instanceDir, 'mods');
      for (const customJar of customJarFileNames) {
        const jarPath = path.join(modsDir, customJar);
        if (fs.existsSync(jarPath)) {
          filesToInclude.push({
            relativePath: `mods/${customJar}`,
            fullPath: jarPath,
            prefix: 'overrides',
          });
        }
      }
    }

    return filesToInclude;
  }
}
