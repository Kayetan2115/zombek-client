# Rejestr Komponentów HUD (`HudComponentRegistry`) — ZombekClient

## 1. Przegląd Rejestru

Klasa [`HudComponentRegistry`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/editor/HudComponentRegistry.java) jest dynamicznym, wielowątkowym singletonem odpowiedzialnym za rejestrację i udostępnianie metadanych wszystkich modyfikowalnych elementów interfejsu klienta.

Zapewnia:
- Bezpieczne pobieranie metadanych bez względu na wielkość znaków w identyfikatorze.
- Filtrowanie elementów według kategorii (`PvP`, `HUD`, `Survival`, `Visual`, `Other`).
- Klasyfikację przestrzenną według typu renderowania (`HudComponentType`).
- Określanie dozwolonych operacji edycyjnych (`HudComponentCapability`).
- Udostępnianie realistycznych procedur renderowania demo (`HudComponentPreviewProvider`).

---

## 2. Typy Komponentów (`HudComponentType`)

1. **`SCREEN_SPACE`**:
   - Klasyczne elementy 2D renderowane bezpośrednio na rzutni ekranu gry.
   - Posiadają pełne możliwości dostosowania pozycji, kotwicy narożnej, skali i widoczności.
   - Przykłady: Keystrokes, Coordinates, FPS, Ping, Net Graph, Scoreboard, Potion Effects.

2. **`CENTER_SCREEN`**:
   - Elementy ekranowe wycentrowane względem celownika gracza.
   - Pozycja bazowa jest zakotwiczona na środku, modyfikowalna jest skala oraz widoczność.
   - Przykład: Celownik (Crosshair).

3. **`WORLD_SPACE`**:
   - Elementy trójwymiarowe, których pozycja w świecie wynika z położenia encji lub bloków w przestrzeni gry.
   - W edytorze HUD prezentowane są w celach poglądowych i konfiguracji stylizacji, natomiast ich pozycja przestrzenna oznaczona jest jako nieprzemieszczalna w płaszczyźnie 2D.
   - Przykłady: NameTags (nick nad graczem), TNT Countdown (licznik nad blokiem TNT), TeamView (wskaźniki nad sojusznikami).

4. **`GUI_OVERLAY`**:
   - Główne nakładki interfejsu gry posiadające własne systemy renderowania i formatowania.
   - W edytorze HUD obsługują zmianę obszaru, skali lub widoczności.
   - Przykłady: Chat Box, Tablist (Tab Editor), Screen Titles.

---

## 3. Możliwości Komponentu (`HudComponentCapability`)

Każdy zarejestrowany deskryptor definiuje zestaw dozwolonych flag manipulacji:
- `POSITION` — zezwala na przesuwanie elementu w osiach X i Y.
- `SCALE` — zezwala na powiększanie lub pomniejszanie (mnożnik 0.2x – 3.0x).
- `SIZE` — zezwala na zmianę wymiarów szerokości i wysokości bounding boxa.
- `VISIBILITY` — zezwala na ukrywanie/pokazywanie w HUD.
- `Z_INDEX` — zezwala na regulację kolejności renderowania (warstwy).
- `ANCHOR` — zezwala na zmianę punktu odniesienia do krawędzi lub rogów ekranu.

---

## 4. Tabela Zarejestrowanych Komponentów ZombekClient

