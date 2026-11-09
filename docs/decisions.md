# Decyzje Architektoniczne (ADR) — ZombekClient

Niniejszy dokument rejestruje kluczowe decyzje architektoniczne podjęte przy tworzeniu fundamentów ZombekClient.

---

### ADR-001: Wybór pnpm workspaces jako menadżera pakietów
- **Status:** Zaakceptowane
- **Kontekst:** Projekt zawiera aplikację frontendowo-desktopową (Launcher) oraz pakiety współdzielone w TypeScript. Potrzebny jest szybki, deterministyczny menadżer pakietów oszczędzający miejsce na dysku.
- **Decyzja:** Wybrano `pnpm` wraz z `pnpm-workspace.yaml`.
- **Konsekwencje:**
  - Szybka instalacja dzięki mechanizmowi hardlinków i globalnego store.
  - Ścisła izolacja zależności (brak phantom dependencies).
  - Wymóg definiowania wewnętrznych zależności poprzez protokół `workspace:*`.

---

### ADR-002: Separacja kodu Launchera i Modyfikacji Minecrafta
- **Status:** Zaakceptowane
- **Kontekst:** Launcher i modyfikacja działają w dwóch całkowicie różnych środowiskach uruchomieniowych (Node.js/Chromium vs OpenJDK).
- **Decyzja:** Podział na dwa odrębne katalogi najwyższego poziomu w `apps/`: `apps/launcher` (Electron + TS) i `apps/minecraft-client/fabric` (Java + Gradle).
- **Konsekwencje:**
  - Całkowity brak konfliktów narzędziowych (npm vs gradle).
  - Możliwość niezależnego budowania, testowania i wersjonowania każdego z komponentów.
  - Łatwa rozbudowa o kolejne modloadery (Forge, NeoForge, Quilt).

---

### ADR-003: Model Bezpieczeństwa Electron (Zero Node w Rendererze)
- **Status:** Zaakceptowane
- **Kontekst:** Aplikacje Electron bywają podatne na ataki RCE (Remote Code Execution), jeśli proces renderujący posiada dostęp do bibliotek Node.js.
- **Decyzja:**
  - Wymuszenie `nodeIntegration: false`, `contextIsolation: true`, `sandbox: true`.
  - Wystawianie funkcjonalności wyłącznie przez bezpieczny Preload Script (`contextBridge`).
  - Zdefiniowanie wszystkich metod w pakiecie `@zombek/contracts`.
- **Konsekwencje:**
  - Renderer jest traktowany jak bezpieczna przeglądarka internetowa.
  - Nawet w przypadku wstrzyknięcia złośliwego skryptu niemożliwe jest bezpośrednie wywołanie poleceń systemowych lub operacji na plikach.

---

### ADR-004: Wyodrębnienie pakietów współdzielonych do `packages/`
- **Status:** Zaakceptowane
- **Kontekst:** Launcher, przyszłe testy E2E, mikroserwis kosmetyków oraz narzędzia CLI wymagają dostępu do spójnych definicji typów i schematów konfiguracyjnych.
- **Decyzja:** Utworzenie pakietów:
  - `@zombek/shared-types` — podstawowe typy domenowe,
  - `@zombek/config-schema` — schematy konfiguracji i ich walidacja,
  - `@zombek/contracts` — definicje kanałów IPC i bezpiecznych interfejsów mostka.
- **Konsekwencje:**
  - Jedno źródło prawdy dla kontraktów.
  - Możliwość kompilacji i testowania pakietów w całkowitej izolacji od UI.

---

### ADR-005: Wybór Fabric Loom jako pierwszego modułu Minecraft Client
- **Status:** Zaakceptowane
- **Kontekst:** Minecraft modding posiada kilka loaderów (Fabric, Forge, NeoForge, Quilt). Fabric charakteryzuje się nowoczesnym i szybkim łańcuchem budowania (Loom), doskonałą modularnością i wysoką wydajnością dla klientów PvP.
- **Decyzja:** Jako pierwszy loader wybrano Fabric 1.20.4+ z Fabric Loom 1.7+.
- **Konsekwencje:**
  - Moduł umieszczony w `apps/minecraft-client/fabric/`.
  - Interfejsy `IClientModule`, `IHudElement`, `IClientConfig` zaprojektowane w sposób niezależny od specyfiki Fabrica, co umożliwi łatwe przeniesienie logiki do Forge/NeoForge.

---

### ADR-006: Autorska Identyfikacja Wizualna "Christmas Minecraft & Ząbek Mascot"
- **Status:** Zaakceptowane (Etap 2)
- **Kontekst:** Launcher inspirowany jest funkcjonalnie Lunar Clientem, ale wymaga unikalnej, rozpoznawalnej tożsamości wizualnej i atmosfery Minecrafta z motywem zimowo-świątecznym.
- **Decyzja:** 
  - Zaprojektowanie oryginalnej maskotki Ząbek w pixel-artowym stylu Minecrafta (z zieloną skórą, czapką mikołaja, szalikiem i charakterystycznym wystającym ząbkiem).
  - Wdrożenie palety kolorystycznej opartej na głębokim granacie, leśnej zieleni (pine), świątecznej czerwieni i złotych akcentach.
  - Dodanie lekkiego, sprzętowo wydajnego efektu opadającego śniegu na Canvasie z możliwością regulacji i wyłączenia.
- **Konsekwencje:**
  - Silna tożsamość wizualna wyróżniająca ZombekClient na tle generycznych launcherów.
  - Pełna kontrola nad grafikami SVG bez zewnętrznych zależności graficznych.

---

### ADR-007: Komponentowa Architektura Frontendowa i React Context
- **Status:** Zaakceptowane (Etap 2)
- **Kontekst:** W Etapie 2 konieczne było stworzenie 9 pełnych widoków launchera z bogatymi interakcjami (katalog modów, edytor HUD na żywo, instalacje, konta, powiadomienia Toast, modale) bez implementowania fałszywego backendu.
- **Decyzja:**
  - Podział na reużywalne prymitywy UI (`components/common/`), strukturę układu (`components/layout/`), dedykowane strony (`pages/`) oraz centralny `LauncherProvider` zarządzający stanem demonstracyjnym.
  - Wyraźne oznaczenie demonstracyjnych funkcji etykietami `DEMO` i `UI PREVIEW`.
- **Konsekwencje:**
  - Modułowy, czytelny kod bez monolitycznych plików.
  - Gotowość na bezproblemowe podłączenie prawdziwego silnika pobierania i autoryzacji w Etapie 3.

---

### ADR-008: Przechowywanie danych gier i instancji poza repozytorium (%APPDATA%/ZombekClient)
- **Status:** Zaakceptowane (Etap 3)
- **Kontekst:** Pobierane pliki gry Minecraft (setki megabajtów bibliotek i assetów oraz zapisy światów) nie mogą zanieczyszczać repozytorium gita.
- **Decyzja:** Zaimplementowano moduł `EnginePaths` zarządzający ścieżkami w katalogu systemowym `%APPDATA%/ZombekClient` (`game-data/assets`, `game-data/libraries`, `instances/<id>`).
- **Konsekwencje:**
  - Repozytorium kodu źródłowego pozostaje czyste i lekkie.
  - Możliwość współdzielenia pobranych bibliotek i obiektów assetów pomiędzy wszystkimi instancjami gry.
  - Ochrona przed atakami Path Traversal poprzez mechanizm `assertSafeFilename` i walidację `isSafePath`.

