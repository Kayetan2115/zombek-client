package pl.zombek.client.core.info;

/**
 * Compass directions for Minecraft navigation.
 * Properly maps Minecraft rotation yaw to cardinal and intermediate headings.
 */
public enum CompassDirection {
    S("S", "South", "Południe (+Z)", 0.0f, true),
    SW("SW", "South-West", "Południowy Zachód", 45.0f, false),
    W("W", "West", "Zachód (-X)", 90.0f, true),
    NW("NW", "North-West", "Północny Zachód", 135.0f, false),
    N("N", "North", "Północ (-Z)", 180.0f, true),
    NE("NE", "North-East", "Północny Wschód", 225.0f, false),
    E("E", "East", "Wschód (+X)", 270.0f, true),
    SE("SE", "South-East", "Południowy Wschód", 315.0f, false);

    private final String code;
    private final String englishName;
    private final String polishName;
    private final float centerAngle;
    private final boolean cardinal;

    CompassDirection(String code, String englishName, String polishName, float centerAngle, boolean cardinal) {
        this.code = code;
        this.englishName = englishName;
        this.polishName = polishName;
        this.centerAngle = centerAngle;
        this.cardinal = cardinal;
    }

    public String getCode() {
        return code;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getPolishName() {
        return polishName;
    }

    public float getCenterAngle() {
        return centerAngle;
    }

    public boolean isCardinal() {
        return cardinal;
    }

    /**
     * Resolves CompassDirection from standard Minecraft player yaw (0..360 where S=0, W=90, N=180, E=270).
     */
    public static CompassDirection fromMinecraftYaw(float yaw) {
        float normalized = ((yaw % 360.0f) + 360.0f) % 360.0f;
        if (normalized >= 337.5f || normalized < 22.5f) return S;
        if (normalized < 67.5f) return SW;
        if (normalized < 112.5f) return W;
        if (normalized < 157.5f) return NW;
        if (normalized < 202.5f) return N;
        if (normalized < 247.5f) return NE;
        if (normalized < 292.5f) return E;
        return SE;
    }

    @Override
    public String toString() {
        return code;
    }
}
