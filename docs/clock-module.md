# Clock Module — ZombekClient (Etap 8B3)

## 1. Przeznaczenie i Rola Modułu

Moduł `ClockModule` (`"clock"`) w ZombekClient dostarcza uniwersalny komponent HUD czasu rzeczywistego (systemowego) oraz czasu świata Minecraft (in-game time). Jest zarejestrowany w kategorii `ModuleCategory.HUD` („HUD / Informacyjne”) i w pełni zintegrowany ze wspólnym silnikiem czasu `ZombekTimeService`, rejestrem `HudElementRegistry`, systemem kolizji `HudPriorityResolver` (priorytet bazowy: 40) oraz menu konfiguracyjnym Right Shift GUI.

Pliki źródłowe:
- Moduł HUD: [ClockModule.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/clock/ClockModule.java)
- Tryby wyświetlania: [ClockMode.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/clock/ClockMode.java)
- Wzorce daty: [DateFormatPattern.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/clock/DateFormatPattern.java)
- Silnik czasu: [ZombekTimeService.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/ZombekTimeService.java)
- Fazy pory dnia: [TimeOfDayPhase.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/TimeOfDayPhase.java)

---

## 2. Architektura i Przepływ Danych

```mermaid
flowchart TD
    subgraph Time Sources
        Sys[System Clock / Wall Clock]
        World[Minecraft World TimeOfDay Ticks]
    end

    subgraph Data Providers
        FabP[FabricTimeDataProvider]
        ForgeP[ForgeTimeDataProvider]
        NeoP[NeoForgeTimeDataProvider]
        QuiltP[QuiltTimeDataProvider]
        FallP[FallbackTimeDataProvider]
    end

    subgraph Core Time Service
        TS[ZombekTimeService]
        Phase[TimeOfDayPhase]
    end

    subgraph HUD Module
        ClockMod[ClockModule]
        HudModel[HudComponentModel]
        HudPrio[HudPriorityResolver (Priority: 40)]
        HudReg[HudElementRegistry]
    end

    Sys --> TS
    World --> FabP & ForgeP & NeoP & QuiltP & FallP
    FabP & ForgeP & NeoP & QuiltP & FallP --> TS
    TS --> Phase
    TS --> ClockMod
    ClockMod --> HudModel
    HudModel --> HudPrio
    ClockMod --> HudReg
```

1. **Źródło czasu rzeczywistego:** `ZombekTimeService.getWallClockZonedDateTime()` pobiera strefę czasową użytkownika i czas systemowy w oparciu o wstrzykiwane `ITimeSource` (wspierające deterministyczne testy jednostkowe).
2. **Źródło czasu gry:** `ITimeDataProvider.getWorldTimeOfDay()` dostarcza ticki czasu świata (0..23999), które przeliczane są na format 24-godzinny Minecraft:
   $$\text{hours} = ((\text{ticks} + 6000) \pmod{24000}) / 1000$$
   $$\text{minutes} = (((\text{ticks} + 6000) \pmod{24000}) \pmod{1000}) \times 60 / 1000$$
3. **Wykrywanie faz dnia:** `TimeOfDayPhase` wylicza fazy: `DAWN` (22200-24000 / 0-1000), `MORNING` (1000-6000), `NOON` (6000), `AFTERNOON` (6000-12000), `DUSK` (12000-13800), `NIGHT` (13800-18000), `MIDNIGHT` (18000), w tym flagi wschodu (`isSunrise`) i zachodu (`isSunset`) słońca.

---

## 3. Tryby Pracy (`ClockMode`)

1. **`DIGITAL` (Domyślny):** Wyświetla sam czas rzeczywisty (np. `14:32:05` lub `02:32 PM`).
2. **`MINIMAL`:** Ultra-kompaktowy zapis godziny i minut (np. `14:32`).
3. **`DETAILED`:** Godzina, data oraz nazwa dnia tygodnia (np. `14:32:05 | Poniedziałek, 2026-11-03`).
4. **`MINECRAFT`:** Czas świata gry wraz z ikoną/wskaźnikiem słońca/księżyca i nazwą fazy (np. `MC: 12:00 (Południe)`).
5. **`DUAL`:** Dwa wiersze łączące precyzyjny czas rzeczywisty oraz czas świata Minecraft.
6. **`CUSTOM`:** Użytkownik definiuje własny szablon tekstowy ze znacznikami `{time}`, `{mc_time}`, `{phase}`, `{date}`.

---

## 4. Opcje Konfiguracyjne

Moduł posiada 5 zorganizowanych grup ustawień:
- **General (`group_general`):**
  - `mode` (Enum: `DIGITAL`, `MINIMAL`, `DETAILED`, `MINECRAFT`, `DUAL`, `CUSTOM`, domyślnie `DIGITAL`).
  - `time_format_24h` (Boolean: 24-godzinny vs 12-godzinny AM/PM, domyślnie `true`).
  - `show_seconds` (Boolean: pokazywanie sekund, domyślnie `true`).
  - `leading_zero` (Boolean: wiodące zero dla godzin < 10, domyślnie `true`).
- **Date (`group_date`):**
  - `show_date` (Boolean: wyświetlanie daty, domyślnie `false`).
  - `show_day_of_week` (Boolean: dzień tygodnia, domyślnie `false`).
  - `date_pattern` (Enum: `YYYY_MM_DD`, `DD_MM_YYYY`, `MM_DD_YYYY`, `SHORT_SLASH`, `MONTH_NAME`, domyślnie `YYYY_MM_DD`).
- **Minecraft Time (`group_mc_time`):**
  - `show_day_night_indicator` (Boolean: ikona/symbol słońca/księżyca, domyślnie `true`).
  - `show_phase_name` (Boolean: słowna faza dnia, domyślnie `true`).
  - `show_sunrise_sunset` (Boolean: wyróżnienie wschodu/zachodu słońca, domyślnie `true`).
- **Appearance (`group_appearance`):**
  - Kolory tekstu, tła i obramowania (`text_color`, `background_color`, `border_color`).
  - Styl tła (`CHROMA`, `RAINBOW`, `SHADOW`, `BORDER`).
- **Layout (`group_layout`):**
  - Kotwica HUD (`anchor`), offsety `offset_x`, `offset_y`, `scale`, marginesy wewnętrzne.

---

## 5. Obsługa Braku Danych i Bezpieczeństwo

Gdy gracz znajduje się w menu głównym lub na serwerze nieprzesyłającym pakietów czasu świata:
- W trybie podglądu (Right Shift GUI): moduł wyświetla syntetyczny podgląd oznaczony flagą `[PREVIEW]` (np. `12:00 [PREVIEW]`).
- W świecie multiplayer bez synchronizacji czasu: moduł zwraca `MC: N/A` zamiast symulować fikcyjny czas.
- Nigdy nie powoduje wyjątków w pętli renderowania HUD.
