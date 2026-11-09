package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Consolidates all first-person 3D item and hand animation transforms.
 * Evaluates category positions, blocking, blockhit, swing, bow, rod, eating, and resource pack compatibility.
 */
public class FirstPersonAnimationRenderer {
    private static FirstPersonAnimationRenderer instance;

    public static synchronized FirstPersonAnimationRenderer getInstance() {
        if (instance == null) {
            instance = new FirstPersonAnimationRenderer();
        }
        return instance;
    }

    /**
     * Calculates composite 3D transform for an in-hand item in first person.
     */
    public TransformOffset calculateCombinedTransform(
            String itemId,
            boolean isMainHand,
            float swingProgress,
            float equipProgress,
            boolean isUsingItem,
            int itemUseTicks,
            boolean isRightMouseDown,
            boolean hasCustomResourcePackModel,
            float partialTicks,
            AnimationSettings settings
    ) {
        TransformOffset result = new TransformOffset();
        if (settings == null) {
            return result;
        }

        ItemCategory category = ItemCategory.fromItemId(itemId);
        float sign = isMainHand ? 1.0f : -1.0f;

        // 1. Resource Pack Compatibility check
        if (hasCustomResourcePackModel && settings.getResourcePackTransformMode() == ResourcePackTransformMode.USE_RESOURCE_PACK) {
            // Respect custom model completely
            return result;
        }

        // 2. Base Category Transform
        CategoryTransform catTransform = ItemTransformManager.getInstance().getTransform(category);
        if (catTransform.isEnabled()) {
            TransformOffset off = catTransform.getOffset();
            result.setPosX(result.getPosX() + sign * off.getPosX());
            result.setPosY(result.getPosY() + off.getPosY());
            result.setPosZ(result.getPosZ() + off.getPosZ());
            result.setRotX(result.getRotX() + off.getRotX());
            result.setRotY(result.getRotY() + sign * off.getRotY());
            result.setRotZ(result.getRotZ() + sign * off.getRotZ());
            result.setScaleX(result.getScaleX() * off.getScaleX());
            result.setScaleY(result.getScaleY() * off.getScaleY());
            result.setScaleZ(result.getScaleZ() * off.getScaleZ());
        }

        // 3. Sword Blocking Pose & Blockhit
        boolean isSword = (category == ItemCategory.SWORD);
        BlockingAnimationController blockingController = BlockingAnimationController.getInstance();
        blockingController.update(isSword, isUsingItem, isRightMouseDown, settings);

        if (blockingController.isBlockingActive()) {
            TransformOffset blockOffset = blockingController.calculateBlockingTransform(partialTicks, settings, isMainHand);
            result.setPosX(result.getPosX() + blockOffset.getPosX());
            result.setPosY(result.getPosY() + blockOffset.getPosY());
            result.setPosZ(result.getPosZ() + blockOffset.getPosZ());
            result.setRotX(result.getRotX() + blockOffset.getRotX());
            result.setRotY(result.getRotY() + blockOffset.getRotY());
            result.setRotZ(result.getRotZ() + blockOffset.getRotZ());
            result.setScaleX(result.getScaleX() * blockOffset.getScaleX());
            result.setScaleY(result.getScaleY() * blockOffset.getScaleY());
            result.setScaleZ(result.getScaleZ() * blockOffset.getScaleZ());

            // Blockhit swing overlay
            if (swingProgress > 0.001f) {
                TransformOffset hitOffset = blockingController.calculateBlockhitOffset(swingProgress, settings, isMainHand);
                result.setPosX(result.getPosX() + hitOffset.getPosX());
                result.setPosY(result.getPosY() + hitOffset.getPosY());
                result.setPosZ(result.getPosZ() + hitOffset.getPosZ());
                result.setRotX(result.getRotX() + hitOffset.getRotX());
                result.setRotY(result.getRotY() + hitOffset.getRotY());
                result.setRotZ(result.getRotZ() + hitOffset.getRotZ());
            }
        } else if (swingProgress > 0.001f) {
            // 4. Standard 1.7 / 1.8 Swing (when not blocking)
            TransformOffset swingOffset = SwingAnimationController.getInstance().calculateSwingTransform(swingProgress, settings, isMainHand);
            result.setPosX(result.getPosX() + swingOffset.getPosX());
            result.setPosY(result.getPosY() + swingOffset.getPosY());
            result.setPosZ(result.getPosZ() + swingOffset.getPosZ());
            result.setRotX(result.getRotX() + swingOffset.getRotX());
            result.setRotY(result.getRotY() + swingOffset.getRotY());
            result.setRotZ(result.getRotZ() + swingOffset.getRotZ());
        }

        // 5. Special Bow Drawing Animation
        if (category == ItemCategory.BOW && isUsingItem && settings.isEnableOldBow()) {
            result.setPosX(result.getPosX() + sign * settings.getBowLeftOffset());
            result.setRotZ(result.getRotZ() + sign * settings.getBowTilt());
        }

        // 6. Special Fishing Rod
        if (category == ItemCategory.FISHING_ROD && settings.isEnableOldFishingRod()) {
            result.setRotX(result.getRotX() + settings.getRodAngle());
        }

        // 7. Special Eating & Drinking Bobbing
        if ((category == ItemCategory.FOOD && settings.isEnableOldEating()) || (category == ItemCategory.POTION && settings.isEnableOldDrinking())) {
            if (isUsingItem && itemUseTicks > 0) {
                float bob = (float) Math.sin(itemUseTicks * 0.8f) * 0.03f * settings.getEatingBobbingIntensity();
                result.setPosY(result.getPosY() + bob);
                result.setRotX(result.getRotX() + bob * 100.0f);
            }
        }

        // 8. Item Switching Scale & Vertical Offset
        if (settings.isEnableOldItemSwitching() && equipProgress < 0.99f) {
            float switchProgress = 1.0f - equipProgress;
            result.setPosY(result.getPosY() + settings.getSwitchingVerticalOffset() * switchProgress);
            result.setPosX(result.getPosX() + sign * settings.getSwitchingHorizontalOffset() * switchProgress);
            float sc = 1.0f - (1.0f - settings.getSwitchingScale()) * switchProgress;
            result.setScaleX(result.getScaleX() * sc);
            result.setScaleY(result.getScaleY() * sc);
            result.setScaleZ(result.getScaleZ() * sc);
        }

        return result;
    }
}
