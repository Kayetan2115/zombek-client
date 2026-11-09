package pl.zombek.client.core.hud.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result report returned after applying a HUD preset.
 */
public class PresetApplyResult {
    private final boolean success;
    private final String presetId;
    private final List<String> changedComponents;
    private final List<String> missingComponents;
    private final String message;

    public PresetApplyResult(boolean success, String presetId, List<String> changedComponents, List<String> missingComponents, String message) {
        this.success = success;
        this.presetId = presetId;
        this.changedComponents = changedComponents != null ? changedComponents : new ArrayList<>();
        this.missingComponents = missingComponents != null ? missingComponents : new ArrayList<>();
        this.message = message != null ? message : "";
    }

    public boolean isSuccess() {
        return success;
    }

    public String getPresetId() {
        return presetId;
    }

    public List<String> getChangedComponents() {
        return Collections.unmodifiableList(changedComponents);
    }

    public List<String> getMissingComponents() {
        return Collections.unmodifiableList(missingComponents);
    }

    public String getMessage() {
        return message;
    }
}
