# NeoForge GUI Integration — ZombekClient In-Game Menu

## 1. Architektura GUI w Środowisku NeoForge

W środowisku **NeoForge 1.20.4**, podobnie jak w nowoczesnym Minecraft Forge, oficjalnym standardem mapowania klas i metod są **Mojang Official Mappings**. Zastępują one dawne mapowania SRG oraz różnią się nazewnictwem od Yarn (stosowanego w Fabric).

Klasa `ZombekNeoForgeScreen` rozszerza `net.minecraft.client.gui.screens.Screen` i wykorzystuje oficjalny kontekst renderowania `net.minecraft.client.gui.GuiGraphics`.

### Porównanie mapowań UI:

| Komponent / Funkcja | Fabric (Yarn Mappings) | NeoForge (Official Mappings) |
|---|---|---|
| Klasa bazowa ekranu | `net.minecraft.client.gui.screen.Screen` | `net.minecraft.client.gui.screens.Screen` |
| Kontekst renderowania 2D | `net.minecraft.client.gui.DrawContext` | `net.minecraft.client.gui.GuiGraphics` |
| Tekst / Tłumaczenia | `net.minecraft.text.Text` | `net.minecraft.network.chat.Component` |
| Klawisze i wejście | `net.minecraft.client.option.KeyBinding` | `net.minecraft.client.KeyMapping` |
| Rysowanie prostokąta | `context.fill(x1, y1, x2, y2, color)` | `guiGraphics.fill(x1, y1, x2, y2, color)` |
| Rysowanie tekstu z cieniem | `context.drawTextWithShadow(renderer, text, x, y, color)` | `guiGraphics.drawString(font, text, x, y, color, true)` |
| Rysowanie wyśrodkowanego tekstu | `context.drawCenteredTextWithShadow(renderer, text, x, y, color)` | `guiGraphics.drawCenteredString(font, text, x, y, color)` |

---

## 2. Motyw Wizualny Festive Dark (`NeoForgeTheme`)

Podobnie jak w wersjach Fabric i Forge, interfejs NeoForge korzysta ze spójnych tokenów graficznych motywu **Festive Dark**:

```java
public final class NeoForgeTheme {
    public static final int BG_OVERLAY      = 0xD00A0E17; // Półprzezroczyste tło ekranu
    public static final int PANEL_BG        = 0xF0101826; // Panel boczny i główne kontenery
    public static final int PANEL_BORDER    = 0x4038BDF8; // Subtelna lodowa ramka
    public static final int CARD_BG         = 0xE0172033; // Karta modułu (nieaktywna)
    public static final int CARD_BG_ENABLED = 0xE01A2E44; // Karta modułu (aktywna)
    public static final int ACCENT_CYAN     = 0xFF38BDF8; // Akcent błękitny / lodowy
    public static final int ACCENT_GREEN    = 0xFF4ADE80; // Włączony przełącznik / sukces
    public static final int ACCENT_RED      = 0xFFF87171; // Wyłączony moduł / błąd
    public static final int TEXT_PRIMARY    = 0xFFF1F5F9; // Tekst główny biały
    public static final int TEXT_MUTED      = 0xFF94A3B8; // Tekst pomocniczy szary
}
```

---

## 3. Komponenty i Układ Ekranu

`ZombekNeoForgeScreen` realizuje w pełni responsywny układ dostosowujący się do skali interfejsu (GUI Scale 1x - 4x) oraz rozdzielczości okna gry:

1. **Górny Pasek Nagłówka (Top Bar)**:
   - Tytuł `ZOMBEKCLIENT` z lodową gwiazdką `❄`.
   - Etykieta wersji (`v0.1.0-alpha [NeoForge]`).
   - Pole wyszukiwania modułów na żywo z filtrowaniem po nazwie i opisie.
2. **Lewy Panel Kategorii (Sidebar)**:
   - 5 zakładek: `PVP`, `HUD`, `VISUAL`, `SURVIVAL`, `OTHER`.
   - Liczniki modułów dla każdej kategorii (np. `OTHER (1)`).
   - Wskaźnik aktywnej kategorii z podświetleniem `ACCENT_CYAN`.
3. **Główny Obszar Modułów (Content Area)**:
   - Dynamiczna siatka kart modułów (moduł aktywny/nieaktywny, opis, wersja, przełącznik ON/OFF, przycisk ustawień `⚙`, skrót klawiszowy).
   - Obsługa stanu błędu modułu (`hasError()`) z czerwoną ikoną ostrzegawczą i komunikatem.
   - Puste kategorie wyświetlają świąteczny placeholder (`Brak aktywnych modułów w tej kategorii ❄`).
4. **Szuflada Ustawień Modułu (Settings Drawer)**:
   - Po kliknięciu w zębatkę `⚙` modułu otwiera się dedykowany panel ustawień.
   - Obsługa wszystkich typów kontrolek:
     - `BooleanSetting`: interaktywny przełącznik [ON] / [OFF].
     - `NumberSetting`: suwak z możliwością przeciągania myszą, precyzją kroku oraz wartościami min/max.
     - `EnumSetting`: przycisk cyklicznej zmiany opcji.
     - `ColorSetting`: podgląd koloru i heksadecymalna reprezentacja.
     - `StringSetting`: podgląd tekstu z informacją o limitach znaków.
     - `KeybindSetting`: bezpośrednie przypisywanie klawisza.
5. **Modal Przypisywania Klawisza (Keybind Capture)**:
   - Kliknięcie przycisku `[BIND: ...]` wprowadza ekran w tryb oczekiwania na naciśnięcie dowolnego klawisza.
   - Naciśnięcie `ESCAPE` usuwa przypisanie (`NONE`), naciśnięcie innego klawisza rejestruje nowy kod GLFW.

---

## 4. Bezpieczeństwo Rozgrywki Sieciowej

Metoda `shouldPause()` w `ZombekNeoForgeScreen`:

```java
@Override
public boolean isPauseScreen() {
    return false;
}
```

Dzięki temu otwarcie menu klienta na serwerach wieloosobowych (multiplayer) nie zatrzymuje czasu gry i pozwala na płynną rozgrywkę oraz podgląd otoczenia w tle.
