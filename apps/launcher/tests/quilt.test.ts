import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import * as path from 'node:path';
import * as fs from 'node:fs';
import * as os from 'node:os';
import {
  QuiltMetaService,
  QUILT_META_BASE_URL,
  QUILT_USER_AGENT,
} from '../electron/loaders/QuiltMetaService';
import { QuiltLoaderAdapter } from '../electron/loaders/QuiltLoaderAdapter';
import { EnginePaths } from '../electron/engine/paths';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import { GameArgumentsBuilder } from '../electron/engine/GameArgumentsBuilder';
import { VersionManifestService } from '../electron/engine/VersionManifestService';
import type { MojangVersionPackage } from '../electron/engine/types';
import type { ModCompatibilityTarget } from '@zombek/shared-types';

describe('Quilt Loader - QuiltMetaService', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-quilt-test-'));
    EnginePaths.setRootDir(tempDir);
    EnginePaths.ensureAllBaseDirs();
    QuiltMetaService.clearMemoryCache();
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
    QuiltMetaService.clearMemoryCache();
    vi.restoreAllMocks();
  });

  it('should fetch and disk-cache compatible game versions with required User-Agent', async () => {
    const mockGames = [
      { version: '1.20.4', stable: true },
      { version: '1.20.1', stable: true },
      { version: '24w04a', stable: false },
    ];

    let capturedHeaders: any;
    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any, init?: any) => {
      expect(String(url)).toBe(`${QUILT_META_BASE_URL}/versions/game`);
      capturedHeaders = init?.headers;
      return {
        ok: true,
        status: 200,
        json: async () => mockGames,
      } as any;
    });

    const games = await QuiltMetaService.getGameVersions(true);
    expect(games).toHaveLength(3);
    expect(games[0].version).toBe('1.20.4');
    expect(games[0].stable).toBe(true);
    expect(capturedHeaders?.['User-Agent']).toBe(QUILT_USER_AGENT);

    // Verify written to disk cache
    const cacheFile = QuiltMetaService.getCacheFilePath();
    expect(fs.existsSync(cacheFile)).toBe(true);
    const cached = JSON.parse(fs.readFileSync(cacheFile, 'utf-8'));
    expect(cached.gameVersions).toHaveLength(3);
  });

  it('should return cached game versions without repeating network call', async () => {
    const mockGames = [{ version: '1.20.4', stable: true }];
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async () => {
      return {
        ok: true,
        status: 200,
        json: async () => mockGames,
      } as any;
    });

    const first = await QuiltMetaService.getGameVersions();
    const second = await QuiltMetaService.getGameVersions();
    expect(first).toEqual(second);
    expect(fetchSpy).toHaveBeenCalledTimes(1);
  });

  it('should fetch compatible Quilt Loader versions for a specific Minecraft version', async () => {
    const mockLoaders = [
      {
        loader: {
          separator: '.',
          build: 1,
          maven: 'org.quiltmc:quilt-loader:0.27.1-beta.1',
          version: '0.27.1-beta.1',
          stable: true,
        },
      },
      {
        loader: {
          separator: '.',
          build: 2,
          maven: 'org.quiltmc:quilt-loader:0.26.0',
          version: '0.26.0',
          stable: true,
        },
      },
    ];

    let capturedHeaders: any;
    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any, init?: any) => {
      expect(String(url)).toBe(`${QUILT_META_BASE_URL}/versions/loader/1.20.4`);
      capturedHeaders = init?.headers;
      return {
        ok: true,
        status: 200,
        json: async () => mockLoaders,
      } as any;
    });

    const loaders = await QuiltMetaService.getLoaderVersions('1.20.4', true);
    expect(loaders).toHaveLength(2);
    expect(loaders[0].version).toBe('0.27.1-beta.1');
    expect(loaders[0].maven).toContain('org.quiltmc:quilt-loader');
    expect(capturedHeaders?.['User-Agent']).toBe(QUILT_USER_AGENT);
  });

  it('should fetch official Quilt profile JSON and cache it locally', async () => {
    const mockProfile = {
      id: 'quilt-loader-0.27.1-beta.1-1.20.4',
      inheritsFrom: '1.20.4',
      type: 'release',
      mainClass: 'org.quiltmc.loader.impl.launch.knot.KnotClient',
      arguments: {
        game: ['--quiltVersion', '0.27.1-beta.1'],
        jvm: ['-Dquilt.development=false'],
      },
      libraries: [
        {
          name: 'org.quiltmc:quilt-loader:0.27.1-beta.1',
          downloads: {
            artifact: {
              path: 'org/quiltmc/quilt-loader/0.27.1-beta.1/quilt-loader-0.27.1-beta.1.jar',
              sha1: '1234567890abcdef',
              size: 2500000,
              url: 'https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-loader/0.27.1-beta.1/quilt-loader-0.27.1-beta.1.jar',
            },
          },
        },
      ],
    } as unknown as MojangVersionPackage;

    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any) => {
      expect(String(url)).toBe(
        `${QUILT_META_BASE_URL}/versions/loader/1.20.4/0.27.1-beta.1/profile/json`
      );
      return {
        ok: true,
        status: 200,
        json: async () => mockProfile,
      } as any;
    });

    const profile = await QuiltMetaService.getProfileJson('1.20.4', '0.27.1-beta.1');
    expect(profile.id).toBe('quilt-loader-0.27.1-beta.1-1.20.4');
    expect(profile.mainClass).toBe('org.quiltmc.loader.impl.launch.knot.KnotClient');

    // Verify written to disk under versions/quilt-loader-0.27.1-beta.1-1.20.4/quilt-loader-0.27.1-beta.1-1.20.4.json
    const expectedPath = path.join(
      EnginePaths.getVersionsDir(),
      'quilt-loader-0.27.1-beta.1-1.20.4',
      'quilt-loader-0.27.1-beta.1-1.20.4.json'
    );
    expect(fs.existsSync(expectedPath)).toBe(true);
  });
});

