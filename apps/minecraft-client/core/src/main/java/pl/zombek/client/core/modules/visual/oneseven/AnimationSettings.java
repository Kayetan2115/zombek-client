package pl.zombek.client.core.modules.visual.oneseven;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Snapshot of all 1.7 Visuals settings with preset defaults and JSON mapping.
 */
public class AnimationSettings {
    public static final int SCHEMA_VERSION = 1;

    // Presets
    private AnimationPreset activePreset = AnimationPreset.MINECRAFT_1_7;
    private String customProfileName = "Default Custom";

    // Blocking
    private boolean enableOldBlocking = true;
    private BlockingStyle blockingStyle = BlockingStyle.VANILLA_1_7;
    private float blockingPosX = -0.14f;
    private float blockingPosY = 0.15f;
    private float blockingPosZ = -0.2f;
    private float blockingRotX = -102.0f;
    private float blockingRotY = -59.0f;
    private float blockingRotZ = 79.0f;
    private float blockingScale = 1.0f;
    private float blockingSpeed = 1.0f;
    private boolean rightHandBlocking = true;
    private boolean leftHandBlocking = true;
    private boolean firstPersonBlocking = true;
    private boolean thirdPersonBlocking = true;

    // Blockhit
    private boolean enableOldBlockhit = true;
    private BlockhitStyle blockhitStyle = BlockhitStyle.CLASSIC_1_7;
    private float blockhitSwingSpeed = 1.0f;
    private float blockhitSwingAngle = 1.0f;
    private float blockhitOffsetX = 0.0f;
    private float blockhitOffsetY = 0.0f;
    private float blockhitRotation = 0.0f;
    private float blockhitScale = 1.0f;
    private float blockhitIntensity = 1.0f;

    // Swing
    private boolean enableOldSwing = true;
    private SwingStyle swingStyle = SwingStyle.VANILLA_1_7;
    private int swingDuration = 6;
    private float swingSpeed = 1.0f;
    private float swingAngle = 1.0f;
    private float swingIntensity = 1.0f;
    private boolean customSwingTransform = false;
    private boolean leftHandSupport = true;

    // Sneak
    private boolean enableOldSneak = true;
    private boolean sneakModelPose = true;
    private boolean sneakCameraAnimation = true;
    private SneakTransition sneakTransition = SneakTransition.INSTANT_DOWN_SMOOTH_UP;
    private float sneakTransitionSpeed = 1.0f;
    private boolean smoothSneak = false;
    private boolean instantCameraDown = true;
    private boolean smoothCameraUp = true;
    private boolean sneakFirstPerson = true;
    private boolean sneakThirdPerson = true;

    // Fishing Rod
    private boolean enableOldFishingRod = true;
    private float rodAngle = -15.0f;
    private boolean rodWhipAnimation = true;

    // Bow
    private boolean enableOldBow = true;
    private float bowLeftOffset = 0.0f;
    private float bowTilt = 8.0f;

    // Eating & Drinking
    private boolean enableOldEating = true;
    private boolean enableOldDrinking = true;
    private float eatingBobbingIntensity = 1.0f;

    // Item Switching
    private boolean enableOldItemSwitching = true;
    private float switchingSpeed = 1.8f;
    private float switchingIntensity = 1.0f;
    private float switchingHorizontalOffset = 0.0f;
    private float switchingVerticalOffset = 0.0f;
    private float switchingScale = 1.0f;

    // Damage Visuals (Red Armor on Hit)
    private boolean enableRedArmor = true;
    private int damageTintColor = 0xFFFF0000;
    private float tintIntensity = 0.7f;
    private int flashDuration = 10;
    private boolean smoothFade = true;
    private boolean armorOnly = false;
    private boolean bodyAndArmor = true;

    // Health Display
    private boolean enableOldHealth = false;
    private boolean classicHeartStyle = true;
    private boolean heartFlash = true;
    private int heartSpacing = 8;
    private boolean absorptionStyle = true;
    private boolean lowHealthAnimation = true;
    private boolean regenerationAnimation = true;
    private float customHeartScale = 1.0f;

    // Resource Pack Compatibility
    private ResourcePackTransformMode resourcePackTransformMode = ResourcePackTransformMode.USE_RESOURCE_PACK;

    public AnimationSettings() {}

    public AnimationSettings copy() {
        AnimationSettings s = new AnimationSettings();
        s.copyFrom(this);
        return s;
    }

