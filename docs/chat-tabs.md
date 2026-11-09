# Chat Tabs System

Dokumentacja systemu lokalnych kart i kanałów czatu w **ZombekClient** (`ChatTabManager`).

## 1. Założenia i Architektura

System Chat Tabs dzieli strumień wiadomości sieciowych na czytelne, dedykowane karty w oknie czatu klienta.
Wszystkie reguły podziału na karty wykonywane są w pamięci lokalnej klienta i **nie wymagają modyfikacji po stronie serwera**.

## 2. Standardowe Karty (Default Tabs)

Klient dostarcza zestaw prekonfigurowanych zakładek:
- `all` (**Wszystko**): Domyślna karta agregująca wszystkie nadesłane wiadomości.
- `general` (**Ogólny**): Wiadomości graczy, komunikaty dołączenia i opuszczenia gry.
- `private` (**Prywatne**): Prywatne wiadomości bezpośrednie (`/msg`, `/tell`, `/whisper`, `/r`).
- `party` (**Party**): Wiadomości z czatu grupy (`Party >`).
- `team` (**Drużyna**): Wiadomości czatu drużynowego (`Team >`, `Drużyna >`).
- `guild` (**Gildia**): Wiadomości z czatu klanu lub gildii (`Guild >`).
- `system` (**System**): Komunikaty systemowe, błędy, odpowiedzi na komendy, powiadomienia o zgonach i osiągnięciach.

## 3. Karty Niestandardowe (Custom Tabs)

Użytkownik może tworzyć własne karty w konfiguracji:
- Unikalny identyfikator i nazwa wyświetlana (Title).
- Kolorystyka i ikona.
- Filtr kategorii wiadomości (`ChatCategory`).
- Opcjonalny filtr nadawców (Custom Sender Filter) — np. karta dedykowana wyłącznie dla lidera gildii lub handlarzy.

## 4. Etykiety Nieprzeczytanych Wiadomości (Unread Badges)

- Każda karta pracująca w tle rejestruje liczbę nadesłanych wiadomości pasujących do jej reguł.
- W momencie przełączenia na daną kartę licznik nieprzeczytanych zostaje wyzerowany.
