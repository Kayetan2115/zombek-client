package pl.zombek.client.core.modules.pvp;

import pl.zombek.client.core.data.ArmorDataProviderRegistry;
import pl.zombek.client.core.data.ArmorItemData;
import pl.zombek.client.core.data.ArmorSlot;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Armor Status HUD Module.
 * Displays equipped helmet, chestplate, leggings, and boots with real durability,
 * percentage indicators, damage thresholds, enchantment levels, and customizable layouts.
 */
public class ArmorStatusModule extends AbstractModule implements IHudElement {

    public enum LayoutStyle {
        VERTICAL("Pionowy"),
        HORIZONTAL("Poziomy"),
        COMPACT("Kompaktowy"),
        ICON_ONLY("Tylko Ikony"),
        DETAILED("Szczegółowy");

        private final String displayName;
        LayoutStyle(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum SlotOrder {
        HELMET_TO_BOOTS("Hełm -> Buty"),
        BOOTS_TO_HELMET("Buty -> Hełm");

        private final String displayName;
        SlotOrder(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    // General Group
    private final BooleanSetting showHelmet;
    private final BooleanSetting showChestplate;
    private final BooleanSetting showLeggings;
    private final BooleanSetting showBoots;
    private final BooleanSetting hideEmptySlots;

    // Display Group
    private final BooleanSetting showItemIcons;
    private final BooleanSetting showDurability;
    private final BooleanSetting showPercentage;
    private final BooleanSetting showRemainingDurability;
    private final BooleanSetting showDurabilityBar;
    private final BooleanSetting showEnchantments;
    private final BooleanSetting showItemNames;

    // Layout Group
    private final EnumSetting<LayoutStyle> layoutStyle;
    private final EnumSetting<SlotOrder> slotOrder;
    private final NumberSetting iconSize;
    private final NumberSetting scale;
    private final NumberSetting spacing;
    private final NumberSetting padding;
    private final NumberSetting positionX;
    private final NumberSetting positionY;
    private final EnumSetting<HudAnchor> anchor;

    // Colors Group
    private final ColorSetting textColor;
    private final ColorSetting durabilityColor;
    private final ColorSetting backgroundColor;
    private final ColorSetting borderColor;
    private final NumberSetting opacity;

    // Durability Colors & Thresholds Group
    private final NumberSetting highThreshold;
    private final NumberSetting lowThreshold;
    private final ColorSetting highColor;
    private final ColorSetting mediumColor;
    private final ColorSetting lowColor;
    private final ColorSetting criticalColor;

    // Appearance Group
    private final BooleanSetting showBackground;
    private final BooleanSetting showBorder;
    private final BooleanSetting showShadow;
    private final NumberSetting cornerRadius;
    private final NumberSetting borderThickness;
    private final NumberSetting textSize;

    public ArmorStatusModule() {
        super("armor_status", "Armor Status", "Wyświetla aktualny stan i wytrzymałość noszonej zbroi na ekranie gry.", ModuleCategory.PVP);

        // --- 1. General ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        showHelmet = generalGroup.addSetting(new BooleanSetting("show_helmet", "Pokaż Hełm", "Wyświetla slot hełmu", true));
        showChestplate = generalGroup.addSetting(new BooleanSetting("show_chestplate", "Pokaż Napierśnik", "Wyświetla slot napierśnika", true));
        showLeggings = generalGroup.addSetting(new BooleanSetting("show_leggings", "Pokaż Nogawice", "Wyświetla slot nogawic", true));
        showBoots = generalGroup.addSetting(new BooleanSetting("show_boots", "Pokaż Buty", "Wyświetla slot butów", true));
        hideEmptySlots = generalGroup.addSetting(new BooleanSetting("hide_empty", "Ukryj puste sloty", "Nie renderuje slotów bez założonego pancerza", true));
        registerGroup(generalGroup);

        // --- 2. Display ---
        SettingGroup displayGroup = new SettingGroup("display", "Informacje");
        showItemIcons = displayGroup.addSetting(new BooleanSetting("show_icons", "Pokaż ikony", "Renderowanie ikon przedmiotów Minecraft", true));
        showDurability = displayGroup.addSetting(new BooleanSetting("show_durability", "Pokaż wytrzymałość", "Wyświetlanie wartości liczbowej lub procentowej", true));
        showPercentage = displayGroup.addSetting(new BooleanSetting("show_percentage", "Pokaż procenty", "Wyświetlanie % pozostałej wytrzymałości", true));
        showRemainingDurability = displayGroup.addSetting(new BooleanSetting("show_remaining", "Pozostałe punkty", "Wyświetlanie pozostałych punktów durability", false));
        showDurabilityBar = displayGroup.addSetting(new BooleanSetting("show_bar", "Pasek wytrzymałości", "Renderowanie kolorowego paska pod ikoną", true));
        showEnchantments = displayGroup.addSetting(new BooleanSetting("show_enchants", "Pokaż zaklęcia", "Wyświetlanie skrótów aktywnych enchantów", false));
        showItemNames = displayGroup.addSetting(new BooleanSetting("show_names", "Pokaż nazwy", "Wyświetlanie pełnych nazw przedmiotów", false));
        registerGroup(displayGroup);

        // --- 3. Layout ---
        SettingGroup layoutGroup = new SettingGroup("layout", "Układ i Pozycja");
        layoutStyle = layoutGroup.addSetting(new EnumSetting<>("layout_style", "Styl układu", "Orientacja i format prezentacji", LayoutStyle.VERTICAL));
        slotOrder = layoutGroup.addSetting(new EnumSetting<>("slot_order", "Kolejność slotów", "Kierunek wyświetlania elementów", SlotOrder.HELMET_TO_BOOTS));
        iconSize = layoutGroup.addSetting(new NumberSetting("icon_size", "Rozmiar ikony", "Bazowy rozmiar ikony przedmiotu", 16.0, 12.0, 32.0, 1.0));
        scale = layoutGroup.addSetting(new NumberSetting("scale", "Skala", "Globalne powiększenie modułu", 1.0, 0.5, 2.5, 0.1));
        spacing = layoutGroup.addSetting(new NumberSetting("spacing", "Odstęp", "Odstęp między elementami pancerza", 4.0, 0.0, 20.0, 1.0));
        padding = layoutGroup.addSetting(new NumberSetting("padding", "Margines wewnętrzny", "Wewnętrzny padding ramki", 4.0, 0.0, 20.0, 1.0));
        positionX = layoutGroup.addSetting(new NumberSetting("position_x", "Pozycja X", "Przesunięcie w poziomie od punktu kotwiczenia", 10.0, 0.0, 2000.0, 1.0));
        positionY = layoutGroup.addSetting(new NumberSetting("position_y", "Pozycja Y", "Przesunięcie w pionie od punktu kotwiczenia", 140.0, 0.0, 2000.0, 1.0));
        anchor = layoutGroup.addSetting(new EnumSetting<>("anchor", "Punkt kotwiczenia", "Róg ekranu dla pozycjonowania", HudAnchor.TOP_LEFT));
        registerGroup(layoutGroup);

        // --- 4. Colors ---
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory");
        textColor = colorsGroup.addSetting(new ColorSetting("text_color", "Kolor tekstu", "Domyślny kolor etykiet", 0xFFFFFFFF, false));
        durabilityColor = colorsGroup.addSetting(new ColorSetting("durability_color", "Kolor durability", "Kolor wartości liczbowej", 0xFFAAAAAA, false));
        backgroundColor = colorsGroup.addSetting(new ColorSetting("bg_color", "Kolor tła", "Tło modułu", 0x80000000, true));
        borderColor = colorsGroup.addSetting(new ColorSetting("border_color", "Kolor obramowania", "Kolor obramowania", 0x40FFFFFF, true));
        opacity = colorsGroup.addSetting(new NumberSetting("opacity", "Przezroczystość", "Globalne krycie tła w %", 80.0, 0.0, 100.0, 5.0));
        registerGroup(colorsGroup);

        // --- 5. Durability Colors & Thresholds ---
        SettingGroup durColorsGroup = new SettingGroup("durability_colors", "Progi i Barwy Trwałości");
        highThreshold = durColorsGroup.addSetting(new NumberSetting("high_threshold", "Próg dobrego stanu (%)", "Powyżej tej wartości stan jest dobry", 60.0, 40.0, 90.0, 5.0, "%"));
        lowThreshold = durColorsGroup.addSetting(new NumberSetting("low_threshold", "Próg ostrzegawczy (%)", "Poniżej tej wartości stan jest niski", 30.0, 10.0, 50.0, 5.0, "%"));
        highColor = durColorsGroup.addSetting(new ColorSetting("high_color", "Stan dobry", "Kolor dla wysokiej trwałości", 0xFF55FF55, false));
        mediumColor = durColorsGroup.addSetting(new ColorSetting("medium_color", "Stan średni", "Kolor ostrzegawczy", 0xFFFFFF55, false));
        lowColor = durColorsGroup.addSetting(new ColorSetting("low_color", "Stan niski", "Kolor niskiej trwałości", 0xFFFF5555, false));
        criticalColor = durColorsGroup.addSetting(new ColorSetting("critical_color", "Stan krytyczny", "Kolor krytycznego uszkodzenia (<15%)", 0xFFAA0000, false));
        registerGroup(durColorsGroup);

        // --- 6. Appearance ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        showBackground = appearanceGroup.addSetting(new BooleanSetting("show_bg", "Pokaż tło", "Czy renderować prostokąt tła", true));
        showBorder = appearanceGroup.addSetting(new BooleanSetting("show_border", "Pokaż obramowanie", "Czy rysować obramowanie", true));
        showShadow = appearanceGroup.addSetting(new BooleanSetting("show_shadow", "Cień tekstu", "Renderowanie cienia czcionki", true));
        cornerRadius = appearanceGroup.addSetting(new NumberSetting("corner_radius", "Zaokrąglenie", "Promień zaokrąglenia narożników", 0.0, 0.0, 6.0, 1.0));
        borderThickness = appearanceGroup.addSetting(new NumberSetting("border_thickness", "Grubość ramki", "Szerokość linii ramki w px", 1.0, 1.0, 4.0, 1.0));
        textSize = appearanceGroup.addSetting(new NumberSetting("text_size", "Skala czcionki", "Mnożnik wielkości tekstu", 1.0, 0.8, 1.5, 0.1));
        registerGroup(appearanceGroup);
    }

    @Override
    public void onEnable() {
        HudElementRegistry.getInstance().register(this);
    }

    @Override
    public void onDisable() {
        HudElementRegistry.getInstance().unregister(this);
    }

    @Override
    public String getElementId() {
        return getId();
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible() || renderer == null) return;

        List<ArmorItemData> armorList = getFilteredAndSortedArmor();
        if (armorList.isEmpty()) return;

        int pad = padding.getValue().intValue();
        int space = spacing.getValue().intValue();
        int iconPx = iconSize.getValue().intValue();
        float s = scale.getValue().floatValue();
        float textScale = textSize.getValue().floatValue() * s;
        boolean isHorizontal = layoutStyle.getValue() == LayoutStyle.HORIZONTAL;
        boolean isIconOnly = layoutStyle.getValue() == LayoutStyle.ICON_ONLY;
        boolean isCompact = layoutStyle.getValue() == LayoutStyle.COMPACT;
        boolean isDetailed = layoutStyle.getValue() == LayoutStyle.DETAILED;

        // Calculate item slot dimensions
        int itemSlotWidth = iconPx;
        int itemSlotHeight = iconPx;

        if (!isIconOnly) {
            int maxTextW = 0;
            for (ArmorItemData item : armorList) {
                String label = buildItemLabel(item);
                int tw = (int) (renderer.getTextWidth(label) * textScale);
                if (tw > maxTextW) maxTextW = tw;
            }
            itemSlotWidth = iconPx + 4 + maxTextW;
            if (isDetailed || showDurabilityBar.getValue()) {
                itemSlotHeight = Math.max(iconPx, renderer.getFontHeight() + 4);
            }
        }

        int totalWidth;
        int totalHeight;

        if (isHorizontal) {
            totalWidth = pad * 2 + (armorList.size() * itemSlotWidth) + (Math.max(0, armorList.size() - 1) * space);
            totalHeight = pad * 2 + itemSlotHeight;
        } else {
            totalWidth = pad * 2 + itemSlotWidth;
            totalHeight = pad * 2 + (armorList.size() * itemSlotHeight) + (Math.max(0, armorList.size() - 1) * space);
        }

        int baseX = anchor.getValue().calculateX(positionX.getValue().intValue(), totalWidth, renderer.getScreenWidth());
        int baseY = anchor.getValue().calculateY(positionY.getValue().intValue(), totalHeight, renderer.getScreenHeight());

        // Render Background & Border
        if (showBackground.getValue()) {
            int bg = applyOpacity(backgroundColor.getValue(), opacity.getValue().floatValue() / 100.0f);
            renderer.fill(baseX, baseY, totalWidth, totalHeight, bg);
        }
        if (showBorder.getValue()) {
            renderer.drawBorder(baseX, baseY, totalWidth, totalHeight, borderColor.getValue(), borderThickness.getValue().intValue());
        }

        // Render Armor Slots
        int curX = baseX + pad;
        int curY = baseY + pad;

        for (ArmorItemData item : armorList) {
            // 1. Draw Item Icon
            if (showItemIcons.getValue() && !item.isEmpty()) {
                renderer.renderItem(item.getItemHandle(), curX, curY, s);
            }

            // 2. Draw Durability Bar
            if (showDurabilityBar.getValue() && item.hasDurability() && item.getMaxDamage() > 0) {
                float pct = item.getDurabilityPercentage();
                int barColor = resolveDurabilityColor(pct);
                int barWidth = Math.max(1, (int) ((iconPx - 2) * (pct / 100.0f)));
                int barY = curY + iconPx - 2;
                // Dark background behind bar
                renderer.fill(curX + 1, barY, iconPx - 2, 2, 0xFF000000);
                // Colored progress bar
                renderer.fill(curX + 1, barY, barWidth, 2, barColor);
            }

            // 3. Draw Labels (Durability / Enchants / Names)
            if (!isIconOnly) {
                String label = buildItemLabel(item);
                if (!label.isEmpty()) {
                    int labelColor = item.hasDurability()
                            ? resolveDurabilityColor(item.getDurabilityPercentage())
                            : textColor.getValue();

                    int textX = curX + iconPx + 4;
                    int textY = curY + (iconPx - renderer.getFontHeight()) / 2;

                    renderer.drawText(label, textX, textY, labelColor, showShadow.getValue(), textScale, false);
                }
            }

            if (isHorizontal) {
                curX += itemSlotWidth + space;
            } else {
                curY += itemSlotHeight + space;
            }
        }
    }

    private List<ArmorItemData> getFilteredAndSortedArmor() {
        List<ArmorItemData> equipped = ArmorDataProviderRegistry.getProvider().getEquippedArmor();
        if (equipped == null || equipped.isEmpty()) return Collections.emptyList();

        List<ArmorItemData> filtered = new ArrayList<>();
        for (ArmorItemData item : equipped) {
            if (item == null) continue;
            if (item.isEmpty() && hideEmptySlots.getValue()) continue;

            if (item.getSlot() == ArmorSlot.HELMET && !showHelmet.getValue()) continue;
            if (item.getSlot() == ArmorSlot.CHESTPLATE && !showChestplate.getValue()) continue;
            if (item.getSlot() == ArmorSlot.LEGGINGS && !showLeggings.getValue()) continue;
            if (item.getSlot() == ArmorSlot.BOOTS && !showBoots.getValue()) continue;

            filtered.add(item);
        }

        // Sort by slot order
        boolean helmetFirst = slotOrder.getValue() == SlotOrder.HELMET_TO_BOOTS;
        filtered.sort((a, b) -> {
            int cmp = Integer.compare(b.getSlot().getInventoryIndex(), a.getSlot().getInventoryIndex());
            return helmetFirst ? cmp : -cmp;
        });

        return filtered;
    }

    private String buildItemLabel(ArmorItemData item) {
        if (item.isEmpty()) return "Brak";

        StringBuilder sb = new StringBuilder();

        if (showItemNames.getValue() && !item.getDisplayName().isEmpty()) {
            sb.append(item.getDisplayName()).append(" ");
        }

        if (item.hasDurability() && showDurability.getValue()) {
            float pct = item.getDurabilityPercentage();
            int remaining = item.getRemainingDurability();

            if (showPercentage.getValue() && showRemainingDurability.getValue()) {
                sb.append(Math.round(pct)).append("% (").append(remaining).append(")");
            } else if (showPercentage.getValue()) {
                sb.append(Math.round(pct)).append("%");
            } else if (showRemainingDurability.getValue()) {
                sb.append(remaining);
            }
        }

        if (showEnchantments.getValue() && !item.getEnchantments().isEmpty()) {
            if (sb.length() > 0) sb.append(" [");
            sb.append(String.join(", ", item.getEnchantments()));
            sb.append("]");
        }

        return sb.toString().trim();
    }

    public int resolveDurabilityColor(float percentage) {
        float high = highThreshold.getValue().floatValue();
        float low = lowThreshold.getValue().floatValue();

        if (percentage >= high) {
            return highColor.getValue();
        } else if (percentage >= low) {
            return mediumColor.getValue();
        } else if (percentage > 15.0f) {
            return lowColor.getValue();
        } else {
            return criticalColor.getValue();
        }
    }

    private int applyOpacity(int color, float alphaFactor) {
        int alpha = (color >> 24) & 0xFF;
        int newAlpha = Math.round(alpha * alphaFactor);
        return (newAlpha << 24) | (color & 0x00FFFFFF);
    }

    // Getters for settings
    public BooleanSetting getShowHelmet() { return showHelmet; }
    public BooleanSetting getShowChestplate() { return showChestplate; }
    public BooleanSetting getShowLeggings() { return showLeggings; }
    public BooleanSetting getShowBoots() { return showBoots; }
    public BooleanSetting getHideEmptySlots() { return hideEmptySlots; }
    public EnumSetting<LayoutStyle> getLayoutStyle() { return layoutStyle; }
    public EnumSetting<SlotOrder> getSlotOrder() { return slotOrder; }
}
