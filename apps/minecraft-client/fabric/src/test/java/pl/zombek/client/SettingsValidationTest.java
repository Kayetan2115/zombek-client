package pl.zombek.client;

import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.modules.impl.CoreTestModule;
import pl.zombek.client.settings.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsValidationTest {

    @Test
    void testBooleanSetting() {
        BooleanSetting setting = new BooleanSetting("bool_test", "Test Bool", "Desc", false);
        assertFalse(setting.getValue());

        setting.toggle();
        assertTrue(setting.getValue());
        assertEquals("true", setting.serialize());

        assertTrue(setting.deserialize("false"));
        assertFalse(setting.getValue());
    }

    @Test
    void testNumberSettingClampingAndStepping() {
        NumberSetting setting = new NumberSetting("num_test", "Number", "Desc", 50.0, 0.0, 100.0, 5.0, 0, "%");
        assertEquals(50.0, setting.getValue());
        assertEquals("50%", setting.getFormattedValue());

        // Above max -> clamps to max
        setting.setValue(150.0);
        assertEquals(100.0, setting.getValue());

        // Below min -> clamps to min
        setting.setValue(-20.0);
        assertEquals(0.0, setting.getValue());

        // Step alignment: 12.3 should round to nearest step (10.0 or 15.0) -> 10.0
        setting.setValue(12.3);
        assertEquals(10.0, setting.getValue());

        setting.setValue(13.0);
        assertEquals(15.0, setting.getValue());

        // Deserialization
        assertTrue(setting.deserialize("75.0"));
        assertEquals(75.0, setting.getValue());
    }

    @Test
    void testColorSetting() {
        ColorSetting color = new ColorSetting("color_test", "Color", "Desc", "#F59E0BFF", true);
        assertEquals(0xFFF59E0B, color.getArgb());
        assertEquals(0xF5, color.getRed());
        assertEquals(0x9E, color.getGreen());
        assertEquals(0x0B, color.getBlue());
        assertEquals(0xFF, color.getAlpha());
        assertEquals("#F59E0BFF", color.getHex());

        // Modify RGBA
        color.setRgba(16, 185, 129, 200);
        assertEquals(16, color.getRed());
        assertEquals(185, color.getGreen());
        assertEquals(129, color.getBlue());
        assertEquals(200, color.getAlpha());

        // Parse hex without alpha
        int parsedRgb = ColorSetting.parseHex("#10B981");
        assertEquals(0xFF10B981, parsedRgb);
    }

    @Test
    void testEnumSetting() {
        EnumSetting<CoreTestModule.TestDisplayMode> setting = new EnumSetting<>(
                "mode", "Mode", "Desc", CoreTestModule.TestDisplayMode.NORMAL);

        assertEquals(CoreTestModule.TestDisplayMode.NORMAL, setting.getValue());

        setting.cycleNext();
        assertEquals(CoreTestModule.TestDisplayMode.EXPANDED, setting.getValue());

        setting.cycleNext();
        assertEquals(CoreTestModule.TestDisplayMode.COMPACT, setting.getValue());

        assertTrue(setting.deserialize("NORMAL"));
        assertEquals(CoreTestModule.TestDisplayMode.NORMAL, setting.getValue());

        assertFalse(setting.deserialize("INVALID_MODE_NAME"));
    }

    @Test
    void testStringSettingConstraints() {
        StringSetting setting = new StringSetting("text", "Text", "Desc", "Default", 10, Pattern.compile("^[a-zA-Z0-9]+$"));
        assertEquals("Default", setting.getValue());

        // Valid set
        assertTrue(setting.setValue("Valid123"));
        assertEquals("Valid123", setting.getValue());

        // Over max length (10) -> truncated
        assertTrue(setting.setValue("TooLongString1234567"));
        assertEquals("TooLongStr", setting.getValue());

        // Regex violation: special characters rejected, reverts to default
        setting.setValue("Invalid#Text!");
        assertEquals("Default", setting.getValue());
    }

    @Test
    void testKeybindSettingNames() {
        KeybindSetting key = new KeybindSetting("kb", "Key", "Desc", GLFW.GLFW_KEY_RIGHT_SHIFT);
        assertEquals("RSHIFT", key.getKeyName());
        assertTrue(key.isBound());

        key.setValue(KeybindSetting.KEY_NONE);
        assertEquals("NONE", key.getKeyName());
        assertFalse(key.isBound());

        key.setValue(GLFW.GLFW_KEY_F5);
        assertEquals("F5", key.getKeyName());
    }

    @Test
    void testMultiSelectSetting() {
        MultiSelectSetting setting = new MultiSelectSetting(
                "flags", "Flags", "Desc",
                Arrays.asList("OptA", "OptB", "OptC"),
                new LinkedHashSet<>(Collections.singletonList("OptA"))
        );

        assertTrue(setting.isSelected("OptA"));
        assertFalse(setting.isSelected("OptB"));

        setting.toggle("OptB");
        assertTrue(setting.isSelected("OptB"));

        setting.toggle("OptA");
        assertFalse(setting.isSelected("OptA"));

        // Serialization
        assertEquals("OptB", setting.serialize());
        assertTrue(setting.deserialize("OptA,OptC"));
        assertTrue(setting.isSelected("OptA"));
        assertTrue(setting.isSelected("OptC"));
    }

    @Test
    void testConditionalVisibility() {
        BooleanSetting master = new BooleanSetting("master", "Master", "Desc", false);
        NumberSetting child = new NumberSetting("child", "Child", "Desc", 10.0, 0.0, 100.0, 1.0)
                .visibleWhen(master::getValue);

        assertFalse(child.isVisible(), "Child should be hidden when master is false");

        master.setValue(true);
        assertTrue(child.isVisible(), "Child should become visible when master is true");
    }
}
