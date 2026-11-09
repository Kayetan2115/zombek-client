# Strategia i Wytyczne Testowania — ZombekClient

## 1. Poziomy Testów w Projekcie

W projekcie ZombekClient przyjęto trójwarstwową piramidę testów:

1. **Testy jednostkowe (Unit Tests):**
   - Weryfikacja czystych funkcji logicznych, walidatorów konfiguracji, konwerterów danych.
   - Narzędzie: **Vitest** dla TypeScript / Node.js; **JUnit 5** dla Javy / Gradle.
   - Szybkie, bezstanowe, uruchamiane przed każdym commitem.

2. **Testy kontraktów (Contract Tests):**
   - Sprawdzanie zgodności kanałów IPC w `@zombek/contracts`.
   - Zapewnienie, że nazwy kanałów są unikalne, posiadają prefiks `zombek:` oraz pasują do sygnatur metod w `ZombekDesktopApi`.

3. **Testy integracyjne i E2E (Planowane dla kolejnych etapów):**
   - Playwright dla procesu renderującego Electrona.
   - Testy uruchamiania instancji Minecrafta w sandboxie testowym.

## 2. Uruchamianie Testów

### Testy monorepo (TypeScript):
```bash
# Uruchomienie wszystkich testów jednostkowych i kontraktów
pnpm test

# Sprawdzenie poprawności typów w całym projekcie
pnpm typecheck

# Uruchomienie lintera i walidatora struktury plików
pnpm lint
pnpm verify:structure
```

### Testy modułu Minecraft Client (Java):
```bash
# Przejście do modułu Fabric
cd apps/minecraft-client/fabric

# Uruchomienie testów JUnit 5
./gradlew test
```

## 3. Zasady Pisania Testów

- Każdy nowy pakiet w `packages/` musi posiadać katalog `tests/` z co najmniej jednym plikiem testowym `.test.ts`.
- Żaden test jednostkowy nie powinien wymagać uruchomionego środowiska graficznego (headless).
- Wszelkie mocki mostka IPC muszą ściśle implementować interfejs `ZombekDesktopApi` zdefiniowany w `@zombek/contracts`.

## 4. Testy i Weryfikacja Interfejsu Graficznego (Etap 2)

W Etapie 2 przetestowano kompletny zestaw 9 widoków launchera:
1. **Nawigacja Sidebar:** Płynne przełączanie zakładek z animacjami i stanami hover.
2. **Ekran Home:** Renderowanie tła `WinterLandscape`, maskotki Ząbek, sekcji PLAY, newsów i kafelków profili.
3. **Ekran Play:** Dynamiczny podgląd przydziału pamięci RAM i generowanych argumentów JVM.
4. **Ekran Versions:** Wyszukiwanie, filtrowanie po loaderach (Fabric/Forge/NeoForge), modal dodawania instalacji.
5. **Ekran Modpacks:** Karty paczek, modal tworzenia, symulacja importu (.mrpack/.zip) i eksportu.
6. **Ekran Mods:** Filtrowanie 5 kategorii, wyszukiwarka, przełączniki toggle, edytor HUD z podglądem na żywo i próbkami kolorów.
7. **Ekran Cosmetics:** Przełącznik rzutu postaci (przód / tył z peleryną Ząbek), zmiana wyposażenia i modal personalizacji.
8. **Ekran Accounts:** Karty kont, przełączanie aktywnego gracza, modal logowania Microsoft (Device Code DEMO) i profil lokalny Offline.
9. **Ekran Settings:** Formularze konfiguracji w 6 kategoriach (Language, RAM, JVM, Java runtime, Snowfall on/off).
10. **Ekran Logs:** Filtrowanie poziomów (INFO, WARN, ERR, DEBUG), szukajka tekstowa, kopiowanie do schowka i czyszczenie listy.
11. **Powiadomienia Toast:** Działanie powiadomień po akcjach w interfejsie.
12. **Cząsteczki śniegu (Snowfall):** Wydajny Canvas z możliwością płynnego włączenia/wyłączenia w Topbarze i Ustawieniach.

## 5. Testy Silnika Gry Minecraft (Etap 3)

W Etapie 3 wdrożono zestaw testów jednostkowych w `apps/launcher/tests/engine.test.ts`:
1. **GameSessionProvider:**
   - Sprawdzenie deterministycznego generowania UUID v3 (MD5) według standardu `java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + nick))`.
   - Weryfikacja formatu RFC 4122 (wersja 3, wariant IETF).
   - Weryfikacja obiektu sesji offline (token `'0'`, typ sesji `'offline'`).
