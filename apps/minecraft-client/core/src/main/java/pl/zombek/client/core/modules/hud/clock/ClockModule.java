package pl.zombek.client.core.modules.hud.clock;

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
import pl.zombek.client.core.time.TimeOfDayPhase;
import pl.zombek.client.core.time.ZombekTimeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Universal Clock HUD Module displaying real-world system time,
 * Minecraft in-game world time, or a synchronized dual-clock layout.
 */
public class ClockModule extends AbstractModule implements IHudElement {

    public static final String ID = "clock";

    private final HudComponentModel model;

    // General Settings
    private final EnumSetting<ClockMode> clockModeSetting;
    private final BooleanSetting showRealTimeSetting;
    private final BooleanSetting showMinecraftTimeSetting;
    private final BooleanSetting showDateSetting;
    private final BooleanSetting showDayOfWeekSetting;
    private final BooleanSetting showSecondsSetting;
    private final NumberSetting prioritySetting;

    // Format Settings
    private final BooleanSetting is24HourSetting;
    private final EnumSetting<DateFormatPattern> dateFormatSetting;
    private final BooleanSetting showAmPmSetting;
    private final BooleanSetting leadingZeroSetting;

    // Minecraft Time Settings
    private final BooleanSetting showDayNightIconSetting;
    private final BooleanSetting showSunriseIndicatorSetting;
    private final BooleanSetting showSunsetIndicatorSetting;
    private final BooleanSetting showPhaseNameSetting;
    private final BooleanSetting showWorldTimeSetting;

    // Appearance Settings
    private final EnumSetting<HudComponentStyle> styleSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting secondaryColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting borderColorSetting;
    private final BooleanSetting showBorderSetting;
    private final NumberSetting borderThicknessSetting;
    private final BooleanSetting shadowSetting;

    // Layout Settings
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting posXSetting;
    private final NumberSetting posYSetting;
    private final NumberSetting scaleSetting;