describe('Quilt Loader - QuiltLoaderAdapter', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-quilt-adapter-test-'));
    EnginePaths.setRootDir(tempDir);
    EnginePaths.ensureAllBaseDirs();
    QuiltMetaService.clearMemoryCache();
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
    QuiltMetaService.clearMemoryCache();
    vi.restoreAllMocks();
  });

  it('should verify supported versions via QuiltMetaService', async () => {
    vi.spyOn(QuiltMetaService, 'getGameVersions').mockResolvedValue([
      { version: '1.20.4', stable: true },
      { version: '1.20.1', stable: true },
    ]);

    const adapter = QuiltLoaderAdapter.getInstance();
    expect(adapter.loaderType).toBe('quilt');
    expect(await adapter.isVersionSupported('1.20.4')).toBe(true);
    expect(await adapter.isVersionSupported('1.12.2')).toBe(false);
  });

  it('should resolve profile and merge Vanilla base with Quilt profile', async () => {
    const mockVanillaPackage = {
      id: '1.20.4',
      type: 'release',
      mainClass: 'net.minecraft.client.main.Main',
      arguments: {
        game: ['--username', '${auth_player_name}', '--version', '${version_name}'],
        jvm: ['-Djava.library.path=${natives_directory}'],
      },
      libraries: [
        {
          name: 'com.mojang:authlib:6.0.52',
          downloads: {
            artifact: {
              path: 'com/mojang/authlib/6.0.52/authlib-6.0.52.jar',
              sha1: 'aaa',
              size: 100,
              url: 'https://libraries.minecraft.net/com/mojang/authlib/6.0.52/authlib-6.0.52.jar',
            },
          },
        },
      ],
      assetIndex: {
        id: '1.20.4',
        sha1: 'asset-sha1',
        size: 1234,
        totalSize: 5678,
        url: 'https://piston-meta.mojang.com/assets/1.20.4.json',
      },
      downloads: {
        client: {
          sha1: 'client-sha',
          size: 1000000,
          url: 'https://piston-data.mojang.com/v1/objects/client.jar',
        },
      },
    } as unknown as MojangVersionPackage;

    const mockQuiltProfile = {
      id: 'quilt-loader-0.27.1-beta.1-1.20.4',
      inheritsFrom: '1.20.4',
      type: 'release',
      mainClass: 'org.quiltmc.loader.impl.launch.knot.KnotClient',
      arguments: {
        game: ['--quiltCustom'],
        jvm: ['-Dloader.quilt=true'],
      },
      libraries: [
        {
          name: 'org.quiltmc:quilt-loader:0.27.1-beta.1',
          downloads: {
            artifact: {
              path: 'org/quiltmc/quilt-loader/0.27.1-beta.1/quilt-loader-0.27.1-beta.1.jar',
              sha1: 'bbb',
              size: 500,
              url: 'https://maven.quiltmc.org/quilt-loader.jar',
            },
          },
        },
        {
          name: 'org.ow2.asm:asm:9.6',
          downloads: {
            artifact: {
              path: 'org/ow2/asm/asm/9.6/asm-9.6.jar',
              sha1: 'ccc',
              size: 200,
              url: 'https://maven.quiltmc.org/asm.jar',
            },
          },
        },
      ],
    } as unknown as MojangVersionPackage;

    vi.spyOn(VersionManifestService, 'getVersionPackage').mockResolvedValueOnce(mockVanillaPackage);
    vi.spyOn(QuiltMetaService, 'getProfileJson').mockResolvedValueOnce(mockQuiltProfile);

    const adapter = QuiltLoaderAdapter.getInstance();
    const resolved = await adapter.resolveProfile('1.20.4', '0.27.1-beta.1');

    expect(resolved.id).toBe('quilt-loader-0.27.1-beta.1-1.20.4');
    expect(resolved.mainClass).toBe('org.quiltmc.loader.impl.launch.knot.KnotClient');
    expect(resolved.inheritsFrom).toBe('1.20.4');

    // Deduplicated libraries count: 1 vanilla + 2 quilt = 3
    expect(resolved.libraries).toHaveLength(3);
    const libNames = resolved.libraries?.map((l) => l.name);
    expect(libNames).toContain('com.mojang:authlib:6.0.52');
    expect(libNames).toContain('org.quiltmc:quilt-loader:0.27.1-beta.1');
    expect(libNames).toContain('org.ow2.asm:asm:9.6');

    // Arguments merged
    expect(resolved.arguments?.jvm).toContain('-Djava.library.path=${natives_directory}');
    expect(resolved.arguments?.jvm).toContain('-Dloader.quilt=true');
    expect(resolved.arguments?.game).toContain('--quiltCustom');
  });

  it('MUST NOT auto-inject or require QSL (Quilt Standard Libraries) according to Dec 2025 deprecation policy', async () => {
    const mockVanillaPackage = {
      id: '1.20.4',
      type: 'release',
      mainClass: 'net.minecraft.client.main.Main',
      libraries: [{ name: 'com.mojang:authlib:6.0.52' }],
    } as unknown as MojangVersionPackage;

    const mockQuiltProfile = {
      id: 'quilt-loader-0.27.1-beta.1-1.20.4',
      inheritsFrom: '1.20.4',
      mainClass: 'org.quiltmc.loader.impl.launch.knot.KnotClient',
      libraries: [{ name: 'org.quiltmc:quilt-loader:0.27.1-beta.1' }],
    } as unknown as MojangVersionPackage;

    vi.spyOn(VersionManifestService, 'getVersionPackage').mockResolvedValueOnce(mockVanillaPackage);
    vi.spyOn(QuiltMetaService, 'getProfileJson').mockResolvedValueOnce(mockQuiltProfile);

    const adapter = QuiltLoaderAdapter.getInstance();
    const resolved = await adapter.resolveProfile('1.20.4', '0.27.1-beta.1');

    // Strict assertion: ensure no QSL libraries are automatically injected
    const allLibNames = resolved.libraries?.map((l) => l.name) || [];
    const hasQsl = allLibNames.some(
      (name) => name.includes('org.quiltmc.qsl') || name.includes('quilt-standard-libraries')
    );
    expect(hasQsl).toBe(false);
  });

  it('should install Quilt: create isolated directories and update instance repository', async () => {
    const mockVanillaPackage = {
      id: '1.20.4',
      type: 'release',
      mainClass: 'net.minecraft.client.main.Main',
      libraries: [],
    } as unknown as MojangVersionPackage;

    const mockQuiltProfile = {
      id: 'quilt-loader-0.27.1-beta.1-1.20.4',
      inheritsFrom: '1.20.4',
      mainClass: 'org.quiltmc.loader.impl.launch.knot.KnotClient',
      libraries: [],
    } as unknown as MojangVersionPackage;

    vi.spyOn(VersionManifestService, 'getVersionPackage').mockResolvedValue(mockVanillaPackage);
    vi.spyOn(QuiltMetaService, 'getProfileJson').mockResolvedValue(mockQuiltProfile);

    const instance = await InstallationRepository.create({
      name: 'Test Quilt Profile',
      minecraftVersion: '1.20.4',
      loader: 'quilt',
      loaderVersion: '0.27.1-beta.1',
      allocatedRamMb: 4096,
    });

    const adapter = QuiltLoaderAdapter.getInstance();
    const result = await adapter.install(instance, '0.27.1-beta.1');

    expect(result.versionId).toBe('quilt-loader-0.27.1-beta.1-1.20.4');

    // Verify subdirectories created in instances/<id>/
    const instanceDir = EnginePaths.getInstanceDir(instance.id);
    expect(fs.existsSync(path.join(instanceDir, 'saves'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'resourcepacks'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'config'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'mods'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'screenshots'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'natives'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'logs'))).toBe(true);

    // Verify repository update
    const updated = await InstallationRepository.getById(instance.id);
    expect(updated?.loader).toBe('quilt');
    expect(updated?.loaderVersion).toBe('0.27.1-beta.1');
    expect(updated?.resolvedVersionId).toBe('quilt-loader-0.27.1-beta.1-1.20.4');
    expect(updated?.status).toBe('ready');
  });

  it('should construct correct launch arguments using GameArgumentsBuilder for Quilt', async () => {
    const instance = await InstallationRepository.create({
      name: 'Launch Quilt Instance',
      minecraftVersion: '1.20.4',
      loader: 'quilt',
      loaderVersion: '0.27.1-beta.1',
      allocatedRamMb: 3072,
      customJvmArgs: '-XX:+UseG1GC',
    });

    const quiltLibPath = path.join(
      EnginePaths.getLibrariesDir(),
      'org',
      'quiltmc',
      'quilt-loader',
      '0.27.1-beta.1',
      'quilt-loader-0.27.1-beta.1.jar'
    );
    fs.mkdirSync(path.dirname(quiltLibPath), { recursive: true });
    fs.writeFileSync(quiltLibPath, 'dummy-jar');

    const clientJarPath = path.join(
      EnginePaths.getVersionsDir(),
      '1.20.4',
      '1.20.4.jar'
    );
    fs.mkdirSync(path.dirname(clientJarPath), { recursive: true });
    fs.writeFileSync(clientJarPath, 'dummy-client-jar');

    const versionPackage = {
      id: 'quilt-loader-0.27.1-beta.1-1.20.4',
      inheritsFrom: '1.20.4',
      type: 'release',
      mainClass: 'org.quiltmc.loader.impl.launch.knot.KnotClient',
      arguments: {
        game: [
          '--username',
          '${auth_player_name}',
          '--version',
          '${version_name}',
          '--gameDir',
          '${game_directory}',
          '--assetsDir',
          '${assets_root}',
          '--assetIndex',
          '${assets_index_name}',
          '--uuid',
          '${auth_uuid}',
          '--accessToken',
          '${auth_access_token}',
          '--userType',
          '${user_type}',
          '--versionType',
          '${version_type}',
        ],
        jvm: [
          '-Djava.library.path=${natives_directory}',
          '-Dminecraft.launcher.brand=${launcher_name}',
          '-Dminecraft.launcher.version=${launcher_version}',
          '-cp',
          '${classpath}',
        ],
      },
      libraries: [
        {
          name: 'org.quiltmc:quilt-loader:0.27.1-beta.1',
          downloads: {
            artifact: {
              path: 'org/quiltmc/quilt-loader/0.27.1-beta.1/quilt-loader-0.27.1-beta.1.jar',
              sha1: '123',
              size: 9,
              url: 'https://example.com/ql.jar',
            },
          },
        },
      ],
      assetIndex: {
        id: '1.20.4',
        sha1: 'dummy',
        size: 1,
        totalSize: 1,
        url: 'https://example.com/assets.json',
      },
    } as unknown as MojangVersionPackage;

    const instanceDir = EnginePaths.getInstanceDir(instance.id);
    const nativesDir = EnginePaths.getNativesDir(instance.id);
    const librariesDir = EnginePaths.getLibrariesDir();
    const versionsDir = EnginePaths.getVersionsDir();

    const args = GameArgumentsBuilder.buildArguments({
      versionPackage,
      instanceDir,
      assetsDir: EnginePaths.getAssetsDir(),
      librariesDir,
      versionsDir,
      nativesDir,
      session: {
        username: 'ZombekTester',
        uuid: '00000000-0000-0000-0000-000000000001',
        accessToken: 'dummy-token',
        userType: 'msa',
      },
      minRamMb: 1024,
      maxRamMb: 3072,
    });

    // Check mainClass
    expect(args).toContain('org.quiltmc.loader.impl.launch.knot.KnotClient');

    // Check RAM
    expect(args).toContain('-Xmx3072M');
    expect(args).toContain('-Xms1024M');

    // Check game arguments
    expect(args).toContain('--username');
    expect(args).toContain('ZombekTester');
    expect(args).toContain('--uuid');
    expect(args).toContain('00000000-0000-0000-0000-000000000001');

    // Check game directory is isolated instance dir
    const gameDirIndex = args.indexOf('--gameDir');
    expect(gameDirIndex).toBeGreaterThan(-1);
    expect(args[gameDirIndex + 1]).toBe(EnginePaths.getInstanceDir(instance.id));

    // Check classpath contains both quilt loader jar and client jar
    const cpIndex = args.indexOf('-cp');
    expect(cpIndex).toBeGreaterThan(-1);
    const classpath = args[cpIndex + 1];
    expect(classpath).toContain('quilt-loader-0.27.1-beta.1.jar');
    expect(classpath).toContain('1.20.4.jar');
  });

  it('should verify ModCompatibilityTarget type definition supports future Modpack Manager', () => {
    const targets: ModCompatibilityTarget[] = [
      'quilt',
      'fabric',
      'fabric-and-quilt',
      'quilt-incompatible',
      'forge',
      'neoforge',
    ];
    expect(targets).toHaveLength(6);
    expect(targets).toContain('quilt');
    expect(targets).toContain('fabric-and-quilt');
    expect(targets).toContain('quilt-incompatible');
  });
});
