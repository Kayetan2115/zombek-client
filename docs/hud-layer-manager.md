# ZombekClient — Advanced Layer Management

## 1. Wprowadzenie

Moduł **Advanced Layer Management** (`HudLayerManager`) zarządza porządkiem renderowania komponentów w osi głębokości (Z-index). W zaawansowanym interfejsie użytkownika poszczególne elementy (tła, główne moduły HUD, wskaźniki informacyjne oraz powiadomienia wyskakujące) muszą być renderowane w ściśle zdefiniowanej kolejności, zapobiegając niepożądanemu przesłanianiu kluczowych danych gry.

---

## 2. Architektura Warstw Logicznych

System wprowadza 4 predefiniowane warstwy logiczne (`HudLayer`) o bazowych przedziałach wartości Z-index, z możliwością definiowania warstw własnych (`custom`):

| Identyfikator Warstwy | Nazwa Wyświetlana | Baza Z-Index | Przeznaczenie i Typowe Komponenty |
|---|---|---|---|
| `background` | Tło (Background) | 0 | Paski tła, ramki, statyczne tekstury ozdobne, zaciemnienia. |
| `main_hud` | Główny HUD (Main HUD) | 100 | Keystrokes, CPS, Armor Status, Paski Zdrowia i Pancerza, Hotbar. |
| `information` | Informacje (Information) | 200 | FPS, Ping, Coordinates, Direction HUD, Memory, Clock, Day Counter. |
| `notifications` | Powiadomienia (Notifications)| 300 | Boss Bar, Title/Subtitle, alerty sieciowe, komunikaty tymczasowe. |
| `custom_*` | Własne Warstwy | 400+ | Dowolne zdefiniowane przez użytkownika warstwy użytkowe. |

Warstwy systemowe posiadają flagę `systemLayer = true` — nie można ich usunąć, co gwarantuje stabilność renderowania bazowych modułów klienta.

---

## 3. Zarządzanie Z-Index i Kolejnością Renderowania

Porządek renderowania wyznaczany jest ściśle przez wartość `zIndex` (liczba całkowita ze znakiem):
$$c_1 \prec c_2 \iff c_1.\text{zIndex} < c_2.\text{zIndex}$$

W przypadku równego Z-indeksu rozstrzygnięcie następuje po alfabetycznym `componentId`, zapewniając determinizm renderowania na każdej platformie.

### Operacje Z-Index:
1. **Bring to Front (Na sam wierzch):**
   - Ustala `zIndex` komponentu na wartość $\max_{c \in \text{Layer}} c.\text{zIndex} + 1$, przenosząc go na wierzch bieżącej warstwy.
2. **Send to Back (Na sam spód):**
   - Ustala `zIndex` komponentu na wartość $\min_{c \in \text{Layer}} c.\text{zIndex} - 1$.
3. **Move Forward / Backward (O krok do przodu / do tyłu):**
   - Zamienia wartości Z-index z bezpośrednim sąsiadem w posortowanej liście.
4. **Normalizacja Z-Index (`normalizeZIndices`):**
   - Przelicza indeksy komponentów w obrębie warstwy do zwartych wartości z zachowaniem względnej kolejności (np. zapobiega narastaniu indeksów do milionów przy częstych operacjach).

---

## 4. Blokowanie i Ukrywanie na Poziomie Warstwy

Z poziomu zakładki `[WARSTWY]` w edytorze użytkownik może kontrolować całe warstwy:
- **Lock Layer:** Blokuje edycję i przeciąganie wszystkich komponentów należących do danej warstwy (przydatne np. do zablokowania tła podczas pracy nad wskaźnikami).
- **Hide Layer:** Tymczasowo wyłącza renderowanie całej warstwy na kanwie i w grze.
