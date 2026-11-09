import * as fs from 'node:fs';
import * as path from 'node:path';
import * as crypto from 'node:crypto';
import * as os from 'node:os';
import { EnginePaths } from '../engine/paths.js';
import type { SecureAccountTokens } from './types.js';

let electronSafeStorage: typeof import('electron').safeStorage | null = null;
try {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const electron = require('electron');
  if (electron && electron.safeStorage) {
    electronSafeStorage = electron.safeStorage;
  }
} catch {
  // Not in Electron runtime (e.g. unit tests or standalone node)
  electronSafeStorage = null;
}

export class TokenStorage {
  private static filePath(): string {
    EnginePaths.ensureAllBaseDirs();
    return path.join(EnginePaths.getAccountsDir(), 'tokens.enc');
  }

  public static isNativeEncryptionAvailable(): boolean {
    return Boolean(
      electronSafeStorage &&
        typeof electronSafeStorage.isEncryptionAvailable === 'function' &&
        electronSafeStorage.isEncryptionAvailable()
    );
  }

  private static getFallbackKey(): Buffer {
    const raw = `${os.hostname()}-${os.userInfo().username}-zombek-secure-storage-key`;
    return crypto.createHash('sha256').update(raw).digest();
  }

  private static encryptData(plainText: string): string {
    if (this.isNativeEncryptionAvailable() && electronSafeStorage) {
      const buffer = electronSafeStorage.encryptString(plainText);
      return `safe:${buffer.toString('base64')}`;
    }

    // AES-256-GCM fallback for test environments or non-DPAPI systems
    const iv = crypto.randomBytes(12);
    const key = this.getFallbackKey();
    const cipher = crypto.createCipheriv('aes-256-gcm', key, iv);
    const encrypted = Buffer.concat([cipher.update(plainText, 'utf8'), cipher.final()]);
    const tag = cipher.getAuthTag();

    const payload = Buffer.concat([iv, tag, encrypted]);
    return `gcm:${payload.toString('base64')}`;
  }

  private static decryptData(encryptedPayload: string): string {
    if (encryptedPayload.startsWith('safe:') && electronSafeStorage) {
      const base64 = encryptedPayload.substring(5);
      const buffer = Buffer.from(base64, 'base64');
      return electronSafeStorage.decryptString(buffer);
    }

    if (encryptedPayload.startsWith('gcm:')) {
      const raw = Buffer.from(encryptedPayload.substring(4), 'base64');
      const iv = raw.subarray(0, 12);
      const tag = raw.subarray(12, 28);
      const cipherText = raw.subarray(28);

      const key = this.getFallbackKey();
      const decipher = crypto.createDecipheriv('aes-256-gcm', key, iv);
      decipher.setAuthTag(tag);
      return decipher.update(cipherText) + decipher.final('utf8');
    }

    throw new Error('Nieobsługiwany format zaszyfrowanych danych tokenów.');
  }

  private static async readStore(): Promise<Record<string, string>> {
    const p = this.filePath();
    if (!fs.existsSync(p)) {
      return {};
    }

    try {
      const raw = await fs.promises.readFile(p, 'utf8');
      return JSON.parse(raw) as Record<string, string>;
    } catch {
      return {};
    }
  }

  private static async writeStore(store: Record<string, string>): Promise<void> {
    const p = this.filePath();
    await fs.promises.writeFile(p, JSON.stringify(store, null, 2), 'utf8');
  }

  public static async setTokens(accountId: string, tokens: SecureAccountTokens): Promise<void> {
    const store = await this.readStore();
    const serialized = JSON.stringify(tokens);
    store[accountId] = this.encryptData(serialized);
    await this.writeStore(store);
  }

  public static async getTokens(accountId: string): Promise<SecureAccountTokens | null> {
    const store = await this.readStore();
    const encrypted = store[accountId];
    if (!encrypted) {
      return null;
    }

    try {
      const decrypted = this.decryptData(encrypted);
      return JSON.parse(decrypted) as SecureAccountTokens;
    } catch {
      return null;
    }
  }

  public static async deleteTokens(accountId: string): Promise<void> {
    const store = await this.readStore();
    if (store[accountId]) {
      delete store[accountId];
      await this.writeStore(store);
    }
  }
}
