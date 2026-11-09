# Moduł Bedwars Team Board — ZombekClient

## 1. Przegląd i przeznaczenie

Komponent `BedwarsTeamBoard` odpowiada za dynamiczną prezentację stanu wszystkich zespołów biorących udział w meczu Bedwars w czytelnym i konfigurowalnym panelu HUD.

Śledzi on:
- Stan łóżka każdej drużyny (`BED_ALIVE`, `BED_DESTROYED`, `TEAM_ELIMINATED`, `UNKNOWN`).
- Szacowaną lub jawną liczbę żyjących graczy zespołu.
- Kolor i nazwę drużyny (zgodnie z 8 oficjalnymi barwami Bedwars).
- Wyróżnienie drużyny lokalnego gracza (`(You)` / `(Ty)`).
- Ostatnie zarejestrowane zdarzenie powiązane z zespołem (np. gracz który zniszczył łóżko).

---

## 2. Obsługiwane drużyny i kolory

Moduł wspiera pełną paletę 8 drużyn Bedwars z predefiniowanymi kolorami HEX ARGB:

| Kod | Drużyna (EN) | Drużyna (PL) | Kolor HEX | Tło ARGB |
| :---: | :--- | :--- | :---: | :---: |
| **R** | Red | Czerwoni | `#FF5555` | `0x88FF5555` |
| **B** | Blue | Niebiescy | `#5555FF` | `0x885555FF` |
| **G** | Green | Zieloni | `#55FF55` | `0x8855FF55` |
| **Y** | Yellow | Żółci | `#FFFF55` | `0x88FFFF55` |
| **A** | Aqua | Błękitni | `#55FFFF` | `0x8855FFFF` |
| **W** | White | Biali | `#FFFFFF` | `0x88FFFFFF` |
| **P** | Pink | Różowi | `#FF55FF` | `0x88FF55FF` |
| **S** | Gray | Szarzy | `#AAAAAA` | `0x88AAAAAA` |

---

## 3. Dostępne układy panelu drużyn (TeamBoardLayout)

Gracz może w ustawieniach Right Shift wybrać jeden z 6 dedykowanych stylów prezentacji:

### 1. `COMPACT` (Kompaktowy)
Wyświetla pojedynczą literę kodu drużyny wraz ze zwięzłym statusem łóżka lub liczbą graczy:
`[R] ✓  [B] ✕  [G] [E]  [Y] 2`

### 2. `DETAILED` (Szczegółowy)
Pełna nazwa drużyny, ikona statusu, liczba żywych członków oraz wskaźnik drużyny własnej:
- `Czerwoni: ✓ (Ty) - 4 graczy`
- `Niebiescy: ✕ - 2 graczy`
- `Zieloni: [E] - Wyeliminowani`

### 3. `HORIZONTAL` (Pasek poziomy)
Płaski wiersz drużyn umieszczony u góry lub dołu ekranu — optymalny dla graczy ceniących maksymalne pole widzenia.

### 4. `VERTICAL` (Kolumna pionowa)
Klasyczna pionowa lista umieszczona po lewej lub prawej stronie ekranu.

### 5. `ICON` (Tylko ikony)
Minimalistyczne kolorowe kwadraty z symbolami `✓`, `✕` lub cyfrą ocalałych graczy.

### 6. `TEXT` (Czysty tekst)
Tekst bez kolorowych prostokątów tła, idealnie komponujący się ze standardowym czatem i scoreboardem.

---

## 4. Formatowanie statusu łóżka i ikon (BedIconStyle)

| Styl ikony | Łóżko aktywne | Łóżko zniszczone | Drużyna wyeliminowana |
| :--- | :---: | :---: | :---: |
| **CHECK_CROSS** (Domyślny) | `✓` | `✕` / liczba graczy | `[E]` |
| **BED_CROSS** | `⚑` | `✕` / liczba graczy | `☠` |
| **LETTERS** | `ALIVE` | `DEAD` | `OUT` |

---

## 5. Ukrywanie drużyn wyeliminowanych (`hideEliminatedTeams`)

Gdy opcja `hideEliminatedTeams` jest włączona w menu Right Shift, drużyny o statusie `TEAM_ELIMINATED` są automatycznie usuwane z renderowanego panelu, co pozwala zaoszczędzić miejsce na ekranie w późniejszych fazach meczu.
Drużyna gracza jest zawsze wyświetlana na samej górze lub z wyraźnym wyróżnieniem graficznym (`highlightMyTeam`).
