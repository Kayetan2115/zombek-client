package pl.zombek.client.core.info;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.info.MemoryUsageModule;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryUsageModuleTest {

    private MemoryUsageModule module;

    @BeforeEach
    public void setup() {
        module = new MemoryUsageModule();
    }

    @Test
    public void testMetadataAndSettingsGroups() {
        assertEquals("memory_usage", module.getId());
        assertEquals("Memory Usage (RAM)", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(5, module.getSettingGroups().size());
    }

    @Test
    public void testRenderingMemorySummary() {
        module.setEnabled(true);
        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("RAM:")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("MB")));
    }

    @Test
    public void testMemoryBarRendering() {
        module.getEnableMemoryBar().setValue(true);
        module.setEnabled(true);

        TestHudRenderer renderer = new TestHudRenderer();
        module.render(renderer);

        // Memory bar renders background fill, allocated fill, and used fill
        assertTrue(renderer.fillCallCount > 1);
    }
}
