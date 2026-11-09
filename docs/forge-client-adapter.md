# Forge Client Adapter — ZombekClient In-Game Mod

## 1. Wprowadzenie i Cel

Adapter Forge dla moda ZombekClient (`apps/minecraft-client/forge/`) stanowi pomost integrujący uniwersalny, loader-agnostyczny rdzeń klienta (`apps/minecraft-client/core/`) ze środowiskiem **Minecraft Forge 1.20.4 (Forge 49.2.13+)**.

Adapter implementuje interfejsy platformowe, rejestruje modyfikację w magistrali FML (Forge Mod Loader), mapuje zdarzenia ticku i renderowania, obsługuje rejestrację skrótów klawiszowych (`KeyMapping`) oraz udostępnia dedykowany ekran konfiguracyjny (`ZombekForgeScreen`) wykorzystujący oficjalne mapowania Mojanga (`GuiGraphics`, `Screen`).

---

## 2. Struktura Projektu i Modułów

```
apps/minecraft-client/
├── core/                                    # Uniwersalny ZombekClient Core (loader-agnostic)
│   ├── src/main/java/pl/zombek/client/core/
│   │   ├── bootstrap/ZombekCore.java        # Centralny bootstrap Core
│   │   ├── config/ModuleConfigManager.java  # Zapis/odczyt konfiguracji JSON schema v1
│   │   ├── lifecycle/IClientModule.java     # Interfejs modułu
│   │   ├── modules/ModuleManager.java       # Centralny rejestr i dispatch pętli gry
│   │   ├── platform/IPlatformAdapter.java   # Kontrakt adaptera platformy
│   │   └── settings/                        # Typowany system ustawień (Boolean, Number, Color, Enum, etc.)
│   └── build.gradle                         # Java 17 library
├── fabric/                                  # Adapter Fabric (Minecraft 1.20.4, Yarn)
│   └── build.gradle                         # Fabric Loom 1.6.12 -> zombekclient-fabric-0.1.0-alpha.jar
└── forge/                                   # Adapter Forge (Minecraft 1.20.4, Official Mappings)
    ├── src/main/java/pl/zombek/client/forge/
    │   ├── entrypoint/ZombekForgeMod.java   # @Mod("zombekclient") FML entrypoint
    │   ├── integration/ForgePlatformAdapter # Implementacja IPlatformAdapter dla Forge
    │   ├── input/ForgeKeybindManager.java   # Forge KeyMapping i edge-detection
    │   ├── events/ForgeClientEvents.java    # @SubscribeEvent TickEvent.ClientTickEvent
    │   ├── gui/ZombekForgeScreen.java       # GUI w Official Mappings (Screen, GuiGraphics)
    │   ├── render/ForgeTheme.java           # Paleta Festive Dark i tokeny graficzne
    │   └── config/ForgeConfigHelper.java    # Integracja ze ścieżką FMLPaths.CONFIGDIR
    ├── src/main/resources/
    │   └── META-INF/mods.toml               # Manifest Forge Mod Loader
    └── build.gradle                         # ForgeGradle 6.0+ -> zombekclient-forge-0.1.0-alpha.jar
```

---

## 3. Rejestracja i Bezpieczeństwo Serwera (Dist Safety)

Minecraft Forge ładuje modyfikacje zarówno po stronie zintegrowanego klienta gry, jak i na serwerach dedykowanych (np. serwery LAN lub standalone). Próba załadowania klas interfejsu graficznego (`Screen`, `GuiGraphics`, `KeyMapping`) na serwerze dedykowanym kończy się natychmiastowym błędem `NoClassDefFoundError` lub `CrashReport`.

W klasie `ZombekForgeMod`:

```java
@Mod(ZombekForgeMod.MOD_ID)
public class ZombekForgeMod {
    public static final String MOD_ID = "zombekclient";

    public ZombekForgeMod() {
        // 1. Zapewnienie, że mod jest ignorowany na serwerach (Client-Only Mod)
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY,
                        (remote, isServer) -> true
                ));

        // 2. Bezpieczna inicjalizacja kodu wyłącznie po stronie klienta
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            initClient(modEventBus);
        });
    }
}
```

1. **`DisplayTest.IGNORESERVERONLY`**: Serwer sieciowy nie wymaga obecności tego modu po stronie serwera ani klienta, co pozwala graczowi łączyć się z dowolnymi serwerami Vanilla/Forge.
2. **`DistExecutor.unsafeRunWhenOn(Dist.CLIENT)`**: Chroni przed ładowaniem jakichkolwiek klas Minecraft Client na serwerze dedykowanym.

---

## 4. Platform Adapter (`ForgePlatformAdapter`)

`ForgePlatformAdapter` implementuje kontrakt `IPlatformAdapter` zdefiniowany w `:core`:

```java
public class ForgePlatformAdapter implements IPlatformAdapter {
    @Override
    public String getPlatformName() { return "Forge"; }

    @Override
    public String getPlatformVersion() {
        return ForgeVersion.getVersion();
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve("zombekclient");
    }

    @Override
    public void openMenu() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new ZombekForgeScreen());
        }
    }

    @Override
    public void scheduleOnRenderThread(Runnable task) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(task);
        } else {
            task.run();
        }
    }
}
```

---

## 5. Event Bus i Pętla Klawiszy (`ForgeClientEvents` & `ForgeKeybindManager`)

W Forge rejestracja zdarzeń klienta odbywa się dwuetapowo:
1. **Magistrala Modowa (`IEventBus`)**: Rejestracja `RegisterKeyMappingsEvent` w celu zarejestrowania `KeyMapping` w silniku gry:
   ```java
   @SubscribeEvent
   public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
       event.register(ForgeKeybindManager.getInstance().getMenuKeyMapping());
   }
   ```
2. **Magistrala Gry (`MinecraftForge.EVENT_BUS`)**: Nasłuchiwanie zdarzeń `TickEvent.ClientTickEvent`:
   - Weryfikacja fazy `TickEvent.Phase.END`.
   - Zabezpieczenie przed wywołaniem w menu głównym (`mc.player != null && mc.level != null`).
   - Wywołanie `ZombekCore.getInstance().getModuleManager().onClientTick()`.
   - Wywołanie `ForgeKeybindManager.getInstance().checkMenuKeybind(mc)`.

### Wykrywanie krawędzi (Edge-Detection):
Aby menu nie otwierało się wielokrotnie przy ciągłym trzymaniu klawisza:
```java
boolean isCurrentlyDown = menuKeyMapping.isDown();
if (isCurrentlyDown && !menuWasDown) {
    mc.setScreen(new ZombekForgeScreen());
}
menuWasDown = isCurrentlyDown;
```

---

## 6. Budowanie i Dystrybucja JAR

W pliku `apps/minecraft-client/forge/build.gradle`:
- Wykorzystano ForgeGradle 6.0+.
- Skonfigurowano zależność od projektu `:core` (`implementation project(':core')`).
- Do zadania `jar` włączono rozpakowane skompilowane klasy z `:core` (`from { zipTree(project(':core').tasks.jar.archiveFile) }`).
- Zadanie `reobfJar` przekształca nazwy klas z Mojang Official Mappings na SRG runtime names, zachowując nienaruszone klasy loader-agnostic Core.

Artefakt wynikowy: `apps/minecraft-client/forge/build/libs/zombekclient-forge-0.1.0-alpha.jar` (rozmiar ~67.5 KB, samowystarczalny, zawiera manifest `META-INF/mods.toml`).