2. **EnginePaths & Ochrona Bezpieczeństwa:**
   - Weryfikacja katalogu bazowego (`%APPDATA%/ZombekClient`).
   - Testy ochrony przed atakiem Path Traversal (`assertSafeFilename` rzuca błąd dla `../`, `..\`, `C:\windows`).
   - Weryfikacja funkcji `isSafePath` potwierdzającej zagnieżdżenie ścieżek wewnątrz docelowego katalogu.
3. **GameInstaller & Reguły OS:**
   - Testy parsowania koordynatów Maven na relatywne ścieżki plików jar (`group/artifact/version/artifact-version.jar`).
   - Weryfikacja klasyfikatorów bibliotek natywnych (`natives-windows`).
   - Ewaluacja reguł `MojangRule` sprawdzająca dopasowanie systemu operacyjnego (`windows`, `osx`, `linux`).
4. **GameArgumentsBuilder:**
   - Testy podmiany zmiennych `${auth_player_name}`, `${version_name}`, `${game_directory}`, itp.
   - Weryfikacja kolejności argumentów nowoczesnych (`arguments.jvm` -> `mainClass` -> `arguments.game`).
   - Weryfikacja fallbacku dla wersji legacy z pojedynczym ciągiem `minecraftArguments`.
   - Testy dynamicznego wyznaczania flag pamięci (`-Xms`, `-Xmx`).
5. **JavaRuntimeManager:**
   - Mapowanie komponentów Mojang (`java-runtime-delta`, `java-runtime-gamma`, `jre-legacy`) na wersje Javy (21, 17, 8).
   - Testy bezpiecznej detekcji środowiska `java -version`.

## 6. Testy Systemu Kont i Autoryzacji (Etap 4)

W Etapie 4 wdrożono zestaw 14 testów jednostkowych w `apps/launcher/tests/auth.test.ts`:
1. **Walidacja nicków i UUID OfflinePlayer:**
   - Weryfikacja ograniczeń nicków (3-16 znaków, znaki `[a-zA-Z0-9_]`).
   - Odrzucanie pustych ciągów, spacji i znaków specjalnych.
   - Deterministyczność UUID v3 oraz formatowanie 32-znakowych hex UUID do formatu z myślnikami (8-4-4-4-12).
2. **Zarządzanie wieloma kontami (AccountRepository):**
   - Dodawanie wielu profili offline i kont Microsoft.
   - Przełączanie aktywnego konta (`setActive`).
   - Usuwanie kont z czyszczeniem powiązanych tokenów bez naruszania katalogu gry.
3. **Bezpieczny Magazyn Tokenów (TokenStorage):**
   - Szyfrowanie, zapis, odczyt i odszyfrowywanie tokenów `SecureAccountTokens`.
   - Weryfikacja braku obecności jawnych tokenów w `accounts.json`.
4. **Logika Microsoft OAuth2 PKCE i Services:**
   - Poprawność generowania pary `code_verifier` i `code_challenge` (S256).
   - Obsługa braku `MICROSOFT_CLIENT_ID` z czytelnym komunikatem o konfiguracji `.env`.
   - Weryfikacja parsowania uprawnień sklepowych (`product_minecraft` / `game_minecraft`).
   - Serializacja profilu gracza z pobieraniem skórek i generowaniem miniatury.
5. **Integracja z Launcherem i Bezpieczeństwo Logów:**
   - Rozwiązywanie sesji przez `CompositeSessionProvider`.
   - Redagowanie tokenów dostępowych w logach (`--accessToken [REDACTED]`).
   - Oznaczenie testu produkcyjnego jako `BLOCKED — WAITING FOR CLIENT ID / APPROVAL` w przypadku braku zewnętrznego dopuszczenia aplikacji w Minecraft Services.

## 7. Testy ModLoadera Fabric (Etap 5A)

W Etapie 5A wdrożono zestaw 8 testów jednostkowych w `apps/launcher/tests/fabric.test.ts`:
1. **FabricMetaService:**
   - Pobieranie wydań gier z oficjalnego punktu końcowego `https://meta.fabricmc.net/v2/versions/game` oraz zapis do cache.
   - Pobieranie kompatybilnych wersji Fabric Loader dla konkretnej wersji Minecrafta z uwzględnieniem flagi `stable`.
   - Weryfikacja działania trybu offline: awaria sieci powoduje bezbłędne odczytanie danych z bufora dyskowego.
   - Pobieranie i zapisywanie profilu instalacyjnego JSON w `versions/<profile_id>/<profile_id>.json`.
2. **FabricLoaderAdapter & Dziedziczenie Profili:**
   - Poprawne łączenie bazowego profilu Vanilla (`inheritsFrom`) z bibliotekami i argumentami Fabrica.
   - Ustawienie `mainClass` na `net.fabricmc.loader.impl.launch.knot.KnotClient`.
   - Deduplikacja bibliotek Maven na Classpath.
   - Weryfikacja izolacji instancji — upewnienie się, że foldery `mods/`, `config/`, `saves/`, `resourcepacks/`, `screenshots/` oraz `natives/` są tworzone dla każdej instalacji.
3. **GameArgumentsBuilder & Gotowość do uruchomienia:**
   - Budowanie Classpath zawierającego zarówno Vanilla Client JAR jak i biblioteki Maven Fabrica.
   - Przekazywanie argumentu startowego Fabric (`-DFabricMcEmu= net.minecraft.client.main.Main `).
   - Przekazywanie katalogu instancji jako `--gameDir`.
   - Poprawne mapowanie ścieżek bibliotek Maven dla repozytorium Fabrica (`https://maven.fabricmc.net/`).

## 8. Testy ModLoadera Forge (Etap 5B)

W Etapie 5B wdrożono zestaw 10 testów jednostkowych w `apps/launcher/tests/forge.test.ts`:
1. **ForgeMetaService — Pobieranie i oznaczanie wersji:**
   - Pobieranie oficjalnych promocji (`promotions_slim.json`) i mapowanie wersji `recommended` oraz `latest`.
   - Dynamiczne pobieranie i parsowanie Maven Metadata XML z sortowaniem wersji semver.
   - Prawidłowe oznaczanie flag `isRecommended` i `isLatest` w wynikowych obiektach `ForgeVersionInfo`.
   - Zapis do bufora dyskowego `%APPDATA%/ZombekClient/game-data/forge_meta_cache.json`.
   - Bezbłędna odporność na brak połączenia z siecią (pobieranie z cache dyskowego przy symulowanym błędzie sieciowym).
2. **ForgeProcessorManager — Ewaluacja procesorów i podstawianie zmiennych:**
   - Poprawne mapowanie koordynatów Maven w formacie `group:artifact:version:classifier@ext` na ścieżki plików w `libraries/`.
   - Podstawianie zmiennych standardowych (`{MINECRAFT_JAR}`, `{ROOT}`, `{INSTALLER}`, `{LIBRARY_DIR}`).
   - Rozwiązywanie tokenów danych słownikowych `{KEY}` oraz argumentów zagnieżdżonych w nawiasach Maven `[coord]`.
   - Poprawne traktowanie dosłownych literałów w apostrofach `'value'` oraz ścieżek wewnątrz archiwum `/data/...`.
   - Filtracja procesorów po stronie instalacji (`sides: ['client']`) — odrzucanie procesorów wyłącznie serwerowych (`sides: ['server']`).
3. **ForgeLoaderAdapter & GameArgumentsBuilder — Integracja i uruchamianie:**
   - Poprawne łączenie bazowego profilu Vanilla (`inheritsFrom`) z profilem Forge.
   - Prawidłowe ustawienie klasy głównej (`net.minecraftforge.bootstrap.ForgeBootstrap`).
   - Budowanie Classpath zawierającego zarówno Vanilla Client JAR, biblioteki Forge jak i patchowane biblioteki klienta.
   - Zachowanie izolacji instancji gry (`instances/<id>/mods/`, `config/`, `saves/`, `resourcepacks/`, `natives/`, `logs/`).

## 9. Testy ModLoadera NeoForge (Etap 5C)

W Etapie 5C wdrożono zestaw 10 testów jednostkowych w `apps/launcher/tests/neoforge.test.ts`:
1. **NeoForgeMetaService — Rozpoznawanie generacji i prefiksów:**
   - Poprawne mapowanie wersji Transition dla Minecraft 1.20.1 (`net.neoforged:forge`, prefiks `1.20.1-47.1.`).
   - Poprawne mapowanie wersji Modern dla Minecraft 1.20.4 (`net.neoforged:neoforge`, prefiks `20.4.`) oraz 1.21.1 (`21.1.`).
   - Oznaczanie starszych wydań (1.19.4, 1.16.5, 1.12.2) jako nieobsługiwane (`null`).
2. **NeoForgeMetaService — Pobieranie i oznaczanie wersji Modern:**
   - Parsowanie oficjalnego `maven-metadata.xml` dla artefaktu `net.neoforged:neoforge`.
   - Sortowanie wersji malejąco z wyznaczeniem `isLatest` oraz stabilnego `isRecommended` (najwyższy build bez etykiet beta/alpha/pre/snapshot).
   - Zapis metadanych do dyskowego bufora `%APPDATA%/ZombekClient/game-data/neoforge_meta_cache.json`.
3. **NeoForgeMetaService — Pobieranie wydań Transition:**
   - Parsowanie metadanych dla `net.neoforged:forge` (1.20.1) i konstrukcja dedykowanych URL instalatorów.
4. **NeoForgeMetaService — Obsługa trybu offline:**
   - Weryfikacja działania w warunkach braku połączenia internetowego poprzez awaryjny odczyt z bufora dyskowego.
5. **NeoForgeMetaService — Obsługa wersji niekompatybilnych:**
   - Zwracanie pustej tablicy bez zbędnych żądań sieciowych dla wydań sprzed powstania NeoForge (np. 1.16.5).
6. **NeoForgeProcessorManager — Rozwiązywanie ścieżek Maven:**
   - Rozwijanie koordynatów Maven z klasyfikatorami i rozszerzeniami (`@jar`, `@zip`, `@tsrg`) do ścieżek w `libraries/`.
7. **NeoForgeProcessorManager — Podstawianie argumentów procesorów:**
   - Prawidłowe rozwiązywanie tokenów `{INSTALLER}`, `{MINECRAFT_JAR}`, `{ROOT}`, `{LIBRARY_DIR}`, danych słownikowych `{MC_SLIM}`, literałów w apostrofach i plików wewnętrznych archiwum `/data/...`.
8. **NeoForgeProcessorManager — Filtracja procesorów po stronie klienta:**
   - Zachowywanie procesorów klienta i uniwersalnych przy jednoczesnym odrzucaniu procesorów czysto serwerowych (`sides: ['server']`).
9. **NeoForgeLoaderAdapter & GameArgumentsBuilder — Scalanie profili i linia poleceń:**
   - Dziedziczenie profilu (`inheritsFrom: '1.20.4'`), klasa główna `cpw.mods.bootstraplauncher.BootstrapLauncher`.
   - Budowanie Classpath z bazowym plikiem JAR Vanilla i bibliotekami NeoForge.
   - Weryfikacja argumentów uruchomieniowych gry i JVM.
10. **Izolacja instancji:**
    - Test niezależności katalogów `instances/<id>/mods/`, `config/`, `saves/`, `resourcepacks/`, `natives/`, `logs/`.
    - Potwierdzenie braku wycieku modów między instancjami NeoForge i Vanilla/Fabric/Forge.

## 10. Testy ModLoadera Quilt (Etap 5D)

W Etapie 5D wdrożono zestaw 10 testów jednostkowych w `apps/launcher/tests/quilt.test.ts`:
1. **QuiltMetaService — Nagłówek User-Agent i buforowanie dyskowe:**
   - Weryfikacja wysyłania wymaganego przez QuiltMC nagłówka `User-Agent: ZombekClient/0.1.0 (desktop launcher)`.
   - Zapis pobranych wersji gier do bufora `%APPDATA%/ZombekClient/game-data/quilt_meta_cache.json`.
2. **QuiltMetaService — Odczyt z pamięci podręcznej (RAM Cache):**
   - Sprawdzenie, że ponowne wywołania nie generują zbędnych żądań HTTP.
3. **QuiltMetaService — Pobieranie wersji Quilt Loader:**
   - Pobieranie wydań loadera z `/v3/versions/loader/{gameVersion}` i mapowanie struktury metadanych.
4. **QuiltMetaService — Pobieranie profilu launchera JSON:**
   - Pobieranie oficjalnego profilu z `/v3/versions/loader/{gameVersion}/{loaderVersion}/profile/json` i zapis do katalogu wersji gry.
5. **QuiltLoaderAdapter — Weryfikacja wspieranych wersji:**
   - Potwierdzenie wsparcia dla wydań Minecrafta obecnych w oficjalnym wykazie Quilt.
6. **QuiltLoaderAdapter — Fuzja profilu Vanilla i Quilt:**
   - Ustawienie klasy głównej `org.quiltmc.loader.impl.launch.knot.KnotClient`, scalenie argumentów JVM i gry, deduplikacja bibliotek.
7. **QuiltLoaderAdapter — Weryfikacja polityki niestosowania QSL (Dec 2025):**
   - Ścisły test asercji sprawdzający, że biblioteki QSL (`org.quiltmc.qsl`) nie są automatycznie wstrzykiwane jako zależność.
8. **QuiltLoaderAdapter — Instalacja i izolacja katalogów:**
   - Tworzenie podkatalogów `instances/<id>/saves/`, `resourcepacks/`, `config/`, `mods/`, `screenshots/`, `natives/`, `logs/` oraz aktualizacja statusu na `ready`.
9. **GameArgumentsBuilder — Uruchamianie Quilt:**
   - Weryfikacja generowanej linii poleceń JVM: klasa główna KnotClient, parametry pamięci RAM, Classpath łączący Vanilla JAR i biblioteki Quilt oraz izolowany `--gameDir`.
10. **ModCompatibilityTarget — Gotowość pod Menedżer Modpacków:**
    - Walidacja kompletności typów klasyfikacji zgodności modów (`quilt`, `fabric`, `fabric-and-quilt`, `quilt-incompatible`, `forge`, `neoforge`).

## 11. Testy Modpack Managera Core (Etap 6A)

W Etapie 6A wdrożono zestaw 23 testów jednostkowych i integracyjnych w `apps/launcher/tests/modpack.test.ts`:
1. **JarArchiveReader — Odczyt wpisów ZIP w pamięci:**
   - Weryfikacja pobierania spisu wpisów z Central Directory oraz dekompresji tekstu UTF-8 z archiwum bez użycia Java runtime.
2. **JarArchiveReader — Ochrona przed atakiem ZipSlip:**
   - Odrzucanie z błędem prób wyjścia poza katalog docelowy przez sekwencje `../../` w nazwach wpisów.
3. **JarArchiveReader — Odporność na uszkodzone archiwa:**
   - Poprawne wykrywanie braku sygnatury End of Central Directory (EOCD) i bezpieczne zgłaszanie błędu.
4. **SimpleTomlParser — Parsowanie Forge mods.toml:**
   - Obsługa struktur `[[mods]]`, zagnieżdżonych zależności `[[dependencies.<id>]]`, wieloliniowych opisów `'''` oraz komentarzy `#`.
5. **SimpleTomlParser — Parsowanie NeoForge neoforge.mods.toml:**
   - Obsługa tablic autorów `authors=["jellysquid", "ims21"]`, typów `type="required"` i zakresów wersji.
6. **ModMetadataReader — Ekstrakcja metadanych Fabric:**
   - Odczyt `fabric.mod.json`, mapowanie pól `id`, `name`, `version`, `depends`, `breaks`.
7. **ModMetadataReader — Ekstrakcja metadanych Quilt:**
   - Odczyt `quilt.mod.json`, sekcji `quilt_loader`, deklaracji bibliotek i reguł kolizji.
8. **ModMetadataReader — Ekstrakcja metadanych NeoForge:**
   - Odczyt `META-INF/neoforge.mods.toml` i normalizacja do `UniversalModMetadata`.
9. **ModMetadataReader — Ekstrakcja metadanych Forge:**
   - Odczyt `META-INF/mods.toml` i normalizacja do `UniversalModMetadata`.
10. **ModMetadataReader — Bezpieczny fallback dla nieznanych JAR:**
    - Generowanie bezpiecznych metadanych domyślnych (`loader: 'unknown'`, `rawSource: 'unknown'`) dla plików bez plików manifestu.
11. **ModCompatibilityChecker — Zgodność pełna (Compatible):**
    - Potwierdzenie statusu przy zgodnym modloaderze i wersji gry Minecraft.
12. **ModCompatibilityChecker — Wsteczna kompatybilność Quilt -> Fabric (Possibly Compatible):**
    - Potwierdzenie, że Quilt pozwala na uruchamianie modów Fabric z informacją o kompatybilności wstecznej.
13. **ModCompatibilityChecker — Wykrywanie niezgodności loadera (Incompatible):**
    - Oznaczanie próby uruchomienia moda Fabric na Forge jako niekompatybilnej.
14. **ModDependencyAnalyzer — Wykrywanie brakujących zależności:**
    - Wykrywanie niespełnionych zależności wymaganych (`mandatory: true`).
15. **ModDependencyAnalyzer — Zaspokojenie zależności:**
    - Potwierdzenie braku błędów, gdy wymagany mod (np. Fabric API) jest obecny i włączony.
16. **ModDependencyAnalyzer — Wykrywanie kolizji i konfliktów:**
    - Wykrywanie deklaracji `breaks` / `conflicts` (np. OptiFabric vs Sodium).
17. **ModFileManager — Obliczanie sumy SHA-256:**
    - Generowanie 64-znakowego hashu SHA-256 dla pliku JAR.
18. **ModFileManager & ModRepository — Włączanie i wyłączanie modów:**
    - Przenoszenie plików między katalogami `mods/` a `disabled-mods/` bez zmiany rozszerzenia `.jar`.
19. **ModFileManager & ModRepository — Trwałe usuwanie z dysku:**
    - Usunięcie pliku moda z fizycznego nośnika i zaktualizowanie repozytorium.
20. **ModInstallationService — Walidacja kandydata i detekcja duplikatów:**
    - Pre-walidacja pliku JAR przed kopiowaniem, wykrywanie duplikatów po hashu SHA-256 i po nazwie pliku, obsługa flagi `overwrite`.
21. **ModpackRepository — Zarządzanie profilami modpacków:**
    - Tworzenie profilu modpacka, odczyt z dysku, aktualizacja i bezpieczne usuwanie.
22. **ModpackProfileManager — Głębokie klonowanie i aplikowanie do instancji:**
    - Zastosowanie paczki w wybranej instalacji Minecraft, wygenerowanie pliku manifestu `modpack.json`.
23. **Izolacja Instancji (Instance Isolation):**
    - Potwierdzenie, że mody zainstalowane w instancji A nie pojawiają się w instancji B.

## 12. Testy Integracji Modrinth (Etap 6B)

W Etapie 6B wdrożono zestaw 13 testów jednostkowych i integracyjnych w `apps/launcher/tests/modrinth.test.ts`:
1. **ModrinthApiClient — Formatowanie User-Agent:**
   - Weryfikacja zgodności nagłówka `User-Agent: ZombekClient/0.1.0 (https://github.com/zombekclient/zombekclient)` z wytycznymi Modrinth API.
2. **ModrinthApiClient — Budowanie zapytań Facets:**
   - Poprawne generowanie zagnieżdżonych tablic filtrów JSON dla loaderów (`categories:fabric`), wersji Minecraft (`versions:1.20.4`), kategorii i środowiska klienta (`client_side:required`).
3. **ModrinthApiClient — Śledzenie limitu zapytań (Rate Limiting):**
   - Śledzenie nagłówków `x-ratelimit-*`, aktywacja stanu cooldown przy wyczerpaniu zapytań i rzucanie `ModrinthRateLimitError`.
4. **ModrinthApiClient — Klasa błędu ModrinthRateLimitError:**
   - Weryfikacja struktury błędu, nazwy i czasu oczekiwania `resetSeconds`.
5. **ModrinthDownloader — Zezwalanie na oficjalne domeny CDN:**
   - Potwierdzenie akceptacji adresów URL z domen `cdn.modrinth.com`, `edge.modrinth.com` oraz `*.modrinth.com`.
6. **ModrinthDownloader — Odrzucanie niebezpiecznych protokołów i domen:**
   - Wymuszenie protokołu HTTPS (odrzucanie HTTP), blokada obcych domen, pętli zwrotnej (`localhost`, `127.0.0.1`) oraz protokołów lokalnych (`file://`).
7. **ModrinthDownloader — Weryfikacja kryptograficzna SHA-512 & SHA-1:**
   - Weryfikacja poprawności pliku z hashem SHA-512 oraz wykrywanie i odrzucanie plików uszkodzonych lub podmienionych.
8. **ModrinthInstallationManager — Wybór pliku głównego (Primary JAR):**
   - Poprawna selekcja pliku z flagą `primary: true` lub fallbacku do pliku z rozszerzeniem `.jar`.
9. **ModrinthInstallationManager — Ocena zgodności loaderów:**
   - Weryfikacja Fabric na Fabric, Forge na Forge, NeoForge na NeoForge, odrzucanie Forge na Fabric oraz obsługa wstecznej zgodności Quilt -> Fabric.
10. **ModrinthInstallationManager — Detekcja brakujących zależności:**
    - Analiza zależności `required` w metadanych wydania Modrinth i generowanie listy brakujących modów w instancji.
11. **Modrinth Live API — Wyszukiwanie na żywo:**
    - Rzeczywiste zapytanie do `https://api.modrinth.com/v2/search?query=sodium` z weryfikacją struktury odpowiedzi JSON, trafień i liczby pobrań.
12. **Modrinth Live API — Pobieranie metadanych projektu i wydań:**
    - Rzeczywiste zapytanie o projekt `sodium` oraz listę wydań dla `fabric` 1.20.4 z weryfikacją obecności hashy SHA-512.
13. **Modrinth Live API — Pobieranie tagów kategorii i loaderów:**
    - Odpytanie oficjalnych endpointów `/tag/category` oraz `/tag/loader` z weryfikacją obecności kluczowych kategorii i loaderów.

---

## 13. Testy Zależności, Aktualizacji i Transakcji Modpacka (Etap 6C)

W Etapie 6C wdrożono zestaw 28 testów jednostkowych i integracyjnych w `apps/launcher/tests/updates.test.ts`:
1. **ModConflictResolver — Niezgodność modloadera:**
   - Wykrycie próby użycia modu Forge na instancji Fabric (`conflictType: 'loader-mismatch'`).
2. **ModConflictResolver — Niezgodność wersji Minecraft:**
   - Wykrycie modu wspierającego 1.19.2 na instalacji Minecraft 1.20.1 (`conflictType: 'mc-version-mismatch'`).
3. **ModConflictResolver — Zadeklarowana niekompatybilność:**
   - Wykrycie jawnego konfliktu deklarowanego przez mod (np. Iris Shaders vs OptiFine, `conflictType: 'incompatible'`).
4. **ModConflictResolver — Kolizja z wersją przypiętą:**
   - Wykrycie sytuacji, w której zależność wymaga innej wersji modu niż zablokowana przez flagę `pinned: true`.
5. **ModDependencyResolver — Bezpośrednie zależności wymagane:**
   - Poprawne dołączenie wymaganej zależności (np. Iris -> Sodium) do planu `toInstall`.
6. **ModDependencyResolver — Rekurencyjne zależności wielopoziomowe:**
   - Rozwiązanie pełnego łańcucha A -> B -> C i umieszczenie obu bibliotek B i C w planie instalacji.
7. **ModDependencyResolver — Detekcja cykli w grafie:**
   - Ochrona przed nieskończoną rekurencją w przypadku zależności cyklicznych (A -> B -> A) przy użyciu `visitedProjects`.
8. **ModDependencyResolver — Obsługa bibliotek zagnieżdżonych (Jar-in-Jar):**
   - Poprawne oznaczenie zależności `embedded` w `alreadySatisfied` z `isEmbedded: true` bez pobierania dodatkowego pliku.
9. **ModpackLockfileManager — Inicjalizacja i odczyt schema v1:**
   - Utworzenie i weryfikacja struktury pliku `modpack.lock.json` w katalogu instancji.
10. **ModpackLockfileManager — Przypinanie wersji i zmiana polityki:**
    - Zmiana flagi `pinned: true` oraz aktualizacja `updatePolicy` (`stable-only` -> `stable-and-beta`) z trwałym zapisem na dysku.
11. **ModpackLockfileManager — Synchronizacja z plikami na dysku:**
    - Poprawna fuzja stanu fizycznych plików JAR z folderu `mods/` z metadanymi i politykami lockfile.
12. **ModpackBackupManager — Tworzenie migawki (Snapshot):**
    - Skopiowanie plików JAR oraz pliku lockfile do folderu backupu, wygenerowanie `manifest.json`.
13. **ModpackBackupManager — Polityka retencji (max 10 kopii):**
    - Weryfikacja automatycznego usuwania najstarszych kopii po przekroczeniu limitu 10 snapshotów.
14. **ModpackBackupManager — 1-kliknięcie Rollback:**
    - Przywrócenie plików JAR z kopii zapasowej, odtworzenie `modpack.lock.json` oraz usunięcie obcych plików dodanych w późniejszym stanie.
15. **ModUpdateManager — Ochrona modów przypiętych:**
    - Wykrycie flagi `isPinned: true` i przypisanie statusu `status: 'pinned'`, z pominięciem automatycznej aktualizacji.
16. **ModUpdateManager — Rozpoznawanie plików lokalnych i customowych:**
    - Oznaczenie plików nieznalezionych w bazie Modrinth statusem `status: 'manual-action'`.
17. **ModUpdateManager — Wykrywanie nowszych wydań:**
    - Poprawne dopasowanie hashy SHA-512 z odpowiedzi Modrinth i oznaczenie modyfikacji statusem `status: 'available'`.
18. **ModpackTransactionManager — Blokada transakcji podczas gry:**
    - Natychmiastowe zablokowanie modyfikacji i zgłoszenie błędu, gdy instancja posiada status `state: 'running'`.
19. **ModpackTransactionManager — Atomowa transakcja aktualizacji:**
    - Pomyślne pobranie pliku do stagingu, weryfikacja integralności, utworzenie kopii bezpieczeństwa, podmiana w `mods/` i uaktualnienie `modpack.lock.json`.
20. **ModpackTransactionManager — Auto-Rollback w przypadku awarii sieci:**
    - Wycofanie zmian i zachowanie nienaruszonego stanu plików w katalogu `mods/` przy błędzie w trakcie transakcji.
21-28. **ModConflictResolver — Matryca kompatybilności loaderów:**
    - Weryfikacja 8 kombinacji loaderów: Fabric/Fabric, Forge/Forge, NeoForge/NeoForge, Quilt/Quilt, Quilt/Fabric (kompatybilność wsteczna), Fabric/Forge (niezgodność), Forge/Fabric (niezgodność), NeoForge/Fabric (niezgodność).

---

## 14. Testy Formatu Modrinth Modpack .mrpack (Etap 6D)

Zestaw testowy `apps/launcher/tests/mrpack.test.ts` obejmuje 29 testów jednostkowych i integracyjnych pokrywających wszystkie wymagania specyfikacji Modrinth Modpack formatVersion 1:

1. **MrpackPathValidator — Normalne relatywne ścieżki:**
   - Poprawna akceptacja i normalizacja standardowych ścieżek `mods/sodium.jar`, `config/test.json`, `options.txt`.
2. **MrpackPathValidator — Ochrona przed Zip Slip:**
   - Wykrywanie i odrzucanie sekwencji wyjścia z katalogu `..`, `mods/../../etc/passwd`, `..\windows\system32`.
3. **MrpackPathValidator — Ścieżki absolutne, litery dysków i UNC:**
   - Odrzucanie ścieżek z wiodącym ukośnikiem `/etc/passwd`, liter Windows `C:/Windows`, `D:\games` oraz zasobów sieciowych `\\server\share`.
4. **MrpackPathValidator — Zastrzeżone nazwy urządzeń Windows:**
   - Odrzucanie nazw systemowych DOS/Windows: `CON`, `PRN.txt`, `mods/AUX`, `config/NUL.json`, `COM1`, `LPT2`.
5. **MrpackPathValidator — Null bytes i znaki zabronione:**
   - Blokowanie znaków null `\0` oraz symboli `< > : " | ? *`.
6. **MrpackPathValidator — Granice katalogu (assertSafeSubpath):**
   - Wymuszenie zatrzymania ścieżki docelowej w obrębie wskazanego folderu bazowego i rzucanie wyjątku przy próbie ucieczki.
7. **MrpackManifestValidator — Poprawny manifest formatVersion 1:**
   - Akceptacja manifestu z `formatVersion: 1`, `game: 'minecraft'`, poprawnymi zależnościami i tablicą `files`.
8. **MrpackManifestValidator — Odrzucenie nieobsługiwanego formatVersion:**
   - Wykrycie i odrzucenie manifestów z `formatVersion: 2` lub innymi nieznanymi wersjami.
9. **MrpackManifestValidator — Odrzucenie błędnego identyfikatora gry:**
   - Blokada modpacków dla gier innych niż Minecraft (np. `game: 'hytale'`).
10. **MrpackManifestValidator — Weryfikacja brakujących pól obowiązkowych:**
    - Odrzucanie manifestów bez nazwy, bez `versionId` lub bez wersji Minecrafta.
11. **MrpackManifestValidator — Wykrywanie sprzecznych deklaracji loaderów:**
    - Odrzucenie paczki deklarującej jednocześnie dwa sprzeczne loadery (np. Fabric + Forge).
12. **MrpackManifestValidator — Matryca detekcji loaderów:**
    - Prawidłowe rozpoznanie Vanilla, Forge, NeoForge oraz Quilt z ich wersjami.
13. **MrpackManifestValidator — Weryfikacja formatu sum kontrolnych:**
    - Weryfikacja dokładnej długości i formatu hex: 40 znaków dla SHA-1, 128 znaków dla SHA-512.
14. **MrpackManifestValidator — Weryfikacja adresów pobierania HTTPS:**
    - Blokada niezabezpieczonych protokołów HTTP oraz pustych tablic `downloads`.
15. **MrpackArchiveWriter & Reader — Generowanie i odczyt archiwum ZIP:**
    - Zbudowanie pliku `.mrpack`, odczyt wpisów w Central Directory i parsowanie `modrinth.index.json` oraz pliku overrides.
16. **MrpackArchiveReader — Odrzucenie archiwum bez manifestu:**
    - Zgłoszenie błędu w przypadku archiwum ZIP niezawierającego `modrinth.index.json` w katalogu głównym.
17. **MrpackArchiveReader — Egzekwowanie limitów archiwum:**
    - Weryfikacja pliku pod kątem maksymalnych limitów rozmiaru (ochrona przed Zip Bomb).
18. **MrpackOverrideManager — Priorytet client-overrides nad overrides:**
    - Zastosowanie warstwy `client-overrides/` nadpisującej plik z `overrides/` o tej samej ścieżce względnej.
19. **MrpackOverrideManager — Izolacja server-overrides:**
    - Potwierdzenie, że pliki z `server-overrides/` nie trafiają do katalogu gry, lecz są zachowywane w `.mrpack-preserved/server-overrides/`.
20. **MrpackOverrideManager — Ścisła filtracja danych prywatnych:**
    - Potwierdzenie bezwzględnego wykluczenia folderów `saves/`, `logs/`, `screenshots/`, `crash-reports/` oraz plików kont i sesji podczas eksportu.
21. **MrpackImporter — Inspekcja archiwum w trybie Dry-Run:**
    - Odczyt metadanych paczki (nazwa, wersja, loader, liczba plików, rozmiar do pobrania) bez modyfikacji plików na dysku.
22. **MrpackImporter — Weryfikacja białej listy hostów CDN:**
    - Akceptacja oficjalnych serwerów CDN (`cdn.modrinth.com`, `edge.modrinth.com`, `github.com`) oraz blokada domen nieautoryzowanych i prywatnych IP.
23. **MrpackImporter — Transakcyjny rollback w przypadku błędu:**
    - Usunięcie utworzonej instancji oraz wyczyszczenie plików staging w razie wystąpienia błędu sieciowego lub niepoprawnego hashu.
24. **Mandatory Round-Trip Integration Test — Pełny cykl Eksport -> Weryfikacja -> Import:**
    - Utworzenie instancji źródłowej z modami i konfiguracją.
    - Eksport do archiwum `.mrpack`.
    - Weryfikacja struktury archiwum i wygenerowanego `modrinth.index.json`.
    - Zaimportowanie wyeksportowanego pliku do nowej instancji docelowej.
    - Potwierdzenie zgodności wszystkich plików modów, plików konfiguracyjnych i wygenerowanego lockfile!
25-29. **Modloader Compatibility Matrix:**
    - Test detekcji i konfiguracji dla 5 środowisk: Vanilla, Fabric, Forge, NeoForge, Quilt.

---

## 11. Testy Systemu Modułów i Menu w Grze (Etap 7B)

W Etapie 7B wdrożono zestaw 20 testów jednostkowych i integracyjnych JUnit 5 dla modułu Fabric w `apps/minecraft-client/fabric/src/test/java/pl/zombek/client/`:

1. **`ClientArchitectureTest`**:
   - `testClientConstants`: Weryfikacja unikalnego MOD_ID (`zombekclient`), nazwy klienta (`ZombekClient`), loadera (`fabric`) oraz wersji.
2. **`ModuleManagerTest`**:
   - `testRegisterAndRetrieveModule`: Dynamiczna rejestracja modułu, pobieranie po ID, unikalność.
   - `testRejectDuplicateModuleId`: Zabezpieczenie przed kolizją ID — rzucanie `IllegalArgumentException` przy próbie zarejestrowania modułu o istniejącym ID.
   - `testFilterByCategory`: Poprawna kategoryzacja modułów do 5 domen: `PVP`, `HUD`, `VISUAL`, `SURVIVAL`, `OTHER`.
   - `testSearchModules`: Wyszukiwanie pełnotekstowe po nazwie, opisie i kategorii; obsługa pustego zapytania i zapytań bez wyników.
3. **`ModuleLifecycleTest`**:
   - `testNormalLifecycleFlow`: Pełny cykl życia `onInitialize` -> `onEnable` -> `onTick` -> `onDisable` oraz idempotencja podwójnych wywołań włączania/wyłączania.
   - `testErrorBoundaryOnEnableCrash`: Izolacja błędu podczas `onEnable()`. Wyjątek jest przechwytywany w `safeEnable()`, moduł pozostaje wyłączony (`isEnabled() == false`), zostaje oznaczony `hasError() == true` z zachowaniem komunikatu błędu.
   - `testErrorBoundaryOnTickCrash`: Izolacja błędu podczas `onTick()`. Rzucenie wyjątku wewnątrz modułu nie wysypuje pętli gry — moduł zostaje automatycznie bezpiecznie wyłączony (`safeDisable()`), a błąd zalogowany.
4. **`SettingsValidationTest`**:
   - `testBooleanSetting`: Przełączanie `toggle()`, serializacja ("true"/"false"), poprawna deserializacja.
   - `testNumberSettingClampingAndStepping`: Przycinanie wartości do granic min/max, kwantyzacja kroków suwaka, precyzja dziesiętna, formatowanie z sufiksem jednostki.
   - `testColorSetting`: Rozbijanie składowych ARGB, kalkulacja heksadecymalna `#RRGGBBAA` oraz `#RRGGBB`, deserializacja i obsługa kanału alfa.
   - `testEnumSetting`: Cykliczne przełączanie wariantów typów wyliczeniowych w przód (`cycleNext()`), deserializacja po nazwie.
   - `testStringSettingConstraints`: Egzekwowanie limitu maksymalnej długości ciągu oraz walidacja wyrażeniem regularnym `Pattern`.
   - `testKeybindSettingNames`: Translacja kodów GLFW na czytelne etykiety (`RSHIFT`, `SPACE`, `F5`, `NONE`).
   - `testMultiSelectSetting`: Dodawanie i usuwanie opcji, filtrowanie nieznanych wartości w `sanitize()`, serializacja do listy rozdzielanej przecinkami.
   - `testConditionalVisibility`: Weryfikacja dynamicznej widoczności parametrów zależnych `.visibleWhen()`.
5. **`ModuleConfigManagerTest`**:
   - `testSaveAndLoadConfiguration`: Zapis stanu modułów, skrótów klawiszowych i wartości parametrów do formatu JSON v1 (`modules.json`), a następnie odczyt i przywrócenie stanu.
   - `testCorruptFileRecovery`: Odporność na uszkodzone pliki konfiguracyjne — automatyczne utworzenie kopii zapasowej `modules.json.bak` i załadowanie wartości domyślnych bez rzucania błędów.
6. **`KeybindManagerTest`**:
   - `testDefaultMenuKeyConstant`: Weryfikacja przypisania domyślnego klawisza otwarcia menu do `GLFW.GLFW_KEY_RIGHT_SHIFT`.
   - `testConflictDetection`: Wykrywanie kolizji skrótów klawiszowych między wieloma modułami i raportowanie listy konfliktujących modułów.

---

## 12. Testy Multiloader Common Core i Forge Client Adapter (Etap 7C1)

W Etapie 7C1 wprowadzono testy weryfikujące poprawność separacji architektury na `:core`, adapter `:forge` oraz integrację z launcherem:

### 12.1. Testy Jednostkowe ZombekClient Core (`apps/minecraft-client/core/src/test/java/`):
1. **`ZombekCoreTest`**:
   - `testCoreLifecycle`: Inicjalizacja `ZombekCore.init()` z mockowanym `IPlatformAdapter`.
   - `testRejectDoubleInit`: Ochrona przed wielokrotnym bootstrapem rdzenia (`IllegalStateException`).
   - `testShutdown`: Zapisywanie konfiguracji podczas zamykania gry.
2. **`ModuleManagerTest`**:
   - Weryfikacja rejestracji modułów, wyszukiwarki pełnotekstowej i filtrowania po 5 kategoriach.
3. **`ModuleLifecycleTest`**:
   - Testy pętli ticków, włączania/wyłączania oraz mechanizmu Error Boundary (`safeTick`, `safeEnable`).
4. **`SettingsValidationTest`**:
   - Testy typów ustawień: Boolean, Number, Color, Enum, String, Keybind, MultiSelect, SettingGroup oraz warunkowości `.visibleWhen()`.
5. **`ModuleConfigManagerTest`**:
   - Zapis atomowy schema v1 JSON, odporność na uszkodzone pliki i automatyczne tworzenie `.bak`.

### 12.2. Testy Integracyjne Forge Adapter (`apps/minecraft-client/forge/src/test/java/`):
1. **`ForgePlatformAdapterTest`**:
   - Weryfikacja implementacji `IPlatformAdapter` zwracającej nazwę "Forge" oraz poprawny katalog konfiguracyjny.
2. **`ForgeKeybindManagerTest`**:
   - Weryfikacja domyślnego klawisza RIGHT SHIFT, logiki detekcji pojedynczej krawędzi (edge-detection) zapobiegającej zapętlonemu otwieraniu menu przy trzymaniu klawisza.
3. **`ForgeCoreIntegrationTest`**:
   - Inicjalizacja Core z `ForgePlatformAdapter`, rejestracja `CoreTestModule`, modyfikacja wartości parametrów i zapis do katalogu konfiguracyjnego Forge.

### 12.3. Testy Rejestru i Rozpoznawania Wydań w Launcherze (`apps/launcher/tests/client-release.test.ts`):
1. `should load manifest with both Fabric and Forge releases`:
   - Wczytanie manifestu `client-releases.json` i potwierdzenie obecności artefaktów dla Fabric i Forge.
2. `should match appropriate release for Forge 1.20.4 instance`:
   - Weryfikacja dopasowania `zombekclient-forge-0.1.0-alpha.jar` dla instancji Forge 1.20.4.
3. `should match appropriate release for Fabric 1.20.4 instance`:
   - Weryfikacja dopasowania `zombekclient-fabric-0.1.0-alpha.jar` dla instancji Fabric 1.20.4.
4. `should reject vanilla instance when attempting client mod installation`:
   - Blokada instalacji modułu na czystym profilu Vanilla z czytelnym komunikatem błędu.
5. `should successfully install Forge client mod and detect its status`:
   - Kopiowanie właściwego JARa do `instances/<id>/mods/` i wykrycie statusu instalacji.
6. `should parse compiled Forge jar metadata via ModMetadataReader`:
   - Odczyt metadanych bezpośrednio ze zbudowanego archiwum `zombekclient-forge-0.1.0-alpha.jar` za pomocą `ModMetadataReader` (odczyt `mods.toml`, nazwa, ID `zombekclient`, zależności `minecraft` i `forge`).

---

## 13. Testy NeoForge Client Adapter (Etap 7C2)

W Etapie 7C2 zaimplementowano i zweryfikowano pełny zestaw testów adaptera NeoForge w środowisku JUnit 5 (`apps/minecraft-client/neoforge/src/test/java/`) oraz rozbudowano zestaw testów launchera:

### 13.1. Testy Jednostkowe NeoForge (`pl.zombek.client.neoforge`):
1. **`NeoForgePlatformAdapterTest`**:
   - Weryfikacja implementacji `IPlatformAdapter` zwracającej nazwę platformy `"NeoForge"`.
   - Potwierdzenie ścieżki katalogu konfiguracji pod `zombekclient`.
   - Test bezpiecznego wykonania akcji na wątku renderowania (`executeOnRenderThread`).
2. **`NeoForgeKeybindManagerTest`**:
   - Weryfikacja domyślnego klawisza otwarcia menu: `GLFW.GLFW_KEY_RIGHT_SHIFT`.
   - Test cyklu edge-detection: wciśnięcie klawisza wywołuje otwarcie menu, trzymanie klawisza (hold) nie wywołuje ponownych akcji, zwolnienie klawisza resetuje flagę.
   - Weryfikacja działania metod detekcji konfliktów klawiszy (`hasConflict`, `getConflictingModules`).
3. **`NeoForgeCoreIntegrationTest`**:
   - Inicjalizacja `ZombekCore.init()` z `NeoForgePlatformAdapter`.
   - Pobranie i aktywacja referencyjnego modułu `CoreTestModule`.
   - Zmiana parametrów: suwak `opacity` na 85.0, `displayMode` na `EXPANDED`, `customText` na `"NeoForgeClientTest"`.
   - Wymuszenie atomowego zapisu do pliku `config/zombekclient/modules.json`.
   - Weryfikacja zawartości pliku na dysku.
   - Zresetowanie stanu w pamięci i załadowanie konfiguracji z dysku (`load()`) — potwierdzenie odzyskania stanu włączonego modułu i wszystkich zmodyfikowanych wartości parametrów.

### 13.2. Testy Integracji Wydań NeoForge w Launcherze (`apps/launcher/tests/client-release.test.ts`):
1. `should load manifest with Fabric, Forge, and NeoForge releases`:
   - Weryfikacja obecności oficjalnych wydań klienta ze specyfikacją `1.20.4` i formatami metadanych.
2. `should match appropriate release for NeoForge 1.20.4 instance`:
   - Weryfikacja dopasowania `zombekclient-neoforge-0.1.0-alpha.jar` oraz punktu wejścia `pl.zombek.client.neoforge.entrypoint.ZombekNeoForgeMod`.
3. `should successfully install NeoForge client mod and detect its status`:
   - Tworzenie instancji z loaderem `neoforge`, instalacja JARa do `instances/<id>/mods/` i potwierdzenie statusu zainstalowania.
4. `should parse compiled NeoForge jar metadata via ModMetadataReader`:
   - Odczyt metadanych bezpośrednio z fizycznego pliku `apps/minecraft-client/neoforge/build/libs/zombekclient-neoforge-0.1.0-alpha.jar`.
   - Weryfikacja loader: `'neoforge'`, źródło: `'neoforge.mods.toml'`, ID: `'zombekclient'`, nazwa: `'ZombekClient'` oraz zależności od `minecraft` i `neoforge`.

---

## 14. Testy Quilt Client Adapter (Etap 7C3)

W Etapie 7C3 zaimplementowano i zweryfikowano pełny zestaw testów adaptera Quilt w środowisku JUnit 5 (`apps/minecraft-client/quilt/src/test/java/`) oraz rozbudowano zestaw testów launchera:

### 14.1. Testy Jednostkowe Quilt (`pl.zombek.client.quilt`):
1. **`QuiltPlatformAdapterTest`**:
   - Weryfikacja implementacji `IPlatformAdapter` zwracającej nazwę platformy `"quilt"`.
   - Potwierdzenie ścieżki katalogu konfiguracji zawierającej `zombekclient`.
   - Weryfikacja `isClient() == true` oraz test metody `executeOnRenderThread`.
2. **`QuiltKeybindManagerTest`**:
   - Weryfikacja domyślnego klawisza otwarcia menu: `GLFW.GLFW_KEY_RIGHT_SHIFT`.
   - Sprawdzenie natywnej translacji klawisza i kategorii (`key.zombekclient.open_menu`, `key.categories.zombekclient`).
   - Weryfikacja działania metod detekcji konfliktów klawiszy (`hasConflict`, `getConflictingModules`).
3. **`QuiltCoreIntegrationTest`**:
   - Inicjalizacja `ZombekCore.init()` z `QuiltPlatformAdapter`.
   - Pobranie i aktywacja referencyjnego modułu `CoreTestModule`.
   - Zmiana parametrów: klawisz `K`, suwak `opacity` na 60.0, `displayMode` na `EXPANDED`, `customText` na `"QuiltClientTest"`.
   - Wymuszenie atomowego zapisu do pliku `config/zombekclient/modules.json`.
   - Weryfikacja zawartości pliku na dysku.
   - Zresetowanie stanu w pamięci i ponowne załadowanie konfiguracji z dysku (`load()`) — potwierdzenie odzyskania stanu włączonego modułu i wszystkich zmodyfikowanych wartości parametrów.

### 14.2. Testy Integracji Wydań Quilt w Launcherze (`apps/launcher/tests/client-release.test.ts`):
1. `should load manifest with Fabric, Forge, NeoForge, and Quilt releases`:
   - Weryfikacja obecności 4 oficjalnych wydań klienta ze specyfikacją `1.20.4` i odpowiednimi formatami metadanych (`fabric.mod.json`, `mods.toml`, `neoforge.mods.toml`, `quilt.mod.json`).
2. `should match appropriate release for Quilt 1.20.4 instance`:
   - Weryfikacja dopasowania `zombekclient-quilt-0.1.0-alpha.jar` oraz punktu wejścia `pl.zombek.client.quilt.entrypoint.QuiltClientBootstrap`.
3. `should successfully install Quilt client mod and detect its status`:
   - Tworzenie instancji z loaderem `quilt`, instalacja JARa do `instances/<id>/mods/` i potwierdzenie statusu zainstalowania.
4. `should parse compiled Quilt jar metadata via ModMetadataReader`:
   - Odczyt metadanych bezpośrednio z fizycznego pliku `apps/minecraft-client/quilt/build/libs/zombekclient-quilt-0.1.0-alpha.jar`.
   - Weryfikacja loader: `'quilt'`, źródło: `'quilt.mod.json'`, ID: `'zombekclient'`, nazwa: `'ZombekClient'` oraz zależności od `minecraft` i `quilt_loader`.

---

## 15. Testy Modułów Keystrokes, CPS Counter i Architektury HUD (Etap 8A1)

W Etapie 8A1 zaimplementowano 5 nowych zestawów testów jednostkowych w `:core` (`apps/minecraft-client/core/src/test/java/pl/zombek/client/core/`):

1. **`ClickTrackerTest`**:
   - Pomiar nanosekundowy za pomocą `System.nanoTime()`.
   - Poprawne usuwanie przestarzałych kliknięć po przekroczeniu okna czasowego (np. 1000 ms).
   - Rejestracja rekordowego CPS (peak CPS) oraz test resetowania (`resetPeak()`).
   - Niezależny pomiar dla lewego (LMB) i prawego (RMB) przycisku myszy.
2. **`KeyTrackerTest`**:
   - Śledzenie stanów klawiszy (W, A, S, D, SPACE, SHIFT, CTRL) oraz przycisków myszy.
   - Test metody `resetAll()` wywoływanej przy otwarciu GUI i utracie fokusu okna.
3. **`HudAnchorTest`**:
   - Poprawność kalkulacji współrzędnych X i Y dla `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`.
   - Zachowanie marginesów ekranowych przy dynamicznej zmianie rozdzielczości gry.
4. **`KeystrokesModuleTest`**:
   - Poprawna rejestracja w kategorii `Category.PVP`.
   - Kompletność wszystkich typowanych grup ustawień: `general`, `layout`, `colors`, `appearance`.
   - Implementacja `IHudElement` z testowym obiektem renderującym `TestHudRenderer`.
5. **`CPSModuleTest`**:
   - Rejestracja w `Category.PVP` i walidacja wartości początkowych.
   - Testy stylów `BOXED`, `STACKED` i `INLINE`.
   - Sprawdzenie formatowania etykiet i suffiksu `" CPS"`.

---

## 16. Testy Modułów Armor Status, Combo Counter i Cooldowns (Etap 8A2)

W Etapie 8A2 wdrożono 5 nowych zestawów testów jednostkowych w module `:core` (`apps/minecraft-client/core/src/test/java/pl/zombek/client/core/`):

1. **`ArmorItemDataTest`**:
   - `testDurabilityCalculation`: Weryfikacja obliczeń pozostałej trwałości (`maxDamage - damage`) oraz zaokrągleń procentowych (`remaining / maxDamage * 100`).
   - `testNonDamageableItem`: Poprawne zachowanie dla przedmiotów bez paska trwałości (np. dynia, głowy mobów) — flaga `hasDurability == false`, brak fałszywych liczb trwałości, zachowanie 100%.
   - `testEmptySlot`: Fabryka `ArmorItemData.empty(slot)` zwracająca pusty element z `isEmpty() == true`.
   - `testEnchantmentsList`: Niemutowalność i integralność listy nazw zaklęć.
2. **`ArmorStatusModuleTest`**:
   - `testModuleMetadata`: Rejestracja w kategorii `Category.PVP`, domyślny status, unikalny identyfikator `"armor-status"`.
   - `testSettingGroups`: Kompletność 6 grup ustawień (`general`, `display`, `layout`, `colors`, `durability_colors`, `appearance`) oraz ich parametrów (progi procentowe, układy, kolejność slotów).
   - `testRenderWithoutException`: Weryfikacja bezpiecznego renderowania w środowisku testowym (z mockowanymi elementami zbroi w `ArmorDataProviderRegistry`).
3. **`ComboTrackerTest`**:
   - `testBasicHitIncrement`: Inkrementacja licznika przy poprawnych ciosach.
   - `testDeduplicationWindow`: Odrzucanie ciosów w oknie mniejszym niż 50 ms (zapobieganie podwójnemu naliczaniu z pętli zdarzeń).
   - `testTargetChangeReset`: Resetowanie serii po zaatakowaniu innego bytu (inny identyfikator celu).
   - `testMissReset`: Natychmiastowe zerowanie combo po zarejestrowaniu chybienia (zamach w próżnię).
   - `testTimeoutExpiration`: Wygasanie combo po przekroczeniu zadanego czasu bez trafienia.
   - `testDeathReset`: Zerowanie licznika po śmierci gracza (`onDeath()`).
4. **`ComboCounterModuleTest`**:
   - `testModuleMetadata`: Rejestracja w `Category.PVP`, identyfikator `"combo-counter"`.
   - `testSettingGroups`: Weryfikacja grup `general`, `display`, `colors`, `milestones`, `animation`, `behavior`, `layout`.
   - `testMilestoneResolutionAndRender`: Poprawność doboru kolorów progu (tier 1-3, 4-6, 7-9, 10+) oraz bezpieczne renderowanie w `render()`.
5. **`CooldownsModuleTest`**:
   - `testModuleMetadata`: Rejestracja w `Category.PVP`, identyfikator `"cooldowns"`.
   - `testSettingGroupsAndRender`: Weryfikacja grup `general`, `display`, `behavior`, `colors`, `layout` oraz bezpiecznego renderowania z aktywnymi cooldownami zarejestrowanymi w `CooldownDataProviderRegistry`.

---

## 17. Testy Modułów Crosshair, Hit Color i Hitbox (Etap 8A3)

W Etapie 8A3 wdrożono 5 nowych zestawów testów jednostkowych w module `:core` (`apps/minecraft-client/core/src/test/java/pl/zombek/client/core/`), rozszerzając bazę testów Core do 74 testów w 20 klasach:

1. **`CrosshairModuleTest`**:
   - `testModuleMetadata`: Rejestracja w kategorii `Category.VISUAL`, unikalny identyfikator `"crosshair"`.
   - `testSettingGroups`: Kompletność 5 grup ustawień (`general`, `size`, `colors`, `dynamic`, `advanced`).
   - `testVanillaCrosshairHidingLogic`: Weryfikacja logiki `shouldHideVanillaCrosshair()` w zależności od stanu włączenia, opcji ukrywania i obecności otwartych menu.
   - `testAllStylesRenderWithoutException`: Weryfikacja bezbłędnego renderowania wszystkich 8 stylów celownika (`CLASSIC_CROSS`, `SIMPLE_CROSS`, `DOT`, `CIRCLE`, `SQUARE`, `T_SHAPE`, `DYNAMIC_CROSS`, `CUSTOM`) na `TestHudRenderer`.
   - `testDynamicColorResolution`: Poprawność wyznaczania barw w zależności od najechania na wrogi byt, gracza, zwierzę lub blok.
2. **`HitColorTrackerTest`**:
   - `testBasicHitRecording`: Poprawne rejestrowanie uderzeń bytu i weryfikacja rekordu `HitColorRecord`.
   - `testFadeProgressDecay`: Monotoniczny spadek intensywności wygaszania w czasie.
   - `testCurveEvaluation`: Porównanie zachowania krzywych `LINEAR`, `EASE_OUT` oraz `EXPONENTIAL`.
   - `testTargetAndLocalPlayerFilters`: Prawidłowe odfiltrowywanie efektu przy włączonych filtrach `onlyTargeted` oraz `onlyLocalPlayerDamage`.
   - `testMaxActiveEffectsCapping`: Utrzymywanie limitu maksymalnej liczby śledzonych efektów i automatyczne usuwanie najstarszych.
3. **`HitColorModuleTest`**:
   - `testModuleMetadata`: Rejestracja w `Category.VISUAL`, identyfikator `"hit-color"`.
   - `testSettingGroups`: Weryfikacja 5 grup ustawień (`general`, `colors`, `timing`, `appearance`, `advanced`).
   - `testConfigurationDefaultsAndClamping`: Weryfikacja zakresów wartości liczbowych i zachowania wartości domyślnych.
4. **`HitboxGeometryCalculatorTest`**:
   - `testVertexCalculationRelativeCamera`: Poprawność wyznaczania 12 krawędzi bounding boxa z uwzględnieniem translacji kamery.
   - `testQuadGeneration`: Poprawność współrzędnych 6 czwórokątów (ścianek) w trybach `FILLED` i `OUTLINE_AND_FILLED`.
   - `testEyeLineAndLookVector`: Dokładność wyliczania linii wzroku oraz wektora spojrzenia.
5. **`HitboxModuleTest`**:
   - `testModuleMetadata`: Rejestracja w `Category.VISUAL`, identyfikator `"hitbox"`.
   - `testSettingGroups`: Weryfikacja 6 grup ustawień (`general`, `appearance`, `colors`, `filters`, `information`, `advanced`).
   - `testEntityFilterDecisions`: Filtrowanie po odległości (`max_distance`), filtrach typów bytów oraz ignorowanie własnego gracza w FPP.
   - `testColorResolutionByEntityType`: Rozstrzyganie odrębnych kolorów obrysu i wypełnienia dla graczy, mobów wrogich, zwierząt i bytów celowanych.

---

## 18. Testy Modułów Freelook, Reach Display i PvP Info (Etap 8A4)

W Etapie 8A4 wdrożono 6 nowych zestawów testów jednostkowych w module `:core` (`apps/minecraft-client/core/src/test/java/pl/zombek/client/core/`), rozszerzając bazę testów Core do **99 testów w 26 klasach**:

1. **`FreelookCameraControllerTest`** (5 testów):
   - `testActivationAndDeactivation`: Prawidłowe przechodzenie między stanem nieaktywnym a aktywnym z utrwaleniem `originalYaw` i `originalPitch`.
   - `testMouseInputClampingAndSensitivity`: Poprawne aplikowanie delty myszy, czułości, inwersji oraz ograniczanie kątów (`clampPitch`, `clampYaw`).
   - `testSnapbackInterpolation`: Weryfikacja animacji powrotu kamery (krzywe `LINEAR`, `EASE_OUT`, `SMOOTH_STEP`) i wrapowanie kątów `yaw`.
   - `testResetCleansState`: Pełne czyszczenie flag kontrolera po wywołaniu `reset()`.
   - `testThreadSafety`: Bezpieczny współbieżny dostęp do stanu kamery.
2. **`FreelookModuleTest`** (4 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.VISUAL`, unikalny identyfikator `"freelook"`.
   - `testSettingGroups`: Weryfikacja 4 grup ustawień (`general`, `camera`, `animation`, `advanced`).
   - `testHandleKeyInputHoldMode`: Aktywacja przy wciśnięciu klawisza i deaktywacja/snapback po zwolnieniu.
   - `testHandleKeyInputToggleMode`: Przełączanie stanu włączony/wyłączony na krawędzi wciśnięcia.
3. **`ReachTrackerTest`** (4 testy):
   - `testDistanceToAABB`: Precyzyjne obliczenia clamp do prostopadłościanu AABB i odległość euklidesowa; $0.00$ dla oczu wewnątrz bryły celu.
   - `testAggregatesAndHistoryCapping`: Poprawne wyliczanie średniej arytmetycznej, min/max oraz limitowanie bufora historii.
   - `testEventDistinction`: Rozróżnianie zdarzeń `OBSERVED_HIT` oraz `ATTACK_ATTEMPT` (miss).
   - `testResetStatistics`: Czyszczenie historii i statystyk przy zmianie świata lub sesji.
4. **`ReachDisplayModuleTest`** (4 testy):
   - `testModuleMetadata`: Rejestracja w `Category.PVP`, identyfikator `"reach_display"`.
   - `testSettingGroups`: Weryfikacja 6 grup ustawień (`general`, `display`, `behavior`, `colors`, `appearance`, `layout`).
   - `testRenderCompactAndDetailed`: Bezpieczne renderowanie na `TestHudRenderer` w obu stylach z poprawnym formatowaniem tekstu.
   - `testVisibility`: Prawidłowe zwracanie `isVisible()` w zależności od stanu modułu i flagi błędu.
5. **`PvPSessionTest`** (4 testy):
   - `testMeleeMetricsAndAccuracy`: Poprawne zliczanie ataków, trafień i wyliczanie celności procentowej (`getEstimatedHitRate()`).
   - `testDamageAndHealthUpdates`: Aktualizacja punktów zdrowia gracza, absorpcji, otrzymanych obrażeń oraz stanu przeciwnika.
   - `testSessionLifecycleAndReset`: Czas trwania potyczki, zamykanie i resetowanie sesji.
   - `testComboTrackingInSession`: Utrwalanie rekordu najwyższego combo w ramach trwającej sesji.
6. **`PvPInfoModuleTest`** (4 testy):
   - `testModuleMetadata`: Rejestracja w `Category.PVP`, identyfikator `"pvp_info"`.
   - `testSettingGroups`: Weryfikacja 5 grup ustawień (`general`, `display`, `colors`, `appearance`, `layout`).
   - `testAllStylesRenderWithoutException`: Weryfikacja renderowania wszystkich 5 stylów (`COMPACT`, `DETAILED`, `HORIZONTAL`, `VERTICAL`, `MINIMAL`) na `TestHudRenderer`.
   - `testResetActionSetting`: Wykrywanie przełączenia ustawienia akcji resetu i automatyczne zerowanie flagi po wykonaniu.

---

## 19. Testy Modułów Toggle Sprint, Toggle Sneak i UHC Overlay (Etap 8A5)

W Etapie 8A5 wdrożono 8 nowych zestawów testów jednostkowych w module `:core` (`apps/minecraft-client/core/src/test/java/pl/zombek/client/core/`), rozszerzając bazę testów Core z 99 do **129 testów w 34 klasach** (100% zaliczone):

1. **`MovementToggleControllerTest`** (7 testów):
   - `testSprintToggleAndHoldModes`: Przełączanie trybów `TOGGLE` i `HOLD` dla sprintu oraz priorytet fizycznego trzymania klawisza.
   - `testSneakToggleAndHoldModes`: Przełączanie trybów `TOGGLE` i `HOLD` dla kucania oraz tryb hybrydowy `TOGGLE_AND_HOLD`.
   - `testVanillaSprintPreconditions`: Blokada sprintu przy braku ruchu naprzód (`forwardImpulse <= 0`), głodzie $\le 6$, ślepocie (`blindness`) lub kolizji czołowej.
   - `testSneakBlocksSprint`: Automatyczne wyłączanie sprintu w trakcie aktywnego kucania.
   - `testAutomaticResetsOnEvents`: Natychmiastowe zerowanie flag ruchu przy śmierci gracza, respawnie, zmianie świata, otwarciu menu GUI oraz utracie fokusu okna gry.
   - `testDisableWhileUsingItems`: Warunek blokady sprintu przy jedzeniu, piciu lub naciąganiu łuku.
   - `testThreadSafety`: Bezpieczna, współbieżna modyfikacja stanu flag ruchu z wielu wątków klienta.

2. **`ToggleSprintModuleTest`** (4 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.PVP`, unikalny identyfikator `"toggle_sprint"`, domyślny klawisz `LEFT_ALT`.
   - `testSettingGroups`: Weryfikacja 4 grup ustawień (`general`, `appearance`, `colors`, `behavior`).
   - `testRenderIndicator`: Poprawne renderowanie wskaźnika tekstowego we wszystkich stylach (`BRACKETED`, `COMPACT`, `DETAILED`, `TEXT_ONLY`) bez rzucania wyjątków.
   - `testLifecycleTransitions`: Prawidłowa obsługa `onEnable()` i `onDisable()` wraz z czyszczeniem stanu w `MovementToggleController`.

3. **`ToggleSneakModuleTest`** (4 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.PVP`, unikalny identyfikator `"toggle_sneak"`, domyślny klawisz `C`.
   - `testSettingGroups`: Weryfikacja 4 grup ustawień (`general`, `appearance`, `colors`, `behavior`).
   - `testRenderIndicator`: Poprawne renderowanie wskaźnika kucania na `TestHudRenderer`.
   - `testDisableResetsSneak`: Weryfikacja deaktywacji modułu i natychmiastowego resetu kucania w kontrolerze.

4. **`UhcTimerTest`** (3 testy):
   - `testStopwatchElapsedNanoseconds`: Dokładność pomiaru stopera opartego na `System.nanoTime()`.
   - `testCountdownTimerCompletion`: Prawidłowe odliczanie timera w dół, stan `RUNNING`, przejście w stan `COMPLETED` i formatowanie czasu `mm:ss`.
   - `testPauseResumeReset`: Weryfikacja pauzowania, wznawiania i zerowania stanu czasomierza.

5. **`UhcScoreboardParserTest`** (4 testy):
   - `testExtractAlivePlayersPolishAndEnglish`: Wykrywanie liczby żywych graczy z linii typu `Gracze: 14`, `Alive: 14`, `Żywi: 14`.
   - `testExtractRemainingTeams`: Wykrywanie liczby zespołów (`Druzyny: 4`, `Teams: 4`).
   - `testExtractBorderRadius`: Parsowanie promienia granicy świata (`Border: ±500`, `Granica: 500m`).
   - `testExtractKills`: Parsowanie zabójstw gracza (`Kills: 3`, `Zabojstwa: 3`).

6. **`UhcSessionTrackerTest`** (2 testy):
   - `testSessionLifecycle`: Rozpoczynanie, aktualizacja stanu meczu i resetowanie statystyk sesji UHC.
   - `testItemCountTracking`: Rejestracja i zliczanie kluczowych przedmiotów (złote jabłka, złote głowy, perły kresu, strzały).

7. **`UhcChatParserTest`** (3 testy):
   - `testPvpEnabledEvent`: Wykrywanie z czatu komunikatu o włączeniu PvP (np. `PvP has been enabled!`, `PvP zostalo wlaczone!`).
   - `testBorderShrinkEvent`: Wykrywanie komunikatów o kurczeniu się strefy.
   - `testDeathAndKillMessages`: Zliczanie eliminacji na podstawie standardowych komunikatów śmierci.

8. **`UHCOverlayModuleTest`** (3 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.SURVIVAL`, unikalny identyfikator `"uhc_overlay"`.
   - `testSettingGroups`: Weryfikacja 6 grup ustawień (`general`, `layout`, `player_status`, `item_tracker`, `timers`, `match_info`).
   - `testRenderAllLayoutStyles`: Bezpieczne renderowanie na `TestHudRenderer` wszystkich 5 układów (`COMPACT`, `DETAILED`, `HORIZONTAL`, `VERTICAL`, `CUSTOM`).

---

## 20. Testy Modułów TNT Countdown, Shiny Pots, Snaplook, Team View oraz Nowego UHC (Etap 8A6)

W Etapie 8A6 rozbudowano bazę testów jednostkowych Core z 129 do **159 testów w 44 klasach** (100% zaliczone), weryfikując wszystkie nowe mechanizmy kalkulacji, kontrolerów i modułów:

1. **`TntCountdownCalculatorTest`** (4 testy):
   - `testTickToSecondConversion`: Konwersja pozostałych ticków lontu na sekundy z precyzją dziesiętną.
   - `testArgbColorGrading`: Weryfikacja progów kolorystycznych ARGB (zielony $> 2.5\text{s}$, żółty $1.0\text{s} - 2.5\text{s}$, czerwony $< 1.0\text{s}$).
   - `testDisplayFormatFormatting`: Prawidłowe formatowanie z prefiksem (`"TNT: 2.5s"`) oraz bez prefiksu (`"2.5s"`).
   - `testMaxDistanceCulling`: Weryfikacja odrzucania encji poza zdefiniowanym promieniem widoczności.

2. **`TntCountdownModuleTest`** (3 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.PVP`, identyfikator `"tnt_countdown"`.
   - `testSettingGroups`: Weryfikacja 4 grup ustawień (`general`, `appearance`, `colors`, `behavior`).
   - `testTrackerManagement`: Bezpieczne dodawanie i usuwanie encji z `TntTracker`.

3. **`ShinyPotsControllerTest`** (5 testów):
   - `testPotionItemDetection`: Prawidłowe rozpoznawanie mikstur zwykłych, miotanych, trwających i zatrutych strzał.
   - `testContextFiltering`: Weryfikacja aktywności efektu w zależności od kontekstu (`INVENTORY`, `HOTBAR`, `CONTAINER`, `HELD`, `WORLD`).
   - `testCustomRulesParsing`: Odczyt i parsowanie reguł niestandardowych (whitelist/blacklist).
   - `testPulsateAlpha`: Wyliczanie sinusoidalnej fali przezroczystości $\alpha(t)$ w czasie.
   - `testDisabledModuleReturnsFalse`: Gwarancja braku połysku, gdy moduł jest wyłączony.

4. **`ShinyPotsModuleTest`** (3 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.VISUAL`, identyfikator `"shiny_pots"`.
   - `testSettingGroups`: Weryfikacja 4 grup ustawień (`general`, `items`, `contexts`, `appearance`).
   - `testDefaultValues`: Weryfikacja domyślnych wartości parametrów suwaków i przełączników.

5. **`SnaplookControllerTest`** (5 testów):
   - `testHoldModeActivation`: Zmiana perspektywy przy wciśnięciu klawisza i natychmiastowy powrót po zwolnieniu.
   - `testToggleModeActivation`: Przełączanie perspektywy kliknięciem i powrót kolejnym kliknięciem.
   - `testManualF5Priority`: Natychmiastowe unieważnienie stanu Snaplook przy manualnym naciśnięciu klawisza F5 (`respect_manual_changes`).
   - `testAutoResetOnFocusLost`: Reset do perspektywy domyślnej przy utracie fokusu okna lub pauzie gry.
   - `testEasingCalculations`: Weryfikacja współczynników wygładzania przejścia kamery.

6. **`SnaplookModuleTest`** (3 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.PVP`, identyfikator `"snaplook"`, domyślny klawisz `GLFW_KEY_V`.
   - `testSettingGroups`: Weryfikacja 4 grup ustawień (`general`, `perspective`, `behavior`, `appearance`).
   - `testLifecycle`: Włączanie i wyłączanie z czyszczeniem stanu w `SnaplookController`.

7. **`TeamTrackerTest`** (4 testy):
   - `testScoreboardTeamDetection`: Rozpoznawanie sojuszników z tej samej drużyny Scoreboard oraz ich kolorów.
   - `testManualListDetection`: Rozpoznawanie sojuszników wpisanych do listy manualnej.
   - `testCombinedDetectionMode`: Prawidłowe działanie trybu `BOTH`.
   - `testITeamDirectionProviderContract`: Poprawne zwracanie listy pozycji i kątów sojuszników przez interfejs providera.

8. **`TeamViewModuleTest`** (3 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.HUD`, identyfikator `"team_view"`.
   - `testSettingGroups`: Weryfikacja 4 grup ustawień (`general`, `markers`, `display`, `behavior`).
   - `testMarkerStyles`: Prawidłowa obsługa stylów `ARROW_DOWN`, `DIAMOND`, `DOT`, `SHIELD`, `CHEVRON`.

9. **`UhcItemScalingControllerTest`** (5 testów):
   - `testDefaultItemScales`: Weryfikacja mnożników dla G-Apple (2.0x), G-Head (2.0x), Pearl (1.8x), Arrow (1.5x), Healing (1.6x), Blocks (1.2x), Buckets (1.4x), Flint & Steel (1.4x).
   - `testGlobalOnlyMode`: Skalowanie wszystkich przedmiotów stałym mnożnikiem globalnym.
   - `testCombinedMode`: Mnożenie skali bazowej przez współczynnik globalny.
   - `testCustomItemRules`: Nadpisywanie skal poprzez pole tekstowe reguł niestandardowych.
   - `testUnscaledItem`: Zwracanie skali 1.0 dla przedmiotów nieobjętych regułami.

10. **`UHCSessionHudModuleTest`** (3 testy):
    - `testModuleMetadata`: Rejestracja w kategorii `Category.SURVIVAL`, identyfikator `"uhc_session_hud"`.
    - `testSettingGroups`: Weryfikacja 6 grup ustawień panelu sesji.
    - `testRenderOnHudRenderer`: Bezpieczne renderowanie na `TestHudRenderer`.

---

## 21. Testy i Weryfikacja 1.7 Visuals (Etap 8A7)

W Etapie 8A7 zaimplementowano 9 nowych dedykowanych klas testów jednostkowych w module `core`, rozszerzając zestaw testów do **189 testów JUnit 5** w 52 klasach testowych:

1. **`OneSevenVisualsModuleTest`** (4 testy):
   - `testModuleMetadata`: Rejestracja w kategorii `Category.VISUAL`, ID `"one_seven_visuals"`.
   - `test13SettingGroupsRegistered`: Weryfikacja obecności wszystkich 13 sekcji konfiguracyjnych w menu Right Shift.
   - `testDefaultPresetIsMinecraft17`: Domyślny preset ustawiony na `MINECRAFT_1_7`.
   - `testResetToDefaults`: Przywrócenie domyślnych wartości parametrów po modyfikacji.

2. **`AnimationProfileManagerTest`** (6 testów):
   - `testPresetsInitialization`: Prawidłowe załadowanie presetów `MODERN`, `MINECRAFT_1_8`, `MINECRAFT_1_7`.
   - `testApplyPreset`: Zaaplikowanie presetu aktualizuje wszystkie powiązane kontrolery i ustawienia.
   - `testSaveAndRenameCustomProfile`: Zapis konfiguracji użytkownika pod unikalną nazwą oraz zmiana nazwy.
   - `testDuplicateAndResetProfile`: Powielanie profilu oraz resetowanie do stanu bazowego.
   - `testExportImportJson`: Eksport do JSON i ponowny import z zachowaniem integralności.
   - `testImportInvalidJsonRejection`: Ścisła walidacja schematu JSON odrzucająca uszkodzone dane.

3. **`ItemTransformManagerTest`** (5 testów):
   - `test12CategoriesInitialized`: Inicjalizacja wszystkich 12 kategorii przedmiotów.
   - `testItemCategoryFromItemId`: Poprawna klasyfikacja mieczy, łuków, wędek, jedzenia, potek i tarcz.
   - `testTransformOffsetsClamping`: Ograniczanie przesunięć, rotacji i skali do bezpiecznych zakresów.
   - `testResetCategoryTransforms`: Reset parametrów pojedynczej kategorii do domyślnych 1.7.
   - `testSerializationRoundtrip`: Serializacja i deserializacja konfiguracji transformacji do formatu Gson.

4. **`BlockingAnimationControllerTest`** (4 testy):
   - `testVisualBlockingPoseCalculation`: Wyliczanie macierzy przesunięcia i rotacji dla stylów 1.7, 1.8 i Lunar.
   - `testBlockhitOverlayMatrix`: Jednoczesna aplikacja zamachu ręki i blokowania mieczem.
   - `testNoGameplayInterference`: Weryfikacja, że kontroler jest w 100% wizualny (brak modyfikacji cooldownu, obrażeń i pakietów).
   - `testLeftHandMirroring`: Poprawne odwrócenie kątów rotacji i przesunięć dla lewej ręki.

5. **`SwingAnimationControllerTest`** (4 testy):
   - `testClassicSwingDuration`: Czas trwania zamachu wynosi 6 ticków zamiast nowoczesnego zmiennego czasu.
   - `testSwingCurveProgress`: Prawidłowa kalkulacja progresu machnięcia opartego na funkcjach potęgowych i sinusie.
   - `testStyles17And18Differences`: Różnice w dynamice i kątach pomiędzy stylami 1.7 i 1.8.
   - `testAttackCooldownIndependence`: Niezależność zamachu wizualnego od czasu odnowienia ataku broni.

6. **`SneakAnimationControllerTest`** (4 testy):
   - `testInstantDownSneakCamera`: Błyskawiczny spadek wysokości oczu kamery przy naciśnięciu kucania.
   - `testSmoothUpTransition`: Płynne unoszenie kamery po zwolnieniu klawisza.
   - `testThirdPersonModelPose`: Wyliczanie kątów pochylenia tułowia i rąk dla widoku TPP.
   - `testHitboxIntegrity`: Gwarancja, że wysokość oczu na serwerze i hitbox gracza nie ulegają modyfikacji.

7. **`DamageVisualControllerTest`** (4 testy):
   - `testRedArmorTintColorCalculation`: Wyliczanie składowych ARGB czerwonego błysku zbroi.
   - `testDamageFlashDurationAndDecay`: Prawidłowe wygaszanie błysku w czasie.
   - `testHitColorModuleCoordination`: Zgodność z modułem `HitColorModule` przy niestandardowym kolorze trafień.
   - `testDisabledRedArmorIn18`: Brak barwienia pancerza w presecie Minecraft 1.8.

8. **`FirstPersonAnimationRendererTest`** (4 testy):
   - `testCompositeMatrixTransform`: Złożenie transformacji kategorii, blokowania, zamachu i jedzenia w jedną macierz.
   - `testItemSwitchingMultiplier`: Skrócenie czasu animacji wyciągania przedmiotu.
   - `testEatingDrinkingBobbing`: Prawidłowa dynamika kołysania podczas jedzenia potraw.
   - `testFishingRodAndBowAngles`: Poprawne ułożenie wędki (-15°) i łuku przy naciąganiu.

9. **`AnimationCompatibilityTest`** (5 testów):
   - `testFreelookAndSnaplookCoexistence`: Brak interferencji z kątami yaw/pitch kamery przy aktywnym 1.7 sneak.
   - `testToggleSneakCoexistence`: Poprawne odczytywanie stanu kucania z kontrolera ruchu.
   - `testResourcePackPriorityModes`: Działanie trybów `USE_RESOURCE_PACK`, `OVERRIDE_RESOURCE_PACK`, `CUSTOM_PRIORITY`.
   - `testShinyPotsCoexistence`: Zachowanie animacji połysku na przekształconych modelach mikstur.
   - `testMultiLoaderParity`: Identyczne wyniki transformacji niezależnie od używanego loadera.

### 21.10. Zbiorcze Wyniki Testów (Stan na Etap 8A7):
- **Testy Java (`:core:test`):** **189 testów w 52 klasach — 100% PASSED** (0 failures, 0 errors, 0 skipped).
- **Testy Launcher TypeScript (`vitest run`):** **178 testów w 15 plikach — 100% PASSED**.
- **Weryfikacja struktury repozytorium (`pnpm run verify:structure`):** **41/41 sprawdzonych ścieżek — 100% OK**.

---

## 22. Zestawy Testów dla Etapu 8A8 (Bedwars Helper)

W Etapie 8A8 dodano 51 nowych testów jednostkowych i integracyjnych w module `:core` (`apps/minecraft-client/core/src/test/java/pl/zombek/client/core/bedwars/`):

1. **`BedwarsGameDetectorTest`** (8 testów):
   - `testScoreboardWaitingTransition`: Rozpoznawanie stanu WAITING i liczby graczy.
   - `testScoreboardStartingTransition`: Przejście do stanu STARTING po wykryciu odliczania.
   - `testBedwarsActiveState`: Pełna detekcja meczu po odnalezieniu linii łóżek.
   - `testSuddenDeathDetection`: Rozpoznawanie fazy Sudden Death z czatu.
   - `testNonBedwarsScoreboard`: Oznaczenie `NOT_IN_BEDWARS` dla scoreboardu z innych minigier.
   - `testModeDetectionSoloAndTeams`: Prawidłowa kategoryzacja trybów Solo, Doubles, 3v3v3v3, 4v4v4v4.
   - `testConfidenceLevels`: Weryfikacja poziomów pewności `HIGH`, `MEDIUM`, `LOW`, `UNKNOWN`.
   - `testDetectorReset`: Czyszczenie stanu przy przejściu między serwerami.

2. **`BedwarsScoreboardParserTest`** (4 testy):
   - `testParseHypixelScoreboardFormat`: Odczyt mapy, trybu, zabójstw, ostatecznych zabić, timerów i stanu łóżek z formatu Hypixel.
   - `testParsePolishScoreboardFormat`: Odczyt statystyk i mapy z polskich prefiksów.
   - `testParseTimersWithRomanNumerals`: Prawidłowe parsowanie rzymskich cyfr ulepszeń (np. Diamond II).
   - `testEmptyOrNullLinesHandledSafely`: Bezpieczna obsługa pustych danych bez rzucania wyjątków.

3. **`BedwarsChatParserTest`** (8 testów):
   - `testHypixelBedDestructionMessage`: Zniszczenie łóżka na Hypixel z przypisaniem sprawcy.
   - `testPolishBedDestructionMessage`: Zniszczenie łóżka na polskich serwerach (obsługa Unicode/polskich znaków).
   - `testHypixelTeamEliminatedMessage`: Eliminacja drużyny po stracie łóżka i graczy.
   - `testPolishTeamEliminatedMessage`: Eliminacja drużyny z polskimi nazwami (np. Żółci).
   - `testFinalKillMessage`: Rejestracja ostatecznego zabójstwa.
   - `testRegularKillMessage`: Rejestracja zwykłego zabójstwa z możliwością odrodzenia.
   - `testMatchVictoryMessage`: Rozpoznanie komunikatu wygranej i aktualizacja licznika zwycięstw.
   - `testArbitraryChatDoesNotTriggerEvents`: Pasywne ignorowanie zwykłych rozmów graczy na czacie.

4. **`BedwarsTeamBoardTest`** (6 testów):
   - `testTeamStatusTransitions`: Zmiana stanu łóżka z ALIVE na DESTROYED i ELIMINATED.
   - `testAlivePlayersDecrement`: Spadek liczby żywych graczy po eliminacji.
   - `testTeamColorPalette`: Poprawność kolorów ARGB i kodów literowych dla 8 zespołów.
   - `testMyTeamHighlighting`: Identyfikacja zespołu gracza `(you)`.
   - `testHideEliminatedTeamsFilter`: Filtrowanie zespołów wyeliminowanych.
   - `testLayoutFormatting`: Poprawność formatowania w stylach COMPACT, DETAILED, HORIZONTAL, VERTICAL, ICON, TEXT.

5. **`BedwarsSessionTrackerTest`** (6 testów):
   - `testRecordKillsAndFinalKills`: Poprawne sumowanie zabójstw i zabić ostatecznych.
   - `testRecordBedsBroken`: Śledzenie liczby zniszczonych łóżek.
   - `testDeduplicationWindow`: Odrzucanie duplikatów zdarzeń w oknie 3000 ms.
   - `testWinLossTracking`: Obliczanie wskaźnika W/L.
   - `testMatchHistoryArchiving`: Zapis do 10 ostatnich meczów z podsumowaniem.
   - `testAutoResetOnNewMatch`: Zerowanie statystyk meczu przy zachowaniu sesji.

6. **`BedwarsEventTrackerTest`** (4 testy):
   - `testEventAdditionAndFifo`: Dodawanie zdarzeń do kolejki z ograniczeniem maksymalnej liczby.
   - `testEventExpiration`: Wygaszanie zdarzeń starszych niż zdefiniowany czas życia.
   - `testEventDeduplication`: Ignorowanie identycznych powiadomień w buforze.
   - `testClearEvents`: Czyszczenie powiadomień przy resecie.

7. **`BedwarsTimerTest`** (4 testy):
   - `testTimerSecondsConversion`: Konwersja formatu MM:SS na sekundy.
   - `testTimerCountdownTick`: Odliczanie czasu w dół co sekundę.
   - `testTimerStateTransitions`: Przejście w stan EXPIRING (< 10 s) i EXPIRED.
   - `testTimerSorting`: Sortowanie timerów wg najbliższego wygaśnięcia.

8. **`BedwarsHelperModuleTest`** (5 testów):
   - `testModuleSettingsRegistration`: Poprawna rejestracja 9 grup ustawień w module.
   - `testRenderWithoutErrors`: Renderowanie modułu w TestHudRenderer bez wyjątków.
   - `testHideWhenNotInBedwars`: Wyciszanie modułu gdy wykrywacz nie znajduje meczu.
   - `testHudPositionCalculation`: Prawidłowe wyliczanie wymiarów i pozycji X/Y z uwzględnieniem kotwicy.
   - `testDefaultsRestoration`: Przywracanie wartości fabrycznych ustawień.

9. **`BedwarsIntegrationTest`** (3 testy):
   - `testEndToEndMatchFlow`: Pełna symulacja meczu (poczekalnia -> start -> zniszczenie łóżka -> final kill -> zwycięstwo).
   - `testCoexistenceWithOtherModules`: Bezkolizyjne renderowanie z Keystrokes, CPS, UHC HUD, Team View i PvP Info.
   - `testTotalAlivePlayersCalculation`: Dokładne wyliczanie pozostałych graczy na podstawie scoreboardu.

### Zbiorcze Wyniki Testów (Stan na Etap 8A8):
- **Testy Java (`:core:test`):** **240 testów w 61 klasach — 100% PASSED** (0 failures, 0 errors, 0 skipped).
- **Testy Launcher TypeScript (`vitest run`):** **178 testów w 15 plikach — 100% PASSED**.
- **Weryfikacja struktury repozytorium (`pnpm run verify:structure`):** **41/41 sprawdzonych ścieżek — 100% OK**.

---

## 23. Weryfikacja i Testy Modułu Hypixel Mods (Etap 8A9)

W Etapie 8A9 opracowano kompleksowy pakiet 50 testów jednostkowych i integracyjnych w pakiecie `pl.zombek.client.core.hypixel`:

1. **`HypixelServerDetectorTest`** (6 testów):
   - `testHypixelDirectAddressDetection`: Rozpoznawanie adresu `hypixel.net`.
   - `testSubdomainDetection`: Rozpoznawanie subdomen `mc.hypixel.net`, `test.hypixel.net`.
   - `testSingleplayerDetection`: Klasyfikacja trybu jednoosobowego do `SINGLEPLAYER`.
   - `testOtherServerDetection`: Odróżnianie obcych serwerów (`other.server.com`).
   - `testForceModes`: Prawidłowe działanie trybów `FORCE_ON` i `FORCE_OFF`.
   - `testDisconnectCleanup`: Reset stanu detektora po rozłączeniu.

2. **`HypixelSafetyControllerTest`** (5 testów):
   - `testStrictSafetyAllowsCosmetics`: Potwierdzenie zezwolenia na kategorie `COSMETIC`, `LOCAL_UI`, `LOCAL_NOTIFICATION`, `LOCAL_AUTOCOMPLETE`.
   - `testStrictSafetyBlocksAutomation`: Bezwzględna blokada `GAMEPLAY_AUTOMATION` i `NETWORK_MODIFICATION`.
   - `testBlockReasonInPolish`: Sprawdzenie czytelnych komunikatów uzasadniających blokadę w języku polskim.
   - `testCustomPresetFlexibility`: Zmiana polityki w profilu `CUSTOM`.
   - `testDisclaimerPresence`: Weryfikacja obecności oficjalnej klauzuli ostrzegawczej.

3. **`HypixelChatControllerTest`** (6 testów):
   - `testChannelPrefixFormatting`: Skracanie prefiksów `[Guild]` -> `[G]`, `[Party]` -> `[P]`.
   - `testPrefixStyles`: Prawidłowe renderowanie stylów `BRACKETED`, `COLON`, `MINIMAL`, `PLAIN`.
   - `testColorAndFormattingPreservation`: Nienaruszalność kodów formatowania Minecrafta.
   - `testGuildMotdSuppression`: Bezpieczne wyciszanie wiadomości MOTD gildii.
   - `testGuildTagOnTabRemoval`: Usuwanie przyrostków gildii z tablisty.
   - `testTimestampPrepending`: Dodawanie znaczników czasu w formacie `[HH:mm]`.

4. **`HypixelChatFilterTest`** (6 testów):
   - `testBuiltinCategoriesMatching`: Filtrowanie typowych komunikatów (Lobby Join, Mystery Box, Tipping).
   - `testCustomExactAndContainsRules`: Działanie reguł `EXACT` i `CONTAINS`.
   - `testRegexRuleMatching`: Poprawne dopasowywanie reguł `REGEX`.
   - `testReDoSLengthLimit`: Odrzucanie wyrażeń regularnych dłuższych niż 100 znaków.
   - `testReDoSCatastrophicBacktrackingGuard`: Blokowanie niebezpiecznych zagnieżdżonych kwantyfikatorów `(a+)+`.
   - `testEmergencyDisableAllFilters`: Działanie globalnego wyłącznika awaryjnego.

5. **`HypixelAutocompleteTest`** (6 testów):
   - `testDictionarySize`: Weryfikacja katalogu 22 komend.
   - `testPrefixMatching`: Dopasowywanie propozycji po wpisaniu przedrostka komendy.
   - `testKeyboardNavigation`: Zmiana zaznaczonej propozycji (strzałki w górę / w dół).
   - `testAcceptSuggestion`: Wstawianie wybranej komendy klawiszem Tab / Enter.
   - `testDescriptionAndSyntax`: Sprawdzenie obecności czytelnej składni i opisu.
   - `testZeroNetworkPackets`: Gwarancja braku wysyłania pakietów sieciowych.

6. **`HypixelLevelDisplayTest`** (4 testy):
   - `testLevelExtractionFromChat`: Wyciąganie gwiazdek z wiadomości `[120★]`.
   - `testLevelFormatting`: Estetyczne formatowanie odznaki poziomu.
   - `testStrictSafetyLevelLock`: Blokada pobierania ukrytych poziomów na Hypixelu.
   - `testInvalidLevelTextGracefulFallback`: Odporność na brak poziomu w tekście.

7. **`HypixelManualSuggestionsTest`** (5 testów):
   - `testSuggestionCreationOnMatchEnd`: Pojawienie się propozycji GG po zakończeniu meczu.
   - `testClipboardCopyAction`: Poprawne kopiowanie treści do schowka (`copyToClipboard()`).
   - `testZeroChatMessageSent`: Gwarancja braku wywołania `sendChatMessage()`.
   - `testCardLifetimeAndDismiss`: Wygaszanie karty po upływie zadanego czasu.
   - `testCustomTemplate`: Użycie niestandardowego szablonu użytkownika.

8. **`HypixelNotificationControllerTest`** (5 testów):
   - `testToastAddition`: Dodanie powiadomienia do kolejki.
   - `testFadeAlphaAnimation`: Obliczanie płynnego współczynnika alpha (fade-in / fade-out).
   - `testExpiration`: Usunięcie powiadomienia po zadanym czasie wyświetlania.
   - `testNotificationTypes`: Obsługa typów Party, Friend, Guild, Match.
   - `testSoundTrigger`: Odtwarzanie subtelnego dźwięku interfejsu.

9. **`HypixelModsModuleTest`** (5 testów):
   - `testNineSettingGroupsRegistered`: Weryfikacja kompletności 9 grup ustawień w module.
   - `testCategoryIsOther`: Potwierdzenie przypisania do `ModuleCategory.OTHER` (`Social / Server`).
   - `testHudElementIdAndVisibility`: Prawidłowe zgłaszanie identyfikatora HUD i widoczności.
   - `testRestoreDefaults`: Bezpieczne przywracanie wartości domyślnych.
   - `testConfigSerialization`: Zapis i odczyt stanu modułu w `modules.json`.

10. **`HypixelIntegrationTest`** (2 testy):
    - `testFullLifecycleOnHypixelConnection`: Przejście cyklu życia (połączenie z Hypixelem -> aktywacja Strict Safety -> filtrowanie czatu -> sugestia GG -> rozłączenie).
    - `testCoexistenceWithBedwarsHelper`: Zgodne współistnienie modułów Bedwars Helper i Hypixel Mods.

### Zbiorcze Wyniki Testów (Stan na Etap 8A9):
- **Testy Java (`:core:test`):** **290 testów w 71 klasach — 100% PASSED** (0 failures, 0 errors, 0 skipped).
- **Testy Launcher TypeScript (`vitest run`):** **178 testów w 15 plikach — 100% PASSED**.
- **Weryfikacja struktury repozytorium (`pnpm run verify:structure`):** **41/41 sprawdzonych ścieżek — 100% OK**.

---

## 24. Testy i Weryfikacja Modułów Informacyjnych HUD (Etap 8B1)

W ramach etapu **Etap 8B1: Universal HUD Information** zaimplementowano 44 nowe testy jednostkowe i integracyjne w module `:core` (`apps/minecraft-client/core/src/test/java/pl/zombek/client/core/info/`):

1. **`DimensionTypeTest`** (2 testy):
   - `testFromStringResolution`: Weryfikacja rozpoznawania wymiarów Minecraft z identyfikatorów (`minecraft:overworld`, `minecraft:the_nether`, `minecraft:the_end`) oraz wymiarów niestandardowych.
   - `testDimensionProperties`: Weryfikacja współczynników skali Nether $8:1$ i właściwości wymiarów.

2. **`CompassDirectionTest`** (3 testy):
   - `testFromYawMapping`: Weryfikacja poprawnego mapowania 8 sektorów świata (N, NE, E, SE, S, SW, W, NW) dla standardowych kątów yaw.
   - `testAngleWrappingAndExtremes`: Weryfikacja odporności na kąty ujemne (np. $-90^\circ$) oraz przekraczające $360^\circ$ (np. $720^\circ$, $405^\circ$).
   - `testShortestAngleDistance`: Test wyliczania minimalnego dystansu kątowego na granicy $359^\circ \leftrightarrow 0^\circ$ (bez przeskoków).

3. **`FpsHistoryTrackerTest`** (5 testów):
   - `testInitialState`: Weryfikacja stanu początkowego bufora i wartości domyślnych.
   - `testSampleAdditionAndAverages`: Dodawanie próbek klatek i wyliczanie średniej w czasie $O(1)$.
   - `testMinAndMaxFpsCalculations`: Wyznaczanie wartości minimalnej (1% low) i maksymalnej z bufora.
   - `testRingBufferOverflow`: Test zachowania bufora kołowego po przepełnieniu (zastępowanie najstarszych próbek).
   - `testReset`: Prawidłowe zerowanie bufora i sumy próbek.

4. **`PingHistoryTrackerTest`** (4 testy):
   - `testSingleplayerHandling`: Zwracanie wartości ujemnej (-1) dla świata jednoosobowego i statusu `UNAVAILABLE`.
   - `testMultiplayerPingCalculations`: Zbieranie próbek opóźnienia i kalkulacja średniej.
   - `testThresholdStatusGrading`: Test klasyfikacji jakości łącza (`GOOD` $\le 60$ ms, `MEDIUM` $\le 120$ ms, `HIGH` $\le 250$ ms, `CRITICAL` $> 250$ ms).
   - `testRingBufferOverflowAndReset`: Test przepełnienia i czyszczenia bufora pingu.

5. **`MemoryUsageTrackerTest`** (3 testy):
   - `testThrottledPolling`: Test mechanizmu dławienia zapytań (throttling 500 ms) — brak odpytywania Runtime przy zbyt częstych wywołaniach.
   - `testMemoryMetricsCalculations`: Weryfikacja obliczania used, allocated, max memory oraz procentu zapełnienia sterty.
   - `testZeroGarbageCollectionInvocations`: Gwarancja braku wymuszania `System.gc()`.

6. **`HudComponentModelTest`** (3 testy):
   - `testAbsolutePositionCalculation`: Poprawne obliczanie pozycji na ekranie dla różnych kotwic (`TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`, `TOP_CENTER`, `BOTTOM_CENTER`, `CENTER`).
   - `testViewportClamping`: Zabezpieczenie przed wysunięciem komponentu poza granice ekranu gry.
   - `testHitTesting`: Weryfikacja metody `contains(mouseX, mouseY)` pod kątem przyszłego edytora HUD.

7. **`CoordinatesModuleTest`** (5 testów):
   - `testAllFourCoordinateFormats`: Formatowanie tekstu w stylach `FULL`, `BLOCK`, `COMPACT` i `SEPARATE`.
   - `testNetherAndOverworldConversion`: Prawidłowość przeliczania współrzędnych $X_{\text{nether}} = \lfloor X/8 \rfloor$ i $X_{\text{overworld}} = \lfloor X \times 8 \rfloor$.
   - `testDecimalPrecisionSettings`: Działanie zaokrąglenia dla precyzji 0, 1, 2 i 3 miejsc po przecinku.
   - `testChunkAndRegionCalculations`: Wyliczanie koordynatów chunka i nazwy pliku regionu `.mca`.
   - `testConfigDefaultsAndRestore`: Bezpieczne przywracanie wartości domyślnych w Right Shift GUI.

8. **`DirectionHudModuleTest`** (5 testów):
   - `testCompassStyles`: Weryfikacja 4 stylów kompasu (`CLASSIC`, `MINIMAL`, `DETAILED`, `CIRCULAR`).
   - `testTapeInterpolationAcrossZero`: Bezszwowe przesuwanie taśmy przy przejściu przez $0^\circ/360^\circ$.
   - `testTeamMarkerCalculations`: Prawidłowe obliczanie azymutu i widoczności sojusznika na taśmie kompasowej.
   - `testSettingsRegistration`: Weryfikacja kompletności grup ustawień (general, display, colors, layout).
   - `testHudElementVisibility`: Widoczność w potoku renderowania HUD.

9. **`FpsModuleTest`** (5 testów):
   - `testSettingGroups`: Kompletność grup parametrów (general, metrics, graph, colors, layout).
   - `testMetricsRenderingLogic`: Warunkowe włączanie current, average, min, max FPS.
   - `testMiniGraphDimensions`: Kalkulacja wymiarów i proporcji mini-wykresu stabilności.
   - `testStatisticsResetTrigger`: Działanie przełącznika zerującego statystyki sesji.
   - `testCategoryIsHud`: Potwierdzenie przypisania do kategorii `ModuleCategory.HUD`.

10. **`PingModuleTest`** (4 testy):
    - `testSingleplayerDisplayAndAutoHide`: Ukrywanie lub wyświetlanie `N/A` w trybie jednoosobowym w zależności od konfiguracji.
    - `testMultiplayerPingRendering`: Poprawne renderowanie prefiksu, wartości w ms i sufiksu.
    - `testDynamicThresholdColoring`: Przypisywanie barw Good, Medium, High, Critical.
    - `testRestoreDefaultsAndConfig`: Zapis konfiguracji do `modules.json` i przywracanie wartości domyślnych.

11. **`MemoryUsageTrackerTest`** (3 testy):
    - `testMemoryUnitFormatting`: Prawidłowe formatowanie jednostek `MB`, `GB` oraz automatycznego wyboru `AUTO`.
    - `testMemoryBarDimensionsAndPercentages`: Kalkulacja szerokości paska i progów ostrzegawczych (75% / 90%).
    - `testSettingsAndLayout`: Sprawdzenie rejestracji grup ustawień i punktów zakotwiczenia.

12. **`UniversalHudIntegrationTest`** (2 testy):
    - `testAllFiveInfoModulesRegistered`: Potwierdzenie obecności wszystkich 5 modułów informacyjnych w `ModuleManager` i `HudElementRegistry`.
    - `testMultiLoaderInfoProvidersCoexistence`: Zgodne współistnienie modułów informacyjnych z dotychczasowymi modułami PvP, Visual i Survival na wszystkich loaderach.

### Zbiorcze Wyniki Testów (Stan na Etap 8B2):
- **Testy Java (`:core:test`):** **370 testów w 89 klasach — 100% PASSED** (0 failures, 0 errors, 0 skipped).
- **Testy Launcher TypeScript (`vitest run`):** **178 testów w 15 plikach — 100% PASSED**.
- **Weryfikacja struktury repozytorium (`pnpm run verify:structure`):** **41/41 sprawdzonych ścieżek — 100% OK**.

---

## 25. Testy Scoreboard, Potion Effects, Saturation, Boss Bar & Rozwiązywania Kolizji HUD (Etap 8B2)

W etapie 8B2 wdrożono 36 nowych dedykowanych testów jednostkowych i integracyjnych w module `core` (`apps/minecraft-client/core/src/test/java/pl/zombek/client/core/`):

### 25.1. Testy Modułów HUD (`pl.zombek.client.core.modules.hud`)

1. **`ScoreboardModuleTest`** (8 testów):
   - `testModuleMetadataAndCategory`: Potwierdzenie przypisania do `ModuleCategory.HUD`, unikalne ID `"scoreboard"`, ikona i opis.
   - `testObjectiveRendering`: Poprawne renderowanie nagłówka celu i linii scoreboardu z dostawcy danych.
   - `testCustomTitleOverride`: Nadpisywanie tytułu serwerowego własnym tekstem przy aktywnej fladze `custom_title_enabled`.
   - `testHideScoresSetting`: Ukrywanie czerwonych liczb punktowych po włączeniu `hide_scores`.
   - `testScoreboardStyles`: Weryfikacja 5 stylów wizualnych (`VANILLA`, `MINIMAL`, `MODERN`, `GLASS`, `CUSTOM`).
   - `testPreviewModeFallback`: Renderowanie przykładowego scoreboardu `[PREVIEW]` z nagłówkiem `ZOMBEKCLIENT` przy braku aktywnego celu.
   - `testMaxLinesAndTruncation`: Ograniczenie do maksymalnej liczby linii (`max_lines`) oraz przycinanie zbyt długich wierszy do szerokości z trzykropkiem `...`.
   - `testRestoreDefaultsAndConfig`: Zapis konfiguracji do JSON i przywracanie wartości domyślnych.

2. **`PotionEffectsModuleTest`** (7 testów):
   - `testModuleMetadataAndCategory`: Weryfikacja ID `"potion_effects"`, kategorii `HUD` i 5 grup ustawień.
   - `testVanillaOverlayOption`: Weryfikacja opcji `hide_vanilla_hud`.
   - `testDurationAndAmplifierFormatting`: Poprawność formatowania czasu `MM:SS` oraz rzymskich cyfr poziomów (I, II, III...).
   - `testSortModes`: Test 6 algorytmów sortowania (`DURATION_SHORTEST`, `DURATION_LONGEST`, `BENEFICIAL_FIRST`, `HARMFUL_FIRST`, `ALPHABETICAL`, `CUSTOM_ORDER`).
   - `testLayoutModes`: Weryfikacja 5 trybów układu (`CLASSIC`, `COMPACT`, `DETAILED`, `ICONS_ONLY`, `CUSTOM`).
   - `testExpiringAlertPulse`: Detekcja efektów wygasających poniżej progu czasowego (`expiring_threshold`).
   - `testPreviewModeFallback`: Podgląd efektów w Right Shift GUI przy braku aktywnych mikstur.

3. **`SaturationModuleTest`** (7 testów):
   - `testModuleMetadataAndCategory`: Weryfikacja ID `"saturation"` i 6 grup ustawień (`general`, `hunger`, `saturation`, `appearance`, `colors`, `layout`).
   - `testNormalHungerAndSaturationDisplay`: Prezentacja wartości głodu i nasycenia w skali 0–20.
   - `testUnavailableSaturationPolicy`: **Rygorystyczny test Fair Play** — weryfikacja wyświetlania komunikatu `Niedostępne` / `Unavailable`, gdy serwer nie przesyła wartości nasycenia do klienta (brak zgadywania).
   - `testLowHungerWarning`: Ostrzeżenie wizualne przy poziomie głodu $\le 6$.
   - `testLayoutModes`: Weryfikacja układów `CLASSIC`, `COMPACT`, `DETAILED`, `MINIMAL`.
   - `testPreviewModeFallback`: Demonstracyjny podgląd głodu i nasycenia w menu Right Shift.
   - `testExhaustionDisplay`: Warunkowe wyświetlanie wyczerpania (0–4.0) w układzie `DETAILED`.

4. **`BossBarModuleTest`** (7 testów):
   - `testModuleMetadataAndCategory`: Weryfikacja ID `"boss_bar"`, kategorii `HUD` i 5 grup ustawień.
   - `testActiveBossBarsRendering`: Renderowanie paska bossa z poprawną nazwą, procentem i kolorem.
   - `testMultipleBossBarsStacking`: Poprawne pionowe układanie wielu bossów z zachowaniem odstępu `boss_spacing`.
   - `testBossBarStyles`: Weryfikacja 6 stylów (`VANILLA`, `CLASSIC`, `MODERN`, `MINIMAL`, `SEGMENTED`, `CUSTOM`).
   - `testSmoothAnimationStep`: Liniowa interpolacja postępu paska (`lerp`) w krokach czasowych.
   - `testColorCustomization`: Dynamiczne dopasowanie barw według barwy paska Mojang lub koloru własnego.
   - `testPreviewModeFallback`: Renderowanie paska demonstracyjnego `[PREVIEW]` z Ender Dragonem w menu konfiguracji.

### 25.2. Testy Geometrii i Rozwiązywania Kolizji HUD (`pl.zombek.client.core.hud.component`)

5. **`HudPriorityResolverTest`** (5 testów):
   - `testPriorityConstants`: Weryfikacja hierarchii priorytetów (`Boss Bar 100 > Scoreboard 80 > Potion Effects 60 > Saturation 50 > Default 0`).
   - `testAabbIntersection`: Test przecięcia prostokątów ograniczających AABB (nakładanie, rozłączność, styczność).
   - `testCollisionResolutionTopAnchored`: Test odpychania w dół kolidującego elementu o niższym priorytecie dla kotwic górnych ($Y_{\text{new}} = Y_{\text{prev}} + \text{height} + \text{spacing}$).
   - `testCollisionResolutionBottomAnchored`: Test odpychania w górę kolidującego elementu dla kotwic dolnych ($Y_{\text{new}} = \text{screenHeight} - Y_{\text{prev}} + \text{spacing}$).
   - `testNonOverlappingElementsUnchanged`: Potwierdzenie, że elementy niekolidujące przestrzennie zachowują swoje pierwotne współrzędne.

### 25.3. Testy Integracyjne Core (`pl.zombek.client.core`)

6. **`Etap8B2IntegrationTest`** (2 testy):
   - `testAllFourModulesRegisteredInCore`: Potwierdzenie rejestracji wszystkich 4 modułów w `ZombekCore.init()`, ich obecności w `ModuleManager` i `HudElementRegistry`.
   - `testModulesRenderPipelineAndPriority`: Sprawdzenie poprawnego wywołania metody `render()` dla każdego z 4 modułów bez rzucania wyjątków oraz zachowania porządku warstw w `HudPriorityResolver`.

---

## 26. Testy Modułów Czasowych i Silnika Czasu (Etap 8B3)

W Etapie 8B3 dodano 40 nowych testów jednostkowych i integracyjnych w module `:core`, zwiększając liczbę testów JUnit z 370 do 410 (100% powodzenia).

### 26.1. Testy Silnika Czasu i Trwałości (`pl.zombek.client.core.time`)

1. **`ZombekTimeServiceTest`** (7 testów):
   - `testWallClockAndTimeFormatting`: Sprawdzenie pobierania czasu rzeczywistego w formacie 24h i 12h AM/PM.
   - `testWorldTimeToMinecraftTimeConversion`: Konwersja ticków (0..24000) na czas gry Minecraft (godziny i minuty) oraz fazy pory dnia (DAWN, MORNING, NOON, AFTERNOON, DUSK, NIGHT, MIDNIGHT).
   - `testSunriseAndSunsetPhases`: Prawidłowe oznaczanie flag wschodu i zachodu słońca.
   - `testTimeRollbackDetection`: Detekcja cofnięcia czasu ($\text{lastGameTime} - \text{currentGameTime} > 24000$) i flaga ostrzegawcza `isTimeResetDetected()`.
   - `testSessionTracking`: Rejestracja startu i końca sesji świata, obliczanie czasu trwania, dodawanie do historii sesji.
   - `testStopwatchProfileManagement`: Tworzenie, pobieranie i niezależność profili stopera (`Speedrun`, `Building`, `Mining`, `Custom`).
   - `testPlaytimeStatsAggregation`: Agregacja statystyk czasu gry: średni czas sesji, najdłuższa sesja, łączny czas, liczba sesji.

2. **`StopwatchProfileTest`** (5 testów):
   - `testInitialStateAndStartPauseResume`: Weryfikacja maszyny stanów: `STOPPED` -> `RUNNING` -> `PAUSED` -> `RUNNING`.
   - `testMonotonicTimingPrecision`: Sprawdzenie niezależności pomiaru od FPS i tickrate silnika gry na bazie nanosekundowego zegara `ITimeSource`.
   - `testLapRecordingAndDeltaCalculation`: Zapisywanie międzyczasów (okrążeń), wyliczanie delty $\Delta = \text{lap}_N - \text{lap}_{N-1}$ (zysk / strata).
   - `testBestAndAverageLapTimes`: Wyliczanie najlepszego okrążenia i średniej arytmetycznej czasów okrążeń.
   - `testResetClearsStateAndLaps`: Pełne czyszczenie stopera (stan `STOPPED`, czas 0, pusta lista okrążeń).

3. **`TimePersistenceTest`** (2 testy):
   - `testSaveAndLoadCycle`: Atomowy zapis `time_data.json`, odczyt na nowej instancji `ZombekTimeService`, weryfikacja łącznego czasu gry, sesji i profilu stopera.
   - `testCorruptFileRecovery`: Obsługa uszkodzonego pliku JSON — automatyczne utworzenie kopii zapasowej `.bak` i bezpieczny start z domyślnymi wartościami bez wyjątku.

### 26.2. Testy Modułów HUD (`pl.zombek.client.core.modules.hud`)

4. **`ClockModuleTest`** (7 testów):
   - `testModuleMetadataAndCategory`: Weryfikacja ID `"clock"`, kategorii `HUD` i 5 grup ustawień.
   - `testDigitalModeFormatting`: Formatowanie czasu cyfrowego (24h vs 12h, z sekundami i bez).
   - `testMinecraftTimeMode`: Formatowanie czasu świata Minecraft wraz z nazwą fazy i statusem słońce/księżyc.
   - `testDateFormatting`: Weryfikacja wzorców daty (`YYYY_MM_DD`, `DD_MM_YYYY`, `MM_DD_YYYY`, `SHORT_SLASH`, `MONTH_NAME`).
   - `testDualMode`: Złożony dwuwierszowy układ prezentujący jednocześnie czas rzeczywisty oraz czas Minecrafta.
   - `testPreviewFallback`: Poprawne renderowanie podglądu z etykietą `[PREVIEW]` w trybie edycji GUI.
   - `testCustomFormat`: Szablon użytkownika ze znacznikami `{time}`, `{mc_time}`, `{phase}`, `{date}`.

5. **`DayCounterModuleTest`** (6 testów):
   - `testModuleMetadataAndCategory`: Weryfikacja ID `"day_counter"`, kategorii `HUD` i 5 grup ustawień.
   - `testWorldDayCalculationFormula`: Weryfikacja matematyczna $\lfloor \text{gameTime}/24000 \rfloor + 1$ (tick 0 -> Day 1, tick 24000 -> Day 2, tick 240000 -> Day 11).
   - `testDisplayFormats`: Weryfikacja formatów `DAY_SPACE` (`Day 11`), `DAY_COLON` (`Day: 11`), `COMPACT` (`D11`).
   - `testStrictUnavailablePolicy`: **Fair Play** — weryfikacja wyświetlania `Day: N/A` na serwerach multiplayer bez synchronizacji pakietu czasu (zero symulacji).
   - `testTimeResetAndManualOffsetIndicators`: Oznaczanie flag diagnostycznych `[Reset]` oraz `(Manual)`.
   - `testPreviewFallback`: Podgląd `Day 127 [PREVIEW]` w Right Shift GUI.

6. **`PlaytimeModuleTest`** (6 testów):
   - `testModuleMetadataAndCategory`: Weryfikacja ID `"playtime"`, kategorii `HUD` i 4 grup ustawień.
   - `testClientSessionAndWorldSessionPlaytime`: Rozróżnienie czasu od uruchomienia klienta i czasu bieżącej sesji w świecie.
   - `testSingleplayerStatsAndMultiplayerUnavailable`: Pobieranie statystyk SP ze wskaźnika ticków oraz restrykcyjne `N/A` w Multiplayer.
   - `testFormattingOptions`: Poprawność formatów `HH_MM_SS`, `DD_HH_MM_SS`, `HUMAN_READABLE`, `COMPACT`.
   - `testSessionHistoryRecordingAndPrivacy`: Weryfikacja anonimizacji adresów serwerów (opt-in) i limitu 100 sesji.
   - `testPreviewFallback`: Podgląd `Playtime: 01:23:45 [PREVIEW]` w menu GUI.

7. **`StopwatchModuleTest`** (5 testów):
   - `testModuleMetadataAndCategory`: Weryfikacja ID `"stopwatch"`, kategorii `HUD` i 3 grup ustawień.
   - `testProfileSelectionAndState`: Przełączanie profili `SPEEDRUN`, `BUILDING`, `MINING`, `CUSTOM` i prezentacja stanów `RUNNING`, `PAUSED`, `STOPPED`.
   - `testDisplayLinesWithLaps`: Prezentacja głównego czasu stopera oraz listy ostatnich międzyczasów z deltami.
   - `testMillisecondsToggle`: Włączanie/wyłączanie prezentacji milisekund (`.xxx`).
   - `testKeybindActions`: Działanie akcji klawiszowych: `toggle` (start/pauza), `lap` (okrążenie), `reset` (zerowanie).

### 26.3. Testy Integracyjne Core (`pl.zombek.client.core`)

8. **`Etap8B3IntegrationTest`** (2 testy):
   - `testAllFourModulesRegisteredInCore`: Potwierdzenie automatycznej rejestracji `ClockModule`, `DayCounterModule`, `PlaytimeModule` i `StopwatchModule` w `ZombekCore.init()` i `ModuleManager`.
   - `testModulesRenderPipelineAndPriorityCollisions`: Bezpieczne wywołanie `render(mockRenderer)` dla każdego modułu oraz rozwiązanie kolizji przestrzennej w `HudPriorityResolver` przy tym samym zakotwiczeniu (Clock priorytet 40 pozostaje na Y=5, Day Counter priorytet 35 przesuwa się w dół).

---

## 27. Weryfikacja i Testy Modułów Item Counter, Item Tracker i Server Address (Etap 8B4)

W Etapie 8B4 opracowano 7 nowych zestawów testów jednostkowych i integracyjnych w module `:core`:

### 27.1. Testy Podsystemu Inwentarza (`pl.zombek.client.core.inventory`)

1. **`InventorySnapshotServiceTest`** (4 testy):
   - `testStackAndTotalCounting`: Zliczanie sumarycznych ilości i pełnych stacków w migawce.
   - `testSourceFiltering`: Poprawne filtrowanie po źródłach (`HOTBAR`, `MAIN_INVENTORY`, `OFFHAND`, `ARMOR`).
   - `testSlotTransferDetectedWithoutNetGainOrLoss`: Detekcja przesunięć między slotami jako `pure slot transfer` o zerowej sumie netto.
   - `testTrueItemAcquisitionAndLoss`: Prawidłowe wyliczanie delty netto przy rzeczywistym zysku i stracie.

### 27.2. Testy Menedżera Śledzenia Przedmiotów (`pl.zombek.client.core.itemtracker`)

2. **`ItemTrackerManagerTest`** (5 testów):
   - `testSessionStateAndPausing`: Zarządzanie sesją, stan pauzy i wznawiania.
   - `testUnknownChangeConfidenceWhenSourceNotVerified`: Weryfikacja reguły Fair Play — niepotwierdzona zmiana inwentarza otrzymuje typ `UNKNOWN_CHANGE` i poziom `UNKNOWN`.
   - `testVerifiedActionPromotedToConfirmed`: Potwierdzona akcja gracza promuje zdarzenie do poziomu `CONFIRMED`.
   - `testInternalTransferDoesNotEmitGainOrLoss`: Ruchy wewnątrzekwipunkowe nie generują zdarzeń w feedzie.
   - `testHistoryExportToJson`: Eksport historii do formatu JSON z weryfikacją pól.

### 27.3. Testy Kontrolera Prywatności i Adresu Serwera (`pl.zombek.client.core.serveraddress`)

3. **`ServerAddressPrivacyControllerTest`** (7 testów):
   - `testFormatHostNormal`: Wyświetlanie surowego hosta w trybie normalnym.
   - `testFormatHostHiddenAndStreamer`: Zastępowanie hosta przez `[HIDDEN]` w trybach ochrony i Streamer Mode.
   - `testFormatHostMasked`: Maskowanie środka hosta (`play.example.net` $\to$ `pla****.net`, krótkie domeny).
   - `testFormatHostUnavailable`: Bezpieczny fallback przy pustym adresie.
   - `testFormatWorldNameSingleplayer`: Ochrona nazwy świata jednoosobowego w Singleplayer.
   - `testServerAliasManager`: Zarządzanie lokalnymi aliasami, kolorami ARGB i normalizacją portu `:25565`.
   - `testCopyToClipboardDoesNotCrash`: Bezpieczeństwo wywołań schowka systemowego w środowiskach bezgłowych (headless).

### 27.4. Testy Modułów HUD (`pl.zombek.client.core.modules.hud`)

4. **`ItemCounterModuleTest`** (5 testów):
   - `testMetadataAndCategory`: ID `"item_counter"`, kategoria `HUD`, 6 grup ustawień.
   - `testCountingAndSourceFiltering`: Zliczanie ilości perły/jabłka z filtrowaniem źródeł Hotbar i Main.
   - `testQuantityAlertThresholds`: Ewaluacja progów ostrzegawczych (warning i critical).
   - `testPreviewFallbackWhenWorldNotLoaded`: Przełączanie w tryb `[PREVIEW]` z danymi przykładowymi.
   - `testWatchedItemManagement` & `testRestoreDefaults`: Dodawanie, usuwanie śledzonych przedmiotów i reset ustawień.

5. **`ItemTrackerModuleTest`** (5 testów):
   - `testMetadataAndCategory`: ID `"item_tracker"`, kategoria `HUD`, 6 grup ustawień.
   - `testPreviewFallbackWhenWorldNotLoaded`: Podgląd `[PREVIEW]` w menu głównym.
   - `testFeedFilteringByEventType`: Filtrowanie feedu po typach zdarzeń (np. wykluczenie `DROPPED`).
   - `testStatisticsLayout`: Poprawne formatowanie podsumowania statystyk sesji (`+3 / -1`).
   - `testRestoreDefaults`: Przywracanie domyślnych wartości ustawień.

6. **`ServerAddressModuleTest`** (7 testów):
   - `testMetadataAndCategory`: ID `"server_address"`, kategoria `HUD`, 5 grup ustawień.
   - `testFullAddressDisplayIsOffByDefault`: Wyświetlanie pełnego adresu jest **domyślnie wyłączone** (`show_server_address: false`).
   - `testSingleplayerNeverShowsFakeIp`: W Singleplayer wyświetla "Singleplayer", **nigdy nie generując 127.0.0.1**.
   - `testMultiplayerAddressDisplayAndMasking`: Maskowanie hosta multiplayer (`pla****.net`).
   - `testCustomAliasSubstitution`: Zastąpienie hosta lokalnym aliasem użytkownika.
   - `testPreviewFallbackWhenDisconnected`: Podgląd `[PREVIEW]` w stanie rozłączenia.
   - `testRestoreDefaults`: Resetowanie poziomu prywatności do `MASK_HOST`.

### 27.5. Testy Integracyjne Core (`pl.zombek.client.core`)

7. **`Etap8B4IntegrationTest`** (2 testy):
   - `testAllThreeModulesRegisteredInCore`: Potwierdzenie automatycznej rejestracji `item_counter`, `item_tracker` i `server_address` w `ZombekCore.init()` i `ModuleManager`.
   - `testModulesRenderPipelineAndPriorityCollisions`: Bezpieczne wywołanie `render(mockRenderer)` dla każdego modułu oraz rozwiązanie kolizji przestrzennej w `HudPriorityResolver` (Item Counter priorytet 32, Item Tracker priorytet 29, Server Address priorytet 24).

---

## 28. Weryfikacja i Testy Modułów NameTags, Tab Editor i Serwisu Informacji o Graczach (Etap 8B5)

W Etapie 8B5 opracowano 8 nowych zestawów testów jednostkowych i integracyjnych w module `:core`, zwiększając łączną liczbę testów do **482** (wszystkie zakończone sukcesem, 0 niepowodzeń):

### 28.1. Testy Menedżera Przyjaciół (`pl.zombek.client.core.friends`)

1. **`NameTagFriendManagerTest`** (8 testów):
   - `testAddAndRemoveFriend`: Dynamiczne dodawanie i usuwanie znajomego po UUID oraz nicku.
   - `testIsFriendLookupByUuidAndNick`: Sprawdzanie relacji z priorytetem UUID i bezpiecznym fallbackiem na nick.
   - `testFriendCustomAliasAndColor`: Przypisywanie i odczyt lokalnego aliasu oraz koloru ARGB.
   - `testPersistenceSaveAndLoad`: Weryfikacja zapisu i odczytu konfiguracji z pliku `config/zombekclient/friends.json`.
   - `testGetDisplayNameFormatted`: Formatowanie display name z lokalnym aliasem (`Nick [Alias]`).
   - `testUpdateFriendNotes`: Aktualizacja i persystencja notatek gracza.
   - `testClearAllFriends`: Bezpieczne czyszczenie listy znajomych w testach i przy zmianie profilu.
   - `testInvalidUuidHandling`: Odporność na niepoprawne formaty UUID bez rzucania niekontrolowanych wyjątków.

### 28.2. Testy Centralnego Serwisu Danych Graczy (`pl.zombek.client.core.playerinfo`)

2. **`ZombekPlayerInfoServiceTest`** (8 testów):
   - `testFallbackProviderWhenNoNativeRegistered`: Bezpieczny fallback do pustych danych przy braku natywnego loadera.
   - `testRegisterCustomProvider`: Rejestracja dedykowanego dostawcy w `PlayerInfoDataProviderRegistry`.
   - `testPlayerInfoCachingTTL`: Buforowanie odpowiedzi z czasem życia TTL 250 ms (zapobieganie przeciążeniu renderera).
   - `testCacheInvalidationOnWorldLeave`: Automatyczne czyszczenie bufora pamięci podręcznej po opuszczeniu świata.
   - `testCacheInvalidationOnTick`: Sprawdzanie wygaśnięcia TTL w `onClientTick()`.
   - `testDataAvailabilityModel`: Weryfikacja dostępności zdrowia (`VANILLA_SCOREBOARD`, `ACTION_BAR`, `ESTIMATED`, `UNAVAILABLE`) i pingu (`EXACT`, `RANGE`, `UNAVAILABLE`).
   - `testPlayerArmorDataIntegrity`: Poprawność mapowania slotów zbroi (`HELMET`, `CHESTPLATE`, `LEGGINGS`, `BOOTS`).
   - `testGetAllPlayers`: Pobieranie pełnej listy graczy na serwerze.

### 28.3. Testy Reguł Widoczności NameTags (`pl.zombek.client.core.modules.visual.nametags`)

3. **`NameTagVisibilityRuleEvaluatorTest`** (7 testów):
   - `testVisiblePlayerRenders`: Standardowy gracz w polu widzenia jest renderowany z zachowaniem bufora głębokości.
   - `testSneakingPlayerFairPlay`: Respektowanie vanillowych zasad skradania się (brak ujawniania ukrytych graczy).
   - `testInvisiblePlayerFairPlay`: Gwarancja Fair Play — brak renderowania etykiet niewidzialnych przeciwników (zakaz ESP/wallhacku).
   - `testSpectatorFairPlay`: Gracz w trybie widza traktowany ściśle wg zasad vanilla.
   - `testMaxDistanceCutoff`: Bezwzględne odcięcie renderowania po przekroczeniu limitu `maxDistance` (domyślnie 64 bloki).
   - `testThirdPersonCameraSelfTag`: Etykieta własnej postaci wyświetlana wyłącznie w widoku trzeciej osoby (F5).
   - `testCategoryFiltering`: Niezależne włączanie/wyłączanie kategorii (`PLAYERS`, `TEAMMATES`, `FRIENDS`, `LIVING_ENTITIES`, `OTHER_ENTITIES`).

4. **`NameTagsModuleTest`** (7 testów):
   - `testMetadataAndCategory`: Identyfikator `"nametags"`, kategoria `VISUAL`, 9 dedykowanych grup ustawień.
   - `testFormatNameTagWithFriendAndHealth`: Kompletne formatowanie etykiety z gwiazdką znajomego, zdrowiem i dystansem.
   - `testHealthDisplayStyles`: Poprawność renderowania 5 styli zdrowia (`NUMERIC`, `HEARTS`, `BAR`, `COMPACT`, `CUSTOM`).
   - `testDistanceFormats`: Formatowanie dystansu w trybach `BLOCKS`, `DECIMAL`, `ROUNDED`.
   - `testArmorDurabilityDisplay`: Generowanie wskaźników trwałości pancerza.
   - `testPreviewModeInGui`: Implementacja `IHudElement` z bezpiecznym podglądem `[PREVIEW]` w menu konfiguracji.
   - `testRestoreDefaults`: Przywracanie domyślnych ustawień modułu NameTags.

### 28.4. Testy Układu i Sortowania Tablisty (`pl.zombek.client.core.modules.visual.tabeditor`)

5. **`TabSortingManagerTest`** (6 testów):
   - `testSortVanilla`: Sortowanie vanillowe wg wag zespołów i kolejności graczy.
   - `testSortAlphabetical`: Sortowanie alfabetyczne rosnące (A-Z) i malejące (Z-A).
   - `testSortPing`: Sortowanie po opóźnieniu połączenia (rosnąco/malejąco).
   - `testSortFriendsFirst`: Priorytetyzacja znajomych na początku tablisty przed pozostałymi graczami.
   - `testSortTeamThenAlphabetical`: Grupowanie po zespołach, a wewnątrz zespołów alfabetycznie.
   - `testSortStabilityWithEmptyList`: Stabilność algorytmu przy pustej lub jednoelementowej liście graczy.

6. **`TabLayoutManagerTest`** (7 testów):
   - `testComputeGridSingleColumn`: 1 kolumna dla małej liczby graczy.
   - `testComputeGridMultipleColumns`: Równoważenie wierszy dla 45 graczy na 3 kolumny po 15 wierszy.
   - `testColumnWidthCalculation`: Dynamiczne wyliczanie szerokości kolumn z uwzględnieniem najdłuższej nazwy.
   - `testScreenBoundsClamping`: Bezpieczne dopasowanie wymiarów siatki do rozdzielczości ekranu.
   - `testLayoutPresets`: Zastosowanie presetów `VANILLA`, `MINIMAL`, `MODERN`, `CARD`, `COLUMNS`, `CLASSIC`.
   - `testRowHeightAndPadding`: Zachowanie spójnych odstępów i wysokości wierszy.
   - `testZeroPlayersFallback`: Bezpieczny fallback przy zerowej liczbie graczy.

7. **`TabEditorModuleTest`** (7 testów):
   - `testMetadataAndCategory`: Identyfikator `"tab_editor"`, kategoria `VISUAL`, 7 grup ustawień, priorytet HUD: 120.
   - `testHeaderFooterModes`: Obsługa trybów nagłówka i stopki `ORIGINAL`, `CUSTOM`, `HIDDEN`.
   - `testPingModes`: Formatowanie opóźnienia w trybach `NUMERIC`, `BARS`, `COLORED_TEXT`, `MINIMAL_ICON`.
   - `testFriendHighlighting`: Wyróżnianie znajomych tłem, kolorem nicku i ikoną.
   - `testColumnToggles`: Przełączanie widoczności kolumn awatara, rangi, zdrowia i pingu.
   - `testAnimationsToggle`: Obsługa animacji fade i slide.
   - `testRestoreDefaults`: Resetowanie ustawień modułu Tab Editor do wartości domyślnych.

### 28.5. Testy Integracyjne Core (`pl.zombek.client.core`)

8. **`Etap8B5IntegrationTest`** (2 testy):
   - `testNameTagsAndTabEditorRegisteredInCore`: Potwierdzenie automatycznej rejestracji `nametags` i `tab_editor` w `ZombekCore.init()` i `ModuleManager`.
   - `testCoreServicesLifecycle`: Weryfikacja cyklu życia `ZombekPlayerInfoService.onClientTick()`, czyszczenia buforów przy `onWorldLeave()`, trwałości `NameTagFriendManager` oraz rozstrzygania priorytetu HUD (120) w `HudPriorityResolver`.
























---

## 29. Zestawy Testów — Etap 8B6 (Chat + Titles)

W etapie 8B6 zaimplementowano 8 dedykowanych zestawów testów jednostkowych i integracyjnych w module `:core` (`apps/minecraft-client/core/src/test/java`), rozszerzając bazę testów Core do **525 testów (100% passed)**:

### 29.1. Testy Filtrowania i Reguł Czatu
1. **`ChatFilterManagerTest`**:
   - `testCategoryClassification`: Klasyfikacja wiadomości prywatnych, party, gildii, dołączeń, opuszczeń, zgonów i powiadomień systemowych.
   - `testExactFilterRule`: Dokładne dopasowanie tekstu reguły z akcją `HIDE_LOCALLY`.
   - `testContainsFilterRule`: Wyszukiwanie słów kluczowych i ukrywanie spamu.
   - `testRegexFilterRule`: Zastosowanie wyrażeń regularnych (np. linki do zewnętrznych serwerów Discord).
   - `testReDosProtectionDoesNotHang`: Ochrona przed katastrofalnym cofaniem (ReDoS) przy złośliwym wzorcu `(a+)+$` i wejściu z powtórzeniami.
   - `testFilterActionHighlightAndColor`: Zmiana koloru i podświetlenie wiadomości pasujących do reguły.
   - `testFilterMetrics`: Zliczanie odfiltrowanych i ukrytych wiadomości.

### 29.2. Testy Kart i Kanałów Czatu
2. **`ChatTabManagerTest`**:
   - `testDefaultTabsPresent`: Weryfikacja obecności 7 standardowych zakładek (`all`, `general`, `private`, `party`, `team`, `guild`, `system`).
   - `testTabSwitching`: Płynne przełączanie aktywnej karty i czyszczenie licznika nieprzeczytanych wiadomości.
   - `testMessageRoutingToTabs`: Kierowanie wiadomości do kart na podstawie dopasowania kategorii `ChatCategory`.
   - `testUnreadBadges`: Inkrementacja licznika nieprzeczytanych wiadomości na kartach w tle i zerowanie po aktywacji.
   - `testCustomTabCreation`: Tworzenie niestandardowych kart użytkownika.

### 29.3. Testy Historii i Bezpieczeństwa
3. **`ChatHistoryManagerTest`**:
   - `testRingBufferLimit`: Przestrzeganie pojemności bufora pierścieniowego (FIFO) przy napływie wielu wiadomości.
   - `testSearchByContent`: Wyszukiwanie wiadomości po frazie tekstowej.
   - `testSearchByCategory`: Wyszukiwanie po kategorii wiadomości.
   - `testCredentialSanitization`: Bezpieczeństwo poświadczeń — komendy uwierzytelniające (`/login`, `/register`) nigdy nie trafiają do historii.
   - `testPrivacyAndStreamerModeExport`: Redagowanie nicków nadawców (`[SENDER REDACTED]`) podczas eksportu w trybie Streamer Mode.
   - `testClearOnDisconnect`: Automatyczne czyszczenie bufora pamięci po rozłączeniu z serwerem.

### 29.4. Testy Powiadomień i Wzmianek
4. **`ChatNotificationControllerTest`**:
   - `testOwnPlayerMention`: Wykrywanie wzmianki nicku gracza, podświetlenie wiadomości i flaga `isImportant`.
   - `testFriendAliasMention`: Wykrywanie wzmianek pseudonimów znajomych zarejestrowanych w `NameTagFriendManager`.
   - `testCustomKeywordMention`: Reakcja na słowa kluczowe zdefiniowane przez użytkownika.
   - `testAudioNotificationCooldown`: Ochrona przed spamem dźwiękowym z zachowaniem konfigurowalnego cooldownu (sekundy).
   - `testUnreadCounter`: Zliczanie i resetowanie powiadomień.

### 29.5. Testy Koordynatora Potoku Wiadomości
5. **`ChatMessageManagerTest`**:
   - `testRepeatedMessageGrouping`: Grupowanie identycznych wiadomości z rzędu z licznikiem powtórzeń (`×2`, `×3`).
   - `testTimestampFormatting`: Prezentacja znaczników czasu w formacie `HH:mm`.
   - `testDisableTimestamps`: Poprawne wyłączenie znaczników czasu.
   - `testHistoryStorageThroughMessageManager`: Zintegrowane przekazywanie wiadomości do bufora historii.

### 29.6. Testy Kolejki i Czasów Tytułów
6. **`TitleQueueManagerTest`**:
   - `testTitleAndSubtitleLifecycle`: Cykl życia Title i Subtitle: interpolacja alpha w fazie Fade In, pobyt w Stay (alpha 1.0) i wygaszanie w Fade Out.
   - `testIndependentActionBarLifecycle`: Pełna niezależność Action Bara od Title/Subtitle — wyczyszczenie tytułów nie usuwa aktywnego Action Bara.
   - `testClientOverrideTimings`: Wymuszenie własnych czasów trwania (override fadeIn, stay, fadeOut) niezależnie od serwera.

### 29.7. Testy Modułów i Integracji
7. **`ChatModuleTest` & `TitlesModuleTest`**:
   - Weryfikacja metadanych, kategorii (`VISUAL` dla Chat, `HUD` dla Titles), rejestracji grup ustawień i resetu do wartości domyślnych (`resetToDefaults()`).
8. **`Etap8B6IntegrationTest`**:
   - Rejestracja w `ZombekCore.init(...)` i `ModuleManager`.
   - Weryfikacja priorytetów HUD: 115 dla Titles (`HudPriorityResolver.PRIORITY_TITLES`) i 110 dla Chat (`HudPriorityResolver.PRIORITY_CHAT`).
   - Postęp ticków w `ZombekCore.onClientTick()`.
   - Potok podglądu `IHudElement.render(...)` i bezpośrednie wywołania `CustomChatRenderer` oraz `CustomTitleRenderer`.

---

## 30. Testy Jednostkowe i Integracyjne Etapu 8B7 (Net Graph)

W Etapie 8B7 baza testów jednostkowych Core została rozszerzona do **563 testów** (100% sukces, 0 błędów). Wszystkie nowe komponenty diagnostyczne i buforujące zostały objęte dedykowanymi zestawami testów JUnit 5:

### 30.1. Testy Śledzenia Opóźnienia i Jittera (`LatencyTrackerTest`)
- `testInitialStateUnavailable`: Weryfikacja stanu początkowego (ping -1, jitter -1, niedostępny).
- `testSingleplayerDoesNotPolluteSamples`: Ochrona przed fałszywym pingiem w Singleplayer — wartości -1 nie zanieczyszczają średniej arytmetycznej.
- `testJitterCalculationRFC3550`: Matematyczna weryfikacja algorytmu RFC 3550 EWMA oraz Mean Absolute Delay Variation dla kolejnych próbek.
- `testMinMaxAndAveragePing`: Wyliczanie wartości skrajnych i średniej w oknie próbek.
- `testCapacityEviction`: Wypychanie najstarszych próbek (FIFO) po zapełnieniu bufora pojemności.
- `testReset`: Prawidłowe zerowanie stanu i unieważnienie wskaźników dostępności.
- `testSamplesWithinDuration`: Filtrowanie próbek po znacznikach czasu w oknie czasowym.

### 30.2. Testy Przepustowości Sieciowej (`NetworkTrafficTrackerTest`)
- `testInitialState`: Weryfikacja zerowych liczników początkowych.
- `testThroughputUpdate`: Obliczanie przepustowości B/s, KB/s w interwale 1000 ms oraz zachowanie wartości szczytowych (Peak).
- `testFormatBytesPerSecond`: Formatowanie transferu w trybach `AUTO`, `B/s`, `KB/s`, `MB/s`.
- `testFormatTotalBytes`: Formatowanie sumarycznego transferu w jednostkach B, KB, MB, GB.
- `testReset`: Zerowanie akumulatorów atomowych i wskaźników przepustowości.

### 30.3. Testy Częstotliwości Pakietów (`PacketRateTrackerTest`)
- `testInitialState`: Weryfikacja zerowych liczników PPS.
- `testPacketRateAccumulation`: Zliczanie pakietów odebranych (RX PPS) i nadanych (TX PPS), obliczanie średnich i wartości szczytowych.
- `testReset`: Prawidłowe zerowanie liczników pakietów.

### 30.4. Testy Bufora Historii (`NetworkHistoryBufferTest`)
- `testEmptyBuffer`: Bezpieczne zachowanie pustego bufora (brak NPE).
- `testAddSampleAndCapacityEviction`: Prawidłowe działanie bufora kołowego przy nadmiarowych próbkach.
- `testAggregatesCalculation`: Wyliczanie wartości szczytowych i średnich w zadanym oknie czasowym.
- `testClear`: Czyszczenie bufora historii.

### 30.5. Testy Klasyfikacji Połączenia (`ConnectionStatusTrackerTest`)
- `testInitialState`: Domyślny stan UNKNOWN i jakość UNAVAILABLE.
- `testSingleplayerHandling`: Wymuszenie stanu SINGLEPLAYER i jakości UNAVAILABLE w grze lokalnej.
- `testDisconnectedHandling`: Poprawne przejście do stanu DISCONNECTED.
- `testQualityClassification`: Progowanie jakości: Excellent, Good, Moderate, Poor.
- `testHighJitterDegradesQuality`: Degradacja jakości połączenia przy wysokim jitterze pomimo niskiego pingu.

### 30.6. Testy Statystyk i Prywatności (`NetworkStatisticsTest`)
- `testInitialState`: Weryfikacja zerowych statystyk sesji.
- `testUpdateAndAverages`: Gromadzenie statystyk szczytowych i średnich.
- `testExportToJsonDoesNotContainPrivateData`: Gwarancja braku prywatnych adresów IP, hostów, haseł i treści czatu w eksportowanym JSON.
- `testExportToText`: Generowanie czytelnego raportu tekstowego.
- `testReset`: Zerowanie statystyk sesji.

### 30.7. Testy Modułu i Integracji (`NetGraphModuleTest` & `Etap8B7IntegrationTest`)
- `testMetadataAndSettingsGroups`: Weryfikacja identyfikatora `"net_graph"`, kategorii `HUD` oraz 11 grup ustawień.
- `testResetToDefaults`: Przywracanie domyślnych wartości wszystkich parametrów.
- `testSingleplayerRendering`: Weryfikacja renderowania widoku lokalnego (brak fałszywych wykresów pingu).
- `testMultiplayerRendering`: Weryfikacja renderowania wykresów z aktywnym połączeniem.
- `testPreviewRendering`: Renderowanie podglądu testowego w menu Right Shift.
- `testModuleRegistrationAndPriority`: Rejestracja w `ZombekCore` i priorytet HUD 36 (`HudPriorityResolver.PRIORITY_NET_GRAPH`).
- `testSharedPingIntegration`: Weryfikacja współdzielenia pingu pomiędzy `PingModule` a `NetGraphModule`.
- `testSessionResetOnDisconnect`: Resetowanie aktywnego stanu po rozłączeniu ze światem gry.

---

## 31. Testy Jednostkowe i Integracyjne HUD Editor Foundation (Etap 9A)

W ramach Etapu 9A wdrożono 6 nowych pakietów testowych JUnit 5 w module `:core` (`pl.zombek.client.core.hud.editor`), zwiększając łączną liczbę testów klienta do **578 (100% sukcesu)**:

### 31.1. Testy Rejestru Komponentów (`HudComponentRegistryTest`)
- `testRegisterAndRetrieve`: Rejestracja deskryptora komponentu, weryfikacja pobierania według identyfikatora oraz ochrona przed duplikatami.
- `testGetByCategoryAndType`: Filtrowanie zarejestrowanych deskryptorów według kategorii systemowych (`HUD`, `PVP`, `VISUAL`, `SURVIVAL`, `OTHER`) oraz typu komponentu (`SCREEN_SPACE`, `CENTER_SCREEN`, `WORLD_SPACE`, `GUI_OVERLAY`).
- `testUnregisterAndClear`: Bezpieczne wyrejestrowywanie komponentów i czyszczenie rejestru.

### 31.2. Testy Menedżera Selekcji (`HudSelectionManagerTest`)
- `testSelectionAndDeselection`: Zaznaczanie i odznaczanie komponentu, nasłuchiwanie zdarzeń selekcji przez zarejestrowanych listenerów.
- `testCycleSelectionAtPoint`: Test cyklicznej selekcji w przypadku nakładających się prostokątów ograniczających (Bounding Box disambiguation) przy wielokrotnym kliknięciu w ten sam punkt.

### 31.3. Testy Serializacji i Trwałości Układów (`HudLayoutSerializationTest`)
- `testSerializeAndDeserialize`: Pełny cykl zapisu i odczytu modelu `HudLayout` do formatu JSON z zachowaniem wszystkich właściwości komponentów (`posX`, `posY`, `scale`, `anchor`, `visible`).
- `testValidationAndAutoRepair`: Automatyczna naprawa uszkodzonych lub niepełnych danych układu (brakujące identyfikatory, nieprawidłowe skale, brakujące pola).
- `testAtomicSaveAndBackupRecovery`: Weryfikacja atomowego zapisu pliku (`layout.json.tmp` -> `layout.json`) oraz odzyskiwania stanu z pliku kopii zapasowej `.bak` w przypadku wykrycia uszkodzenia pliku głównego.
- `testDirtyCheckingInsensitiveToTimestamps`: Precyzyjne sprawdzanie stanu niezapisanych zmian (`hasUnsavedChanges()`) bez fałszywych alarmów wywoływanych różnicami znaczników czasu `updatedAt`.

### 31.4. Testy Migracji Konfiguracji (`HudConfigMigratorTest`)
- `testMigrationFromModuleSettings`: Bezpieczna, bezstratna migracja współrzędnych i skali z istniejących obiektów `Setting` modułów (`pos_x`, `pos_y`, `scale`, `anchor`) do struktury `HudLayout` bez modyfikowania pliku `modules.json`.

### 31.5. Testy Menedżera Edytora HUD (`HudEditorManagerTest`)
- `testOpenAndCloseSession`: Inicjalizacja sesji edytora (`openEditor()`), przygotowanie bufora roboczego (staging) i zamknięcie z zachowaniem integralności stanu.
- `testCommitStagedChanges`: Zatwierdzenie modyfikacji z bufora tymczasowego do aktywnego profilu oraz zapis na dysku.
- `testDiscardStagedChanges`: Anulowanie zmian i przywrócenie stanu pierwotnego z aktywnego profilu.

### 31.6. Testy Integracyjne Etapu 9A (`Etap9AIntegrationTest`)
- `testFullEditorLifecycle`: Kompletny cykl życia edytora: rejestracja modułów, migracja parametrów, edycja współrzędnych w buforze roboczym, dirty check, atomowy commit i weryfikacja końcowego układu.
- `testDefaultComponentsRegistered`: Weryfikacja, czy wszystkie standardowe moduły klienta (32 moduły) zostały poprawnie zarejestrowane w `HudComponentRegistry` z unikalnymi identyfikatorami i poprawnymi kategoriami.

---

## 32. Testy Jednostkowe i Integracyjne HUD Editor Interaction System (Etap 9B)

W ramach Etapu 9B wdrożono 9 nowych pakietów testowych JUnit 5 w module `:core` (`pl.zombek.client.core.hud.editor`), zwiększając łączną liczbę testów klienta w `:core` do **618 (100% sukcesu)**, a w całym monorepo do **796 testów**:

### 32.1. Testy Kontrolera Przeciągania (`HudDragControllerTest`)
- `testDragPreservesGrabOffset`: Sprawdzenie, czy chwycenie myszą w dowolnym punkcie wewnątrz komponentu nie powoduje skoku do lewego górnego rogu.
- `testDragClampsToScreenBounds`: Wymuszenie zatrzymania elementu na krawędziach ekranu (`0 <= x <= screenW - compW`, `0 <= y <= screenH - compH`).
- `testDragLockedComponentFails`: Potwierdzenie, że komponent z flagą `locked == true` odrzuca próbę przeciągania.
- `testDragWorldSpaceComponentFails`: Odrzucenie przeciągania dla elementów z kotwicą `HudAnchor.WORLD_SPACE`.
- `testCancelDragRestoresInitialPosition`: Cofnięcie pozycji początkowej po wywołaniu `cancelDrag()`.

### 32.2. Testy Kontrolera Zmiany Rozmiaru (`HudResizeControllerTest`)
- `testResizeRightHandleIncreasesWidth`: Zwiększanie szerokości uchwytem `MR` z zachowaniem lewej krawędzi.
- `testResizeLeftHandleAdjustsXAndWidthStationaryRightEdge`: Przeciąganie uchwytu `ML` z zachowaniem idealnie nieruchomej prawej krawędzi.
- `testResizeRespectsMinAndMaxLimits`: Egzekwowanie limitów `minWidth`, `minHeight`, `maxWidth`, `maxHeight` z deskryptora.
- `testResizeLockedComponentFails`: Odrzucenie zmiany rozmiaru dla elementów zablokowanych.
- `testResizeAspectRatioLock`: Utrzymanie stałego stosunku proporcji `w/h` przy włączonej fladze `lockAspectRatio`.
- `testResizeDisablesAutoSize`: Automatyczne odpięcie `autoSize = false` w momencie ręcznej zmiany rozmiaru.

### 32.3. Testy Kontrolera Skalowania (`HudScaleControllerTest`)
- `testSetScaleClampsToMinMax`: Przycinanie skali do zakresu `0.25` - `3.00`.
- `testScaleRoundsToTwoDecimals`: Zaokrąglanie wartości skali do 2 miejsc po przecinku (eliminacja błędów float).
- `testScaleLockedComponentFails`: Ochrona zablokowanego elementu przed skalowaniem.
- `testScaleDoesNotMutateBaseDimensions`: Potwierdzenie, że `scale` zmienia jedynie wymiary efektywne, a `width`/`height` pozostają bazowe.

### 32.4. Testy Menedżera Siatki (`HudGridManagerTest`)
- `testGridManagerDefaultsAndToggles`: Domyślny styl `LINES`, spacing 16 px, przełączanie widoczności.
- `testCycleGridStyles`: Cykliczne przełączanie pomiędzy 5 stylami siatki (`NONE`, `DOTS`, `LINES`, `CROSSHAIR_GRID`, `CUSTOM`).
- `testSetSpacingPreset`: Ustawianie presetów wielkości siatki (2, 4, 8, 16, 24, 32).

### 32.5. Testy Przyciągania (`HudSnappingTest`)
- `testSnapToCenterAxis`: Przyciąganie do osi środka ekranu z generowaniem linii pomocniczej Cyan.
- `testSnapToScreenEdges`: Przyciąganie do zewnętrznych granic ekranu z linią pomocniczą Amber.
- `testSnapToGridLines`: Przyciąganie do aktywnych linii siatki z linią pomocniczą Blue.
- `testBeyondThresholdDoesNotSnap`: Płynny ruch bez przyciągania po przekroczeniu progu histerezy.

### 32.6. Testy Menedżera Wyrównywania (`HudAlignmentManagerTest`)
- `testAlignLeftAndRight`: Wyrównanie do lewej i prawej krawędzi ekranu.
- `testAlignTopAndBottom`: Wyrównanie do górnej i dolnej krawędzi ekranu.
- `testCenterHorizontalAndVertical`: Wycentrowanie w osi poziomej i pionowej.
- `testAlignmentFailsForLockedComponent`: Blokada operacji wyrównywania dla elementów locked.

### 32.7. Testy Wirtualnej Rzutni Płótna (`HudCanvasViewportTest`)
- `testInitialState`: Weryfikacja stanu początkowego (zoom 1.0, pan 0, 0).
- `testZoomInAndZoomOut`: Stopniowe powiększanie i pomniejszanie z ograniczeniami 0.25x - 4.00x.
- `testZoomAtPoint`: Zoomowanie z zachowaniem punktu wirtualnego pod kursorem myszy.
- `testPan`: Przesuwanie widoku kamery offsetami deltaX i deltaY.
- `testCoordinateTransformationsRoundTrip`: Zerowy dryf przy przeliczaniu `screenToVirtual` i `virtualToScreen`.
- `testFitCanvasAndReset`: Automatyczne dopasowanie widoku do okna edytora i reset parametrów.

### 32.8. Testy Dyspozytora Myszy (`HudEditorMouseControllerTest`)
- `testClickOnResizeHandleInitiatesResize`: Przechwycenie kliknięcia w uchwyt zmiany rozmiaru.
- `testClickOnComponentInitiatesDrag`: Rozpoczęcie przeciągania po kliknięciu w ciało komponentu.
- `testClickOnCanvasClearsSelection`: Odznaczenie aktywnego elementu po kliknięciu w puste tło.
- `testMiddleMouseInitiatesPan`: Uruchomienie przesuwania widoku środkowym przyciskiem myszy.
- `testPanToolDrag`: Przesuwanie widoku lewym przyciskiem myszy przy aktywnym narzędziu PAN.

### 32.9. Testy Integracyjne Interakcji (`Etap9BIntegrationTest`)
- `testFullInteractionLifecycle`: Kompletny potok: przeciąganie elementu, zmiana rozmiaru uchwytem BR, zmiana skali, wycentrowanie na ekranie, commit do pliku na dysku i weryfikacja trwałego zapisu.
- `testCancelRevertsAllInteractions`: Anulowanie sesji edycji i weryfikacja przywrócenia pierwotnego stanu wszystkich komponentów.
- `testIntermediateDragFramesDoNotWriteToDisk`: Weryfikacja, że klatki pośrednie przeciągania modyfikują jedynie bufor pamięci roboczej i nie wykonują żadnych operacji zapisu na dysku.

---

## 33. Zestaw Testów Zaawansowanych Systemów HUD Editora (Etap 9C)

W Etapie 9C zaimplementowano 10 kompleksowych klas testowych w module `:core` (`pl.zombek.client.core.hud.editor`), osiągając łączną liczbę **663 pomyślnie wykonanych testów jednostkowych i integracyjnych**:

### 33.1. Testy Menedżera Profili (`HudProfileManagerTest`)
- `testCreateAndSwitchProfile`: Tworzenie nowego profilu, zapis na dysku, aktywacja profilu i weryfikacja stanu aktywnego layoutu.
- `testCannotDeleteActiveProfile`: Zabezpieczenie przed usunięciem aktualnie aktywnego profilu (wymóg wcześniejszego przełączenia).
- `testCannotDeleteDefaultProfile`: Ochrona profilu domyślnego (`default`) przed usunięciem.
- `testDuplicateProfile`: Klonowanie istniejącego profilu wraz ze wszystkimi komponentami, grupami i warstwami.
- `testSwitchToNextProfileInCycle`: Cykliczne przełączanie kolejnych profili (Default -> PvP -> Survival -> ... -> Default).
- `testAutoBackupOnSave`: Weryfikacja tworzenia pliku kopii bezpieczeństwa `.bak` przed nadpisaniem profilu.
- `testProfilePathTraversalProtection`: Odrzucenie niebezpiecznych identyfikatorów profili zawierających sekwencje `../`.
- `testProfileSwitchDoesNotMutateGameplayConfig`: Sprawdzenie niezależności profilu HUD od ustawień modułów i klawiszy.
- `testCorruptProfileRecovery`: Pomyślne odzyskanie profilu z pliku `.bak` w przypadku uszkodzenia pliku głównego JSON.

### 33.2. Testy Menedżera Szablonów (`HudPresetManagerTest`)
- `testEightSystemPresetsRegistered`: Potwierdzenie rejestracji co najmniej 8 wbudowanych szablonów (Default, Minimal, PvP, Survival, UHC, Building, Performance, Streamer).
- `testApplyPvpPreset`: Aplikacja presetu PvP na layout, weryfikacja przesunięć Keystrokes, CPS i Armor Status oraz podgląd zmodyfikowanych elementów.
- `testApplyMinimalPreset`: Aplikacja presetu minimalistycznego, weryfikacja ukrycia zbędnych elementów i widoczności wyłącznie FPS oraz Ping.
- `testApplyPresetCanBeUndone`: Weryfikacja, że aplikacja presetu rejestruje pojedyncze polecenie `ApplyPresetCommand`, w pełni odwracalne przez `Undo`.
- `testCustomPresetSaveAndLoad`: Zapisanie niestandardowego szablonu użytkownika na dysk i jego ponowne odczytanie.

### 33.3. Testy Wielokrotnego Zaznaczania (`HudMultiSelectionManagerTest`)
- `testSingleSelection`: Kliknięcie pojedynczego komponentu zaznacza go i odznacza poprzednie.
- `testToggleSelectionWithCtrl`: Ctrl+kliknięcie dodaje i usuwa elementy z zaznaczenia bez czyszczenia reszty.
- `testSelectAllAndClear`: `Ctrl+A` zaznacza wszystkie widoczne elementy, `Esc` czyści całe zaznaczenie.
- `testMarqueeBoxSelection`: Zaznaczanie ramką obszarową w trybach `INTERSECTING` i `FULLY_INSIDE`.
- `testMultiDragPreservesRelativeOffsets`: Przesuwanie wielu elementów jednocześnie z zachowaniem idealnych odległości względnych.

### 33.4. Testy Zarządzania Grupami (`HudGroupManagerTest`)
- `testCreateGroupAndAssignComponents`: Utworzenie grupy (`HudGroup`), przypisanie elementów i weryfikacja `groupId`.
- `testDeleteGroupUngroupsComponents`: Rozgrupowanie usuwa definicję grupy i czyści pole `groupId` w komponentach bez zmiany ich pozycji.
- `testCalculateGroupBounds`: Precyzyjne wyliczenie obwiedni min/max X i Y dla grupy składającej się z kilku elementów.
- `testDuplicateGroup`: Sklonowanie całej grupy z nowym ID i offsetem przestrzennym.

### 33.5. Testy Zarządzania Warstwami (`HudLayerManagerTest`)
- `testDefaultLayersExist`: Weryfikacja obecności 4 warstw systemowych (`background`, `main_hud`, `information`, `notifications`).
- `testCreateCustomLayer`: Dodanie nowej warstwy użytkownika z unikalną bazą Z-Index.
- `testCannotDeleteSystemLayer`: Ochrona warstw systemowych przed usunięciem.
- `testAssignComponentToLayer`: Przypisanie komponentu do innej warstwy i aktualizacja jego Z-Index.
- `testGetSortedComponents`: Deterministyczne sortowanie komponentów na osi Z przed renderowaniem.

### 33.6. Testy Historii Cofania i Ponawiania (`HudUndoRedoManagerTest`)
- `testUndoRedoMoveCommand`: Cofnięcie i ponowienie przesunięcia komponentu (`MoveComponentsCommand`).
- `testGestureGrouping`: Scalenie ciągłego przeciągania myszą w jedno atomowe polecenie po zwolnieniu przycisku myszy.
- `testHistoryLimitMaxFifty`: Ograniczenie stosu historii do maksymalnie 50 kroków (FIFO eviction).
- `testNewActionClearsRedoStack`: Wykonanie nowej operacji po `Undo` bezpowrotnie unieważnia stos `Redo`.

### 33.7. Testy Schowka i Duplikacji (`HudComponentClipboardTest`)
- `testCopyAndPasteInPlace`: Skopiowanie elementów (`Ctrl+C`) i wklejenie w tym samym miejscu (`IN_PLACE`).
- `testDuplicateWithOffset`: Duplikowanie (`Ctrl+D`) tworzy kopię ze standardowym przesunięciem +15px.
- `testSingleInstanceProtection`: Zabezpieczenie przed powielaniem singletonów (np. wskaźnika FPS) przy wklejaniu.

### 33.8. Testy Importu i Eksportu Układów (`HudLayoutImportExportTest`)
- `testExportAndImportLayoutReplace`: Eksport układu do JSON Schema v1 i ponowny import w trybie `REPLACE`.
- `testImportMergePreservesOtherComponents`: Import w trybie `MERGE` z zachowaniem istniejących komponentów gracza.
- `testPathTraversalGuard`: Bezpieczne odrzucanie niebezpiecznych ścieżek z próbą wyjścia poza dozwolony katalog.

### 33.9. Testy Układów Responsywnych (`HudResponsiveLayoutTest`)
- `testAspectRatioDimensions`: Wyliczenie wymiarów wirtualnych dla proporcji 4:3, 16:9 i 21:9 z zachowaniem letterboxingu.
- `testValidationOutOfBounds`: Wykrycie komponentu wykraczającego poza ekran i wyemitowanie ostrzeżenia `OUT_OF_BOUNDS`.
- `testValidationOverlap`: Wykrycie dwóch widocznych komponentów nakładających się na tym samym Z-Index (`OVERLAP`).

### 33.10. Kompleksowy Test Integracyjny Etapu 9C (`Etap9CIntegrationTest`)
- `testFullEtap9CWorkflow`: Pełny scenariusz użytkownika: otwarcie sesji edytora, zaznaczenie wielu elementów, utworzenie grupy `net_group`, przypisanie do warstwy `information`, przesunięcie z rejestracją w historii, cofnięcie `Undo`, skopiowanie do schowka, wklejenie, commit do pliku JSON na dysku i zamknięcie sesji edytora.

---

## 34. Testy Jednostkowe i Integracyjne Etapu 8C1: Camera & Screen Visuals

W Etapie 8C1 utworzono zestaw 6 dedykowanych klas testowych w pakiecie `pl.zombek.client.core.camera`, weryfikujących logikę optyczną, interpolację, zawijanie kątowe oraz bezpieczeństwo silnika gry.

### 34.1. Testy Kontrolera Przybliżenia (`ZoomControllerTest`)
- `testHoldActivationMode`: Weryfikacja aktywacji w trybie `HOLD` (przybliżenie aktywne przy wciśnięciu, powrót po zwolnieniu).
- `testToggleActivationMode`: Pojedyncze naciśnięcie włącza stan zoomu, kolejne naciśnięcie go wyłącza.
- `testToggleHoldActivationMode`: Krótkie kliknięcie (< 300 ms) działa jak `TOGGLE`, dłuższe przytrzymanie działa jak `HOLD`.
- `testSmoothZoomEasing`: Płynna interpolacja współczynnika w czasie rzeczywistym wg krzywej `EASE_IN_OUT`.
- `testInstantZoom`: Natychmiastowe osiągnięcie docelowego przybliżenia w 1 klatce w trybie `instant_zoom`.
- `testScrollZoom`: Zmiana współczynnika kółkiem myszy z ograniczeniem do przedziału $[1.1\text{x}, 30.0\text{x}]$ oraz reset do wartości bazowej.
- `testFocusLostAndGuiOpenedResetsZoom`: Automatyczne wygaszenie przybliżenia przy utracie fokusu lub otwarciu GUI.

### 34.2. Testy Kontrolera Pola Widzenia (`CameraFovControllerTest`)
- `testFovPresets`: Poprawność wartości docelowych presetów (DEFAULT 70°, PRO_PVP 100°, QUAKE_PRO 110°).
- `testCustomFovClamping`: Ograniczenie niestandardowego kąta widzenia do bezpiecznego przedziału $[30.0^\circ, 150.0^\circ]$.
- `testDynamicFovMultipliers`: Prawidłowe mnożenie kąta widzenia przy sprincie (1.15x) i naciąganiu łuku (0.85x).
- `testDisableDynamicFov`: Całkowite zablokowanie zmian FOV przy włączonej opcji `disable_dynamic_fov`.
- `testZoomIntegrationAndProjectionClamp`: Dzielenie kąta dynamicznego przez aktywny ZoomFactor i rzutowanie do bezpiecznego zakresu $[5.0^\circ, 170.0^\circ]$.

### 34.3. Testy Kontrolera Rozmycia Ruchu (`MotionBlurControllerTest`)
- `testMotionCalculationWith360Wrap`: Poprawne obliczenie delty kątowej bez skoku przy przechodzeniu przez $0^\circ / 360^\circ$ (np. obrót z $359^\circ$ na $1^\circ$ to $\Delta = 2^\circ$, a nie $358^\circ$).
- `testThresholdAndDecay`: Wygaszanie rozmycia w spoczynku (`blur_decay`) oraz brak aktywacji poniżej `movement_threshold`.
- `testQualityProfiles`: Weryfikacja liczby próbek bufora historii dla profili `LOW` (2), `MEDIUM` (4), `HIGH` (6) i `ULTRA` (8).
- `testCrashProofFallback`: Bezpieczne przełączenie w tryb pasywny bez rzucania wyjątków w przypadku braku wsparcia FBO.

### 34.4. Testy Kontrolera Przechyłu Obrażeń (`HurtCamControllerTest`)
- `testHurtCamModes`: Tryb `VANILLA` zachowuje natywne 100% przechyłu, `DISABLED` anuluje przechył ($0^\circ$), `REDUCED` skaluje go do 50%.
- `testCustomMode`: Weryfikacja własnych mnożników `camera_tilt_multiplier` i `camera_shake`.
- `testSpringRecoveryDecay`: Płynny powrót kamery do pionu po uderzeniu w czasie zadanym w milisekundach.
- `testZeroDamageImpact`: Sprawdzenie, że anulowanie efektu wizualnego nie ingeruje w rejestrację obrażeń w `PvPInfoModule`.

### 34.5. Testy Centralnego Kontrolera Kamery (`ZombekCameraControllerTest`)
- `testPipelineOrdering`: Deterministyczna sekwencja kalkulacji (Base -> FOV -> Zoom -> Freelook -> Hurt Cam -> Motion Blur).
- `testLifecycleGuards`: Weryfikacja wywołań `onFocusLost`, `onGuiOpened`, `onPlayerDeath`, `onDimensionChange` i `shutdown`.

### 34.6. Kompleksowy Test Integracyjny Etapu 8C1 (`Etap8C1IntegrationTest`)
- `testAllVisualModulesRegistered`: Sprawdzenie obecności `ZoomModule`, `FovChangerModule`, `MotionBlurModule` i `HurtCamModule` w `ModuleManager`.
- `testHudComponentRegistrar`: Potwierdzenie rejestracji komponentu #33 `zoom_indicator` w `HudComponentRegistry`.
- `testCoordinatedCameraTick`: Równoczesna praca aktywnego zoomu (kółko myszy), sprintu (dynamic FOV) i otrzymania uderzenia (Hurt Cam) — bez kolizji, wycieków pamięci ani błędów numerycznych.

---

## 35. Testy Środowiska Wizualnego Świata (Etap 8C2: Lighting, Full Bright, Time Changer, Weather Changer)

W Etapie 8C2 wprowadzono kompleksowy pakiet testów jednostkowych i integracyjnych pokrywający wszystkie mechanizmy oświetlenia, rozjaśnienia sceny, zmiany pory dnia i pogody (710+ testów w module `:core`):

### 35.1. Testy Menedżera Oświetlenia (`DynamicLightManagerTest`)
- `testDefaultStateAndEnable`: Domyślny stan wyłączony, poprawne włączenie i aktywacja platformowego providera.
- `testCollectCandidateSourcesWithLimits`: Poprawne odfiltrowanie źródeł powyżej `maxLightDistance` (24 m) oraz ograniczenie do `maxActiveSources` (16) z odrzucaniem najdalszych wg $d^2$.
- `testLightFalloffFormula`: Weryfikacja fizycznego modelu spadku oświetlenia wraz z odległością wg wzoru $I(d) = I_0 (1 - d/R)^\gamma$.
- `testColorModeResolution`: Prawidłowe rozwiązywanie kolorów ARGB dla trybów `NATURAL`, `WARM`, `COOL` i `CUSTOM`.
- `testFlickerModulation`: Test oscylacji jasności migotania płomienia bez przekraczania dozwolonego przedziału $[0, 15]$.
- `testClearOnWorldChange`: Natychmiastowe zerowanie listy aktywnych źródeł światła po wywołaniu `clear()`.

### 35.2. Testy Kontrolera Full Bright (`FullBrightControllerTest`)
- `testModes`: Prawidłowe wyliczanie jasności dla trybów `VANILLA` (0.0), `FULL_BRIGHT` (15.0), `CUSTOM` (zadanego przez suwak) oraz `NIGHT_VISION_STYLE` (12.0).
- `testSmoothTransitionAnimation`: Płynna interpolacja jasności w zadanym czasie `transitionDurationMs` wg krzywej Ease-In-Out.
- `testEffectiveGammaComputation`: Wyliczanie współczynnika gamma z redukcją wyblaknięcia $\gamma = (I/15)^{0.85}$ bez naruszania opcji gry.
- `testShadowPreservationInTotalDarkness`: Zachowanie subtelnej głębi mroku w skrajnej ciemności (clamping do 14.0).
- `testComputeBoostedLightLevel`: Prawidłowe podnoszenie poziomu światła bloku $[original, 15]$.

### 35.3. Testy Kontrolera Czasu Świata (`WorldTimeVisualControllerTest`)
- `testPresetValuesAndSkyAngle`: Weryfikacja astronomicznych kątów nieba dla presetów `SUNRISE` (23000), `NOON` (6000), `SUNSET` (12000), `MIDNIGHT` (18000).
- `testCircularShortestPathInterpolation`: Najkrótsza droga na okręgu zegara 24000 ticków (np. przejście z 23000 do 1000 pokonuje 2000 ticków, a nie 22000).
- `testDimensionFiltering`: Poprawne aplikowanie w Overworld i ignorowanie w Netherze/The End przy domyślnej konfiguracji.
- `testPreservationOfTrueWorldTime`: Nienaruszalność rzeczywistego czasu świata pobieranego przez moduły `ClockModule` i `DayCounterModule`.

### 35.4. Testy Kontrolera Pogody (`WeatherVisualControllerTest`)
- `testWeatherPresets`: Weryfikacja gradientów dla `CLEAR` (0.0/0.0), `RAIN` (1.0/0.0), `THUNDER` (1.0/0.8).
- `testSmoothGradientTransitions`: Płynne rozjaśnianie i ściemnianie deszczu/burzy bez skoków luminancji.
- `testBiomeClimateRespect`: Zerowanie deszczu na pustyniach i sawannach przy włączonej opcji `respectBiomeClimate`.
- `testWeatherParticleLimit`: Egzekwowanie twardego limitu cząsteczek `maxWeatherParticles` (50 - 2000).

### 35.5. Testy Centralnego Koordynatora (`ZombekWorldVisualControllerTest`)
- `testPipelineExecutionAndOrder`: Sprawdzenie deterministycznej sekwencji (Vanilla -> Weather -> Time -> Lighting -> Full Bright).
- `testSnapshotCreation`: Niemutowalna migawka `WorldVisualOverrideState` z kompletnym stanem podsystemów.
- `testLifecycleHandling`: Prawidłowe reagowanie na `onClientTick`, `onWorldChange` i `shutdown`.

### 35.6. Kompleksowy Test Integracyjny Etapu 8C2 (`Etap8C2IntegrationTest`)
- `testAllModulesRegistered`: Potwierdzenie obecności `LightingModule`, `FullBrightModule`, `TimeChangerModule`, `WeatherChangerModule` w `ModuleManager`.
- `testCoordinatedWorldVisualTick`: Równoczesna praca wszystkich 4 modułów środowiskowych podczas ticku klienta bez wycieków pamięci, kolizji ani błędów numerycznych.

## 36. Testy Efektów Renderowania (Etap 8C3: Fog Customizer, Color Saturation, Glint Colorizer, Particle Changer)

W Etapie 8C3 zaimplementowano pełny pakiet testów jednostkowych i integracyjnych pokrywających modyfikacje mgły, gradacji barw, kolorowania zaklęć i cząsteczek (740+ testów w module `:core`):

### 36.1. Testy Kontrolera Mgły (`FogVisualControllerTest`)
- `testSafeDistanceClipping`: Weryfikacja zacisku odległości mgły (`start < end`, `start >= minDistance`).
- `testNeverExceedRenderDistance`: Gwarancja, że mgła nigdy nie przekroczy zasięgu renderowania chunków (`viewDistance`), wykluczając wallhack i błędy bufora głębi.
- `testFogColorBlending`: Poprawne mieszanie koloru niestandardowego z barwą waniliową wg współczynnika `colorBlend`.
- `testModuleSyncAndPresets`: Weryfikacja synchronizacji modułu `FogCustomizerModule` z `FogVisualController` i profilami `FogPreset`.

### 36.2. Testy Gradacji Barw (`ColorGradingControllerTest`)
- `testMonochromeProfileMath`: Potwierdzenie pełnej desaturacji profilu monochromatycznego ($R = G = B = L$).
- `testPreserveHudColors`: Gwarancja, że barwy docelowe `TargetType.HUD` pozostają w 100% nienaruszone przy aktywnej fladze `preserveHud`.
- `testArgbPacking`: Sprawdzenie poprawności konwersji i zachowania kanału alfa w formacie 32-bit ARGB.

### 36.3. Testy Kolorowania Glintu (`GlintRenderControllerTest`)
- `testStaticGlintColorCalculation`: Rozwiązywanie jednolitego koloru ARGB glintu.
- `testRainbowAnimationCyclesHue`: Potwierdzenie dynamicznej rotacji barw na kole barw w czasie rzeczywistym bez alokacji `RenderType`.
- `testCategoryOverrides`: Weryfikacja nadpisywania kolorów dla kategorii broni (`WEAPONS`), narzędzi (`TOOLS`) oraz domyślnego fallbacku dla ksiąg (`BOOKS`).
- `testLocationFiltering`: Filtrowanie widoczności glintu wg lokalizacji (`HOTBAR`, `CONTAINERS`).

### 36.4. Testy Kontrolera Cząsteczek (`ParticleRenderControllerTest`)
- `testDistanceCutoff`: Odrzucanie cząstek wykraczających poza promień `maxDistanceCutoff`.
- `testMaxParticleLimitCap`: Twarda blokada generowania cząstek po osiągnięciu limitu `maxParticleLimit`.
- `testCustomParticleRuleResolution`: Poprawne skalowanie, barwienie i przezroczystość dla reguły ciosu krytycznego (`crit`).

### 36.5. Testy Centralnego Koordynatora Efektów (`ZombekRenderEffectControllerTest`)
- `testSafeWorldLeaveRestoration`: Automatyczne przywracanie stanu waniliowego dla wszystkich 4 kontrolerów przy opuszczeniu świata (`onWorldLeave`).
- `testSnapshotIntegrity`: Integralność zrzutu diagnostycznego `RenderEffectSnapshot`.
- `testPipelineLayerPriorityOrdering`: Sprawdzenie poprawnej hierarchii warstw: `WORLD_FOG` (10) < `POST_PROCESSING` (20) < `ITEM_RENDERING` (30) < `PARTICLE_RENDERING` (40).

### 36.6. Kompleksowy Test Integracyjny Etapu 8C3 (`Etap8C3IntegrationTest`)
- `testModulesRegisteredInCore`: Potwierdzenie rejestracji `FogCustomizerModule`, `ColorSaturationModule`, `GlintColorizerModule`, `ParticleChangerModule` w `ZombekCore`.
- `testFogCustomizerIntegration`: Integracja przełącznika i presetów mgły z silnikiem klienta.
- `testColorSaturationIntegration`: Poprawność aktywacji i konfiguracji nasycenia barw.
- `testGlintColorizerIntegration`: Integracja kolorowania ekwipunku z hierarchią priorytetów.
- `testParticleChangerIntegration`: Integracja redukcji cząstek i presetów dostępności.
- `testUnifiedSnapshotAndTickDispatch`: Bezproblemowe wykonanie `onClientTick` przez centralny koordynator bez wyjątków.






