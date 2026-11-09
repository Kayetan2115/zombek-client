package pl.zombek.client.core.modules.hud.itemcounter;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.inventory.*;
import pl.zombek.client.core.itemtracker.TrackedItemRegistry;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.*;

/**
 * Universal Item Counter HUD module displaying real-time quantities of tracked items.
 */
public class ItemCounterModule extends AbstractModule implements IHudElement {

    public static final String ID = "item_counter";

    public static final class DisplayItemEntry {
        public final String itemId;
        public final String displayName;
        public final int count;
        public final int fullStacks;
        public final Object itemHandle;
        public final boolean warning;
        public final boolean critical;

        public DisplayItemEntry(String itemId, String displayName, int count, int fullStacks, Object itemHandle, boolean warning, boolean critical) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.count = count;
            this.fullStacks = fullStacks;
            this.itemHandle = itemHandle;
            this.warning = warning;
            this.critical = critical;
        }
    }

    public enum SortMode {
        CUSTOM_ORDER("Custom Order"),
        COUNT_DESC("Highest Count First"),
        COUNT_ASC("Lowest Count First"),
        ALPHABETICAL("Alphabetical");

        private final String label;
        SortMode(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final HudComponentModel model;

    // Settings Groups
    private final SettingGroup groupGeneral;
    private final EnumSetting<ItemGroupingMode> groupingModeSetting;
    private final BooleanSetting showItemNamesSetting;
    private final BooleanSetting showItemIconsSetting;
    private final BooleanSetting showQuantitySetting;
    private final BooleanSetting showStackCountSetting;
    private final BooleanSetting showZeroQuantitySetting;

    private final SettingGroup groupSources;
    private final BooleanSetting countHotbarSetting;
    private final BooleanSetting countMainInventorySetting;
    private final BooleanSetting countOffhandSetting;
    private final BooleanSetting countArmorSlotsSetting;

    private final SettingGroup groupTracking;
    private final StringSetting watchedItemsSetting;
    private final EnumSetting<SortMode> sortModeSetting;

    private final SettingGroup groupAlerts;
    private final BooleanSetting enableAlertsSetting;
    private final NumberSetting warningThresholdSetting;
    private final NumberSetting criticalThresholdSetting;
    private final ColorSetting warningColorSetting;
    private final ColorSetting criticalColorSetting;

    private final SettingGroup groupAppearance;
    private final EnumSetting<ItemCounterLayout> layoutSetting;
    private final EnumSetting<HudComponentStyle> styleSetting;
    private final NumberSetting iconSizeSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting borderColorSetting;
    private final BooleanSetting showBorderSetting;
    private final NumberSetting borderThicknessSetting;
    private final BooleanSetting shadowSetting;
    private final NumberSetting rowSpacingSetting;
    private final NumberSetting columnSpacingSetting;

    private final SettingGroup groupLayout;
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting posXSetting;
    private final NumberSetting posYSetting;
    private final NumberSetting scaleSetting;

    public ItemCounterModule() {
        super(ID, "Item Counter", "Wyświetla aktualną liczbę określonych przedmiotów w ekwipunku gracza.", ModuleCategory.HUD);

        this.model = new HudComponentModel(ID, 100, 40, HudAnchor.TOP_LEFT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_ITEM_COUNTER);

        // 1. General
        this.groupGeneral = new SettingGroup("general", "General");
        this.groupingModeSetting = new EnumSetting<>("grouping_mode", "Grouping Mode", "Tryb grupowania przedmiotów.", ItemGroupingMode.BY_ITEM_ID);
        this.showItemNamesSetting = new BooleanSetting("show_item_names", "Show Item Names", "Czy wyświetlać nazwy przedmiotów obok licznika.", true);
        this.showItemIconsSetting = new BooleanSetting("show_item_icons", "Show Item Icons", "Czy renderować ikony przedmiotów.", true);
        this.showQuantitySetting = new BooleanSetting("show_quantity", "Show Quantity", "Wyświetla liczbę posiadanych przedmiotów.", true);
        this.showStackCountSetting = new BooleanSetting("show_stack_count", "Show Stack Count", "Pokazuje liczbę pełnych stacków w nawiasie.", false);
        this.showZeroQuantitySetting = new BooleanSetting("show_zero_quantity", "Show Zero Quantity", "Pokazuje śledzone przedmioty nawet gdy ilość wynosi 0.", true);

        this.groupGeneral.addSetting(groupingModeSetting);
        this.groupGeneral.addSetting(showItemNamesSetting);
        this.groupGeneral.addSetting(showItemIconsSetting);
        this.groupGeneral.addSetting(showQuantitySetting);
        this.groupGeneral.addSetting(showStackCountSetting);
        this.groupGeneral.addSetting(showZeroQuantitySetting);

        // 2. Sources
        this.groupSources = new SettingGroup("sources", "Inventory Sources");
        this.countHotbarSetting = new BooleanSetting("count_hotbar", "Count Hotbar", "Zlicza przedmioty z paska podręcznego (Hotbar).", true);
        this.countMainInventorySetting = new BooleanSetting("count_main_inventory", "Count Main Inventory", "Zlicza przedmioty z głównego ekwipunku.", true);
        this.countOffhandSetting = new BooleanSetting("count_offhand", "Count Offhand", "Zlicza przedmioty w drugiej ręce (Offhand).", true);
        this.countArmorSlotsSetting = new BooleanSetting("count_armor_slots", "Count Armor Slots", "Zlicza przedmioty w slotach zbroi.", false);

        this.groupSources.addSetting(countHotbarSetting);
        this.groupSources.addSetting(countMainInventorySetting);
        this.groupSources.addSetting(countOffhandSetting);
        this.groupSources.addSetting(countArmorSlotsSetting);

        // 3. Tracking
        this.groupTracking = new SettingGroup("tracking", "Tracking & Selection");
        this.watchedItemsSetting = new StringSetting("watched_items", "Watched Items", "Lista identyfikatorów przedmiotów rozdzielona przecinkami.", "minecraft:ender_pearl,minecraft:golden_apple,minecraft:arrow,minecraft:shield");
        this.sortModeSetting = new EnumSetting<>("sort_mode", "Sort Mode", "Kolejność wyświetlania przedmiotów.", SortMode.CUSTOM_ORDER);

        this.groupTracking.addSetting(watchedItemsSetting);
        this.groupTracking.addSetting(sortModeSetting);

        // 4. Alerts
        this.groupAlerts = new SettingGroup("alerts", "Quantity Alerts");
        this.enableAlertsSetting = new BooleanSetting("enable_alerts", "Enable Alerts", "Ostrzeżenia kolorystyczne przy niskiej ilości.", true);
        this.warningThresholdSetting = new NumberSetting("warning_threshold", "Warning Threshold", "Próg ostrzegawczy ilości.", 4.0, 0.0, 64.0, 1.0);
        this.criticalThresholdSetting = new NumberSetting("critical_threshold", "Critical Threshold", "Próg krytyczny ilości.", 1.0, 0.0, 64.0, 1.0);
        this.warningColorSetting = new ColorSetting("warning_color", "Warning Color", "Kolor ostrzeżenia (niski stan).", 0xFFFFFF55);
        this.criticalColorSetting = new ColorSetting("critical_color", "Critical Color", "Kolor krytyczny (brak/minimalny stan).", 0xFFFF5555);

        this.groupAlerts.addSetting(enableAlertsSetting);
        this.groupAlerts.addSetting(warningThresholdSetting);
        this.groupAlerts.addSetting(criticalThresholdSetting);
        this.groupAlerts.addSetting(warningColorSetting);
        this.groupAlerts.addSetting(criticalColorSetting);

        // 5. Appearance
        this.groupAppearance = new SettingGroup("appearance", "Appearance");
        this.layoutSetting = new EnumSetting<>("layout", "Layout Mode", "Sposób prezentacji elementów na ekranie.", ItemCounterLayout.COMPACT);
        this.styleSetting = new EnumSetting<>("style", "HUD Style", "Styl wizualny tła komponentu.", HudComponentStyle.MODERN);
        this.iconSizeSetting = new NumberSetting("icon_size", "Icon Size", "Rozmiar ikon przedmiotów w pikselach.", 16.0, 8.0, 32.0, 2.0);
        this.textColorSetting = new ColorSetting("text_color", "Text Color", "Główny kolor tekstu.", 0xFFFFFFFF);
        this.backgroundColorSetting = new ColorSetting("background_color", "Background Color", "Kolor tła modułu.", 0x80000000);
        this.backgroundOpacitySetting = new NumberSetting("background_opacity", "Background Opacity", "Przezroczystość tła (%).", 70.0, 0.0, 100.0, 5.0);
        this.borderColorSetting = new ColorSetting("border_color", "Border Color", "Kolor obramowania.", 0x00000000);
        this.showBorderSetting = new BooleanSetting("show_border", "Show Border", "Czy wyświetlać ramkę.", false);
        this.borderThicknessSetting = new NumberSetting("border_thickness", "Border Thickness", "Grubość ramki w pikselach.", 1.0, 1.0, 4.0, 1.0);
        this.shadowSetting = new BooleanSetting("shadow", "Text Shadow", "Cień pod tekstem.", true);
        this.rowSpacingSetting = new NumberSetting("row_spacing", "Row Spacing", "Odstęp pionowy między wierszami.", 3.0, 0.0, 10.0, 1.0);
        this.columnSpacingSetting = new NumberSetting("column_spacing", "Column Spacing", "Odstęp poziomy w trybie poziomym.", 6.0, 1.0, 20.0, 1.0);

        this.groupAppearance.addSetting(layoutSetting);
        this.groupAppearance.addSetting(styleSetting);
        this.groupAppearance.addSetting(iconSizeSetting);
        this.groupAppearance.addSetting(textColorSetting);
        this.groupAppearance.addSetting(backgroundColorSetting);
        this.groupAppearance.addSetting(backgroundOpacitySetting);
        this.groupAppearance.addSetting(borderColorSetting);
        this.groupAppearance.addSetting(showBorderSetting);
        this.groupAppearance.addSetting(borderThicknessSetting);
        this.groupAppearance.addSetting(shadowSetting);
        this.groupAppearance.addSetting(rowSpacingSetting);
        this.groupAppearance.addSetting(columnSpacingSetting);

        // 6. Layout
        this.groupLayout = new SettingGroup("layout", "Layout & Position");
        this.anchorSetting = new EnumSetting<>("anchor", "Anchor", "Punkt zakotwiczenia na ekranie.", HudAnchor.TOP_LEFT);
        this.posXSetting = new NumberSetting("pos_x", "Position X", "Offset poziomy.", 5.0, -2000.0, 2000.0, 1.0);
        this.posYSetting = new NumberSetting("pos_y", "Position Y", "Offset pionowy.", 100.0, -2000.0, 2000.0, 1.0);
        this.scaleSetting = new NumberSetting("scale", "Scale", "Skala elementu HUD.", 1.0, 0.5, 2.5, 0.1);

        this.groupLayout.addSetting(anchorSetting);
        this.groupLayout.addSetting(posXSetting);
        this.groupLayout.addSetting(posYSetting);
        this.groupLayout.addSetting(scaleSetting);

        registerGroup(groupGeneral);
        registerGroup(groupSources);
        registerGroup(groupTracking);
        registerGroup(groupAlerts);
        registerGroup(groupAppearance);
        registerGroup(groupLayout);
    }

    @Override
    public String getElementId() {
        return ID;
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError();
    }

    public HudComponentModel getHudModel() {
        return model;
    }

    public Set<InventorySlotSource> getAllowedSources() {
        Set<InventorySlotSource> sources = EnumSet.noneOf(InventorySlotSource.class);
        if (countHotbarSetting.getValue()) sources.add(InventorySlotSource.HOTBAR);
        if (countMainInventorySetting.getValue()) sources.add(InventorySlotSource.MAIN_INVENTORY);
        if (countOffhandSetting.getValue()) sources.add(InventorySlotSource.OFFHAND);
        if (countArmorSlotsSetting.getValue()) sources.add(InventorySlotSource.ARMOR);
        return sources;
    }

    public List<String> getWatchedItemIds() {
        String raw = watchedItemsSetting.getValue();
        if (raw == null || raw.trim().isEmpty()) return Collections.emptyList();
        String[] parts = raw.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public void addWatchedItem(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) return;
        List<String> current = new ArrayList<>(getWatchedItemIds());
        if (!current.contains(itemId.trim())) {
            current.add(itemId.trim());
            watchedItemsSetting.setValue(String.join(",", current));
        }
    }

    public void removeWatchedItem(String itemId) {
        if (itemId == null) return;
        List<String> current = new ArrayList<>(getWatchedItemIds());
        if (current.remove(itemId.trim())) {
            watchedItemsSetting.setValue(String.join(",", current));
        }
    }

    public List<DisplayItemEntry> buildDisplayEntries(InventorySnapshot snapshot, boolean inGame) {
        List<DisplayItemEntry> entries = new ArrayList<>();

        if (!inGame || snapshot == null) {
            // Preview Fallback
            entries.add(new DisplayItemEntry("minecraft:ender_pearl", "Ender Pearls", 12, 0, null, false, false));
            entries.add(new DisplayItemEntry("minecraft:golden_apple", "Golden Apples", 4, 0, null, false, false));
            entries.add(new DisplayItemEntry("minecraft:obsidian", "Obsidian", 32, 0, null, false, false));
            return entries;
        }

        Set<InventorySlotSource> sources = getAllowedSources();
        List<String> watchedIds = getWatchedItemIds();

        for (String id : watchedIds) {
            int count = snapshot.getItemCount(id, sources);
            int stacks = snapshot.getFullStacksCount(id, sources);

            if (count == 0 && !showZeroQuantitySetting.getValue()) {
                continue;
            }

            InventoryItemData sample = snapshot.findFirst(id);
            String name = (sample != null && sample.getItemName() != null)
                    ? sample.getItemName()
                    : TrackedItemRegistry.getFriendlyName(id);

            Object handle = sample != null ? sample.getItemHandle() : null;

            boolean isWarning = false;
            boolean isCritical = false;

            if (enableAlertsSetting.getValue()) {
                double crit = criticalThresholdSetting.getValue();
                double warn = warningThresholdSetting.getValue();
                if (count <= crit) {
                    isCritical = true;
                } else if (count <= warn) {
                    isWarning = true;
                }
            }

            entries.add(new DisplayItemEntry(id, name, count, stacks, handle, isWarning, isCritical));
        }

        // Apply Sorting
        SortMode sortMode = sortModeSetting.getValue();
        if (sortMode == SortMode.COUNT_DESC) {
            entries.sort((a, b) -> Integer.compare(b.count, a.count));
        } else if (sortMode == SortMode.COUNT_ASC) {
            entries.sort((a, b) -> Integer.compare(a.count, b.count));
        } else if (sortMode == SortMode.ALPHABETICAL) {
            entries.sort(Comparator.comparing(a -> a.displayName));
        }

        return entries;
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible()) return;

        ZombekInventoryService inventoryService = ZombekInventoryService.getInstance();
        boolean inGame = InventoryDataProviderRegistry.getInstance().getProvider().isPlayerAvailable();
        InventorySnapshot snapshot = inGame ? inventoryService.getSnapshot() : null;

        List<DisplayItemEntry> entries = buildDisplayEntries(snapshot, inGame);
        if (entries.isEmpty()) return;

        int screenWidth = renderer.getScreenWidth();
        int screenHeight = renderer.getScreenHeight();
        float txtScale = scaleSetting.getValue().floatValue();
        int iconSize = iconSizeSetting.getValue().intValue();
        boolean showIcons = showItemIconsSetting.getValue();
        boolean showNames = showItemNamesSetting.getValue();
        boolean showQuantity = showQuantitySetting.getValue();
        boolean showStacks = showStackCountSetting.getValue();
        boolean shadow = shadowSetting.getValue();
        ItemCounterLayout layout = layoutSetting.getValue();

        int rowSpacing = rowSpacingSetting.getValue().intValue();
        int colSpacing = columnSpacingSetting.getValue().intValue();
        int paddingH = 6;
        int paddingV = 4;

        // Calculate sizing
        int lineHeight = Math.max(showIcons ? iconSize : 9, (int) Math.ceil(renderer.getFontHeight() * txtScale)) + rowSpacing;
        int totalWidth;
        int totalHeight;

        if (layout == ItemCounterLayout.HORIZONTAL) {
            int currentX = 0;
            for (DisplayItemEntry entry : entries) {
                int itemW = (showIcons ? iconSize + 2 : 0) + (int) Math.ceil(renderer.getTextWidth(formatItemText(entry, showNames, showQuantity, showStacks, !inGame)) * txtScale) + colSpacing;
                currentX += itemW;
            }
            totalWidth = currentX - colSpacing + (paddingH * 2);
            totalHeight = lineHeight + (paddingV * 2);
        } else {
            // Vertical / List / Detailed
            int maxW = 0;
            for (DisplayItemEntry entry : entries) {
                int itemW = (showIcons ? iconSize + 4 : 0) + (int) Math.ceil(renderer.getTextWidth(formatItemText(entry, showNames, showQuantity, showStacks, !inGame)) * txtScale);
                maxW = Math.max(maxW, itemW);
            }
            totalWidth = maxW + (paddingH * 2);
            totalHeight = (entries.size() * lineHeight) + (paddingV * 2);
        }

        model.setWidth(totalWidth);
        model.setHeight(totalHeight);
        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.clampToScreen(screenWidth, screenHeight);

        int renderX = model.getScreenX(screenWidth);
        int renderY = model.getScreenY(screenHeight);

        // Render Background
        HudComponentRenderer.renderBackground(
                renderer,
                renderX, renderY, totalWidth, totalHeight,
                styleSetting.getValue(),
                backgroundColorSetting.getValue(),
                backgroundOpacitySetting.getValue(),
                borderColorSetting.getValue(),
                showBorderSetting.getValue(),
                borderThicknessSetting.getValue().intValue()
        );

        int curX = renderX + paddingH;
        int curY = renderY + paddingV;

        for (DisplayItemEntry entry : entries) {
            int textColor = textColorSetting.getValue();
            if (entry.critical) {
                textColor = criticalColorSetting.getValue();
            } else if (entry.warning) {
                textColor = warningColorSetting.getValue();
            }

            int itemDrawX = curX;

            if (showIcons && entry.itemHandle != null) {
                renderer.renderItem(entry.itemHandle, itemDrawX, curY);
                itemDrawX += iconSize + 3;
            }

            String text = formatItemText(entry, showNames, showQuantity, showStacks, !inGame);
            int textY = curY + ((lineHeight - (int) Math.ceil(renderer.getFontHeight() * txtScale)) / 2);
            renderer.drawText(text, itemDrawX, textY, textColor, shadow, txtScale, false);

            if (layout == ItemCounterLayout.HORIZONTAL) {
                curX += (showIcons ? iconSize + 3 : 0) + (int) Math.ceil(renderer.getTextWidth(text) * txtScale) + colSpacing;
            } else {
                curY += lineHeight;
            }
        }
    }

    private String formatItemText(DisplayItemEntry entry, boolean showNames, boolean showQuantity, boolean showStacks, boolean preview) {
        StringBuilder sb = new StringBuilder();
        if (showNames) {
            sb.append(entry.displayName);
            if (showQuantity) {
                sb.append(": ");
            }
        }
        if (showQuantity) {
            sb.append(entry.count);
            if (showStacks && entry.fullStacks > 0) {
                sb.append(" (").append(entry.fullStacks).append("s)");
            }
        }
        if (preview) {
            sb.append(" [PREVIEW]");
        }
        return sb.toString();
    }
}
