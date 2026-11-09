# Title Renderer

Dokumentacja silnika renderowania tytułów (Title) i podtytułów (Subtitle) w **ZombekClient** (`CustomTitleRenderer`).

## 1. Architektura Renderera

Silnik `CustomTitleRenderer` obsługuje renderowanie napisów Title oraz Subtitle w oparciu o wspólny interfejs `IHudRenderer`:
- Centralne pozycjonowanie na ekranie lub konfigurowalny offset Y (`title_pos_y`).
- Niezależne skalowanie czcionki dla tytułu głównego (`title_scale`) i podtytułu (`subtitle_scale`).
- Cienie tekstu oraz zaokrąglona karta tła (Background Card).

## 2. Style Animacji (Animation Styles)

Silnik wspiera następujące profile animacji wejścia/wyjścia:
- `FADE`: Klasyczna interpolacja przezroczystości (Alpha 0.0 -> 1.0 -> 0.0).
- `SLIDE`: Płynne wysuwanie z góry/dołu z krzywą ease-out.
- `SCALE`: Dynamiczne powiększanie (Zoom-in) od 0.5x do docelowej skali z tłumieniem.
- `STATIC`: Natychmiastowe pojawienie się bez przejść przezroczystości.
- `CUSTOM`: Konfigurowalne czasy klatek i krzywe przejścia.

## 3. Integracja z Loaderami

- **Fabric / Quilt**: `InGameHudMixin` przechwytuje pakiety tytułów (`setTitle`, `setSubtitle`, `setTitleTicks`), zeruje `titleRemainTicks` vanilla i renderuje `CustomTitleRenderer.getInstance().renderTitles` na końcu renderowania HUD.
- **Forge / NeoForge**: `RenderGuiOverlayEvent.Pre` anuluje `VanillaGuiOverlay.TITLE_TEXT`, a `ForgeTitleSynchronizer` / `NeoForgeTitleSynchronizer` przekazuje stan do `CustomTitleRenderer`.
