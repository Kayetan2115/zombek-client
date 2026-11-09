package pl.zombek.client.quilt.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.quilt.platform.QuiltPlatformAdapter;

/**
 * Main entrypoint for ZombekClient on Quilt Loader.
 * Implements Quilt's native PreLaunchEntrypoint and ClientModInitializer.
 */
@ClientOnly
public class QuiltClientBootstrap implements PreLaunchEntrypoint, ClientModInitializer {
    public static final String MOD_ID = ClientConstants.MOD_ID;
    private static final Logger LOGGER = LoggerFactory.getLogger(QuiltClientBootstrap.class);

    private static QuiltClientBootstrap instance;
    private static boolean initialized = false;

    private ModContainer modContainer;
    private QuiltPlatformAdapter platformAdapter;

    public static QuiltClientBootstrap getInstance() {
        return instance;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onPreLaunch(ModContainer mod) {
        if (MinecraftQuiltLoader.getEnvironmentType() != EnvType.CLIENT) {
            LOGGER.info("[{}] Wykryto środowisko serwera dedykowanego, pomijanie ładowania klienta ZombekClient.", MOD_ID);
            return;
        }

        bootstrap(mod);
    }

    @Override
    public void onInitializeClient() {
        if (!initialized) {
            bootstrap(null);
        }
    }

    private synchronized void bootstrap(ModContainer mod) {
        if (initialized) {
            return;
        }

        instance = this;
        this.modContainer = mod;
        LOGGER.info("[{}] Bootstrapping ZombekClient Quilt 1.20.4 Client Adapter...", MOD_ID);

        try {
            // 1. Initialize Common Client Core with QuiltPlatformAdapter
            this.platformAdapter = new QuiltPlatformAdapter(mod);
            ZombekCore.init(this.platformAdapter);

            // 2. Register data providers for HUD modules
            pl.zombek.client.core.data.ArmorDataProviderRegistry.setProvider(new pl.zombek.client.quilt.platform.QuiltArmorDataProvider());
            pl.zombek.client.core.data.CooldownDataProviderRegistry.setProvider(new pl.zombek.client.quilt.platform.QuiltCooldownDataProvider());
            pl.zombek.client.core.data.CrosshairTargetRegistry.setProvider(new pl.zombek.client.quilt.integration.QuiltCrosshairTargetProvider(MinecraftClient.getInstance()));
            pl.zombek.client.core.uhc.UhcDataProviderRegistry.setProvider(new pl.zombek.client.quilt.platform.QuiltUhcDataProvider());
            pl.zombek.client.core.bedwars.BedwarsDataProviderRegistry.setProvider(new pl.zombek.client.quilt.platform.QuiltBedwarsDataProvider());
            pl.zombek.client.core.hypixel.HypixelDataProviderRegistry.setProvider(new pl.zombek.client.quilt.platform.QuiltHypixelDataProvider());
            pl.zombek.client.core.info.InfoHudDataProviderRegistry.getInstance().register(new pl.zombek.client.quilt.platform.QuiltInfoHudDataProvider());
            pl.zombek.client.core.hud.scoreboard.ScoreboardDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltScoreboardDataProvider());
            pl.zombek.client.core.hud.potions.PotionEffectsDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltPotionEffectsDataProvider());
            pl.zombek.client.core.hud.saturation.SaturationDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltSaturationDataProvider());
            pl.zombek.client.core.hud.bossbar.BossBarDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltBossBarDataProvider());
            pl.zombek.client.core.time.TimeDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltTimeDataProvider());
            pl.zombek.client.core.inventory.InventoryDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltInventoryDataProvider());
            pl.zombek.client.core.serveraddress.ServerAddressDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltServerAddressDataProvider());
            pl.zombek.client.core.playerinfo.PlayerInfoDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltPlayerInfoDataProvider());
            pl.zombek.client.core.netgraph.NetworkDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.quilt.integration.QuiltNetworkDataProvider());
            pl.zombek.client.core.worldvisual.lighting.DynamicLightManager.getInstance().setPlatformProvider(new pl.zombek.client.quilt.integration.QuiltDynamicLightProvider());

            // 3. Register JVM shutdown hook for safe configuration saving
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("[{}] Zamykanie klienta Quilt — zapisywanie konfiguracji ZombekClient...", MOD_ID);
                ZombekCore core = ZombekCore.getInstance();
                if (core != null && core.getConfigManager() != null) {
                    core.getConfigManager().saveNow();
                }
            }, "ZombekClient-Quilt-Shutdown"));

            initialized = true;
            LOGGER.info("[{}] ZombekClient Quilt Client Adapter został pomyślnie zainicjalizowany.", MOD_ID);
        } catch (Throwable t) {
            LOGGER.error("[{}] Krytyczny błąd podczas inicjalizacji QuiltClientBootstrap: {}", MOD_ID, t.getMessage(), t);
        }
    }

    public ModContainer getModContainer() {
        return modContainer;
    }

    public QuiltPlatformAdapter getPlatformAdapter() {
        return platformAdapter;
    }
}
