# NeoForge Keybinds & Input Loop — ZombekClient In-Game Mod

## 1. Wprowadzenie

W architekturze modyfikacji klienckiej ZombekClient na platformie **Minecraft NeoForge 1.20.4**, obsługa klawiatury realizowana jest przez wyspecjalizowany menedżer `NeoForgeKeybindManager` oraz integrację z magistralą zdarzeń klienta.

Kluczowe wymagania systemu wejścia:
1. Rejestracja oficjalnego mapowania klawisza w ustawieniach sterowania Minecrafta (`Options -> Key Binds`).
2. Domyślne przypisanie klawisza **PRAWY SHIFT (`RIGHT SHIFT` / `GLFW.GLFW_KEY_RIGHT_SHIFT`)**.
3. Detekcja pojedynczego zbocza (edge-detection) zapobiegająca migotaniu ekranu przy przytrzymaniu klawisza.
4. Ignorowanie skrótu, gdy gracz pisze na czacie (`ChatScreen`) lub korzysta z kontenerów/ekwipunku (`InventoryScreen`, `ChestScreen`).
5. Obsługa dynamicznej zmiany przypisań klawiszy w locie z poziomu menu modułów.
6. Wykrywanie kolizji i konfliktów klawiszy pomiędzy modułami klienta.

---

## 2. Rejestracja Mapowania Klawisza (`RegisterKeyMappingsEvent`)

NeoForge 20.4 wymaga rejestracji obiektów `KeyMapping` wyłącznie podczas zdarzenia `RegisterKeyMappingsEvent` na szynie zdarzeń modu (`Mod Event Bus`):

```java
public class NeoForgeClientEvents {
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        NeoForgeKeybindManager.getInstance().registerKeyMappings(event);
    }
}
```

W `NeoForgeKeybindManager`:

```java
public void registerKeyMappings(RegisterKeyMappingsEvent event) {
    if (this.openMenuKeyMapping == null) {
        this.openMenuKeyMapping = new KeyMapping(
                KEY_OPEN_MENU_NAME,          // "key.zombekclient.open_menu"
                InputConstants.Type.KEYSYM,
                DEFAULT_OPEN_KEY,            // GLFW.GLFW_KEY_RIGHT_SHIFT
                KEY_CATEGORY_ZOMBEK          // "key.categories.zombekclient"
        );
        event.register(this.openMenuKeyMapping);
    }
}
```

---

## 3. Pętla Tickowa i Detekcja Zbocza (Edge-Detection)

W zdarzeniu `TickEvent.ClientTickEvent` (faza `Phase.END` na szynie `NeoForge.EVENT_BUS`), menedżer weryfikuje stan klawisza:

```java
public void onClientTick() {
    Minecraft mc = Minecraft.getInstance();
    if (mc == null) return;

    // 1. Obsługa klawisza otwierania menu ZombekClient
    if (openMenuKeyMapping != null) {
        boolean isDown = openMenuKeyMapping.isDown();
        if (isDown && !menuKeyWasDown) {
            // Pojedyncze zbocze narastające (single press edge)
            if (mc.screen == null) {
                // Bezpieczne otwarcie, gdy gracz nie znajduje się w innym GUI
                mc.setScreen(new ZombekNeoForgeScreen());
            } else if (mc.screen instanceof ZombekNeoForgeScreen) {
                // Ponowne naciśnięcie zamyka ekran klienta
                mc.setScreen(null);
            }
        }
        menuKeyWasDown = isDown;
    }

    // 2. Obsługa skrótów klawiszowych poszczególnych modułów
    if (mc.screen == null && mc.player != null) {
        checkModuleKeybinds();
    }
}
```

---

## 4. Wykrywanie Konfliktów Klawiszy (`Keybind Conflicts`)

`NeoForgeKeybindManager` integruje się z `ModuleManager` z uniwersalnego rdzenia `:core`:

- Metoda `hasConflict(int keyCode)` sprawdza, czy dany kod klawisza GLFW jest przypisany do więcej niż jednego modułu jednocześnie.
- Metoda `getConflictingModules(int keyCode)` zwraca listę modułów kolidujących ze sobą.
- Kolidujące moduły są oznaczane w GUI ostrzeżeniem wizualnym w kolorze bursztynowym (`0xFFF59E0B`), informując użytkownika o dublującym się skrócie.

---

## 5. Przypisywanie Klawiszy w Czasie Rzeczywistym

Gdy użytkownik w `ZombekNeoForgeScreen` kliknie przycisk `[BIND: ...]`:
1. Ekran przechodzi w stan `listeningKeybind = true`.
2. Dowolne naciśnięcie klawisza w metodzie `keyPressed(int keyCode, int scanCode, int modifiers)` przechwytuje kod klawisza.
3. Klawisz `GLFW_KEY_ESCAPE` zeruje przypisanie (`GLFW_KEY_UNKNOWN` / brak bindu).
4. Każdy inny klawisz aktualizuje `KeybindSetting` modułu i wywołuje automatyczny debounced zapis do `config/zombekclient/modules.json`.