    public void copyFrom(AnimationSettings src) {
        if (src == null) return;
        this.activePreset = src.activePreset;
        this.customProfileName = src.customProfileName;

        this.enableOldBlocking = src.enableOldBlocking;
        this.blockingStyle = src.blockingStyle;
        this.blockingPosX = src.blockingPosX;
        this.blockingPosY = src.blockingPosY;
        this.blockingPosZ = src.blockingPosZ;
        this.blockingRotX = src.blockingRotX;
        this.blockingRotY = src.blockingRotY;
        this.blockingRotZ = src.blockingRotZ;
        this.blockingScale = src.blockingScale;
        this.blockingSpeed = src.blockingSpeed;
        this.rightHandBlocking = src.rightHandBlocking;
        this.leftHandBlocking = src.leftHandBlocking;
        this.firstPersonBlocking = src.firstPersonBlocking;
        this.thirdPersonBlocking = src.thirdPersonBlocking;

        this.enableOldBlockhit = src.enableOldBlockhit;
        this.blockhitStyle = src.blockhitStyle;
        this.blockhitSwingSpeed = src.blockhitSwingSpeed;
        this.blockhitSwingAngle = src.blockhitSwingAngle;
        this.blockhitOffsetX = src.blockhitOffsetX;
        this.blockhitOffsetY = src.blockhitOffsetY;
        this.blockhitRotation = src.blockhitRotation;
        this.blockhitScale = src.blockhitScale;
        this.blockhitIntensity = src.blockhitIntensity;

        this.enableOldSwing = src.enableOldSwing;
        this.swingStyle = src.swingStyle;
        this.swingDuration = src.swingDuration;
        this.swingSpeed = src.swingSpeed;
        this.swingAngle = src.swingAngle;
        this.swingIntensity = src.swingIntensity;
        this.customSwingTransform = src.customSwingTransform;
        this.leftHandSupport = src.leftHandSupport;

        this.enableOldSneak = src.enableOldSneak;
        this.sneakModelPose = src.sneakModelPose;
        this.sneakCameraAnimation = src.sneakCameraAnimation;
        this.sneakTransition = src.sneakTransition;
        this.sneakTransitionSpeed = src.sneakTransitionSpeed;
        this.smoothSneak = src.smoothSneak;
        this.instantCameraDown = src.instantCameraDown;
        this.smoothCameraUp = src.smoothCameraUp;
        this.sneakFirstPerson = src.sneakFirstPerson;
        this.sneakThirdPerson = src.sneakThirdPerson;

        this.enableOldFishingRod = src.enableOldFishingRod;
        this.rodAngle = src.rodAngle;
        this.rodWhipAnimation = src.rodWhipAnimation;

        this.enableOldBow = src.enableOldBow;
        this.bowLeftOffset = src.bowLeftOffset;
        this.bowTilt = src.bowTilt;

        this.enableOldEating = src.enableOldEating;
        this.enableOldDrinking = src.enableOldDrinking;
        this.eatingBobbingIntensity = src.eatingBobbingIntensity;

        this.enableOldItemSwitching = src.enableOldItemSwitching;
        this.switchingSpeed = src.switchingSpeed;
        this.switchingIntensity = src.switchingIntensity;
        this.switchingHorizontalOffset = src.switchingHorizontalOffset;
        this.switchingVerticalOffset = src.switchingVerticalOffset;
        this.switchingScale = src.switchingScale;

        this.enableRedArmor = src.enableRedArmor;
        this.damageTintColor = src.damageTintColor;
        this.tintIntensity = src.tintIntensity;
        this.flashDuration = src.flashDuration;
        this.smoothFade = src.smoothFade;
        this.armorOnly = src.armorOnly;
        this.bodyAndArmor = src.bodyAndArmor;

        this.enableOldHealth = src.enableOldHealth;
        this.classicHeartStyle = src.classicHeartStyle;
        this.heartFlash = src.heartFlash;
        this.heartSpacing = src.heartSpacing;
        this.absorptionStyle = src.absorptionStyle;
        this.lowHealthAnimation = src.lowHealthAnimation;
        this.regenerationAnimation = src.regenerationAnimation;
        this.customHeartScale = src.customHeartScale;

        this.resourcePackTransformMode = src.resourcePackTransformMode;
    }

    public void applyPresetDefaults(AnimationPreset preset) {
        this.activePreset = preset;
        switch (preset) {
            case MODERN -> {
                enableOldBlocking = false;
                enableOldBlockhit = false;
                enableOldSwing = false;
                swingStyle = SwingStyle.MODERN;
                enableOldSneak = false;
                sneakTransition = SneakTransition.MODERN;
                enableOldFishingRod = false;
                enableOldBow = false;
                enableOldEating = false;
                enableOldDrinking = false;
                enableOldItemSwitching = false;
                enableRedArmor = false;
                enableOldHealth = false;
            }
            case MINECRAFT_1_8 -> {
                enableOldBlocking = true;
                blockingStyle = BlockingStyle.VANILLA_1_8;
                enableOldBlockhit = true;
                blockhitStyle = BlockhitStyle.CLASSIC_1_8;
                enableOldSwing = true;
                swingStyle = SwingStyle.VANILLA_1_8;
                enableOldSneak = true;
                sneakTransition = SneakTransition.SMOOTH_1_7;
                enableOldFishingRod = true;
                enableOldBow = true;
                enableOldEating = true;
                enableOldDrinking = true;
                enableOldItemSwitching = true;
                switchingSpeed = 1.5f;
                enableRedArmor = false; // 1.8 did not tint armor red
                enableOldHealth = false;
            }
            case MINECRAFT_1_7 -> {
                enableOldBlocking = true;
                blockingStyle = BlockingStyle.VANILLA_1_7;
                enableOldBlockhit = true;
                blockhitStyle = BlockhitStyle.CLASSIC_1_7;
                enableOldSwing = true;
                swingStyle = SwingStyle.VANILLA_1_7;
                enableOldSneak = true;
                sneakTransition = SneakTransition.INSTANT_DOWN_SMOOTH_UP;
                instantCameraDown = true;
                smoothCameraUp = true;
                enableOldFishingRod = true;
                enableOldBow = true;
                enableOldEating = true;
                enableOldDrinking = true;
                enableOldItemSwitching = true;
                switchingSpeed = 2.0f;
                enableRedArmor = true; // 1.7 authentic red armor
                damageTintColor = 0xFFFF0000;
                enableOldHealth = true;
            }
            case CUSTOM -> {
                // Keep current settings as baseline for custom customization
            }
        }
    }

