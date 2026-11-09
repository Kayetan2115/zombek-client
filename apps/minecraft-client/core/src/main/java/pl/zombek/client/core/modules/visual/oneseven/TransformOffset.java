package pl.zombek.client.core.modules.visual.oneseven;

import java.util.Objects;

/**
 * 3D transform container holding translation, rotation, and scaling components.
 */
public class TransformOffset {
    private float posX;
    private float posY;
    private float posZ;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float scaleX;
    private float scaleY;
    private float scaleZ;

    public TransformOffset() {
        this(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public TransformOffset(float posX, float posY, float posZ,
                           float rotX, float rotY, float rotZ,
                           float scaleX, float scaleY, float scaleZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public TransformOffset copy() {
        return new TransformOffset(posX, posY, posZ, rotX, rotY, rotZ, scaleX, scaleY, scaleZ);
    }

    public void set(TransformOffset other) {
        if (other != null) {
            this.posX = other.posX;
            this.posY = other.posY;
            this.posZ = other.posZ;
            this.rotX = other.rotX;
            this.rotY = other.rotY;
            this.rotZ = other.rotZ;
            this.scaleX = other.scaleX;
            this.scaleY = other.scaleY;
            this.scaleZ = other.scaleZ;
        }
    }

    public void reset() {
        this.posX = 0.0f;
        this.posY = 0.0f;
        this.posZ = 0.0f;
        this.rotX = 0.0f;
        this.rotY = 0.0f;
        this.rotZ = 0.0f;
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.scaleZ = 1.0f;
    }

    public boolean isIdentity() {
        return posX == 0.0f && posY == 0.0f && posZ == 0.0f
                && rotX == 0.0f && rotY == 0.0f && rotZ == 0.0f
                && scaleX == 1.0f && scaleY == 1.0f && scaleZ == 1.0f;
    }

    // Getters and Setters
    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }

    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }

    public float getPosZ() { return posZ; }
    public void setPosZ(float posZ) { this.posZ = posZ; }

    public float getRotX() { return rotX; }
    public void setRotX(float rotX) { this.rotX = rotX; }

    public float getRotY() { return rotY; }
    public void setRotY(float rotY) { this.rotY = rotY; }

    public float getRotZ() { return rotZ; }
    public void setRotZ(float rotZ) { this.rotZ = rotZ; }

    public float getScaleX() { return scaleX; }
    public void setScaleX(float scaleX) { this.scaleX = scaleX; }

    public float getScaleY() { return scaleY; }
    public void setScaleY(float scaleY) { this.scaleY = scaleY; }

    public float getScaleZ() { return scaleZ; }
    public void setScaleZ(float scaleZ) { this.scaleZ = scaleZ; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransformOffset that)) return false;
        return Float.compare(that.posX, posX) == 0 &&
                Float.compare(that.posY, posY) == 0 &&
                Float.compare(that.posZ, posZ) == 0 &&
                Float.compare(that.rotX, rotX) == 0 &&
                Float.compare(that.rotY, rotY) == 0 &&
                Float.compare(that.rotZ, rotZ) == 0 &&
                Float.compare(that.scaleX, scaleX) == 0 &&
                Float.compare(that.scaleY, scaleY) == 0 &&
                Float.compare(that.scaleZ, scaleZ) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posY, posZ, rotX, rotY, rotZ, scaleX, scaleY, scaleZ);
    }
}
