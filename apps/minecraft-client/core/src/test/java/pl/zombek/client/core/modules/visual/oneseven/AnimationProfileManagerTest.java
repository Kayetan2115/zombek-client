package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnimationProfileManagerTest {

    private AnimationProfileManager manager;

    @BeforeEach
    void setUp() {
        manager = AnimationProfileManager.getInstance();
    }

    @Test
    void testApplyPreset17() {
        manager.applyPreset(AnimationPreset.MINECRAFT_1_7);
        AnimationSettings s = manager.getCurrentSettings();
        assertEquals(AnimationPreset.MINECRAFT_1_7, s.getActivePreset());
        assertTrue(s.isEnableOldBlocking());
        assertEquals(BlockingStyle.VANILLA_1_7, s.getBlockingStyle());
        assertTrue(s.isEnableOldBlockhit());
        assertEquals(BlockhitStyle.CLASSIC_1_7, s.getBlockhitStyle());
        assertTrue(s.isEnableOldSwing());
        assertEquals(SwingStyle.VANILLA_1_7, s.getSwingStyle());
        assertTrue(s.isEnableOldSneak());
        assertEquals(SneakTransition.INSTANT_DOWN_SMOOTH_UP, s.getSneakTransition());
        assertTrue(s.isEnableRedArmor());
        assertTrue(s.isEnableOldHealth());
    }

    @Test
    void testApplyPreset18() {
        manager.applyPreset(AnimationPreset.MINECRAFT_1_8);
        AnimationSettings s = manager.getCurrentSettings();
        assertEquals(AnimationPreset.MINECRAFT_1_8, s.getActivePreset());
        assertTrue(s.isEnableOldBlocking());
        assertEquals(BlockingStyle.VANILLA_1_8, s.getBlockingStyle());
        assertEquals(BlockhitStyle.CLASSIC_1_8, s.getBlockhitStyle());
        assertEquals(SwingStyle.VANILLA_1_8, s.getSwingStyle());
        assertFalse(s.isEnableRedArmor()); // 1.8 did not have red armor
        assertFalse(s.isEnableOldHealth());
    }

    @Test
    void testApplyPresetModern() {
        manager.applyPreset(AnimationPreset.MODERN);
        AnimationSettings s = manager.getCurrentSettings();
        assertEquals(AnimationPreset.MODERN, s.getActivePreset());
        assertFalse(s.isEnableOldBlocking());
        assertFalse(s.isEnableOldBlockhit());
        assertFalse(s.isEnableOldSwing());
        assertFalse(s.isEnableOldSneak());
        assertFalse(s.isEnableRedArmor());
    }

    @Test
    void testSaveRenameDuplicateProfile() {
        manager.saveCustomProfile("TestProfile");
        assertTrue(manager.getCustomProfiles().containsKey("TestProfile"));

        boolean renamed = manager.renameProfile("TestProfile", "RenamedProfile");
        assertTrue(renamed);
        assertFalse(manager.getCustomProfiles().containsKey("TestProfile"));
        assertTrue(manager.getCustomProfiles().containsKey("RenamedProfile"));

        AnimationProfile dup = manager.duplicateProfile("RenamedProfile", "DuplicatedProfile");
        assertNotNull(dup);
        assertEquals("DuplicatedProfile", dup.getName());
        assertTrue(manager.getCustomProfiles().containsKey("DuplicatedProfile"));
    }

    @Test
    void testExportAndImportProfile() {
        manager.applyPreset(AnimationPreset.MINECRAFT_1_7);
        manager.saveCustomProfile("ExportTest");
        String json = manager.exportProfile("ExportTest");
        assertNotNull(json);
        assertTrue(json.contains("ExportTest"));
        assertTrue(json.contains("schema_version"));

        AnimationProfile imported = manager.importProfile(json);
        assertNotNull(imported);
        assertTrue(imported.getName().startsWith("ExportTest"));
        assertEquals(BlockingStyle.VANILLA_1_7, imported.getSettings().getBlockingStyle());
    }

    @Test
    void testRejectInvalidProfileJson() {
        assertThrows(IllegalArgumentException.class, () -> manager.importProfile("not a json string"));
        assertThrows(IllegalArgumentException.class, () -> manager.importProfile(""));
        assertThrows(IllegalArgumentException.class, () -> manager.importProfile("[]")); // array instead of object
    }
}
