# ZombekClient Core — Architektura Modyfikacji Klienta (Fabric 1.20.4)

## 1. Informacje Ogólne

- **Identyfikator Moda (MOD_ID):** `zombekclient`
- **Nazwa Klienta:** `ZombekClient`
- **Wersja:** `0.1.0-alpha`
- **Wspierany Loader:** `fabric`
- **Docelowy Minecraft:** `1.20.4`
- **Loom:** `1.6.12` / Java 17

---

## 2. Punkty Wejścia i Inicjalizacja

Modyfikacja jest ładowana w procesie klienta Minecraft przez wpis `client` w `fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "zombekclient",
  "version": "0.1.0-alpha",
  "name": "ZombekClient",
  "entrypoints": {
    "client": [
      "pl.zombek.client.core.ZombekClientMod"
    ]
  }
}
```

Podczas uruchamiania (`onInitializeClient()`):
1. **Rejestracja Modułów:** Inicjalizowany jest `ModuleManager` i rejestrowany moduł referencyjny `CoreTestModule`.
2. **Konfiguracja:** Wyznaczany jest katalog gry przez `FabricLoader.getInstance().getGameDir()`, inicjalizowany `ModuleConfigManager` i wczytywana konfiguracja `config/zombekclient/modules.json`.
3. **Skróty Klawiszowe:** `KeybindManager` rejestruje klawisz otwierania menu `RIGHT SHIFT` w Fabric API.
4. **Pętla Zdarzeń:** Rejestrowany jest nasłuchiwacz `ClientTickEvents.END_CLIENT_TICK` dla modułów i skrótów klawiszowych.
5. **Shutdown Hook:** Rejestrowany jest wątek bezpiecznego zapisu stanu przy zamykaniu JVM.

---

## 3. Podsystemy Modułu

| Pakiet | Klasa Główna | Rola |
|--------|--------------|------|
| `pl.zombek.client.core` | `ZombekClientMod` | Bootstrap, koordynacja podsystemów |
| `pl.zombek.client.modules` | `ModuleManager`, `AbstractModule` | Zarządzanie modułami, cykl życia, kategoryzacja |
| `pl.zombek.client.modules.impl` | `CoreTestModule` | Referencyjna implementacja ze wszystkimi typami ustawień |
| `pl.zombek.client.settings` | `Setting<T>`, typy potomne | Silnie typowane ustawienia z warunkowością |
| `pl.zombek.client.keybind` | `KeybindManager` | Skróty klawiszowe, Right Shift, detekcja konfliktów |
| `pl.zombek.client.config` | `ModuleConfigManager` | Trwałość JSON v1, atomowy zapis, debouncing 300ms |
| `pl.zombek.client.gui` | `ZombekMenuScreen`, `ZombekTheme` | Świąteczne menu w grze, Yarn Screen/DrawContext |

---

## 4. Wytyczne dla Kolejnych Etapów (Etap 7C+)

1. **Nowe Moduły:** Każdy nowy moduł (np. Keystrokes, CPS, Fullbright) musi dziedziczyć po `AbstractModule`, definiować metadane `ModuleMetadata` i rejestrować swoje parametry przez `registerSetting()`.
2. **Izolacja Błędów:** Nigdy nie należy usuwać ani omijać mechanizmu `safeTick()` / `safeRender()` — pętla gry musi pozostać w 100% odporna na błędy w kodzie pojedynczych modułów.
3. **Brak Surowego OpenGL:** Rysowanie interfejsu i elementów HUD musi odbywać się przez oficjalny Minecraft Yarn `DrawContext`, co gwarantuje kompatybilność z silnikami Sodium / Iris / Canvas.
