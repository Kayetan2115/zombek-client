package pl.zombek.client.core.hud.editor;

/**
 * Command contract for reversible actions in the HUD Editor (Undo/Redo).
 */
public interface IHudCommand {
    /**
     * Executes the action on the target layout.
     */
    void execute(HudLayout layout);

    /**
     * Undoes the action on the target layout, restoring the previous state.
     */
    void undo(HudLayout layout);

    /**
     * Redoes the action on the target layout.
     */
    void redo(HudLayout layout);

    /**
     * Human-readable description of the operation (e.g. "Przesunięto 2 komponenty").
     */
    String getDescription();

    /**
     * Millisecond timestamp of creation.
     */
    long getTimestamp();
}
