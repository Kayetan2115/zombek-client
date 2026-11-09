package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.hud.HudAnchor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HudLayoutSerializationTest {

    @Test
    void testJsonRoundTrip() {
        HudLayout layout = new HudLayout("custom_layout", "Mój Profil");
        HudComponentState state = new HudComponentState("fps");
        state.setPosX(45);
        state.setPosY(120);
        state.setWidth(80);
        state.setHeight(24);
        state.setScale(1.2);
        state.setAnchor(HudAnchor.TOP_RIGHT);
        layout.setComponentState(state);

        String json = HudLayoutSerializer.toJson(layout);
        assertNotNull(json);
        assertTrue(json.contains("custom_layout"));
        assertTrue(json.contains("TOP_RIGHT"));

        HudLayout deserialized = HudLayoutSerializer.fromJson(json);
        assertNotNull(deserialized);
        assertEquals("custom_layout", deserialized.getLayoutId());
        assertEquals("Mój Profil", deserialized.getName());

        HudComponentState readState = deserialized.getComponentState("fps");
        assertNotNull(readState);
        assertEquals(45, readState.getPosX());
        assertEquals(120, readState.getPosY());
        assertEquals(80, readState.getWidth());
        assertEquals(24, readState.getHeight());
        assertEquals(1.2, readState.getScale(), 0.001);
        assertEquals(HudAnchor.TOP_RIGHT, readState.getAnchor());
    }

    @Test
    void testCorruptJsonRecovery() {
        String corruptJson = "{ \"layoutId\": \"bad\", \"components\": \"invalid_structure\" }";
        HudLayout recovered = HudLayoutSerializer.fromJson(corruptJson);
        assertNotNull(recovered);
        assertTrue(HudLayoutValidator.isValid(recovered));
    }

    @Test
    void testFileSaveAndLoad(@TempDir Path tempDir) throws IOException {
        Path layoutFile = tempDir.resolve("hud_layout.json");

        HudLayout layout = new HudLayout("file_layout", "Zapis Do Pliku");
        HudComponentState state = new HudComponentState("ping");
        state.setPosX(15);
        state.setPosY(25);
        state.setScale(1.5);
        state.setAnchor(HudAnchor.BOTTOM_LEFT);
        layout.setComponentState(state);

        HudLayoutSerializer.saveToFile(layout, layoutFile);
        assertTrue(Files.exists(layoutFile));

        HudLayout loaded = HudLayoutSerializer.loadFromFile(layoutFile);
        assertNotNull(loaded);
        assertEquals("file_layout", loaded.getLayoutId());
        assertEquals("Zapis Do Pliku", loaded.getName());

        HudComponentState loadedPing = loaded.getComponentState("ping");
        assertNotNull(loadedPing);
        assertEquals(15, loadedPing.getPosX());
        assertEquals(25, loadedPing.getPosY());
        assertEquals(1.5, loadedPing.getScale(), 0.001);
        assertEquals(HudAnchor.BOTTOM_LEFT, loadedPing.getAnchor());
    }

    @Test
    void testDeepCloningMutationSafety() {
        HudLayout original = new HudLayout("orig", "Oryginał");
        HudComponentState state = new HudComponentState("test");
        state.setPosX(10);
        original.setComponentState(state);

        HudLayout clone = original.cloneLayout();
        assertEquals(10, clone.getComponentState("test").getPosX());

        // Mutate clone
        clone.getComponentState("test").setPosX(999);
        assertEquals(999, clone.getComponentState("test").getPosX());
        // Original remains untouched!
        assertEquals(10, original.getComponentState("test").getPosX());
    }
}