---

### ADR-009: Uruchamianie gry wyłącznie przez `child_process.spawn`
- **Status:** Zaakceptowane (Etap 3)
- **Kontekst:** Uruchomienie gry z przekazaniem setek argumentów i ścieżek przez `child_process.exec` rodzi ryzyko wstrzyknięcia poleceń powłoki (shell injection) oraz limitu długości wiersza poleceń powłoki Windows.
- **Decyzja:** Użyto wyłącznie `child_process.spawn` z jawnie przekazaną tablicą argumentów bez uruchamiania powłoki pośredniczącej (`shell: false`).
- **Konsekwencje:**
  - Całkowite wyeliminowanie podatności Command Injection.
  - Możliwość asynchronicznego strumieniowania stdout i stderr linijka po linijce bezpośrednio do UI launchera i pliku `latest.log`.
  - Niezawodne śledzenie PID procesu i detekcja kodów zakończenia gry.

---

### ADR-010: Weryfikacja sum kontrolnych SHA-1 i atomowy zapis plików
- **Status:** Zaakceptowane (Etap 3)
- **Kontekst:** Pobieranie tysięcy małych plików gry i bibliotek przez sieć może ulec przerwaniu, co prowadziłoby do uszkodzenia plików gry i błędów uruchomienia JVM (np. `CorruptGZIPException` lub `ClassNotFoundException`).
- **Decyzja:** Wszystkie pliki są pobierane z rozszerzeniem tymczasowym `.tmp.<timestamp>`, a po zakończeniu sprawdzana jest ich suma kontrolna SHA-1 oraz rozmiar. Dopiero po pomyślnej weryfikacji następuje atomowe przemianowanie (`fs.promises.rename`) na docelową ścieżkę.
- **Konsekwencje:**
  - Gwarancja integralności wszystkich plików gry na dysku.
  - Możliwość bezpiecznego wznawiania i ponawiania pobierania bez pozostawiania uszkodzonych plików.

---

### ADR-011: Bezpieczne przechowywanie tokenów w Electron safeStorage (DPAPI)
- **Status:** Zaakceptowane (Etap 4)
- **Kontekst:** Przechowywanie tokenów odświeżania Microsoft oraz tokenów dostępu Minecraft Services w formacie jawnym (JSON, localStorage) rodzi krytyczne ryzyko kradzieży tożsamości gracza.
- **Decyzja:** Tokeny są natychmiast szyfrowane za pomocą `safeStorage` (wykorzystującego Windows DPAPI powiązane z poświadczeniami zalogowanego użytkownika systemu) i zapisywane w osobnym pliku `tokens.enc`.
- **Konsekwencje:**
  - Plik `accounts.json` zawiera wyłącznie niesekretne metadane konta.
  - Nawet w przypadku skopiowania katalogu `%APPDATA%/ZombekClient` na inny komputer lub profil, odszyfrowanie tokenów bez klucza systemowego DPAPI jest niemożliwe.
  - Tokeny nigdy nie trafiają do pamięci procesu renderującego (React) ani do logów konsoli.

---

### ADR-012: OAuth 2.0 PKCE w zewnętrznej przeglądarce systemowej
- **Status:** Zaakceptowane (Etap 4)
- **Kontekst:** Logowanie kontem Microsoft wymaga bezpiecznego przepływu bez przechwytywania haseł przez aplikację i bez używania niebezpiecznych, zagnieżdżonych kontrolek webview.
- **Decyzja:** Zastosowano standard RFC 8252 dla aplikacji natywnych: generowanie pary kluczy PKCE (`code_verifier` i `code_challenge`), uruchomienie bezpiecznego serwera loopback na interfejsie `127.0.0.1`, otwarcie domyślnej przeglądarki użytkownika przez `shell.openExternal` i walidację parametru `state`.
- **Konsekwencje:**
  - Użytkownik loguje się w zaufanym środowisku swojej przeglądarki (z obsługą kluczy FIDO2/Windows Hello i 2FA).
  - Aplikacja desktopowa nigdy nie ma dostępu do hasła użytkownika.
  - Pełna ochrona przed atakami typu CSRF i Code Interception.

---

### ADR-013: Dynamiczne metadane Fabric z oficjalnego Fabric Meta API oraz lokalny cache dyskowy
- **Status:** Zaakceptowane (Etap 5A)
- **Kontekst:** Wersje Minecraft i wersje Fabric Loader stale ewoluują. Twarde kodowanie wersji uniemożliwiłoby uruchamianie nowszych wydań gry. Jednocześnie awaria sieci lub brak internetu nie powinny blokować uruchomienia już pobranej gry.
- **Decyzja:** Zaimplementowano klienta `FabricMetaService` korzystającego wyłącznie z oficjalnego API `https://meta.fabricmc.net/v2` (`/versions/game`, `/versions/loader/{game_version}`, `/versions/loader/{game_version}/{loader_version}/profile/json`). Odpowiedzi są zapisywane w pliku `%APPDATA%/ZombekClient/game-data/fabric_meta_cache.json`.
- **Konsekwencje:**
  - Zawsze aktualna lista stabilnych wersji Fabric Loader bez konieczności aktualizacji aplikacji.
  - Pełna odporność na brak połączenia z siecią (offline fallback z dyskowego pliku bufora).
  - Wyeliminowanie zależności od nieoficjalnych lub podejrzanych mirrorów.

---

### ADR-014: Wzorzec ModLoaderAdapter, dziedziczenie profili i izolacja folderu mods
- **Status:** Zaakceptowane (Etap 5A)
- **Kontekst:** Architektura launchera musi obsługiwać w przyszłości różne modloadery (Fabric, Forge, NeoForge, Quilt) bez tworzenia powielonych silników uruchamiania. Modyfikacje dodane do jednego profilu nie mogą wpływać na inne profile.
- **Decyzja:**
  - Wprowadzono generyczny interfejs `ModLoaderAdapter` implementowany przez `FabricLoaderAdapter`.
  - Mechanizm dziedziczenia (`inheritsFrom`): profil Fabric rozszerza profil Vanilla — łączy biblioteki Maven z bibliotekami Mojang, przejmuje argumenty JVM oraz ustawia `mainClass` na `net.fabricmc.loader.impl.launch.knot.KnotClient`.
  - Plik JAR klienta gry jest pobierany ze źródła Mojang dla bazowej wersji (`versions/<game_version>/<game_version>.jar`) i współdzielony na poziomie Classpath.
  - Każda instalacja posiada w pełni izolowany katalog `instances/<id>/mods/` oraz `instances/<id>/config/`.
