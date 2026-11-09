package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.chat.ChatHistoryManager;
import pl.zombek.client.core.chat.ChatMessageEntry;
import pl.zombek.client.core.chat.CustomChatRenderer;
import pl.zombek.client.core.chat.ZombekChatService;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.hud.titles.TitlesModule;
import pl.zombek.client.core.modules.visual.chat.ChatModule;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.core.titles.CustomTitleRenderer;
import pl.zombek.client.core.titles.TitleQueueManager;
import pl.zombek.client.core.titles.ZombekTitleService;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8B6IntegrationTest {

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
        if (core != null) {
            core.shutdown();
        }
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
    }

    @Test
    public void testModulesRegisteredInCore() {
        ModuleManager mm = core.getModuleManager();
        assertNotNull(mm.getModuleById("chat"), "ChatModule should be registered in core");
        assertNotNull(mm.getModuleById("titles"), "TitlesModule should be registered in core");
    }

    @Test
    public void testHudPriorities() {
        assertEquals(115, HudPriorityResolver.PRIORITY_TITLES, "Titles should have priority 115");
        assertEquals(110, HudPriorityResolver.PRIORITY_CHAT, "Chat should have priority 110");
    }

    @Test
    public void testLifecycleTickAdvancement() {
        ZombekTitleService.getInstance().onTitle("Integration Title");
        ZombekTitleService.getInstance().onActionBar("Integration Action Bar");

        assertTrue(TitleQueueManager.getInstance().getState().isTitleActive());
        assertTrue(TitleQueueManager.getInstance().getState().isActionBarActive());

        // Tick via core
        core.onClientTick();
        assertNotNull(TitleQueueManager.getInstance().getState());
    }

    @Test
    public void testIncomingChatProcessingInService() {
        ChatMessageEntry entry = ZombekChatService.getInstance().onIncomingMessage(
                "Hello everyone!", "Steve", "uuid-123", false, null, "Alex"
        );
        assertNotNull(entry);
        assertFalse(entry.isHidden());
        assertEquals(1, ChatHistoryManager.getInstance().getMessages().size());
    }

    @Test
    public void testRenderPreviewPipeline() {
        ModuleManager mm = core.getModuleManager();
        AbstractModule chatMod = mm.getModuleById("chat");
        AbstractModule titlesMod = mm.getModuleById("titles");

        chatMod.setEnabled(true);
        titlesMod.setEnabled(true);

        IHudRenderer mockRenderer = new IHudRenderer() {
            @Override public int getScreenWidth() { return 854; }
            @Override public int getScreenHeight() { return 480; }
            @Override public void fill(int x, int y, int width, int height, int color) {}
            @Override public void drawBorder(int x, int y, int width, int height, int color, int thickness) {}
            @Override public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) {}
            @Override public int getTextWidth(String text) { return text != null ? text.length() * 6 : 0; }
            @Override public int getFontHeight() { return 9; }
        };

        if (chatMod instanceof IHudElement element) {
            assertTrue(element.isVisible());
            assertDoesNotThrow(() -> element.render(mockRenderer), "Chat preview rendering should not throw");
        }

        if (titlesMod instanceof IHudElement element) {
            assertTrue(element.isVisible());
            assertDoesNotThrow(() -> element.render(mockRenderer), "Titles preview rendering should not throw");
        }

        assertDoesNotThrow(() -> {
            CustomChatRenderer.getInstance().render(mockRenderer, 0, 0, false, 854, 480);
            CustomTitleRenderer.getInstance().renderTitles(mockRenderer, 854, 480);
            CustomTitleRenderer.getInstance().renderActionBar(mockRenderer, 854, 480);
        }, "Direct custom renderers should not throw");
    }
}
