# ZombekClient — Multi-Component Selection

## 1. Wprowadzenie

Moduł **Multi-Component Selection** (`HudMultiSelectionManager`) umożliwia jednoczesne zaznaczanie, przesuwanie, skalowanie oraz grupowanie wielu elementów HUD na obszarze roboczym `HudEditorCanvas`. Jest to kluczowa funkcja profesjonalnego edytora interfejsu, eliminująca konieczność żmudnego przestawiania każdego wskaźnika pojedynczo.

---

## 2. Metody Zaznaczania

Edytor wspiera trzy główne techniki selekcji:

1. **Pojedyncze kliknięcie (Single Click):**
   - Kliknięcie lewym przyciskiem myszy (LPM) na komponent zaznacza go i odznacza wszystkie pozostałe.
   - Kliknięcie w puste tło odznacza wszystkie elementy.

2. **Modyfikatory klawiaturowe (Ctrl / Shift + LPM):**
   - **Ctrl + Klik:** Przełącza stan zaznaczenia klikniętego komponentu (Toggle Selection) — dodaje do istniejącej grupy zaznaczenia lub z niej usuwa bez resetowania reszty.
   - **Shift + Klik:** Rozszerza zaznaczenie (Range / Additive Selection) o kliknięty element.

3. **Zaznaczanie ramką (Marquee / Box Selection):**
   - Przeciągnięcie myszą z wciśniętym lewym przyciskiem po pustym obszarze kanwy rysuje półprzezroczystą ramkę zaznaczenia.
   - Wspierane są dwa tryby detekcji kolizji z ramką:
     - `INTERSECTING` (domyślny) — zaznacza każdy komponent, którego obwiednia chociażby częściowo przecina obszar ramki.
     - `FULLY_INSIDE` — wymaga, aby cały komponent znajdował się wewnątrz narysowanej ramki.

---

## 3. Skróty Globalne Selekcji

- **Ctrl + A:** Zaznacza wszystkie widoczne i niezablokowane komponenty w aktywnym układzie (`selectAll()`).
- **Esc:** Czyści całe zaznaczenie (`clearSelection()`). Jeśli aktywna była ramka, anuluje jej rysowanie.

---

## 4. Operacje na Wielokrotnym Zaznaczeniu

Gdy zaznaczonych jest co najmniej dwa elementy:
- **Wspólne przemieszczanie (Multi-Drag):** Przeciąganie dowolnego ze zaznaczonych komponentów przesuwa całą grupę o ten sam wektor `(deltaX, deltaY)`, zachowując wzajemne odległości i relacje przestrzenne.
- **Wspólne skalowanie (Multi-Scale):** Zmiana suwaka skali w Inspektorze skaluje proporcjonalnie wszystkie zaznaczone komponenty.
- **Wspólne usuwanie / ukrywanie:** Klawisz `Delete` ukrywa wszystkie zaznaczone elementy za pomocą pojedynczego polecenia `BatchCommand`.
- **Wspólne wyrównywanie:** Dostęp do paska narzędzi Alignment Tools (wyrównanie do lewej, prawej, góry, dołu, środka).

---

## 5. Inspektor Wielokrotnego Zaznaczenia (Mixed Values)

Gdy użytkownik zaznaczy kilka komponentów o różnych właściwościach (np. różne warstwy lub różne kotwice):
- Inspektor wyświetla nagłówek: `Zaznaczono: X komponentów`.
- Pola o zgodnych wartościach prezentują rzeczywistą wartość.
- Pola o rozbieżnych wartościach prezentują neutralną etykietę `<różne>`.
- Wybór nowej wartości w takim polu (np. zmiana warstwy na `Information`) natychmiast ujednolica tę właściwość dla wszystkich zaznaczonych obiektów.
