import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import * as path from 'node:path';
import * as fs from 'node:fs';
import * as os from 'node:os';
import { ForgeMetaService, FORGE_PROMOTIONS_URL, FORGE_MAVEN_METADATA_URL } from '../electron/loaders/ForgeMetaService';
import { ForgeProcessorManager } from '../electron/loaders/ForgeProcessorManager';
import { ForgeLoaderAdapter } from '../electron/loaders/ForgeLoaderAdapter';
import { EnginePaths } from '../electron/engine/paths';
import { GameInstaller } from '../electron/engine/GameInstaller';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import { GameArgumentsBuilder } from '../electron/engine/GameArgumentsBuilder';
import { VersionManifestService } from '../electron/engine/VersionManifestService';
import type { MojangVersionPackage } from '../electron/engine/types';
import type { ForgeInstallProfile } from '@zombek/shared-types';

describe('Forge Loader - ForgeMetaService', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-forge-meta-test-'));
    EnginePaths.setRootDir(tempDir);
    EnginePaths.ensureAllBaseDirs();
    ForgeMetaService.clearMemoryCache();
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
    ForgeMetaService.clearMemoryCache();
    vi.restoreAllMocks();
  });

  it('should fetch and parse Forge promotions slim JSON and identify recommended build', async () => {
    const mockPromos = {
      promos: {
        '1.20.4-latest': '49.2.13',
        '1.20.4-recommended': '49.2.13',
        '1.20.1-latest': '47.4.28',
        '1.20.1-recommended': '47.4.0',
      },
    };

    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any) => {
      expect(String(url)).toBe(FORGE_PROMOTIONS_URL);
      return {
        ok: true,
        status: 200,
        json: async () => mockPromos,
      } as any;
    });

    const promos = await ForgeMetaService.getPromotions(true);
    expect(promos['1.20.4-recommended']).toBe('49.2.13');
    expect(promos['1.20.1-latest']).toBe('47.4.28');

    // Verify cache written to disk
    const cacheFile = ForgeMetaService.getCacheFilePath();
    expect(fs.existsSync(cacheFile)).toBe(true);
  });

  it('should parse maven-metadata.xml and extract versions list', async () => {
    const mockXml = `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>net.minecraftforge</groupId>
  <artifactId>forge</artifactId>
  <versioning>
    <versions>
      <version>1.20.4-49.2.13</version>
      <version>1.20.4-49.2.0</version>
      <version>1.20.1-47.4.28</version>
    </versions>
  </versioning>
</metadata>`;

    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any) => {
      expect(String(url)).toBe(FORGE_MAVEN_METADATA_URL);
      return {
        ok: true,
        status: 200,
        text: async () => mockXml,
      } as any;
    });

    const versions = await ForgeMetaService.getMavenVersions(true);
    expect(versions).toHaveLength(3);
    expect(versions).toContain('1.20.4-49.2.13');
    expect(versions).toContain('1.20.1-47.4.28');
  });

  it('should combine promotions and maven versions, marking recommended and latest builds', async () => {
    vi.spyOn(ForgeMetaService, 'getPromotions').mockResolvedValue({
      '1.20.4-latest': '49.2.13',
      '1.20.4-recommended': '49.2.10',
    });

    vi.spyOn(ForgeMetaService, 'getMavenVersions').mockResolvedValue([
      '1.20.4-49.2.13',
      '1.20.4-49.2.10',
      '1.20.4-49.2.0',
      '1.20.1-47.4.28',
    ]);

    const versions = await ForgeMetaService.getForgeVersionsForGame('1.20.4', true);
    expect(versions).toHaveLength(3);

    const rec = versions.find((v) => v.isRecommended);
    expect(rec).toBeDefined();
    expect(rec?.rawVersion).toBe('49.2.10');
    expect(rec?.installerUrl).toContain('1.20.4-49.2.10/forge-1.20.4-49.2.10-installer.jar');

    const latest = versions.find((v) => v.isLatest);
    expect(latest).toBeDefined();
    expect(latest?.rawVersion).toBe('49.2.13');
  });

  it('should fallback gracefully to disk cache when offline', async () => {
    const cacheFile = ForgeMetaService.getCacheFilePath();
    const mockCache = {
      versionsByGame: {
        '1.20.4': [
          {
            version: '1.20.4-49.2.13',
            rawVersion: '49.2.13',
            minecraftVersion: '1.20.4',
            isRecommended: true,
            isLatest: true,
            installerUrl: 'https://example.com/installer.jar',
          },
        ],
      },
    };
    fs.writeFileSync(cacheFile, JSON.stringify(mockCache), 'utf-8');

    // Network failure
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('ENOTFOUND files.minecraftforge.net'));

    const list = await ForgeMetaService.getForgeVersionsForGame('1.20.4', false);
    expect(list).toHaveLength(1);
    expect(list[0].rawVersion).toBe('49.2.13');
  });
});

