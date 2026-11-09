# Wspólna Architektura Renderowania HUD (Multi-Loader)

## 1. Założenie Architektoniczne

Zgodnie z zasadą DRY i wzorcem Adaptera, logika obliczeń geometrycznych, układu kafli, marginesów, kolorów z przezroczystością alfa oraz formatowania tekstu modułów HUD (`KeystrokesModule`, `CPSModule`) znajduje się w 100% we wspólnym module `core` (`pl.zombek.client.core.hud`).

Żaden loader (Fabric, Forge, NeoForge, Quilt) nie duplikuje kodu rysowania. Każdy loader dostarcza jedynie minimalistyczny mostek implementujący interfejs `IHudRenderer` (~50 linii kodu).

---

## 2. Kontrakt `IHudRenderer`

```java
public interface IHudRenderer {
    void fill(int x, int y, int width, int height, int color);
    void drawBorder(int x, int y, int width, int height, int color, int thickness);
    void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold);
    int getTextWidth(String text);
    int getFontHeight();
    int getScreenWidth();
    int getScreenHeight();

    // Dodane w Etapie 8A2: Natywne renderowanie przedmiotów (Armor Status, Cooldowns)
    default void renderItem(Object itemHandle, int x, int y) {
        renderItem(itemHandle, x, y, 1.0f);
    }
    default void renderItem(Object itemHandle, int x, int y, float scale) {
        // Domyślny no-op w środowisku testowym / headless
    }
}
```

---

## 3. Implementacje Adapterów Platformowych

| Loader | Klasa Adaptera | Narzędzie Graficzne | Mechanizm Wywołania | Renderowanie Przedmiotów |
| :--- | :--- | :--- | :--- | :--- |
| **Fabric** | `FabricHudRenderer` | `net.minecraft.client.gui.DrawContext` | `HudRenderCallback.EVENT` | `context.drawItem((ItemStack) itemHandle, x, y)` |
| **Quilt** | `QuiltHudRenderer` | `net.minecraft.client.gui.DrawContext` | Mixin `InGameHudMixin` (`InGameHud.render` TAIL) | `context.drawItem((ItemStack) itemHandle, x, y)` |
| **Forge** | `ForgeHudRenderer` | `net.minecraft.client.gui.GuiGraphics` | `RegisterGuiOverlaysEvent.registerAboveAll()` | `guiGraphics.renderItem((ItemStack) itemHandle, x, y)` |
| **NeoForge** | `NeoForgeHudRenderer` | `net.minecraft.client.gui.GuiGraphics` | `RenderGuiEvent.Post` (`NeoForge.EVENT_BUS`) | `guiGraphics.renderItem((ItemStack) itemHandle, x, y)` |

---

## 4. Rejestr Elementów HUD (`HudElementRegistry`)

Klasa `HudElementRegistry` zarządza cyklem życia wszystkich elementów interfejsu graficznego:
- **Thread-Safety:** Rejestracja oparta na `CopyOnWriteArrayList`.
- **Error Containment:** Każdy element renderowany jest w bloku `try-catch`. Awaria renderowania jednego modułu nie zawiesza gry ani pozostałych elementów interfejsu.
- **Dynamiczna widoczność:** Przed wywołaniem metody `render(IHudRenderer)` sprawdzany jest warunek `element.isVisible()` (np. moduł włączony w `ModuleManager`).

---

## 5. Renderowanie Przedmiotów i Pasków Trwałości (Etap 8A2)

Dla modułów `ArmorStatusModule` oraz `CooldownsModule`:
1. **Natywne Ikony Przedmiotów**: Metoda `renderItem` przekazuje `ItemStack` (opakowany jako `Object` w module `:core`) bezpośrednio do natywnego mechanizmu rysowania danego loadera. W przypadku podania skali różnej od `1.0f`, adaptery stosują przeskalowanie stosu macierzy (`MatrixStack` / `PoseStack`).
2. **Kalkulacja Trwałości i Pasków**: Paski trwałości (Armor Status) i paski czasu odnowienia (Cooldowns) są rysowane geometrycznie za pomocą `fill()`, zapewniając perfekcyjne dopasowanie i progi kolorystyczne ARGB bez polegania na sztywnych teksturach gry.

---

## 6. Proceduralne Renderowanie Celownika (Crosshair Overlay — Etap 8A3)

Moduł `CrosshairModule` implementuje interfejs `IHudElement` i rejestruje się w `HudElementRegistry`:
1. **Środek Ekranu**: Pozycja bazowa wyznaczana jest jako środek ekranu:
   $$x_c = \lfloor \text{screenWidth} / 2 \rfloor$$
   $$y_c = \lfloor \text{screenHeight} / 2 \rfloor$$
2. **Geometria Proceduralna**: Rysowanie ramion (górne, dolne, lewe, prawe), punktu centralnego, okręgów i kwadratów odbywa się z wykorzystaniem metod geometrycznych `IHudRenderer` (`fill` i `drawBorder`).
3. **Dynamiczne Zabarwienie i Wskaźnik Cooldownu**: `CrosshairRenderer` pobiera aktualny kolor z `CrosshairColorResolver` oraz stan cooldownu ataku z `CrosshairTargetRegistry`, dynamicznie dopasowując geometrię bez naruszania pozostałych warstw interfejsu.

---

## 7. Renderowanie Modułów Zasięgu i Informacji PvP (Etap 8A4)

W Etapie 8A4 zintegrowano dwa nowe moduły z rejestrem `HudElementRegistry`:
1. **`ReachDisplayModule`**:
   - Dynamiczne formatowanie tekstu z uwzględnieniem liczby miejsc po przecinku (`decimal_places`), prefiksu (np. `Reach: `) i przyrostka jednostki (np. ` b`).
   - W stylu `COMPACT` renderuje pojedynczy kafelek z ostatnim lub bieżącym zasięgiem.
   - W stylu `DETAILED` renderuje dodatkowe linie ze średnim zasięgiem (`Avg: `), maksymalnym (`Max: `) oraz bieżącym dystansem do celu (`Target: `).
   - Dynamiczne dopasowanie szerokości panelu (`getWidth()`) i wysokości (`getHeight()`) z zachowaniem paddingu i tła.

2. **`PvPInfoModule`**:
   - Obsługuje 5 wariantów stylu (`COMPACT`, `DETAILED`, `HORIZONTAL`, `VERTICAL`, `MINIMAL`).
   - Elastyczny podział na sekcje: `MELEE` (ataki, trafienia, celność %, combo), `PROJECTILE` (wystrzelone, celne, hit rate %, typ pocisku) oraz `HEALTH` (zdrowie gracza, absorpcja, otrzymane obrażenia, zdrowie przeciwnika).
   - Spójna hierarchia kolorystyczna: dedykowany kolor nagłówków (`header_color`), etykiet parametrów (`label_color`) oraz wartości (`value_color`).

