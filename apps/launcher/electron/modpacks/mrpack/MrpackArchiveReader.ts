import * as fs from 'node:fs';
import * as path from 'node:path';
import * as zlib from 'node:zlib';
import { MrpackPathValidator } from './MrpackPathValidator.js';
import type { MrpackIndex } from '@zombek/shared-types';

export interface MrpackZipEntry {
  fileName: string;
  normalizedPath: string;
  compressionMethod: number; // 0 = Stored, 8 = Deflated
  compressedSize: number;
  uncompressedSize: number;
  relativeOffset: number;
  crc32: number;
  isDirectory: boolean;
  isSymlink: boolean;
}

export class MrpackArchiveReader {
  public static readonly MAX_ARCHIVE_SIZE_BYTES = 500 * 1024 * 1024; // 500 MB
  public static readonly MAX_UNCOMPRESSED_TOTAL_BYTES = 2 * 1024 * 1024 * 1024; // 2 GB
  public static readonly MAX_SINGLE_ENTRY_BYTES = 250 * 1024 * 1024; // 250 MB
  public static readonly MAX_ENTRIES_COUNT = 20_000;
  public static readonly MAX_MANIFEST_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

  /**
   * Validates archive file basic metadata.
   */
  public static validateArchiveFile(filePath: string): { valid: boolean; error?: string } {
    if (!fs.existsSync(filePath)) {
      return { valid: false, error: `Plik archiwum .mrpack nie istnieje: ${filePath}` };
    }
    const stat = fs.statSync(filePath);
    if (!stat.isFile()) {
      return { valid: false, error: `Ścieżka nie wskazuje na prawidłowy plik: ${filePath}` };
    }
    if (stat.size < 22) {
      return { valid: false, error: 'Plik jest uszkodzony lub zbyt mały na prawidłowe archiwum ZIP (poniżej 22 bajtów).' };
    }
    if (stat.size > this.MAX_ARCHIVE_SIZE_BYTES) {
      return {
        valid: false,
        error: `Rozmiar archiwum (${(stat.size / 1024 / 1024).toFixed(1)} MB) przekracza maksymalny limit (${this.MAX_ARCHIVE_SIZE_BYTES / 1024 / 1024} MB).`,
      };
    }
    return { valid: true };
  }

  /**
   * Opens an archive and provides helper methods for reading manifest and entries.
   */
  public static async open(filePath: string) {
    const val = this.validateArchiveFile(filePath);
    if (!val.valid) {
      throw new Error(val.error);
    }
    const buffer = await fs.promises.readFile(filePath);
    const entries = this.parseCentralDirectory(buffer);
    return {
      hasManifest: () =>
        entries.some(
          (e) => e.normalizedPath === 'modrinth.index.json' || e.fileName.toLowerCase() === 'modrinth.index.json'
        ),
      readManifest: () => {
        const manifestEntry = entries.find(
          (e) => e.normalizedPath === 'modrinth.index.json' || e.fileName.toLowerCase() === 'modrinth.index.json'
        );
        if (!manifestEntry) {
          throw new Error('Brak wymaganego pliku modrinth.index.json w katalogu głównym archiwum.');
        }
        const data = this.extractEntryData(buffer, manifestEntry);
        if (!data) throw new Error('Nie udało się odczytać modrinth.index.json');
        return JSON.parse(data.toString('utf-8')) as MrpackIndex;
      },
      listEntries: () => entries.map((e) => ({ name: e.fileName, size: e.uncompressedSize })),
      readEntry: (entryName: string) => {
        const normalizedTarget = entryName.replace(/\\/g, '/');
        const target = entries.find(
          (e) => e.normalizedPath === normalizedTarget || e.fileName === normalizedTarget
        );
        if (!target) return null;
        return this.extractEntryData(buffer, target);
      },
    };
  }

  /**
   * Scans and returns all entries in the Central Directory of the mrpack ZIP archive.
   */
  public static async listEntries(filePath: string): Promise<MrpackZipEntry[]> {
    const val = this.validateArchiveFile(filePath);
    if (!val.valid) {
      throw new Error(val.error);
    }

    const buffer = await fs.promises.readFile(filePath);
    return this.parseCentralDirectory(buffer);
  }

