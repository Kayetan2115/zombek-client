package pl.zombek.client.core.playerinfo;

/**
 * Immutable DTO representing a player's or entity's equipped piece of armor or held item.
 */
public final class PlayerArmorData {

    public enum ArmorSlot {
        HELMET("Helmet"),
        CHESTPLATE("Chestplate"),
        LEGGINGS("Leggings"),
        BOOTS("Boots"),
        OFFHAND_SHIELD("Shield"),
        MAINHAND("Main Hand");

        private final String label;
        ArmorSlot(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final ArmorSlot slot;
    private final String itemId;
    private final String displayName;
    private final int durability;
    private final int maxDurability;
    private final boolean hasDurability;
    private final Object itemHandle;

    public PlayerArmorData(ArmorSlot slot, String itemId, String displayName, int durability, int maxDurability, boolean hasDurability, Object itemHandle) {
        this.slot = slot;
        this.itemId = itemId != null ? itemId : "minecraft:air";
        this.displayName = displayName != null ? displayName : "";
        this.durability = durability;
        this.maxDurability = maxDurability > 0 ? maxDurability : 1;
        this.hasDurability = hasDurability;
        this.itemHandle = itemHandle;
    }

    public ArmorSlot getSlot() { return slot; }
    public String getItemId() { return itemId; }
    public String getDisplayName() { return displayName; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public boolean hasDurability() { return hasDurability; }
    public Object getItemHandle() { return itemHandle; }

    public double getDurabilityPercent() {
        if (!hasDurability || maxDurability <= 0) return 1.0;
        return (double) durability / maxDurability;
    }

    public boolean isLowDurability(double thresholdPercent) {
        return hasDurability && getDurabilityPercent() <= thresholdPercent;
    }
}
