package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class HudProfileManagerTest {

    private HudProfileManager profileManager;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) {
        profileManager = HudProfileManager.getInstance();
        profileManager.resetForTesting();
        profileManager.initialize(tempDir);
    }

    @AfterEach
    public void tearDown() {
        profileManager.resetForTesting();
    }

    @Test
    public void testSystemDefaultsRegistered() {
        Collection<HudProfileMetadata> all = profileManager.getAllProfiles();
        assertTrue(all.size() >= 7, "Should register system profiles (default, pvp, survival, uhc, building, minimal, custom)");
        assertTrue(profileManager.hasProfile("default"));
        assertTrue(profileManager.hasProfile("pvp"));
        assertTrue(profileManager.hasProfile("survival"));
        assertTrue(profileManager.hasProfile("uhc"));
        assertTrue(profileManager.hasProfile("building"));
        assertTrue(profileManager.hasProfile("minimal"));
        assertTrue(profileManager.hasProfile("custom"));
        assertEquals("default", profileManager.getActiveProfileId());
    }

    @Test
    public void testActivateProfile() {
        boolean activated = profileManager.activateProfile("pvp");
        assertTrue(activated);
        assertEquals("pvp", profileManager.getActiveProfileId());

        HudLayout activeLayout = HudLayoutManager.getInstance().getActiveLayout();
        assertNotNull(activeLayout);
        assertEquals("pvp", activeLayout.getLayoutId());
    }

    @Test
    public void testActivateInvalidProfileReturnsFalse() {
        assertFalse(profileManager.activateProfile("non_existent_profile_xyz"));
        assertEquals("default", profileManager.getActiveProfileId());
    }

    @Test
    public void testCreateCustomProfile() {
        HudLayout template = new HudLayout("tmpl", "Szablon");
        template.setComponentState(new HudComponentState("fps", 100, 100, 50, 20));

        boolean created = profileManager.createProfile("my_profile", "Mój Profil", "Opis profilu", template);
        assertTrue(created);
        assertTrue(profileManager.hasProfile("my_profile"));

        HudProfileMetadata meta = profileManager.getAllProfiles().stream()
                .filter(p -> p.getId().equalsIgnoreCase("my_profile"))
                .findFirst().orElse(null);
        assertNotNull(meta);
        assertEquals("Mój Profil", meta.getName());
        assertEquals("Opis profilu", meta.getDescription());
        assertFalse(meta.isSystemProfile());
    }

    @Test
    public void testCannotDuplicateToExistingId() {
        assertFalse(profileManager.duplicateProfile("default", "pvp", "Kopia"));
    }

    @Test
    public void testDuplicateProfile() {
        boolean duplicated = profileManager.duplicateProfile("pvp", "pvp_copy", "Kopia PvP");
        assertTrue(duplicated);
        assertTrue(profileManager.hasProfile("pvp_copy"));
    }

    @Test
    public void testRenameProfile() {
        profileManager.createProfile("test_rename", "Przed Zmianą", "Opis", null);
        boolean renamed = profileManager.renameProfile("test_rename", "Po Zmianie");
        assertTrue(renamed);

        HudLayout layout = profileManager.loadProfileLayout("test_rename");
        assertEquals("Po Zmianie", layout.getName());
    }

    @Test
    public void testCannotDeleteDefaultProfile() {
        assertFalse(profileManager.deleteProfile("default"), "Default profile must be protected against deletion");
        assertTrue(profileManager.hasProfile("default"));
    }

    @Test
    public void testCannotDeleteActiveProfile() {
        profileManager.createProfile("temp_prof", "Tymczasowy", "Opis", null);
        profileManager.activateProfile("temp_prof");
        assertFalse(profileManager.deleteProfile("temp_prof"), "Active profile must be protected against deletion");
        assertTrue(profileManager.hasProfile("temp_prof"));
    }

    @Test
    public void testDeleteCustomProfile() {
        profileManager.createProfile("to_delete", "Do Usunięcia", "Opis", null);
        assertTrue(profileManager.hasProfile("to_delete"));

        boolean deleted = profileManager.deleteProfile("to_delete");
        assertTrue(deleted);
        assertFalse(profileManager.hasProfile("to_delete"));
    }

    @Test
    public void testSwitchToNextAndPreviousProfile() {
        String first = profileManager.getActiveProfileId();
        profileManager.switchProfileNext();
        String second = profileManager.getActiveProfileId();
        assertNotEquals(first, second);

        profileManager.switchToPreviousProfile();
        assertEquals(first, profileManager.getActiveProfileId());
    }
}
