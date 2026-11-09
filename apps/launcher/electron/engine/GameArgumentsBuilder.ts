import * as path from 'node:path';
import * as fs from 'node:fs';
import { GameInstaller } from './GameInstaller.js';
import type { MojangVersionPackage, MojangRule } from './types.js';
import type { GameSession } from './GameSessionProvider.js';

export interface GameLaunchConfig {
  versionPackage: MojangVersionPackage;
  instanceDir: string;
  assetsDir: string;
  librariesDir: string;
  versionsDir: string;
  nativesDir: string;
  session: GameSession;
  minRamMb?: number;
  maxRamMb?: number;
  customJvmArgs?: string[];
  customGameArgs?: string[];
  windowWidth?: number;
  windowHeight?: number;
  fullscreen?: boolean;
}

export class GameArgumentsBuilder {
  /**
   * Evaluates rules against current OS and feature flags
   */
  public static evaluateRules(rules?: MojangRule[], features?: Record<string, boolean>): boolean {
    if (!rules || rules.length === 0) return true;

    let allowed = false;
    const currentOs = process.platform === 'win32' ? 'windows' : process.platform === 'darwin' ? 'osx' : 'linux';
    const currentArch = process.arch === 'ia32' ? 'x86' : process.arch;

    for (const rule of rules) {
      let matches = true;

      if (rule.os) {
        if (rule.os.name && rule.os.name !== currentOs) {
          matches = false;
        }
        if (rule.os.arch && rule.os.arch !== currentArch) {
          matches = false;
        }
      }

      if (rule.features) {
        for (const [key, expectedValue] of Object.entries(rule.features)) {
          const actualValue = features ? Boolean(features[key]) : false;
          if (actualValue !== expectedValue) {
            matches = false;
            break;
          }
        }
      }

      if (matches) {
        allowed = rule.action === 'allow';
      }
    }

    return allowed;
  }

  /**
   * Builds the complete classpath string from verified libraries and client jar
   */
  public static buildClasspath(
    versionPackage: MojangVersionPackage,
    librariesDir: string,
    versionsDir: string
  ): string {
    const cpEntries: string[] = [];

    // Add libraries
    for (const lib of versionPackage.libraries || []) {
      if (!this.evaluateRules(lib.rules)) {
        continue;
      }

      let libRelPath: string | null = null;
      if (lib.downloads?.artifact?.path) {
        libRelPath = lib.downloads.artifact.path;
      } else if (lib.name) {
        // Native-only libraries that don't have an artifact download are skipped from classpath
        if (!lib.natives) {
          libRelPath = GameInstaller.getMavenPath(lib.name);
        }
      }

      if (libRelPath) {
        const fullLibPath = path.join(librariesDir, libRelPath);
        if (fs.existsSync(fullLibPath)) {
          cpEntries.push(fullLibPath);
        }
      }
    }

    // Add client jar (inheritsFrom points to the base vanilla client jar)
    const jarVersionId = versionPackage.inheritsFrom || versionPackage.id;
    const clientJarPath = path.join(versionsDir, jarVersionId, `${jarVersionId}.jar`);
    if (fs.existsSync(clientJarPath)) {
      cpEntries.push(clientJarPath);
    }

    return cpEntries.join(path.delimiter);
  }

  /**
   * Replaces Mojang variable placeholders in string
   */
  public static substitute(str: string, dict: Record<string, string>): string {
    let result = str;
    for (const [key, value] of Object.entries(dict)) {
      result = result.split(`\${${key}}`).join(value);
    }
    return result;
  }

