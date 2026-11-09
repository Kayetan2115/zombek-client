# Moduł Hurt Cam (`HurtCamModule`) — Dokumentacja Techniczna

## 1. Przegląd Modułu

Moduł **Hurt Cam** (`pl.zombek.client.core.modules.visual.HurtCamModule`) umożliwia pełne dostosowanie lub wyłączenie efektu wstrząsu i przechyłu kamery (Camera Tilt / Camera Shake) występującego w momencie otrzymania obrażeń przez postać gracza w Minecraft 1.20.4.

Zarejestrowany w kategorii `ModuleCategory.VISUAL` pod identyfikatorem `"hurt_cam"`.
Domyślny klawisz skrótu: brak przypisania (`GLFW_KEY_UNKNOWN`).

---

## 2. Tryby Pracy (`HurtCamMode`)

1. **`VANILLA`** — standardowy, domyślny przechył kamery znany z czystej gry.
2. **`DISABLED`** — całkowite anulowanie przechyłu kamery (`tiltViewWhenHurt` pomijany, roll kątowy zredukowany do $0^\circ$). Bardzo ceniony w trybach PvP, pozwala zachować stabilne celowanie podczas intensywnej walki.
3. **`REDUCED`** — zredukowana siła efektu do 50% wartości Vanilla, zapewniająca subtelną informację o uderzeniu bez utraty kontroli nad celownikiem.
4. **`CUSTOM`** — w pełni konfigurowalne parametry fizyki wstrząsu kamery.

---

## 3. Parametry Trybu Niestandardowego i Tłumienie (`HurtCamEasing`)

W trybie `CUSTOM` użytkownik posiada dostęp do zaawansowanych parametrów:
- `camera_tilt_multiplier` (0.0 – 2.0, domyślnie 0.5) — skala kątowego przechyłu kamery.
- `camera_shake` (0.0 – 1.0, domyślnie 0.25) — losowe drżenie kamery przy potężnych uderzeniach.
- `horizontal_offset`, `vertical_offset` — odchylenia projekcji w płaszczyznach X i Y.
- `recovery_duration_ms` — czas trwania powrotu kamery do pionu (100–1000 ms, domyślnie 250 ms).
- **Krzywe Tłumienia Ruchu:**
  - `SPRING` — sprężyste wytłumienie fizyczne (delikatne odbicie).
  - `EASE_OUT` — gwałtowne uderzenie z płynnym powrotem do równowagi.
  - `DAMPED` — silnie wytłumiony oscylator harmoniczny (domyślne).
  - `LINEAR` — stały, równomierny powrót.

---

## 4. Architektura i Pasywność (Fair Play)

1. **Efekt w 100% lokalny:**
   - Modyfikacja przechyłu kamery odbywa się wyłącznie w procedurze renderingu świata klienta (`GameRenderer.tiltViewWhenHurt` lub `ViewportEvent.ComputeCameraAngles`).
   - Moduł **nie modyfikuje** punktów zdrowia gracza, redukcji obrażeń, czasu nietykalności po uderzeniu (Hurt Resistance Time) ani wektora odrzutu (Knockback).
2. **Pełna spójność ze zdarzeniami PvP:**
   - Anulowanie efektu wizualnego kamery nie blokuje rejestracji uderzenia w systemie statystyk walki — moduły `PvPInfoModule`, `ComboTracker` oraz `HitColorTracker` nadal prawidłowo odnotowują trafienia i obrażenia.
