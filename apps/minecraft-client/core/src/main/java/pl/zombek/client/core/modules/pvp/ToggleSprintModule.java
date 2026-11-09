package pl.zombek.client.core.modules.pvp;

import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.movement.MovementIndicatorStyle;
import pl.zombek.client.core.movement.MovementToggleController;
import pl.zombek.client.core.movement.SprintActivationMode;
import pl.zombek.client.core.settings.*;

/**
 * Toggle Sprint Module.
 * Allows sprinting without holding down the sprint key, with full vanilla condition compliance.
 */
public class ToggleSprintModule extends AbstractModule implements IHudElement {
    private static ToggleSprintModule instance;

    // General
    private final SettingGroup groupGeneral;
    private final EnumSetting<SprintActivationMode> activationModeSetting;
    private final BooleanSetting defaultStateSetting;
    private final BooleanSetting alwaysSprintSetting;
    private final BooleanSetting preserveVanillaSprintSetting;
    private final BooleanSetting disableWhileSneakingSetting;

    // Behavior
    private final SettingGroup groupBehavior;
    private final BooleanSetting sprintWhileMovingForwardSetting;
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting resetOnDeathSetting;
    private final BooleanSetting resetOnRespawnSetting;
    private final BooleanSetting resetOnFocusLostSetting;
    private final BooleanSetting disableInMenusSetting;
    private final BooleanSetting disableWhileUsingItemsSetting;

    // Keybind
    private final SettingGroup groupKeybind;
    private final KeybindSetting toggleKeySetting;
    private final KeybindSetting holdKeySetting;
    private final BooleanSetting resetKeybindSetting;

    // HUD
    private final SettingGroup groupHud;
    private final BooleanSetting showIndicatorSetting;
    private final BooleanSetting showStatusSetting;
    private final EnumSetting<MovementIndicatorStyle> indicatorStyleSetting;
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting offsetXSetting;
    private final NumberSetting offsetYSetting;
    private final NumberSetting indicatorScaleSetting;

    // Appearance
    private final SettingGroup groupAppearance;
    private final ColorSetting activeColorSetting;
    private final ColorSetting inactiveColorSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting opacitySetting;

    public ToggleSprintModule() {
        super(
                "toggle_sprint",
                "Toggle Sprint",
                "Umożliwia automatyczny lub przełączany sprint bez konieczności ciągłego trzymania klawisza.",
                ModuleCategory.PVP,
                GLFW.GLFW_KEY_LEFT_ALT
        );
        instance = this;

        // General
        this.groupGeneral = new SettingGroup("general", "General");
        this.activationModeSetting = new EnumSetting<>(
                "activation_mode", "Activation Mode",
                "Tryb aktywacji: TOGGLE (przełącznik) lub HOLD (przytrzymanie).",
                SprintActivationMode.TOGGLE
        );
        this.defaultStateSetting = new BooleanSetting(
                "default_state", "Default State",
                "Domyślny stan sprintu po włączeniu klienta.", false
        );
        this.alwaysSprintSetting = new BooleanSetting(
                "always_sprint", "Always Sprint",
                "Automatycznie sprintuje przy ruchu do przodu, gdy warunki gry na to pozwalają.", false
        );
        this.preserveVanillaSprintSetting = new BooleanSetting(
                "preserve_vanilla_sprint", "Preserve Vanilla Sprint",
                "Zachowuje działanie standardowego klawisza sprintu Minecraft.", true
        );
        this.disableWhileSneakingSetting = new BooleanSetting(
                "disable_while_sneaking", "Disable While Sneaking",
                "Tymczasowo wyłącza sprint podczas kucania.", true
        );
        this.groupGeneral.addSetting(activationModeSetting);
        this.groupGeneral.addSetting(defaultStateSetting);
        this.groupGeneral.addSetting(alwaysSprintSetting);
        this.groupGeneral.addSetting(preserveVanillaSprintSetting);
        this.groupGeneral.addSetting(disableWhileSneakingSetting);
        registerGroup(this.groupGeneral);

        // Behavior
        this.groupBehavior = new SettingGroup("behavior", "Behavior");
        this.sprintWhileMovingForwardSetting = new BooleanSetting(
                "sprint_while_moving_forward", "Sprint While Moving Forward",
                "Aktywuje sprint tylko podczas poruszania się do przodu.", true
        );
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change",
                "Resetuje stan przełącznika przy zmianie świata lub rozłączeniu.", true
        );
        this.resetOnDeathSetting = new BooleanSetting(
                "reset_on_death", "Reset on Death",
                "Resetuje sprint po śmierci gracza.", true
        );
        this.resetOnRespawnSetting = new BooleanSetting(
                "reset_on_respawn", "Reset on Respawn",
                "Resetuje stan sprintu po odrodzeniu.", true
        );
        this.resetOnFocusLostSetting = new BooleanSetting(
                "reset_on_focus_lost", "Reset on Focus Lost",
                "Zwalnia przytrzymanie klawisza przy utracie fokusu okna gry.", true
        );
        this.disableInMenusSetting = new BooleanSetting(
                "disable_in_menus", "Disable in Menus",
                "Wyłącza sprint przy otwartym menu, GUI lub czacie.", true
        );
        this.disableWhileUsingItemsSetting = new BooleanSetting(
                "disable_while_using_items", "Disable While Using Items",
                "Wyłącza sprint podczas używania przedmiotów (np. jedzenie, łuk).", false
        );
        this.groupBehavior.addSetting(sprintWhileMovingForwardSetting);
        this.groupBehavior.addSetting(resetOnWorldChangeSetting);
        this.groupBehavior.addSetting(resetOnDeathSetting);
        this.groupBehavior.addSetting(resetOnRespawnSetting);
        this.groupBehavior.addSetting(resetOnFocusLostSetting);
        this.groupBehavior.addSetting(disableInMenusSetting);
        this.groupBehavior.addSetting(disableWhileUsingItemsSetting);
        registerGroup(this.groupBehavior);

