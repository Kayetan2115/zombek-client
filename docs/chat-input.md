# Custom Chat Input

Dokumentacja pola wprowadzania wiadomości (Custom Chat Input) w **ZombekClient** (`ChatInputManager`).

## 1. Założenia i Architektura

Standardowe pole wprowadzania w Minecraft traci wpisany tekst w momencie przypadkowego zamknięcia ekranu czatu (klawiszem Escape) lub przełączenia karty.
Silnik `ChatInputManager` eliminuje ten problem poprzez:
- **Zapamiętywanie szkiców (Draft Preservation)**: Każda karta kanału (`all`, `general`, `private`, `party`, `guild`, itp.) posiada niezależny bufor wersji roboczej.
- **Odporność na zamknięcie GUI**: Zamknięcie ekranu czatu nie czyści wpisanego tekstu — po ponownym otwarciu czatu tekst zostaje natychmiast przywrócony.

## 2. Wygląd i Personalizacja

- Konfigurowalna przezroczystość tła pola edycji.
- Dedykowany kursor tekstowy z płynną animacją pulsowania.
- Wskazówka kontekstowa (Placeholder) informująca, na którym kanale gracz aktualnie pisze.
- Wsparcie dla standardowych skrótów klawiszowych (Ctrl+A, Ctrl+C, Ctrl+V, strzałki historii góra/dół).
