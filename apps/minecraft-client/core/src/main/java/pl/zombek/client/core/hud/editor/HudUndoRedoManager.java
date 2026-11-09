package pl.zombek.client.core.hud.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages reversible action history (Undo / Redo) for the HUD Editor.
 */
public class HudUndoRedoManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudUndoRedoManager.class);
    private static final HudUndoRedoManager INSTANCE = new HudUndoRedoManager();

    private static final int MAX_HISTORY_LIMIT = 50;

    private final Deque<IHudCommand> undoStack = new ArrayDeque<>();
    private final Deque<IHudCommand> redoStack = new ArrayDeque<>();

    private HudUndoRedoManager() {
    }

    public static HudUndoRedoManager getInstance() {
        return INSTANCE;
    }

    public synchronized void push(IHudCommand command) {
        if (command == null) return;
        redoStack.clear();
        undoStack.push(command);
        while (undoStack.size() > MAX_HISTORY_LIMIT) {
            undoStack.removeLast();
        }
        LOGGER.debug("Pushed undo command: '{}', stack size: {}", command.getDescription(), undoStack.size());
    }

    public synchronized void executeCommand(IHudCommand command, HudLayout layout) {
        if (command == null) return;
        if (layout != null) {
            command.execute(layout);
        }
        push(command);
    }

    public synchronized void executeCommand(IHudCommand command) {
        executeCommand(command, HudLayoutManager.getInstance().getStagedLayout());
    }

    public synchronized void recordExecutedCommand(IHudCommand command) {
        push(command);
    }

    public synchronized boolean undo(HudLayout layout) {
        if (!canUndo() || layout == null) return false;
        IHudCommand cmd = undoStack.pop();
        cmd.undo(layout);
        redoStack.push(cmd);
        LOGGER.info("Undid command: '{}'", cmd.getDescription());
        return true;
    }

    public synchronized boolean undo() {
        return undo(HudLayoutManager.getInstance().getStagedLayout());
    }

    public synchronized boolean redo(HudLayout layout) {
        if (!canRedo() || layout == null) return false;
        IHudCommand cmd = redoStack.pop();
        cmd.redo(layout);
        undoStack.push(cmd);
        LOGGER.info("Redid command: '{}'", cmd.getDescription());
        return true;
    }

    public synchronized boolean redo() {
        return redo(HudLayoutManager.getInstance().getStagedLayout());
    }

    public synchronized boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public synchronized String getLastActionDescription() {
        if (undoStack.isEmpty()) return null;
        return undoStack.peek().getDescription();
    }

    public synchronized List<IHudCommand> getUndoHistory() {
        return new ArrayList<>(undoStack);
    }

    public synchronized List<String> getHistory() {
        List<String> list = new ArrayList<>();
        for (IHudCommand cmd : undoStack) {
            list.add(cmd.getDescription());
        }
        return list;
    }

    public synchronized void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public synchronized void resetForTesting() {
        clear();
    }
}
