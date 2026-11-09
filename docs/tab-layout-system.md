# Tab Layout System

## 1. Architektura Siatki (`TabLayoutManager`)

System układu listy graczy `TabLayoutManager` dynamicznie organizuje wpisy graczy w siatkę wielokolumnową.

### 2. Algorytm Adaptacyjny

1. **Obliczanie Liczby Kolumn:**
   - W oparciu o liczbę graczy (`count`) oraz maksymalną dopuszczalną liczbę wierszy (`maxRows`):
     $$\text{cols} = \min(\max(\lceil \text{count} / \text{maxRows} \rceil, 1), \text{maxCols})$$
2. **Równomierne Równoważenie Wierszy:**
   - Wiersze są dzielone symetrycznie pomiędzy kolumny:
     $$\text{rows} = \lceil \text{count} / \text{cols} \rceil$$
   Dzięki temu lista 45 graczy przy `maxRows = 20` zostanie ułożona w 3 kolumny po 15 wierszy każda, zachowując zbalansowaną geometrię.
3. **Adaptacyjna Szerokość Kolumn (`adaptiveWidth`):**
   - Jeśli szerokość ekranu pozwala, kolumny mogą rozszerzać się proporcjonalnie do najdłuższych prefiksów i nicków.
4. **Pozycjonowanie Komórek:**
   - Komórka o indeksie kolumny $c$ i wiersza $r$ otrzymuje współrzędne:
     $$X = \text{startX} + c \cdot (\text{columnWidth} + \text{hSpacing})$$
     $$Y = \text{startY} + r \cdot (\text{rowHeight} + \text{vSpacing})$$

## 3. Presety Układów (`TabLayout`)

- **`MODERN`**: Półprzezroczyste karty poszczególnych wierszy, eleganckie odstępy.
- **`MINIMAL`**: Czysta lista bez kart wierszy, zoptymalizowana pod minimalizm.
- **`CARD`**: Styl kafelkowy z wyraźnymi zaokrągleniami i tłem dla każdego wpisu.
- **`COLUMNS`**: Sztywny podział tabelaryczny z separacją kolumn.
- **`CLASSIC`**: Klasyczny układ zbliżony do Minecraft 1.8.
- **`VANILLA`**: Odzwierciedlenie domyślnego zachowania z zachowaniem personalizacji kolorów.
- **`CUSTOM`**: Pełna swoboda ręcznej konfiguracji wymiarów, odstępów i siatki.
