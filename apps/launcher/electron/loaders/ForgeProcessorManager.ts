import * as fs from 'node:fs';
import * as path from 'node:path';
import * as cp from 'node:child_process';
import { GameInstaller } from '../engine/GameInstaller.js';
import type { ForgeInstallProfile, ForgeProcessorEntry } from '@zombek/shared-types';

export interface ForgeProcessorContext {
  installerJarPath: string;
  minecraftJarPath: string;
  rootDir: string;
  librariesDir: string;
  javaPath: string;
  tempDir: string;
  onProgress?: (message: string, current: number, total: number) => void;
  onLog?: (line: string) => void;
}

export class ForgeProcessorManager {
  /**
   * Resolves a Maven coordinate string to a file path inside librariesDir.
   * Handles coordinates like "group:artifact:version", "group:artifact:version:classifier",
   * and with extensions like "group:artifact:version@zip" or "[group:artifact:version@tsrg]".
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
   * Substitutes variable references in a processor argument.
   * Handles:
   * - {INSTALLER} -> installer JAR path
   * - {MINECRAFT_JAR} -> vanilla client JAR path
   * - {ROOT} -> root directory
   * - {LIBRARY_DIR} -> libraries directory
   * - {KEY} -> data[KEY].client or data[KEY].server
   * - [maven:coord] -> local path to maven library
   * - 'literal' -> literal string
   * - /path/in/archive -> extracted file path
   */
  public static substituteArgument(
    arg: string,
    context: ForgeProcessorContext,
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

    // Quoted literal value 'literal'
    if (result.startsWith("'") && result.endsWith("'")) {
      return result.slice(1, -1);
    }

    // Archive path /data/...
    if (result.startsWith('/')) {
      return path.join(context.tempDir, result.slice(1));
    }

    // Replace embedded {ROOT} or other placeholders within complex strings
    result = result.replace(/\{INSTALLER\}/g, context.installerJarPath);
    result = result.replace(/\{MINECRAFT_JAR\}/g, context.minecraftJarPath);
    result = result.replace(/\{ROOT\}/g, context.rootDir);
    result = result.replace(/\{LIBRARY_DIR\}/g, context.librariesDir);

    return result;
  }

  /**
   * Filters processors applicable for Minecraft client side.
   */
  public static getClientProcessors(profile: ForgeInstallProfile): ForgeProcessorEntry[] {
    if (!profile.processors || profile.processors.length === 0) {
      return [];
    }

    return profile.processors.filter((p) => {
      if (!p.sides || p.sides.length === 0) return true;
      return p.sides.includes('client');
    });
  }

  /**
   * Inspects a JAR file and extracts its Main-Class attribute from META-INF/MANIFEST.MF
   */
  public static async getJarMainClass(jarPath: string): Promise<string> {
    if (!fs.existsSync(jarPath)) {
      throw new Error(`Plik procesora nie istnieje: ${jarPath}`);
    }

    return new Promise((resolve) => {
      // Use tar to read MANIFEST.MF
      const proc = cp.spawn('tar.exe', ['-xOf', jarPath, 'META-INF/MANIFEST.MF'], {
        stdio: ['ignore', 'pipe', 'pipe'],
        windowsHide: true,
      });

      let manifestContent = '';
      proc.stdout.on('data', (d) => (manifestContent += d.toString('utf-8')));

      proc.on('close', (code) => {
        if (code === 0 && manifestContent) {
          const match = manifestContent.match(/Main-Class:\s*([^\r\n]+)/);
          if (match && match[1]) {
            return resolve(match[1].trim());
          }
        }
        // Fallback default for common Forge installer tools
        if (jarPath.includes('installertools')) {
          return resolve('net.minecraftforge.installertools.ConsoleTool');
        }
        if (jarPath.includes('binarypatcher')) {
          return resolve('net.minecraftforge.binarypatcher.ConsoleTool');
        }
        if (jarPath.includes('ForgeAutoRenamingTool')) {
          return resolve('net.minecraftforge.fart.Main');
        }
        resolve('net.minecraftforge.installer.ConsoleTool');
      });

      proc.on('error', () => {
        resolve('net.minecraftforge.installertools.ConsoleTool');
      });
    });
  }

  /**
   * Executes a single Forge processor safely via spawn
   */
  public static async executeProcessor(
    processor: ForgeProcessorEntry,
    context: ForgeProcessorContext,
    data: Record<string, { client?: string; server?: string }> = {}
  ): Promise<void> {
    const processorJarPath = this.resolveMavenPath(processor.jar, context.librariesDir);
    if (!fs.existsSync(processorJarPath)) {
      throw new Error(`Brak pliku JAR procesora: ${processorJarPath}`);
    }

    // Build classpath for processor
    const cpEntries: string[] = [processorJarPath];
    for (const lib of processor.classpath || []) {
      const libPath = this.resolveMavenPath(lib, context.librariesDir);
      if (fs.existsSync(libPath)) {
        cpEntries.push(libPath);
      }
    }

    const fullClasspath = cpEntries.join(path.delimiter);
    const mainClass = await this.getJarMainClass(processorJarPath);

    // Substitute processor arguments
    const finalArgs: string[] = [];
    for (const rawArg of processor.args || []) {
      const substituted = this.substituteArgument(rawArg, context, data);
      finalArgs.push(substituted);
    }

    context.onLog?.(`Uruchamianie procesora: ${path.basename(processorJarPath)} (${mainClass})`);

    const jvmArgs = [
      '-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT',
      '-cp',
      fullClasspath,
      mainClass,
      ...finalArgs,
    ];

    await new Promise<void>((resolve, reject) => {
      const child = cp.spawn(context.javaPath, jvmArgs, {
        cwd: context.rootDir,
        stdio: ['ignore', 'pipe', 'pipe'],
      });

      child.stdout.on('data', (d) => {
        const text = d.toString('utf-8').trim();
        if (text) context.onLog?.(`[PROCESSOR STDOUT]: ${text}`);
      });

      child.stderr.on('data', (d) => {
        const text = d.toString('utf-8').trim();
        if (text) context.onLog?.(`[PROCESSOR STDERR]: ${text}`);
      });

      child.on('error', (err) => {
        reject(new Error(`Nie udało się uruchomić procesora ${processor.jar}: ${err.message}`));
      });

      child.on('close', (code) => {
        if (code === 0) {
          resolve();
        } else {
          reject(new Error(`Procesor ${processor.jar} zakończył się błędem (kod: ${code})`));
        }
      });
    });
  }

  /**
   * Executes the entire queue of client-side processors in sequence
   */
  public static async executeQueue(
    profile: ForgeInstallProfile,
    context: ForgeProcessorContext
  ): Promise<void> {
    const processors = this.getClientProcessors(profile);
    const data = profile.data || {};
    const total = processors.length;

    context.onLog?.(`Rozpoczynanie przetwarzania kolejki processorów Forge (łącznie: ${total})...`);

    for (let i = 0; i < total; i++) {
      const p = processors[i];
      context.onProgress?.(`Wykonywanie procesora ${i + 1}/${total}: ${p.jar}`, i + 1, total);
      await this.executeProcessor(p, context, data);
    }

    context.onLog?.('Wszystkie procesory Forge zakończyły pracę pomyślnie.');
  }
}
