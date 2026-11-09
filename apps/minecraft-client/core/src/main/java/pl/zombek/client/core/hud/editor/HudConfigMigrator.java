package pl.zombek.client.core.hud.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.EnumSetting;
import pl.zombek.client.core.settings.NumberSetting;
import pl.zombek.client.core.settings.Setting;

import java.util.Locale;

/**
 * Non-destructive migrator that extracts existing module position, anchor, and scale settings
 * and populates a HudLayout without modifying or resetting existing module configurations.
 */
public class HudConfigMigrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudConfigMigrator.class);

    /**
     * Inspects the ModuleManager and synchronizes module positions and scales into the target layout.
     * Existing layout values are preserved unless explicitly migrating or unconfigured.
     *
     * @param targetLayout The layout to enrich with existing module settings
     * @param moduleManager Active module manager
     * @return The enriched layout
     */
    public static HudLayout migrateFromModules(HudLayout targetLayout, ModuleManager moduleManager) {
        if (targetLayout == null) {
            targetLayout = new HudLayout("default", "Domyślny Układ");
        }
        if (moduleManager == null) {
            return targetLayout;
        }

        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        int migratedCount = 0;

        for (AbstractModule module : moduleManager.getModules()) {
            String modId = module.getId().toLowerCase(Locale.ROOT);
            HudComponentState state = targetLayout.getComponentState(modId);

            if (state == null) {
                // If descriptor exists, create from default, otherwise create empty state
                if (registry.hasDescriptor(modId)) {
                    state = registry.getDescriptor(modId).get().createDefaultState();
                } else {
                    state = new HudComponentState(modId);
                }
                targetLayout.setComponentState(state);
            }

            state.setEnabled(module.isEnabled());
            state.setVisible(module.isEnabled());

            // Check for positionX setting
            Setting<?> posXSetting = findSetting(module, "pos_x", "position_x", "x");
            if (posXSetting instanceof NumberSetting) {
                state.setPosX(((NumberSetting) posXSetting).getValue().intValue());
                migratedCount++;
            }

            // Check for positionY setting
            Setting<?> posYSetting = findSetting(module, "pos_y", "position_y", "y");
            if (posYSetting instanceof NumberSetting) {
                state.setPosY(((NumberSetting) posYSetting).getValue().intValue());
            }

            // Check for anchor setting
            Setting<?> anchorSetting = findSetting(module, "anchor", "hud_anchor");
            if (anchorSetting instanceof EnumSetting) {
                Object val = ((EnumSetting<?>) anchorSetting).getValue();
                if (val instanceof HudAnchor) {
                    state.setAnchor((HudAnchor) val);
                }
            }

            // Check for scale setting
            Setting<?> scaleSetting = findSetting(module, "scale", "ui_scale", "hud_scale");
            if (scaleSetting instanceof NumberSetting) {
                state.setScale(((NumberSetting) scaleSetting).getValue().doubleValue());
            }
        }

        LOGGER.info("Completed non-destructive HUD migration from modules (checked {} components)", migratedCount);
        return targetLayout;
    }

    private static Setting<?> findSetting(AbstractModule module, String... candidateNames) {
        for (Setting<?> setting : module.getSettings()) {
            for (String candidate : candidateNames) {
                if (candidate.equalsIgnoreCase(setting.getId()) || candidate.equalsIgnoreCase(setting.getName())) {
                    return setting;
                }
            }
        }
        return null;
    }
}