describe('Forge Loader - ForgeProcessorManager & Variable Resolution', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-forge-proc-test-'));
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
  });

  it('should resolve maven coordinates to local file paths correctly', () => {
    const libDir = path.join(tempDir, 'libraries');

    // Standard coordinate
    const p1 = ForgeProcessorManager.resolveMavenPath('net.minecraftforge:installertools:1.4.0', libDir);
    expect(p1).toBe(path.join(libDir, 'net', 'minecraftforge', 'installertools', '1.4.0', 'installertools-1.4.0.jar'));

    // Coordinate with classifier
    const p2 = ForgeProcessorManager.resolveMavenPath('net.minecraftforge:forge:1.20.4-49.2.13:client', libDir);
    expect(p2).toBe(path.join(libDir, 'net', 'minecraftforge', 'forge', '1.20.4-49.2.13', 'forge-1.20.4-49.2.13-client.jar'));

    // Coordinate with custom extension @zip in brackets
    const p3 = ForgeProcessorManager.resolveMavenPath('[de.oceanlabs.mcp:mcp_config:1.20.4-20231207.112700@zip]', libDir);
    expect(p3).toBe(path.join(libDir, 'de', 'oceanlabs', 'mcp', 'mcp_config', '1.20.4-20231207.112700', 'mcp_config-1.20.4-20231207.112700.zip'));
  });

  it('should substitute variable arguments, data keys, and literal values', () => {
    const context = {
      installerJarPath: path.join(tempDir, 'installer.jar'),
      minecraftJarPath: path.join(tempDir, '1.20.4.jar'),
      rootDir: tempDir,
      librariesDir: path.join(tempDir, 'libraries'),
      javaPath: 'java',
      tempDir: path.join(tempDir, 'temp'),
    };

    const data = {
      BINPATCH: { client: '/data/client.lzma', server: '/data/server.lzma' },
      PATCHED: { client: '[net.minecraftforge:forge:1.20.4-49.2.13:client]' },
      MAPPINGS_SHA: { client: "'a9852bd9d5b3cd76530c1b32c9d12d3937f8c980'" },
    };

    // 1. Direct {MINECRAFT_JAR}
    expect(ForgeProcessorManager.substituteArgument('{MINECRAFT_JAR}', context, data)).toBe(context.minecraftJarPath);

    // 2. Direct {INSTALLER}
    expect(ForgeProcessorManager.substituteArgument('{INSTALLER}', context, data)).toBe(context.installerJarPath);

    // 3. Quoted literal from data
    expect(ForgeProcessorManager.substituteArgument('{MAPPINGS_SHA}', context, data)).toBe('a9852bd9d5b3cd76530c1b32c9d12d3937f8c980');

    // 4. Archive file from data
    expect(ForgeProcessorManager.substituteArgument('{BINPATCH}', context, data)).toBe(path.join(context.tempDir, 'data', 'client.lzma'));

    // 5. Maven bracket output from data
    expect(ForgeProcessorManager.substituteArgument('{PATCHED}', context, data)).toBe(
      path.join(context.librariesDir, 'net', 'minecraftforge', 'forge', '1.20.4-49.2.13', 'forge-1.20.4-49.2.13-client.jar')
    );
  });

  it('should filter out server-only processors for client installation', () => {
    const profile: ForgeInstallProfile = {
      version: '1.20.4-forge-49.2.13',
      processors: [
        { sides: ['server'], jar: 'net.minecraftforge:installertools:1.4.0', classpath: [], args: [] },
        { sides: ['client'], jar: 'net.minecraftforge:ForgeAutoRenamingTool:1.0.6', classpath: [], args: [] },
        { sides: undefined, jar: 'net.minecraftforge:binarypatcher:1.2.0', classpath: [], args: [] },
      ],
    };

    const clientProcessors = ForgeProcessorManager.getClientProcessors(profile);
    expect(clientProcessors).toHaveLength(2);
    expect(clientProcessors[0].jar).toBe('net.minecraftforge:ForgeAutoRenamingTool:1.0.6');
    expect(clientProcessors[1].jar).toBe('net.minecraftforge:binarypatcher:1.2.0');
  });
});

