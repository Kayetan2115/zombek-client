import * as fs from 'node:fs';
import * as path from 'node:path';
import * as crypto from 'node:crypto';
import { EnginePaths } from '../engine/paths.js';
import type { ModrinthVersionFile, ModrinthVersionFileHashes } from '@zombek/shared-types';

export interface DownloadProgressInfo {
  downloadedBytes: number;
  totalBytes: number;
  percent: number;
  speedMb: number;
  filename: string;
}

export type DownloadProgressCallback = (progress: DownloadProgressInfo) => void;

export class ModrinthDownloader {
  private static readonly ALLOWED_HOSTS = [
    'cdn.modrinth.com',
    'edge.modrinth.com',
    'api.modrinth.com',
  ];

  /**
   * Asserts that a download URL is strictly HTTPS and originates from an allowed Modrinth CDN domain.
   */
  public static assertSafeDownloadUrl(urlStr: string): URL {
    let parsed: URL;
    try {
      parsed = new URL(urlStr);
    } catch {
      throw new Error(`Nieprawidłowy adres URL pliku: ${urlStr}`);
    }

    // Protocol must be HTTPS
    if (parsed.protocol !== 'https:') {
      throw new Error(`Odrzucono niebezpieczny protokół pobierania (${parsed.protocol}). Wymagany HTTPS.`);
    }

    // Check host whitelist
    const host = parsed.hostname.toLowerCase();
    const isAllowedHost =
      this.ALLOWED_HOSTS.includes(host) ||
      (host.endsWith('.modrinth.com') && !host.includes('..'));

    if (!isAllowedHost) {
      throw new Error(
        `Odrzucono pobieranie z niezaufanej domeny: ${host}. Dozwolone wyłącznie oficjalne serwery Modrinth CDN.`
      );
    }

    // Reject localhost and private IP addresses
    if (
      host === 'localhost' ||
      host === '127.0.0.1' ||
      host === '0.0.0.0' ||
      host.startsWith('192.168.') ||
      host.startsWith('10.') ||
      host.startsWith('172.16.')
    ) {
      throw new Error(`Odrzucono próbę pobierania z adresu lokalnego / prywatnego: ${host}`);
    }

    return parsed;
  }

  /**
   * Calculates SHA-512 hash of a file.
   */
  public static async calculateSha512(filePath: string): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      const hash = crypto.createHash('sha512');
      const stream = fs.createReadStream(filePath);
      stream.on('data', (d) => hash.update(d));
      stream.on('end', () => resolve(hash.digest('hex')));
      stream.on('error', (err) => reject(err));
    });
  }

  /**
   * Calculates SHA-1 hash of a file.
   */
  public static async calculateSha1(filePath: string): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      const hash = crypto.createHash('sha1');
      const stream = fs.createReadStream(filePath);
      stream.on('data', (d) => hash.update(d));
      stream.on('end', () => resolve(hash.digest('hex')));
      stream.on('error', (err) => reject(err));
    });
  }

  /**
   * Verifies file integrity against provided Modrinth hashes (SHA-512 preferred, SHA-1 fallback).
   */
  public static async verifyFileHash(
    filePath: string,
    hashes: ModrinthVersionFileHashes
  ): Promise<boolean> {
    try {
      if (!fs.existsSync(filePath)) return false;
      if (hashes.sha512) {
        const computed = await this.calculateSha512(filePath);
        return computed.toLowerCase() === hashes.sha512.toLowerCase();
      }
      if (hashes.sha1) {
        const computed = await this.calculateSha1(filePath);
        return computed.toLowerCase() === hashes.sha1.toLowerCase();
      }
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Downloads a Modrinth version file to a temporary location with full hash and size verification.
   * Returns the path to the verified temporary file.
   */
  public static async downloadAndVerify(
    file: ModrinthVersionFile,
    onProgress?: DownloadProgressCallback
  ): Promise<string> {
    this.assertSafeDownloadUrl(file.url);

    const tempDir = path.join(EnginePaths.getGameDataDir(), 'temp-downloads');
    if (!fs.existsSync(tempDir)) {
      await fs.promises.mkdir(tempDir, { recursive: true });
    }

    const safeFilename = path.basename(file.filename);
    const tempFilePath = path.join(
      tempDir,
      `modrinth-${Date.now()}-${Math.random().toString(36).slice(2, 7)}-${safeFilename}`
    );

    const startTime = Date.now();

    try {
      const res = await fetch(file.url, {
        headers: {
          'User-Agent': 'ZombekClient/0.1.0 (https://github.com/zombekclient/zombekclient)',
        },
      });

      if (!res.ok) {
        throw new Error(`Błąd pobierania pliku ${file.filename} (HTTP ${res.status}): ${res.statusText}`);
      }

      const totalBytes = file.size || parseInt(res.headers.get('content-length') || '0', 10);
      let downloadedBytes = 0;

      // Stream the response to temporary file
      const fileStream = fs.createWriteStream(tempFilePath);

      if (!res.body) {
        throw new Error('Odpowiedź serwera nie zawiera strumienia danych.');
      }

      // @ts-ignore Node.js web stream reader
      const reader = res.body.getReader();

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = Buffer.from(value);
        fileStream.write(chunk);
        downloadedBytes += chunk.length;

        if (onProgress && totalBytes > 0) {
          const elapsedSec = Math.max((Date.now() - startTime) / 1000, 0.1);
          const speedMb = Number(((downloadedBytes / (1024 * 1024)) / elapsedSec).toFixed(2));
          const percent = Math.min(Math.round((downloadedBytes / totalBytes) * 100), 100);

          onProgress({
            downloadedBytes,
            totalBytes,
            percent,
            speedMb,
            filename: safeFilename,
          });
        }
      }

      await new Promise<void>((resolve, reject) => {
        fileStream.end(() => resolve());
        fileStream.on('error', reject);
      });

      // Verify file size
      const stat = await fs.promises.stat(tempFilePath);
      if (file.size && stat.size !== file.size) {
        throw new Error(
          `Niezgodność rozmiaru pliku: oczekiwano ${file.size} B, pobrano ${stat.size} B.`
        );
      }

      // Verify hash (SHA-512 primary, SHA-1 fallback)
      if (file.hashes?.sha512) {
        const computedSha512 = await this.calculateSha512(tempFilePath);
        if (computedSha512.toLowerCase() !== file.hashes.sha512.toLowerCase()) {
          throw new Error(
            `Naruszenie integralności (SHA-512 mismatch): oczekiwano ${file.hashes.sha512}, obliczono ${computedSha512}.`
          );
        }
      } else if (file.hashes?.sha1) {
        const computedSha1 = await this.calculateSha1(tempFilePath);
        if (computedSha1.toLowerCase() !== file.hashes.sha1.toLowerCase()) {
          throw new Error(
            `Naruszenie integralności (SHA-1 mismatch): oczekiwano ${file.hashes.sha1}, obliczono ${computedSha1}.`
          );
        }
      }

      return tempFilePath;
    } catch (err) {
      // Clean up corrupt temporary file immediately
      if (fs.existsSync(tempFilePath)) {
        try {
          fs.unlinkSync(tempFilePath);
        } catch {
          // ignore cleanup errors
        }
      }
      throw err;
    }
  }
}
