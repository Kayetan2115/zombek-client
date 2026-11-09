package pl.zombek.client.core.info;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.info.CoordinatesModule;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinatesModuleTest {

    private CoordinatesModule module;
    private InfoHudDataProviderRegistry.FallbackInfoDataProvider fallbackProvider;

    @BeforeEach
    public void setup() {
        InfoHudDataProviderRegistry.getInstance().resetToDefault();
        fallbackProvider = (InfoHudDataProviderRegistry.FallbackInfoDataProvider) InfoHudDataProviderRegistry.getInstance().getProvider();
        module = new CoordinatesModule();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("coordinates", module.getId());
        assertEquals("Coordinates", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(4, module.getSettingGroups().size());
    }

    @Test
    public void testFullFormatRendering() {
        fallbackProvider.setPosition(120.542, 64.0, -230.121);
        fallbackProvider.setDimension("minecraft:overworld");
        fallbackProvider.setRotation(180.0f, 0.0f); // North

        module.setEnabled(true);
        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("120.54")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("-230.12")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Overworld")));
    }

    @Test
    public void testOverworldToNetherConversion() {
        fallbackProvider.setPosition(800.0, 64.0, -1600.0);
        fallbackProvider.setDimension("minecraft:overworld");

        module.setEnabled(true);
        module.getShowNetherEquivalent().setValue(true);
        TestHudRenderer renderer = new TestHudRenderer();
        module.render(renderer);

        // 800 / 8 = 100, -1600 / 8 = -200
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("100, 64, -200")));
    }

    @Test
    public void testNetherToOverworldConversion() {
        fallbackProvider.setPosition(15.0, 70.0, -25.0);
        fallbackProvider.setDimension("minecraft:the_nether");

        module.setEnabled(true);
        module.getShowOverworldEquivalent().setValue(true);
        TestHudRenderer renderer = new TestHudRenderer();
        module.render(renderer);

        // 15 * 8 = 120, -25 * 8 = -200
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("120, 70, -200")));
    }

    @Test
    public void testEndDimensionHidesConversion() {
        fallbackProvider.setPosition(100.0, 50.0, 100.0);
        fallbackProvider.setDimension("minecraft:the_end");

        module.setEnabled(true);
        TestHudRenderer renderer = new TestHudRenderer();
        module.render(renderer);

        // Neither Nether nor Overworld conversion should be drawn
        assertFalse(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Nether:")));
        assertFalse(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Overworld:")));
    }
}
