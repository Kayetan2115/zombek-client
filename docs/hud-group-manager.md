# ZombekClient — HUD Group Manager

## 1. Wprowadzenie

Moduł **HUD Group Manager** (`HudGroupManager`) odpowiada za logiczne i przestrzenne łączenie powiązanych ze sobą komponentów interfejsu HUD w jednostki zwane **Grupami** (`HudGroup`). Dzięki grupowaniu gracz może traktować zestaw wskaźników (np. zestawienie liczników Keystrokes + CPS lub zestawienie parametrów sieci Ping + Net Graph) jako spójną całość przy manipulacji, blokowaniu lub ukrywaniu.

---

## 2. Model Grupy (`HudGroup`)

Definicja grupy w strukturze `HudLayout` przechowuje:
- `groupId` — unikalny alfanumeryczny identyfikator (np. `group_combat_hud`).
- `name` — czytelna nazwa grupy wyświetlana na liście i w inspektorze.
- `locked` — flaga blokady edycji pozycji wszystkich elementów w grupie.
- `visible` — flaga widoczności całej grupy (ukrycie grupy wyłącza renderowanie jej składowych).
- `color` — kolor akcentowy grupy w edytorze (wykorzystywany do obwiedni i etykiet).

Komponenty przypisane są do grupy za pomocą pola `groupId` w obiekcie `HudComponentState`.
Architektura w Etapie 9C zakłada model **jednopoziomowy (flat hierarchy)** — komponent może należeć do maksymalnie jednej grupy.

---

## 3. Cykl Życia i Operacje na Grupach

- **Tworzenie grupy (Create Group):**
  - Klawisz skrótu: `Ctrl + G` (przy zaznaczonych co najmniej 2 komponentach).
  - Wywołanie: `createGroup(HudLayout layout, String groupId, String name, Collection<String> componentIds)`.
  - Operacja jest w pełni odwracalna za pomocą polecenia `CreateGroupCommand`.
- **Rozgrupowanie (Ungroup / Delete Group):**
  - Klawisz skrótu: `Ctrl + Shift + G` lub przycisk *Rozgrupuj* w panelu bocznym.
  - Wywołanie: `deleteGroup(HudLayout layout, String groupId)`.
  - Elementy składowe powracają do stanu wolnego (`groupId = null`), zachowując swoje pozycje i właściwości.
- **Dodawanie / Usuwanie komponentu z grupy:**
  - `addComponentToGroup(layout, groupId, compId)` / `removeComponentFromGroup(layout, compId)`.
  - Możliwe również z poziomu zakładki `[GRUPY]` w lewym panelu edytora.
- **Zmiana nazwy grupy:**
  - `renameGroup(layout, groupId, newName)`.
- **Duplikowanie grupy (Duplicate Group):**
  - Tworzy wierną kopię logiczną grupy wraz ze sklonowanymi komponentami i przesunięciem przestrzennym `(+15px, +15px)`.

---

## 4. Wizualizacja i Obliczanie Obwiedni Grupy (Group Bounds)

System dynamicznie kalkuluje obwiednię grupy (`GroupBounds`):
$$\min X = \min_{c \in \text{Group}} c.x, \quad \max X = \max_{c \in \text{Group}} (c.x + c.w)$$
$$\min Y = \min_{c \in \text{Group}} c.y, \quad \max Y = \max_{c \in \text{Group}} (c.y + c.h)$$

W edytorze `HudEditorCanvas`:
- Zaznaczenie grupy wyświetla wspólną przerywaną ramkę akcentową oraz etykietę z nazwą grupy nad lewym górnym rogiem.
- Kliknięcie w dowolny komponent należący do grupy automatycznie zaznacza całą grupę (chyba że użytkownik przytrzymuje `Alt`, co pozwala na selekcję podkomponentu).
- Przeciągnięcie dowolnego elementu grupy płynnie przesuwa wszystkie jej elementy bez zniekształceń.
