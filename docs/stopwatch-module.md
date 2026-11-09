# Stopwatch Module — ZombekClient (Etap 8B3)

## 1. Przeznaczenie i Rola Modułu

Moduł `StopwatchModule` (`"stopwatch"`) w ZombekClient zapewnia w pełni niezależny, precyzyjny stoper z pomiarem międzyczasów (lap times), niezależnymi profilami aktywności (`Speedrun`, `Building`, `Mining`, `Custom`) oraz integracją ze skrótami klawiszowymi `KeybindManager`.

Działa w kategorii `ModuleCategory.HUD` („HUD / Informacyjne”) i rejestruje się w systemie HUD z priorytetem `HudPriorityResolver` równym 28.

Pliki źródłowe:
- Moduł HUD: [StopwatchModule.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/stopwatch/StopwatchModule.java)
- Stan stopera: [StopwatchState.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/StopwatchState.java)
- Profil stopera: [StopwatchProfile.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/StopwatchProfile.java)
- Typ profilu: [StopwatchProfileType.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/StopwatchProfileType.java)
- Międzyczas: [StopwatchLap.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/StopwatchLap.java)
- Silnik czasu: [ZombekTimeService.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/ZombekTimeService.java)

---

## 2. Niezależność od Tickrate i Precyzja Monotoniczna

Tradycyjne stopery zaimplementowane w modach Minecraft opierają się na liczniku ticków silnika gry ($20 \text{ Hz}$). Gdy serwer cierpi na spadek TPS (lagi) lub gra doświadcza spadków klatek na sekundę (FPS), czas takiego stopera zwalnia, prowadząc do sfałszowanych wyników.

ZombekClient `StopwatchProfile` rozwiązuje ten problem fundamentalnie:
1. Pomiar oparty jest na **`System.nanoTime()`** (monotoniczny zegar sprzętowy nanosekundowy CPU):
   - Niewrażliwy na zmiany zegara systemowego (NTP, ręczna zmiana godziny w systemie operacyjnym).
   - Niewrażliwy na TPS serwera (20 TPS, 5 TPS czy chwilowe zawieszenie nie wpływa na mierzony czas).
   - Niewrażliwy na liczbę klatek HUD / FPS renderingu.
2. Formuła aktualnego czasu w stanie `RUNNING`:
   $$\text{elapsed} = \text{accumulatedNanos} + (\text{currentNanoTime} - \text{startNanoTime})$$
3. W stanach `PAUSED` i `STOPPED`:
   $$\text{elapsed} = \text{accumulatedNanos}$$

---

## 3. Profile Stopera (`StopwatchProfileType`)

Użytkownik może błyskawicznie przełączać się pomiędzy 4 niezależnymi profilami aktywności:
1. **`SPEEDRUN`:** Dedykowany do prób czasowych, pokonywania bossów, rajdów i wyścigów.
2. **`BUILDING`:** Mierzy czas poświęcony na wznoszenie budowli i projektów architektonicznych.
3. **`MINING`:** Monitoruje czas spędzony na wydobyciu i wyprawach górniczych.
4. **`CUSTOM`:** Ogólny stoper użytkownika do dowolnych celów.

Każdy profil zachowuje własny stan, własny czas upłynięty oraz własną historię okrążeń/międzyczasów (do 50 rekordów).

---

## 4. System Okrążeń i Międzyczasów (Laps)

- Rejestracja międzyczasu wylicza:
  - Czas bieżącego okrążenia (`lapTimeNanos`).
  - Całkowity czas od startu (`totalTimeNanos`).
  - Deltę w stosunku do poprzedniego okrążenia ($\Delta = \text{lap}_N - \text{lap}_{N-1}$):
    - $\Delta < 0$ (zielony): czas szybszy niż poprzednie okrążenie.
    - $\Delta > 0$ (czerwony): czas wolniejszy niż poprzednie okrążenie.
- Wyliczanie najlepszego okrążenia (`getBestLap()`) oraz średniego czasu (`getAverageLapTimeNanos()`).

---

## 5. Sterowanie i Integracja z KeybindManager

Moduł udostępnia zarówno przyciski akcji w menu Right Shift, jak i bezpośrednie skróty klawiszowe w `KeybindManager`:
- **Toggle Start/Pause (`keybind_toggle`):** Uruchamia lub pauzuje aktywny profil.
- **Lap (`keybind_lap`):** Rejestruje międzyczas.
- **Reset (`keybind_reset`):** Zeruje licznik (wstrzymuje i ustawia 0).

---

## 6. Opcje Konfiguracyjne

- **General (`group_general`):**
  - `active_profile` (Enum: `SPEEDRUN`, `BUILDING`, `MINING`, `CUSTOM`).
  - `show_millis` (Boolean: wyświetlanie milisekund `.xxx`, domyślnie `true`).
  - `show_profile_name` (Boolean: etykieta profilu, domyślnie `true`).
  - `show_laps` (Boolean: lista ostatnich międzyczasów, domyślnie `true`).
  - `max_laps_displayed` (Number: 1..10, domyślnie 3).
- **Appearance & Layout (`group_appearance`, `group_layout`):**
  - Kolory, styl tła, kotwica HUD, offsety, skala.
