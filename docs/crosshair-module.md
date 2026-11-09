# Moduł Crosshair (Celownik) — ZombekClient

## 1. Przegląd i Cel

Moduł **Crosshair** (`crosshair`) to moduł wizualny / PvP klasy Lunar Client, zastępujący standardowy celownik Minecrafta proceduralnie generowaną geometrią 2D o wysokiej precyzji renderowania. Oferuje 8 zróżnicowanych stylów celownika, precyzyjne ustawienia wymiarów, dynamiczne reagowanie na cel pod kursorem (bloki, byty z rozróżnieniem klasyfikacji) oraz wskaźnik cooldownu ataku z płynnymi przejściami kolorystycznymi.

---

## 2. Architektura i Komponenty

Moduł zlokalizowany jest we wspólnym rdzeniu (`apps/minecraft-client/core/`):

- **[`CrosshairModule`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/CrosshairModule.java)**: Główna klasa modułu dziedzicząca po `AbstractHudModule`. Zarządza 5 grupami ustawień (`general`, `size`, `colors`, `dynamic`, `advanced`), implementuje `render(IHudRenderer)` oraz decyduje o tłumieniu celownika vanilla (`shouldHideVanillaCrosshair()`).
- **[`CrosshairRenderer`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/CrosshairRenderer.java)**: Silnik renderowania geometrycznego wykonujący rysowanie linii, obrysów, punktów, okręgów i kwadratów na interfejsie `IHudRenderer`.
- **[`CrosshairStyle`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/CrosshairStyle.java)**: Enum 8 stylów:
  1. `CLASSIC_CROSS` (klasyczny krzyżyk z centralną przerwą)
  2. `SIMPLE_CROSS` (ciągły krzyżyk bez przerwy)
  3. `DOT` (pojedynczy precyzyjny punkt)
  4. `CIRCLE` (okrągły celownik z konfigurowalnym promieniem)
  5. `SQUARE` (ramka kwadratowa)
  6. `T_SHAPE` (celownik taktyczny w kształcie litery T, bez górnego ramienia)
  7. `DYNAMIC_CROSS` (krzyżyk reagujący rozszerzeniem przerwy w zależności od cooldownu ataku)
  8. `CUSTOM` (pełna swoboda konfiguracji parametrów)
- **[`CrosshairColorResolver`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/CrosshairColorResolver.java)**: Oblicza docelowy kolor celownika na podstawie stanu (normalny, najechanie na wrogi byt, gracza, byt neutralny/pasywny, blok) z obsługą płynnej interpolacji kanałów ARGB.
- **[`ICrosshairTargetProvider`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/data/ICrosshairTargetProvider.java)** i **[`CrosshairTargetRegistry`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/data/CrosshairTargetRegistry.java)**: Abstrakcja dostarczająca informacje o celu celownika (`CrosshairTargetType.NONE`, `BLOCK`, `ENTITY`), stanie cooldownu ataku oraz trybach widza i ukrycia GUI bez wiązania Core z klasami konkretnego loadera.

---

## 3. Rozwiązanie Tłumienia Celownika Vanilla

Celownik Vanilla nie jest trwale wyłączany w opcjach gry, co zapobiega problemom w razie awarii lub wyłączenia modułu:

1. **Warunek `shouldHideVanillaCrosshair()`**:
   Zwraca `true` tylko wtedy, gdy:
   - Moduł `crosshair` jest włączony (`isEnabled() == true`).
   - Ustawienie `hide_vanilla` ma wartość `true`.
   - Gracz nie jest widzem ze specjalnym celownikiem (chyba że zezwala na to konfiguracja).
   - Menu i otwarte ekrany nie blokują renderowania celownika.
   - Opcja F1 (`hideGui`) nie jest aktywna.

2. **Integracja z loaderami**:
   - **Fabric & Quilt**: Wstrzyknięcie Mixin w klasę `InGameHud.renderCrosshair` (`@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)`). Jeżeli warunek zachodzi, wywołanie `ci.cancel()` zapobiega narysowaniu celownika vanilla.
   - **Forge**: Obsługa zdarzenia `RenderGuiOverlayEvent.Pre`. Sprawdzenie `VanillaGuiOverlay.CROSSHAIR.id().equals(event.getOverlay().id())` i wywołanie `event.setCanceled(true)`.
   - **NeoForge**: Obsługa zdarzenia `RenderGuiOverlayEvent.Pre`. Sprawdzenie identyfikatora warstwy `VanillaGuiOverlay.CROSSHAIR.id()` i wywołanie `event.setCanceled(true)`.

---

## 4. Grupy Ustawień

| Grupa | Klucz | Typ | Opis | Domyślna wartość |
| :--- | :--- | :--- | :--- | :--- |
| **General** | `style` | Enum (`CrosshairStyle`) | Styl wizualny celownika | `CLASSIC_CROSS` |
| **General** | `hide_vanilla` | Boolean | Ukrywanie celownika domyślnego | `true` |
| **General** | `show_dot` | Boolean | Punkt w centrum celownika | `true` |
| **Size** | `length` | Number (1.0 - 50.0) | Długość ramion | `4.0` |
| **Size** | `thickness` | Number (1.0 - 10.0) | Grubość linii | `1.0` |
| **Size** | `gap` | Number (0.0 - 30.0) | Rozmiar przerwy centralnej | `3.0` |
| **Size** | `dot_size` | Number (1.0 - 8.0) | Średnica punktu centralnego | `1.0` |
| **Size** | `outline_width` | Number (0.5 - 4.0) | Grubość obrysu | `1.0` |
| **Colors** | `color` | Color | Główny kolor celownika | `#FFFFFF` |
| **Colors** | `outline_color` | Color | Kolor obrysu | `#000000` |
| **Colors** | `dot_color` | Color | Kolor punktu środkowego | `#FFFFFF` |
| **Colors** | `outline` | Boolean | Włączenie obrysu | `true` |
| **Dynamic** | `dynamic_color` | Boolean | Zmiana koloru w zależności od celu | `true` |
| **Dynamic** | `enemy_color` | Color | Kolor po najechaniu na wroga/gracza | `#FF3333` |
| **Dynamic** | `passive_color` | Color | Kolor po najechaniu na zwierzę | `#33FF33` |
| **Dynamic** | `block_color` | Color | Kolor po najechaniu na interaktywny blok | `#3388FF` |
| **Dynamic** | `attack_cooldown` | Boolean | Wskaźnik cooldownu broni | `false` |
| **Advanced** | `smooth_transition` | Boolean | Płynne przenikanie barw | `true` |
| **Advanced** | `background_shade` | Boolean | Cieniowane tło wokół celownika | `false` |
