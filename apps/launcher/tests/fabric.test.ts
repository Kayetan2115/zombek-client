import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import * as path from 'node:path';
import * as fs from 'node:fs';
import * as os from 'node:os';
import { FabricMetaService, FABRIC_META_BASE_URL } from '../electron/loaders/FabricMetaService';
import { FabricLoaderAdapter } from '../electron/loaders/FabricLoaderAdapter';
import { EnginePaths } from '../electron/engine/paths';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import { GameArgumentsBuilder } from '../electron/engine/GameArgumentsBuilder';
import { GameInstaller } from '../electron/engine/GameInstaller';
import { VersionManifestService } from '../electron/engine/VersionManifestService';
import type { MojangVersionPackage } from '../electron/engine/types';

describe('Fabric Loader - FabricMetaService', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-fabric-test-'));
    EnginePaths.setRootDir(tempDir);
    EnginePaths.ensureAllBaseDirs();
    FabricMetaService.clearMemoryCache();
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
    FabricMetaService.clearMemoryCache();
    vi.restoreAllMocks();
  });

  it('should fetch and disk-cache compatible game versions from Fabric Meta API', async () => {
    const mockGames = [
      { version: '1.21.1', stable: true },
      { version: '1.20.4', stable: true },
      { version: '24w40a', stable: false },
    ];

    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any) => {
      expect(String(url)).toBe(`${FABRIC_META_BASE_URL}/versions/game`);
      return {
        ok: true,
        status: 200,
        json: async () => mockGames,
      } as any;
    });

    const games = await FabricMetaService.getGameVersions(true);
    expect(games).toHaveLength(3);
    expect(games[0].version).toBe('1.21.1');
    expect(games[0].stable).toBe(true);

    // Verify written to disk cache
    const cacheFile = FabricMetaService.getCacheFilePath();
    expect(fs.existsSync(cacheFile)).toBe(true);
    const cached = JSON.parse(fs.readFileSync(cacheFile, 'utf-8'));
    expect(cached.gameVersions).toHaveLength(3);
  });

  it('should fetch compatible Fabric Loader versions for a specific game version', async () => {
    const mockLoaders = [
      {
        loader: {
          separator: '.',
          build: 10,
          maven: 'net.fabricmc:fabric-loader:0.16.10',
          version: '0.16.10',
          stable: true,
        },
        intermediary: {
          maven: 'net.fabricmc:intermediary:1.20.4',
          version: '1.20.4',
          stable: true,
        },
      },
      {
        loader: {
          separator: '.',
          build: 9,
          maven: 'net.fabricmc:fabric-loader:0.16.9',
          version: '0.16.9',
          stable: false,
        },
      },
    ];

    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any) => {
      expect(String(url)).toContain('/versions/loader/1.20.4');
      return {
        ok: true,
        status: 200,
        json: async () => mockLoaders,
      } as any;
    });

    const loaders = await FabricMetaService.getLoaderVersions('1.20.4', true);
    expect(loaders).toHaveLength(2);
    expect(loaders[0].version).toBe('0.16.10');
    expect(loaders[0].stable).toBe(true);
    expect(loaders[1].version).toBe('0.16.9');
    expect(loaders[1].stable).toBe(false);
  });

  it('should fallback to disk cache when offline or network fails', async () => {
    const cacheFile = FabricMetaService.getCacheFilePath();
    const mockCachedData = {
      gameVersions: [{ version: '1.20.4', stable: true }],
      loaderVersions: {
        '1.20.4': [
          {
            separator: '.',
            build: 10,
            maven: 'net.fabricmc:fabric-loader:0.16.10',
            version: '0.16.10',
            stable: true,
          },
        ],
      },
    };
    fs.writeFileSync(cacheFile, JSON.stringify(mockCachedData), 'utf-8');

    // Mock network failure
    vi.spyOn(globalThis, 'fetch').mockImplementation(async () => {
      throw new Error('ENOTFOUND meta.fabricmc.net (Offline test)');
    });

    const games = await FabricMetaService.getGameVersions(false);
    expect(games).toHaveLength(1);
    expect(games[0].version).toBe('1.20.4');

    const loaders = await FabricMetaService.getLoaderVersions('1.20.4', false);
    expect(loaders).toHaveLength(1);
    expect(loaders[0].version).toBe('0.16.10');
  });

  it('should fetch and save official Fabric profile JSON', async () => {
    const mockProfile = {
      id: 'fabric-loader-0.16.10-1.20.4',
      inheritsFrom: '1.20.4',
      type: 'release',
      mainClass: 'net.fabricmc.loader.impl.launch.knot.KnotClient',
      arguments: {
        game: [],
        jvm: ['-DFabricMcEmu= net.minecraft.client.main.Main '],
      },
      libraries: [
        {
          name: 'net.fabricmc:fabric-loader:0.16.10',
          url: 'https://maven.fabricmc.net/',
          sha1: 'abcdef1234567890',
          size: 150000,
        },
      ],
    };

    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any) => {
      expect(String(url)).toContain('/loader/1.20.4/0.16.10/profile/json');
      return {
        ok: true,
        status: 200,
        json: async () => mockProfile,
      } as any;
    });

    const profile = await FabricMetaService.getProfileJson('1.20.4', '0.16.10');
    expect(profile.id).toBe('fabric-loader-0.16.10-1.20.4');
    expect(profile.mainClass).toBe('net.fabricmc.loader.impl.launch.knot.KnotClient');

    const profilePath = path.join(
      EnginePaths.getVersionsDir(),
      'fabric-loader-0.16.10-1.20.4',
      'fabric-loader-0.16.10-1.20.4.json'
    );
    expect(fs.existsSync(profilePath)).toBe(true);
  });
});

