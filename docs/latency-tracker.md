# LatencyTracker — Śledzenie Opóźnienia i Obliczanie Jittera

## 1. Zastosowanie

`LatencyTracker` (`pl.zombek.client.core.netgraph.LatencyTracker`) odpowiada za monitorowanie opóźnienia sieciowego (ping) oraz matematyczną analizę fluktuacji opóźnienia (jitter) w kliencie Minecraft.

---

## 2. Metryki Ping

- **Current Ping:** Najnowsza zarejestrowana wartość pingu w ms (zwraca `-1` w przypadku braku połączenia lub w trybie Singleplayer).
- **Average Ping:** Średnia arytmetyczna z próbek zgromadzonych w buforze kołowym.
- **Minimum Ping:** Najniższa zarejestrowana wartość w aktywnym oknie.
- **Maximum Ping:** Najwyższa wartość szczytowa (peak) w aktywnym oknie.
- **Last Known Ping:** Ostatnia znana poprawna wartość pingu (chroni przed nagłym resetem wykresu przy chwilowej utracie próbki).

---

## 3. Matematyczny Model Obliczania Jittera (RFC 3550 / IPDV)

Jitter sieciowy (Packet Delay Variation) to zmienność opóźnienia w czasie. ZombekClient implementuje standardowy algorytm oparty na **RFC 3550** (RTP Protocol Specification) oraz **Mean Absolute Delay Variation (MAD)**:

### 3.1. Różnica Chwilowa
Dla kolejnych poprawnych próbek pingu $P_n$ oraz $P_{n-1}$:
$$D_n = |P_n - P_{n-1}|$$

### 3.2. Wskaźnik Bieżący (Current Jitter — EWMA)
Wykorzystuje wykładniczo ważoną średnią kroczącą (EWMA z wagą $1/16$):
$$J_n = J_{n-1} + \frac{|D_n| - J_{n-1}}{16.0}$$
(dla pierwszej pary próbek: $J_1 = D_1$).

### 3.3. Średni Jitter w Oknie (Average Jitter)
Średnia arytmetyczna ze wszystkich bezwzględnych różnic w buforze historii:
$$\text{Average Jitter} = \frac{1}{K} \sum_{i=1}^{K} |P_i - P_{i-1}|$$
gdzie $K = N - 1$ to liczba przejść między próbkami.

### 3.4. Maksymalny Jitter (Maximum Jitter)
Najwyższa pojedyncza fluktuacja odnotowana w oknie:
$$\text{Max Jitter} = \max_{i=1 \dots K} |P_i - P_{i-1}|$$

### 3.5. Warunek Dostępności
Obliczenie wskaźnika jitter wymaga co najmniej **dwóch próbek** ($N \ge 2$). Jeśli w buforze znajduje się mniej niż 2 próbki lub gracz gra w Singleplayer, wskaźniki zwracają `-1.0` (wyświetlane jako `N/A`).
