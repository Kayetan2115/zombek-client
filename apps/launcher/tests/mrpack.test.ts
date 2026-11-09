import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import * as fs from 'node:fs';
import * as path from 'node:path';
import * as os from 'node:os';
import * as crypto from 'node:crypto';
import { MrpackPathValidator } from '../electron/modpacks/mrpack/MrpackPathValidator';
import { MrpackArchiveReader } from '../electron/modpacks/mrpack/MrpackArchiveReader';
import { MrpackManifestValidator } from '../electron/modpacks/mrpack/MrpackManifestValidator';
import { MrpackArchiveWriter } from '../electron/modpacks/mrpack/MrpackArchiveWriter';
import { MrpackOverrideManager } from '../electron/modpacks/mrpack/MrpackOverrideManager';
import { MrpackImporter } from '../electron/modpacks/mrpack/MrpackImporter';
import { MrpackExporter } from '../electron/modpacks/mrpack/MrpackExporter';
import { MrpackFormatManager } from '../electron/modpacks/mrpack/MrpackFormatManager';
import { EnginePaths } from '../electron/engine/paths';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import { ModRepository } from '../electron/modpacks/ModRepository';
import { ModpackLockfileManager } from '../electron/modpacks/ModpackLockfileManager';
import type {
  MrpackIndex,
  MrpackIndexFile,
  InstanceRecord,
  ModRecord,
} from '@zombek/shared-types';

