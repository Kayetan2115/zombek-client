package pl.zombek.client.neoforge.entrypoint;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.neoforge.events.NeoForgeClientEvents;
import pl.zombek.client.neoforge.integration.NeoForgePlatformAdapter;

/**
 * Main entrypoint for ZombekClient on Minecraft NeoForge 1.20.4.
 * Marked with dist = Dist.CLIENT so dedicated servers never load client classes.
 */
@Mod(ZombekNeoForgeMod.MOD_ID)
public class ZombekNeoForgeMod {
    public static final String MOD_ID = ClientConstants.MOD_ID;
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombekNeoForgeMod.class);

    public ZombekNeoForgeMod(IEventBus modEventBus) {
        LOGGER.info("[{}] Bootstrapping ZombekClient NeoForge 1.20.4 Client Adapter...", MOD_ID);

        // 1. Initialize Common Client Core
        ZombekCore.init(new NeoForgePlatformAdapter());

        // 2. Register data providers for HUD modules
        pl.zombek.client.core.data.ArmorDataProviderRegistry.setProvider(new pl.zombek.client.neoforge.integration.NeoForgeArmorDataProvider());
        pl.zombek.client.core.data.CooldownDataProviderRegistry.setProvider(new pl.zombek.client.neoforge.integration.NeoForgeCooldownDataProvider());
        pl.zombek.client.core.data.CrosshairTargetRegistry.setProvider(new pl.zombek.client.neoforge.integration.NeoForgeCrosshairTargetProvider(Minecraft.getInstance()));
        pl.zombek.client.core.uhc.UhcDataProviderRegistry.setProvider(new pl.zombek.client.neoforge.integration.NeoForgeUhcDataProvider());
        pl.zombek.client.core.bedwars.BedwarsDataProviderRegistry.setProvider(new pl.zombek.client.neoforge.integration.NeoForgeBedwarsDataProvider());
        pl.zombek.client.core.hypixel.HypixelDataProviderRegistry.setProvider(new pl.zombek.client.neoforge.integration.NeoForgeHypixelDataProvider());
        pl.zombek.client.core.info.InfoHudDataProviderRegistry.getInstance().register(new pl.zombek.client.neoforge.integration.NeoForgeInfoHudDataProvider());
        pl.zombek.client.core.hud.scoreboard.ScoreboardDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgeScoreboardDataProvider());
        pl.zombek.client.core.hud.potions.PotionEffectsDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgePotionEffectsDataProvider());
        pl.zombek.client.core.hud.saturation.SaturationDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgeSaturationDataProvider());
        pl.zombek.client.core.hud.bossbar.BossBarDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgeBossBarDataProvider());
        pl.zombek.client.core.time.TimeDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgeTimeDataProvider());
        pl.zombek.client.core.inventory.InventoryDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgeInventoryDataProvider());
        pl.zombek.client.core.serveraddress.ServerAddressDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgeServerAddressDataProvider());
        pl.zombek.client.core.playerinfo.PlayerInfoDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgePlayerInfoDataProvider());
        pl.zombek.client.core.netgraph.NetworkDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.neoforge.integration.NeoForgeNetworkDataProvider());
        pl.zombek.client.core.worldvisual.lighting.DynamicLightManager.getInstance().setPlatformProvider(new pl.zombek.client.neoforge.integration.NeoForgeDynamicLightProvider());

        // 3. Register Mod Event Bus listeners (KeyMappings)
        modEventBus.addListener(NeoForgeClientEvents::onRegisterKeyMappings);

        // 3. Register Game Event Bus listeners (Client Tick)
        NeoForge.EVENT_BUS.register(NeoForgeClientEvents.class);

        LOGGER.info("[{}] ZombekClient NeoForge Client Adapter initialization complete.", MOD_ID);
    }
}
