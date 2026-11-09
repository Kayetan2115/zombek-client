package pl.zombek.client.core.modules.hud.stopwatch;

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
import pl.zombek.client.core.time.StopwatchLap;
import pl.zombek.client.core.time.StopwatchProfile;
import pl.zombek.client.core.time.StopwatchProfileType;
import pl.zombek.client.core.time.ZombekTimeService;

import java.util.ArrayList;
import java.util.List;

/**
 * High-precision Stopwatch HUD module using monotonic nanosecond timing,
 * supporting named task profiles, keybind controls, and an advanced lap tracking system.
 */
public class StopwatchModule extends AbstractModule implements IHudElement {

    public static final String ID = "stopwatch";

    private final HudComponentModel model;

    // 1. General Group
    private final EnumSetting<StopwatchProfileType> profileSetting;
    private final BooleanSetting autoStartSetting;
    private final BooleanSetting autoPauseSetting;
    private final BooleanSetting autoSaveSetting;
    private final BooleanSetting preserveOnWorldChangeSetting;
    private final BooleanSetting preserveOnDeathSetting;
    private final BooleanSetting preserveOnDisconnectSetting;
    private final NumberSetting prioritySetting;

    // 2. Display Group
    private final BooleanSetting showMillisSetting;
    private final BooleanSetting showHoursSetting;
    private final BooleanSetting showLapsSetting;
    private final BooleanSetting showBestLapSetting;
    private final BooleanSetting showAvgLapSetting;
    private final NumberSetting maxVisibleLapsSetting;

    // 3. Behavior Group
    private final BooleanSetting pauseGuiOpenSetting;
    private final BooleanSetting pauseMinimizedSetting;
    private final BooleanSetting resetOnWorldChangeSetting;
    private final BooleanSetting persistStateSetting;

    // 4. Keybinds Group
    private final KeybindSetting startPauseKeySetting;
    private final KeybindSetting lapKeySetting;
    private final KeybindSetting resetKeySetting;

    // 5. Appearance Group
    private final EnumSetting<HudComponentStyle> styleSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting timerColorSetting;
    private final ColorSetting lapColorSetting;
    private final ColorSetting backgroundColorSetting;
    private final NumberSetting backgroundOpacitySetting;
    private final ColorSetting borderColorSetting;
    private final BooleanSetting showBorderSetting;
    private final NumberSetting borderThicknessSetting;
    private final BooleanSetting shadowSetting;

    // 6. Layout Group
    private final EnumSetting<HudAnchor> anchorSetting;
    private final NumberSetting posXSetting;
    private final NumberSetting posYSetting;
    private final NumberSetting scaleSetting;

