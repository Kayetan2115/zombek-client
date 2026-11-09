# Niedestrukcyjna Migracja Konfiguracji HUD (`HudConfigMigrator`)

## 1. Kontekst i Założenia

Przed Etapem 9A moduły informacyjne i PvP (np. `PingModule`, `FpsModule`, `CoordinatesModule`, `ScoreboardModule`, `NetGraphModule`) zapisywały pozycję i skalę jako indywidualne opcje w pliku `config/zombekclient/modules.json`:
- `pos_x` (NumberSetting)
- `pos_y` (NumberSetting)
- `anchor` (EnumSetting<HudAnchor>)
- `scale` (NumberSetting)

Wprowadzenie centralnego pliku `hud_layout.json` stwarzało ryzyko utraty dotychczasowych preferencji gracza lub wymuszenia resetu.

---

## 2. Działanie `HudConfigMigrator`

Klasa [`HudConfigMigrator`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/editor/HudConfigMigrator.java) implementuje bezpieczną, niedestrukcyjną procedurę importu:

1. Podczas inicjalizacji klienta sprawdzana jest obecność pliku `config/zombekclient/hud_layout.json`.
2. Jeśli plik nie istnieje (pierwsze uruchomienie z edytorem):
   - Menedżer iteruje po wszystkich modułach zarejestrowanych w `ModuleManager`.
   - Wyszukuje opcje o identyfikatorach lub nazwach: `pos_x`, `pos_y`, `anchor`, `scale`.
   - Wartości te są mapowane na obiekt `HudComponentState` dla danego komponentu w nowym układzie `HudLayout`.
   - Jeśli moduł nie był konfigurowany, pobierane są wartości fabryczne z `HudComponentRegistry`.
   - Nowy plik `hud_layout.json` jest zapisywany na dysku.
3. **Plik `modules.json` pozostaje całkowicie nienaruszony**: migracja nie usuwa ani nie modyfikuje żadnych istniejących ustawień.

---

## 3. Gwarancje Spójności i Weryfikacja

- Wszystkie zaimportowane koordynaty podlegają sanityzacji przez [`HudLayoutValidator`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/editor/HudLayoutValidator.java).
- Działanie zostało zweryfikowane automatycznym testem jednostkowym [`HudConfigMigratorTest`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/test/java/pl/zombek/client/core/hud/editor/HudConfigMigratorTest.java).