- **Konsekwencje:**
  - Silnik `GameLauncher` i `GameArgumentsBuilder` obsługują zarówno Vanilla jak i Fabric w jednym, eleganckim przepływie.
  - Czysty kod przygotowany do implementacji Forge w kolejnym etapie.
  - Zero konfliktów modów między różnymi wersjami Minecrafta.

---

### ADR-015: Oficjalny Instalator Forge, Izolowane Środowisko Staging i Zaufanie Certyfikatów Windows
- **Status:** Zaakceptowane (Etap 5B)
- **Kontekst:** W przeciwieństwie do Fabrica (gdzie profil JSON i biblioteki są bezpośrednio dostarczane przez Meta API), Minecraft Forge wykorzystuje skomplikowany proces patchowania binarnego klienta gry za pomocą procesorów (narzędzi takich jak `BinaryPatcher`, `SpecialSource`, `JarSplitter`). Standardowy instalator Forge domyślnie instaluje pliki bezpośrednio w `%APPDATA%/.minecraft`, co zanieczyszczałoby globalną instalację użytkownika i groziło konfliktami profili. Ponadto w środowiskach Windows instalator Forge uruchamiany w Javie potrafi odrzucić połączenia SSL Maven z błędem `PKIX path building failed`, jeśli w systemie działa proxy lub korporacyjne zaufane CA nieobecne w domyślnym pliku `cacerts` Javy.
- **Decyzja:**
  - Zastosowano dedykowany, izolowany katalog pośredni (`game-data/forge-staging/`).
  - Uruchamianie oficjalnego instalatora Forge (`java -jar forge-installer.jar --installClient <stagingDir>`) w ściśle kontrolowanym środowisku z przekazaniem flagi systemowej `-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT`.
  - Po pomyślnym zakończeniu instalatora wygenerowane biblioteki i wersje są synchronizowane do globalnych struktur ZombekClient (`game-data/libraries/` i `game-data/versions/`), a katalog staging jest bezpiecznie czyszczony.
  - Implementacja modułu `ForgeProcessorManager` do dynamicznego rozwiązywania zmiennych w tokenach `{...}` i nawiasach koordynatów Maven `[...]` wraz z filtracją procesorów dla strony `client`.
  - Profile Forge korzystają z mechanizmu `inheritsFrom` bazowego profilu Vanilla, z klasą główną `net.minecraftforge.bootstrap.ForgeBootstrap` lub starszymi punktami wejścia FML.
- **Konsekwencje:**
  - 100% gwarancja czystości środowiska użytkownika: oficjalny katalog `%APPDATA%/.minecraft` nigdy nie jest modyfikowany.
  - Pełna zgodność z binarnymi patchami tworzonymi przez oficjalny zespół Minecraft Forge.
  - Odporność na problemy certyfikatów SSL na systemach Windows dzięki zaufaniu magazynu certyfikatów głównego systemu operacyjnego.
  - Każda instalacja Forge posiada własny izolowany folder `instances/<id>/mods/` i `config/`.

---

### ADR-016: Architektura Dwugeneracyjna NeoForge (Transition & Modern) i Izolacja Instalatora
- **Status:** Zaakceptowane (Etap 5C)
- **Kontekst:** Projekt NeoForge powstał jako fork Minecraft Forge w połowie 2023 roku. W wersji 1.20.1 publikował artefakty pod historycznymi współrzędnymi Maven `net.neoforged:forge` z wersjami typu `1.20.1-47.1.x`, natomiast od wersji Minecraft 1.20.2 przeszedł na własne współrzędne `net.neoforged:neoforge` i nową konwencję numeracji semver odpowiadającą wydaniom gry (`20.2.x`, `20.4.x`, `21.1.x`). NeoForge nie istnieje dla wydań starszych niż 1.20.1. Dodatkowo oficjalny instalator NeoForge wymaga w katalogu docelowym pliku `launcher_profiles.json` oraz flagi `--installClient`, domyślnie próbując pisać do globalnego `.minecraft`.
- **Decyzja:**
  - Zaimplementowano w `NeoForgeMetaService` automatyczne wykrywanie generacji na podstawie wersji gry:
    - Dla 1.20.1: pobieranie z `https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml`.
    - Dla 1.20.2+: pobieranie z `https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml`.
    - Dla wydań <= 1.19.4: jawne oznaczanie jako nieobsługiwane i zwracanie pustej listy wersji.
  - Wykonywanie instalatora w dedykowanym katalogu `game-data/neoforge-staging/` z automatycznie generowanym minimalnym `launcher_profiles.json` (`{ "profiles": {} }`) i flagą `-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT`.
  - Stworzenie modułu `NeoForgeProcessorManager` do niezależnego rozwiązywania procesorów NeoForge (`install_profile.json`), obsługującego opcjonalne pole `path` oraz specyficzne słowniki danych NeoForge (`MC_SLIM`, `PATCHED`, `MERGED_MAPPINGS`).
  - Integracja profilu w `GameLauncher` z klasą główną `cpw.mods.bootstraplauncher.BootstrapLauncher` oraz zachowanie całkowitej izolacji katalogów `instances/<id>/mods/` i `config/`.
- **Konsekwencje:**
  - Użytkownik widzi wyłącznie realnie dostępne i działające wersje NeoForge.
  - Pełna separacja środowisk: 4 modloadery (Vanilla, Fabric, Forge, NeoForge) działają obok siebie w jednym launcherze bez żadnych konfliktów.
  - Środowisko systemowe użytkownika pozostaje w 100% czyste.

---

### ADR-017: Quilt Loader, Quilt Meta v3 i polityka niestosowania przestarzałego QSL
- **Status:** Zaakceptowane (Etap 5D)
- **Kontekst:** Quilt to nowoczesny, modułowy modloader rozwijany przez społeczność QuiltMC, wykorzystujący zaawansowany silnik ładowania Knot. Quilt udostępnia oficjalne API metadanych Quilt Meta v3 pod adresem `https://meta.quiltmc.org/v3`, które wymaga identyfikującego nagłówka `User-Agent`. W grudniu 2025 r. projekt QuiltMC podjął oficjalną decyzję o zakończeniu rozwoju i wycofaniu (discontinuation) biblioteki QSL (Quilt Standard Libraries), zalecając czyste środowisko loadera. Jednocześnie launcher wymaga przygotowania modelu danych pod przyszły menedżer modpacków (rozpoznawanie modów Fabric kompatybilnych z Quilt).
- **Decyzja:**
  - Zaimplementowano `QuiltMetaService` komunikujący się z oficjalnym Quilt Meta v3 z nagłówkiem `User-Agent: ZombekClient/0.1.0 (desktop launcher)` oraz buforem dyskowym `%APPDATA%/ZombekClient/game-data/quilt_meta_cache.json`.
  - Zaimplementowano `QuiltLoaderAdapter` implementujący wspólny kontrakt `ModLoaderAdapter` dla `loaderType: 'quilt'`, z klasą główną `org.quiltmc.loader.impl.launch.knot.KnotClient` oraz profilem launchera łączonym z bazową wersją Vanilla (`inheritsFrom`).
  - Zgodnie z wytycznymi QuiltMC z grudnia 2025 r. instalacja Quilt **nie wstrzykuje automatycznie QSL** jako obowiązkowej biblioteki — instalowany jest czysty, stabilny Quilt Loader.
  - Zachowano pełną izolację katalogu instancji `instances/<id>/mods/`, `config/`, `saves/`, `resourcepacks/`, `screenshots/`, `natives/`, `logs/`.
  - Wprowadzono typy klasyfikacji `ModCompatibilityTarget` (`quilt`, `fabric`, `fabric-and-quilt`, `quilt-incompatible`, `forge`, `neoforge`) pod kątem przyszłego menedżera modpacków.
