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
import pl.zombek.client.core.movement.SneakActivationMode;
import pl.zombek.client.core.settings.*;

/**
 * Toggle Sneak Module.
 * Allows sneaking without holding down the SHIFT key, with physical hold priority.
 */
public class ToggleSneakModule extends AbstractModule implements IHudElement {
    private static ToggleSneakModule instance;

    // General
    private final SettingGroup groupGeneral;
    private final EnumSetting<SneakActivationMode> activationModeSetting;
    private final BooleanSetting defaultStateSetting;
    private final BooleanSetting toggleAndPhysicalHoldSetting;
    private final BooleanSetting preserveVanillaSneakSetting;

    // Behavior
    private final SettingGroup groupBehavior;
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting resetOnDeathSetting;
    private final BooleanSetting resetOnRespawnSetting;
    private final BooleanSetting resetOnFocusLostSetting;
    private final BooleanSetting disableInMenusSetting;
    private final BooleanSetting disableWhileFlyingSetting;
    private final BooleanSetting disableWhileRidingSetting;

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

    // Colors
    private final SettingGroup groupColors;
    private final ColorSetting activeColorSetting;
    private final ColorSetting inactiveColorSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting opacitySetting;

    public ToggleSneakModule() {
        super(
                "toggle_sneak",
                "Toggle Sneak",
                "Umożliwia przełączanie kucania bez konieczności ciągłego przytrzymywania klawisza SHIFT.",
                ModuleCategory.PVP,
                GLFW.GLFW_KEY_C
        );
        instance = this;

        // General
        this.groupGeneral = new SettingGroup("general", "General");
        this.activationModeSetting = new EnumSetting<>(
                "activation_mode", "Activation Mode",
                "Tryb aktywacji: TOGGLE, HOLD lub TOGGLE_AND_HOLD.",
                SneakActivationMode.TOGGLE_AND_HOLD
        );
        this.defaultStateSetting = new BooleanSetting(
                "default_state", "Default State",
                "Domyślny stan kucania po uruchomieniu klienta.", false
        );
        this.toggleAndPhysicalHoldSetting = new BooleanSetting(
                "toggle_and_physical_hold", "Toggle + Physical Hold",
                "Fizyczne wciśnięcie SHIFT zawsze kuca niezależnie od stanu toggle.", true
        );
        this.preserveVanillaSneakSetting = new BooleanSetting(
                "preserve_vanilla_sneak", "Preserve Vanilla Sneak",
                "Zachowuje domyślne działanie klawisza SHIFT w grze.", true
        );
        this.groupGeneral.addSetting(activationModeSetting);
        this.groupGeneral.addSetting(defaultStateSetting);
        this.groupGeneral.addSetting(toggleAndPhysicalHoldSetting);
        this.groupGeneral.addSetting(preserveVanillaSneakSetting);
        registerGroup(this.groupGeneral);

        // Behavior
        this.groupBehavior = new SettingGroup("behavior", "Behavior");
        this.resetOnWorldChangeSetting = new BooleanSetting(
                "reset_on_world_change", "Reset on World Change",
                "Wyłącza kucanie przy zmianie świata lub rozłączeniu.", true
        );
        this.resetOnDeathSetting = new BooleanSetting(
                "reset_on_death", "Reset on Death",
                "Wyłącza kucanie po śmierci gracza.", true
        );
        this.resetOnRespawnSetting = new BooleanSetting(
                "reset_on_respawn", "Reset on Respawn",
                "Resetuje kucanie po odrodzeniu.", true
        );
        this.resetOnFocusLostSetting = new BooleanSetting(
                "reset_on_focus_lost", "Reset on Focus Lost",
                "Bezpiecznie zwalnia kucanie przy utracie fokusu okna.", true
        );
        this.disableInMenusSetting = new BooleanSetting(
                "disable_in_menus", "Disable in Menus",
                "Wyłącza kucanie podczas otwarcia menu, ekwipunku lub czatu.", true
        );
        this.disableWhileFlyingSetting = new BooleanSetting(
                "disable_while_flying", "Disable While Flying",
                "Wyłącza kucanie podczas latania (Creative / Spectator).", true
        );
        this.disableWhileRidingSetting = new BooleanSetting(
                "disable_while_riding", "Disable While Riding",
                "Wyłącza kucanie podczas jazdy na wierzchowcu lub w łódce.", true
        );
        this.groupBehavior.addSetting(resetOnWorldChangeSetting);
        this.groupBehavior.addSetting(resetOnDeathSetting);
        this.groupBehavior.addSetting(resetOnRespawnSetting);
        this.groupBehavior.addSetting(resetOnFocusLostSetting);
        this.groupBehavior.addSetting(disableInMenusSetting);
        this.groupBehavior.addSetting(disableWhileFlyingSetting);
        this.groupBehavior.addSetting(disableWhileRidingSetting);
        registerGroup(this.groupBehavior);

        // Keybind
        this.groupKeybind = new SettingGroup("keybind", "Keybind");
        this.toggleKeySetting = new KeybindSetting(
                "toggle_key", "Toggle Key",
                "Klawisz przełączania kucania (domyślnie C).", GLFW.GLFW_KEY_C
        );
        this.holdKeySetting = new KeybindSetting(
                "hold_key", "Hold Key",
                "Opcjonalny dodatkowy klawisz kucania.", KeybindSetting.KEY_NONE
        );
        this.resetKeybindSetting = new BooleanSetting(
                "reset_keybind", "Reset Keybind",
                "Przywraca domyślny klawisz C.", false
        );
        this.groupKeybind.addSetting(toggleKeySetting);
        this.groupKeybind.addSetting(holdKeySetting);
        this.groupKeybind.addSetting(resetKeybindSetting);
        registerGroup(this.groupKeybind);

        // HUD
        this.groupHud = new SettingGroup("hud", "HUD");
        this.showIndicatorSetting = new BooleanSetting(
                "show_indicator", "Show Sneak Indicator",
                "Wyświetla wskaźnik kucania na ekranie.", true
        );
        this.showStatusSetting = new BooleanSetting(
                "show_status", "Show Status",
                "Pokazuje stan kucania na wskaźniku.", true
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
                "Przesunięcie w osi Y.", 120.0, -1000.0, 1000.0, 1.0
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

        // Colors
        this.groupColors = new SettingGroup("colors", "Colors");
        this.activeColorSetting = new ColorSetting(
                "active_color", "Active Color",
                "Kolor aktywnego wskaźnika kucania.", 0xFFFF5555
        );
        this.inactiveColorSetting = new ColorSetting(
                "inactive_color", "Inactive Color",
                "Kolor nieaktywnego wskaźnika.", 0xFFAAAAAA
        );
        this.textColorSetting = new ColorSetting(
                "text_color", "Text Color",
                "Kolor tekstu.", 0xFFFFFFFF
        );
        this.backgroundColorSetting = new ColorSetting(
                "background_color", "Background Color",
                "Kolor tła.", 0x80000000
        );
        this.opacitySetting = new NumberSetting(
                "opacity", "Opacity",
                "Przezroczystość tła.", 0.6, 0.0, 1.0, 0.05
        );
        this.groupColors.addSetting(activeColorSetting);
        this.groupColors.addSetting(inactiveColorSetting);
        this.groupColors.addSetting(textColorSetting);
        this.groupColors.addSetting(backgroundColorSetting);
        this.groupColors.addSetting(opacitySetting);
        registerGroup(this.groupColors);
    }

    public static ToggleSneakModule getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        HudElementRegistry.getInstance().register(this);
        if (defaultStateSetting.getValue()) {
            MovementToggleController.getInstance().setSneakToggled(true);
        }
    }

