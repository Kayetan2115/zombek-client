package pl.zombek.client.forge.entrypoint;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.forge.events.ForgeClientEvents;
import pl.zombek.client.forge.integration.ForgePlatformAdapter;

/**
 * Main Forge entry point for ZombekClient.
 * Registered with @Mod("zombekclient").
 * Enforces client-only execution and server-safe display test.
 */
@Mod(ClientConstants.MOD_ID)
public class ZombekForgeMod {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConstants.CLIENT_NAME);

    public ZombekForgeMod() {
        LOGGER.info("[{}] Constructing {} v{} on Forge...",
                ClientConstants.MOD_ID, ClientConstants.CLIENT_NAME, ClientConstants.CLIENT_VERSION);

        // 1. Register DisplayTest to ensure dedicated servers ignore this client-only mod
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY,
                        (remote, isServer) -> true
                )
        );

        // 2. Only initialize client logic on physical client distribution
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            initClient(modEventBus);
        });
    }

    private static void initClient(IEventBus modEventBus) {
        LOGGER.info("[{}] Bootstrapping Forge client adapter and shared ZombekCore...", ClientConstants.MOD_ID);

        // Initialize Core with ForgePlatformAdapter
        ZombekCore.init(new ForgePlatformAdapter());

        // Register data providers for HUD modules
        pl.zombek.client.core.data.ArmorDataProviderRegistry.setProvider(new pl.zombek.client.forge.integration.ForgeArmorDataProvider());
        pl.zombek.client.core.data.CooldownDataProviderRegistry.setProvider(new pl.zombek.client.forge.integration.ForgeCooldownDataProvider());
        pl.zombek.client.core.data.CrosshairTargetRegistry.setProvider(new pl.zombek.client.forge.integration.ForgeCrosshairTargetProvider(Minecraft.getInstance()));
        pl.zombek.client.core.uhc.UhcDataProviderRegistry.setProvider(new pl.zombek.client.forge.integration.ForgeUhcDataProvider());
        pl.zombek.client.core.bedwars.BedwarsDataProviderRegistry.setProvider(new pl.zombek.client.forge.integration.ForgeBedwarsDataProvider());
        pl.zombek.client.core.hypixel.HypixelDataProviderRegistry.setProvider(new pl.zombek.client.forge.integration.ForgeHypixelDataProvider());
        pl.zombek.client.core.info.InfoHudDataProviderRegistry.getInstance().register(new pl.zombek.client.forge.integration.ForgeInfoHudDataProvider());
        pl.zombek.client.core.hud.scoreboard.ScoreboardDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgeScoreboardDataProvider());
        pl.zombek.client.core.hud.potions.PotionEffectsDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgePotionEffectsDataProvider());
        pl.zombek.client.core.hud.saturation.SaturationDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgeSaturationDataProvider());
        pl.zombek.client.core.hud.bossbar.BossBarDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgeBossBarDataProvider());
        pl.zombek.client.core.time.TimeDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgeTimeDataProvider());
        pl.zombek.client.core.inventory.InventoryDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgeInventoryDataProvider());
        pl.zombek.client.core.serveraddress.ServerAddressDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgeServerAddressDataProvider());
        pl.zombek.client.core.playerinfo.PlayerInfoDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgePlayerInfoDataProvider());
        pl.zombek.client.core.netgraph.NetworkDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.forge.integration.ForgeNetworkDataProvider());
        pl.zombek.client.core.worldvisual.lighting.DynamicLightManager.getInstance().setPlatformProvider(new pl.zombek.client.forge.integration.ForgeDynamicLightProvider());

        // Register Mod Event Bus listeners (e.g. RegisterKeyMappingsEvent)
        modEventBus.register(ForgeClientEvents.class);

        // Register Forge Event Bus listeners (e.g. ClientTickEvent)
        MinecraftForge.EVENT_BUS.register(ForgeClientEvents.class);

        LOGGER.info("[{}] Forge client mod initialized successfully with shared Core.", ClientConstants.MOD_ID);
    }
}
