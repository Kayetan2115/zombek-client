import * as fs from 'node:fs';
import * as path from 'node:path';
import * as zlib from 'node:zlib';
import { MrpackPathValidator } from './MrpackPathValidator.js';
import type { MrpackIndex } from '@zombek/shared-types';

export interface MrpackWriterEntry {
  archivePath: string; // Relative path inside ZIP (e.g. "modrinth.index.json", "overrides/config/mod.json")
  data: Buffer;
  mtime?: Date;
}

// CRC32 table
const CRC32_TABLE = new Uint32Array(256);
for (let n = 0; n < 256; n++) {
  let c = n;
  for (let k = 0; k < 8; k++) {
    c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  }
  CRC32_TABLE[n] = c;
}

function calculateCrc32(buf: Buffer): number {
  let crc = 0 ^ -1;
  for (let i = 0; i < buf.length; i++) {
    crc = (crc >>> 8) ^ CRC32_TABLE[(crc ^ buf[i]) & 0xff];
  }
  return (crc ^ -1) >>> 0;
}

export class MrpackArchiveWriter {
  /**
   * Builds and writes a verified .mrpack archive containing modrinth.index.json and overrides.
   */
  public static async createArchive(params: {
    destinationPath: string;
    manifest: MrpackIndex;
    overrideFiles?: Array<{ relativePath: string; fullPath: string; prefix?: 'overrides' | 'client-overrides' | 'server-overrides' }>;
    inMemoryEntries?: MrpackWriterEntry[];
  }): Promise<{ success: boolean; totalBytes: number; entriesCount: number }> {
    const { destinationPath, manifest, overrideFiles = [], inMemoryEntries = [] } = params;

    // Validate destination path
    const targetDir = path.dirname(destinationPath);
    if (!fs.existsSync(targetDir)) {
      await fs.promises.mkdir(targetDir, { recursive: true });
    }

    const tempFilePath = `${destinationPath}.tmp-${Date.now()}`;

    const allEntries: MrpackWriterEntry[] = [];

    // 1. Add modrinth.index.json as primary entry
    const manifestJsonString = JSON.stringify(manifest, null, 2);
    allEntries.push({
      archivePath: 'modrinth.index.json',
      data: Buffer.from(manifestJsonString, 'utf-8'),
    });

    // 2. Add in-memory entries if any
    for (const entry of inMemoryEntries) {
      const pathVal = MrpackPathValidator.validateRelativePath(entry.archivePath);
      if (!pathVal.valid || !pathVal.normalized) {
        throw new Error(`Niepoprawna ścieżka wpisu archiwum [${entry.archivePath}]: ${pathVal.error}`);
      }
      allEntries.push({
        archivePath: pathVal.normalized,
        data: entry.data,
      });
    }

    // 3. Add override files from disk
    for (const ov of overrideFiles) {
      if (!fs.existsSync(ov.fullPath)) continue;
      const stat = await fs.promises.stat(ov.fullPath);
      if (!stat.isFile()) continue;

      const prefix = ov.prefix || 'overrides';
      const cleanRel = ov.relativePath.replace(/\\/g, '/').replace(/^\/+/, '');
      const archivePath = `${prefix}/${cleanRel}`;

      const pathVal = MrpackPathValidator.validateRelativePath(archivePath);
      if (!pathVal.valid || !pathVal.normalized) {
        throw new Error(`Niepoprawna ścieżka override [${archivePath}]: ${pathVal.error}`);
      }

      const fileData = await fs.promises.readFile(ov.fullPath);
      allEntries.push({
        archivePath: pathVal.normalized,
        data: fileData,
        mtime: stat.mtime,
      });
    }

    // 4. Generate ZIP binary in memory or stream
    const zipBuffer = this.buildZipBuffer(allEntries);

    // 5. Write to temporary file
    await fs.promises.writeFile(tempFilePath, zipBuffer);

    // 6. Verify written archive before finalizing
    if (!fs.existsSync(tempFilePath) || fs.statSync(tempFilePath).size < 22) {
      throw new Error('Błąd zapisu pliku tymczasowego .mrpack.');
    }

    // 7. Atomic rename
    await fs.promises.rename(tempFilePath, destinationPath);

    return {
      success: true,
      totalBytes: zipBuffer.length,
      entriesCount: allEntries.length,
    };
  }

