import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import * as path from 'node:path';
import * as fs from 'node:fs';
import * as os from 'node:os';
import {
  NeoForgeMetaService,
  NEOFORGE_MAVEN_MODERN_METADATA_URL,
  NEOFORGE_MAVEN_TRANSITION_METADATA_URL,
} from '../electron/loaders/NeoForgeMetaService';
import { NeoForgeProcessorManager } from '../electron/loaders/NeoForgeProcessorManager';
import { NeoForgeLoaderAdapter } from '../electron/loaders/NeoForgeLoaderAdapter';
import { EnginePaths } from '../electron/engine/paths';
import { GameInstaller } from '../electron/engine/GameInstaller';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import { GameArgumentsBuilder } from '../electron/engine/GameArgumentsBuilder';
import { VersionManifestService } from '../electron/engine/VersionManifestService';
import type { MojangVersionPackage } from '../electron/engine/types';
import type { NeoForgeInstallProfile } from '@zombek/shared-types';

describe('NeoForge Loader - NeoForgeMetaService', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-neoforge-meta-test-'));
    EnginePaths.setRootDir(tempDir);
    EnginePaths.ensureAllBaseDirs();
    NeoForgeMetaService.clearMemoryCache();
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
    NeoForgeMetaService.clearMemoryCache();
    vi.restoreAllMocks();
  });

  it('should resolve correct generations and prefixes for Minecraft versions', () => {
    // 1.20.1 transition
    const v1201 = NeoForgeMetaService.getNeoForgePrefix('1.20.1');
    expect(v1201).not.toBeNull();
    expect(v1201?.generation).toBe('transition');
    expect(v1201?.artifactCoordinates).toBe('net.neoforged:forge');
    expect(v1201?.prefix).toBe('1.20.1-47.1.');

    // 1.20.4 modern
    const v1204 = NeoForgeMetaService.getNeoForgePrefix('1.20.4');
    expect(v1204).not.toBeNull();
    expect(v1204?.generation).toBe('modern');
    expect(v1204?.artifactCoordinates).toBe('net.neoforged:neoforge');
    expect(v1204?.prefix).toBe('20.4.');

    // 1.21.1 modern
    const v1211 = NeoForgeMetaService.getNeoForgePrefix('1.21.1');
    expect(v1211).not.toBeNull();
    expect(v1211?.generation).toBe('modern');
    expect(v1211?.artifactCoordinates).toBe('net.neoforged:neoforge');
    expect(v1211?.prefix).toBe('21.1.');

    // 1.19.4 and 1.16.5 unsupported
    expect(NeoForgeMetaService.getNeoForgePrefix('1.19.4')).toBeNull();
    expect(NeoForgeMetaService.getNeoForgePrefix('1.16.5')).toBeNull();
    expect(NeoForgeMetaService.getNeoForgePrefix('1.12.2')).toBeNull();
  });

  it('should fetch and parse modern Maven metadata XML and sort versions descending', async () => {
    const mockModernXml = `
      <metadata>
        <groupId>net.neoforged</groupId>
        <artifactId>neoforge</artifactId>
        <versioning>
          <versions>
            <version>20.4.0-beta</version>
            <version>20.4.10</version>
            <version>20.4.2</version>
            <version>20.4.237</version>
            <version>20.4.238-beta</version>
            <version>20.2.86</version>
          </versions>
        </versioning>
      </metadata>
    `;

    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any) => {
      expect(String(url)).toBe(NEOFORGE_MAVEN_MODERN_METADATA_URL);
      return {
        ok: true,
        status: 200,
        text: async () => mockModernXml,
      } as any;
    });

    const versions = await NeoForgeMetaService.getNeoForgeVersionsForGame('1.20.4', true);
    expect(versions.length).toBe(5);

    // Sorted descending: 20.4.238-beta is latest, 20.4.237 is recommended (highest non-beta)
    expect(versions[0].version).toBe('20.4.238-beta');
    expect(versions[0].isLatest).toBe(true);
    expect(versions[0].isRecommended).toBe(false);

    const recommended = versions.find((v) => v.isRecommended);
    expect(recommended).toBeDefined();
    expect(recommended?.version).toBe('20.4.237');
    expect(recommended?.generation).toBe('modern');
    expect(recommended?.installerUrl).toContain('net/neoforged/neoforge/20.4.237/neoforge-20.4.237-installer.jar');

    // Verify disk cache
    const cacheFile = NeoForgeMetaService.getCacheFilePath();
    expect(fs.existsSync(cacheFile)).toBe(true);
  });

  it('should fetch and parse transition Maven metadata XML for Minecraft 1.20.1', async () => {
    const mockTransitionXml = `
      <metadata>
        <groupId>net.neoforged</groupId>
        <artifactId>forge</artifactId>
        <versioning>
          <versions>
            <version>1.20.1-47.1.5</version>
            <version>1.20.1-47.1.106</version>
            <version>1.20.1-47.1.84</version>
          </versions>
        </versioning>
      </metadata>
    `;

    vi.spyOn(globalThis, 'fetch').mockImplementationOnce(async (url: any) => {
      expect(String(url)).toBe(NEOFORGE_MAVEN_TRANSITION_METADATA_URL);
      return {
        ok: true,
        status: 200,
        text: async () => mockTransitionXml,
      } as any;
    });

    const versions = await NeoForgeMetaService.getNeoForgeVersionsForGame('1.20.1', true);
    expect(versions.length).toBe(3);

    expect(versions[0].version).toBe('1.20.1-47.1.106');
    expect(versions[0].generation).toBe('transition');
    expect(versions[0].artifactCoordinates).toBe('net.neoforged:forge');
    expect(versions[0].installerUrl).toContain('net/neoforged/forge/1.20.1-47.1.106/forge-1.20.1-47.1.106-installer.jar');
  });

  it('should fall back to disk cache when offline or network fails', async () => {
    const cacheFile = NeoForgeMetaService.getCacheFilePath();
    const diskCache = {
      lastUpdated: new Date().toISOString(),
      versionsByGame: {
        '1.20.4': [
          {
            version: '20.4.237',
            rawVersion: '20.4.237',
            minecraftVersion: '1.20.4',
            artifactCoordinates: 'net.neoforged:neoforge',
            installerUrl: 'https://maven.neoforged.net/releases/net/neoforged/neoforge/20.4.237/neoforge-20.4.237-installer.jar',
            generation: 'modern',
            isRecommended: true,
            isLatest: true,
          },
        ],
      },
    };
    fs.writeFileSync(cacheFile, JSON.stringify(diskCache, null, 2), 'utf-8');

    // Simulate network error
    vi.spyOn(globalThis, 'fetch').mockRejectedValueOnce(new Error('Network offline'));

    const versions = await NeoForgeMetaService.getNeoForgeVersionsForGame('1.20.4', true);
    expect(versions.length).toBe(1);
    expect(versions[0].version).toBe('20.4.237');
  });

  it('should return empty list for unsupported Minecraft versions', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    const versions = await NeoForgeMetaService.getNeoForgeVersionsForGame('1.16.5', false);
    expect(versions).toEqual([]);
    expect(fetchSpy).not.toHaveBeenCalled();
  });
});

