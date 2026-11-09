package pl.zombek.client.core.worldvisual.lighting;

import java.util.Objects;

/**
 * Represents an active localized dynamic light source in the client world.
 */
public class DynamicLightSource {
    private final String id;
    private final DynamicLightSourceType sourceType;
    private double x;
    private double y;
    private double z;
    private int baseLightLevel;
    private int effectiveLightLevel;
    private int argbColor;
    private long lastUpdateTime;
    private boolean active;

    public DynamicLightSource(String id, DynamicLightSourceType sourceType, double x, double y, double z, int baseLightLevel, int argbColor) {
        this.id = id != null ? id : "unknown";
        this.sourceType = sourceType != null ? sourceType : DynamicLightSourceType.CUSTOM;
        this.x = x;
        this.y = y;
        this.z = z;
        this.baseLightLevel = Math.max(0, Math.min(15, baseLightLevel));
        this.effectiveLightLevel = this.baseLightLevel;
        this.argbColor = argbColor;
        this.lastUpdateTime = System.currentTimeMillis();
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public DynamicLightSourceType getSourceType() {
        return sourceType;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void updatePosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public int getBaseLightLevel() {
        return baseLightLevel;
    }

    public void setBaseLightLevel(int baseLightLevel) {
        this.baseLightLevel = Math.max(0, Math.min(15, baseLightLevel));
        this.effectiveLightLevel = this.baseLightLevel;
    }

    public int getEffectiveLightLevel() {
        return effectiveLightLevel;
    }

    public void setEffectiveLightLevel(int effectiveLightLevel) {
        this.effectiveLightLevel = Math.max(0, Math.min(15, effectiveLightLevel));
    }

    public int getArgbColor() {
        return argbColor;
    }

    public void setArgbColor(int argbColor) {
        this.argbColor = argbColor;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double distanceSquared(double targetX, double targetY, double targetZ) {
        double dx = this.x - targetX;
        double dy = this.y - targetY;
        double dz = this.z - targetZ;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(double targetX, double targetY, double targetZ) {
        return Math.sqrt(distanceSquared(targetX, targetY, targetZ));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicLightSource that = (DynamicLightSource) o;
        return Objects.equals(id, that.id) && sourceType == that.sourceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceType);
    }
}
