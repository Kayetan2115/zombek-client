# Ekran HUD Editora — ZombekClient (Etap 9A)

## 1. Wygląd i Układ Trzykolumnowy

Ekran edytora HUD został zaprojektowany z zachowaniem spójnej, świątecznej estetyki ZombekClient (ciemne tła `#16181F`, akcenty złota `#FFCC00`, błękitu `#4A90E2` oraz zieleni statusowej `#55FF55`).

```
+---------------------------------------------------------------------------------------------------------+
| ⚡ EDYTOR HUD • Układ: Domyślny Układ                          [Siatka: ON] [Tryb: 1080p (FullHD)]     |
+-------------------+-----------------------------------------------------------------+-------------------+
| KOMPONENTY HUD  ◀ |                                                                 | WŁAŚCIWOŚCI     ▶ |
| 🔍 Szukaj...      |               Obszar Gry: 960x540 (Bieżąca rozdzielczość)       |                   |
| [ALL][HUD][PvP]   |   +---------------------------------------------------------+   | FPS Display       |
|                   |   | [XYZ: 145 / 68 / -312]                                  |   | Typ: Screen Space |
| 👁 Keystrokes     |   |                                                         |   |                   |
| 👁 CPS Display    |   |                                                         |   | Pozycja:          |
| 👁 Armor Status   |   |                                                         |   | X: 10 px  [-] [+] |
| 👁 FPS Display    |   |   +-------------------+                                 |   | Y: 44 px  [-] [+] |
| 👁 Ping Display   |   |   | FPS: 144          |                                 |   |                   |
| 👁 Memory Usage   |   |   +-------------------+                                 |   | Skala:            |
| 👁 Coordinates    |   |                                                         |   | 1.0x      [-] [+] |
| 👁 Scoreboard     |   |                                                         |   |                   |
| 👁 Net Graph      |   |                                                         |   | Kotwica:          |
| 👁 Bedwars Helper |   |                                                         |   | ⚓ Lewy Górny     |
|                   |   |                                                         |   |                   |
|                   |   |                                                         |   | Widoczny:         |
|                   |   |                                                         |   | [ WŁĄCZONY ]      |
|                   |   |                                                         |   |                   |
|                   |   |                                                         |   | ↺ Przywróć        |
|                   |   +---------------------------------------------------------+   |   domyślne        |
+-------------------+-----------------------------------------------------------------+-------------------+
| ● Masz niezapisane zmiany                                        [↺ Domyślne]  [Anuluj]  [💾 Zapisz]    |
+---------------------------------------------------------------------------------------------------------+
```

---

## 2. Pasek Górny (Topbar)
- **Tytuł i Branding**: `⚡ EDYTOR HUD` z nazwą aktualnie edytowanego profilu układu.
- **Przełącznik Siatki**: `[Siatka: ON/OFF]` — włącza lub wyłącza rysowanie siatki pomocniczej na canvasie.
- **Tryb Rozdzielczości Podglądu**: `[Bieżąca / 1080p / 720p]` — symuluje wygląd HUD w typowych rozdzielczościach ekranowych.

---

## 3. Lewy Panel (Lista Komponentów)
- **Wyszukiwarka na żywo**: dynamiczne filtrowanie elementów po wpisaniu frazy.
- **Kategorie**: szybkie przełączanie grup `Wszystkie`, `HUD`, `PvP`, `Survival`, `Visual`, `Other`.
- **Karty elementów**:
  - Przycisk szybkiego ukrywania/pokazywania `[👁]`.
  - Nazwa elementu i etykieta typu (`[HUD • SCREEN_SPACE]`).
  - Podświetlenie aktywnego zaznaczenia.
- **Możliwość zwijania panelu**: przycisk `[◀]` pozwala zwinąć panel do paska 24px, aby powiększyć obszar roboczy.

---

## 4. Centralny Canvas Roboczy
- **Siatka referencyjna**: delikatne linie co 20px wspomagające orientację przestrzenną.
- **Ramka rzutni Minecrafta**: precyzyjnie wyznaczone granice ekranu gry.
- **Bounding Boxy i Zaznaczenie**:
  - Zaznaczony element otoczony jest złotą ramką 2px (`#FFCC00`) z etykietą koordynatów i skali.
  - Najechany element (hover) podświetla się ramką błękitną (`#4A90E2`).
  - Niezaznaczone elementy posiadają subtelny obrys pomocniczy.
- **Realistyczny podgląd (Preview Mode)**: każdy element renderuje przykładowe, realistyczne dane testowe.

---

## 5. Prawy Panel (Property Inspector)
- **Informacje o elemencie**: Nazwa, kategoria, opis oraz klasyfikacja przestrzenna.
- **Wskazówki dla elementów trójwymiarowych**: dla elementów typu `WORLD_SPACE` (NameTags, TNT, TeamView) wyświetlany jest czytelny komunikat:
  *„Element świata 3D: Pozycja wynika z encji w grze”*.
- **Kontrolki parametrów**:
  - **Pozycja X / Y**: precyzyjna zmiana współrzędnych przyciskami `[-]` i `[+]`.
  - **Skala**: regulacja skali z krokiem 0.1x.
  - **Kotwica**: cykliczne przełączanie narożników ekranu (`TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`, `BOTTOM_RIGHT`, `BOTTOM_CENTER`, `BOTTOM_LEFT`, `CENTER`).
  - **Widoczność**: przełącznik włącz/wyłącz.
  - **Przywróć domyślne**: natychmiastowe zresetowanie wybranego elementu do wartości fabrycznych.

---

## 6. Dolny Pasek Akcji i Modal Potwierdzenia
- **Wskaźnik zmian**:
  - `● Masz niezapisane zmiany` (pomarańczowy, gdy edytowany bufor różni się od zapisanego układu).
  - `✓ Układ zsynchronizowany` (zielony, gdy brak zmian).
- **Przyciski**:
  - `[↺ Domyślne]` — reset całego układu do domyślnych współrzędnych.
  - `[Anuluj]` — wyjście bez zapisywania.
  - `[💾 Zapisz]` — zatwierdzenie i trwały zapis na dysk.
- **Modal bezpieczeństwa**: naciśnięcie ESC lub Anuluj przy niezapisanych zmianach wyświetla okno dialogowe zapobiegające przypadkowej utracie konfiguracji.