    public void serializeToJson(JsonObject node) {
        node.addProperty("schema_version", SCHEMA_VERSION);
        node.addProperty("preset", activePreset.name());
        node.addProperty("custom_profile_name", customProfileName);

        JsonObject blocking = new JsonObject();
        blocking.addProperty("enabled", enableOldBlocking);
        blocking.addProperty("style", blockingStyle.name());
        blocking.addProperty("posX", blockingPosX);
        blocking.addProperty("posY", blockingPosY);
        blocking.addProperty("posZ", blockingPosZ);
        blocking.addProperty("rotX", blockingRotX);
        blocking.addProperty("rotY", blockingRotY);
        blocking.addProperty("rotZ", blockingRotZ);
        blocking.addProperty("scale", blockingScale);
        blocking.addProperty("speed", blockingSpeed);
        blocking.addProperty("rightHand", rightHandBlocking);
        blocking.addProperty("leftHand", leftHandBlocking);
        blocking.addProperty("firstPerson", firstPersonBlocking);
        blocking.addProperty("thirdPerson", thirdPersonBlocking);
        node.add("blocking", blocking);

        JsonObject blockhit = new JsonObject();
        blockhit.addProperty("enabled", enableOldBlockhit);
        blockhit.addProperty("style", blockhitStyle.name());
        blockhit.addProperty("swingSpeed", blockhitSwingSpeed);
        blockhit.addProperty("swingAngle", blockhitSwingAngle);
        blockhit.addProperty("offsetX", blockhitOffsetX);
        blockhit.addProperty("offsetY", blockhitOffsetY);
        blockhit.addProperty("rotation", blockhitRotation);
        blockhit.addProperty("scale", blockhitScale);
        blockhit.addProperty("intensity", blockhitIntensity);
        node.add("blockhit", blockhit);

        JsonObject swing = new JsonObject();
        swing.addProperty("enabled", enableOldSwing);
        swing.addProperty("style", swingStyle.name());
        swing.addProperty("duration", swingDuration);
        swing.addProperty("speed", swingSpeed);
        swing.addProperty("angle", swingAngle);
        swing.addProperty("intensity", swingIntensity);
        swing.addProperty("customTransform", customSwingTransform);
        swing.addProperty("leftHand", leftHandSupport);
        node.add("swing", swing);

        JsonObject sneak = new JsonObject();
        sneak.addProperty("enabled", enableOldSneak);
        sneak.addProperty("modelPose", sneakModelPose);
        sneak.addProperty("cameraAnimation", sneakCameraAnimation);
        sneak.addProperty("transition", sneakTransition.name());
        sneak.addProperty("transitionSpeed", sneakTransitionSpeed);
        sneak.addProperty("smoothSneak", smoothSneak);
        sneak.addProperty("instantDown", instantCameraDown);
        sneak.addProperty("smoothUp", smoothCameraUp);
        sneak.addProperty("firstPerson", sneakFirstPerson);
        sneak.addProperty("thirdPerson", sneakThirdPerson);
        node.add("sneak", sneak);

        JsonObject rod = new JsonObject();
        rod.addProperty("enabled", enableOldFishingRod);
        rod.addProperty("angle", rodAngle);
        rod.addProperty("whipAnimation", rodWhipAnimation);
        node.add("fishing_rod", rod);

        JsonObject bow = new JsonObject();
        bow.addProperty("enabled", enableOldBow);
        bow.addProperty("leftOffset", bowLeftOffset);
        bow.addProperty("tilt", bowTilt);
        node.add("bow", bow);

        JsonObject eating = new JsonObject();
        eating.addProperty("enableOldEating", enableOldEating);
        eating.addProperty("enableOldDrinking", enableOldDrinking);
        eating.addProperty("bobbingIntensity", eatingBobbingIntensity);
        node.add("eating_drinking", eating);

        JsonObject switching = new JsonObject();
        switching.addProperty("enabled", enableOldItemSwitching);
        switching.addProperty("speed", switchingSpeed);
        switching.addProperty("intensity", switchingIntensity);
        switching.addProperty("horizontalOffset", switchingHorizontalOffset);
        switching.addProperty("verticalOffset", switchingVerticalOffset);
        switching.addProperty("scale", switchingScale);
        node.add("item_switching", switching);

        JsonObject damage = new JsonObject();
        damage.addProperty("enableRedArmor", enableRedArmor);
        damage.addProperty("damageTintColor", damageTintColor);
        damage.addProperty("tintIntensity", tintIntensity);
        damage.addProperty("flashDuration", flashDuration);
        damage.addProperty("smoothFade", smoothFade);
        damage.addProperty("armorOnly", armorOnly);
        damage.addProperty("bodyAndArmor", bodyAndArmor);
        node.add("damage_visuals", damage);

        JsonObject health = new JsonObject();
        health.addProperty("enabled", enableOldHealth);
        health.addProperty("classicHeartStyle", classicHeartStyle);
        health.addProperty("heartFlash", heartFlash);
        health.addProperty("heartSpacing", heartSpacing);
        health.addProperty("absorptionStyle", absorptionStyle);
        health.addProperty("lowHealthAnimation", lowHealthAnimation);
        health.addProperty("regenerationAnimation", regenerationAnimation);
        health.addProperty("customHeartScale", customHeartScale);
        node.add("health", health);

        node.addProperty("resource_pack_mode", resourcePackTransformMode.name());
    }

