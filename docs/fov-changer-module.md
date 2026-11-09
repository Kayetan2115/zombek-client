# Moduł FOV Changer (`FovChangerModule`) — Dokumentacja Techniczna

## 1. Przegląd Modułu

Moduł **FOV Changer** (`pl.zombek.client.core.modules.visual.FovChangerModule`) umożliwia pełną kontrolę nad kątem widzenia (Field of View) kamery gracza, dynamicznymi efektami ruchu oraz płynnymi przejściami między stanami widoku.

Zarejestrowany w kategorii `ModuleCategory.VISUAL` pod identyfikatorem `"fov_changer"`.
Domyślny klawisz skrótu: brak przypisania (`GLFW_KEY_UNKNOWN`), zapobiega konfliktom ze standardowymi klawiszami.

---

## 2. Profile FOV (`FovPreset`)

Moduł oferuje gotowe profile pola widzenia dopasowane do preferencji graczy:
- **`DEFAULT`** — 70.0° (standardowa wartość Vanilla Minecraft).
- **`LOW`** — 60.0° (węższe pole widzenia dla precyzyjnego celowania).
- **`MEDIUM`** — 80.0° (optymalne pole widzenia dla rozgrywki survival).
- **`HIGH`** — 90.0° (szerokie pole widzenia popularne w dynamicznej rozgrywce).
- **`PRO_PVP`** — 100.0° (rekomendowane ustawienie turniejowe PvP).
- **`QUAKE_PRO`** — 110.0° (maksymalne pole widzenia z Vanilla).
- **`CUSTOM`** — niestandardowe pole widzenia w bezpiecznym zakresie 30.0° – 150.0°.

---

## 3. Dynamiczne Mnożniki Ruchu (Dynamic FOV)

W standardowym Minecraft dynamiczne zmiany FOV (sprint, naciąganie łuku, pływanie) bywają uciążliwe lub gwałtowne. Moduł oferuje precyzyjną kontrolę:
- `preserve_vanilla_dynamic` — zachowanie natywnych mnożników gry.
- `disable_dynamic_fov` — całkowite wyłączenie zmian FOV podczas ruchu i korzystania z przedmiotów (stabilna perspektywa).
- **Dostosowywane mnożniki własne:**
  - `custom_sprint_multiplier` (domyślnie 1.15)
  - `custom_movement_multiplier` (domyślnie 1.05)
  - `custom_item_use_multiplier` (domyślnie 0.85, np. naciąganie łuku)

---

## 4. Płynne Animacje (`FovEasing`)

Przejścia pomiędzy wartościami docelowymi FOV są animowane z wykorzystaniem krzywych interpolacji:
- `LINEAR` — przejście liniowe.
- `EASE_IN` — łagodny start.
- `EASE_OUT` — gwałtowny start, łagodne domknięcie.
- `EASE_IN_OUT` — zbalansowane przyspieszenie i wyhamowanie (domyślne).
- `instant_transitions` — natychmiastowe zmiany bez opóźnień animacji.

---

## 5. Współpraca z Modułem Zoom i Priorytet Projekcji

Architektura `CameraFovController` gwarantuje pełną i bezbłędną koordynację z modułem `Zoom`:
1. Baza: Pobierana jest bazowa wartość pola widzenia (wartość Vanilla lub niestandardowa z `FovChangerModule`).
2. Mnożnik dynamiczny: Aplikowany jest dynamiczny mnożnik ruchu / łuku.
3. Zoom: Jeżeli aktywny jest `ZoomController`, aktualny FOV jest dzielony przez współczynnik przybliżenia:
   $$\text{FOV}_{\text{computed}} = \frac{\text{FOV}_{\text{base}} \cdot \text{DynamicMultiplier}}{\text{ZoomFactor}}$$
4. Bezpieczny zacisk: Wynikowy kąt jest zawsze rzutowany na bezpieczny zakres geometryczny $[5.0^\circ, 170.0^\circ]$, eliminując błędy odwrócenia perspektywy lub dzielenia przez zero.
5. Zmiana ustawień FOV gracza w trakcie trwania przybliżenia nie resetuje zoomu, lecz zostaje natychmiast uwzględniona w bieżącym rzutowaniu.
