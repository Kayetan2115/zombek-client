package pl.zombek.client.core.modules.hud.itemtracker;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.inventory.InventoryDataProviderRegistry;
import pl.zombek.client.core.itemtracker.*;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.*;

/**
 * Universal Item Tracker HUD module monitoring item acquisition, usage, and drop history during sessions.
 */
public class ItemTrackerModule extends AbstractModule implements IHudElement {

    public static final String ID = "item_tracker";

    private final HudComponentModel model;

    // Setting Groups
    private final SettingGroup groupGeneral;
    private final BooleanSetting autoStartSetting;
    private final BooleanSetting autoPauseSetting;
    private final BooleanSetting trackSingleplayerSetting;
    private final BooleanSetting trackMultiplayerSetting;
    private final BooleanSetting resetOnWorldChangeSetting;

    private final SettingGroup groupEvents;
    private final BooleanSetting trackCollectedSetting;
    private final BooleanSetting trackUsedSetting;
    private final BooleanSetting trackConsumedSetting;
    private final BooleanSetting trackDroppedSetting;
    private final BooleanSetting trackCraftedSetting;
    private final BooleanSetting trackPlacedSetting;
    private final BooleanSetting trackUnknownChangesSetting;

    private final SettingGroup groupConfidence;
    private final BooleanSetting showConfirmedSetting;
    private final BooleanSetting showObservedSetting;
    private final BooleanSetting showInferredSetting;
    private final BooleanSetting showUnknownSetting;
    private final BooleanSetting confirmedStatisticsOnlySetting;

    private final SettingGroup groupDisplay;
    private final EnumSetting<ItemTrackerLayout> layoutSetting;
    private final NumberSetting maxEventsDisplayedSetting;
    private final BooleanSetting showTimestampsSetting;
    private final BooleanSetting showConfidenceLabelSetting;
    private final BooleanSetting showHeaderSetting;

    private final SettingGroup groupAppearance;
    private final EnumSetting<HudComponentStyle> styleSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting borderColorSetting;
    private final BooleanSetting showBorderSetting;
    private final NumberSetting borderThicknessSetting;
    private final BooleanSetting shadowSetting;

    private final SettingGroup groupLayout;
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting posXSetting;
    private final NumberSetting posYSetting;
    private final NumberSetting scaleSetting;

