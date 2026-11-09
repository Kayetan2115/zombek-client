package pl.zombek.client.core.hypixel;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * User-configurable custom filter rule for chat messages.
 */
public class ChatFilterRule {
    public enum MatchType {
        EXACT,
        CONTAINS,
        REGEX
    }

    private String name;
    private MatchType matchType;
    private String patternString;
    private ChatFilterCategory category;
    private boolean enabled;

    // Compiled regex cache
    private transient Pattern compiledPattern;
    private transient boolean invalidRegex = false;

    public ChatFilterRule(String name, MatchType matchType, String patternString, ChatFilterCategory category, boolean enabled) {
        this.name = name != null ? name : "Reguła";
        this.matchType = matchType != null ? matchType : MatchType.CONTAINS;
        this.patternString = patternString != null ? patternString : "";
        this.category = category != null ? category : ChatFilterCategory.CUSTOM;
        this.enabled = enabled;
        compile();
    }

    private void compile() {
        if (matchType == MatchType.REGEX && patternString != null && !patternString.isEmpty()) {
            // ReDoS guard: limit pattern length to 100 characters and reject catastrophic nested quantifiers
            if (patternString.length() > 100 || hasCatastrophicBacktrackingRisk(patternString)) {
                this.invalidRegex = true;
                this.compiledPattern = null;
                return;
            }

            try {
                this.compiledPattern = Pattern.compile("(?iu)" + patternString);
                this.invalidRegex = false;
            } catch (PatternSyntaxException e) {
                this.invalidRegex = true;
                this.compiledPattern = null;
            }
        }
    }

    private boolean hasCatastrophicBacktrackingRisk(String regex) {
        // Simple heuristic for common ReDoS vulnerability patterns like (a+)+, (.*)*, ([a-z]+)*
        return regex.contains("+)+") || regex.contains("*)*") || regex.contains("+)*") || regex.contains("*)+");
    }

    public boolean matches(String cleanMessage) {
        if (!enabled || cleanMessage == null || patternString.isEmpty() || invalidRegex) {
            return false;
        }

        switch (matchType) {
            case EXACT:
                return cleanMessage.equalsIgnoreCase(patternString);
            case CONTAINS:
                return cleanMessage.toLowerCase(Locale.ROOT).contains(patternString.toLowerCase(Locale.ROOT));
            case REGEX:
                return compiledPattern != null && compiledPattern.matcher(cleanMessage).find();
            default:
                return false;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MatchType getMatchType() { return matchType; }
    public void setMatchType(MatchType matchType) { this.matchType = matchType; compile(); }
    public String getPatternString() { return patternString; }
    public void setPatternString(String patternString) { this.patternString = patternString; compile(); }
    public ChatFilterCategory getCategory() { return category; }
    public void setCategory(ChatFilterCategory category) { this.category = category; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isInvalidRegex() { return invalidRegex; }
}