- **Konsekwencje:**
  - Launcher ZombekClient w pełni obsługuje komplet 5 wiodących środowisk Minecraft Java Edition: Vanilla, Fabric, Forge, NeoForge i Quilt.
  - Środowiska nie kolidują ze sobą — każda instancja posiada własny katalog modów i ustawień.
  - Nowoczesna instalacja Quilt bez przestarzałego balastu QSL.

---

### ADR-018: Bezpieczny Odczyt Metadanych JAR w Pamięci, Zero Java Execution i Strategia Folderu disabled-mods
- **Status:** Zaakceptowane (Etap 6A)
- **Kontekst:** W Etapie 6A tworzony jest rdzeń zarządzania modami i modpackami dla 5 środowisk gry. Mody do Minecrafta są archiwami ZIP zawierającymi skompilowane klasy `.class` oraz pliki konfiguracyjne (`fabric.mod.json`, `quilt.mod.json`, `META-INF/mods.toml`, `META-INF/neoforge.mods.toml`). Uruchamianie zewnętrznego procesu Javy lub ładowanie obcych klas w celu odczytania nazwy czy wersji modułu stanowiłoby poważne zagrożenie bezpieczeństwa (arbitrary code execution) oraz drastycznie degradowało wydajność launchera. Ponadto wyłączanie modów poprzez zmianę rozszerzenia (np. na `.jar.disabled`) psuje skojarzenia plików w systemie i rodzi problemy przy synchronizacji modpacków.
- **Decyzja:**
  - **Zero Java Execution:** Żaden plik `.jar` nie jest uruchamiany w JVM. Zaimplementowano dedykowany parser `JarArchiveReader` w czystym TypeScript/Node.js, odczytujący struktury Central Directory bez rozpakowywania całego archiwum na dysk.
  - **Ochrona przed podatnościami:** Wprowadzono ścisłe zabezpieczenia przed atakami ZipSlip (`..`, ścieżki bezwzględne, separatory napędów Windows) oraz limitami zasobów (maksymalny rozmiar pliku 500 MB, limit 15 000 wpisów w archiwum, limit pojedynczego wpisu tekstowego 5 MB).
  - **Strategia `disabled-mods/`:** Wyłączone mody są przenoszone do katalogu `instances/<id>/disabled-mods/` z zachowaniem oryginalnej nazwy i rozszerzenia `.jar`. Ponieważ Minecraft skanuje wyłącznie katalog `mods/`, mod jest w 100% zignorowany przez silnik gry.
  - **Autonomiczny parser TOML:** Moduł `SimpleTomlParser` przetwarza pliki TOML formatów Forge i NeoForge bez konieczności instalowania zewnętrznych pakietów npm.
  - **Głębokie klonowanie profili modpacków:** Kopiowanie modpacka w `ModpackProfileManager` tworzy niezależną kopię plików modów oraz katalogu konfiguracji, zapobiegając wzajemnemu nadpisywaniu ustawień.
- **Konsekwencje:**
  - Błyskawiczny i bezpieczny odczyt metadanych modów nawet dla dużych paczek (ponad 200 modów).
  - Brak ryzyka wykonania złośliwego kodu Java podczas przeglądania lub dodawania modów do launchera.
  - Czysta i niezawodna struktura plików — brak zaśmiecania rozszerzeń `.jar.disabled`.

---

