# ZombekClient — Complete Undo / Redo System

## 1. Wprowadzenie

Moduł **HUD Undo / Redo** (`HudUndoRedoManager`) implementuje klasyczny wzorzec projektowy **Command (Polecenie)** ze stosem historii modyfikacji. System rejestruje wszystkie akcje użytkownika w `HudEditorScreen` (przesuwanie, zmiana rozmiaru, zmiana skali, zmiana kotwicy, widoczności, warstwy, grupowanie oraz nakładanie presetów), umożliwiając bezpieczne cofanie i ponawianie operacji bez ryzyka utraty pracy lub uszkodzenia układu.

---

## 2. Architektura Poleceń (`IHudCommand`)

Każda modyfikowalna akcja w edytorze implementuje interfejs `IHudCommand`:

```java
public interface IHudCommand {
    String getName();
    void execute(HudLayout layout);
    void undo(HudLayout layout);
    void redo(HudLayout layout);
}
```

### Zaimplementowane Polecenia Specjalistyczne:
- `MoveComponentsCommand` — rejestruje przesunięcie jednego lub wielu komponentów (stare i nowe koordynaty `posX, posY`).
- `ResizeComponentCommand` — rejestruje zmianę szerokości i wysokości (`width, height`) z obsługą uchwytów narożnych.
- `ScaleComponentsCommand` — rejestruje zmianę współczynnika skali (`scale`).
- `ChangeAnchorCommand` — rejestruje zmianę punktu zakotwiczenia (`anchor`) wraz z przeliczonymi offsetami.
- `ChangeVisibilityCommand` — rejestruje włączenie/wyłączenie widoczności (`visible`).
- `ChangeLayerCommand` — rejestruje przeniesienie komponentu między warstwami lub zmianę Z-index.
- `CreateGroupCommand` / `DeleteGroupCommand` — rejestruje tworzenie i usuwanie grup.
- `ApplyPresetCommand` — zapisuje pełny stan układu sprzed nałożenia presetu, pozwalając cofnąć całą operację jednym kliknięciem.
- `BatchCommand` — grupuje zestaw dowolnych poleceń w jedną atomową operację w historii (np. jednoczesne usunięcie 5 komponentów).

---

## 3. Grupowanie Gestów Myszy (Gesture Grouping)

Częstym problemem w prostych edytorach jest rejestrowanie każdego piksela przesunięcia myszą jako osobnego kroku w historii, co zapełnia stos `Undo` w ułamku sekundy.

W ZombekClient zastosowano **Gesture Grouping**:
1. W momencie naciśnięcia myszy (`handleMouseClick`) zapisywany jest stan początkowy `(startX, startY)`.
2. Podczas przeciągania (`handleMouseDrag`) kanwa odświeża widok w czasie rzeczywistym, ale **nie emituje** wpisów do historii.
3. W momencie puszczenia myszy (`handleMouseRelease`) następuje weryfikacja: jeśli dystans $\Delta = |x - startX| + |y - startY| > 0$, tworzone i rejestrowane jest dokładnie **jedno** polecenie `MoveComponentsCommand` reprezentujące cały gest.

---

## 4. Stos Historii i Ograniczenia Pamięciowe

- **Pojemność stosu:** Do 50 kroków wstecz (wartość konfigurowalna w `MAX_HISTORY_SIZE`).
- **Polityka przepełnienia:** Przy przekroczeniu 50 wpisów najstarsze polecenia są usuwane ze spodu stosu FIFO.
- **Unieważnienie Redo:** Wykonanie nowej akcji po cofnięciu czyści stos operacji ponownych (`redoStack.clear()`).
- **Skróty klawiszowe:**
  - `Ctrl + Z` — Cofnij (Undo).
  - `Ctrl + Y` lub `Ctrl + Shift + Z` — Ponów (Redo).
- **Przyciski w pasku narzędzi:** W lewym górnym rogu edytora znajdują się przyciski `[⮌ Cofnij]` i `[⮎ Ponów]` z dynamicznym stanem aktywny/nieaktywny (`enabled = canUndo() / canRedo()`).