describe('Fabric Loader - FabricLoaderAdapter & Profile Inheritance', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-fabric-adapter-test-'));
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

  it('should correctly merge Vanilla parent package with Fabric profile', async () => {
    const vanillaPackage: MojangVersionPackage = {
      id: '1.20.4',
      type: 'release',
      time: '2023-12-07T11:00:00+00:00',
      releaseTime: '2023-12-07T11:00:00+00:00',
      mainClass: 'net.minecraft.client.main.Main',
      arguments: {
        game: ['--username', '${auth_player_name}'],
        jvm: ['-Djava.library.path=${natives_directory}'],
      },
      downloads: {
        client: {
          sha1: '1111222233334444555566667777888899990000',
          size: 25000000,
          url: 'https://piston-data.mojang.com/v1/objects/vanilla-client.jar',
        },
      },
      libraries: [
        {
          name: 'com.mojang:authlib:6.0.52',
          downloads: {
            artifact: {
              sha1: 'aaaa',
              size: 50000,
              url: 'https://libraries.minecraft.net/authlib.jar',
              path: 'com/mojang/authlib/6.0.52/authlib-6.0.52.jar',
            },
          },
        },
      ],
      assetIndex: {
        id: '16',
        sha1: 'bbbb',
        size: 1000,
        totalSize: 500000,
        url: 'https://piston-meta.mojang.com/v1/packages/assetIndex.json',
      },
      assets: '16',
      javaVersion: {
        component: 'java-runtime-gamma',
        majorVersion: 17,
      },
    };

    const fabricProfileJson = {
      id: 'fabric-loader-0.16.10-1.20.4',
      inheritsFrom: '1.20.4',
      mainClass: 'net.fabricmc.loader.impl.launch.knot.KnotClient',
      arguments: {
        game: [],
        jvm: ['-DFabricMcEmu= net.minecraft.client.main.Main '],
      },
      libraries: [
        {
          name: 'net.fabricmc:fabric-loader:0.16.10',
          url: 'https://maven.fabricmc.net/',
          sha1: 'fabricloaderhash',
          size: 140000,
        },
        {
          name: 'org.ow2.asm:asm:9.6',
          url: 'https://maven.fabricmc.net/',
          sha1: 'asmhash',
          size: 120000,
        },
      ],
    };

    vi.spyOn(VersionManifestService, 'getVersionPackage').mockResolvedValue(vanillaPackage);
    vi.spyOn(FabricMetaService, 'getProfileJson').mockResolvedValue(fabricProfileJson as any);

    const adapter = FabricLoaderAdapter.getInstance();
    const resolved = await adapter.resolveProfile('1.20.4', '0.16.10');

    expect(resolved.id).toBe('fabric-loader-0.16.10-1.20.4');
    expect(resolved.inheritsFrom).toBe('1.20.4');
    expect(resolved.mainClass).toBe('net.fabricmc.loader.impl.launch.knot.KnotClient');

    // Libraries merged: 1 vanilla + 2 fabric = 3 libraries
    expect(resolved.libraries).toHaveLength(3);
    expect(resolved.libraries.some((l) => l.name === 'com.mojang:authlib:6.0.52')).toBe(true);
    expect(resolved.libraries.some((l) => l.name === 'net.fabricmc:fabric-loader:0.16.10')).toBe(true);

    // JVM arguments merged: includes Vanilla + Fabric JVM args
    expect(resolved.arguments?.jvm).toContain('-DFabricMcEmu= net.minecraft.client.main.Main ');

    // Vanilla assets and java version preserved
    expect(resolved.assetIndex.id).toBe('16');
    expect(resolved.javaVersion?.majorVersion).toBe(17);
  });

  it('should isolate instance folder and create mods, config, saves, resourcepacks, screenshots, natives', async () => {
    const inst = await InstallationRepository.create({
      name: 'Fabric Survival Test',
      minecraftVersion: '1.20.4',
      loader: 'fabric',
      loaderVersion: '0.16.10',
      allocatedRamMb: 4096,
    });

    expect(inst.loader).toBe('fabric');
    expect(inst.loaderVersion).toBe('0.16.10');

    const instanceDir = inst.instanceDir;
    expect(fs.existsSync(path.join(instanceDir, 'mods'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'config'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'saves'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'resourcepacks'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'screenshots'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'natives'))).toBe(true);

    // Verify adapter.install ensures all directories
    const adapter = FabricLoaderAdapter.getInstance();
    vi.spyOn(adapter, 'resolveProfile').mockResolvedValue({
      id: 'fabric-loader-0.16.10-1.20.4',
      inheritsFrom: '1.20.4',
      mainClass: 'net.fabricmc.loader.impl.launch.knot.KnotClient',
      downloads: { client: { sha1: '1', size: 1, url: 'http://example.com' } },
      libraries: [],
      arguments: {},
      assetIndex: { id: '16', sha1: '', size: 0, totalSize: 0, url: '' },
      assets: '16',
      type: 'release',
      time: '',
      releaseTime: '',
    });

    const result = await adapter.install(inst, '0.16.10');
    expect(result.versionId).toBe('fabric-loader-0.16.10-1.20.4');

    const updatedInst = await InstallationRepository.getById(inst.id);
    expect(updatedInst?.status).toBe('ready');
    expect(updatedInst?.resolvedVersionId).toBe('fabric-loader-0.16.10-1.20.4');
  });
});

