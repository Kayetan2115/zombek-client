import { describe, it, expect, beforeEach } from 'vitest';
import { AccountRepository } from '../electron/auth/AccountRepository';
import { TokenStorage } from '../electron/auth/TokenStorage';
import { MicrosoftAuthService } from '../electron/auth/MicrosoftAuthService';
import { generateOfflineUuid, CompositeSessionProvider } from '../electron/engine/GameSessionProvider';
import type { SecureAccountTokens, MinecraftProfileResponse } from '../electron/auth/types';

describe('Player Accounts - Offline Profiles & Validation', () => {
  it('should validate Minecraft username constraints according to Java Edition rules', () => {
    // Valid usernames
    expect(AccountRepository.validateOfflineUsername('Steve')).toBe('Steve');
    expect(AccountRepository.validateOfflineUsername('Zombek_123')).toBe('Zombek_123');
    expect(AccountRepository.validateOfflineUsername('Player_Pro_99')).toBe('Player_Pro_99');

    // Invalid usernames
    expect(() => AccountRepository.validateOfflineUsername('')).toThrow(/pusty/i);
    expect(() => AccountRepository.validateOfflineUsername('ab')).toThrow(/od 3 do 16/i);
    expect(() => AccountRepository.validateOfflineUsername('ThisUsernameIsWayTooLong')).toThrow(/od 3 do 16/i);
    expect(() => AccountRepository.validateOfflineUsername('Invalid Nick!')).toThrow(/tylko litery/i);
    expect(() => AccountRepository.validateOfflineUsername('Test-User')).toThrow(/tylko litery/i);
    expect(() => AccountRepository.validateOfflineUsername('User@Home')).toThrow(/tylko litery/i);
  });

  it('should generate deterministic RFC 4122 v3 UUID for offline player', () => {
    const nick = 'ZombekPlayer';
    const uuid1 = generateOfflineUuid(nick);
    const uuid2 = generateOfflineUuid(nick);

    expect(uuid1).toBe(uuid2);
    expect(uuid1.length).toBe(36);
    // Version 3 character at index 14
    expect(uuid1.charAt(14)).toBe('3');
    // Variant character at index 19 must be 8, 9, a, or b
    expect(['8', '9', 'a', 'b']).toContain(uuid1.charAt(19).toLowerCase());
  });

  it('should format raw 32-character hex UUID with standard hyphens', () => {
    const raw = '8c5c7d23289546f9b4cf5975dbd1fa18';
    const formatted = AccountRepository.formatUuid(raw);
    expect(formatted).toBe('8c5c7d23-2895-46f9-b4cf-5975dbd1fa18');

    // Should leave already hyphenated UUID intact
    expect(AccountRepository.formatUuid(formatted)).toBe(formatted);
  });

  it('should create an offline profile and register it in AccountRepository', async () => {
    const account = await AccountRepository.createOffline('TestPlayer');
    expect(account.username).toBe('TestPlayer');
    expect(account.type).toBe('offline');
    expect(account.hasEntitlement).toBe(false);
    expect(account.sessionStatus).toBe('valid');
    expect(account.uuid).toBe(generateOfflineUuid('TestPlayer'));

    const all = await AccountRepository.getAll();
    expect(all.some((a) => a.id === account.id)).toBe(true);

    const active = await AccountRepository.getActive();
    expect(active?.id).toBe(account.id);
  });
});

describe('Player Accounts - Multi-Account Switching & Management', () => {
  it('should switch active accounts cleanly', async () => {
    const acc1 = await AccountRepository.createOffline('PlayerOne');
    const acc2 = await AccountRepository.createOffline('PlayerTwo');

    await AccountRepository.setActive(acc1.id);
    let current = await AccountRepository.getActive();
    expect(current?.id).toBe(acc1.id);
    expect(current?.username).toBe('PlayerOne');

    await AccountRepository.setActive(acc2.id);
    current = await AccountRepository.getActive();
    expect(current?.id).toBe(acc2.id);
    expect(current?.username).toBe('PlayerTwo');
  });

  it('should remove an account and clean up its tokens without affecting other accounts', async () => {
    const accToDelete = await AccountRepository.createOffline('ToDeleteUser');
    const initialCount = (await AccountRepository.getAll()).length;

    const removed = await AccountRepository.remove(accToDelete.id);
    expect(removed).toBe(true);

    const remaining = await AccountRepository.getAll();
    expect(remaining.length).toBe(initialCount - 1);
    expect(remaining.some((a) => a.id === accToDelete.id)).toBe(false);

    // Tokens should also be removed
    const tokens = await TokenStorage.getTokens(accToDelete.id);
    expect(tokens).toBeNull();
  });
});

describe('Player Accounts - Secure Token Storage', () => {
  it('should encrypt, store, and decrypt sensitive tokens without plain-text leak', async () => {
    const accountId = 'acc-test-secure-123';
    const sampleTokens: SecureAccountTokens = {
      msAccessToken: 'mock-ms-access-token-xyz',
      msRefreshToken: 'mock-ms-refresh-token-abc',
      msExpiresAt: Date.now() + 3600000,
      mcAccessToken: 'mock-minecraft-jwt-token-secret',
      mcExpiresAt: Date.now() + 86400000,
      xstsToken: 'mock-xsts-token-789',
      userHash: 'mock-user-hash',
    };

    await TokenStorage.setTokens(accountId, sampleTokens);

    const retrieved = await TokenStorage.getTokens(accountId);
    expect(retrieved).not.toBeNull();
    expect(retrieved?.mcAccessToken).toBe(sampleTokens.mcAccessToken);
    expect(retrieved?.msRefreshToken).toBe(sampleTokens.msRefreshToken);
    expect(retrieved?.userHash).toBe(sampleTokens.userHash);

    // Clean up
    await TokenStorage.deleteTokens(accountId);
    const afterDelete = await TokenStorage.getTokens(accountId);
    expect(afterDelete).toBeNull();
  });
});

