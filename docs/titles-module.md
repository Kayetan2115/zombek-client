# Titles Module

Moduł **Titles** (`titles`) wchodzi w skład kategorii `HUD` klienta **ZombekClient** (priorytet HUD: `115`).
Odpowiada za kompleksową personalizację wyświetlania napisów na ekranie: tytułu głównego (Title), podtytułu (Subtitle) oraz komunikatów nad paskiem ekwipunku (Action Bar).

## 1. Grupy Ustawień

Moduł posiada 8 dedykowanych grup konfiguracyjnych w menu Right Shift:

1. **Ustawienia Ogólne (General)**:
   - `enable_title`: Włączanie/wyłączanie tytułu głównego.
   - `enable_subtitle`: Włączanie/wyłączanie napisów podtytułowych.
   - `enable_action_bar`: Włączanie/wyłączanie Action Bara.
   - `preserve_vanilla_timing`: Czy stosować czasy nadsyłane przez serwer czy własne nadpisania klienta.

2. **Preset Wyglądu**:
   - Wybór stylu: `VANILLA`, `CLASSIC`, `MODERN`, `MINIMAL`, `CUSTOM`.

3. **Styl Tytułu Głównego (Title Styling)**:
   - `title_scale`: Skala powiększenia czcionki tytułu (1.0x - 6.0x, domyślnie 3.0x).
   - `title_color`: Kolor tekstu (ARGB).
   - `title_alignment`: Wyrównanie (`CENTER`, `LEFT`, `RIGHT`).
   - `title_pos_y`: Przesunięcie pionowe względem środka ekranu (-150 do +150 px).
   - `title_shadow`: Cień pod tekstem tytułu.
   - `title_background`: Rysowanie subtelnej karty tła za tytułem.

4. **Styl Podtytułu (Subtitle Styling)**:
   - `subtitle_scale`: Skala czcionki podtytułu (0.8x - 3.0x).
   - `subtitle_color`: Kolor tekstu podtytułu.
   - `subtitle_spacing`: Odstęp pionowy pomiędzy tytułem a podtytułem.
   - `subtitle_shadow`: Cień pod tekstem.

5. **Styl Action Bar (Action Bar Styling)**:
   - `action_bar_scale`: Skala czcionki (0.8x - 2.5x).
   - `action_bar_color`: Kolor tekstu komunikatu Action Bar.
   - `action_bar_pos_y`: Pozycja pionowa nad paskiem podręcznym.
   - `action_bar_background`: Tło komunikatów Action Bar.
   - `action_bar_shadow`: Cień pod tekstem.

6. **Animacje i Przejścia**:
   - `animation_style`: Styl animacji pojawiania się i znikania (`FADE`, `SLIDE`, `SCALE`, `STATIC`, `CUSTOM`).

7. **Czas Trwania i Nadpisywanie Czasów (Timings & Overrides)**:
   - `override_fade_in`: Czas trwania animacji wejścia (w tickach).
   - `override_stay`: Czas trwania widoczności (w tickach).
   - `override_fade_out`: Czas trwania animacji wyjścia (w tickach).
   - `override_action_bar_stay`: Niezależny czas trwania widoczności Action Bara.

8. **Widoczność i Podgląd (Visibility & Preview)**:
   - `hide_in_gui`: Ukrywanie napisów przy otwartych ekranach ekwipunku/menu.
   - `hide_in_spectator`: Ukrywanie w trybie obserwatora.
   - `preview_mode`: Tryb podglądu w menu Right Shift.

## 2. Niezależność Action Bara

Action Bar posiada w 100% odrębny cykl życia (`abAgeTicks`, `abStayTicks`) od Title i Subtitle.
Wyczyszczenie tytułów przez pakiet serwera (`clearTitles`) **nie powoduje** przedwczesnego zniknięcia aktywnego komunikatu Action Bar.
