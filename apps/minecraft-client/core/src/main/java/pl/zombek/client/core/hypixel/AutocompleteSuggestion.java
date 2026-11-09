package pl.zombek.client.core.hypixel;

/**
 * Single local command completion suggestion.
 */
public class AutocompleteSuggestion {
    private final String command;
    private final String syntax;
    private final String description;

    public AutocompleteSuggestion(String command, String syntax, String description) {
        this.command = command;
        this.syntax = syntax;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getDescription() {
        return description;
    }
}
