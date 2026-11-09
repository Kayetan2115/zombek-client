package pl.zombek.client.core.modules.hud.clock;

import java.time.format.DateTimeFormatter;

public enum DateFormatPattern {
    DD_MM_YYYY("DD.MM.YYYY", "dd.MM.yyyy"),
    YYYY_MM_DD("YYYY-MM-DD", "yyyy-MM-dd"),
    MM_DD_YYYY("MM/DD/YYYY", "MM/dd/yyyy");

    private final String label;
    private final String pattern;
    private final DateTimeFormatter formatter;

    DateFormatPattern(String label, String pattern) {
        this.label = label;
        this.pattern = pattern;
        this.formatter = DateTimeFormatter.ofPattern(pattern);
    }

    public String getLabel() {
        return label;
    }

    public String getPattern() {
        return pattern;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }
}
