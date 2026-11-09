# Quilt Client Adapter — ZombekClient In-Game Mod

## 1. Wprowadzenie i Cel

Adapter Quilt dla moda ZombekClient (`apps/minecraft-client/quilt/`) stanowi nowoczesną integrację współdzielonego, w 100% loader-agnostycznego rdzenia klienta (`apps/minecraft-client/core/`) ze środowiskiem **Minecraft Quilt Loader 1.20.4 (Quilt Loader 0.27.1-beta.1+)**.

Adapter implementuje kontrakt platformowy `IPlatformAdapter`, rejestruje modyfikację jako dedykowany mod klienta (`environment: "client"`, `@ClientOnly`), korzysta z oficjalnych interfejsów startowych Quilt (`PreLaunchEntrypoint`, `ClientModInitializer`), obsługuje rejestrację skrótów klawiszowych pod **RIGHT SHIFT** z precyzyjną detekcją zbocza (edge-detection) w LWJGL/GLFW oraz udostępnia pełnoekranowe świąteczne menu konfiguracyjne (`ZombekQuiltScreen`) wykorzystujące mapowania Yarn (`DrawContext`, `Screen`, `Text`).

Co kluczowe, zgodnie ze stanem ekosystemu po wygaszeniu Quilt Standard Libraries (QSL) w grudniu 2025 r., adapter został zaprojektowany z minimalnymi, stabilnymi zależnościami wyłącznie od czystego `quilt_loader` i `minecraft`, bez zbędnych nakładek.

---

## 2. Architektura Multi-Loader ZombekClient

```
apps/minecraft-client/
├── core/                                      # Uniwersalny ZombekClient Core (loader-agnostic, pure Java 17)
│   ├── src/main/java/pl/zombek/client/core/
│   │   ├── bootstrap/ZombekCore.java          # Centralny koordynator cyklu życia
│   │   ├── config/ModuleConfigManager.java    # Trwały zapis i odczyt modules.json
│   │   ├── lifecycle/IClientModule.java       # Kontrakt modułów (onEnable, onDisable, onTick)
│   │   ├── modules/ModuleManager.java         # Rejestr modułów i routing
│   │   ├── platform/IPlatformAdapter.java     # Kontrakt adaptera platformy
│   │   └── settings/                          # Typowane ustawienia (Boolean, Number, Color, Enum, etc.)
│   └── build.gradle                           # Czysta biblioteka Java 17 (zero importów z MC)
├── fabric/                                    # Adapter Fabric (Minecraft 1.20.4, Yarn)
├── forge/                                     # Adapter Forge (Minecraft 1.20.4, Official Mappings)
├── neoforge/                                  # Adapter NeoForge (Minecraft 1.20.4, Official Mappings)
└── quilt/                                     # Adapter Quilt (Minecraft 1.20.4, Yarn)
    ├── src/main/java/pl/zombek/client/quilt/
    │   ├── entrypoint/QuiltClientBootstrap.java # PreLaunchEntrypoint + ClientModInitializer (@ClientOnly)
    │   ├── platform/QuiltPlatformAdapter.java  # Implementacja IPlatformAdapter dla Quilt
    │   ├── input/QuiltKeybindManager.java     # KeyBinding i detekcja zbocza GLFW RIGHT SHIFT
    │   ├── mixin/MinecraftClientMixin.java    # Wstrzyknięcie cyklu życia klienta i pętli tickowej
    │   ├── gui/ZombekQuiltScreen.java         # Responsywne menu GUI w Yarn (Festive Dark)
    │   ├── render/QuiltTheme.java             # Tokeny graficzne i kolory motywu świątecznego
    │   └── config/QuiltConfigHelper.java      # Ścieżka QuiltLoader.getConfigDir()
    ├── src/main/resources/
    │   ├── quilt.mod.json                     # Manifest Quilt v1 z intermediate mappings (hashed)
    │   └── zombekclient-quilt.mixins.json     # Konfiguracja Mixin dla klienta
    └── build.gradle                           # Quilt Loom 1.6.9 -> zombekclient-quilt-0.1.0-alpha.jar
```

---

## 3. Rejestracja i Bezpieczeństwo Środowiska (Client-Only Safety)

Dedykowane serwery Minecraft nie ładują klas graficznych (LWJGL, `Screen`, `DrawContext`, `KeyBinding`). W celu zagwarantowania 100% bezpieczeństwa przed `ClassNotFoundException` i awariami serwerów:

1. **Manifest `quilt.mod.json`**:
   W sekcji `quilt_loader` zdefiniowano:
   ```json
   "environment": "client"
   ```
2. **Adnotacja `@ClientOnly`**:
   Klasy `QuiltClientBootstrap` oraz `QuiltPlatformAdapter` oznaczone są adnotacją `org.quiltmc.loader.api.minecraft.ClientOnly`.
3. **Runtime Guard w `PreLaunchEntrypoint`**:
   ```java
   if (MinecraftQuiltLoader.getEnvironmentType() != EnvType.CLIENT) {
       LOGGER.warn("[ZombekClient-Quilt] Środowisko dedykowane serwera wykryte. Wyłączam inicjalizację klienta ZombekClient.");
       return;
   }
   ```
4. **Mixiny ograniczone do klienta**:
   W pliku `zombekclient-quilt.mixins.json` miksin `MinecraftClientMixin` znajduje się wyłącznie w sekcji `"client": [ "MinecraftClientMixin" ]`.

---

## 4. Mostek Platformowy `QuiltPlatformAdapter`

Klasa `QuiltPlatformAdapter` implementuje interfejs `pl.zombek.client.core.platform.IPlatformAdapter`:

```java
public class QuiltPlatformAdapter implements IPlatformAdapter {
    @Override public String getLoaderName() { return "quilt"; }
    @Override public String getLoaderVersion() { ... }
    @Override public String getMinecraftVersion() { return "1.20.4"; }
    @Override public Path getGameDirectory() { return QuiltLoader.getGameDir(); }
    @Override public Path getConfigDirectory() { return QuiltConfigHelper.getConfigDirectory(); }
    @Override public boolean isClient() { return QuiltLoader.getEnvironmentType() == EnvType.CLIENT; }
    @Override public void openMenu() { ... }
}
```

Dzięki temu wspólny rdzeń `ZombekCore` operuje w sposób całkowicie identyczny jak na Fabric, Forge i NeoForge.

---

## 5. Budowanie i Dystrybucja Artefaktu

Plik `build.gradle` projektu Quilt bazuje na wtyczce `org.quiltmc.loom:1.6.9` i automatycznie pakuje skompilowane klasy ze wspólnego projektu `:core`:

```groovy
jar {
    dependsOn ':core:jar'
    from { zipTree(project(':core').tasks.jar.archiveFile) }
}
```

- **Plik wynikowy**: `apps/minecraft-client/quilt/build/libs/zombekclient-quilt-0.1.0-alpha.jar`
- **Rozmiar**: 69 568 bajtów
- **SHA-256**: `ef1608220e502f3745cf0fbb30f147a0d6cd9057b14e01421f6ce814e02eb137`
- **Weryfikacja zawartości**: Zawiera pełną strukturę klas wspólnych `pl/zombek/client/core/` oraz specyficznych dla Quilt `pl/zombek/client/quilt/`, jak również `quilt.mod.json`, `zombekclient-quilt.mixins.json` i wygenerowany refmap.