---

## 8. Renderowanie Wskaźników Ruchu i UHC Overlay (Etap 8A5)

W Etapie 8A5 wprowadzono trzy nowe moduły HUD zarejestrowane w `HudElementRegistry`:
1. **`ToggleSprintModule`**:
   - Dynamiczny wskaźnik stanu sprintu na ekranie (`[Sprinting (Toggled)]`, `[Sprinting (Key)]`, `[Sprinting (Vanilla)]`).
   - Cztery konfigurowalne style prezentacji: `BRACKETED` (np. `[Sprinting (Toggled)]`), `COMPACT` (np. `Sprint: ON`), `DETAILED` (pełna nazwa trybu i status blokady), `TEXT_ONLY` (prosty tekst bez nawiasów).
   - Konfigurowalne kolory tekstu dla stanu aktywnego (`active_color`), nieaktywnego (`inactive_color`), tła oraz obrysu.

2. **`ToggleSneakModule`**:
   - Wskaźnik stanu skradania (`[Sneaking (Toggled)]`, `[Sneaking (Key)]`, `[Sneaking (Vanilla)]`).
   - Identyczny zestaw stylów wskaźnika (`BRACKETED`, `COMPACT`, `DETAILED`, `TEXT_ONLY`) dla pełnej spójności wizualnej.
   - Niezależne pozycjonowanie (`x`, `y`), tło, zaokrąglenie oraz obramowanie.

3. **`UHCSessionHudModule` (dawniej UHC Overlay)**:
   - W Etapie 8A6 panel informacyjny sesji UHC został wydzielony jako `UHCSessionHudModule` (`uhc_session_hud`), podczas gdy nazwa `uhc_overlay` została przypisana do skalowania upuszczonych przedmiotów.
   - Obsługa 5 układów graficznych (`COMPACT`, `DETAILED`, `HORIZONTAL`, `VERTICAL`, `CUSTOM`).
   - Cztery zintegrowane sekcje:
     - **Status Gracza**: Zdrowie (format serduszek lub liczbowy), poziom głodu, absorpcja oraz współrzędne gracza.
     - **Item Tracker**: Złote jabłka, złote głowy (Golden Heads), perły kresu, łuki i strzały.
     - **UHC Timers**: Zegar czasu gry, odliczanie do PvP, zmniejszania się granicy świata (Border Shrink) oraz fazy Meetup.
     - **Informacje o Meczu**: Liczba żywych graczy, pozostałe drużyny, promień granicy (World Border) oraz zabójstwa gracza/drużyny.
   - Paski postępu timerów oraz wskaźniki kolorystyczne wyliczane czysto geometrycznie za pomocą `renderer.fill()` i `renderer.drawBorder()`.

---

## 9. Renderowanie Nakładek 3D w Świecie Gry (In-World Billboard Overlays — Etap 8A6)

W Etapie 8A6 wprowadzono zunifikowany potok renderowania nakładek trójwymiarowych w przestrzeni świata gry dla modułów **TNT Countdown** oraz **Team View**.

### 9.1. Architektura Renderera Billboardów
Nakładki billboardowe muszą być zawsze skierowane idealnie prostopadle do osi wzroku gracza (Face-Camera Billboarding):
- **Translacja Pozycji**: Przejście z koordynatów świata do przestrzeni relatywnej kamery:
  $$x_{\text{rel}} = x_{\text{entity}} - x_{\text{cam}}, \quad y_{\text{rel}} = y_{\text{entity}} + \text{heightOffset} - y_{\text{cam}}, \quad z_{\text{rel}} = z_{\text{entity}} - z_{\text{cam}}$$
- **Rotacja do Kamery**:
  - Fabric / Quilt: `matrices.multiply(camera.getRotation())`
  - Forge / NeoForge: `poseStack.mulPose(camera.rotation())`
- **Skalowanie i Odwrócenie Y**:
  - Standardowe skalowanie tekstu w Minecraft: `matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale)`.
  - Wartości ujemne osi Y i X kompensują domyślny układ współrzędnych czcionki silnika.

### 9.2. Realizacja TNT Countdown
- `TntCountdownCalculator.getTntArgb(remainingSeconds)` wylicza dynamiczny kolor tekstu (zielony $\to$ żółty $\to$ czerwony).
- Wyświetlanie sformatowanego łańcucha (np. `"3.4s"` lub `"TNT: 1.85s"`).
- Renderowanie półprzezroczystego tła pod tekstem z zachowaniem bufora głębokości (culling odległości do 64 bloków).

### 9.3. Realizacja Team View
- Renderowanie znaczników geometrycznych (`▼`, `◆`, `●`, `🛡`, `︾`) w kolorze drużyny gracza.
- Wyświetlanie etykiety z nickiem sojusznika i odległością w metrach (np. `Gracz (32m)`).
- Opcjonalny wskaźnik zdrowia sojusznika.
- Rygorystyczny test widoczności i brak ujawniania wrogich graczy.

---

## 10. Klasyczny Wygląd Paska Zdrowia (1.7 Health Styling — Etap 8A7)

W ramach modułu **1.7 Visuals** (`OneSevenVisualsModule`) udostępniono opcjonalne klasyczne renderowanie elementów paska zdrowia gracza w GUI:
- **Klasyczny Obrys Serc:** Przywrócenie proporcji i odstępów ikon serduszek znanych z wersji Minecraft 1.7.
- **Biały Błysk przy Obrażeniach (White Damage Flash):** W wersji 1.7 utrata zdrowia wywoływała charakterystyczny biały błysk konturu serc zamiast nowoczesnego czerwonego zanikania.
- **Złote Serca Absorpcji:** Klasyczne ułożenie i tekstury dodatkowych serc (złote jabłko).
- **Animacja Regeneracji:** Klasyczny, szybszy rytm podskakiwania serc przy aktywnej regeneracji zdrowia.
Obliczenia stanu zdrowia i migania opierają się o istniejący potok `IHudRenderer` i parametry zarejestrowane w grupie `health` modułu 1.7 Visuals.

---

## 11. Renderowanie Panelu Bedwars Helper (Etap 8A8)

W ramach modułu **Bedwars Helper** (`BedwarsHelperModule`) wprowadzono rozbudowany, wielosekcyjny panel renderowany w oparciu o `IHudRenderer`:

