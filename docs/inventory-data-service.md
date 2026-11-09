# Inventory Data Service — ZombekClient Core (Etap 8B4)

## 1. Przeznaczenie i Rola Systemu

Silnik `ZombekInventoryService` stanowi centralne, jedyne źródło prawdy dla wszystkich modułów i podsystemów ZombekClient potrzebujących danych o ekwipunku gracza (m.in. `ItemCounterModule`, `ItemTrackerModule`, `ArmorStatusModule`, `UhcOverlayModule`).

Główne założenia architektoniczne:
1. **Uniezależnienie od loaderów:** Wszystkie zapytania inwentarza operują na generycznych DTO `InventoryItemData` oraz `InventorySnapshot` wewnątrz modułu `:core`.
2. **Optymalizacja wydajności (Throttling / Caching per Tick):** Ekwipunek gracza jest odczytywany najwyżej jeden raz na tick klienta (20 TPS, 50 ms), co całkowicie eliminuje narzut pętli renderowania HUD (gdzie klatki renderowane są np. 144–240 razy na sekundę).
3. **Mechanizm subskrypcji zmian:** Interfejs `InventoryChangeListener` powiadamia zarejestrowane serwisy o każdej wykrytej zmianie stanu inwentarza.

Pliki źródłowe:
- Centralny serwis: [ZombekInventoryService.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/ZombekInventoryService.java)
- Interfejs dostawcy danych: [IInventoryDataProvider.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/IInventoryDataProvider.java)
- Rejestr dostawców: [InventoryDataProviderRegistry.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/InventoryDataProviderRegistry.java)
- Dostawca domyślny/testowy: [FallbackInventoryDataProvider.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/inventory/FallbackInventoryDataProvider.java)

---

## 2. Implementacje Platformowe (Loader Data Providers)

Każdy wspierany loader implementuje natywny adapter mapujący specyficzne klasy Minecraft na wspólne DTO:

### 1. Fabric (`FabricInventoryDataProvider.java`)
- Rejestrowany w `ZombekClientMod.onInitializeClient()`.
- Korzysta z Yarn mappings: `client.player.getInventory().getStack(i)`, `Registries.ITEM.getId(stack.getItem()).toString()`, `stack.getName().getString()`.

### 2. Quilt (`QuiltInventoryDataProvider.java`)
- Rejestrowany w `QuiltClientBootstrap.onInitializeClient()`.
- Korzysta ze specyfikacji Quilt/Fabric Yarn mappings w sposób w pełni kompatybilny.

### 3. Forge (`ForgeInventoryDataProvider.java`)
- Rejestrowany w `ZombekForgeMod`.
- Korzysta z Mojang mappings: `mc.player.getInventory().getItem(i)`, `BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()`, `stack.getHoverName().getString()`.

### 4. NeoForge (`NeoForgeInventoryDataProvider.java`)
- Rejestrowany w `ZombekNeoForgeMod`.
- Korzysta z Mojang mappings w zgodzie ze standardem NeoForge 20.4.

---

## 3. Cykl Życia i Reset Stanu

- **`onClientTick()`:** Wywoływany w pętli ticków klienta. Pobiera stan ze środowiska i w przypadku wykrycia zmian wywołuje `notifyListeners(previousSnapshot, currentSnapshot)`.
- **`onWorldLeave()`:** Wywoływany przy opuszczeniu świata. Czyści bufor inwentarza, zapobiegając wyciekom pamięci oraz zanieczyszczeniu stanu nowego świata danymi z poprzedniego.
- **`onWorldJoin(worldId)`:** Inicjalizuje nową sesję przechwytywania danych dla danego identyfikatora świata.