    public void deserializeFromJson(JsonObject node) {
        if (node == null) return;
        if (node.has("preset")) {
            try {
                this.activePreset = AnimationPreset.valueOf(node.get("preset").getAsString());
            } catch (Exception ignored) {}
        }
        if (node.has("custom_profile_name")) {
            this.customProfileName = node.get("custom_profile_name").getAsString();
        }

        if (node.has("blocking")) {
            JsonElement elem = node.get("blocking");
            if (elem.isJsonObject()) {
                JsonObject b = elem.getAsJsonObject();
                if (b.has("enabled")) enableOldBlocking = b.get("enabled").getAsBoolean();
                if (b.has("style")) {
                    try { blockingStyle = BlockingStyle.valueOf(b.get("style").getAsString()); } catch (Exception ignored) {}
                }
                if (b.has("posX")) blockingPosX = b.get("posX").getAsFloat();
                if (b.has("posY")) blockingPosY = b.get("posY").getAsFloat();
                if (b.has("posZ")) blockingPosZ = b.get("posZ").getAsFloat();
                if (b.has("rotX")) blockingRotX = b.get("rotX").getAsFloat();
                if (b.has("rotY")) blockingRotY = b.get("rotY").getAsFloat();
                if (b.has("rotZ")) blockingRotZ = b.get("rotZ").getAsFloat();
                if (b.has("scale")) blockingScale = b.get("scale").getAsFloat();
                if (b.has("speed")) blockingSpeed = b.get("speed").getAsFloat();
                if (b.has("rightHand")) rightHandBlocking = b.get("rightHand").getAsBoolean();
                if (b.has("leftHand")) leftHandBlocking = b.get("leftHand").getAsBoolean();
                if (b.has("firstPerson")) firstPersonBlocking = b.get("firstPerson").getAsBoolean();
                if (b.has("thirdPerson")) thirdPersonBlocking = b.get("thirdPerson").getAsBoolean();
            }
        }

        if (node.has("blockhit")) {
            JsonElement elem = node.get("blockhit");
            if (elem.isJsonObject()) {
                JsonObject bh = elem.getAsJsonObject();
                if (bh.has("enabled")) enableOldBlockhit = bh.get("enabled").getAsBoolean();
                if (bh.has("style")) {
                    try { blockhitStyle = BlockhitStyle.valueOf(bh.get("style").getAsString()); } catch (Exception ignored) {}
                }
                if (bh.has("swingSpeed")) blockhitSwingSpeed = bh.get("swingSpeed").getAsFloat();
                if (bh.has("swingAngle")) blockhitSwingAngle = bh.get("swingAngle").getAsFloat();
                if (bh.has("offsetX")) blockhitOffsetX = bh.get("offsetX").getAsFloat();
                if (bh.has("offsetY")) blockhitOffsetY = bh.get("offsetY").getAsFloat();
                if (bh.has("rotation")) blockhitRotation = bh.get("rotation").getAsFloat();
                if (bh.has("scale")) blockhitScale = bh.get("scale").getAsFloat();
                if (bh.has("intensity")) blockhitIntensity = bh.get("intensity").getAsFloat();
            }
        }

        if (node.has("swing")) {
            JsonElement elem = node.get("swing");
            if (elem.isJsonObject()) {
                JsonObject sw = elem.getAsJsonObject();
                if (sw.has("enabled")) enableOldSwing = sw.get("enabled").getAsBoolean();
                if (sw.has("style")) {
                    try { swingStyle = SwingStyle.valueOf(sw.get("style").getAsString()); } catch (Exception ignored) {}
                }
                if (sw.has("duration")) swingDuration = sw.get("duration").getAsInt();
                if (sw.has("speed")) swingSpeed = sw.get("speed").getAsFloat();
                if (sw.has("angle")) swingAngle = sw.get("angle").getAsFloat();
                if (sw.has("intensity")) swingIntensity = sw.get("intensity").getAsFloat();
                if (sw.has("customTransform")) customSwingTransform = sw.get("customTransform").getAsBoolean();
                if (sw.has("leftHand")) leftHandSupport = sw.get("leftHand").getAsBoolean();
            }
        }

        if (node.has("sneak")) {
            JsonElement elem = node.get("sneak");
            if (elem.isJsonObject()) {
                JsonObject sn = elem.getAsJsonObject();
                if (sn.has("enabled")) enableOldSneak = sn.get("enabled").getAsBoolean();
                if (sn.has("modelPose")) sneakModelPose = sn.get("modelPose").getAsBoolean();
                if (sn.has("cameraAnimation")) sneakCameraAnimation = sn.get("cameraAnimation").getAsBoolean();
                if (sn.has("transition")) {
                    try { sneakTransition = SneakTransition.valueOf(sn.get("transition").getAsString()); } catch (Exception ignored) {}
                }
                if (sn.has("transitionSpeed")) sneakTransitionSpeed = sn.get("transitionSpeed").getAsFloat();
                if (sn.has("smoothSneak")) smoothSneak = sn.get("smoothSneak").getAsBoolean();
                if (sn.has("instantDown")) instantCameraDown = sn.get("instantDown").getAsBoolean();
                if (sn.has("smoothUp")) smoothCameraUp = sn.get("smoothUp").getAsBoolean();
                if (sn.has("firstPerson")) sneakFirstPerson = sn.get("firstPerson").getAsBoolean();
                if (sn.has("thirdPerson")) sneakThirdPerson = sn.get("thirdPerson").getAsBoolean();
            }
        }

        if (node.has("fishing_rod")) {
            JsonElement elem = node.get("fishing_rod");
            if (elem.isJsonObject()) {
                JsonObject rod = elem.getAsJsonObject();
                if (rod.has("enabled")) enableOldFishingRod = rod.get("enabled").getAsBoolean();
                if (rod.has("angle")) rodAngle = rod.get("angle").getAsFloat();
                if (rod.has("whipAnimation")) rodWhipAnimation = rod.get("whipAnimation").getAsBoolean();
            }
        }

        if (node.has("bow")) {
            JsonElement elem = node.get("bow");
            if (elem.isJsonObject()) {
                JsonObject bow = elem.getAsJsonObject();
                if (bow.has("enabled")) enableOldBow = bow.get("enabled").getAsBoolean();
                if (bow.has("leftOffset")) bowLeftOffset = bow.get("leftOffset").getAsFloat();
                if (bow.has("tilt")) bowTilt = bow.get("tilt").getAsFloat();
            }
        }

        if (node.has("eating_drinking")) {
            JsonElement elem = node.get("eating_drinking");
            if (elem.isJsonObject()) {
                JsonObject ed = elem.getAsJsonObject();
                if (ed.has("enableOldEating")) enableOldEating = ed.get("enableOldEating").getAsBoolean();
                if (ed.has("enableOldDrinking")) enableOldDrinking = ed.get("enableOldDrinking").getAsBoolean();
                if (ed.has("bobbingIntensity")) eatingBobbingIntensity = ed.get("bobbingIntensity").getAsFloat();
            }
        }

        if (node.has("item_switching")) {
            JsonElement elem = node.get("item_switching");
            if (elem.isJsonObject()) {
                JsonObject is = elem.getAsJsonObject();
                if (is.has("enabled")) enableOldItemSwitching = is.get("enabled").getAsBoolean();
                if (is.has("speed")) switchingSpeed = is.get("speed").getAsFloat();
                if (is.has("intensity")) switchingIntensity = is.get("intensity").getAsFloat();
                if (is.has("horizontalOffset")) switchingHorizontalOffset = is.get("horizontalOffset").getAsFloat();
                if (is.has("verticalOffset")) switchingVerticalOffset = is.get("verticalOffset").getAsFloat();
                if (is.has("scale")) switchingScale = is.get("scale").getAsFloat();
            }
        }

        if (node.has("damage_visuals")) {
            JsonElement elem = node.get("damage_visuals");
            if (elem.isJsonObject()) {
                JsonObject dv = elem.getAsJsonObject();
                if (dv.has("enableRedArmor")) enableRedArmor = dv.get("enableRedArmor").getAsBoolean();
                if (dv.has("damageTintColor")) damageTintColor = dv.get("damageTintColor").getAsInt();
                if (dv.has("tintIntensity")) tintIntensity = dv.get("tintIntensity").getAsFloat();
                if (dv.has("flashDuration")) flashDuration = dv.get("flashDuration").getAsInt();
                if (dv.has("smoothFade")) smoothFade = dv.get("smoothFade").getAsBoolean();
                if (dv.has("armorOnly")) armorOnly = dv.get("armorOnly").getAsBoolean();
                if (dv.has("bodyAndArmor")) bodyAndArmor = dv.get("bodyAndArmor").getAsBoolean();
            }
        }

        if (node.has("health")) {
            JsonElement elem = node.get("health");
            if (elem.isJsonObject()) {
                JsonObject h = elem.getAsJsonObject();
                if (h.has("enabled")) enableOldHealth = h.get("enabled").getAsBoolean();
                if (h.has("classicHeartStyle")) classicHeartStyle = h.get("classicHeartStyle").getAsBoolean();
                if (h.has("heartFlash")) heartFlash = h.get("heartFlash").getAsBoolean();
                if (h.has("heartSpacing")) heartSpacing = h.get("heartSpacing").getAsInt();
                if (h.has("absorptionStyle")) absorptionStyle = h.get("absorptionStyle").getAsBoolean();
                if (h.has("lowHealthAnimation")) lowHealthAnimation = h.get("lowHealthAnimation").getAsBoolean();
                if (h.has("regenerationAnimation")) regenerationAnimation = h.get("regenerationAnimation").getAsBoolean();
                if (h.has("customHeartScale")) customHeartScale = h.get("customHeartScale").getAsFloat();
            }
        }

        if (node.has("resource_pack_mode")) {
            try {
                this.resourcePackTransformMode = ResourcePackTransformMode.valueOf(node.get("resource_pack_mode").getAsString());
            } catch (Exception ignored) {}
        }
    }

