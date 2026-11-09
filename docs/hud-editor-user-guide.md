# ZombekClient — Kompletny Podręcznik Użytkownika HUD Editora

## 1. Jak Otworzyć HUD Editor?

Dostęp do edytora można uzyskać na trzy sposoby:
1. **Menu Główne ZombekClient:** Naciśnij `Prawy Shift` w trakcie gry, a następnie kliknij przycisk `[Edytor HUD]` na górnym pasku.
2. **Dedykowany Klawisz Skrótu:** Domyślnie przypisany pod klawisz `H` lub w menu sterowania Minecraft (`key.zombekclient.hud_editor`).
3. **Komenda Czatowa:** Wpisz na czacie `/zombek hud`.

---

## 2. Układ Ekranu Edytora

Ekran edytora podzielony jest na logiczne strefy ułatwiające orientację:

```
+-----------------------------------------------------------------------------------------+
| [⮌ Cofnij] [⮎ Ponów] | Profil: [PvP ▾] | Preset: [Wybierz ▾] | [Eksport] [Import] | [✕ Zamknij] |
+--------------+-----------------------------------------------------------+--------------+
| [KOMPONENTY] |                                                           | INSPEKTOR    |
| [WARSTWY]    |                                                           |              |
| [GRUPY]      |                     OBSZAR KANWY                          | Komponent:   |
|              |                  (Obszar Roboczy HUD)                     | Coordinates  |
| Lista modów: |                                                           |              |
| ☑ Keystrokes |      [Keystrokes]                         [Scoreboard]    | Pozycja X: 10|
| ☑ FPS        |                                                           | Pozycja Y: 20|
| ☑ Ping       |                                                           | Skala: 1.0x  |
| ☑ Coords     |      [FPS]                                                | Kotwica:     |
| ☐ Clock      |      [Ping]                                               | [TOP_LEFT ▾] |
|              |                                                           | Warstwa:     |
| [Wyrównanie] |                                                           | [INFO ▾]     |
| ⇤ ⇥ ⤒ ⤓ ⇸    |                                                           | [Zablokuj]   |
+--------------+-----------------------------------------------------------+--------------+
| Status: Gotowy | Rozdzielczość: 1920x1080 (16:9) | Skala GUI: Auto | Ostrzeżenia: 0     |
+-----------------------------------------------------------------------------------------+
```

---

## 3. Skróty Klawiszowe (Pełna Ściąga)

| Skrót | Działanie |
|---|---|
| `LPM (Klik)` | Zaznaczenie komponentu / odznaczenie pozostałych. |
| `Ctrl + LPM` | Dodanie/usunięcie klikniętego komponentu do zaznaczenia (Multi-Select). |
| `LPM + Przeciągnięcie (w tle)` | Rysowanie ramki zaznaczania (Marquee Selection). |
| `LPM + Przeciągnięcie (element)`| Przemieszczanie zaznaczonych elementów po ekranie (Snap to Grid). |
| `Ctrl + A` | Zaznacz wszystkie widoczne elementy. |
| `Esc` | Odznacz wszystkie elementy / anuluj zaznaczanie ramką. |
| `Delete` / `Backspace` | Ukryj zaznaczone komponenty. |
| `Ctrl + Z` | Cofnij ostatnią operację (Undo). |
| `Ctrl + Y` / `Ctrl + Shift + Z` | Ponów cofniętą operację (Redo). |
| `Ctrl + C` | Skopiuj zaznaczone elementy do schowka. |
| `Ctrl + V` | Wklej elementy ze schowka. |
| `Ctrl + D` | Zduplikuj zaznaczone elementy z przesunięciem +15px. |
| `Ctrl + G` | Połącz zaznaczone elementy w logiczną Grupę. |
| `Ctrl + Shift + G` | Rozgrupuj zaznaczoną grupę. |
| `Ctrl + S` | Zapisz układ na dysku bez wychodzenia z edytora. |
| `Kółko Myszy (Scroll)` | Zoom kanwy (przybliżenie / oddalenie obszaru roboczego). |
| `ŚPM (Klik kółkiem) + Drag` | Pan kanwy (przesuwanie całego obszaru roboczego). |

> **Wskazówka bezpieczeństwa:** Kiedy kursor znajduje się wewnątrz pola tekstowego (np. przy wpisywaniu nazwy profilu lub koordynatów), globalne skróty edytora są automatycznie zawieszane, zapobiegając przypadkowemu usunięciu komponentu po naciśnięciu `Backspace`.

---

## 4. Przewodnik Krok po Kroku

### A. Jak szybko ustawić układ pod PvP?
1. Otwórz edytor klawiszem `H`.
2. Na górnym pasku kliknij listę rozwijaną **Preset** i wybierz `PvP Pro`.
3. Kliknij `Zastosuj`. Wszystkie liczniki (Keystrokes, CPS, Pancerz, Efekty mikstur) natychmiast ułożą się w ergonomiczny układ bojowy.
4. Dopasuj pozycję według uznania i kliknij `[Zapisz i Zamknij]` lub wciśnij `Esc`.

### B. Jak wyrównać kilka elementów w jednej linii?
1. Przytrzymaj klawisz `Ctrl` i kliknij lewym przyciskiem myszy na komponenty, które chcesz wyrównać (np. FPS, Ping, Memory).
2. W lewym panelu w sekcji *Narzędzia Wyrównywania* kliknij ikonę `[Wyrównaj do lewej]` lub `[Wyrównaj w pionie]`.
3. Elementy zostaną natychmiast ułożone w równej kolumnie z zachowaniem siatki pomocniczej.

### C. Jak wyeksportować i wysłać układ znajomemu?
1. Na górnym pasku kliknij przycisk `[Eksportuj Układ]`.
2. W oknie dialogowym wybierz nazwę pliku, np. `moj_hud_2026.json`.
3. Plik zostanie zapisany w folderze `.minecraft/config/zombekclient/hud/exports/`.
4. Wyślij ten plik koledze — może go wgrać za pomocą przycisku `[Importuj Układ]`, wybierając tryb *Zastąp* lub *Połącz*.
