package pl.zombek.client.core.worldvisual.time;

/**
 * Encapsulates the visual time state applied to sky and celestial rendering.
 */
public class SkyTimeOverride {
    private final long visualTimeTicks;
    private final float skyAngle;
    private final boolean active;

    public SkyTimeOverride(long visualTimeTicks, float skyAngle, boolean active) {
        this.visualTimeTicks = visualTimeTicks;
        this.skyAngle = skyAngle;
        this.active = active;
    }

    public long getVisualTimeTicks() {
        return visualTimeTicks;
    }

    public float getSkyAngle() {
        return skyAngle;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Converts Minecraft day ticks (0 - 24000) into a celestial sky angle (0.0 - 1.0).
     */
    public static float calculateSkyAngle(long dayTicks) {
        long normalized = Math.floorMod(dayTicks, 24000L);
        double d = ((double) normalized) / 24000.0 - 0.25;
        if (d < 0.0) d += 1.0;
        if (d > 1.0) d -= 1.0;
        double e = 1.0 - (Math.cos(d * Math.PI) + 1.0) / 2.0;
        return (float) (d + (e - d) / 3.0);
    }
}