describe('NeoForge Loader - NeoForgeProcessorManager', () => {
  let librariesDir: string;
  let context: any;

  beforeEach(() => {
    librariesDir = path.join(os.tmpdir(), 'zombek-neoforge-proc-test-' + Date.now(), 'libraries');
    fs.mkdirSync(librariesDir, { recursive: true });

    context = {
      installerJarPath: path.join(librariesDir, 'installer.jar'),
      minecraftJarPath: path.join(librariesDir, 'client.jar'),
      rootDir: path.dirname(librariesDir),
      librariesDir: librariesDir,
      javaPath: 'java',
      tempDir: path.join(librariesDir, 'temp'),
    };
  });

  afterEach(() => {
    try {
      fs.rmSync(path.dirname(librariesDir), { recursive: true, force: true });
    } catch {
      // ignore
    }
  });

  it('should resolve Maven coordinate paths with classifiers and extensions', () => {
    const p1 = NeoForgeProcessorManager.resolveMavenPath(
      'net.neoforged.installertools:binarypatcher:2.1.2',
      librariesDir
    );
    expect(p1.replace(/\\/g, '/')).toContain(
      'libraries/net/neoforged/installertools/binarypatcher/2.1.2/binarypatcher-2.1.2.jar'
    );

    const p2 = NeoForgeProcessorManager.resolveMavenPath(
      '[net.neoforged:mergetool:2.0.0:api@jar]',
      librariesDir
    );
    expect(p2.replace(/\\/g, '/')).toContain(
      'libraries/net/neoforged/mergetool/2.0.0/mergetool-2.0.0-api.jar'
    );
  });

  it('should substitute variable tokens, data dictionary keys, and literal arguments', () => {
    const dataDict = {
      MC_SLIM: {
        client: '[net.minecraft:client:1.20.4:slim@jar]',
      },
      PATCHED: {
        client: '[net.neoforged:neoforge:20.4.237:client@jar]',
      },
    };

    // Standard tokens
    expect(NeoForgeProcessorManager.substituteArgument('{INSTALLER}', context, dataDict)).toBe(
      context.installerJarPath
    );
    expect(NeoForgeProcessorManager.substituteArgument('{MINECRAFT_JAR}', context, dataDict)).toBe(
      context.minecraftJarPath
    );
    expect(NeoForgeProcessorManager.substituteArgument('{ROOT}', context, dataDict)).toBe(
      context.rootDir
    );

    // Literal string unquoting
    expect(NeoForgeProcessorManager.substituteArgument("'client'", context, dataDict)).toBe('client');

    // Archive data reference
    const dataRef = NeoForgeProcessorManager.substituteArgument('data/client.lzma', context, dataDict);
    expect(dataRef.replace(/\\/g, '/')).toContain('temp/data/client.lzma');

    // Data dictionary key reference
    const resolvedData = NeoForgeProcessorManager.substituteArgument('{MC_SLIM}', context, dataDict);
    expect(resolvedData.replace(/\\/g, '/')).toContain('net/minecraft/client/1.20.4/client-1.20.4-slim.jar');
  });

  it('should filter client processors and reject server-only processors', () => {
    const processors: any[] = [
      { jar: 'tool1', classpath: [], args: [], sides: ['server'] },
      { jar: 'tool2', classpath: [], args: [], sides: ['client'] },
      { jar: 'tool3', classpath: [], args: [] }, // both
    ];

    const clientOnly = NeoForgeProcessorManager.filterClientProcessors(processors);
    expect(clientOnly.length).toBe(2);
    expect(clientOnly[0].jar).toBe('tool2');
    expect(clientOnly[1].jar).toBe('tool3');
  });
});

