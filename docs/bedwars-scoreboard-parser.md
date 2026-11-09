# Moduł Bedwars Scoreboard Parser — ZombekClient

## 1. Przegląd i przeznaczenie

`BedwarsScoreboardParser` (`pl.zombek.client.core.bedwars.BedwarsScoreboardParser`) to wydajny komponent czasu rzeczywistego, przetwarzający linie tekstu pobierane z bocznego paska punktacji (Minecraft Objective / Sidebar Scoreboard).

Komponent ten dostarcza kluczowych danych strukturalnych dla pozostałych podsystemów Bedwars Helper:
- Wykrywanie mapy i trybu gry.
- Wykrywanie własnej drużyny gracza (`(you)` / `(ty)`).
- Statusy łóżek i liczby żyjących graczy dla 8 drużyn.
- Zegary ulepszeń generatorów (Diamenty, Szmaragdy, Zniszczenie Łóżek, Nagła Śmierć).
- Statystyki meczowe podawane przez serwer (Kills, Final Kills, Beds Broken).

---

## 2. Profile parsera (`ParserProfile`)

Komponent obsługuje 3 profile parsowania:

1. **`HYPIXEL_ENGLISH` (Domyślny)**:
   - Zoptymalizowany pod oficjalne formaty Hypixel Network (`hypixel.net`).
   - Dopasowuje linie typu `R Red: ✓ (you)`, `B Blue: ✕`, `G Green: 2`, `Y Yellow: [E]`.
   - Rozpoznaje timery: `Diamond II in 01:30`, `Emerald II in 03:45`, `Beds Destroy in 05:00`.
2. **`GENERIC_POLISH`**:
   - Zoptymalizowany pod polskie serwery Bedwars.
   - Dopasowuje prefiksy: `Mapa:`, `Tryb:`, `Zabójstwa:`, `Ostateczne:`, `Zniszczone łóżka:`.
   - Obsługuje odmianę barw drużyn: `Czerwoni`, `Niebiescy`, `Zieloni`, `Żółci` itd.
3. **`CUSTOM` (Niestandardowy)**:
   - Pozwala użytkownikowi zdefiniować w menu Right Shift własne reguły regex rozdzielone średnikami (np. `team=([A-Z]):(\\w+);timer=(\\w+):(\\d+:\\d+)`).

---

## 3. Optymalizacja wydajności i buforowanie

Sidebar scoreboard w Minecraft odświeża się do 20 razy na sekundę. Aby uniknąć niepotrzebnego obciążania CPU wyrażeniami regularnymi:
1. `lastRawLines` — parser przechowuje listę poprzednich linii i natychmiast wychodzi, jeśli lista się nie zmieniła (`equals`).
2. Kompilowane stałe `Pattern` — wszystkie wyrażenia regularne są kompilowane raz przy starcie klasy (flagi `(?iu)`: case-insensitive i Unicode).
3. Rozdzielenie analizy statystyk:
   - `STATS_FINALS_PATTERN` ewaluowane przed `STATS_KILLS_PATTERN` z użyciem negative lookbehind (`(?<!final\\s)(?<!ostateczne\\s)\\b(?:Kills|Zabójstwa)`), co zapobiega błędnemu nadpisywaniu zabójstw przez zabójstwa ostateczne.

---

## 4. Parser timerów generatorów

Timery są parsowane za pomocą regexu:
```java
private static final Pattern TIMER_PATTERN = Pattern.compile(
    "(?iu)(Diamond|Emerald|Beds? Destroy|Sudden Death|Dragon Spawn|Koniec|Ulepszenie)(?:\\s+([IVX0-9]+))?\\s+(?:in|za)?\\s*([0-9]{1,2}:[0-9]{2})"
);
```

Czas podany w formacie `MM:SS` jest automatycznie przeliczany na całkowitą liczbę sekund ($T = 60 \cdot M + S$). Obiekt `BedwarsTimer` jest przekazywany do `BedwarsTimerManager`, który lokalnie kontynuuje płynne odliczanie między aktualizacjami scoreboardu.