    @Override
    public void onDisable() {
        HudElementRegistry.getInstance().unregister(this);
        MovementToggleController.getInstance().resetSneak();
    }

    public void handleKeyInput(boolean isDown) {
        if (!isEnabled()) return;
        MovementToggleController.getInstance().onSneakKeyInput(isDown, activationModeSetting.getValue());
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
            setKeybind(GLFW.GLFW_KEY_C);
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
        boolean active = MovementToggleController.getInstance().isSneakActive(
                isEnabled(),
                activationModeSetting.getValue(),
                preserveVanillaSneakSetting.getValue(),
                disableWhileFlyingSetting.getValue(),
                disableWhileRidingSetting.getValue()
        );
        int color = active ? activeColorSetting.getValue() : inactiveColorSetting.getValue();

        renderer.drawText(text, x + 4, y + 3, color, true, scale, false);
    }

    public String getDisplayText() {
        return MovementToggleController.getInstance().getSneakStatusText(
                indicatorStyleSetting.getValue(),
                isEnabled(),
                activationModeSetting.getValue()
        );
    }

    // Getters for settings
    public EnumSetting<SneakActivationMode> getActivationModeSetting() { return activationModeSetting; }
    public BooleanSetting getPreserveVanillaSneakSetting() { return preserveVanillaSneakSetting; }
    public BooleanSetting getResetOnWorldChangeSetting() { return resetOnWorldChangeSetting; }
    public BooleanSetting getResetOnDeathSetting() { return resetOnDeathSetting; }
    public BooleanSetting getResetOnRespawnSetting() { return resetOnRespawnSetting; }
    public BooleanSetting getResetOnFocusLostSetting() { return resetOnFocusLostSetting; }
    public BooleanSetting getDisableInMenusSetting() { return disableInMenusSetting; }
    public BooleanSetting getDisableWhileFlyingSetting() { return disableWhileFlyingSetting; }
    public BooleanSetting getDisableWhileRidingSetting() { return disableWhileRidingSetting; }
    public KeybindSetting getToggleKeySetting() { return toggleKeySetting; }
    public EnumSetting<MovementIndicatorStyle> getIndicatorStyleSetting() { return indicatorStyleSetting; }
}
