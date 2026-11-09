package pl.zombek.client.core.hud.editor;

/**
 * Diagnostic validation warning indicating layout issues (out-of-bounds, overlaps, etc.).
 */
public class LayoutValidationWarning {
    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }

    public enum WarningType {
        OUT_OF_BOUNDS,
        OVERLAP,
        MISSING_COMPONENT,
        INVALID_SCALE,
        SAFE_AREA_VIOLATION
    }

    private final String componentId;
    private final WarningType type;
    private final Severity severity;
    private final String message;

    public LayoutValidationWarning(String componentId, WarningType type, Severity severity, String message) {
        this.componentId = componentId;
        this.type = type;
        this.severity = severity;
        this.message = message;
    }

    public String getComponentId() {
        return componentId;
    }

    public WarningType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
}
