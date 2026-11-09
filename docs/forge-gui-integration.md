# Integracja GUI w Minecraft Forge — ZombekForgeScreen

## 1. Wybór Mapowań i Architektura Renderowania

W ekosystemie Minecraft 1.20.4 Minecraft Forge korzysta domyślnie z **Official Mojang Mappings**. Różni się to od Yarn stosowanego w Fabric:

| Element | Fabric (Yarn) | Forge (Official Mappings) |
| :--- | :--- | :--- |
| **Klasa bazowa ekranu** | `net.minecraft.client.gui.screen.Screen` | `net.minecraft.client.gui.screens.Screen` |
| **Kontekst rysowania** | `net.minecraft.client.gui.DrawContext` | `net.minecraft.client.gui.GuiGraphics` |
| **Komponent tekstowy** | `net.minecraft.text.Text` | `net.minecraft.network.chat.Component` |
| **Pobieranie szerokości tekstu**| `mc.textRenderer.getWidth(text)` | `this.font.width(text)` |
| **Rysowanie prostokąta** | `context.fill(x1, y1, x2, y2, color)` | `guiGraphics.fill(x1, y1, x2, y2, color)` |
| **Rysowanie ramki** | `context.drawBorder(x, y, w, h, col)` | `guiGraphics.renderOutline(x, y, w, h, col)` |
| **Wsparcie dla suwaków/interakcji** | `mouseClicked`, `mouseDragged`, `mouseReleased` | `mouseClicked`, `mouseDragged`, `mouseReleased` |

Pomimo różnic w nazwach klas Mojang vs Yarn, logika renderowania i paleta barw zostały zachowane w 100% spójne.

---

## 2. Paleta i Motyw Festive Dark (`ForgeTheme`)

`ForgeTheme` odwzorowuje świąteczną paletę ZombekClient:

```java
public final class ForgeTheme {
    public static final int COLOR_BACKGROUND_OVERLAY = 0xD90A0E17; // Głęboka czerń z przezroczystością
    public static final int COLOR_CARD_BACKGROUND    = 0xEE111726; // Ciemny granat
    public static final int COLOR_CARD_HOVER         = 0xEE1A2238; // Podświetlenie karty
    public static final int COLOR_ACCENT_RED         = 0xFFE02B3A; // Świąteczna czerwień
    public static final int COLOR_ACCENT_GREEN       = 0xFF1DB954; // Świąteczna zieleń
    public static final int COLOR_ACCENT_GOLD        = 0xFFFFD700; // Złoto akcentów
    public static final int COLOR_TEXT_PRIMARY       = 0xFFFFFFFF; // Biały tekst główny
    public static final int COLOR_TEXT_SECONDARY     = 0xFF8A99AD; // Szary tekst pomocniczy
    public static final int COLOR_TOGGLE_ON          = 0xFF1DB954; // Przełącznik aktywny (zielony)
    public static final int COLOR_TOGGLE_OFF         = 0xFF3E4759; // Przełącznik nieaktywny
}
```

---

## 3. Komponenty Ekranu (`ZombekForgeScreen`)

Ekran podzielony jest na responsywne strefy:

1. **Górny Pasek (Header)**:
   - Tytuł: `ZombekClient — Panel Ustawień [Forge 1.20.4]`
   - Podtytuł: Informacja o aktualnej kategorii oraz liczbie włączonych modułów.
   - Pole wyszukiwania: Filtrowanie modułów na żywo po nazwie i opisie.
2. **Lewy Sidebar (Kategorie)**:
   - Przyciski 5 kategorii:
     - `PvP / Walka`
     - `HUD / Informacje`
     - `Wizualne`
     - `Survival / Świat`
     - `Inne / Narzędzia`
   - Oznaczenie aktywnej kategorii czerwonym wskaźnikiem i podświetleniem.
3. **Środkowa Lista Modułów**:
   - Karty modułów z ikonami statusu (ON/OFF).
   - Przycisk przełączania (Toggle).
   - Przycisk otwierania ustawień szczegółowych (Trybik / Szczegóły).
   - Czysty placeholder informacyjny w przypadku pustych kategorii ("Kategoria jest pusta w tym etapie").
4. **Prawy Panel Ustawień Modułu (Settings Panel)**:
   - Wyświetla listę ustawień powiązanych z wybranym modułem.
   - Obsługa typów:
     - `BooleanSetting`: Przycisk WŁ/WYŁ.
     - `NumberSetting`: Interaktywny suwak myszki z płynnym draggingiem i zaokrągleniem kroku.
     - `EnumSetting`: Przełącznik cykliczny wartości wyliczeniowych.
     - `ColorSetting`: Podgląd koloru RGBA i wartości szesnastkowej HEX.
     - `KeybindSetting`: Przycisk nasłuchiwania wciśnięcia nowego klawisza.
   - Respektowanie `setting.isVisible()` w celu ukrywania opcji zależnych.
5. **Dolny Pasek (Footer)**:
   - Wskazówka: `Wciśnij RIGHT SHIFT lub ESC aby zamknąć menu`.

---

## 4. Obsługa Klawiatury i Myszy

1. **Zamykanie ekranu**:
   - Wciśnięcie klawisza `ESC` lub `RIGHT SHIFT` wywołuje `onClose()`.
   - `onClose()` automatycznie wywołuje zapis konfiguracji do pliku JSON:
     ```java
     @Override
     public void onClose() {
         ZombekCore.getInstance().getConfigManager().save();
         super.onClose();
     }
     ```
2. **Dragging suwaków numerycznych**:
   - Metoda `mouseDragged` przelicza względną pozycję kursora myszy na przedział `[min, max]` z uwzględnieniem `step`, zapewniając precyzyjną i płynną regulację wartości parametrów.
