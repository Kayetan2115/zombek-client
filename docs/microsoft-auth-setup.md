# Konfiguracja Autoryzacji Microsoft & Minecraft Services — ZombekClient

Niniejszy dokument opisuje szczegółowo architekturę uwierzytelniania kont Microsoft, proces rejestracji aplikacji w Microsoft Entra ID oraz specyfikę oficjalnego API Minecraft Services.

---

## 1. Architektura Uwierzytelniania (6-Krokowy Potok)

ZombekClient implementuje oficjalny standard uwierzytelniania kont Microsoft dla Minecraft Java Edition.
Cały potok realizowany jest w procesie głównym Electrona (`apps/launcher/electron/auth/MicrosoftAuthService.ts`) bez ujawniania sekretów ani tokenów w warstwie renderera (React).

```text
[Użytkownik] 
     │
     ▼ (1. Zewnętrzna Przeglądarka Systemowa: OAuth 2.0 Authorization Code + PKCE)
[Microsoft Identity Platform / login.microsoftonline.com]
     │ (Microsoft Access Token + Refresh Token)
     ▼ (2. Xbox Live User Authentication: user.auth.xboxlive.com)
[Xbox Live]
     │ (Xbox Token + User Hash `uhs`)
     ▼ (3. XSTS Authorization: xsts.auth.xboxlive.com / RelyingParty: rp://api.minecraftservices.com/)
[XSTS Service]
     │ (XSTS Token)
     ▼ (4. Minecraft Services Login: api.minecraftservices.com/authentication/login_with_xbox)
[Minecraft Services]
     │ (Minecraft Access Token JWT)
     ├──────────────────────────────────────────┐
     ▼ (5. Sprawdzenie Licencji)                ▼ (6. Pobranie Profilu)
[/entitlements/mcstore]                   [/minecraft/profile]
(product_minecraft / game_minecraft)      (UUID, Nick, Skin, Peleryny)
```

---

## 2. Rejestracja Własnej Aplikacji w Microsoft Entra ID

Aby umożliwić użytkownikom logowanie przez oficjalne konto Microsoft, należy zarejestrować aplikację w portalu **Microsoft Entra** (dawniej Azure AD):

1. Zaloguj się do portalu [Azure Portal (Entra ID)](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationsListBlade).
2. Wybierz **Nowa rejestracja (New registration)**.
3. Wypełnij formularz:
   - **Nazwa:** `ZombekClient Launcher` (lub własna nazwa).
   - **Obsługiwane typy kont (Supported account types):** 
     Wybierz: **Konta w dowolnym katalogu organizacyjnym (dowolny katalog Microsoft Entra — wielodostępny) i konta osobiste Microsoft (np. Skype, Xbox)** (`common`) LUB **Tylko konta osobiste Microsoft (Personal Microsoft accounts only)** (`consumers`).
   - **Identyfikator URI przekierowania (Redirect URI):**
     - Platforma: **Aplikacja klasyczna i mobilna (Public client/native)**.
     - Wprowadź: `http://127.0.0.1/callback` oraz `msal{TWÓJ_CLIENT_ID}://auth`.
4. Po utworzeniu aplikacji przejdź do widoku **Przegląd (Overview)** i skopiuj **Identyfikator aplikacji (klienta) — Application (client) ID**.
5. Wklej uzyskany identyfikator do pliku `.env` w katalogu głównym projektu:
   ```env
   MICROSOFT_CLIENT_ID=twoj-client-id-z-azure
   ```

> [!IMPORTANT]
> **Aplikacja Desktopowa jest Klientem Publicznym (Public Client):**
> Nie twórz Client Secret (klucza tajnego klienta). Zgodnie ze standardem RFC 8252 aplikacje desktopowe nie są w stanie bezpiecznie ukryć sekretu klienta, dlatego autoryzacja korzysta wyłącznie z mechanizmu **PKCE (Proof Key for Code Exchange)**.

---

## 3. Wymagane Uprawnienia (OAuth Scopes)

Do poprawnego uwierzytelnienia w usłudze Xbox Live i późniejszego odświeżania sesji wymagane są następujące zakresy:
- `XboxLive.signin` — pozwala na uzyskanie biletu RPS wymaganego przez endpoint `user.auth.xboxlive.com`.
- `offline_access` — zapewnia wydanie długoterminowego `refresh_token`, umożliwiającego automatyczne odnawianie sesji w tle bez konieczności ponownego logowania przez przeglądarkę.

---

## 4. Ograniczenie Minecraft Services (Błąd 403: Invalid app registration)

### Kontekst Techniczny
Microsoft Identity Platform oraz Xbox Live akceptują dowolny poprawnie skonfigurowany publiczny Client ID. Jednak na 4. etapie (`api.minecraftservices.com/authentication/login_with_xbox`) Mojang weryfikuje identyfikator aplikacji na wewnętrznej liście dozwolonych launcherów.

W przypadku nowo zarejestrowanego Client ID, Minecraft Services może zwrócić odpowiedź:
```json
HTTP/1.1 403 Forbidden
{
  "path": "/authentication/login_with_xbox",
  "error": "Invalid app registration",
  "errorMessage": "The application registration is not valid for this operation."
}
```

### Diagnoza w ZombekClient
ZombekClient w pełni obsługuje tę sytuację:
1. Rejestruje sukces etapów 1, 2 oraz 3 (Microsoft OAuth, Xbox Live, XSTS zakończyły się sukcesem).
2. Wskazuje dokładnie, że błąd 403 wystąpił po stronie Minecraft Services.
3. Wyświetla czytelny komunikat diagnostyczny i nie fałszuje stanu posiadania licencji.
4. **ZombekClient nie podszywa się pod inne launchery i nie wykrada cudzych identyfikatorów.**

---

## 5. Bezpieczne Przechowywanie Tokenów (Zero Plain-Text)

Zgodnie z wymaganiami bezpieczeństwa ZombekClient:
- Wszystkie tokeny dostępu (`mcAccessToken`, `xstsToken`, `msRefreshToken`) są natychmiast szyfrowane sprzętowo za pomocą **Electron safeStorage** (wykorzystującego **Windows DPAPI**).
- Plik `accounts.json` zawiera wyłącznie metadane publiczne (nick, UUID, link do miniatury skina, status ważności sesji).
- Tokeny **nigdy** nie są przesyłane przez IPC do renderera (React).
- Wszelkie argumenty linii poleceń przekazywane do `child_process.spawn` są filtrowane przed zalogowaniem — flagi `--accessToken` są maskowane ciągiem `[REDACTED]`.

---

## 6. Profile Offline (Non-Premium)

Dla użytkowników niekorzystających z konta Microsoft launcher zapewnia w 100% lokalne profile Offline:
- Generowanie UUID następuje według standardu Minecraft Java: `MD5("OfflinePlayer:" + nick)` (wersja 3, wariant IETF).
- Walidacja nicku: długość 3–16 znaków, znaki `[a-zA-Z0-9_]`.
- Token sesji przekazywany do gry to `'0'`, a typ użytkownika to `'offline'`.
- Profile offline są wyraźnie oznaczone jako lokalne i nie posiadają statusu Premium.
