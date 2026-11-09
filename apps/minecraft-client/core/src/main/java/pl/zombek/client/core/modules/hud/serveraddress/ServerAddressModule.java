package pl.zombek.client.core.modules.hud.serveraddress;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.serveraddress.*;
import pl.zombek.client.core.settings.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal Server Address HUD module displaying connection status, server host, port, and privacy-safe metadata.
 */
public class ServerAddressModule extends AbstractModule implements IHudElement {

    public static final String ID = "server_address";

    private final HudComponentModel model;

    // Setting Groups
    private final SettingGroup groupGeneral;
    private final BooleanSetting showConnectionTypeSetting;
    private final BooleanSetting showServerAddressSetting;
    private final BooleanSetting showPortSetting;
    private final BooleanSetting showServerNameSetting;
    private final BooleanSetting showConnectionStatusSetting;
    private final BooleanSetting showDimensionSetting;

    private final SettingGroup groupDisplay;
    private final EnumSetting<ServerAddressLayout> layoutSetting;
    private final BooleanSetting customAliasEnabledSetting;
    private final BooleanSetting aliasPrioritySetting;

    private final SettingGroup groupPrivacy;
    private final EnumSetting<PrivacyMode> privacyModeSetting;
    private final BooleanSetting streamerModeSetting;
    private final BooleanSetting hideWorldNameSetting;

    private final SettingGroup groupAppearance;
    private final EnumSetting<HudComponentStyle> styleSetting;
    private final ColorSetting textColorSetting;
    private final ColorSetting labelColorSetting;
    private final ColorSetting statusColorSetting;
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

