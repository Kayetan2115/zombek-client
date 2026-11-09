# ZombekClient — Reach Display Module

## 1. Wprowadzenie i Cel

Moduł **Reach Display** (`ReachDisplayModule`, ID: `reach_display`) w kategorii `Category.PVP` mierzy i wyświetla na ekranie rzeczywisty dystans ataku gracza. 

W przeciwieństwie do uproszczonych implementacji mierzących odległość środek-środek (center-to-center), ZombekClient oblicza precyzyjną odległość od pozycji oczu gracza (`Vec3 eyePos`) do **najbliższego punktu na prostopadłościanie ograniczającym cel (AABB — Axis-Aligned Bounding Box)**. Jest to standard kalkulacji zasięgu zgodny z fizyką silnika Minecraft.

---

## 2. Algorytm Obliczania Dystansu do AABB

Algorytm zaimplementowany w `ReachTracker.calculateDistanceToAABB(...)`:
1. Dana jest pozycja oczu atakującego $(x_e, y_e, z_e)$.
2. Dany jest prostopadłościan celu $[x_{min}, y_{min}, z_{min}] \times [x_{max}, y_{max}, z_{max}]$.
3. Obliczany jest punkt najbliższy na AABB poprzez rzutowanie (clamp):
   $$x_c = \max(x_{min}, \min(x_e, x_{max}))$$
   $$y_c = \max(y_{min}, \min(y_e, y_{max}))$$
   $$z_c = \max(z_{min}, \min(z_e, z_{max}))$$
4. Euklidesowa odległość od oczu gracza do punktu $(x_c, y_c, z_c)$:
   $$d = \sqrt{(x_e - x_c)^2 + (y_e - y_c)^2 + (z_e - z_c)^2}$$
5. Jeżeli oczy atakującego znajdują się wewnątrz bryły przeciwnika, dystans wynosi $0.00\text{ b}$.

---

## 3. Typy Zdarzeń i Historia

Śledzenie obsługiwane jest przez klasę `ReachTracker`:
- **`OBSERVED_HIT`**: Zaobserwowany pomyślny atak na byt (potwierdzony przez event ataku bądź wskaźnik namierzania).
- **`ATTACK_ATTEMPT`**: Próba ataku w pustkę (miss / kliknięcie lewym przyciskiem myszy bez trafienia).
- **`UNKNOWN_RESULT`**: Zdarzenie niejednoznaczne.

Historia przechowuje ograniczoną liczbę wpisów (`max_history_entries`, domyślnie 20).
Dostępne agregacje statystyczne:
- **Last Reach**: Ostatnio zmierzona wartość.
- **Average Reach**: Średnia arytmetyczna z zarejestrowanych trafień.
- **Max Reach / Min Reach**: Wartości ekstremalne z bieżącej sesji.
- **Target Distance**: Odległość do aktualnie namierzanego bytu w czasie rzeczywistym.

---

## 4. Opcje Konfiguracyjne Modułu

| Ustawienie | Klucz / ID | Typ | Wartość domyślna | Opcje | Opis |
|---|---|---|---|---|---|
| **Display Style** | `display_style` | Enum | `COMPACT` | `COMPACT`, `DETAILED`, `MINIMAL` | Styl formatowania widgetu HUD. |
| **Prefix** | `prefix` | String | `Reach: ` | Dowolny tekst | Tekst poprzedzający wartość. |
| **Suffix** | `suffix` | String | ` b` | Dowolny tekst | Jednostka wyświetlana za liczbą (np. ` b` / ` blocks`). |
| **Decimal Places** | `decimal_places` | Number | `2` | `1` – `4` | Dokładność miejsc po przecinku. |
| **Show Average** | `show_average` | Boolean | `false` | `true`, `false` | Wyświetlanie średniego zasięgu (styl Detailed). |
| **Show Max** | `show_max` | Boolean | `false` | `true`, `false` | Wyświetlanie maksymalnego zasięgu. |
| **Show Target Dist**| `show_target_dist`| Boolean | `false` | `true`, `false` | Wyświetlanie odległości do namierzonego celu. |
| **Max History** | `max_history` | Number | `20` | `5` – `100` | Rozmiar bufora historii pomiarów. |
| **Color** | `color` | Color | `#00FFAA` | Format ARGB/HEX | Kolor tekstu wartości. |
| **Background** | `background` | Boolean | `true` | `true`, `false` | Rysowanie tła panelu HUD. |
| **Padding** | `padding` | Number | `4` | `0` – `20` px | Wewnętrzny margines elementu HUD. |
| **X / Y Position** | `x` / `y` | Number | `10` / `160` | Dowolna | Pozycja widgetu na ekranie. |

---

## 5. Integracja HUD

Moduł implementuje interfejs `IHudElement` ze wspólnego modułu Core:
- Posiada metody `render(IHudRenderer)`, `getWidth()`, `getHeight()`, `isDraggable()`.
- W stanie spoczynku (brak zarejestrowanych ataków) wyświetla wartość zastępczą `0.00 b` lub stan oczekiwania.
- Dynamicznie adaptuje rozmiar ramki do wybranego stylu (`COMPACT` vs `DETAILED`).
