package pl.zombek.client.core.hud.editor;

/**
 * Command recording creation of a HUD component group.
 */
public class CreateGroupCommand implements IHudCommand {
    private final HudGroup createdGroup;
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public CreateGroupCommand(HudGroup group) {
        this.createdGroup = group != null ? new HudGroup(group) : null;
        this.description = "Utworzono grupę: " + (group != null ? group.getName() : "");
    }

    public CreateGroupCommand(HudGroup group, HudLayout layout) {
        this(group);
    }

    @Override
    public void execute(HudLayout layout) {
        redo(layout);
    }

    @Override
    public void undo(HudLayout layout) {
        if (layout == null || createdGroup == null) return;
        HudGroupManager.getInstance().deleteGroup(layout, createdGroup.getGroupId());
    }

    @Override
    public void redo(HudLayout layout) {
        if (layout == null || createdGroup == null) return;
        layout.addGroup(new HudGroup(createdGroup));
        for (String compId : createdGroup.getComponentIds()) {
            HudComponentState state = layout.getComponentState(compId);
            if (state != null) {
                state.setGroupId(createdGroup.getGroupId());
            }
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

    public HudGroup getCreatedGroup() {
        return createdGroup;
    }
}
