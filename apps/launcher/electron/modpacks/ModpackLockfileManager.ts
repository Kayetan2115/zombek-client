import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import type {
  ModpackLockfile,
  ModpackLockfileMod,
  ModUpdatePolicy,
  ModRecord,
  MinecraftLoaderType,
} from '@zombek/shared-types';

export class ModpackLockfileManager {
  public static readonly CURRENT_SCHEMA_VERSION = 1;

  /**
   * Retrieves the current lockfile for an instance or null if not yet created.
   */
  public static async getLockfile(instanceId: string): Promise<ModpackLockfile | null> {
    const lockfilePath = EnginePaths.getInstanceLockfilePath(instanceId);
    if (!fs.existsSync(lockfilePath)) {
      return null;
    }

    try {
      const raw = await fs.promises.readFile(lockfilePath, 'utf-8');
      const parsed = JSON.parse(raw) as ModpackLockfile;
      return this.migrateLockfile(parsed);
    } catch (err: any) {
      console.warn(`[LockfileManager] Błąd odczytu lockfile dla ${instanceId}:`, err.message);
      return null;
    }
  }

  /**
   * Writes the lockfile safely to disk.
   */
  public static async saveLockfile(instanceId: string, lockfile: ModpackLockfile): Promise<void> {
    const lockfilePath = EnginePaths.getInstanceLockfilePath(instanceId);
    const parentDir = path.dirname(lockfilePath);

    if (!fs.existsSync(parentDir)) {
      await fs.promises.mkdir(parentDir, { recursive: true });
    }

    lockfile.updatedAt = new Date().toISOString();
    const raw = JSON.stringify(lockfile, null, 2);
    await fs.promises.writeFile(lockfilePath, raw, 'utf-8');
  }

  /**
   * Sets pinned status for a specific mod file in the instance lockfile.
   */
  public static async setModPinned(
    instanceId: string,
    fileName: string,
    pinned: boolean
  ): Promise<ModpackLockfile> {
    let lockfile = await this.getLockfile(instanceId);
    if (!lockfile) {
      lockfile = await this.initDefaultLockfile(instanceId);
    }

    const targetMod = lockfile.mods.find(
      (m) => m.fileName.toLowerCase() === fileName.toLowerCase()
    );

    if (targetMod) {
      targetMod.pinned = pinned;
    } else {
      // Add entry to lockfile if missing
      lockfile.mods.push({
        fileName,
        name: fileName.replace(/\.jar$/i, ''),
        version: 'unknown',
        source: 'local',
        pinned,
        updatePolicy: 'stable-only',
        enabled: true,
        dependencies: [],
        installedAt: new Date().toISOString(),
      });
    }

    await this.saveLockfile(instanceId, lockfile);
    return lockfile;
  }

  /**
   * Sets the update policy for a specific mod (stable-only, stable-and-beta, all-versions).
   */
  public static async setModUpdatePolicy(
    instanceId: string,
    fileName: string,
    policy: ModUpdatePolicy
  ): Promise<ModpackLockfile> {
    let lockfile = await this.getLockfile(instanceId);
    if (!lockfile) {
      lockfile = await this.initDefaultLockfile(instanceId);
    }

    const targetMod = lockfile.mods.find(
      (m) => m.fileName.toLowerCase() === fileName.toLowerCase()
    );

    if (targetMod) {
      targetMod.updatePolicy = policy;
    } else {
      lockfile.mods.push({
        fileName,
        name: fileName.replace(/\.jar$/i, ''),
        version: 'unknown',
        source: 'local',
        pinned: false,
        updatePolicy: policy,
        enabled: true,
        dependencies: [],
        installedAt: new Date().toISOString(),
      });
    }

    await this.saveLockfile(instanceId, lockfile);
    return lockfile;
  }

  /**
   * Synchronizes lockfile with actual installed mod files on disk.
   */
  public static async syncFromInstalledMods(
    instanceId: string,
    installedMods: ModRecord[]
  ): Promise<ModpackLockfile> {
    let lockfile = await this.getLockfile(instanceId);
    if (!lockfile) {
      lockfile = await this.initDefaultLockfile(instanceId);
    }

    const existingMap = new Map<string, ModpackLockfileMod>();
    for (const m of lockfile.mods) {
      existingMap.set(m.fileName.toLowerCase(), m);
    }

    const updatedMods: ModpackLockfileMod[] = [];

    for (const mod of installedMods) {
      const existing = existingMap.get(mod.fileName.toLowerCase());
      if (existing) {
        // Preserve pinned status, policies, and known project/version IDs
        updatedMods.push({
          ...existing,
          enabled: mod.enabled,
          sha1: mod.sha256 || existing.sha1,
          name: mod.metadata.name || existing.name,
          version: mod.metadata.version || existing.version,
        });
      } else {
        // New mod added manually or newly installed
        updatedMods.push({
          fileName: mod.fileName,
          name: mod.metadata.name || mod.fileName,
          version: mod.metadata.version || 'unknown',
          sha1: mod.sha256,
          source: 'local',
          pinned: false,
          updatePolicy: 'stable-only',
          enabled: mod.enabled,
          dependencies: mod.metadata.dependencies?.map((d) => d.id) || [],
          installedAt: mod.dateAdded || new Date().toISOString(),
        });
      }
    }

    lockfile.mods = updatedMods;
    await this.saveLockfile(instanceId, lockfile);
    return lockfile;
  }

  /**
   * Initializes a default lockfile for an instance from installation metadata.
   */
  public static async initDefaultLockfile(instanceId: string): Promise<ModpackLockfile> {
    const instance = await InstallationRepository.getById(instanceId);
    const now = new Date().toISOString();

    return {
      schemaVersion: this.CURRENT_SCHEMA_VERSION,
      instanceId,
      gameVersion: instance?.minecraftVersion || '1.20.4',
      loader: {
        id: (instance?.loader || 'fabric') as MinecraftLoaderType,
        version: instance?.loaderVersion,
      },
      createdAt: now,
      updatedAt: now,
      mods: [],
    };
  }

  /**
   * Migrates older lockfile schemas if needed.
   */
  private static migrateLockfile(raw: any): ModpackLockfile {
    if (!raw.schemaVersion || raw.schemaVersion < 1) {
      raw.schemaVersion = 1;
    }
    if (!Array.isArray(raw.mods)) {
      raw.mods = [];
    }
    return raw as ModpackLockfile;
  }
}