### 11.1. Sekcje Panelu Bedwars HUD
1. **Nagłówek Meczu:** Nazwa mapy, tryb gry (Solo/Doubles/4v4/4v4v4v4) oraz status wykrywania (z kolorem zależnym od `DetectionConfidence`).
2. **Tablica Drużyn (Team Board):** Renderowanie 8 drużyn z ich barwami ARGB w 6 układach (`COMPACT`, `DETAILED`, `HORIZONTAL`, `VERTICAL`, `ICON`, `TEXT`), z wyróżnieniem drużyny gracza `(Ty)` oraz filtrowaniem wyeliminowanych zespołów.
3. **Zegary Ulepszeń (Timers):** Timery ulepszeń generatorów (Diamenty II/III/IV, Szmaragdy II/III, Zniszczenie Łóżek, Nagła Śmierć) z ostrzeżeniem pulsacyjnym przy $< 10$ s.
4. **Statystyki Sesji (Session Tracker):** Kompaktowy pasek z liczbą zabić, ostatecznych zabić, zniszczonych łóżek i wskaźnikiem W/L.
5. **Kolejka Zdarzeń (Event Feed):** Dynamiczne powiadomienia o zniszczeniu łóżek i eliminacjach z animacją zanikania `alpha` i ikonami zdarzeń.

### 11.2. Kotwiczenie i Dopasowanie do Ekranu
Panel wykorzystuje `HudAnchor` do elastycznego pozycjonowania (domyślnie `TOP_LEFT` z przesunięciem $X=10, Y=10$). Wymiary ramki są dynamicznie przeliczane na podstawie szerokości najdłuższego napisu w sekcjach (`renderer.getTextWidth()`) oraz liczby wierszy z uwzględnieniem `padding` i `spacing`.

---

## 12. Ujednolicony Model Komponentów i Rendering Informacyjny (Etap 8B1)

W ramach etapu 8B1 wprowadzono pakiet `pl.zombek.client.core.hud.component`, który standaryzuje rendering wszystkich elementów HUD:

### 12.1. Klasa `HudComponentModel`
- Reprezentuje pełną geometrię: pozycję relatywną (`posX`, `posY`), wymiary nieskalowane (`width`, `height`), skalę (`scale`), widoczność (`visible`), zakotwiczenie (`anchor`), porządek warstw (`zIndex`) oraz styl wizualny (`style`).
- Automatycznie oblicza bezwzględne współrzędne ekranowe w oparciu o `HudAnchor` i aktualne wymiary `screenWidth` oraz `screenHeight`.
- **Viewport Clamping**: Zabezpiecza komponenty przed wysunięciem się poza ekran gry po zmianie współczynnika skali interfejsu (GUI Scale) lub rozmiaru okna:
  $$X_{\text{screen}} \in [0, \text{screenWidth} - \text{scaledWidth}], \quad Y_{\text{screen}} \in [0, \text{screenHeight} - \text{scaledHeight}]$$

### 12.2. Pomocnik Renderowania `HudComponentRenderer`
Dostarcza uniwersalne metody rysowania kontenerów HUD:
- `renderComponentBackground(IHudRenderer, HudComponentModel, style, bgColor, borderColor)` — rysuje tło i obramowanie w stylach:
  - `MINIMAL`: czysty tekst bez tła i obramowania;
  - `CLASSIC`: ciemne prostokątne tło z subtelną ramką (klasyczny HUD klientów PvP);
  - `MODERN`: ciemne tło z akcentem i podwyższonym kontrastem;
  - `CUSTOM`: pełna paleta barw zdefiniowana przez gracza.

### 12.3. Specyfika Renderowania Nowych Modułów
1. **Coordinates (`CoordinatesModule`)**: Obsługuje zarówno kompaktowe jednolinijkowce, jak i dynamiczne bloki wieloliniowe (np. w stylu `SEPARATE`), przeliczając wysokość ramki na podstawie liczby aktywnych wierszy.
2. **Direction HUD (`DirectionHudModule`)**: Rysuje przesuwną taśmę kompasową z podziałkami co $15^\circ$, centralnym wskaźnikiem kursu oraz naniesionymi pozycjami sojuszników z `ITeamDirectionProvider`.
3. **FPS (`FpsModule`)**: Rysuje dynamiczny wykres stabilności klatek (mini-graph) ze słupkami historii i benchmarkowymi liniami średniej oraz minimum.
4. **Ping (`PingModule`)**: Rysuje wskaźnik opóźnienia z dynamicznym kodowaniem barwnym (Good/Medium/High/Critical) oraz obsługą stanu `N/A` w trybie jednoosobowym.
5. **Memory Usage (`MemoryUsageModule`)**: Rysuje graficzny pasek pamięci sterty JVM (Memory Bar) z ostrzeżeniami progowymi przy 75% i 90% zapełnienia.

---

## 13. Zaawansowane Systemy Renderowania: Scoreboard, Boss Bar, Potion Effects, Saturation i Rozwiązywanie Kolizji (Etap 8B2)

### 13.1. Niezależny Renderer Scoreboardu (`CustomScoreboardRenderer`)
- Zastępuje natywny pasek boczny gry (Sidebar Objective) zoptymalizowanym rendererem.
- **Dynamiczna Kalkulacja Szerokości:** Szerokość tablicy dopasowuje się automatycznie do najdłuższego tekstu (nagłówek lub linie) powiększonego o opcjonalny bufor na wyniki punktowe (czerwone cyfry).
- **Formatowanie Wyników:** Obsługa wyrównania punktów (`LEFT`, `CENTER`, `RIGHT`) lub ich całkowitego ukrycia (`hide_scores`).
- **5 Stylów Prezentacji:** `VANILLA` (tradycyjne ciemne tło Mojang), `MINIMAL` (czysty tekst bez tła), `MODERN` (ciemny gradient z akcentem), `GLASS` (subtelne szkło z cienkim obramowaniem), `CUSTOM` (pełna personalizacja kolorów tła, tekstu, punktów i obramowania).
- **Obsługa Wielu Linii i Obcinanie:** Konfigurowalny limit wierszy (`max_lines`, 1–15) oraz automatyczne skracanie tekstu z trzykropkiem (`truncate_lines`).
- **Zapobieganie Podwójnemu Renderowaniu i Fallback:** Loader mixin/event anuluje natywne wywołanie paska bocznego tylko wtedy, gdy moduł jest aktywny, funkcja `replace_vanilla` jest włączona, a moduł nie zgłosił błędu. W razie wyjątku renderowania flaga `hasError` wycofuje klienta do renderera natywnego.

