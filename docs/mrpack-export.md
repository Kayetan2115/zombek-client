# Eksport Modpacków Modrinth (.mrpack) — ZombekClient

Niniejszy dokument opisuje architekturę i działanie modułu eksportu modpacków do oficjalnego formatu **Modrinth Modpack** (`.mrpack`, `formatVersion: 1`) w launcherze ZombekClient.

---

## 1. Wstęp i Cel

Eksport modpacka umożliwia graczom i twórcom przygotowanie gotowej paczki modyfikacji, którą można udostępnić innym użytkownikom ZombekClient lub opublikować na platformie Modrinth.

Główne zasady eksportu w ZombekClient:
1. **Zgodność ze specyfikacją Modrinth formatVersion 1:** Wygenerowane archiwum jest w 100% zgodne ze standardem i może być odczytywane przez dowolny launcher obsługujący `.mrpack`.
2. **Lekka waga archiwum:** Mody pochodzące z platformy Modrinth nie są pakowane binarnie do pliku `.mrpack`, lecz deklarowane w `modrinth.index.json` jako zdalne zasoby z oficjalnymi adresami URL i hashami SHA-512/SHA-1.
3. **Prywatność i bezpieczeństwo danych gracza:** Żadne prywatne dane (zapisy światów, zrzuty ekranu, logi czatu, tokeny logowania, konta graczy) nie mają prawa znaleźć się w wyeksportowanym archiwum.
4. **Odpowiedzialne zarządzanie prawami autorskimi:** Mody lokalne / autorskie (niepochodzące z Modrinth) mogą zostać spakowane do folderu `overrides/mods/` wyłącznie pod warunkiem świadomego potwierdzenia przez użytkownika posiadania praw do ich redystrybucji.

---

## 2. Potok Eksportu (Export Pipeline)

```
[Wybrana Instalacja Minecraft]
      │
      ▼
1. Odczyt Metadanych i Identyfikacja Loaderów
   ├─ Wersja Minecrafta (dependencies.minecraft)
   ├─ Modloader i wersja (fabric-loader, forge, neoforge, quilt-loader)
   └─ Odczyt zainstalowanych modów (ModRepository) i pliku modpack.lock.json
      │
      ▼
2. Klasyfikacja Modów na Zdalne vs Lokalne
   ├─ Mody z Modrinth:
   │   └─ Wygenerowanie wpisu w files[] z linkiem CDN, hashami SHA-512 i SHA-1
   └─ Mody lokalne / custom JAR:
       ├─ Jeśli użytkownik potwierdził prawa: dodanie do overrides/mods/
       └─ Jeśli nie: pominięcie z ostrzeżeniem w raporcie
      │
      ▼
3. Zbieranie Plików Konfiguracyjnych (Overrides)
   ├─ Skanowanie folderu config/ (jeśli zaznaczono)
   ├─ Dołączenie options.txt (jeśli zaznaczono)
   ├─ Dołączenie resourcepacks/ i shaderpacks/ (opcjonalnie)
   └─ Bezwzględne wykluczenie katalogów saves/, logs/, screenshots/, kont i tokenów
      │
      ▼
4. Zbudowanie Manifestu modrinth.index.json
   ├─ formatVersion: 1
   ├─ game: "minecraft"
   ├─ name, versionId, summary
   ├─ files: tablica zweryfikowanych modyfikacji
   └─ dependencies: wersje gry i modloadera
      │
      ▼
5. Generowanie Archiwum ZIP (MrpackArchiveWriter)
   ├─ Zapis wpisu głównego modrinth.index.json
   ├─ Zapis wpisów overrides/... z kompresją Deflate (node:zlib)
   ├─ Obliczenie sum CRC32 i budowa nagłówków Local + Central Directory + EOCD
   ├─ Zapis do pliku tymczasowego .tmp i weryfikacja poprawności
   └─ Atomowa zmiana nazwy na docelowy plik .mrpack
```

---

## 3. Szczegóły Komponentów Eksportu

### 3.1. `MrpackExporter`
Koordynuje cały proces przygotowania danych:
1. Pobiera definicję instancji za pomocą `InstallationRepository.getById(instanceId)`.
2. Buduje sekcję `dependencies`:
   - `minecraft`: wersja gry Minecraft.
   - Odpowiedni klucz loadera: `fabric-loader`, `forge`, `neoforge` lub `quilt-loader`. W przypadku Vanilla żaden dodatkowy klucz nie jest dodawany.
3. Pobiera listę włączonych modów z instancji za pomocą `ModRepository.getModsForInstance(instanceId)`.
4. Odczytuje `modpack.lock.json` w celu precyzyjnego powiązania modów z ich identyfikatorami Modrinth (`projectId`, `versionId`).

