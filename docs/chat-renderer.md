# Custom Chat Renderer

Dokumentacja silnika renderowania okna czatu w **ZombekClient** (`CustomChatRenderer`).

## 1. Architektura Renderera

Silnik `CustomChatRenderer` implementuje niezależny od konkretnego loadera potok renderowania bazujący na abstrakcji `IHudRenderer`:
- Obsługa Fabric i Quilt za pośrednictwem `FabricHudRenderer` / `QuiltHudRenderer` (`DrawContext` Yarn).
- Obsługa Forge i NeoForge za pośrednictwem `ForgeHudRenderer` / `NeoForgeHudRenderer` (`GuiGraphics` Mojang).

## 2. Warstwy Renderowania

Podczas wywołania `render(...)` renderer przetwarza następujące warstwy w kolejności od tła do pierwszego planu:

1. **Warstwa Tła i Obramowania**:
   - Wyliczenie wymiarów dynamicznych w oparciu o stan skupienia (Focused / Unfocused) i liczbę wyświetlanych wierszy.
   - Płynna przezroczystość z parametru `background_opacity` z adaptacją do wybranego presetu (np. `GLASS`, `MODERN`, `MINIMAL`).
   - Rysowanie zewnętrznej ramki lub linii oddzielających sekcje okna.

2. **Warstwa Kart Kanałów (Chat Tabs Bar)**:
   - Rysowanie zakładek kanałów (`Wszystko`, `Ogólny`, `Prywatne`, `Party`, `Drużyna`, `Gildia`, `System`, karty niestandardowe).
   - Etykiety nieprzeczytanych wiadomości (Unread Badge) z liczbą nowych wpisów i dedykowanym kolorem akcentu.
   - Wyróżnienie aktywnej karty z wskaźnikiem podświetlenia.

3. **Warstwa Wiadomości (Message List)**:
   - Formatowanie znaczników czasu wg wybranego profilu (`HH:mm`, `HH:mm:ss`, itp.).
   - Znaczniki powtórzonych wiadomości z licznikiem (`×2`, `×3`).
   - Indywidualne kolory wierszy zależne od kategorii (`ChatCategory`) lub reguły filtra.
   - Wyróżnienia (Highlight) i wskaźniki ważności (Important).

4. **Warstwa Pola Wprowadzania (Custom Chat Input Field)**:
   - Wyświetlana przy otwartym ekranie czatu (`chatOpen == true`).
   - Płynny kursor tekstowy, obsługa tekstu zastępczego (Placeholder: *"Napisz wiadomość na kanale..."*).
   - Dynamiczne zapamiętywanie szkiców (Drafts) per zakładka.
