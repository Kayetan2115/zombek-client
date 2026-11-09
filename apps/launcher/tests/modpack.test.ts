import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import * as path from 'node:path';
import * as fs from 'node:fs';
import * as os from 'node:os';
import { EnginePaths } from '../electron/engine/paths';
import { JarArchiveReader } from '../electron/modpacks/JarArchiveReader';
import { SimpleTomlParser } from '../electron/modpacks/SimpleTomlParser';
import { ModMetadataReader } from '../electron/modpacks/ModMetadataReader';
import { ModCompatibilityChecker } from '../electron/modpacks/ModCompatibilityChecker';
import { ModDependencyAnalyzer } from '../electron/modpacks/ModDependencyAnalyzer';
import { ModFileManager } from '../electron/modpacks/ModFileManager';
import { ModRepository } from '../electron/modpacks/ModRepository';
import { ModInstallationService } from '../electron/modpacks/ModInstallationService';
import { ModpackRepository } from '../electron/modpacks/ModpackRepository';
import { ModpackProfileManager } from '../electron/modpacks/ModpackProfileManager';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import type { UniversalModMetadata } from '@zombek/shared-types';

// CRC32 table for ZIP creation in tests
const CRC_TABLE = new Uint32Array(256);
for (let n = 0; n < 256; n++) {
  let c = n;
  for (let k = 0; k < 8; k++) {
    c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  }
  CRC_TABLE[n] = c;
}

function calculateCrc32(buf: Buffer): number {
  let crc = 0 ^ -1;
  for (let i = 0; i < buf.length; i++) {
    crc = (crc >>> 8) ^ CRC_TABLE[(crc ^ buf[i]) & 0xff];
  }
  return (crc ^ -1) >>> 0;
}

/**
 * Creates a valid ZIP/JAR buffer in memory for testing
 */
function createMockJar(entries: Record<string, string | Buffer>): Buffer {
  const localHeaders: Buffer[] = [];
  const centralHeaders: Buffer[] = [];
  let currentOffset = 0;

  for (const [filename, content] of Object.entries(entries)) {
    const rawData = typeof content === 'string' ? Buffer.from(content, 'utf-8') : content;
    const nameBytes = Buffer.from(filename, 'utf-8');
    const crc = calculateCrc32(rawData);

    // Local file header (30 bytes + name)
    const localHeader = Buffer.alloc(30 + nameBytes.length);
    localHeader.writeUInt32LE(0x04034b50, 0); // Signature
    localHeader.writeUInt16LE(20, 4); // Min version (2.0)
    localHeader.writeUInt16LE(0, 6); // Flags
    localHeader.writeUInt16LE(0, 8); // Method: 0 (Stored)
    localHeader.writeUInt16LE(0, 10); // Mod time
    localHeader.writeUInt16LE(0, 12); // Mod date
    localHeader.writeUInt32LE(crc, 14); // CRC32
    localHeader.writeUInt32LE(rawData.length, 18); // Compressed size
    localHeader.writeUInt32LE(rawData.length, 22); // Uncompressed size
    localHeader.writeUInt16LE(nameBytes.length, 26); // Name length
    localHeader.writeUInt16LE(0, 28); // Extra length
    nameBytes.copy(localHeader, 30);

    localHeaders.push(localHeader, rawData);

    // Central directory header (46 bytes + name)
    const centralHeader = Buffer.alloc(46 + nameBytes.length);
    centralHeader.writeUInt32LE(0x02014b50, 0); // Signature
    centralHeader.writeUInt16LE(20, 4); // Made by
    centralHeader.writeUInt16LE(20, 6); // Min version
    centralHeader.writeUInt16LE(0, 8); // Flags
    centralHeader.writeUInt16LE(0, 10); // Method 0
    centralHeader.writeUInt16LE(0, 12); // Mod time
    centralHeader.writeUInt16LE(0, 14); // Mod date
    centralHeader.writeUInt32LE(crc, 16); // CRC32
    centralHeader.writeUInt32LE(rawData.length, 20); // Compressed size
    centralHeader.writeUInt32LE(rawData.length, 24); // Uncompressed size
    centralHeader.writeUInt16LE(nameBytes.length, 28); // Name length
    centralHeader.writeUInt16LE(0, 30); // Extra field length
    centralHeader.writeUInt16LE(0, 32); // Comment length
    centralHeader.writeUInt16LE(0, 34); // Disk start
    centralHeader.writeUInt16LE(0, 36); // Internal attr
    centralHeader.writeUInt32LE(0, 38); // External attr
    centralHeader.writeUInt32LE(currentOffset, 42); // Offset of local header
    nameBytes.copy(centralHeader, 46);

    centralHeaders.push(centralHeader);
    currentOffset += localHeader.length + rawData.length;
  }

  const centralDirOffset = currentOffset;
  const centralDirSize = centralHeaders.reduce((sum, h) => sum + h.length, 0);
  const entryCount = Object.keys(entries).length;

  // End of central directory record (22 bytes)
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0);
  eocd.writeUInt16LE(0, 4); // Disk num
  eocd.writeUInt16LE(0, 6); // Disk with central
  eocd.writeUInt16LE(entryCount, 8); // Entries this disk
  eocd.writeUInt16LE(entryCount, 10); // Total entries
  eocd.writeUInt32LE(centralDirSize, 12); // Size of central dir
  eocd.writeUInt32LE(centralDirOffset, 16); // Offset of central dir
  eocd.writeUInt16LE(0, 20); // Comment length

  return Buffer.concat([...localHeaders, ...centralHeaders, eocd]);
}

