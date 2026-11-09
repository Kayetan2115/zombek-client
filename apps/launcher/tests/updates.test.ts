import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import * as fs from 'node:fs';
import * as path from 'node:path';
import * as os from 'node:os';
import * as crypto from 'node:crypto';
import { ModConflictResolver } from '../electron/modpacks/ModConflictResolver';
import { ModDependencyResolver } from '../electron/modpacks/ModDependencyResolver';
import { ModpackLockfileManager } from '../electron/modpacks/ModpackLockfileManager';
import { ModpackBackupManager } from '../electron/modpacks/ModpackBackupManager';
import { ModUpdateManager } from '../electron/modpacks/ModUpdateManager';
import { ModpackTransactionManager } from '../electron/modpacks/ModpackTransactionManager';
import { ModrinthApiClient } from '../electron/modrinth/ModrinthApiClient';
import { ModrinthDownloader } from '../electron/modrinth/ModrinthDownloader';
import { GameLauncher } from '../electron/engine/GameLauncher';
import { EnginePaths } from '../electron/engine/paths';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import { ModRepository } from '../electron/modpacks/ModRepository';
import type {
  ModrinthVersion,
  ModrinthDependency,
  ModpackLockfile,
  ModRecord,
} from '@zombek/shared-types';

describe('Etap 6C - Modpack Dependency Resolution & Auto-Updates', () => {
  let testDir: string;
  let instanceId: string;
  let instanceModsDir: string;

  beforeEach(() => {
    testDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-etap6c-test-'));
    instanceId = 'test-inst-6c';
    instanceModsDir = path.join(testDir, 'instances', instanceId, 'mods');
    fs.mkdirSync(instanceModsDir, { recursive: true });

    // Mock EnginePaths
    vi.spyOn(EnginePaths, 'getInstanceDir').mockImplementation((id: string) => {
      return path.join(testDir, 'instances', id);
    });
    vi.spyOn(EnginePaths, 'getInstanceModsDir').mockImplementation((id: string) => {
      return path.join(testDir, 'instances', id, 'mods');
    });
    vi.spyOn(EnginePaths, 'getInstanceBackupsDir').mockImplementation((id: string) => {
      return path.join(testDir, 'backups', id);
    });
    vi.spyOn(EnginePaths, 'getInstanceLockfilePath').mockImplementation((id: string) => {
      return path.join(testDir, 'instances', id, 'modpack.lock.json');
    });
    vi.spyOn(EnginePaths, 'getTempTransactionsDir').mockImplementation(() => {
      return path.join(testDir, 'temp-transactions');
    });

    // Mock InstallationRepository
    vi.spyOn(InstallationRepository, 'getById').mockImplementation((id: string) => {
      if (id === instanceId) {
        return {
          id: instanceId,
          name: 'Test Fabric 1.20.1',
          minecraftVersion: '1.20.1',
          gameVersion: '1.20.1',
          loader: 'fabric',
          loaderVersion: '0.15.11',
          created: new Date().toISOString(),
          lastPlayed: null,
          resolution: { width: 854, height: 480 },
          javaPath: null,
          jvmArguments: [],
        } as any;
      }
      return null;
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
    try {
      fs.rmSync(testDir, { recursive: true, force: true });
    } catch {}
  });

  // ==========================================================================
  // 1. ModConflictResolver Tests
  // ==========================================================================
  describe('ModConflictResolver', () => {
    it('should detect loader mismatches between mod and installation', () => {
      const candidate: ModrinthVersion = {
        id: 'ver-forge-mod',
        project_id: 'forge-mod',
        author_id: 'auth',
        name: 'Forge Only Mod',
        version_number: '1.0.0',
        dependencies: [],
        game_versions: ['1.20.1'],
        loaders: ['forge', 'neoforge'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 10,
        files: [{ hashes: { sha512: '', sha1: '' }, url: '', filename: 'forge-mod.jar', primary: true, size: 10 }],
      };

      const conflicts = ModConflictResolver.detectConflicts({
        candidateVersions: [candidate],
        installedMods: [],
        targetInstance: {
          loader: 'fabric',
          minecraftVersion: '1.20.1',
        },
      });

      expect(conflicts.length).toBe(1);
      expect(conflicts[0].conflictType).toBe('loader-mismatch');
      expect(conflicts[0].description).toContain('FABRIC');
    });

    it('should detect Minecraft version mismatches', () => {
      const candidate: ModrinthVersion = {
        id: 'ver-old-mc',
        project_id: 'old-mod',
        author_id: 'auth',
        name: 'Old MC Mod',
        version_number: '1.0.0',
        dependencies: [],
        game_versions: ['1.19.2'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 10,
        files: [{ hashes: { sha512: '', sha1: '' }, url: '', filename: 'old.jar', primary: true, size: 10 }],
      };

      const conflicts = ModConflictResolver.detectConflicts({
        candidateVersions: [candidate],
        installedMods: [],
        targetInstance: {
          loader: 'fabric',
          minecraftVersion: '1.20.1',
        },
      });

      expect(conflicts.length).toBe(1);
      expect(conflicts[0].conflictType).toBe('mc-version-mismatch');
      expect(conflicts[0].description).toContain('1.20.1');
    });

    it('should detect declared incompatible dependencies', () => {
      const candidate: ModrinthVersion = {
        id: 'ver-iris',
        project_id: 'iris-shaders',
        author_id: 'auth',
        name: 'Iris Shaders',
        version_number: '1.7.0',
        dependencies: [
          {
            project_id: 'optifine',
            version_id: null,
            file_name: null,
            dependency_type: 'incompatible',
          },
        ],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 100,
        files: [{ hashes: { sha512: '', sha1: '' }, url: '', filename: 'iris.jar', primary: true, size: 10 }],
      };

      const installedOptifine: ModRecord = {
        fileName: 'optifine.jar',
        filePath: path.join(instanceModsDir, 'optifine.jar'),
        fileSize: 100,
        sha256: 'hash',
        enabled: true,
        metadata: {
          id: 'optifine',
          name: 'OptiFine',
          version: '1.20.1_HD_U_I5',
          description: '',
          authors: [],
          loader: 'fabric',
          dependencies: [],
          rawSource: 'unknown',
        },
        compatibilityStatus: 'Compatible',
        dateAdded: new Date().toISOString(),
      };

      const conflicts = ModConflictResolver.detectConflicts({
        candidateVersions: [candidate],
        installedMods: [installedOptifine],
        targetInstance: {
          loader: 'fabric',
          minecraftVersion: '1.20.1',
        },
      });

      expect(conflicts.length).toBe(1);
      expect(conflicts[0].conflictType).toBe('incompatible');
      expect(conflicts[0].description).toContain('niekompatybilność');
    });

    it('should detect collision when update candidate conflicts with a pinned mod', () => {
      const candidate: ModrinthVersion = {
        id: 'ver-api-new',
        project_id: 'fabric-api',
        author_id: 'auth',
        name: 'Fabric API',
        version_number: '0.92.0',
        dependencies: [],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 100,
        files: [
          {
            hashes: { sha512: '', sha1: '' },
            url: '',
            filename: 'fabric-api-0.90.0.jar',
            primary: true,
            size: 10,
          },
        ],
      };

      const installedMod: ModRecord = {
        fileName: 'fabric-api-0.90.0.jar',
        filePath: path.join(instanceModsDir, 'fabric-api-0.90.0.jar'),
        fileSize: 100,
        sha256: 'hash',
        enabled: true,
        metadata: {
          id: 'fabric-api',
          name: 'Fabric API',
          version: '0.90.0',
          description: '',
          authors: [],
          loader: 'fabric',
          dependencies: [],
          rawSource: 'fabric.mod.json',
        },
        compatibilityStatus: 'Compatible',
        dateAdded: new Date().toISOString(),
      };

      const conflicts = ModConflictResolver.detectConflicts({
        candidateVersions: [candidate],
        installedMods: [installedMod],
        targetInstance: {
          loader: 'fabric',
          minecraftVersion: '1.20.1',
        },
        pinnedFileNames: new Set(['fabric-api-0.90.0.jar']),
      });

      expect(conflicts.length).toBe(1);
      expect(conflicts[0].conflictType).toBe('pinned-version');
      expect(conflicts[0].description).toContain('przypięty');
    });
  });

  // ==========================================================================
  // 2. ModDependencyResolver Tests
  // ==========================================================================
  describe('ModDependencyResolver', () => {
    it('should resolve direct required dependencies and add to toInstall plan', async () => {
      const targetVersion: ModrinthVersion = {
        id: 'ver-iris-1',
        project_id: 'iris-project',
        author_id: 'author-1',
        name: 'Iris 1.7.0',
        version_number: '1.7.0',
        dependencies: [
          {
            project_id: 'sodium-project',
            version_id: 'ver-sodium-1',
            file_name: null,
            dependency_type: 'required',
          },
        ],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 1000,
        files: [
          {
            hashes: { sha512: 'sha512-iris', sha1: 'sha1-iris' },
            url: 'https://cdn.modrinth.com/iris.jar',
            filename: 'iris-1.7.0.jar',
            primary: true,
            size: 2048,
          },
        ],
      };

      const sodiumVersion: ModrinthVersion = {
        id: 'ver-sodium-1',
        project_id: 'sodium-project',
        author_id: 'author-2',
        name: 'Sodium 0.5.8',
        version_number: '0.5.8',
        dependencies: [],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 5000,
        files: [
          {
            hashes: { sha512: 'sha512-sodium', sha1: 'sha1-sodium' },
            url: 'https://cdn.modrinth.com/sodium.jar',
            filename: 'sodium-0.5.8.jar',
            primary: true,
            size: 4096,
          },
        ],
      };

      vi.spyOn(ModrinthApiClient, 'getVersion').mockImplementation(async (id: string) => {
        if (id === 'ver-iris-1') return targetVersion;
        if (id === 'ver-sodium-1') return sodiumVersion;
        throw new Error(`Version not found: ${id}`);
      });

      const plan = await ModDependencyResolver.resolvePlan({
        targetVersion,
        installedMods: [],
        targetInstance: {
          loader: 'fabric',
          minecraftVersion: '1.20.1',
          instanceId,
        },
      });

      expect(plan.canProceed).toBe(true);
      expect(plan.conflicts.length).toBe(0);
      expect(plan.toInstall.length).toBe(1);
      expect(plan.toInstall[0].projectId).toBe('sodium-project');
      expect(plan.toInstall[0].version.id).toBe('ver-sodium-1');
    });

    it('should recursively resolve multi-level dependencies (A -> B -> C)', async () => {
      const verA: ModrinthVersion = {
        id: 'ver-A',
        project_id: 'proj-A',
        author_id: 'auth',
        name: 'Mod A',
        version_number: '1.0.0',
        dependencies: [
          { project_id: 'proj-B', version_id: 'ver-B', file_name: null, dependency_type: 'required' },
        ],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 100,
        files: [{ hashes: { sha512: 'hA', sha1: 'hA' }, url: 'http://a.jar', filename: 'a.jar', primary: true, size: 100 }],
      };

      const verB: ModrinthVersion = {
        id: 'ver-B',
        project_id: 'proj-B',
        author_id: 'auth',
        name: 'Mod B',
        version_number: '1.0.0',
        dependencies: [
          { project_id: 'proj-C', version_id: 'ver-C', file_name: null, dependency_type: 'required' },
        ],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 100,
        files: [{ hashes: { sha512: 'hB', sha1: 'hB' }, url: 'http://b.jar', filename: 'b.jar', primary: true, size: 100 }],
      };

      const verC: ModrinthVersion = {
        id: 'ver-C',
        project_id: 'proj-C',
        author_id: 'auth',
        name: 'Mod C',
        version_number: '1.0.0',
        dependencies: [],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 100,
        files: [{ hashes: { sha512: 'hC', sha1: 'hC' }, url: 'http://c.jar', filename: 'c.jar', primary: true, size: 100 }],
      };

      vi.spyOn(ModrinthApiClient, 'getVersion').mockImplementation(async (id: string) => {
        if (id === 'ver-A') return verA;
        if (id === 'ver-B') return verB;
        if (id === 'ver-C') return verC;
        throw new Error('Not found');
      });

      const plan = await ModDependencyResolver.resolvePlan({
        targetVersion: verA,
        installedMods: [],
        targetInstance: {
          loader: 'fabric',
          minecraftVersion: '1.20.1',
          instanceId,
        },
      });

      expect(plan.canProceed).toBe(true);
      expect(plan.toInstall.length).toBe(2);
      const projectIds = plan.toInstall.map((item) => item.projectId);
      expect(projectIds).toContain('proj-B');
      expect(projectIds).toContain('proj-C');
    });

    it('should detect circular dependencies without infinite loops', async () => {
      const ver1: ModrinthVersion = {
        id: 'ver-cycle-1',
        project_id: 'proj-cycle-1',
        author_id: 'auth',
        name: 'Cycle 1',
        version_number: '1.0.0',
        dependencies: [
          { project_id: 'proj-cycle-2', version_id: 'ver-cycle-2', file_name: null, dependency_type: 'required' },
        ],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 10,
        files: [{ hashes: { sha512: 'h1', sha1: 'h1' }, url: 'http://1.jar', filename: '1.jar', primary: true, size: 100 }],
      };

      const ver2: ModrinthVersion = {
        id: 'ver-cycle-2',
        project_id: 'proj-cycle-2',
        author_id: 'auth',
        name: 'Cycle 2',
        version_number: '1.0.0',
        dependencies: [
          { project_id: 'proj-cycle-1', version_id: 'ver-cycle-1', file_name: null, dependency_type: 'required' },
        ],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 10,
        files: [{ hashes: { sha512: 'h2', sha1: 'h2' }, url: 'http://2.jar', filename: '2.jar', primary: true, size: 100 }],
      };

      vi.spyOn(ModrinthApiClient, 'getVersion').mockImplementation(async (id: string) => {
        if (id === 'ver-cycle-1') return ver1;
        if (id === 'ver-cycle-2') return ver2;
        throw new Error('Not found');
      });

      // Should finish without stack overflow
      const plan = await ModDependencyResolver.resolvePlan({
        targetVersion: ver1,
        installedMods: [],
        targetInstance: {
          loader: 'fabric',
          minecraftVersion: '1.20.1',
          instanceId,
        },
      });

      expect(plan.canProceed).toBe(true);
      expect(plan.toInstall.length).toBe(1);
      expect(plan.toInstall[0].projectId).toBe('proj-cycle-2');
    });

    it('should recognize embedded (jar-in-jar) dependencies without downloading separate files', async () => {
      const verRoot: ModrinthVersion = {
        id: 'ver-root',
        project_id: 'proj-root',
        author_id: 'auth',
        name: 'Mod with JiJ',
        version_number: '1.0.0',
        dependencies: [
          {
            project_id: 'proj-embedded-lib',
            version_id: null,
            file_name: null,
            dependency_type: 'embedded',
          },
        ],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 10,
        files: [{ hashes: { sha512: 'h', sha1: 'h' }, url: 'http://r.jar', filename: 'r.jar', primary: true, size: 100 }],
      };

      const plan = await ModDependencyResolver.resolvePlan({
        targetVersion: verRoot,
        installedMods: [],
        targetInstance: {
          loader: 'fabric',
          minecraftVersion: '1.20.1',
          instanceId,
        },
      });

      expect(plan.canProceed).toBe(true);
      expect(plan.toInstall.length).toBe(0);
      expect(plan.alreadySatisfied.length).toBe(1);
      expect(plan.alreadySatisfied[0].projectId).toBe('proj-embedded-lib');
      expect(plan.alreadySatisfied[0].isEmbedded).toBe(true);
    });
  });

  // ==========================================================================
  // 3. ModpackLockfileManager Tests
  // ==========================================================================
  describe('ModpackLockfileManager', () => {
    it('should create and read schema v1 modpack.lock.json', async () => {
      const lockfile = await ModpackLockfileManager.initDefaultLockfile(instanceId);
      await ModpackLockfileManager.saveLockfile(instanceId, lockfile);
      expect(lockfile.schemaVersion).toBe(1);
      expect(lockfile.instanceId).toBe(instanceId);
      expect(lockfile.gameVersion).toBe('1.20.1');
      expect(lockfile.loader.id).toBe('fabric');

      // Check file was actually written to disk
      const filePath = EnginePaths.getInstanceLockfilePath(instanceId);
      expect(fs.existsSync(filePath)).toBe(true);
    });

    it('should add, update, and pin mod entries in lockfile', async () => {
      await ModpackLockfileManager.initDefaultLockfile(instanceId);

      // Pin the mod
      const updated1 = await ModpackLockfileManager.setModPinned(
        instanceId,
        'test-mod-1.0.0.jar',
        true
      );
      expect(updated1.mods.length).toBe(1);
      expect(updated1.mods[0].pinned).toBe(true);

      // Change update policy
      const updated2 = await ModpackLockfileManager.setModUpdatePolicy(
        instanceId,
        'test-mod-1.0.0.jar',
        'stable-and-beta'
      );
      expect(updated2.mods[0].updatePolicy).toBe('stable-and-beta');

      const readBack = await ModpackLockfileManager.getLockfile(instanceId);
      expect(readBack?.mods[0].pinned).toBe(true);
      expect(readBack?.mods[0].updatePolicy).toBe('stable-and-beta');
    });

    it('should sync lockfile from installed disk files', async () => {
      // Put a test file in instance mods dir
      const testJar = path.join(instanceModsDir, 'local-mod.jar');
      fs.writeFileSync(testJar, 'dummy-jar-content');

      const mockRecord: ModRecord = {
        fileName: 'local-mod.jar',
        filePath: testJar,
        fileSize: 17,
        sha256: 'sha256-hash',
        enabled: true,
        metadata: {
          id: 'local-mod',
          name: 'Local Mod',
          version: '2.0.0',
          description: '',
          authors: [],
          loader: 'fabric',
          dependencies: [],
          rawSource: 'fabric.mod.json',
        },
        compatibilityStatus: 'Compatible',
        dateAdded: new Date().toISOString(),
      };

      const lockfile = await ModpackLockfileManager.syncFromInstalledMods(instanceId, [mockRecord]);
      expect(lockfile.mods.length).toBe(1);
      expect(lockfile.mods[0].fileName).toBe('local-mod.jar');
      expect(lockfile.mods[0].source).toBe('local');
    });
  });

  // ==========================================================================
  // 4. ModpackBackupManager Tests
  // ==========================================================================
  describe('ModpackBackupManager', () => {
    it('should create backup snapshot with manifest and lockfile', async () => {
      // Put two mock jar files in mods dir
      fs.writeFileSync(path.join(instanceModsDir, 'mod-a.jar'), 'mod-a-data');
      fs.writeFileSync(path.join(instanceModsDir, 'mod-b.jar'), 'mod-b-data');

      // Create initial lockfile
      const lockfile = await ModpackLockfileManager.initDefaultLockfile(instanceId);

      const backupInfo = await ModpackBackupManager.createBackup({
        instanceId,
        description: 'Przed aktualizacją modpacka',
        fileNamesToBackup: ['mod-a.jar', 'mod-b.jar'],
        lockfileSnapshot: lockfile,
      });

      expect(backupInfo).not.toBeNull();
      expect(backupInfo.filesCount).toBe(2);
      expect(backupInfo.description).toBe('Przed aktualizacją modpacka');
      expect(backupInfo.modNames).toContain('mod-a.jar');
      expect(backupInfo.modNames).toContain('mod-b.jar');

      // Verify backup folder contents
      const backups = await ModpackBackupManager.listBackups(instanceId);
      expect(backups.length).toBe(1);
      expect(backups[0].id).toBe(backupInfo.id);
    });

    it('should enforce 10-backup retention policy', async () => {
      fs.writeFileSync(path.join(instanceModsDir, 'test.jar'), 'test');

      for (let i = 1; i <= 12; i++) {
        await ModpackBackupManager.createBackup({
          instanceId,
          description: `Backup #${i}`,
          fileNamesToBackup: ['test.jar'],
        });
      }

      const backups = await ModpackBackupManager.listBackups(instanceId);
      expect(backups.length).toBe(10);
      // Newest should be #12
      expect(backups[0].description).toBe('Backup #12');
    });

    it('should perform 1-click rollback restoring files and lockfile', async () => {
      // State 1: mod-original.jar
      fs.writeFileSync(path.join(instanceModsDir, 'mod-original.jar'), 'original-v1');
      const lockfile1 = await ModpackLockfileManager.initDefaultLockfile(instanceId);
      lockfile1.mods.push({
        fileName: 'mod-original.jar',
        name: 'Original Mod',
        version: '1.0.0',
        source: 'local',
        pinned: false,
        enabled: true,
        dependencies: [],
        installedAt: new Date().toISOString(),
      });
      await ModpackLockfileManager.saveLockfile(instanceId, lockfile1);

      const backup1 = await ModpackBackupManager.createBackup({
        instanceId,
        description: 'State 1',
        fileNamesToBackup: ['mod-original.jar'],
        lockfileSnapshot: lockfile1,
      });

      // State 2: Replace with mod-broken.jar
      fs.unlinkSync(path.join(instanceModsDir, 'mod-original.jar'));
      fs.writeFileSync(path.join(instanceModsDir, 'mod-broken.jar'), 'broken-v2');
      const lockfile2 = await ModpackLockfileManager.getLockfile(instanceId);
      if (lockfile2) {
        lockfile2.mods = [
          {
            fileName: 'mod-broken.jar',
            name: 'Broken Mod',
            version: '2.0.0',
            source: 'local',
            pinned: false,
            enabled: true,
            dependencies: [],
            installedAt: new Date().toISOString(),
          },
        ];
        await ModpackLockfileManager.saveLockfile(instanceId, lockfile2);
      }

      // Rollback to State 1
      const rollbackResult = await ModpackBackupManager.rollback(instanceId, backup1.id);
      expect(rollbackResult.success).toBe(true);

      // Verify files in mods dir: mod-original should exist, mod-broken should be gone
      expect(fs.existsSync(path.join(instanceModsDir, 'mod-original.jar'))).toBe(true);
      expect(fs.existsSync(path.join(instanceModsDir, 'mod-broken.jar'))).toBe(false);

      // Verify lockfile restored
      const restoredLockfile = await ModpackLockfileManager.getLockfile(instanceId);
      expect(restoredLockfile?.mods.some((m) => m.fileName === 'mod-original.jar')).toBe(true);
      expect(restoredLockfile?.mods.some((m) => m.fileName === 'mod-broken.jar')).toBe(false);
    });
  });

  // ==========================================================================
  // 5. ModUpdateManager Tests
  // ==========================================================================
  describe('ModUpdateManager', () => {
    it('should mark pinned mods as pinned and skip updating them', async () => {
      const jarPath = path.join(instanceModsDir, 'fabric-api.jar');
      fs.writeFileSync(jarPath, 'some-jar-data');

      vi.spyOn(ModRepository, 'getModsForInstance').mockResolvedValue([
        {
          fileName: 'fabric-api.jar',
          filePath: jarPath,
          fileSize: 13,
          sha256: 'hash',
          enabled: true,
          metadata: {
            id: 'fabric-api',
            name: 'Fabric API',
            version: '0.90.0',
            description: '',
            authors: [],
            loader: 'fabric',
            dependencies: [],
            rawSource: 'fabric.mod.json',
          },
          compatibilityStatus: 'Compatible',
          dateAdded: new Date().toISOString(),
        },
      ]);

      // Create lockfile with pinned status
      await ModpackLockfileManager.initDefaultLockfile(instanceId);
      await ModpackLockfileManager.setModPinned(instanceId, 'fabric-api.jar', true);

      const result = await ModUpdateManager.checkUpdates(instanceId);
      expect(result.pinnedCount).toBe(1);
      const pinnedItem = result.updates.find((u) => u.fileName === 'fabric-api.jar');
      expect(pinnedItem).toBeDefined();
      expect(pinnedItem?.isPinned).toBe(true);
      expect(pinnedItem?.status).toBe('pinned');
    });

    it('should classify unrecognized local files as manual-action', async () => {
      const customJar = path.join(instanceModsDir, 'my-private-mod.jar');
      fs.writeFileSync(customJar, 'custom-bytes');

      vi.spyOn(ModRepository, 'getModsForInstance').mockResolvedValue([
        {
          fileName: 'my-private-mod.jar',
          filePath: customJar,
          fileSize: 12,
          sha256: 'hash-custom',
          enabled: true,
          metadata: {
            id: 'my-private-mod',
            name: 'My Private Mod',
            version: '1.0.0',
            description: '',
            authors: [],
            loader: 'fabric',
            dependencies: [],
            rawSource: 'unknown',
          },
          compatibilityStatus: 'Compatible',
          dateAdded: new Date().toISOString(),
        },
      ]);

      // Modrinth API returns empty hash update map
      vi.spyOn(ModrinthApiClient, 'getLatestUpdatesForFiles').mockResolvedValue({});
      vi.spyOn(ModrinthApiClient, 'getVersionFiles').mockResolvedValue({});

      const result = await ModUpdateManager.checkUpdates(instanceId);
      expect(result.manualCount).toBe(1);
      const manualItem = result.updates.find((u) => u.fileName === 'my-private-mod.jar');
      expect(manualItem?.status).toBe('manual-action');
    });

    it('should detect available updates when newer version is returned from Modrinth', async () => {
      const jarContent = 'sodium-jar-file-content';
      const hash = crypto.createHash('sha512').update(jarContent).digest('hex');
      const jarPath = path.join(instanceModsDir, 'sodium-0.5.0.jar');
      fs.writeFileSync(jarPath, jarContent);

      vi.spyOn(ModRepository, 'getModsForInstance').mockResolvedValue([
        {
          fileName: 'sodium-0.5.0.jar',
          filePath: jarPath,
          fileSize: jarContent.length,
          sha256: 'hash-sodium',
          enabled: true,
          metadata: {
            id: 'sodium',
            name: 'Sodium',
            version: '0.5.0',
            description: '',
            authors: [],
            loader: 'fabric',
            dependencies: [],
            rawSource: 'fabric.mod.json',
          },
          compatibilityStatus: 'Compatible',
          dateAdded: new Date().toISOString(),
        },
      ]);

      const newerVersion: ModrinthVersion = {
        id: 'ver-sodium-new',
        project_id: 'sodium-proj',
        author_id: 'auth',
        name: 'Sodium 0.5.8',
        version_number: '0.5.8',
        dependencies: [],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 1000,
        files: [
          {
            hashes: { sha512: 'new-sha512', sha1: 'new-sha1' },
            url: 'https://cdn.modrinth.com/sodium-0.5.8.jar',
            filename: 'sodium-0.5.8.jar',
            primary: true,
            size: 5000,
          },
        ],
      };

      vi.spyOn(ModrinthApiClient, 'getLatestUpdatesForFiles').mockResolvedValue({
        [hash]: newerVersion,
      });

      const result = await ModUpdateManager.checkUpdates(instanceId);
      expect(result.updates.length).toBe(1);
      expect(result.updates[0].status).toBe('available');
      expect(result.updates[0].targetVersion.version_number).toBe('0.5.8');
    });
  });

  // ==========================================================================
  // 6. ModpackTransactionManager Tests
  // ==========================================================================
  describe('ModpackTransactionManager', () => {
    it('should block transaction if Minecraft instance is actively running', async () => {
      // Mock GameLauncher status to running
      vi.spyOn(GameLauncher.prototype, 'getStatus').mockReturnValue({
        state: 'running',
        currentInstanceId: instanceId,
        pid: 1234,
        progress: null,
      });

      const res = await ModpackTransactionManager.applyTransaction({
        instanceId,
        updates: [{ versionId: 'ver-any' }],
      });

      expect(res.success).toBe(false);
      expect(res.error).toContain('uruchomiona');
    });

    it('should safely execute atomic transaction and update lockfile', async () => {
      vi.spyOn(GameLauncher.prototype, 'getStatus').mockReturnValue({
        state: 'idle',
        currentInstanceId: null,
        pid: null,
        progress: null,
      });

      vi.spyOn(ModRepository, 'getModsForInstance').mockImplementation(async () => {
        if (fs.existsSync(path.join(instanceModsDir, 'target-2.0.0.jar'))) {
          return [
            {
              fileName: 'target-2.0.0.jar',
              filePath: path.join(instanceModsDir, 'target-2.0.0.jar'),
              fileSize: 100,
              sha256: 'hash-target',
              enabled: true,
              metadata: {
                id: 'target-mod',
                name: 'Target Mod',
                version: '2.0.0',
                description: '',
                authors: [],
                loader: 'fabric',
                dependencies: [],
                rawSource: 'fabric.mod.json',
              },
              compatibilityStatus: 'Compatible',
              dateAdded: new Date().toISOString(),
            },
          ];
        }
        return [];
      });

      const targetVersion: ModrinthVersion = {
        id: 'ver-target',
        project_id: 'proj-target',
        author_id: 'auth',
        name: 'Target Mod 2.0.0',
        version_number: '2.0.0',
        dependencies: [],
        game_versions: ['1.20.1'],
        loaders: ['fabric'],
        version_type: 'release',
        date_published: new Date().toISOString(),
        downloads: 100,
        files: [
          {
            hashes: { sha512: 'sha512-val', sha1: 'sha1-val' },
            url: 'http://cdn/target-2.0.0.jar',
            filename: 'target-2.0.0.jar',
            primary: true,
            size: 10,
          },
        ],
      };

      vi.spyOn(ModrinthApiClient, 'getVersion').mockResolvedValue(targetVersion);

      // Mock downloading to return a temporary file path
      const mockStagedFile = path.join(testDir, 'temp-download.jar');
      fs.writeFileSync(mockStagedFile, 'downloaded-jar-content');

      vi.spyOn(ModrinthDownloader, 'downloadAndVerify').mockResolvedValue(mockStagedFile);

      const res = await ModpackTransactionManager.applyTransaction({
        instanceId,
        updates: [{ versionId: 'ver-target' }],
        createBackup: true,
      });

      expect(res.success).toBe(true);
      expect(res.appliedCount).toBe(1);
      expect(fs.existsSync(path.join(instanceModsDir, 'target-2.0.0.jar'))).toBe(true);

      const lockfile = await ModpackLockfileManager.getLockfile(instanceId);
      expect(lockfile?.mods.some((m) => m.fileName === 'target-2.0.0.jar')).toBe(true);
    });

    it('should rollback transaction on download or verification failure', async () => {
      vi.spyOn(GameLauncher.prototype, 'getStatus').mockReturnValue({
        state: 'idle',
        currentInstanceId: null,
        pid: null,
        progress: null,
      });

      // Existing mod that must be preserved
      fs.writeFileSync(path.join(instanceModsDir, 'safe-mod.jar'), 'safe-data');
      const lockfile = await ModpackLockfileManager.initDefaultLockfile(instanceId);
      lockfile.mods.push({
        fileName: 'safe-mod.jar',
        name: 'Safe Mod',
        version: '1.0.0',
        source: 'local',
        pinned: false,
        enabled: true,
        dependencies: [],
        installedAt: new Date().toISOString(),
      });
      await ModpackLockfileManager.saveLockfile(instanceId, lockfile);

      // Mock failure during version retrieval
      vi.spyOn(ModrinthApiClient, 'getVersion').mockRejectedValue(
        new Error('Network connection dropped')
      );

      const res = await ModpackTransactionManager.applyTransaction({
        instanceId,
        updates: [{ versionId: 'broken-ver' }],
        createBackup: true,
      });

      expect(res.success).toBe(false);
      expect(res.rolledBack).toBe(true);
      // Safe mod still intact
      expect(fs.existsSync(path.join(instanceModsDir, 'safe-mod.jar'))).toBe(true);
    });
  });

  // ==========================================================================
  // 7. ModLoader Compatibility Tests (Fabric, Forge, NeoForge, Quilt)
  // ==========================================================================
  describe('ModLoader Compatibility in Resolution', () => {
    it.each([
      ['fabric', ['fabric'], true],
      ['forge', ['forge'], true],
      ['neoforge', ['neoforge'], true],
      ['quilt', ['quilt'], true],
      ['quilt', ['fabric'], true], // Quilt is backwards-compatible with Fabric!
      ['fabric', ['forge'], false],
      ['forge', ['fabric'], false],
      ['neoforge', ['fabric'], false],
    ])('loader %s with mod supporting %j -> compatible: %s', (loader, modLoaders, expectedCompatible) => {
      const isCompatible = ModConflictResolver.isLoaderCompatible(
        modLoaders,
        loader as any
      );
      expect(isCompatible).toBe(expectedCompatible);
    });
  });
});