  /**
   * Reads and parses modrinth.index.json from the root of the archive.
   */
  public static async readManifest(filePath: string): Promise<MrpackIndex> {
    const val = this.validateArchiveFile(filePath);
    if (!val.valid) {
      throw new Error(val.error);
    }

    const buffer = await fs.promises.readFile(filePath);
    const entries = this.parseCentralDirectory(buffer);

    const manifestEntry = entries.find(
      (e) => e.normalizedPath === 'modrinth.index.json' || e.fileName.toLowerCase() === 'modrinth.index.json'
    );

    if (!manifestEntry) {
      throw new Error(
        'Nieprawidłowa paczka .mrpack: Brak wymaganego pliku modrinth.index.json w katalogu głównym archiwum.'
      );
    }

    if (manifestEntry.uncompressedSize > this.MAX_MANIFEST_SIZE_BYTES) {
      throw new Error(
        `Plik manifestu przekracza dopuszczalny limit rozmiaru (${this.MAX_MANIFEST_SIZE_BYTES / 1024 / 1024} MB).`
      );
    }

    const manifestBuffer = this.extractEntryData(buffer, manifestEntry);
    if (!manifestBuffer) {
      throw new Error('Nie udało się odczytać zawartości modrinth.index.json.');
    }

    try {
      const manifestStr = manifestBuffer.toString('utf-8');
      return JSON.parse(manifestStr) as MrpackIndex;
    } catch (err: any) {
      throw new Error(`Błąd parsowania modrinth.index.json jako poprawny JSON: ${err.message}`);
    }
  }

  /**
   * Reads a single entry as Buffer from the archive.
   */
  public static async readEntryBuffer(filePath: string, entryName: string): Promise<Buffer | null> {
    const val = this.validateArchiveFile(filePath);
    if (!val.valid) return null;

    const buffer = await fs.promises.readFile(filePath);
    const entries = this.parseCentralDirectory(buffer);

    const normalizedTarget = entryName.replace(/\\/g, '/');
    const target = entries.find(
      (e) => e.normalizedPath === normalizedTarget || e.fileName === normalizedTarget
    );

    if (!target) return null;
    return this.extractEntryData(buffer, target);
  }

  /**
   * Parses Central Directory of ZIP and performs Zip Slip and Zip Bomb checks.
   */
  public static parseCentralDirectory(buffer: Buffer): MrpackZipEntry[] {
    if (buffer.length < 22) {
      throw new Error('Archiwum jest zbyt małe, aby być prawidłowym plikiem ZIP.');
    }

    // Locate EOCD signature: 0x06054b50
    let eocdOffset = -1;
    const maxSearch = Math.min(buffer.length, 65535 + 22);
    for (let i = buffer.length - 22; i >= buffer.length - maxSearch; i--) {
      if (
        buffer[i] === 0x50 &&
        buffer[i + 1] === 0x4b &&
        buffer[i + 2] === 0x05 &&
        buffer[i + 3] === 0x06
      ) {
        eocdOffset = i;
        break;
      }
    }

    if (eocdOffset === -1) {
      throw new Error('Uszkodzone archiwum .mrpack: Brak sygnatury End of Central Directory (EOCD).');
    }

    const totalEntries = buffer.readUInt16LE(eocdOffset + 10);
    const cdSize = buffer.readUInt32LE(eocdOffset + 12);
    const cdOffset = buffer.readUInt32LE(eocdOffset + 16);

    if (totalEntries > this.MAX_ENTRIES_COUNT) {
      throw new Error(
        `Archiwum zawiera zbyt wiele wpisów (${totalEntries} > dozwolone ${this.MAX_ENTRIES_COUNT}). Potencjalny atak ZIP Bomb.`
      );
    }

    if (cdOffset + cdSize > buffer.length) {
      throw new Error('Uszkodzone archiwum: Central Directory wykracza poza rozmiar pliku.');
    }

    const entries: MrpackZipEntry[] = [];
    const seenPaths = new Set<string>();
    let totalUncompressedSize = 0;
    let curOffset = cdOffset;

    for (let i = 0; i < totalEntries && curOffset < cdOffset + cdSize; i++) {
      if (
        buffer[curOffset] !== 0x50 ||
        buffer[curOffset + 1] !== 0x4b ||
        buffer[curOffset + 2] !== 0x01 ||
        buffer[curOffset + 3] !== 0x02
      ) {
        break;
      }

      const compressionMethod = buffer.readUInt16LE(curOffset + 10);
      const crc32 = buffer.readUInt32LE(curOffset + 16);
      const compressedSize = buffer.readUInt32LE(curOffset + 20);
      const uncompressedSize = buffer.readUInt32LE(curOffset + 24);
      const fileNameLen = buffer.readUInt16LE(curOffset + 28);
      const extraLen = buffer.readUInt16LE(curOffset + 30);
      const commentLen = buffer.readUInt16LE(curOffset + 32);
      const externalAttributes = buffer.readUInt32LE(curOffset + 38);
      const relativeOffset = buffer.readUInt32LE(curOffset + 42);

      const fileName = buffer.toString('utf-8', curOffset + 46, curOffset + 46 + fileNameLen);
      const isDirectory = fileName.endsWith('/') || fileName.endsWith('\\');

      // Detect symlinks: (externalAttributes >>> 16) & S_IFLNK (0o120000)
      const isSymlink = ((externalAttributes >>> 16) & 0o170000) === 0o120000;
      if (isSymlink) {
        throw new Error(
          `Wykryto niebezpieczne dowiązanie symboliczne (Symlink) w paczce: '${fileName}'. Symlinki są zablokowane.`
        );
      }

      // Check single entry size limit
      if (uncompressedSize > this.MAX_SINGLE_ENTRY_BYTES) {
        throw new Error(
          `Wpis '${fileName}' w archiwum przekracza maksymalny dopuszczalny rozmiar (${this.MAX_SINGLE_ENTRY_BYTES / 1024 / 1024} MB).`
        );
      }

      totalUncompressedSize += uncompressedSize;
      if (totalUncompressedSize > this.MAX_UNCOMPRESSED_TOTAL_BYTES) {
        throw new Error(
          `Łączny nieskompresowany rozmiar zawartości archiwum przekracza bezpieczny limit (${this.MAX_UNCOMPRESSED_TOTAL_BYTES / 1024 / 1024} MB). Zablokowano potencjalny atak ZIP Bomb.`
        );
      }

      // Validate relative path using MrpackPathValidator
      const pathVal = MrpackPathValidator.validateRelativePath(fileName);
      if (!pathVal.valid || !pathVal.normalized) {
        throw new Error(`Niebezpieczna ścieżka wpisu w archiwum [${fileName}]: ${pathVal.error}`);
      }

      // Check duplicates
      const lowerNorm = pathVal.normalized.toLowerCase();
      if (!isDirectory && seenPaths.has(lowerNorm)) {
        throw new Error(`Zduplikowana ścieżka w archiwum ZIP: '${fileName}'.`);
      }
      seenPaths.add(lowerNorm);

      entries.push({
        fileName,
        normalizedPath: pathVal.normalized,
        compressionMethod,
        compressedSize,
        uncompressedSize,
        relativeOffset,
        crc32,
        isDirectory,
        isSymlink,
      });

      curOffset += 46 + fileNameLen + extraLen + commentLen;
    }

    return entries;
  }

