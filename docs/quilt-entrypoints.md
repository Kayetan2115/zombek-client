# Quilt Entrypoints — Cykl Życia i Inicjalizacja ZombekClient

## 1. Oficjalne Punkty Wejścia Quilt Loader

Quilt Loader oferuje dedykowane interfejsy cyklu życia modyfikacji, różniące się od tradycyjnego Fabrica oraz Forge/NeoForge.

W ZombekClient dla Quilt zastosowano model podwójnego, odpornego punktu wejścia (dual-entrypoint pattern) zrealizowany w klasie `QuiltClientBootstrap`:

1. **`org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint`** (`onPreLaunch(ModContainer mod)`):
   - Wywoływany bardzo wcześnie przez Quilt Loader przed uruchomieniem silnika gry.
   - Weryfikuje środowisko klienta (`MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT`).
   - Rejestruje adapter platformy `QuiltPlatformAdapter` i uruchamia `ZombekCore.init(adapter)`.
2. **`net.fabricmc.api.ClientModInitializer`** (`onInitializeClient()`):
   - Quilt Loader w pełni implementuje kompatybilność z punktami wejścia Fabric Client.
   - Gwarantuje dostępność pętli klienta i rejestrację modułów w przypadku opóźnionego startu.
3. **Synchronizowany strażnik bootstrapu (`synchronized void bootstrap(ModContainer mod)`)**:
   - Gwarantuje jednokrotną inicjalizację niezależnie od kolejności ładowania punktów wejścia przez Quilt.

```java
@ClientOnly
public class QuiltClientBootstrap implements PreLaunchEntrypoint, ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("zombekclient-quilt");
    private static volatile boolean bootstrapped = false;

    @Override
    public void onPreLaunch(ModContainer mod) {
        if (MinecraftQuiltLoader.getEnvironmentType() != EnvType.CLIENT) {
            return;
        }
        bootstrap(mod);
    }

    @Override
    public void onInitializeClient() {
        bootstrap(null);
    }

    private static synchronized void bootstrap(ModContainer mod) {
        if (bootstrapped) return;
        bootstrapped = true;

        QuiltPlatformAdapter adapter = new QuiltPlatformAdapter();
        ZombekCore.init(adapter);
    }
}
```

---

## 2. Konfiguracja Manifestu `quilt.mod.json`

Manifest Quilt v1 deklaruje intermediate mappings, entrypoints oraz powiązania zależności:

```json
{
  "schema_version": 1,
  "quilt_loader": {
    "group": "pl.zombek",
    "id": "zombekclient",
    "version": "0.1.0-alpha",
    "metadata": {
      "name": "ZombekClient",
      "description": "Oficjalny mod klienta ZombekClient dla Quilt Loader.",
      "contact": {
        "homepage": "https://zombekclient.pl"
      }
    },
    "intermediate_mappings": "net.fabricmc:intermediary",
    "entrypoints": {
      "pre_launch": "pl.zombek.client.quilt.entrypoint.QuiltClientBootstrap",
      "client": "pl.zombek.client.quilt.entrypoint.QuiltClientBootstrap"
    },
    "depends": [
      {
        "id": "quilt_loader",
        "versions": ">=0.25.0"
      },
      {
        "id": "minecraft",
        "versions": "1.20.4"
      },
      {
        "id": "java",
        "versions": ">=17"
      }
    ]
  },
  "mixin": [
    "zombekclient-quilt.mixins.json"
  ]
}
```

---

## 3. Integracja z Pętlą Gry przez `MinecraftClientMixin`

Aby nie wprowadzać zależności od wygaszonego QSL (Quilt Standard Libraries) ani Quilted Fabric API, pętla tickowa klienta oraz wstępna rejestracja klawiszy podpinane są bezpośrednio pod rdzeń klienta Minecraft za pośrednictwem minimalnego, stabilnego Mixinu `MinecraftClientMixin`:

```java
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onClientInitComplete(RunArgs args, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        QuiltKeybindManager.getInstance().registerKeyMappings(client);
    }

    @Inject(method = "tick", at = @At("END"))
    private void onClientTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        QuiltKeybindManager.getInstance().onClientTick(client);
    }
}
```

---

## 4. Bezpieczne Zamknięcie Gry (Shutdown Hook)

Podczas zamykania gry Minecraft, ZombekClient Core rejestruje wirtualny JVM Shutdown Hook:
```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    LOGGER.info("[zombekclient] Shutting down ZombekClient Core, saving state...");
    shutdown();
}, "ZombekClient-Shutdown"));
```
Dzięki temu aktualny stan wszystkich włączonych modułów oraz ich parametrów konfiguracyjnych jest natychmiastowo zapisywany w pliku `.minecraft/config/zombekclient/modules.json` przed zakończeniem procesu JVM.
