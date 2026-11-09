package pl.zombek.client.core.hud.bossbar;

/**
 * Immutable data container representing an active boss bar in Minecraft.
 */
public class BossBarData {
    private final String id;
    private final String name;
    private float progress;
    private float animatedProgress;
    private final BossBarColor color;
    private final BossBarOverlay overlay;
    private final boolean darkenSky;
    private final boolean playMusic;
    private final boolean createWorldFog;

    public BossBarData(String id, String name, float progress, BossBarColor color, BossBarOverlay overlay, boolean darkenSky, boolean playMusic, boolean createWorldFog) {
        this.id = id != null ? id : "boss";
        this.name = name != null ? name : "Boss";
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
        this.animatedProgress = this.progress;
        this.color = color != null ? color : BossBarColor.PURPLE;
        this.overlay = overlay != null ? overlay : BossBarOverlay.PROGRESS;
        this.darkenSky = darkenSky;
        this.playMusic = playMusic;
        this.createWorldFog = createWorldFog;
    }

    public BossBarData(String id, String name, float progress, BossBarColor color, BossBarOverlay overlay) {
        this(id, name, progress, color, overlay, false, false, false);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
    }

    public float getPercent() {
        return getProgress();
    }

    public void setPercent(float percent) {
        setProgress(percent);
    }

    public float getAnimatedProgress() {
        return animatedProgress;
    }

    public void setAnimatedProgress(float anim) {
        this.animatedProgress = anim;
    }

    public float getAnimatedPercent() {
        return getAnimatedProgress();
    }

    public void updateAnimation(float speed) {
        this.animatedProgress += (this.progress - this.animatedProgress) * speed;
    }

    public int getPercentage() {
        return Math.round(progress * 100.0f);
    }

    public BossBarColor getColor() {
        return color;
    }

    public BossBarOverlay getOverlay() {
        return overlay;
    }

    public boolean isDarkenSky() {
        return darkenSky;
    }

    public boolean isPlayMusic() {
        return playMusic;
    }

    public boolean isCreateWorldFog() {
        return createWorldFog;
    }

    @Override
    public String toString() {
        return "BossBarData{" + id + ", name='" + name + "', progress=" + getPercentage() + "%, color=" + color + "}";
    }
}
