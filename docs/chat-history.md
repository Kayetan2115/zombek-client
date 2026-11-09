# Chat History System

Dokumentacja bufora historii czatu w **ZombekClient** (`ChatHistoryManager`).

## 1. Założenia i Pojemność

Moduł historii czatu przechowuje nadesłane wiadomości w pamięci RAM w strukturze bufora pierścieniowego (Ring Buffer) o konfigurowalnym limicie:
- Zakres: od 50 do 5000 wiadomości (domyślnie 1000).
- Przy przekroczeniu limitu najstarsze wpisy są automatycznie usuwane (FIFO).

## 2. Bezpieczeństwo i Sanifikacja Poświadczeń (Credential Sanitization)

W celach ochrony poufności danych użytkownika, bufor historii stosuje rygorystyczny filtr poleceń uwierzytelniających:
- Wyrażenie regularne: `(?i)^/(?:login|register|l|reg|changepassword|auth)\s+.*`
- Wiadomości zawierające hasła gracza **nigdy nie są zapisywane w historii** ani eksportowane do plików.

## 3. Wyszukiwanie Wielokryterialne (Search)

Metoda `search(query, sender, category, fromEpoch, toEpoch)` umożliwia błyskawiczne filtrowanie historii:
- Fraza tekstowa (wyszukiwanie case-insensitive).
- Nazwa nadawcy (Sender Name).
- Kategoria (`ChatCategory`).
- Przedział czasowy (od/do w milisekundach).

## 4. Eksport i Czyszczenie

- **Eksport do pliku tekstowego**: `exportHistoryToText(TimestampFormat)` generuje sformatowany log czatu z uwzględnieniem trybu Streamer/Privacy (gdzie nicki nadawców są maskowane).
- **Automatyczne czyszczenie**: Opcja `clear_on_disconnect` czyści pamięć podręczną po opuszczeniu serwera, uniemożliwiając wyciek danych pomiędzy sesjami.