  /**
   * Decompresses an individual entry from the ZIP buffer.
   */
  private static extractEntryData(buffer: Buffer, entry: MrpackZipEntry): Buffer | null {
    if (entry.isDirectory) {
      return Buffer.alloc(0);
    }

    const localHeaderOffset = entry.relativeOffset;
    if (localHeaderOffset + 30 > buffer.length) {
      throw new Error(`Uszkodzone archiwum: local header wpisu ${entry.fileName} poza buforem.`);
    }

    // Verify local header signature: 0x04034b50
    if (
      buffer[localHeaderOffset] !== 0x50 ||
      buffer[localHeaderOffset + 1] !== 0x4b ||
      buffer[localHeaderOffset + 2] !== 0x03 ||
      buffer[localHeaderOffset + 3] !== 0x04
    ) {
      throw new Error(`Nieprawidłowa sygnatura local file header dla wpisu: ${entry.fileName}`);
    }

    const localNameLen = buffer.readUInt16LE(localHeaderOffset + 26);
    const localExtraLen = buffer.readUInt16LE(localHeaderOffset + 28);
    const dataOffset = localHeaderOffset + 30 + localNameLen + localExtraLen;

    if (dataOffset + entry.compressedSize > buffer.length) {
      throw new Error(`Uszkodzone archiwum: dane wpisu ${entry.fileName} wykraczają poza rozmiar pliku.`);
    }

    const compressedSlice = buffer.subarray(dataOffset, dataOffset + entry.compressedSize);

    let decompressed: Buffer;
    if (entry.compressionMethod === 0) {
      // Stored
      decompressed = compressedSlice;
    } else if (entry.compressionMethod === 8) {
      // Deflated
      try {
        decompressed = zlib.inflateRawSync(compressedSlice);
      } catch (err: any) {
        throw new Error(`Błąd dekompresji deflate dla wpisu ${entry.fileName}: ${err.message}`);
      }
    } else {
      throw new Error(`Nieobsługiwana metoda kompresji (${entry.compressionMethod}) dla ${entry.fileName}.`);
    }

    if (decompressed.length !== entry.uncompressedSize) {
      throw new Error(
        `Niezgodność rozmiaru po dekompresji dla ${entry.fileName} (oczekiwano ${entry.uncompressedSize}, otrzymano ${decompressed.length}).`
      );
    }

    return decompressed;
  }
}
