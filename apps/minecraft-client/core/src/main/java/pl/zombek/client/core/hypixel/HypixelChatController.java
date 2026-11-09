package pl.zombek.client.core.hypixel;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modifies local display formatting of Hypixel chat messages and tablist entries.
 * Strictly operates on locally rendered text components without altering packets,
 * server chat logs, or client-to-server outgoing messages.
 */
public class HypixelChatController {
    private static HypixelChatController instance;

    // Default Hypixel channel prefix pattern with optional color formatting
    private static final Pattern HYPIXEL_CHANNEL_PATTERN = Pattern.compile(
            "(?iu)^((?:§[0-9a-fk-orA-FK-OR])*)\\s*(Party|Guild|Officer|Team)\\s*>\\s*"
    );

    // Guild tag on tablist pattern: e.g. " §6[TAG]" or " [MVP+] Steve [GUILD]"
    private static final Pattern GUILD_TAB_TAG_PATTERN = Pattern.compile(
            "(?iu)\\s*(?:§[0-9a-fk-orA-FK-OR])*\\[([A-Za-z0-9_]{1,6})\\]\\s*$"
    );

    // Guild MOTD indicator patterns
    private static final Pattern GUILD_MOTD_PATTERN = Pattern.compile(
            "(?iu)(?:guild\\s+message\\s+of\\s+the\\s+day|guild\\s+motd|^-{20,})"
    );

    // Configuration
    private boolean enableShortChannelNames = true;
    private ChannelPrefixStyle prefixStyle = ChannelPrefixStyle.BRACKETED;
    private boolean preserveOriginalFormatting = true;
    private String customChannelAliases = "Guild=G;Party=P;Team=T;Officer=O";
    private boolean hideGuildMotd = false;
    private boolean hideGuildOnTab = false;
    private boolean showTimestamps = false;
    private String timestampFormat = "HH:mm";

    // Parsed alias lookup table
    private final Map<String, String> aliasMap = new HashMap<>();

    public HypixelChatController() {
        refreshAliasMap();
    }

    public static synchronized HypixelChatController getInstance() {
        if (instance == null) {
            instance = new HypixelChatController();
        }
        return instance;
    }

    public synchronized void refreshAliasMap() {
        aliasMap.clear();
        aliasMap.put("guild", "G");
        aliasMap.put("party", "P");
        aliasMap.put("team", "T");
        aliasMap.put("officer", "O");

        if (customChannelAliases != null && !customChannelAliases.trim().isEmpty()) {
            String[] pairs = customChannelAliases.split(";");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    aliasMap.put(kv[0].trim().toLowerCase(Locale.ROOT), kv[1].trim());
                }
            }
        }
    }

    /**
     * Formats incoming chat message for local display: applies short channel names
     * and optional timestamp prefix while preserving color codes.
     */
    public synchronized String formatChatMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            return rawMessage;
        }

        String result = rawMessage;

        // 1. Short Channel Names
        if (enableShortChannelNames) {
            Matcher m = HYPIXEL_CHANNEL_PATTERN.matcher(result);
            if (m.find()) {
                String colorPrefix = m.group(1);
                String channelName = m.group(2).toLowerCase(Locale.ROOT);
                String alias = aliasMap.getOrDefault(channelName, channelName.substring(0, 1).toUpperCase(Locale.ROOT));
                String formattedPrefix = prefixStyle.format(alias);

                if (preserveOriginalFormatting && colorPrefix != null && !colorPrefix.isEmpty()) {
                    result = colorPrefix + formattedPrefix + result.substring(m.end());
                } else {
                    result = formattedPrefix + result.substring(m.end());
                }
            }
        }

        // 2. Prepend Timestamp if enabled
        if (showTimestamps) {
            String timeStr = formatCurrentTimestamp();
            result = "§7[" + timeStr + "] §r" + result;
        }

        return result;
    }

    /**
     * Checks if a message line belongs to a Guild MOTD announcement.
     */
    public boolean isGuildMotd(String rawMessage) {
        if (!hideGuildMotd || rawMessage == null) {
            return false;
        }
        String stripped = stripFormatting(rawMessage).trim();
        return GUILD_MOTD_PATTERN.matcher(stripped).find();
    }

    /**
     * Cleans guild tag suffix from a tablist player entry string if hideGuildOnTab is enabled.
     */
    public String cleanTablistName(String rawName) {
        if (!hideGuildOnTab || rawName == null || rawName.isEmpty()) {
            return rawName;
        }
        Matcher m = GUILD_TAB_TAG_PATTERN.matcher(rawName);
        if (m.find()) {
            return rawName.substring(0, m.start()).trim();
        }
        return rawName;
    }

    private String formatCurrentTimestamp() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
            return sdf.format(new Date());
        } catch (Exception e) {
            SimpleDateFormat fallback = new SimpleDateFormat("HH:mm");
            return fallback.format(new Date());
        }
    }

    public static String stripFormatting(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    // Getters and Setters
    public boolean isEnableShortChannelNames() { return enableShortChannelNames; }
    public void setEnableShortChannelNames(boolean enable) { this.enableShortChannelNames = enable; }
    public ChannelPrefixStyle getPrefixStyle() { return prefixStyle; }
    public void setPrefixStyle(ChannelPrefixStyle style) { this.prefixStyle = style != null ? style : ChannelPrefixStyle.BRACKETED; }
    public boolean isPreserveOriginalFormatting() { return preserveOriginalFormatting; }
    public void setPreserveOriginalFormatting(boolean preserve) { this.preserveOriginalFormatting = preserve; }
    public String getCustomChannelAliases() { return customChannelAliases; }
    public void setCustomChannelAliases(String aliases) { this.customChannelAliases = aliases; refreshAliasMap(); }
    public boolean isHideGuildMotd() { return hideGuildMotd; }
    public void setHideGuildMotd(boolean hide) { this.hideGuildMotd = hide; }
    public boolean isHideGuildOnTab() { return hideGuildOnTab; }
    public void setHideGuildOnTab(boolean hide) { this.hideGuildOnTab = hide; }
    public boolean isShowTimestamps() { return showTimestamps; }
    public void setShowTimestamps(boolean show) { this.showTimestamps = show; }
    public String getTimestampFormat() { return timestampFormat; }
    public void setTimestampFormat(String format) { this.timestampFormat = format; }
}
