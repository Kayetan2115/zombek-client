package pl.zombek.client.core.rendereffect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.rendereffect.particle.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ParticleRenderControllerTest {

    private ParticleRenderController controller;

    @BeforeEach
    public void setup() {
        controller = ParticleRenderController.getInstance();
        controller.reset();
        controller.setActive(true);
    }

    @Test
    public void testDistanceCutoff() {
        ParticleVisualSettings settings = new ParticleVisualSettings(
                1.0f, 1.0f, 1.0f, 1000, 32.0f, Collections.emptyMap(), Collections.emptyMap()
        );
        controller.updateSettings(settings);

        // Within 32 blocks (10 blocks away)
        assertTrue(controller.shouldSpawnParticle("crit", 10.0, 64.0, 0.0, 0.0, 64.0, 0.0));

        // Beyond 32 blocks (40 blocks away)
        assertFalse(controller.shouldSpawnParticle("crit", 40.0, 64.0, 0.0, 0.0, 64.0, 0.0),
                "Particles beyond distance cutoff must be throttled");
    }

    @Test
    public void testMaxParticleLimitCap() {
        ParticleVisualSettings settings = new ParticleVisualSettings(
                1.0f, 1.0f, 1.0f, 5, 64.0f, Collections.emptyMap(), Collections.emptyMap()
        );
        controller.updateSettings(settings);

        for (int i = 0; i < 5; i++) {
            assertTrue(controller.shouldSpawnParticle("smoke", 0, 0, 0, 0, 0, 0));
            controller.onParticleSpawned();
        }

        assertEquals(5, controller.getActiveParticleCount());

        // 6th particle must be blocked by the hard cap
        assertFalse(controller.shouldSpawnParticle("smoke", 0, 0, 0, 0, 0, 0));

        // Decrement and verify it can spawn again
        controller.onParticleRemoved();
        assertTrue(controller.shouldSpawnParticle("smoke", 0, 0, 0, 0, 0, 0));
    }

    @Test
    public void testCustomParticleRuleResolution() {
        Map<String, CustomParticleRule> rules = new HashMap<>();
        rules.put("crit", new CustomParticleRule(
                "crit", true, 1.0f, 2.5f, true, 0x00FF88, 0.8f
        ));

        ParticleVisualSettings settings = new ParticleVisualSettings(
                1.0f, 1.0f, 1.0f, 1000, 64.0f, Collections.emptyMap(), rules
        );
        controller.updateSettings(settings);

        assertEquals(2.5f, controller.resolveScaleMultiplier("crit"), 0.01f);
        int color = controller.resolveParticleColor("crit", 0xFFFFFFFF);
        assertEquals(0x00FF88, color & 0x00FFFFFF);
        assertEquals(0.8f, controller.resolveOpacity("crit", 1.0f), 0.01f);
    }
}
