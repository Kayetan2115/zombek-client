package pl.zombek.client.core.modules.hud.daycounter;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.time.ITimeDataProvider;
import pl.zombek.client.core.time.ZombekTimeService;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal Day Counter HUD module calculating and rendering Minecraft world day,
 * total world age, and session duration without ever fabricating client-side time data.
 */
public class DayCounterModule extends AbstractModule implements IHudElement {

    public static final String ID = "day_counter";

    private final HudComponentModel model;

    // General Group
    private final EnumSetting<DayCounterMode> counterModeSetting;
    private final BooleanSetting showDayNumberSetting;
    private final BooleanSetting showWorldAgeSetting;
    private final BooleanSetting showRemainingTimeSetting;
    private final BooleanSetting showDimensionSetting;
    private final NumberSetting prioritySetting;

    // Format Group
    private final EnumSetting<DayDisplayFormat> displayFormatSetting;
    private final StringSetting customPrefixSetting;
    private final StringSetting customSuffixSetting;

    // Behavior Group
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting trackPerWorldSetting;
    private final BooleanSetting trackPerDimensionSetting;
    private final BooleanSetting detectTimeResetSetting;
    private final BooleanSetting showUnknownStateSetting;
    private final NumberSetting manualOffsetSetting;

    // Appearance Group
    private final EnumSetting<HudComponentStyle> styleSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting labelColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting borderColorSetting;
    private final BooleanSetting showBorderSetting;
    private final NumberSetting borderThicknessSetting;
    private final BooleanSetting shadowSetting;

    // Layout Group
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting posXSetting;
    private final NumberSetting posYSetting;
    private final NumberSetting scaleSetting;

