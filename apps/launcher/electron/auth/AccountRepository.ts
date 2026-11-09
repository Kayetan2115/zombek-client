import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import { generateOfflineUuid } from '../engine/GameSessionProvider.js';
import { TokenStorage } from './TokenStorage.js';
import type { AccountRecord, AccountSessionStatus } from '@zombek/shared-types';
import type { MinecraftProfileResponse, SecureAccountTokens } from './types.js';

interface AccountsDataFile {
  activeAccountId: string | null;
  accounts: AccountRecord[];
}

export class AccountRepository {
  private static filePath(): string {
    EnginePaths.ensureAllBaseDirs();
    return path.join(EnginePaths.getAccountsDir(), 'accounts.json');
  }

  public static validateOfflineUsername(username: string): string {
    const trimmed = username.trim();
    if (!trimmed) {
      throw new Error('Nick gracza nie może być pusty.');
    }
    if (trimmed.length < 3 || trimmed.length > 16) {
      throw new Error('Nick gracza musi zawierać od 3 do 16 znaków (zgodnie z zasadami Minecraft Java Edition).');
    }
    if (!/^[a-zA-Z0-9_]{3,16}$/.test(trimmed)) {
      throw new Error('Nick gracza może zawierać tylko litery alfabetu łacińskiego, cyfry oraz znak podkreślenia (_).');
    }
    return trimmed;
  }

  public static formatUuid(rawUuid: string): string {
    const clean = rawUuid.replace(/-/g, '').toLowerCase();
    if (clean.length === 32) {
      return [
        clean.substring(0, 8),
        clean.substring(8, 12),
        clean.substring(12, 16),
        clean.substring(16, 20),
        clean.substring(20, 32),
      ].join('-');
    }
    return rawUuid;
  }

  private static async readData(): Promise<AccountsDataFile> {
    const p = this.filePath();
    if (!fs.existsSync(p)) {
      const defaultRecord = await this.generateDefaultOfflineRecord();
      const initial: AccountsDataFile = {
        activeAccountId: defaultRecord.id,
        accounts: [defaultRecord],
      };
      await this.writeData(initial);
      return initial;
    }

    try {
      const raw = await fs.promises.readFile(p, 'utf8');
      const parsed = JSON.parse(raw) as AccountsDataFile;
      if (!parsed.accounts || parsed.accounts.length === 0) {
        const defaultRecord = await this.generateDefaultOfflineRecord();
        parsed.accounts = [defaultRecord];
        parsed.activeAccountId = defaultRecord.id;
        await this.writeData(parsed);
      }
      return parsed;
    } catch {
      const defaultRecord = await this.generateDefaultOfflineRecord();
      return {
        activeAccountId: defaultRecord.id,
        accounts: [defaultRecord],
      };
    }
  }

  private static async writeData(data: AccountsDataFile): Promise<void> {
    const p = this.filePath();
    await fs.promises.writeFile(p, JSON.stringify(data, null, 2), 'utf8');
  }

  private static async generateDefaultOfflineRecord(): Promise<AccountRecord> {
    const username = 'ZombekPlayer';
    const uuid = generateOfflineUuid(username);
    return {
      id: `acc-offline-${Date.now()}`,
      type: 'offline',
      username,
      uuid,
      avatar: `https://mc-heads.net/avatar/${uuid}/100`,
      hasEntitlement: false,
      entitlements: [],
      sessionStatus: 'valid',
      lastUsed: new Date().toISOString(),
      created: new Date().toISOString(),
      isDefault: true,
    };
  }

  public static async getAll(): Promise<AccountRecord[]> {
    const data = await this.readData();
    return data.accounts;
  }

  public static async getById(id: string): Promise<AccountRecord | null> {
    const data = await this.readData();
    return data.accounts.find((a) => a.id === id) || null;
  }

  public static async getActive(): Promise<AccountRecord | null> {
    const data = await this.readData();
    if (!data.activeAccountId && data.accounts.length > 0) {
      return data.accounts[0];
    }
    return data.accounts.find((a) => a.id === data.activeAccountId) || data.accounts[0] || null;
  }

