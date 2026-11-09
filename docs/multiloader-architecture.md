# Architektura Multiloader — ZombekClient

## 1. Koncepcja Architektoniczna

ZombekClient In-Game Client został zaprojektowany w oparciu o czysty wzorzec **Multiloader Common Core + Loader Adapters**:

```
                               ┌───────────────────────────────────┐
                               │      ZombekClient Core (:core)    │
                               │  - ModuleManager                  │
                               │  - AbstractModule & IClientModule │
                               │  - Setting<T> & SettingGroup      │
                               │  - ModuleConfigManager (v1 JSON)  │
                               │  - ZombekCore bootstrap           │
                               │  - IPlatformAdapter (interfejs)   │
                               └─────────────────┬─────────────────┘
                                                 │
        ┌────────────────────────┬───────────────┴────────┬────────────────────────┐
        ▼                        ▼                        ▼                        ▼
 ┌───────────────┐        ┌───────────────┐        ┌───────────────┐        ┌───────────────┐
 │Fabric Adapter │        │ Forge Adapter │        │NeoForge Adapt.│        │ Quilt Adapter │
 │  (:fabric)    │        │   (:forge)    │        │  (:neoforge)  │        │   (:quilt)    │
 └───────┬───────┘        └───────┬───────┘        └───────┬───────┘        └───────┬───────┘
         │                        │                        │                        │
         ▼                        ▼                        ▼                        ▼
zombekclient-fabric.jar  zombekclient-forge.jar   zombekclient-neoforge.jar zombekclient-quilt.jar
```

---

## 2. Granice Zależności (Separation of Concerns)

| Obszar | ZombekClient Core (`:core`) | Fabric Adapter (`:fabric`) | Forge Adapter (`:forge`) | NeoForge Adapter (`:neoforge`) | Quilt Adapter (`:quilt`) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Zależności Minecraft** | **Brak (0 importów MC)** | `net.fabricmc.*`, `net.minecraft.*` (Yarn) | `net.minecraftforge.*`, `net.minecraft.*` (Mojang) | `net.neoforged.*`, `net.minecraft.*` (Mojang) | `org.quiltmc.*`, `net.minecraft.*` (Yarn) |
| **System Mapowań** | Brak (Czysta Java 17) | Yarn Mappings | Official Mojang Mappings | Official Mojang Mappings | Yarn Mappings |
| **Cykl Życia** | `IClientModule`, `safeEnable`, `safeDisable` | `ClientModInitializer` | `@Mod("zombekclient")`, `FMLClientSetupEvent` | `@Mod("zombekclient")`, `IEventBus` | `PreLaunchEntrypoint`, `ClientModInitializer` |
| **System Zdarzeń** | Wewnętrzny dispatch (`onClientTick`, `onRenderHud`) | `ClientTickEvents.END_CLIENT_TICK` | `MinecraftForge.EVENT_BUS`, `TickEvent` | `NeoForge.EVENT_BUS`, `TickEvent.ClientTickEvent` | `MinecraftClientMixin` (tick injection) |
| **Interfejs Graficzny** | Brak (Model danych i ustawień) | `Screen`, `DrawContext` | `Screen`, `GuiGraphics` | `Screen`, `GuiGraphics` | `Screen`, `DrawContext` |
| **Rejestracja Klawiszy** | Logika wyzwalania, stan wciśnięć | `KeyBindingHelper.registerKeyBinding` | `RegisterKeyMappingsEvent` | `RegisterKeyMappingsEvent` | `GameOptions.allKeys` + GLFW Edge Detection |
| **Konfiguracja** | `config/zombekclient/modules.json` (GSON) | Zapisuje do `<run>/config/zombekclient` | Zapisuje do `FMLPaths.CONFIGDIR/zombekclient` | Zapisuje do `FMLPaths.CONFIGDIR/zombekclient` | Zapisuje do `QuiltLoader.getConfigDir()/zombekclient` |
| **Format Manifestu** | Brak | `fabric.mod.json` | `META-INF/mods.toml` | `META-INF/neoforge.mods.toml` & `mods.toml` | `quilt.mod.json` |

---

## 3. Współdzielenie Kodu i Izolacja Błędów

1. **Jedno źródło prawdy dla logiki**:
   Cała logika modułów, sprawdzanie warunków widoczności ustawień (`visibleWhen`), walidacja wartości, limity numeryczne, bezpieczne wykonywanie (`safeTick`, `safeRender`) oraz zapis/odczyt konfiguracji znajdują się w `:core`. Wprowadzenie nowego modułu lub ustawienia w `:core` natychmiast działa identycznie we wszystkich loaderach.
2. **Izolacja awarii modułów (Error Boundary)**:
   Gdy moduł rzuci wyjątek w `onTick()` lub `onRenderHud()`, `ModuleManager` wychwytuje błąd, rejestruje stacktrace w logach i bezpiecznie dezaktywuje wyłącznie ten jeden wadliwy moduł (`safeDisable`). Cała gra oraz menu konfiguracyjne działają dalej bez crashu gry na każdym loaderze.
3. **Zgodność formatu konfiguracji**:
   Wszystkie adaptery korzystają z tego samego `ModuleConfigManager` generującego identyczny plik `config/zombekclient/modules.json`. Użytkownik może przenieść katalog `config/zombekclient` między instalacjami Fabric, Forge, NeoForge i Quilt, a wszystkie włączone moduły i wartości ustawień zostaną zachowane 1:1.

---

## 4. Rejestracja Wydań w Launcherze (`client-releases.json`)

Launcher ZombekClient korzysta z rejestru wydań `apps/minecraft-client/client-releases.json` oraz serwisu `ClientReleaseService`:

- **Fabric 1.20.4**: Instaluje `zombekclient-fabric-0.1.0-alpha.jar`
- **Forge 1.20.4**: Instaluje `zombekclient-forge-0.1.0-alpha.jar`
- **NeoForge 1.20.4**: Instaluje `zombekclient-neoforge-0.1.0-alpha.jar`
- **Quilt 1.20.4**: Instaluje `zombekclient-quilt-0.1.0-alpha.jar`
- **Walidacja integralności**: Sprawdzanie sum SHA-256 przed skopiowaniem do `instances/<id>/mods/`.
- **Automatyczna rotacja**: Usunięcie poprzednich wersji moda przed wdrożeniem nowej, co zapobiega konfliktom duplikatów w classpath gry.
