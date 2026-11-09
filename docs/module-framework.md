# ZombekClient — Architektura i Cykl Życia Systemu Modułów (Etap 7B)

## 1. Wprowadzenie

System modułów w ZombekClient stanowi fundament rozszerzalności klienta gry Minecraft (wersja docelowa Fabric 1.20.4). Każdy moduł reprezentuje autonomiczną jednostkę funkcjonalną, która może być włączana, wyłączana, konfigurowana oraz przypisywana do skrótów klawiszowych.

Framework zaprojektowano z zachowaniem rygorystycznych zasad izolacji błędów (Error Boundary), dzięki czemu awaria pojedynczego modułu nie prowadzi do zatrzymania gry ani problemów z pętlą renderowania.

---

## 2. Podział na Kategorie (`ModuleCategory`)

Wszystkie moduły w ZombekClient są przyporządkowane do jednej z 5 głównych kategorii systemowych:

| Kategoria | Identyfikator | Ikona | Przeznaczenie |
|-----------|---------------|-------|---------------|
| **PvP / Combat** | `PVP` | ⚔ | Moduły usprawniające walkę, śledzenie uderzeń i mechaniki bitewne |
| **HUD / Information** | `HUD` | 📊 | Elementy interfejsu ekranowego (overlaye, liczniki, statusy zbroi) |
| **Visual** | `VISUAL` | 👁 | Efekty wizualne, modyfikacje oświetlenia, rozmycie ruchu, powiększenie |
| **Survival / World** | `SURVIVAL` | 🌲 | Narzędzia ułatwiające eksplorację i codzienne czynności w świecie gry |
| **Social / Server (Other)** | `OTHER` | 🌐 | Moduły sieciowe, społecznościowe, personalizacja czatu i moduły specjalne (np. Hypixel Mods) |

Kategorie puste (nieposiadające jeszcze zaimplementowanych modułów) prezentują w GUI świąteczny placeholder z informacją, że kolejne moduły zostaną dodane w następnych etapach rozwoju.

---

## 3. Cykl Życia Modułu (`AbstractModule`)

Każdy moduł rozszerza klasę bazową `pl.zombek.client.modules.AbstractModule` (implementującą kontrakt `IClientModule`).

```
                +-------------------+
                |   onInitialize()  | (Wywoływane jednorazowo przy rejestracji)
                +---------+---------+
                          |
            +-------------+-------------+
            |                           |
            v                           v
     +--------------+           +---------------+
     |  onEnable()  | <-------+ |  onDisable()  |
     +------+-------+           +-------+-------+
            |                           ^
            | (gdy aktywny)             | (po wyłączeniu lub błędzie)
            v                           |
     +--------------+                   |
     |   onTick()   | ------------------+
     |  onRender()  | (wyjątek -> auto-disable & error flag)
     +--------------+
```

### Metody cyklu życia:
1. **`onInitialize()`**: Inicjalizacja zasobów statycznych, rejestracja ustawień i grup parametrów.
2. **`onEnable()`**: Wywoływana w momencie aktywacji modułu (przez kliknięcie w GUI lub skrót klawiszowy).
3. **`onDisable()`**: Wywoływana przy dezaktywacji modułu; czyści stan tymczasowy.
4. **`onTick()`**: Wywoływana w każdym ticku klienta gry (tylko gdy moduł jest aktywny).
5. **`onRender(DrawContext context, float delta)`**: Wywoływana w warstwie renderowania interfejsu (DrawContext).

---

## 4. Bezpieczeństwo i Izolacja Błędów (Error Boundary)

Dla każdej metody cyklu życia `AbstractModule` implementuje bezpieczne wrappery:
- `safeEnable()`
- `safeDisable()`
- `safeTick()`
- `safeRender(DrawContext context, float delta)`

