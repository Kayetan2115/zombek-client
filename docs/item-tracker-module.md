# Item Tracker Module — ZombekClient (Etap 8B4)

## 1. Przeznaczenie i Rola Modułu

Moduł `ItemTrackerModule` (`"item_tracker"`) rejestruje i prezentuje na ekranie historię zmian ekwipunku gracza w trakcie trwania sesji rozgrywki (Singleplayer i Multiplayer). Śledzi przyrosty (zdobycie, podniesienie, wytworzenie) oraz ubytki (użycie, zjedzenie, wyrzucenie, postawienie bloku), a także zmiany o niepotwierdzonym źródle.

Moduł zarejestrowany jest w kategorii `ModuleCategory.HUD` („HUD / Informacyjne”), współpracuje z `ItemTrackerManager`, modelem `HudComponentModel`, systemem priorytetów `HudPriorityResolver` (priorytet: **29**) oraz menu Right Shift.

Pliki źródłowe:
- Moduł HUD: [ItemTrackerModule.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/itemtracker/ItemTrackerModule.java)
- Układy prezentacji: [ItemTrackerLayout.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/itemtracker/ItemTrackerLayout.java)
- Koordynator sesji: [ItemTrackerManager.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/itemtracker/ItemTrackerManager.java)
- Typy zdarzeń: [ItemEventType.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/itemtracker/ItemEventType.java)
- Poziomy pewności: [EventConfidence.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/itemtracker/EventConfidence.java)
- Statystyki sesji: [ItemTrackingStatistics.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/itemtracker/ItemTrackingStatistics.java)
- Historia i bufor cykliczny: [ItemTrackingHistory.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/itemtracker/ItemTrackingHistory.java)

---

## 2. Kategoryzacja Zdarzeń i Poziomy Pewności (Fair Play)

### Typy Zdarzeń (`ItemEventType`)
- `COLLECTED` / `PICKED_UP`: Podniesienie lub pozyskanie przedmiotu.
- `USED`: Użycie przedmiotu lub narzędzia (np. rzucenie perły, strzał z łuku).
- `CONSUMED`: Zjedzenie jabłka, spożycie potrawki lub wypicie mikstury.
- `DROPPED`: Wyrzucenie przedmiotu z ekwipunku na ziemię klawiszem Q lub przeciągnięciem myszą.
- `CRAFTED`: Wytworzenie przedmiotu w craftingu.
- `PLACED`: Postawienie bloku w świecie gry.
- `TRANSFERRED`: Czyste przemieszczenie między slotami (nigdy niezaliczane jako zysk ani strata).
- `UNKNOWN_CHANGE`: Zmiana ilości o niepotwierdzonym przez klienta źródle (np. komenda `/give`, plugin serwerowy, czyszczenie ekwipunku przez minigrę).

### Poziomy Pewności (`EventConfidence`)
Zgodnie z rygorystycznymi zasadami Fair Play ZombekClient:
- **`CONFIRMED`:** Zdarzenie w 100% potwierdzone przez silnik gry (np. wykonanie akcji zjedzenia potwierdzonej pakietem i zmianą stanu przedmiotu).
- **`OBSERVED`:** Zdarzenie zaobserwowane bezpośrednio przez klienta w bieżącym ticku.
- **`INFERRED`:** Zdarzenie wywnioskowane z kontekstu gry z wysokim prawdopodobieństwem.
- **`UNKNOWN`:** Źródło zmiany nieznane. **Klient pod żadnym pozorem nie zgaduje ani nie fabrykuje etykiety "podniesiono" dla niepotwierdzonych zmian ilości!**

---

## 3. Ignorowanie Przemieszczeń Wewnętrznych (Slot Transfers)

Złożony system `InventorySnapshotService` normalizuje ruchy wewnętrzne:
- Przeniesienie przedmiotu ze slotu Hotbaru do slotu głównego ekwipunku,
- Rozdzielenie stacka na dwa pół-stacki (np. 64 -> 32 + 32 w dwóch slotach),
- Połączenie dwóch mniejszych stacków w jeden większy,
- Zmiana kolejności przedmiotów w ekwipunku.

Wszystkie te operacje charakteryzują się zerową zmianą sumaryczną (`net delta = 0`) i są klasyfikowane jako czyste transfery slotowe (`pure slot transfer`). Zostają całkowicie pominięte w feedzie zdarzeń i statystykach zysków/strat.

---

## 4. Tryby Układu (`ItemTrackerLayout`)

1. **`FEED` (Domyślny):** Ostatnie zdarzenia w formie przewijanego strumienia (np. `+4 Ender Pearls — Collected (Confirmed)`).
2. **`STATISTICS`:** Podsumowanie sesji dla każdego śledzonego przedmiotu z sumą zysków i strat (np. `Golden Apple: +3 / -1`).
3. **`COMPACT_SUMMARY`:** Skrócone zestawienie sumarycznych zmian netto.
4. **`DETAILED_SPLIT`:** Rozbicie na osobne sekcje: Ostatnie Podniesione vs Ostatnie Użyte.

---

## 5. Eksport Historii do Formatów Zewnętrznych

Klasa `ItemTrackingHistory` udostępnia metodę `exportToJson()` generującą pełny, ustrukturyzowany zrzut sesji:
```json
{
  "totalEvents": 2,
  "events": [
    {
      "timestamp": 1793716527000,
      "itemId": "minecraft:ender_pearl",
      "itemName": "Ender Pearl",
      "delta": 4,
      "eventType": "COLLECTED",
      "confidence": "CONFIRMED",
      "description": "+4 Ender Pearl — Collected"
    }
  ]
}
```
Możliwe jest również wyczyszczenie historii (`clear()`), zatrzymanie sesji (`pause()`) i jej wznowienie (`resume()`).
