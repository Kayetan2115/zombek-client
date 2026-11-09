# NeoForge Client Adapter — ZombekClient In-Game Mod

## 1. Wprowadzenie i Cel

Adapter NeoForge dla moda ZombekClient (`apps/minecraft-client/neoforge/`) stanowi nowoczesną integrację współdzielonego, w 100% loader-agnostycznego rdzenia klienta (`apps/minecraft-client/core/`) ze środowiskiem **Minecraft NeoForge 1.20.4 (NeoForge 20.4.237+)**.

Adapter implementuje interfejsy platformowe `IPlatformAdapter`, rejestruje modyfikację jako dedykowany mod klienta (`dist = Dist.CLIENT`), konfiguruje magistralę zdarzeń NeoForge, obsługuje rejestrację skrótów klawiszowych (`KeyMapping`) pod RIGHT SHIFT oraz udostępnia pełnoekranowe świąteczne menu konfiguracyjne (`ZombekNeoForgeScreen`) zintegrowane z oficjalnymi mapowaniami Mojanga (`GuiGraphics`, `Screen`, `Component`).

---

## 2. Struktura Projektu i Modułów

```
apps/minecraft-client/
├── core/                                      # Uniwersalny ZombekClient Core (loader-agnostic, pure Java 17)
│   ├── src/main/java/pl/zombek/client/core/
│   │   ├── bootstrap/ZombekCore.java          # Centralny bootstrap Core
│   │   ├── config/ModuleConfigManager.java    # Zapis/odczyt konfiguracji JSON schema v1
│   │   ├── lifecycle/IClientModule.java       # Interfejs modułu i bezpieczny cykl życia
│   │   ├── modules/ModuleManager.java         # Centralny rejestr i dispatch pętli gry
│   │   ├── platform/IPlatformAdapter.java     # Kontrakt adaptera platformy
│   │   └── settings/                          # Typowany system ustawień (Boolean, Number, Color, Enum, etc.)
│   └── build.gradle                           # Czysta biblioteka Java 17 (zero zależności od Minecrafta)
├── fabric/                                    # Adapter Fabric (Minecraft 1.20.4, Yarn)
│   └── build.gradle                           # Fabric Loom -> zombekclient-fabric-0.1.0-alpha.jar
├── forge/                                     # Adapter Forge (Minecraft 1.20.4, Official Mappings)
│   └── build.gradle                           # ForgeGradle -> zombekclient-forge-0.1.0-alpha.jar
└── neoforge/                                  # Adapter NeoForge (Minecraft 1.20.4, Official Mappings)
    ├── src/main/java/pl/zombek/client/neoforge/
    │   ├── entrypoint/ZombekNeoForgeMod.java  # @Mod(value = "zombekclient", dist = Dist.CLIENT)
    │   ├── integration/NeoForgePlatformAdapter # Implementacja IPlatformAdapter dla NeoForge
    │   ├── input/NeoForgeKeybindManager.java  # KeyMapping i edge-detection pod Right Shift
    │   ├── events/NeoForgeClientEvents.java   # Mod Event Bus & NeoForge.EVENT_BUS listeners
    │   ├── gui/ZombekNeoForgeScreen.java      # Responsywne menu w Official Mappings
    │   ├── render/NeoForgeTheme.java          # Motyw Festive Dark i tokeny graficzne
    │   └── config/NeoForgeConfigHelper.java   # Ścieżka FMLPaths.CONFIGDIR
    ├── src/main/resources/
    │   └── META-INF/neoforge.mods.toml        # Manifest NeoForge 20.4 z displayTest IGNORE_SERVER_VERSION
    └── build.gradle                           # NeoGradle userdev -> zombekclient-neoforge-0.1.0-alpha.jar
```

---

## 3. Rejestracja i Bezpieczeństwo Serwera (Dist Safety)

NeoForge 20.4 wprowadził zaawansowane mechanizmy izolacji środowiska klienta i serwera dedykowanego. W odróżnieniu od klasycznego Forge, NeoForge umożliwia bezpośrednią deklarację dystrybucji na poziomie adnotacji `@Mod`:

```java
@Mod(value = ZombekNeoForgeMod.MOD_ID, dist = Dist.CLIENT)
public class ZombekNeoForgeMod {
    public static final String MOD_ID = "zombekclient";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ZombekNeoForgeMod(IEventBus modEventBus) {
        LOGGER.info("[ZombekClient-NeoForge] Inicjalizacja klienta ZombekClient na platformie NeoForge...");

        // 1. Inicjalizacja platform adaptera i ZombekCore
        NeoForgePlatformAdapter adapter = new NeoForgePlatformAdapter();
        ZombekCore.getInstance().initialize(adapter);

        // 2. Rejestracja zdarzeń na Mod Event Bus (cykl życia modu, rejestracja klawiszy)
        NeoForgeClientEvents.registerModEvents(modEventBus);

        // 3. Rejestracja zdarzeń na Game Event Bus (NeoForge.EVENT_BUS - pętla tickowa gry)
        NeoForgeClientEvents.registerGameEvents();

        LOGGER.info("[ZombekClient-NeoForge] ZombekClient Core zainicjalizowany pomyślnie.");
    }
}
```

### Manifest `META-INF/neoforge.mods.toml`:
```toml
modLoader="javafml"
loaderVersion="[20.4,)"
license="Proprietary"

[[mods]]
modId="zombekclient"
version="0.1.0-alpha"
displayName="ZombekClient"
authors="Zombek Team"
description="Oficjalny mod ZombekClient dla Minecraft NeoForge 1.20.4."
displayTest="IGNORE_SERVER_VERSION"

[[dependencies.zombekclient]]
    modId="neoforge"
    type="required"
    versionRange="[20.4,)"
    ordering="NONE"
    side="CLIENT"

[[dependencies.zombekclient]]
    modId="minecraft"
    type="required"
    versionRange="[1.20.4,1.21)"
    ordering="NONE"
    side="CLIENT"
```

Kluczowe gwarancje bezpieczeństwa:
1. **`dist = Dist.CLIENT` w `@Mod`**: NeoForge całkowicie ignoruje i nie ładuje klasy moda na dedykowanym serwerze Minecrafta.
2. **`displayTest = "IGNORE_SERVER_VERSION"`**: Gracz z ZombekClient na NeoForge może swobodnie łączyć się z dowolnymi serwerami Vanilla, Paper, Purpur czy NeoForge bez wymogu instalowania modu na serwerze.
3. **`side = "CLIENT"` w zależnościach**: Wskazuje menedżerowi zależności, że mod jest wymagany wyłącznie w środowisku klienta.

---

## 4. Platform Adapter (`NeoForgePlatformAdapter`)

`NeoForgePlatformAdapter` implementuje interfejs `IPlatformAdapter` wymagany przez uniwersalny rdzeń `:core`:

```java
public class NeoForgePlatformAdapter implements IPlatformAdapter {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public String getPlatformVersion() {
        return NeoForgeVersion.getVersion();
    }

    @Override
    public Path getConfigDirectory() {
        return NeoForgeConfigHelper.getConfigDirectory(); // FMLPaths.CONFIGDIR.get().resolve("zombekclient")
    }

    @Override
    public void openMenu() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new ZombekNeoForgeScreen());
        }
    }

    @Override
    public void executeOnRenderThread(Runnable action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.isSameThread()) {
            action.run();
        } else if (mc != null) {
            mc.execute(action);
        } else {
            action.run();
        }
    }
}
```

---

## 5. Separacja Szyn Zdarzeń (Mod Event Bus vs Game Event Bus)

W architekturze NeoForge występuje ścisłe rozróżnienie dwóch szyn zdarzeń:
1. **Mod Event Bus (`IEventBus` wstrzykiwany w konstruktorze `@Mod`)**:
   - Służy do zdarzeń inicjalizacyjnych modu.
   - Obsługuje `RegisterKeyMappingsEvent`, rejestrując skrót klawiszowy otwierający menu (`RIGHT SHIFT`).
2. **Game Event Bus (`NeoForge.EVENT_BUS`)**:
   - Służy do zdarzeń pętli gry.
   - Obsługuje `ClientTickEvent` (w NeoForge 20.4 `TickEvent.ClientTickEvent`), gdzie w fazie `Phase.END` wykonywana jest detekcja zbocza klawisza (edge detection) oraz tickowanie aktywnych modułów przez `ZombekCore.getInstance().getModuleManager().onTick()`.

---

## 6. Integracja z Launcherem i Dystrybucja

- Mod kompilowany jest do pliku `zombekclient-neoforge-0.1.0-alpha.jar`.
- Manifest wydań `apps/minecraft-client/client-releases.json` zawiera wpis dla loadera `neoforge` o wymaganym zakresie `[20.4,)`.
- Launcher ZombekClient (`ClientReleaseService`) automatycznie kopiuje odpowiedni JAR do katalogu `instances/<id>/mods/` podczas tworzenia lub uruchamiania instancji z NeoForge.
