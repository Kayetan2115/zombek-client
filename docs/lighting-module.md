# Moduł Oświetlenia (LightingModule)

## 1. Wprowadzenie i Cel
Moduł `LightingModule` wchodzi w skład pakietu efektów wizualnych świata ZombekClient (Etap 8C2). Umożliwia dynamiczne, realistyczne oświetlenie otoczenia gracza w czasie rzeczywistym bazując na przedmiotach trzymanych w dłoniach, leżących przedmiotach na ziemi (dropped items) oraz świecących lub płonących encjach.

Wszystkie obliczenia oświetlenia wykonywane są w 100% po stronie klienta (Client-Side Only). Moduł nie modyfikuje rzeczywistego poziomu światła bloków na serwerze (`BlockState.lightLevel`), nie wpływa na mechanikę spawnowania potworów, wzrost upraw ani na pakiety sieciowe.

## 2. Grupy Ustawień i Parametry

### 2.1. Ogólne (General)
- `main_hand_light` (Domyślnie: `true`) — emitowanie światła przez przedmioty trzymane w ręce głównej (np. pochodnia, latarnia, lawa).
- `offhand_light` (Domyślnie: `true`) — dynamiczne światło dla przedmiotu w drugiej ręce.
- `dropped_item_light` (Domyślnie: `true`) — oświetlenie wokół upuszczonych przedmiotów emitujących światło.
- `entity_light` (Domyślnie: `true`) — emisja światła przez świecące encje (Blaze, Magma Cube, Glow Squid, płonące moby).
- `custom_light_sources` (Domyślnie: `true`) — obsługa niestandardowych reguł oświetlenia zdefiniowanych przez użytkownika.

### 2.2. Intensywność (Intensity)
- `light_intensity` (0.1 - 2.0, Domyślnie: `1.0`) — mnożnik jasności generowanego światła.
- `max_light_level` (1 - 15, Domyślnie: `15`) — maksymalny dopuszczalny poziom światła w centrum źródła.
- `source_strength` (0.5 - 2.0, Domyślnie: `1.0`) — siła rozpraszania promieni światła.
- `light_falloff` (0.5 - 3.0, Domyślnie: `1.0`) — wykładnik zanikania światła wraz z odległością.

### 2.3. Zasięg i Wydajność (Distance & Capacity)
- `max_light_distance` (4.0 - 64.0 m, Domyślnie: `24.0 m`) — maksymalna odległość renderowania dynamicznych źródeł od kamery gracza.
- `max_active_sources` (1 - 64, Domyślnie: `16`) — limit jednocześnie aktywnych dynamicznych źródeł światła (zabezpieczenie FPS).
- `update_interval_ms` (10 - 200 ms, Domyślnie: `30 ms`) — minimalny interwał czasowy między aktualizacjami źródeł światła (throttling).

### 2.4. Wygląd i Kolorystyka (Appearance)
- `preserve_vanilla_lighting` (Domyślnie: `false`) — tryb ścisłego zachowania waniliowej charakterystyki światła.
- `light_color_mode` (Domyślnie: `NATURAL`) — wybór palety barwnej światła:
  - `NATURAL` — waniliowe, ciepłe światło pochodni i zimne latarni dusz.
  - `WARM` — nasycone, przytulne ciepłe tony bursztynowo-złote.
  - `COOL` — chłodne, błękitnawo-srebrzyste światło.
  - `CUSTOM` — pełna personalizacja koloru HEX/ARGB.
- `custom_light_color` (Domyślnie: `0xFFFFD700`) — niestandardowy kolor poświaty światła.
- `light_flicker` (Domyślnie: `false`) — nastrojowe, delikatne migotanie płomienia pochodni.
- `flicker_strength` (0.0 - 1.0, Domyślnie: `0.15`) — amplituda wahań jasności migotania.

### 2.5. Blokady Widoczności (Visibility Guards)
- `disable_in_gui` (Domyślnie: `true`) — wyłączenie renderowania światła przy otwartym ekwipunku/GUI.
- `disable_in_menus` (Domyślnie: `true`) — wyłączenie w menu pauzy/głównym.
- `disable_in_spectator` (Domyślnie: `true`) — wyłączenie w trybie obserwatora.
- `disable_in_hud_editor` (Domyślnie: `true`) — wyłączenie podczas pracy w edytorze HUD.

## 3. Integracja z DynamicLightManager
Po włączeniu modułu (`onEnable()`), `LightingModule` przekazuje wszystkie parametry konfiguracyjne do instancji `DynamicLightManager.getInstance()`. Przy wyłączeniu (`onDisable()`) wywoływane jest `DynamicLightManager.getInstance().clear()`, co natychmiastowo usuwa wszelkie aktywne źródła światła i przywraca waniliowy stan renderowania.