    // Getters and Setters
    public AnimationPreset getActivePreset() { return activePreset; }
    public void setActivePreset(AnimationPreset activePreset) { this.activePreset = activePreset; }

    public String getCustomProfileName() { return customProfileName; }
    public void setCustomProfileName(String customProfileName) { this.customProfileName = customProfileName; }

    public boolean isEnableOldBlocking() { return enableOldBlocking; }
    public void setEnableOldBlocking(boolean enableOldBlocking) { this.enableOldBlocking = enableOldBlocking; }

    public BlockingStyle getBlockingStyle() { return blockingStyle; }
    public void setBlockingStyle(BlockingStyle blockingStyle) { this.blockingStyle = blockingStyle; }

    public float getBlockingPosX() { return blockingPosX; }
    public void setBlockingPosX(float blockingPosX) { this.blockingPosX = blockingPosX; }

    public float getBlockingPosY() { return blockingPosY; }
    public void setBlockingPosY(float blockingPosY) { this.blockingPosY = blockingPosY; }

    public float getBlockingPosZ() { return blockingPosZ; }
    public void setBlockingPosZ(float blockingPosZ) { this.blockingPosZ = blockingPosZ; }

    public float getBlockingRotX() { return blockingRotX; }
    public void setBlockingRotX(float blockingRotX) { this.blockingRotX = blockingRotX; }

