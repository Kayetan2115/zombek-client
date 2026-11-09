package pl.zombek.client.core.worldvisual;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.worldvisual.lighting.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicLightManagerTest {

    private DynamicLightManager manager;
    private DynamicLightSourceRegistry registry;

    @BeforeEach
    public void setUp() {
        manager = DynamicLightManager.getInstance();
        manager.resetForTesting();
        registry = DynamicLightSourceRegistry.getInstance();
        registry.resetToDefaults();
    }

    @Test
    public void testLightSourceClassifierDefaults() {
        assertEquals(14, LightSourceClassifier.resolveItemLight("minecraft:torch"));
        assertEquals(15, LightSourceClassifier.resolveItemLight("minecraft:lantern"));
        assertEquals(10, LightSourceClassifier.resolveItemLight("minecraft:soul_torch"));
        assertEquals(15, LightSourceClassifier.resolveItemLight("glowstone"));
        assertEquals(0, LightSourceClassifier.resolveItemLight("minecraft:dirt"));

        assertEquals(12, LightSourceClassifier.resolveEntityLight("blaze"));
        assertEquals(8, LightSourceClassifier.resolveEntityLight("magma_cube"));
        assertEquals(11, LightSourceClassifier.resolveEntityLight("glow_squid"));
        assertEquals(0, LightSourceClassifier.resolveEntityLight("zombie"));
    }

    @Test
    public void testCustomLightSources() {
        assertEquals(0, registry.getLightLevelForItem("minecraft:dirt"));

        registry.registerCustomItemLight("minecraft:dirt", 13);
        assertEquals(13, registry.getLightLevelForItem("minecraft:dirt"));

        registry.removeCustomItemLight("minecraft:dirt");
        assertEquals(0, registry.getLightLevelForItem("minecraft:dirt"));
    }

    @Test
    public void testDynamicLightFalloffCalculation() {
        DynamicLightRenderer renderer = new DynamicLightRenderer();
        renderer.configure(1.0, 15, 1.0, 1.0, false, 0.0);

        List<DynamicLightSource> sources = new ArrayList<>();
        sources.add(new DynamicLightSource("torch_1", DynamicLightSourceType.MAIN_HAND, 0.0, 64.0, 0.0, 15, 0xFFFFFFFF));

        // Right at the source: level 15
        int lightAtSource = renderer.calculateDynamicLight(0.0, 64.0, 0.0, sources);
        assertEquals(15, lightAtSource);

        // 5 blocks away: 15 - 5 = 10
        int light5BlocksAway = renderer.calculateDynamicLight(5.0, 64.0, 0.0, sources);
        assertEquals(10, light5BlocksAway);

        // 15+ blocks away: 0
        int lightFarAway = renderer.calculateDynamicLight(20.0, 64.0, 0.0, sources);
        assertEquals(0, lightFarAway);
    }

    @Test
    public void testCacheCapacityAndProximitySorting() {
        LightSourceCache cache = new LightSourceCache();
        cache.configure(2, 20.0); // max 2 sources, 20 blocks max distance

        List<DynamicLightSource> candidates = new ArrayList<>();
        DynamicLightSource near = new DynamicLightSource("near", DynamicLightSourceType.DROPPED_ITEM, 1.0, 0.0, 0.0, 14, 0);
        DynamicLightSource mid = new DynamicLightSource("mid", DynamicLightSourceType.DROPPED_ITEM, 5.0, 0.0, 0.0, 14, 0);
        DynamicLightSource far = new DynamicLightSource("far", DynamicLightSourceType.DROPPED_ITEM, 15.0, 0.0, 0.0, 14, 0);
        DynamicLightSource tooFar = new DynamicLightSource("tooFar", DynamicLightSourceType.DROPPED_ITEM, 30.0, 0.0, 0.0, 14, 0);

        candidates.add(far);
        candidates.add(mid);
        candidates.add(tooFar);
        candidates.add(near);

        cache.updateSources(candidates, 0.0, 0.0, 0.0);

        assertEquals(2, cache.size());
        assertEquals("near", cache.getActiveSources().get(0).getId());
        assertEquals("mid", cache.getActiveSources().get(1).getId());
    }

    @Test
    public void testManagerTickAndProviderIntegration() {
        IDynamicLightProvider mockProvider = (px, py, pz, main, off, dropped, ent) -> {
            List<DynamicLightSource> list = new ArrayList<>();
            list.add(new DynamicLightSource("main_torch", DynamicLightSourceType.MAIN_HAND, px, py, pz, 14, 0));
            return list;
        };

        manager.setPlatformProvider(mockProvider);
        manager.setUpdateIntervalTicks(1);

        manager.onClientTick(10.0, 70.0, 10.0, true, true, true, true);

        assertEquals(1, manager.getActiveSources().size());
        int lightLevel = manager.getDynamicLightLevel(10.0, 70.0, 10.0);
        assertEquals(14, lightLevel);

        manager.setEnabled(false);
        assertEquals(0, manager.getDynamicLightLevel(10.0, 70.0, 10.0));
    }
}
