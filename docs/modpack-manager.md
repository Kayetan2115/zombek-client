# Modpack Manager Core — Dokumentacja Techniczna (Etap 6A)

Niniejszy dokument opisuje architekturę, implementację i działanie modułu **Modpack Manager Core** w launcherze ZombekClient.

---

## 1. Wstęp i Cele Architektoniczne

Moduł Modpack Managera odpowiada za zarządzanie lokalnymi plikami modyfikacji Java Edition (`.jar`) oraz niezależnymi profilami modpacków dla wszystkich 5 obsługiwanych środowisk gry: **Vanilla**, **Fabric**, **Forge**, **NeoForge** i **Quilt**.

### Kluczowe cele etapu 6A:
1. **Bezpieczeństwo (Zero Java Execution):** Odczyt metadanych bezpośrednio z bajtów archiwum ZIP w pamięci bez uruchamiania maszyny wirtualnej Java (JVM) ani ładowania obcych klas do pamięci.
2. **Ochrona przed podatnościami:** Odporność na ataki typu **ZipSlip** oraz zabezpieczenia limitami rozmiaru przed atakami typu **Zip Bomb**.
3. **Niezależność środowisk:** Czysta separacja plików między instalacjami — mody zainstalowane w instancji A nigdy nie przenikają do instancji B.
4. **Strategia folderu `disabled-mods/`:** Wyłączanie modyfikacji poprzez fizyczne przesunięcie do podkatalogu `disabled-mods/` zamiast psucia rozszerzeń plików (`.jar.disabled`).
5. **Autonomia:** Zero zewnętrznych zależności npm dla parsowania formatów TOML (Forge `mods.toml` i NeoForge `neoforge.mods.toml`).

---

## 2. Architektura Komponentów Backendowych

Moduły backendu zlokalizowane są w katalogu `apps/launcher/electron/modpacks/`:

```
apps/launcher/electron/modpacks/
├── JarArchiveReader.ts         # Bezpieczny parser struktur ZIP w pamięci (EOCD, Central Directory)
├── SimpleTomlParser.ts         # Zero-dependency parser formatu TOML dla Forge/NeoForge
├── ModMetadataReader.ts        # Ekstraktor metadanych (Fabric, Quilt, NeoForge, Forge, Unknown)
├── ModCompatibilityChecker.ts  # Weryfikacja zgodności z loaderem, wersją gry i środowiskiem
├── ModDependencyAnalyzer.ts    # Analizator deklaracji zależności (depends) i konfliktów (breaks)
├── ModFileManager.ts           # Niskopoziomowy menedżer I/O, hashowanie SHA-256, bezpieczne kopiowanie
├── ModRepository.ts            # Repozytorium modów instancji z buforem dyskowym .mods-cache.json
├── ModInstallationService.ts   # Serwis walidacji kandydata i instalacji z detekcją duplikatów
├── ModpackRepository.ts        # Trwałość profili modpacków w game-data/modpacks.json
├── ModpackProfileManager.ts    # Głębokie klonowanie plików, konfiguracji i wdrożenia do instancji
└── ModpackValidator.ts         # Walidator integralności zestawu modów przed uruchomieniem gry
```

---

## 3. Szczegółowy Opis Modułów

### 3.1. `JarArchiveReader` — Bezpieczny Odczyt ZIP w Pamięci
- **Zasada działania:** Archiwum `.jar` jest standardowym plikiem ZIP. Zamiast rozpakowywać plik na dysk lub odpalać JVM, parser odczytuje nagłówek **End of Central Directory (EOCD)** z ostatnich 65 KB pliku, wyznacza przesunięcie Central Directory i parsuje nagłówki poszczególnych wpisów.
- **Dekompresja wpisów:**
  - Metoda kompresji `0` (Stored): bezpośredni wycinek (`buffer.subarray`).
  - Metoda kompresji `8` (Deflate): natywna dekompresja przez `node:zlib.inflateRawSync`.
