import * as path from 'node:path';
import * as os from 'node:os';
import * as fs from 'node:fs';

export class EnginePaths {
  private static rootDir: string = EnginePaths.resolveDefaultRootDir();

  public static resolveDefaultRootDir(): string {
    if (process.platform === 'win32') {
      const appData = process.env.APPDATA || path.join(os.homedir(), 'AppData', 'Roaming');
      return path.join(appData, 'ZombekClient');
    } else if (process.platform === 'darwin') {
      return path.join(os.homedir(), 'Library', 'Application Support', 'ZombekClient');
    } else {
      return path.join(os.homedir(), '.zombekclient');
    }
  }

  public static setRootDir(dir: string): void {
    if (dir && typeof dir === 'string') {
      this.rootDir = path.resolve(dir);
    }
  }

  public static getRootDir(): string {
    return this.rootDir;
  }

  public static getBaseDir(): string {
    return this.rootDir;
  }

  public static getGameDataDir(): string {
    return path.join(this.rootDir, 'game-data');
  }

  public static getAssetsDir(): string {
    return path.join(this.getGameDataDir(), 'assets');
  }

  public static getAssetIndexesDir(): string {
    return path.join(this.getAssetsDir(), 'indexes');
  }

  public static getAssetObjectsDir(): string {
    return path.join(this.getAssetsDir(), 'objects');
  }

  public static getLibrariesDir(): string {
    return path.join(this.getGameDataDir(), 'libraries');
  }

  public static getVersionsDir(): string {
    return path.join(this.getGameDataDir(), 'versions');
  }

  public static getRuntimesDir(): string {
    return path.join(this.getGameDataDir(), 'runtimes');
  }

  public static getInstancesDir(): string {
    return path.join(this.rootDir, 'instances');
  }

  public static getLogsDir(): string {
    return path.join(this.rootDir, 'logs');
  }

  public static getAccountsDir(): string {
    return path.join(this.rootDir, 'accounts');
  }

  public static getInstanceDir(instanceId: string): string {
    this.assertSafeFilename(instanceId);
    return path.join(this.getInstancesDir(), instanceId);
  }

  public static getNativesDir(instanceId: string): string {
    return path.join(this.getInstanceDir(instanceId), 'natives');
  }

  public static getInstanceModsDir(instanceId: string): string {
    return path.join(this.getInstanceDir(instanceId), 'mods');
  }

  public static getInstanceDisabledModsDir(instanceId: string): string {
    return path.join(this.getInstanceDir(instanceId), 'disabled-mods');
  }

  public static getInstanceConfigDir(instanceId: string): string {
    return path.join(this.getInstanceDir(instanceId), 'config');
  }

  public static getModpacksFilePath(): string {
    return path.join(this.getGameDataDir(), 'modpacks.json');
  }

  public static getModpacksDir(): string {
    return path.join(this.getGameDataDir(), 'modpacks');
  }

  public static getModrinthCacheDir(): string {
    return path.join(this.getGameDataDir(), 'modrinth-cache');
  }

  public static getInstanceBackupsDir(instanceId: string): string {
    return path.join(this.getInstanceDir(instanceId), 'backups');
  }

  public static getInstanceLockfilePath(instanceId: string): string {
    return path.join(this.getInstanceDir(instanceId), 'modpack.lock.json');
  }

  public static getTempTransactionsDir(): string {
    return path.join(this.getGameDataDir(), 'temp-transactions');
  }

  public static getTempMrpackDir(): string {
    return path.join(this.getGameDataDir(), 'temp-mrpack');
  }

  public static ensureAllBaseDirs(): void {
    const dirs = [
      this.rootDir,
      this.getGameDataDir(),
      this.getAssetsDir(),
      this.getAssetIndexesDir(),
      this.getAssetObjectsDir(),
      this.getLibrariesDir(),
      this.getVersionsDir(),
      this.getRuntimesDir(),
      this.getInstancesDir(),
      this.getLogsDir(),
      this.getAccountsDir()
    ];
    for (const dir of dirs) {
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }
    }
  }

  public static isSafePath(baseDir: string, targetPath: string): boolean {
    const resolvedBase = path.resolve(baseDir);
    const resolvedTarget = path.resolve(targetPath);
    return resolvedTarget === resolvedBase || resolvedTarget.startsWith(resolvedBase + path.sep);
  }

  public static assertSafeFilename(filename: string): void {
    if (!filename || filename.includes('..') || filename.includes('/') || filename.includes('\\')) {
      throw new Error(`Invalid or dangerous identifier: ${filename}`);
    }
  }
}