W przypadku wystąpienia nieobsłużonego wyjątku (`Throwable`):
1. Błąd zostaje zalogowany przez dedykowany logger modułu (`LoggerFactory.getLogger("ZombekClient/" + id)`).
2. Stan błędu modułu zostaje ustawiony na `hasError = true`, a treść komunikatu zapisana w `lastErrorMessage`.
3. Moduł zostaje natychmiast bezpiecznie odłączony (`safeDisable()`), nie dopuszczając do wysypania pętli głównej gry Minecraft.
4. Na karcie modułu w GUI pojawia się czerwony znacznik błędu `[BŁĄD - Moduł wstrzymany]`.

---

## 5. Rejestr Modułów (`ModuleManager`)

`pl.zombek.client.modules.ModuleManager` zarządza globalną listą modułów:
- Gwarancja unikalności identyfikatorów modułów (`registerModule`).
- Wyszukiwanie pełnotekstowe (`searchModules(query)`) przeszukujące ID, nazwę, opis oraz nazwę kategorii.
- Filtrowanie po kategorii (`getModulesByCategory(category)`).
- Wywoływanie pętli `onTick()` oraz `onRender()` dla wszystkich aktywnych modułów.
- Płynna synchronizacja zmian z systemem zapisu konfiguracji `ModuleConfigManager`.

---

## 6. Integracja z Warstwą HUD (`IHudElement` & `HudElementRegistry`)

