package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.hud.itemcounter.ItemCounterModule;
import pl.zombek.client.core.modules.hud.itemtracker.ItemTrackerModule;
import pl.zombek.client.core.modules.hud.serveraddress.ServerAddressModule;
import pl.zombek.client.core.platform.IPlatformAdapter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8B4IntegrationTest {

    @TempDir
    Path tempConfigDir;

    private ZombekCore core;

    static class MockPlatformAdapter implements IPlatformAdapter {
        private final Path dir;
        MockPlatformAdapter(Path dir) { this.dir = dir; }
        @Override public String getLoaderName() { return "TestLoader"; }
        @Override public String getLoaderVersion() { return "1.0.0"; }
        @Override public String getMinecraftVersion() { return "1.20.4"; }
        @Override public Path getGameDirectory() { return dir; }
        @Override public Path getConfigDirectory() { return dir; }
        @Override public boolean isClient() { return true; }
        @Override public void openMenu() {}
    }

    @BeforeEach
    public void setUp() {
        ModuleManager.getInstance().clear();
        ZombekCore.resetInstanceForTesting();
        core = ZombekCore.init(new MockPlatformAdapter(tempConfigDir));
    }

    @AfterEach
    public void tearDown() {
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
    }

    @Test
    public void testAllThreeModulesRegisteredInCore() {
        ModuleManager mm = core.getModuleManager();
        assertNotNull(mm.getModuleById("item_counter"), "ItemCounterModule should be registered in core");
        assertNotNull(mm.getModuleById("item_tracker"), "ItemTrackerModule should be registered in core");
        assertNotNull(mm.getModuleById("server_address"), "ServerAddressModule should be registered in core");
    }

    @Test
    public void testModulesRenderPipelineAndPriorityCollisions() {
        ModuleManager mm = core.getModuleManager();
        AbstractModule itemCounter = mm.getModuleById("item_counter");
        AbstractModule itemTracker = mm.getModuleById("item_tracker");
        AbstractModule serverAddress = mm.getModuleById("server_address");

        itemCounter.setEnabled(true);
        itemTracker.setEnabled(true);
        serverAddress.setEnabled(true);

        // Mock HUD renderer
        IHudRenderer mockRenderer = new IHudRenderer() {
            @Override public int getScreenWidth() { return 800; }
            @Override public int getScreenHeight() { return 600; }
            @Override public void fill(int x, int y, int width, int height, int color) {}
            @Override public void drawBorder(int x, int y, int width, int height, int color, int thickness) {}
            @Override public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) {}
            @Override public int getTextWidth(String text) { return text != null ? text.length() * 6 : 0; }
            @Override public int getFontHeight() { return 9; }
        };

        // Render each without throwing exceptions
        assertDoesNotThrow(() -> ((IHudElement) itemCounter).render(mockRenderer));
        assertDoesNotThrow(() -> ((IHudElement) itemTracker).render(mockRenderer));
        assertDoesNotThrow(() -> ((IHudElement) serverAddress).render(mockRenderer));

        // Test priorities
        assertEquals(32, HudPriorityResolver.PRIORITY_ITEM_COUNTER);
        assertEquals(29, HudPriorityResolver.PRIORITY_ITEM_TRACKER);
        assertEquals(24, HudPriorityResolver.PRIORITY_SERVER_ADDRESS);

        // Collision resolver with Item Counter and Item Tracker stacked at TOP_LEFT
        List<HudComponentModel> components = new ArrayList<>();
        components.add(((ItemCounterModule) itemCounter).getHudModel());
        components.add(((ItemTrackerModule) itemTracker).getHudModel());

        // Set both to same position to force collision
        components.get(0).setAnchor(HudAnchor.TOP_LEFT);
        components.get(0).setPosX(10);
        components.get(0).setPosY(10);
        components.get(0).setWidth(100);
        components.get(0).setHeight(30);

        components.get(1).setAnchor(HudAnchor.TOP_LEFT);
        components.get(1).setPosX(10);
        components.get(1).setPosY(10);
        components.get(1).setWidth(100);
        components.get(1).setHeight(30);

        HudPriorityResolver.resolveCollisions(components, 800, 600, 4);

        // Item Counter has higher priority (32 vs 29), so Item Counter stays at Y=10, Item Tracker shifted down
        assertEquals(10, components.get(0).getPosY());
        assertTrue(components.get(1).getPosY() >= 40, "Lower priority ItemTracker should be shifted down to avoid collision");
    }
}