        // Keybind
        this.groupKeybind = new SettingGroup("keybind", "Keybind");
        this.toggleKeySetting = new KeybindSetting(
                "toggle_key", "Toggle Key",
                "Klawisz przełączania sprintu (domyślnie LEFT ALT).", GLFW.GLFW_KEY_LEFT_ALT
        );
        this.holdKeySetting = new KeybindSetting(
                "hold_key", "Hold Key",
                "Opcjonalny klawisz przytrzymania sprintu.", KeybindSetting.KEY_NONE
        );
        this.resetKeybindSetting = new BooleanSetting(
                "reset_keybind", "Reset Keybind",
                "Przywraca domyślny klawisz LEFT ALT.", false
        );
        this.groupKeybind.addSetting(toggleKeySetting);
        this.groupKeybind.addSetting(holdKeySetting);
        this.groupKeybind.addSetting(resetKeybindSetting);
        registerGroup(this.groupKeybind);

        // HUD
        this.groupHud = new SettingGroup("hud", "HUD");
        this.showIndicatorSetting = new BooleanSetting(
                "show_indicator", "Show Sprint Indicator",
                "Wyświetla wskaźnik sprintu na ekranie gry.", true
        );
        this.showStatusSetting = new BooleanSetting(
                "show_status", "Show Sprint Status",
                "Pokazuje szczegółowy stan (Toggled, Held itp.).", true
        );
        this.indicatorStyleSetting = new EnumSetting<>(
                "indicator_style", "Indicator Style",
                "Styl wizualny wskaźnika.", MovementIndicatorStyle.BRACKETED
        );
        this.anchorSetting = new EnumSetting<>(
                "anchor", "HUD Anchor",
                "Punkt zakotwiczenia wskaźnika.", HudAnchor.TOP_LEFT
        );
        this.offsetXSetting = new NumberSetting(
                "offset_x", "Offset X",
                "Przesunięcie w osi X.", 10.0, -1000.0, 1000.0, 1.0
        );
        this.offsetYSetting = new NumberSetting(
                "offset_y", "Offset Y",
                "Przesunięcie w osi Y.", 100.0, -1000.0, 1000.0, 1.0
        );
        this.indicatorScaleSetting = new NumberSetting(
                "indicator_scale", "Indicator Scale",
                "Skala rozmiaru wskaźnika.", 1.0, 0.5, 2.5, 0.1
        );
        this.groupHud.addSetting(showIndicatorSetting);
        this.groupHud.addSetting(showStatusSetting);
        this.groupHud.addSetting(indicatorStyleSetting);
        this.groupHud.addSetting(anchorSetting);
        this.groupHud.addSetting(offsetXSetting);
        this.groupHud.addSetting(offsetYSetting);
        this.groupHud.addSetting(indicatorScaleSetting);
        registerGroup(this.groupHud);

