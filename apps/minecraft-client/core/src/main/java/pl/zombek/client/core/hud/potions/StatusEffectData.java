package pl.zombek.client.core.hud.potions;

/**
 * Immutable data transfer object representing a player's active potion/status effect.
 */
public class StatusEffectData {
    private final String id;
    private final String name;
    private final int amplifier;
    private final int durationTicks;
    private final boolean ambient;
    private final boolean infinite;
    private final StatusEffectCategory category;
    private final int color;

    public StatusEffectData(String id, String name, int amplifier, int durationTicks, boolean ambient, boolean infinite, StatusEffectCategory category, int color) {
        this.id = id != null ? id : "unknown";
        this.name = name != null ? name : "Effect";
        this.amplifier = Math.max(0, amplifier);
        this.durationTicks = Math.max(0, durationTicks);
        this.ambient = ambient;
        this.infinite = infinite || durationTicks > 32000;
        this.category = category != null ? category : StatusEffectCategory.BENEFICIAL;
        this.color = color;
    }

    public StatusEffectData(String id, String name, int amplifier, int durationTicks, StatusEffectCategory category, int color) {
        this(id, name, amplifier, durationTicks, false, false, category, color);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getDurationSeconds() {
        return durationTicks / 20;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean isInfinite() {
        return infinite;
    }

    public StatusEffectCategory getCategory() {
        return category;
    }

    public int getColor() {
        return color;
    }

    /**
     * Formats amplifier as Roman numeral (0 -> I, 1 -> II, etc.)
     */
    public String getAmplifierRoman() {
        switch (amplifier) {
            case 0: return "I";
            case 1: return "II";
            case 2: return "III";
            case 3: return "IV";
            case 4: return "V";
            case 5: return "VI";
            default: return String.valueOf(amplifier + 1);
        }
    }

    /**
     * Formats duration as MM:SS, HH:MM:SS, or infinite symbol.
     */
    public String formatDuration() {
        if (infinite) {
            return "**:**";
        }
        int totalSec = getDurationSeconds();
        int hours = totalSec / 3600;
        int mins = (totalSec % 3600) / 60;
        int secs = totalSec % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, mins, secs);
        } else {
            return String.format("%02d:%02d", mins, secs);
        }
    }

    @Override
    public String toString() {
        return "StatusEffectData{" + id + ", name='" + name + "', level=" + getAmplifierRoman() + ", duration=" + formatDuration() + '}';
    }
}