describe('Fabric Loader - GameArgumentsBuilder & Execution Readiness', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-fabric-args-test-'));
    EnginePaths.setRootDir(tempDir);
    EnginePaths.ensureAllBaseDirs();
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
  });

  it('should build command line arguments with KnotClient and Vanilla client jar', () => {
    const versionsDir = EnginePaths.getVersionsDir();
    const librariesDir = EnginePaths.getLibrariesDir();
    const instanceDir = path.join(tempDir, 'instances', 'fabric-inst');
    const nativesDir = path.join(instanceDir, 'natives');

    // Create dummy client jar in base versions/1.20.4/1.20.4.jar
    const vanillaDir = path.join(versionsDir, '1.20.4');
    fs.mkdirSync(vanillaDir, { recursive: true });
    const clientJarPath = path.join(vanillaDir, '1.20.4.jar');
    fs.writeFileSync(clientJarPath, 'dummy-jar');

    // Create dummy fabric library
    const fabricLibRel = GameInstaller.getMavenPath('net.fabricmc:fabric-loader:0.16.10');
    const fabricLibFull = path.join(librariesDir, fabricLibRel);
    fs.mkdirSync(path.dirname(fabricLibFull), { recursive: true });
    fs.writeFileSync(fabricLibFull, 'dummy-fabric-lib');

    const fabricPackage: MojangVersionPackage = {
      id: 'fabric-loader-0.16.10-1.20.4',
      inheritsFrom: '1.20.4',
      mainClass: 'net.fabricmc.loader.impl.launch.knot.KnotClient',
      arguments: {
        jvm: [
          '-DFabricMcEmu= net.minecraft.client.main.Main ',
          '-cp',
          '${classpath}',
        ],
        game: ['--username', '${auth_player_name}', '--gameDir', '${game_directory}'],
      },
      downloads: {
        client: { sha1: 'abc', size: 100, url: 'http://example.com' },
      },
      libraries: [
        {
          name: 'net.fabricmc:fabric-loader:0.16.10',
          url: 'https://maven.fabricmc.net/',
        },
      ],
      assetIndex: { id: '16', sha1: '', size: 0, totalSize: 0, url: '' },
      assets: '16',
      type: 'release',
      time: '',
      releaseTime: '',
    };

    const args = GameArgumentsBuilder.buildArguments({
      versionPackage: fabricPackage,
      instanceDir,
      assetsDir: EnginePaths.getAssetsDir(),
      librariesDir,
      versionsDir,
      nativesDir,
      session: {
        username: 'ZombekFabricUser',
        uuid: '00000000-0000-0000-0000-000000000000',
        accessToken: 'mock_token',
        userType: 'msa',
      },
      minRamMb: 2048,
      maxRamMb: 4096,
    });

    // 1. KnotClient is mainClass
    expect(args).toContain('net.fabricmc.loader.impl.launch.knot.KnotClient');

    // 2. Classpath contains both Vanilla jar and Fabric library
    const cpIndex = args.indexOf('-cp');
    expect(cpIndex).toBeGreaterThan(-1);
    const cpValue = args[cpIndex + 1];
    expect(cpValue).toContain(clientJarPath);
    expect(cpValue).toContain(fabricLibFull);

    // 3. Fabric JVM arguments are present
    expect(args).toContain('-DFabricMcEmu= net.minecraft.client.main.Main ');

    // 4. Memory settings are applied
    expect(args).toContain('-Xms2048M');
    expect(args).toContain('-Xmx4096M');

    // 5. Game directory points to isolated instance
    const gameDirIndex = args.indexOf('--gameDir');
    expect(gameDirIndex).toBeGreaterThan(-1);
    expect(args[gameDirIndex + 1]).toBe(instanceDir);
  });

  it('should format maven repository library URLs correctly for Fabric', () => {
    const relPath = GameInstaller.getMavenPath('org.ow2.asm:asm:9.6');
    expect(relPath).toBe(path.normalize('org/ow2/asm/asm/9.6/asm-9.6.jar').replace(/\\/g, '/'));

    const intermediaryRel = GameInstaller.getMavenPath('net.fabricmc:intermediary:1.20.4');
    expect(intermediaryRel).toBe(path.normalize('net/fabricmc/intermediary/1.20.4/intermediary-1.20.4.jar').replace(/\\/g, '/'));
  });
});
