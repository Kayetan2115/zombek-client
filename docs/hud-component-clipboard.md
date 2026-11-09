# ZombekClient — Copy / Paste / Duplicate System

## 1. Wprowadzenie

Moduł **HUD Component Clipboard** (`HudComponentClipboard`) odpowiada za schowek komponentów wewnątrz edytora HUD. Umożliwia graczom kopiowanie parametrów wyglądu, położeń lub całych elementów pomiędzy różnymi grupami, układami, a nawet pomiędzy różnymi profilami (`HudProfileManager`).

---

## 2. Model Danych Schowka (`HudClipboardData`)

Schowek przechowuje:
- `components` — głębokie kopie obiektów `HudComponentState` skopiowanych elementów.
- `groups` — definicje skopiowanych grup (jeśli zaznaczenie obejmowało grupę).
- `sourceResolution` — rozdzielczość ekranu, przy której dokonano operacji kopiowania (pozwala na inteligentne skalowanie relatywne przy wklejaniu na ekranie o innej rozdzielczości).
- `timestamp` — czas skopiowania.

Schowek działa w pamięci sesji gry (`HudEditorState`), dzięki czemu skopiowane elementy można przenieść do innego profilu:
1. Gracz zaznacza komponenty w profilu `PvP`.
2. Naciska `Ctrl + C`.
3. Przełącza profil na `Survival`.
4. Naciska `Ctrl + V` — komponenty zostają przeniesione z zachowaniem parametrów.

---

## 3. Tryby Wklejania (`PasteMode`)

Edytor wspiera 4 zróżnicowane tryby wklejania:

1. **Wklej w miejscu (`IN_PLACE`):**
   - Wkleja komponenty dokładnie w tych samych współrzędnych ekranowych `(posX, posY)`. Jeśli komponent już istnieje w układzie, jego parametry zostają zaktualizowane.
2. **Wklej obok kursora (`NEAR_CURSOR`):**
   - Wykorzystuje aktualną pozycję kursora myszy na kanwie `(cursorX, cursorY)` jako punkt wstawienia lewego górnego rogu wklejanego zestawu.
3. **Wklej tylko wygląd (`APPEARANCE_ONLY`):**
   - Kopiuje wyłącznie skalę, kolory, widoczność i Z-index na aktualnie zaznaczony komponent, nie zmieniając jego współrzędnych ani kotwicy.
4. **Wklej tylko pozycję (`POSITION_ONLY`):**
   - Kopiuje wyłącznie współrzędne `(posX, posY)` i kotwicę `anchor`, pozostawiając wymiary i skalę komponentu docelowego.

---

## 4. Ochrona Instancji Pojedynczych (Single-Instance Protection)

Wbudowane komponenty informacyjne Minecraft (np. współrzędne `coordinates`, licznik klatek `fps`, pasek zdrowia `boss_bar`) są z natury **singletonami** — na ekranie nie powinny istnieć dwa niezależne wskaźniki FPS próbujące pobierać te same dane z różnymi konfiguracjami.

Menedżer schowka realizuje regułę ochronną:
- Przy próbie duplikowania lub wklejania komponentu-singletona, który jest już obecny w układzie docelowym, system **nie tworzy zduplikowanego obiektu**, lecz:
  - Włącza widoczność komponentu (`visible = true`), jeśli był ukryty.
  - Przesuwa istniejący komponent na zadaną pozycję.
  - Wyświetla w pasku powiadomień informację: `Zaktualizowano istniejącą instancję komponentu [nazwa]`.

---

## 5. Duplikowanie (`Duplicate` — `Ctrl + D`)

Operacja duplikowania stanowi skróconą ścieżkę kopiuj-wklej:
- Klawisz skrótu: `Ctrl + D`.
- Tworzy natychmiastową kopię zaznaczonych elementów z automatycznym przesunięciem przestrzennym:
  $$x_{\text{new}} = x_{\text{old}} + 15\text{px}, \quad y_{\text{new}} = y_{\text{old}} + 15\text{px}$$
- Zapewnia, że wklejony element jest od razu widoczny i nie przesłania całkowicie obiektu źródłowego.
- Zaznaczenie na kanwie zostaje natychmiast przeniesione na nowo utworzone obiekty.
