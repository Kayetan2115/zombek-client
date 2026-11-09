# Polityka Bezpieczeństwa — ZombekClient Security Architecture

## 1. Wprowadzenie

Projekt **ZombekClient** realizuje wielowarstwową strategię bezpieczeństwa (Defense in Depth), chroniąc dane uwierzytelniające graczy, integralność systemu operacyjnego oraz konta przed banami anticheat na serwerach multiplayer.

Dokument opisuje 4 główne filary bezpieczeństwa zaimplementowane w projekcie:
1. Bezpieczeństwo kont i tokenów autoryzacyjnych (OAuth2 / Mojang).
2. Bezpieczeństwo systemu plików, archiwów i pobierania modów (Modrinth / MRPACK).
3. Bezpieczeństwo wyrażeń regularnych i ochrona przed atakami ReDoS (HypixelChatFilter).
4. Bezpieczeństwo Fair Play i ochrona przed banami anticheat (HypixelSafetyController).

---

## 2. Bezpieczeństwo Kont i Pamięci Podręcznej Tokenów

### 2.1. Szyfrowanie tokenów (Electron safeStorage / DPAPI)
- Tokeny dostępowe Minecraft JWT, refresh tokeny oraz tokeny Xbox Live **nigdy** nie są zapisywane jawnym tekstem (plaintext) na dysku.
- W środowisku Windows wykorzystywane jest natywne API Windows Data Protection API (DPAPI) za pośrednictwem `electron.safeStorage`.

### 2.2. Redagowanie danych poufnych w logach (Log Sanitization)
- Launcher oraz silnik uruchamiania JVM (`GameLauncher`) stosują automatyczną filtrację argumentów uruchomieniowych.
- Parametr `--accessToken <token>` jest natychmiast maskowany ciągiem `--accessToken [REDACTED]` przed wysłaniem do strumieni stdout/stderr oraz pliku `latest.log`.

---

## 3. Bezpieczeństwo Plików, Archiwów i Modów

