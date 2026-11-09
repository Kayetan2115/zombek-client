# CPS Counter Module — Specyfikacja Techniczna i Architektura

## 1. Cel i Założenia

Moduł `CPSModule` (`pl.zombek.client.core.modules.pvp.CPSModule`) odpowiada za precyzyjny pomiar i wyświetlanie liczby kliknięć na sekundę (Clicks Per Second) dla lewego (LMB) i prawego (RMB) przycisku myszy.

Moduł:
- Mierzy rzeczywiste kliknięcia gracza;
- **Nie zawiera żadnych mechanizmów autoclickera, makr ani manipulacji prędkością ataku**;
- Używa bezpiecznego zegara monotonicznego JVM (`System.nanoTime()`), co gwarantuje odporność na zmiany czasu systemowego i brak zakłóceń pomiędzy klatkami renderowania.

---

## 2. Architektura Pomiarowa (ClickTracker)

Mechanizm pomiarowy został odseparowany od warstwy renderowania i zrealizowany w klasie `pl.zombek.client.core.tracking.ClickTracker`:
- **Przesuwne okno czasowe (Sliding Window):** Domyślnie 1000 ms (konfigurowalne 500–2000 ms). Kliknięcia zapisywane są jako znaczniki czasowe nanosekund w strukturze `ConcurrentLinkedQueue<Long>`.
- **Ekspiracja próbki:** Przy każdym odczycie CPS (`getLeftCps()`, `getRightCps()`) znaczniki starsze niż `now - windowNanos` są usuwane z kolejki.
- **Wartość maksymalna (Peak CPS):** Śledzenie rekordowego CPS osiągniętego przez gracza w trakcie sesji z możliwością wyzerowania (`resetPeak()`).

```mermaid
flowchart LR
    A["Naciśnięcie LPM/RPM w grze"] --> B["ClickTracker.recordClick()"]
    B --> C["Kolejka FIFO (System.nanoTime)"]
    C --> D["Przesuwne okno 1000ms"]
    D --> E["CPSModule.render() na HUD"]
```

---

## 3. Typowane Ustawienia (Settings)

### 3.1 Grupa: Ogólne (`general`)
- `show_lmb` (Boolean, domyślnie `true`) — wyświetlanie licznika LPM;
- `show_rmb` (Boolean, domyślnie `true`) — wyświetlanie licznika RPM;
- `show_max` (Boolean, domyślnie `false`) — wyświetlanie maksymalnego zarejestrowanego CPS;
- `reset_max` (Boolean, domyślnie `false`) — przełącznik zerujący rekordy CPS.

### 3.2 Grupa: Wyświetlanie (`display`)
- `style` (Enum: `INLINE`, `STACKED`, `BOXED`; domyślnie `BOXED`);
- `prefix` (String, domyślnie `""`);
- `suffix` (String, domyślnie `" CPS"`);
- `show_labels` (Boolean, domyślnie `true` — wyświetla "LMB:" / "RMB:").

### 3.3 Grupa: Kolory (`colors`)
- `bg_color` (Color ARGB, domyślnie `0x80000000` — półprzezroczysty czarny);
- `text_color` (Color ARGB, domyślnie `0xFFFFFFFF` — biały tekst);
- `border_color` (Color ARGB, domyślnie `0x40FFFFFF` — subtelna ramka).

### 3.4 Grupa: Układ (`layout`)
- `anchor` (Enum: `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`; domyślnie `TOP_LEFT`);
- `x_offset` (Number: 0–1000, domyślnie `10`);
- `y_offset` (Number: 0–1000, domyślnie `90`);
- `padding` (Number: 2–12, domyślnie `4`);
- `text_scale` (Number: 0.5–2.0, domyślnie `1.0`);
- `draw_border` (Boolean, domyślnie `true`).

### 3.5 Grupa: Zaawansowane (`advanced`)
- `time_window_ms` (Number: 500–2000 ms, domyślnie `1000 ms`).

---

## 4. Style Prezentacji

1. **`BOXED`:** Licznik w eleganckim, wyśrodkowanym prostokątnym okienku z obramowaniem.
2. **`STACKED`:** Wartości LPM i RPM ułożone pionowo w dwóch linijkach.
3. **`INLINE`:** Wartości wyświetlane w jednej linii (np. `LMB: 12 | RMB: 8 CPS`).
