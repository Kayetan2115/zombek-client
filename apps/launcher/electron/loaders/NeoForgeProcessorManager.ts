import * as fs from 'node:fs';
import * as path from 'node:path';
import * as cp from 'node:child_process';
import type { NeoForgeInstallProfile, NeoForgeProcessorEntry } from '@zombek/shared-types';

export interface NeoForgeProcessorContext {
  installerJarPath: string;
  minecraftJarPath: string;
  rootDir: string;
  librariesDir: string;
  javaPath: string;
  tempDir: string;
  onProgress?: (message: string, current: number, total: number) => void;
  onLog?: (line: string) => void;
}

export class NeoForgeProcessorManager {
  /**
   * Resolves a Maven coordinate string to a file path inside librariesDir.
   * Handles coordinates like "group:artifact:version", "group:artifact:version:classifier",
   * and with extensions like "group:artifact:version@zip" or "[group:artifact:version@jar]".
   */
  public static resolveMavenPath(coord: string, librariesDir: string): string {
    let clean = coord.trim();
    if (clean.startsWith('[') && clean.endsWith(']')) {
      clean = clean.slice(1, -1);
    }

    let ext = 'jar';
    const atIndex = clean.lastIndexOf('@');
    if (atIndex > -1) {
      ext = clean.slice(atIndex + 1);
      clean = clean.slice(0, atIndex);
    }

    const parts = clean.split(':');
    if (parts.length < 3) {
      return path.join(librariesDir, clean);
    }

    const group = parts[0].replace(/\./g, '/');
    const artifact = parts[1];
    const version = parts[2];
    const classifier = parts[3];

    const filename = classifier
      ? `${artifact}-${version}-${classifier}.${ext}`
      : `${artifact}-${version}.${ext}`;

    return path.join(librariesDir, group, artifact, version, filename);
  }

  /**
   * Substitutes variable references in a processor argument according to NeoForge format.
   * Handles:
   * - {INSTALLER} -> installer JAR path
   * - {MINECRAFT_JAR} -> vanilla client JAR path
   * - {ROOT} -> root staging directory
   * - {LIBRARY_DIR} -> libraries directory
   * - {KEY} -> data[KEY].client or data[KEY].server
   * - [maven:coord] -> local path to maven library
   * - 'literal' -> literal string (unquoted)
   * - /data/... -> path to extracted archive resource
   */
  public static substituteArgument(
    arg: string,
    context: NeoForgeProcessorContext,
    data: Record<string, { client?: string; server?: string }> = {}
  ): string {
    let result = arg.trim();

    // Direct context substitutions
    if (result === '{INSTALLER}') return context.installerJarPath;
    if (result === '{MINECRAFT_JAR}') return context.minecraftJarPath;
    if (result === '{ROOT}') return context.rootDir;
    if (result === '{LIBRARY_DIR}') return context.librariesDir;

    // Check if it's a data key reference {DATA_KEY}
    if (result.startsWith('{') && result.endsWith('}')) {
      const dataKey = result.slice(1, -1);
      const dataEntry = data[dataKey];
      if (dataEntry) {
        const rawValue = dataEntry.client || dataEntry.server || '';
        return this.substituteArgument(rawValue, context, data);
      }
    }

    // Maven bracket reference [group:artifact:version]
    if (result.startsWith('[') && result.endsWith(']')) {
      return this.resolveMavenPath(result, context.librariesDir);
    }

    // Literal string wrapped in single quotes: 'something'
    if (result.startsWith("'") && result.endsWith("'") && result.length >= 2) {
      return result.slice(1, -1);
    }

    // File path in archive data: /data/... or data/...
    if (result.startsWith('/data/') || result.startsWith('data/')) {
      const rel = result.startsWith('/') ? result.slice(1) : result;
      return path.join(context.tempDir, rel);
    }

    // Compound substitutions like "{ROOT}/libraries/..."
    if (result.includes('{ROOT}')) {
      result = result.replace(/\{ROOT\}/g, context.rootDir);
    }
    if (result.includes('{LIBRARY_DIR}')) {
      result = result.replace(/\{LIBRARY_DIR\}/g, context.librariesDir);
    }
    if (result.includes('{MINECRAFT_JAR}')) {
      result = result.replace(/\{MINECRAFT_JAR\}/g, context.minecraftJarPath);
    }
    if (result.includes('{INSTALLER}')) {
      result = result.replace(/\{INSTALLER\}/g, context.installerJarPath);
    }

    return result;
  }