    public DayCounterModule() {
        super(ID, "Day Counter", "Śledzi i wyświetla numer dnia w świecie gry Minecraft oraz wiek świata.", ModuleCategory.HUD);

        this.model = new HudComponentModel(ID, 70, 18, HudAnchor.TOP_RIGHT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_DAY_COUNTER);

        // 1. General Group
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        this.counterModeSetting = generalGroup.addSetting(new EnumSetting<>("counter_mode", "Tryb licznika", "Wybór trybu wyświetlania dni", DayCounterMode.WORLD_DAY));
        this.showDayNumberSetting = generalGroup.addSetting(new BooleanSetting("show_day_number", "Pokaż numer dnia", "Wyświetla numer dnia", true));
        this.showWorldAgeSetting = generalGroup.addSetting(new BooleanSetting("show_world_age", "Pokaż wiek świata", "Wyświetla łączny czas trwania świata w dniach i godzinach", false));
        this.showRemainingTimeSetting = generalGroup.addSetting(new BooleanSetting("show_remaining_time", "Pokaż czas do nocy/dnia", "Czas pozostały do kolejnej fazy", false));
        this.showDimensionSetting = generalGroup.addSetting(new BooleanSetting("show_dimension", "Pokaż wymiar", "Nazwa bieżącego wymiaru", false));
        this.prioritySetting = generalGroup.addSetting(new NumberSetting("render_priority", "Priorytet renderowania", "Priorytet w resolverze kolizji", (double) HudPriorityResolver.PRIORITY_DAY_COUNTER, 0.0, 100.0, 1.0));
        registerGroup(generalGroup);

        // 2. Format Group
        SettingGroup formatGroup = new SettingGroup("format", "Formatowanie");
        this.displayFormatSetting = formatGroup.addSetting(new EnumSetting<>("display_format", "Format wyświetlania", "Wybór stylu zapisu dnia", DayDisplayFormat.DAY_SPACE));
        this.customPrefixSetting = formatGroup.addSetting(new StringSetting("custom_prefix", "Własny prefiks", "Tekst przed numerem dnia", "Day "));
        this.customSuffixSetting = formatGroup.addSetting(new StringSetting("custom_suffix", "Własny sufiks", "Tekst za numerem dnia", ""));
        registerGroup(formatGroup);

        // 3. Behavior Group
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Zachowanie");
        this.resetOnWorldChangeSetting = behaviorGroup.addSetting(new BooleanSetting("reset_on_world_change", "Reset przy zmianie świata", "Zeruj stan po wyjściu ze świata", true));
        this.trackPerWorldSetting = behaviorGroup.addSetting(new BooleanSetting("track_per_world", "Śledź per świat", "Zachowaj historię dla poszczególnych światów", true));
        this.trackPerDimensionSetting = behaviorGroup.addSetting(new BooleanSetting("track_per_dimension", "Osobno per wymiar", "Śledzenie wymiarów niezależnie", false));
        this.detectTimeResetSetting = behaviorGroup.addSetting(new BooleanSetting("detect_time_reset", "Wykrywaj cofnięcie czasu serwera", "Oznacza cofnięcie czasu znacznikiem diagnostycznym", true));
        this.showUnknownStateSetting = behaviorGroup.addSetting(new BooleanSetting("show_unknown_state", "Pokaż N/A przy braku danych", "Wyświetla N/A gdy serwer nie przesyła czasu", true));
        this.manualOffsetSetting = behaviorGroup.addSetting(new NumberSetting("manual_offset", "Ręczna korekta dni (Manual Offset)", "Ręczne przesunięcie numeru dnia", 0.0, -100000.0, 100000.0, 1.0));
        registerGroup(behaviorGroup);

        // 4. Appearance Group
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        this.styleSetting = appearanceGroup.addSetting(new EnumSetting<>("style", "Styl", "Styl obramowania i tła", HudComponentStyle.CLASSIC));
        this.textColorSetting = appearanceGroup.addSetting(new ColorSetting("text_color", "Kolor tekstu", "Kolor wartości dnia", 0xFFFFFFFF));
        this.labelColorSetting = appearanceGroup.addSetting(new ColorSetting("label_color", "Kolor etykiety", "Kolor prefiksu i etykiet", 0xFFFFD700));
        this.backgroundColorSetting = appearanceGroup.addSetting(new ColorSetting("background_color", "Kolor tła", "Własny kolor tła", 0x80000000));
        this.backgroundOpacitySetting = appearanceGroup.addSetting(new NumberSetting("background_opacity", "Przezroczystość tła", "Stopień krycia tła", 50.0, 0.0, 100.0, 5.0));
        this.borderColorSetting = appearanceGroup.addSetting(new ColorSetting("border_color", "Kolor ramki", "Kolor obramowania", 0x40FFFFFF));
        this.showBorderSetting = appearanceGroup.addSetting(new BooleanSetting("show_border", "Pokaż obramowanie", "Rysuje ramkę wokół komponentu", true));
        this.borderThicknessSetting = appearanceGroup.addSetting(new NumberSetting("border_thickness", "Grubość ramki", "Grubość obramowania w pikselach", 1.0, 1.0, 4.0, 1.0));
        this.shadowSetting = appearanceGroup.addSetting(new BooleanSetting("shadow", "Cień tekstu", "Włącza cień pod tekstem", true));
        registerGroup(appearanceGroup);

        // 5. Layout Group
        SettingGroup layoutGroup = new SettingGroup("layout", "Pozycja");
        this.anchorSetting = layoutGroup.addSetting(new EnumSetting<>("anchor", "Zakotwiczenie", "Punkt odniesienia pozycji", HudAnchor.TOP_RIGHT));
        this.posXSetting = layoutGroup.addSetting(new NumberSetting("pos_x", "Pozycja X", "Przesunięcie w poziomie", 5.0, 0.0, 1000.0, 1.0));
        this.posYSetting = layoutGroup.addSetting(new NumberSetting("pos_y", "Pozycja Y", "Przesunięcie w pionie", 26.0, 0.0, 1000.0, 1.0));
        this.scaleSetting = layoutGroup.addSetting(new NumberSetting("scale", "Skala", "Współczynnik wielkości", 1.0, 0.5, 2.0, 0.1));
        registerGroup(layoutGroup);
    }

    @Override
    public String getElementId() {
        return getId();
    }

    @Override
    public boolean isVisible() {
        return isEnabled() && !hasError();
    }

    public HudComponentModel getHudModel() {
        model.setAnchor(anchorSetting.getValue());
        model.setPosX(posXSetting.getValue().intValue());
        model.setPosY(posYSetting.getValue().intValue());
        model.setScale(scaleSetting.getValue());
        model.setRenderPriority(prioritySetting.getValue().intValue());
        model.setVisible(isVisible());
        return model;
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible()) return;