### 3.2. Identyfikacja Modów z Modrinth i Generowanie URL
W przypadku każdego pliku `.jar`:
- Odczytywana jest zawartość pliku i obliczane są sumy kryptograficzne SHA-512 oraz SHA-1.
- Jeśli mod jest zarejestrowany w pliku lockfile z `source === 'modrinth'` oraz posiada identyfikator `versionId`:
  - Plik zostaje zadeklarowany w sekcji `files` manifestu `modrinth.index.json`:
  ```json
  {
    "path": "mods/sodium-fabric-0.5.8.jar",
    "hashes": {
      "sha1": "713600fbe9...",
      "sha512": "b6a37f6d89..."
    },
    "env": {
      "client": "required",
      "server": "unsupported"
    },
    "downloads": [
      "https://cdn.modrinth.com/data/AANobbMI/versions/x123/sodium-fabric-0.5.8.jar"
    ],
    "fileSize": 1284560
  }
  ```

### 3.3. Obsługa Modów Autorskich (Custom JARs)
Pliki modów, które nie pochodzą z Modrinth (np. prywatne mody serwerowe lub pliki dodane ręcznie):
- Zgodnie ze specyfikacją Modrinth nie mogą mieć sfałszowanych linków CDN.
- Mogą zostać dołączone bezpośrednio do archiwum w podfolderze `overrides/mods/<fileName>`.
- **Wymóg prawny w ZombekClient:** Kreator eksportu (`MrpackExportModal`) wymaga od użytkownika zaznaczenia formalnego oświadczenia:
  > *"Oświadczam, że posiadam prawa do redystrybucji niestandardowych modów w paczce lub są one objęte wolną licencją."*
- W razie braku zgody pliki lokalne są pomijane, a użytkownik otrzymuje ostrzeżenie w podsumowaniu eksportu.

### 3.4. `MrpackArchiveWriter` — Natywny Generator ZIP
Eksport archiwum `.mrpack` odbywa się przy użyciu w pełni autonomicznego generatora ZIP napisanego w TypeScript:
- **Zero zewnętrznych zależności npm:** Wykorzystuje wyłącznie standardowe biblioteki środowiska Node.js (`node:fs`, `node:path`, `node:zlib`, `node:crypto`).
- **Kompresja Deflate:** Natywna kompresja strumieniowa przez `zlib.deflateRawSync`.
- **Precyzyjna tablica CRC32:** Deterministyczne wyliczanie 32-bitowych sum kontrolnych zgodnych ze standardem PKZIP.
- **Atomowy zapis:** Plik jest generowany do ścieżki `.tmp-[timestamp]`. Po pomyślnej weryfikacji integralności następuje atomowa operacja `fs.promises.rename` do ścieżki docelowej wybranej przez użytkownika.

---

## 4. Ochrona Prywatności Danych Gracza

Podczas skanowania katalogu instalacji w celu zebrania plików do folderu `overrides/`, moduł `MrpackOverrideManager` stosuje bezwzględną czarną listę katalogów i plików:

| Element wykluczony | Uzasadnienie bezpieczeństwa |
| :--- | :--- |
| `saves/` | Ochrona prywatnych światów dla pojedynczego gracza przed przypadkowym udostępnieniem. |
| `screenshots/` | Zrzuty ekranu mogą zawierać prywatne wiadomości lub wrażliwe koordynaty. |
| `logs/`, `crash-reports/` | Pliki dziennika (`latest.log`) zawierają nicki graczy, adresy IP serwerów oraz dane czatu. |
| `launcher_accounts.json` | Pliki autoryzacyjne kont graczy. |
| `usercache.json`, `usernamecache.json` | Pamięć podręczna kont i identyfikatorów UUID. |
| `.session` | Tokeny sesji gry i identyfikatory XUID. |
| `natives/`, `runtimes/` | Pliki binarne systemu operacyjnego (nie powinny być przenoszone między platformami). |
| `backups/`, `temp-transactions/` | Wewnętrzne migawki i pliki robocze launchera. |
| `modpack.lock.json` | Plik blokady wewnętrznej (odtworzy się automatycznie przy imporcie). |

---

## 5. Podsumowanie Wyniku Eksportu

Po zakończeniu operacji `MrpackExporter.export(...)` zwraca szczegółowy raport (`MrpackExportResult`):
- `success: boolean` — status powodzenia operacji,
- `outputPath: string` — docelowa ścieżka zapisanego pliku `.mrpack`,
- `filesCount: number` — liczba modów zadeklarowanych w sekcji `files`,
- `overridesCount: number` — liczba plików spakowanych do folderu `overrides/`,
- `customJarsCount: number` — liczba plików autorskich dołączonych do `overrides/mods/`,
- `warningMessages: string[]` — lista uwag i ostrzeżeń dla użytkownika.
