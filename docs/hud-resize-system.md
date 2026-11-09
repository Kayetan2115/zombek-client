# System Zmiany Rozmiaru Komponentów (Resize System) — ZombekClient (Etap 9B)

## 1. Wprowadzenie i Cel Modułu

System zmiany rozmiaru komponentów (`HudResizeController`) umożliwia interaktywne dostosowywanie szerokości i wysokości elementów HUD bezpośrednio na kanwie edytora.
Rozwiązanie zapewnia precyzyjną kontrolę geometrii z poszanowaniem ograniczeń deskryptorów modułów oraz proporcji wymiarów.

---

## 2. 8 Uchwytów Zmiany Rozmiaru (8-Handle Box)

Gdy komponent HUD zostanie zaznaczony, wokół jego obramowania rysowanych jest 8 kwadratowych uchwytów (handles) o domyślnym rozmiarze 6x6 px:

```
  TL --------- TC --------- TR
   |                         |
  ML       KOMPONENT         MR
   |                         |
  BL --------- BC --------- BR
```

| Uchwyt | Pozycja | Kursor | Działanie |
| :--- | :--- | :--- | :--- |
| **TL** | Top-Left | `RESIZE_NWSE` | Zmiana X, Y, szerokości i wysokości (nieruchomy dolny prawy róg) |
| **TC** | Top-Center | `RESIZE_NS` | Zmiana Y i wysokości (nieruchoma dolna krawędź) |
| **TR** | Top-Right | `RESIZE_NESW` | Zmiana Y, szerokości i wysokości (nieruchomy dolny lewy róg) |
| **ML** | Middle-Left | `RESIZE_EW` | Zmiana X i szerokości (nieruchoma prawa krawędź) |
| **MR** | Middle-Right | `RESIZE_EW` | Zmiana szerokości (nieruchoma lewa krawędź) |
| **BL** | Bottom-Left | `RESIZE_NESW` | Zmiana X, szerokości i wysokości (nieruchomy górny prawy róg) |
| **BC** | Bottom-Center | `RESIZE_NS` | Zmiana wysokości (nieruchoma górna krawędź) |
| **BR** | Bottom-Right | `RESIZE_NWSE` | Zmiana szerokości i wysokości (nieruchomy górny lewy róg) |

---

## 3. Zasada Nieruchomej Przeciwległej Krawędzi (Stationary Opposite Edge)

Podczas manipulacji danym uchwytem, krawędź lub wierzchołek leżący naprzeciwko pozostaje idealnie nieruchomy na ekranie.
Przykład dla uchwytu lewego (`ML`):
- Pozycja prawej krawędzi: $X_{right} = X_{initial} + W_{initial}$
- Nowa szerokość: $W_{new} = \max(W_{min}, X_{right} - X_{mouse})$
- Nowa pozycja lewej krawędzi: $X_{new} = X_{right} - W_{new}$
- Nowy offset w kotwicy: przeliczany za pomocą `HudAnchor.screenXToOffsetX(...)`.

Dzięki tej zasadzie zmiana rozmiaru nie powoduje niepożądanego przesunięcia całego komponentu.

---

## 4. Blokada Proporcji (Aspect Ratio Lock)

Gdy włączona jest opcja `lockAspectRatio` lub użytkownik przytrzymuje klawisz `SHIFT`:
1. System zapamiętuje współczynnik proporcji z początku operacji: $ratio = \frac{W_{initial}}{H_{initial}}$.
2. Podczas zmiany szerokości, wysokość jest automatycznie wyliczana jako $H = \mathrm{round}\left(\frac{W}{ratio}\right)$.
3. Zabezpiecza to elementy graficzne (np. ikony pancerza, wykresy, tarcze) przed zniekształceniami perspektywicznymi.

---

## 5. Ograniczenia i Auto Size

- Każdy deskryptor komponentu (`HudComponentDescriptor`) definiuje wartości `minWidth`, `minHeight`, `maxWidth`, `maxHeight`.
- Jeśli użytkownik ręcznie zmieni rozmiar elementu posiadającego `autoSize == true`, system automatycznie przełącza tryb na `autoSize = false`, respektując intencję gracza.
