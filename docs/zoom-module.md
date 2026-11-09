# Moduł Zoom (`ZoomModule`) — Dokumentacja Techniczna

## 1. Przegląd Modułu

Moduł **Zoom** (`pl.zombek.client.core.modules.visual.ZoomModule`) to zaawansowany system przybliżania widoku (kamery) w Minecraft 1.20.4, działający w 100% po stronie klienta (Client-Side Only).

Zarejestrowany w kategorii `ModuleCategory.VISUAL` pod identyfikatorem `"zoom"`.
Domyślny klawisz aktywacji to **Z** (`GLFW_KEY_Z`), który jest w pełni zweryfikowany jako wolny od domyślnych konfliktów w czystym Minecraft.

---

## 2. Tryby Aktywacji (`ZoomActivationMode`)

1. **`HOLD` (Przytrzymanie — Domyślny):**
   - Przybliżenie jest aktywne tylko tak długo, jak długo użytkownik trzyma wciśnięty klawisz Zoom.
   - Puszczenie klawisza natychmiast rozpoczyna płynny powrót kamery do pierwotnego pola widzenia (FOV).
2. **`TOGGLE` (Przełącznik):**
   - Jednokrotne naciśnięcie klawisza włącza stan przybliżenia; ponowne naciśnięcie go wyłącza.
3. **`TOGGLE_HOLD` (Hybrydowy):**
   - Krótkie kliknięcie (< 300 ms) działa jak `TOGGLE`.
   - Dłuższe przytrzymanie (> 300 ms) automatycznie przełącza zachowanie na `HOLD` (wyłącza przybliżenie po zwolnieniu klawisza).

---

## 3. Płynne Animacje i Easing (`ZoomEasing`)

Moduł interpoluje współczynnik przybliżenia w czasie rzeczywistym w oparciu o czas delta (Time-based interpolation, niezależna od liczby FPS):
- `LINEAR` — stała prędkość przybliżania i oddalania.
- `EASE_IN` — łagodny początek przejścia.
- `EASE_OUT` — gwałtowny start z płynnym wyhamowaniem.
- `EASE_IN_OUT` — płynny początek i płynne wyhamowanie (domyślne).
- `EXPONENTIAL` — wykładnicza krzywa zbliżenia.

Dostępne parametry konfiguracyjne:
- `transition_duration_ms` — czas trwania animacji w milisekundach (50–500 ms, domyślnie 150 ms).
- `instant_zoom` — przełącznik pomijający animację (natychmiastowe przybliżenie w 1 klatce).

---

## 4. Scroll Zoom (Dynamiczne przybliżanie kółkiem myszy)

- W czasie gdy Zoom jest aktywny, obrót kółkiem myszy dynamicznie modyfikuje bieżący poziom przybliżenia.
- Zdarzenie obrotu kółkiem myszy jest przechwytywane i anulowane w silniku gry (`event.setCanceled(true)` / return w mixinie), co zapobiega niezamierzonej zmianie slotu w pasku szybkiego dostępu (Hotbar).
- Zabezpieczenie przed konfliktami: przewijanie myszy w GUI, menu pauzy czy ekwipunku nie wpływa na zoom.
- Parametry: `scroll_zoom_enabled`, `scroll_sensitivity`, `scroll_step`, `min_zoom` (1.1x–5.0x), `max_zoom` (5.0x–30.0x).
- Klawisz resetu poziomu zoomu przywraca domyślny współczynnik (domyślnie 4.0x).

---

## 5. Overlay i Wskaźnik HUD (`zoom_indicator`)

Moduł udostępnia wskaźnik HUD zarejestrowany w `HudComponentRegistry` pod ID `"zoom_indicator"`:
- Style wskaźnika (`ZoomIndicatorStyle`):
  - `BADGE` — elegancka plakietka z tłem.
  - `COMPACT` — minimalistyczny wskaźnik.
  - `TEXT_ONLY` — sam tekst bez ramki i tła.
- Informacja o poziomie: opcjonalne wyświetlanie dokładnego mnożnika (np. `ZOOM: 4.0x`).
- Pełna integracja z HUD Editorem (pozycjonowanie, skalowanie, kolory, widoczność).

---

## 6. Bezpieczeństwo i Zasady Fair Play

1. **Brak wpływu na zasięg ataku:** Kąty promienia ataku i zasięg uderzenia (Reach) nie ulegają powiększeniu.
2. **Brak symulacji ruchu gracza:** Kąty gracza wysyłane do serwera (`PlayerMoveC2SPacket`) nie są fałszowane.
3. **Czysto optyczny efekt:** Zmniejszenie FOV odbywa się wyłącznie w macierzy projekcji kamery klienta:
   $$\text{FOV}_{\text{final}} = \frac{\text{FOV}_{\text{dynamic}}}{\text{ZoomFactor}}$$
4. **Automatyczny Reset:** Po utracie fokusu okna, otwarciu menu gry, teleportacji, zmianie świata lub śmierci gracza, kontroler płynnie przywraca domyślny stan widoku.
