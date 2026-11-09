# Lokalne autouzupełnianie komend — HypixelAutocomplete

## 1. Cel i zasada działania

Serwer Hypixel posiada setki specyficznych komend sieciowych (zarządzanie drużyną `/party`, gildią `/guild`, przyjaciółmi `/friend`, grami `/play` itp.). Domyślne autouzupełnianie Minecrafta wysyła pakiet sieciowy z zapytaniem o podpowiedzi do serwera (`ServerboundCommandSuggestionPacket`), co generuje opóźnienia i może nie zwracać wszystkich przydatnych wariantów.

Komponent **`HypixelAutocomplete`** (`pl.zombek.client.core.hypixel.HypixelAutocomplete`) implementuje w 100% **lokalny słownik komend**, który działa natychmiastowo i nie generuje żadnego ruchu sieciowego.

---

## 2. Słownik 22 wbudowanych komend Hypixela

Moduł zawiera zoptymalizowany katalog najczęściej używanych komend wraz z ich parametrami i opisami:

| Komenda bazowa | Przykłady uzupełnień | Opis działania |
| :--- | :--- | :--- |
| `/party` (`/p`) | `/p invite [player]`, `/p leave`, `/p warp`, `/p disband`, `/p list`, `/p kick [player]` | Zarządzanie drużyną graczy. |
| `/guild` (`/g`) | `/g list`, `/g chat`, `/g online`, `/g leave`, `/g invite [player]` | Zarządzanie gildią. |
| `/friend` (`/f`) | `/f add [player]`, `/f remove [player]`, `/f list`, `/f requests` | Zarządzanie listą znajomych. |
| `/msg` (`/w`) | `/msg [player] [message]` | Prywatna wiadomość do gracza. |
| `/r` | `/r [message]` | Szybka odpowiedź na ostatnią wiadomość prywatną. |
| `/play` | `/play bedwars_eight_two`, `/play skywars_solo` | Dołączanie do kolejki gry. |
| `/lobby` (`/hub`) | `/lobby`, `/lobby bedwars`, `/lobby skywars` | Powrót do lobby głównego lub trybu. |
| `/rejoin` | `/rejoin` | Ponowne dołączenie do trwającego meczu po rozłączeniu. |
| `/who` | `/who` | Lista graczy na bieżącym serwerze gry. |
| `/tip` | `/tip all` | Wysłanie podziękowań dla aktywnych boosterów. |

---

## 3. Dopasowywanie prefiksów i nawigacja

1. **Dopasowanie prefiksowe:**
   - Wpisanie `/p ` natychmiast filtruje listę do operacji związanych z drużyną.
   - Wpisanie `/play b` podpowiada dostępne tryby Bedwars (`bedwars_eight_one`, `bedwars_eight_two`, `bedwars_four_four`).
2. **Nawigacja klawiaturą:**
   - Strzałki `GÓRA` / `DÓŁ` pozwalają na przeglądanie dostępnych propozycji (`selectNext()`, `selectPrevious()`).
   - Klawisz `TAB` lub `ENTER` wywołuje `acceptSuggestion()`, wstawiając wybraną propozycję do pola tekstowego czatu.
3. **Podgląd składni i opisów:**
   - Każda propozycja (`AutocompleteSuggestion`) zawiera tekst komendy, jej wymaganą składnię oraz krótki opis funkcjonalny.

Scenariusze działania słownika i nawigacji zostały przetestowane w `HypixelAutocompleteTest.java` (6 testów, 100% zaliczone).
