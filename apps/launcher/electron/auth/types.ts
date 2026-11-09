import type { PlayerSkin, PlayerCape } from '@zombek/shared-types';

export interface SecureAccountTokens {
  msAccessToken?: string;
  msRefreshToken: string;
  msExpiresAt?: number;
  mcAccessToken: string;
  mcExpiresAt: number;
  xstsToken?: string;
  userHash?: string;
}

export interface MicrosoftOAuthTokenResponse {
  token_type: string;
  expires_in: number;
  scope: string;
  access_token: string;
  refresh_token: string;
  id_token?: string;
  user_id?: string;
}

export interface XboxLiveAuthResponse {
  IssueInstant: string;
  NotAfter: string;
  Token: string;
  DisplayClaims: {
    xui: Array<{
      uhs: string;
      agg?: string;
      gtg?: string;
      prv?: string;
      xid?: string;
    }>;
  };
}

export interface XstsAuthResponse {
  IssueInstant: string;
  NotAfter: string;
  Token: string;
  DisplayClaims: {
    xui: Array<{
      uhs: string;
    }>;
  };
}

export interface MinecraftServicesAuthResponse {
  username: string;
  roles: string[];
  access_token: string;
  token_type: string;
  expires_in: number;
}

export interface MinecraftStoreEntitlementsResponse {
  items: Array<{
    name: string;
    signature: string;
  }>;
  signature?: string;
  keyId?: string;
}

export interface MinecraftProfileResponse {
  id: string;
  name: string;
  skins: PlayerSkin[];
  capes: PlayerCape[];
}

export type AuthFlowStep =
  | 'connecting'
  | 'waiting_browser'
  | 'xbox_auth'
  | 'minecraft_auth'
  | 'checking_entitlements'
  | 'fetching_profile'
  | 'saving'
  | 'completed'
  | 'failed';

export interface MicrosoftAuthResult {
  success: boolean;
  account?: import('@zombek/shared-types').AccountRecord;
  error?: string;
  step?: AuthFlowStep;
  diagnostic?: {
    oauthSuccess: boolean;
    xboxSuccess: boolean;
    xstsSuccess: boolean;
    minecraftServicesStatus?: number;
    appRegistrationError?: boolean;
    hasEntitlement?: boolean;
  };
}
