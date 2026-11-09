# Quilt Keybinds — System Klawiszy i Wykrywania Zboczy w ZombekClient

## 1. Domyślny Klawisz Menu: RIGHT SHIFT

Domyślnym klawiszem otwierającym menu ZombekClient na Quilt jest **PRAWY SHIFT (`GLFW.GLFW_KEY_RIGHT_SHIFT`)**.

```java
public static final int DEFAULT_OPEN_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;
```

---

## 2. Podwójna Strategia Rejestracji i Detekcji (Hybrid Keybind System)

W środowisku Quilt Loader (szczególnie bez instalacji dodatkowego QSL lub Quilted Fabric API) klawisze mogą być rejestrowane natywnie w opcjach gry Minecraft lub odpytywane bezpośrednio przez GLFW:

### 1. Rejestracja w `GameOptions.allKeys`
W trakcie inicjalizacji klienta (`MinecraftClient.<init>` przez miksin) `QuiltKeybindManager` dodaje instancję `openMenuKeyBinding` do tablicy `client.options.allKeys`. Umożliwia to graczowi standardową rekonfigurację klawisza w menu opcji Minecrafta (`Sterowanie -> Przypisanie klawiszy -> Kategoria: ZombekClient`).

### 2. Bezpośrednia Detekcja Zbocza (Rising Edge Detection) przez GLFW
Jeśli gracz korzysta z domyślnego klawisza lub w przypadku braku pełnej synchronizacji `KeyBinding`, `QuiltKeybindManager.onClientTick()` odpytuje bezpośrednio stan klawisza w oknie GLFW:

```java
boolean isDown = false;
if (openMenuKeyBinding != null && openMenuKeyBinding.isPressed()) {
    isDown = true;
} else if (client.getWindow() != null) {
    long handle = client.getWindow().getHandle();
    if (handle != 0 && GLFW.glfwGetKey(handle, DEFAULT_OPEN_KEY) == GLFW.GLFW_PRESS) {
        isDown = true;
    }
}

if (isDown && !menuKeyWasDown) {
    // Wykryto zbocze narastające (rising edge)
    if (client.currentScreen == null) {
        client.setScreen(new ZombekQuiltScreen());
    } else if (client.currentScreen instanceof ZombekQuiltScreen) {
        client.setScreen(null);
    }
}
menuKeyWasDown = isDown;
```

### Zalety tego podejścia:
- Menu otwiera się dokładnie **jeden raz** na pojedyncze naciśnięcie klawisza, eliminując efekt migotania i natychmiastowego zamykania przy przytrzymaniu klawisza.
- Powtórne naciśnięcie RIGHT SHIFT w otwartym menu powoduje jego płynne zamknięcie.
- System działa niezależnie od tego, czy gra posiada zainstalowane jakiekolwiek zewnętrzne biblioteki API.

---

## 3. Wykrywanie Konfliktów Skrótów

Klasa `QuiltKeybindManager` integruje się z `ModuleManager` w celu detekcji nakładających się skrótów klawiszowych:

```java
public boolean hasConflict(int keyCode) {
    if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return false;
    return getConflictingModules(keyCode).size() > 1;
}

public List<AbstractModule> getConflictingModules(int keyCode) {
    if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return Collections.emptyList();
    ModuleManager mm = ZombekCore.getInstance().getModuleManager();
    if (mm == null) return Collections.emptyList();

    List<AbstractModule> conflicts = new ArrayList<>();
    for (AbstractModule module : mm.getModules()) {
        if (module.getKeybind() == keyCode) {
            conflicts.add(module);
        }
    }
    return conflicts;
}
```

W przypadku przypisania tego samego klawisza do wielu modułów (np. `K`), GUI wyświetla ostrzeżenie o konflikcie, a użytkownik może natychmiast skorygować powiązanie w szufladzie ustawień modułu.
