package pl.zombek.client.forge.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.hud.editor.HudEditorManager;
import pl.zombek.client.forge.render.ForgeHudRenderer;

/**
 * Forge Screen for the ZombekClient visual HUD Editor.
 * Delegates rendering and interaction to the core platform-agnostic HudEditorManager.
 */
public class ZombekForgeHudEditorScreen extends Screen {

    private final Screen parentScreen;
    private final HudEditorManager editorManager;

    public ZombekForgeHudEditorScreen(Screen parentScreen) {
        super(Component.literal(ClientConstants.CLIENT_NAME + " - Edytor HUD"));
        this.parentScreen = parentScreen;
        this.editorManager = HudEditorManager.getInstance();
    }

    public ZombekForgeHudEditorScreen() {
        this(null);
    }

    @Override
    protected void init() {
        super.init();
        editorManager.openEditor();
        editorManager.setEditorCloseListener(active -> {
            if (!active) {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parentScreen);
                }
            }
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        ForgeHudRenderer renderer = new ForgeHudRenderer(graphics, this.minecraft);
        editorManager.render(renderer, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, delta);
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (editorManager.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (editorManager.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        editorManager.closeEditor();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }
}