  /**
   * Builds complete command line argument array for spawning the Minecraft process
   */
  public static buildArguments(config: GameLaunchConfig): string[] {
    const {
      versionPackage,
      instanceDir,
      assetsDir,
      librariesDir,
      versionsDir,
      nativesDir,
      session,
      minRamMb = 1024,
      maxRamMb = 2048,
      customJvmArgs = [],
      customGameArgs = [],
      windowWidth,
      windowHeight,
      fullscreen = false,
    } = config;

    const classpath = this.buildClasspath(versionPackage, librariesDir, versionsDir);
    const assetIndexId = versionPackage.assetIndex?.id || versionPackage.assets || 'legacy';

    const hasCustomResolution = Boolean(windowWidth && windowHeight);
    const features: Record<string, boolean> = {
      is_demo_user: false,
      has_custom_resolution: hasCustomResolution,
    };

    const dict: Record<string, string> = {
      auth_player_name: session.username,
      version_name: versionPackage.id,
      game_directory: instanceDir,
      assets_root: assetsDir,
      assets_index_name: assetIndexId,
      auth_uuid: session.uuid,
      auth_access_token: session.accessToken,
      user_type: session.userType,
      version_type: versionPackage.type || 'release',
      natives_directory: nativesDir,
      launcher_name: 'ZombekClient',
      launcher_version: '1.0.0',
      classpath,
      classpath_separator: path.delimiter,
      library_directory: librariesDir,
      auth_xuid: session.xuid || '',
      clientid: '0',
      user_properties: '{}',
      resolution_width: windowWidth ? String(windowWidth) : '854',
      resolution_height: windowHeight ? String(windowHeight) : '480',
    };

    const jvmArgs: string[] = [];

    // Memory settings
    jvmArgs.push(`-Xms${minRamMb}M`);
    jvmArgs.push(`-Xmx${maxRamMb}M`);

    // On Windows, use the Windows Root Certificate Store to prevent SSL handshake / cert path errors
    if (process.platform === 'win32') {
      jvmArgs.push('-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT');
    }

    // User-provided JVM arguments
    if (customJvmArgs.length > 0) {
      jvmArgs.push(...customJvmArgs);
    }

    let hasClasspathInJvm = false;

    // JVM arguments from manifest
    if (versionPackage.arguments?.jvm) {
      for (const item of versionPackage.arguments.jvm) {
        if (typeof item === 'string') {
          const substituted = this.substitute(item, dict);
          jvmArgs.push(substituted);
          if (substituted === '-cp' || substituted.startsWith('-Djava.class.path=')) {
            hasClasspathInJvm = true;
          }
        } else if (item && typeof item === 'object') {
          if (this.evaluateRules(item.rules, features)) {
            const values = Array.isArray(item.value) ? item.value : [item.value];
            for (const val of values) {
              const substituted = this.substitute(val, dict);
              jvmArgs.push(substituted);
              if (substituted === '-cp' || substituted.startsWith('-Djava.class.path=')) {
                hasClasspathInJvm = true;
              }
            }
          }
        }
      }
    } else {
      // Legacy fallback JVM args (<1.13)
      jvmArgs.push(`-Djava.library.path=${nativesDir}`);
      jvmArgs.push('-Dminecraft.launcher.name=ZombekClient');
      jvmArgs.push('-Dminecraft.launcher.version=1.0.0');
      jvmArgs.push('-cp');
      jvmArgs.push(classpath);
      hasClasspathInJvm = true;
    }

    // Ensure classpath is present
    if (!hasClasspathInJvm) {
      jvmArgs.push('-cp', classpath);
    }

    // Main class
    const mainClass = versionPackage.mainClass || 'net.minecraft.client.main.Main';

    // Game arguments
    const gameArgs: string[] = [];

    if (versionPackage.arguments?.game) {
      for (const item of versionPackage.arguments.game) {
        if (typeof item === 'string') {
          gameArgs.push(this.substitute(item, dict));
        } else if (item && typeof item === 'object') {
          if (this.evaluateRules(item.rules, features)) {
            const values = Array.isArray(item.value) ? item.value : [item.value];
            for (const val of values) {
              gameArgs.push(this.substitute(val, dict));
            }
          }
        }
      }
    } else if (versionPackage.minecraftArguments) {
      // Legacy string format
      const substituted = this.substitute(versionPackage.minecraftArguments, dict);
      const tokens = substituted.split(/\s+/).filter(Boolean);
      gameArgs.push(...tokens);
    }

    // Fallback if custom resolution wasn't handled by manifest rules
    if (hasCustomResolution && !gameArgs.includes('--width')) {
      gameArgs.push('--width', String(windowWidth), '--height', String(windowHeight));
    }

    if (fullscreen && !gameArgs.includes('--fullscreen')) {
      gameArgs.push('--fullscreen');
    }

    // User-provided Game arguments
    if (customGameArgs.length > 0) {
      gameArgs.push(...customGameArgs);
    }

    return [...jvmArgs, mainClass, ...gameArgs];
  }
}
