# Śledzenie Sesji UHC (UhcSessionTracker & UhcTimer) — ZombekClient

## 1. Cel i Odpowiedzialności

Klasa `UhcSessionTracker` oraz komponenty zegarowe `UhcTimer` we wspólnym module `:core` (`pl.zombek.client.core.uhc`) odpowiadają za precyzyjne, odporne na desynchronizacje i lag serwera śledzenie czasu meczu UHC oraz jego poszczególnych faz.

Główna klasa menedżera znajduje się w:
[UhcSessionTracker.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/uhc/UhcSessionTracker.java)

---

## 2. Precyzyjne Zegary Monotoniczne (`UhcTimer`)

Klasa `UhcTimer` wykorzystuje monotoniczny zegar systemowy `System.nanoTime()`, eliminując anomalie związane ze zmianami czasu systemowego, dryfem zegara czy zawieszeniami klatek.

Obsługiwane typy zegarów (`UhcTimerType`):
- **`GAME` (Count-up):** Całkowity czas od rozpoczęcia meczu (domyślnie format `HH:MM:SS` lub `MM:SS`).
- **`PVP` (Countdown):** Czas trwania okresu ochronnego (Grace Period, domyślnie 10 minut / 600 sekund). Po wygaśnięciu wyświetla status `ACTIVE`.
- **`BORDER` (Countdown):** Czas do pierwszego lub kolejnego zmniejszenia granicy świata (domyślnie 20 minut / 1200 sekund).
- **`MEETUP` (Countdown):** Czas do fazy zbiórki na koordynatach `0,0` (domyślnie 45 minut / 2700 sekund).
- **`DEATHMATCH` (Countdown):** Odliczanie fazy finałowej.
- **`CUSTOM` (Count-up / Countdown):** Dowolny zegar konfigurowany przez gracza.

Wszystkie zegary obsługują metody cyklu życia: `start()`, `pause()`, `resume()`, `stop()`, `reset()`.

---

## 3. Pasywne Parsowanie Czatu (`UhcChatParser`)

`UhcChatParser` analizuje wyłącznie wiadomości tekstowe dostarczone do klienta przez serwer (np. wiadomości systemowe i broadcasty meczowe). Nie wysyła żadnych komend ani pakietów.

Wykrywane zdarzenia i wzorce wielojęzyczne (angielski i polski):
- **Rozpoczęcie gry:** `game has begun`, `match started`, `the game has started`, `gra wystartowala`, `mecz rozpoczety` -> `onMatchStart()`.
- **Aktywacja PvP:** `pvp is now enabled`, `grace period has ended`, `pvp enabled`, `pvp wlaczone`, `ochrona minela` -> `onPvpEnabled()`.
- **Ruch granicy:** `border is shrinking`, `border moving`, `world border is shrinking`, `granica sie zmniejsza` -> `onBorderShrinking()`.
- **Faza Deathmatch:** `deathmatch will begin`, `deathmatch started`, `meetup time`, `faza deathmatch` -> `onDeathmatch()`.
- **Koniec gry:** `has won the game`, `winner is`, `victory!`, `zwyciezca meczu` -> `onGameOver()`.

---

## 4. Ekstrakcja Danych ze Scoreboardu (`UhcScoreboardParser`)

`UhcScoreboardParser` pobiera linie tekstu z bocznego paska wyników (`DisplaySlot.SIDEBAR`).
Zastosowano wyrażenia regularne z obsługą Unicode (`(?iu)`):
- Gracze żywi: `(?:players|alive|żywi|zywi|gracze)[:\s]+([0-9]+(?:/[0-9]+)?)`
- Drużyny: `(?:teams|drużyny|druzyny)[:\s]+([0-9]+)`
- Granica: `(?:border|granica)[:\s]+([±0-9xX]+(?:\s*m)?)`
- Czas: `(?:time|czas)[:\s]+([0-9]{1,2}:[0-9]{2}(?::[0-9]{2})?)`
- Status: `(?:status|phase|faza)[:\s]+([a-zA-Z0-9_ -]+)`

W przypadku braku dopasowania pole zachowuje bezpieczną wartość domyślną `"N/A"`.
Moduł czyści dane natychmiast po wywołaniu `reset()`.