        // Appearance
        this.groupAppearance = new SettingGroup("appearance", "Appearance");
        this.activeColorSetting = new ColorSetting(
                "active_color", "Active Color",
                "Kolor aktywnego wskaźnika.", 0xFF55FF55
        );
        this.inactiveColorSetting = new ColorSetting(
                "inactive_color", "Inactive Color",
                "Kolor nieaktywnego wskaźnika.", 0xFFAAAAAA
        );
        this.textColorSetting = new ColorSetting(
                "text_color", "Text Color",
                "Kolor bazowy tekstu.", 0xFFFFFFFF
        );
        this.backgroundColorSetting = new ColorSetting(
                "background_color", "Background Color",
                "Kolor tła pod wskaźnikiem.", 0x80000000
        );
        this.opacitySetting = new NumberSetting(
                "opacity", "Opacity",
                "Przezroczystość tła.", 0.6, 0.0, 1.0, 0.05
        );
        this.groupAppearance.addSetting(activeColorSetting);
        this.groupAppearance.addSetting(inactiveColorSetting);
        this.groupAppearance.addSetting(textColorSetting);
        this.groupAppearance.addSetting(backgroundColorSetting);
        this.groupAppearance.addSetting(opacitySetting);
        registerGroup(this.groupAppearance);
    }

    public static ToggleSprintModule getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        HudElementRegistry.getInstance().register(this);
        if (defaultStateSetting.getValue()) {
            MovementToggleController.getInstance().setSprintToggled(true);
        }
    }

    @Override
    public void onDisable() {
        HudElementRegistry.getInstance().unregister(this);
        MovementToggleController.getInstance().resetSprint();
    }

    public void handleKeyInput(boolean isDown) {
        if (!isEnabled()) return;
        MovementToggleController.getInstance().onSprintKeyInput(isDown, activationModeSetting.getValue());
    }

    // ==========================================
    // IHudElement IMPLEMENTATION
    // ==========================================

    @Override
    public String getElementId() {
        return getId();
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError() && showIndicatorSetting.getValue();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible()) return;

        // Check if reset keybind action triggered
        if (resetKeybindSetting.getValue()) {
            toggleKeySetting.reset();
            setKeybind(GLFW.GLFW_KEY_LEFT_ALT);
            resetKeybindSetting.setValue(false);
        }

        String text = getDisplayText();
        float scale = indicatorScaleSetting.getValue().floatValue();
        int textWidth = (int) (renderer.getTextWidth(text) * scale);
        int textHeight = (int) (renderer.getFontHeight() * scale);
        int width = textWidth + 8;
        int height = textHeight + 6;

        int x = anchorSetting.getValue().calculateX(offsetXSetting.getValue().intValue(), width, renderer.getScreenWidth());
        int y = anchorSetting.getValue().calculateY(offsetYSetting.getValue().intValue(), height, renderer.getScreenHeight());

        // Background
        int bg = backgroundColorSetting.getValue();
        double op = opacitySetting.getValue();
        int alpha = (int) (op * 255) & 0xFF;
        int resolvedBg = (alpha << 24) | (bg & 0x00FFFFFF);
        renderer.fill(x, y, width, height, resolvedBg);

        // Text color: active or inactive
        boolean active = MovementToggleController.getInstance().isSprintActive(
                isEnabled(),
                activationModeSetting.getValue(),
                alwaysSprintSetting.getValue(),
                preserveVanillaSprintSetting.getValue(),
                disableWhileSneakingSetting.getValue(),
                disableWhileUsingItemsSetting.getValue()
        );
        int color = active ? activeColorSetting.getValue() : inactiveColorSetting.getValue();

        renderer.drawText(text, x + 4, y + 3, color, true, scale, false);
    }

    public String getDisplayText() {
        return MovementToggleController.getInstance().getSprintStatusText(
                indicatorStyleSetting.getValue(),
                isEnabled(),
                activationModeSetting.getValue(),
                alwaysSprintSetting.getValue()
        );
    }

    // Getters for settings
    public EnumSetting<SprintActivationMode> getActivationModeSetting() { return activationModeSetting; }
    public BooleanSetting getAlwaysSprintSetting() { return alwaysSprintSetting; }
    public BooleanSetting getPreserveVanillaSprintSetting() { return preserveVanillaSprintSetting; }
    public BooleanSetting getDisableWhileSneakingSetting() { return disableWhileSneakingSetting; }
    public BooleanSetting getSprintWhileMovingForwardSetting() { return sprintWhileMovingForwardSetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public BooleanSetting getResetOnDeathSetting() { return resetOnDeathSetting; }
    public BooleanSetting getResetOnRespawnSetting() { return resetOnRespawnSetting; }
    public BooleanSetting getResetOnFocusLostSetting() { return resetOnFocusLostSetting; }
    public BooleanSetting getDisableInMenusSetting() { return disableInMenusSetting; }
    public BooleanSetting getDisableWhileUsingItemsSetting() { return disableWhileUsingItemsSetting; }
    public KeybindSetting getToggleKeySetting() { return toggleKeySetting; }
    public EnumSetting<MovementIndicatorStyle> getIndicatorStyleSetting() { return indicatorStyleSetting; }
}
