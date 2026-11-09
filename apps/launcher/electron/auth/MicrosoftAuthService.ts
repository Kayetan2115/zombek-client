import * as http from 'node:http';
import * as crypto from 'node:crypto';
import { AccountRepository } from './AccountRepository.js';
import { TokenStorage } from './TokenStorage.js';
import type {
  MicrosoftOAuthTokenResponse,
  XboxLiveAuthResponse,
  XstsAuthResponse,
  MinecraftServicesAuthResponse,
  MinecraftStoreEntitlementsResponse,
  MinecraftProfileResponse,
  SecureAccountTokens,
  MicrosoftAuthResult,
  AuthFlowStep,
} from './types.js';
import type { MicrosoftAuthProgress } from '@zombek/shared-types';

let electronShell: typeof import('electron').shell | null = null;
try {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const electron = require('electron');
  if (electron && electron.shell) {
    electronShell = electron.shell;
  }
} catch {
  electronShell = null;
}

export class MicrosoftAuthService {
  private static progressCallback?: (progress: MicrosoftAuthProgress) => void;

  public static setProgressCallback(cb?: (progress: MicrosoftAuthProgress) => void): void {
    this.progressCallback = cb;
  }

  private static emitProgress(step: AuthFlowStep, message: string, error?: string): void {
    this.progressCallback?.({ step, message, error });
  }

  public static getClientId(): string | null {
    const envId = process.env.MICROSOFT_CLIENT_ID?.trim();
    if (envId && envId.length > 0) {
      return envId;
    }
    return null;
  }

  /**
   * Generates cryptographically secure PKCE verifier and challenge
   */
  public static generatePkce(): { verifier: string; challenge: string } {
    const verifier = crypto.randomBytes(32).toString('base64url');
    const challenge = crypto.createHash('sha256').update(verifier).digest('base64url');
    return { verifier, challenge };
  }

