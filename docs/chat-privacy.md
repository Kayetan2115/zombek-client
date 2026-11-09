# Chat Privacy & Streamer Mode

Dokumentacja zabezpieczeń prywatności i trybu transmisji (Streamer Mode) w **ZombekClient**.

## 1. Ochrona w Trybie Transmisji (Streamer Mode)

Podczas transmisji na żywo (Twitch, YouTube) lub nagrywania rozgrywki, okno czatu może stać się źródłem nieintencjonalnego ujawnienia wrażliwych danych.
Klient udostępnia następujące mechanizmy:
- **Redakcja Nadawców**: Zamiana nazw graczy i pseudonimów na `[SENDER REDACTED]` w logach i eksporcie.
- **Wykluczenie Wiadomości Prywatnych**: Opcja `exclude_private_messages` automatycznie ukrywa wiadomości z kanału `/msg`, `/tell` oraz zaproszenia przed widzami transmisji.
- **Maskowanie Adresów IP**: Wszelkie wykryte wzorce adresów IP serwerów lub graczy są automatycznie cenzurowane gwiazdkami.

## 2. Integralność Pakietów i Architektura Lokalna

- Wszystkie filtry i maskowania są aplikowane **wyłącznie w warstwie prezentacji (GUI)**.
- Oryginalne pakiety sieciowe i podpisy kryptograficzne Minecraft 1.19+ / 1.20+ pozostają nienaruszone.
- Klient nigdy nie modyfikuje wiadomości wychodzących ani nie wysyła komend bez interakcji gracza.