- **Zabezpieczenia:**
  - Ochrona przed **ZipSlip**: odrzucanie ścieżek zawierających `..`, ścieżek bezwzględnych (`/` lub `\`) oraz separatorów dysków Windows (`:`).
  - Limit rozmiaru pliku JAR: 500 MB.
  - Limit liczby wpisów w archiwum: 15 000.
  - Limit pojedynczego odczytywanego wpisu tekstowego: 5 MB.

### 3.2. `SimpleTomlParser` — Autonomiczny Parser TOML
- Lekki, deterministyczny parser napisany od zera w TypeScript.
- Obsługuje:
  - Tablice obiektów `[[mods]]` oraz zagnieżdżone `[[dependencies.<modId>]]`.
  - Wieloliniowe ciągi znaków w potrójnych apostrofach (`'''...'''`) lub cudzysłowach (`"""..."""`).
  - Komentarze zaczynające się od znaku `#` (z poszanowaniem znaków wewnątrz cudzysłowów).
  - Typy podstawowe: boolean (`true`/`false`), liczby całkowite i zmiennoprzecinkowe, ciągi znaków oraz tablice inline `[...]`.

### 3.3. `ModMetadataReader` — Ekstrakcja Metadanych
Odczytuje i normalizuje metadane do jednolitej struktury `UniversalModMetadata`:
- **Fabric:** Parsuje `/fabric.mod.json` (odczytuje `id`, `name`, `version`, `description`, `authors`, `depends`, `breaks`, `environment`).
- **Quilt:** Parsuje `/quilt.mod.json` (odczytuje blok `quilt_loader`, `group`, `id`, `metadata`, `depends`, `breaks`). Jeśli archiwum zawiera zarówno `quilt.mod.json` jak i `fabric.mod.json`, loader oznaczany jest jako `quilt-or-fabric`.
- **NeoForge:** Parsuje `/META-INF/neoforge.mods.toml` (odczytuje `[[mods]]`, wersje, zależności z polem `type="required"`).
- **Forge:** Parsuje `/META-INF/mods.toml` (odczytuje `[[mods]]`, `displayName`, zależności z `mandatory=true`).
- **Fallback (Unknown):** W przypadku braku plików manifestu tworzy bezpieczny rekord z `loader: 'unknown'`, `rawSource: 'unknown'` i nazwą pliku.

### 3.4. `ModCompatibilityChecker` & `ModDependencyAnalyzer`
- **Weryfikacja loadera:**
  - Zgodność 1:1 dla Fabric na Fabric, Forge na Forge, NeoForge na NeoForge, Quilt na Quilt.
  - Wsteczna kompatybilność: Mod Fabric uruchamiany na Quilt otrzymuje status `Possibly Compatible` z informacją o obsłudze modów Fabric przez silnik QuiltMC.
  - Instancja Vanilla: Każdy mod otrzymuje status `Incompatible` (Vanilla nie wczytuje modów).
- **Weryfikacja wersji Minecrafta:**
  - Obsługa prefiksów `~`, `^`, `>=`, `<=`, `*` oraz zakresów przedziałowych `[1.20, 1.21)`.
- **Analiza zależności:**
  - Wykrywa brakujące zależności obowiązkowe (`mandatory: true`, `kind: 'depends'`).
  - Wykrywa konflikty z innymi zainstalowanymi modami (`kind: 'conflicts'`, `kind: 'breaks'`).

### 3.5. `ModFileManager` & `ModRepository`
- **Organizacja katalogów instancji:**
  - `instances/<id>/mods/` — pliki włączone (przekazywane do silnika gry).
  - `instances/<id>/disabled-mods/` — pliki wyłączone (ukryte przed silnikiem).
  - `instances/<id>/config/` — konfiguracja modów.
  - `instances/<id>/.mods-cache.json` — pamięć podręczna metadanych skanowanych plików.
- **Operacje:**
  - `toggleMod(instanceId, fileName, enabled)` — fizycznie przenosi plik między `mods/` a `disabled-mods/`.
  - `deleteMod(instanceId, fileName)` — trwale usuwa plik z nośnika.
  - `calculateSha256(filePath)` — strumieniowe obliczanie sumy SHA-256.

### 3.6. `ModInstallationService` — Walidacja i Instalacja
- Sprawdza istnienie pliku, rozszerzenie `.jar`, spójność archiwum ZIP.
- Wykrywa duplikaty w instalacji docelowej:
  - Po sumie kontrolnej SHA-256 (identyczna zawartość, nawet przy innej nazwie).
  - Po nazwie pliku.
- Zwraca szczegółowy obiekt `ModValidationResult` przed podjęciem decyzji o instalacji.

### 3.7. `ModpackRepository` & `ModpackProfileManager`
- Trwałość profili modpacków zapisywana w `%APPDATA%/ZombekClient/game-data/modpacks.json`.
- `duplicateModpack(id, newName)`:
  - Tworzy głęboką kopię (deep copy) plików modów i katalogu `config/`.
  - Zapewnia pełną niezależność kopii od oryginału.
- `applyModpackToInstance(modpackId, instanceId)`:
  - Weryfikuje zgodność modloadera paczki i instalacji docelowej.
  - Kopiuje pliki modów do katalogu `mods/` instalacji.
  - Zapisuje manifest `instances/<id>/modpack.json`.

---

## 4. Architektura IPC i Kontrakty Typów

Nowe kanały IPC w `@zombek/contracts`:
- `MODS_GET_FOR_INSTANCE` — pobiera listę przeskanowanych modów dla danej instalacji.
- `MODS_VALIDATE_FILE` — bada plik kandydata przed instalacją.
- `MODS_INSTALL_FILE` — instaluje mod w katalogu `mods/` instancji.
- `MODS_TOGGLE` — włącza/wyłącza mod (przenoszenie `mods/` <-> `disabled-mods/`).
- `MODS_DELETE` — trwale usuwa mod z dysku.
- `MODS_OPEN_FILE_DIALOG` — natywne okno wyboru plików OS (`dialog.showOpenDialog`).
- `MODS_OPEN_FOLDER` — otwarcie folderu `mods/` w eksploratorze plików OS (`shell.openPath`).
- `MODPACKS_GET_ALL` — odczyt wszystkich profili modpacków.
- `MODPACKS_CREATE` — utworzenie nowego profilu.
- `MODPACKS_UPDATE` — aktualizacja metadanych modpacka.
- `MODPACKS_DUPLICATE` — głębokie powielenie modpacka.
- `MODPACKS_DELETE` — usunięcie profilu modpacka.
- `MODPACKS_APPLY_TO_INSTANCE` — wdrożenie modpacka do instancji gry.

---

## 5. Interfejs Użytkownika (UI / UX)

### Ekran `ModsPage.tsx`:
1. **Płynny przełącznik widoków:**
   - **Pliki Modów (.jar):** Rzeczywisty menedżer plików modyfikacji.
   - **Nakładki HUD (Lunar Preview):** Podgląd konfiguracji nakładek HUD (Keystrokes, CPS, FPS).
2. **Kontekstowy nagłówek instancji:**
   - Wybór aktywnej instalacji gry.
   - Wskaźnik loadera (Fabric, Forge, NeoForge, Quilt, Vanilla).
   - Ostrzeżenie przy instalacji Vanilla (informacja, że Vanilla nie ładuje modów).
   - Przyciski "Dodaj Mod (.jar)", "Otwórz Folder Mods", "Odśwież".
3. **Filtry i wyszukiwarka:**
   - Filtrowanie: Wszystkie, Włączone, Wyłączone, Kompatybilne, Z uwagami.
   - Sortowanie: Nazwa (A-Z), Data dodania, Rozmiar pliku, Status kompatybilności.
4. **Karty modów:**
   - Wskaźnik statusu: `Kompatybilny` (zielony), `Prawdopodobnie działa` (żółty), `Brak zależności` (czerwony/pomarańczowy), `Konflikt` (czerwony), `Wyłączony` (szary).
   - Włącznik Toggle (natychmiastowe przeniesienie między `mods/` i `disabled-mods/`).
   - Przycisk "Szczegóły" z pełnym podglądem hashów SHA-256 i zadeklarowanych zależności.
   - Przycisk "Usuń" z modalem potwierdzenia trwałego usunięcia pliku z dysku.
5. **Modal dodawania modu:**
   - Wybór pliku z dysku lub wklejenie ścieżki.
   - Analiza na żywo (live inspection) z ostrzeżeniem o duplikatach i braku zależności.

### Ekran `ModpacksPage.tsx`:
1. **Siatka profili modpacków:**
   - Nazwa, opis, wersja MC, odznaka loadera, liczba modów, status kompatybilności.
2. **Akcje:**
   - "Zastosuj w grze" (wdrożenie do wybranej instalacji).
   - "Szczegóły" (spis plików w paczce).
   - "Kopia" (niezależny deep-copy).
   - "Usuń" (usunięcie profilu bez naruszania zapisów światów).
3. **Modal tworzenia modpacka:**
   - Formularz z nazwą, opisem, wersją Minecraft i wyborem modloadera.

---

## 6. Pokrycie Testowe

Moduł Modpack Managera posiada dedykowany zestaw **23 testów jednostkowych i integracyjnych** w `apps/launcher/tests/modpack.test.ts`.

---

## 7. Powiązane Dokumenty (Rozszerzenia Etapu 6C)

- [Rekurencyjne Rozwiązywanie Zależności](dependency-resolution.md)
- [System Wykrywania i Zarządzania Aktualizacjami Modów](mod-update-system.md)
- [Potok Transakcyjny i System Przywracania (Rollback)](transaction-and-rollback.md)
- [Import Modpacków Modrinth .mrpack](mrpack-import.md)
- [Eksport Modpacków Modrinth .mrpack](mrpack-export.md)
- [Architektura Bezpieczeństwa Formatu MRPACK](mrpack-security.md)
