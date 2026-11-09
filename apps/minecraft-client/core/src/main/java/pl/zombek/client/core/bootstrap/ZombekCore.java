package pl.zombek.client.core.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.config.ModuleConfigManager;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.hud.teamview.TeamViewModule;
import pl.zombek.client.core.modules.impl.CoreTestModule;
import pl.zombek.client.core.modules.pvp.ArmorStatusModule;
import pl.zombek.client.core.modules.pvp.CPSModule;
import pl.zombek.client.core.modules.pvp.ComboCounterModule;
import pl.zombek.client.core.modules.pvp.CooldownsModule;
import pl.zombek.client.core.modules.pvp.KeystrokesModule;
import pl.zombek.client.core.modules.pvp.PvPInfoModule;
import pl.zombek.client.core.modules.pvp.ReachDisplayModule;
import pl.zombek.client.core.modules.pvp.ToggleSneakModule;
import pl.zombek.client.core.modules.pvp.ToggleSprintModule;
import pl.zombek.client.core.modules.pvp.snaplook.SnaplookModule;
import pl.zombek.client.core.modules.pvp.tnt.TntCountdownModule;
import pl.zombek.client.core.modules.survival.UHCOverlayModule;
import pl.zombek.client.core.modules.survival.UHCSessionHudModule;
import pl.zombek.client.core.modules.visual.CrosshairModule;
import pl.zombek.client.core.modules.visual.FreelookModule;
import pl.zombek.client.core.modules.visual.HitColorModule;
import pl.zombek.client.core.modules.visual.HitboxModule;
import pl.zombek.client.core.modules.visual.shinypots.ShinyPotsModule;
import pl.zombek.client.core.platform.IPlatformAdapter;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Main coordinator of ZombekClient Core logic.
 * Decoupled from mod loaders through IPlatformAdapter.
 */