### 3.1. Ochrona przed Zip Slip i atakami na ścieżki (`MrpackPathValidator`)
- Zaimplementowano rygorystyczny walidator ścieżek archiwów JAR i MRPACK.
- Bezwzględne odrzucanie sekwencji wyjścia z katalogu (`..`), ścieżek bezwzględnych (`/`, `C:\`), ścieżek sieciowych UNC oraz znaków specjalnych Windows (`CON`, `PRN`, `AUX`, `NUL`).

### 3.2. Weryfikacja sum kontrolnych (SHA-512 & SHA-256)
- Każdy pakiet modyfikacji pobierany z Modrinth CDN jest weryfikowany sumą SHA-512 przed umieszczeniem w katalogu instancji.
- W przypadku niezgodności hasha plik jest natychmiast usuwany, a transakcja wycofywana (Auto-Rollback).

### 3.3. Restrykcyjna biała lista domen CDN
- Klient pobiera zasoby wyłącznie z zaufanych domen (`cdn.modrinth.com`, `edge.modrinth.com`, `piston-meta.mojang.com`, `meta.fabricmc.net`, `maven.neoforged.net`, `meta.quiltmc.org`).
- Blokada odpytywania prywatnych adresów IP (SSRF protection: 127.0.0.1, 10.0.0.0/8, 192.168.0.0/16 itp.).

---

## 4. Ochrona przed atakami ReDoS (Regular Expression Denial of Service)

W module `HypixelChatFilter` gracze mogą definiować własne reguły filtracji czatu z użyciem wyrażeń regularnych. Złe wyrażenie regularne może doprowadzić do zawieszenia wątku renderowania Minecrafta.

### Zastosowane zabezpieczenia:
1. **Ograniczenie długości wzorca:**
   - Wzorce dłuższe niż 100 znaków są odrzucane (`MAX_REGEX_LENGTH = 100`).
2. **Wykrywanie katastrofalnego cofania (Catastrophic Backtracking):**
   - Heurystyczny filtr sprawdza obecność zagnieżdżonych kwantyfikatorów, np. `(a+)+`, `(.*)*`, `([a-z]+)+`. Wzorce tego typu są flagowane jako `invalidRegex` i nie podlegają kompilacji.
3. **Izolacja wyjątków:**
   - Kompilacja `Pattern.compile` znajduje się w bloku `try-catch`, co zapobiega awarii gry przy błędnej składni użytkownika.

---

## 5. Zgodność z Hypixel Fair Play (Zero-Risk Anti-Ban Policy)

ZombekClient nie posiada oficjalnego certyfikatu serwera Hypixel. Dlatego moduł **Hypixel Mods** (`pl.zombek.client.core.hypixel`) został zaprojektowany z rygorystycznymi ograniczeniami:

### 5.1. Oficjalne zastrzeżenie prawne (Disclaimer)
> *"Unofficial client modification. Use at your own risk."*

### 5.2. Profil `STRICT_SAFETY` (Domyślny dla Hypixel Network)
- Dostępne są wyłącznie operacje kosmetyczne i lokalne elementy UI:
  - `COSMETIC` — dozwolone
  - `LOCAL_UI` — dozwolone
  - `LOCAL_NOTIFICATION` — dozwolone
  - `LOCAL_AUTOCOMPLETE` — dozwolone
- Bezwzględna blokada:
  - `GAMEPLAY_AUTOMATION` — **ZABLOKOWANE**
  - `NETWORK_MODIFICATION` — **ZABLOKOWANE**
  - `UNKNOWN` — **ZABLOKOWANE**

### 5.3. Całkowity zakaz automatyzacji czatu (Zero Auto-Chat)
- Tradycyjne modyfikacje typu AutoGG, AutoTip, AutoFriend czy AutoWho są surowo zabronione.
- Zamiast AutoGG wprowadzono **Manual Social Suggestions**:
  - Dyskretna karta HUD informująca o zakończeniu meczu.
  - Przycisk kopiowania wiadomości `gg` do schowka systemowego (`copyToClipboard()`).
  - Zero wywołań `sendChatMessage()`, zero symulacji klawiatury, zero ingerencji w pakiety sieciowe.
- Całkowity brak zapytań do zewnętrznych API Hypixel podczas trwania gry.

---

## 6. Ochrona Prywatności Gracza i Streamer Mode (Etap 8B4)

W Etapie 8B4 wdrożono zaawansowany system ochrony tożsamości sieciowej i danych środowiska gracza (`ServerAddressPrivacyController`, `PrivacyMode`, `ServerAliasManager`):

### 6.1. Domyślna Ochrona Prywatności (Default-Off Full Address)
- Wyświetlanie pełnego adresu serwera w module `ServerAddressModule` jest **domyślnie wyłączone** (`show_server_address: false`).
- Domyślny tryb to `MASK_HOST`, w którym środek adresu hosta jest zacierany (np. `play.example.net` $\to$ `pla****.net`). Zapobiega to przypadkowemu ujawnieniu IP/domeny prywatnych serwerów znajomych na zrzutach ekranu lub transmisjach na żywo.

### 6.2. Tryb Streama (Streamer Mode)
- Jednym kliknięciem przełącza moduł w stan maskowania wszystkich newralgicznych pól:
  - Adres serwera $\to$ `[HIDDEN]`
  - Port $\to$ ukryty
  - Nazwa lokalnego świata $\to$ `[HIDDEN]`

### 6.3. Ochrona Świata Jednoosobowego (Zero Fake IP)
- W trybie Singleplayer moduł jednoznacznie komunikuje stan "Singleplayer".
- Rygorystyczny zakaz fabrykowania adresów: ZombekClient nigdy nie wyświetla sztucznych adresów IP typu `127.0.0.1` czy `localhost` w świecie lokalnym.

### 6.4. Całkowicie Lokalne Dane (Local-Only Storage)
- Niestandardowe aliasy serwerów, etykiety oraz przypisane kolory ARGB definiowane w `ServerAliasManager` są przechowywane wyłącznie na dysku lokalnym użytkownika (`config/zombekclient/aliases.json`).
- Nigdy nie są przesyłane do serwerów gier, telemetrycznych ani innych podmiotów trzecich.

---

## 7. Zasady Fair Play i Bezpieczeństwo Modułów NameTags oraz Tab Editor (Etap 8B5)

W Etapie 8B5 wprowadzono moduły `NameTagsModule` i `TabEditorModule`, ściśle przestrzegając wytycznych Fair Play obowiązujących na publicznych serwerach Minecraft (Hypixel, standardowe serwery PvP i survival):

### 7.1. Bezwzględny Zakaz Funkcji Niedozwolonych (No ESP / No Wallhack / No See-Through-Walls)
- **Test głębokości (Depth Test):** Etykiety 3D generowane przez `CustomNameTagRenderer` renderowane są z zachowaniem bufora głębokości (Depth Buffer). Etykiety encji znajdujących się za ścianami, pod ziemią lub za przeszkodami terenowymi są fizycznie zasłonięte i niewidoczne dla gracza.
- **Skradanie się (`isSneaking` / `isCrouching`):** `NameTagVisibilityRuleEvaluator` rygorystycznie respektuje stan kucania. Etykieta kucającego gracza jest ukrywana lub renderowana jako mocno przyciemniona/półprzezroczysta na bardzo bliski dystans (ściśle wg logiki silnika Minecraft vanilla). Gracz skradający się nigdy nie jest demaskowany zza przeszkód.
- **Niewidzialność (`isInvisible`):** Przeciwnicy z aktywnym efektem niewidzialności nie mają renderowanego nametaga (chyba że mechanika gry/drużyny w vanilli na to zezwala, np. świecenie `Glowing` lub ten sam zespół).
- **Tryb widza (`isSpectator`):** Gracze w trybie widza podlegają vanillowym regułom widoczności.
- **Limit dystansu (`maxDistance`):** Moduł nie rozszerza zasięgu śledzenia encji (Entity Tracking Range) ponad to, co serwer przesyła w pakietach. Dodatkowo posiada konfigurowalny limit dystansu (domyślnie 64 bloki) z płynnym zanikaniem.

### 7.2. Brak Fałszowania Danych i Rangi Serwerowych
- ZombekClient nie fabrykuje rang, prefiksów ani uprawnień serwerowych.
- Wszelkie metadane graczy (`PlayerInfoData`) pochodzą wyłącznie z autentycznych pakietów serwerowych dostarczanych przez loader (`IPlayerInfoDataProvider`).
- `DataAvailability` jasno rozróżnia stany danych: jeśli serwer nie przesyła zdrowia graczy (brak scoreboardu health), moduł wyświetla `UNAVAILABLE` zamiast zgadywać lub pobierać je nielegalnymi metodami.

### 7.3. Całkowicie Lokalne Zarządzanie Znajomymi (Local-Only Friends Storage)
- Menedżer znajomych `NameTagFriendManager` przechowuje listę przyjaciół, lokalne aliasy i notatki wyłącznie w pliku `config/zombekclient/friends.json` na dysku lokalnym użytkownika.
- Moduł nie wykonuje żadnych połączeń sieciowych, zapytań telemetrii ani synchronizacji w tle. Aliasy i oznaczenia znajomych widzi wyłącznie gracz lokalny.



---

## 8. Bezpieczeństwo i Poufność Modułów Chat oraz Titles (Etap 8B6)

W Etapie 8B6 wprowadzono moduły `ChatModule` oraz `TitlesModule`. Architektura obu modułów została zaprojektowana z zachowaniem najwyższych standardów bezpieczeństwa sieciowego i ochrony prywatności użytkownika:

### 8.1. Sanifikacja Poświadczeń w Historii Czatu (Credential Sanitization)
- **Problem:** Gracze na serwerach Minecraft często logują się lub rejestrują komendami typu `/login <hasło>` lub `/register <hasło>`.
- **Zabezpieczenie:** `ChatHistoryManager` posiada wbudowany, nieodwracalny filtr `CREDENTIAL_COMMAND_PATTERN`. Żadna wiadomość rozpoczynająca się od `/login`, `/register`, `/l`, `/reg`, `/changepassword` czy `/auth` nie jest zapisywana w buforze historii ani eksportowana do plików logów na dysku.

### 8.2. Odporność na Ataki ReDoS (Catastrophic Backtracking Immunity)
- **Problem:** Niewłaściwie sformułowane wyrażenia regularne użytkownika (np. `(a+)+$`, `(.*)*`) mogą doprowadzić do zawieszenia wątku głównego gry (ReDoS).
- **Zabezpieczenie:** `UniversalChatFilterRule` dokonuje statycznej inspekcji ryzyka zagnieżdżonych kwantyfikatorów przed kompilacją oraz ogranicza czas ewaluacji do bezpiecznego limitu 50 ms. W razie przekroczenia limitu reguła jest dezaktywowana bez wpływu na płynność gry.

### 8.3. Integralność Podpisów Kryptograficznych (`SignedMessage`)
- **Architektura:** W Minecraft 1.19+ i 1.20+ wiadomości czatu graczy są chronione podpisami kryptograficznymi serwera i profilu Mojang.
- **Zasada:** ZombekClient **nie modyfikuje** pakietów sieciowych ani struktury `SignedMessage`. Filtrowanie, grupowanie i formatowanie odbywa się wyłącznie w pamięci lokalnej klienta (`CustomChatRenderer`) bez naruszania podpisów.

### 8.4. Zero Automatyzacji i Zakaz Makr
- Moduł czatu nie udostępnia funkcji auto-wysyłania wiadomości (np. AutoGG, automatyczne odpowiedzi, auto-reconnect), które mogłyby zostać uznane za nielegalne makro na serwerach publicznych.
- Moduł `Titles` prezentuje wyłącznie autentyczne komunikaty nadsyłane przez serwer lub lokalne alerty użytkownika bez ingerencji w mechanikę gry.

---

## 9. Bezpieczeństwo Sieciowe i Prywatność w Net Graph (Etap 8B7)

W Etapie 8B7 zaimplementowano moduł diagnostyczny `NetGraphModule`. Wdrożono rygorystyczne zabezpieczenia sieciowe i prywatności:

### 9.1. Pasywny Pomiar bez Ingerencji w Sieć (Zero-Injection Policy)
- Moduł **nie wysyła** żadnych dodatkowych pakietów sondujących, nie generuje sztucznego ruchu i nie wykonuje testów obciążeniowych (stress testing).
- Pomiar opóźnienia i transferu opiera się wyłącznie na danych naturalnie wymienianych przez klienta z serwerem.

### 9.2. Bezpieczeństwo Danych i Brak Inspekcji Treści (Non-Invasive Counters)
- `NetworkTrafficTracker` oraz `PacketRateTracker` zliczają wyłącznie surowe liczby bajtów (`readableBytes()`) i zdarzenia transmisji.
- Klient **nigdy nie czyta, nie klonuje ani nie buforuje** zawartości pakietów. Płatności, hasła, korespondencja czatu i dane sesyjne pozostają całkowicie nienaruszone.

### 9.3. Bezpieczeństwo Wątkowe (Thread Safety)
- Wątki obsługi Netty aktualizują bezblokadowe liczniki atomowe `AtomicLong` i `AtomicInteger`.
- Rysowanie wykresów i kalkulacja HUD odbywają się wyłącznie na głównym wątku gry w bezpiecznych fazach renderowania, bez blokowania pętli zdarzeń sieciowych.

### 9.4. Prywatność przy Eksporcie Statystyk
- Funkcje `exportToJson()` i `exportToText()` w `NetworkStatistics` podlegają rygorystycznej filtracji: wykluczono adresy IP, nazwy domenowe hostów, loginy graczy i identyfikatory sesyjne.
- Dane statystyczne nigdy nie są automatycznie wysyłane na zewnętrzne serwery telemetryczne.

---

## 10. Bezpieczeństwo Optyczne, Pasywność Kamery i Zasady Fair Play (Etap 8C1)

W Etapie 8C1 wdrożono moduły modyfikacji widoku: `ZoomModule`, `FovChangerModule`, `MotionBlurModule` oraz `HurtCamModule`. Każdy z nich został zaprojektowany w oparciu o bezwzględne reguły bezpieczeństwa i zasady Fair Play:

### 10.1. Brak Przewagi Zasięgu Ataku (Zero Reach Advantage)
- Zmniejszenie kąta widzenia (FOV) podczas przybliżenia (`ZoomController`) jest operacją realizowaną **wyłącznie w macierzy projekcji kamery klienta**.
- Moduł w żaden sposób nie wydłuża promienia śledzenia promieni (Raycast Reach), zasięgu uderzenia mieczem (3.0 bloki w Survival), interakcji z blokami (4.5 bloka) ani precyzji obliczeń kolizji po stronie serwera.

### 10.2. Nienaruszalność Pakietów Ruchu Gracza (Movement Packet Integrity)
- Kąty patrzenia głowy i ciała gracza wysyłane w pakietach sieciowych do serwera (`ServerboundMovePlayerPacket.Rot` / `PosRot`) są całkowicie odseparowane od efektów optycznych klienta.
- Serwer Minecraft oraz systemy antycheat (np. GrimAC, Vulcan, Polar, Watchdog) widzą w 100% standardowe pakiety pozycji i rotacji zgodne z ruchem fizycznym postaci.

### 10.3. Niezależność Mechaniki Obrażeń i Odrzutu (Damage & Knockback Invariance)
- Wyłączenie lub zmniejszenie przechyłu kamery w `HurtCamModule` (`tiltViewWhenHurt`) wpływa wyłącznie na lokalny render klatki świata.
- Moduł **nie modyfikuje** punktów zdrowia, czasu nietykalności postaci (Invulnerability Ticks), absorpcji tarczy, wektora odrzutu (Knockback) ani danych o obrażeniach. Wszystkie uderzenia są w pełni i rzetelnie rejestrowane przez serwery oraz moduły analizy walki `PvPInfoModule` i `ComboTracker`.

### 10.4. Ochrona Zasobów Karty Graficznej i Brak Wycieków Pamięci (VRAM & Crash Safety)
- Moduł `MotionBlurModule` korzysta ze ścisłego cyklu życia buforów FBO. Wszelkie zmiany trybu pełnoekranowego, rozdzielczości lub skali GUI skutkują natychmiastowym bezpiecznym zwolnieniem poprzednich buforów OpenGL bez wycieków pamięci VRAM.
- W przypadku wykrycia nieobsługiwanych sterowników GPU lub braku wsparcia dla wymaganych shaderów, kontroler automatycznie przełącza się w bezpieczny tryb `Fallback Mode`, nie dopuszczając do awarii gry (Crash-Proof).
- Przejście do innego wymiaru, śmierć lub rozłączenie z serwerem natychmiastowo czyszczą historię klatek bufora, zapobiegając powidokom (Ghosting).

---

## 11. Bezpieczeństwo Środowiska Świata, Izolacja Renderowania i Zasady Fair Play (Etap 8C2)

W Etapie 8C2 wdrożono moduły środowiskowe i oświetleniowe: `LightingModule`, `FullBrightModule`, `TimeChangerModule` oraz `WeatherChangerModule`. Ich implementacja spełnia najostrzejsze kryteria bezpieczeństwa i pasywności sieciowej:

### 11.1. 100% Client-Side Only (Zero Server World Impact)
- **Czas Świata:** `TimeChangerModule` modyfikuje wyłącznie kąt renderowania ciał niebieskich (`getSkyAngle`) i wizualny czas (`getTimeOfDay`) dla procesów renderowania klienta. Rzeczywisty czas świata serwerowego, mechanika snu w łóżkach, wzrost upraw, działanie czerwonego kamienia (redstone) i pakiety `ClientboundSetTimePacket` pozostają w 100% nienaruszone.
- **Pogoda:** `WeatherChangerModule` modyfikuje lokalne gradienty renderowania deszczu, śniegu i burzy w silniku graficznym. Nie wysyła żadnych komend administratorskich (`/weather clear`) ani pakietów sieciowych. Prawdziwa pogoda serwera (np. uderzenia piorunów w byty, przewodzenie trójzębu) pozostaje nienaruszona.
- **Oświetlenie:** `LightingModule` nie modyfikuje poziomu światła bloków na serwerze (`BlockState.lightLevel`). Algorytmy spawnowania potworów (zależne od natężenia światła równego 0 w wersji 1.20.4) działają w oparciu o stan serwerowy, uniemożliwiając wykorzystanie dynamicznego oświetlenia jako niedozwolonej przewagi w mechanikach mobów.

### 11.2. Bezpieczeństwo Konfiguracji (Brak Psujących Hacks Gamma)
- `FullBrightModule` nie nadpisuje waniliowego pliku `options.txt` nieprawidłowymi wartościami (np. `gamma: 1000.0`). Wartość gammy jest modulowana dynamicznie w locie w klasie `LightmapTextureManager`, a wyłączenie modułu lub zamknięcie gry natychmiast przywraca naturalny poziom jasności bez uszkadzania konfiguracji użytkownika.
- Wdrożona kompensacja nasycenia barw i zachowanie cieni w całkowitym mroku (shadow preservation) zapobiegają efektowi "wyblakłego ekranu" i zachowują trójwymiarowość terenu.

### 11.3. Ochrona Wydajności i GPU Throttling
- Zbiórka źródeł światła przez `DynamicLightManager` posiada twarde ograniczenie częstotliwości (`updateIntervalMs = 30 ms`) oraz limit aktywnych źródeł (16), odrzucając obiekty najbardziej odległe od gracza ($d^2$).
- `WeatherChangerModule` nakłada twardy limit na liczbę cząsteczek deszczu i śniegu (`maxWeatherParticles`), chroniąc zintegrowane układy GPU przed drastycznym spadkiem FPS.

---

## 12. Bezpieczeństwo Efektów Renderowania i Ochrona Fair Play (Etap 8C3)

W Etapie 8C3 wdrożono moduły efektów renderowania: `FogCustomizerModule`, `ColorSaturationModule`, `GlintColorizerModule` oraz `ParticleChangerModule`. Ich projektowanie podporządkowano rygorystycznym zasadom Fair Play i bezpieczeństwa:

### 12.1. Ochrona przed Wallhackiem i Oszustwami Zasięgu (Anti-X-Ray Fog)
- `FogVisualController` i `FogDistanceController` zawsze ograniczają końcowy dystans mgły (`endDistance`) do wartości mniejszej bądź równej fizycznemu zasięgowi renderowania chunków (`viewDistance`).
- Klient nigdy nie próbuje rozszerzać widoczności w głąb niezaładowanych chunków ani redukować mgły poniżej bezpiecznego bufora geometrycznego.
- W środowiskach nieprzejrzystych (woda, lawa, sypki śnieg) system respektuje minimalne odcięcia, uniemożliwiając uzyskanie niedozwolonej przezroczystości cieczy.

### 12.2. Nienaruszalność NBT i Danych Przedmiotów (Zero ItemStack Modification)
- `GlintColorizerModule` operuje wyłącznie na poziomie renderingu wierzchołków modeli i parametrów shaderów.
- Przedmioty nie otrzymują modyfikacji tagu NBT `Enchantments`, a serwer gry nie otrzymuje żadnych zmienionych pakietów inwentarza (`ServerboundSetCarriedItemPacket`, `ServerboundContainerClickPacket`).
- Całkowicie wyklucza to desynchronizację ekwipunku, cofanie przedmiotów (rollback) czy podejrzenia o oszustwa związane z ekwipunkiem.

### 12.3. Ochrona Czytelności HUD i Dostępności
- `ColorSaturationModule` implementuje bezwarunkową regułę ochrony interfejsu `Preserve HUD Colors`. Niezależnie od poziomu nasycenia (od 0% monochromu do 200% nasycenia) czy przesunięcia barw świata, wszystkie elementy HUD (życie, głód, pancerz, czat, celownik) renderowane są z zachowaniem oryginalnych kontrastów i barw.
- Presety dostępności cząsteczek (`REDUCED_PARTICLES`, `MINIMAL_PARTICLES`) pozwalają graczom ze słabszym wzrokiem lub cierpiącym na epilepsję/migreny na ograniczenie migotania ekranu bez utraty kluczowych wskaźników walki (np. ciosów krytycznych).

### 12.4. Twarde Limity Wydajnościowe i Ochrona przed Zamrożeniem Gry
- `ParticleRenderController` narzuca twardy limit aktywnych cząstek (domyślnie 2000, konfigurowalny 50 - 5000) oraz odcięcie dystansowe `maxDistanceCutoff`. Zabezpiecza to przed atakami typu "particle lag machine" na serwerach wieloosobowych.
- `GlintAnimationController` generuje animacje tęczy i gradientów na bazie czystej arytmetyki czasu bez alokowania nowych obiektów `RenderType` lub warstw buforów, gwarantując zero narzutu na Garbage Collector (GC).



