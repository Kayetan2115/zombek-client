# Moduł Color Saturation (Etap 8C3)

## 1. Przegląd i przeznaczenie

Moduł **Color Saturation** (`ColorSaturationModule`) zapewnia graczom możliwość globalnej i selektywnej modyfikacji nasycenia, kontrastu, jasności, wibrancji i balansu kolorów świata gry, postaci, przedmiotów oraz cząsteczek.

Kluczową właściwością modułu jest opcja **Preserve HUD Colors**, która gwarantuje, że elementy interfejsu (pasek życia, pasek głodu, pancerz, tekst czatu, wskaźniki HUD) zachowują naturalne kolory i pełną czytelność bez zniekształceń.

## 2. Grupy ustawień

| Grupa | Klucz | Typ | Domyślnie | Zakres / Wartości | Opis |
|---|---|---|---|---|---|
| **General** | `preset` | `EnumSetting<ColorProfile>` | `VANILLA` | `VANILLA`, `VIBRANT`, `NATURAL`, `SOFT`, `CINEMATIC`, `MONOCHROME`, `CUSTOM` | Profil kolorystyczny |
| | `saturation` | `NumberSetting` | `1.0` | `0.0` - `2.0` | Główne nasycenie barw (0% - 200%) |
| **Adjustment** | `vibrance` | `NumberSetting` | `0.0` | `-1.0` - `1.0` | Inteligentne nasycenie wyciągające blade kolory |
| | `contrast` | `NumberSetting` | `1.0` | `0.5` - `1.5` | Kontrast tonalny |
| | `brightness` | `NumberSetting` | `1.0` | `0.5` - `1.5` | Jasność barw |
| | `hue_shift` | `NumberSetting` | `0°` | `-180°` - `180°` | Przesunięcie odcienia na kole barw HSV |
| | `temperature` | `NumberSetting` | `0.0` | `-1.0` - `1.0` | Temperatura barwowa (ujemne: chłód, dodatnie: ciepło) |
| **RGB Channels** | `red_multiplier` | `NumberSetting` | `1.0` | `0.0` - `2.0` | Wzmocnienie kanału czerwonego |
| | `green_multiplier` | `NumberSetting` | `1.0` | `0.0` - `2.0` | Wzmocnienie kanału zielonego |
| | `blue_multiplier` | `NumberSetting` | `1.0` | `0.0` - `2.0` | Wzmocnienie kanału niebieskiego |
| **Targets** | `affect_world` | `BooleanSetting` | `true` | `true` / `false` | Aplikacja do terenu i bloków |
| | `affect_entities` | `BooleanSetting` | `true` | `true` / `false` | Aplikacja do postaci i mobów |
| | `affect_items` | `BooleanSetting` | `true` | `true` / `false` | Aplikacja do modeli przedmiotów |
| | `affect_particles` | `BooleanSetting` | `true` | `true` / `false` | Aplikacja do cząsteczek |
| | `preserve_hud` | `BooleanSetting` | `true` | `true` / `false` | Zachowanie naturalnych barw HUD |
| **Animation** | `smooth_transition` | `BooleanSetting` | `true` | `true` / `false` | Płynne przechodzenie między profilami |
| | `transition_duration_ms` | `NumberSetting` | `300 ms` | `50` - `2000 ms` | Czas trwania animacji |
| **Behavior** | `reset_on_world_change` | `BooleanSetting` | `false` | `true` / `false` | Przywrócenie profilu Vanilla przy zmianie świata |
