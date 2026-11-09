# Friend Highlight System

## 1. Założenia i Prywatność

Lokalny system znajomych w ZombekClient (`NameTagFriendManager` i `TabFriendHighlightManager`) działa w oparciu o następujące fundamenty:
- **Zero wycieków sieciowych:** Lista znajomych jest przechowywana wyłącznie lokalnie na dysku gracza w pliku `config/zombekclient/friends.json`.
- **Zero auto-zaproszeń:** Klient nie wysyła żadnych pakietów ani komend do serwera w celu dodania znajomych.
- **Identyfikacja hybrydowa:** Kluczowanie opiera się na UUID gracza (gdy dostępne) z automatycznym fallbackiem na nazwę użytkownika (case-insensitive).

## 2. Model Wpisu Znajomego (`FriendEntry`)

Każdy znajomy może posiadać:
- `uuid` (UUID konta gracza)
- `username` (nazwa użytkownika)
- `alias` (własna przyjazna nazwa wyświetlana lokalnie)
- `customColorArgb` (kolor wyróżnienia w NameTagu i na liście TAB)
- `friendMarkerIcon` (symbol, np. `★`, `♦`, `♥`)
- `prefix` (opcjonalny lokalny prefiks)
- `notes` (notatka gracza)

## 3. Integracja w NameTags i Tab Editor

- **NameTags:**
  - Dodanie ikony znajomego przed nickiem/aliasem.
  - Wyświetlanie aliasu zamiast oficjalnego nicku gdy `enable_alias = true`.
  - Kolorowanie etykiety na zdefiniowany kolor przyjaciela.
- **Tab Editor:**
  - Wyróżnienie wpisu znajomego unikalnym kolorem wiersza/nazwy.
  - Opcjonalne przypięcie znajomych na szczycie listy (`friends_first = true`).
  - Ikona gwiazdki i alias w komórce gracza.
