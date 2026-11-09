package pl.zombek.client.core.data;

/**
 * Immutable geometric and state snapshot of an entity for hitbox world rendering.
 */
public class HitboxEntityData {
    private final int entityId;
    private final String name;
    private final TargetEntityType type;
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;
    private final double eyeY;
    private final double lookX;
    private final double lookY;
    private final double lookZ;
    private final double distanceToCamera;
    private final boolean isTargeted;
    private final boolean isInvisible;
    private final boolean isDead;
    private final boolean isSpectator;

    public HitboxEntityData(int entityId,
                            String name,
                            TargetEntityType type,
                            double minX, double minY, double minZ,
                            double maxX, double maxY, double maxZ,
                            double eyeY,
                            double lookX, double lookY, double lookZ,
                            double distanceToCamera,
                            boolean isTargeted,
                            boolean isInvisible,
                            boolean isDead,
                            boolean isSpectator) {
        this.entityId = entityId;
        this.name = name != null ? name : "Unknown";
        this.type = type != null ? type : TargetEntityType.OTHER;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.eyeY = eyeY;
        this.lookX = lookX;
        this.lookY = lookY;
        this.lookZ = lookZ;
        this.distanceToCamera = distanceToCamera;
        this.isTargeted = isTargeted;
        this.isInvisible = isInvisible;
        this.isDead = isDead;
        this.isSpectator = isSpectator;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public TargetEntityType getType() {
        return type;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getEyeY() {
        return eyeY;
    }

    public double getLookX() {
        return lookX;
    }

    public double getLookY() {
        return lookY;
    }

    public double getLookZ() {
        return lookZ;
    }

    public double getDistanceToCamera() {
        return distanceToCamera;
    }

    public boolean isTargeted() {
        return isTargeted;
    }

    public boolean isInvisible() {
        return isInvisible;
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isSpectator() {
        return isSpectator;
    }

    public double getCenterX() {
        return (minX + maxX) / 2.0;
    }

    public double getCenterZ() {
        return (minZ + maxZ) / 2.0;
    }
}
