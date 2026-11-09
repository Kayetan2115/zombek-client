package pl.zombek.client.core.modules.hud.daycounter;

public enum DayDisplayFormat {
    DAY_SPACE("Day 127"),
    DAY_COLON("Day: 127"),
    COMPACT("D127"),
    CUSTOM("Własny format");

    private final String label;

    DayDisplayFormat(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String format(long dayNumber, String customPrefix, String customSuffix) {
        switch (this) {
            case DAY_SPACE:
                return "Day " + dayNumber;
            case DAY_COLON:
                return "Day: " + dayNumber;
            case COMPACT:
                return "D" + dayNumber;
            case CUSTOM:
                String p = customPrefix != null ? customPrefix : "";
                String s = customSuffix != null ? customSuffix : "";
                return p + dayNumber + s;
            default:
                return "Day " + dayNumber;
        }
    }
}
