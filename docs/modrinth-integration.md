# Dokumentacja Integracji Modrinth — ZombekClient

Niniejszy dokument opisuje architekturę, implementację i działanie integracji ZombekClient z platformą **Modrinth** w ramach **ETAPU 6B**.

---

## 1. Wprowadzenie i Cel

ZombekClient integruje się bezpośrednio z oficjalnym **Modrinth API v2** (`https://api.modrinth.com/v2`), oferując graczom i twórcom modpacków możliwość:
- przeszukiwania katalogu ponad 100 000 modów bezpośrednio z okna launchera,
- przeglądania popularnych, najnowszych i najczęściej aktualizowanych projektów,
- filtrowania po wybranym modloaderze (Fabric, Forge, NeoForge, Quilt), wersji gry Minecraft, kategorii i środowisku (Client/Server),
- wyświetlania galerii screenshotów, pełnych opisów i wykazów wydań (Release, Beta, Alpha),
- bezpiecznego pobierania plików JAR z Modrinth CDN z natychmiastową weryfikacją sumy kontrolnej SHA-512,
- bezpośredniej instalacji pobranych modów do aktywnego profilu gry w izolowanym katalogu `instances/<id>/mods/`,
- analizowania zależności wymaganych i opcjonalnych z ostrzeganiem gracza przed brakującymi bibliotekami.

---

## 2. Architektura Modułów

Całość integracji została zaimplementowana w warstwie głównej procesu Electron (`apps/launcher/electron/modrinth/`) oraz warstwie interfejsu React (`apps/launcher/src/components/modrinth/`):

```
apps/launcher/
├── electron/
│   └── modrinth/
│       ├── ModrinthApiClient.ts          # Komunikacja z API, nagłówki, zapytania facets, dwuwarstwowy cache, obsługa 429
│       ├── ModrinthDownloader.ts         # Bezpieczne pobieranie strumieniowe z CDN, ochrona SSRF, weryfikacja SHA-512/SHA-1
│       └── ModrinthInstallationManager.ts # Logika instalacji: selekcja JAR, sprawdzanie zgodności, duplikaty, zależności
├── src/
│   ├── components/
│   │   └── modrinth/
│   │       ├── ModrinthCatalog.tsx       # Główny widok katalogu online z filtrami, paginacją i kartami modów
│   │       └── ModrinthProjectModal.tsx  # Szczegółowy modal projektu: opis, galeria, wykaz wersji, atrybucja
│   └── pages/
│       └── ModsPage.tsx                  # Zintegrowany przełącznik widoków: Pliki (.jar) / Katalog Modrinth / Nakładki HUD
└── tests/
    └── modrinth.test.ts                  # Zestaw 13 testów jednostkowych i integracyjnych z weryfikacją Live API
```

---

## 3. Zgodność z Wytycznymi Modrinth API (API Policy)

### 3.1. Nagłówek User-Agent
Zgodnie z regulaminem Modrinth, każde oprogramowanie korzystające z API musi przekazywać unikalny nagłówek `User-Agent` zawierający nazwę, wersję oraz kontakt:
```http
User-Agent: ZombekClient/0.1.0 (https://github.com/zombekclient/zombekclient)
```

### 3.2. Zarządzanie Limitami Zapytań (Rate Limiting)
- Standardowy limit wynosi **300 zapytań na minutę** na adres IP.
- `ModrinthApiClient` odczytuje nagłówki odpowiedzi:
  - `x-ratelimit-limit`
  - `x-ratelimit-remaining`
  - `x-ratelimit-reset` (czas w sekundach do zresetowania puli)
- Jeśli serwer zwróci kod **HTTP 429 (Too Many Requests)** lub pula zapytań zostanie wyczerpana:
  - Rzucany jest dedykowany błąd `ModrinthRateLimitError`.
  - W interfejsie użytkownika pojawia się żółty banner informacyjny z zegarem odliczającym czas do odnowienia puli.
  - Aplikacja wstrzymuje automatyczne zapytania, chroniąc przed blokadą IP.

### 3.3. Buforowanie (Dwuwarstwowy Cache)
1. **Pamięć RAM (L1 Cache):**
   - Wyniki wyszukiwania tekstowego: 2 minuty.
   - Szczegóły projektu (`/project/{id}`): 10 minut.
   - Listy wersji (`/project/{id}/version`): 5 minut.
2. **Dysk lokalny (L2 Cache):**
   - Statyczne tagi (`categories.json`, `loaders.json`, `game_versions.json`) zapisywane w `%APPDATA%/ZombekClient/cache/modrinth/` z czasem ważności 24 godziny.

---

## 4. Składnia Zapytań Wyszukiwania (`facets`)

Wyszukiwarka Modrinth wykorzystuje format JSON składający się z tablicy tablic:
- Wpisy wewnątrz danej podtablicy łączone są alternatywą (**OR**).
- Podtablice łączone są koniunkcją (**AND**).

Przykład wygenerowanego zapytania dla instalacji Fabric Minecraft 1.20.4 z kategorią `optimization`:
```json
[
  ["project_type:mod"],
  ["categories:fabric"],
  ["versions:1.20.4"],
  ["categories:optimization"],
  ["client_side:required", "client_side:optional", "client_side:unknown"]
]
```

Domyślnie ZombekClient eliminuje mody czysto serwerowe (`client_side:unsupported`), gwarantując, że gracz nie zainstaluje narzędzi bezużytecznych w środowisku klienta desktopowego.

---

## 5. Bezpieczeństwo i Weryfikacja Integralności

