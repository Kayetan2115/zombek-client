package pl.zombek.client.core.modules.visual.oneseven;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages first person 3D transform adjustments across all item categories.
 */
public class ItemTransformManager {
    private static ItemTransformManager instance;

    private final Map<ItemCategory, CategoryTransform> transforms = new EnumMap<>(ItemCategory.class);

    public ItemTransformManager() {
        for (ItemCategory cat : ItemCategory.values()) {
            transforms.put(cat, new CategoryTransform(cat));
        }
    }

    public static synchronized ItemTransformManager getInstance() {
        if (instance == null) {
            instance = new ItemTransformManager();
        }
        return instance;
    }

    public CategoryTransform getTransform(ItemCategory category) {
        return transforms.computeIfAbsent(category, CategoryTransform::new);
    }

    public CategoryTransform resolveTransform(String itemId) {
        ItemCategory cat = ItemCategory.fromItemId(itemId);
        return getTransform(cat);
    }

    public void resetCategory(ItemCategory category) {
        CategoryTransform transform = getTransform(category);
        transform.reset();
    }

    public void resetAllCategories() {
        for (CategoryTransform transform : transforms.values()) {
            transform.reset();
        }
    }

    public void copyCategory(ItemCategory source, ItemCategory target) {
        Objects.requireNonNull(source, "source category cannot be null");
        Objects.requireNonNull(target, "target category cannot be null");
        CategoryTransform srcTransform = getTransform(source);
        CategoryTransform tgtTransform = getTransform(target);
        tgtTransform.set(srcTransform);
    }

    /**
     * Applies preset offsets to all item categories.
     */
    public void applyPreset(AnimationPreset preset) {
        resetAllCategories();
        if (preset == AnimationPreset.MODERN) {
            return;
        }

        if (preset == AnimationPreset.MINECRAFT_1_8) {
            // 1.8 sword
            CategoryTransform sword = getTransform(ItemCategory.SWORD);
            sword.setEnabled(true);
            sword.getOffset().setRotY(-5.0f);
            sword.getOffset().setPosY(-0.02f);

            // 1.8 bow
            CategoryTransform bow = getTransform(ItemCategory.BOW);
            bow.setEnabled(true);
            bow.getOffset().setRotX(4.0f);
            bow.getOffset().setRotY(-4.0f);
            bow.getOffset().setPosY(-0.02f);

            // 1.8 rod
            CategoryTransform rod = getTransform(ItemCategory.FISHING_ROD);
            rod.setEnabled(true);
            rod.getOffset().setRotX(-8.0f);
            rod.getOffset().setPosY(-0.03f);
        } else if (preset == AnimationPreset.MINECRAFT_1_7) {
            // 1.7 authentic sword position
            CategoryTransform sword = getTransform(ItemCategory.SWORD);
            sword.setEnabled(true);
            sword.getOffset().setPosX(-0.02f);
            sword.getOffset().setPosY(-0.05f);
            sword.getOffset().setPosZ(-0.02f);
            sword.getOffset().setRotX(-5.0f);
            sword.getOffset().setRotY(-12.0f);
            sword.getOffset().setRotZ(5.0f);

            // 1.7 bow
            CategoryTransform bow = getTransform(ItemCategory.BOW);
            bow.setEnabled(true);
            bow.getOffset().setRotX(8.0f);
            bow.getOffset().setRotY(-8.0f);
            bow.getOffset().setRotZ(2.0f);
            bow.getOffset().setPosY(-0.04f);

            // 1.7 fishing rod angled whip
            CategoryTransform rod = getTransform(ItemCategory.FISHING_ROD);
            rod.setEnabled(true);
            rod.getOffset().setRotX(-15.0f);
            rod.getOffset().setRotY(10.0f);
            rod.getOffset().setRotZ(-5.0f);
            rod.getOffset().setPosY(-0.06f);

            // 1.7 tools
            CategoryTransform axe = getTransform(ItemCategory.AXE);
            axe.setEnabled(true);
            axe.getOffset().setRotY(-8.0f);
            axe.getOffset().setPosY(-0.03f);

            CategoryTransform pickaxe = getTransform(ItemCategory.PICKAXE);
            pickaxe.setEnabled(true);
            pickaxe.getOffset().setRotY(-8.0f);
            pickaxe.getOffset().setPosY(-0.03f);

            // 1.7 food & potion
            CategoryTransform food = getTransform(ItemCategory.FOOD);
            food.setEnabled(true);
            food.getOffset().setPosY(-0.02f);
            food.getOffset().setRotX(4.0f);

            CategoryTransform potion = getTransform(ItemCategory.POTION);
            potion.setEnabled(true);
            potion.getOffset().setPosY(-0.02f);
            potion.getOffset().setRotX(4.0f);
        }
    }

    public void serializeToJson(JsonObject parentNode) {
        JsonObject itemsNode = new JsonObject();
        for (Map.Entry<ItemCategory, CategoryTransform> entry : transforms.entrySet()) {
            CategoryTransform ct = entry.getValue();
            JsonObject catNode = new JsonObject();
            catNode.addProperty("enabled", ct.isEnabled());
            TransformOffset off = ct.getOffset();
            catNode.addProperty("posX", off.getPosX());
            catNode.addProperty("posY", off.getPosY());
            catNode.addProperty("posZ", off.getPosZ());
            catNode.addProperty("rotX", off.getRotX());
            catNode.addProperty("rotY", off.getRotY());
            catNode.addProperty("rotZ", off.getRotZ());
            catNode.addProperty("scaleX", off.getScaleX());
            catNode.addProperty("scaleY", off.getScaleY());
            catNode.addProperty("scaleZ", off.getScaleZ());
            itemsNode.add(entry.getKey().name(), catNode);
        }
        parentNode.add("item_categories", itemsNode);
    }

    public void deserializeFromJson(JsonObject parentNode) {
        if (parentNode == null || !parentNode.has("item_categories")) {
            return;
        }
        JsonElement element = parentNode.get("item_categories");
        if (!element.isJsonObject()) return;
        JsonObject itemsNode = element.getAsJsonObject();
        for (ItemCategory cat : ItemCategory.values()) {
            if (itemsNode.has(cat.name())) {
                JsonElement catElem = itemsNode.get(cat.name());
                if (!catElem.isJsonObject()) continue;
                JsonObject catNode = catElem.getAsJsonObject();
                CategoryTransform ct = getTransform(cat);
                if (catNode.has("enabled")) ct.setEnabled(catNode.get("enabled").getAsBoolean());
                TransformOffset off = ct.getOffset();
                if (catNode.has("posX")) off.setPosX(catNode.get("posX").getAsFloat());
                if (catNode.has("posY")) off.setPosY(catNode.get("posY").getAsFloat());
                if (catNode.has("posZ")) off.setPosZ(catNode.get("posZ").getAsFloat());
                if (catNode.has("rotX")) off.setRotX(catNode.get("rotX").getAsFloat());
                if (catNode.has("rotY")) off.setRotY(catNode.get("rotY").getAsFloat());
                if (catNode.has("rotZ")) off.setRotZ(catNode.get("rotZ").getAsFloat());
                if (catNode.has("scaleX")) off.setScaleX(catNode.get("scaleX").getAsFloat());
                if (catNode.has("scaleY")) off.setScaleY(catNode.get("scaleY").getAsFloat());
                if (catNode.has("scaleZ")) off.setScaleZ(catNode.get("scaleZ").getAsFloat());
            }
        }
    }
}
