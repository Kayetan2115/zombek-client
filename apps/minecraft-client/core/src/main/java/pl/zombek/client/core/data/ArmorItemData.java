package pl.zombek.client.core.data;

import java.util.Collections;
import java.util.List;

/**
 * Snapshot of an equipped armor item's state, durability, and metadata.
 */
public class ArmorItemData {

    private final ArmorSlot slot;
    private final String itemId;
    private final String displayName;
    private final int damage;
    private final int maxDamage;
    private final boolean hasDurability;
    private final boolean isEmpty;
    private final List<String> enchantments;
    private final Object itemHandle;

    public ArmorItemData(
            ArmorSlot slot,
            String itemId,
            String displayName,
            int damage,
            int maxDamage,
            boolean hasDurability,
            boolean isEmpty,
            List<String> enchantments,
            Object itemHandle
    ) {
        this.slot = slot;
        this.itemId = itemId != null ? itemId : "";
        this.displayName = displayName != null ? displayName : "";
        this.damage = damage;
        this.maxDamage = maxDamage;
        this.hasDurability = hasDurability;
        this.isEmpty = isEmpty;
        this.enchantments = enchantments != null ? Collections.unmodifiableList(enchantments) : Collections.emptyList();
        this.itemHandle = itemHandle;
    }

    public static ArmorItemData empty(ArmorSlot slot) {
        return new ArmorItemData(slot, "", "Pusty slot", 0, 0, false, true, Collections.emptyList(), null);
    }

    public ArmorSlot getSlot() {
        return slot;
    }

    public String getItemId() {
        return itemId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDamage() {
        return damage;
    }

    public int getMaxDamage() {
        return maxDamage;
    }

    public boolean hasDurability() {
        return hasDurability;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public List<String> getEnchantments() {
        return enchantments;
    }

    public Object getItemHandle() {
        return itemHandle;
    }

    public int getRemainingDurability() {
        if (!hasDurability || maxDamage <= 0) return 0;
        return Math.max(0, maxDamage - damage);
    }

    public float getDurabilityPercentage() {
        if (!hasDurability || maxDamage <= 0) return 100.0f;
        float pct = ((float) getRemainingDurability() / maxDamage) * 100.0f;
        return Math.max(0.0f, Math.min(100.0f, pct));
    }
}
