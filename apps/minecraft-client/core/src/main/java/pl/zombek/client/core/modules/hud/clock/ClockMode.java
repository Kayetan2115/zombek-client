package pl.zombek.client.core.modules.hud.clock;

public enum ClockMode {
    DIGITAL("Cyfrowy (Digital)"),
    MINIMAL("Minimalny (Minimal)"),
    DETAILED("Szczegółowy (Detailed)"),
    MINECRAFT("Świat gry (Minecraft)"),
    DUAL("Podwójny (Dual)"),
    CUSTOM("Własny (Custom)");

    private final String displayName;

    ClockMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