    public ClockModule() {
        super(ID, "Clock", "Wyświetla czas rzeczywisty oraz czas w świecie gry Minecraft.", ModuleCategory.HUD);

        this.model = new HudComponentModel(ID, 80, 18, HudAnchor.TOP_RIGHT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_CLOCK);

        // 1. General Group
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        this.clockModeSetting = generalGroup.addSetting(new EnumSetting<>("clock_mode", "Tryb zegara", "Wybór trybu wyświetlania czasu", ClockMode.DIGITAL));
        this.showRealTimeSetting = generalGroup.addSetting(new BooleanSetting("show_real_time", "Pokaż czas rzeczywisty", "Wyświetla czas systemowy", true));
        this.showMinecraftTimeSetting = generalGroup.addSetting(new BooleanSetting("show_minecraft_time", "Pokaż czas Minecraft", "Wyświetla czas w świecie gry", false));
        this.showDateSetting = generalGroup.addSetting(new BooleanSetting("show_date", "Pokaż datę", "Wyświetla bieżącą datę", false));
        this.showDayOfWeekSetting = generalGroup.addSetting(new BooleanSetting("show_day_of_week", "Pokaż dzień tygodnia", "Wyświetla nazwę dnia tygodnia", false));
        this.showSecondsSetting = generalGroup.addSetting(new BooleanSetting("show_seconds", "Pokaż sekundy", "Wyświetla upływ sekund", true));
        this.prioritySetting = generalGroup.addSetting(new NumberSetting("render_priority", "Priorytet renderowania", "Priorytet w resolverze kolizji", (double) HudPriorityResolver.PRIORITY_CLOCK, 0.0, 100.0, 1.0));
        registerGroup(generalGroup);

        // 2. Format Group
        SettingGroup formatGroup = new SettingGroup("format", "Formatowanie");
        this.is24HourSetting = formatGroup.addSetting(new BooleanSetting("is_24_hour", "Format 24-godzinny", "Włącza format 24h zamiast 12h AM/PM", true));
        this.dateFormatSetting = formatGroup.addSetting(new EnumSetting<>("date_format", "Format daty", "Wzór formatowania daty", DateFormatPattern.DD_MM_YYYY));
        this.showAmPmSetting = formatGroup.addSetting(new BooleanSetting("show_am_pm", "Pokaż AM / PM (12h)", "Wyświetla wskaźnik AM/PM", true));
        this.leadingZeroSetting = formatGroup.addSetting(new BooleanSetting("leading_zero", "Wiodące zero", "Dodaje wiodące zero dla godzin < 10", true));
        registerGroup(formatGroup);

        // 3. Minecraft Time Group
        SettingGroup mcGroup = new SettingGroup("minecraft_time", "Czas Minecraft");
        this.showDayNightIconSetting = mcGroup.addSetting(new BooleanSetting("show_day_night", "Ikona dnia / nocy", "Wyświetla symbol słońca lub księżyca", true));
        this.showSunriseIndicatorSetting = mcGroup.addSetting(new BooleanSetting("show_sunrise", "Wskaźnik wschodu słońca", "Wyróżnia wschód słońca", true));
        this.showSunsetIndicatorSetting = mcGroup.addSetting(new BooleanSetting("show_sunset", "Wskaźnik zachodu słońca", "Wyróżnia zachód słońca", true));
        this.showPhaseNameSetting = mcGroup.addSetting(new BooleanSetting("show_phase_name", "Nazwa pory dnia", "Wyświetla nazwę fazy (np. Południe)", true));
        this.showWorldTimeSetting = mcGroup.addSetting(new BooleanSetting("show_world_time", "Pokaż czas numeryczny", "Wyświetla czas świata liczbowo", true));
        registerGroup(mcGroup);

        // 4. Appearance Group
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        this.styleSetting = appearanceGroup.addSetting(new EnumSetting<>("style", "Styl", "Styl obramowania i tła", HudComponentStyle.CLASSIC));
        this.textColorSetting = appearanceGroup.addSetting(new ColorSetting("text_color", "Kolor tekstu", "Główny kolor tekstu", 0xFFFFFFFF));
        this.secondaryColorSetting = appearanceGroup.addSetting(new ColorSetting("secondary_color", "Kolor akcentu", "Drugorzędny kolor tekstu", 0xFFFFD700));
        this.backgroundColorSetting = appearanceGroup.addSetting(new ColorSetting("background_color", "Kolor tła", "Własny kolor tła", 0x80000000));
        this.backgroundOpacitySetting = appearanceGroup.addSetting(new NumberSetting("background_opacity", "Przezroczystość tła", "Stopień krycia tła", 50.0, 0.0, 100.0, 5.0));
        this.borderColorSetting = appearanceGroup.addSetting(new ColorSetting("border_color", "Kolor ramki", "Kolor obramowania", 0x40FFFFFF));
        this.showBorderSetting = appearanceGroup.addSetting(new BooleanSetting("show_border", "Pokaż obramowanie", "Rysuje ramkę wokół komponentu", true));
        this.borderThicknessSetting = appearanceGroup.addSetting(new NumberSetting("border_thickness", "Grubość ramki", "Grubość linii obramowania w pikselach", 1.0, 1.0, 4.0, 1.0));
        this.shadowSetting = appearanceGroup.addSetting(new BooleanSetting("shadow", "Cień tekstu", "Włącza cień pod tekstem", true));
        registerGroup(appearanceGroup);

        // 5. Layout Group
        SettingGroup layoutGroup = new SettingGroup("layout", "Pozycja");
        this.anchorSetting = layoutGroup.addSetting(new EnumSetting<>("anchor", "Zakotwiczenie", "Punkt odniesienia pozycji", HudAnchor.TOP_RIGHT));
        this.posXSetting = layoutGroup.addSetting(new NumberSetting("pos_x", "Pozycja X", "Przesunięcie w poziomie", 5.0, 0.0, 1000.0, 1.0));
        this.posYSetting = layoutGroup.addSetting(new NumberSetting("pos_y", "Pozycja Y", "Przesunięcie w pionie", 5.0, 0.0, 1000.0, 1.0));
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
            int color = (i == 0) ? textColorSetting.getValue() : secondaryColorSetting.getValue();
            renderer.drawText(line, renderX + paddingH, curY, color, shadow, txtScale, false);
            curY += lineHeight;
        }
    }

    public List<String> buildDisplayLines(ZombekTimeService timeService, ITimeDataProvider provider, boolean inGame) {
        List<String> lines = new ArrayList<>();
        ClockMode mode = clockModeSetting.getValue();

        switch (mode) {
            case DIGITAL:
                lines.add(formatRealTime(timeService, showSecondsSetting.getValue()));
                break;
            case MINIMAL:
                lines.add(formatRealTime(timeService, false));
                break;
            case DETAILED:
                lines.add(formatDateAndDayOfWeek(timeService));
                lines.add(formatRealTime(timeService, showSecondsSetting.getValue()));
                break;
            case MINECRAFT:
                lines.add(formatMinecraftWorldTime(timeService, provider, inGame));
                break;
            case DUAL:
                lines.add("Real: " + formatRealTime(timeService, showSecondsSetting.getValue()));
                lines.add("MC: " + formatMinecraftWorldTime(timeService, provider, inGame));
                break;
            case CUSTOM:
                if (showDayOfWeekSetting.getValue() || showDateSetting.getValue()) {
                    lines.add(formatCustomDateLine(timeService));
                }
                if (showRealTimeSetting.getValue()) {
                    lines.add(formatRealTime(timeService, showSecondsSetting.getValue()));
                }
                if (showMinecraftTimeSetting.getValue()) {
                    lines.add(formatMinecraftWorldTime(timeService, provider, inGame));
                }
                if (lines.isEmpty()) {
                    lines.add(formatRealTime(timeService, true));
                }
                break;
        }

        return lines;
    }

    private String formatRealTime(ZombekTimeService timeService, boolean includeSeconds) {
        LocalTime lt = timeService.getLocalTime();
        boolean is24h = is24HourSetting.getValue();
        boolean leadingZero = leadingZeroSetting.getValue();
        boolean showAmPm = showAmPmSetting.getValue();

        int hour = lt.getHour();
        int minute = lt.getMinute();
        int second = lt.getSecond();

        StringBuilder sb = new StringBuilder();
        if (is24h) {
            if (leadingZero) {
                sb.append(String.format("%02d:%02d", hour, minute));
            } else {
                sb.append(String.format("%d:%02d", hour, minute));
            }
            if (includeSeconds) {
                sb.append(String.format(":%02d", second));
            }
        } else {
            int h12 = hour % 12;
            if (h12 == 0) h12 = 12;
            String ampm = hour >= 12 ? "PM" : "AM";
            if (leadingZero) {
                sb.append(String.format("%02d:%02d", h12, minute));
            } else {
                sb.append(String.format("%d:%02d", h12, minute));
            }
            if (includeSeconds) {
                sb.append(String.format(":%02d", second));
            }
            if (showAmPm) {
                sb.append(" ").append(ampm);
            }
        }

        return sb.toString();
    }

    private String formatDateAndDayOfWeek(ZombekTimeService timeService) {
        LocalDate date = timeService.getLocalDate();
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String formattedDate = date.format(dateFormatSetting.getValue().getFormatter());
        return dayOfWeek + ", " + formattedDate;
    }

    private String formatCustomDateLine(ZombekTimeService timeService) {
        LocalDate date = timeService.getLocalDate();
        StringBuilder sb = new StringBuilder();
        if (showDayOfWeekSetting.getValue()) {
            sb.append(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
        }
        if (showDateSetting.getValue()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(date.format(dateFormatSetting.getValue().getFormatter()));
        }
        return sb.toString();
    }

    private String formatMinecraftWorldTime(ZombekTimeService timeService, ITimeDataProvider provider, boolean inGame) {
        if (!inGame || provider == null || !provider.isTimeAvailable()) {
            return "☀ 12:00 [PREVIEW]";
        }

        long ticks = provider.getWorldTimeOfDay();
        if (ticks < 0) {
            return "MC Time: N/A";
        }

        TimeOfDayPhase phase = TimeOfDayPhase.fromTicks(ticks);
        StringBuilder sb = new StringBuilder();

        if (showDayNightIconSetting.getValue()) {
            if (showSunriseIndicatorSetting.getValue() && TimeOfDayPhase.isSunrise(ticks)) {
                sb.append("🌅 ");
            } else if (showSunsetIndicatorSetting.getValue() && TimeOfDayPhase.isSunset(ticks)) {
                sb.append("🌇 ");
            } else {
                sb.append(phase.getIcon()).append(" ");
            }
        }

        if (showWorldTimeSetting.getValue()) {
            String timeStr = ZombekTimeService.formatMinecraftTime(
                    ticks,
                    is24HourSetting.getValue(),
                    showAmPmSetting.getValue(),
                    leadingZeroSetting.getValue()
            );
            sb.append(timeStr);
        }

        if (showPhaseNameSetting.getValue()) {
            if (sb.length() > 0 && !sb.toString().endsWith(" ")) {
                sb.append(" ");
            }
            sb.append("(").append(phase.getPolishName()).append(")");
        }

        return sb.toString().trim();
    }

    public ClockMode getClockMode() {
        return clockModeSetting.getValue();
    }
}
