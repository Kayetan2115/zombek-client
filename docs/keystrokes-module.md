# Keystrokes Module — Specyfikacja Techniczna i Architektura

## 1. Cel i Odpowiedzialność

Moduł `KeystrokesModule` (`pl.zombek.client.core.modules.pvp.KeystrokesModule`) odpowiada za wizualną prezentację klawiszy poruszania się oraz przycisków myszy w czasie rzeczywistym na ekranie Minecrafta (HUD).

Implementacja jest w 100% zintegrowana ze wspólnym rdzeniem `ZombekClient Core`, korzysta z `ModuleManager`, frameworku typowanych ustawień (`SettingGroup`, `BooleanSetting`, `ColorSetting`, `EnumSetting`, `NumberSetting`), systemu serializacji `modules.json` oraz z interfejsu renderowania `IHudElement` / `IHudRenderer`.

---

## 2. Obsługiwane Klawisze i Przyciski

Moduł obsługuje niezależną konfigurację widoczności dla każdego klawisza:
- **W, A, S, D** — klawisze poruszania się (domyślnie włączone);
- **LMB (Left Mouse Button)** — lewy przycisk myszy z opcjonalnym wyświetlaniem CPS;
- **RMB (Right Mouse Button)** — prawy przycisk myszy z opcjonalnym wyświetlaniem CPS;
- **SPACE** — spacja z czytelnym paskiem lub etykietą;
- **LSHIFT** — skradanie (domyślnie wyłączone, konfigurowalne);
- **LCTRL** — sprint (domyślnie wyłączone, konfigurowalne).

---

## 3. Typowane Ustawienia (Settings)

### 3.1 Grupa: Ogólne (`general`)
- `show_w` (Boolean, domyślnie `true`) — widoczność klawisza W;
- `show_a` (Boolean, domyślnie `true`) — widoczność klawisza A;
- `show_s` (Boolean, domyślnie `true`) — widoczność klawisza S;
- `show_d` (Boolean, domyślnie `true`) — widoczność klawisza D;
- `show_lmb` (Boolean, domyślnie `true`) — widoczność LPM;
- `show_rmb` (Boolean, domyślnie `true`) — widoczność RPM;
- `show_space` (Boolean, domyślnie `true`) — widoczność spacji;
- `show_shift` (Boolean, domyślnie `false`) — widoczność klawisza Shift;
- `show_ctrl` (Boolean, domyślnie `false`) — widoczność klawisza Ctrl;
- `show_mouse_cps` (Boolean, domyślnie `true`) — wyświetlanie wartości CPS wewnątrz przycisków myszy.

### 3.2 Grupa: Układ (`layout`)
- `anchor` (Enum: `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`; domyślnie `TOP_LEFT`);
- `x_offset` (Number: 0–1000, krok 1; domyślnie `10`);
- `y_offset` (Number: 0–1000, krok 1; domyślnie `10`);
- `key_size` (Number: 16–48, krok 1; domyślnie `24`);
- `key_gap` (Number: 1–8, krok 1; domyślnie `2`);
- `space_height` (Number: 8–24, krok 1; domyślnie `14`).

### 3.3 Grupa: Kolory (`colors`)
- `bg_color` (Color ARGB, domyślnie `0x80000000` — półprzezroczysty czarny);
- `pressed_bg_color` (Color ARGB, domyślnie `0xCCFFFFFF` — jasny biały po wciśnięciu);
- `text_color` (Color ARGB, domyślnie `0xFFFFFFFF` — biały tekst);
- `pressed_text_color` (Color ARGB, domyślnie `0xFF000000` — czarny tekst po wciśnięciu);
- `border_color` (Color ARGB, domyślnie `0x40FFFFFF` — subtelna ramka).

### 3.4 Grupa: Wygląd (`appearance`)
- `draw_border` (Boolean, domyślnie `true`);
- `border_thickness` (Number: 1–4, domyślnie `1`);
- `text_scale` (Number: 0.5–2.0, krok 0.1; domyślnie `1.0`);
- `text_shadow` (Boolean, domyślnie `true`).

---

## 4. Matematyka Pozycjonowania i Anchor

Moduł implementuje `IHudElement`. Współrzędne bazowe obliczane są za pośrednictwem enum `HudAnchor`:
```java
int totalWidth = keySize * 3 + keyGap * 2;
int baseX = anchor.calculateX(xOffset, totalWidth, renderer.getScreenWidth());
int baseY = anchor.calculateY(yOffset, totalHeight, renderer.getScreenHeight());
```
W przypadku zmiany rozdzielczości okna Minecrafta elementy zakotwiczone w rogach (`TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`) zachowują stały odstęp od krawędzi bez zniekształceń.

---

## 5. Bezpieczeństwo i Niewykrywalność

- Moduł jest w 100% po stronie klienta (Client-side Visual HUD).
- Nie modyfikuje ruchu gracza, pakietów sieciowych ani zachowania fizyki gry.
- W momencie otwarcia GUI (np. czat, ekwipunek, menu ZombekClient) stan wszystkich klawiszy jest bezpiecznie resetowany przez `KeyTracker.getInstance().resetAll()`, zapobiegając efektowi „zablokowanego klawisza”.