public class ZombekCore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConstants.CLIENT_NAME);
    private static ZombekCore instance;

    private final IPlatformAdapter platform;
    private final ModuleManager moduleManager;
    private final ModuleConfigManager configManager;
    private boolean initialized = false;

    public ZombekCore(IPlatformAdapter platform) {
        this.platform = Objects.requireNonNull(platform, "Platform adapter cannot be null");
        this.moduleManager = ModuleManager.getInstance();

        Path configDir = platform.getConfigDirectory();
        this.configManager = new ModuleConfigManager(configDir, moduleManager);
    }

    public static synchronized ZombekCore init(IPlatformAdapter platform) {
        if (instance != null) {
            return instance;
        }
        instance = new ZombekCore(platform);
        instance.bootstrap();
        return instance;
    }

    public static ZombekCore getInstance() {
        return instance;
    }

    public IPlatformAdapter getPlatform() {
        return platform;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ModuleConfigManager getConfigManager() {
        return configManager;
    }

    private void bootstrap() {
        if (initialized) return;

        LOGGER.info("[{}] Bootstrapping {} v{} on platform [{}] (Loader: {}, MC: {})",
                ClientConstants.MOD_ID,
                ClientConstants.CLIENT_NAME,
                ClientConstants.CLIENT_VERSION,
                platform.getLoaderName(),
                platform.getLoaderVersion(),
                platform.getMinecraftVersion());

        // 1. Register base core modules
        moduleManager.registerModule(new KeystrokesModule());
        moduleManager.registerModule(new CPSModule());
        moduleManager.registerModule(new ArmorStatusModule());
        moduleManager.registerModule(new ComboCounterModule());
        moduleManager.registerModule(new CooldownsModule());
        moduleManager.registerModule(new CrosshairModule());
        moduleManager.registerModule(new HitColorModule());
        moduleManager.registerModule(new HitboxModule());
        moduleManager.registerModule(new FreelookModule());
        moduleManager.registerModule(new ReachDisplayModule());
        moduleManager.registerModule(new PvPInfoModule());
        moduleManager.registerModule(new ToggleSprintModule());
        moduleManager.registerModule(new ToggleSneakModule());
        moduleManager.registerModule(new TntCountdownModule());
        moduleManager.registerModule(new SnaplookModule());
        moduleManager.registerModule(new ShinyPotsModule());
        moduleManager.registerModule(new TeamViewModule());
        moduleManager.registerModule(new UHCOverlayModule());
        moduleManager.registerModule(new UHCSessionHudModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.oneseven.OneSevenVisualsModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.BedwarsHelperModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hypixel.HypixelModsModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.info.CoordinatesModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.info.DirectionHudModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.info.FpsModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.info.PingModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.info.MemoryUsageModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.scoreboard.ScoreboardModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.potions.PotionEffectsModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.saturation.SaturationModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.bossbar.BossBarModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.clock.ClockModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.daycounter.DayCounterModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.playtime.PlaytimeModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.stopwatch.StopwatchModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.itemcounter.ItemCounterModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.itemtracker.ItemTrackerModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.serveraddress.ServerAddressModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.nametags.NameTagsModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.chat.ChatModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.titles.TitlesModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.hud.netgraph.NetGraphModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.ZoomModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.FovChangerModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.MotionBlurModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.HurtCamModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.LightingModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.FullBrightModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.TimeChangerModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.WeatherChangerModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.FogCustomizerModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.ColorSaturationModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.GlintColorizerModule());
        moduleManager.registerModule(new pl.zombek.client.core.modules.visual.ParticleChangerModule());
        moduleManager.registerModule(new CoreTestModule());

        // 2. Initialize Central Services
        pl.zombek.client.core.time.ZombekTimeService.init(platform.getConfigDirectory());
        pl.zombek.client.core.friends.NameTagFriendManager.getInstance().setConfigDirectory(platform.getConfigDirectory());
        pl.zombek.client.core.hud.editor.DefaultHudComponentRegistrar.registerAll();
        pl.zombek.client.core.hud.editor.HudLayoutManager.getInstance().initialize(platform.getConfigDirectory());

        // 3. Load persistent configuration
        configManager.load();

        // 4. Register shutdown hook for safe flushing
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("[{}] Shutting down ZombekClient Core, saving state...", ClientConstants.MOD_ID);
            shutdown();
        }, "ZombekClient-Shutdown"));

        initialized = true;
        LOGGER.info("[{}] ZombekClient Core initialized successfully.", ClientConstants.MOD_ID);
    }

    /**
     * Called on each client tick by the active platform adapter.
     */
    public void onClientTick() {
        if (!initialized) return;
        pl.zombek.client.core.time.ZombekTimeService.getInstance().onClientTick();
        pl.zombek.client.core.inventory.ZombekInventoryService.getInstance().onClientTick();
        pl.zombek.client.core.playerinfo.ZombekPlayerInfoService.getInstance().onClientTick();
        pl.zombek.client.core.chat.ZombekChatService.getInstance().onClientTick();
        pl.zombek.client.core.titles.ZombekTitleService.getInstance().onClientTick();
        pl.zombek.client.core.netgraph.NetworkMetricsCollector.getInstance().onClientTick();
        pl.zombek.client.core.camera.ZombekCameraController.getInstance().onClientTick();
        pl.zombek.client.core.rendereffect.ZombekRenderEffectController.getInstance().onClientTick();
        moduleManager.onTick();
    }

    public void shutdown() {
        pl.zombek.client.core.time.ZombekTimeService.getInstance().shutdown();
        pl.zombek.client.core.inventory.ZombekInventoryService.getInstance().onWorldLeave();
        pl.zombek.client.core.playerinfo.ZombekPlayerInfoService.getInstance().clearCache();
        pl.zombek.client.core.chat.ZombekChatService.getInstance().onWorldLeave();
        pl.zombek.client.core.titles.ZombekTitleService.getInstance().onWorldLeave();
        pl.zombek.client.core.netgraph.NetworkMetricsCollector.getInstance().shutdown();
        pl.zombek.client.core.camera.ZombekCameraController.getInstance().resetAll();
        pl.zombek.client.core.worldvisual.ZombekWorldVisualController.getInstance().shutdown();
        pl.zombek.client.core.rendereffect.ZombekRenderEffectController.getInstance().shutdown();
        if (configManager != null) {
            configManager.shutdown();
        }
    }

    /**
     * Resets the singleton instance for isolated test execution.
     */
    public static synchronized void resetInstanceForTesting() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
        pl.zombek.client.core.time.ZombekTimeService.resetForTesting(null);
        pl.zombek.client.core.inventory.ZombekInventoryService.getInstance().resetForTesting();
        pl.zombek.client.core.itemtracker.ItemTrackerManager.getInstance().resetForTesting();
        pl.zombek.client.core.inventory.InventoryDataProviderRegistry.getInstance().resetForTesting();
        pl.zombek.client.core.serveraddress.ServerAddressDataProviderRegistry.getInstance().resetForTesting();
        pl.zombek.client.core.playerinfo.ZombekPlayerInfoService.getInstance().resetForTesting();
        pl.zombek.client.core.friends.NameTagFriendManager.getInstance().clearAll();
        pl.zombek.client.core.playerinfo.PlayerInfoDataProviderRegistry.getInstance().resetForTesting();
        pl.zombek.client.core.chat.ZombekChatService.resetForTesting();
        pl.zombek.client.core.titles.ZombekTitleService.resetForTesting();
        pl.zombek.client.core.netgraph.NetworkMetricsCollector.resetForTesting();
        pl.zombek.client.core.netgraph.NetworkDataProviderRegistry.getInstance().resetForTesting();
        pl.zombek.client.core.camera.ZombekCameraController.getInstance().resetAll();
        pl.zombek.client.core.worldvisual.ZombekWorldVisualController.getInstance().shutdown();
        pl.zombek.client.core.rendereffect.ZombekRenderEffectController.getInstance().resetForTesting();
        pl.zombek.client.core.hud.editor.HudEditorManager.getInstance().resetForTesting();
        pl.zombek.client.core.hud.editor.HudComponentRegistry.getInstance().resetForTesting();
        pl.zombek.client.core.modules.ModuleManager.getInstance().clear();
    }
}
