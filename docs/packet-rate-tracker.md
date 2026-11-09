# PacketRateTracker — Śledzenie Częstotliwości Pakietów (PPS)

## 1. Zastosowanie

`PacketRateTracker` (`pl.zombek.client.core.netgraph.PacketRateTracker`) odpowiada za zliczanie częstotliwości transmisji pakietowej Minecraft w ujęciu pakietów na sekundę (Packets Per Second — PPS).

---

## 2. Metryki Transmisji Pakietowej

- **Incoming Packets per Second (RX PPS):** Liczba pakietów odebranych przez klienta w ciągu ostatniej sekundy.
- **Outgoing Packets per Second (TX PPS):** Liczba pakietów wysłanych przez klienta do serwera w ciągu ostatniej sekundy.
- **Average Incoming PPS:** Średnia częstotliwość odbierania pakietów w sesji.
- **Average Outgoing PPS:** Średnia częstotliwość wysyłania pakietów w sesji.
- **Maximum Incoming PPS:** Szczytowe tempo odbierania pakietów.
- **Maximum Outgoing PPS:** Szczytowe tempo nadawania pakietów.
- **Total Incoming Packets:** Całkowita liczba odebranych pakietów.
- **Total Outgoing Packets:** Całkowita liczba wysłanych pakietów.

---

## 3. Zasady Pomiaru i Unikanie Duplikacji

- **Poziom Sieciowy:** Zliczanie następuje bezpośrednio w punktach transmisji protokołu sieciowego.
- **Brak Fałszywych Liczników:** Liczniki pakietów nie zliczają lokalnych zdarzeń GUI, klatek renderowania ani ticków logiki gry. Każdy zainkrementowany licznik odpowiada fizycznemu zdarzeniu pakietowemu.
- **Thread Safety:** Inkrementacja odbywa się za pomocą `AtomicInteger`, bez obciążania pętli zdarzeń Netty.
