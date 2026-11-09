# Moduł Full Bright (FullBrightModule)

## 1. Cel i Bezpieczeństwo
Moduł `FullBrightModule` odpowiada za bezpieczne, estetyczne i w pełni konfigurowalne rozjaśnienie otoczenia w ciemnych lokacjach (jaskinie, noc, kopalnie).

### Bezpieczeństwo i Integralność Gry
- **Brak hackowania gamma poza zakres w pliku konfiguracyjnym:** Waniliowy plik `options.txt` nie jest nadpisywany nieprawidłowymi wartościami (np. `gamma: 1000.0`), które mogłyby uszkodzić profil gry gracza.
- **Brak ingerencji w pakiety i mechaniki gry:** Poziom światła po stronie serwera pozostaje niezmieniony. Spawnowanie mobów, efekty widzenia w ciemności i pakiety sieciowe są w 100% nienaruszone.
- **Płynna animacja przejścia:** Zamiast nagłego błysku po włączeniu modułu, jasność płynnie interpoluje z użyciem krzywej Ease-In-Out.

## 2. Dostępne Tryby Pracy (FullBrightMode)
1. `VANILLA` — standardowe, waniliowe renderowanie światła gry.
2. `FULL_BRIGHT` — maksymalne, równomierne rozjaśnienie sceny (poziom światła 15.0).
3. `CUSTOM` — precyzyjnie definiowany przez gracza poziom jasności (od 0.0 do 15.0).
4. `NIGHT_VISION_STYLE` — charakterystyczna, klimatyczna stylizacja przypominająca miksturę Night Vision (poziom 12.0) z zachowaniem głębi cieni.

## 3. Parametry Konfiguracyjne
- `fullbright_mode` — wybór jednego z powyższych 4 trybów pracy.
- `custom_brightness` (0.0 - 15.0, Domyślnie: `10.0`) — suwak jasności dla trybu `CUSTOM`.
- `brightness_strength` (0.1 - 2.0, Domyślnie: `1.0`) — ogólny mnożnik siły efektu.
- `preserve_colors` (Domyślnie: `true`) — zachowanie nasycenia barw i zapobieganie zjawisku wyblakłego obrazu.
- `preserve_lightmap` (Domyślnie: `true`) — zachowanie subtelnej charakterystyki tekstury lightmapy.
- `color_compensation` (Domyślnie: `true`) — kompensacja chromatyczna przy wysokiej luminancji.
- `reduce_washed_out` (Domyślnie: `true`) — nieliniowa korekta krzywej gamma:
$$\gamma_{eff} = \left(\frac{I_{boost}}{15.0}\right)^{0.85}$$
- `shadow_preservation` (Domyślnie: `true`) — zachowanie delikatnego cieniowania w skrajnym mroku (clamp do 14.0) zapobiegające spłaszczeniu geometrii trójwymiarowej terenu.
- `smooth_brightness` (Domyślnie: `true`) — płynna interpolacja przejścia.
- `transition_duration_ms` (10 - 2000 ms, Domyślnie: `200 ms`) — czas trwania animacji rozjaśniania i ściemniania.

## 4. Punkty Styku w Rendererze
Rozjaśnienie jest wprowadzane w shaderze mapy światła (`LightmapTextureManagerMixin` na loaderach Fabric/Quilt oraz eventach Forge/NeoForge). Metoda `FullBrightController.getInstance().computeEffectiveGamma(vanillaGamma)` przekazuje wyliczoną wartość w locie bez trwałej modyfikacji ustawień klienta.
