import * as fs from 'node:fs';
import * as path from 'node:path';
import * as cp from 'node:child_process';
import { EnginePaths } from './paths.js';
import { DownloadManager } from './DownloadManager.js';
import type { DownloadTask, MojangJavaRuntimeManifestAll, MojangJavaRuntimeFilesManifest } from './types.js';
import type { JavaDetectionResult } from '@zombek/shared-types';

export const MOJANG_JAVA_ALL_MANIFEST_URL =
  'https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json';

export class JavaRuntimeManager {
  public static getPlatformKey(): string {
    const isWindows = process.platform === 'win32';
    const isMac = process.platform === 'darwin';
    const isArm = process.arch === 'arm64';

    if (isWindows) {
      if (isArm) return 'windows-arm64';
      return process.arch === 'ia32' ? 'windows-x86' : 'windows-x64';
    } else if (isMac) {
      return isArm ? 'mac-os-arm64' : 'mac-os';
    } else {
      return process.arch === 'ia32' ? 'linux-i386' : 'linux';
    }
  }

  public static async detectJavaExecutable(candidatePath?: string): Promise<JavaDetectionResult> {
    const javaPath = candidatePath || (process.platform === 'win32' ? 'java.exe' : 'java');

    return new Promise<JavaDetectionResult>((resolve) => {
      try {
        const proc = cp.spawn(javaPath, ['-version'], { stdio: ['ignore', 'pipe', 'pipe'] });
        let output = '';

        proc.stdout.on('data', (d) => (output += d.toString()));
        proc.stderr.on('data', (d) => (output += d.toString()));

        proc.on('error', (err) => {
          resolve({
            valid: false,
            path: javaPath,
            error: `Błąd wykonania Javy (${javaPath}): ${err.message}`
          });
        });

        proc.on('close', (code) => {
          if (code === 0 || output.includes('version')) {
            const versionMatch = output.match(/version\s+"?([0-9._]+)"?/i);
            const rawVersion = versionMatch ? versionMatch[1] : 'unknown';

            let major = 8;
            if (rawVersion.startsWith('1.')) {
              major = parseInt(rawVersion.split('.')[1], 10) || 8;
            } else {
              major = parseInt(rawVersion.split('.')[0], 10) || 17;
            }

            resolve({
              valid: true,
              version: rawVersion,
              majorVersion: major,
              path: javaPath
            });
          } else {
            resolve({
              valid: false,
              path: javaPath,
              error: `Polecenie java -version zwróciło kod ${code}: ${output}`
            });
          }
        });
      } catch (err: any) {
        resolve({
          valid: false,
          path: javaPath,
          error: err.message
        });
      }
    });
  }

  public static async prepareJavaRuntime(
    requiredComponent: string = 'java-runtime-gamma',
    downloadManager?: DownloadManager
  ): Promise<string> {
    EnginePaths.ensureAllBaseDirs();
    const runtimeBase = path.join(EnginePaths.getRuntimesDir(), requiredComponent);
    const exeName = process.platform === 'win32' ? 'javaw.exe' : 'java';
    const fallbackExeName = process.platform === 'win32' ? 'java.exe' : 'java';

    // Check if runtime is already fully downloaded in runtimes dir
    const potentialPaths = [
      path.join(runtimeBase, 'bin', exeName),
      path.join(runtimeBase, 'bin', fallbackExeName),
      path.join(runtimeBase, 'jre', 'bin', exeName),
      path.join(runtimeBase, 'Contents', 'Home', 'bin', 'java')
    ];

    for (const p of potentialPaths) {
      if (fs.existsSync(p)) {
        return p;
      }
    }

    // Check system Java first if it matches
    const systemJava = await this.detectJavaExecutable();
    const requiredMajor = this.componentToMajorVersion(requiredComponent);

    if (systemJava.valid && systemJava.majorVersion) {
      // If system Java matches requirement (e.g. required 17 and system has 17)
      if (systemJava.majorVersion === requiredMajor || (requiredMajor >= 17 && systemJava.majorVersion >= 17)) {
        return systemJava.path;
      }
    }

    // Otherwise download runtime from Mojang
    const platform = this.getPlatformKey();
    const manifestRes = await fetch(MOJANG_JAVA_ALL_MANIFEST_URL);
    if (!manifestRes.ok) {
      // If download fails and system Java is valid, fallback to system Java
      if (systemJava.valid) return systemJava.path;
      throw new Error(`Nie udało się pobrać manifestu Java Runtime: HTTP ${manifestRes.status}`);
    }

    const allManifest = (await manifestRes.json()) as MojangJavaRuntimeManifestAll;
    const platformRuntimes = allManifest[platform];
    if (!platformRuntimes || !platformRuntimes[requiredComponent] || platformRuntimes[requiredComponent].length === 0) {
      // fallback to system Java
      if (systemJava.valid) return systemJava.path;
      throw new Error(`Brak dostępnego środowiska ${requiredComponent} dla platformy ${platform}`);
    }

    const componentInfo = platformRuntimes[requiredComponent][0];
    const filesRes = await fetch(componentInfo.manifest.url);
    if (!filesRes.ok) {
      if (systemJava.valid) return systemJava.path;
      throw new Error(`Nie udało się pobrać listy plików Javy: HTTP ${filesRes.status}`);
    }

    const filesManifest = (await filesRes.json()) as MojangJavaRuntimeFilesManifest;
    const downloadTasks: DownloadTask[] = [];

    for (const [relPath, fileEntry] of Object.entries(filesManifest.files)) {
      if (fileEntry.type === 'directory') {
        const dirPath = path.join(runtimeBase, relPath);
        if (!fs.existsSync(dirPath)) {
          fs.mkdirSync(dirPath, { recursive: true });
        }
      } else if (fileEntry.type === 'file' && fileEntry.downloads?.raw) {
        const dest = path.join(runtimeBase, relPath);
        downloadTasks.push({
          id: `java-${relPath}`,
          url: fileEntry.downloads.raw.url,
          destination: dest,
          sha1: fileEntry.downloads.raw.sha1,
          size: fileEntry.downloads.raw.size,
          description: `Java Runtime: ${path.basename(relPath)}`
        });
      }
    }

    const dm = downloadManager || new DownloadManager(8);
    await dm.downloadBatch(downloadTasks, `Pobieranie środowiska Java (${requiredComponent})...`);

    // Find the installed executable
    for (const p of potentialPaths) {
      if (fs.existsSync(p)) {
        // Set permissions on unix
        if (process.platform !== 'win32') {
          try {
            fs.chmodSync(p, 0o755);
          } catch {
            // ignore
          }
        }
        return p;
      }
    }

    if (systemJava.valid) return systemJava.path;
    throw new Error(`Nie odnaleziono pliku wykonywalnego Javy w ${runtimeBase}`);
  }

  public static componentToMajorVersion(component: string): number {
    switch (component) {
      case 'java-runtime-delta':
        return 21;
      case 'java-runtime-gamma':
        return 17;
      case 'java-runtime-beta':
      case 'java-runtime-alpha':
        return 16;
      case 'jre-legacy':
      default:
        return 8;
    }
  }
}
