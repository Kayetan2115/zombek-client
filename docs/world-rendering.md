# Architektura Renderowania Świata 3D (World Rendering) — ZombekClient

## 1. Wstęp i Założenia

Podczas gdy elementy HUD (Keystrokes, CPS, Armor Status, Combo Counter, Cooldowns, Crosshair) operują w dwuwymiarowej przestrzeni ekranu (określanej przez `IHudRenderer` i `GuiGraphics` / `DrawContext`), moduły takie jak **Hitbox** wymagają rysowania geometrii bezpośrednio w trójwymiarowej przestrzeni świata gry Minecraft.

Wspólna architektura ZombekClient Core zachowuje pełną niezależność od silników graficznych poszczególnych loaderów, operując na abstrakcji `IHitboxRenderer` oraz matematycznym module `HitboxGeometryCalculator`.

---

## 2. Podpięcia do Pętli Renderowania Świata (World Render Hooks)

W zależności od loadera punkt wstrzyknięcia do potoku renderowania 3D został zrealizowany w fazie następującej po wyrenderowaniu bytów:

| Loader | Mechanizm Podpięcia | Zdarzenie / Punkt wstrzyknięcia |
| :--- | :--- | :--- |
| **Fabric** | Fabric API Rendering | `WorldRenderEvents.AFTER_ENTITIES` |
| **Quilt** | Mixin do `WorldRenderer` | `@Inject(method = "render", at = @At("TAIL"))` |
| **Forge** | MinecraftForge Event Bus | `RenderLevelStageEvent` (`Stage.AFTER_ENTITIES`) |
| **NeoForge**| NeoForge Event Bus | `RenderLevelStageEvent` (`Stage.AFTER_ENTITIES`) |

---

## 3. Matematyka Transformacji Kamery i Buforowania

1. **Współrzędne Relatywne do Kamery**:
   Minecraft 1.20+ korzysta z pływającej kamery, gdzie wierzchołki geometrii poziomu są definiowane względem pozycji kamery (`Camera.getPosition()`).
   Współrzędne wierzchołków bounding boxa:
   $$x_{rel} = x_{world} - camX$$
   $$y_{rel} = y_{world} - camY$$
   $$z_{rel} = z_{world} - camZ$$
   Dzięki temu unika się błędów precyzji zmiennoprzecinkowej (`jittering`) przy dużych odległościach od zera świata.

2. **Warstwy Renderowania (Render Types)**:
   - **Krawędzie i wektory**: Renderowane za pomocą bufora `RenderType.lines()` / `RenderLayer.getLines()`. Zapewniają stałą grubość linii bez artefaktów migotania.
   - **Wypełnienia ścian**: Renderowane za pomocą bufora `RenderType.debugQuads()` / `RenderLayer.getDebugQuads()` z przezroczystością alfa.

3. **Gwarancja Bufora Głębokości (Depth Test)**:
   Wszystkie używane bufory wierzchołków podlegają testowi bufora głębokości karty graficznej. Jeśli między kamerą a danym bytem znajduje się blok, piksele hitboxa zostają odrzucone na poziomie rasteryzacji GPU, zapobiegając nieuczciwej przewadze (brak wallhacka).

---

## 4. Optymalizacja i Zarządzanie Wydajnością

Aby zapobiec spadkom klatek (FPS drop) w gęsto zaludnionych obszarach (np. lobby serwerowe, farmy mobów):
1. **Ograniczenie Dystansu (`max_distance`)**: Byt oddalony dalej niż skonfigurowany promień (domyślnie 32 bloki) jest odrzucany przed obliczeniami geometrycznymi.
2. **Budżet Bytów (`max_entities`)**: Twardy limit liczby renderowanych hitboxów na klatkę (domyślnie 64).
3. **Pominięcie Gracza w FPP**: Eliminacja niepotrzebnego rysowania własnego hitboxa w trybie pierwszej osoby.
