import * as fs from 'node:fs';
import * as path from 'node:path';
import * as zlib from 'node:zlib';

export interface ZipEntryHeader {
  fileName: string;
  compressionMethod: number; // 0 = stored, 8 = deflated
  compressedSize: number;
  uncompressedSize: number;
  relativeOffset: number;
}

export class JarArchiveReader {
  private static readonly MAX_JAR_SIZE_BYTES = 500 * 1024 * 1024; // 500 MB
  private static readonly MAX_ENTRIES_COUNT = 15000;
  private static readonly MAX_TEXT_ENTRY_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

  /**
   * Validates whether a file exists, is a valid file, and has acceptable size.
   */
  public static validateFile(filePath: string): { valid: boolean; error?: string } {
    if (!fs.existsSync(filePath)) {
      return { valid: false, error: `Plik nie istnieje: ${filePath}` };
    }
    const stat = fs.statSync(filePath);
    if (!stat.isFile()) {
      return { valid: false, error: `Ścieżka nie jest plikiem: ${filePath}` };
    }
    if (stat.size <= 0) {
      return { valid: false, error: 'Plik jest pusty (0 bajtów).' };
    }
    if (stat.size > this.MAX_JAR_SIZE_BYTES) {
      return {
        valid: false,
        error: `Plik przekracza dozwolony limit rozmiaru (maksymalnie ${this.MAX_JAR_SIZE_BYTES / 1024 / 1024} MB).`,
      };
    }
    return { valid: true };
  }

  /**
   * Scans the Central Directory of the ZIP/JAR archive and returns list of entries.
   */
  public static async listEntries(filePath: string): Promise<ZipEntryHeader[]> {
    const val = this.validateFile(filePath);
    if (!val.valid) {
      throw new Error(val.error);
    }

    const buffer = await fs.promises.readFile(filePath);
    return this.parseCentralDirectory(buffer);
  }

  /**
   * Reads an entry from the JAR as a UTF-8 string.
   */
  public static async readEntryText(
    filePath: string,
    entryName: string,
    maxBytes: number = this.MAX_TEXT_ENTRY_SIZE_BYTES
  ): Promise<string | null> {
    const buf = await this.readEntryBuffer(filePath, entryName, maxBytes);
    if (!buf) return null;
    return buf.toString('utf-8');
  }

  /**
   * Reads an entry from the JAR as a Buffer.
   */
  public static async readEntryBuffer(
    filePath: string,
    entryName: string,
    maxBytes: number = this.MAX_TEXT_ENTRY_SIZE_BYTES
  ): Promise<Buffer | null> {
    const val = this.validateFile(filePath);
    if (!val.valid) return null;

    const buffer = await fs.promises.readFile(filePath);
    const entries = this.parseCentralDirectory(buffer);

    const normalizedTarget = entryName.replace(/\\/g, '/').toLowerCase();
    const targetEntry = entries.find(
      (e) => e.fileName.replace(/\\/g, '/').toLowerCase() === normalizedTarget
    );

    if (!targetEntry) return null;

    if (targetEntry.uncompressedSize > maxBytes) {
      throw new Error(
        `Wpis ${entryName} w archiwum przekracza bezpieczny limit rozmiaru (${maxBytes} B).`
      );
    }

    return this.extractEntryData(buffer, targetEntry);
  }

  /**
   * Checks whether the archive contains a given entry name.
   */
  public static async hasEntry(filePath: string, entryName: string): Promise<boolean> {
    try {
      const entries = await this.listEntries(filePath);
      const normalizedTarget = entryName.replace(/\\/g, '/').toLowerCase();
      return entries.some((e) => e.fileName.replace(/\\/g, '/').toLowerCase() === normalizedTarget);
    } catch {
      return false;
    }
  }

  /**
   * Reads an entry directly from a buffer as a UTF-8 string.
   */
  public static readEntryTextFromBuffer(
    buffer: Buffer,
    entryName: string,
    maxBytes: number = this.MAX_TEXT_ENTRY_SIZE_BYTES
  ): string | null {
    const entries = this.parseCentralDirectory(buffer);
    const normalizedTarget = entryName.replace(/\\/g, '/').toLowerCase();
    const targetEntry = entries.find(
      (e) => e.fileName.replace(/\\/g, '/').toLowerCase() === normalizedTarget
    );
    if (!targetEntry) return null;
    if (targetEntry.uncompressedSize > maxBytes) {
      throw new Error(
        `Wpis ${entryName} w archiwum przekracza bezpieczny limit rozmiaru (${maxBytes} B).`
      );
    }
    const buf = this.extractEntryData(buffer, targetEntry);
    return buf ? buf.toString('utf-8') : null;
  }

