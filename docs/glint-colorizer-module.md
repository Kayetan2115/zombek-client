# Moduł Glint Colorizer (Etap 8C3)

## 1. Przegląd i przeznaczenie

Moduł **Glint Colorizer** (`GlintColorizerModule`) umożliwia zmianę wyglądu i zachowania fioletowego waniliowego efektu zaklęcia na przedmiotach i zbrojach Minecraft.

Obsługuje style animacji:
- `VANILLA` — domyślny fioletowy blask
- `STATIC` — jednolita wybrana barwa
- `RAINBOW` — płynna animacja tęczy generowana w locie bez alokacji buforów
- `GRADIENT` — płynne sinusoidalne przejście między dwiema barwami
- `PULSE` — łagodne pulsowanie jasności i przezroczystości
- `CUSTOM` — niezależna konfiguracja per kategoria przedmiotu

Wszystkie modyfikacje są czysto renderujące — nie modyfikują danych NBT, komponentów ani pakietów sieciowych przedmiotu.

## 2. Grupy ustawień

| Grupa | Klucz | Typ | Domyślnie | Opis |
|---|---|---|---|---|
| **General** | `mode` | `EnumSetting<GlintMode>` | `STATIC` | Tryb animacji glintu |
| | `glint_speed` | `NumberSetting` | `1.0x` | Prędkość animacji tęczy/gradientu/pulsacji |
| | `glint_opacity` | `NumberSetting` | `100%` | Przezroczystość efektu zaklęcia |
| **Colors** | `primary_color` | `ColorSetting` | `0xFF8040CC` | Główny kolor zaklęcia |
| | `secondary_color` | `ColorSetting` | `0xFF00FFFF` | Drugi kolor dla gradientu |
| **Categories** | `category_overrides` | `BooleanSetting` | `false` | Włącz odrębne barwy per kategoria |
| | `weapons_color` | `ColorSetting` | `0xFFFF2222` | Barwa dla broni (miecze, topory, łuki) |
| | `tools_color` | `ColorSetting` | `0xFF22FF22` | Barwa dla narzędzi (kilofy, łopaty) |
| | `armor_color` | `ColorSetting` | `0xFF2288FF` | Barwa dla pancerza |
| | `books_color` | `ColorSetting` | `0xFFFFDD00` | Barwa dla zaklętych ksiąg |
| | `potions_color` | `ColorSetting` | `0xFFFF00FF` | Barwa dla mikstur |
| | `elytra_color` | `ColorSetting` | `0xFF00FFFF` | Barwa dla elytry |
| | `shields_color` | `ColorSetting` | `0xFFFF8800` | Barwa dla tarcz |
| **Locations** | `show_inventory` | `BooleanSetting` | `true` | Widoczność w ekwipunku |
| | `show_hotbar` | `BooleanSetting` | `true` | Widoczność na pasku podręcznym |
| | `show_hand` | `BooleanSetting` | `true` | Widoczność w dłoniach postaci |
| | `show_dropped` | `BooleanSetting` | `true` | Widoczność na przedmiotach na ziemi |
| | `show_containers` | `BooleanSetting` | `true` | Widoczność w skrzyniach |
| | `show_armor` | `BooleanSetting` | `true` | Widoczność na modelu założonej zbroi |
| **Integration** | `shiny_pots_priority` | `EnumSetting<ShinyPotsPriority>` | `PRESERVE_SHINY_POTS` | Priorytet kolidowania z modułem Shiny Pots |
| **Behavior** | `reset_on_world_change` | `BooleanSetting` | `false` | Reset do waniliowego glintu przy wyjściu |
