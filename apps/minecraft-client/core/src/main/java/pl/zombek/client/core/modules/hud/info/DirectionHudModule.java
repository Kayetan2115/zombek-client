package pl.zombek.client.core.modules.hud.info;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.info.CompassDirection;
import pl.zombek.client.core.info.CompassStyle;
import pl.zombek.client.core.info.IInfoHudDataProvider;
import pl.zombek.client.core.info.InfoHudDataProviderRegistry;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.teamview.ITeamDirectionProvider;
import pl.zombek.client.core.modules.hud.teamview.TeamTracker;
import pl.zombek.client.core.modules.hud.teamview.TeammateDirectionInfo;
import pl.zombek.client.core.settings.*;

import java.util.List;
import java.util.Locale;

/**
 * Universal Direction HUD Module.
 * Displays a smooth horizontal or circular compass with cardinal / intermediate headings,
 * yaw degrees, active direction highlighting, and seamless Team View integration.
 */
public class DirectionHudModule extends AbstractModule implements IHudElement {

    // General Group
    private final EnumSetting<CompassStyle> compassStyle;
    private final BooleanSetting showCardinalDirections;
    private final BooleanSetting showIntermediateDirections;
    private final BooleanSetting showDegrees;
    private final BooleanSetting showCurrentFacing;
    private final BooleanSetting showTeammates;

    // Appearance Group
    private final EnumSetting<HudComponentStyle> componentStyle;
    private final ColorSetting activeDirectionColor;
    private final ColorSetting inactiveDirectionColor;
    private final ColorSetting cardinalColor;
    private final ColorSetting backgroundColor;
    private final NumberSetting backgroundOpacity;
    private final BooleanSetting showBorder;
    private final ColorSetting borderColor;
    private final NumberSetting borderThickness;
    private final NumberSetting scale;
    private final NumberSetting textSize;
    private final NumberSetting markerSize;
    private final NumberSetting compassWidth;

    // Behavior Group
    private final BooleanSetting smoothMovement;
    private final NumberSetting interpolationSpeed;
    private final BooleanSetting hideInGui;
    private final BooleanSetting hideInSpectator;

    // Position Group
    private final EnumSetting<HudAnchor> anchor;
    private final NumberSetting positionX;
    private final NumberSetting positionY;

    private final HudComponentModel model;
    private float smoothedYaw = 0.0f;
    private long lastRenderTime = 0L;