Od Etapu 8A1 moduły prezentujące elementy na ekranie (HUD) mogą implementować interfejs `pl.zombek.client.core.hud.IHudElement`.
- `HudElementRegistry.getInstance().register(this)` rejestruje moduł w pętli renderowania HUD.
- Metoda `render(IHudRenderer renderer)` otrzymuje loader-agnostyczny renderer, a stan widoczności sprawdzany jest przez `isVisible()` (`isEnabled() && !hasError()`).
- Przykłady zaimplementowanych modułów:
  - `KeystrokesModule` (`pl.zombek.client.core.modules.pvp.KeystrokesModule`) w kategorii `PVP`.
  - `CPSModule` (`pl.zombek.client.core.modules.pvp.CPSModule`) w kategorii `PVP`.
  - `ArmorStatusModule` (`pl.zombek.client.core.modules.pvp.ArmorStatusModule`) w kategorii `PVP`.
  - `ComboCounterModule` (`pl.zombek.client.core.modules.pvp.ComboCounterModule`) w kategorii `PVP`.
  - `CooldownsModule` (`pl.zombek.client.core.modules.pvp.CooldownsModule`) w kategorii `PVP`.
  - `CrosshairModule` (`pl.zombek.client.core.modules.visual.CrosshairModule`) w kategorii `VISUAL`.
  - `HitColorModule` (`pl.zombek.client.core.modules.visual.HitColorModule`) w kategorii `VISUAL`.
  - `HitboxModule` (`pl.zombek.client.core.modules.visual.HitboxModule`) w kategorii `VISUAL`.
  - `FreelookModule` (`pl.zombek.client.core.modules.visual.FreelookModule`) w kategorii `VISUAL`.
  - `ReachDisplayModule` (`pl.zombek.client.core.modules.pvp.ReachDisplayModule`) w kategorii `PVP`.
  - `PvPInfoModule` (`pl.zombek.client.core.modules.pvp.PvPInfoModule`) w kategorii `PVP`.
  - `ToggleSprintModule` (`pl.zombek.client.core.modules.pvp.ToggleSprintModule`) w kategorii `PVP` (Etap 8A5).
  - `ToggleSneakModule` (`pl.zombek.client.core.modules.pvp.ToggleSneakModule`) w kategorii `PVP` (Etap 8A5).
  - `UHCSessionHudModule` (`pl.zombek.client.core.modules.survival.UHCSessionHudModule`) w kategorii `SURVIVAL` (Etap 8A6).
  - `UHCOverlayModule` (`pl.zombek.client.core.modules.survival.UHCOverlayModule`) w kategorii `SURVIVAL` (Skalowanie upuszczonych przedmiotów, Etap 8A6).
  - `TntCountdownModule` (`pl.zombek.client.core.modules.pvp.TntCountdownModule`) w kategorii `PVP` (Etap 8A6).
  - `ShinyPotsModule` (`pl.zombek.client.core.modules.visual.ShinyPotsModule`) w kategorii `VISUAL` (Etap 8A6).
  - `SnaplookModule` (`pl.zombek.client.core.modules.pvp.SnaplookModule`) w kategorii `PVP` (Etap 8A6).
  - `TeamViewModule` (`pl.zombek.client.core.modules.hud.TeamViewModule`) w kategorii `HUD` (Etap 8A6).
  - `OneSevenVisualsModule` (`pl.zombek.client.core.modules.visual.oneseven.OneSevenVisualsModule`) w kategorii `VISUAL` (Etap 8A7).
  - `BedwarsHelperModule` (`pl.zombek.client.core.modules.hud.BedwarsHelperModule`) w kategorii `HUD` (Etap 8A8).
  - `HypixelModsModule` (`pl.zombek.client.core.modules.hypixel.HypixelModsModule`) w kategorii `OTHER` (wyświetlanej jako `Social / Server` `🌐`, Etap 8A9).
  - `CoordinatesModule` (`pl.zombek.client.core.modules.hud.info.CoordinatesModule`) w kategorii `HUD` (Etap 8B1).
  - `DirectionHudModule` (`pl.zombek.client.core.modules.hud.info.DirectionHudModule`) w kategorii `HUD` (Etap 8B1).
  - `FpsModule` (`pl.zombek.client.core.modules.hud.info.FpsModule`) w kategorii `HUD` (Etap 8B1).
  - `PingModule` (`pl.zombek.client.core.modules.hud.info.PingModule`) w kategorii `HUD` (Etap 8B1).
  - `MemoryUsageModule` (`pl.zombek.client.core.modules.hud.info.MemoryUsageModule`) w kategorii `HUD` (Etap 8B1).
  - `ScoreboardModule` (`pl.zombek.client.core.modules.hud.scoreboard.ScoreboardModule`) w kategorii `HUD` (Etap 8B2).
  - `PotionEffectsModule` (`pl.zombek.client.core.modules.hud.potions.PotionEffectsModule`) w kategorii `HUD` (Etap 8B2).
  - `SaturationModule` (`pl.zombek.client.core.modules.hud.saturation.SaturationModule`) w kategorii `HUD` (Etap 8B2).
  - `BossBarModule` (`pl.zombek.client.core.modules.hud.bossbar.BossBarModule`) w kategorii `HUD` (Etap 8B2).
  - `ClockModule` (`pl.zombek.client.core.modules.hud.clock.ClockModule`) w kategorii `HUD` (Etap 8B3).
  - `DayCounterModule` (`pl.zombek.client.core.modules.hud.daycounter.DayCounterModule`) w kategorii `HUD` (Etap 8B3).
  - `PlaytimeModule` (`pl.zombek.client.core.modules.hud.playtime.PlaytimeModule`) w kategorii `HUD` (Etap 8B3).
  - `StopwatchModule` (`pl.zombek.client.core.modules.hud.stopwatch.StopwatchModule`) w kategorii `HUD` (Etap 8B3).
  - `ItemCounterModule` (`pl.zombek.client.core.modules.hud.itemcounter.ItemCounterModule`) w kategorii `HUD` (Etap 8B4).
  - `ItemTrackerModule` (`pl.zombek.client.core.modules.hud.itemtracker.ItemTrackerModule`) w kategorii `HUD` (Etap 8B4).
  - `ServerAddressModule` (`pl.zombek.client.core.modules.hud.serveraddress.ServerAddressModule`) w kategorii `HUD` (Etap 8B4).
  - `NameTagsModule` (`pl.zombek.client.core.modules.visual.nametags.NameTagsModule`) w kategorii `VISUAL` (Etap 8B5).
  - `TabEditorModule` (`pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule`) w kategorii `VISUAL` (Etap 8B5).
  - `ChatModule` (`pl.zombek.client.core.modules.visual.chat.ChatModule`) w kategorii `VISUAL` (Etap 8B6).
  - `TitlesModule` (`pl.zombek.client.core.modules.hud.titles.TitlesModule`) w kategorii `HUD` (Etap 8B6).
  - `NetGraphModule` (`pl.zombek.client.core.modules.hud.netgraph.NetGraphModule`) w kategorii `HUD` (Etap 8B7).
  - `ZoomModule` (`pl.zombek.client.core.modules.visual.ZoomModule`) w kategorii `VISUAL` (Etap 8C1).
  - `FovChangerModule` (`pl.zombek.client.core.modules.visual.FovChangerModule`) w kategorii `VISUAL` (Etap 8C1).
  - `MotionBlurModule` (`pl.zombek.client.core.modules.visual.MotionBlurModule`) w kategorii `VISUAL` (Etap 8C1).
  - `HurtCamModule` (`pl.zombek.client.core.modules.visual.HurtCamModule`) w kategorii `VISUAL` (Etap 8C1).
  - `LightingModule` (`pl.zombek.client.core.modules.visual.LightingModule`) w kategorii `VISUAL` (Etap 8C2).
  - `FullBrightModule` (`pl.zombek.client.core.modules.visual.FullBrightModule`) w kategorii `VISUAL` (Etap 8C2).
  - `TimeChangerModule` (`pl.zombek.client.core.modules.visual.TimeChangerModule`) w kategorii `VISUAL` (Etap 8C2).
  - `WeatherChangerModule` (`pl.zombek.client.core.modules.visual.WeatherChangerModule`) w kategorii `VISUAL` (Etap 8C2).
  - `FogCustomizerModule` (`pl.zombek.client.core.modules.visual.FogCustomizerModule`) w kategorii `VISUAL` (Etap 8C3).
  - `ColorSaturationModule` (`pl.zombek.client.core.modules.visual.ColorSaturationModule`) w kategorii `VISUAL` (Etap 8C3).
  - `GlintColorizerModule` (`pl.zombek.client.core.modules.visual.GlintColorizerModule`) w kategorii `VISUAL` (Etap 8C3).
  - `ParticleChangerModule` (`pl.zombek.client.core.modules.visual.ParticleChangerModule`) w kategorii `VISUAL` (Etap 8C3).

