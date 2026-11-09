import * as cp from 'node:child_process';
import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from './paths.js';

export class ZipExtractor {
  public static async extractJarNatives(jarPath: string, targetDir: string, excludePatterns: string[] = ['META-INF']): Promise<void> {
    if (!fs.existsSync(jarPath)) {
      throw new Error(`Plik JAR nie istnieje: ${jarPath}`);
    }

    if (!fs.existsSync(targetDir)) {
      await fs.promises.mkdir(targetDir, { recursive: true });
    }

    // Windows 10/11 native bsdtar
    return new Promise<void>((resolve, reject) => {
      const args = ['-xf', jarPath, '-C', targetDir];
      for (const exc of excludePatterns) {
        args.push(`--exclude=${exc}*`);
      }

      const proc = cp.spawn('tar.exe', args, {
        stdio: ['ignore', 'pipe', 'pipe'],
        windowsHide: true
      });

      let errOutput = '';
      proc.stderr.on('data', (d) => (errOutput += d.toString()));

      proc.on('error', (err) => {
        // If tar.exe fails, fallback to powershell Expand-Archive
        this.fallbackExtract(jarPath, targetDir, excludePatterns).then(resolve).catch(reject);
      });

      proc.on('close', (code) => {
        if (code === 0) {
          this.sanitizeNativesDir(targetDir, excludePatterns).then(resolve).catch(reject);
        } else {
          this.fallbackExtract(jarPath, targetDir, excludePatterns).then(resolve).catch(reject);
        }
      });
    });
  }

  private static async fallbackExtract(jarPath: string, targetDir: string, excludePatterns: string[]): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      const psScript = `Expand-Archive -LiteralPath '${jarPath.replace(/'/g, "''")}' -DestinationPath '${targetDir.replace(/'/g, "''")}' -Force`;
      const proc = cp.spawn('powershell.exe', ['-NoProfile', '-NonInteractive', '-Command', psScript], {
        stdio: ['ignore', 'pipe', 'pipe'],
        windowsHide: true
      });

      proc.on('error', reject);
      proc.on('close', async (code) => {
        if (code === 0) {
          await this.sanitizeNativesDir(targetDir, excludePatterns);
          resolve();
        } else {
          reject(new Error(`Błąd rozpakowywania natives: kod ${code}`));
        }
      });
    });
  }

  private static async sanitizeNativesDir(targetDir: string, excludePatterns: string[]): Promise<void> {
    // Clean up META-INF or other excluded directories if created
    for (const exc of excludePatterns) {
      const excPath = path.join(targetDir, exc);
      if (fs.existsSync(excPath)) {
        try {
          await fs.promises.rm(excPath, { recursive: true, force: true });
        } catch {
          // ignore
        }
      }
    }
  }
}
