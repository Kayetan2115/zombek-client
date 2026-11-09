# Centralny Kontroler Kamery (`ZombekCameraController`) — Dokumentacja Techniczna

## 1. Cel i Odpowiedzialność

Klasa `pl.zombek.client.core.camera.ZombekCameraController` jest centralnym koordynatorem wszystkich modyfikacji kamery, perspektywy i efektów wizualnych w ZombekClient.

Zapewnia spójność między niezależnymi modułami wizualnymi:
1. `ZoomController` (moduł Zoom).
2. `CameraFovController` (moduł FOV Changer).
3. `MotionBlurController` (moduł Motion Blur).
4. `HurtCamController` (moduł Hurt Cam).
5. `FreelookCameraController` (moduł Freelook z Etapu 8A).
6. `SnaplookController` (moduł Snaplook z Etapu 8A).

---

## 2. Model Stanu Kamery (`CameraState`)

Kontroler zarządza ujednoliconym obiektem stanu kamery zawierającym:
- `baseFov`, `currentFov`, `targetFov`.
- `zoomActive`, `zoomFactor`.
- `yaw`, `pitch`, `roll`.
- `freelookActive`, `snaplookActive`.
- `hurtCamActive`, `hurtTiltMultiplier`.
- `motionBlurIntensity`, `motionBlurActive`.
- `partialTicks`, `timestampNanos`.

---

## 3. Cykl Życia i Reset Bezpieczeństwa (Lifecycle Guards)

W celu zapewnienia niezawodności i ochrony przed zablokowaniem kamery w nieprawidłowej perspektywie, `ZombekCameraController` automatycznie przechwytuje kluczowe zdarzenia silnika gry:

- **Utrata fokusu okna (`onFocusLost`):**
  - Wyłącza aktywne tryby `HOLD` dla Zoom, Freelook i Snaplook.
- **Otwarcie interfejsu GUI (`onGuiOpened`):**
  - Przywraca standardową kontrolę kursora, resetuje zoom lub wstrzymuje manipulacje kątami widoku.
- **Śmierć gracza i odrodzenie (`onPlayerDeath`, `onPlayerRespawn`):**
  - Natychmiastowy twardy reset pozycji kamery do standardowych wartości początkowych.
- **Zmiana wymiaru i zmiana serwera (`onDimensionChange`, `onWorldUnload`):**
  - Czyszczenie historii próbek rozmycia ruchu (Motion Blur) oraz wygaszenie animacji interpolacyjnych.
- **Zamknięcie klienta (`shutdown`):**
  - Kompletne zwolnienie zasobów, czyszczenie referencji statycznych i zatrzymanie timerów.

---

## 4. Wieloplatformowość (Fabric, Quilt, Forge, NeoForge)

`ZombekCameraController` jest zaimplementowany w warstwie `core`, dzięki czemu zachowuje 100% niezależności od silnika modloadera:
- Na loaderach **Fabric i Quilt** kontroler jest wywoływany poprzez zunifikowane punkty wstrzyknięć Mixin (`GameRendererMixin`, `MouseMixin`, `InGameHudMixin`).
- Na loaderach **Forge i NeoForge** kontroler jest zasilany przez dedykowane zdarzenia magistrali zdarzeń klienta (`ViewportEvent.ComputeFov`, `ViewportEvent.ComputeCameraAngles`, `InputEvent.MouseScrollingEvent`, `RenderGuiEvent`).
