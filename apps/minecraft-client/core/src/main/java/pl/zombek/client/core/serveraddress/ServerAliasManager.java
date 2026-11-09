package pl.zombek.client.core.serveraddress;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user-defined local server aliases and custom colors.
 * Kept strictly local to the client; never transmitted over the network.
 */
public final class ServerAliasManager {

    public static final class AliasEntry {
        private final String address;
        private final String alias;
        private final int customColor;

        public AliasEntry(String address, String alias, int customColor) {
            this.address = address;
            this.alias = alias;
            this.customColor = customColor;
        }

        public String getAddress() { return address; }
        public String getAlias() { return alias; }
        public int getCustomColor() { return customColor; }
    }

    private static final ServerAliasManager INSTANCE = new ServerAliasManager();

    private final Map<String, AliasEntry> aliases = new ConcurrentHashMap<>();

    private ServerAliasManager() {}

    public static ServerAliasManager getInstance() {
        return INSTANCE;
    }

    public void setAlias(String serverAddress, String alias, int color) {
        if (serverAddress != null && alias != null && !alias.trim().isEmpty()) {
            String key = normalizeAddress(serverAddress);
            aliases.put(key, new AliasEntry(serverAddress, alias.trim(), color));
        }
    }

    public AliasEntry getAlias(String serverAddress) {
        if (serverAddress == null) return null;
        return aliases.get(normalizeAddress(serverAddress));
    }

    public boolean hasAlias(String serverAddress) {
        if (serverAddress == null) return false;
        return aliases.containsKey(normalizeAddress(serverAddress));
    }

    public void removeAlias(String serverAddress) {
        if (serverAddress != null) {
            aliases.remove(normalizeAddress(serverAddress));
        }
    }

    public void clear() {
        aliases.clear();
    }

    private String normalizeAddress(String addr) {
        String clean = addr.trim().toLowerCase(Locale.ROOT);
        if (clean.endsWith(":25565")) {
            clean = clean.substring(0, clean.length() - 6);
        }
        return clean;
    }
}
