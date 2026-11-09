# Universal Chat Filter System

Dokumentacja silnika filtrowania wiadomości czatu w **ZombekClient** (`ChatFilterManager`).

## 1. Cel i Założenia

Uniwersalny silnik filtrowania umożliwia graczowi eliminację niepożądanych treści z widoku czatu (spam, reklamy, niechciane komunikaty serwerowe), podświetlanie ważnych informacji oraz automatyczne przekierowywanie wiadomości do dedykowanych kart.

## 2. Typy Dopasowania (Match Types)

- `EXACT`: Dosłowne dopasowanie całej treści wiadomości.
- `CONTAINS`: Wystąpienie frazy lub słowa w treści wiadomości.
- `STARTS_WITH`: Wiadomość rozpoczyna się od zadanego tekstu.
- `ENDS_WITH`: Wiadomość kończy się na zadany tekst.
- `REGEX`: Bezpieczne wyrażenie regularne z wbudowaną ochroną ReDoS.

## 3. Ochrona przed Atakami ReDoS (Catastrophic Backtracking)

Silnik `UniversalChatFilterRule` weryfikuje strukturę wyrażenia regularnego pod kątem zagnieżdżonych powtórzeń (np. `(a+)+$`, `(.*)*`) oraz ogranicza czas ewaluacji za pomocą mechanizmu `RegexTimeoutExecutor` (limit 50ms per ewaluacja).
W razie wykrycia ryzyka reguła zostaje oznaczona jako niebezpieczna i nie powoduje zamrożenia wątku klienta gry.

## 4. Akcje Filtra (Filter Actions)

- `HIDE_LOCALLY`: Wiadomość nie jest renderowana na ekranie, ale pozostaje zachowana w pamięci (zależnie od trybu prywatności).
- `HIGHLIGHT`: Oznaczenie wiadomości ramką podświetlenia lub tłem.
- `CHANGE_COLOR`: Nadanie wiadomości wskazanego koloru tekstu ARGB.
- `MOVE_TO_TAB`: Wymuszone skierowanie wiadomości do określonej karty kanału.
- `MARK_IMPORTANT`: Oznaczenie wiadomości jako priorytetowej (np. powiadomienie dźwiękowe).
