package pl.zombek.client.core.hud.editor;

/**
 * Command recording deletion (ungrouping) of a group.
 */
public class DeleteGroupCommand implements IHudCommand {
    private final HudGroup deletedGroup;
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public DeleteGroupCommand(HudGroup group) {
        this.deletedGroup = group != null ? new HudGroup(group) : null;
        this.description = "Usunięto grupę: " + (group != null ? group.getName() : "");
    }

    public DeleteGroupCommand(HudGroup group, HudLayout layout) {
        this(group);
    }

    @Override
    public void execute(HudLayout layout) {
        redo(layout);
    }

    @Override
    public void undo(HudLayout layout) {
        if (layout == null || deletedGroup == null) return;
        layout.addGroup(new HudGroup(deletedGroup));
        for (String compId : deletedGroup.getComponentIds()) {
            HudComponentState state = layout.getComponentState(compId);
            if (state != null) {
                state.setGroupId(deletedGroup.getGroupId());
            }
        }
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null || deletedGroup == null) return;
        HudGroupManager.getInstance().deleteGroup(layout, deletedGroup.getGroupId());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public HudGroup getDeletedGroup() {
        return deletedGroup;
    }
}