        ZombekTimeService timeService = ZombekTimeService.getInstance();
        ITimeDataProvider provider = timeService.getTimeDataProvider();
        boolean inGame = provider != null && provider.isWorldLoaded();

        List<String> lines = buildDisplayLines(timeService, provider, inGame);
        if (lines.isEmpty()) return;

        int screenWidth = renderer.getScreenWidth();
        int screenHeight = renderer.getScreenHeight();
        float txtScale = scaleSetting.getValue().floatValue();

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

        // Render Lines
        boolean shadow = shadowSetting.getValue();
        int curY = renderY + paddingV;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int color = (i == 0) ? textColorSetting.getValue() : labelColorSetting.getValue();
            renderer.drawText(line, renderX + paddingH, curY, color, shadow, txtScale, false);
            curY += lineHeight;
        }
    }

    public List<String> buildDisplayLines(ZombekTimeService timeService, ITimeDataProvider provider, boolean inGame) {
        List<String> lines = new ArrayList<>();

        if (!inGame || provider == null || !provider.isTimeAvailable()) {
            if (showUnknownStateSetting.getValue()) {
                if (!inGame) {
                    lines.add("Day 127 [PREVIEW]");
                } else {
                    lines.add("Day: N/A");
                }
            }
            return lines;
        }

        long gameTime = provider.getWorldGameTime();
        long dayTime = provider.getWorldTimeOfDay();

        if (gameTime < 0) {
            if (showUnknownStateSetting.getValue()) {
                lines.add("Day: N/A");
            }
            return lines;
        }

        long offset = manualOffsetSetting.getValue().longValue();
        long dayNumber = ZombekTimeService.calculateWorldDay(gameTime, offset);

        String manualLabel = (offset != 0) ? " (Manual)" : "";
        String resetFlag = "";
        if (detectTimeResetSetting.getValue() && timeService.isTimeResetDetected()) {
            resetFlag = " [Reset]";
        }

        DayCounterMode mode = counterModeSetting.getValue();
        DayDisplayFormat fmt = displayFormatSetting.getValue();

        switch (mode) {
            case WORLD_DAY:
                String formatted = fmt.format(dayNumber, customPrefixSetting.getValue(), customSuffixSetting.getValue());
                lines.add(formatted + manualLabel + resetFlag);
                break;
            case WORLD_AGE:
                lines.add("Age: " + ZombekTimeService.formatWorldAge(gameTime) + manualLabel + resetFlag);
                break;
            case SESSION_DAY:
                long sessionSeconds = timeService.getWorldSessionSeconds();
                long sessionDay = (sessionSeconds / 1200L) + 1L;
                lines.add("Session Day: " + sessionDay);
                break;
            case CUSTOM:
                if (showDayNumberSetting.getValue()) {
                    lines.add(fmt.format(dayNumber, customPrefixSetting.getValue(), customSuffixSetting.getValue()) + manualLabel + resetFlag);
                }
                if (showWorldAgeSetting.getValue()) {
                    lines.add("Age: " + ZombekTimeService.formatWorldAge(gameTime));
                }
                break;
        }

        if (showRemainingTimeSetting.getValue() && dayTime >= 0) {
            long normDay = Math.floorMod(dayTime, 24000L);
            if (normDay < 12000L) {
                long leftTicks = 12000L - normDay;
                long mins = (leftTicks * 60L) / 12000L;
                lines.add("Noc za: " + mins + "m");
            } else {
                long leftTicks = 24000L - normDay;
                long mins = (leftTicks * 60L) / 12000L;
                lines.add("Dzień za: " + mins + "m");
            }
        }

        if (showDimensionSetting.getValue()) {
            String dim = provider.getDimensionId();
            if (dim != null) {
                if (dim.contains("nether")) lines.add("Nether");
                else if (dim.contains("end")) lines.add("The End");
                else lines.add("Overworld");
            }
        }

        return lines;
    }

    public DayCounterMode getCounterMode() {
        return counterModeSetting.getValue();
    }
}