    public DirectionHudModule() {
        super("direction_hud", "Direction HUD", "Wyświetla płynny kompas z kierunkiem świata, kątem yaw i wskaźnikami drużyny.", ModuleCategory.HUD);

        this.model = new HudComponentModel("direction_hud", 160, 28, HudAnchor.TOP_LEFT);

        // --- 1. General Settings ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        compassStyle = generalGroup.addSetting(new EnumSetting<>(
                "style", "Styl kompasu", "Układ prezentacji kierunku",
                CompassStyle.CLASSIC));
        showCardinalDirections = generalGroup.addSetting(new BooleanSetting(
                "show_cardinal", "Kierunki główne", "Wyświetla N, S, E, W", true));
        showIntermediateDirections = generalGroup.addSetting(new BooleanSetting(
                "show_intermediate", "Kierunki pośrednie", "Wyświetla NE, NW, SE, SW", true));
        showDegrees = generalGroup.addSetting(new BooleanSetting(
                "show_degrees", "Pokaż stopnie", "Wyświetla dokładną wartość kąta yaw", true));
        showCurrentFacing = generalGroup.addSetting(new BooleanSetting(
                "show_current_facing", "Aktualny kierunek", "Wyświetla nazwę aktualnego kierunku", true));
        showTeammates = generalGroup.addSetting(new BooleanSetting(
                "show_teammates", "Znaczniki sojuszników", "Wyświetla pozycje sojuszników z Team View", true));
        registerGroup(generalGroup);

        // --- 2. Appearance Settings ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        componentStyle = appearanceGroup.addSetting(new EnumSetting<>(
                "box_style", "Styl komponentu", "Styl tła i ramki",
                HudComponentStyle.CLASSIC));
        activeDirectionColor = appearanceGroup.addSetting(new ColorSetting(
                "active_color", "Kolor aktywnego kierunku", "Podświetlenie aktualnego kierunku", 0xFFEF4444));
        inactiveDirectionColor = appearanceGroup.addSetting(new ColorSetting(
                "inactive_color", "Kolor nieaktywnych kierunków", "Kolor pozostałych oznaczeń", 0xFFA0AEC0));
        cardinalColor = appearanceGroup.addSetting(new ColorSetting(
                "cardinal_color", "Kolor kierunków głównych", "Kolor liter N, S, E, W", 0xFFFFFFFF));
        backgroundColor = appearanceGroup.addSetting(new ColorSetting(
                "bg_color", "Kolor tła", "Własny kolor tła kompasu", 0xFF111827));
        backgroundOpacity = appearanceGroup.addSetting(new NumberSetting(
                "opacity", "Przezroczystość tła", "Przezroczystość tła w procentach", 65.0, 0.0, 100.0, 5.0));
        showBorder = appearanceGroup.addSetting(new BooleanSetting(
                "show_border", "Pokaż ramkę", "Rysuje obramowanie", true));
        borderColor = appearanceGroup.addSetting(new ColorSetting(
                "border_color", "Kolor ramki", "Kolor obramowania", 0x40FFFFFF));
        borderThickness = appearanceGroup.addSetting(new NumberSetting(
                "border_thickness", "Grubość ramki", "Grubość obramowania w pikselach", 1.0, 1.0, 4.0, 1.0));
        scale = appearanceGroup.addSetting(new NumberSetting(
                "scale", "Skala modułu", "Ogólne skalowanie elementu", 1.0, 0.5, 2.5, 0.1));
        textSize = appearanceGroup.addSetting(new NumberSetting(
                "text_size", "Rozmiar tekstu", "Mnożnik czcionki", 1.0, 0.5, 2.0, 0.1));
        markerSize = appearanceGroup.addSetting(new NumberSetting(
                "marker_size", "Wysokość wskaźnika", "Wysokość znacznika środkowego", 4.0, 2.0, 10.0, 1.0));
        compassWidth = appearanceGroup.addSetting(new NumberSetting(
                "compass_width", "Szerokość paska", "Szerokość paska w pikselach", 160.0, 80.0, 320.0, 10.0));
        registerGroup(appearanceGroup);

        // --- 3. Behavior Settings ---
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Zachowanie");
        smoothMovement = behaviorGroup.addSetting(new BooleanSetting(
                "smooth", "Płynny ruch", "Wygładza przesuwanie kompasu między klatkami", true));
        interpolationSpeed = behaviorGroup.addSetting(new NumberSetting(
                "interp_speed", "Prędkość interpolacji", "Szybkość płynnego śledzenia obrotu", 14.0, 2.0, 30.0, 1.0));
        hideInGui = behaviorGroup.addSetting(new BooleanSetting(
                "hide_in_gui", "Ukryj w menu", "Ukrywa kompas przy otwartych ekranach GUI", false));
        hideInSpectator = behaviorGroup.addSetting(new BooleanSetting(
                "hide_in_spectator", "Ukryj w trybie widza", "Ukrywa kompas w Spectator Mode", false));
        registerGroup(behaviorGroup);

        // --- 4. Position Group ---
        SettingGroup positionGroup = new SettingGroup("position", "Pozycja");
        anchor = positionGroup.addSetting(new EnumSetting<>(
                "anchor", "Kotwica", "Narożnik ekranu",
                HudAnchor.TOP_LEFT));
        positionX = positionGroup.addSetting(new NumberSetting(
                "pos_x", "Pozycja X", "Przesunięcie w poziomie", 120.0, 0.0, 2000.0, 1.0));
        positionY = positionGroup.addSetting(new NumberSetting(
                "pos_y", "Pozycja Y", "Przesunięcie w pionie", 4.0, 0.0, 2000.0, 1.0));
        registerGroup(positionGroup);
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
        if (renderer == null) return;
        IInfoHudDataProvider provider = InfoHudDataProviderRegistry.getInstance().getProvider();
        if (provider == null || !provider.isPlayerInGame()) return;

        float targetYaw = provider.getPlayerYaw();
        targetYaw = ((targetYaw % 360.0f) + 360.0f) % 360.0f;

        // Smooth Interpolation
        long now = System.currentTimeMillis();
        if (lastRenderTime > 0 && smoothMovement.getValue()) {
            float deltaSeconds = Math.min(0.1f, (now - lastRenderTime) / 1000.0f);
            float diff = targetYaw - smoothedYaw;
            while (diff < -180.0f) diff += 360.0f;
            while (diff > 180.0f) diff -= 360.0f;
            smoothedYaw += diff * Math.min(1.0f, (float) (interpolationSpeed.getValue() * deltaSeconds));
            smoothedYaw = ((smoothedYaw % 360.0f) + 360.0f) % 360.0f;
        } else {
            smoothedYaw = targetYaw;
        }
        lastRenderTime = now;

        CompassStyle style = compassStyle.getValue();
        switch (style) {
            case MINIMAL:
                renderMinimal(renderer, smoothedYaw);
                break;
            case DETAILED:
                renderDetailed(renderer, smoothedYaw);
                break;
            case CIRCULAR:
                renderCircular(renderer, smoothedYaw);
                break;
            case CLASSIC:
            default:
                renderClassic(renderer, smoothedYaw);
                break;
        }
    }

    /**
     * Renders Minimal single-badge heading.
     */
    private void renderMinimal(IHudRenderer renderer, float currentYaw) {
        CompassDirection dir = CompassDirection.fromMinecraftYaw(currentYaw);
        String label = dir.getCode();
        if (showDegrees.getValue()) {
            label = String.format(Locale.US, "%s %.0f°", label, currentYaw);
        }

        float s = (float) (scale.getValue() * textSize.getValue());
        int padding = 4;
        int textW = (int) Math.ceil(renderer.getTextWidth(label) * s);
        int textH = (int) Math.ceil(renderer.getFontHeight() * s);

        int totalW = textW + padding * 2;
        int totalH = textH + padding * 2;

        setupModel(totalW, totalH);
        int drawX = model.getScreenX(renderer.getScreenWidth());
        int drawY = model.getScreenY(renderer.getScreenHeight());

        drawBackground(renderer, drawX, drawY, totalW, totalH);
        renderer.drawText(label, drawX + padding, drawY + padding, activeDirectionColor.getValue(), true, s, true);
    }

    /**
     * Renders Classic horizontal scrolling compass tape.
     */
    private void renderClassic(IHudRenderer renderer, float currentYaw) {
        int width = (int) Math.ceil(compassWidth.getValue() * scale.getValue());
        int height = (int) Math.ceil(24 * scale.getValue());

        setupModel(width, height);
        int drawX = model.getScreenX(renderer.getScreenWidth());
        int drawY = model.getScreenY(renderer.getScreenHeight());

        drawBackground(renderer, drawX, drawY, width, height);

        // Center Indicator (Arrow / Marker)
        int centerX = drawX + width / 2;
        int mSize = (int) Math.ceil(markerSize.getValue() * scale.getValue());
        renderer.fill(centerX - 1, drawY + 1, 2, mSize, activeDirectionColor.getValue());

        // Field of view on tape: 180 degrees visible
        float fovDegrees = 180.0f;
        float pxPerDegree = (float) width / fovDegrees;

        // Draw directions
        float s = (float) (scale.getValue() * textSize.getValue());
        int fontH = (int) Math.ceil(renderer.getFontHeight() * s);

        for (CompassDirection dir : CompassDirection.values()) {
            if (dir.isCardinal() && !showCardinalDirections.getValue()) continue;
            if (!dir.isCardinal() && !showIntermediateDirections.getValue()) continue;

            float dirAngle = dir.getCenterAngle();
            float diff = dirAngle - currentYaw;
            while (diff < -180.0f) diff += 360.0f;
            while (diff > 180.0f) diff -= 360.0f;

            if (Math.abs(diff) <= (fovDegrees / 2.0f)) {
                int itemX = centerX + (int) (diff * pxPerDegree);
                String code = dir.getCode();
                int strW = (int) Math.ceil(renderer.getTextWidth(code) * s);
                int textX = itemX - strW / 2;
                int textY = drawY + height - fontH - 3;

                // Color based on active / cardinal / inactive
                int col = inactiveDirectionColor.getValue();
                if (Math.abs(diff) < 15.0f) {
                    col = activeDirectionColor.getValue();
                } else if (dir.isCardinal()) {
                    col = cardinalColor.getValue();
                }

                if (textX >= drawX + 2 && textX + strW <= drawX + width - 2) {
                    renderer.drawText(code, textX, textY, col, true, s, dir.isCardinal());
                }
            }
        }

        // Draw Team View teammate markers if available
        renderTeammateMarkers(renderer, currentYaw, centerX, drawX, width, drawY, height, fovDegrees, pxPerDegree);
    }

    /**
     * Renders Detailed compass with degree ticks and header.
     */
    private void renderDetailed(IHudRenderer renderer, float currentYaw) {
        int width = (int) Math.ceil(compassWidth.getValue() * scale.getValue());
        int height = (int) Math.ceil(34 * scale.getValue());

        setupModel(width, height);
        int drawX = model.getScreenX(renderer.getScreenWidth());
        int drawY = model.getScreenY(renderer.getScreenHeight());

        drawBackground(renderer, drawX, drawY, width, height);

        int centerX = drawX + width / 2;
        float fovDegrees = 150.0f;
        float pxPerDegree = (float) width / fovDegrees;

        // Degree header
        float s = (float) (scale.getValue() * textSize.getValue());
        if (showDegrees.getValue() || showCurrentFacing.getValue()) {
            CompassDirection currentDir = CompassDirection.fromMinecraftYaw(currentYaw);
            String header = showDegrees.getValue()
                    ? String.format(Locale.US, "%.0f° %s", currentYaw, currentDir.getCode())
                    : currentDir.getPolishName();
            int hW = (int) Math.ceil(renderer.getTextWidth(header) * (s * 0.85f));
            renderer.drawText(header, centerX - hW / 2, drawY + 3, activeDirectionColor.getValue(), true, s * 0.85f, true);
        }

        // Center pointer
        int pointerY = drawY + (int) (12 * scale.getValue());
        renderer.fill(centerX - 1, pointerY, 2, 4, activeDirectionColor.getValue());

        // Direction ticks
        int tickBaseY = drawY + height - 12;
        for (CompassDirection dir : CompassDirection.values()) {
            if (dir.isCardinal() && !showCardinalDirections.getValue()) continue;
            if (!dir.isCardinal() && !showIntermediateDirections.getValue()) continue;

            float diff = dir.getCenterAngle() - currentYaw;
            while (diff < -180.0f) diff += 360.0f;
            while (diff > 180.0f) diff -= 360.0f;

            if (Math.abs(diff) <= (fovDegrees / 2.0f)) {
                int itemX = centerX + (int) (diff * pxPerDegree);
                String code = dir.getCode();
                int strW = (int) Math.ceil(renderer.getTextWidth(code) * s);
                int textX = itemX - strW / 2;

                int col = Math.abs(diff) < 12.0f ? activeDirectionColor.getValue() : (dir.isCardinal() ? cardinalColor.getValue() : inactiveDirectionColor.getValue());
                if (textX >= drawX + 3 && textX + strW <= drawX + width - 3) {
                    renderer.drawText(code, textX, tickBaseY, col, true, s, dir.isCardinal());
                }
            }
        }

        renderTeammateMarkers(renderer, currentYaw, centerX, drawX, width, drawY, height, fovDegrees, pxPerDegree);
    }

    /**
     * Renders Circular rosette compass.
     */
    private void renderCircular(IHudRenderer renderer, float currentYaw) {
        int radius = (int) Math.ceil(24 * scale.getValue());
        int size = radius * 2 + 8;

        setupModel(size, size);
        int drawX = model.getScreenX(renderer.getScreenWidth());
        int drawY = model.getScreenY(renderer.getScreenHeight());

        drawBackground(renderer, drawX, drawY, size, size);

        int cx = drawX + size / 2;
        int cy = drawY + size / 2;

        float s = (float) (scale.getValue() * textSize.getValue() * 0.8f);

        // Cardinal Letters (North, South, East, West) around the circle
        // In MC yaw: S=0, W=90, N=180, E=270.
        // We draw them relative to current player rotation so needle or cardinal points rotate!
        for (CompassDirection dir : CompassDirection.values()) {
            if (!dir.isCardinal()) continue;
            float relAngle = dir.getCenterAngle() - currentYaw;
            double rad = Math.toRadians(relAngle - 90.0); // -90 so 0 (S) is relative
            int px = cx + (int) (Math.cos(rad) * (radius - 5));
            int py = cy + (int) (Math.sin(rad) * (radius - 5));

            int strW = (int) Math.ceil(renderer.getTextWidth(dir.getCode()) * s);
            int fontH = (int) Math.ceil(renderer.getFontHeight() * s);
            int col = Math.abs(relAngle) < 25.0f ? activeDirectionColor.getValue() : cardinalColor.getValue();
            renderer.drawText(dir.getCode(), px - strW / 2, py - fontH / 2, col, true, s, true);
        }

        // Center pivot & top needle pointing straight ahead
        renderer.fill(cx - 1, cy - 1, 2, 2, activeDirectionColor.getValue());
        renderer.fill(cx - 1, cy - radius + 2, 2, 6, activeDirectionColor.getValue());
    }

    private void renderTeammateMarkers(
            IHudRenderer renderer, float currentYaw,
            int centerX, int drawX, int width, int drawY, int height,
            float fovDegrees, float pxPerDegree
    ) {
        if (!showTeammates.getValue()) return;
        ITeamDirectionProvider teamProvider = TeamTracker.getInstance();
        if (teamProvider == null) return;

        List<TeammateDirectionInfo> teammates = teamProvider.getTeammateDirections();
        if (teammates == null || teammates.isEmpty()) return;

        int markerH = 3;
        int markerY = drawY + height - markerH - 1;

        for (TeammateDirectionInfo info : teammates) {
            if (info == null) continue;
            float diff = (float) info.relativeAngleDegrees();
            while (diff < -180.0f) diff += 360.0f;
            while (diff > 180.0f) diff -= 360.0f;

            if (Math.abs(diff) <= (fovDegrees / 2.0f)) {
                int itemX = centerX + (int) (diff * pxPerDegree);
                if (itemX >= drawX + 4 && itemX <= drawX + width - 4) {
                    renderer.fill(itemX - 1, markerY, 3, markerH, info.teamColorArgb());
                }
            }
        }
    }

    private void setupModel(int w, int h) {
        model.setWidth(w);
        model.setHeight(h);
        model.setScale(1.0);
        model.setAnchor(anchor.getValue());
        model.setPosX(positionX.getValue().intValue());
        model.setPosY(positionY.getValue().intValue());
    }

    private void drawBackground(IHudRenderer renderer, int x, int y, int w, int h) {
        HudComponentRenderer.renderBackground(
                renderer, x, y, w, h,
                componentStyle.getValue(),
                backgroundColor.getValue(),
                backgroundOpacity.getValue(),
                borderColor.getValue(),
                showBorder.getValue(),
                borderThickness.getValue().intValue()
        );
    }

    // Getters for settings testing
    public EnumSetting<CompassStyle> getCompassStyle() { return compassStyle; }
    public BooleanSetting getShowCardinalDirections() { return showCardinalDirections; }
    public BooleanSetting getShowIntermediateDirections() { return showIntermediateDirections; }
    public BooleanSetting getShowDegrees() { return showDegrees; }
    public BooleanSetting getSmoothMovement() { return smoothMovement; }
    public NumberSetting getInterpolationSpeed() { return interpolationSpeed; }
    public EnumSetting<HudAnchor> getAnchor() { return anchor; }
    public NumberSetting getPositionX() { return positionX; }
    public NumberSetting getPositionY() { return positionY; }
    public float getSmoothedYaw() { return smoothedYaw; }
}