  /**
   * Filters out server-only processors from the installation profile.
   */
  public static filterClientProcessors(processors: NeoForgeProcessorEntry[]): NeoForgeProcessorEntry[] {
    return processors.filter((proc) => {
      if (!proc.sides || proc.sides.length === 0) {
        return true;
      }
      return proc.sides.includes('client');
    });
  }

  /**
   * Reads Main-Class from the JAR manifest if needed.
   */
  public static async getMainClassFromJar(jarPath: string): Promise<string | null> {
    if (!fs.existsSync(jarPath)) return null;

    try {
      return new Promise<string | null>((resolve) => {
        const proc = cp.spawn(
          'tar.exe',
          ['-xf', jarPath, '-O', 'META-INF/MANIFEST.MF'],
          { stdio: ['ignore', 'pipe', 'pipe'], windowsHide: true }
        );
        let manifest = '';
        proc.stdout.on('data', (d) => (manifest += d.toString('utf-8')));
        proc.on('close', (code) => {
          if (code === 0 && manifest) {
            const match = manifest.match(/^Main-Class:\s*([^\r\n]+)/m);
            if (match && match[1]) {
              resolve(match[1].trim());
              return;
            }
          }
          resolve(null);
        });
        proc.on('error', () => resolve(null));
      });
    } catch {
      return null;
    }
  }

  /**
   * Executes a single NeoForge processor using the JVM.
   */
  public static async executeProcessor(
    processor: NeoForgeProcessorEntry,
    context: NeoForgeProcessorContext,
    data: Record<string, { client?: string; server?: string }> = {}
  ): Promise<void> {
    const jarPath = this.resolveMavenPath(processor.jar, context.librariesDir);
    if (!fs.existsSync(jarPath)) {
      throw new Error(`Brak pliku JAR procesora NeoForge: ${jarPath}`);
    }

    // Build processor classpath
    const cpEntries: string[] = [];
    for (const cpItem of processor.classpath || []) {
      const resolved = this.resolveMavenPath(cpItem, context.librariesDir);
      if (fs.existsSync(resolved)) {
        cpEntries.push(resolved);
      }
    }
    // Ensure processor jar is also on classpath
    if (!cpEntries.includes(jarPath)) {
      cpEntries.push(jarPath);
    }

    const mainClass = await this.getMainClassFromJar(jarPath);
    if (!mainClass) {
      throw new Error(`Nie udało się odnaleźć Main-Class w procesorze NeoForge: ${jarPath}`);
    }

    // Substitute processor arguments
    const finalArgs: string[] = [];
    for (const arg of processor.args || []) {
      finalArgs.push(this.substituteArgument(arg, context, data));
    }

    context.onLog?.(`[NEOFORGE PROCESSOR]: Uruchamianie ${path.basename(jarPath)} (${mainClass})`);

    const jvmArgs = [
      '-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT',
      '-cp',
      cpEntries.join(path.delimiter),
      mainClass,
      ...finalArgs,
    ];

    await new Promise<void>((resolve, reject) => {
      const proc = cp.spawn(context.javaPath, jvmArgs, {
        cwd: context.rootDir,
        stdio: ['ignore', 'pipe', 'pipe'],
        windowsHide: true,
      });

      let errOutput = '';
      proc.stdout.on('data', (d) => {
        const text = d.toString('utf-8').trim();
        if (text) context.onLog?.(`[PROCESSOR STDOUT]: ${text}`);
      });
      proc.stderr.on('data', (d) => {
        const text = d.toString('utf-8').trim();
        if (text) {
          errOutput += text + '\n';
          context.onLog?.(`[PROCESSOR STDERR]: ${text}`);
        }
      });

      proc.on('close', (code) => {
        if (code === 0) {
          resolve();
        } else {
          reject(new Error(`Procesor NeoForge zakończył się błędem (kod: ${code}): ${errOutput.slice(0, 300)}`));
        }
      });

      proc.on('error', (err) => {
        reject(new Error(`Błąd wykonania procesora NeoForge: ${err.message}`));
      });
    });
  }
}
