# Chat Module

Moduł **Chat** (`chat`) wchodzi w skład kategorii `VISUAL` klienta **ZombekClient** (priorytet HUD: `110`).
Odpowiada za kompleksową personalizację okna czatu Minecraft, rendering graficzny z obsługą stylów i przezroczystości, wielokanałowe karty lokalne (Chat Tabs), uniwersalny silnik filtrowania (Universal Chat Filter), bufor historii wiadomości (Chat History Ring-Buffer) z sanifikacją poświadczeń, konfigurowalne pole wprowadzania (Custom Chat Input) oraz lokalny system powiadomień i wzmianek.

## 1. Grupy Ustawień

Moduł udostępnia 11 spójnych grup konfiguracyjnych w menu Right Shift:

1. **General Settings**:
   - `chat_enabled`: Główny przełącznik ON/OFF modułu.
   - `override_vanilla`: Zastąpienie domyślnego mechanizmu ChatHud silnikiem ZombekClient.
   - `compact_repeats`: Grupowanie identycznych wiadomości z rzędu z licznikiem powtórzeń (`×2`, `×3`).
   - `timestamps`: Format znaczników czasu (`NONE`, `HH_MM`, `HH_MM_SS`, `HH_MM_SS_12H`, `RELATIVE`, `CUSTOM`).

2. **Visual Preset**:
   - Wybór szablonu wyglądu: `VANILLA`, `CLASSIC`, `MINIMAL`, `MODERN`, `GLASS`, `COMPACT`, `CUSTOM`.

3. **Dimensions & Layout**:
   - `chat_width`: Szerokość okna czatu (160 - 640 px, domyślnie 320 px).
   - `chat_height_focused`: Wysokość okna przy otwartym ekranie czatu (60 - 400 px, domyślnie 180 px).
   - `chat_height_unfocused`: Wysokość przy zamkniętym ekranie czatu (40 - 300 px, domyślnie 100 px).
   - `chat_padding`: Margines wewnętrzny okna (0 - 16 px).
   - `chat_scale`: Skalowanie tekstu i elementów czatu (0.5x - 2.0x).
   - `line_height`: Odstęp wierszy (8 - 24 px).

4. **Typography**:
   - `font_shadow`: Cień pod czcionką tekstu.
   - `smooth_font`: Wygładzanie czcionki.

5. **Colors & Transparency**:
   - `background_opacity`: Przezroczystość tła okna (0.0 - 1.0).
   - `background_color`: Kolor bazowy tła (ARGB).
   - `border_color`: Kolor obramowania okna.
   - Kolorystyka dedykowana dla poszczególnych kategorii wiadomości (General, Whisper, Party, Guild, System, Error).

6. **Tabs Configuration**:
   - `show_tabs`: Wyświetlanie paska kart kanałów.
   - `tab_unread_badge`: Wyświetlanie etykiety nieprzeczytanych wiadomości.

7. **Universal Filter**:
   - Szybkie przełączniki filtrowania: ukrywanie spamu, dołączania/opuszczania, wiadomości o śmierci, alertów serwera.
   - Dostęp do reguł niestandardowych (Exact, Contains, Safe RegEx).

8. **Local History**:
   - `history_limit`: Pojemność pierścieniowego bufora historii (do 5000 wpisów).
   - `sanitize_credentials`: Automatyczne maskowanie haseł w poleceniach `/login`, `/register`.
   - `clear_on_disconnect`: Czyszczenie pamięci po rozłączeniu z serwerem.

9. **Custom Chat Input**:
   - `save_drafts`: Zapamiętywanie wersji roboczych tekstu osobno dla każdej karty i po zamknięciu GUI.
   - `input_background_opacity`: Przezroczystość pola edycji tekstu.

10. **Local Notifications**:
    - `mention_player_name`: Dźwięk i podświetlenie przy wymienieniu nicku gracza.
    - `mention_friends`: Podświetlenie i powiadomienie przy wymienieniu pseudonimu znajomego (`NameTagFriendManager`).
    - `custom_keywords`: Niestandardowe słowa kluczowe wywołujące alert.
    - `notification_cooldown_ms`: Ochrona przed spamem dźwiękowym.

11. **Advanced**:
    - Przełączniki integracji loaderów i Reset do wartości domyślnych.

## 2. Architektura i Bezpieczeństwo

Moduł działa wyłącznie po stronie klienta (Client-Side Only).
Nie modyfikuje ani nie fałszuje podpisów kryptograficznych wiadomości sieciowych (`SignedMessage`), nie wysyła automatycznych pakietów do serwera i nie wykonuje poleceń bez wiedzy użytkownika.