    public StopwatchModule() {
        super(ID, "Stopwatch", "Niezależny stoper monotoniczny z obsługą okrążeń (Laps) i profilami zadań.", ModuleCategory.HUD);

        this.model = new HudComponentModel(ID, 95, 20, HudAnchor.BOTTOM_RIGHT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_STOPWATCH);

        // 1. General Group
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        this.profileSetting = generalGroup.addSetting(new EnumSetting<>("active_profile", "Aktywny profil", "Wybór profilu stopera", StopwatchProfileType.SPEEDRUN));
        this.autoStartSetting = generalGroup.addSetting(new BooleanSetting("auto_start", "Auto Start po wejściu do świata", "Automatyczne uruchomienie po dołączeniu", false));
        this.autoPauseSetting = generalGroup.addSetting(new BooleanSetting("auto_pause", "Auto Pauza po wyjściu ze świata", "Automatyczne wstrzymanie po wyjściu", false));
        this.autoSaveSetting = generalGroup.addSetting(new BooleanSetting("auto_save", "Automatyczny zapis stanu", "Zapisywanie stanu w time_data.json", true));
        this.preserveOnWorldChangeSetting = generalGroup.addSetting(new BooleanSetting("preserve_on_world_change", "Zachowaj stan przy zmianie świata", "Nie zeruj czasu przy zmianie wymiaru/świata", true));
        this.preserveOnDeathSetting = generalGroup.addSetting(new BooleanSetting("preserve_on_death", "Zachowaj stan po śmierci", "Kontynuuj odmierzanie po śmierci gracza", true));
        this.preserveOnDisconnectSetting = generalGroup.addSetting(new BooleanSetting("preserve_on_disconnect", "Zachowaj stan po rozłączeniu", "Nie zeruj czasu po rozłączeniu", true));
        this.prioritySetting = generalGroup.addSetting(new NumberSetting("render_priority", "Priorytet renderowania", "Priorytet w resolverze kolizji", (double) HudPriorityResolver.PRIORITY_STOPWATCH, 0.0, 100.0, 1.0));
        registerGroup(generalGroup);

        // 2. Display Group
        SettingGroup displayGroup = new SettingGroup("display", "Wyświetlanie");
        this.showMillisSetting = displayGroup.addSetting(new BooleanSetting("show_millis", "Pokaż milisekundy", "Wyświetla setne części sekundy", true));
        this.showHoursSetting = displayGroup.addSetting(new BooleanSetting("show_hours", "Pokaż godziny", "Wymusza format z godzinami", false));
        this.showLapsSetting = displayGroup.addSetting(new BooleanSetting("show_laps", "Pokaż historię okrążeń", "Wyświetla listę ostatnich okrążeń", true));
        this.showBestLapSetting = displayGroup.addSetting(new BooleanSetting("show_best_lap", "Pokaż najlepsze okrążenie", "Wyświetla czas najszybszego okrążenia", false));
        this.showAvgLapSetting = displayGroup.addSetting(new BooleanSetting("show_avg_lap", "Pokaż średni czas okrążenia", "Wyświetla średni czas okrążeń", false));
        this.maxVisibleLapsSetting = displayGroup.addSetting(new NumberSetting("max_visible_laps", "Maksymalna liczba widocznych okrążeń", "Limit wierszy okrążeń na ekranie", 3.0, 1.0, 10.0, 1.0));
        registerGroup(displayGroup);

        // 3. Behavior Group
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Zachowanie");
        this.pauseGuiOpenSetting = behaviorGroup.addSetting(new BooleanSetting("pause_gui_open", "Pauza po otwarciu GUI", "Wstrzymuje licznik po otwarciu menu gry", false));
        this.pauseMinimizedSetting = behaviorGroup.addSetting(new BooleanSetting("pause_minimized", "Pauza po minimalizacji okna", "Wstrzymuje licznik po zminimalizowaniu gry", false));
        this.resetOnWorldChangeSetting = behaviorGroup.addSetting(new BooleanSetting("reset_on_world_change", "Reset przy zmianie świata", "Zeruje stoper przy zmianie świata", false));
        this.persistStateSetting = behaviorGroup.addSetting(new BooleanSetting("persist_state", "Zapamiętaj stan między restartami", "Zapisuje czas stopera na dysku", true));
        registerGroup(behaviorGroup);

        // 4. Keybinds Group
        SettingGroup keybindsGroup = new SettingGroup("keybinds", "Skróty klawiszowe");
        this.startPauseKeySetting = keybindsGroup.addSetting(new KeybindSetting("key_start_pause", "Start / Pauza", "Klawisz uruchomienia/wstrzymania", -1));
        this.lapKeySetting = keybindsGroup.addSetting(new KeybindSetting("key_lap", "Okrążenie (Lap)", "Klawisz rejestracji okrążenia", -1));
        this.resetKeySetting = keybindsGroup.addSetting(new KeybindSetting("key_reset", "Reset (wymaga potwierdzenia)", "Klawisz wyzerowania stopera", -1));
        registerGroup(keybindsGroup);

        // 5. Appearance Group
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        this.styleSetting = appearanceGroup.addSetting(new EnumSetting<>("style", "Styl", "Styl obramowania i tła", HudComponentStyle.CLASSIC));
        this.textColorSetting = appearanceGroup.addSetting(new ColorSetting("text_color", "Kolor etykiet", "Kolor nazwy profilu", 0xFFFFFFFF));
        this.timerColorSetting = appearanceGroup.addSetting(new ColorSetting("timer_color", "Kolor stopera", "Kolor cyfr stopera", 0xFF00E5FF));
        this.lapColorSetting = appearanceGroup.addSetting(new ColorSetting("lap_color", "Kolor okrążeń", "Kolor wierszy okrążeń", 0xFFAAAAAA));
        this.backgroundColorSetting = appearanceGroup.addSetting(new ColorSetting("background_color", "Kolor tła", "Własny kolor tła", 0x80000000));
        this.backgroundOpacitySetting = appearanceGroup.addSetting(new NumberSetting("background_opacity", "Przezroczystość tła", "Stopień krycia tła", 50.0, 0.0, 100.0, 5.0));
        this.borderColorSetting = appearanceGroup.addSetting(new ColorSetting("border_color", "Kolor ramki", "Kolor obramowania", 0x40FFFFFF));
        this.showBorderSetting = appearanceGroup.addSetting(new BooleanSetting("show_border", "Pokaż obramowanie", "Rysuje ramkę wokół komponentu", true));
        this.borderThicknessSetting = appearanceGroup.addSetting(new NumberSetting("border_thickness", "Grubość ramki", "Grubość obramowania w pikselach", 1.0, 1.0, 4.0, 1.0));
        this.shadowSetting = appearanceGroup.addSetting(new BooleanSetting("shadow", "Cień tekstu", "Włącza cień pod tekstem", true));
        registerGroup(appearanceGroup);

        // 6. Layout Group
        SettingGroup layoutGroup = new SettingGroup("layout", "Pozycja");
        this.anchorSetting = layoutGroup.addSetting(new EnumSetting<>("anchor", "Zakotwiczenie", "Punkt odniesienia pozycji", HudAnchor.BOTTOM_RIGHT));
        this.posXSetting = layoutGroup.addSetting(new NumberSetting("pos_x", "Pozycja X", "Przesunięcie w poziomie", 10.0, 0.0, 1000.0, 1.0));
        this.posYSetting = layoutGroup.addSetting(new NumberSetting("pos_y", "Pozycja Y", "Przesunięcie w pionie", 30.0, 0.0, 1000.0, 1.0));
        this.scaleSetting = layoutGroup.addSetting(new NumberSetting("scale", "Skala", "Współczynnik wielkości", 1.0, 0.5, 2.0, 0.1));
        registerGroup(layoutGroup);
    }