describe('Forge Loader - ForgeLoaderAdapter & Instance Isolation', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-forge-adapter-test-'));
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

  it('should isolate instance directory and create mods, config, saves, resourcepacks, screenshots', async () => {
    const inst = await InstallationRepository.create({
      name: 'Forge Modded 1.20.4',
      minecraftVersion: '1.20.4',
      loader: 'forge',
      loaderVersion: '49.2.13',
      allocatedRamMb: 6144,
    });

    expect(inst.loader).toBe('forge');
    expect(inst.loaderVersion).toBe('49.2.13');

    const instanceDir = inst.instanceDir;
    expect(fs.existsSync(path.join(instanceDir, 'mods'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'config'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'saves'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'resourcepacks'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'screenshots'))).toBe(true);
    expect(fs.existsSync(path.join(instanceDir, 'natives'))).toBe(true);
  });

  it('should resolve profile merging Vanilla base with Forge launch profile', async () => {
    const vanillaPkg: MojangVersionPackage = {
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
        client: { sha1: 'abc', size: 100, url: 'http://example.com' },
      },
      libraries: [{ name: 'com.mojang:authlib:6.0.52' }],
      assetIndex: { id: '16', sha1: '', size: 0, totalSize: 0, url: '' },
      assets: '16',
    };

    const forgeProfileJson: MojangVersionPackage = {
      id: '1.20.4-forge-49.2.13',
      inheritsFrom: '1.20.4',
      type: 'release',
      time: '2024-01-01',
      releaseTime: '2024-01-01',
      mainClass: 'net.minecraftforge.bootstrap.ForgeBootstrap',
      arguments: {
        game: ['--launchTarget', 'forgeclient'],
        jvm: ['-Dforge.logging.console.level=debug'],
      },
      downloads: {
        client: { sha1: 'abc', size: 100, url: 'http://example.com' },
      },
      libraries: [
        { name: 'net.minecraftforge:forge:1.20.4-49.2.13:client' },
        { name: 'cpw.mods:modlauncher:10.0.9' },
      ],
      assetIndex: { id: '16', sha1: '', size: 0, totalSize: 0, url: '' },
      assets: '16',
    };

    // Save forge profile to versions/1.20.4-forge-49.2.13/1.20.4-forge-49.2.13.json
    const vDir = path.join(EnginePaths.getVersionsDir(), '1.20.4-forge-49.2.13');
    fs.mkdirSync(vDir, { recursive: true });
    fs.writeFileSync(path.join(vDir, '1.20.4-forge-49.2.13.json'), JSON.stringify(forgeProfileJson));

    vi.spyOn(VersionManifestService, 'getVersionPackage').mockResolvedValue(vanillaPkg);

    const adapter = ForgeLoaderAdapter.getInstance();
    const resolved = await adapter.resolveProfile('1.20.4', '49.2.13');

    expect(resolved.id).toBe('1.20.4-forge-49.2.13');
    expect(resolved.inheritsFrom).toBe('1.20.4');
    expect(resolved.mainClass).toBe('net.minecraftforge.bootstrap.ForgeBootstrap');

    // Libraries merged: 1 vanilla + 2 forge = 3 libraries
    expect(resolved.libraries).toHaveLength(3);
    expect(resolved.libraries.some((l) => l.name === 'cpw.mods:modlauncher:10.0.9')).toBe(true);

    // Arguments merged: both vanilla and forge args are included
    expect(resolved.arguments?.game).toContain('--launchTarget');
    expect(resolved.arguments?.jvm).toContain('-Dforge.logging.console.level=debug');
  });
});

