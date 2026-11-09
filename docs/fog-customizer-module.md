# Moduł Fog Customizer (Etap 8C3)

## 1. Przegląd i przeznaczenie

Moduł **Fog Customizer** (`FogCustomizerModule`) jest zaawansowanym systemem renderowania mgły w ZombekClient. Umożliwia graczowi precyzyjną, lokalną kontrolę nad gęstością, dystansem, krzywą zaniku oraz kolorami mgły w różnych środowiskach i wymiarach Minecraft 1.20.4.

Wszystkie modyfikacje są realizowane w 100% po stronie klienta i nie naruszają mechanik Fair Play:
- Mgła nigdy nie przekracza fizycznego zasięgu renderowania chunków (`viewDistance`).
- Nie ujawnia niezaładowanych chunków (brak wallhack / X-ray).
- Nie wysyła żadnych dodatkowych pakietów sieciowych na serwer.

## 2. Grupy ustawień modułu

| Grupa | Klucz | Typ | Domyślnie | Zakres / Opcje | Opis |
|---|---|---|---|---|---|
| **General** | `preset` | `EnumSetting<FogPreset>` | `VANILLA` | `VANILLA`, `REDUCED_FOG`, `CLEAR_VIEW`, `DENSE_FOG`, `SOFT_FOG`, `CUSTOM` | Profil konfiguracji mgły |
| | `fog_strength` | `NumberSetting` | `1.0` | `0.1` - `3.0` | Mnożnik intensywności rozmycia |
| | `fog_density` | `NumberSetting` | `1.0` | `0.0` - `2.0` | Gęstość zamglenia |
| | `fog_falloff` | `NumberSetting` | `1.0` | `0.2` - `3.0` | Wykładnik krzywej zaniku krawędzi |
| **Distance** | `fog_start` | `NumberSetting` | `16.0 m` | `0.0` - `512.0 m` | Dystans startu mgły od kamery |
| | `fog_end` | `NumberSetting` | `128.0 m` | `4.0` - `1024.0 m` | Dystans pełnego nasycenia mgłą |
| | `min_fog_distance` | `NumberSetting` | `0.0 m` | `0.0` - `128.0 m` | Dolny bufor odcięcia |
| | `max_fog_distance` | `NumberSetting` | `512.0 m` | `32.0` - `1024.0 m` | Górny bezpieczny bufor odcięcia |
| **Color** | `use_custom_color` | `BooleanSetting` | `false` | `true` / `false` | Przełącznik własnej barwy mgły |
| | `custom_color` | `ColorSetting` | `0xFFC0D8FF` | `0x00000000` - `0xFFFFFFFF` | Kolor ARGB mgły |
| | `color_blend` | `NumberSetting` | `1.0` | `0.0` - `1.0` | Współczynnik mieszania z kolorem waniliowym |
| **Environments** | `air_fog` | `BooleanSetting` | `true` | `true` / `false` | Aktywność w powietrzu |
| | `water_fog` | `BooleanSetting` | `true` | `true` / `false` | Aktywność w wodzie |
| | `lava_fog` | `BooleanSetting` | `true` | `true` / `false` | Aktywność w lawie |
| | `powder_snow_fog` | `BooleanSetting` | `true` | `true` / `false` | Aktywność w sypkim śniegu |
| | `dimension_fog` | `BooleanSetting` | `true` | `true` / `false` | Aktywność w warunkach wymiarowych |
| | `biome_fog` | `BooleanSetting` | `true` | `true` / `false` | Uwzględnianie barwy biomu |
| | `boss_fog` | `BooleanSetting` | `true` | `true` / `false` | Uwzględnianie mgły walki z bossami |
| **Animation** | `smooth_transition` | `BooleanSetting` | `true` | `true` / `false` | Płynna interpolacja dystansu i barw |
| | `transition_duration_ms` | `NumberSetting` | `400 ms` | `50` - `3000 ms` | Czas trwania animacji przejścia |
| **Dimensions** | `enable_in_overworld` | `BooleanSetting` | `true` | `true` / `false` | Działanie w Overworld |
| | `enable_in_nether` | `BooleanSetting` | `true` | `true` / `false` | Działanie w Netherze |
| | `enable_in_end` | `BooleanSetting` | `true` | `true` / `false` | Działanie w The End |
| | `enable_in_custom_dimensions` | `BooleanSetting` | `true` | `true` / `false` | Działanie w modyfikowanych wymiarach |
| **Behavior** | `reset_on_world_change` | `BooleanSetting` | `true` | `true` / `false` | Resetowanie tranzycji przy zmianie świata |
| | `restore_vanilla_fog` | `BooleanSetting` | `true` | `true` / `false` | Przywracanie stanu waniliowego przy wyłączeniu |

## 3. Integracja z kontrolerem renderowania

Moduł komunikuje się bezpośrednio z [FogVisualController](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/rendereffect/fog/FogVisualController.java). Przy każdej zmianie suwaków lub przełączeniu profilu wywoływana jest metoda `syncToController()`, która aktualizuje parametry w kontrolerze.