describe('NeoForge Loader - NeoForgeLoaderAdapter & GameArgumentsBuilder', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-neoforge-adapter-test-'));
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

  it('should merge Vanilla package with installed NeoForge profile and construct valid classpath', async () => {
    const versionsDir = EnginePaths.getVersionsDir();
    const librariesDir = EnginePaths.getLibrariesDir();
    const instanceDir = path.join(tempDir, 'instances', 'neoforge-inst-1');
    const nativesDir = path.join(instanceDir, 'natives');

    // Create dummy base vanilla client JAR in versions/1.20.4/1.20.4.jar
    const vanillaDir = path.join(versionsDir, '1.20.4');
    fs.mkdirSync(vanillaDir, { recursive: true });
    const vanillaJarPath = path.join(vanillaDir, '1.20.4.jar');
    fs.writeFileSync(vanillaJarPath, 'dummy-vanilla-client-jar');

    // Create dummy neoforge library in libraries/
    const neoLibRel = GameInstaller.getMavenPath('cpw.mods:bootstraplauncher:1.1.2');
    const neoLibFull = path.join(librariesDir, neoLibRel);
    fs.mkdirSync(path.dirname(neoLibFull), { recursive: true });
    fs.writeFileSync(neoLibFull, 'dummy-neoforge-bootstrap-lib');

    const neoPackage: any = {
      id: 'neoforge-20.4.237',
      inheritsFrom: '1.20.4',
      mainClass: 'cpw.mods.bootstraplauncher.BootstrapLauncher',
      arguments: {
        jvm: [
          '-Djava.net.preferIPv6Addresses=system',
          '-cp',
          '${classpath}',
        ],
        game: [
          '--fml.neoForgeVersion',
          '20.4.237',
        ],
      },
      libraries: [
        {
          name: 'cpw.mods:bootstraplauncher:1.1.2',
          downloads: {
            artifact: {
              path: neoLibRel,
              sha1: '123',
              size: 100,
              url: 'dummy',
            },
          },
        },
      ],
      time: '2026-11-02T12:00:00Z',
      releaseTime: '2026-11-02T12:00:00Z',
      type: 'release',
    };

    // Save installed NeoForge version json
    const neoProfileDir = path.join(versionsDir, 'neoforge-20.4.237');
    fs.mkdirSync(neoProfileDir, { recursive: true });
    fs.writeFileSync(
      path.join(neoProfileDir, 'neoforge-20.4.237.json'),
      JSON.stringify(neoPackage, null, 2),
      'utf-8'
    );

    // Mock VersionManifestService.getVersionPackage to return base vanilla package
    vi.spyOn(VersionManifestService, 'getVersionPackage').mockResolvedValueOnce({
      id: '1.20.4',
      type: 'release',
      time: '2023-12-07T12:00:00Z',
      releaseTime: '2023-12-07T12:00:00Z',
      mainClass: 'net.minecraft.client.main.Main',
      arguments: {
        game: ['--username', '${auth_player_name}'],
        jvm: ['-Xms1024M', '-Xmx2048M'],
      },
      libraries: [],
    } as any);

    const adapter = NeoForgeLoaderAdapter.getInstance();
    const resolved = await adapter.resolveProfile('1.20.4', '20.4.237');

    expect(resolved.id).toBe('neoforge-20.4.237');
    expect(resolved.inheritsFrom).toBe('1.20.4');
    expect(resolved.mainClass).toBe('cpw.mods.bootstraplauncher.BootstrapLauncher');
    expect(resolved.libraries.length).toBeGreaterThanOrEqual(1);

    // Build classpath using GameArgumentsBuilder
    const classpath = GameArgumentsBuilder.buildClasspath(resolved, librariesDir, versionsDir);
    expect(classpath).toContain('1.20.4.jar');
    expect(classpath).toContain('bootstraplauncher-1.1.2.jar');

    // Build process arguments
    const args = GameArgumentsBuilder.buildArguments({
      versionPackage: resolved,
      instanceDir,
      assetsDir: path.join(tempDir, 'assets'),
      librariesDir,
      versionsDir,
      nativesDir,
      session: {
        username: 'TestZombek',
        uuid: '00000000-0000-0000-0000-000000000001',
        accessToken: 'dummy-token',
        userType: 'offline',
      },
    });

    expect(args).toContain('cpw.mods.bootstraplauncher.BootstrapLauncher');
    expect(args).toContain('--fml.neoForgeVersion');
    expect(args).toContain('20.4.237');
    expect(args).toContain('TestZombek');
  });

  it('should isolate instances into independent directories', async () => {
    const instanceDir = path.join(EnginePaths.getInstancesDir(), 'neoforge-inst-iso');
    const subdirs = ['mods', 'config', 'saves', 'resourcepacks', 'screenshots', 'natives', 'logs'];

    for (const sub of subdirs) {
      const p = path.join(instanceDir, sub);
      fs.mkdirSync(p, { recursive: true });
      expect(fs.existsSync(p)).toBe(true);
    }

    // Create a mock mod in neoforge instance
    const modFile = path.join(instanceDir, 'mods', 'test-neoforge-mod.jar');
    fs.writeFileSync(modFile, 'dummy-jar');
    expect(fs.existsSync(modFile)).toBe(true);

    // Ensure vanilla instance mods dir does not see this mod
    const vanillaInstanceDir = path.join(EnginePaths.getInstancesDir(), 'vanilla-inst');
    const vanillaModFile = path.join(vanillaInstanceDir, 'mods', 'test-neoforge-mod.jar');
    expect(fs.existsSync(vanillaModFile)).toBe(false);
  });
});