### 5.1. Bezpieczny Downloader (`ModrinthDownloader`)
- **Biała lista domen CDN:** Akceptowane są wyłącznie `cdn.modrinth.com`, `edge.modrinth.com` oraz `*.modrinth.com`.
- **Wymóg protokołu:** Wyłącznie bezpieczny protokół `https:`. Próby użycia `http:`, `file:`, `ftp:` są blokowane.
- **Ochrona przed SSRF:** Adresy lokalne (`localhost`, `127.0.0.1`, `0.0.0.0`) oraz prywatne zakresy IP (`192.168.*`, `10.*`, `172.16.*`) są bezwzględnie odrzucane.

### 5.2. Weryfikacja Sum Kontrolnych (SHA-512 & SHA-1)
Przed przeniesieniem pobranego pliku `.jar` do katalogu `instances/<id>/mods/`:
1. Plik pobierany jest strumieniowo do tymczasowej lokalizacji (`temp-downloads/`).
2. Obliczana jest suma kontrolna **SHA-512**.
3. Suma jest porównywana z wartością zwracaną przez API w obiekcie `hashes.sha512`.
4. W przypadku braku SHA-512 weryfikowana jest suma **SHA-1** (`hashes.sha1`).
5. Jeśli suma nie jest identyczna co do bajtu, plik jest **natychmiastowo kasowany z dysku**, a użytkownik otrzymuje ostrzeżenie o uszkodzeniu lub manipulacji plikiem.

---

## 6. Integracja z Loaderami i Wsteczna Kompatybilność

| Loader Instalacji | Obsługiwane Mody z Modrinth | Kompatybilność |
| :--- | :--- | :---: |
| **Fabric** | Mody z tagiem `fabric` | Pełna |
| **Forge** | Mody z tagiem `forge` | Pełna |
| **NeoForge** | Mody z tagiem `neoforge` | Pełna |
| **Quilt** | Mody z tagiem `quilt` oraz `fabric` | Pełna (Quilt wspiera mody Fabric) |
| **Vanilla** | Żadne | Zablokowane (wymagany modloader) |

W przypadku wyboru instancji Vanilla interfejs informuje gracza o braku obsługi modów w czystym silniku Mojang i proponuje utworzenie nowej instalacji z modloaderem.

---

## 7. Kanały IPC

Integracja komunikuje się przez dedykowane kanały Electron IPC:

| Kanał IPC | Opis |
| :--- | :--- |
| `modrinth:search` | Wyszukiwanie modów z parametrami i stronicowaniem |
| `modrinth:get-project` | Pobranie pełnego profilu projektu (opis, autor, licencja, galeria) |
| `modrinth:get-project-versions` | Pobranie listy wydań danego projektu z opcjonalnymi filtrami |
| `modrinth:get-version` | Pobranie szczegółów pojedynczego wydania |
| `modrinth:get-categories` | Lista oficjalnych kategorii z ikonami i opisami |
| `modrinth:get-loaders` | Lista oficjalnych loaderów wspieranych przez platformę |
| `modrinth:get-game-versions` | Lista oficjalnych wersji Minecraft |
| `modrinth:install-version` | Pobranie, weryfikacja SHA-512 i instalacja moda do instalacji |
| `mods:check-updates` | Zbiorcze sprawdzanie aktualizacji przez SHA-512 i POST /version_files/update |
| `mods:resolve-dependencies` | Rekurencyjne rozwiązywanie zależności dla danego wydania Modrinth |
| `mods:pin-mod` | Blokowanie/odblokowywanie wersji moda przed automatycznymi aktualizacjami |
| `mods:set-update-policy` | Zmiana polityki aktualizacji (stable-only, stable-and-beta, all-versions) |
| `mods:apply-transaction` | Transakcyjne zastosowanie aktualizacji z auto-rollbackiem i kopią zapasową |
| `mods:get-backups` | Pobranie listy punktów przywracania modpacka (kopii zapasowych) |
| `mods:rollback-backup` | Przywrócenie stanu modpacka do wybranej migawki |
| `mods:get-lockfile` | Pobranie aktualnego pliku modpack.lock.json dla instancji |
| `mrpack:inspect` | Bezinwazyjna inspekcja paczki .mrpack przed instalacją (Dry-Run) |
| `mrpack:import` | Transakcyjny import paczki .mrpack, pobieranie modów z CDN i overrides |
| `mrpack:export` | Eksport instancji gry do oficjalnego formatu .mrpack zgodnego ze specyfikacją |
| `mrpack:open-file-dialog` | Otwarcie systemowego okna dialogowego wyboru pliku .mrpack |
| `mrpack:open-save-dialog` | Otwarcie systemowego okna dialogowego zapisu pliku .mrpack |

---

## 8. Zrzuty z Testów Automatycznych

Zestaw testów weryfikuje poprawność integracji:
- 13 testów w `apps/launcher/tests/modrinth.test.ts`,
- 28 testów w `apps/launcher/tests/updates.test.ts`,
- 29 testów w `apps/launcher/tests/mrpack.test.ts`,
- Łącznie **166 testów** w całym projekcie ZombekClient,
- 100% testów przechodzi pomyślnie (`pnpm test`).

---

## 9. Powiązane Dokumenty (Rozszerzenia Etapu 6D)

- [Rekurencyjne Rozwiązywanie Zależności](dependency-resolution.md)
- [System Wykrywania i Zarządzania Aktualizacjami Modów](mod-update-system.md)
- [Potok Transakcyjny i System Przywracania (Rollback)](transaction-and-rollback.md)
- [Import Modpacków Modrinth .mrpack](mrpack-import.md)
- [Eksport Modpacków Modrinth .mrpack](mrpack-export.md)
- [Architektura Bezpieczeństwa Formatu MRPACK](mrpack-security.md)
