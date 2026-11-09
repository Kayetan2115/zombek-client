# Item Counter Module — ZombekClient (Etap 8B4)

## 1. Przeznaczenie i Rola Modułu

Moduł `ItemCounterModule` (`"item_counter"`) w ZombekClient dostarcza uniwersalny, zoptymalizowany pod kątem wydajności komponent HUD prezentujący w czasie rzeczywistym aktualną liczbę określonych przedmiotów w ekwipunku gracza.

Moduł zarejestrowany jest w kategorii `ModuleCategory.HUD` („HUD / Informacyjne”) i w pełni integruje się ze wspólnym serwisem inwentarza `ZombekInventoryService`, modelem pozycjonowania `HudComponentModel`, systemem zapobiegania kolizjom `HudPriorityResolver` (priorytet: **32**) oraz menu konfiguracyjnym Right Shift GUI.

Pliki źródłowe:
- Moduł HUD: [ItemCounterModule.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/itemcounter/ItemCounterModule.java)
- Tryby układu HUD: [ItemCounterLayout.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/itemcounter/ItemCounterLayout.java)
- Tryby grupowania: [ItemGroupingMode.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/itemcounter/ItemGroupingMode.java)
- Rejestr presetów: [TrackedItemRegistry.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/itemtracker/TrackedItemRegistry.java)
- Serwis inwentarza: [ZombekInventoryService.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/ZombekInventoryService.java)

---

## 2. Architektura i Przepływ Danych

```mermaid
flowchart TD
    subgraph Minecraft Inventory
        HB[Hotbar: slots 0-8]
        MI[Main Inventory: slots 9-35]
        OH[Offhand: slot 40]
        AR[Armor: slots 36-39]
    end

    subgraph Loader Adapters
        FabP[FabricInventoryDataProvider]
        ForgeP[ForgeInventoryDataProvider]
        NeoP[NeoForgeInventoryDataProvider]
        QuiltP[QuiltInventoryDataProvider]
        FallP[FallbackInventoryDataProvider]
    end

    subgraph Central Inventory Service
        ZIS[ZombekInventoryService (Cached per Tick)]
        Snap[InventorySnapshot (Immutable)]
    end

    subgraph Item Counter HUD
        ICM[ItemCounterModule (Priority: 32)]
        Disp[buildDisplayEntries()]
        Warn[Threshold & Alert Evaluator]
        HudRen[HudComponentRenderer]
    end

    HB & MI & OH & AR --> FabP & ForgeP & NeoP & QuiltP & FallP
    FabP & ForgeP & NeoP & QuiltP & FallP --> ZIS
    ZIS --> Snap
    Snap --> ICM
    ICM --> Disp --> Warn --> HudRen
```

1. **Źródła inwentarza (`InventorySlotSource`):** Użytkownik decyduje, z których obszarów zliczane są przedmioty (`count_hotbar`, `count_main_inventory`, `count_offhand`, `count_armor_slots`).
2. **Pojedyncze źródło prawdy:** Moduł nigdy nie odpytuje bezpośrednio platformowego obiektu gracza w pętli renderowania HUD (np. 144 razy na sekundę). Odpytuje wyłącznie `ZombekInventoryService.getInstance().getSnapshot()`, którego stan jest buforowany i odświeżany co tick (20 Hz).
3. **Wizualizacja ostrzeżeń:** Dla każdego śledzonego przedmiotu wyliczane są progi ostrzegawcze (`warning_threshold`, `critical_threshold`). W przypadku spadku ilości poniżej progu moduł dynamicznie barwi licznik na kolor żółty/czerwony lub stosuje pulsowanie.

---

## 3. Tryby Układu (`ItemCounterLayout`)

1. **`COMPACT` (Domyślny):** Ikona przedmiotu + ilość (np. `[Icon] 16`).
2. **`DETAILED`:** Ikona, nazwa przedmiotu, aktualna ilość oraz liczba pełnych stacków w nawiasie (np. `[Icon] Ender Pearl: 16 (1 st)`).
3. **`ICONS_ONLY`:** Wyłącznie ikony przedmiotów z liczbą nałożoną w rogu ikony (w stylu slotu ekwipunku Minecraft).
4. **`LIST`:** Pionowa lista wierszy z nazwami przedmiotów i wyrównaną do prawej ilością.
5. **`HORIZONTAL`:** Poziomy pasek liczników ułożony obok siebie z konfigurowalnym odstępem kolumn.
6. **`CUSTOM`:** Możliwość dostosowania kolejności i widoczności elementów.

---

## 4. Opcje Konfiguracyjne (Grupy Ustawień)

- **General (`group_general`):**
  - `grouping_mode`: Tryb grupowania (`BY_ITEM_ID`, `BY_ITEM_NAME`, `BY_CUSTOM_CATEGORY`).
  - `show_item_names`: Czy wyświetlać nazwę tekstową przedmiotu (domyślnie `true`).
  - `show_item_icons`: Czy renderować ikony przedmiotów (domyślnie `true`).
  - `show_quantity`: Czy renderować liczbę sztuk (domyślnie `true`).
  - `show_stack_count`: Czy pokazywać liczbę pełnych stacków (domyślnie `false`).
  - `show_zero_quantity`: Czy pokazywać przedmiot, gdy jego ilość wynosi 0 (domyślnie `true`).
- **Sources (`group_sources`):**
  - `count_hotbar`: Zliczanie z paska podręcznego (domyślnie `true`).
  - `count_main_inventory`: Zliczanie z głównego ekwipunku (domyślnie `true`).
  - `count_offhand`: Zliczanie z drugiej ręki (domyślnie `true`).
  - `count_armor_slots`: Zliczanie ze slotów zbroi (domyślnie `false`).
- **Tracking (`group_tracking`):**
  - `watched_items`: Identyfikatory przedmiotów rozdzielone przecinkami (np. `minecraft:ender_pearl,minecraft:golden_apple,minecraft:arrow,minecraft:shield`).
  - `sort_mode`: Tryb sortowania (`CUSTOM_ORDER`, `COUNT_DESC`, `COUNT_ASC`, `ALPHABETICAL`).
- **Alerts (`group_alerts`):**
  - `enable_alerts`: Globalny przełącznik ostrzeżeń niskiego stanu (domyślnie `true`).
  - `warning_threshold`: Próg ostrzeżenia (domyślnie `4.0`).
  - `critical_threshold`: Próg krytyczny (domyślnie `1.0`).
  - `warning_color`: Kolor ostrzeżenia (domyślnie żółty `0xFFFFAA00`).
  - `critical_color`: Kolor krytyczny (domyślnie czerwony `0xFFFF5555`).
- **Appearance (`group_appearance`):**
  - `layout`, `style`, `icon_size`, `text_color`, `background_color`, `background_opacity`, `border_color`, `show_border`, `border_thickness`, `shadow`, `row_spacing`, `column_spacing`.
- **Layout (`group_layout`):**
  - `anchor`, `pos_x`, `pos_y`, `scale`.

---

## 5. Podgląd w Menu (Preview Mode)

Gdy gracz znajduje się w menu głównym, na ekranie ładowania lub w Right Shift GUI bez aktywnego świata, moduł automatycznie przełącza się w bezpieczny tryb `[PREVIEW]` z realistycznymi danymi przykładowymi (np. 12 pereł, 4 złote jabłka, 32 obsydiany), co pozwala na natychmiastowe konfigurowanie wyglądu bez konieczności uruchamiania świata gry.
