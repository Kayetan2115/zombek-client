# Import Modpacków Modrinth (.mrpack) — ZombekClient

Niniejszy dokument opisuje techniczny potok importu oficjalnych paczek modyfikacji **Modrinth Modpack** (`.mrpack`, `formatVersion: 1`) w launcherze ZombekClient.

---

## 1. Wstęp i Zasada Działania

Format `.mrpack` to ustandaryzowane archiwum ZIP stworzone przez platformę Modrinth, zaprojektowane w celu minimalizacji wagi plików dystrybucyjnych modpacków. Zamiast pakować do archiwum setki megabajtów plików `.jar`, `.mrpack` zawiera:
1. `modrinth.index.json` — manifest z listą modów, sumami kontrolnymi SHA-512/SHA-1 oraz oficjalnymi adresami URL do pobrania z Modrinth CDN,
2. `overrides/` — pliki konfiguracyjne, skrypty i opcje gry rozpakowywane bezpośrednio do katalogu głównego instancji,
3. `client-overrides/` — pliki specyficzne dla klienta gry, nadpisujące pliki bazowe z `overrides/`,
4. `server-overrides/` — pliki specyficzne dla serwera, które w launcherze klienckim są izolowane i nie zaśmiecają katalogu gry gracza.

---

## 2. Potok Przetwarzania (Import Pipeline)

```
[Plik .mrpack] (Wybór z dysku lub Drag & Drop)
      │
      ▼
1. Inspekcja Archiwum (Dry-Run / MrpackImporter.inspect)
   ├─ Weryfikacja nagłówków ZIP i obecności modrinth.index.json w root
   ├─ Walidacja schematu v1 (formatVersion: 1, game: "minecraft")
   ├─ Detekcja modloadera (Fabric, Forge, NeoForge, Quilt, Vanilla)
   ├─ Zliczenie plików, rozmiaru pobierania, overrides i plików opcjonalnych
   └─ Prezentacja w UI (MrpackImportModal)
      │
      ▼ (Użytkownik klika "Zainstaluj Modpack")
2. Przygotowanie Instancji i Katalogu Staging
   ├─ Utworzenie odizolowanej instalacji w InstallationRepository
   └─ Utworzenie bezpiecznego katalogu temp w temp-mrpack/
      │
      ▼
3. Filtrowanie i Równoległe Pobieranie Plików
   ├─ Odfiltrowanie plików z env.client === "unsupported"
   ├─ Uwzględnienie tylko wybranych przez gracza plików z env.client === "optional"
   ├─ Pobieranie strumieniowe z oficjalnych serwerów CDN (https://cdn.modrinth.com)
   └─ Obsługa przekierowań (max 3) i fallback URLs
      │
      ▼
4. Weryfikacja Integralności Haszy
   ├─ Sprawdzenie sumy SHA-512 (128 znaków hex)
   └─ Sprawdzenie sumy SHA-1 (40 znaków hex) i rozmiaru pliku
      │
      ▼
5. Aplikowanie Warstw Overrides
   ├─ Wypakowanie overrides/ do katalogu gry
   ├─ Nałożenie client-overrides/ z priorytetem
   └─ Zabezpieczenie server-overrides/ w .mrpack-preserved/
      │
      ▼
6. Utworzenie Modpack Lockfile i Finalizacja
   ├─ Wygenerowanie modpack.lock.json (wersje, hashe, modrinth IDs)
   ├─ Usunięcie plików staging
   └─ Oznaczenie instancji jako gotowej do gry
```

---

## 3. Komponenty Odpowiedzialne za Import

### 3.1. `MrpackArchiveReader`
Natywny moduł odczytujący strukturę ZIP bezpośrednio w pamięci RAM bez używania obcych narzędzi:
- Odnajduje sygnaturę **End of Central Directory (EOCD)** `0x06054b50`.
- Parsuje tablicę nagłówków Central Directory (`0x02014b50`).
- Wyciąga zawartość `modrinth.index.json` (limit wielkości 5 MB) i weryfikuje poprawność JSON.