Każdy z modułów korzysta z dedykowanych grup ustawień (`SettingGroup`), w pełni wspiera zapis konfiguracji do `modules.json` oraz integruje się z systemem dostawców danych (`ArmorDataProviderRegistry`, `CooldownDataProviderRegistry`, `ComboTracker`, `CrosshairTargetRegistry`, `HitColorTracker`, `HitboxGeometryCalculator`, `FreelookCameraController`, `ReachTracker`, `PvPSessionManager`, `MovementToggleController`, `UhcDataProviderRegistry`, `UhcSessionTracker`, `UhcItemScalingController`, `TntTracker`, `TntCountdownCalculator`, `ShinyPotsController`, `SnaplookController`, `TeamTracker`, `AnimationProfileManager`, `ItemTransformManager`, `BlockingAnimationController`, `SwingAnimationController`, `SneakAnimationController`, `DamageVisualController`, `FirstPersonAnimationRenderer`, `ThirdPersonAnimationRenderer`, `BedwarsDataProviderRegistry`, `BedwarsManager`, `HypixelDataProviderRegistry`, `HypixelManager`, `InfoHudDataProviderRegistry`, `ScoreboardDataProviderRegistry`, `PotionEffectsDataProviderRegistry`, `SaturationDataProviderRegistry`, `BossBarDataProviderRegistry`, `TimeDataProviderRegistry`, `ZombekTimeService`, `InventoryDataProviderRegistry`, `ZombekInventoryService`, `ServerAddressDataProviderRegistry`, `ServerAddressPrivacyController`, `ServerAliasManager`, `PlayerInfoDataProviderRegistry`, `ZombekPlayerInfoService`, `NameTagFriendManager`, `TabLayoutManager`, `CustomTabRenderer`, `CustomNameTagRenderer`, `ZombekChatService`, `ChatTabManager`, `ChatFilterManager`, `ChatHistoryManager`, `ChatNotificationController`, `ChatInputManager`, `CustomChatRenderer`, `ZombekTitleService`, `TitleQueueManager`, `CustomTitleRenderer`, `NetworkDataProviderRegistry`, `NetworkMetricsCollector`, `NetGraphManager`, `NetworkGraphRenderer`, `ZoomController`, `CameraFovController`, `MotionBlurController`, `HurtCamController`, `ZombekCameraController`, `DynamicLightManager`, `FullBrightController`, `WorldTimeVisualController`, `WeatherVisualController`, `ZombekWorldVisualController`, `FogVisualController`, `ColorGradingController`, `GlintRenderController`, `ParticleRenderController`, `ZombekRenderEffectController`).
Wszystkie moduły informacyjne HUD korzystają ze zunifikowanego modelu geometrii `HudComponentModel` (`pl.zombek.client.core.hud.component`) z automatycznym clampingiem do widoku ekranu oraz mechanizmu `HudPriorityResolver` zapobiegającego kolizjom i nakładaniu się komponentów (w tym Net Graph o priorytecie 36, Chat o priorytecie 110, Titles o priorytecie 115, Tab Editor o priorytecie 120 oraz Zoom Indicator o priorytecie 130).

