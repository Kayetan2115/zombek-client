# Moduł Bedwars Session Tracker — ZombekClient

## 1. Przegląd i przeznaczenie

`BedwarsSessionTracker` (`pl.zombek.client.core.bedwars.BedwarsSessionTracker`) to komponent rejestrujący i analizujący osiągnięcia gracza podczas bieżącej sesji gry w Bedwars.

Działa całkowicie lokalnie w pamięci klienta i nie wymaga zewnętrznych kluczy API (np. Hypixel API) ani zapytań HTTP do serwisów statystyk.

---

## 2. Śledzone wskaźniki i statystyki

Tracker gromadzi następujące dane z bieżącej gry oraz zagregowane dane sesji:

| Wskaźnik | Źródło danych | Opis |
| :--- | :--- | :--- |
| **Kills** (Zwykłe zabójstwa) | Scoreboard + Chat Parser | Liczba wyeliminowanych przeciwników, którzy mogą się jeszcze odrodzić |
| **Final Kills** (Ostateczne zabójstwa) | Chat Parser + Scoreboard | Liczba ostatecznie wyeliminowanych graczy (bez łóżka) |
| **Deaths** (Zgony gracza) | Chat Parser (self-victim) | Liczba zgonów gracza w trakcie meczu |
| **Beds Broken** (Zniszczone łóżka) | Chat Parser (self-killer) + Scoreboard | Liczba łóżek osobiście zniszczonych przez gracza |
| **Confirmed Wins** (Wygrane mecze) | Chat Parser ("VICTORY!") | Liczba zwycięskich rozgrywek w sesji |
| **Confirmed Losses** (Przegrane mecze) | Chat Parser ("GAME OVER!") | Liczba przegranych meczów |
| **W/L Ratio** (Wskaźnik wygranych) | Wyliczenie matematyczne | Stosunek wygranych do przegranych: $\text{Wins} / \max(1, \text{Losses})$ |
| **K/D Ratio** (Stosunek zabić do zgonów) | Wyliczenie matematyczne | Stosunek zabić do zgonów: $\text{Kills} / \max(1, \text{Deaths})$ |
| **Session Time** (Czas trwania sesji) | Zegar systemowy | Całkowity czas od uruchomienia klienta lub rozpoczęcia meczu |

---

## 3. Mechanizm deduplikacji zdarzeń (Deduplication Cache)

Aby zapobiec podwójnemu naliczaniu statystyk wynikającemu ze zbieżności zdarzeń (np. pojawienie się wpisu na czacie oraz jednoczesna aktualizacja wiersza na scoreboardzie), `BedwarsSessionTracker` wykorzystuje bufor deduplikacji w oknie czasowym:

```java
private final Map<String, Long> deduplicationCache = new HashMap<>();
private static final long DEDUP_WINDOW_MS = 3000L;
```

Dla każdego zdarzenia generowana jest unikalna sygnatura (np. `kill:steve`, `final_kill:alex`, `bed_broken:BLUE`). Jeśli w ciągu 3000 ms pojawi się zdarzenie o identycznej sygnaturze, jest ono ignorowane.

---

## 4. Archiwizacja meczów i historia sesji (Match History)

Po zakończeniu meczu tworzony jest obiekt `BedwarsMatchSummary`:
```java
public static class BedwarsMatchSummary {
    private final boolean victory;
    private final int kills;
    private final int finalKills;
    private final int deaths;
    private final int bedsBroken;
    private final long durationSeconds;
    private final long timestamp;
}
```

Historia przechowuje do 10 ostatnich meczów (`maxStoredSessions`), co umożliwia podgląd trendu formy gracza w trakcie sesji.

Gdy opcja `autoResetOnNewMatch` jest włączona, statystyki pojedynczego meczu (Kills, Final Kills, Beds Broken) są zerowane na początku nowej rundy, podczas gdy zagregowane statystyki sesji (Wins, Losses, całkowity czas) są zachowywane.
