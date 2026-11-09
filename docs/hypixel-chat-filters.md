# Filtry czatu i ochrona ReDoS — HypixelChatFilter

## 1. Cel i architektura systemu filtrów

Serwer Hypixel generuje znaczną liczbę powiadomień systemowych (komunikaty o dołączaniu graczy do lobby, losowania Mystery Box, globalne podziękowania za tipy, reklamy rang itp.), które odciągają uwagę gracza w trakcie meczów.

Komponent **`HypixelChatFilter`** (`pl.zombek.client.core.hypixel.HypixelChatFilter`) zapewnia wysoce wydajne, wielopoziomowe filtrowanie niechcianych wiadomości bez wpływu na płynność gry (FPS).

---

## 2. 13 Wbudowanych kategorii filtrów (`ChatFilterCategory`)

System definiuje 13 precyzyjnych kategorii komunikatów:

1. **`LOBBY_JOIN`**: Komunikaty o dołączeniu graczy VIP/MVP do lobby (`Player joined the lobby!`).
2. **`GUILD_MOTD`**: Komunikaty powitalne gildii po wejściu na serwer.
3. **`MYSTERY_BOX`**: Powiadomienia o znalezieniu skrzynki Mystery Box (`You found a Mystery Box!`).
4. **`REWARD_SUMMARY`**: Podsumowania nagród i poziomów po ukończeniu gry.
5. **`ADVERTISEMENT`**: Wiadomości promujące sklep Hypixel, zniżki i pakiety boosterów.
6. **`AUTO_GG_SPAM`**: Masowy spam wiadomości "gg" wysyłanych przez automaty po meczu.
7. **`TIP_NOTIFICATIONS`**: Komunikaty o napiwkach boosterów (`You tipped...`).
8. **`RANK_UPGRADE_BROADCAST`**: Globalne ogłoszenia o zakupie rangi przez innego gracza.
9. **`QUEST_REMINDERS`**: Cykliczne przypomnienia o misjach dziennych i tygodniowych.
10. **`SOCIAL_MEDIA_LINKS`**: Linki do profili społecznościowych w lobby.
11. **`EVENT_ANNOUNCEMENTS`**: Ogłoszenia o trwających turniejach i wydarzeniach sezonowych.
12. **`HOUSING_INVITES`**: Zaproszenia na serwery Housing wysyłane publicznie w lobby.
13. **`CUSTOM_RULES`**: Niestandardowe reguły zdefiniowane bezpośrednio przez użytkownika.

---

## 3. Typy reguł i zaawansowana ochrona ReDoS

Własne reguły filtracji (`ChatFilterRule`) mogą działać w 3 trybach dopasowania:
- **`EXACT`**: Dokładne dopasowanie całej linii tekstu.
- **`CONTAINS`**: Sprawdzenie obecności podciągu znaków (najszybsze, odporne na błędy).
- **`REGEX`**: Dopasowanie za pomocą wyrażeń regularnych.

### Ochrona przed atakami ReDoS (Regular Expression Denial of Service)
Złe lub złośliwe wyrażenia regularne mogą zawiesić wątek renderowania klienta poprzez katastrofalne cofanie (Catastrophic Backtracking). ZombekClient stosuje podwójną barierę ochronną:
1. **Limit długości wzorca:** Maksymalna dopuszczalna długość wzorca regex to **100 znaków**. Wzorce dłuższe są natychmiast odrzucane.
2. **Wykrywanie zagnieżdżonych kwantyfikatorów:** Reguły sprawdzają obecność niebezpiecznych konstrukcji takich jak `(a+)+`, `(.*)*` lub `([0-9]+)+`. W przypadku ich wykrycia reguła jest flagowana jako `invalidRegex` i nie jest kompilowana.
3. **Izolacja wyjątków:** Wszelkie błędy składniowe regex są bezpiecznie przechwytywane i nie powodują awarii gry.

---

## 4. Licznik statystyk i wyłącznik awaryjny

- **Licznik zablokowanych wiadomości (`filteredCount`):** Moduł zlicza odrzucone wiadomości w bieżącej sesji, prezentując użytkownikowi statystykę w GUI.
- **Wyłącznik awaryjny (`disableAllFilters()`):** W razie pomyłkowego zablokowania ważnych wiadomości użytkownik może jednym kliknięciem wyłączyć wszystkie filtry, przywracając pełny strumień czatu.

Kompletne testy filtrów i odporności na ReDoS znajdują się w `HypixelChatFilterTest.java` (6 testów, 100% zaliczone).
