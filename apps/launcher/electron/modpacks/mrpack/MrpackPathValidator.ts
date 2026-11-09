import * as path from 'node:path';

export class MrpackPathValidator {
  private static readonly WINDOWS_RESERVED_NAMES = new Set([
    'CON',
    'PRN',
    'AUX',
    'NUL',
    'COM1',
    'COM2',
    'COM3',
    'COM4',
    'COM5',
    'COM6',
    'COM7',
    'COM8',
    'COM9',
    'LPT1',
    'LPT2',
    'LPT3',
    'LPT4',
    'LPT5',
    'LPT6',
    'LPT7',
    'LPT8',
    'LPT9',
  ]);

  /**
   * Validates whether a relative path inside an mrpack archive or override directory
   * is strictly safe and does not perform path traversal, drive letter jumping,
   * UNC resolution, or invalid character usage.
   */
  public static validateRelativePath(relPath: string): {
    valid: boolean;
    normalized?: string;
    error?: string;
  } {
    if (!relPath || typeof relPath !== 'string') {
      return { valid: false, error: 'Ścieżka nie może być pusta.' };
    }

    // 1. Check for null bytes
    if (relPath.includes('\0')) {
      return { valid: false, error: 'Wykryto niedozwolony znak null byte (\\0).' };
    }

    // 2. Normalize separators to forward slashes for cross-platform checking
    const forwardSlashed = relPath.replace(/\\/g, '/');

    // 3. Absolute path checks (leading slash or backslash)
    if (forwardSlashed.startsWith('/')) {
      return { valid: false, error: 'Ścieżka absolutna jest zabroniona w paczce mrpack.' };
    }

    // 4. Windows drive letters (e.g. C:, D:)
    if (/^[a-zA-Z]:/.test(forwardSlashed)) {
      return { valid: false, error: 'Ścieżka z literą dysku Windows jest zabroniona.' };
    }

    // 5. UNC paths (e.g. //server/share or \\server\share)
    if (forwardSlashed.startsWith('//') || relPath.startsWith('\\\\')) {
      return { valid: false, error: 'Ścieżki sieciowe UNC są zabronione.' };
    }

    // 6. Split segments and check each
    const segments = forwardSlashed.split('/');
    for (const segment of segments) {
      if (segment === '..') {
        return {
          valid: false,
          error: 'Próba wyjścia poza katalog nadrzędny (.. / Zip Slip).',
        };
      }

      // Check Windows reserved names (e.g. "CON", "NUL", "AUX.txt")
      const baseName = segment.split('.')[0].toUpperCase();
      if (this.WINDOWS_RESERVED_NAMES.has(baseName)) {
        return {
          valid: false,
          error: `Wykryto zastrzeżoną nazwę urządzenia Windows: '${segment}'.`,
        };
      }

      // Check for forbidden characters in Windows file systems: < > : " | ? *
      if (/[<>:"|?*]/.test(segment)) {
        return {
          valid: false,
          error: `Ścieżka zawiera niedozwolone znaki systemowe (< > : " | ? *): '${segment}'.`,
        };
      }
    }

    // 7. Canonical normalization
    // Use path.posix to maintain clean slash representation
    const normalized = path.posix.normalize(forwardSlashed);
    if (normalized === '.' || normalized.startsWith('../') || normalized === '..') {
      return { valid: false, error: 'Znormalizowana ścieżka prowadzi poza katalog docelowy.' };
    }

    return { valid: true, normalized };
  }

  /**
   * Asserts that a relative path stays strictly within the base directory.
   * Returns the final resolved absolute path or throws an Error.
   */
  public static assertSafePath(baseDir: string, relPath: string): string {
    const validation = this.validateRelativePath(relPath);
    if (!validation.valid || !validation.normalized) {
      throw new Error(`Niebezpieczna ścieżka pliku [${relPath}]: ${validation.error}`);
    }

    const resolvedBase = path.resolve(baseDir);
    // Convert posix normalized path to platform-specific path
    const platformRel = validation.normalized.split('/').join(path.sep);
    const resolvedTarget = path.resolve(resolvedBase, platformRel);

    if (resolvedTarget !== resolvedBase && !resolvedTarget.startsWith(resolvedBase + path.sep)) {
      throw new Error(
        `Naruszenie granic katalogu (Path Traversal)! Cel: [${resolvedTarget}] poza bazą: [${resolvedBase}]`
      );
    }

    return resolvedTarget;
  }

  public static isValidRelativePath(relPath: string): boolean {
    return this.validateRelativePath(relPath).valid;
  }

  public static sanitizePath(relPath: string): string {
    const res = this.validateRelativePath(relPath);
    if (!res.valid || !res.normalized) {
      throw new Error(res.error || 'Nieprawidłowa ścieżka');
    }
    return res.normalized;
  }

  public static assertSafeSubpath(baseDir: string, relPath: string): string {
    return this.assertSafePath(baseDir, relPath);
  }
}
