# Moduł Particle Changer (Etap 8C3)

## 1. Przegląd i przeznaczenie

Moduł **Particle Changer** (`ParticleChangerModule`) służy do pełnej personalizacji generowanych cząsteczek w świecie gry, zapewniając jednocześnie ochronę stabilności klatek na sekundę (FPS) dzięki wbudowanym strażnikom wydajności.

Gracz może:
- Dostosować ogólną gęstość i rozmiar cząsteczek.
- Ograniczyć uciążliwe cząsteczki środowiskowe (np. deszcz, dym z ognisk) zachowując czytelne cząstki walki (ciosy krytyczne, sweep attack).
- Zdefiniować własne barwy i skalę dla ciosów krytycznych (`minecraft:crit`) oraz ostrości (`minecraft:enchanted_hit`).
- Ustawić twardy limit aktywnych cząsteczek (od 50 do 5000).

## 2. Grupy ustawień

| Grupa | Klucz | Typ | Domyślnie | Opis |
|---|---|---|---|---|
| **General** | `preset` | `EnumSetting<ParticlePreset>` | `VANILLA` | Profil cząstek (Vanilla, Low, Medium, High, Reduced, Minimal, Custom) |
| | `global_density` | `NumberSetting` | `1.0x` | Ogólny mnożnik gęstości cząsteczek |
| | `global_scale` | `NumberSetting` | `1.0x` | Mnożnik rozmiaru renderowanych cząsteczek |
| | `global_opacity` | `NumberSetting` | `100%` | Przezroczystość cząsteczek |
| **Categories** | `combat_density` | `NumberSetting` | `1.0x` | Gęstość cząstek walki (krytyki, trafienia) |
| | `environment_density` | `NumberSetting` | `1.0x` | Gęstość deszczu, dymu, wody |
| | `magic_density` | `NumberSetting` | `1.0x` | Gęstość mikstur, portali, zaklęć |
| | `blocks_density` | `NumberSetting` | `1.0x` | Gęstość niszczenia i stawiania bloków |
| | `items_density` | `NumberSetting` | `1.0x` | Gęstość pękających narzędzi i pereł |
| | `other_density` | `NumberSetting` | `1.0x` | Gęstość serduszek, nutek i innych |
| **Performance** | `max_particles` | `NumberSetting` | `2000` | Maksymalny limit aktywnych cząstek na scenie |
| | `max_distance` | `NumberSetting` | `64 m` | Maksymalna odległość generowania od gracza |
| **Custom Rules** | `crit_scale` | `NumberSetting` | `1.0x` | Skala cząstek ciosu krytycznego |
| | `crit_density` | `NumberSetting` | `1.0x` | Mnożnik ilości cząstek krytyka |
| | `custom_crit_color`| `BooleanSetting` | `false` | Włącz własny kolor krytyków |
| | `crit_color` | `ColorSetting` | `0xFFFFCC00` | Kolor cząstek krytyka |
| | `sharp_scale` | `NumberSetting` | `1.0x` | Skala cząstek ostrości |
| | `sharp_density` | `NumberSetting` | `1.0x` | Mnożnik ilości cząstek ostrości |
| | `custom_sharp_color`| `BooleanSetting`| `false` | Włącz własny kolor ostrości |
| | `sharp_color` | `ColorSetting` | `0xFF00FFFF` | Kolor cząstek ostrości |
| **Behavior** | `reset_on_world_change` | `BooleanSetting` | `false` | Reset do wartości waniliowych przy rozłączeniu |