    public ServerAddressModule() {
        super(ID, "Server Address", "Prezentuje informacje o aktualnym połączeniu z serwerem lub lokalnym świecie z ochroną prywatności.", ModuleCategory.HUD);

        this.model = new HudComponentModel(ID, 120, 24, HudAnchor.TOP_RIGHT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_SERVER_ADDRESS);

        // 1. General
        this.groupGeneral = new SettingGroup("general", "General");
        this.showConnectionTypeSetting = new BooleanSetting("show_connection_type", "Show Connection Type", "Pokazuje typ połączenia (Singleplayer, Multiplayer, LAN).", true);
        this.showServerAddressSetting = new BooleanSetting("show_server_address", "Show Server Address", "Pokazuje adres serwera (domyślnie wyłączony dla prywatności).", false);
        this.showPortSetting = new BooleanSetting("show_port", "Show Port", "Pokazuje port serwera.", false);
        this.showServerNameSetting = new BooleanSetting("show_server_name", "Show Server Name", "Pokazuje nazwę serwera z listy serwerów.", true);
        this.showConnectionStatusSetting = new BooleanSetting("show_connection_status", "Show Status", "Pokazuje status połączenia (CONNECTED / DISCONNECTED).", true);
        this.showDimensionSetting = new BooleanSetting("show_dimension", "Show Dimension", "Pokazuje aktualny wymiar (Overworld, Nether, End).", false);

        this.groupGeneral.addSetting(showConnectionTypeSetting);
        this.groupGeneral.addSetting(showServerAddressSetting);
        this.groupGeneral.addSetting(showPortSetting);
        this.groupGeneral.addSetting(showServerNameSetting);
        this.groupGeneral.addSetting(showConnectionStatusSetting);
        this.groupGeneral.addSetting(showDimensionSetting);

        // 2. Display
        this.groupDisplay = new SettingGroup("display", "Display Mode & Aliases");
        this.layoutSetting = new EnumSetting<>("layout", "Layout Mode", "Sposób prezentacji danych połączenia.", ServerAddressLayout.COMPACT);
        this.customAliasEnabledSetting = new BooleanSetting("custom_alias_enabled", "Enable Custom Aliases", "Używa własnego aliasu zamiast surowego adresu serwera.", true);
        this.aliasPrioritySetting = new BooleanSetting("alias_priority", "Alias Priority", "Preferuje alias użytkownika nad nazwą serwera.", true);

        this.groupDisplay.addSetting(layoutSetting);
        this.groupDisplay.addSetting(customAliasEnabledSetting);
        this.groupDisplay.addSetting(aliasPrioritySetting);

        // 3. Privacy
        this.groupPrivacy = new SettingGroup("privacy", "Privacy & Security");
        this.privacyModeSetting = new EnumSetting<>("privacy_mode", "Privacy Mode", "Poziom ukrywania / maskowania adresu serwera.", PrivacyMode.MASK_HOST);
        this.streamerModeSetting = new BooleanSetting("streamer_mode", "Streamer Mode", "Automatycznie ukrywa host, port i nazwę świata.", false);
        this.hideWorldNameSetting = new BooleanSetting("hide_world_name", "Hide World Name", "Ukrywa nazwę lokalnego świata w trybie Singleplayer.", true);

        this.groupPrivacy.addSetting(privacyModeSetting);
        this.groupPrivacy.addSetting(streamerModeSetting);
        this.groupPrivacy.addSetting(hideWorldNameSetting);

        // 4. Appearance
        this.groupAppearance = new SettingGroup("appearance", "Appearance");
        this.styleSetting = new EnumSetting<>("style", "HUD Style", "Styl wizualny tła.", HudComponentStyle.MODERN);
        this.textColorSetting = new ColorSetting("text_color", "Text Color", "Kolor wartości.", 0xFFFFFFFF);
        this.labelColorSetting = new ColorSetting("label_color", "Label Color", "Kolor etykiet.", 0xFF55FFFF);
        this.statusColorSetting = new ColorSetting("status_color", "Status Color", "Kolor wskaźnika połączenia.", 0xFF55FF55);
        this.backgroundColorSetting = new ColorSetting("background_color", "Background Color", "Kolor tła modułu.", 0x80000000);
        this.backgroundOpacitySetting = new NumberSetting("background_opacity", "Background Opacity", "Przezroczystość tła (%).", 70.0, 0.0, 100.0, 5.0);
        this.borderColorSetting = new ColorSetting("border_color", "Border Color", "Kolor ramki.", 0x00000000);
        this.showBorderSetting = new BooleanSetting("show_border", "Show Border", "Czy rysować ramkę.", false);
        this.borderThicknessSetting = new NumberSetting("border_thickness", "Border Thickness", "Grubość ramki.", 1.0, 1.0, 4.0, 1.0);
        this.shadowSetting = new BooleanSetting("shadow", "Text Shadow", "Cień tekstu.", true);

        this.groupAppearance.addSetting(styleSetting);
        this.groupAppearance.addSetting(textColorSetting);
        this.groupAppearance.addSetting(labelColorSetting);
        this.groupAppearance.addSetting(statusColorSetting);
        this.groupAppearance.addSetting(backgroundColorSetting);
        this.groupAppearance.addSetting(backgroundOpacitySetting);
        this.groupAppearance.addSetting(borderColorSetting);
        this.groupAppearance.addSetting(showBorderSetting);
        this.groupAppearance.addSetting(borderThicknessSetting);
        this.groupAppearance.addSetting(shadowSetting);

        // 5. Layout
        this.groupLayout = new SettingGroup("layout", "Layout & Position");
        this.anchorSetting = new EnumSetting<>("anchor", "Anchor", "Punkt zakotwiczenia.", HudAnchor.TOP_RIGHT);
        this.posXSetting = new NumberSetting("pos_x", "Position X", "Offset X.", 5.0, -2000.0, 2000.0, 1.0);
        this.posYSetting = new NumberSetting("pos_y", "Position Y", "Offset Y.", 100.0, -2000.0, 2000.0, 1.0);
        this.scaleSetting = new NumberSetting("scale", "Scale", "Skala modułu.", 1.0, 0.5, 2.5, 0.1);

        this.groupLayout.addSetting(anchorSetting);
        this.groupLayout.addSetting(posXSetting);
        this.groupLayout.addSetting(posYSetting);
        this.groupLayout.addSetting(scaleSetting);

        registerGroup(groupGeneral);
        registerGroup(groupDisplay);
        registerGroup(groupPrivacy);
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

    public List<String> buildDisplayLines(ServerConnectionData data, boolean inGame) {
        List<String> lines = new ArrayList<>();

        if (!inGame || data == null || !data.isConnected()) {
            // Preview Fallback
            lines.add("Server: play.example.net [PREVIEW]");
            return lines;
        }

        boolean streamer = streamerModeSetting.getValue();
        PrivacyMode privacy = privacyModeSetting.getValue();
        ServerAddressLayout layout = layoutSetting.getValue();

        if (data.isSingleplayer()) {
            // SINGLEPLAYER: never show a fake IP address!
            if (layout == ServerAddressLayout.MINIMAL) {
                lines.add("SINGLEPLAYER");
            } else if (layout == ServerAddressLayout.DETAILED || layout == ServerAddressLayout.CONNECTION_INFO) {
                lines.add("TYPE: SINGLEPLAYER");
                if (!hideWorldNameSetting.getValue() && !streamer && data.getWorldName() != null) {
                    lines.add("WORLD: " + data.getWorldName());
                }
                if (showDimensionSetting.getValue() && data.getDimension() != null) {
                    lines.add("DIMENSION: " + data.getDimension());
                }
            } else {
                // COMPACT or CUSTOM
                String world = (!hideWorldNameSetting.getValue() && !streamer && data.getWorldName() != null)
                        ? " (" + data.getWorldName() + ")"
                        : "";
                lines.add("Singleplayer" + world);
            }
            return lines;
        }

        // MULTIPLAYER / LAN
        String rawAddress = data.getServerAddress();
        ServerAliasManager.AliasEntry aliasEntry = null;
        if (customAliasEnabledSetting.getValue() && rawAddress != null) {
            aliasEntry = ServerAliasManager.getInstance().getAlias(rawAddress);
        }

        String effectiveHost;
        if (aliasEntry != null && (aliasPrioritySetting.getValue() || privacy == PrivacyMode.SHOW_ALIAS_ONLY)) {
            effectiveHost = aliasEntry.getAlias();
        } else {
            effectiveHost = ServerAddressPrivacyController.formatHost(rawAddress, privacy, streamer);
        }

        boolean showPort = showPortSetting.getValue() && privacy != PrivacyMode.HIDE_PORT && !streamer && data.getServerPort() != 25565;

        switch (layout) {
            case MINIMAL:
                lines.add(effectiveHost);
                break;

            case DETAILED:
            case CONNECTION_INFO:
                if (showConnectionTypeSetting.getValue()) {
                    lines.add("TYPE: " + data.getConnectionType().getLabel().toUpperCase());
                }
                if (showServerNameSetting.getValue() && data.getServerName() != null) {
                    lines.add("NAME: " + data.getServerName());
                }
                lines.add("SERVER: " + effectiveHost);
                if (showPort) {
                    lines.add("PORT: " + data.getServerPort());
                }
                if (showConnectionStatusSetting.getValue()) {
                    lines.add("STATUS: CONNECTED");
                }
                if (showDimensionSetting.getValue() && data.getDimension() != null) {
                    lines.add("DIMENSION: " + data.getDimension());
                }
                break;

            case COMPACT:
            default:
                StringBuilder sb = new StringBuilder("Server: ");
                sb.append(effectiveHost);
                if (showPort) {
                    sb.append(":").append(data.getServerPort());
                }
                lines.add(sb.toString());
                break;
        }

        return lines;
    }

    public boolean copyAddressToClipboard(boolean fullAddress) {
        IServerAddressDataProvider provider = ServerAddressDataProviderRegistry.getInstance().getProvider();
        ServerConnectionData data = provider.getConnectionData();
        if (data == null || !data.isMultiplayer()) return false;

        String addr = data.getServerAddress();
        if (addr == null || addr.isEmpty()) return false;

        if (fullAddress && data.getServerPort() != 25565 && data.getServerPort() > 0) {
            return ServerAddressPrivacyController.copyToClipboard(addr + ":" + data.getServerPort());
        } else {
            return ServerAddressPrivacyController.copyToClipboard(addr);
        }
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible()) return;

        IServerAddressDataProvider provider = ServerAddressDataProviderRegistry.getInstance().getProvider();
        ServerConnectionData data = provider.getConnectionData();
        boolean inGame = data != null && data.isConnected();

        List<String> lines = buildDisplayLines(data, inGame);
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

        for (String line : lines) {
            int color = defaultColor;
            if (line.startsWith("STATUS: CONNECTED")) {
                color = statusColorSetting.getValue();
            } else if (line.contains("Singleplayer")) {
                color = 0xFF55FF55; // green for SP
            }

            renderer.drawText(line, renderX + paddingH, curY, color, shadow, txtScale, false);
            curY += lineHeight;
        }
    }
}
