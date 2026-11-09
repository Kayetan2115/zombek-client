# NameTags Module

## 1. Przegląd

Moduł **NameTags** (`"nametags"`) wchodzi w skład kategorii `ModuleCategory.VISUAL` w ZombekClient. Umożliwia lokalną personalizację i wzbogacenie wyglądu oraz informacji renderowanych w etykietach (3D NameTags) nad postaciami graczy oraz innymi encjami w świecie gry.

## 2. Podstawowe Założenia i Fair Play

Moduł został zaprojektowany z bezwzględnym poszanowaniem zasad **Fair Play** oraz waniliowych mechanik widoczności Minecraft:
- **Brak Wallhacka / ESP:** NameTagi nie są renderowane przez ściany wbrew waniliowym regułom.
- **Obsługa skradania (Sneaking):** Kiedy gracz się skrada (`isSneaking()`), jego NameTag zostaje ukryty lub przyciemniony ściśle według waniliowych zasad.
- **Niewidzialność (Invisibility):** Encje z nałożonym efektem niewidzialności nie mają ujawnianych etykiet.
- **Tryb Obserwatora (Spectator):** Gracze w trybie widza są traktowani zgodnie z waniliowym protokołem.
- **Dystans renderowania:** Ograniczony do waniliowego limitu (domyślnie 64 bloki) bez możliwości wymuszania widoczności poza zasięgiem wzroku.

## 3. Grupy Ustawień

Moduł posiada 9 dedykowanych grup konfiguracyjnych w Settings Framework:

1. **`general`**:
   - `target_players` (boolean, domyślnie `true`): personalizacja NameTagów graczy.
   - `target_living` (boolean, domyślnie `true`): personalizacja NameTagów stworzeń (moby z nametagiem, zwierzęta).
   - `target_other` (boolean, domyślnie `false`): personalizacja innych nazwanych encji.
   - `use_custom_renderer` (boolean, domyślnie `true`): zastąpienie domyślnego renderera.
   - `preserve_formatting` (boolean, domyślnie `true`): zachowanie kodów kolorów formatowania serwera.

2. **`player_info`**:
   - `show_name` (boolean, domyślnie `true`)
   - `show_health` (boolean, domyślnie `true`)
   - `show_absorption` (boolean, domyślnie `true`)
   - `show_distance` (boolean, domyślnie `false`)
   - `show_team` (boolean, domyślnie `true`)
   - `show_ping` (boolean, domyślnie `false`)
   - `show_friend_marker` (boolean, domyślnie `true`)
   - `show_armor` (boolean, domyślnie `false`)
   - `show_held_item` (boolean, domyślnie `false`)

3. **`custom_name`**:
   - `enable_alias` (boolean, domyślnie `true`): wyświetla lokalny alias znajomego.
   - `show_custom_prefix` / `custom_prefix` (string)
   - `show_custom_suffix` / `custom_suffix` (string)
   - `friend_marker_icon` (string, domyślnie `"★"`)

4. **`health`**:
   - `health_style` (enum: `NUMERIC`, `HEARTS`, `BAR`, `COMPACT`, `CUSTOM`)
   - `health_color` (color, domyślnie zielony `0xFF55FF55`)
   - `low_health_color` (color, domyślnie czerwony `0xFFFF5555`)
   - `absorption_color` (color, domyślnie złoty `0xFFFFAA00`)
   - `max_health_display` (boolean)
   - `decimal_precision` (number: 0–2)
   - `smooth_transition` (boolean)

5. **`distance`**:
   - `distance_format` (enum: `BLOCKS`, `DECIMAL`, `ROUNDED`)
   - `max_display_distance` (number: 16–128 bloków, domyślnie 64)
   - `min_display_distance` (number: 0–16 bloków, domyślnie 0)

6. **`armor`**:
   - `show_durability` (boolean)
   - `show_armor_icons` (boolean)
   - `low_durability_warning` (boolean)

7. **`appearance`**:
   - `layout` (enum: `VANILLA`, `MINIMAL`, `MODERN`, `DETAILED`, `COMPACT`, `CUSTOM`)
   - `scale` / `scale_multiplier` (number: 0.5–2.0, domyślnie 1.0)
   - `dynamic_scale` / `constant_scale` (boolean)
   - `background_color` / `background_opacity` (color / number 0.0–1.0)
   - `show_border` / `border_color` / `border_thickness` / `corner_radius`
   - `shadow` (boolean, domyślnie `true`)
   - `spacing` (number)

8. **`visibility`**:
   - `respect_vanilla_visibility` (boolean, domyślnie `true`)
   - `max_render_distance` (number, domyślnie 64.0)
   - `hide_in_gui` (boolean, domyślnie `true`)
   - `hide_in_spectator` (boolean, domyślnie `true`)
   - `hide_invisible` (boolean, domyślnie `true`)
   - `hide_sneaking` (boolean, domyślnie `true`)

9. **`position`**:
   - `vertical_offset` (number, domyślnie 0.0)
   - `horizontal_offset` (number, domyślnie 0.0)
   - `nametag_height` (number, domyślnie 0.0)

## 4. Architektura Integracji z Loaderami

- **Fabric & Quilt:** Przechwycenie `EntityRenderer.renderLabelIfPresent` poprzez `@Mixin(EntityRenderer.class)`. Ewaluacja widoczności za pomocą `NameTagVisibilityRuleEvaluator.shouldRender(...)`. Anulowanie vanilla labelu i renderowanie spersonalizowanego widoku 3D lub delegowanie do formatted text.
- **Forge & NeoForge:** Rejestracja listenera `RenderNameTagEvent`. W przypadku negatywnej ewaluacji widoczności Fair Play event ustawia rezultat na `DENY`. W przypadku dopuszczenia modyfikuje treść etykiety za pomocą `event.setContent(...)`.
