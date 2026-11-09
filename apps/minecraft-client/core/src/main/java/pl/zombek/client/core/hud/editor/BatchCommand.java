package pl.zombek.client.core.hud.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite command executing or rolling back multiple atomic commands as one undo step.
 */
public class BatchCommand implements IHudCommand {
    private final List<IHudCommand> commands = new ArrayList<>();
    private final String description;
    private final long timestamp = System.currentTimeMillis();

    public BatchCommand(String description) {
        this.description = description != null ? description : "Operacja zbiorcza";
    }

    public void add(IHudCommand command) {
        if (command != null) {
            commands.add(command);
        }
    }

    @Override
    public void execute(HudLayout layout) {
        redo(layout);
    }

    @Override
    public void undo(HudLayout layout) {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo(layout);
        }
    }

    @Override
    public void redo(HudLayout layout) {
        for (IHudCommand cmd : commands) {
            cmd.redo(layout);
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

    public List<IHudCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
