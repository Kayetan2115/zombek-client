package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.friends.FriendEntry;
import pl.zombek.client.core.friends.NameTagFriendManager;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory;
import pl.zombek.client.core.modules.visual.nametags.NameTagVisibilityRuleEvaluator;
import pl.zombek.client.core.modules.visual.nametags.NameTagsModule;
import pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule;
import pl.zombek.client.core.platform.IPlatformAdapter;
import pl.zombek.client.core.playerinfo.FallbackPlayerInfoDataProvider;
import pl.zombek.client.core.playerinfo.PlayerInfoData;
import pl.zombek.client.core.playerinfo.PlayerInfoDataProviderRegistry;
import pl.zombek.client.core.playerinfo.ZombekPlayerInfoService;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class Etap8B5IntegrationTest {

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
        PlayerInfoDataProviderRegistry.getInstance().registerProvider(new FallbackPlayerInfoDataProvider());
    }

    @AfterEach
    public void tearDown() {
        ZombekCore.resetInstanceForTesting();
        ModuleManager.getInstance().clear();
    }

    @Test
    public void testModulesRegisteredInCore() {
        ModuleManager mm = core.getModuleManager();
        assertNotNull(mm.getModuleById("nametags"), "NameTagsModule should be registered in core");
        assertNotNull(mm.getModuleById("tab_editor"), "TabEditorModule should be registered in core");
    }

    @Test
    public void testFriendSystemCentralServiceIntegration() {
        UUID friendUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        FriendEntry friend = new FriendEntry(friendUuid, "ZombekMaster", "BossZombek", 0xFFFFAA00, "★", "[VIP] ", "Dev friend");
        NameTagFriendManager.getInstance().addFriend(friend);

        ZombekPlayerInfoService.getInstance().refreshCache();
        PlayerInfoData data = ZombekPlayerInfoService.getInstance().getPlayer(friendUuid, "ZombekMaster");

        assertNotNull(data);
        assertTrue(data.isFriend());
        assertEquals("BossZombek", data.getEffectiveName(true));
        assertEquals("ZombekMaster", data.getEffectiveName(false));
    }

    @Test
    public void testRenderPreviewPipeline() {
        ModuleManager mm = core.getModuleManager();
        AbstractModule nameTags = mm.getModuleById("nametags");
        AbstractModule tabEditor = mm.getModuleById("tab_editor");

        nameTags.setEnabled(true);
        tabEditor.setEnabled(true);

        IHudRenderer mockRenderer = new IHudRenderer() {
            @Override public int getScreenWidth() { return 854; }
            @Override public int getScreenHeight() { return 480; }
            @Override public void fill(int x, int y, int width, int height, int color) {}
            @Override public void drawBorder(int x, int y, int width, int height, int color, int thickness) {}
            @Override public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) {}
            @Override public int getTextWidth(String text) { return text != null ? text.length() * 6 : 0; }
            @Override public int getFontHeight() { return 9; }
        };

        assertDoesNotThrow(() -> ((IHudElement) nameTags).render(mockRenderer));
        assertDoesNotThrow(() -> ((IHudElement) tabEditor).render(mockRenderer));

        assertEquals(120, HudPriorityResolver.PRIORITY_TAB_EDITOR);
    }

    @Test
    public void testStrictFairPlayGuarantees() {
        ZombekPlayerInfoService.getInstance().refreshCache();
        PlayerInfoData data = ZombekPlayerInfoService.getInstance().getPlayer(null, "Alex");
        assertNotNull(data);

        // Sneaking player must not have NameTag rendered when sneaking
        boolean canRenderSneaking = NameTagVisibilityRuleEvaluator.shouldRender(
                data, NameTagEntityCategory.PLAYERS,
                true, false, false, false, 12.0,
                true, true, true, true, 64.0, 0.0, false, false, false, true
        );
        assertFalse(canRenderSneaking, "Sneaking player must not render NameTag under Fair Play rules");

        // Invisible player must not have NameTag rendered
        boolean canRenderInvisible = NameTagVisibilityRuleEvaluator.shouldRender(
                data, NameTagEntityCategory.PLAYERS,
                false, true, false, false, 12.0,
                true, true, true, true, 64.0, 0.0, false, false, true, false
        );
        assertFalse(canRenderInvisible, "Invisible entity must not render NameTag under Fair Play rules");
    }
}
