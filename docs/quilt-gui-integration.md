# Quilt GUI Integration — Świąteczne Menu ZombekClient

## 1. Architektura Interfejsu

Klasa `ZombekQuiltScreen` rozszerza natywną klasę `net.minecraft.client.gui.screen.Screen` i renderuje pełnoekranowe, responsywne menu konfiguracji ZombekClient w mapowaniach Yarn dla Minecraft 1.20.4 (`DrawContext`, `Screen`, `Text`).

### Główne Założenia:
1. **Brak pauzy gry**: metoda `shouldPause()` zwraca `false`, umożliwiając konfigurację na serwerach multiplayer.
2. **Klawisz otwierania**: Domyślnie **RIGHT SHIFT (`GLFW.GLFW_KEY_RIGHT_SHIFT`)**. Ponowne naciśnięcie klawisza RIGHT SHIFT lub klawisza ESCAPE natychmiast zamyka menu i przywraca kursor do gry.
3. **Motyw Festive Dark**: spójny design tokenowy zdefiniowany w klasie `QuiltTheme`:
   - Głębokie tło: `0xEE0B0F19` (półprzezroczysty zintegrowany gradient)
   - Kolor akcentu (szmaragdowy): `0xFF10B981`
   - Akcent świąteczny (karmazynowy): `0xFFEF4444`
   - Świąteczne złoto: `0xFFF59E0B`
   - Karta modułu (tło): `0xFF161E2E`
   - Karta modułu (aktywna): `0xFF1E293B`

---

## 2. Elementy Ekranu i Układ Wizualny

Menu podzielone jest na wyraźne strefy:

```
+-----------------------------------------------------------------------------------+
|  [Ikona] ZOMBEKCLIENT v0.1.0-alpha [QUILT]     [Szukaj modułu...]         [ X ]   |
+-----------------------------------------------------------------------------------+
|  [WSZYSTKIE]  [PVP]  [HUD]  [RENDER]  [RUCH]  [INNE]                              |
+-----------------------------------------------------------------------------------+
|                                                     | [SZCZEGÓŁY MODUŁU]          |
|  +--------------------+   +--------------------+    | Wybrany: Core Test Module   |
|  | [ON] Test Module   |   | [OFF] CPS Counter  |    | Kategoria: Inne             |
|  | Klawisz: [ P ]     |   | Klawisz: [ BRAK ]  |    | Skrót: [ P ] (Kliknij, aby  |
|  | Pokaż ustawienia > |   | Pokaż ustawienia > |    |               zmienić)      |
|  +--------------------+   +--------------------+    | --------------------------- |
|                                                     | USTAWIENIA:                 |
|  +--------------------+   +--------------------+    | Opacity: [======|---] 85%   |
|  | [OFF] Keystrokes   |   | [ON] Armor Status  |    | Display: [ EXPANDED v ]     |
|  | Klawisz: [ BRAK ]  |   | Klawisz: [ K ]     |    |                             |
|  +--------------------+   +--------------------+    | [ Zapisz konfigurację ]     |
+-----------------------------------------------------------------------------------+
|  Wskazówka: Naciśnij RIGHT SHIFT lub ESC, aby zamknąć menu                        |
+-----------------------------------------------------------------------------------+
```

---

## 3. Funkcjonalności Menu

1. **Filtrowanie według kategorii**:
   Zakładki `WSZYSTKIE`, `PVP`, `HUD`, `RENDER`, `MOVEMENT`, `OTHER`.
2. **Wyszukiwarka na żywo**:
   Pole tekstowe wyszukiwania filtrujące moduły po nazwie, identyfikatorze oraz opisie w czasie rzeczywistym.
3. **Przełączanie modułów (Toggle)**:
   Kliknięcie przycisku stanu przełącza `module.toggle()`, natychmiast wywołując `onEnable()` lub `onDisable()`, odświeża stan wizualny i zapisuje zmiany do pamięci podręcznej konfiguratora.
4. **Boczny panel ustawień (Drawer)**:
   Kliknięcie karty modułu otwiera prawy panel z pełnym wsparciem dla typowanych kontrolek:
   - `BooleanSetting`: interaktywny przełącznik ON/OFF
   - `NumberSetting`: suwak wartości (Slider) z obsługą przeciągania myszą i walidacją zakresu min/max/step
   - `EnumSetting`: cykliczny selektor opcji
   - `StringSetting`: pole edycji tekstu
   - `ColorSetting`: wskaźnik próbki koloru
5. **Przechwytywanie skrótów klawiszowych (Keybind Capture)**:
   Kliknięcie przycisku przypisania klawisza wprowadza moduł w tryb nasłuchiwania (`listeningKeybind = true`). Naciśnięcie dowolnego klawisza (lub ESC/Delete w celu usunięcia przypisania) natychmiast aktualizuje przypisanie w `KeybindSetting` i weryfikuje konflikty w `QuiltKeybindManager`.
