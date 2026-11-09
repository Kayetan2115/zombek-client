package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelAutocompleteTest {
    private HypixelAutocomplete autocomplete;

    @BeforeEach
    void setUp() {
        autocomplete = new HypixelAutocomplete();
        autocomplete.setEnabled(true);
        autocomplete.setSuggestionCount(5);
        autocomplete.setCaseSensitive(false);
        autocomplete.setMatchPrefix(true);
    }

    @Test
    void testPrefixMatchingCommand() {
        List<AutocompleteSuggestion> suggestions = autocomplete.getSuggestions("/lo");
        assertFalse(suggestions.isEmpty());
        assertEquals("/lobby", suggestions.get(0).getCommand());
        assertTrue(suggestions.get(0).getDescription().contains("lobby"));
    }

    @Test
    void testMultipleMatchingSuggestions() {
        List<AutocompleteSuggestion> suggestions = autocomplete.getSuggestions("/p");
        assertTrue(suggestions.size() >= 2);

        boolean foundParty = suggestions.stream().anyMatch(s -> s.getCommand().equals("/party"));
        boolean foundPlay = suggestions.stream().anyMatch(s -> s.getCommand().equals("/play"));
        assertTrue(foundParty);
        assertTrue(foundPlay);
    }

    @Test
    void testAcceptSuggestion() {
        String completed = autocomplete.acceptSuggestion("/lo");
        assertEquals("/lobby ", completed);
    }

    @Test
    void testNavigationNextAndPrevious() {
        List<AutocompleteSuggestion> suggestions = autocomplete.getSuggestions("/p");
        int count = suggestions.size();

        assertEquals(0, autocomplete.getSelectedIndex());
        autocomplete.nextSuggestion(count);
        assertEquals(1, autocomplete.getSelectedIndex());
        autocomplete.previousSuggestion(count);
        assertEquals(0, autocomplete.getSelectedIndex());
    }

    @Test
    void testNonCommandIgnored() {
        List<AutocompleteSuggestion> suggestions = autocomplete.getSuggestions("hello world");
        assertTrue(suggestions.isEmpty());

        suggestions = autocomplete.getSuggestions(null);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testDisabledAutocomplete() {
        autocomplete.setEnabled(false);
        List<AutocompleteSuggestion> suggestions = autocomplete.getSuggestions("/lobby");
        assertTrue(suggestions.isEmpty());
    }
}