    public float getBlockingRotY() { return blockingRotY; }
    public void setBlockingRotY(float blockingRotY) { this.blockingRotY = blockingRotY; }

    public float getBlockingRotZ() { return blockingRotZ; }
    public void setBlockingRotZ(float blockingRotZ) { this.blockingRotZ = blockingRotZ; }

    public float getBlockingScale() { return blockingScale; }
    public void setBlockingScale(float blockingScale) { this.blockingScale = blockingScale; }

    public float getBlockingSpeed() { return blockingSpeed; }
    public void setBlockingSpeed(float blockingSpeed) { this.blockingSpeed = blockingSpeed; }

    public boolean isRightHandBlocking() { return rightHandBlocking; }
    public void setRightHandBlocking(boolean rightHandBlocking) { this.rightHandBlocking = rightHandBlocking; }

    public boolean isLeftHandBlocking() { return leftHandBlocking; }
    public void setLeftHandBlocking(boolean leftHandBlocking) { this.leftHandBlocking = leftHandBlocking; }

    public boolean isFirstPersonBlocking() { return firstPersonBlocking; }
    public void setFirstPersonBlocking(boolean firstPersonBlocking) { this.firstPersonBlocking = firstPersonBlocking; }

    public boolean isThirdPersonBlocking() { return thirdPersonBlocking; }
    public void setThirdPersonBlocking(boolean thirdPersonBlocking) { this.thirdPersonBlocking = thirdPersonBlocking; }

    public boolean isEnableOldBlockhit() { return enableOldBlockhit; }
    public void setEnableOldBlockhit(boolean enableOldBlockhit) { this.enableOldBlockhit = enableOldBlockhit; }

    public BlockhitStyle getBlockhitStyle() { return blockhitStyle; }
    public void setBlockhitStyle(BlockhitStyle blockhitStyle) { this.blockhitStyle = blockhitStyle; }

    public float getBlockhitSwingSpeed() { return blockhitSwingSpeed; }
    public void setBlockhitSwingSpeed(float blockhitSwingSpeed) { this.blockhitSwingSpeed = blockhitSwingSpeed; }