  /**
   * Starts local loopback HTTP server to safely capture authorization code callback
   */
  public static async waitForAuthCode(
    clientId: string,
    state: string,
    challenge: string
  ): Promise<{ code: string; redirectUri: string }> {
    return new Promise((resolve, reject) => {
      let isResolved = false;

      const server = http.createServer((req, res) => {
        try {
          if (!req.url || !req.url.startsWith('/callback')) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('Not Found');
            return;
          }

          const parsedUrl = new URL(req.url, 'http://127.0.0.1');
          const incomingState = parsedUrl.searchParams.get('state');
          const incomingCode = parsedUrl.searchParams.get('code');
          const incomingError = parsedUrl.searchParams.get('error');
          const errorDescription = parsedUrl.searchParams.get('error_description');

          if (incomingError) {
            res.writeHead(400, { 'Content-Type': 'text/html; charset=utf-8' });
            res.end(`
              <html>
                <body style="font-family: sans-serif; background: #0f172a; color: #f87171; text-align: center; padding: 40px;">
                  <h2>Logowanie anulowane lub odrzucone</h2>
                  <p>${errorDescription || incomingError}</p>
                  <p>Możesz zamknąć tę kartę przeglądarki.</p>
                </body>
              </html>
            `);
            cleanup();
            reject(new Error(`Microsoft OAuth error: ${incomingError} - ${errorDescription || ''}`));
            return;
          }

          if (incomingState !== state) {
            res.writeHead(400, { 'Content-Type': 'text/plain' });
            res.end('Nieprawidłowy stan sesji (Invalid state parameter).');
            cleanup();
            reject(new Error('Niezgodność parametru state w powrocie z przeglądarki (CSRF protection).'));
            return;
          }

          if (!incomingCode) {
            res.writeHead(400, { 'Content-Type': 'text/plain' });
            res.end('Brak kodu autoryzacyjnego.');
            cleanup();
            reject(new Error('Brak kodu autoryzacyjnego w odpowiedzi Microsoft.'));
            return;
          }

          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
          res.end(`
            <html>
              <body style="font-family: sans-serif; background: #070a0f; color: #10b981; text-align: center; padding: 50px;">
                <h1 style="color: #38bdf8;">ZombekClient</h1>
                <h2>Autoryzacja Microsoft zakończona sukcesem!</h2>
                <p style="color: #94a3b8;">Trwa weryfikacja profilu Minecraft Java Edition...</p>
                <p style="color: #64748b; font-size: 13px;">Możesz bezpiecznie zamknąć tę kartę przeglądarki i wrócić do launchera.</p>
              </body>
            </html>
          `);

          const port = (server.address() as any)?.port;
          const redirectUri = `http://127.0.0.1:${port}/callback`;

          cleanup();
          resolve({ code: incomingCode, redirectUri });
        } catch (err: any) {
          cleanup();
          reject(err);
        }
      });

      const cleanup = () => {
        if (!isResolved) {
          isResolved = true;
          clearTimeout(timeoutId);
          server.close();
        }
      };

      // 5-minute timeout for browser authorization
      const timeoutId = setTimeout(() => {
        cleanup();
        reject(new Error('Przekroczono limit czasu oczekiwania na logowanie w przeglądarce (5 minut).'));
      }, 5 * 60 * 1000);

      // Listen on ephemeral port on loopback interface
      server.listen(0, '127.0.0.1', () => {
        const port = (server.address() as any)?.port;
        const redirectUri = `http://127.0.0.1:${port}/callback`;

        const authUrl =
          `https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?` +
          `client_id=${encodeURIComponent(clientId)}` +
          `&response_type=code` +
          `&redirect_uri=${encodeURIComponent(redirectUri)}` +
          `&scope=${encodeURIComponent('XboxLive.signin offline_access')}` +
          `&state=${encodeURIComponent(state)}` +
          `&code_challenge=${encodeURIComponent(challenge)}` +
          `&code_challenge_method=S256` +
          `&prompt=select_account`;

        if (electronShell) {
          electronShell.openExternal(authUrl);
        } else {
          console.log(`[MicrosoftAuth] Otwórz adres w przeglądarce: ${authUrl}`);
        }
      });

      server.on('error', (err) => {
        cleanup();
        reject(new Error(`Nie udało się uruchomić serwera callback: ${err.message}`));
      });
    });
  }

  /**
   * Exchanges authorization code for Microsoft tokens
   */
  public static async exchangeCodeForMsToken(
    clientId: string,
    code: string,
    redirectUri: string,
    codeVerifier: string
  ): Promise<MicrosoftOAuthTokenResponse> {
    const params = new URLSearchParams({
      client_id: clientId,
      grant_type: 'authorization_code',
      code,
      redirect_uri: redirectUri,
      code_verifier: codeVerifier,
    });

    const res = await fetch('https://login.microsoftonline.com/consumers/oauth2/v2.0/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        Accept: 'application/json',
      },
      body: params.toString(),
    });

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`Błąd wymiany kodu Microsoft OAuth (${res.status}): ${errText}`);
    }

    return (await res.json()) as MicrosoftOAuthTokenResponse;
  }

  /**
   * Authenticates with Xbox Live User endpoint
   */
  public static async authenticateXboxLive(msAccessToken: string): Promise<XboxLiveAuthResponse> {
    const res = await fetch('https://user.auth.xboxlive.com/user/authenticate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({
        Properties: {
          AuthMethod: 'RPS',
          SiteName: 'user.auth.xboxlive.com',
          RpsTicket: `d=${msAccessToken}`,
        },
        RelyingParty: 'http://auth.xboxlive.com',
        TokenType: 'JWT',
      }),
    });

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`Uwierzytelnienie Xbox Live nie powiodło się (${res.status}): ${errText}`);
    }

    return (await res.json()) as XboxLiveAuthResponse;
  }

  /**
   * Authorizes with XSTS endpoint for Minecraft Services
   */
  public static async authorizeXsts(xblToken: string): Promise<XstsAuthResponse> {
    const res = await fetch('https://xsts.auth.xboxlive.com/xsts/authorize', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({
        Properties: {
          SandboxId: 'RETAIL',
          UserTokens: [xblToken],
        },
        RelyingParty: 'rp://api.minecraftservices.com/',
        TokenType: 'JWT',
      }),
    });

    if (!res.ok) {
      const errText = await res.text();
      let xstsError: any = null;
      try {
        xstsError = JSON.parse(errText);
      } catch {
        // ignore
      }

      const xerr = xstsError?.XErr;
      if (xerr === 2148916233) {
        throw new Error(
          'To konto Microsoft nie posiada powiązanego profilu Xbox Live. Wejdź na stronę xbox.com i utwórz profil gracza.'
        );
      }
      if (xerr === 2148916238) {
        throw new Error(
          'Konto Microsoft należy do osoby niepełnoletniej i wymaga zgody rodzica w Microsoft Family Safety.'
        );
      }

      throw new Error(`Autoryzacja XSTS nie powiodła się (${res.status}): ${errText}`);
    }

    return (await res.json()) as XstsAuthResponse;
  }

  /**
   * Logs in to Minecraft Services using XSTS and User Hash
   */
  public static async loginMinecraftServices(
    userHash: string,
    xstsToken: string
  ): Promise<MinecraftServicesAuthResponse> {
    const res = await fetch('https://api.minecraftservices.com/authentication/login_with_xbox', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({
        identityToken: `XBL3.0 x=${userHash};${xstsToken}`,
      }),
    });

    if (!res.ok) {
      const errText = await res.text();
      if (res.status === 403 || errText.toLowerCase().includes('invalid app registration')) {
        throw new Error(
          'BLOCKED_APP_REGISTRATION: Minecraft Services odrzuciło żądanie z kodem 403 (Invalid app registration). Microsoft OAuth i Xbox Live zadziałały poprawnie, lecz użyty Client ID nie został jeszcze dopuszczony przez Mojang/Microsoft do korzystania z Minecraft Services API. Zapoznaj się z docs/microsoft-auth-setup.md.'
        );
      }
      throw new Error(`Logowanie do Minecraft Services nie powiodło się (${res.status}): ${errText}`);
    }

    return (await res.json()) as MinecraftServicesAuthResponse;
  }

  /**
   * Verifies Minecraft store entitlements
   */
  public static async checkEntitlements(
    mcAccessToken: string
  ): Promise<{ hasEntitlement: boolean; items: string[] }> {
    const res = await fetch('https://api.minecraftservices.com/entitlements/mcstore', {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${mcAccessToken}`,
        Accept: 'application/json',
      },
    });

    if (!res.ok) {
      // In case entitlements endpoint returns error, fall back to checking profile
      return { hasEntitlement: false, items: [] };
    }

    const data = (await res.json()) as MinecraftStoreEntitlementsResponse;
    const itemNames = (data.items || []).map((i) => i.name);
    const hasMinecraft = itemNames.some(
      (n) => n === 'product_minecraft' || n === 'game_minecraft'
    );

    return {
      hasEntitlement: hasMinecraft,
      items: itemNames,
    };
  }

  /**
   * Fetches official Minecraft player profile (username, UUID, skins, capes)
   */
  public static async fetchMinecraftProfile(mcAccessToken: string): Promise<MinecraftProfileResponse> {
    const res = await fetch('https://api.minecraftservices.com/minecraft/profile', {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${mcAccessToken}`,
        Accept: 'application/json',
      },
    });

    if (res.status === 404) {
      throw new Error(
        'Konto Microsoft nie posiada jeszcze utworzonego profilu Minecraft Java Edition. Zaloguj się na minecraft.net i ustaw swój nick w grze.'
      );
    }

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`Pobieranie profilu Minecraft nie powiodło się (${res.status}): ${errText}`);
    }

    return (await res.json()) as MinecraftProfileResponse;
  }

  /**
   * Refreshes Microsoft session using refresh token
   */
  public static async refreshAccountSession(accountId: string): Promise<boolean> {
    const tokens = await TokenStorage.getTokens(accountId);
    if (!tokens || !tokens.msRefreshToken) {
      await AccountRepository.updateSessionStatus(accountId, 'requires_login');
      throw new Error('Brak zapisanego tokenu odświeżania. Wymagane ponowne zalogowanie.');
    }

    const clientId = this.getClientId();
    if (!clientId) {
      throw new Error('Brak skonfigurowanego MICROSOFT_CLIENT_ID.');
    }

    try {
      const params = new URLSearchParams({
        client_id: clientId,
        grant_type: 'refresh_token',
        refresh_token: tokens.msRefreshToken,
      });

      const res = await fetch('https://login.microsoftonline.com/consumers/oauth2/v2.0/token', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          Accept: 'application/json',
        },
        body: params.toString(),
      });

      if (!res.ok) {
        await AccountRepository.updateSessionStatus(accountId, 'expired');
        return false;
      }

      const msTokens = (await res.json()) as MicrosoftOAuthTokenResponse;
      const xbl = await this.authenticateXboxLive(msTokens.access_token);
      const userHash = xbl.DisplayClaims.xui[0]?.uhs;
      const xsts = await this.authorizeXsts(xbl.Token);
      const mcAuth = await this.loginMinecraftServices(userHash, xsts.Token);

      const updatedTokens: SecureAccountTokens = {
        msAccessToken: msTokens.access_token,
        msRefreshToken: msTokens.refresh_token || tokens.msRefreshToken,
        msExpiresAt: Date.now() + (msTokens.expires_in || 3600) * 1000,
        mcAccessToken: mcAuth.access_token,
        mcExpiresAt: Date.now() + (mcAuth.expires_in || 86400) * 1000,
        xstsToken: xsts.Token,
        userHash,
      };

      await TokenStorage.setTokens(accountId, updatedTokens);
      await AccountRepository.updateSessionStatus(accountId, 'valid');
      return true;
    } catch (err: any) {
      await AccountRepository.updateSessionStatus(accountId, 'requires_login');
      throw err;
    }
  }

  /**
   * Executes full end-to-end Microsoft authentication flow
   */
  public static async startLogin(): Promise<MicrosoftAuthResult> {
    const clientId = this.getClientId();
    if (!clientId) {
      const msg =
        'Brak skonfigurowanego MICROSOFT_CLIENT_ID w środowisku. ' +
        'Utwórz plik .env na podstawie .env.example i wprowadź Client ID z Microsoft Entra ID. ' +
        'Więcej informacji znajdziesz w docs/microsoft-auth-setup.md.';
      this.emitProgress('failed', msg, msg);
      return {
        success: false,
        error: msg,
        step: 'connecting',
      };
    }

    const state = crypto.randomBytes(16).toString('hex');
    const { verifier, challenge } = this.generatePkce();

    const diagnostic = {
      oauthSuccess: false,
      xboxSuccess: false,
      xstsSuccess: false,
      minecraftServicesStatus: undefined as number | undefined,
      appRegistrationError: false,
      hasEntitlement: false,
    };

    try {
      // Step 1: OAuth Browser Code
      this.emitProgress('waiting_browser', 'Oczekiwanie na autoryzację w zewnętrznej przeglądarce...');
      const { code, redirectUri } = await this.waitForAuthCode(clientId, state, challenge);

      this.emitProgress('connecting', 'Wymiana kodu autoryzacyjnego na tokeny Microsoft...');
      const msTokens = await this.exchangeCodeForMsToken(clientId, code, redirectUri, verifier);
      diagnostic.oauthSuccess = true;

      // Step 2: Xbox Live
      this.emitProgress('xbox_auth', 'Uwierzytelnianie w usłudze Xbox Live...');
      const xbl = await this.authenticateXboxLive(msTokens.access_token);
      diagnostic.xboxSuccess = true;
      const userHash = xbl.DisplayClaims?.xui?.[0]?.uhs;
      if (!userHash) {
        throw new Error('Nie odnaleziono parametru userHash (uhs) w odpowiedzi Xbox Live.');
      }

      // Step 3: XSTS
      this.emitProgress('xbox_auth', 'Pobieranie tokenu XSTS dla Minecraft Services...');
      const xsts = await this.authorizeXsts(xbl.Token);
      diagnostic.xstsSuccess = true;

      // Step 4: Minecraft Services
      this.emitProgress('minecraft_auth', 'Logowanie do oficjalnych usług Minecraft Services...');
      let mcAuth: MinecraftServicesAuthResponse;
      try {
        mcAuth = await this.loginMinecraftServices(userHash, xsts.Token);
      } catch (err: any) {
        if (err.message.startsWith('BLOCKED_APP_REGISTRATION')) {
          diagnostic.appRegistrationError = true;
          diagnostic.minecraftServicesStatus = 403;
        }
        throw err;
      }

      // Step 5: Entitlements
      this.emitProgress('checking_entitlements', 'Sprawdzanie licencji Minecraft Java Edition...');
      const { hasEntitlement, items } = await this.checkEntitlements(mcAuth.access_token);
      diagnostic.hasEntitlement = hasEntitlement;

      // Step 6: Profile
      this.emitProgress('fetching_profile', 'Pobieranie oficjalnego profilu gracza Minecraft...');
      const profile = await this.fetchMinecraftProfile(mcAuth.access_token);

      // Step 7: Saving
      this.emitProgress('saving', 'Bezpieczne zapisywanie konta gracza...');
      const tokens: SecureAccountTokens = {
        msAccessToken: msTokens.access_token,
        msRefreshToken: msTokens.refresh_token,
        msExpiresAt: Date.now() + (msTokens.expires_in || 3600) * 1000,
        mcAccessToken: mcAuth.access_token,
        mcExpiresAt: Date.now() + (mcAuth.expires_in || 86400) * 1000,
        xstsToken: xsts.Token,
        userHash,
      };

      const account = await AccountRepository.saveMicrosoftAccount(
        profile,
        tokens,
        hasEntitlement,
        items
      );

      this.emitProgress('completed', `Zalogowano pomyślnie jako ${account.username}!`);
      return {
        success: true,
        account,
        diagnostic,
      };
    } catch (err: any) {
      this.emitProgress('failed', err.message, err.message);
      return {
        success: false,
        error: err.message,
        diagnostic,
      };
    }
  }
}
