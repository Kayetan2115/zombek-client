# Matryca Kompatybilności — ZombekClient

Niniejszy dokument przedstawia macierz zgodności wersji Minecraft Java Edition, modloaderów Fabric, Forge i NeoForge oraz środowisk Java Runtime w ZombekClient.

---

## 1. Wersje Minecraft & Modloadery

| Wersja Minecraft | Typ Wydania | Vanilla | Fabric Loader | Forge | NeoForge | Quilt Loader | Wymagana Java |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1.21.1** | Release | Pełne wsparcie | 0.16.x / 0.15.x | Pełne wsparcie (51.x) | Pełne wsparcie (21.1.x) | Pełne wsparcie (0.27.x) | Java 21 (`java-runtime-delta`) |
| **1.21.0** | Release | Pełne wsparcie | 0.16.x / 0.15.x | Pełne wsparcie (51.x) | Pełne wsparcie (21.0.x) | Pełne wsparcie (0.27.x) | Java 21 (`java-runtime-delta`) |
| **1.20.4** | Release | Pełne wsparcie | 0.16.x / 0.15.x (Zalecana) | Pełne wsparcie (49.2.13 zalecana) | Pełne wsparcie (20.4.237 zalecana) | Pełne wsparcie (0.27.1-beta.1 zalecana) | Java 17 (`java-runtime-gamma`) |
| **1.20.2** | Release | Pełne wsparcie | 0.15.x / 0.14.x | Pełne wsparcie (48.x) | Pełne wsparcie (20.2.93 zalecana) | Pełne wsparcie (0.26.x) | Java 17 (`java-runtime-gamma`) |
| **1.20.1** | Release | Pełne wsparcie | 0.15.x / 0.14.x | Pełne wsparcie (47.3.0 zalecana) | Pełne wsparcie (1.20.1-47.1.106 transition) | Pełne wsparcie (0.26.x) | Java 17 (`java-runtime-gamma`) |
| **1.19.4** | Release | Pełne wsparcie | 0.14.x / 0.15.x | Pełne wsparcie (45.3.0 zalecana) | Nieobsługiwany (NeoForge nie istniało) | Pełne wsparcie (0.20.x+) | Java 17 (`java-runtime-gamma`) |
| **1.18.2** | Release | Pełne wsparcie | 0.14.x / 0.13.x | Pełne wsparcie (40.2.21 zalecana) | Nieobsługiwany (NeoForge nie istniało) | Pełne wsparcie (0.18.x+) | Java 17 (`java-runtime-gamma`) |
| **1.16.5** | Release | Pełne wsparcie | 0.14.x / 0.12.x | Pełne wsparcie (36.2.39 zalecana) | Nieobsługiwany (NeoForge nie istniało) | Nieobsługiwany (Quilt wspiera 1.18.2+) | Java 8 / 11 (`java-runtime-alpha` / `jre-legacy`) |
| **1.12.2** | Release | Pełne wsparcie | Nieobsługiwany | Pełne wsparcie (14.23.5.2860 zalecana) | Nieobsługiwany (NeoForge nie istniało) | Nieobsługiwany | Java 8 (`jre-legacy`) |
| **1.8.9** | Release | Pełne wsparcie | Nieobsługiwany | Pełne wsparcie (11.15.1.2318 zalecana) | Nieobsługiwany (NeoForge nie istniało) | Nieobsługiwany | Java 8 (`jre-legacy`) |

---

## 2. Źródła Metadanych i Repozytoriów