  /**
   * Internal ZIP parser: finds EOCD and parses Central Directory headers.
   */
  public static parseCentralDirectory(buffer: Buffer): ZipEntryHeader[] {
    if (buffer.length < 22) {
      throw new Error('Archiwum jest zbyt małe, aby być prawidłowym plikiem ZIP/JAR.');
    }

    // 1. Locate End of Central Directory (EOCD) signature: 0x06054b50 (PK\x05\x06)
    // EOCD record is 22 bytes + comment (up to 65535 bytes)
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
      throw new Error('Nieprawidłowe archiwum ZIP/JAR (brak sygnatury End of Central Directory).');
    }

    const totalEntries = buffer.readUInt16LE(eocdOffset + 10);
    const cdSize = buffer.readUInt32LE(eocdOffset + 12);
    const cdOffset = buffer.readUInt32LE(eocdOffset + 16);

    if (totalEntries > this.MAX_ENTRIES_COUNT) {
      throw new Error(
        `Archiwum zawiera zbyt wiele wpisów (${totalEntries} > ${this.MAX_ENTRIES_COUNT}).`
      );
    }

    if (cdOffset + cdSize > buffer.length) {
      throw new Error('Uszkodzone archiwum ZIP: Central Directory wykracza poza rozmiar pliku.');
    }

    // 2. Parse Central Directory headers (signature 0x02014b50)
    const entries: ZipEntryHeader[] = [];
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
      const compressedSize = buffer.readUInt32LE(curOffset + 20);
      const uncompressedSize = buffer.readUInt32LE(curOffset + 24);
      const fileNameLen = buffer.readUInt16LE(curOffset + 28);
      const extraLen = buffer.readUInt16LE(curOffset + 30);
      const commentLen = buffer.readUInt16LE(curOffset + 32);
      const relativeOffset = buffer.readUInt32LE(curOffset + 42);

      const fileName = buffer.toString('utf-8', curOffset + 46, curOffset + 46 + fileNameLen);

      // Path traversal security check (ZipSlip)
      this.assertSafeEntryPath(fileName);

      entries.push({
        fileName,
        compressionMethod,
        compressedSize,
        uncompressedSize,
        relativeOffset,
      });

      curOffset += 46 + fileNameLen + extraLen + commentLen;
    }

    return entries;
  }

  /**
   * Extracts data for a single entry from buffer.
   */
  private static extractEntryData(buffer: Buffer, entry: ZipEntryHeader): Buffer {
    const loc = entry.relativeOffset;
    if (
      loc + 30 > buffer.length ||
      buffer[loc] !== 0x50 ||
      buffer[loc + 1] !== 0x4b ||
      buffer[loc + 2] !== 0x03 ||
      buffer[loc + 3] !== 0x04
    ) {
      throw new Error(`Nieprawidłowy nagłówek lokalny dla wpisu ${entry.fileName}`);
    }

    const localFileNameLen = buffer.readUInt16LE(loc + 26);
    const localExtraLen = buffer.readUInt16LE(loc + 28);
    const dataOffset = loc + 30 + localFileNameLen + localExtraLen;

    if (dataOffset + entry.compressedSize > buffer.length) {
      throw new Error(`Dane wpisu ${entry.fileName} wykraczają poza rozmiar archiwum.`);
    }

    const compressedSlice = buffer.subarray(dataOffset, dataOffset + entry.compressedSize);

    if (entry.compressionMethod === 0) {
      // Stored (no compression)
      return compressedSlice;
    } else if (entry.compressionMethod === 8) {
      // Deflated
      return zlib.inflateRawSync(compressedSlice);
    } else {
      throw new Error(
        `Nieobsługiwana metoda kompresji (${entry.compressionMethod}) dla wpisu ${entry.fileName}`
      );
    }
  }

  /**
   * ZipSlip / Path Traversal protection.
   */
  public static assertSafeEntryPath(entryPath: string): void {
    const normalized = path.normalize(entryPath.replace(/\\/g, '/'));
    if (
      normalized.startsWith('..') ||
      path.isAbsolute(normalized) ||
      entryPath.includes(':') ||
      normalized.includes('/../')
    ) {
      throw new Error(`Wykryto niebezpieczną ścieżkę (ZipSlip / Path Traversal) w archiwum: ${entryPath}`);
    }
  }
}
