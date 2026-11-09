package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Visual animation styles for blockhit (simultaneous block and swing).
 */
public enum BlockhitStyle {
    CLASSIC_1_7("Classic 1.7", "Klasyczny blockhit z pełnym ruchem ręki z wersji 1.7"),
    CLASSIC_1_8("Classic 1.8", "Standardowy blockhit z wersji 1.8 z delikatniejszym ruchem"),
    SMOOTH("Smooth", "Płynny, nowoczesny blockhit z interpolacją pozycji"),
    AGGRESSIVE("Aggressive", "Dynamiczny, szybki blockhit o zwiększonej amplitudzie"),
    CUSTOM("Custom", "Własny kąt, prędkość i intensywność animacji");

    private final String displayName;
    private final String description;

    BlockhitStyle(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
