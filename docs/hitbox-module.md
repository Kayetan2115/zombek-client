# Moduł Hitbox (Wizualizacja Bounding Boxów Bytów) — ZombekClient

## 1. Przegląd i Cel

Moduł **Hitbox** (`hitbox`) służy do precyzyjnej wizualizacji rzeczywistych ramek kolizji i trafień (bounding boxów) otaczających byty w świecie 3D gry Minecraft. Zapewnia graczom PvP i twórcom mechanik pełną przejrzystość geometrii modeli bez uciekania się do narzędzi typu cheat/wallhack.

Moduł:
- **Ściśle respektuje bufor głębokości (Depth Test)** — linie i wypełnienia są zasłaniane przez bloki i ściany. Nie pozwala na prześwietlanie terenu ani wykrywanie graczy zza przeszkód.
- Oferuje 4 style renderowania oraz konfigurowalne kolory w zależności od typu bytu.
- Wizualizuje poziom oczu (Eye Level) i wektor kierunku patrzenia (Look Vector).

---

## 2. Architektura i Komponenty

- **[`HitboxModule`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/HitboxModule.java)**: Moduł wizualny zarządzający 6 grupami ustawień (`general`, `appearance`, `colors`, `filters`, `information`, `advanced`).
- **[`HitboxGeometryCalculator`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/HitboxGeometryCalculator.java)**: Niezależny od środowiska kalkulator wierzchołków geometrii 3D. Oblicza 12 krawędzi pudełka, 6 ścianek, linie poziomu wzroku oraz wektor spojrzenia, transponując współrzędne absolutne świata na współrzędne relatywne kamery (`camX, camY, camZ`).
- **[`HitboxEntityData`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/data/HitboxEntityData.java)**: Czysty obiekt DTO przekazujący stan bytu (ID, nazwa, typ, bounding box, eyeY, wektor patrzenia, odległość, celowanie, niewidzialność) do Core.
- **[`IHitboxRenderer`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/data/IHitboxRenderer.java)**: Abstrakcyjny interfejs renderowania linii i ścianek (`drawLine`, `drawQuad`), implementowany przez dedykowane klasy dla każdego z czterech loaderów:
  - `FabricHitboxRenderer`
  - `QuiltHitboxRenderer`
  - `ForgeHitboxRenderer`
  - `NeoForgeHitboxRenderer`

---

## 3. Style Renderowania (`HitboxRenderStyle`)

1. **`OUTLINE`**: 12 krawędzi prostopadłościanu o zadanej grubości linii.
2. **`FILLED`**: Półprzezroczyste czwórokątne ścianki (6 ścian) o konfigurowalnym kolorze i przezroczystości.
3. **`OUTLINE_AND_FILLED`**: Połączenie wyraźnego obrysu krawędzi z wypełnieniem wnętrza ramki.
4. **`WIREFRAME`**: Pełna siatka druciana zawierająca krawędzie oraz linie przekątnych na każdej ze ścian prostopadłościanu.

---

## 4. Ochrona Przed Oszustwami i Depth Testing

Kluczowym założeniem modułu jest zgodność z zasadami fair-play:
- Renderowanie podpięte jest w fazie `AFTER_ENTITIES` (po wyrenderowaniu geometrii świata i bytów).
- Używane bufory wierzchołków (`RenderType.lines()`, `RenderType.debugQuads()`, `RenderLayer.getLines()`) korzystają ze standardowego stanu bufora głębokości OpenGL (`GL_DEPTH_TEST`).
- Geometria hitboxa bytu znajdującego się za ścianą jest w pełni odrzucana przez test głębokości karty graficznej.
- W trybie pierwszej osoby hitbox własnego gracza jest celowo pomijany (`if (entity == mc.player && firstPerson) continue;`), aby nie zasłaniać pola widzenia.

---

## 5. Grupy Ustawień

| Grupa | Klucz | Typ | Opis | Domyślna wartość |
| :--- | :--- | :--- | :--- | :--- |
| **General** | `render_style` | Enum (`HitboxRenderStyle`) | Styl wizualny hitboxa | `OUTLINE` |
| **General** | `max_distance` | Number (4 - 128) | Maksymalny zasięg renderowania (bloki) | `32.0` |
| **Appearance** | `line_width` | Number (1 - 5) | Grubość linii obrysu | `2.0` |
| **Appearance** | `fill_alpha` | Number (0.05 - 0.8) | Przezroczystość wypełnienia | `0.2` |
| **Colors** | `player_color` | Color | Kolor obrysu graczy | `#FFFFFF` |
| **Colors** | `hostile_color` | Color | Kolor obrysu wrogich mobów | `#FF3333` |
| **Colors** | `passive_color` | Color | Kolor obrysu zwierząt | `#33FF33` |
| **Colors** | `targeted_color` | Color | Kolor aktualnie celowanego bytu | `#FFAA00` |
| **Filters** | `render_players` | Boolean | Wyświetlanie graczy | `true` |
| **Filters** | `render_hostiles` | Boolean | Wyświetlanie wrogów | `true` |
| **Filters** | `render_passives` | Boolean | Wyświetlanie zwierząt | `false` |
| **Filters** | `render_projectiles`| Boolean | Wyświetlanie pocisków (strzały, perły) | `false` |
| **Information** | `show_eye_position`| Boolean | Wyświetlanie linii poziomu wzroku | `true` |
| **Information** | `show_look_direction`| Boolean | Wektor kierunku patrzenia | `true` |
| **Advanced** | `max_entities` | Number (8 - 256) | Limit przetwarzanych bytów na klatkę | `64.0` |
