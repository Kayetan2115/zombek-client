# ZombekClient — PvP Info Module

## 1. Wprowadzenie i Przeznaczenie

Moduł **PvP Info** (`PvPInfoModule`, ID: `pvp_info`) w kategorii `Category.PVP` agreguje i prezentuje na ekranie kluczowe statystyki z trwającej potyczki PvP w czasie rzeczywistym. 

Panel zorganizowany jest w trzy logiczne sekcje:
1. **MELEE (Walka w zwarciu)**: Liczba wyprowadzonych ataków, zaobserwowane trafienia, procentowa celność (accuracy / hit rate), bieżący ciąg trafień (combo) oraz średni zasięg (avg reach).
2. **PROJECTILE (Broń miotana)**: Liczba wystrzelonych pocisków, trafienia, skuteczność oraz typ ostatnio użytej broni (Strzały, Śnieżki, Jajka, Trójzęby, Mikstury, Spławik).
3. **HEALTH (Życie i obrażenia)**: Aktualne punkty życia gracza (HP + absorpcja), otrzymane obrażenia w sesji, oraz punkty życia aktualnie namierzanego przeciwnika (lub `N/A`, jeśli serwer nie przesyła tych danych).

---

## 2. Zarządzanie Sesją (`PvPSessionManager`)

Dane analityczne są grupowane w obiektach `PvPSession` zarządzanych przez wątkowo bezpieczny menedżer `PvPSessionManager`:
- **Automatyczny cykl życia sesji**: Nowa sesja rozpoczyna się przy pierwszym ataku lub otrzymaniu obrażeń.
- **Auto-Reset**:
  - Przy śmierci gracza (`onPlayerDeath(true)`).
  - Przy zmianie świata, wymiaru lub rozłączeniu (`onWorldChange(true)`).
  - Po upływie zdefiniowanego czasu bezczynności (timeout).
- **Ręczny reset**: Bezpośrednie wywołanie `resetSession()` (np. poprzez przycisk w menu ustawień modułu).

---

## 3. Style Prezentacji HUD

Moduł obsługuje 5 wariantów układu (`PvPInfoStyle`):
- **`COMPACT`**: Skrócony, zwięzły widok najważniejszych metryk (Combo, Hits, Accuracy %, HP).
- **`DETAILED`**: Pełny wieloliniowy raport z nagłówkami sekcji MELEE, PROJECTILE i HEALTH.
- **`HORIZONTAL`**: Poziomy pasek statystyk ułożonych jedna za drugą, idealny na górną lub dolną krawędź ekranu.
- **`VERTICAL`**: Klasyczny pionowy stos statystyk.
- **`MINIMAL`**: Wyłącznie kluczowe liczby (np. `4 Hits | 80% | 18 HP`).

---

## 4. Opcje Konfiguracyjne Modułu

| Ustawienie | Klucz / ID | Typ | Wartość domyślna | Opcje | Opis |
|---|---|---|---|---|---|
| **Display Style** | `display_style` | Enum | `DETAILED` | 5 stylów | Układ graficzny widgetu. |
| **Show Melee** | `show_melee` | Boolean | `true` | `true`, `false` | Włączenie sekcji ataku wręcz. |
| **Show Projectile** | `show_projectile` | Boolean | `true` | `true`, `false` | Włączenie sekcji pocisków. |
| **Show Health** | `show_health` | Boolean | `true` | `true`, `false` | Włączenie sekcji zdrowia i obrażeń. |
| **Show Target Health**| `show_target_health`| Boolean | `true` | `true`, `false` | Wyświetlanie HP celu (gdy dostępne). |
| **Reset Session** | `reset_session` | Boolean | `false` | `true`, `false` | Przycisk akcji resetu bieżącej sesji statystyk. |
| **Header Color** | `header_color` | Color | `#FF5555` | Format ARGB/HEX | Kolor nagłówków sekcji. |
| **Label Color** | `label_color` | Color | `#AAAAAA` | Format ARGB/HEX | Kolor etykiet parametrów. |
| **Value Color** | `value_color` | Color | `#FFFFFF` | Format ARGB/HEX | Kolor wartości liczbowych. |
| **Background** | `background` | Boolean | `true` | `true`, `false` | Tło panelu HUD. |
| **Padding** | `padding` | Number | `4` | `0` – `20` px | Margines wewnętrzny ramki. |
| **X / Y Position** | `x` / `y` | Number | `10` / `210` | Dowolna | Koordynaty panelu na ekranie. |
