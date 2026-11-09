# ZombekClient — Menu w Grze (In-Game Configuration GUI, Etap 7B)

## 1. Wygląd i Estetyka (Zombek Festive Theme)

Menu w grze (`pl.zombek.client.gui.ZombekMenuScreen`) zaimplementowano w świątecznej, ciemnej estetyce (Festive Dark Theme) spójnej z marką Zombek:

- **Tło / Półprzezroczysty Overlay**: Głęboki granat/grafit `0xAA020617` przyciemniający świat gry.
- **Okno Główne**: Ciemny łupek (`0xF50F172A`) z subtelnym obramowaniem (`#334155`).
- **Sidebar (Lewy Panel)**: Odcień nocy (`0xF80B1120`).
- **Główny Akcent**: Szmaragdowa zieleń świerkowa (`#10B981`) dla aktywnych modułów i przełączników.
- **Akcent Świąteczny**: Świąteczne złoto (`#F59E0B`) dla brandingowych ikon, skrótów i nagłówków.
- **Akcent Alarmowy**: Świąteczna czerwień (`#EF4444`) dla błędów i trybu oczekiwania na klawisz.
- **Teksty**: Wyrazisty śnieżnobiały (`#F8FAFC`) oraz stonowany szaroniebieski (`#94A3B8`).

---

## 2. Responsywność i Skalowanie

- Menu dopasowuje się automatycznie do rozdzielczości ekranu i bieżącej skali Minecraft GUI Scale:
  - Szerokość: `Math.min(760, Math.max(480, screenWidth - 40))`
  - Wysokość: `Math.min(480, Math.max(340, screenHeight - 40))`
- Okno pozostaje wycentrowane na ekranie niezależnie od wymiarów monitora.
- Wykorzystuje natywne przycinanie widoku (`DrawContext.enableScissor / disableScissor`), dzięki czemu zawartość przewijana nie wystaje poza krawędzie panelu.

---

## 3. Struktura Interfejsu

### 3.1. Lewy Panel Boczny (Sidebar)
- **Górna sekcja**: Logo `⚡ ZOMBEKCLIENT` oraz informacja o wersji (`v0.1.0-alpha`).
- **Zakładki kategorii**:
  - `⚔ PvP / Combat`
  - `📊 HUD / Information`
  - `👁 Visual`
  - `🌲 Survival / World`
  - `⚙ Other`
  - `⚙ Opcje Klienta`
- Licznik modułów dostępnych w danej kategorii wyświetlany w złotym badge'u.

### 3.2. Górny Pasek (Topbar)
- Nazwa wybranej kategorii lub modułu.
- **Wyszukiwarka na żywo**: Pole `🔍 Szukaj...` z natychmiastowym filtrowaniem modułów we wszystkich kategoriach.

### 3.3. Główny Obszar Treści (Content Area)

#### A. Widok Listy Modułów
Dla każdego modułu renderowana jest karta zawierająca:
1. Kolorowy pasek stanu (zielony: aktywny, szary: nieaktywny, czerwony: błąd).
2. Tytuł modułu oraz opcjonalny znacznik `[BŁĄD - Moduł wstrzymany]`.
3. Opis modułu z inteligentnym zawijaniem i skracaniem tekstu.
4. **Przycisk skrótu klawiszowego**: Wyświetla aktualny klawisz w formacie `[KLAWISZ]`. Kliknięcie otwiera modal przechwytywania klawisza.
5. **Przycisk "⚙ Opcje"**: Dostępny dla modułów posiadających dodatkowe ustawienia. Przełącza widok na panel konfiguracji modułu.
6. **Przełącznik zasilania ON / OFF**: Interaktywny przycisk zmieniający stan modułu na żywo.

Gdy kategoria jest pusta, wyświetlany jest dedykowany placeholder ze śnieżynkami i informacją o kolejnych etapach.

#### B. Widok Ustawień Modułu (Settings View)
- Przycisk `← Powrót` do listy modułów.
- Podsumowanie modułu (kategoria, autor, wersja).
- Lista interaktywnych kontrolek:
  - Przełączniki WŁĄCZONE / WYŁĄCZONE
  - Suwaki z paskiem postępu i wartością liczbową
  - Podgląd koloru i heksadecymalny selektor
  - Przyciski cykliczne dla list wyliczeniowych (Enum)
  - Przechwytywacze klawiszy

### 3.4. Dolny Pasek Statusu (Bottombar)
- Informacja o środowisku: `ZombekClient v0.1.0-alpha | Minecraft 1.20.4 | Loader: fabric`.
- Wskazówka sterowania: `ESC / RIGHT SHIFT aby zamknąć`.
- Menu nie pauzuje rozgrywki (`shouldPause() == false`), co pozwala na bezproblemową konfigurację na serwerach wieloosobowych.
