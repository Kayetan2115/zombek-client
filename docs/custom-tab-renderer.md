# Custom Tab Renderer

## 1. Przegląd

`CustomTabRenderer` (`pl.zombek.client.core.modules.visual.tabeditor.CustomTabRenderer`) to wyspecjalizowany komponent odpowiedzialny za rysowanie dostosowanej listy graczy (TAB) na ekranie gry oraz w trybie podglądu w Right Shift GUI.

## 2. Kolejność Renderowania

Renderowanie realizowane jest w 6 zorganizowanych fazach:
1. **Animacja Przezroczystości (Fade-in/out):** Płynne obliczanie współczynnika alpha na podstawie `animation_duration` i czasu naciśnięcia klawisza TAB.
2. **Przygotowanie Danych:** Pobranie połączonych graczy z `ZombekPlayerInfoService`, posortowanie przez `TabSortingManager` oraz przygotowanie linii nagłówka i stopki przez `TabHeaderFooterManager`.
3. **Geometria Siatki:** Obliczenie pozycji komórek i wymiarów całkowitych przez `TabLayoutManager`.
4. **Tło i Obramowanie:** Wypełnienie prostokąta tła o wybranym kolorze i przezroczystości, z opcjonalnym obramowaniem (`border_thickness`).
5. **Nagłówek:** Wyśrodkowany wieloliniowy tekst nagłówka serwera lub własnego.
6. **Siatka Graczy:** Komórki zawierające awatar, prefiks, nick lub alias znajomego (z ikoną gwiazdki), oraz wskaźnik ping (ikona kresek, kropka lub liczba w ms z dynamiczną kolorystyką progową).
7. **Stopka:** Wyśrodkowane linie stopki (informacja o ZombekClient, zegar itp.).

## 3. Formaty Wskaźnika Opóźnienia (`TabPingMode`)

- **`NUMERIC`**: Standardowa wartość w milisekundach (np. `42ms`).
- **`BARS`**: Klasyczny wskaźnik kresek zasięgu (np. `📶5`, `📶4`, `📶3`, `📶2`, `📶1`).
- **`MINIMAL_ICON`**: Dyskretna kolorowa kropka statusu (`●`).
- **`COLORED_TEXT`**: Kolorowany tekst z kodami zielony/żółty/czerwony w zależności od zdefiniowanych progów.
