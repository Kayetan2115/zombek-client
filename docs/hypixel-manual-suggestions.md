# Manualne sugestie socjalne (Alternatywa AutoGG) — HypixelManualSuggestions

## 1. Dlaczego tradycyjny AutoGG jest niebezpieczny?

Wielu graczy korzystało w przeszłości z funkcji **AutoGG** (automatycznego wysyłania wiadomości "gg" na czacie ułamek sekundy po wygranej lub przegranej). Na serwerze Hypixel automatyzacja ta niesie ze sobą istotne ryzyka:

1. **Wykrywanie przez systemy heurystyczne Watchdog:**
   Automatyczne wysłanie pakietu czatu w tej samej milisekundzie po pakiecie zakończenia gry jest nienaturalnym zachowaniem człowieka i może skutkować flagowaniem konta.
2. **Naruszenie regulaminu Fair Play:**
   Regulamin Hypixel jednoznacznie zabrania automatyzacji jakichkolwiek akcji gracza, w tym generowania wpisów na czacie bez fizycznej interakcji użytkownika.

---

## 2. Rozwiązanie Fair Play: Manual Social Suggestions

W ZombekClient zastąpiono niebezpieczną automatyzację systemem **Manual Social Suggestions** (`pl.zombek.client.core.hypixel.HypixelManualSuggestions`).

Zasada działania opiera się na **wspomaganiu użytkownika, a nie zastępowaniu go**:

1. **Dyskretna karta HUD:**
   - Po wykryciu zakończenia meczu (zwycięstwo, porażka, koniec rundy Bedwars/SkyWars) na ekranie pojawia się elegancka karta HUD.
   - Karta zawiera sugerowaną wiadomość (np. `Good Game!`, `gg`, `Powodzenia!`).
2. **Kopiowanie do schowka jednym kliknięciem (1-Click Clipboard Copy):**
   - Kliknięcie przycisku na karcie lub naciśnięcie dedykowanego skrótu kopiuje treść do systemowego schowka operacyjnego (`copyToClipboard()`).
3. **Bezwzględny brak symulacji klawiatury:**
   - Moduł **nigdy** nie wywołuje `sendChatMessage()`.
   - Moduł **nigdy** nie symuluje wciśnięć klawiszy (brak użycia bibliotek `java.awt.Robot` czy wstrzykiwania zdarzeń GLFW).
   - To gracz decyduje, czy otworzy czat (`T`) i wklei wiadomość (`Ctrl+V`).

---

## 3. Konfiguracja i szablony wiadomości

Użytkownik może w menu `Right Shift` -> `Hypixel Mods` -> `Sugestie socjalne` dostosować:

- **Wybór szablonu:**
  - `GG`: klasyczne "gg" lub "Good Game!".
  - `GL`: życzenie powodzenia przed meczem ("Good Luck! Have fun!").
  - `CUSTOM`: własna treść zdefiniowana w konfiguracji klienta.
- **Czas wyświetlania karty:** od 1.0 do 10.0 sekund z płynną animacją zanikania.
- **Dźwięk powiadomienia:** subtelny dźwięk interfejsu informujący o dostępności sugestii.

Pełny pakiet testów jednostkowych znajduje się w `HypixelManualSuggestionsTest.java` (5 testów, 100% zaliczone).
