package pl.zombek.client.core.modules.hud.info;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.info.CompassDirection;
import pl.zombek.client.core.info.CoordinateFormat;
import pl.zombek.client.core.info.DimensionType;
import pl.zombek.client.core.info.IInfoHudDataProvider;
import pl.zombek.client.core.info.InfoHudDataProviderRegistry;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Universal Coordinates HUD Module.
 * Displays live player position (X, Y, Z), facing direction, dimension,
 * chunk/region coordinates, and Overworld <-> Nether ratio conversion.
 */
public class CoordinatesModule extends AbstractModule implements IHudElement {

    // General Group
    private final EnumSetting<CoordinateFormat> coordinateFormat;
    private final NumberSetting decimalPrecision;
    private final BooleanSetting showX;
    private final BooleanSetting showY;
    private final BooleanSetting showZ;
    private final BooleanSetting showDimension;
    private final BooleanSetting showFacing;
    private final BooleanSetting showChunkCoordinates;
    private final BooleanSetting showRegionCoordinates;

    // Dimension Group
    private final BooleanSetting showOverworldEquivalent;
    private final BooleanSetting showNetherEquivalent;
    private final BooleanSetting showConversionLabel;
    private final BooleanSetting hideInOtherDimensions;

    // Appearance Group
    private final EnumSetting<HudComponentStyle> componentStyle;
    private final ColorSetting textColor;
    private final ColorSetting labelColor;
    private final ColorSetting conversionColor;
    private final ColorSetting backgroundColor;
    private final NumberSetting backgroundOpacity;
    private final BooleanSetting showBorder;
    private final ColorSetting borderColor;
    private final NumberSetting borderThickness;
    private final NumberSetting textSize;
    private final NumberSetting scale;
    private final NumberSetting spacing;
    private final BooleanSetting textShadow;

    // Position Group
    private final EnumSetting<HudAnchor> anchor;
    private final NumberSetting positionX;
    private final NumberSetting positionY;

    private final HudComponentModel model;
    private final List<RenderLine> cachedLines = new ArrayList<>(10);

