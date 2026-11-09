package pl.zombek.client.core.hud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.TestHudRenderer;
import pl.zombek.client.core.hud.scoreboard.*;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.hud.scoreboard.ScoreboardModule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreboardModuleTest {

    private ScoreboardModule module;
    private FallbackScoreboardDataProvider provider;

    @BeforeEach
    public void setup() {
        ScoreboardDataProviderRegistry.getInstance().resetToDefault();
        provider = (FallbackScoreboardDataProvider) ScoreboardDataProviderRegistry.getInstance().getProvider();
        provider.setPreviewMode(false);
        module = new ScoreboardModule();
    }

    @Test
    public void testModuleMetadata() {
        assertEquals("scoreboard", module.getId());
        assertEquals("Scoreboard", module.getName());
        assertEquals(ModuleCategory.HUD, module.getCategory());
        assertEquals(6, module.getSettingGroups().size());
        assertFalse(module.isVisible());
        module.setEnabled(true);
        assertTrue(module.isVisible());
    }

    @Test
    public void testNoRenderWhenNoObjectiveAndPreviewOff() {
        module.setEnabled(true);
        module.getPreviewModeSetting().setValue(false);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertEquals(0, renderer.drawTextCallCount);
        assertFalse(ScoreboardModule.shouldOverrideVanilla());
    }

    @Test
    public void testRenderWithActiveObjective() {
        List<ScoreboardLineData> lines = new ArrayList<>();
        lines.add(new ScoreboardLineData(15, "ZombekServer", "§e§l", "", "§e§lZombekServer"));
        lines.add(new ScoreboardLineData(14, "Gracz: Zombek"));
        lines.add(new ScoreboardLineData(13, "Monety: 1000"));

        ScoreboardObjectiveData obj = new ScoreboardObjectiveData("staty", "§6§lSTATYSTYKI", lines);
        provider.setActiveObjective(obj);

        module.setEnabled(true);
        assertTrue(ScoreboardModule.shouldOverrideVanilla());

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("STATYSTYKI")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("ZombekServer")));
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("15")));
    }

    @Test
    public void testCustomTitleOverride() {
        List<ScoreboardLineData> lines = new ArrayList<>();
        lines.add(new ScoreboardLineData(10, "Line 1"));

        ScoreboardObjectiveData obj = new ScoreboardObjectiveData("test", "Oryginalny Tytuł", lines);
        provider.setActiveObjective(obj);

        module.setEnabled(true);
        module.getCustomTitleSetting().setValue("WŁASNY TYTUŁ");

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("WŁASNY TYTUŁ")));
        assertFalse(renderer.drawnTexts.stream().anyMatch(t -> t.contains("Oryginalny Tytuł")));
    }

    @Test
    public void testHideScores() {
        List<ScoreboardLineData> lines = new ArrayList<>();
        lines.add(new ScoreboardLineData(99, "OnlyText"));

        ScoreboardObjectiveData obj = new ScoreboardObjectiveData("test", "Tytuł", lines);
        provider.setActiveObjective(obj);

        module.setEnabled(true);
        module.getShowScoresSetting().setValue(false);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("OnlyText")));
        assertFalse(renderer.drawnTexts.stream().anyMatch(t -> t.contains("99")));
    }

    @Test
    public void testStylesRendering() {
        List<ScoreboardLineData> lines = new ArrayList<>();
        lines.add(new ScoreboardLineData(1, "StyleLine"));
        ScoreboardObjectiveData obj = new ScoreboardObjectiveData("test", "StyleTest", lines);
        provider.setActiveObjective(obj);

        module.setEnabled(true);

        for (ScoreboardStyle style : ScoreboardStyle.values()) {
            module.getStyleSetting().setValue(style);
            TestHudRenderer renderer = new TestHudRenderer(800, 600);
            module.render(renderer);
            assertTrue(renderer.drawTextCallCount > 0, "Style " + style + " failed to render text");
        }
    }

    @Test
    public void testPreviewModeFallback() {
        module.setEnabled(true);
        module.getPreviewModeSetting().setValue(true);

        assertTrue(ScoreboardModule.shouldOverrideVanilla());
        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("ZOMBEK") || t.contains("CLIENT")));
    }

    @Test
    public void testMaxLinesAndTruncation() {
        List<ScoreboardLineData> lines = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            lines.add(new ScoreboardLineData(i, "Linia numer " + i + " bardzo dlugi tekst przekraczajacy dozwolony limit"));
        }
        ScoreboardObjectiveData obj = new ScoreboardObjectiveData("test", "Długi Scoreboard", lines);
        provider.setActiveObjective(obj);

        module.setEnabled(true);
        module.getMaxLinesSetting().setValue(5.0);
        module.getTruncateLinesSetting().setValue(true);

        TestHudRenderer renderer = new TestHudRenderer(800, 600);
        module.render(renderer);

        assertTrue(renderer.drawTextCallCount > 0);
        assertTrue(renderer.drawnTexts.stream().anyMatch(t -> t.contains("...")));
    }
}
