package pl.zombek.client.core.hypixel;

/**
 * Visual styling formats for shortened chat channel prefixes.
 */
public enum ChannelPrefixStyle {
    BRACKETED("[G]", "[%s] "),
    COLON("G:", "%s: "),
    MINIMAL("G >", "%s > "),
    PLAIN("G", "%s ");

    private final String preview;
    private final String pattern;

    ChannelPrefixStyle(String preview, String pattern) {
        this.preview = preview;
        this.pattern = pattern;
    }

    public String getPreview() {
        return preview;
    }

    public String format(String channelCode) {
        return String.format(pattern, channelCode);
    }
}
