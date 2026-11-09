package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.survival.UHCOverlayModule;
import pl.zombek.client.core.uhc.UhcScaleMode;

import static org.junit.jupiter.api.Assertions.*;

public class UHCOverlayModuleTest {

    private UHCOverlayModule module;

    @BeforeEach
    public void setUp() {
        module = new UHCOverlayModule();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("uhc_overlay", module.getId());
        assertEquals("UHC Overlay", module.getName());
        assertEquals(ModuleCategory.SURVIVAL, module.getCategory());
        assertFalse(module.isEnabled());
    }

    @Test
    public void testSettingGroupsAndDefaults() {
        assertNotNull(module.getGroup("general"));
        assertNotNull(module.getGroup("items"));
        assertNotNull(module.getGroup("appearance"));

        assertEquals(1.5, module.getGlobalScaleSetting().getValue().doubleValue());
        assertEquals(UhcScaleMode.PER_ITEM, module.getScaleModeSetting().getValue());
        assertEquals(2.0, module.getGoldenApplesScaleSetting().getValue().doubleValue());
        assertEquals(2.0, module.getGoldenHeadsScaleSetting().getValue().doubleValue());
        assertEquals(1.8, module.getEnderPearlsScaleSetting().getValue().doubleValue());
        assertEquals(1.5, module.getArrowsScaleSetting().getValue().doubleValue());
        assertEquals(1.6, module.getHealingItemsScaleSetting().getValue().doubleValue());
        assertEquals(1.2, module.getBlocksScaleSetting().getValue().doubleValue());
        assertEquals(1.4, module.getBucketsScaleSetting().getValue().doubleValue());
        assertEquals(1.4, module.getFlintAndSteelScaleSetting().getValue().doubleValue());

        assertTrue(module.getPreserveAnimationSetting().getValue());
        assertTrue(module.getPreserveRotationSetting().getValue());
        assertTrue(module.getPreserveLightingSetting().getValue());
        assertTrue(module.getPreserveShadowsSetting().getValue());
    }
}