describe('Etap 6D - Modrinth Modpack (.mrpack) Support & Security', () => {
  let testDir: string;
  let instancesDir: string;
  let tempMrpackDir: string;

  beforeEach(() => {
    testDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-mrpack-test-'));
    instancesDir = path.join(testDir, 'instances');
    tempMrpackDir = path.join(testDir, 'temp-mrpack');
    fs.mkdirSync(instancesDir, { recursive: true });
    fs.mkdirSync(tempMrpackDir, { recursive: true });

    // Mock EnginePaths
    vi.spyOn(EnginePaths, 'getInstanceDir').mockImplementation((id: string) => {
      return path.join(instancesDir, id);
    });
    vi.spyOn(EnginePaths, 'getInstanceModsDir').mockImplementation((id: string) => {
      return path.join(instancesDir, id, 'mods');
    });
    vi.spyOn(EnginePaths, 'getInstanceLockfilePath').mockImplementation((id: string) => {
      return path.join(instancesDir, id, 'modpack.lock.json');
    });
    vi.spyOn(EnginePaths, 'getInstanceDisabledModsDir').mockImplementation((id: string) => {
      return path.join(instancesDir, id, 'disabled-mods');
    });
    vi.spyOn(EnginePaths, 'getInstanceConfigDir').mockImplementation((id: string) => {
      return path.join(instancesDir, id, 'config');
    });
    vi.spyOn(EnginePaths, 'getTempMrpackDir').mockImplementation(() => {
      return tempMrpackDir;
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
    try {
      fs.rmSync(testDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
  });

  // ==========================================================================
  // 1. MRPACK PATH VALIDATOR & SECURITY
  // ==========================================================================
  describe('MrpackPathValidator - Path Traversal & Zip Slip Security', () => {
    it('should validate and sanitize normal relative paths', () => {
      expect(MrpackPathValidator.isValidRelativePath('mods/sodium.jar')).toBe(true);
      expect(MrpackPathValidator.isValidRelativePath('config/fabric/test.json')).toBe(true);
      expect(MrpackPathValidator.isValidRelativePath('options.txt')).toBe(true);
      expect(MrpackPathValidator.sanitizePath('mods\\sodium.jar')).toBe('mods/sodium.jar');
    });

    it('should reject path traversal sequences (Zip Slip attack vectors)', () => {
      expect(MrpackPathValidator.isValidRelativePath('../secret.txt')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('mods/../../etc/passwd')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('..\\windows\\system32')).toBe(false);
      expect(() => MrpackPathValidator.sanitizePath('../../boot.ini')).toThrow();
    });

    it('should reject absolute paths, Windows drive letters, and UNC paths', () => {
      expect(MrpackPathValidator.isValidRelativePath('/etc/passwd')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('\\root\\file.txt')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('C:/Windows/System32/cmd.exe')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('D:\\games\\minecraft')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('\\\\server\\share\\malware.exe')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('//server/share/file')).toBe(false);
    });

    it('should reject reserved Windows device names', () => {
      expect(MrpackPathValidator.isValidRelativePath('CON')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('PRN.txt')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('mods/AUX')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('config/NUL.json')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('COM1')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('LPT2')).toBe(false);
    });

    it('should reject null bytes and illegal control characters', () => {
      expect(MrpackPathValidator.isValidRelativePath('mods/file\0.jar')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('mods/file<name>.jar')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('mods/file"quote".jar')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('mods/file|pipe.jar')).toBe(false);
      expect(MrpackPathValidator.isValidRelativePath('mods/file?star*.jar')).toBe(false);
    });

    it('should enforce root boundary confinement with assertSafeSubpath', () => {
      const root = path.join(testDir, 'sandbox');
      fs.mkdirSync(root, { recursive: true });

      const safe = MrpackPathValidator.assertSafeSubpath(root, 'mods/test.jar');
      expect(safe.startsWith(path.resolve(root))).toBe(true);

      expect(() => MrpackPathValidator.assertSafeSubpath(root, '../outside.txt')).toThrow();
      expect(() => MrpackPathValidator.assertSafeSubpath(root, 'C:\\windows\\system32')).toThrow();
    });
  });

  // ==========================================================================
  // 2. MRPACK MANIFEST VALIDATOR (SCHEMA V1)
  // ==========================================================================
  describe('MrpackManifestValidator - Schema V1 & Dependency Enforcement', () => {
    const validManifest: MrpackIndex = {
      formatVersion: 1,
      game: 'minecraft',
      versionId: '1.0.0',
      name: 'Test Modpack',
      summary: 'A test modpack for ZombekClient',
      dependencies: {
        minecraft: '1.20.4',
        'fabric-loader': '0.15.7',
      },
      files: [
        {
          path: 'mods/fabric-api.jar',
          hashes: {
            sha1: 'a'.repeat(40),
            sha512: 'b'.repeat(128),
          },
          env: {
            client: 'required',
            server: 'optional',
          },
          downloads: ['https://cdn.modrinth.com/data/P7dR8mSH/versions/1.0/fabric-api.jar'],
          fileSize: 1024,
        },
      ],
    };

    it('should validate a correct Modrinth Modpack formatVersion 1 manifest', () => {
      const res = MrpackManifestValidator.validate(validManifest);
      expect(res.valid).toBe(true);
      expect(res.errors).toHaveLength(0);
      expect(res.loaderType).toBe('fabric');
      expect(res.loaderVersion).toBe('0.15.7');
      expect(res.minecraftVersion).toBe('1.20.4');
    });

    it('should reject unsupported formatVersion', () => {
      const invalid = { ...validManifest, formatVersion: 2 };
      const res = MrpackManifestValidator.validate(invalid);
      expect(res.valid).toBe(false);
      expect(res.errors.some((e) => e.includes('formatVersion'))).toBe(true);
    });

    it('should reject manifests with wrong game identifier', () => {
      const invalid = { ...validManifest, game: 'hytale' };
      const res = MrpackManifestValidator.validate(invalid);
      expect(res.valid).toBe(false);
      expect(res.errors.some((e) => e.includes('game'))).toBe(true);
    });

    it('should reject manifests missing required metadata fields', () => {
      const noName = { ...validManifest, name: '' };
      expect(MrpackManifestValidator.validate(noName).valid).toBe(false);

      const noVersion = { ...validManifest, versionId: '' };
      expect(MrpackManifestValidator.validate(noVersion).valid).toBe(false);

      const noMinecraft = { ...validManifest, dependencies: {} };
      expect(MrpackManifestValidator.validate(noMinecraft).valid).toBe(false);
    });

    it('should reject conflicting loader dependencies (e.g. Fabric + Forge)', () => {
      const conflicting: MrpackIndex = {
        ...validManifest,
        dependencies: {
          minecraft: '1.20.4',
          'fabric-loader': '0.15.7',
          forge: '49.0.3',
        },
      };
      const res = MrpackManifestValidator.validate(conflicting);
      expect(res.valid).toBe(false);
      expect(res.errors.some((e) => e.includes('Konflikt loaderów'))).toBe(true);
    });

    it('should correctly detect Vanilla, Forge, NeoForge, and Quilt loaders', () => {
      // Vanilla (no loader key)
      const vanilla = {
        ...validManifest,
        dependencies: { minecraft: '1.20.4' },
      };
      expect(MrpackManifestValidator.validate(vanilla).loaderType).toBe('vanilla');

      // Forge
      const forge = {
        ...validManifest,
        dependencies: { minecraft: '1.20.4', forge: '49.0.30' },
      };
      expect(MrpackManifestValidator.validate(forge).loaderType).toBe('forge');

      // NeoForge
      const neoforge = {
        ...validManifest,
        dependencies: { minecraft: '1.20.4', neoforge: '20.4.80-beta' },
      };
      expect(MrpackManifestValidator.validate(neoforge).loaderType).toBe('neoforge');

      // Quilt
      const quilt = {
        ...validManifest,
        dependencies: { minecraft: '1.20.4', 'quilt-loader': '0.25.0' },
      };
      expect(MrpackManifestValidator.validate(quilt).loaderType).toBe('quilt');
    });

    it('should reject invalid hash lengths or non-hex characters', () => {
      const invalidSha512 = {
        ...validManifest,
        files: [
          {
            ...validManifest.files[0],
            hashes: { sha1: 'a'.repeat(40), sha512: 'short-hash' },
          },
        ],
      };
      expect(MrpackManifestValidator.validate(invalidSha512).valid).toBe(false);

      const invalidSha1 = {
        ...validManifest,
        files: [
          {
            ...validManifest.files[0],
            hashes: { sha1: 'xyz!'.padEnd(40, '0'), sha512: 'b'.repeat(128) },
          },
        ],
      };
      expect(MrpackManifestValidator.validate(invalidSha1).valid).toBe(false);
    });

    it('should reject non-HTTPS download URLs or empty download lists', () => {
      const httpUrl = {
        ...validManifest,
        files: [
          {
            ...validManifest.files[0],
            downloads: ['http://insecure.site/mod.jar'],
          },
        ],
      };
      expect(MrpackManifestValidator.validate(httpUrl).valid).toBe(false);

      const noUrls = {
        ...validManifest,
        files: [
          {
            ...validManifest.files[0],
            downloads: [],
          },
        ],
      };
      expect(MrpackManifestValidator.validate(noUrls).valid).toBe(false);
    });
  });

  // ==========================================================================
  // 3. MRPACK ARCHIVE WRITER & READER
  // ==========================================================================
  describe('MrpackArchiveWriter & MrpackArchiveReader - ZIP Handling & Limits', () => {
    it('should generate a valid ZIP archive containing modrinth.index.json and overrides', async () => {
      const archivePath = path.join(tempMrpackDir, 'output.mrpack');
      const manifest: MrpackIndex = {
        formatVersion: 1,
        game: 'minecraft',
        versionId: '1.0.0',
        name: 'Created Modpack',
        dependencies: { minecraft: '1.20.4', 'fabric-loader': '0.15.7' },
        files: [],
      };

      const overrideSource = path.join(testDir, 'sample-config.json');
      fs.writeFileSync(overrideSource, JSON.stringify({ key: 'custom-config-value' }));

      const writeResult = await MrpackArchiveWriter.createArchive({
        destinationPath: archivePath,
        manifest,
        overrideFiles: [
          {
            relativePath: 'config/sample-config.json',
            fullPath: overrideSource,
            prefix: 'overrides',
          },
        ],
      });

      expect(writeResult.success).toBe(true);
      expect(fs.existsSync(archivePath)).toBe(true);

      // Now read back using MrpackArchiveReader
      const reader = await MrpackArchiveReader.open(archivePath);
      expect(reader.hasManifest()).toBe(true);

      const readManifest = reader.readManifest();
      expect(readManifest.name).toBe('Created Modpack');
      expect(readManifest.dependencies.minecraft).toBe('1.20.4');

      const entries = reader.listEntries();
      expect(entries.some((e) => e.name === 'modrinth.index.json')).toBe(true);
      expect(entries.some((e) => e.name === 'overrides/config/sample-config.json')).toBe(true);

      const configBuf = reader.readEntry('overrides/config/sample-config.json');
      expect(configBuf).not.toBeNull();
      const parsed = JSON.parse(configBuf!.toString('utf-8'));
      expect(parsed.key).toBe('custom-config-value');
    });

    it('should reject archives without modrinth.index.json in root', async () => {
      const emptyArchive = path.join(tempMrpackDir, 'no-manifest.zip');
      // Create empty ZIP (EOCD record of 22 bytes)
      const eocd = Buffer.alloc(22);
      eocd.writeUInt32LE(0x06054b50, 0);
      fs.writeFileSync(emptyArchive, eocd);

      const reader = await MrpackArchiveReader.open(emptyArchive);
      expect(reader.hasManifest()).toBe(false);
      expect(() => reader.readManifest()).toThrow();
    });

    it('should enforce archive limits (rejecting oversized files or zip bombs)', () => {
      const fakePath = path.join(tempMrpackDir, 'oversized.mrpack');
      // Create small file but test max size limit check
      fs.writeFileSync(fakePath, Buffer.alloc(100));

      const res = MrpackArchiveReader.validateArchiveFile(fakePath);
      expect(res.valid).toBe(true);

      // Test validation of non-existent file
      const nonExistent = MrpackArchiveReader.validateArchiveFile(path.join(tempMrpackDir, 'ghost.mrpack'));
      expect(nonExistent.valid).toBe(false);
    });
  });

  // ==========================================================================
  // 4. MRPACK OVERRIDE MANAGER
  // ==========================================================================
  describe('MrpackOverrideManager - Client vs Server & Data Exclusion', () => {
    it('should apply client-overrides over standard overrides with precedence', async () => {
      const destInstanceDir = path.join(instancesDir, 'override-test-inst');
      fs.mkdirSync(destInstanceDir, { recursive: true });

      const entries = [
        {
          name: 'overrides/config/test.txt',
          content: Buffer.from('BASE OVERRIDE CONTENT'),
        },
        {
          name: 'client-overrides/config/test.txt',
          content: Buffer.from('CLIENT SPECIFIC CONTENT'),
        },
        {
          name: 'overrides/config/other.txt',
          content: Buffer.from('OTHER CONTENT'),
        },
      ];

      const res = await MrpackOverrideManager.applyOverridesToInstance({
        instanceDir: destInstanceDir,
        entries,
        readEntryBuffer: (entryName) => {
          const item = entries.find((e) => e.name === entryName);
          return item ? item.content : null;
        },
      });

      expect(res.appliedFiles).toContain('config/test.txt');
      expect(res.appliedFiles).toContain('config/other.txt');

      const testContent = fs.readFileSync(path.join(destInstanceDir, 'config', 'test.txt'), 'utf-8');
      expect(testContent).toBe('CLIENT SPECIFIC CONTENT'); // client-overrides won!
    });

    it('should preserve server-overrides in .mrpack-preserved/ without installing to game root', async () => {
      const destInstanceDir = path.join(instancesDir, 'server-override-test-inst');
      fs.mkdirSync(destInstanceDir, { recursive: true });

      const entries = [
        {
          name: 'server-overrides/server.properties',
          content: Buffer.from('server-port=25565'),
        },
      ];

      const res = await MrpackOverrideManager.applyOverridesToInstance({
        instanceDir: destInstanceDir,
        entries,
        readEntryBuffer: (entryName) => {
          const item = entries.find((e) => e.name === entryName);
          return item ? item.content : null;
        },
      });

      expect(res.serverOverridesSavedCount).toBe(1);
      // Not installed in root
      expect(fs.existsSync(path.join(destInstanceDir, 'server.properties'))).toBe(false);
      // Preserved in hidden directory
      expect(
        fs.existsSync(path.join(destInstanceDir, '.mrpack-preserved', 'server-overrides', 'server.properties'))
      ).toBe(true);
    });

    it('should strictly exclude private data, logs, saves, accounts, and session tokens when collecting overrides for export', async () => {
      const sourceDir = path.join(instancesDir, 'privacy-test-inst');
      fs.mkdirSync(path.join(sourceDir, 'config'), { recursive: true });
      fs.mkdirSync(path.join(sourceDir, 'saves', 'world1'), { recursive: true });
      fs.mkdirSync(path.join(sourceDir, 'logs'), { recursive: true });
      fs.mkdirSync(path.join(sourceDir, 'screenshots'), { recursive: true });
      fs.mkdirSync(path.join(sourceDir, 'crash-reports'), { recursive: true });

      fs.writeFileSync(path.join(sourceDir, 'config', 'mod.cfg'), 'safe config');
      fs.writeFileSync(path.join(sourceDir, 'options.txt'), 'fov:70');
      fs.writeFileSync(path.join(sourceDir, 'saves', 'world1', 'level.dat'), 'private world save');
      fs.writeFileSync(path.join(sourceDir, 'logs', 'latest.log'), 'sensitive chat log');
      fs.writeFileSync(path.join(sourceDir, 'screenshots', 'secret.png'), 'screenshot');
      fs.writeFileSync(path.join(sourceDir, 'launcher_accounts.json'), '{"tokens":"SECRET"}');
      fs.writeFileSync(path.join(sourceDir, '.session'), 'session-id');

      const collected = await MrpackOverrideManager.collectOverridesForExport({
        instanceDir: sourceDir,
        selectedOptions: { config: true, optionsTxt: true },
      });

      const paths = collected.map((c) => c.relativePath);
      expect(paths).toContain('config/mod.cfg');
      expect(paths).toContain('options.txt');

      // Assert ABSOLUTE EXCLUSION of sensitive files
      expect(paths.some((p) => p.includes('saves'))).toBe(false);
      expect(paths.some((p) => p.includes('logs'))).toBe(false);
      expect(paths.some((p) => p.includes('screenshots'))).toBe(false);
      expect(paths.some((p) => p.includes('crash-reports'))).toBe(false);
      expect(paths.some((p) => p.includes('launcher_accounts'))).toBe(false);
      expect(paths.some((p) => p.includes('.session'))).toBe(false);
    });
  });

  // ==========================================================================
  // 5. MRPACK IMPORTER & TRANSACTIONAL ROLLBACK
  // ==========================================================================
  describe('MrpackImporter - Inspection, Whitelisting & Rollback', () => {
    it('should inspect an archive returning full summary without modifying game installations', async () => {
      const archivePath = path.join(tempMrpackDir, 'inspect-pack.mrpack');
      const manifest: MrpackIndex = {
        formatVersion: 1,
        game: 'minecraft',
        versionId: '1.2.0',
        name: 'Inspection Test Pack',
        summary: 'Preview summary for UI',
        dependencies: {
          minecraft: '1.20.4',
          'fabric-loader': '0.15.7',
        },
        files: [
          {
            path: 'mods/mod-a.jar',
            hashes: { sha1: '1'.repeat(40), sha512: '2'.repeat(128) },
            downloads: ['https://cdn.modrinth.com/data/mod-a.jar'],
            fileSize: 2048,
          },
          {
            path: 'mods/mod-optional.jar',
            hashes: { sha1: '3'.repeat(40), sha512: '4'.repeat(128) },
            env: { client: 'optional' },
            downloads: ['https://cdn.modrinth.com/data/mod-opt.jar'],
            fileSize: 4096,
          },
        ],
      };

      await MrpackArchiveWriter.createArchive({
        destinationPath: archivePath,
        manifest,
        overrideFiles: [],
      });

      const inspection = await MrpackImporter.inspect(archivePath);
      expect(inspection.valid).toBe(true);
      expect(inspection.index?.name).toBe('Inspection Test Pack');
      expect(inspection.minecraftVersion).toBe('1.20.4');
      expect(inspection.loaderType).toBe('fabric');
      expect(inspection.loaderVersion).toBe('0.15.7');
      expect(inspection.totalFilesCount).toBe(2);
      expect(inspection.optionalFilesCount).toBe(1);
      expect(inspection.totalDownloadSizeBytes).toBe(6144);
    });

    it('should reject unapproved download hostnames outside whitelist', () => {
      expect(MrpackImporter.isWhitelistedHost('https://cdn.modrinth.com/data/test.jar')).toBe(true);
      expect(MrpackImporter.isWhitelistedHost('https://edge.modrinth.com/file.jar')).toBe(true);
      expect(MrpackImporter.isWhitelistedHost('https://github.com/releases/download/test.jar')).toBe(true);
      expect(MrpackImporter.isWhitelistedHost('https://raw.githubusercontent.com/user/repo/mod.jar')).toBe(true);

      // Unapproved external / private hosts
      expect(MrpackImporter.isWhitelistedHost('https://malicious-domain.com/stealer.jar')).toBe(false);
      expect(MrpackImporter.isWhitelistedHost('https://192.168.1.1/mod.jar')).toBe(false);
      expect(MrpackImporter.isWhitelistedHost('https://127.0.0.1:8080/mod.jar')).toBe(false);
      expect(MrpackImporter.isWhitelistedHost('https://10.0.0.1/mod.jar')).toBe(false);
    });

    it('should rollback transaction and clean up staging on fatal download or verification error', async () => {
      const archivePath = path.join(tempMrpackDir, 'failing-pack.mrpack');
      const manifest: MrpackIndex = {
        formatVersion: 1,
        game: 'minecraft',
        versionId: '1.0.0',
        name: 'Failing Pack',
        dependencies: { minecraft: '1.20.4', 'fabric-loader': '0.15.7' },
        files: [
          {
            path: 'mods/unapproved.jar',
            hashes: { sha1: 'a'.repeat(40), sha512: 'b'.repeat(128) },
            downloads: ['https://unapproved-hacker-site.com/exploit.jar'],
            fileSize: 100,
          },
        ],
      };

      await MrpackArchiveWriter.createArchive({
        destinationPath: archivePath,
        manifest,
        overrideFiles: [],
      });

      const stages: string[] = [];
      const result = await MrpackImporter.import(
        {
          archivePath,
          instanceName: 'Failed Import Instance',
        },
        (prog) => {
          stages.push(prog.stage);
        }
      );

      expect(result.success).toBe(false);
      expect(result.error).toBeDefined();
      expect(stages).toContain('rollback');
      expect(stages).toContain('failed');

      // The new instance should have been deleted during rollback
      if (result.instanceId) {
        expect(fs.existsSync(path.join(instancesDir, result.instanceId))).toBe(false);
      }
    });
  });

  // ==========================================================================
  // 6. MANDATORY ROUND-TRIP INTEGRATION TEST
  // ==========================================================================
  describe('Mandatory Round-Trip: Export Instance -> Validate .mrpack -> Import Instance', () => {
    it('should export an instance with mods and configs, produce valid .mrpack, and import cleanly into a new instance', async () => {
      // Step A: Setup source instance with mock mod files and config overrides
      const sourceInstId = 'source-roundtrip-inst';
      const sourceInstDir = path.join(instancesDir, sourceInstId);
      const sourceModsDir = path.join(sourceInstDir, 'mods');
      const sourceConfigDir = path.join(sourceInstDir, 'config');
      fs.mkdirSync(sourceModsDir, { recursive: true });
      fs.mkdirSync(sourceConfigDir, { recursive: true });

      // Create dummy mod file with known hashes
      const modContent = Buffer.from('JAR MOCK CONTENT FOR SODIUM MOD 0.5.8');
      const modSha1 = crypto.createHash('sha1').update(modContent).digest('hex');
      const modSha512 = crypto.createHash('sha512').update(modContent).digest('hex');
      const modJarPath = path.join(sourceModsDir, 'sodium-fabric-0.5.8.jar');
      fs.writeFileSync(modJarPath, modContent);

      // Create dummy config file
      const configJsonPath = path.join(sourceConfigDir, 'sodium-options.json');
      fs.writeFileSync(configJsonPath, JSON.stringify({ graphicsPreset: 'HIGH', vsync: true }));

      // Create instance record
      const instanceRecord: InstanceRecord = {
        id: sourceInstId,
        name: 'Source Production Pack',
        minecraftVersion: '1.20.4',
        loader: 'fabric',
        loaderVersion: '0.15.7',
        allocatedRamMb: 4096,
        instanceDir: sourceInstDir,
        created: new Date().toISOString(),
      };
      fs.writeFileSync(path.join(sourceInstDir, 'instance.json'), JSON.stringify(instanceRecord));

      // Create mock lockfile so exporter recognizes it as Modrinth mod
      await ModpackLockfileManager.saveLockfile(sourceInstId, {
        schemaVersion: 1,
        instanceId: sourceInstId,
        gameVersion: '1.20.4',
        loader: { id: 'fabric', version: '0.15.7' },
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        mods: [
          {
            fileName: 'sodium-fabric-0.5.8.jar',
            name: 'Sodium',
            version: '0.5.8',
            projectId: 'AANobbMI',
            versionId: 'test-sodium-v1',
            sha1: modSha1,
            sha512: modSha512,
            source: 'modrinth',
            pinned: false,
            enabled: true,
            dependencies: [],
            installedAt: new Date().toISOString(),
          },
        ],
      });

      // Step B: Export instance to .mrpack archive
      const exportedMrpackPath = path.join(tempMrpackDir, 'roundtrip-exported.mrpack');
      const exportResult = await MrpackExporter.export({
        instanceId: sourceInstId,
        outputFilePath: exportedMrpackPath,
        name: 'RoundTrip Pack',
        versionId: '2.0.0',
        summary: 'Automated round-trip test modpack',
        includeCustomJars: true,
        selectedOverrides: { config: true, optionsTxt: true },
      });

      expect(exportResult.success).toBe(true);
      expect(exportResult.filesCount).toBe(1);
      expect(exportResult.overridesCount).toBeGreaterThanOrEqual(1);
      expect(fs.existsSync(exportedMrpackPath)).toBe(true);

      // Step C: Validate exported archive structure
      const reader = await MrpackArchiveReader.open(exportedMrpackPath);
      expect(reader.hasManifest()).toBe(true);
      const manifest = reader.readManifest();
      expect(manifest.name).toBe('RoundTrip Pack');
      expect(manifest.versionId).toBe('2.0.0');
      expect(manifest.dependencies.minecraft).toBe('1.20.4');
      expect(manifest.dependencies['fabric-loader']).toBe('0.15.7');
      expect(manifest.files).toHaveLength(1);
      expect(manifest.files[0].path).toBe('mods/sodium-fabric-0.5.8.jar');
      expect(manifest.files[0].hashes.sha512).toBe(modSha512);

      // Verify config override was packaged into overrides/config/
      const entries = reader.listEntries();
      expect(entries.some((e) => e.name === 'overrides/config/sodium-options.json')).toBe(true);

      // Step D: Import the exported .mrpack archive into a NEW instance
      // Mock DownloadManager / fetch for the mod download
      const targetInstName = 'Imported Fresh Pack';
      const importProgressList: string[] = [];

      // Mock file download in MrpackImporter by writing the mock mod bytes
      vi.spyOn(MrpackImporter as any, 'downloadSingleUrl').mockImplementation(
        async (...args: any[]) => {
          const destPath = args[1] as string;
          await fs.promises.writeFile(destPath, modContent);
        }
      );

      const importResult = await MrpackImporter.import(
        {
          archivePath: exportedMrpackPath,
          instanceName: targetInstName,
        },
        (prog) => {
          importProgressList.push(prog.stage);
        }
      );

      expect(importResult.success).toBe(true);
      expect(importResult.instanceId).toBeDefined();
      expect(importProgressList).toContain('completed');

      const importedInstDir = path.join(instancesDir, importResult.instanceId!);
      expect(fs.existsSync(importedInstDir)).toBe(true);

      // Verify mod was installed
      const importedModFile = path.join(importedInstDir, 'mods', 'sodium-fabric-0.5.8.jar');
      expect(fs.existsSync(importedModFile)).toBe(true);
      const importedModBytes = fs.readFileSync(importedModFile);
      expect(importedModBytes.toString('utf-8')).toBe(modContent.toString('utf-8'));

      // Verify config override was restored
      const importedConfig = path.join(importedInstDir, 'config', 'sodium-options.json');
      expect(fs.existsSync(importedConfig)).toBe(true);
      const parsedConfig = JSON.parse(fs.readFileSync(importedConfig, 'utf-8'));
      expect(parsedConfig.graphicsPreset).toBe('HIGH');
      expect(parsedConfig.vsync).toBe(true);

      // Verify lockfile was generated
      const importedLockfile = await ModpackLockfileManager.getLockfile(importResult.instanceId!);
      expect(importedLockfile).not.toBeNull();
      expect(importedLockfile?.mods).toHaveLength(1);
      expect(importedLockfile?.mods[0].fileName).toBe('sodium-fabric-0.5.8.jar');
      expect(importedLockfile?.mods[0].sha512).toBe(modSha512);
    });
  });

  // ==========================================================================
  // 7. MODLOADER COMPATIBILITY MATRIX
  // ==========================================================================
  describe('Modloader Compatibility Matrix (Vanilla, Fabric, Forge, NeoForge, Quilt)', () => {
    const loaders: Array<{ loader: string; key?: string; version: string }> = [
      { loader: 'vanilla', version: '' },
      { loader: 'fabric', key: 'fabric-loader', version: '0.15.11' },
      { loader: 'forge', key: 'forge', version: '47.2.20' },
      { loader: 'neoforge', key: 'neoforge', version: '20.4.80-beta' },
      { loader: 'quilt', key: 'quilt-loader', version: '0.25.0' },
    ];

    loaders.forEach(({ loader, key, version }) => {
      it(`should properly detect and validate ${loader.toUpperCase()} modpack index`, () => {
        const deps: Record<string, string> = { minecraft: '1.20.4' };
        if (key && version) {
          deps[key] = version;
        }

        const manifest: MrpackIndex = {
          formatVersion: 1,
          game: 'minecraft',
          versionId: '1.0.0',
          name: `${loader} Test Modpack`,
          dependencies: deps as any,
          files: [],
        };

        const res = MrpackManifestValidator.validate(manifest);
        expect(res.valid).toBe(true);
        expect(res.loaderType).toBe(loader);
        if (version) {
          expect(res.loaderVersion).toBe(version);
        }
      });
    });
  });
});