describe('Forge Loader - GameArgumentsBuilder with Forge Profile', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-forge-args-test-'));
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

  it('should build command line arguments with ForgeBootstrap and Vanilla client JAR in classpath', () => {
    const versionsDir = EnginePaths.getVersionsDir();
    const librariesDir = EnginePaths.getLibrariesDir();
    const instanceDir = path.join(tempDir, 'instances', 'forge-inst-1');
    const nativesDir = path.join(instanceDir, 'natives');

    // Create dummy base vanilla client JAR in versions/1.20.4/1.20.4.jar
    const vanillaDir = path.join(versionsDir, '1.20.4');
    fs.mkdirSync(vanillaDir, { recursive: true });
    const vanillaJarPath = path.join(vanillaDir, '1.20.4.jar');
    fs.writeFileSync(vanillaJarPath, 'dummy-vanilla-client-jar');

    // Create dummy forge bootstrap library in libraries/
    const forgeLibRel = GameInstaller.getMavenPath('net.minecraftforge:bootstrap:2.1.8');
    const forgeLibFull = path.join(librariesDir, forgeLibRel);
    fs.mkdirSync(path.dirname(forgeLibFull), { recursive: true });
    fs.writeFileSync(forgeLibFull, 'dummy-forge-lib');

    const forgePackage: MojangVersionPackage = {
      id: '1.20.4-forge-49.2.13',
      inheritsFrom: '1.20.4',
      mainClass: 'net.minecraftforge.bootstrap.ForgeBootstrap',
      arguments: {
        jvm: [
          '-Dforge.logging.console.level=debug',
          '-cp',
          '${classpath}',
        ],
        game: [
          '--username', '${auth_player_name}',
          '--gameDir', '${game_directory}',
          '--launchTarget', 'forgeclient',
        ],
      },
      downloads: {
        client: { sha1: 'abc', size: 100, url: 'http://example.com' },
      },
      libraries: [
        {
          name: 'net.minecraftforge:bootstrap:2.1.8',
        },
      ],
      assetIndex: { id: '16', sha1: '', size: 0, totalSize: 0, url: '' },
      assets: '16',
      type: 'release',
      time: '',
      releaseTime: '',
    };

    const args = GameArgumentsBuilder.buildArguments({
      versionPackage: forgePackage,
      instanceDir,
      assetsDir: EnginePaths.getAssetsDir(),
      librariesDir,
      versionsDir,
      nativesDir,
      session: {
        username: 'ZombekForgePlayer',
        uuid: '11111111-2222-3333-4444-555555555555',
        accessToken: 'mock_token',
        userType: 'msa',
      },
      minRamMb: 3072,
      maxRamMb: 6144,
    });

    // 1. MainClass is ForgeBootstrap
    expect(args).toContain('net.minecraftforge.bootstrap.ForgeBootstrap');

    // 2. Classpath contains both base Vanilla jar and Forge library
    const cpIndex = args.indexOf('-cp');
    expect(cpIndex).toBeGreaterThan(-1);
    const cpValue = args[cpIndex + 1];
    expect(cpValue).toContain(vanillaJarPath);
    expect(cpValue).toContain(forgeLibFull);

    // 3. Game args contain Forge launchTarget
    expect(args).toContain('--launchTarget');
    expect(args).toContain('forgeclient');

    // 4. Game directory points to instance directory
    const gameDirIndex = args.indexOf('--gameDir');
    expect(gameDirIndex).toBeGreaterThan(-1);
    expect(args[gameDirIndex + 1]).toBe(instanceDir);

    // 5. Memory flags
    expect(args).toContain('-Xms3072M');
    expect(args).toContain('-Xmx6144M');
  });
});
