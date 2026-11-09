package pl.zombek.client.core.hud.editor;

/**
 * Command recording a full preset application with complete rollback capability.
 */
public class ApplyPresetCommand implements IHudCommand {
    private final String presetId;
    private final HudLayout layoutBefore;
    private final HudLayout layoutAfter;
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public ApplyPresetCommand(String presetId, HudLayout before, HudLayout after) {
        this.presetId = presetId;
        this.layoutBefore = before != null ? before.cloneLayout() : null;
        this.layoutAfter = after != null ? after.cloneLayout() : null;
        this.description = "Zastosowano preset: " + presetId;
    }

    public ApplyPresetCommand(HudPreset preset, HudLayout stagedLayout) {
        this(preset != null ? preset.getId() : "preset",
                stagedLayout != null ? stagedLayout.cloneLayout() : null,
                createAfterLayout(preset, stagedLayout));
    }

    private static HudLayout createAfterLayout(HudPreset preset, HudLayout stagedLayout) {
        if (stagedLayout == null || preset == null) return stagedLayout;
        HudLayout after = stagedLayout.cloneLayout();
        HudPresetManager.getInstance().applyPreset(after, preset.getId());
        return after;
    }

    @Override
    public void execute(HudLayout layout) {
        redo(layout);
    }

    @Override
    public void undo(HudLayout layout) {
        if (layout == null || layoutBefore == null) return;
        restore(layout, layoutBefore);
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null || layoutAfter == null) return;
        restore(layout, layoutAfter);
    }

    private void restore(HudLayout target, HudLayout source) {
        target.getComponents().clear();
        for (HudComponentState state : source.getComponents().values()) {
            target.setComponentState(new HudComponentState(state));
        }
        target.getGroups().clear();
        for (HudGroup group : source.getGroups().values()) {
            target.addGroup(new HudGroup(group));
        }
        target.getLayers().clear();
        for (HudLayer layer : source.getLayers().values()) {
            target.addLayer(new HudLayer(layer));
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public String getPresetId() {
        return presetId;
    }

    public HudLayout getLayoutBefore() {
        return layoutBefore;
    }

    public HudLayout getLayoutAfter() {
        return layoutAfter;
    }
}