| ID | Wyświetlana Nazwa | Kategoria | Typ | Domyślna Pozycja | Domyślna Kotwica | Możliwości |
|---|---|:---:|:---:|:---:|:---:|---|
| `keystrokes` | Keystrokes | PvP | SCREEN_SPACE | (20, 20) 76x56 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `cps` | CPS Display | PvP | SCREEN_SPACE | (20, 82) 70x18 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `armor_status` | Armor Status | PvP | SCREEN_SPACE | (10, 120) 54x70 | BOTTOM_LEFT | POS, SCALE, VIS, ANCHOR |
| `combo_counter` | Combo Counter | PvP | SCREEN_SPACE | (0, 35) 75x18 | TOP_CENTER | POS, SCALE, VIS, ANCHOR |
| `cooldowns` | Cooldowns | PvP | SCREEN_SPACE | (10, 80) 70x36 | BOTTOM_RIGHT | POS, SCALE, VIS, ANCHOR |
| `pvp_info` | PvP Info | PvP | SCREEN_SPACE | (10, 195) 80x20 | BOTTOM_LEFT | POS, SCALE, VIS, ANCHOR |
| `coordinates` | Coordinates | HUD | SCREEN_SPACE | (10, 10) 125x30 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `direction_hud` | Direction HUD | HUD | SCREEN_SPACE | (0, 10) 130x20 | TOP_CENTER | POS, SCALE, VIS, ANCHOR |
| `fps` | FPS Display | HUD | SCREEN_SPACE | (10, 44) 65x18 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `ping` | Ping Display | HUD | SCREEN_SPACE | (10, 64) 65x18 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `memory_usage` | Memory Usage | HUD | SCREEN_SPACE | (10, 84) 90x18 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `clock` | Clock | HUD | SCREEN_SPACE | (10, 104) 75x18 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `day_counter` | Day Counter | HUD | SCREEN_SPACE | (10, 124) 75x18 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `playtime` | Playtime | HUD | SCREEN_SPACE | (10, 144) 90x18 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `stopwatch` | Stopwatch | HUD | SCREEN_SPACE | (10, 164) 90x18 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `scoreboard` | Scoreboard | HUD | SCREEN_SPACE | (10, 80) 110x120 | TOP_RIGHT | POS, SCALE, VIS, ANCHOR, SIZE |
| `potion_effects`| Potion Effects | HUD | SCREEN_SPACE | (10, 10) 90x60 | TOP_RIGHT | POS, SCALE, VIS, ANCHOR |
| `saturation` | Saturation | Survival | SCREEN_SPACE | (0, 48) 85x18 | BOTTOM_CENTER | POS, SCALE, VIS, ANCHOR |
| `bossbar` | Boss Bar | HUD | SCREEN_SPACE | (0, 15) 160x24 | TOP_CENTER | POS, SCALE, VIS, ANCHOR |
| `item_counter` | Item Counter | Survival | SCREEN_SPACE | (10, 140) 70x20 | BOTTOM_LEFT | POS, SCALE, VIS, ANCHOR |
| `item_tracker` | Item Tracker | Survival | SCREEN_SPACE | (10, 165) 85x26 | BOTTOM_LEFT | POS, SCALE, VIS, ANCHOR |
| `server_address`| Server Address | HUD | SCREEN_SPACE | (10, 10) 110x18 | BOTTOM_RIGHT | POS, SCALE, VIS, ANCHOR |
| `net_graph` | Net Graph | HUD | SCREEN_SPACE | (10, 32) 140x60 | BOTTOM_RIGHT | POS, SCALE, VIS, ANCHOR, SIZE |
| `uhc_session_hud`| UHC Session HUD | Survival | SCREEN_SPACE | (10, 40) 100x45 | TOP_LEFT | POS, SCALE, VIS, ANCHOR |
| `bedwars_helper`| Bedwars Helper | Other | SCREEN_SPACE | (10, 50) 120x80 | TOP_RIGHT | POS, SCALE, VIS, ANCHOR |
| `crosshair` | Crosshair | Visual | CENTER_SCREEN | (0, 0) 16x16 | CENTER | SCALE, VIS |
| `nametags` | NameTags | Visual | WORLD_SPACE | (0, 0) 80x20 | CENTER | VIS, SCALE |
| `tnt_countdown` | TNT Countdown | PvP | WORLD_SPACE | (0, 0) 60x18 | CENTER | VIS |
| `teamview` | TeamView | PvP | WORLD_SPACE | (0, 0) 70x18 | CENTER | VIS |
| `chat` | Chat Box | Visual | GUI_OVERLAY | (10, 20) 180x80 | BOTTOM_LEFT | POS, SCALE, VIS, ANCHOR, SIZE |
| `tab_editor` | Tablist | Visual | GUI_OVERLAY | (0, 15) 160x90 | TOP_CENTER | SCALE, VIS, SIZE |
| `titles` | Screen Titles | Visual | GUI_OVERLAY | (0, 50) 140x35 | TOP_CENTER | SCALE, VIS |

---

## 5. Integracja z Podsystemami Etapu 9C

Wszystkie zarejestrowane komponenty współpracują bezpośrednio z podsystemami wprowadzonymi w Etapie 9C:
- **Presety (`HudPresetManager`):** Domyślne wartości współrzędnych i wymiarów z deskryptorów stanowią bazę dla szablonu `default`, a pozostałe szablony (PvP, Survival, UHC itp.) rekonfigurują je deklaratywnie.
- **Wielokrotne Zaznaczanie (`HudMultiSelectionManager`):** Wszystkie komponenty o typie `SCREEN_SPACE` mogą być wspólnie zaznaczane ramką (Marquee) i przemieszczane (Multi-Drag).
- **Grupy i Warstwy:** Komponenty mogą być przypisywane do warstw logicznych (`HudLayer`) oraz łączone w grupy (`HudGroup`).
- **Walidacja Responsywna (`HudResponsiveLayoutManager`):** Deskryptory definiują minimalne i maksymalne bezpieczne wymiary wykorzystywane przy sprawdzaniu poprawności układu na małych ekranach.

