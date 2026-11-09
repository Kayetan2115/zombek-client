# Tab Editor Module

## 1. Przegląd

Moduł **Tab Editor** (`"tab_editor"`) w kategorii `ModuleCategory.VISUAL` umożliwia pełną personalizację listy graczy wyświetlanej po naciśnięciu i przytrzymaniu klawisza TAB w Minecraft.

Zastępuje waniliowy komponent `PlayerListHud` / overlay nowoczesnym, wysoce konfigurowalnym panelem siatkowym z obsługą animacji, wyróżnianiem znajomych, formatowaniem opóźnienia ping oraz nagłówków i stopek serwerowych.

## 2. Grupy Ustawień

Moduł posiada 12 grup konfiguracyjnych w Settings Framework:

1. **`general`**:
   - `use_custom_tab` (boolean, domyślnie `true`)
   - `layout_preset` (enum: `MODERN`, `VANILLA`, `MINIMAL`, `CARD`, `COLUMNS`, `CLASSIC`, `CUSTOM`)

2. **`header`**:
   - `show_header` (boolean, domyślnie `true`)
   - `header_mode` (enum: `ORIGINAL`, `CUSTOM`, `HIDDEN`)
   - `custom_header` (string, domyślnie `"§6§lZombekClient Server"`)
   - `hide_server_header` (boolean, domyślnie `false`)
   - `show_server_ip` (boolean, domyślnie `true`)
   - `show_player_count` (boolean, domyślnie `true`)
   - `show_ping_summary` (boolean, domyślnie `false`)

3. **`footer`**:
   - `show_footer` (boolean, domyślnie `true`)
   - `footer_mode` (enum: `ORIGINAL`, `CUSTOM`, `HIDDEN`)
   - `custom_footer` (string, domyślnie `"§ePowered by §bZombekClient"`)
   - `hide_server_footer` (boolean, domyślnie `false`)
   - `show_client_brand` (boolean, domyślnie `true`)
   - `show_time` (boolean, domyślnie `false`)

4. **`player_entry`**:
   - `show_avatar` (boolean, domyślnie `true`)
   - `avatar_size` (number: 8–16 px, domyślnie 8)
   - `show_prefix` (boolean, domyślnie `true`)
   - `show_name` (boolean, domyślnie `true`)
   - `show_suffix` (boolean, domyślnie `true`)
   - `show_team` (boolean, domyślnie `true`)
   - `show_ping` (boolean, domyślnie `true`)
   - `show_gamemode` (boolean, domyślnie `false`)
   - `show_score` (boolean, domyślnie `false`)

5. **`appearance`**:
   - `background_color` (color, domyślnie `0x000000`)
   - `background_opacity` (number: 0.0–1.0, domyślnie 0.6)
   - `show_border` (boolean, domyślnie `false`)
   - `border_color` (color, domyślnie `0x40FFFFFF`)
   - `border_thickness` (number: 1–4)
   - `corner_radius` (number: 0–10)
   - `header_color` / `footer_color` / `name_color` (color)
   - `shadow` (boolean, domyślnie `true`)
   - `font_scale` (number: 0.5–2.0, domyślnie 1.0)

6. **`grid`**:
   - `max_columns` (number: 1–6, domyślnie 4)
   - `max_rows` (number: 5–40, domyślnie 20)
   - `column_width` (number: 60–250 px, domyślnie 120)
   - `row_height` (number: 10–24 px, domyślnie 12)
   - `h_spacing` / `v_spacing` (number: 1–10 px)
   - `adaptive_columns` (boolean, domyślnie `true`)
   - `adaptive_width` (boolean, domyślnie `true`)

7. **`sorting`**:
   - `sorting_mode` (enum: `VANILLA_ORDER`, `ALPHABETICAL`, `TEAM`, `FRIENDS_FIRST`, `CUSTOM_ORDER`)
   - `reverse_sort` (boolean)
   - `friends_first` (boolean)
   - `team_grouping` (boolean)
   - `team_headers` (boolean)

8. **`ping`**:
   - `ping_mode` (enum: `NUMERIC`, `BARS`, `COLORED_TEXT`, `MINIMAL_ICON`)
   - `show_numeric_ping` (boolean)
   - `ping_colored` (boolean)
   - `good_color` / `medium_color` / `high_color` / `unknown_color`
   - `threshold_green` (domyślnie 60 ms) / `threshold_yellow` (150 ms) / `threshold_red` (300 ms)

9. **`friends`**:
   - `enable_friend_highlight` (boolean, domyślnie `true`)
   - `friend_color` (color, domyślnie `0xFF55FF55`)
   - `friend_icon` (string, domyślnie `"★"`)
   - `show_friend_icon` (boolean, domyślnie `true`)
   - `friend_at_top` (boolean, domyślnie `false`)
   - `use_friend_alias` (boolean, domyślnie `true`)

10. **`animations`**:
    - `fade_in` (boolean, domyślnie `true`)
    - `animation_duration` (number: 0–500 ms, domyślnie 150)
    - `enable_slide` (boolean, domyślnie `false`)
    - `smooth_resize` (boolean, domyślnie `true`)

11. **`privacy`**:
    - `hide_self` (boolean, domyślnie `false`)
    - `hide_spectators` (boolean, domyślnie `false`)
    - `anonymize_names` (boolean, domyślnie `false`)

12. **`position`**:
    - `vertical_offset` (number: 0–200 px, domyślnie 10)
    - `center_horizontally` (boolean, domyślnie `true`)

## 3. Integracja z Loaderami

- **Fabric & Quilt:** Przechwycenie `PlayerListHud.render` w mixinie `PlayerListHudMixin`. Jeśli `TabEditorModule.shouldOverrideVanilla()` zwraca `true`, vanilla render zostaje anulowany i wywołany zostaje `CustomTabRenderer.renderTabList(...)`.
- **Forge & NeoForge:** Rejestracja w `RenderGuiOverlayEvent.Pre`. Wykrycie `VanillaGuiOverlay.PLAYER_LIST` i anulowanie eventu. Wywołanie `CustomTabRenderer.renderTabList(...)` następuje w warstwie GUI overlay gdy naciśnięty jest klawisz `keyPlayerList`.
