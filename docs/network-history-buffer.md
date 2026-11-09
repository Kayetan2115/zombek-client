# NetworkHistoryBuffer — Bufor Historii Próbek Sieciowych

## 1. Zastosowanie

`NetworkHistoryBuffer` (`pl.zombek.client.core.netgraph.NetworkHistoryBuffer`) to wysokowydajny, synchronizowany pierścieniowy bufor kołowy (Ring Buffer) przechowujący obiekty `NetworkMetricSample`.

---

## 2. Architektura i Ograniczenie Pamięci

- **Stała Pojemność (Fixed Capacity):**
  - Domyślna pojemność wynosi 300 próbek (co odpowiada 5 minutom historii przy próbkowaniu 1 Hz).
  - W konfiguracji modułu użytkownik może wybrać aktywne okno prezentacji na wykresie: 5s, 10s, 30s, 60s lub czas niestandardowy.
- **Nadpisywanie Najstarszych Danych:**
  - Wskaźnik głowicy (`head = (head + 1) % capacity`) nadpisuje najstarsze wpisy bez konieczności kosztownego przesuwania tablicy (`System.arraycopy`).
  - Gwarantuje zerowe wycieki pamięci (zero memory leaks) podczas wielogodzinnych sesji gry.
- **Metody Agregacji w Oknie Czasowym:**
  - `getSamplesWithin(long durationMillis)`: Zwraca chronologicznie posortowane próbki z zadanego okresu.
  - `getMaxPing(duration)` / `getMinPing(duration)` / `getAvgPing(duration)`.
  - `getMaxTraffic(duration)` / `getMaxPps(duration)`.
