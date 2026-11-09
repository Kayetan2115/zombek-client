package pl.zombek.client.core.tracking;

import pl.zombek.client.core.data.TargetEntityType;
import pl.zombek.client.core.modules.visual.FadeCurve;
import pl.zombek.client.core.modules.visual.HitColorModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks, filters, and fades damage hit color effects across entities.
 * Decoupled from Minecraft internals for testability and multiloader safety.
 */
public class HitColorTracker {
    private static final HitColorTracker INSTANCE = new HitColorTracker();

    private final Map<Integer, HitColorRecord> activeEffects = new ConcurrentHashMap<>();

    private HitColorTracker() {
    }

    public static HitColorTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Records a damage hit event on an entity.
     */
    public void recordHit(int entityId,
                          TargetEntityType type,
                          boolean isLocalPlayerDamage,
                          boolean isTargeted,
                          long hitTimeMs,
                          long durationMs,
                          long fadeDurationMs,
                          int colorArgb,
                          FadeCurve curve,
                          float intensity,
                          float opacity) {
        HitColorRecord record = new HitColorRecord(
                entityId,
                hitTimeMs,
                durationMs,
                fadeDurationMs,
                colorArgb,
                curve,
                intensity,
                opacity,
                type,
                isLocalPlayerDamage,
                isTargeted
        );
        activeEffects.put(entityId, record);
    }

    /**
     * Convenience method using current module settings.
     */
    public void recordHit(int entityId, TargetEntityType type, boolean isLocalPlayerDamage, boolean isTargeted) {
        HitColorModule mod = HitColorModule.getInstance();
        if (mod == null || !mod.isEnabled()) {
            return;
        }

        // Check max active effects to prevent memory accumulation in crowded areas
        if (activeEffects.size() >= mod.getMaxActiveEffectsSetting().getValue().intValue()) {
            cleanupExpired(System.currentTimeMillis());
            if (activeEffects.size() >= mod.getMaxActiveEffectsSetting().getValue().intValue()) {
                return;
            }
        }

        recordHit(
                entityId,
                type,
                isLocalPlayerDamage,
                isTargeted,
                System.currentTimeMillis(),
                mod.getDurationSetting().getValue().longValue(),
                mod.getFadeDurationSetting().getValue().longValue(),
                mod.getHitColorSetting().getValue(),
                mod.getFadeCurveSetting().getValue(),
                mod.getIntensitySetting().getValue().floatValue() / 100.0f,
                mod.getOpacitySetting().getValue().floatValue() / 100.0f
        );
    }

    /**
     * Retrieves the active calculated color for rendering, or null if no effect or filtered out.
     */
    public Integer getActiveColor(int entityId, long now) {
        HitColorRecord record = activeEffects.get(entityId);
        if (record == null) {
            return null;
        }

        if (record.isExpired(now)) {
            activeEffects.remove(entityId);
            return null;
        }

        HitColorModule mod = HitColorModule.getInstance();
        if (mod != null) {
            if (!mod.isEnabled()) {
                return null;
            }

            // 1. Filter: Targeted entity only
            if (mod.getOnlyTargetedEntitySetting().getValue() && !record.isTargeted()) {
                return null;
            }

            // 2. Filter: Local player damage only
            if (mod.getOnlyLocalPlayerDamageSetting().getValue() && !record.isLocalPlayerDamage()) {
                return null;
            }

            // 3. Filter: Entity category toggles
            TargetEntityType type = record.getEntityType();
            if (type == TargetEntityType.PLAYER && !mod.getPlayersSetting().getValue()) return null;
            if (type == TargetEntityType.HOSTILE && !mod.getHostileMobsSetting().getValue()) return null;
            if (type == TargetEntityType.PASSIVE && !mod.getPassiveMobsSetting().getValue()) return null;
            if (type == TargetEntityType.NEUTRAL && !mod.getOtherEntitiesSetting().getValue()) return null;
            if (type == TargetEntityType.OTHER && !mod.getOtherEntitiesSetting().getValue()) return null;
        }

        return record.computeColor(now);
    }

    public HitColorRecord getRecord(int entityId) {
        return activeEffects.get(entityId);
    }

    public int getActiveEffectCount() {
        return activeEffects.size();
    }

    public void cleanupExpired(long now) {
        activeEffects.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    public void clear() {
        activeEffects.clear();
    }

    public void resetForTesting() {
        activeEffects.clear();
    }
}