    public StopwatchProfile getActiveProfile() {
        String name = profileSetting.getValue().name();
        ZombekTimeService svc = ZombekTimeService.getInstance();
        StopwatchProfile profile = svc.getStopwatchProfile(name);
        if (profile == null) {
            profile = svc.getActiveStopwatchProfile();
        }
        return profile;
    }

    public void start() {
        StopwatchProfile p = getActiveProfile();
        if (p != null) p.start();
    }

    public void pause() {
        StopwatchProfile p = getActiveProfile();
        if (p != null) p.pause();
    }

    public void toggle() {
        StopwatchProfile p = getActiveProfile();
        if (p != null) p.toggle();
    }

    public void reset() {
        StopwatchProfile p = getActiveProfile();
        if (p != null) p.reset();
    }

    public StopwatchLap recordLap() {
        StopwatchProfile p = getActiveProfile();
        return p != null ? p.recordLap() : null;
    }

    public void clearLaps() {
        StopwatchProfile p = getActiveProfile();
        if (p != null) p.clearLaps();
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

        StopwatchProfile profile = getActiveProfile();
        if (profile == null) return;

        List<String> lines = buildDisplayLines(profile);
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
            int color;
            if (i == 0) {
                color = timerColorSetting.getValue();
            } else if (line.startsWith("LAP")) {
                color = lapColorSetting.getValue();
            } else {
                color = textColorSetting.getValue();
            }
            renderer.drawText(line, renderX + paddingH, curY, color, shadow, txtScale, false);
            curY += lineHeight;
        }
    }

    public List<String> buildDisplayLines(StopwatchProfile profile) {
        List<String> lines = new ArrayList<>();
        boolean showMillis = showMillisSetting.getValue();
        boolean showHours = showHoursSetting.getValue();

        if (profile.isStopped() && profile.getElapsedNanos() == 0L) {
            lines.add("00:35.21 [PREVIEW]");
            if (showLapsSetting.getValue()) {
                lines.add("LAP 1 — 00:35.21 (±0.00)");
            }
            return lines;
        }

        String profileName = profile.getName();
        String timeStr = profile.getFormattedElapsed(showMillis, showHours);
        String stateIndicator = profile.isPaused() ? " [PAUSED]" : (profile.isStopped() ? " [STOPPED]" : "");
        lines.add(profileName + ": " + timeStr + stateIndicator);

        if (showBestLapSetting.getValue()) {
            StopwatchLap best = profile.getBestLap();
            if (best != null) {
                lines.add("Best: " + best.getFormattedLapTime(showMillis, showHours));
            }
        }
        if (showAvgLapSetting.getValue()) {
            long avgNanos = profile.getAverageLapNanos();
            if (avgNanos > 0) {
                lines.add("Avg: " + StopwatchLap.formatNanos(avgNanos, showMillis, showHours));
            }
        }

        if (showLapsSetting.getValue()) {
            List<StopwatchLap> allLaps = profile.getLaps();
            if (!allLaps.isEmpty()) {
                int maxVisible = maxVisibleLapsSetting.getValue().intValue();
                int startIndex = Math.max(0, allLaps.size() - maxVisible);
                for (int i = startIndex; i < allLaps.size(); i++) {
                    StopwatchLap lap = allLaps.get(i);
                    String lapStr = String.format("LAP %d — %s (%s)",
                            lap.getLapNumber(),
                            lap.getFormattedLapTime(showMillis, showHours),
                            lap.getFormattedDelta()
                    );
                    lines.add(lapStr);
                }
            }
        }

        return lines;
    }
}