    public ItemTrackerModule() {
        super(ID, "Item Tracker", "Śledzi historię zmian ilości przedmiotów (zdobywanie, używanie, wyrzucanie) podczas sesji.", ModuleCategory.HUD);

        this.model = new HudComponentModel(ID, 140, 60, HudAnchor.TOP_LEFT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_ITEM_TRACKER);

        // 1. General
        this.groupGeneral = new SettingGroup("general", "General");
        this.autoStartSetting = new BooleanSetting("auto_start", "Auto Start", "Automatycznie rozpoczyna nową sesję po wejściu do świata.", true);
        this.autoPauseSetting = new BooleanSetting("auto_pause", "Auto Pause", "Pauzuje śledzenie gdy gra jest zapauzowana / w menu.", true);
        this.trackSingleplayerSetting = new BooleanSetting("track_singleplayer", "Track Singleplayer", "Śledzi zmiany w grze jednoosobowej.", true);
        this.trackMultiplayerSetting = new BooleanSetting("track_multiplayer", "Track Multiplayer", "Śledzi zmiany na serwerach wieloosobowych.", true);
        this.resetOnWorldChangeSetting = new BooleanSetting("reset_on_world_change", "Reset on World Change", "Resetuje statystyki i historię po zmianie świata.", false);

        this.groupGeneral.addSetting(autoStartSetting);
        this.groupGeneral.addSetting(autoPauseSetting);
        this.groupGeneral.addSetting(trackSingleplayerSetting);
        this.groupGeneral.addSetting(trackMultiplayerSetting);
        this.groupGeneral.addSetting(resetOnWorldChangeSetting);

        // 2. Events
        this.groupEvents = new SettingGroup("events", "Tracked Event Types");
        this.trackCollectedSetting = new BooleanSetting("track_collected", "Track Collected / Picked Up", "Śledzi podnoszenie i zdobywanie przedmiotów.", true);
        this.trackUsedSetting = new BooleanSetting("track_used", "Track Used", "Śledzi użycie narzędzi, pereł i przedmiotów.", true);
        this.trackConsumedSetting = new BooleanSetting("track_consumed", "Track Consumed", "Śledzi zjadanie jabłek, jedzenia i mikstur.", true);
        this.trackDroppedSetting = new BooleanSetting("track_dropped", "Track Dropped", "Śledzi wyrzucanie przedmiotów z ekwipunku.", true);
        this.trackCraftedSetting = new BooleanSetting("track_crafted", "Track Crafted", "Śledzi przedmioty wytworzone w craftingu.", true);
        this.trackPlacedSetting = new BooleanSetting("track_placed", "Track Placed", "Śledzi stawianie bloków w świecie.", true);
        this.trackUnknownChangesSetting = new BooleanSetting("track_unknown", "Track Unknown Changes", "Śledzi zmiany ilości o niepotwierdzonym źródle.", true);

        this.groupEvents.addSetting(trackCollectedSetting);
        this.groupEvents.addSetting(trackUsedSetting);
        this.groupEvents.addSetting(trackConsumedSetting);
        this.groupEvents.addSetting(trackDroppedSetting);
        this.groupEvents.addSetting(trackCraftedSetting);
        this.groupEvents.addSetting(trackPlacedSetting);
        this.groupEvents.addSetting(trackUnknownChangesSetting);

        // 3. Confidence
        this.groupConfidence = new SettingGroup("confidence", "Confidence Levels");
        this.showConfirmedSetting = new BooleanSetting("show_confirmed", "Show Confirmed", "Wyświetla zdarzenia w 100% potwierdzone przez silnik gry.", true);
        this.showObservedSetting = new BooleanSetting("show_observed", "Show Observed", "Wyświetla zdarzenia zaobserwowane przez klienta.", true);
        this.showInferredSetting = new BooleanSetting("show_inferred", "Show Inferred", "Wyświetla zdarzenia wywnioskowane z kontekstu gry.", true);
        this.showUnknownSetting = new BooleanSetting("show_unknown", "Show Unknown Changes", "Wyświetla zdarzenia o nieznanym źródle zmiany.", true);
        this.confirmedStatisticsOnlySetting = new BooleanSetting("confirmed_stats_only", "Confirmed Stats Only", "Zlicza do oficjalnych statystyk wyłącznie potwierdzone zdarzenia.", false);

        this.groupConfidence.addSetting(showConfirmedSetting);
        this.groupConfidence.addSetting(showObservedSetting);
        this.groupConfidence.addSetting(showInferredSetting);
        this.groupConfidence.addSetting(showUnknownSetting);
        this.groupConfidence.addSetting(confirmedStatisticsOnlySetting);

        // 4. Display
        this.groupDisplay = new SettingGroup("display", "Display Options");
        this.layoutSetting = new EnumSetting<>("layout", "Layout Mode", "Tryb prezentacji modułu.", ItemTrackerLayout.FEED);
        this.maxEventsDisplayedSetting = new NumberSetting("max_events", "Max Events Displayed", "Liczba ostatnich zdarzeń na ekranie.", 4.0, 1.0, 10.0, 1.0);
        this.showTimestampsSetting = new BooleanSetting("show_timestamps", "Show Timestamps", "Wyświetla godzinę zdarzenia [HH:mm:ss].", false);
        this.showConfidenceLabelSetting = new BooleanSetting("show_confidence", "Show Confidence Label", "Wyświetla poziom pewności obok wpisu.", true);
        this.showHeaderSetting = new BooleanSetting("show_header", "Show Header", "Wyświetla nagłówek 'Item Tracker'.", true);

        this.groupDisplay.addSetting(layoutSetting);
        this.groupDisplay.addSetting(maxEventsDisplayedSetting);
        this.groupDisplay.addSetting(showTimestampsSetting);
        this.groupDisplay.addSetting(showConfidenceLabelSetting);
        this.groupDisplay.addSetting(showHeaderSetting);

        // 5. Appearance
        this.groupAppearance = new SettingGroup("appearance", "Appearance");
        this.styleSetting = new EnumSetting<>("style", "HUD Style", "Styl wizualny kontenera.", HudComponentStyle.MODERN);
        this.textColorSetting = new ColorSetting("text_color", "Text Color", "Domyślny kolor tekstu.", 0xFFFFFFFF);
        this.backgroundColorSetting = new ColorSetting("background_color", "Background Color", "Kolor tła modułu.", 0x80000000);
        this.backgroundOpacitySetting = new NumberSetting("background_opacity", "Background Opacity", "Przezroczystość tła (%).", 70.0, 0.0, 100.0, 5.0);
        this.borderColorSetting = new ColorSetting("border_color", "Border Color", "Kolor obramowania.", 0x00000000);
        this.showBorderSetting = new BooleanSetting("show_border", "Show Border", "Czy wyświetlać obramowanie.", false);
        this.borderThicknessSetting = new NumberSetting("border_thickness", "Border Thickness", "Grubość ramki.", 1.0, 1.0, 4.0, 1.0);
        this.shadowSetting = new BooleanSetting("shadow", "Text Shadow", "Cień pod napisami.", true);

        this.groupAppearance.addSetting(styleSetting);
        this.groupAppearance.addSetting(textColorSetting);
        this.groupAppearance.addSetting(backgroundColorSetting);
        this.groupAppearance.addSetting(backgroundOpacitySetting);
        this.groupAppearance.addSetting(borderColorSetting);
        this.groupAppearance.addSetting(showBorderSetting);
        this.groupAppearance.addSetting(borderThicknessSetting);
        this.groupAppearance.addSetting(shadowSetting);

        // 6. Layout
        this.groupLayout = new SettingGroup("layout", "Layout & Position");
        this.anchorSetting = new EnumSetting<>("anchor", "Anchor", "Kotwica ekranu.", HudAnchor.TOP_LEFT);
        this.posXSetting = new NumberSetting("pos_x", "Position X", "Offset X.", 5.0, -2000.0, 2000.0, 1.0);
        this.posYSetting = new NumberSetting("pos_y", "Position Y", "Offset Y.", 180.0, -2000.0, 2000.0, 1.0);
        this.scaleSetting = new NumberSetting("scale", "Scale", "Skala modułu.", 1.0, 0.5, 2.5, 0.1);

        this.groupLayout.addSetting(anchorSetting);
        this.groupLayout.addSetting(posXSetting);
        this.groupLayout.addSetting(posYSetting);
        this.groupLayout.addSetting(scaleSetting);

        registerGroup(groupGeneral);
        registerGroup(groupEvents);
        registerGroup(groupConfidence);
        registerGroup(groupDisplay);
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

    public Set<ItemEventType> getAllowedEventTypes() {
        Set<ItemEventType> set = EnumSet.noneOf(ItemEventType.class);
        if (trackCollectedSetting.getValue()) {
            set.add(ItemEventType.COLLECTED);
            set.add(ItemEventType.PICKED_UP);
        }
        if (trackUsedSetting.getValue()) set.add(ItemEventType.USED);
        if (trackConsumedSetting.getValue()) set.add(ItemEventType.CONSUMED);
        if (trackDroppedSetting.getValue()) set.add(ItemEventType.DROPPED);
        if (trackCraftedSetting.getValue()) set.add(ItemEventType.CRAFTED);
        if (trackPlacedSetting.getValue()) set.add(ItemEventType.PLACED);
        if (trackUnknownChangesSetting.getValue()) set.add(ItemEventType.UNKNOWN_CHANGE);
        return set;
    }

    public Set<EventConfidence> getAllowedConfidences() {
        Set<EventConfidence> set = EnumSet.noneOf(EventConfidence.class);
        if (showConfirmedSetting.getValue()) set.add(EventConfidence.CONFIRMED);
        if (showObservedSetting.getValue()) set.add(EventConfidence.OBSERVED);
        if (showInferredSetting.getValue()) set.add(EventConfidence.INFERRED);
        if (showUnknownSetting.getValue()) set.add(EventConfidence.UNKNOWN);
        return set;
    }

    public List<String> buildDisplayLines(ItemTrackerManager manager, boolean inGame) {
        List<String> lines = new ArrayList<>();

        if (!inGame || manager == null) {
            // Preview Fallback
            if (showHeaderSetting.getValue()) {
                lines.add("Item Tracker [PREVIEW]");
            }
            lines.add("+4 Ender Pearls — Collected (Confirmed)");
            lines.add("-1 Golden Apple — Consumed (Confirmed)");
            lines.add("-32 Oak Planks — Placed (Confirmed)");
            lines.add("+2 Obsidian — Unknown Change (Unknown)");
            return lines;
        }

        ItemTrackingSession session = manager.getCurrentSession();
        if (session == null) {
            if (showHeaderSetting.getValue()) {
                lines.add("Item Tracker (No Active Session)");
            }
            return lines;
        }

        ItemTrackerLayout layout = layoutSetting.getValue();

        if (layout == ItemTrackerLayout.STATISTICS) {
            if (showHeaderSetting.getValue()) {
                lines.add("Item Tracker Stats");
            }
            Map<String, ItemTrackingStatistics> allStats = session.getAllStatistics();
            if (allStats.isEmpty()) {
                lines.add("No tracked changes yet");
            } else {
                for (ItemTrackingStatistics s : allStats.values()) {
                    boolean confOnly = confirmedStatisticsOnlySetting.getValue();
                    int collected = confOnly ? s.getConfirmedInflow() : s.getTotalInflow();
                    int used = confOnly ? s.getConfirmedOutflow() : s.getTotalOutflow();
                    lines.add(s.getItemName() + ": +" + collected + " / -" + used);
                }
            }
            return lines;
        }

        // Feed layout
        if (showHeaderSetting.getValue()) {
            lines.add("Item Tracker" + (session.isPaused() ? " [PAUSED]" : ""));
        }

        int maxEvents = maxEventsDisplayedSetting.getValue().intValue();
        boolean timestamps = showTimestampsSetting.getValue();
        boolean confidence = showConfidenceLabelSetting.getValue();

        Set<ItemEventType> types = getAllowedEventTypes();
        Set<EventConfidence> confs = getAllowedConfidences();

        List<ItemTrackEvent> filtered = session.getHistory().getFilteredEvents(types, confs);
        int count = Math.min(maxEvents, filtered.size());

        if (count == 0) {
            lines.add("Waiting for item changes...");
        } else {
            List<ItemTrackEvent> recent = filtered.subList(filtered.size() - count, filtered.size());
            for (int i = recent.size() - 1; i >= 0; i--) {
                lines.add(recent.get(i).getFeedDisplayLine(timestamps, confidence));
            }
        }

        return lines;
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible()) return;

        ItemTrackerManager manager = ItemTrackerManager.getInstance();
        boolean inGame = InventoryDataProviderRegistry.getInstance().getProvider().isPlayerAvailable();

        List<String> lines = buildDisplayLines(manager, inGame);
        if (lines.isEmpty()) return;

        int screenWidth = renderer.getScreenWidth();
        int screenHeight = renderer.getScreenHeight();
        float txtScale = scaleSetting.getValue().floatValue();
        boolean shadow = shadowSetting.getValue();

        int maxTextWidth = 0;
        for (String line : lines) {
            maxTextWidth = Math.max(maxTextWidth, (int) Math.ceil(renderer.getTextWidth(line) * txtScale));
        }

        int paddingH = 6;
        int paddingV = 4;
        int lineHeight = (int) Math.ceil(renderer.getFontHeight() * txtScale) + 2;
        int contentWidth = maxTextWidth + (paddingH * 2);
        int contentHeight = (lines.size() * lineHeight) + (paddingV * 2);

        model.setWidth(contentWidth);
        model.setHeight(contentHeight);
        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.clampToScreen(screenWidth, screenHeight);

        int renderX = model.getScreenX(screenWidth);
        int renderY = model.getScreenY(screenHeight);

        // Render Background
        HudComponentRenderer.renderBackground(
                renderer,
                renderX, renderY, contentWidth, contentHeight,
                styleSetting.getValue(),
                backgroundColorSetting.getValue(),
                backgroundOpacitySetting.getValue(),
                borderColorSetting.getValue(),
                showBorderSetting.getValue(),
                borderThicknessSetting.getValue().intValue()
        );

        int curY = renderY + paddingV;
        int defaultColor = textColorSetting.getValue();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int color = defaultColor;

            if (line.startsWith("+")) {
                color = 0xFF55FF55; // green for gained
            } else if (line.startsWith("-")) {
                color = 0xFFFF5555; // red for spent
            } else if (line.contains("Unknown Change")) {
                color = 0xFFAAAAAA; // gray for unknown
            } else if (i == 0 && showHeaderSetting.getValue()) {
                color = 0xFF55FFFF; // aqua header
            }

            renderer.drawText(line, renderX + paddingH, curY, color, shadow, txtScale, (i == 0 && showHeaderSetting.getValue()));
            curY += lineHeight;
        }
    }
}
