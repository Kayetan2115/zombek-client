import * as fs from 'node:fs';
import * as path from 'node:path';
import * as crypto from 'node:crypto';
import { EnginePaths } from '../engine/paths.js';

export interface ModDirectoryStructure {
  modsDir: string;
  disabledModsDir: string;
  configDir: string;
}

export class ModFileManager {
  /**
   * Asserts that a file or instance identifier has no path traversal components.
   */
  public static assertSafeName(name: string): void {
    if (!name || typeof name !== 'string') {
      throw new Error('Nazwa pliku nie może być pusta.');
    }
    const normalized = path.normalize(name.replace(/\\/g, '/'));
    if (
      normalized.includes('..') ||
      path.isAbsolute(normalized) ||
      name.includes('/') ||
      name.includes('\\') ||
      name.includes(':')
    ) {
      throw new Error(`Wykryto niebezpieczną lub nieprawidłową nazwę pliku: ${name}`);
    }
  }

  /**
   * Ensures that instance directories for mods, disabled-mods, and config exist.
   */
  public static async ensureDirectories(instanceId: string): Promise<ModDirectoryStructure> {
    EnginePaths.assertSafeFilename(instanceId);

    const modsDir = EnginePaths.getInstanceModsDir(instanceId);
    const disabledModsDir = EnginePaths.getInstanceDisabledModsDir(instanceId);
    const configDir = EnginePaths.getInstanceConfigDir(instanceId);

    for (const dir of [modsDir, disabledModsDir, configDir]) {
      if (!fs.existsSync(dir)) {
        await fs.promises.mkdir(dir, { recursive: true });
      }
    }

    return { modsDir, disabledModsDir, configDir };
  }

  /**
   * Calculates SHA-256 hash of a file.
   */
  public static async calculateSha256(filePath: string): Promise<string> {
    if (!fs.existsSync(filePath)) {
      throw new Error(`Plik nie istnieje: ${filePath}`);
    }

    return new Promise<string>((resolve, reject) => {
      const hash = crypto.createHash('sha256');
      const stream = fs.createReadStream(filePath);
      stream.on('data', (d) => hash.update(d));
      stream.on('end', () => resolve(hash.digest('hex')));
      stream.on('error', (err) => reject(err));
    });
  }

  /**
   * Lists all `.jar` files in `mods/` (enabled) and `disabled-mods/` (disabled).
   */
  public static async listModFiles(
    instanceId: string
  ): Promise<{ enabled: string[]; disabled: string[] }> {
    const { modsDir, disabledModsDir } = await this.ensureDirectories(instanceId);

    const getJars = async (dir: string): Promise<string[]> => {
      try {
        const files = await fs.promises.readdir(dir);
        return files.filter((f) => f.toLowerCase().endsWith('.jar'));
      } catch {
        return [];
      }
    };

    const enabled = await getJars(modsDir);
    const disabled = await getJars(disabledModsDir);

    return { enabled, disabled };
  }

  /**
   * Copies a mod JAR into the instance's active `mods/` directory with pre/post hash validation.
   */
  public static async copyModFile(
    sourcePath: string,
    instanceId: string,
    overwrite: boolean = false
  ): Promise<{ fileName: string; destinationPath: string; sha256: string }> {
    if (!fs.existsSync(sourcePath)) {
      throw new Error(`Plik źródłowy nie istnieje: ${sourcePath}`);
    }

    const fileName = path.basename(sourcePath);
    this.assertSafeName(fileName);

    if (!fileName.toLowerCase().endsWith('.jar')) {
      throw new Error(`Nieprawidłowe rozszerzenie pliku: ${fileName}. Wymagany plik .jar.`);
    }

    const { modsDir, disabledModsDir } = await this.ensureDirectories(instanceId);
    const destinationPath = path.join(modsDir, fileName);
    const disabledPath = path.join(disabledModsDir, fileName);

    // Calculate source hash
    const preCopyHash = await this.calculateSha256(sourcePath);

    // Check if duplicate exists by path
    if (fs.existsSync(destinationPath) && !overwrite) {
      throw new Error(`Mod o nazwie "${fileName}" jest już zainstalowany w tym profilu.`);
    }

    // If it was previously disabled, remove it from disabled-mods when overwriting
    if (fs.existsSync(disabledPath)) {
      await fs.promises.rm(disabledPath, { force: true });
    }

    // Perform copy
    await fs.promises.copyFile(sourcePath, destinationPath);

    // Verify post-copy hash
    const postCopyHash = await this.calculateSha256(destinationPath);
    if (preCopyHash !== postCopyHash) {
      // Clean up corrupt copy
      try {
        await fs.promises.rm(destinationPath, { force: true });
      } catch {
        // ignore
      }
      throw new Error('Błąd integralności pliku: suma kontrolna SHA-256 po skopiowaniu nie zgadza się ze źródłem.');
    }

    return {
      fileName,
      destinationPath,
      sha256: postCopyHash,
    };
  }

  /**
   * Moves a mod between `mods/` and `disabled-mods/`.
   */
  public static async toggleMod(
    instanceId: string,
    fileName: string,
    targetState: boolean
  ): Promise<{ success: boolean; newPath: string }> {
    this.assertSafeName(fileName);
    const { modsDir, disabledModsDir } = await this.ensureDirectories(instanceId);

    const activePath = path.join(modsDir, fileName);
    const disabledPath = path.join(disabledModsDir, fileName);

    if (targetState === true) {
      // Enable: move from disabled-mods to mods
      if (!fs.existsSync(disabledPath)) {
        if (fs.existsSync(activePath)) {
          return { success: true, newPath: activePath }; // already enabled
        }
        throw new Error(`Plik moda "${fileName}" nie został znaleziony w folderze disabled-mods.`);
      }
      await fs.promises.rename(disabledPath, activePath);
      return { success: true, newPath: activePath };
    } else {
      // Disable: move from mods to disabled-mods
      if (!fs.existsSync(activePath)) {
        if (fs.existsSync(disabledPath)) {
          return { success: true, newPath: disabledPath }; // already disabled
        }
        throw new Error(`Plik moda "${fileName}" nie został znaleziony w aktywnym folderze mods.`);
      }
      await fs.promises.rename(activePath, disabledPath);
      return { success: true, newPath: disabledPath };
    }
  }

  /**
   * Deletes a mod file from `mods/` or `disabled-mods/`.
   */
  public static async deleteModFile(instanceId: string, fileName: string): Promise<boolean> {
    this.assertSafeName(fileName);
    const { modsDir, disabledModsDir } = await this.ensureDirectories(instanceId);

    const activePath = path.join(modsDir, fileName);
    const disabledPath = path.join(disabledModsDir, fileName);

    let deleted = false;
    if (fs.existsSync(activePath)) {
      await fs.promises.rm(activePath, { force: true });
      deleted = true;
    }
    if (fs.existsSync(disabledPath)) {
      await fs.promises.rm(disabledPath, { force: true });
      deleted = true;
    }

    return deleted;
  }
}