  public static async setActive(id: string): Promise<boolean> {
    const data = await this.readData();
    const target = data.accounts.find((a) => a.id === id);
    if (!target) return false;

    data.activeAccountId = id;
    for (const acc of data.accounts) {
      acc.isDefault = acc.id === id;
      if (acc.id === id) {
        acc.lastUsed = new Date().toISOString();
      }
    }

    await this.writeData(data);
    return true;
  }

  public static async createOffline(rawUsername: string): Promise<AccountRecord> {
    const username = this.validateOfflineUsername(rawUsername);
    const data = await this.readData();

    // Check if an offline account with this username already exists
    const existing = data.accounts.find(
      (a) => a.type === 'offline' && a.username.toLowerCase() === username.toLowerCase()
    );
    if (existing) {
      await this.setActive(existing.id);
      return existing;
    }

    const uuid = generateOfflineUuid(username);
    const newAccount: AccountRecord = {
      id: `acc-offline-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
      type: 'offline',
      username,
      uuid,
      avatar: `https://mc-heads.net/avatar/${uuid}/100`,
      hasEntitlement: false,
      entitlements: [],
      sessionStatus: 'valid',
      lastUsed: new Date().toISOString(),
      created: new Date().toISOString(),
      isDefault: true,
    };

    data.accounts.push(newAccount);
    data.activeAccountId = newAccount.id;
    for (const a of data.accounts) {
      a.isDefault = a.id === newAccount.id;
    }

    await this.writeData(data);
    return newAccount;
  }

  public static async saveMicrosoftAccount(
    profile: MinecraftProfileResponse,
    tokens: SecureAccountTokens,
    hasEntitlement: boolean,
    entitlements: string[] = []
  ): Promise<AccountRecord> {
    const data = await this.readData();
    const formattedUuid = this.formatUuid(profile.id);

    // Look for existing account with same UUID
    let target = data.accounts.find((a) => a.uuid.toLowerCase() === formattedUuid.toLowerCase());
    const accountId = target ? target.id : `acc-ms-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;

    // Resolve skin avatar
    let avatarUrl = `https://mc-heads.net/avatar/${formattedUuid}/100`;
    const activeSkin = profile.skins?.find((s) => s.state === 'ACTIVE') || profile.skins?.[0];
    if (activeSkin && activeSkin.url) {
      avatarUrl = `https://mc-heads.net/avatar/${formattedUuid}/100`;
    }

    if (target) {
      target.username = profile.name;
      target.avatar = avatarUrl;
      target.hasEntitlement = hasEntitlement;
      target.entitlements = entitlements;
      target.sessionStatus = 'valid';
      target.lastUsed = new Date().toISOString();
      target.isDefault = true;
    } else {
      target = {
        id: accountId,
        type: 'microsoft',
        username: profile.name,
        uuid: formattedUuid,
        avatar: avatarUrl,
        hasEntitlement,
        entitlements,
        sessionStatus: 'valid',
        lastUsed: new Date().toISOString(),
        created: new Date().toISOString(),
        isDefault: true,
      };
      data.accounts.push(target);
    }

    data.activeAccountId = target.id;
    for (const a of data.accounts) {
      a.isDefault = a.id === target.id;
    }

    await this.writeData(data);

    // Save tokens securely (never written to accounts.json!)
    await TokenStorage.setTokens(target.id, tokens);

    return target;
  }

  public static async updateSessionStatus(id: string, status: AccountSessionStatus): Promise<void> {
    const data = await this.readData();
    const acc = data.accounts.find((a) => a.id === id);
    if (acc) {
      acc.sessionStatus = status;
      await this.writeData(data);
    }
  }

  public static async remove(id: string): Promise<boolean> {
    const data = await this.readData();
    const idx = data.accounts.findIndex((a) => a.id === id);
    if (idx === -1) return false;

    // Delete secure tokens
    await TokenStorage.deleteTokens(id);

    data.accounts.splice(idx, 1);
    if (data.activeAccountId === id) {
      data.activeAccountId = data.accounts.length > 0 ? data.accounts[0].id : null;
      if (data.accounts.length > 0) {
        data.accounts[0].isDefault = true;
      }
    }

    await this.writeData(data);
    return true;
  }
}
