# ZombekClient — System Skrótów Klawiszowych (Keybind Manager, Etap 7B)

## 1. Zasada Działania (`KeybindManager`)

Menedżer skrótów klawiszowych (`pl.zombek.client.keybind.KeybindManager`) odpowiada za rejestrację, obsługę oraz detekcję konfliktów klawiszy w całym kliencie gry.

### Domyślny Klawisz Menu
- Domyślnym klawiszem otwierającym menu ZombekClient jest **PRAWY SHIFT (`RIGHT SHIFT` / `GLFW.GLFW_KEY_RIGHT_SHIFT`)**.
- Skrót ten jest zarejestrowany jako oficjalny `KeyBinding` biblioteki Fabric (`KeyBindingHelper.registerKeyBinding`), co umożliwia graczowi jego ewentualną zmianę bezpośrednio w standardowych opcjach sterowania Minecraft (`Options -> Controls -> Key Binds`).

---

## 2. Obsługa Skrótów w Grze (In-Game Edge Triggering)

1. **Warunki wyzwolenia**:
   - Skróty modułów reagują wyłącznie wtedy, gdy gracz znajduje się w świecie gry (`client.world != null`) oraz żaden ekran/GUI (np. czat, ekwipunek, menu pauzy) nie jest aktywny (`client.currentScreen == null`).
2. **Krawędź wciśnięcia (Single Press Edge)**:
   - System rejestruje moment wciśnięcia klawisza za pomocą zestawu `pressedKeys`. Zapobiega to wielokrotnemu, lawinowemu przełączaniu modułu przy przytrzymaniu klawisza.

---

## 3. Wykrywanie Konfliktów Klawiszy (Conflict Detection)

KeybindManager dostarcza metody:
- `hasConflict(int keyCode)`: Zwraca `true`, jeśli ten sam kod klawisza przypisano do więcej niż jednego modułu.
- `getConflictingModules(int keyCode)`: Zwraca listę wszystkich modułów współdzielących dany skrót.

W GUI moduły posiadające konflikt klawiszowy mogą być oznaczane ostrzegawczym kolorem złota/czerwieni.

---

## 4. Przechwytywanie Klawiszy w GUI (Interactive Rebinding)

Po kliknięciu przycisku skrótu na karcie modułu lub w jego ustawieniach:
1. Interfejs przechodzi w tryb przechwytywania (`capturingModule` / `capturingSetting`).
2. Wyświetlane jest okno modalne: *"Przypisz klawisz... Naciśnij dowolny klawisz lub przycisk myszy"*.
3. Wciśnięcie klawisza klawiatury (GLFW) lub przycisku myszy (`M1`, `M2`, `M3` itd.) natychmiast aktualizuje przypisanie.
4. Wciśnięcie `ESC`, `BACKSPACE` lub `DELETE` anuluje skrót i ustawia go w stan nieprzypisany (`NONE` / `0`).
5. Nowe przypisanie natychmiast wyzwala zaplanowany zapis do pliku konfiguracyjnego `config/zombekclient/modules.json`.
