# Inventory Snapshot System — ZombekClient Core (Etap 8B4)

## 1. Przeznaczenie i Rola Systemu

System migawek inwentarza (`InventorySnapshot` oraz `InventorySnapshotService`) odpowiada za tworzenie niezmiennych (immutable) reprezentacji zawartości ekwipunku gracza w zadanym punkcie czasu oraz ich precyzyjne porównywanie pod kątem zmian netto i transferów slotowych.

Pliki źródłowe:
- Migawka inwentarza: [InventorySnapshot.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/InventorySnapshot.java)
- Serwis porównywania migawek: [InventorySnapshotService.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/InventorySnapshotService.java)
- Wynik dyferencjacji: [SnapshotDiffResult.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/SnapshotDiffResult.java)
- Model elementu ekwipunku: [InventoryItemData.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/InventoryItemData.java)

---

## 2. Model Danych `InventoryItemData`

Obiekt `InventoryItemData` reprezentuje zawartość pojedynczego slotu w sposób całkowicie uniezależniony od silnika graficznego:
- `itemId`: Identyfikator przestrzeni nazw Minecraft (np. `"minecraft:diamond"`).
- `itemName`: Nazwa wyświetlana (z uwzględnieniem kowadła / niestandardowych tagów).
- `count`: Liczba przedmiotów w slocie.
- `maxStackSize`: Maksymalny rozmiar stacka dla danego typu (np. 16 dla pereł, 64 dla bloków, 1 dla zbroi).
- `slotIndex`: Fizyczny indeks slotu w kontenerze gracza.
- `source`: Źródło slotu (`HOTBAR`, `MAIN_INVENTORY`, `OFFHAND`, `ARMOR`).
- `components`: Serializowany stan komponentów lub tagów NBT.
- `itemHandle`: Opcjonalny uchwyt do natywnego `ItemStack` (używany wyłącznie przez platformowy renderer).
- `isBlock` / `isFood`: Właściwości kategoryzacyjne.

---

## 3. Algorytm Porównywania i Normalizacji Ruchów Wewnętrznych

Metoda `InventorySnapshotService.compare(previous, current, allowedSources)` realizuje dwuetapowe porównanie:

```mermaid
flowchart TD
    Prev[Poprzednia Migawka S1]
    Curr[Aktualna Migawka S2]
    Diff[InventorySnapshotService.compare()]

    Prev & Curr --> Diff
    Diff --> Step1[Krok 1: Agregacja sum arytmetycznych per Item ID]
    Diff --> Step2[Krok 2: Analiza zmian per Slot Index]

    Step1 --> CheckNet{Czy występuje zmiana sumy netto?}
    CheckNet -- TAK --> EmitNet[Zarejestruj Zmianę Netto: delta = S2 - S1]
    CheckNet -- NIE --> CheckSlot{Czy zmieniły się indeksy slotów?}

    CheckSlot -- TAK --> EmitTransfer[Zarejestruj Pure Slot Transfer: bez zmiany ilości]
    CheckSlot -- NIE --> NoChange[Brak Zmian]
```

Dzięki rozdzieleniu analizy per slot od analizy zagregowanej sumy:
- Przeniesienie przedmiotu ze slotu 0 do slotu 15 nie wywołuje fałszywego zdarzenia "wyrzucono ze slotu 0" ani "podniesiono do slotu 15".
- Rozdzielenie stacka 64 na dwa stacki po 32 jest poprawnie traktowane jako transfer wewnątrzekwipunkowy.
- Wynik `SnapshotDiffResult.hasNetChanges()` zwraca `false` dla ruchów czysto porządkowych.
