import { describe, it, expect } from 'vitest';
import * as path from 'node:path';
import { generateOfflineUuid, OfflineSessionProvider } from '../electron/engine/GameSessionProvider';
import { EnginePaths } from '../electron/engine/paths';
import { GameInstaller } from '../electron/engine/GameInstaller';
import { GameArgumentsBuilder } from '../electron/engine/GameArgumentsBuilder';
import { JavaRuntimeManager } from '../electron/engine/JavaRuntimeManager';
import type { MojangVersionPackage, MojangRule } from '../electron/engine/types';

describe('Minecraft Engine - GameSessionProvider', () => {
  it('should generate deterministic valid RFC 4122 v3 UUID for offline player', () => {
    const uuid1 = generateOfflineUuid('ZombekPlayer');
    const uuid2 = generateOfflineUuid('ZombekPlayer');

    expect(uuid1).toBe(uuid2);
    // UUID v3 format regex: 8-4-4-4-12, version 3 at pos 14, variant [89ab] at pos 19
    const uuidV3Regex = /^[0-9a-f]{8}-[0-9a-f]{4}-3[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    expect(uuidV3Regex.test(uuid1)).toBe(true);

    const diffUuid = generateOfflineUuid('AnotherPlayer');
    expect(diffUuid).not.toBe(uuid1);
    expect(uuidV3Regex.test(diffUuid)).toBe(true);
  });

  it('should create an offline player session with token "0"', async () => {
    const provider = new OfflineSessionProvider();
    const session = await provider.getSession('Steve');

    expect(session.username).toBe('Steve');
    expect(session.accessToken).toBe('0');
    expect(session.userType).toBe('offline');
    expect(session.uuid).toBe(generateOfflineUuid('Steve'));
  });
});

describe('Minecraft Engine - EnginePaths & Security', () => {
  it('should manage paths outside the repository root', () => {
    const baseDir = EnginePaths.getBaseDir();
    expect(baseDir).toContain('ZombekClient');

    const instancesDir = EnginePaths.getInstancesDir();
    expect(instancesDir).toContain(path.join('ZombekClient', 'instances'));

    const assetsDir = EnginePaths.getAssetsDir();
    expect(assetsDir).toContain(path.join('ZombekClient', 'game-data', 'assets'));
  });

  it('should reject unsafe path traversal filenames', () => {
    expect(() => EnginePaths.assertSafeFilename('../malicious')).toThrow();
    expect(() => EnginePaths.assertSafeFilename('..\\malicious')).toThrow();
    expect(() => EnginePaths.assertSafeFilename('/root/dir')).toThrow();
    expect(() => EnginePaths.assertSafeFilename('C:\\windows\\system32')).toThrow();
    expect(() => EnginePaths.assertSafeFilename('valid_instance_123')).not.toThrow();
  });

  it('should verify isSafePath containment', () => {
    const base = 'C:\\ZombekClient\\instances\\inst-1';
    expect(EnginePaths.isSafePath(base, 'C:\\ZombekClient\\instances\\inst-1\\saves\\world')).toBe(true);
    expect(EnginePaths.isSafePath(base, 'C:\\ZombekClient\\instances\\inst-2\\secret.txt')).toBe(false);
    expect(EnginePaths.isSafePath(base, 'C:\\Windows\\System32\\cmd.exe')).toBe(false);
  });
});

describe('Minecraft Engine - GameInstaller & Rules', () => {
  it('should parse Maven coordinates into file paths correctly', () => {
    const simple = GameInstaller.getMavenPath('com.mojang:patchy:1.1');
    expect(simple).toBe('com/mojang/patchy/1.1/patchy-1.1.jar');

    const withClassifier = GameInstaller.getMavenPath(
      'org.lwjgl.lwjgl:lwjgl-platform:2.9.4-nightly-20150209',
      'natives-windows'
    );
    expect(withClassifier).toBe(
      'org/lwjgl/lwjgl/lwjgl-platform/2.9.4-nightly-20150209/lwjgl-platform-2.9.4-nightly-20150209-natives-windows.jar'
    );
  });

  it('should evaluate OS rules matching current platform', () => {
    const currentOs = process.platform === 'win32' ? 'windows' : process.platform === 'darwin' ? 'osx' : 'linux';
    const otherOs = currentOs === 'windows' ? 'osx' : 'windows';

    const allowCurrentRule: MojangRule[] = [
      { action: 'allow', os: { name: currentOs } }
    ];
    expect(GameInstaller.isRuleAllowed(allowCurrentRule)).toBe(true);

    const allowOtherRule: MojangRule[] = [
      { action: 'allow', os: { name: otherOs } }
    ];
    expect(GameInstaller.isRuleAllowed(allowOtherRule)).toBe(false);

    const disallowCurrentRule: MojangRule[] = [
      { action: 'allow' },
      { action: 'disallow', os: { name: currentOs } }
    ];
    expect(GameInstaller.isRuleAllowed(disallowCurrentRule)).toBe(false);
  });
});

describe('Minecraft Engine - GameArgumentsBuilder', () => {
  it('should replace placeholders in templates', () => {
    const template = '--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory}';
    const dict = {
      auth_player_name: 'ZombekUser',
      version_name: '1.20.4',
      game_directory: 'C:\\instances\\test',
    };

    const substituted = GameArgumentsBuilder.substitute(template, dict);
    expect(substituted).toBe('--username ZombekUser --version 1.20.4 --gameDir C:\\instances\\test');
  });

  it('should build complete argument list for modern Minecraft versions', () => {
    const mockPkg = {
      id: '1.20.4',
      type: 'release',
      mainClass: 'net.minecraft.client.main.Main',
      arguments: {
        jvm: [
          '-Djava.library.path=${natives_directory}',
          '-Dminecraft.launcher.name=${launcher_name}',
          '-cp',
          '${classpath}'
        ],
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
          '${user_type}'
        ]
      },
      libraries: []
    } as unknown as MojangVersionPackage;

    const args = GameArgumentsBuilder.buildArguments({
      versionPackage: mockPkg,
      instanceDir: 'C:\\instances\\test',
      assetsDir: 'C:\\assets',
      librariesDir: 'C:\\libraries',
      versionsDir: 'C:\\versions',
      nativesDir: 'C:\\natives',
      session: {
        username: 'ZombekHero',
        uuid: '00000000-0000-0000-0000-000000000001',
        accessToken: '0',
        userType: 'offline',
      },
      minRamMb: 2048,
      maxRamMb: 4096,
      windowWidth: 1920,
      windowHeight: 1080,
      fullscreen: false
    });

    // Check RAM args
    expect(args).toContain('-Xms2048M');
    expect(args).toContain('-Xmx4096M');

    // Check mainClass position
    const mainClassIndex = args.indexOf('net.minecraft.client.main.Main');
    expect(mainClassIndex).toBeGreaterThan(0);

    // Check JVM args are before mainClass
    const jvmArgIndex = args.indexOf('-Dminecraft.launcher.name=ZombekClient');
    expect(jvmArgIndex).toBeLessThan(mainClassIndex);

    // Check game args are after mainClass
    const usernameIndex = args.indexOf('--username');
    expect(usernameIndex).toBeGreaterThan(mainClassIndex);
    expect(args[usernameIndex + 1]).toBe('ZombekHero');

    const uuidIndex = args.indexOf('--uuid');
    expect(args[uuidIndex + 1]).toBe('00000000-0000-0000-0000-000000000001');

    const gameDirIndex = args.indexOf('--gameDir');
    expect(args[gameDirIndex + 1]).toBe('C:\\instances\\test');
  });

  it('should support legacy minecraftArguments string format', () => {
    const mockLegacyPkg = {
      id: '1.8.9',
      type: 'release',
      mainClass: 'net.minecraft.client.main.Main',
      minecraftArguments: '--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userType ${user_type}',
      libraries: []
    } as unknown as MojangVersionPackage;

    const args = GameArgumentsBuilder.buildArguments({
      versionPackage: mockLegacyPkg,
      instanceDir: 'C:\\instances\\legacy',
      assetsDir: 'C:\\assets',
      librariesDir: 'C:\\libraries',
      versionsDir: 'C:\\versions',
      nativesDir: 'C:\\natives',
      session: {
        username: 'OldSchoolPvP',
        uuid: '00000000-0000-0000-0000-000000000002',
        accessToken: '0',
        userType: 'offline',
      },
      minRamMb: 1024,
      maxRamMb: 2048
    });

    expect(args).toContain('-Xms1024M');
    expect(args).toContain('-Xmx2048M');
    expect(args).toContain('-cp');
    expect(args).toContain('net.minecraft.client.main.Main');
    expect(args).toContain('--username');
    expect(args).toContain('OldSchoolPvP');
  });
});

describe('Minecraft Engine - JavaRuntimeManager', () => {
  it('should map Mojang component names to expected major versions', () => {
    expect(JavaRuntimeManager.componentToMajorVersion('java-runtime-delta')).toBe(21);
    expect(JavaRuntimeManager.componentToMajorVersion('java-runtime-gamma')).toBe(17);
    expect(JavaRuntimeManager.componentToMajorVersion('java-runtime-beta')).toBe(16);
    expect(JavaRuntimeManager.componentToMajorVersion('java-runtime-alpha')).toBe(16);
    expect(JavaRuntimeManager.componentToMajorVersion('jre-legacy')).toBe(8);
  });
});
