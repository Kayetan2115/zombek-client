package pl.zombek.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.visual.nametags.CustomNameTagRenderer;
import pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory;
import pl.zombek.client.core.modules.visual.nametags.NameTagVisibilityRuleEvaluator;
import pl.zombek.client.core.modules.visual.nametags.NameTagsModule;
import pl.zombek.client.core.playerinfo.PlayerInfoData;
import pl.zombek.client.core.playerinfo.ZombekPlayerInfoService;
import pl.zombek.client.integration.FabricEntityClassifier;

/**
 * Mixin intercepting entity name tag rendering to apply custom NameTags formatting,
 * friend markers, health, distance, and Fair Play visibility rules on Fabric.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Shadow @Final protected EntityRenderDispatcher dispatcher;
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void zombekclient$onRenderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!NameTagsModule.shouldOverrideVanilla()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        NameTagsModule module = (NameTagsModule) ModuleManager.getInstance().getModuleById(NameTagsModule.ID);
        if (module == null) {
            return;
        }

        NameTagEntityCategory category = FabricEntityClassifier.classifyToNameTagCategory(entity);
        if (category == null) {
            return;
        }

        if (category == NameTagEntityCategory.PLAYERS && !module.getTargetPlayersSetting().getValue()) return;
        if (category == NameTagEntityCategory.LIVING_ENTITIES && !module.getTargetLivingSetting().getValue()) return;
        if (category == NameTagEntityCategory.OTHER_ENTITIES && !module.getTargetOtherSetting().getValue()) return;

        PlayerInfoData data = ZombekPlayerInfoService.getInstance().getEntityInfo(entity);
        if (data == null) {
            return;
        }

        boolean isSneaking = entity.isSneaking();
        boolean isInvisible = entity.isInvisible();
        boolean isSpectator = entity.isSpectator();
        boolean isGuiOpen = client.currentScreen != null;
        double distance = client.player.distanceTo(entity);

        boolean canRender = NameTagVisibilityRuleEvaluator.shouldRender(
                data,
                category,
                isSneaking,
                isInvisible,
                isSpectator,
                isGuiOpen,
                distance,
                module.getTargetPlayersSetting().getValue(),
                module.getTargetLivingSetting().getValue(),
                module.getTargetOtherSetting().getValue(),
                module.getRespectVanillaVisibilitySetting().getValue(),
                module.getMaxDisplayDistanceSetting().getValue().doubleValue(),
                module.getMinDisplayDistanceSetting().getValue().doubleValue(),
                module.getHideInGuiSetting().getValue(),
                module.getHideInSpectatorSetting().getValue(),
                module.getHideInvisibleSetting().getValue(),
                module.getHideSneakingSetting().getValue()
        );

        if (!canRender) {
            ci.cancel();
            return;
        }

        CustomNameTagRenderer.NameTagFormattedContent formatted = CustomNameTagRenderer.formatNameTag(data, module);
        if (formatted == null || formatted.mainLine.isBlank()) {
            return;
        }

        ci.cancel();

        // Render custom 3D NameTag
        double distSq = this.dispatcher.getSquaredDistanceToCamera(entity);
        if (distSq > 4096.0) return;

        boolean seeThrough = !isSneaking;
        float yOffset = entity.getHeight() + 0.5f + module.getVerticalOffsetSetting().getValue().floatValue() * 0.1f;
        float scaleMultiplier = module.getScaleMultiplierSetting().getValue().floatValue();
        float baseScale = -0.025f * scaleMultiplier;

        matrices.push();
        matrices.translate(0.0f, yOffset, 0.0f);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(baseScale, baseScale, -baseScale);

        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
        TextRenderer tr = this.getTextRenderer();
        float textWidth = tr.getWidth(formatted.mainLine);
        float x = -textWidth / 2.0f;

        int bgAlpha = (int) (module.getBackgroundOpacitySetting().getValue() * 255.0f);
        int bgColor = (bgAlpha << 24) | (module.getBackgroundColorSetting().getValue() & 0x00FFFFFF);

        // Header line if present
        float curY = 0.0f;
        if (!formatted.headerLine.isBlank()) {
            float hw = tr.getWidth(formatted.headerLine);
            tr.draw(
                    formatted.headerLine,
                    -hw / 2.0f,
                    -10.0f,
                    0xFFFFAA00,
                    module.getShadowSetting().getValue(),
                    posMatrix,
                    vertexConsumers,
                    seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL,
                    bgColor,
                    light
            );
        }

        // Main line
        tr.draw(
                formatted.mainLine,
                x,
                curY,
                formatted.mainTextColor,
                module.getShadowSetting().getValue(),
                posMatrix,
                vertexConsumers,
                seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL,
                bgColor,
                light
        );

        // Sub line if present
        if (!formatted.subLine.isBlank()) {
            float sw = tr.getWidth(formatted.subLine);
            tr.draw(
                    formatted.subLine,
                    -sw / 2.0f,
                    curY + 10.0f,
                    0xFFAAAAAA,
                    module.getShadowSetting().getValue(),
                    posMatrix,
                    vertexConsumers,
                    seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL,
                    bgColor,
                    light
            );
        }

        matrices.pop();
    }
}
