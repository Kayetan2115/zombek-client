package pl.zombek.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.hud.editor.HudEditorManager;
import pl.zombek.client.render.FabricHudRenderer;

/**
 * Fabric Screen for the ZombekClient visual HUD Editor.
 * Delegates rendering and interaction to the core platform-agnostic HudEditorManager.
 */
public class ZombekFabricHudEditorScreen extends Screen {

    private final Screen parentScreen;
    private final HudEditorManager editorManager;

    public ZombekFabricHudEditorScreen(Screen parentScreen) {
        super(Text.literal(ClientConstants.CLIENT_NAME + " - Edytor HUD"));
        this.parentScreen = parentScreen;
        this.editorManager = HudEditorManager.getInstance();
    }

    public ZombekFabricHudEditorScreen() {
        this(null);
    }

    @Override
    protected void init() {
        super.init();
        editorManager.openEditor();
        editorManager.setEditorCloseListener(active -> {
            if (!active) {
                if (this.client != null) {
                    this.client.setScreen(parentScreen);
                }
            }
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        FabricHudRenderer renderer = FabricHudRenderer.getOrCreate(context, this.client);
        editorManager.render(renderer, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (editorManager.mouseClicked((int) mouseX, (int) mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (editorManager.mouseReleased((int) mouseX, (int) mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (editorManager.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        editorManager.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editorManager.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (editorManager.charTyped(chr)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (editorManager.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        editorManager.closeEditor();
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }
    }
}
