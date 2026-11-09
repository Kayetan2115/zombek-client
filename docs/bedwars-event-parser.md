# Moduł Bedwars Event Parser & Feed — ZombekClient

## 1. Przegląd i przeznaczenie

`BedwarsChatParser` oraz `BedwarsEventTracker` to komponenty odpowiedzialne za odczytywanie zdarzeń meczowych z czatu gry i prezentowanie ich w formie estetycznego powiadomienia (Event Feed) na ekranie HUD.

System działa w sposób czysto pasywny — nie filtruje, nie usuwa ani nie modyfikuje wiadomości gracza na czacie, lecz bezpiecznie odczytuje przychodzące pakiety tekstowe po usunięciu kodów kolorystycznych Minecraft (`§[0-9a-fk-or]`).

---

## 2. Obsługiwane typy zdarzeń (`BedwarsEventType`)

| Typ zdarzenia | Nazwa wyświetlana | Domyślna ikona | Kolor akcentu |
| :--- | :--- | :---: | :---: |
| `BED_DESTROYED` | `<DRUŻYNA> BED DESTROYED` | `⚑` | Kolor drużyny |
| `FINAL_KILL` | `FINAL KILL: <Gracz>` | `⚔` | `#FF5555` (Czerwony) |
| `PLAYER_ELIMINATED`| `<Gracz> zabity` | `☠` | `#AAAAAA` (Szary) |
| `TEAM_ELIMINATED` | `<DRUŻYNA> ELIMINATED` | `✕` | Kolor drużyny |
| `GAME_STARTED` | `MECZ ROZPOCZĘTY` | `▶` | `#55FF55` (Zielony) |
| `SUDDEN_DEATH` | `NAGŁA ŚMIERĆ` | `⚡` | `#FF55FF` (Fioletowy) |
| `VICTORY` | `ZWYCIĘSTWO!` | `★` | `#55FF55` (Jasnozielony) |
| `DEFEAT` | `KONIEC GRY` | `✖` | `#FF5555` (Czerwony) |

---

## 3. Dopasowywanie wzorców regex (Polski i Angielski)

Parser obsługuje zarówno formaty Hypixel (język angielski), jak i standardowe formaty serwerów polskich:

### Zniszczenie łóżka (Bed Broken)
- **Hypixel (EN)**: `(?iu)(?:BED DESTRUCTION > )?(\S+)\s+Bed was destroyed by\s+([^!.]+)`
  - Przykład: `BED DESTRUCTION > Red Bed was destroyed by Steve!`
- **Polskie serwery (PL)**: `(?iu)Łóżko drużyny\s+(\S+)\s+zostało zniszczone przez\s+([^!.]+)`
  - Przykład: `Łóżko drużyny Niebiescy zostało zniszczone przez Alex!`

### Eliminacja drużyny (Team Eliminated)
- **Hypixel (EN)**: `(?iu)(?:TEAM ELIMINATED > )?(\\S+)\\s+Team has been eliminated`
  - Przykład: `TEAM ELIMINATED > Green Team has been eliminated!`
- **Polskie serwery (PL)**: `(?iu)Drużyna\\s+(\\S+)\\s+została wyeliminowana`
  - Przykład: `Drużyna Żółci została wyeliminowana!`

### Ostateczne zabójstwo (Final Kill)
- **Hypixel (EN)**: `(?iu)(?:FINAL KILL > )?(.+?)\s+(?:was killed by|was knocked into the void by|was struck down by)\s+(.+?)(?:\.?\s*FINAL KILL!?)`
  - Przykład: `Alex was knocked into the void by Steve. FINAL KILL!`
- **Polskie serwery (PL)**: `(?iu)(?:OSTATECZNE ZABÓJSTWO > )?(.+?)\s+został wyeliminowany przez\s+([^!.]+)`
  - Przykład: `Alex został wyeliminowany przez Steve!`

---

## 4. Animacja zanikania i czas trwania (Event Feed Lifecycle)

Każde zdarzenie trafiające do `BedwarsEventTracker` ma przypisany znacznik czasu utworzenia `timestampMs`.

W pętli renderowania HUD (`onTick` / `render`):
1. Zdarzenia starsze niż `eventDurationSeconds` (konfigurowalne w zakresie 1.0–15.0 s) są usuwane (`pruneExpiredEvents`).
2. Jeśli włączona jest opcja `fadeAnimation`, przezroczystość alfa jest dynamicznie wyliczana ze wzoru:
   $$\alpha = 1.0 - \max\left(0.0, \frac{\Delta t - t_{\text{solid}}}{t_{\text{fade}}}\right)$$
   gdzie $\Delta t$ to wiek powiadomienia, a $t_{\text{solid}}$ to początkowy okres pełnej widoczności (np. pierwsze 70% czasu trwania).
3. Bufor zdarzeń jest ograniczony do `maxEvents` (domyślnie 5 najnowszych powiadomień).
