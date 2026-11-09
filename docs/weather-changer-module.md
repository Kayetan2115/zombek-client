# Moduł Zmiany Pogody (WeatherChangerModule)

## 1. Cel i Architektura
`WeatherChangerModule` odpowiada za lokalną, czysto kliencką modyfikację warunków atmosferycznych (deszcz, śnieg, burza, chmury). Pozwala graczom na pełne wyczyszczenie pogody podczas intensywnej rozgrywki PvP (brak uciążliwego szumu deszczu i kropel ograniczających widoczność) lub stworzenie klimatycznej aury deszczowej/burzowej według własnych upodobań.

Moduł nie wykonuje żadnych poleceń serwerowych (`/weather clear`) i nie wysyła pakietów modyfikujących pogodę. Zmiana dotyczy wyłącznie lokalnych gradientów renderowania nieba, chmur i cząsteczek.

## 2. Presety Pogodowe (WeatherPreset)
1. `VANILLA` — wierne odzwierciedlenie rzeczywistego stanu atmosfery serwera.
2. `CLEAR` — idealnie bezchmurne, czyste niebo bez opadów (`rainGradient = 0.0`, `thunderGradient = 0.0`).
3. `RAIN` — deszcz o zadanej intensywności (`rainGradient = rainIntensity`, `thunderGradient = 0.0`).
4. `SNOW` — opady śniegu w chłodnych biomach.
5. `THUNDER` — potężna burza z piorunami i ciemnym niebem (`rainGradient = rainIntensity`, `thunderGradient = thunderIntensity`).
6. `CUSTOM` — precyzyjnie konfigurowane gradienty opadów, chmur i wyładowań.

## 3. Płynne Przejścia i Wygładzanie (WeatherTransitionManager)
Nagła zmiana pogody w waniliowym silniku może wywoływać nieprzyjemne skoki oświetlenia. `WeatherTransitionManager` realizuje płynne rozjaśnianie i ściemnianie gradientu deszczu oraz burzy w zadanym czasie trwania (domyślnie 800 ms), eliminując skokowe przełączanie tekstur.

## 4. Opcje Personalizacji i Wydajność
- **Deszcz (Rain Settings):**
  - `enable_rain` (Domyślnie: `true`)
  - `rain_intensity` (0.0 - 1.0, Domyślnie: `1.0`)
  - `rain_speed` (0.1 - 3.0, Domyślnie: `1.0`)
  - `rain_opacity` (0.0 - 1.0, Domyślnie: `1.0`)
- **Śnieg (Snow Settings):**
  - `enable_snow` (Domyślnie: `true`)
  - `snow_intensity` (0.0 - 1.0, Domyślnie: `1.0`)
  - `snow_size` (0.5 - 2.0, Domyślnie: `1.0`)
  - `snow_opacity` (0.0 - 1.0, Domyślnie: `1.0`)
- **Chmury (Cloud Settings):**
  - `show_clouds` (Domyślnie: `true`)
  - `cloud_density` (0.0 - 2.0, Domyślnie: `1.0`)
  - `cloud_opacity` (0.0 - 1.0, Domyślnie: `1.0`)
  - `cloud_speed` (0.0 - 5.0, Domyślnie: `1.0`)
- **Pioruny i Błyski (Thunder & Flash):**
  - `visual_thunder` (Domyślnie: `false`)
  - `thunder_intensity` (0.0 - 1.0, Domyślnie: `0.8`)
  - `flash_intensity` (0.0 - 1.0, Domyślnie: `0.5`)
- **Wydajność (Performance Limits):**
  - `max_weather_particles` (50 - 2000, Domyślnie: `500`) — twardy limit cząsteczek deszczu/śniegu chroniący wydajność słabszych GPU.
- **Klimat Biomów (Climate Respect):**
  - `respect_biome_climate` (Domyślnie: `true`) — zachowanie natury biomów bezdeszczowych (pustynie, sawanny, badlands).
  - `override_biome_restrictions` (Domyślnie: `false`) — wymuszenie opadów nawet w strefach suchych na życzenie użytkownika.