### 3.2. `MrpackManifestValidator`
Weryfikuje zgodność z oficjalną specyfikacją Modrinth formatVersion 1:
- `formatVersion === 1`: Odrzucenie wersji nieobsługiwanych (np. `2`).
- `game === "minecraft"`: Odrzucenie paczek dla innych gier.
- Wymagane pola: `name`, `versionId`, `dependencies.minecraft`.
- Wykrywanie modloadera:
  - `fabric-loader` ➔ `fabric`
  - `forge` ➔ `forge`
  - `neoforge` ➔ `neoforge`
  - `quilt-loader` ➔ `quilt`
  - brak loadera ➔ `vanilla`
- Detekcja sprzecznych deklaracji modloaderów (np. deklarowanie Fabric i Forge w jednej paczce).
- Weryfikacja formatu hex i długości hashy (`sha1`: 40 znaków, `sha512`: 128 znaków).

### 3.3. `MrpackPathValidator` (Ochrona Zip Slip)
Każda ścieżka wpisu archiwum oraz ścieżka relatywna pliku w manifeście jest rygorystycznie walidowana:
- Blokada sekwencji ucieczki z katalogu (`..`).
- Blokada ścieżek bezwzględnych (`/`, `\`).
- Blokada liter dysków Windows (`C:`, `D:`) oraz zasobów sieciowych UNC (`\\server\share`).
- Blokada zastrzeżonych nazw urządzeń Windows (`CON`, `PRN`, `AUX`, `NUL`, `COM1-9`, `LPT1-9`).
- Blokada znaków zakazanych systemowo (`< > : " | ? *`) oraz bajtów zerowych (`\0`).

### 3.4. `MrpackOverrideManager`
Zarządza hierarchią plików konfiguracyjnych zawartych w archiwum:
1. `overrides/`: Wypakowywane jako baza instancji.
2. `client-overrides/`: Aplikowane na bazę; jeśli ten sam plik istnieje w obu folderach, wersja z `client-overrides/` nadpisuje plik bazowy.
3. `server-overrides/`: Pliki te nie są umieszczane w katalogu klienta, lecz zachowywane w ukrytym podfolderze `.mrpack-preserved/server-overrides/` w celu zachowania integralności paczki.

---

## 4. Obsługa Środowiska Gry (Client / Server) i Plików Opcjonalnych

Każdy plik w sekcji `files` manifestu `modrinth.index.json` może definiować flagi środowiska `env`:
- `env.client === "required"`: Plik jest bezwzględnie wymagany na kliencie i pobierany automatycznie.
- `env.client === "optional"`: Plik opcjonalny (np. mod poprawiający animacje lub opcjonalne audio). W interfejsie użytkownika pojawia się pole wyboru pozwalające graczowi zdecydować, czy chce zainstalować dany plik.
- `env.client === "unsupported"`: Plik przeznaczony wyłącznie dla serwera dedykowanego. Jest pomijany podczas pobierania na klienta gry.

---

## 5. Transakcyjność i Mechanizm Rollback

Import paczki w ZombekClient gwarantuje transakcyjność:
1. Wszystkie pliki modów są najpierw pobierane i weryfikowane kryptograficznie w odizolowanym folderze tymczasowym (`temp-mrpack/import-<timestamp>/`).
2. Dopiero po pomyślnym pobraniu i sprawdzeniu wszystkich hashy pliki są przenoszone do katalogu instancji.
3. W przypadku wystąpienia błędu sieciowego (np. utrata połączenia), błędu sumy kontrolnej lub próby pobrania z niedozwolonego hosta:
   - Zdarzenie zostaje zgłoszone z fazą `rollback`.
   - Nowo utworzona instancja w `InstallationRepository` zostaje automatycznie wyrejestrowana i usunięta z dysku.
   - Folder tymczasowy staging zostaje całkowicie wyczyszczony.
   - Użytkownik otrzymuje czytelny komunikat w języku polskim informujący o przyczynie niepowodzenia.
