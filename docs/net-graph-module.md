# Moduł Net Graph (`NetGraphModule`) — Dokumentacja Techniczna

## 1. Przegląd Modułu

Moduł **Net Graph** (`pl.zombek.client.core.modules.hud.netgraph.NetGraphModule`) to profesjonalny, zaawansowany system graficznego monitorowania jakości, opóźnienia i aktywności połączenia sieciowego klienta Minecraft.

Zarejestrowany w kategorii `ModuleCategory.HUD` pod identyfikatorem `"net_graph"` oraz z priorytetem renderowania `HudPriorityResolver.PRIORITY_NET_GRAPH = 36`.

---

## 2. Tryby Pracy Wykresu (`NetGraphMode`)

1. **`LATENCY` (Opóźnienie):**
   - Wykres historii pingu w milisekundach.
   - Pozioma linia średniej wartości pingu (`avg: XX ms`).
   - Obliczanie i wyświetlanie wskaźnika Jitter (wariacja opóźnienia).
2. **`TRAFFIC` (Przepustowość):**
   - Dwie odrębne serie danych: ruch wejściowy (`RX`) i ruch wyjściowy (`TX`).
   - Dynamiczne lub stałe jednostki: `B/s`, `KB/s`, `MB/s`.
3. **`PACKETS` (Pakiety):**
   - Częstotliwość transmisji pakietowej w pakietach na sekundę (PPS).
   - Osobne serie dla pakietów odebranych (`RX PPS`) i wysłanych (`TX PPS`).
4. **`COMBINED` (Wielowarstwowy):**
   - Równoległa prezentacja opóźnienia (obszar/linia), ruchu łącza oraz pakietów z czytelnymi skalami i kolorami.
5. **`CUSTOM` (Niestandardowy):**
   - Użytkownik samodzielnie decyduje w konfiguracji, które serie mają być renderowane.

---

## 3. Typy Wykresu (`NetGraphType`)

- **`LINE`:** Ciągła, dynamicznie łączona krzywa liniowa.
- **`BAR`:** Dyskretny histogram słupkowy próbek czasowych.
- **`AREA`:** Wypełniony poligon warstwowy z regulowanym stopniem przezroczystości (`fill_opacity`).

---

## 4. Grupy Ustawień Modułu

Moduł zawiera 11 grup konfiguracyjnych w pełni zintegrowanych z `Settings Framework`:
1. `general` — tryb wykresu, pokazywanie statusu, jakości, bieżących wartości, legendy, automatyczne skalowanie.
2. `latency` — włączenie pingu, średniej, jittera, min/max, szerokość okna historii (5-60s), wygładzanie, precyzja.
3. `traffic` — włączenie ruchu, pokazywanie RX/TX, transfer łączny, jednostka (`AUTO`, `B/s`, `KB/s`, `MB/s`), interwał próbkowania.
4. `packets` — włączenie pakietów, pokazywanie RX/TX PPS, wartości szczytowe, okres buforowania.
5. `graph` — typ wykresu (`LINE`, `BAR`, `AREA`), szerokość, wysokość, skala, siatka referencyjna, etykiety osi, grubość linii, przezroczystość wypełnienia.
6. `connection` — typ połączenia, plakietka statusu, odznaka jakości, czas sesji.
7. `statistics` — statystyki sesji, automatyczny reset przy reconnect.
8. `thresholds` — progi klasyfikacji jakości: Excellent (35ms), Good (75ms), Moderate (150ms), Poor (250ms), ostrzeżenie o jitterze (15ms), ostrzeżenie o transferze (500 KB/s).
9. `appearance` — kolor tła, przezroczystość tła, ramka, grubość ramki, rozmiar tekstu, cień, margines wewnętrzny (padding), odstępy.
10. `position` — kotwica ekranu (`HudAnchor`), współrzędne X i Y.
11. `colors` — paleta kolorów dla pingu, ruchu przychodzącego/wychodzącego, pakietów, siatki i tekstu.

---

## 5. Singleplayer i Fair Play

- **Singleplayer:** Klient rozpoznaje lokalny serwer zintegrowany. Nie generuje sztucznego pingu internetowego (`0 ms`), wyświetlając zamiast tego informację `Singleplayer — Local Server` oraz `External Latency: N/A`.
- **Fair Play:** Moduł jest w 100% pasywny. Nie wysyła żadnych dodatkowych pakietów sondujących, nie generuje sztucznego ruchu i nie ingeruje w protokół sieciowy.
