# Moduł Motion Blur (`MotionBlurModule`) — Dokumentacja Techniczna

## 1. Przegląd Modułu

Moduł **Motion Blur** (`pl.zombek.client.core.modules.visual.MotionBlurModule`) odpowiada za kinowe rozmycie ruchu kamery przy szybkich obrotach głowy gracza w Minecraft 1.20.4.

Zarejestrowany w kategorii `ModuleCategory.VISUAL` pod identyfikatorem `"motion_blur"`.
Domyślny klawisz skrótu: brak przypisania (`GLFW_KEY_UNKNOWN`).

---

## 2. Architektura i Wykrywanie Ruchu (`MotionDetectionMode`)

Kontroler `MotionBlurController` analizuje rotację kamery lub delty myszy między klatkami renderowania:
1. **`CAMERA_ROTATION`** — kalkulacja różnicy kątów kamery ($\Delta\text{yaw}$, $\Delta\text{pitch}$) z poprawnym zawijaniem kątowym wokół $360^\circ$ (ang. *angular wrap-around*), zapobiegająca skokom rozmycia przy przechodzeniu przez $0^\circ / 360^\circ$.
2. **`MOUSE_DELTA`** — bezpośrednia analiza przesunięć kursora myszy z kontrolera wejścia.
3. **`COMPOSITE`** — hybrydowe łączenie delty obrotu kamery i wektora ruchu myszy.

---

## 3. Parametry Rozmycia i Jakość

- **Intensywność i Tłumienie:**
  - `blur_intensity` (0.0 – 1.0, domyślnie 0.5) — bazowa waga rozmycia.
  - `blur_strength` (0.1 – 2.0, domyślnie 1.0) — mnożnik siły efektu.
  - `max_blur` (0.05 – 0.95, domyślnie 0.8) — górny limit przezroczystości bufora akumulacji zapobiegający efektowi "oślepiacza".
  - `blur_decay` (0.1 – 1.0, domyślnie 0.5) — szybkość wygaszania rozmycia w spoczynku.
- **Czułość i Progi:**
  - `horizontal_sensitivity`, `vertical_sensitivity` — niezależna czułość osi X i Y.
  - `movement_threshold` — minimalna prędkość kątowa wymagana do aktywacji efektu (zapobiega rozmywaniu przy mikro-drganiach).
- **Profile Jakości (`MotionBlurQuality`):**
  - `LOW` (2 próbki bufora historii, najmniejsze zużycie GPU).
  - `MEDIUM` (4 próbki historii, zbalansowane wygładzanie).
  - `HIGH` (6 próbek historii, kinowy efekt).
  - `ULTRA` (8 próbek historii, maksymalna jakość post-processingu).

---

## 4. Zarządzanie Zasobami i Stabilność (Lifecycle Management)

1. **Adapter Renderera (`IMotionBlurRendererAdapter`):**
   - Warstwa abstrakcji pozwalająca loaderom (Fabric, Quilt, Forge, NeoForge) na bezpieczne podpięcie buforów ramki (FBO) i shaderów post-processingu.
2. **Bezpieczny Fallback:**
   - W przypadku braku wsparcia dla akceleracji shaderów FBO na danym sprzęcie lub sterowniku GPU, moduł automatycznie aktywuje tryb `Fallback Mode` (soft-pass / pasywny), nie dopuszczając do awarii gry (Crash-Proof).
3. **Automatyczne Czyszczenie Buforów:**
   - Przy zmianie rozdzielczości okna, przełączeniu w tryb pełnoekranowy, zmianie skali GUI lub utracie kontekstu OpenGL, bufory są natychmiast reallokowane bez wycieków pamięci VRAM.
   - Wejście do innego wymiaru, zmiana serwera lub śmierć gracza resetują historię próbek bufora, eliminując "duchy" (ghosting) z poprzedniego świata.
4. **Filtry Wyłączenia (Guards):**
   - Efekt jest automatycznie wygaszany w interfejsach GUI, ekwipunku, menu pauzy, edytorze HUD oraz w trybie obserwatora (Spectator).