### ADR-019: Integracja z Modrinth API v2, Weryfikacja Sum Kontrolnych SHA-512 i Ochrona przed Rate Limiting (HTTP 429)
- **Status:** Zaakceptowane (Etap 6B)
- **Kontekst:** Modrinth jest wiodącą otwartą platformą hostingową modyfikacji dla Minecraft Java Edition. Jej oficjalne API v2 (`https://api.modrinth.com/v2`) nakłada precyzyjne wytyczne dotyczące używania nagłówka `User-Agent` (musi zawierać nazwę aplikacji i kontakt/URL), przestrzegania limitów zapytań (standardowo 300 zapytań/minutę na IP, nagłówki `x-ratelimit-*`), oraz specyficznej składni zapytań `facets` (tablica tablic JSON). Ponadto bezpośrednie pobieranie plików JAR z internetu wymaga ścisłej weryfikacji domen CDN, integralności pliku (Modrinth dostarcza sumy SHA-512 oraz SHA-1) oraz ochrony przed atakami SSRF i niebezpiecznymi protokołami.
- **Decyzja:**
  - **Zgodność z polityką API:** Wprowadzono `ModrinthApiClient` z nagłówkiem `User-Agent: ZombekClient/0.1.0 (https://github.com/zombekclient/zombekclient)`. Wszystkie zapytania są śledzone pod kątem nagłówków `x-ratelimit-remaining` i `x-ratelimit-reset`. W przypadku napotkania HTTP 429 zgłaszany jest dedykowany `ModrinthRateLimitError`, a interfejs użytkownika blokuje nadmierne odpytywanie i wyświetla licznik czasu odnowienia.
  - **Dwuwarstwowy Cache:** Wyniki wyszukiwania i metadane projektów buforowane są w pamięci RAM (TTL 2-10 min). Statyczne tagi (`categories.json`, `loaders.json`, `game_versions.json`) zapisywane są trwale na dysku w `%APPDATA%/ZombekClient/cache/modrinth/` z czasem życia 24h, co drastycznie redukuje liczbę zapytań sieciowych.
  - **Bezpieczny Downloader (`ModrinthDownloader`):** Wymuszono protokół HTTPS oraz restrykcyjną białą listę domen CDN (`cdn.modrinth.com`, `edge.modrinth.com`). Wszelkie próby pobierania z obcych hostów, protokołów lokalnych (file://) lub prywatnych adresów IP są natychmiast blokowane wyjątkiem bezpieczeństwa.
  - **Kryptograficzna weryfikacja SHA-512:** Każdy plik pobierany jest do tymczasowego katalogu i sprawdzany pod kątem deklarowanego hashu SHA-512 (z zapasowym fallbackiem do SHA-1). Niezgodność hashu powoduje natychmiastowe usunięcie pliku i przerwanie instalacji przed naruszeniem katalogu gry.
  - **Zgodność Modloaderów i Instancji:** `ModrinthInstallationManager` weryfikuje kompatybilność wersji Minecraft oraz loadera (w tym Quilt wspierający mody Fabric), zapobiega duplikatom poprzez weryfikację nazwy pliku i sumy SHA-256 oraz analizuje wymagane zależności (`dependencies`), ostrzegając użytkownika o brakujących komponentach.
- **Konsekwencje:**
  - ZombekClient zyskał bezpieczną, w 100% oficjalną integrację z bazą tysięcy modów Modrinth.
  - Ochrona użytkownika przed pobieraniem uszkodzonych lub podmienionych plików JAR.
  - Stabilna praca launchera bez ryzyka permanentnego zablokowania IP przez serwery Modrinth.

---

### ADR-020: Rekurencyjny Resolver Zależności, Transakcyjny Menadżer Aktualizacji z Rollbackiem i Modpack Lockfile
- **Status:** Zaakceptowane (Etap 6C)
- **Kontekst:** W poprzednich etapach launcher pozwalał na instalację pojedynczych plików JAR oraz sprawdzanie metadanych. Jednak w ekosystemie Minecraft mody powszechnie zależą od innych bibliotek (np. Iris Shaders wymaga Sodium, który z kolei wymaga Fabric API). Brak automatycznego rozwiązywania zależności skutkował awariami gry podczas startu. Ponadto aktualizacja modów bezpośrednio w katalogu gry stwarzała ryzyko uszkodzenia działającego modpacka w razie utraty połączenia lub błędu pobierania. Modyfikowanie plików podczas działania Minecrafta (gra uruchomiona) prowadziło do blokady plików przez JVM w systemie Windows (`EBUSY` / `EPERM`).
- **Decyzja:**
  - **Rekurencyjny Resolver (`ModDependencyResolver`):** Zbudowano silnik rekurencyjnego rozwiązywania grafu zależności A -> B -> C -> D z detekcją cykli (`visitedProjects`, `visitedVersions`). Wdrożono pełną obsługę 4 typów relacji Modrinth (`required`, `optional`, `incompatible`, `embedded`). Zależności `embedded` (Jar-in-Jar) są rejestrowane jako spełnione w pamięci, zapobiegając dublowaniu plików.
  - **Sprawdzanie konfliktów (`ModConflictResolver`):** Weryfikacja kompatybilności loaderów (w tym Quilt obsługujący Fabric), wersji gry, zadeklarowanych niekompatybilności oraz kolizji z modami o zablokowanej wersji (`pinned`).
  - **Wsadowe zapytania o aktualizacje (`ModUpdateManager`):** Wykorzystanie endpointu `POST /version_files/update` przyjmującego tablicę hashy SHA-512, co pozwala na sprawdzenie aktualizacji setek modów w pojedynczym zapytaniu HTTP. Identyfikacja plików lokalnych i autorskich przez `POST /version_files`.
  - **Modpack Lockfile Schema v1 (`ModpackLockfileManager`):** Wprowadzenie deterministycznego pliku `instances/<id>/modpack.lock.json` przechowującego historię wersji, sumy SHA-512/SHA-1, źródła (`modrinth` vs `local`), flagi przypięcia (`pinned`) oraz polityki stabilności (`stable-only`, `stable-and-beta`, `all-versions`).
  - **Menedżer Kopii Zapasowych (`ModpackBackupManager`):** Tworzenie pełnych migawek plików JAR i pliku `modpack.lock.json` w `%APPDATA%/ZombekClient/backups/<instanceId>/<backupId>/` przed każdą modyfikacją. Ograniczenie retencji do maksymalnie 10 kopii na instancję. Implementacja procedury 1-kliknięcia Rollback przywracającej poprzedni stan i usuwającej pliki dodane po wykonaniu migawki.
  - **Transakcyjność i Bezpieczeństwo (`ModpackTransactionManager`):**
    - Wymuszenie sprawdzenia `GameLauncher.getStatus()`. Jeśli gra jest w stanie `running` na modyfikowanej instancji, operacja jest natychmiast blokowana.
    - Wszystkie pliki są pobierane i weryfikowane pod kątem sumy SHA-512 w katalogu staging `%APPDATA%/ZombekClient/temp-transactions/<txId>/`.
    - Dopiero po 100% poprawnym pobraniu i utworzeniu kopii zapasowej następuje atomowe naniesienie zmian w folderze `mods/` i aktualizacja `modpack.lock.json`.
    - W przypadku jakiegokolwiek błędu uruchamiany jest automatyczny rollback, a folder staging jest bezwzględnie czyszczony.
- **Konsekwencje:**
  - Niemożliwe jest uszkodzenie modpacka podczas aktualizacji — każda operacja jest albo w pełni udana, albo całkowicie wycofana.
  - Użytkownik nie musi ręcznie szukać bibliotek zależnych dla pobieranych modów.
  - Gracz posiada pełną kontrolę nad wersjami (przypinanie modów kluczowych) i w razie problemów z kompatybilnością może cofnąć stan modpacka jednym kliknięciem.

---

### ADR-022: Uniwersalny Framework Modułów w Grze, Izolacja Błędów (Error Boundary) i Świąteczne Menu Konfiguracyjne (RIGHT SHIFT)
- **Status:** Zaakceptowane (Etap 7B)
- **Kontekst:** W Etapie 7A powstał rdzeń modyfikacji Fabric (`ZombekClientMod`). Kolejnym krokiem w tworzeniu zaawansowanego klienta gry (na wzór Lunar Client / Badlion) jest budowa uniwersalnego frameworka modułów działającego bezpośrednio wewnątrz procesu Minecrafta. Wprowadzenie modułów niesie za sobą ryzyko destabilizacji pętli renderowania gry (crash w `onTick` lub `onRender` wysypujący całą grę na pulpit), problemów z wydajnością operacji dyskowych przy regulacji suwaków w GUI, niespójnego systemu typów ustawień oraz konfliktów skrótów klawiszowych.
- **Decyzja:**
  - **Izolacja Błędów (Error Boundary):** Każdy moduł (`AbstractModule`) posiada metody bezpieczne (`safeEnable`, `safeDisable`, `safeTick`, `safeRender`). Wystąpienie wyjątku w kodzie modułu nie propaguje się do głównej pętli Minecrafta; błąd jest rejestrowany w dedykowanym loggerze, moduł otrzymuje status `hasError = true` z opisem błędu, zostaje natychmiast bezpiecznie odłączony (`safeDisable()`), a na karcie modułu pojawia się wyraźna flaga ostrzegawcza.
  - **Podział na 5 Kategorii (`ModuleCategory`):** Moduły pogrupowano w 5 domenowych kategorii: `PVP` (PvP / Combat), `HUD` (HUD / Information), `VISUAL` (Visual), `SURVIVAL` (Survival / World) oraz `OTHER` (Other). Puste kategorie wyświetlają estetyczny placeholder ze śnieżynkami informujący o kolejnych etapach rozwoju.
  - **Uniwersalny System Ustawień (`pl.zombek.client.settings`):** Wdrożono silnie typowaną hierarchię ustawień: `BooleanSetting`, `NumberSetting` (ze sztywnymi granicami, krokiem i precyzją dziesiętną), `ColorSetting` (RGBA/ARGB z heksadecymalnym zapisem `#RRGGBB` / `#RRGGBBAA`), `EnumSetting` (z bezpiecznym cyklicznym przełączaniem w interfejsie), `StringSetting` (z ograniczeniami długości i walidacją regex), `KeybindSetting`, `MultiSelectSetting` oraz grupy `SettingGroup`. Wszystkie ustawienia wspierają warunkową widoczność (`visibleWhen()`) na podstawie predykatów logicznych.
  - **Centralny Menadżer Skrótów (`KeybindManager`):** Domyślnym klawiszem otwierającym menu jest **PRAWY SHIFT (`RIGHT SHIFT` / `GLFW.GLFW_KEY_RIGHT_SHIFT`)**, zarejestrowany jako oficjalny `KeyBinding` biblioteki Fabric. Wdrożono detekcję pojedynczych krawędzi wciśnięć (single press edge), blokadę wyzwalania w trakcie pisania na czacie / w kontenerach oraz wykrywanie konfliktów klawiszy pomiędzy modułami.
  - **Trwałość Konfiguracji (`ModuleConfigManager`):** Konfiguracja zapisywana jest w formacie JSON v1 w pliku `config/zombekclient/modules.json`. Zapis wykonywany jest w sposób atomowy (zapis do `.tmp` i zamiana), z buforowaniem (debouncing 300ms zapobiegający katowaniu dysku przy przeciąganiu suwaków), automatycznym tworzeniem kopii zapasowej (`modules.json.bak`) w razie uszkodzenia pliku oraz zachowywaniem nieznanych kluczy.
  - **Świąteczne Menu w Grze (`ZombekMenuScreen`):** Zbudowano pełny interfejs GUI oparty o Yarn `DrawContext` i `Screen` bez surowych hacków OpenGL. Interfejs jest w 100% responsywny, nie pauzuje gry na serwerach multiplayer (`shouldPause() == false`), posiada lewy sidebar nawigacyjny z licznikami, wyszukiwarkę na żywo, interaktywne karty modułów z przełącznikami ON/OFF, dedykowany panel ustawień modułów z powrotem oraz modalne okno przypisywania klawiszy.
  - **Referencyjny Moduł Deweloperski (`CoreTestModule`):** Zaimplementowano pełnowartościowy moduł demonstracyjny w kategorii `OTHER` testujący wszystkie typy ustawień, warunki widoczności i zdarzenia cyklu życia.
- **Konsekwencje:**
  - Stabilna, profesjonalna baza architektoniczna gotowa na implementację konkretnych modułów PvP, HUD i Visual w kolejnych etapach bez ryzyka crashy gry.
  - Pełna niezależność konfiguracji i wygoda użytkownika (otwieranie menu pod Right Shift, błyskawiczne wyszukiwanie).

---

### ADR-023: Architektura Wielomodułowa Client Core, Port na Minecraft Forge i Bezpieczeństwo Serwera Dedykowanego
- **Status:** Zaakceptowane (Etap 7C1)
- **Kontekst:** W Etapie 7B powstał framework modułów oraz menu dla Fabric (`apps/minecraft-client/fabric`). Wymogiem etapu 7C1 jest wsparcie dla środowiska Minecraft Forge 1.20.4 bez tworzenia drugiego, niezależnego moda ani duplikowania kodu logiki modułów, ustawień, trwałości czy rejestracji. Forge 1.20.4 różni się diametralnie od Fabric: używa Official Mojang Mappings (`GuiGraphics`, `Screen`, `KeyMapping` vs Yarn `DrawContext`, `Screen`, `KeyBinding`), systemu FML (`@Mod`, `IEventBus`, `RegisterKeyMappingsEvent`) oraz magistrali `MinecraftForge.EVENT_BUS`. Dodatkowo, mody client-only na Forge grożą awarią serwera dedykowanego w przypadku odwołania do klas interfejsu graficznego.
- **Decyzja:**
  - **Wydzielenie ZombekClient Core (`:core`):** Wyodrębniono moduł `apps/minecraft-client/core/` jako czystą bibliotekę Java 17 z zerem zależności od Minecrafta, Fabric API i Forge API. Moduł `:core` zawiera `ZombekCore`, `ModuleManager`, `AbstractModule`, `IClientModule`, `Setting<T>`, `SettingGroup`, `ModuleConfigManager` oraz referencyjny `CoreTestModule`.
  - **Kontrakt `IPlatformAdapter`:** Zdefiniowano interfejs platformowy umożliwiający Core interakcję ze środowiskiem gry (nazwa i wersja platformy, ścieżka katalogu konfiguracji, otwieranie menu, wykonywanie zadań w wątku renderowania).
  - **Adapter Forge (`:forge`):** Utworzono moduł `apps/minecraft-client/forge/` dla MC 1.20.4 i Forge 49.2.13 z ForgeGradle 6.0+. Implementuje punkt wejścia `@Mod("zombekclient")`, adapter `ForgePlatformAdapter`, rejestrację `KeyMapping` pod RIGHT SHIFT przez `RegisterKeyMappingsEvent` oraz pętlę tickową w `TickEvent.ClientTickEvent`.
  - **Client-Only Server Safety:** Zastosowano `DisplayTest.IGNORESERVERONLY` w punkcie wejścia, informując serwery sieciowe o braku wymogu obecności modu po stronie serwera. Całość inicjalizacji klienta opakowano w `DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)`.
  - **Ekran `ZombekForgeScreen`:** Zbudowano natywny ekran w Official Mappings (`Screen`, `GuiGraphics`, `Component`), w 100% spójny wizualnie i funkcjonalnie ze świątecznym motywem Festive Dark znanym z wersji Fabric.
  - **Pakowanie samowystarczalnego JAR i Reobfuscation:** Skonfigurowano zadanie `jar` w `:forge` do włączania skompilowanych klas z `:core`, a następnie uruchomienia `reobfJar` (SRG mappings).
  - **Manifest Wydań i Rozpoznawanie w Launcherze:** Utworzono `client-releases.json` oraz `ClientReleaseService` w launcherze, gwarantujący automatyczną instalację właściwego pliku JAR (`zombekclient-fabric.jar` vs `zombekclient-forge.jar`) zależnie od wybranego w instancji loadera.
- **Konsekwencje:**
  - ZombekClient In-Game Client działa w 100% identycznie na obu wiodących platformach: Fabric i Forge.
  - Kod logiki modułów i ustawień jest współdzielony bez żadnej duplikacji.
  - Brak ryzyka awarii serwerów dedykowanych lub serwerów sieciowych przy dołączaniu gracza z ZombekClient na Forge.
  - Pełna integracja z silnikiem launchera i menedżerem instancji.

---

### ADR-024: Architektura NeoForge Client Adapter, Separacja Szyn Zdarzeń i Bezpieczeństwo Serwerów Dedykowanych
- **Status:** Zaakceptowane (Etap 7C2)
- **Kontekst:** Po pomyślnym porcie ZombekClient Core na Fabric i Forge, kolejnym krokiem jest integracja z dynamicznie rozwijającym się loaderem **Minecraft NeoForge 1.20.4 (NeoForge 20.4.x)**. NeoForge oddzielił się od Forge i wprowadził własne mechanizmy architektury: natywne wsparcie dla `net.neoforged.fml.common.Mod`, oddzielną magistralę zdarzeń gry `NeoForge.EVENT_BUS` oraz modyfikacje w strukturze deskryptora `neoforge.mods.toml`. Konieczne było zachowanie 100% spójności ze wspólnym ZombekClient Core (`:core`) bez duplikacji kodu modułów, trwałości konfiguracji czy tokenów graficznych.
- **Decyzja:**
  - **Użycie NeoGradle UserDev (`net.neoforged.gradle.userdev:7.0.80`):** Dla MC 1.20.4 i środowiska Gradle 8.6 wybrano stabilny plugin NeoGradle 7.0 userdev, eliminując wymóg podnoszenia wersji Gradle powyżej 8.8.
  - **Punkt wejścia `@Mod(ZombekNeoForgeMod.MOD_ID)` i izolacja Client-Only:** W punkcie wejścia NeoForge wstrzykiwana jest szyna zdarzeń modu (`IEventBus modEventBus`). Dodatkowo w deskryptorze `neoforge.mods.toml` skonfigurowano regułę `displayTest = "IGNORE_SERVER_VERSION"` oraz zależności `side = "CLIENT"`, co uniemożliwia błędy handshake z serwerami dedykowanymi.
  - **Separacja Szyn Zdarzeń:**
    - `Mod Event Bus`: Rejestracja mapowania klawisza `RegisterKeyMappingsEvent` dla skrótu RIGHT SHIFT.
    - `Game Event Bus (NeoForge.EVENT_BUS)`: Subskrypcja zdarzenia `ClientTickEvent` (faza `Phase.END`), w którym wywoływana jest detekcja zbocza klawisza oraz dispatch ticku do modułów.
  - **Implementacja `NeoForgePlatformAdapter`:** Klasa adaptera implementuje interfejs `IPlatformAdapter` ze współdzielonego Core, zwracając nazwę `"NeoForge"`, wersję z `NeoForgeVersion.getVersion()`, katalog `FMLPaths.CONFIGDIR.get().resolve("zombekclient")` oraz otwierając ekran `ZombekNeoForgeScreen`.
  - **Ekran `ZombekNeoForgeScreen` i Motyw `NeoForgeTheme`:** Zbudowano natywny ekran w Mojang Official Mappings (`Screen`, `GuiGraphics`, `Component`), w 100% spójny z motywem Festive Dark.
  - **Samodzielny Artefakt JAR i Rozszerzenie Manifestu Wydań:** Zbudowano `zombekclient-neoforge-0.1.0-alpha.jar` (67,562 bytes) zawierający połączone klasy z `:core` oraz `:neoforge`, zaktualizowano `client-releases.json` oraz rozbudowano `ClientReleaseService` w launcherze.
- **Konsekwencje:**
  - Klient ZombekClient działa spójnie na 3 wiodących platformach: Fabric, Forge i NeoForge.
  - 100% współdzielenie logiki biznesowej, systemu modułów i konfiguracji bez powielania kodu.
  - Gracze na NeoForge mają dostęp do pełnego menu pod RIGHT SHIFT z identyczną estetyką i funkcjami.

---

### ADR-025: Architektura Quilt Client Adapter, Model Dual-Entrypoint i Integracja bez Wygaszonego QSL
- **Status:** Zaakceptowane (Etap 7C3)
- **Kontekst:** W Etapie 7C3 zamykamy obsługę wszystkich 4 modloaderów dla ZombekClient In-Game Client poprzez dodanie adaptera dla **Quilt Loader 1.20.4 (Quilt 0.27.1-beta.1)**. Ekosystem Quilt przeszedł istotną transformację: w grudniu 2025 r. projekt QuiltMC oficjalnie wygasił bibliotekę Quilt Standard Libraries (QSL). Wymuszenie zależności od QSL lub Quilted Fabric API wprowadzałoby ryzyko niekompatybilności i niepotrzebny narzut pamięciowy. Jednocześnie Quilt posiada własne natywne punkty wejścia (`PreLaunchEntrypoint`, `ModContainer`), mapowania pośrednie (intermediary / hashed), system adnotacji `@ClientOnly` oraz pełną wsteczną kompatybilność z punktami wejścia Fabric `ClientModInitializer`.
- **Decyzja:**
  - **Wspólny Rdzeń (`:core`):** Wykorzystanie dokładnie tego samego, w 100% loader-agnostycznego rdzenia `apps/minecraft-client/core/` bez tworzenia drugiego moda ani dublowania systemu modułów czy ustawień.
  - **Brak Sztucznych Zależności od QSL:** Adapter `apps/minecraft-client/quilt` zależy wyłącznie od czystego `quilt-loader` oraz `minecraft` 1.20.4.
  - **Model Odpornego Podwójnego Punktu Wejścia (Dual-Entrypoint):** `QuiltClientBootstrap` implementuje zarówno oficjalny `org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint` (`onPreLaunch(ModContainer)`), jak i `net.fabricmc.api.ClientModInitializer` (`onInitializeClient()`), zabezpieczone zsynchronizowanym jednokrotnym strażnikiem bootstrapu.
  - **Client-Only Server Safety:**
    - Manifest `quilt.mod.json` posiada deklarację `"environment": "client"`.
    - Klasy klienta oznaczone są adnotacją `@ClientOnly` (`org.quiltmc.loader.api.minecraft.ClientOnly`).
    - Wczesny guard runtime w punkcie wejścia odrzuca uruchomienie na serwerach dedykowanych (`MinecraftQuiltLoader.getEnvironmentType() != EnvType.CLIENT`).
    - Miksin klienta `MinecraftClientMixin` zdefiniowany jest wyłącznie w sekcji `"client"` pliku `zombekclient-quilt.mixins.json`.
  - **Hybrydowy Menadżer Klawiszy (`QuiltKeybindManager`):** Domyślny skrót otwierający menu to **RIGHT SHIFT (`GLFW.GLFW_KEY_RIGHT_SHIFT`)**. Zastosowano hybrydową detekcję: rejestrację w `GameOptions.allKeys` z detekcją zbocza narastającego (rising edge detection) bezpośrednio w pętli tickowej klienta, eliminującą wielokrotne otwieranie/zamykanie menu przy trzymaniu klawisza.
  - **Menu Ekranowe w Yarn (`ZombekQuiltScreen`):** Zbudowano pełnoekranowy interfejs oparty na Yarn `Screen` i `DrawContext`, zgodny z motywem Festive Dark, 5 kategoriami, filtrowaniem na żywo, suwakami wartości i przechwytywaniem klawiszy.
  - **Wbudowanie Core i Dystrybucja w Launcherze:** Zadanie `jar` w `quilt/build.gradle` włącza skompilowane klasy ze wspólnego modułu `:core`, tworząc samowystarczalny artefakt `zombekclient-quilt-0.1.0-alpha.jar` (69 568 bajtów, SHA-256 `ef1608220e502f3745cf0fbb30f147a0d6cd9057b14e01421f6ce814e02eb137`). Rozszerzono `client-releases.json` oraz `ClientReleaseService` o rozpoznawanie i instalację wariantu Quilt.
- **Konsekwencje:**
  - Kompletne pokrycie wszystkich 4 modloaderów (Fabric, Forge, NeoForge, Quilt) w ZombekClient In-Game Client.
  - Identyczne doświadczenie użytkownika i pełna wymienność konfiguracji (`modules.json`) między wszystkimi środowiskami.
  - Maksymalna lekkość i odporność na awarie dzięki braku zależności od wygaszonego QSL.

---

### ADR-026: Architektura Renderowania HUD, Moduły Keystrokes i CPS Counter oraz Izolacja Pomiaru Wejścia
- **Status:** Zaakceptowane (Etap 8A1)
- **Kontekst:** W Etapie 8A1 wdrażamy pierwsze właściwe moduły w grze: Keystrokes oraz CPS Counter. Moduły te muszą działać jednocześnie na 4 modloaderach (Fabric, Forge, NeoForge, Quilt). Powielanie kodu rysowania kafli, tekstu, marginesów i kalkulacji nanosekundowych kliknięć w każdym z 4 modułów platformowych drastycznie zwiększyłoby dług technologiczny i ryzyko desynchronizacji wizualnej. Ponadto błędna detekcja wejścia mogłaby zliczać kliknięcia w menu (GUI) jako CPS lub pozostawiać klawisze w stanie wciśniętym po przełączeniu okna.
- **Decyzja:**
  - **Wspólny Rdzeń HUD (`apps/minecraft-client/core/hud`):** Utworzono loader-agnostyczny kontrakt `IHudRenderer` oraz `IHudElement`, a także centralny rejestr `HudElementRegistry`. Cała matematyka layoutu, kolorów, obramowań, zakotwiczenia (`HudAnchor`) oraz formatowania tekstu modułów `KeystrokesModule` i `CPSModule` znajduje się w 100% w module `:core`.
  - **Lekkie Adaptery Renderowania:** Każdy loader dostarcza jedynie minimalistyczny mostek adaptera (`FabricHudRenderer`, `ForgeHudRenderer`, `NeoForgeHudRenderer`, `QuiltHudRenderer`) implementujący `IHudRenderer` za pomocą natywnego `DrawContext` (Fabric/Quilt) lub `GuiGraphics` (Forge/NeoForge).
  - **Izolacja Pomiaru Czasu Monotonicznego (`ClickTracker`):** Pomiar CPS odbywa się przy użyciu zegara monotonicznego `System.nanoTime()` z przesuwnym oknem czasowym (domyślnie 1000 ms), eliminując wpływ zmian zegara systemowego i nieregularności FPS.
  - **Ścisłe Bezpieczeństwo GUI (`KeyTracker`):** W każdym adapterze klawiatury/myszy wprowadzono regułę natychmiastowego resetowania stanów (`KeyTracker.getInstance().resetAll()`), gdy aktywne jest jakiekolwiek menu ekranowe (`client.screen != null`), gracz nie znajduje się w świecie lub okno traci fokus systemowy.
  - **Zero Autoclickera / Combat Mod:** Moduły są w 100% pasywnymi elementami HUD po stronie klienta, nie ingerują w pakiety sieciowe ani fizykę ataku Minecrafta.
- **Konsekwencje:**
  - Identyczny wygląd i zachowanie Keystrokes oraz CPS Counter na wszystkich 4 platformach.
  - Zabezpieczenie przed zaciętymi klawiszami i fałszywymi kliknięciami w menu.
  - Czysta, rozszerzalna baza pod kolejne moduły HUD w kolejnych etapach.

---

### ADR-027: Wdrożenie Modułów Armor Status, Combo Counter i Cooldowns oraz Rejestru Providerów Danych
- **Status:** Zaakceptowane (Etap 8A2)
- **Kontekst:** W Etapie 8A2 rozszerzamy możliwości ZombekClient o kolejne trzy kluczowe moduły PvP: Armor Status, Combo Counter oraz Cooldowns. Moduły te wymagają dostępu do danych rozgrywki: slotów pancerza i ich zużycia, zdarzeń ataku na przeciwników oraz menedżera cooldownów przedmiotów. Bezpośrednie powiązanie modułów z klasami Minecrafta zniszczyłoby czystość architektury `:core` i uniemożliwiło natychmiastowe testowanie jednostkowe w JUnit 5 bez uruchamiania środowiska gry.
- **Decyzja:**
  - **Architektura Rejestrów Providerów Danych (`pl.zombek.client.core.data`):** Zdefiniowano abstrakcyjne interfejsy `IArmorDataProvider` i `ICooldownDataProvider` oraz rejestry singletonowe (`ArmorDataProviderRegistry`, `CooldownDataProviderRegistry`). W środowisku testowym rejestry dostarczają bezpieczne makiety (domyślny fallback), a w grze adaptery loaderów rejestrują właściwych providerów pobierających dane bezpośrednio z obiektów `PlayerInventory` i `ItemCooldownManager` / `ItemCooldowns`.
  - **Rozszerzenie `IHudRenderer` o `renderItem`:** Dodano metodę `renderItem(Object itemHandle, int x, int y, float scale)` z domyślną pustą implementacją (no-op dla testów headless), mapowaną w adapterach loaderów na `DrawContext.drawItem` (Fabric/Quilt) lub `GuiGraphics.renderItem` (Forge/NeoForge).
  - **Niezależny Silnik `ComboTracker`:** Śledzenie serii uderzeń oparto na nanosekundowym rejestrze hitów z tłumieniem duplikatów poniżej 50 ms, resetem przy zmianie celu/śmierci/wygaśnięciu (timeout) oraz 4-stopniowymi progami kolorów. Zdarzenia trafień przekazywane są z legalnych listenerów ataku (`AttackEntityCallback` na Fabric/Quilt, `AttackEntityEvent` na Forge/NeoForge).
  - **Zero Fałszywych Danych i Autoclickera:** Przedmioty bez trwałości nie wyświetlają fikcyjnych punktów, pudła nie zaliczają combo, a cooldowny odczytywane są wyłącznie z oficjalnego mechanizmu gry.
- **Konsekwencje:**
  - Czysty, modularny kod w 100% zgodny z wzorcami projektowymi i zasadą pojedynczej odpowiedzialności.
  - Bezproblemowe działanie na 4 modloaderach bez powielania logiki HUD i matematyki.
  - Błyskawiczne i niezawodne testy jednostkowe w module `:core`.











