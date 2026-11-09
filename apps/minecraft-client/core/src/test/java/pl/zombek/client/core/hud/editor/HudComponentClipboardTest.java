package pl.zombek.client.core.hud.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HudComponentClipboardTest {

    private HudComponentClipboard clipboard;
    private HudLayout layout;

    @BeforeEach
    public void setUp() {
        clipboard = HudComponentClipboard.getInstance();
        clipboard.resetForTesting();

        layout = new HudLayout("test", "Układ Testowy");
        layout.setComponentState(new HudComponentState("fps", 10, 20, 50, 20));
        layout.setComponentState(new HudComponentState("ping", 10, 45, 50, 20));
    }

    @Test
    public void testCopyAndPasteInPlace() {
        assertFalse(clipboard.hasContent());

        clipboard.copyComponents(layout, Arrays.asList("fps", "ping"));
        assertTrue(clipboard.hasContent());
        assertEquals(2, clipboard.getClipboardData().getComponents().size());

        HudLayout targetLayout = new HudLayout("target", "Docelowy");
        targetLayout.setComponentState(new HudComponentState("fps", 0, 0, 50, 20));
        targetLayout.setComponentState(new HudComponentState("ping", 0, 0, 50, 20));

        List<String> pasted = clipboard.paste(targetLayout, HudComponentClipboard.PasteMode.IN_PLACE, 0, 0);
        assertEquals(2, pasted.size());

        HudComponentState targetFps = targetLayout.getComponentState("fps");
        assertEquals(20, targetFps.getPosX()); // 10 + 10 in-place offset
        assertEquals(30, targetFps.getPosY()); // 20 + 10 in-place offset
        assertTrue(targetFps.isVisible());
    }

    @Test
    public void testPasteNearCursor() {
        clipboard.copyComponents(layout, Collections.singleton("fps"));

        HudLayout targetLayout = new HudLayout("target", "Docelowy");
        targetLayout.setComponentState(new HudComponentState("fps", 0, 0, 50, 20));

        clipboard.paste(targetLayout, HudComponentClipboard.PasteMode.NEAR_CURSOR, 200, 300);
        HudComponentState targetFps = targetLayout.getComponentState("fps");
        assertEquals(200, targetFps.getPosX());
        assertEquals(300, targetFps.getPosY());
    }

    @Test
    public void testDuplicateWithOffset() {
        HudComponentState fps = layout.getComponentState("fps");
        assertEquals(10, fps.getPosX());
        assertEquals(20, fps.getPosY());

        List<String> duplicated = clipboard.duplicateComponents(layout, Collections.singleton("fps"), 25, 25);
        assertEquals(1, duplicated.size());
        assertEquals(35, fps.getPosX());
        assertEquals(45, fps.getPosY());
    }
}