    public float getBlockhitSwingAngle() { return blockhitSwingAngle; }
    public void setBlockhitSwingAngle(float blockhitSwingAngle) { this.blockhitSwingAngle = blockhitSwingAngle; }

    public float getBlockhitOffsetX() { return blockhitOffsetX; }
    public void setBlockhitOffsetX(float blockhitOffsetX) { this.blockhitOffsetX = blockhitOffsetX; }

    public float getBlockhitOffsetY() { return blockhitOffsetY; }
    public void setBlockhitOffsetY(float blockhitOffsetY) { this.blockhitOffsetY = blockhitOffsetY; }

    public float getBlockhitRotation() { return blockhitRotation; }
    public void setBlockhitRotation(float blockhitRotation) { this.blockhitRotation = blockhitRotation; }

    public float getBlockhitScale() { return blockhitScale; }
    public void setBlockhitScale(float blockhitScale) { this.blockhitScale = blockhitScale; }

    public float getBlockhitIntensity() { return blockhitIntensity; }
    public void setBlockhitIntensity(float blockhitIntensity) { this.blockhitIntensity = blockhitIntensity; }

    public boolean isEnableOldSwing() { return enableOldSwing; }
    public void setEnableOldSwing(boolean enableOldSwing) { this.enableOldSwing = enableOldSwing; }

    public SwingStyle getSwingStyle() { return swingStyle; }
    public void setSwingStyle(SwingStyle swingStyle) { this.swingStyle = swingStyle; }

    public int getSwingDuration() { return swingDuration; }
    public void setSwingDuration(int swingDuration) { this.swingDuration = swingDuration; }

    public float getSwingSpeed() { return swingSpeed; }
    public void setSwingSpeed(float swingSpeed) { this.swingSpeed = swingSpeed; }

    public float getSwingAngle() { return swingAngle; }
    public void setSwingAngle(float swingAngle) { this.swingAngle = swingAngle; }

    public float getSwingIntensity() { return swingIntensity; }
    public void setSwingIntensity(float swingIntensity) { this.swingIntensity = swingIntensity; }

    public boolean isCustomSwingTransform() { return customSwingTransform; }
    public void setCustomSwingTransform(boolean customSwingTransform) { this.customSwingTransform = customSwingTransform; }

    public boolean isLeftHandSupport() { return leftHandSupport; }
    public void setLeftHandSupport(boolean leftHandSupport) { this.leftHandSupport = leftHandSupport; }

    public boolean isEnableOldSneak() { return enableOldSneak; }
    public void setEnableOldSneak(boolean enableOldSneak) { this.enableOldSneak = enableOldSneak; }

    public boolean isSneakModelPose() { return sneakModelPose; }
    public void setSneakModelPose(boolean sneakModelPose) { this.sneakModelPose = sneakModelPose; }

    public boolean isSneakCameraAnimation() { return sneakCameraAnimation; }
    public void setSneakCameraAnimation(boolean sneakCameraAnimation) { this.sneakCameraAnimation = sneakCameraAnimation; }

    public SneakTransition getSneakTransition() { return sneakTransition; }
    public void setSneakTransition(SneakTransition sneakTransition) { this.sneakTransition = sneakTransition; }

    public float getSneakTransitionSpeed() { return sneakTransitionSpeed; }
    public void setSneakTransitionSpeed(float sneakTransitionSpeed) { this.sneakTransitionSpeed = sneakTransitionSpeed; }

    public boolean isSmoothSneak() { return smoothSneak; }
    public void setSmoothSneak(boolean smoothSneak) { this.smoothSneak = smoothSneak; }

    public boolean isInstantCameraDown() { return instantCameraDown; }
    public void setInstantCameraDown(boolean instantCameraDown) { this.instantCameraDown = instantCameraDown; }

    public boolean isSmoothCameraUp() { return smoothCameraUp; }
    public void setSmoothCameraUp(boolean smoothCameraUp) { this.smoothCameraUp = smoothCameraUp; }

    public boolean isSneakFirstPerson() { return sneakFirstPerson; }
    public void setSneakFirstPerson(boolean sneakFirstPerson) { this.sneakFirstPerson = sneakFirstPerson; }

    public boolean isSneakThirdPerson() { return sneakThirdPerson; }
    public void setSneakThirdPerson(boolean sneakThirdPerson) { this.sneakThirdPerson = sneakThirdPerson; }

    public boolean isEnableOldFishingRod() { return enableOldFishingRod; }
    public void setEnableOldFishingRod(boolean enableOldFishingRod) { this.enableOldFishingRod = enableOldFishingRod; }

    public float getRodAngle() { return rodAngle; }
    public void setRodAngle(float rodAngle) { this.rodAngle = rodAngle; }

    public boolean isRodWhipAnimation() { return rodWhipAnimation; }
    public void setRodWhipAnimation(boolean rodWhipAnimation) { this.rodWhipAnimation = rodWhipAnimation; }