---

## 6. Integracja z Rejestrem Komponentów HUD Editora (`HudComponentRegistry` — Etapy 9A, 9B, 9C i 8C1)

W ramach Etapu 9A oraz rozszerzenia w Etapie 8C1 wszystkie 33 komponenty klienta (w tym wskaźnik `#33 zoom_indicator`) zyskują pełną reprezentację w systemie edytora interfejsu za pośrednictwem dedykowanych deskryptorów (`HudComponentDescriptor`):

### 6.1. Klasyfikacja Komponentów (`HudComponentType`)
- `SCREEN_SPACE`: Elementy przytwierdzone do krawędzi lub rogów ekranu (np. FPS, Ping, Coordinates, Memory, Net Graph, Bedwars Helper, Scoreboard, Potion Effects).
- `CENTER_SCREEN`: Elementy zorientowane względem środka celownika/ekranu (np. Keystrokes, CPS, Reach Display, Crosshair, Titles).
- `WORLD_SPACE`: Projekcje trójwymiarowe w świecie gry (np. NameTags, Hitboxes, TNT Countdown).
- `GUI_OVERLAY`: Modyfikacje standardowych nakładek gry (np. Tab Editor, Chat, Boss Bar).

### 6.2. Zdolności Edycyjne (`HudComponentCapability`)
Deskryptor każdego komponentu ściśle określa dozwolone operacje w edytorze HUD:
- `POSITION`: Zezwolenie na zmianę współrzędnych X/Y.
- `SCALE`: Zezwolenie na zmianę skali w zadanym zakresie (domyślnie 0.5x - 2.5x).
- `SIZE`: Zezwolenie na modyfikację szerokości i wysokości (dla elementów o zmiennej geometrii).
- `VISIBILITY`: Zezwolenie na lokalne ukrywanie/pokazywanie w widoku edytora i HUD.
- `Z_INDEX`: Zezwolenie na zmianę kolejności warstw i priorytetu renderowania.
- `ANCHOR`: Zezwolenie na zmianę punktu zakotwiczenia (`AnchorPoint`).

### 6.3. Dostawcy Podglądu (`HudComponentPreviewProvider`)
Każdy zarejestrowany komponent posiada implementację `HudComponentPreviewProvider` dostarczającą realistyczne dane demonstracyjne (np. 144 FPS, 24 ms ping, 12.4 CPS, kompletna zbroja z trzykrotnym enchantem, realistyczny wykres Net Graph) renderowane wyłącznie wewnątrz płótna edytora HUD bez zakłócania rzeczywistej rozgrywki.

### 6.4. Rejestrator Domyślny (`DefaultHudComponentRegistrar`)
Klasa `DefaultHudComponentRegistrar.registerAll()` jest wywoływana automatycznie podczas startu klienta (`ZombekCore.bootstrap()`), mapując istniejące instancje modułów z `ModuleManager` do `HudComponentRegistry` oraz inicjalizując profil układu w `HudLayoutManager`.