### 13.2. Niezależny Renderer Pasków Bossów (`CustomBossBarRenderer`)
- Obsługuje jednoczesne renderowanie wielu pasków bossów (Smok Kresu, Wither, Najazdy, serwerowe paski niestandardowe).
- **Wielopoziomowe Układanie (Multi-Boss Stacking):** Automatyczne układanie pasków w kolumnie pionowej z regulowanym odstępem (`boss_spacing`).
- **Płynna Interpolacja Animacji (`smooth_animation`):** Krok interpolacji liniowej (`lerp`) wygładzający zmiany paska zdrowia bossa bez skoków.
- **6 Stylów Paska:** `VANILLA` (klasyczne tekstury Minecraft), `CLASSIC` (prosty prostokąt z obrysem), `MODERN` (zaokrąglony pasek z cieniowaniem), `MINIMAL` (cienki pasek 2–3 px), `SEGMENTED` (podział na 6, 10, 12 lub 20 segmentów zgodny z overlayem bossa), `CUSTOM` (indywidualne barwy).
- **Wsparcie Kolorów:** Dynamiczne barwienie pasków według typów Minecraft (`PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, `WHITE`) lub jednolity kolor zdefiniowany przez gracza.

### 13.3. Renderer Efektów Statusu (`PotionEffectsModule`)
- Rysuje aktywne efekty mikstur z podziałem na kategorie (`BENEFICIAL`, `NEUTRAL`, `HARMFUL`).
- **Elastyczne Układy:** `CLASSIC` (ikona, nazwa i czas w kolumnie), `COMPACT` (skrócona nazwa i czas), `DETAILED` (pełna nazwa z rzymskim stopniem i czasem), `ICONS_ONLY` (poziomy lub pionowy pasek samych ikon), `CUSTOM`.
- **Wizualne Ostrzeżenie o Wygasaniu (`expiring_alert`):** Efekty z czasem poniżej progu (np. $\le 10$ s) pulsują kolorem ostrzegawczym.
- **Ukrywanie Domyślnych Ikon:** Opcja `hide_vanilla_hud` wyłącza standardowe ikony w prawym górnym rogu ekranu, zapobiegając powielaniu informacji.

### 13.4. Renderer Nasycenia i Głodu (`SaturationModule`)
- Prezentuje wskaźniki głodu (0–20), nasycenia (0–20) oraz wyczerpania (0–4.0).
- **Rzetelność Danych (Fair Play):** Gdy serwer nie udostępnia danych nasycenia na kliencie, renderowany jest czytelny napis `Niedostępne` / `Unavailable`. Klient nigdy nie symuluje ani nie generuje fałszywych wartości.
- **Formaty:** `CLASSIC` (etykiety ze stanem), `COMPACT` (ułamki np. `20/20`), `DETAILED` (głód, nasycenie i wyczerpanie), `MINIMAL` (wartości liczbowe).
- **Ostrzeżenie o Niskim Poziomie:** Wizualne podświetlenie paska/tekstu gdy poziom głodu uniemożliwia sprint ($\le 6$).

### 13.5. System Priorytetyzacji i Unikania Kolizji (`HudPriorityResolver`)
- Odpowiada za dynamiczne rozwiązywanie konfliktów przestrzennych na ekranie (np. gdy Scoreboard i Potion Effects lub Boss Bar znajdą się w tym samym obszarze ekranu).
- **Porządek Priorytetów:** Komponenty sortowane są malejąco według `renderPriority`:
  1. Boss Bar (100)
  2. Scoreboard (80)
  3. Potion Effects (60)
  4. Saturation (50)
  5. Inne elementy HUD (0)
- **Detekcja Kolizji AABB:** Wykorzystuje funkcję `intersects(box1, box2)`.
- **Przesuwanie w Osi Y:** Dla elementów kolidujących z wyższym priorytetem:
  - Dla kotwic górnych (`TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`): element o niższym priorytecie przesuwa się w dół:
    $$Y_{\text{new}} = Y_{\text{prev}} + \text{height}_{\text{prev}} + \text{spacing}$$
  - Dla kotwic dolnych (`BOTTOM_LEFT`, `BOTTOM_CENTER`, `BOTTOM_RIGHT`): element przesuwa się w górę ekranu:
    $$Y_{\text{new}} = \text{screenHeight} - Y_{\text{prev}} + \text{spacing}$$
- Zapewnia to idealną czytelność interfejsu niezależnie od liczby jednocześnie wyświetlanych pasków i modułów.

---

## 14. Moduły Czasowe HUD (Etap 8B3)

### 14.1. Moduł Zegara (`ClockModule` — Priorytet: 40)
- Wyświetlanie czasu rzeczywistego (systemowego) oraz czasu świata gry Minecraft (00:00–23:59).
- Tryby: `DIGITAL`, `MINIMAL`, `DETAILED`, `MINECRAFT`, `DUAL`, `CUSTOM`.
- Wskaźniki faz pory dnia (wschód, południe, zachód, północ) i ikony dnia/nocy.
- Restrykcyjny fallback na serwerach multiplayer (`MC: N/A`) oraz znacznik `[PREVIEW]` w trybie edycji GUI.

### 14.2. Moduł Licznika Dni (`DayCounterModule` — Priorytet: 35)
- Prezentacja bieżącego dnia świata ($\lfloor \text{gameTime} / 24000 \rfloor + 1$), wieku świata lub dnia sesji.
- Wykrywanie cofnięć czasu gry (rollback / `/time set`) z flagą diagnostyczną `[Reset]`.
- Obsługa ręcznego przesunięcia `manual_offset` z oznaczeniem `(Manual)`.
- Całkowity brak symulacji na serwerach wieloosobowych (`Day: N/A`).

### 14.3. Moduł Czasu Gry (`PlaytimeModule` — Priorytet: 30)
- Prezentacja czasu sesji klienta (od startu gry), sesji w świecie lub całkowitego czasu SP (`PLAY_TIME` / `TOTAL_WORLD_TIME`).
- Formaty: `HH_MM_SS`, `DD_HH_MM_SS`, `HUMAN_READABLE`, `COMPACT`.
- Ochrona prywatności: bezpieczna historia do 100 ostatnich sesji, zero wrażliwych danych uwierzytelniających, adresy serwerów domyślnie wyłączone (opt-in).

### 14.4. Moduł Precyzyjnego Stopera (`StopwatchModule` — Priorytet: 28)
- Odliczanie oparte na monotonicznym sprzętowym zegarze `System.nanoTime()` — całkowita niezależność od TPS serwera i FPS grafiki.
- Niezależne profile: `SPEEDRUN`, `BUILDING`, `MINING`, `CUSTOM`.
- Rejestracja międzyczasów (okrążeń), wyliczanie delty czasu $\Delta$ z kolorowaniem zysk/strata, średni i najlepszy czas.
- Pełna integracja ze skrótami klawiszowymi `KeybindManager`.

---

## 15. Moduły Inwentarza, Śledzenia i Adresu Serwera HUD (Etap 8B4)

### 15.1. Moduł Licznika Przedmiotów (`ItemCounterModule` — Priorytet: 32)
- Renderowanie ilości określonych przedmiotów w czasie rzeczywistym.
- Źródła slotów konfigurowalne przez użytkownika (`HOTBAR`, `MAIN_INVENTORY`, `OFFHAND`, `ARMOR`).
- Dynamiczne sprawdzanie progów ostrzegawczych (`warning_threshold`, `critical_threshold`) i natychmiastowa zmiana koloru licznika.
- Tryby układu: `COMPACT`, `DETAILED`, `ICONS_ONLY`, `LIST`, `HORIZONTAL`, `CUSTOM`.
- Bezpieczny tryb podglądu `[PREVIEW]` poza światem gry.

### 15.2. Moduł Śledzenia Zmian Przedmiotów (`ItemTrackerModule` — Priorytet: 29)
- Renderowanie historii modyfikacji ekwipunku podczas sesji (zdobycie, użycie, wyrzucenie, postawienie bloku, itp.).
- Ścisłe poziomy pewności Fair Play: zdarzenia niepotwierdzone są renderowane jako `Unknown Change (Unknown)`.
- Ignorowanie ruchów wewnętrznych (transfery między slotami i podziały stacków o zerowej zmianie sumarycznej).
- Tryby prezentacji: strumień ostatnich zdarzeń (`FEED`), tabela statystyk sesji (`STATISTICS`), podsumowanie netto (`COMPACT_SUMMARY`), podział na zyski/straty (`DETAILED_SPLIT`).

### 15.3. Moduł Adresu Serwera (`ServerAddressModule` — Priorytet: 24)
- Prezentacja danych połączenia (Singleplayer, Multiplayer, LAN).
- W Singleplayer wyświetla "Singleplayer" i nazwę świata (lub `[HIDDEN]`), **nigdy nie generując fałszywych adresów IP (127.0.0.1)**.
- Domyślnie wyłączone wyświetlanie pełnego adresu (`show_server_address: false`) dla ochrony prywatności.
- Integracja z `ServerAddressPrivacyController` (maskowanie, ukrywanie, streamer mode) i lokalnym `ServerAliasManager`.

---

## 16. Renderowanie Listy Graczy Tab i Etykiet NameTags (Etap 8B5)

### 16.1. Renderer Listy Graczy (`CustomTabRenderer` & `TabEditorModule` — Priorytet: 120)
- **Priorytet w warstwie HUD:** Zarejestrowany w `HudPriorityResolver.PRIORITY_TAB_EDITOR = 120` jako nakładka o wysokim priorytecie (renderowana ponad standardowymi modułami informacyjnymi po przytrzymaniu klawisza TAB).
- **Architektura siatki wielokolumnowej:** `TabLayoutManager` dynamicznie dzieli listę graczy (od 1 do 80+ graczy) na zrównoważoną liczbę kolumn (od 1 do 4 kolumn) z zachowaniem czytelności i marginesów ekranu.
- **Nagłówek i Stopka (`TabHeaderFooterManager`):**
  - `ORIGINAL`: Parsowanie surowego tekstu JSON dostarczonego z pakietów serwera.
  - `CUSTOM`: Niestandardowy tekst skonfigurowany przez gracza.
  - `HIDDEN`: Pominięcie nagłówka/stopki w celu maksymalizacji miejsca na listę graczy.
- **Wizualizacja Pingu (`TabPingMode`):**
  - `NUMERIC`: Rzeczywista wartość opóźnienia w ms (`42ms`) z dynamicznym progowaniem kolorów (zielony < 100ms, żółty < 200ms, czerwony >= 200ms).
  - `BARS`: 5-stopniowy wskaźnik kreskowy zgodny z vanillą.
  - `COLORED_TEXT`: Kolorowany tekst pingu bez jednostki ms.
  - `MINIMAL_ICON`: Dyskretna kropka statusu połączenia.
- **Wyróżnianie Znajomych (`TabFriendHighlightManager`):**
  - Podświetlanie tła wiersza (np. ciemnozielony półprzezroczysty prostokąt).
  - Niestandardowy kolor nicku ARGB i gwiazdka `★`.
  - Wyświetlanie lokalnego aliasu (`Nickname [Alias]`).
- **Płynność i Animacje:** Interpolowane przejścia krycia (fade in/out) oraz opcjonalne przesunięcie (slide).

### 16.2. Renderer Etykiet 3D NameTags (`CustomNameTagRenderer` & `NameTagsModule`)
- **Renderowanie Billboard 3D:**
  - Obrót etykiety w przestrzeni świata gry tak, aby zawsze była zwrócona prostopadle do kamery gracza (`dispatcher.getRotation()`).
  - Przesunięcie pionowe: `entity.getHeight() + 0.5f` ponad modelem encji.
  - **Depth Test / Fair Play:** Etykiety nie używają `SEE_THROUGH` ani nie pomijają bufora głębokości (Depth Buffer). Ściany, sufity i bloki przeszkadzające bezwzględnie zasłaniają nametag.
- **Stylowanie i Informacje:**
  - Pasek zdrowia lub tekst (`NUMERIC`, `HEARTS`, `BAR`, `COMPACT`, `CUSTOM`).
  - Dystans w blokach z formatowaniem (`BLOCKS`, `DECIMAL`, `ROUNDED`).
  - Odznaki znajomego (`[FRIEND]`, custom alias, gwiazdka `★`).
  - Wskaźniki wyposażenia i trwałości zbroi.
- **Podgląd GUI (`IHudElement`):**
  - Zaimplementowany bezpieczny podgląd w menu Right Shift z przykładowym graczem `[PREVIEW] Alex (Friend)` z 20 HP, 12m dystansem i kompletem zbroi.

---

## 17. Renderowanie Nowoczesnego Czatu i Tytułów Ekranowych (Etap 8B6)

### 17.1. Renderer Czatu (`CustomChatRenderer` & `ChatModule` — Priorytet: 110)
- **Priorytet w warstwie HUD:** Zarejestrowany w `HudPriorityResolver.PRIORITY_CHAT = 110`. Zastępuje vanillowe okno czatu, renderując w pełni spersonalizowany widok komunikatów, paska zakładek oraz pola wprowadzania.
- **Pasek Zakładek (`ChatTabManager`):**
  - Renderowanie horyzontalnego paska zakładek (`[Wszystkie]`, `[Główny]`, `[Prywatne]`, `[Party]`, `[Gildia]`, itp.) nad oknem czatu gdy interfejs wprowadzania jest otwarty (`chatOpen == true`).
  - Wizualne oznaczenie aktywnej zakładki (podświetlenie tła `0x80336699` i jasna ramka) oraz nieprzeczytanych wiadomości w postaci czerwonej plakietki liczbowej `(+N)`.
- **Renderowanie Wiadomości (`ChatMessageEntry`):**
  - Obsługa prefiksów czasowych zformatowanych wg `TimestampFormat` (`HH:mm`, `HH:mm:ss`, itp.) z osobnym kolorem wyciszonym (np. ciemnoszary `0xFFAAAAAA`).
  - Wizualne wskaźniki powtórzonych wiadomości: kompaktowy licznik `×N` (np. `0xFFFFAA00`) zapobiegający zalewaniu czatu duplikatami.
  - Wyróżnienia kolorystyczne i podświetlenia tła dla wzmianek o graczu (`@Player`), znajomych (`[FRIEND]`) oraz reguł filtrów (`UniversalChatFilterRule`).
  - Dynamiczne zawijanie tekstu do szerokości okna (`chatWidth`) z uwzględnieniem skali (`chatScale`).
- **Płynność i Fade-out:**
  - Płynne zanikanie starych wiadomości po upływie `fadeTimeSeconds` (domyślnie 10.0s).
  - W trybie otwartego GUI czatu (`chatOpen == true`) zanikanie jest wyłączone, a tło okna i pasek przewijania są w pełni widoczne.
- **Pole Wprowadzania (`ChatInputManager`):**
  - Nowoczesny panel wejściowy renderowany na dole ekranu z zachowaniem kopii roboczej (draft) niezależnie dla każdej zakładki.
  - Wyświetlanie placeholderów kanałowych (np. `Napisz wiadomość na /party...`) oraz licznika znaków (`N / 256`).

### 17.2. Renderer Tytułów i Paska Akcji (`CustomTitleRenderer` & `TitlesModule` — Priorytet: 115)
- **Priorytet w warstwie HUD:** Zarejestrowany w `HudPriorityResolver.PRIORITY_TITLES = 115`.
- **Separacja Tytułów i Action Bar:**
  - `TitleQueueManager` i `ActiveTitleState` precyzyjnie rozdzielają cykl życia głównego tytułu/podtytułu od paska akcji (`Action Bar`).
  - Czyszczenie tytułów przez serwer (np. pakiet `ClearTitleS2CPacket`) nie usuwa aktywnego komunikatu paska akcji.
  - Komunikaty Action Bar nigdy nie trafiają do bufora historii czatu.
- **Silnik Animacji Tytułów (`TitleAnimationStyle`):**
  - `FADE`: Interpolacja przezroczystości (alpha) w fazach `fadeInTicks`, `stayTicks`, `fadeOutTicks`.
  - `SLIDE`: Płynne wjeżdżanie z góry ekranu w fazie fade-in i zjazd w fazie fade-out.
  - `SCALE`: Płynne powiększanie (od 0.5x do 1.0x w skali `titleScale`) podczas pojawiania się tekstu.
  - `STATIC`: Natychmiastowe wyświetlanie bez animacji z pełnym kryciem.
- **Stylowanie i Pozycjonowanie:**
  - Niezależne skale dla tytułu głównego (`titleScale`, domyślnie 2.5x - 3.5x) oraz podtytułu (`subtitleScale`, domyślnie 1.3x - 1.8x).
  - Wybór wyrównania tekstu (`TitleAlignment`: `CENTER`, `TOP`, `BOTTOM`).
  - Opcjonalne tło prostokątne za tytułem z regulowaną przezroczystością dla maksymalnej czytelności na jasnych tłach świata gry.
- **Integracja Wielomodowa (Multi-Loader Hooks):**
  - **Fabric / Quilt:** `ChatHudMixin` i `InGameHudMixin` anulują domyślne renderowanie `renderTitle` i `renderChatMessage` w vanilli i delegują rysowanie do `CustomTitleRenderer` oraz `CustomChatRenderer`.
  - **Forge / NeoForge:** `onRenderGuiOverlayPre` anuluje `VanillaGuiOverlay.CHAT_PANEL`, `VanillaGuiOverlay.TITLE_TEXT` oraz `VanillaGuiOverlay.RECORD_OVERLAY`, a zdarzenie `RenderGuiEvent` / `onRenderGui` wywołuje renderery ZombekClient w kolejności priorytetów HUD.

---

## 18. Renderowanie Graficznego Net Graph (Etap 8B7)

### 18.1. Renderer Wykresu (`NetworkGraphRenderer` & `NetGraphModule` — Priorytet: 36)
- **Priorytet w warstwie HUD:** Zarejestrowany w `HudPriorityResolver.PRIORITY_NET_GRAPH = 36` (pomiędzy Day Counter 35 a Clock 40).
- **Struktura Wizualna:**
  - Panel tła o regulowanym kolorze i przezroczystości (`0xFF111827`, 75% krycia) z obramowaniem.
  - Nagłówek: tytuł `Net Graph`, plakietka statusu (`[Connected]`, `[Singleplayer]`) oraz dynamiczna odznaka jakości (`[Excellent]`, `[Good]`, `[Moderate]`, `[Poor]`).
  - Obszar wykresu (Plot Area): siatka pomocnicza (linie poziome na 25%, 50%, 75% wysokości), dynamiczna linia średniego pingu (`avg: XX ms`).
  - Stopka: legenda i live badge wartości bieżących (`Ping`, `Jitter`, `RX`, `TX`).
- **Tryby Serii Pomiarowych (`NetGraphMode`):**
  - `LATENCY`: wykres opóźnienia i fluktuacji (ping/jitter).
  - `TRAFFIC`: dwie odrębne serie przepustowości (RX cyan, TX orange).
  - `PACKETS`: częstotliwość transmisji pakietowej (RX PPS blue, TX PPS magenta).
  - `COMBINED`: wielowarstwowa prezentacja z autorską synchronizacją skal.
  - `CUSTOM`: selektywne włączanie wybranych serii.
- **Płynność i Automatyczne Skalowanie:**
  - Auto Scale wylicza szczyt próbek w oknie z marginesem 25% (headroom).
  - Wygładzanie tłumione (damping factor 0.20) zapobiega gwałtownym skokom pionowej skali osi Y przy pojedynczych pikach transferu.
- **Obsługa Trybu Singleplayer:**
  - Klient nie renderuje fikcyjnych wartości 0 ms, zastępując wykres czytelną informacją `Singleplayer — Integrated Server` oraz `External Latency: N/A (Local loopback)`.
- **Tryb Podglądu w Menu (`IHudElement`):**
  - Funkcja `renderPreview` generuje realistyczną, dynamiczną krzywą testową (28-36 ms, 12 KB/s RX), umożliwiając graczowi precyzyjne ustawienie pozycji i stylu w menu Right Shift.

---

## 19. Architektura Renderera HUD Editora (Etap 9A)

### 19.1. Rozszerzenie Kontraktu `IHudRenderer`
W Etapie 9A interfejs `IHudRenderer` został rozszerzony o domyślną metodę pomocniczą wyśrodkowanego tekstu z cieniem:
```java
default void drawCenteredTextWithShadow(String text, int centerX, int y, int color) {
    int width = getTextWidth(text);
    drawText(text, centerX - width / 2, y, color, true, 1.0f, false);
}
```
Zapewnia to 100% spójność wyśrodkowanych etykiet, nagłówków paneli, podglądów i przycisków na wszystkich loaderach bez konieczności duplikacji kodu w klasach adapterów.

### 19.2. Wielowarstwowy Cykl Renderowania Edytora (`HudEditorRenderer`)
Architektura renderowania ekranu edytora HUD składa się ze ściśle określonych warstw graficznych:
1. **Dark Canvas Backdrop:** Półprzezroczyste przyciemnienie sceny gry (`0x88000000`).
2. **Obszar Roboczy Canvas (`HudEditorCanvas`):**
   - Obrys granic ekranu (kolor fioletowo-niebieski ZombekClient `0x448B5CF6`).
   - Opcjonalna subtelna siatka pomocnicza (Grid lines o regulowanym skoku, np. 20px).
3. **Renderowanie Komponentów HUD w Trybie Preview:**
   - Wywołanie `previewProvider.renderPreview(...)` dla wszystkich zarejestrowanych i widocznych komponentów.
   - Bounding Boxy: Rysowanie prostokątnych ramek (`HudComponentState.getBounds()`).
   - Wskaźniki selekcji: Aktywnie zaznaczony komponent otrzymuje podświetloną ramkę akcentową (`0xFF8B5CF6`) oraz plakietkę z nazwą elementu i rozmiarem.
   - Wskaźniki nieaktywności: Elementy z flagą `visible=false` w podglądzie są wyszarzone lub oznaczone kreskowanym obrysem.
4. **Lewy Panel Boczny (`HudEditorComponentList`):**
   - Pole wyszukiwania na żywo (Live Search) filtrujące po nazwach i identyfikatorach.
   - Pigułki filtrów kategorii (`ALL`, `PVP`, `HUD`, `VISUAL`, `SURVIVAL`, `OTHER`).
   - Przewijalna lista kafelków komponentów z przełącznikami widoczności (Eye icon) i wskaźnikami aktywnej selekcji.
5. **Prawy Panel Boczny (`HudEditorInspector`):**
   - Nagłówek wybranego komponentu z identyfikatorem i nazwą.
   - Kontrolki modyfikacji współrzędnych X/Y (przyciski krokowe -10, -1, +1, +10 oraz klawisze strzałek).
   - Kontrolki modyfikacji skali (przyciski -0.05, +0.05 z ograniczeniem do zakresu [0.5, 2.5]).
   - Przełącznik punktu zakotwiczenia (`AnchorPoint`: Top-Left, Top-Center, Top-Right, Center-Left, Center, Center-Right, Bottom-Left, Bottom-Center, Bottom-Right).
   - Przycisk przywrócenia domyślnej pozycji i skali dla zaznaczonego komponentu ("↺ Reset Component").
6. **Górny Pasek Narzędziowy (Top Bar):**
   - Nazwa aktywnego profilu układu (`Default Layout`).
   - Tryb rozdzielczości referencyjnej (`Ref: 1920x1080` / `Canvas: WxH`).
   - Wskaźnik stanu: `Unsaved Changes` (gdy `hasUnsavedChanges() == true`).
   - Przyciski akcji: "↺ Reset All" oraz "💾 Save & Close".
7. **Dolny Pasek Skrótów (Bottom Bar):**
   - Wskazówki sterowania: `[Click]` Select, `[Tab]` Next, `[Arrows]` Move (±1px, Shift: ±10px), `[Del]` Toggle Visibility, `[Ctrl+S]` Save, `[Esc]` Close.
8. **Modal Potwierdzenia Odrzucenia Zmian (Unsaved Changes Dialog):**
   - Wyświetlany przy próbie wyjścia z edytora klawiszem `ESC` w przypadku obecności niezapisanych modyfikacji.
   - Przyciski: "Save & Exit", "Discard & Exit", "Cancel".

---

## 20. Renderowanie Uchwytów, Płótna i Siatki (Etap 9B)

W Etapie 9B rozbudowano potok renderowania `HudEditorRenderer` oraz `HudEditorCanvas` o elementy bezpośredniej manipulacji myszą:

1. **8 Uchwytów Zmiany Rozmiaru (Resize Handles):**
   - Renderowane jako kwadratowe wskaźniki (domyślnie 6x6 px) wokół aktywnego komponentu na pozycjach `TL`, `TC`, `TR`, `ML`, `MR`, `BL`, `BC`, `BR`.
   - Wypełnienie w kolorze akcentu motywu klienta (`0xFF00E5FF` / `0xFFFFFFFF`) z kontrastującą ciemną ramką (`0xFF1E1E2E`).
   - Wyróżnienie po najechaniu kursorem (Hover Effect) poprzez rozjaśnienie barwy.

2. **Wizualizacja Siatki Pomocniczej (`HudGridManager`):**
   - Renderowanie za pomocą nowych prymitywów `drawHLine(x, y, w, color)` oraz `drawVLine(x, y, h, color)` w interfejsie `IHudRenderer`.
   - 5 stylów: kropki w węzłach (`DOTS`), linie ciągłe (`LINES`), siatka krzyżykowa (`CROSSHAIR_GRID`), ukryta (`NONE`) oraz `CUSTOM`.
   - Dynamiczne dopasowanie przezroczystości (opacity 0.05 - 1.0) i grubości linii bez wpływu na wydajność klatkową (frustum culling w rzutni).

3. **Linie Pomocnicze Przyciągania (Snapping Guides):**
   - **Oś środka ekranu (Center Guide):** Rysowana przez całą wysokość/szerokość płótna w kolorze Cyan (`0xFF00E5FF`).
   - **Krawędzie ekranu (Edge Guide):** Rysowana wzdłuż brzegu ekranu w kolorze bursztynowym (`0xFFFFB300`).
   - **Linia siatki (Grid Guide):** Rysowana wzdłuż aktywnej linii siatki w kolorze błękitnym (`0xFF2979FF`).

4. **Strefa Bezpieczna (Safe Area Overlay):**
   - Subtelna przerywana obwódka w odległości 8-16 px od krawędzi ekranu (`HudSafeAreaManager`), pomagająca zachować czytelność interfejsu na różnych monitorach.

5. **Wskaźnik Kursora i Trybu w Pasku Narzędzi (Status Pill):**
   - Prezentacja aktualnego powiększenia (np. `100%`, `150%`), współrzędnych kursora oraz aktywnego trybu (`SELECT`, `MOVE`, `RESIZE`, `PAN`).

---

## 21. Renderowanie Grup, Z-Index, Marquee i Pasków (Etap 9C)

W Etapie 9C rozbudowano silnik renderowania `HudEditorRenderer` o wizualizację zaawansowanych struktur edytora:

1. **Ramka Zaznaczania (Marquee Box Selection):**
   - Renderowana w trakcie przeciągania myszą po pustym obszarze kanwy.
   - Półprzezroczyste wypełnienie akcentem motywu (`0x334A90E2`) z wyraźną obwódką 1px (`0xFF4A90E2`).
   - Dynamiczne podświetlanie elementów wpadających w obszar selekcji w czasie rzeczywistym.

2. **Obwiednie Grup Logicznych (Group Bounding Boxes):**
   - Renderowanie przerywanej obwiedni obejmującej wszystkie komponenty należące do grupy (`HudGroup`).
   - Etykieta grupy wyświetlana tuż nad lewym górnym rogiem obwiedni z nazwą grupy i ikoną spięcia.
   - Kolorystyka obwiedni odpowiada skonfigurowanemu kolorowi akcentu grupy.

3. **Deterministyczny Potok Sortowania Z-Index:**
   - Wszystkie komponenty przed renderowaniem są sortowane rosnąco według wartości Z-Index.
   - Gwarancja, że elementy warstwy `Background` (Z: 0-99) są renderowane pod `Main HUD` (Z: 100-199), a te pod `Information` (Z: 200-299) i `Notifications` (Z: 300+).
   - Eliminacja migotania nakładających się komponentów na płaszczyźnie 2D.

4. **Symulacja Proporcji Ekranu i Letterboxing:**
   - W trybach symulacji (np. 4:3 na ekranie 16:9 lub 21:9) obszar nieaktywny jest zaciemniany czarnymi pasami (Letterbox/Pillarbox) z poziomem przezroczystości `0xCC000000`.
   - Obrys symulowanego ekranu renderowany w kolorze złotym (`0xFFFFD700`) z etykietą proporcji.

5. **Wielofunkcyjny Pasek Górny i Dolny:**
   - Górny pasek narzędzi z przyciskami `[⮌ Undo]`, `[⮎ Redo]`, menu profili, menu presetów oraz przyciskami importu/eksportu.
   - Dolny pasek statusu z powiadomieniami o błędach i ostrzeżeniach walidacyjnych (`LayoutValidationWarning`).

---

## 11. Wskaźnik HUD Zoom Indicator (`zoom_indicator` — Etap 8C1)

W Etapie 8C1 zarejestrowano 33. oficjalny komponent interfejsu klienta w `HudComponentRegistry`: **Wskaźnik Przybliżenia (Zoom Indicator)**.

### 11.1. Metadane Deskryptora Komponentu
- **ID:** `"zoom_indicator"`
- **Nazwa:** `"Zoom Indicator"`
- **Kategoria:** `ModuleCategory.VISUAL`
- **Typ Pozycjonowania:** `HudComponentType.SCREEN_SPACE`
- **Domyślne Zakotwiczenie:** `AnchorPoint.BOTTOM_CENTER` ($X = 0$, $Y = -50$)
- **Dozwolone Zdolności:** `POSITION`, `SCALE`, `VISIBILITY`, `Z_INDEX`, `ANCHOR`
- **Priorytet Renderowania:** `HudPriorityResolver = 130`

### 11.2. Style Renderowania (`ZoomIndicatorStyle`)
- **`BADGE`:** Zaokrąglona plakietka z półprzezroczystym ciemnym tłem (`0x80000000`), elegancką ramką akcentową i wyśrodkowanym napisem.
- **`COMPACT`:** Zwięzła plakietka z minimalnym paddingiem (2px) oszczędzająca miejsce na ekranie.
- **`TEXT_ONLY`:** Surowy tekst z cieniem czcionki bez tła i obrysu, w kolorze skonfigurowanym w module Zoom.

### 11.3. Format Wyświetlania i Podgląd Demonstracyjny
- Jeżeli włączona jest opcja `show_zoom_level`, etykieta prezentuje precyzyjny bieżący mnożnik: `ZOOM: 4.0x`.
- W przeciwnym razie wyświetlany jest minimalistyczny napis `ZOOM`.
- W edytorze HUD dostawca podglądu `HudComponentPreviewProvider` renderuje przykładową etykietę `ZOOM: 4.0x`, pozwalając na intuicyjne pozycjonowanie wskaźnika nawet wtedy, gdy zoom w danej chwili nie jest wciśnięty w grze.

---

## 12. Izolacja Warstwy HUD od Efektów Renderowania (Etap 8C3)

W Etapie 8C3 wprowadzono zaawansowane efekty post-processingu i gradacji barw (`ColorSaturationModule`, `FogCustomizerModule`, `GlintColorizerModule`, `ParticleChangerModule`).

### 12.1. Ochrona Czytelności i Nienaruszalność Kolorów HUD
- Wszelkie operacje modyfikacji nasycenia, kontrastu, jasności czy temperatury barwowej są filtrowane przez parametr `TargetType`.
- Elementy interfejsu użytkownika (`TargetType.HUD`) korzystają z gwarancji `preserve_hud = true`, co sprawia, że silnik `ColorGradingController` pomija je w potoku przekształceń barwnych.
- Teksty modułów HUD, celownik (`CrosshairRenderer`), wskaźniki pancerza, serca, pasek głodu oraz czat zachowują 100% oryginalnych waniliowych barw i kontrastu.

### 12.2. Porządek Warstw w ZombekRenderEffectController
Warstwa HUD jest renderowana jako ostatnia w potoku klatki graficznej:
$$\text{World Fog} \to \text{World Visuals} \to \text{Color Grading} \to \text{Item Glint} \to \text{Particles} \to \mathbf{HUD\ Layer}$$
Gwarantuje to brak zakłóceń tekstur GUI przez cząsteczki, poświaty zaklęć czy mgłę środowiskową.
















