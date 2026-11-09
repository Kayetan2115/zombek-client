package pl.zombek.client.core.hypixel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Local command autocomplete provider for Hypixel Network.
 * Suggests known Hypixel commands purely from a local dictionary without sending
 * network packets or querying the server.
 */
public class HypixelAutocomplete {
    private static HypixelAutocomplete instance;

    private boolean enabled = true;
    private int suggestionCount = 5;
    private boolean matchPrefix = true;
    private boolean caseSensitive = false;
    private boolean showDescription = true;
    private int selectedIndex = 0;

    // Local command database
    private final List<AutocompleteSuggestion> commandDictionary = new ArrayList<>();

    public HypixelAutocomplete() {
        populateDefaultCommands();
    }

    public static synchronized HypixelAutocomplete getInstance() {
        if (instance == null) {
            instance = new HypixelAutocomplete();
        }
        return instance;
    }

    private void populateDefaultCommands() {
        commandDictionary.add(new AutocompleteSuggestion("/lobby", "/lobby [tryb]", "Powrót do lobby głównego lub trybu"));
        commandDictionary.add(new AutocompleteSuggestion("/hub", "/hub", "Powrót do lobby głównego"));
        commandDictionary.add(new AutocompleteSuggestion("/play", "/play <tryb>", "Dołączenie do kolejki minigry (np. /play bedwars_four_four)"));
        commandDictionary.add(new AutocompleteSuggestion("/party", "/party <invite|leave|disband|warp>", "Zarządzanie grupą Party"));
        commandDictionary.add(new AutocompleteSuggestion("/p", "/p <invite|leave|chat>", "Skrót do komend grupy Party"));
        commandDictionary.add(new AutocompleteSuggestion("/guild", "/guild <info|join|leave|chat>", "Zarządzanie gildią"));
        commandDictionary.add(new AutocompleteSuggestion("/g", "/g <chat|info>", "Skrót do komend gildii"));
        commandDictionary.add(new AutocompleteSuggestion("/msg", "/msg <gracz> <treść>", "Wysłanie prywatnej wiadomości"));
        commandDictionary.add(new AutocompleteSuggestion("/tell", "/tell <gracz> <treść>", "Wysłanie prywatnej wiadomości"));
        commandDictionary.add(new AutocompleteSuggestion("/r", "/r <treść>", "Szybka odpowiedź na ostatnią prywatną wiadomość"));
        commandDictionary.add(new AutocompleteSuggestion("/friend", "/friend <add|remove|list>", "Zarządzanie listą znajomych"));
        commandDictionary.add(new AutocompleteSuggestion("/f", "/f <add|list>", "Skrót do listy znajomych"));
        commandDictionary.add(new AutocompleteSuggestion("/shout", "/shout <treść>", "Wysłanie wiadomości do wszystkich graczy w meczu"));
        commandDictionary.add(new AutocompleteSuggestion("/who", "/who", "Wyświetlenie listy graczy w bieżącej grze"));
        commandDictionary.add(new AutocompleteSuggestion("/whereami", "/whereami", "Identyfikator serwera i krainy"));
        commandDictionary.add(new AutocompleteSuggestion("/settings", "/settings", "Menu ustawień gracza"));
        commandDictionary.add(new AutocompleteSuggestion("/tip", "/tip <all|gracz>", "Przekazanie napiwku za aktywny booster"));
        commandDictionary.add(new AutocompleteSuggestion("/booster", "/booster", "Przegląd aktywnych boosterów sieciowych"));
        commandDictionary.add(new AutocompleteSuggestion("/mystery", "/mystery", "Otwieranie Mystery Box w lobby"));
        commandDictionary.add(new AutocompleteSuggestion("/housing", "/housing", "Przejście do modułu Housing"));
        commandDictionary.add(new AutocompleteSuggestion("/rejoin", "/rejoin", "Ponowne dołączenie do trwającego meczu"));
        commandDictionary.add(new AutocompleteSuggestion("/swaplobby", "/swaplobby <numer>", "Przełączenie na inne lobby"));
    }

    /**
     * Resolves matching command suggestions for the currently typed chat prompt.
     */
    public synchronized List<AutocompleteSuggestion> getSuggestions(String input) {
        if (!enabled || input == null || input.trim().isEmpty() || !input.startsWith("/")) {
            return Collections.emptyList();
        }

        String search = caseSensitive ? input.trim() : input.trim().toLowerCase(Locale.ROOT);

        List<AutocompleteSuggestion> matches = commandDictionary.stream()
                .filter(cmd -> {
                    String candidate = caseSensitive ? cmd.getCommand() : cmd.getCommand().toLowerCase(Locale.ROOT);
                    return matchPrefix ? candidate.startsWith(search) : candidate.contains(search);
                })
                .limit(suggestionCount)
                .collect(Collectors.toList());

        if (selectedIndex >= matches.size()) {
            selectedIndex = Math.max(0, matches.size() - 1);
        }

        return matches;
    }

    public synchronized void nextSuggestion(int matchCount) {
        if (matchCount > 0) {
            selectedIndex = (selectedIndex + 1) % matchCount;
        }
    }

    public synchronized void previousSuggestion(int matchCount) {
        if (matchCount > 0) {
            selectedIndex = (selectedIndex - 1 + matchCount) % matchCount;
        }
    }

    /**
     * Returns the completed command string for the selected suggestion.
     */
    public synchronized String acceptSuggestion(String currentInput) {
        List<AutocompleteSuggestion> suggestions = getSuggestions(currentInput);
        if (suggestions.isEmpty() || selectedIndex < 0 || selectedIndex >= suggestions.size()) {
            return currentInput;
        }
        return suggestions.get(selectedIndex).getCommand() + " ";
    }

    public synchronized void addCommand(AutocompleteSuggestion cmd) {
        if (cmd != null) {
            commandDictionary.add(cmd);
        }
    }

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getSuggestionCount() { return suggestionCount; }
    public void setSuggestionCount(int count) { this.suggestionCount = Math.max(1, Math.min(10, count)); }
    public boolean isMatchPrefix() { return matchPrefix; }
    public void setMatchPrefix(boolean matchPrefix) { this.matchPrefix = matchPrefix; }
    public boolean isCaseSensitive() { return caseSensitive; }
    public void setCaseSensitive(boolean caseSensitive) { this.caseSensitive = caseSensitive; }
    public boolean isShowDescription() { return showDescription; }
    public void setShowDescription(boolean showDescription) { this.showDescription = showDescription; }
    public int getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(int selectedIndex) { this.selectedIndex = Math.max(0, selectedIndex); }
    public List<AutocompleteSuggestion> getCommandDictionary() { return Collections.unmodifiableList(commandDictionary); }
}