  /**
   * Constructs raw ZIP buffer with Local Headers, Deflate data, Central Directory, and EOCD.
   */
  public static buildZipBuffer(entries: MrpackWriterEntry[]): Buffer {
    const localChunks: Buffer[] = [];
    const centralChunks: Buffer[] = [];
    let currentOffset = 0;

    for (const entry of entries) {
      const nameBytes = Buffer.from(entry.archivePath, 'utf-8');
      const uncompressedSize = entry.data.length;
      const crc = calculateCrc32(entry.data);

      let compressedData: Buffer;
      let compressionMethod = 8; // Deflated

      if (uncompressedSize === 0) {
        compressedData = Buffer.alloc(0);
        compressionMethod = 0; // Stored
      } else {
        const deflated = zlib.deflateRawSync(entry.data);
        if (deflated.length < uncompressedSize) {
          compressedData = deflated;
          compressionMethod = 8;
        } else {
          // If deflating does not reduce size, store as-is
          compressedData = entry.data;
          compressionMethod = 0;
        }
      }

      const compressedSize = compressedData.length;

      // DOS date / time (or default 2026-01-01)
      const mtime = entry.mtime || new Date();
      const dosTime =
        ((mtime.getHours() & 0x1f) << 11) |
        ((mtime.getMinutes() & 0x3f) << 5) |
        Math.floor(mtime.getSeconds() / 2);
      const dosDate =
        (((mtime.getFullYear() - 1980) & 0x7f) << 9) |
        (((mtime.getMonth() + 1) & 0x0f) << 5) |
        (mtime.getDate() & 0x1f);

      // Local Header: 30 bytes + name
      const localHeader = Buffer.alloc(30 + nameBytes.length);
      localHeader.writeUInt32LE(0x04034b50, 0); // Signature
      localHeader.writeUInt16LE(20, 4); // Min version (2.0)
      localHeader.writeUInt16LE(0x0800, 6); // Flags: UTF-8 filename (bit 11)
      localHeader.writeUInt16LE(compressionMethod, 8);
      localHeader.writeUInt16LE(dosTime, 10);
      localHeader.writeUInt16LE(dosDate, 12);
      localHeader.writeUInt32LE(crc, 14);
      localHeader.writeUInt32LE(compressedSize, 18);
      localHeader.writeUInt32LE(uncompressedSize, 22);
      localHeader.writeUInt16LE(nameBytes.length, 26);
      localHeader.writeUInt16LE(0, 28); // Extra field len
      nameBytes.copy(localHeader, 30);

      localChunks.push(localHeader, compressedData);

      // Central Directory Header: 46 bytes + name
      const cdHeader = Buffer.alloc(46 + nameBytes.length);
      cdHeader.writeUInt32LE(0x02014b50, 0); // Signature
      cdHeader.writeUInt16LE(20, 4); // Made by (2.0)
      cdHeader.writeUInt16LE(20, 6); // Version needed (2.0)
      cdHeader.writeUInt16LE(0x0800, 8); // Flags: UTF-8
      cdHeader.writeUInt16LE(compressionMethod, 10);
      cdHeader.writeUInt16LE(dosTime, 12);
      cdHeader.writeUInt16LE(dosDate, 14);
      cdHeader.writeUInt32LE(crc, 16);
      cdHeader.writeUInt32LE(compressedSize, 20);
      cdHeader.writeUInt32LE(uncompressedSize, 24);
      cdHeader.writeUInt16LE(nameBytes.length, 28);
      cdHeader.writeUInt16LE(0, 30); // Extra field len
      cdHeader.writeUInt16LE(0, 32); // File comment len
      cdHeader.writeUInt16LE(0, 34); // Disk number start
      cdHeader.writeUInt16LE(0, 36); // Internal file attributes
      cdHeader.writeUInt32LE(0, 38); // External file attributes
      cdHeader.writeUInt32LE(currentOffset, 42); // Relative offset of local header
      nameBytes.copy(cdHeader, 46);

      centralChunks.push(cdHeader);
      currentOffset += localHeader.length + compressedData.length;
    }

    const cdOffset = currentOffset;
    const cdBuffer = Buffer.concat(centralChunks);
    const cdSize = cdBuffer.length;
    const totalEntries = entries.length;

    // End of Central Directory Record (EOCD): 22 bytes
    const eocd = Buffer.alloc(22);
    eocd.writeUInt32LE(0x06054b50, 0); // Signature
    eocd.writeUInt16LE(0, 4); // Disk number
    eocd.writeUInt16LE(0, 6); // CD disk number
    eocd.writeUInt16LE(totalEntries, 8); // Entries on this disk
    eocd.writeUInt16LE(totalEntries, 10); // Total entries
    eocd.writeUInt32LE(cdSize, 12); // Size of Central Directory
    eocd.writeUInt32LE(cdOffset, 16); // Offset of Central Directory
    eocd.writeUInt16LE(0, 20); // Comment length

    return Buffer.concat([...localChunks, cdBuffer, eocd]);
  }
}
