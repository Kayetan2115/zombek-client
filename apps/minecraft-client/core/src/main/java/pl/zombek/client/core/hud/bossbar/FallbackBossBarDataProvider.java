package pl.zombek.client.core.hud.bossbar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fallback provider used in tests or when no boss is present.
 * Generates sample Ender Dragon and Wither bars when preview mode is toggled.
 */
public class FallbackBossBarDataProvider implements IBossBarDataProvider {

    private boolean previewMode = false;
    private List<BossBarData> customBossBars = null;

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    public void setActiveBossBars(List<BossBarData> bars) {
        this.customBossBars = bars;
    }

    @Override
    public boolean hasActiveBossBar() {
        return (customBossBars != null && !customBossBars.isEmpty()) || previewMode;
    }

    @Override
    public List<BossBarData> getActiveBossBars() {
        if (customBossBars != null) {
            return customBossBars;
        }
        if (!previewMode) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                new BossBarData("preview_ender_dragon", "Ender Dragon", 0.72f, BossBarColor.PINK, BossBarOverlay.PROGRESS, true, true, false),
                new BossBarData("preview_wither", "Wither", 0.45f, BossBarColor.PURPLE, BossBarOverlay.NOTCHED_6, false, false, false)
        );
    }
}
