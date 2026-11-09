# NetworkMetricsCollector — Centralny Agregator Metryk Sieciowych

## 1. Rola i Architektura

`NetworkMetricsCollector` (`pl.zombek.client.core.netgraph.NetworkMetricsCollector`) jest singletonem w warstwie Core koordynującym gromadzenie, agregację, buforowanie i klasyfikację danych diagnostycznych sieci.

### Komponenty Podrzędne
- **`LatencyTracker`:** Rejestruje próbki pingu, oblicza średnie, skrajności i RFC 3550 Jitter.
- **`NetworkTrafficTracker`:** Zbiera zagregowane rozmiary danych wejściowych i wyjściowych (B/s, KB/s, sumaryczne bajty).
- **`PacketRateTracker`:** Zlicza rzeczywiste zdarzenia nadawania i odbierania pakietów w ujęciu sekundowym (PPS).
- **`NetworkHistoryBuffer`:** Pierścieniowy bufor kołowy (Ring Buffer) o stałej pamięci, przechowujący obiekty `NetworkMetricSample`.
- **`ConnectionStatusTracker`:** Dynamicznie ocenia stan (`CONNECTED`, `SINGLEPLAYER`, itp.) oraz jakość (`EXCELLENT` do `POOR`).
- **`NetworkStatistics`:** Całkowite metryki bieżącej sesji połączenia z możliwością bezpiecznego eksportu.

---

## 2. Cykl Życia i Ticking

1. **Wątek Główny Gry (`ZombekCore.onClientTick()`):**
   - Co tick klienta (20 razy na sekundę) wywoływana jest metoda `collector.onClientTick()`.
   - Odpytywany jest aktywny dostawca `INetworkDataProvider` oraz współdzielony `InfoHudDataProviderRegistry`.
   - Próbki pingu są rejestrowane w `LatencyTracker`.
2. **Kwantyzacja Sekundowa (1000 ms):**
   - Po upływie 1000 ms generowany jest zagregowany obiekt `NetworkMetricSample`.
   - Próbka jest wstawiana do `NetworkHistoryBuffer` oraz przekazywana do `NetworkStatistics`.
   - Zerowane są sekundowe akumulatory atomowe transferu i pakietów.
3. **Rozłączenie i Zmiana Świata (`onWorldLeave()`):**
   - Status połączenia przechodzi w `DISCONNECTED`.
   - Aktywne wartości pingu są oznaczane jako `-1` (`N/A`).
