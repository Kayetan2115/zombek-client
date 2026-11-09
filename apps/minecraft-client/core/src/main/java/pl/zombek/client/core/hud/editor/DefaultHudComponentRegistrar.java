package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudRenderer;

import java.util.EnumSet;

/**
 * Registers all built-in ZombekClient HUD components into the HudComponentRegistry
 * along with default positions, capabilities, and realistic preview rendering routines.
 */
public class DefaultHudComponentRegistrar {

    public static void registerAll() {
        HudComponentRegistry reg = HudComponentRegistry.getInstance();

        // 1. Keystrokes
        reg.register(HudComponentDescriptor.builder("keystrokes", "Keystrokes")
                .description("Wizualizacja naciśniętych klawiszy WASD, LPM, PPM i Spacji")
                .category("PvP")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(20, 20)
                .defaultSize(76, 56)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    int kw = 22, kh = 16;
                    r.fill(x + 27, y + 4, kw, kh, 0xAA333333);
                    r.drawText("W", x + 34, y + 8, 0xFFFFFFFF, true, 1.0f, true);
                    r.fill(x + 3, y + 22, kw, kh, 0xAA333333);
                    r.drawText("A", x + 10, y + 26, 0xFFFFFFFF, true, 1.0f, true);
                    r.fill(x + 27, y + 22, kw, kh, 0xAA333333);
                    r.drawText("S", x + 34, y + 26, 0xFFFFFFFF, true, 1.0f, true);
                    r.fill(x + 51, y + 22, kw, kh, 0xAA333333);
                    r.drawText("D", x + 58, y + 26, 0xFFFFFFFF, true, 1.0f, true);
                    r.fill(x + 3, y + 40, 34, 12, 0xAA333333);
                    r.drawText("LMB", x + 10, y + 42, 0xFFCCCCCC, false, 0.8f, false);
                    r.fill(x + 39, y + 40, 34, 12, 0xAA333333);
                    r.drawText("RMB", x + 46, y + 42, 0xFFCCCCCC, false, 0.8f, false);
                })
                .build());

        // 2. CPS
        reg.register(HudComponentDescriptor.builder("cps", "CPS Display")
                .description("Licznik kliknięć na sekundę dla lewego i prawego przycisku myszy")
                .category("PvP")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(20, 82)
                .defaultSize(70, 18)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("CPS: ", x + 6, y + 5, 0xFFAAAAAA, true, 1.0f, false);
                    r.drawText("12.4 | 14.0", x + 28, y + 5, 0xFF55FF55, true, 1.0f, true);
                })
                .build());

        // 3. Armor Status
        reg.register(HudComponentDescriptor.builder("armor_status", "Armor Status")
                .description("Wskaźnik stanu i wytrzymałości elementów zbroi oraz trzymanego przedmiotu")
                .category("PvP")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 120)
                .defaultSize(54, 70)
                .defaultAnchor(HudAnchor.BOTTOM_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("⛑ 98%", x + 8, y + 6, 0xFF55FF55, true, 0.9f, false);
                    r.drawText("🦺 85%", x + 8, y + 20, 0xFF55FF55, true, 0.9f, false);
                    r.drawText("👖 92%", x + 8, y + 34, 0xFF55FF55, true, 0.9f, false);
                    r.drawText("🥾 64%", x + 8, y + 48, 0xFFFFFF55, true, 0.9f, false);
                })
                .build());

        // 4. Combo Counter
        reg.register(HudComponentDescriptor.builder("combo_counter", "Combo Counter")
                .description("Wyświetla bieżącą serię celnych uderzeń przeciwnika w walce PvP")
                .category("PvP")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(0, 35)
                .defaultSize(75, 18)
                .defaultAnchor(HudAnchor.TOP_CENTER)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Combo: ", x + 6, y + 5, 0xFFAAAAAA, true, 1.0f, false);
                    r.drawText("5 Hits", x + 38, y + 5, 0xFFFF5555, true, 1.0f, true);
                })
                .build());

        // 5. Cooldowns
        reg.register(HudComponentDescriptor.builder("cooldowns", "Cooldowns")
                .description("Czas odnowienia pereł enderu, jabłek, tarczy i innych przedmiotów")
                .category("PvP")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 80)
                .defaultSize(70, 36)
                .defaultAnchor(HudAnchor.BOTTOM_RIGHT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Ender Pearl: 4.2s", x + 4, y + 6, 0xFF55FFFF, true, 0.8f, false);
                    r.drawText("GApple: 1.0s", x + 4, y + 20, 0xFFFFAA00, true, 0.8f, false);
                })
                .build());

        // 6. PvP Info
        reg.register(HudComponentDescriptor.builder("pvp_info", "PvP Info")
                .description("Informacje o celności ciosów, Reach i statystykach pojedynku")
                .category("PvP")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 195)
                .defaultSize(80, 20)
                .defaultAnchor(HudAnchor.BOTTOM_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Reach: 2.94m", x + 6, y + 6, 0xFF55FFFF, true, 0.9f, true);
                })
                .build());

        // 7. Coordinates
        reg.register(HudComponentDescriptor.builder("coordinates", "Coordinates")
                .description("Bieżące współrzędne X, Y, Z gracza, biom i kierunek świata")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 10)
                .defaultSize(125, 30)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("XYZ: 145 / 68 / -312", x + 6, y + 5, 0xFFFFFFFF, true, 0.9f, true);
                    r.drawText("Biom: Plains (North)", x + 6, y + 17, 0xFFAAAAAA, false, 0.8f, false);
                })
                .build());

        // 8. Direction HUD
        reg.register(HudComponentDescriptor.builder("direction_hud", "Direction HUD")
                .description("Pasek kompasu wskazujący strony świata N, E, S, W")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(0, 10)
                .defaultSize(130, 20)
                .defaultAnchor(HudAnchor.TOP_CENTER)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("[ W  NW  N  NE  E ]", x + 12, y + 6, 0xFFFFAA00, true, 1.0f, true);
                })
                .build());

        // 9. FPS
        reg.register(HudComponentDescriptor.builder("fps", "FPS Display")
                .description("Liczba klatek na sekundę z wskaźnikiem płynności gry")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 44)
                .defaultSize(65, 18)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("FPS: ", x + 6, y + 5, 0xFFAAAAAA, true, 1.0f, false);
                    r.drawText("144", x + 32, y + 5, 0xFF55FF55, true, 1.0f, true);
                })
                .build());

        // 10. Ping
        reg.register(HudComponentDescriptor.builder("ping", "Ping Display")
                .description("Opóźnienie sieciowe w milisekundach z kolorystycznym progiem jakości")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 64)
                .defaultSize(65, 18)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Ping: ", x + 6, y + 5, 0xFFAAAAAA, true, 1.0f, false);
                    r.drawText("24 ms", x + 32, y + 5, 0xFF55FF55, true, 1.0f, true);
                })
                .build());

        // 11. Memory Usage
        reg.register(HudComponentDescriptor.builder("memory_usage", "Memory Usage")
                .description("Użycie pamięci RAM przydzielonej procesowi JVM Minecraft")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 84)
                .defaultSize(90, 18)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("RAM: 42% (2048MB)", x + 6, y + 5, 0xFF55FFFF, true, 0.85f, false);
                })
                .build());

        // 12. Clock
        reg.register(HudComponentDescriptor.builder("clock", "Clock")
                .description("Bieżący czas rzeczywisty lub czas w świecie Minecraft")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 104)
                .defaultSize(75, 18)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("⏰ 14:32:05", x + 6, y + 5, 0xFFFFFFFF, true, 0.9f, false);
                })
                .build());

        // 13. Day Counter
        reg.register(HudComponentDescriptor.builder("day_counter", "Day Counter")
                .description("Licznik minionych dni w bieżącym świecie gry")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 124)
                .defaultSize(75, 18)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Dzień: 42", x + 6, y + 5, 0xFFFFAA00, true, 0.9f, true);
                })
                .build());

        // 14. Playtime
        reg.register(HudComponentDescriptor.builder("playtime", "Playtime")
                .description("Czas trwania bieżącej sesji lub łączny czas gry")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 144)
                .defaultSize(90, 18)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Sesja: 01h 45m", x + 6, y + 5, 0xFFFFFFFF, true, 0.85f, false);
                })
                .build());

        // 15. Stopwatch
        reg.register(HudComponentDescriptor.builder("stopwatch", "Stopwatch")
                .description("Precyzyjny stoper z możliwością startu, pauzy i resetu")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 164)
                .defaultSize(90, 18)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("⏱ 00:03:15.4", x + 6, y + 5, 0xFF55FF55, true, 0.85f, true);
                })
                .build());

        // 16. Scoreboard
        reg.register(HudComponentDescriptor.builder("scoreboard", "Scoreboard")
                .description("Pływający panel tablicy wyników serwera z możliwością zmiany pozycji")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 80)
                .defaultSize(110, 120)
                .defaultAnchor(HudAnchor.TOP_RIGHT)
                .capabilities(HudComponentCapability.POSITION, HudComponentCapability.SCALE,
                        HudComponentCapability.VISIBILITY, HudComponentCapability.ANCHOR, HudComponentCapability.SIZE)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("BED WARS", x + 25, y + 6, 0xFFFFAA00, true, 1.0f, true);
                    r.fill(x + 6, y + 18, w - 12, 1, 0xFF555555);
                    r.drawText("Mapa: Lighthouse", x + 8, y + 24, 0xFFFFFFFF, false, 0.8f, false);
                    r.drawText("R Red: [✓]", x + 8, y + 36, 0xFFFF5555, false, 0.8f, false);
                    r.drawText("B Blue: [✓]", x + 8, y + 48, 0xFF5555FF, false, 0.8f, false);
                    r.drawText("G Green: [Bed]", x + 8, y + 60, 0xFF55FF55, false, 0.8f, false);
                    r.drawText("Y Yellow: [X]", x + 8, y + 72, 0xFFFFFF55, false, 0.8f, false);
                    r.drawText("Kills: 4", x + 8, y + 88, 0xFFCCCCCC, false, 0.8f, false);
                    r.drawText("zombekclient.pl", x + 12, y + 104, 0xFF888888, false, 0.75f, false);
                })
                .build());

        // 17. Potion Effects
        reg.register(HudComponentDescriptor.builder("potion_effects", "Potion Effects")
                .description("Wyświetlanie aktywnych efektów mikstur, poziomów i pozostałego czasu")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 10)
                .defaultSize(90, 60)
                .defaultAnchor(HudAnchor.TOP_RIGHT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Speed II (01:45)", x + 6, y + 8, 0xFF55FFFF, true, 0.85f, false);
                    r.drawText("Strength I (00:30)", x + 6, y + 24, 0xFFFF5555, true, 0.85f, false);
                    r.drawText("Regen I (00:12)", x + 6, y + 40, 0xFFFF55FF, true, 0.85f, false);
                })
                .build());

        // 18. Saturation
        reg.register(HudComponentDescriptor.builder("saturation", "Saturation")
                .description("Wskaźnik poziomu nasycenia i wyczerpania głodu postaci")
                .category("Survival")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(0, 48)
                .defaultSize(85, 18)
                .defaultAnchor(HudAnchor.BOTTOM_CENTER)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("🍖 20.0 (Sat: 15.0)", x + 6, y + 5, 0xFFFFAA00, true, 0.85f, true);
                })
                .build());

        // 19. Boss Bar
        reg.register(HudComponentDescriptor.builder("bossbar", "Boss Bar")
                .description("Pływający pasek zdrowia bossów (Wither, Smok) z dostosowaniem pozycji")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(0, 15)
                .defaultSize(160, 24)
                .defaultAnchor(HudAnchor.TOP_CENTER)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Ender Dragon", x + 46, y + 4, 0xFFFFAA00, true, 0.9f, true);
                    r.fill(x + 10, y + 15, w - 20, 5, 0xFF441144);
                    r.fill(x + 10, y + 15, (w - 20) * 3 / 4, 5, 0xFFFF55FF);
                })
                .build());

        // 20. Item Counter
        reg.register(HudComponentDescriptor.builder("item_counter", "Item Counter")
                .description("Licznik określonych przedmiotów w ekwipunku gracza (np. Perły, Strzały)")
                .category("Survival")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 140)
                .defaultSize(70, 20)
                .defaultAnchor(HudAnchor.BOTTOM_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("🔮 16  🏹 64", x + 8, y + 6, 0xFFFFFFFF, true, 0.9f, true);
                })
                .build());

        // 21. Item Tracker
        reg.register(HudComponentDescriptor.builder("item_tracker", "Item Tracker")
                .description("Śledzi zmiany ilości przedmiotów w sesji (zdobyte, zużyte, utracone)")
                .category("Survival")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 165)
                .defaultSize(85, 26)
                .defaultAnchor(HudAnchor.BOTTOM_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("+4 Diamonds", x + 6, y + 4, 0xFF55FF55, true, 0.8f, true);
                    r.drawText("-1 Golden Apple", x + 6, y + 14, 0xFFFF5555, true, 0.8f, false);
                })
                .build());

        // 22. Server Address
        reg.register(HudComponentDescriptor.builder("server_address", "Server Address")
                .description("Adres aktualnego serwera wieloosobowego lub informacja o grze jednoosobowej")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 10)
                .defaultSize(110, 18)
                .defaultAnchor(HudAnchor.BOTTOM_RIGHT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("hypixel.net (1.20.4)", x + 6, y + 5, 0xFF55FFFF, true, 0.85f, false);
                })
                .build());

        // 23. Net Graph
        reg.register(HudComponentDescriptor.builder("net_graph", "Net Graph")
                .description("Wykres diagnostyczny połączenia sieciowego, jitter, przepustowość i pakiety/s")
                .category("HUD")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 32)
                .defaultSize(140, 60)
                .defaultAnchor(HudAnchor.BOTTOM_RIGHT)
                .capabilities(HudComponentCapability.POSITION, HudComponentCapability.SCALE,
                        HudComponentCapability.VISIBILITY, HudComponentCapability.ANCHOR, HudComponentCapability.SIZE)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("NetGraph: 24ms | 0.8ms", x + 6, y + 4, 0xFF55FF55, true, 0.8f, true);
                    r.drawText("In: 14 KB/s | Out: 6 KB/s", x + 6, y + 14, 0xFFAAAAAA, false, 0.75f, false);
                    // Mini waveform simulation
                    int graphY = y + 26;
                    int graphH = 28;
                    r.fill(x + 6, graphY, w - 12, graphH, 0x66000000);
                    for (int i = 0; i < w - 16; i += 4) {
                        int barH = 6 + (i * 7) % 20;
                        r.fill(x + 8 + i, graphY + graphH - barH, 2, barH, 0xFF55FF55);
                    }
                })
                .build());

        // 24. UHC Session HUD
        reg.register(HudComponentDescriptor.builder("uhc_session_hud", "UHC Session HUD")
                .description("Statystyki meczu UHC: czas fazy, gracze na żywo, odległość do granicy")
                .category("Survival")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 40)
                .defaultSize(100, 45)
                .defaultAnchor(HudAnchor.TOP_LEFT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("UHC Faza: PvP ON", x + 6, y + 5, 0xFFFF5555, true, 0.85f, true);
                    r.drawText("Gracze: 18 / 64", x + 6, y + 17, 0xFFFFFFFF, false, 0.8f, false);
                    r.drawText("Border: 500m", x + 6, y + 29, 0xFFFFAA00, false, 0.8f, false);
                })
                .build());

        // 25. Bedwars Helper
        reg.register(HudComponentDescriptor.builder("bedwars_helper", "Bedwars Helper")
                .description("Pomocnik meczu Bedwars ze statusem łóżek i generatorami")
                .category("Other")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(10, 50)
                .defaultSize(120, 80)
                .defaultAnchor(HudAnchor.TOP_RIGHT)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Bedwars 4v4v4v4", x + 16, y + 5, 0xFFFFAA00, true, 0.9f, true);
                    r.drawText("Czerwoni: [✓] 4", x + 6, y + 20, 0xFFFF5555, false, 0.8f, false);
                    r.drawText("Niebiescy: [✓] 3", x + 6, y + 32, 0xFF5555FF, false, 0.8f, false);
                    r.drawText("Zieloni: [Bed] 2", x + 6, y + 44, 0xFF55FF55, false, 0.8f, false);
                    r.drawText("Żółci: [X] 0", x + 6, y + 56, 0xFFFFFF55, false, 0.8f, false);
                    r.drawText("Diament II: 00:24", x + 6, y + 68, 0xFF55FFFF, false, 0.75f, false);
                })
                .build());

        // 26. Crosshair (CENTER_SCREEN)
        reg.register(HudComponentDescriptor.builder("crosshair", "Crosshair")
                .description("Własny celownik wycentrowany na środku ekranu")
                .category("Visual")
                .type(HudComponentType.CENTER_SCREEN)
                .defaultPosition(0, 0)
                .defaultSize(16, 16)
                .defaultAnchor(HudAnchor.CENTER)
                .capabilities(HudComponentCapability.SCALE, HudComponentCapability.VISIBILITY)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    int cx = x + w / 2;
                    int cy = y + h / 2;
                    r.fill(cx - 5, cy, 11, 1, 0xFFFFFFFF);
                    r.fill(cx, cy - 5, 1, 11, 0xFFFFFFFF);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                })
                .build());

        // 27. NameTags (WORLD_SPACE)
        reg.register(HudComponentDescriptor.builder("nametags", "NameTags")
                .description("Dostosowanie nicków i zdrowia renderowanych nad graczami w świecie 3D")
                .category("Visual")
                .type(HudComponentType.WORLD_SPACE)
                .defaultPosition(0, 0)
                .defaultSize(80, 20)
                .defaultAnchor(HudAnchor.CENTER)
                .capabilities(HudComponentCapability.VISIBILITY, HudComponentCapability.SCALE)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88222222);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Steve [20 HP]", x + 8, y + 6, 0xFF55FF55, true, 0.9f, true);
                })
                .build());

        // 28. TNT Countdown (WORLD_SPACE)
        reg.register(HudComponentDescriptor.builder("tnt_countdown", "TNT Countdown")
                .description("Odliczanie do wybuchu zapalonego bloku TNT w świecie gry")
                .category("PvP")
                .type(HudComponentType.WORLD_SPACE)
                .defaultPosition(0, 0)
                .defaultSize(60, 18)
                .defaultAnchor(HudAnchor.CENTER)
                .capabilities(HudComponentCapability.VISIBILITY)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88222222);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("💣 3.42s", x + 10, y + 5, 0xFFFF5555, true, 0.9f, true);
                })
                .build());

        // 29. TeamView (WORLD_SPACE)
        reg.register(HudComponentDescriptor.builder("teamview", "TeamView")
                .description("Wskaźniki pozycji i dystansu do sojuszników z drużyny")
                .category("PvP")
                .type(HudComponentType.WORLD_SPACE)
                .defaultPosition(0, 0)
                .defaultSize(70, 18)
                .defaultAnchor(HudAnchor.CENTER)
                .capabilities(HudComponentCapability.VISIBILITY)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88222222);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("Alex (24m)", x + 8, y + 5, 0xFF55FFFF, true, 0.85f, true);
                })
                .build());

        // 30. Chat (GUI_OVERLAY)
        reg.register(HudComponentDescriptor.builder("chat", "Chat Box")
                .description("Główne okno czatu gry z animacjami, tłem i filtrowaniem")
                .category("Visual")
                .type(HudComponentType.GUI_OVERLAY)
                .defaultPosition(10, 20)
                .defaultSize(180, 80)
                .defaultAnchor(HudAnchor.BOTTOM_LEFT)
                .capabilities(HudComponentCapability.POSITION, HudComponentCapability.SCALE,
                        HudComponentCapability.VISIBILITY, HudComponentCapability.ANCHOR, HudComponentCapability.SIZE)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("[G] Steve: GG!", x + 6, y + 8, 0xFFFFFFFF, false, 0.8f, false);
                    r.drawText("[G] Alex: Nice game", x + 6, y + 20, 0xFFCCCCCC, false, 0.8f, false);
                    r.drawText("[VIP] Notch: Welcome", x + 6, y + 32, 0xFF55FF55, false, 0.8f, false);
                    r.fill(x + 4, y + h - 14, w - 8, 10, 0x66000000);
                    r.drawText("Wpisz wiadomość...", x + 8, y + h - 12, 0xFF777777, false, 0.75f, false);
                })
                .build());

        // 31. Tab Editor (GUI_OVERLAY)
        reg.register(HudComponentDescriptor.builder("tab_editor", "Tablist")
                .description("Rozszerzona lista graczy pod klawiszem TAB")
                .category("Visual")
                .type(HudComponentType.GUI_OVERLAY)
                .defaultPosition(0, 15)
                .defaultSize(160, 90)
                .defaultAnchor(HudAnchor.TOP_CENTER)
                .capabilities(HudComponentCapability.SCALE, HudComponentCapability.VISIBILITY, HudComponentCapability.SIZE)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("ZombekClient Network (12/100)", x + 16, y + 6, 0xFFFFAA00, true, 0.8f, true);
                    r.fill(x + 6, y + 16, w - 12, 1, 0xFF444444);
                    r.drawText("Steve   [24ms]", x + 10, y + 22, 0xFFFFFFFF, false, 0.75f, false);
                    r.drawText("Alex    [32ms]", x + 10, y + 34, 0xFFFFFFFF, false, 0.75f, false);
                    r.drawText("Notch   [12ms]", x + 10, y + 46, 0xFF55FF55, false, 0.75f, false);
                })
                .build());

        // 32. Titles (GUI_OVERLAY)
        reg.register(HudComponentDescriptor.builder("titles", "Screen Titles")
                .description("Wielkie napisy tytułowe i podtytuły pojawiające się na środku ekranu")
                .category("Visual")
                .type(HudComponentType.GUI_OVERLAY)
                .defaultPosition(0, 50)
                .defaultSize(140, 35)
                .defaultAnchor(HudAnchor.TOP_CENTER)
                .capabilities(HudComponentCapability.SCALE, HudComponentCapability.VISIBILITY)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88111111);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("ZWYCIĘSTWO!", x + 30, y + 8, 0xFFFFAA00, true, 1.1f, true);
                    r.drawText("Wszyscy przeciwnicy wyeliminowani", x + 10, y + 22, 0xFFCCCCCC, false, 0.75f, false);
                })
                .build());

        // 33. Zoom Indicator (HUD Overlay)
        reg.register(HudComponentDescriptor.builder("zoom_indicator", "Zoom Indicator")
                .description("Wskaźnik poziomu aktywnego przybliżenia kamery (Zoom)")
                .category("Visual")
                .type(HudComponentType.SCREEN_SPACE)
                .defaultPosition(0, 80)
                .defaultSize(64, 20)
                .defaultAnchor(HudAnchor.TOP_CENTER)
                .capabilities(HudComponentCapability.POSITION, HudComponentCapability.SCALE,
                        HudComponentCapability.VISIBILITY, HudComponentCapability.ANCHOR)
                .previewProvider((r, d, x, y, w, h, s) -> {
                    r.fill(x, y, w, h, 0x88000000);
                    r.drawBorder(x, y, w, h, 0xFF4A90E2, 1);
                    r.drawText("ZOOM: 4.0x", x + 6, y + 6, 0xFFFFFFFF, true, 0.85f, true);
                })
                .build());
    }
}