    public boolean isEnableOldBow() { return enableOldBow; }
    public void setEnableOldBow(boolean enableOldBow) { this.enableOldBow = enableOldBow; }

    public float getBowLeftOffset() { return bowLeftOffset; }
    public void setBowLeftOffset(float bowLeftOffset) { this.bowLeftOffset = bowLeftOffset; }

    public float getBowTilt() { return bowTilt; }
    public void setBowTilt(float bowTilt) { this.bowTilt = bowTilt; }

    public boolean isEnableOldEating() { return enableOldEating; }
    public void setEnableOldEating(boolean enableOldEating) { this.enableOldEating = enableOldEating; }

    public boolean isEnableOldDrinking() { return enableOldDrinking; }
    public void setEnableOldDrinking(boolean enableOldDrinking) { this.enableOldDrinking = enableOldDrinking; }

    public float getEatingBobbingIntensity() { return eatingBobbingIntensity; }
    public void setEatingBobbingIntensity(float eatingBobbingIntensity) { this.eatingBobbingIntensity = eatingBobbingIntensity; }

    public boolean isEnableOldItemSwitching() { return enableOldItemSwitching; }
    public void setEnableOldItemSwitching(boolean enableOldItemSwitching) { this.enableOldItemSwitching = enableOldItemSwitching; }

    public float getSwitchingSpeed() { return switchingSpeed; }
    public void setSwitchingSpeed(float switchingSpeed) { this.switchingSpeed = switchingSpeed; }

    public float getSwitchingIntensity() { return switchingIntensity; }
    public void setSwitchingIntensity(float switchingIntensity) { this.switchingIntensity = switchingIntensity; }

    public float getSwitchingHorizontalOffset() { return switchingHorizontalOffset; }
    public void setSwitchingHorizontalOffset(float switchingHorizontalOffset) { this.switchingHorizontalOffset = switchingHorizontalOffset; }

    public float getSwitchingVerticalOffset() { return switchingVerticalOffset; }
    public void setSwitchingVerticalOffset(float switchingVerticalOffset) { this.switchingVerticalOffset = switchingVerticalOffset; }

    public float getSwitchingScale() { return switchingScale; }
    public void setSwitchingScale(float switchingScale) { this.switchingScale = switchingScale; }

    public boolean isEnableRedArmor() { return enableRedArmor; }
    public void setEnableRedArmor(boolean enableRedArmor) { this.enableRedArmor = enableRedArmor; }

    public int getDamageTintColor() { return damageTintColor; }
    public void setDamageTintColor(int damageTintColor) { this.damageTintColor = damageTintColor; }

    public float getTintIntensity() { return tintIntensity; }
    public void setTintIntensity(float tintIntensity) { this.tintIntensity = tintIntensity; }

    public int getFlashDuration() { return flashDuration; }
    public void setFlashDuration(int flashDuration) { this.flashDuration = flashDuration; }

    public boolean isSmoothFade() { return smoothFade; }
    public void setSmoothFade(boolean smoothFade) { this.smoothFade = smoothFade; }

    public boolean isArmorOnly() { return armorOnly; }
    public void setArmorOnly(boolean armorOnly) { this.armorOnly = armorOnly; }

    public boolean isBodyAndArmor() { return bodyAndArmor; }
    public void setBodyAndArmor(boolean bodyAndArmor) { this.bodyAndArmor = bodyAndArmor; }

    public boolean isEnableOldHealth() { return enableOldHealth; }
    public void setEnableOldHealth(boolean enableOldHealth) { this.enableOldHealth = enableOldHealth; }

    public boolean isClassicHeartStyle() { return classicHeartStyle; }
    public void setClassicHeartStyle(boolean classicHeartStyle) { this.classicHeartStyle = classicHeartStyle; }

    public boolean isHeartFlash() { return heartFlash; }
    public void setHeartFlash(boolean heartFlash) { this.heartFlash = heartFlash; }

    public int getHeartSpacing() { return heartSpacing; }
    public void setHeartSpacing(int heartSpacing) { this.heartSpacing = heartSpacing; }

    public boolean isAbsorptionStyle() { return absorptionStyle; }
    public void setAbsorptionStyle(boolean absorptionStyle) { this.absorptionStyle = absorptionStyle; }

    public boolean isLowHealthAnimation() { return lowHealthAnimation; }
    public void setLowHealthAnimation(boolean lowHealthAnimation) { this.lowHealthAnimation = lowHealthAnimation; }

    public boolean isRegenerationAnimation() { return regenerationAnimation; }
    public void setRegenerationAnimation(boolean regenerationAnimation) { this.regenerationAnimation = regenerationAnimation; }

    public float getCustomHeartScale() { return customHeartScale; }
    public void setCustomHeartScale(float customHeartScale) { this.customHeartScale = customHeartScale; }

    public ResourcePackTransformMode getResourcePackTransformMode() { return resourcePackTransformMode; }
    public void setResourcePackTransformMode(ResourcePackTransformMode mode) { this.resourcePackTransformMode = mode; }
}