### Oficjalne źródła Quilt:
- **Oficjalna strona:** [QuiltMC Website](https://quiltmc.org/)
- **Dokumentacja deweloperska:** [Quilt Documentation](https://quiltmc.org/en/guide/)
- **Quilt Meta API v3:** `https://meta.quiltmc.org/v3`
  - Wersje gry: `GET /v3/versions/game`
  - Wersje loadera: `GET /v3/versions/loader/{game_version}`
  - Profil JSON launchera: `GET /v3/versions/loader/{game_version}/{loader_version}/profile/json`
- **Quilt Maven:** `https://maven.quiltmc.org/repository/release/`
  - `org.quiltmc:quilt-loader`
  - `org.quiltmc:quilt-json5`
  - `org.spongepowered:mixin`
- **Wymóg User-Agent:** `ZombekClient/0.1.0 (desktop launcher)`
- **Polityka QSL (Quilt Standard Libraries):** Wycofana/zakończona w grudniu 2025 r. Czysta instalacja loadera bez wstrzykiwania QSL.

### Oficjalne źródła NeoForge:
- **Oficjalna strona:** [NeoForged Website](https://neoforged.net/)
- **Dokumentacja:** [NeoForge Documentation](https://docs.neoforged.net/)
- **Oficjalne Maven:** `https://maven.neoforged.net/releases/`
- **Metadane Maven Modern (1.20.2+):** `https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml`
- **Metadane Maven Transition (1.20.1):** `https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml`
- **Oficjalny Instalator Modern:** `https://maven.neoforged.net/releases/net/neoforged/neoforge/{version}/neoforge-{version}-installer.jar`
- **Oficjalny Instalator Transition:** `https://maven.neoforged.net/releases/net/neoforged/forge/{version}/forge-{version}-installer.jar`

### Oficjalne źródła Forge:
- **Oficjalna strona:** [MinecraftForge Files](https://files.minecraftforge.net/)
- **Promocje (Recommended / Latest):** `https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json`
- **Oficjalne Maven:** `https://maven.minecraftforge.net/`
- **Maven Metadata XML:** `https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml`
- **Oficjalny Instalator:** `https://maven.minecraftforge.net/net/minecraftforge/forge/{version}/forge-{version}-installer.jar`
- **Dokumentacja Forge:** [Forge Documentation](https://docs.minecraftforge.net/)

### Oficjalne źródła Fabric:
- **Dokumentacja instalatora:** [Fabric Installation Guide](https://fabricmc.net/use/)
- **Fabric Wiki:** [Fabric Wiki Install](https://wiki.fabricmc.net/install)
- **Fabric Meta API:** [https://meta.fabricmc.net/](https://meta.fabricmc.net/)
  - Wersje gry: `GET /v2/versions/game`
  - Wersje loadera: `GET /v2/versions/loader/{game_version}`
  - Profil JSON: `GET /v2/versions/loader/{game_version}/{loader_version}/profile/json`
- **Fabric Maven:** `https://maven.fabricmc.net/`
  - `net.fabricmc:fabric-loader`
  - `net.fabricmc:intermediary`
  - `org.ow2.asm:*`

### Oficjalne źródła Mojang:
- **Piston Meta v2:** `https://piston-meta.mojang.com/mc/game/version_manifest_v2.json`
- **Mojang Libraries:** `https://libraries.minecraft.net/`
- **Mojang Assets Objects:** `https://resources.download.minecraft.net/`

---

## 3. Struktura Izolacji Instancji

Każda utworzona instalacja gry posiada własny, niezależny katalog w `%APPDATA%/ZombekClient/instances/<id>/`:

```
instances/<id>/
├── instance.json          # Konfiguracja profilu (loader: vanilla | fabric | forge | neoforge, wersje, RAM, JVM args)
├── mods/                  # Izolowany katalog na modyfikacje (.jar) wyłącznie dla danej instalacji
├── config/                # Izolowane pliki konfiguracyjne modów
├── saves/                 # Zapisy światów trybu jednoosobowego
├── resourcepacks/         # Pakiety zasobów (tekstur)
├── screenshots/           # Zrzuty ekranu F2
├── natives/               # Wyodrębnione biblioteki natywne OS (.dll / .so / .dylib)
└── logs/
    └── latest.log         # Pełne logi z ostatniej sesji gry z zamaskowanymi tokenami
```

Dzięki temu:
- Modyfikacje Fabric, Forge i NeoForge nie kolidują ze sobą.
- Każda instancja posiada własne ustawienia modów i zapisy światów.
- Zmiana aktywnego profilu w launcherze nie narusza stanu innych profili.

---

## 4. Matryca Zgodności Modów i Metadanych (Etap 6A)

### Obsługiwane formaty metadanych modów:
| Modloader | Plik Manifestu | Lokalizacja w archiwum .jar | Format |
| :--- | :--- | :--- | :--- |
| **Fabric** | `fabric.mod.json` | `/fabric.mod.json` | JSON (schema v1) |
| **Quilt** | `quilt.mod.json` | `/quilt.mod.json` | JSON (schema v1) |
| **Forge** | `mods.toml` | `/META-INF/mods.toml` | TOML |
| **NeoForge** | `neoforge.mods.toml` | `/META-INF/neoforge.mods.toml` | TOML |
| **Legacy / Inne** | Brak | Brak | Fallback do nazwy pliku (.jar) |

### Matryca Zgodności Międzyloaderowej (Cross-Loader Compatibility):
| Środowisko Instancji | Mod Fabric | Mod Quilt | Mod Forge | Mod NeoForge |
| :--- | :---: | :---: | :---: | :---: |
| **Vanilla** | Niekompatybilny | Niekompatybilny | Niekompatybilny | Niekompatybilny |
| **Fabric** | **Kompatybilny** | Niekompatybilny | Niekompatybilny | Niekompatybilny |
| **Quilt** | **Prawdopodobnie działa** (Wsteczna zgodność) | **Kompatybilny** | Niekompatybilny | Niekompatybilny |
| **Forge** | Niekompatybilny | Niekompatybilny | **Kompatybilny** | Niekompatybilny |
| **NeoForge** | Niekompatybilny | Niekompatybilny | Prawdopodobnie działa (tylko 1.20.1) | **Kompatybilny** |

### Podkatalogi Modpack Managera w Instancji:
```
instances/<id>/
├── mods/                  # Włączone pliki modów (.jar) wczytywane przez silnik gry
├── disabled-mods/         # Wyłączone pliki modów (.jar) bezpiecznie ukryte przed silnikiem
├── config/                # Niezależne pliki konfiguracyjne modów
└── modpack.json           # Manifest powiązanego profilu modpacka
```

---

## 5. Matryca Wydań Modyfikacji Klienta ZombekClient (Etap 9C)

| Wersja Minecraft | Modloader | Artefakt Klienta | Zakres Wersji Loadera | Środowisko Java | Rozmiar / SHA-256 | Status Wsparcia |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1.20.4** | **Fabric** | `zombekclient-fabric-0.1.0-alpha.jar` | Fabric Loader `>=0.15.0` | Java 17 | 141 490 B<br>`208781b3afe2ef6776731dc25c3019701b3012d0f9a982c4c0c63e7ea47d1314` | **Pełne wsparcie** (Etap 9C) |
| **1.20.4** | **Forge** | `zombekclient-forge-0.1.0-alpha.jar` | Forge `[49.0,)` (49.2.13 zalecana) | Java 17 | 1 237 127 B<br>`541342827497271399cb364466e5eb89cee2ee5665d1e61e68b83048e59a4675` | **Pełne wsparcie** (Etap 9C) |
| **1.20.4** | **NeoForge** | `zombekclient-neoforge-0.1.0-alpha.jar` | NeoForge `[20.4,)` (20.4.237 zalecana) | Java 17 | 1 234 384 B<br>`33132e4508adac0b9857a4e9d334c23795ce1ea90efb0b6a24eb17e6a4e65fd9` | **Pełne wsparcie** (Etap 9C) |
| **1.20.4** | **Quilt** | `zombekclient-quilt-0.1.0-alpha.jar` | Quilt Loader `>=0.25.0` (0.27.1-beta.1 zalecana) | Java 17 | 1 263 126 B<br>`e18a92039a8e1e399accae1db669cccfb76f78e42fec0cabb395963ebd02d6cd` | **Pełne wsparcie** (Etap 9C) |
| **1.20.4** | **Vanilla** | Nie dotyczy | Brak | Java 17 | — | Nieobsługiwany (Vanilla nie ładuje modów) |

### 5.1. Kompatybilność Modułów Etapu 8B7

| Moduł | Kategoria | Identyfikator | Fabric | Quilt | Forge | NeoForge |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Net Graph** | `HUD` | `"net_graph"` | **TAK** (`FabricNetworkDataProvider`, `HudElementRegistry`) | **TAK** (`QuiltNetworkDataProvider`, `HudElementRegistry`) | **TAK** (`ForgeNetworkDataProvider`, `HudElementRegistry`) | **TAK** (`NeoForgeNetworkDataProvider`, `HudElementRegistry`) |
| **LatencyTracker** | `DIAG` | Singleton Core | **TAK** (RFC 3550 Jitter) | **TAK** (RFC 3550 Jitter) | **TAK** (RFC 3550 Jitter) | **TAK** (RFC 3550 Jitter) |
| **NetworkTrafficTracker** | `DIAG` | Singleton Core | **TAK** (Non-invasive counters) | **TAK** (Non-invasive counters) | **TAK** (Non-invasive counters) | **TAK** (Non-invasive counters) |
| **PacketRateTracker** | `DIAG` | Singleton Core | **TAK** (RX/TX PPS) | **TAK** (RX/TX PPS) | **TAK** (RX/TX PPS) | **TAK** (RX/TX PPS) |
| **NetworkHistoryBuffer** | `DATA` | Singleton Core | **TAK** (Circular Ring Buffer) | **TAK** (Circular Ring Buffer) | **TAK** (Circular Ring Buffer) | **TAK** (Circular Ring Buffer) |
| **Chat** | `VISUAL` | `"chat"` | **TAK** (`ChatHudMixin`) | **TAK** (`ChatHudMixin`) | **TAK** (`ClientChatReceivedEvent`, `VanillaGuiOverlay.CHAT_PANEL`) | **TAK** (`ClientChatReceivedEvent`, `VanillaGuiOverlay.CHAT_PANEL`) |
| **Titles** | `HUD` | `"titles"` | **TAK** (`InGameHudMixin`) | **TAK** (`InGameHudMixin`) | **TAK** (`RenderGuiOverlayEvent.Pre`, `VanillaGuiOverlay.TITLE_TEXT`, `RECORD_OVERLAY`) | **TAK** (`RenderGuiOverlayEvent.Pre`, `VanillaGuiOverlay.TITLE_TEXT`, `RECORD_OVERLAY`) |
| **ZombekChatService** | `INFRA` | Singleton Core | **TAK** (`ZombekChatService`) | **TAK** (`ZombekChatService`) | **TAK** (`ZombekChatService`) | **TAK** (`ZombekChatService`) |
| **ZombekTitleService** | `INFRA` | Singleton Core | **TAK** (`ZombekTitleService`) | **TAK** (`ZombekTitleService`) | **TAK** (`ForgeTitleSynchronizer`) | **TAK** (`NeoForgeTitleSynchronizer`) |
| **ChatTabManager** | `DATA` | Singleton Core | **TAK** (`ChatTabManager`) | **TAK** (`ChatTabManager`) | **TAK** (`ChatTabManager`) | **TAK** (`ChatTabManager`) |
| **ChatFilterManager** | `DATA` | Singleton Core | **TAK** (ReDoS Guard) | **TAK** (ReDoS Guard) | **TAK** (ReDoS Guard) | **TAK** (ReDoS Guard) |
| **ChatHistoryManager**| `DATA` | Singleton Core | **TAK** (Ring Buffer) | **TAK** (Ring Buffer) | **TAK** (Ring Buffer) | **TAK** (Ring Buffer) |

### 5.2. Kompatybilność Architektury HUD Editora (Etap 9A)

| Komponent / Ekran | Odpowiedzialność | Fabric | Quilt | Forge | NeoForge |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **HudEditorScreen** | Natywny ekran edytora GUI | **TAK** (`ZombekFabricHudEditorScreen`) | **TAK** (`ZombekQuiltHudEditorScreen`) | **TAK** (`ZombekForgeHudEditorScreen`) | **TAK** (`ZombekNeoForgeHudEditorScreen`) |
| **Keybind Editor** | Otwieranie dedykowanym klawiszem | **TAK** (`KeybindManager`, domyślnie NONE) | **TAK** (`QuiltKeybindManager`, domyślnie NONE) | **TAK** (`ForgeKeybindManager`, domyślnie NONE) | **TAK** (`NeoForgeKeybindManager`, domyślnie NONE) |
| **Menu Button** | Przycisk w menu Right Shift | **TAK** (`ZombekMenuScreen` — "🎨 EDIT HUD") | **TAK** (`ZombekQuiltScreen` — "🎨 EDIT HUD") | **TAK** (`ZombekForgeScreen` — "🎨 EDIT HUD") | **TAK** (`ZombekNeoForgeScreen` — "🎨 EDIT HUD") |
| **HudComponentRegistry** | Rejestr metadanych 32 modułów | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) |
| **HudLayoutManager** | Zarządzanie profilami i buforem zmian | **TAK** (`core` JSON / atomic save) | **TAK** (`core` JSON / atomic save) | **TAK** (`core` JSON / atomic save) | **TAK** (`core` JSON / atomic save) |
| **HudConfigMigrator** | Bezpieczna migracja z `modules.json` | **TAK** (Non-destructive) | **TAK** (Non-destructive) | **TAK** (Non-destructive) | **TAK** (Non-destructive) |
| **HudEditorCanvas** | Rysowanie siatki i granic roboczych | **TAK** (`IHudRenderer`) | **TAK** (`IHudRenderer`) | **TAK** (`IHudRenderer`) | **TAK** (`IHudRenderer`) |
| **Preview Providers** | Podgląd demonstracyjny komponentów | **TAK** (32 realistyczne providery) | **TAK** (32 realistyczne providery) | **TAK** (32 realistyczne providery) | **TAK** (32 realistyczne providery) |

### 5.3. Kompatybilność Interakcji HUD Editora (Etap 9B)

| Podsystem / Kontroler | Odpowiedzialność | Fabric | Quilt | Forge | NeoForge |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **HudDragController** | Przesuwanie myszką z zachowaniem grab offsetu | **TAK** (`IHudRenderer`, staging) | **TAK** (`IHudRenderer`, staging) | **TAK** (`IHudRenderer`, staging) | **TAK** (`IHudRenderer`, staging) |
| **HudResizeController** | 8 uchwytów resize z nieruchomą krawędzią | **TAK** (`HudResizeHandle`) | **TAK** (`HudResizeHandle`) | **TAK** (`HudResizeHandle`) | **TAK** (`HudResizeHandle`) |
| **HudScaleController** | Skalowanie wizualne 0.25x - 3.00x | **TAK** (Precision 0.01) | **TAK** (Precision 0.01) | **TAK** (Precision 0.01) | **TAK** (Precision 0.01) |
| **HudGridManager** | 5 stylów siatki, presety 2-32 px | **TAK** (Frustum culled) | **TAK** (Frustum culled) | **TAK** (Frustum culled) | **TAK** (Frustum culled) |
| **HudSnapping** | Przyciąganie do środka, krawędzi i siatki | **TAK** (Linie pomocnicze) | **TAK** (Linie pomocnicze) | **TAK** (Linie pomocnicze) | **TAK** (Linie pomocnicze) |
| **HudAlignmentManager** | Szybkie wyrównywanie do krawędzi i osi | **TAK** (Inverse anchor math) | **TAK** (Inverse anchor math) | **TAK** (Inverse anchor math) | **TAK** (Inverse anchor math) |
| **HudCanvasViewport** | Wirtualna rzutnia (Zoom 25%-400% & Pan) | **TAK** (Zero-drift math) | **TAK** (Zero-drift math) | **TAK** (Zero-drift math) | **TAK** (Zero-drift math) |
| **Mouse Event Forwarding**| Delegowanie mouseClicked/Dragged/Released/Moved/Scrolled | **TAK** (`ZombekFabricHudEditorScreen`) | **TAK** (`ZombekQuiltHudEditorScreen`) | **TAK** (`ZombekForgeHudEditorScreen`) | **TAK** (`ZombekNeoForgeHudEditorScreen`) |
| **Gameplay Isolation** | Ochrona przed przenikaniem kliknięć do gry | **TAK** (100% consumed) | **TAK** (100% consumed) | **TAK** (100% consumed) | **TAK** (100% consumed) |

### 5.4. Kompatybilność Zaawansowanych Systemów HUD Editora (Etap 9C)

| Podsystem / Funkcja | Zakres Odpowiedzialności | Fabric | Quilt | Forge | NeoForge |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **HudProfileManager** | Zarządzanie profilami i przełączanie | **TAK** (`KeybindManager`, Menu) | **TAK** (`QuiltKeybindManager`) | **TAK** (`ForgeKeybindManager`) | **TAK** (`NeoForgeKeybindManager`) |
| **HudPresetManager** | 8 szablonów systemowych + szablony gracza | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) |
| **HudMultiSelectionManager** | Zaznaczanie wielu elementów (Ctrl, Shift, Box) | **TAK** (`HudEditorMouseController`) | **TAK** (`HudEditorMouseController`) | **TAK** (`HudEditorMouseController`) | **TAK** (`HudEditorMouseController`) |
| **HudGroupManager** | Grupowanie logiczne (Ctrl+G, Ctrl+Shift+G) | **TAK** (`HudGroup`, Staging) | **TAK** (`HudGroup`, Staging) | **TAK** (`HudGroup`, Staging) | **TAK** (`HudGroup`, Staging) |
| **HudLayerManager** | Warstwy logiczne (Z-index ordering) | **TAK** (Deterministyczny sort) | **TAK** (Deterministyczny sort) | **TAK** (Deterministyczny sort) | **TAK** (Deterministyczny sort) |
| **HudUndoRedoManager** | Historia operacji (Command Pattern, 50 kroków) | **TAK** (`IHudCommand`, Ctrl+Z/Y) | **TAK** (`IHudCommand`, Ctrl+Z/Y) | **TAK** (`IHudCommand`, Ctrl+Z/Y) | **TAK** (`IHudCommand`, Ctrl+Z/Y) |
| **HudComponentClipboard** | Schowek: Kopiuj, Wklej, Duplikuj (Ctrl+D) | **TAK** (Memory session clipboard)| **TAK** (Memory session clipboard)| **TAK** (Memory session clipboard)| **TAK** (Memory session clipboard)|
| **HudLayoutImportExport** | Import i Eksport JSON (Schema v1, Safe IO) | **TAK** (Path Traversal Guard) | **TAK** (Path Traversal Guard) | **TAK** (Path Traversal Guard) | **TAK** (Path Traversal Guard) |
| **HudResponsiveLayout** | Symulacja proporcji (16:9, 4:3, 21:9) i walidacja| **TAK** (Letterboxing, Warnings) | **TAK** (Letterboxing, Warnings) | **TAK** (Letterboxing, Warnings) | **TAK** (Letterboxing, Warnings) |
| **Keyboard Input Guard** | Ochrona skrótów podczas pisania w polach | **TAK** (`activeTextField != null`)| **TAK** (`activeTextField != null`)| **TAK** (`activeTextField != null`)| **TAK** (`activeTextField != null`)|

### 5.5. Kompatybilność Modułów Kamery i Efektów Wizualnych (Etap 8C1)

| Podsystem / Moduł | Mechanizm Integracji | Fabric | Quilt | Forge | NeoForge |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ZoomModule** | Modulacja macierzy FOV + Scroll Zoom + Key Z | **TAK** (`GameRendererMixin`, `MouseMixin`) | **TAK** (`GameRendererMixin`, `MouseMixin`) | **TAK** (`ComputeFov`, `MouseScrollingEvent`) | **TAK** (`ComputeFov`, `MouseScrollingEvent`) |
| **FovChangerModule** | Profile pola widzenia + dynamiczne mnożniki | **TAK** (`CameraFovController.computeFov`) | **TAK** (`CameraFovController.computeFov`) | **TAK** (`CameraFovController.computeFov`) | **TAK** (`CameraFovController.computeFov`) |
| **MotionBlurModule** | Akumulacja i post-processing rotacji kamery | **TAK** (`ZombekCameraController.onRenderTick`) | **TAK** (`ZombekCameraController.onRenderTick`) | **TAK** (`ComputeCameraAngles`) | **TAK** (`ComputeCameraAngles`) |
| **HurtCamModule** | Anulowanie lub skalowanie wstrząsu uderzenia | **TAK** (`GameRendererMixin.tiltViewWhenHurt`) | **TAK** (`GameRendererMixin.tiltViewWhenHurt`) | **TAK** (`ComputeCameraAngles.setRoll`) | **TAK** (`ComputeCameraAngles.setRoll`) |
| **ZombekCameraController** | Centralny koordynator 6-stopniowego potoku | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) |
| **Zoom Indicator Overlay** | Wskaźnik HUD (`zoom_indicator`, ID #33) | **TAK** (`InGameHudMixin`) | **TAK** (`InGameHudMixin`) | **TAK** (`RenderGuiOverlayEvent`) | **TAK** (`RenderGuiEvent`) |
| **Lifecycle Safety** | Reset przy utracie fokusu / GUI / śmierci | **TAK** (`ZombekCore.onClientTick`) | **TAK** (`ZombekCore.onClientTick`) | **TAK** (`ClientTickEvent`) | **TAK** (`ClientTickEvent`) |

### 5.6. Kompatybilność Modułów Oświetlenia i Środowiska Świata (Etap 8C2)

| Podsystem / Moduł | Mechanizm Integracji | Fabric | Quilt | Forge | NeoForge |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **LightingModule** | Dynamiczne światło trzymanych itemów, lootu i bytów | **TAK** (`FabricDynamicLightProvider`) | **TAK** (`QuiltDynamicLightProvider`) | **TAK** (`ForgeDynamicLightProvider`) | **TAK** (`NeoForgeDynamicLightProvider`) |
| **DynamicLightManager** | Sortowanie dystansowe $d^2$, throttling i cache | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) |
| **FullBrightModule** | Bezpieczne rozjaśnienie sceny i lightmapy | **TAK** (`LightmapTextureManagerMixin`) | **TAK** (`LightmapTextureManagerMixin`) | **TAK** (`ClientTickEvent` / Gamma) | **TAK** (`ClientTickEvent` / Gamma) |
| **TimeChangerModule** | Lokalna zmiana kąta nieba i pory dnia | **TAK** (`ClientWorldMixin.getSkyAngle`) | **TAK** (`ClientWorldMixin.getSkyAngle`) | **TAK** (`Level.getDayTime` hook) | **TAK** (`Level.getDayTime` hook) |
| **WeatherChangerModule** | Lokalny gradient deszczu, śniegu i burzy | **TAK** (`ClientWorldMixin.getRainGradient`) | **TAK** (`ClientWorldMixin.getRainGradient`) | **TAK** (`Level.getRainLevel` hook) | **TAK** (`Level.getRainLevel` hook) |
| **ZombekWorldVisualController** | Centralny 4-stopniowy potok środowiska | **TAK** (`ZombekWorldVisualController`) | **TAK** (`ZombekWorldVisualController`) | **TAK** (`ZombekWorldVisualController`) | **TAK** (`ZombekWorldVisualController`) |
| **Sodium / Iris Compatibility** | Brak konfliktów z buforami chunków i shaderami | **TAK** (Lightmap interception) | **TAK** (Lightmap interception) | **TAK** (Lightmap interception) | **TAK** (Lightmap interception) |
| **Zero Server World Impact** | Nienaruszony czas, pogoda i mob spawning | **TAK** (100% Client-Side) | **TAK** (100% Client-Side) | **TAK** (100% Client-Side) | **TAK** (100% Client-Side) |

### 5.7. Kompatybilność Modułów Efektów Renderowania (Etap 8C3)

| Podsystem / Moduł | Mechanizm Integracji | Fabric | Quilt | Forge | NeoForge |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **FogCustomizerModule** | Odległość, gęstość i kolor mgły w profilach | **TAK** (`BackgroundRendererMixin`) | **TAK** (`BackgroundRendererMixin`) | **TAK** (`ViewportEvent.RenderFog`, `ComputeFogColor`) | **TAK** (`ViewportEvent.RenderFog`, `ComputeFogColor`) |
| **FogVisualController** | Tranzycje dystansu, zacisk render distance | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) |
| **ColorSaturationModule**| Potok gradacji barw i ochrona HUD | **TAK** (`ColorAdjustmentPipeline`) | **TAK** (`ColorAdjustmentPipeline`) | **TAK** (`ColorAdjustmentPipeline`) | **TAK** (`ColorAdjustmentPipeline`) |
| **ColorGradingController**| Płynne tranzycje profili i interpolacja | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) |
| **GlintColorizerModule**| Barwy zaklęć, animacje tęczy/gradientu | **TAK** (`ItemStackMixin`, `GlintRenderController`) | **TAK** (`ItemStackMixin`, `GlintRenderController`) | **TAK** (`GlintRenderController`) | **TAK** (`GlintRenderController`) |
| **ParticleChangerModule**| Mnożniki gęstości, skala, limity i odcięcia | **TAK** (`ParticleManagerMixin`) | **TAK** (`ParticleManagerMixin`) | **TAK** (`ParticleRenderController`) | **TAK** (`ParticleRenderController`) |
| **ZombekRenderEffectController**| Koordynacja potoku renderowania 6 warstw | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) | **TAK** (`core` JVM singleton) |
| **Fair Play & Safety** | Brak X-ray, brak modyfikacji NBT i pakietów | **TAK** (100% Client-Side) | **TAK** (100% Client-Side) | **TAK** (100% Client-Side) | **TAK** (100% Client-Side) |