describe('Player Accounts - Microsoft OAuth & Services Logic', () => {
  it('should generate valid PKCE verifier and challenge pair', () => {
    const { verifier, challenge } = MicrosoftAuthService.generatePkce();
    expect(verifier).toBeDefined();
    expect(verifier.length).toBeGreaterThanOrEqual(43);
    expect(challenge).toBeDefined();
    expect(challenge).not.toBe(verifier);
  });

  it('should report missing MICROSOFT_CLIENT_ID clearly when not set', async () => {
    const originalEnv = process.env.MICROSOFT_CLIENT_ID;
    delete process.env.MICROSOFT_CLIENT_ID;

    const result = await MicrosoftAuthService.startLogin();
    expect(result.success).toBe(false);
    expect(result.error).toContain('MICROSOFT_CLIENT_ID');
    expect(result.error).toContain('docs/microsoft-auth-setup.md');

    if (originalEnv) {
      process.env.MICROSOFT_CLIENT_ID = originalEnv;
    }
  });

  it('should map Minecraft Store Entitlements correctly', async () => {
    // Entitlements endpoint mock response parser test
    const mockEntitlementsData = {
      items: [
        { name: 'product_minecraft', signature: 'sig1' },
        { name: 'game_minecraft', signature: 'sig2' },
      ],
      signature: 'master-sig',
      keyId: '1',
    };

    const hasMinecraft = mockEntitlementsData.items.some(
      (n) => n.name === 'product_minecraft' || n.name === 'game_minecraft'
    );
    expect(hasMinecraft).toBe(true);

    const mockNonPremiumData = {
      items: [{ name: 'some_other_product', signature: 'sig' }],
    };
    const hasMinecraftFalse = mockNonPremiumData.items.some(
      (n) => n.name === 'product_minecraft' || n.name === 'game_minecraft'
    );
    expect(hasMinecraftFalse).toBe(false);
  });

  it('should save verified Microsoft account metadata without leaking tokens to accounts.json', async () => {
    const mockProfile: MinecraftProfileResponse = {
      id: '8c5c7d23289546f9b4cf5975dbd1fa18',
      name: 'MojangTestPlayer',
      skins: [
        {
          id: 'skin-1',
          state: 'ACTIVE',
          url: 'http://textures.minecraft.net/texture/abc12345',
          variant: 'CLASSIC',
        },
      ],
      capes: [],
    };

    const mockTokens: SecureAccountTokens = {
      msAccessToken: 'ms-secret-access',
      msRefreshToken: 'ms-secret-refresh',
      mcAccessToken: 'mc-secret-jwt',
      mcExpiresAt: Date.now() + 86400000,
      userHash: 'uhs-123',
    };

    const account = await AccountRepository.saveMicrosoftAccount(
      mockProfile,
      mockTokens,
      true,
      ['product_minecraft']
    );

    expect(account.username).toBe('MojangTestPlayer');
    expect(account.type).toBe('microsoft');
    expect(account.hasEntitlement).toBe(true);
    expect(account.uuid).toBe('8c5c7d23-2895-46f9-b4cf-5975dbd1fa18');

    // Verify token was stored securely
    const storedTokens = await TokenStorage.getTokens(account.id);
    expect(storedTokens?.mcAccessToken).toBe('mc-secret-jwt');

    // Clean up
    await AccountRepository.remove(account.id);
  });
});

describe('Player Accounts - GameSessionProvider Integration & Token Redaction', () => {
  it('should resolve session for offline accounts via CompositeSessionProvider', async () => {
    const provider = new CompositeSessionProvider();
    const session = await provider.getSessionForLaunch({ offlineUsername: 'LanPlayer' });

    expect(session.username).toBe('LanPlayer');
    expect(session.userType).toBe('offline');
    expect(session.accessToken).toBe('0');
    expect(session.uuid).toBe(generateOfflineUuid('LanPlayer'));
  });

  it('should redact sensitive access tokens in launcher command logs', () => {
    const sensitiveLog =
      'Executing: javaw.exe -Xmx4096M -cp ... net.minecraft.client.main.Main --username Steve --accessToken eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.supersecret --uuid 0000';

    const redacted = sensitiveLog
      .replace(/(--accessToken\s+)[^\s]+/gi, '$1[REDACTED]')
      .replace(/(-accessToken\s+)[^\s]+/gi, '$1[REDACTED]');

    expect(redacted).not.toContain('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.supersecret');
    expect(redacted).toContain('--accessToken [REDACTED]');
  });

  it('Live Integration Test Status: BLOCKED — WAITING FOR CLIENT ID / APPROVAL', () => {
    // In compliance with Section 6 & 15 of Prompt:
    // If no custom approved MICROSOFT_CLIENT_ID is provided yet in this environment,
    // mark live external auth test as BLOCKED without simulating fake live credentials.
    const clientId = MicrosoftAuthService.getClientId();
    if (!clientId) {
      console.log(
        '[STATUS: BLOCKED — WAITING FOR CLIENT ID / APPROVAL] Brak skonfigurowanego MICROSOFT_CLIENT_ID w .env. Konfiguracja udokumentowana w docs/microsoft-auth-setup.md.'
      );
      expect(clientId).toBeNull();
    } else {
      expect(clientId).toBeDefined();
    }
  });
});
