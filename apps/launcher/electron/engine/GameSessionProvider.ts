import crypto from 'node:crypto';
import { AccountRepository } from '../auth/AccountRepository.js';
import { TokenStorage } from '../auth/TokenStorage.js';
import { MicrosoftAuthService } from '../auth/MicrosoftAuthService.js';

export interface GameSession {
  username: string;
  uuid: string;
  accessToken: string;
  userType: 'mojang' | 'msa' | 'offline';
  xuid?: string;
}

export interface GameSessionProvider {
  getSession(identifier: string): Promise<GameSession>;
}

/**
 * Generates an offline player UUID matching java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8))
 */
export function generateOfflineUuid(username: string): string {
  const hash = crypto.createHash('md5').update(`OfflinePlayer:${username}`, 'utf8').digest();

  // Set version to 3 (name-based MD5)
  hash[6] = (hash[6] & 0x0f) | 0x30;
  // Set variant to IETF (RFC 4122)
  hash[8] = (hash[8] & 0x3f) | 0x80;

  const hex = hash.toString('hex');
  return [
    hex.substring(0, 8),
    hex.substring(8, 12),
    hex.substring(12, 16),
    hex.substring(16, 20),
    hex.substring(20, 32),
  ].join('-');
}

export class OfflineSessionProvider implements GameSessionProvider {
  async getSession(username: string): Promise<GameSession> {
    const cleanUsername = username.trim() || 'ZombekPlayer';
    const uuid = generateOfflineUuid(cleanUsername);

    return {
      username: cleanUsername,
      uuid,
      accessToken: '0',
      userType: 'offline',
    };
  }
}

export class MicrosoftSessionProvider implements GameSessionProvider {
  async getSession(accountId: string): Promise<GameSession> {
    const account = await AccountRepository.getById(accountId);
    if (!account) {
      throw new Error(`Konto o ID '${accountId}' nie zostało znalezione.`);
    }

    if (account.type !== 'microsoft') {
      throw new Error(`Konto '${account.username}' nie jest kontem Microsoft.`);
    }

    let tokens = await TokenStorage.getTokens(accountId);
    if (!tokens || !tokens.mcAccessToken) {
      await AccountRepository.updateSessionStatus(accountId, 'requires_login');
      throw new Error(`Brak ważnego tokenu dla konta ${account.username}. Zaloguj się ponownie.`);
    }

    // Check if token expires within 5 minutes (300,000 ms)
    const fiveMinutes = 5 * 60 * 1000;
    const isExpiringSoon = tokens.mcExpiresAt && Date.now() + fiveMinutes >= tokens.mcExpiresAt;

    if (isExpiringSoon && tokens.msRefreshToken) {
      try {
        await MicrosoftAuthService.refreshAccountSession(accountId);
        tokens = await TokenStorage.getTokens(accountId);
      } catch (err: any) {
        console.warn(`[MicrosoftSessionProvider] Nie udało się odświeżyć tokenu: ${err.message}`);
      }
    }

    if (!tokens || !tokens.mcAccessToken) {
      throw new Error(`Sesja konta ${account.username} wygasła. Wymagane ponowne logowanie.`);
    }

    return {
      username: account.username,
      uuid: account.uuid,
      accessToken: tokens.mcAccessToken,
      userType: 'msa',
      xuid: account.xuid,
    };
  }
}

export class CompositeSessionProvider implements GameSessionProvider {
  private offlineProvider = new OfflineSessionProvider();
  private microsoftProvider = new MicrosoftSessionProvider();

  async getSession(target: string): Promise<GameSession> {
    // If target is an account ID registered in repository
    const account = await AccountRepository.getById(target);
    if (account) {
      if (account.type === 'microsoft') {
        return this.microsoftProvider.getSession(account.id);
      } else {
        return this.offlineProvider.getSession(account.username);
      }
    }

    // Otherwise assume target is an offline username
    return this.offlineProvider.getSession(target);
  }

  async getSessionForLaunch(options: { accountId?: string; offlineUsername?: string }): Promise<GameSession> {
    if (options.accountId) {
      return this.getSession(options.accountId);
    }

    if (options.offlineUsername) {
      return this.offlineProvider.getSession(options.offlineUsername);
    }

    // Default to currently active account
    const active = await AccountRepository.getActive();
    if (active) {
      return this.getSession(active.id);
    }

    return this.offlineProvider.getSession('ZombekPlayer');
  }
}