describe('Modpack Manager Core — Comprehensive Test Suite', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-modpack-test-'));
    EnginePaths.setRootDir(tempDir);
    EnginePaths.ensureAllBaseDirs();
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
    vi.restoreAllMocks();
  });

  // =========================================================================
  // 1. JarArchiveReader & Security
  // =========================================================================
  describe('JarArchiveReader & Zip Security', () => {
    it('should read file entries and retrieve uncompressed text', () => {
      const jar = createMockJar({
        'fabric.mod.json': JSON.stringify({ id: 'testmod', name: 'Test Mod' }),
        'pack.png': Buffer.from([0x89, 0x50, 0x4e, 0x47]),
      });

      const entries = JarArchiveReader.parseCentralDirectory(jar);
      const names = entries.map((e) => e.fileName);

      expect(names).toContain('fabric.mod.json');
      expect(names).toContain('pack.png');

      const content = JarArchiveReader.readEntryTextFromBuffer(jar, 'fabric.mod.json');
      expect(content).toBeDefined();
      expect(JSON.parse(content!)).toEqual({ id: 'testmod', name: 'Test Mod' });
    });

    it('should reject ZipSlip path traversal attacks in archive entries', () => {
      const maliciousJar = createMockJar({
        '../../evil.txt': 'malicious payload',
      });

      expect(() => JarArchiveReader.parseCentralDirectory(maliciousJar)).toThrow(/ZipSlip/);
    });

    it('should throw on corrupted or invalid ZIP headers', () => {
      const corruptedBuf = Buffer.from('this is not a valid zip archive header');
      expect(() => JarArchiveReader.parseCentralDirectory(corruptedBuf)).toThrow(/sygnatury/);
    });
  });

  // =========================================================================
  // 2. SimpleTomlParser
  // =========================================================================
  describe('SimpleTomlParser', () => {
    it('should parse Forge mods.toml with tables, arrays, and multiline values', () => {
      const tomlContent = `
modLoader="javafml"
loaderVersion="[49,)"
license="LGPL-3.0"

[[mods]]
modId="jei"
version="15.2.0.27"
displayName="Just Enough Items"
authors="mezz"
description='''
JEI is an item and recipe viewing mod.
Highly customizable!
'''

[[dependencies.jei]]
modId="forge"
mandatory=true
versionRange="[49.0.0,)"
ordering="NONE"
side="BOTH"
`;

      const result = SimpleTomlParser.parse(tomlContent);
      expect(result.modLoader).toBe('javafml');
      expect(result.loaderVersion).toBe('[49,)');
      expect(Array.isArray(result.mods)).toBe(true);
      expect(result.mods[0].modId).toBe('jei');
      expect(result.mods[0].displayName).toBe('Just Enough Items');
      expect(result.mods[0].authors).toBe('mezz');
      expect(result.mods[0].description).toContain('JEI is an item');
      expect(result.dependencies.jei[0].mandatory).toBe(true);
      expect(result.dependencies.jei[0].modId).toBe('forge');
    });

    it('should parse NeoForge neoforge.mods.toml format', () => {
      const tomlContent = `
modLoader="neoforge"
loaderVersion="[20.4,)"
license="MIT"

[[mods]]
modId="sodium"
version="0.5.8"
displayName="Sodium NeoForge"
authors=["jellysquid", "ims21"]
description="Modern rendering engine"

[[dependencies.sodium]]
modId="neoforge"
type="required"
versionRange="[20.4.190,)"
`;

      const result = SimpleTomlParser.parse(tomlContent);
      expect(result.modLoader).toBe('neoforge');
      expect(result.mods[0].modId).toBe('sodium');
      expect(result.mods[0].displayName).toBe('Sodium NeoForge');
      expect(result.mods[0].authors).toEqual(['jellysquid', 'ims21']);
      expect(result.dependencies.sodium[0].type).toBe('required');
    });
  });

  // =========================================================================
  // 3. ModMetadataReader
  // =========================================================================
  describe('ModMetadataReader', () => {
    it('should read and normalize Fabric metadata from fabric.mod.json', async () => {
      const jar = createMockJar({
        'fabric.mod.json': JSON.stringify({
          schemaVersion: 1,
          id: 'fabric-api',
          version: '0.92.0+1.20.4',
          name: 'Fabric API',
          description: 'Essential hooks for Fabric mods.',
          authors: ['FabricMC'],
          license: 'Apache-2.0',
          environment: '*',
          depends: {
            fabricloader: '>=0.15.0',
            minecraft: '~1.20.4',
          },
          breaks: {
            optifine: '*',
          },
        }),
      });

      const filePath = path.join(tempDir, 'fabric-api.jar');
      fs.writeFileSync(filePath, jar);

      const meta = await ModMetadataReader.readMetadata(filePath);
      expect(meta.loader).toBe('fabric');
      expect(meta.id).toBe('fabric-api');
      expect(meta.name).toBe('Fabric API');
      expect(meta.version).toBe('0.92.0+1.20.4');
      expect(meta.minecraftVersionRequirement).toBe('~1.20.4');
      expect(meta.loaderVersionRequirement).toBe('>=0.15.0');
      expect(meta.dependencies.some((d) => d.id === 'minecraft')).toBe(true);
      expect(meta.dependencies.some((d) => d.id === 'optifine' && d.kind === 'breaks')).toBe(true);
    });

    it('should read and normalize Quilt metadata from quilt.mod.json', async () => {
      const jar = createMockJar({
        'quilt.mod.json': JSON.stringify({
          schema_version: 1,
          quilt_loader: {
            group: 'org.quiltmc',
            id: 'quilt-standard-libraries',
            version: '6.0.0+1.20.4',
            metadata: {
              name: 'Quilt Standard Libraries',
              description: 'Standard library collection for Quilt',
              contributors: { QuiltMC: 'Owner' },
            },
            depends: [
              { id: 'quilt_loader', versions: '>=0.23.0' },
              { id: 'minecraft', versions: '1.20.4' },
            ],
            breaks: [
              { id: 'fabric-api', versions: '<0.90.0' },
            ],
          },
        }),
      });

      const filePath = path.join(tempDir, 'qsl.jar');
      fs.writeFileSync(filePath, jar);

      const meta = await ModMetadataReader.readMetadata(filePath);
      expect(meta.loader).toBe('quilt');
      expect(meta.id).toBe('quilt-standard-libraries');
      expect(meta.name).toBe('Quilt Standard Libraries');
      expect(meta.version).toBe('6.0.0+1.20.4');
      expect(meta.minecraftVersionRequirement).toBe('1.20.4');
      expect(meta.loaderVersionRequirement).toBe('>=0.23.0');
      expect(meta.dependencies.some((d) => d.id === 'fabric-api' && d.kind === 'breaks')).toBe(true);
    });

    it('should read and normalize NeoForge metadata from META-INF/neoforge.mods.toml', async () => {
      const jar = createMockJar({
        'META-INF/neoforge.mods.toml': `
modLoader="neoforge"
loaderVersion="[20.4,)"

[[mods]]
modId="jade"
version="13.2.0"
displayName="Jade"
description="Information HUD"
authors=["Snownee"]

[[dependencies.jade]]
modId="minecraft"
type="required"
versionRange="[1.20.4, 1.20.5)"
`,
      });

      const filePath = path.join(tempDir, 'jade-neoforge.jar');
      fs.writeFileSync(filePath, jar);

      const meta = await ModMetadataReader.readMetadata(filePath);
      expect(meta.loader).toBe('neoforge');
      expect(meta.id).toBe('jade');
      expect(meta.name).toBe('Jade');
      expect(meta.version).toBe('13.2.0');
      expect(meta.minecraftVersionRequirement).toBe('[1.20.4, 1.20.5)');
      expect(meta.dependencies.some((d) => d.id === 'minecraft')).toBe(true);
    });

    it('should read and normalize Forge metadata from META-INF/mods.toml', async () => {
      const jar = createMockJar({
        'META-INF/mods.toml': `
modLoader="javafml"
loaderVersion="[49,)"

[[mods]]
modId="journeymap"
version="5.9.18"
displayName="JourneyMap"
description="Real-time mapping in game"
authors="techbrew"

[[dependencies.journeymap]]
modId="forge"
mandatory=true
versionRange="[49.0.0,)"
`,
      });

      const filePath = path.join(tempDir, 'journeymap-forge.jar');
      fs.writeFileSync(filePath, jar);

      const meta = await ModMetadataReader.readMetadata(filePath);
      expect(meta.loader).toBe('forge');
      expect(meta.id).toBe('journeymap');
      expect(meta.name).toBe('JourneyMap');
      expect(meta.version).toBe('5.9.18');
      expect(meta.dependencies.some((d) => d.id === 'forge')).toBe(true);
    });

    it('should fallback cleanly on JARs with unknown metadata', async () => {
      const jar = createMockJar({
        'com/example/MyClass.class': Buffer.from([0xca, 0xfe, 0xba, 0xbe]),
      });

      const filePath = path.join(tempDir, 'custom-utility.jar');
      fs.writeFileSync(filePath, jar);

      const meta = await ModMetadataReader.readMetadata(filePath);
      expect(meta.loader).toBe('unknown');
      expect(meta.name).toBe('custom-utility.jar');
      expect(meta.rawSource).toBe('unknown');
    });
  });

  // =========================================================================
  // 4. ModCompatibilityChecker & Dependencies
  // =========================================================================
  describe('ModCompatibilityChecker & Dependency Analyzer', () => {
    const sampleFabricMod: UniversalModMetadata = {
      id: 'sodium',
      name: 'Sodium',
      version: '0.5.8',
      description: 'Renderer',
      authors: ['JellySquid'],
      loader: 'fabric',
      minecraftVersionRequirement: '1.20.4',
      loaderVersionRequirement: '>=0.15.0',
      dependencies: [
        { id: 'fabricloader', mandatory: true, kind: 'depends' },
        { id: 'fabric-api', mandatory: true, kind: 'depends' },
      ],
      rawSource: 'fabric.mod.json',
    };

    it('should mark mod as Compatible when loader and MC version match', () => {
      const result = ModCompatibilityChecker.checkCompatibility(sampleFabricMod, {
        loader: 'fabric',
        minecraftVersion: '1.20.4',
        loaderVersion: '0.15.7',
      });
      expect(result.status).toBe('Compatible');
    });

    it('should detect Quilt running Fabric mods as Possibly Compatible', () => {
      const result = ModCompatibilityChecker.checkCompatibility(sampleFabricMod, {
        loader: 'quilt',
        minecraftVersion: '1.20.4',
        loaderVersion: '0.23.1',
      });
      expect(result.status).toBe('Possibly Compatible');
      expect(result.reason).toContain('kompatybilności QuiltMC');
    });

    it('should mark mod as Incompatible when loader does not match (e.g. Fabric mod on Forge)', () => {
      const result = ModCompatibilityChecker.checkCompatibility(sampleFabricMod, {
        loader: 'forge',
        minecraftVersion: '1.20.4',
      });
      expect(result.status).toBe('Incompatible');
    });

    it('should detect missing dependencies', () => {
      const installedMods: UniversalModMetadata[] = []; // fabric-api is missing

      const depResult = ModDependencyAnalyzer.analyzeDependencies(
        sampleFabricMod,
        installedMods,
        { loader: 'fabric', minecraftVersion: '1.20.4', loaderVersion: '0.15.7' }
      );
      expect(depResult.missingDependencies).toContain('fabric-api');
      expect(depResult.conflictingMods).toHaveLength(0);
    });

    it('should detect when required dependencies are satisfied', () => {
      const installedMods: UniversalModMetadata[] = [
        {
          id: 'fabric-api',
          name: 'Fabric API',
          version: '0.92.0',
          description: '',
          authors: [],
          loader: 'fabric',
          dependencies: [],
          rawSource: 'fabric.mod.json',
        },
      ];

      const depResult = ModDependencyAnalyzer.analyzeDependencies(
        sampleFabricMod,
        installedMods,
        { loader: 'fabric', minecraftVersion: '1.20.4', loaderVersion: '0.15.7' }
      );
      expect(depResult.missingDependencies).toHaveLength(0);
    });

    it('should detect conflicting mods (breaks/conflicts)', () => {
      const modWithConflict: UniversalModMetadata = {
        id: 'optifabric',
        name: 'OptiFabric',
        version: '1.14.3',
        description: '',
        authors: [],
        loader: 'fabric',
        dependencies: [
          { id: 'sodium', mandatory: false, kind: 'breaks' },
        ],
        rawSource: 'fabric.mod.json',
      };

      const installedMods: UniversalModMetadata[] = [sampleFabricMod];

      const depResult = ModDependencyAnalyzer.analyzeDependencies(
        modWithConflict,
        installedMods,
        { loader: 'fabric', minecraftVersion: '1.20.4' }
      );
      expect(depResult.conflictingMods[0].toLowerCase()).toContain('sodium');
    });
  });

  // =========================================================================
  // 5. ModFileManager & ModRepository (Enabling, Disabling, Deleting)
  // =========================================================================
  describe('ModFileManager & ModRepository', () => {
    let instanceId: string;

    beforeEach(async () => {
      const inst = await InstallationRepository.create({
        name: 'Inst 1',
        minecraftVersion: '1.20.4',
        loader: 'fabric',
        allocatedRamMb: 4096,
      });
      instanceId = inst.id;
      // Ensure directory structure
      await ModFileManager.ensureDirectories(instanceId);
    });

    it('should compute accurate SHA-256 and copy files safely', async () => {
      const jarBuffer = createMockJar({ 'test.txt': 'hello zombek' });
      const sourceFile = path.join(tempDir, 'test-mod.jar');
      fs.writeFileSync(sourceFile, jarBuffer);

      const hash = await ModFileManager.calculateSha256(sourceFile);
      expect(hash).toHaveLength(64);

      const copyRes = await ModFileManager.copyModFile(sourceFile, instanceId, false);
      expect(fs.existsSync(copyRes.destinationPath)).toBe(true);
      expect(copyRes.fileName).toBe('test-mod.jar');
    });

    it('should enable and disable mods by moving between mods/ and disabled-mods/', async () => {
      const jarBuffer = createMockJar({
        'fabric.mod.json': JSON.stringify({ id: 'togglemod', name: 'Toggle Mod', version: '1.0' }),
      });
      const sourceFile = path.join(tempDir, 'toggle-mod.jar');
      fs.writeFileSync(sourceFile, jarBuffer);

      await ModFileManager.copyModFile(sourceFile, instanceId, false);

      const modsDir = EnginePaths.getInstanceModsDir(instanceId);
      const disabledDir = EnginePaths.getInstanceDisabledModsDir(instanceId);
      expect(fs.existsSync(path.join(modsDir, 'toggle-mod.jar'))).toBe(true);

      // Disable: moves to disabled-mods/
      const disabledMod = await ModRepository.toggleMod(instanceId, 'toggle-mod.jar', false);
      expect(disabledMod.enabled).toBe(false);
      expect(fs.existsSync(path.join(modsDir, 'toggle-mod.jar'))).toBe(false);
      expect(fs.existsSync(path.join(disabledDir, 'toggle-mod.jar'))).toBe(true);

      // Enable: moves back to mods/
      const enabledMod = await ModRepository.toggleMod(instanceId, 'toggle-mod.jar', true);
      expect(enabledMod.enabled).toBe(true);
      expect(fs.existsSync(path.join(modsDir, 'toggle-mod.jar'))).toBe(true);
      expect(fs.existsSync(path.join(disabledDir, 'toggle-mod.jar'))).toBe(false);
    });

    it('should delete mod permanently from disk', async () => {
      const jarBuffer = createMockJar({ 'dummy.txt': 'data' });
      const sourceFile = path.join(tempDir, 'to-delete.jar');
      fs.writeFileSync(sourceFile, jarBuffer);

      await ModFileManager.copyModFile(sourceFile, instanceId, false);

      const ok = await ModRepository.deleteMod(instanceId, 'to-delete.jar');
      expect(ok).toBe(true);

      const modsDir = EnginePaths.getInstanceModsDir(instanceId);
      expect(fs.existsSync(path.join(modsDir, 'to-delete.jar'))).toBe(false);
    });
  });

  // =========================================================================
  // 6. ModInstallationService & Duplicate Detection
  // =========================================================================
  describe('ModInstallationService', () => {
    let instanceId: string;

    beforeEach(async () => {
      const inst = await InstallationRepository.create({
        name: 'Validation Instance',
        minecraftVersion: '1.20.4',
        loader: 'fabric',
        allocatedRamMb: 4096,
      });
      instanceId = inst.id;
      await ModFileManager.ensureDirectories(instanceId);
    });

    it('should validate candidate JAR and detect duplicates by hash or filename', async () => {
      const jarBuffer = createMockJar({
        'fabric.mod.json': JSON.stringify({ id: 'valmod', name: 'Validation Mod', version: '1.0.0' }),
      });
      const sourceFile = path.join(tempDir, 'val-mod.jar');
      fs.writeFileSync(sourceFile, jarBuffer);

      // Pre-validation before installation
      const firstVal = await ModInstallationService.validateCandidate(sourceFile, instanceId);
      expect(firstVal.valid).toBe(true);
      expect(firstVal.metadata.id).toBe('valmod');
      expect(firstVal.isDuplicate).toBe(false);

      // Install it
      const installRes = await ModInstallationService.installMod(sourceFile, instanceId, false);
      expect(installRes.success).toBe(true);

      // Validate again -> should detect duplicate
      const secondVal = await ModInstallationService.validateCandidate(sourceFile, instanceId);
      expect(secondVal.isDuplicate).toBe(true);
      expect(secondVal.duplicateFileName).toBe('val-mod.jar');

      // Attempting to install without overwrite should return error
      const dupFail = await ModInstallationService.installMod(sourceFile, instanceId, false);
      expect(dupFail.success).toBe(false);
      expect(dupFail.error).toContain('już zainstalowany');

      // Overwriting should succeed
      const overwriteRes = await ModInstallationService.installMod(sourceFile, instanceId, true);
      expect(overwriteRes.success).toBe(true);
      expect(overwriteRes.mod?.fileName).toBe('val-mod.jar');
    });
  });

  // =========================================================================
  // 7. ModpackRepository & ModpackProfileManager
  // =========================================================================
  describe('Modpack Profiles & Duplication', () => {
    it('should persist, read, duplicate, and delete modpack records', async () => {
      const created = await ModpackRepository.create({
        name: 'Festive Fabric Pack',
        description: 'Paczka modów świątecznych',
        minecraftVersion: '1.20.4',
        loader: 'fabric',
      });

      expect(created.id).toBeDefined();
      expect(created.name).toBe('Festive Fabric Pack');

      const all = await ModpackRepository.getAll();
      expect(all.some((p) => p.id === created.id)).toBe(true);

      // Duplicate pack -> creates an independent copy
      const copy = await ModpackProfileManager.duplicateModpack(created.id, 'Festive Fabric Pack (Kopia 1)');
      expect(copy.id).not.toBe(created.id);
      expect(copy.name).toBe('Festive Fabric Pack (Kopia 1)');
      expect(copy.minecraftVersion).toBe('1.20.4');

      // Delete original pack
      const deleted = await ModpackRepository.delete(created.id);
      expect(deleted).toBe(true);

      const afterDelete = await ModpackRepository.getAll();
      expect(afterDelete.some((p) => p.id === created.id)).toBe(false);
      expect(afterDelete.some((p) => p.id === copy.id)).toBe(true);
    });

    it('should apply modpack profile to an instance and create modpack.json manifest', async () => {
      const inst = await InstallationRepository.create({
        name: 'Apply Test Target',
        minecraftVersion: '1.20.4',
        loader: 'fabric',
        allocatedRamMb: 4096,
      });
      const targetInst = inst.id;
      await ModFileManager.ensureDirectories(targetInst);

      const pack = await ModpackRepository.create({
        name: 'Apply Test Pack',
        description: 'Test profilu',
        minecraftVersion: '1.20.4',
        loader: 'fabric',
      });

      const success = await ModpackProfileManager.applyModpackToInstance(pack.id, targetInst);
      expect(success).toBe(true);

      const manifestPath = path.join(EnginePaths.getInstanceDir(targetInst), 'modpack.json');
      expect(fs.existsSync(manifestPath)).toBe(true);
      const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf-8'));
      expect(manifest.modpackId).toBe(pack.id);
      expect(manifest.name).toBe('Apply Test Pack');
    });
  });

  // =========================================================================
  // 8. Instance Isolation
  // =========================================================================
  describe('Instance Isolation', () => {
    it('should strictly isolate mod files between two separate instances', async () => {
      const instA = (
        await InstallationRepository.create({
          name: 'Instance Alpha',
          minecraftVersion: '1.20.4',
          loader: 'fabric',
          allocatedRamMb: 4096,
        })
      ).id;

      const instB = (
        await InstallationRepository.create({
          name: 'Instance Beta',
          minecraftVersion: '1.20.4',
          loader: 'fabric',
          allocatedRamMb: 4096,
        })
      ).id;

      await ModFileManager.ensureDirectories(instA);
      await ModFileManager.ensureDirectories(instB);

      const jarA = createMockJar({ 'mod.txt': 'Mod for Alpha' });
      const jarB = createMockJar({ 'mod.txt': 'Mod for Beta' });

      const fileA = path.join(tempDir, 'mod-alpha.jar');
      const fileB = path.join(tempDir, 'mod-beta.jar');
      fs.writeFileSync(fileA, jarA);
      fs.writeFileSync(fileB, jarB);

      await ModInstallationService.installMod(fileA, instA, false);
      await ModInstallationService.installMod(fileB, instB, false);

      const modsA = await ModRepository.getModsForInstance(instA);
      const modsB = await ModRepository.getModsForInstance(instB);

      expect(modsA.some((m) => m.fileName === 'mod-alpha.jar')).toBe(true);
      expect(modsA.some((m) => m.fileName === 'mod-beta.jar')).toBe(false);

      expect(modsB.some((m) => m.fileName === 'mod-beta.jar')).toBe(true);
      expect(modsB.some((m) => m.fileName === 'mod-alpha.jar')).toBe(false);
    });
  });
});
