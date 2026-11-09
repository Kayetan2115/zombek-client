package pl.zombek.client.modules.impl;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.modules.AbstractModule;
import pl.zombek.client.modules.ModuleCategory;
import pl.zombek.client.modules.ModuleMetadata;
import pl.zombek.client.settings.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Reference development module used for verifying framework behavior,
 * testing all setting types, conditional dependencies, and lifecycle hooks.
 */
public class CoreTestModule extends AbstractModule {

    public enum TestDisplayMode {
        COMPACT("Kompaktowy"),
        NORMAL("Normalny"),
        EXPANDED("Rozszerzony");

        private final String displayName;

        TestDisplayMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final BooleanSetting enableFeature;
    private final NumberSetting opacity;
    private final ColorSetting accentColor;
    private final EnumSetting<TestDisplayMode> displayMode;
    private final StringSetting customText;
    private final KeybindSetting actionKey;
    private final MultiSelectSetting visualFlags;

    private int tickCount = 0;

    public CoreTestModule() {
        super(new ModuleMetadata(
                "core_test_module",
                "Moduł Testowy Core",
                "Referencyjny moduł demonstracyjny testujący wszystkie typy ustawień, cykl życia oraz warunki widoczności.",
                ModuleCategory.OTHER,
                "1.0.0",
                "ZombekClient Core"
        ), GLFW.GLFW_KEY_H);

        // 1. Boolean
        this.enableFeature = registerSetting(new BooleanSetting(
                "enable_feature",
                "Włącz funkcję testową",
                "Włącza mechanizmy demonstracyjne modułu testowego.",
                true
        ));

        // 2. Number (with conditional visibility depending on enableFeature)
        this.opacity = registerSetting(new NumberSetting(
                "opacity",
                "Przezroczystość",
                "Poziom przezroczystości elementu renderowanego.",
                80.0, 0.0, 100.0, 5.0, 0, "%"
        ).visibleWhen(enableFeature::getValue));

        // 3. Color
        this.accentColor = registerSetting(new ColorSetting(
                "accent_color",
                "Kolor akcentu",
                "Świąteczny kolor akcentujący moduł.",
                "#F59E0BFF",
                true
        ));

        // 4. Enum
        this.displayMode = registerSetting(new EnumSetting<>(
                "display_mode",
                "Tryb wyświetlania",
                "Określa format renderowania informacji.",
                TestDisplayMode.NORMAL
        ));

        // 5. String
        this.customText = registerSetting(new StringSetting(
                "custom_text",
                "Własny tekst",
                "Tekst wyświetlany w widoku testowym.",
                "ZombekClient 2026"
        ));

        // 6. Keybind
        this.actionKey = registerSetting(new KeybindSetting(
                "action_key",
                "Klawisz akcji",
                "Dodatkowy skrót klawiszowy wywołujący akcję testową.",
                GLFW.GLFW_KEY_G
        ));

        // 7. MultiSelect
        this.visualFlags = registerSetting(new MultiSelectSetting(
                "visual_flags",
                "Flagi wizualne",
                "Elementy dekoracyjne aktywowane dla modułu.",
                Arrays.asList("Ramka", "Cień", "Zaokrąglenie", "Animacja"),
                new LinkedHashSet<>(Arrays.asList("Ramka", "Cień"))
        ));
    }

    public BooleanSetting getEnableFeature() {
        return enableFeature;
    }

    public NumberSetting getOpacity() {
        return opacity;
    }

    public ColorSetting getAccentColor() {
        return accentColor;
    }

    public EnumSetting<TestDisplayMode> getDisplayMode() {
        return displayMode;
    }

    public StringSetting getCustomText() {
        return customText;
    }

    public KeybindSetting getActionKey() {
        return actionKey;
    }

    public MultiSelectSetting getVisualFlags() {
        return visualFlags;
    }

    public int getTickCount() {
        return tickCount;
    }

    @Override
    public void onInitialize() {
        logger.info("CoreTestModule initialized successfully.");
    }

    @Override
    public void onEnable() {
        tickCount = 0;
        logger.info("CoreTestModule enabled! Current display mode: {}", displayMode.getValue());
    }

    @Override
    public void onDisable() {
        logger.info("CoreTestModule disabled! Ticks processed: {}", tickCount);
    }

    @Override
    public void onTick() {
        tickCount++;
    }

    @Override
    public void onRender(DrawContext context, float delta) {
        // Safe HUD render placeholder for test module
    }
}
