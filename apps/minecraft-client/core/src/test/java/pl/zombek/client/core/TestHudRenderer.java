package pl.zombek.client.core;

import pl.zombek.client.core.hud.IHudRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared mock HUD renderer for unit tests.
 */
public class TestHudRenderer implements IHudRenderer {
    public int screenWidth;
    public int screenHeight;
    public int fillCallCount = 0;
    public int drawBorderCallCount = 0;
    public int drawTextCallCount = 0;
    public String lastTextDrawn = "";
    public final List<String> drawnTexts = new ArrayList<>();

    public TestHudRenderer(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public TestHudRenderer() {
        this(800, 600);
    }

    @Override
    public void fill(int x, int y, int width, int height, int color) {
        fillCallCount++;
    }

    @Override
    public void drawBorder(int x, int y, int width, int height, int color, int thickness) {
        drawBorderCallCount++;
    }

    @Override
    public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) {
        drawTextCallCount++;
        lastTextDrawn = text;
        drawnTexts.add(text);
    }

    @Override
    public int getTextWidth(String text) {
        return text != null ? text.length() * 6 : 0;
    }

    @Override
    public int getFontHeight() {
        return 9;
    }

    @Override
    public int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return screenHeight;
    }
}
