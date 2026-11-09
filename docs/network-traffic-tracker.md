# NetworkTrafficTracker — Monitorowanie Przepustowości Sieciowej

## 1. Zastosowanie

`NetworkTrafficTracker` (`pl.zombek.client.core.netgraph.NetworkTrafficTracker`) monitoruje zagregowaną objętość danych przesyłanych pomiędzy klientem Minecraft a serwerem w jednostkach B/s, KB/s, MB/s oraz sumarycznych bajtach.

---

## 2. Metryki Przepustowości

- **Incoming Bytes per Second (RX):** Bieżący transfer danych pobieranych z sieci.
- **Outgoing Bytes per Second (TX):** Bieżący transfer danych wysyłanych do sieci.
- **Total Bytes Received:** Łączna objętość danych odebranych podczas sesji.
- **Total Bytes Sent:** Łączna objętość danych wysłanych podczas sesji.
- **Peak Incoming B/s:** Maksymalna odnotowana chwilowa przepustowość wejściowa.
- **Peak Outgoing B/s:** Maksymalna odnotowana chwilowa przepustowość wyjściowa.

---

## 3. Bezpieczeństwo i Zasada Non-Invasive Byte Counter

1. **Agregacja Numeryczna:**
   - Zliczane są wyłącznie surowe liczby bajtów (`readableBytes()`) z nagłówków buforów Netty.
   - Moduł **nigdy** nie odczytuje, nie klonuje ani nie analizuje treści pakietów.
   - Płatności, hasła, wiadomości czatu i tokeny sesyjne pozostają nienaruszone i niewidoczne dla systemu metryk.
2. **Wielowątkowość (Thread Safety):**
   - Wątki obsługi Netty aktualizują bezblokadowe liczniki atomowe `AtomicLong`.
   - Główny wątek gry podczas kwantyzacji sekundowej pobiera wartości za pomocą `getAndSet(0)`, eliminując ryzyko race conditions.
3. **Obsługa Singleplayer:**
   - W trybie gry jednoosobowej komunikacja odbywa się przez serwer zintegrowany w pamięci (loopback), dlatego transfer internetowy raportowany jest jako `Local Traffic / N/A`.