    public CoordinatesModule() {
        super("coordinates", "Coordinates", "Wyświetla współrzędne gracza, kierunek, wymiar oraz przeliczenia Nether/Overworld.", ModuleCategory.HUD);

        this.model = new HudComponentModel("coordinates", 120, 48, HudAnchor.TOP_LEFT);

        // --- 1. General Settings ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        coordinateFormat = generalGroup.addSetting(new EnumSetting<>(
                "format", "Format współrzędnych", "Układ wyświetlania współrzędnych",
                CoordinateFormat.FULL));
        decimalPrecision = generalGroup.addSetting(new NumberSetting(
                "precision", "Miejsca po przecinku", "Liczba cyfr dziesiętnych dla trybu pełnego",
                2.0, 0.0, 4.0, 1.0));
        showX = generalGroup.addSetting(new BooleanSetting("show_x", "Pokaż X", "Wyświetla oś X", true));
        showY = generalGroup.addSetting(new BooleanSetting("show_y", "Pokaż Y", "Wyświetla oś Y (wysokość)", true));
        showZ = generalGroup.addSetting(new BooleanSetting("show_z", "Pokaż Z", "Wyświetla oś Z", true));
        showDimension = generalGroup.addSetting(new BooleanSetting("show_dimension", "Pokaż Wymiar", "Wyświetla aktualny wymiar (Overworld, Nether, End)", true));
        showFacing = generalGroup.addSetting(new BooleanSetting("show_facing", "Pokaż Kierunek", "Wyświetla kierunek patrzenia (N, S, E, W)", true));
        showChunkCoordinates = generalGroup.addSetting(new BooleanSetting("show_chunk", "Pokaż Współrzędne Chunka", "Wyświetla indeks chunka", false));
        showRegionCoordinates = generalGroup.addSetting(new BooleanSetting("show_region", "Pokaż Współrzędne Regionu", "Wyświetla indeks pliku regionu .mca", false));
        registerGroup(generalGroup);

        // --- 2. Dimension Conversion Settings ---
        SettingGroup dimensionGroup = new SettingGroup("dimension", "Przeliczanie Wymiarów");
        showOverworldEquivalent = dimensionGroup.addSetting(new BooleanSetting(
                "show_overworld_equiv", "Odpowiednik w Overworld",
                "Przelicza współrzędne z Netheru do Overworldu (x8)", true));
        showNetherEquivalent = dimensionGroup.addSetting(new BooleanSetting(
                "show_nether_equiv", "Odpowiednik w Netherze",
                "Przelicza współrzędne z Overworldu do Netheru (/8)", true));
        showConversionLabel = dimensionGroup.addSetting(new BooleanSetting(
                "show_conversion_label", "Etykieta przeliczenia",
                "Dodaje etykietę [Nether: ...] lub [Overworld: ...]", true));
        hideInOtherDimensions = dimensionGroup.addSetting(new BooleanSetting(
                "hide_in_other_dimensions", "Ukryj w The End i innych",
                "Nie wyświetla błędnego przeliczenia w The End lub wymiarach modowanych", true));
        registerGroup(dimensionGroup);

        // --- 3. Appearance Settings ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        componentStyle = appearanceGroup.addSetting(new EnumSetting<>(
                "style", "Styl komponentu", "Styl obramowania i tła",
                HudComponentStyle.CLASSIC));
        textColor = appearanceGroup.addSetting(new ColorSetting(
                "text_color", "Kolor tekstu", "Kolor wartości liczbowych", 0xFFFFFFFF));
        labelColor = appearanceGroup.addSetting(new ColorSetting(
                "label_color", "Kolor etykiet", "Kolor prefiksów X, Y, Z", 0xFFA0AEC0));
        conversionColor = appearanceGroup.addSetting(new ColorSetting(
                "conversion_color", "Kolor przeliczenia", "Kolor przeliczonych współrzędnych", 0xFFF59E0B));
        backgroundColor = appearanceGroup.addSetting(new ColorSetting(
                "background_color", "Kolor tła", "Własny kolor tła komponentu", 0xFF111827));
        backgroundOpacity = appearanceGroup.addSetting(new NumberSetting(
                "opacity", "Przezroczystość tła", "Przezroczystość tła w procentach", 65.0, 0.0, 100.0, 5.0));
        showBorder = appearanceGroup.addSetting(new BooleanSetting(
                "show_border", "Pokaż obramowanie", "Rysuje ramkę wokół modułu", true));
        borderColor = appearanceGroup.addSetting(new ColorSetting(
                "border_color", "Kolor obramowania", "Kolor ramki modułu", 0x40FFFFFF));
        borderThickness = appearanceGroup.addSetting(new NumberSetting(
                "border_thickness", "Grubość ramki", "Grubość ramki w pikselach", 1.0, 1.0, 4.0, 1.0));
        textSize = appearanceGroup.addSetting(new NumberSetting(
                "text_size", "Rozmiar tekstu", "Mnożnik skali czcionki", 1.0, 0.5, 2.0, 0.1));
        scale = appearanceGroup.addSetting(new NumberSetting(
                "scale", "Skala modułu", "Ogólne skalowanie elementu", 1.0, 0.5, 2.5, 0.1));
        spacing = appearanceGroup.addSetting(new NumberSetting(
                "spacing", "Odstęp linii", "Odstęp między liniami tekstu", 2.0, 1.0, 10.0, 1.0));
        textShadow = appearanceGroup.addSetting(new BooleanSetting(
                "text_shadow", "Cień tekstu", "Rysuje cień pod napisami", true));
        registerGroup(appearanceGroup);

        // --- 4. Position Group ---
        SettingGroup positionGroup = new SettingGroup("position", "Pozycja");
        anchor = positionGroup.addSetting(new EnumSetting<>(
                "anchor", "Kotwica ekranu", "Narożnik ekranu",
                HudAnchor.TOP_LEFT));
        positionX = positionGroup.addSetting(new NumberSetting(
                "pos_x", "Pozycja X", "Przesunięcie poziome", 4.0, 0.0, 2000.0, 1.0));
        positionY = positionGroup.addSetting(new NumberSetting(
                "pos_y", "Pozycja Y", "Przesunięcie pionowe", 4.0, 0.0, 2000.0, 1.0));
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

        double rawX = provider.getPlayerX();
        double rawY = provider.getPlayerY();
        double rawZ = provider.getPlayerZ();
        float rawYaw = provider.getPlayerYaw();
        DimensionType dimType = provider.getDimensionType();

        int blockX = (int) Math.floor(rawX);
        int blockY = (int) Math.floor(rawY);
        int blockZ = (int) Math.floor(rawZ);

        int precision = decimalPrecision.getValue().intValue();
        String formatPattern = "%." + precision + "f";

        cachedLines.clear();
        List<RenderLine> lines = cachedLines;

        // 1. Build Coordinates Lines
        CoordinateFormat fmt = coordinateFormat.getValue();
        if (fmt == CoordinateFormat.COMPACT) {
            StringBuilder sb = new StringBuilder("XYZ: ");
            if (showX.getValue()) sb.append(blockX);
            if (showY.getValue()) sb.append(showX.getValue() ? ", " : "").append(blockY);
            if (showZ.getValue()) sb.append((showX.getValue() || showY.getValue()) ? ", " : "").append(blockZ);
            lines.add(new RenderLine("", sb.toString(), textColor.getValue()));
        } else if (fmt == CoordinateFormat.BLOCK) {
            if (showX.getValue() && showY.getValue() && showZ.getValue()) {
                lines.add(new RenderLine("XYZ: ", blockX + ", " + blockY + ", " + blockZ, textColor.getValue()));
            } else {
                if (showX.getValue()) lines.add(new RenderLine("X: ", String.valueOf(blockX), textColor.getValue()));
                if (showY.getValue()) lines.add(new RenderLine("Y: ", String.valueOf(blockY), textColor.getValue()));
                if (showZ.getValue()) lines.add(new RenderLine("Z: ", String.valueOf(blockZ), textColor.getValue()));
            }
        } else if (fmt == CoordinateFormat.SEPARATE) {
            if (showX.getValue()) lines.add(new RenderLine("X: ", String.format(Locale.US, formatPattern, rawX), textColor.getValue()));
            if (showY.getValue()) lines.add(new RenderLine("Y: ", String.format(Locale.US, formatPattern, rawY), textColor.getValue()));
            if (showZ.getValue()) lines.add(new RenderLine("Z: ", String.format(Locale.US, formatPattern, rawZ), textColor.getValue()));
        } else { // FULL
            if (showX.getValue()) lines.add(new RenderLine("X: ", String.format(Locale.US, formatPattern, rawX), textColor.getValue()));
            if (showY.getValue()) lines.add(new RenderLine("Y: ", String.format(Locale.US, formatPattern, rawY), textColor.getValue()));
            if (showZ.getValue()) lines.add(new RenderLine("Z: ", String.format(Locale.US, formatPattern, rawZ), textColor.getValue()));
        }

        // 2. Facing
        if (showFacing.getValue()) {
            CompassDirection dir = CompassDirection.fromMinecraftYaw(rawYaw);
            float normalizedYaw = ((rawYaw % 360f) + 360f) % 360f;
            String facingText = String.format(Locale.US, "%s (%.1f°)", dir.getPolishName(), normalizedYaw);
            lines.add(new RenderLine("Kierunek: ", facingText, textColor.getValue()));
        }

        // 3. Dimension
        if (showDimension.getValue()) {
            lines.add(new RenderLine("Wymiar: ", dimType.getDisplayName(), textColor.getValue()));
        }

        // 4. Dimension Conversion (Nether <-> Overworld)
        boolean allowConversion = !hideInOtherDimensions.getValue() || dimType == DimensionType.OVERWORLD || dimType == DimensionType.NETHER;
        if (allowConversion) {
            if (dimType == DimensionType.OVERWORLD && showNetherEquivalent.getValue()) {
                int netherX = blockX / 8;
                int netherZ = blockZ / 8;
                String prefix = showConversionLabel.getValue() ? "Nether: " : "";
                String val = String.format(Locale.US, "%d, %d, %d", netherX, blockY, netherZ);
                lines.add(new RenderLine(prefix, val, conversionColor.getValue()));
            } else if (dimType == DimensionType.NETHER && showOverworldEquivalent.getValue()) {
                int overworldX = blockX * 8;
                int overworldZ = blockZ * 8;
                String prefix = showConversionLabel.getValue() ? "Overworld: " : "";
                String val = String.format(Locale.US, "%d, %d, %d", overworldX, blockY, overworldZ);
                lines.add(new RenderLine(prefix, val, conversionColor.getValue()));
            }
        }

        // 5. Chunk and Region Coordinates
        if (showChunkCoordinates.getValue()) {
            int chunkX = blockX >> 4;
            int chunkZ = blockZ >> 4;
            int inChunkX = blockX & 15;
            int inChunkZ = blockZ & 15;
            lines.add(new RenderLine("Chunk: ", String.format(Locale.US, "%d, %d [%d, %d]", chunkX, chunkZ, inChunkX, inChunkZ), textColor.getValue()));
        }
        if (showRegionCoordinates.getValue()) {
            int regionX = blockX >> 9;
            int regionZ = blockZ >> 9;
            lines.add(new RenderLine("Region: ", String.format(Locale.US, "r.%d.%d.mca", regionX, regionZ), textColor.getValue()));
        }

        if (lines.isEmpty()) return;

        // Calculate layout size
        float txtScale = (float) (textSize.getValue() * scale.getValue());
        int fontH = (int) Math.ceil(renderer.getFontHeight() * txtScale);
        int lineSpacing = (int) Math.ceil(spacing.getValue() * scale.getValue());
        int padding = (int) Math.ceil(4 * scale.getValue());

        int maxWidth = 0;
        for (RenderLine line : lines) {
            int w = (int) Math.ceil(renderer.getTextWidth(line.prefix + line.text) * txtScale);
            if (w > maxWidth) maxWidth = w;
        }

        int totalWidth = maxWidth + padding * 2;
        int totalHeight = (fontH * lines.size()) + (lineSpacing * (lines.size() - 1)) + padding * 2;

        model.setWidth(totalWidth);
        model.setHeight(totalHeight);
        model.setScale(1.0); // handled via txtScale
        model.setAnchor(anchor.getValue());
        model.setPosX(positionX.getValue().intValue());
        model.setPosY(positionY.getValue().intValue());

        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        model.clampToScreen(screenW, screenH);

        int drawX = model.getScreenX(screenW);
        int drawY = model.getScreenY(screenH);

        // Render Background Panel
        HudComponentRenderer.renderBackground(
                renderer,
                drawX, drawY, totalWidth, totalHeight,
                componentStyle.getValue(),
                backgroundColor.getValue(),
                backgroundOpacity.getValue(),
                borderColor.getValue(),
                showBorder.getValue(),
                borderThickness.getValue().intValue()
        );

        // Render Lines
        int currentY = drawY + padding;
        boolean shadow = textShadow.getValue();
        int lColor = labelColor.getValue();

        for (RenderLine line : lines) {
            int currentX = drawX + padding;
            if (!line.prefix.isEmpty()) {
                renderer.drawText(line.prefix, currentX, currentY, lColor, shadow, txtScale, false);
                currentX += (int) Math.ceil(renderer.getTextWidth(line.prefix) * txtScale);
            }
            renderer.drawText(line.text, currentX, currentY, line.color, shadow, txtScale, false);
            currentY += fontH + lineSpacing;
        }
    }

    private static class RenderLine {
        final String prefix;
        final String text;
        final int color;

        RenderLine(String prefix, String text, int color) {
            this.prefix = prefix;
            this.text = text;
            this.color = color;
        }
    }

    // Getters for settings testing
    public EnumSetting<CoordinateFormat> getCoordinateFormat() { return coordinateFormat; }
    public NumberSetting getDecimalPrecision() { return decimalPrecision; }
    public BooleanSetting getShowX() { return showX; }
    public BooleanSetting getShowY() { return showY; }
    public BooleanSetting getShowZ() { return showZ; }
    public BooleanSetting getShowDimension() { return showDimension; }
    public BooleanSetting getShowFacing() { return showFacing; }
    public BooleanSetting getShowOverworldEquivalent() { return showOverworldEquivalent; }
    public BooleanSetting getShowNetherEquivalent() { return showNetherEquivalent; }
    public EnumSetting<HudAnchor> getAnchor() { return anchor; }
    public NumberSetting getPositionX() { return positionX; }
    public NumberSetting getPositionY() { return positionY; }
}
