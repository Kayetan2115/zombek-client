package pl.zombek.client.core.modules.visual.oneseven;

import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

/**
 * 1.7 Visuals Module recreating classic animations, item positions, and visual behaviors
 * from Minecraft 1.7 and 1.8 (Lunar Client style).
 * Purely client-side visual presentation; zero impact on server combat, reach, cooldowns, or hitboxes.
 */
public class OneSevenVisualsModule extends AbstractModule {
    private static OneSevenVisualsModule instance;

    // --- 1. Presets Group ---
    private final EnumSetting<AnimationPreset> presetSetting;
    private final BooleanSetting applyPresetAction;
    private final BooleanSetting resetProfileAction;

    // --- 2. Blocking Group ---
    private final BooleanSetting enableOldBlockingSetting;
    private final EnumSetting<BlockingStyle> blockingStyleSetting;
    private final NumberSetting blockingPosXSetting;
    private final NumberSetting blockingPosYSetting;
    private final NumberSetting blockingPosZSetting;
    private final NumberSetting blockingRotXSetting;
    private final NumberSetting blockingRotYSetting;
    private final NumberSetting blockingRotZSetting;
    private final NumberSetting blockingScaleSetting;
    private final NumberSetting blockingSpeedSetting;
    private final BooleanSetting rightHandBlockingSetting;
    private final BooleanSetting leftHandBlockingSetting;
    private final BooleanSetting firstPersonBlockingSetting;
    private final BooleanSetting thirdPersonBlockingSetting;

    // --- 3. Blockhit Group ---
    private final BooleanSetting enableOldBlockhitSetting;
    private final EnumSetting<BlockhitStyle> blockhitStyleSetting;
    private final NumberSetting blockhitSwingSpeedSetting;
    private final NumberSetting blockhitSwingAngleSetting;
    private final NumberSetting blockhitOffsetXSetting;
    private final NumberSetting blockhitOffsetYSetting;
    private final NumberSetting blockhitRotationSetting;
    private final NumberSetting blockhitScaleSetting;
    private final NumberSetting blockhitIntensitySetting;

    // --- 4. Swing Group ---
    private final BooleanSetting enableOldSwingSetting;
    private final EnumSetting<SwingStyle> swingStyleSetting;
    private final NumberSetting swingDurationSetting;
    private final NumberSetting swingSpeedSetting;
    private final NumberSetting swingAngleSetting;
    private final NumberSetting swingIntensitySetting;
    private final BooleanSetting customSwingTransformSetting;
    private final BooleanSetting leftHandSupportSetting;

    // --- 5. Sneak Group ---
    private final BooleanSetting enableOldSneakSetting;
    private final BooleanSetting sneakModelPoseSetting;
    private final BooleanSetting sneakCameraAnimationSetting;
    private final EnumSetting<SneakTransition> sneakTransitionSetting;
    private final NumberSetting sneakTransitionSpeedSetting;
    private final BooleanSetting smoothSneakSetting;
    private final BooleanSetting instantCameraDownSetting;
    private final BooleanSetting smoothCameraUpSetting;
    private final BooleanSetting sneakFirstPersonSetting;
    private final BooleanSetting sneakThirdPersonSetting;

    // --- 6. Item Positions Group ---
    private final EnumSetting<ItemCategory> activeCategorySetting;
    private final BooleanSetting categoryEnabledSetting;
    private final NumberSetting categoryPosXSetting;
    private final NumberSetting categoryPosYSetting;
    private final NumberSetting categoryPosZSetting;
    private final NumberSetting categoryRotXSetting;
    private final NumberSetting categoryRotYSetting;
    private final NumberSetting categoryRotZSetting;
    private final NumberSetting categoryScaleXSetting;
    private final NumberSetting categoryScaleYSetting;
    private final NumberSetting categoryScaleZSetting;
    private final BooleanSetting resetCategoryAction;
    private final BooleanSetting resetAllCategoriesAction;

    // --- 7. Fishing Rod Group ---
    private final BooleanSetting enableOldFishingRodSetting;
    private final NumberSetting rodAngleSetting;
    private final BooleanSetting rodWhipAnimationSetting;

    // --- 8. Bow Group ---
    private final BooleanSetting enableOldBowSetting;
    private final NumberSetting bowLeftOffsetSetting;
    private final NumberSetting bowTiltSetting;

    // --- 9. Eating & Drinking Group ---
    private final BooleanSetting enableOldEatingSetting;
    private final BooleanSetting enableOldDrinkingSetting;
    private final NumberSetting eatingBobbingIntensitySetting;

    // --- 10. Item Switching Group ---
    private final BooleanSetting enableOldItemSwitchingSetting;
    private final NumberSetting switchingSpeedSetting;
    private final NumberSetting switchingIntensitySetting;
    private final NumberSetting switchingHorizontalOffsetSetting;
    private final NumberSetting switchingVerticalOffsetSetting;
    private final NumberSetting switchingScaleSetting;

    // --- 11. Damage Visuals Group ---
    private final BooleanSetting enableRedArmorSetting;
    private final ColorSetting damageTintColorSetting;
    private final NumberSetting tintIntensitySetting;
    private final NumberSetting flashDurationSetting;
    private final BooleanSetting smoothFadeSetting;
    private final BooleanSetting armorOnlySetting;
    private final BooleanSetting bodyAndArmorSetting;

    // --- 12. Health Group ---
    private final BooleanSetting enableOldHealthSetting;
    private final BooleanSetting classicHeartStyleSetting;
    private final BooleanSetting heartFlashSetting;
    private final NumberSetting heartSpacingSetting;
    private final BooleanSetting absorptionStyleSetting;
    private final BooleanSetting lowHealthAnimationSetting;
    private final BooleanSetting regenerationAnimationSetting;
    private final NumberSetting customHeartScaleSetting;

    // --- 13. Resource Pack Group ---
    private final EnumSetting<ResourcePackTransformMode> resourcePackModeSetting;

    public OneSevenVisualsModule() {
        super(
                "one_seven_visuals",
                "1.7 Visuals",
                "Klasyczne animacje, pozycje przedmiotów i zachowania z Minecraft 1.7 i 1.8 (Old Blocking, Blockhit, Swing, Sneak, Red Armor).",
                ModuleCategory.VISUAL
        );
        instance = this;

        // 1. Presets
        SettingGroup presetsGroup = new SettingGroup("presets", "Profile i Presety");
        this.presetSetting = presetsGroup.addSetting(new EnumSetting<>(
                "preset", "Aktywny Preset", "Wybierz gotowy profil animacji lub Custom", AnimationPreset.MINECRAFT_1_7
        ));
        this.applyPresetAction = presetsGroup.addSetting(new BooleanSetting(
                "apply_preset_action", "Zastosuj Preset", "Wymuś zaaplikowanie domyślnych wartości wybranego presetu", false
        ));
        this.resetProfileAction = presetsGroup.addSetting(new BooleanSetting(
                "reset_profile_action", "Zresetuj Profil", "Przywróć domyślne ustawienia profilu", false
        ));
        registerGroup(presetsGroup);

        // 2. Blocking
        SettingGroup blockingGroup = new SettingGroup("blocking", "Old Blocking (Blokowanie Mieczem)");
        this.enableOldBlockingSetting = blockingGroup.addSetting(new BooleanSetting(
                "enable_blocking", "Włącz Old Blocking", "Wizualna poza blokowania mieczem (Lunar style)", true
        ));
        this.blockingStyleSetting = blockingGroup.addSetting(new EnumSetting<>(
                "blocking_style", "Styl Blokowania", "Wariant pozy blokowania", BlockingStyle.VANILLA_1_7
        ));
        this.blockingPosXSetting = blockingGroup.addSetting(new NumberSetting("blocking_pos_x", "Pozycja X", "Przesunięcie w poziomie", -0.14, -1.0, 1.0, 0.01));
        this.blockingPosYSetting = blockingGroup.addSetting(new NumberSetting("blocking_pos_y", "Pozycja Y", "Przesunięcie w pionie", 0.15, -1.0, 1.0, 0.01));
        this.blockingPosZSetting = blockingGroup.addSetting(new NumberSetting("blocking_pos_z", "Pozycja Z", "Przesunięcie w głąb", -0.2, -1.0, 1.0, 0.01));
        this.blockingRotXSetting = blockingGroup.addSetting(new NumberSetting("blocking_rot_x", "Rotacja X", "Kąt pochylenia", -102.0, -180.0, 180.0, 1.0));
        this.blockingRotYSetting = blockingGroup.addSetting(new NumberSetting("blocking_rot_y", "Rotacja Y", "Kąt obrotu", -59.0, -180.0, 180.0, 1.0));
        this.blockingRotZSetting = blockingGroup.addSetting(new NumberSetting("blocking_rot_z", "Rotacja Z", "Kąt przechyłu", 79.0, -180.0, 180.0, 1.0));
        this.blockingScaleSetting = blockingGroup.addSetting(new NumberSetting("blocking_scale", "Skala", "Mnożnik skali miecza podczas bloku", 1.0, 0.5, 2.0, 0.05));
        this.blockingSpeedSetting = blockingGroup.addSetting(new NumberSetting("blocking_speed", "Prędkość", "Szybkość przejścia do pozy blokowania", 1.0, 0.1, 3.0, 0.1));
        this.rightHandBlockingSetting = blockingGroup.addSetting(new BooleanSetting("right_hand_blocking", "Prawa Ręka", "Obsługa prawej ręki", true));
        this.leftHandBlockingSetting = blockingGroup.addSetting(new BooleanSetting("left_hand_blocking", "Lewa Ręka", "Obsługa lewej ręki", true));
        this.firstPersonBlockingSetting = blockingGroup.addSetting(new BooleanSetting("first_person_blocking", "Pierwsza Osoba", "Renderowanie w widoku pierwszej osoby", true));
        this.thirdPersonBlockingSetting = blockingGroup.addSetting(new BooleanSetting("third_person_blocking", "Trzecia Osoba", "Renderowanie pozy w widoku zza pleców", true));
        registerGroup(blockingGroup);

        // 3. Blockhit
        SettingGroup blockhitGroup = new SettingGroup("blockhit", "Old Blockhit (Blok + Zamach)");
        this.enableOldBlockhitSetting = blockhitGroup.addSetting(new BooleanSetting(
                "enable_blockhit", "Włącz Old Blockhit", "Animacja uderzenia mieczem w trakcie blokowania", true
        ));
        this.blockhitStyleSetting = blockhitGroup.addSetting(new EnumSetting<>(
                "blockhit_style", "Styl Blockhitu", "Wariant animacji uderzenia z blokiem", BlockhitStyle.CLASSIC_1_7
        ));
        this.blockhitSwingSpeedSetting = blockhitGroup.addSetting(new NumberSetting("blockhit_speed", "Prędkość Zamachu", "Mnożnik tempa zamachu", 1.0, 0.2, 3.0, 0.1));
        this.blockhitSwingAngleSetting = blockhitGroup.addSetting(new NumberSetting("blockhit_angle", "Kąt Zamachu", "Amplituda kąta uderzenia", 1.0, 0.2, 3.0, 0.1));
        this.blockhitOffsetXSetting = blockhitGroup.addSetting(new NumberSetting("blockhit_offset_x", "Przesunięcie X", "Dodatkowe przesunięcie poziome", 0.0, -0.5, 0.5, 0.02));
        this.blockhitOffsetYSetting = blockhitGroup.addSetting(new NumberSetting("blockhit_offset_y", "Przesunięcie Y", "Dodatkowe przesunięcie pionowe", 0.0, -0.5, 0.5, 0.02));
        this.blockhitRotationSetting = blockhitGroup.addSetting(new NumberSetting("blockhit_rotation", "Kąt Dodatkowy", "Dodatkowy obrót miecza", 0.0, -90.0, 90.0, 5.0));
        this.blockhitScaleSetting = blockhitGroup.addSetting(new NumberSetting("blockhit_scale", "Skala Blockhit", "Mnożnik skali podczas zamachu", 1.0, 0.5, 2.0, 0.05));
        this.blockhitIntensitySetting = blockhitGroup.addSetting(new NumberSetting("blockhit_intensity", "Intensywność", "Siła animacji blockhit", 1.0, 0.1, 2.5, 0.1));
        registerGroup(blockhitGroup);

        // 4. Swing
        SettingGroup swingGroup = new SettingGroup("swing", "Old Swing (Klasyczny Zamach Ręki)");
        this.enableOldSwingSetting = swingGroup.addSetting(new BooleanSetting("enable_swing", "Włącz Old Swing", "Klasyczny zamach ręki niezależny od attack speed", true));
        this.swingStyleSetting = swingGroup.addSetting(new EnumSetting<>("swing_style", "Styl Zamachu", "Wariant zamachu", SwingStyle.VANILLA_1_7));
        this.swingDurationSetting = swingGroup.addSetting(new NumberSetting("swing_duration", "Czas Trwania (ticki)", "Długość zamachu", 6.0, 2.0, 20.0, 1.0));
        this.swingSpeedSetting = swingGroup.addSetting(new NumberSetting("swing_speed", "Prędkość", "Szybkość animacji", 1.0, 0.2, 3.0, 0.1));
        this.swingAngleSetting = swingGroup.addSetting(new NumberSetting("swing_angle", "Kąt", "Amplituda kąta", 1.0, 0.2, 2.5, 0.1));
        this.swingIntensitySetting = swingGroup.addSetting(new NumberSetting("swing_intensity", "Intensywność", "Głębokość zamachu", 1.0, 0.2, 2.5, 0.1));
        this.customSwingTransformSetting = swingGroup.addSetting(new BooleanSetting("custom_swing_transform", "Własna Transformacja", "Własne współrzędne zamachu", false));
        this.leftHandSupportSetting = swingGroup.addSetting(new BooleanSetting("left_hand_support", "Wsparcie Lewej Ręki", "Obsługa zamachu lewą ręką", true));
        registerGroup(swingGroup);

        // 5. Sneak
        SettingGroup sneakGroup = new SettingGroup("sneak", "Old Sneak (Klasyczna Animacja Kucania)");
        this.enableOldSneakSetting = sneakGroup.addSetting(new BooleanSetting("enable_sneak", "Włącz Old Sneak", "Klasyczne zachowanie kamery i modelu przy kucaniu", true));
        this.sneakModelPoseSetting = sneakGroup.addSetting(new BooleanSetting("sneak_model_pose", "Poza Modelu", "Klasyczne ułożenie rąk i tułowia w trzeciej osobie", true));
        this.sneakCameraAnimationSetting = sneakGroup.addSetting(new BooleanSetting("sneak_camera_anim", "Animacja Kamery", "Szybkie/natychmiastowe obniżenie wysokości oczu", true));
        this.sneakTransitionSetting = sneakGroup.addSetting(new EnumSetting<>("sneak_transition", "Styl Przejścia", "Sposób obniżania kamery", SneakTransition.INSTANT_DOWN_SMOOTH_UP));
        this.sneakTransitionSpeedSetting = sneakGroup.addSetting(new NumberSetting("sneak_speed", "Szybkość Przejścia", "Mnożnik tempa przejścia", 1.0, 0.2, 3.0, 0.1));
        this.smoothSneakSetting = sneakGroup.addSetting(new BooleanSetting("smooth_sneak", "Płynne Kucanie", "Płynna interpolacja zamiast natychmiastowego skoku", false));
        this.instantCameraDownSetting = sneakGroup.addSetting(new BooleanSetting("instant_camera_down", "Natychmiast w Dół", "Błyskawiczny skok kamery w dół", true));
        this.smoothCameraUpSetting = sneakGroup.addSetting(new BooleanSetting("smooth_camera_up", "Płynnie w Górę", "Łagodny powrót kamery w górę po wstaniu", true));
        this.sneakFirstPersonSetting = sneakGroup.addSetting(new BooleanSetting("sneak_first_person", "Pierwsza Osoba", "Działanie w widoku FPP", true));
        this.sneakThirdPersonSetting = sneakGroup.addSetting(new BooleanSetting("sneak_third_person", "Trzecia Osoba", "Działanie w widoku TPP", true));
        registerGroup(sneakGroup);

        // 6. Item Positions
        SettingGroup itemPositionsGroup = new SettingGroup("item_positions", "Pozycje Przedmiotów (Item Transforms)");
        this.activeCategorySetting = itemPositionsGroup.addSetting(new EnumSetting<>("active_category", "Kategoria do Edycji", "Wybierz kategorię przedmiotu", ItemCategory.SWORD));
        this.categoryEnabledSetting = itemPositionsGroup.addSetting(new BooleanSetting("cat_enabled", "Włącz dla Kategorii", "Aktywuj niestandardową pozycję", false));
        this.categoryPosXSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_pos_x", "Przesunięcie X", "Pozycja w osi X", 0.0, -1.0, 1.0, 0.01));
        this.categoryPosYSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_pos_y", "Przesunięcie Y", "Pozycja w osi Y", 0.0, -1.0, 1.0, 0.01));
        this.categoryPosZSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_pos_z", "Przesunięcie Z", "Pozycja w osi Z", 0.0, -1.0, 1.0, 0.01));
        this.categoryRotXSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_rot_x", "Kąt X", "Rotacja w osi X", 0.0, -180.0, 180.0, 1.0));
        this.categoryRotYSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_rot_y", "Kąt Y", "Rotacja w osi Y", 0.0, -180.0, 180.0, 1.0));
        this.categoryRotZSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_rot_z", "Kąt Z", "Rotacja w osi Z", 0.0, -180.0, 180.0, 1.0));
        this.categoryScaleXSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_scale_x", "Skala X", "Mnożnik skali X", 1.0, 0.2, 3.0, 0.05));
        this.categoryScaleYSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_scale_y", "Skala Y", "Mnożnik skali Y", 1.0, 0.2, 3.0, 0.05));
        this.categoryScaleZSetting = itemPositionsGroup.addSetting(new NumberSetting("cat_scale_z", "Skala Z", "Mnożnik skali Z", 1.0, 0.2, 3.0, 0.05));
        this.resetCategoryAction = itemPositionsGroup.addSetting(new BooleanSetting("reset_cat_action", "Zresetuj Kategorię", "Przywróć domyślne dla wybranej kategorii", false));
        this.resetAllCategoriesAction = itemPositionsGroup.addSetting(new BooleanSetting("reset_all_cats_action", "Zresetuj Wszystkie", "Przywróć domyślne dla wszystkich kategorii", false));
        registerGroup(itemPositionsGroup);

        // 7. Fishing Rod
        SettingGroup rodGroup = new SettingGroup("fishing_rod", "Wędka (Fishing Rod)");
        this.enableOldFishingRodSetting = rodGroup.addSetting(new BooleanSetting("enable_rod", "Włącz Klasyczną Wędkę", "Klasyczne ułożenie i animacja wyrzutu", true));
        this.rodAngleSetting = rodGroup.addSetting(new NumberSetting("rod_angle", "Kąt Wędki", "Kąt pochylenia", -15.0, -45.0, 45.0, 1.0));
        this.rodWhipAnimationSetting = rodGroup.addSetting(new BooleanSetting("rod_whip_anim", "Animacja Wyrzutu", "Dynamiczny ruch wędki podczas zarzucania", true));
        registerGroup(rodGroup);

        // 8. Bow
        SettingGroup bowGroup = new SettingGroup("bow", "Łuk (Bow)");
        this.enableOldBowSetting = bowGroup.addSetting(new BooleanSetting("enable_bow", "Włącz Klasyczny Łuk", "Klasyczne celowanie i ułożenie łuku", true));
        this.bowLeftOffsetSetting = bowGroup.addSetting(new NumberSetting("bow_offset", "Przesunięcie Boczne", "Pozycja łuku przy naciągu", 0.0, -0.5, 0.5, 0.02));
        this.bowTiltSetting = bowGroup.addSetting(new NumberSetting("bow_tilt", "Przechył Łuku", "Kąt przechyłu przy naciąganiu", 8.0, -30.0, 30.0, 1.0));
        registerGroup(bowGroup);

        // 9. Eating & Drinking
        SettingGroup eatingGroup = new SettingGroup("eating_drinking", "Jedzenie i Picie");
        this.enableOldEatingSetting = eatingGroup.addSetting(new BooleanSetting("enable_eating", "Klasyczne Jedzenie", "Klasyczna animacja konsumpcji potraw", true));
        this.enableOldDrinkingSetting = eatingGroup.addSetting(new BooleanSetting("enable_drinking", "Klasyczne Picie", "Klasyczna animacja picia mikstur", true));
        this.eatingBobbingIntensitySetting = eatingGroup.addSetting(new NumberSetting("eating_bobbing", "Intensywność Ruchu", "Siła kołysania przedmiotu przy ustach", 1.0, 0.1, 2.5, 0.1));
        registerGroup(eatingGroup);

        // 10. Item Switching
        SettingGroup switchingGroup = new SettingGroup("item_switching", "Zmiana Przedmiotów (Hotbar Switching)");
        this.enableOldItemSwitchingSetting = switchingGroup.addSetting(new BooleanSetting("enable_switching", "Włącz Szybką Zmianę", "Klasyczne przejście przy przełączaniu slotów hotbara", true));
        this.switchingSpeedSetting = switchingGroup.addSetting(new NumberSetting("switching_speed", "Prędkość Zmiany", "Szybkość animacji wyciągania przedmiotu", 1.8, 0.5, 4.0, 0.1));
        this.switchingIntensitySetting = switchingGroup.addSetting(new NumberSetting("switching_intensity", "Intensywność", "Głębokość opuszczenia przedmiotu", 1.0, 0.1, 2.0, 0.1));
        this.switchingHorizontalOffsetSetting = switchingGroup.addSetting(new NumberSetting("switching_offset_x", "Przesunięcie X", "Poziome przesunięcie przy zmianie", 0.0, -0.5, 0.5, 0.02));
        this.switchingVerticalOffsetSetting = switchingGroup.addSetting(new NumberSetting("switching_offset_y", "Przesunięcie Y", "Pionowe przesunięcie przy zmianie", 0.0, -0.5, 0.5, 0.02));
        this.switchingScaleSetting = switchingGroup.addSetting(new NumberSetting("switching_scale", "Skala", "Mnożnik skali przy wyciąganiu", 1.0, 0.5, 1.5, 0.05));
        registerGroup(switchingGroup);

        // 11. Damage Visuals
        SettingGroup damageGroup = new SettingGroup("damage_visuals", "Efekty Obrażeń (Red Armor on Hit)");
        this.enableRedArmorSetting = damageGroup.addSetting(new BooleanSetting("enable_red_armor", "Włącz Red Armor", "Klasyczne czerwone zabarwienie zbroi przy otrzymywaniu ciosów (1.7)", true));
        this.damageTintColorSetting = damageGroup.addSetting(new ColorSetting("damage_tint_color", "Kolor Zabarwienia", "Kolor błysku obrażeń", 0xFFFF0000));
        this.tintIntensitySetting = damageGroup.addSetting(new NumberSetting("tint_intensity", "Intensywność", "Przezroczystość/nasycenie zabarwienia", 0.7, 0.1, 1.0, 0.05));
        this.flashDurationSetting = damageGroup.addSetting(new NumberSetting("flash_duration", "Czas Błysku (ticki)", "Długość trwania efektu", 10.0, 2.0, 20.0, 1.0));
        this.smoothFadeSetting = damageGroup.addSetting(new BooleanSetting("smooth_fade", "Płynne Wygaszanie", "Płynne zanikanie koloru", true));
        this.armorOnlySetting = damageGroup.addSetting(new BooleanSetting("armor_only", "Tylko Zbroja", "Barwienie wyłącznie pancerza", false));
        this.bodyAndArmorSetting = damageGroup.addSetting(new BooleanSetting("body_and_armor", "Ciało i Zbroja", "Barwienie ciała i elementów pancerza jednocześnie", true));
        registerGroup(damageGroup);

        // 12. Health
        SettingGroup healthGroup = new SettingGroup("health", "HUD Zdrowia (Old Health Display)");
        this.enableOldHealthSetting = healthGroup.addSetting(new BooleanSetting("enable_old_health", "Włącz Old Health", "Klasyczny styl i animacja serc zdrowia", false));
        this.classicHeartStyleSetting = healthGroup.addSetting(new BooleanSetting("classic_hearts", "Klasyczne Serca", "Klasyczny obrys serc", true));
        this.heartFlashSetting = healthGroup.addSetting(new BooleanSetting("heart_flash", "Miganie Serc", "Klasyczne białe miganie przy obrażeniach", true));
        this.heartSpacingSetting = healthGroup.addSetting(new NumberSetting("heart_spacing", "Odstęp Serc", "Odstęp pikseli pomiędzy sercami", 8.0, 4.0, 16.0, 1.0));
        this.absorptionStyleSetting = healthGroup.addSetting(new BooleanSetting("absorption_style", "Złote Serca Absorpcji", "Klasyczny wygląd serc absorpcji", true));
        this.lowHealthAnimationSetting = healthGroup.addSetting(new BooleanSetting("low_health_anim", "Animacja Niskiego HP", "Drżenie serc przy niskim stanie zdrowia", true));
        this.regenerationAnimationSetting = healthGroup.addSetting(new BooleanSetting("regen_anim", "Animacja Regeneracji", "Klasyczna fala regeneracji", true));
        this.customHeartScaleSetting = healthGroup.addSetting(new NumberSetting("heart_scale", "Skala Serc", "Rozmiar wskaźnika serc", 1.0, 0.5, 2.0, 0.1));
        registerGroup(healthGroup);

        // 13. Resource Pack Compatibility
        SettingGroup rpGroup = new SettingGroup("resource_pack", "Zgodność z Resource Packami");
        this.resourcePackModeSetting = rpGroup.addSetting(new EnumSetting<>(
                "rp_mode", "Tryb Zgodności", "Sposób traktowania modeli z Resource Packów", ResourcePackTransformMode.USE_RESOURCE_PACK
        ));
        registerGroup(rpGroup);
    }

    public static OneSevenVisualsModule getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        syncToSettings();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BlockingAnimationController.getInstance().reset();
        SneakAnimationController.getInstance().reset();
    }

    @Override
    public void onTick() {
        if (!isEnabled()) {
            return;
        }

        // Handle action settings
        if (applyPresetAction.getValue()) {
            AnimationProfileManager.getInstance().applyPreset(presetSetting.getValue());
            syncFromSettings();
            applyPresetAction.setValue(false);
        }

        if (resetProfileAction.getValue()) {
            AnimationProfileManager.getInstance().resetProfile(presetSetting.getValue().name());
            syncFromSettings();
            resetProfileAction.setValue(false);
        }

        if (resetCategoryAction.getValue()) {
            ItemTransformManager.getInstance().resetCategory(activeCategorySetting.getValue());
            syncCategoryFromManager();
            resetCategoryAction.setValue(false);
        }

        if (resetAllCategoriesAction.getValue()) {
            ItemTransformManager.getInstance().resetAllCategories();
            syncCategoryFromManager();
            resetAllCategoriesAction.setValue(false);
        }

        syncToSettings();
    }

    /**
     * Pushes values from UI settings to the active AnimationSettings in manager.
     */
    public void syncToSettings() {
        AnimationSettings s = AnimationProfileManager.getInstance().getCurrentSettings();
        s.setActivePreset(presetSetting.getValue());

        s.setEnableOldBlocking(enableOldBlockingSetting.getValue());
        s.setBlockingStyle(blockingStyleSetting.getValue());
        s.setBlockingPosX(blockingPosXSetting.getValue().floatValue());
        s.setBlockingPosY(blockingPosYSetting.getValue().floatValue());
        s.setBlockingPosZ(blockingPosZSetting.getValue().floatValue());
        s.setBlockingRotX(blockingRotXSetting.getValue().floatValue());
        s.setBlockingRotY(blockingRotYSetting.getValue().floatValue());
        s.setBlockingRotZ(blockingRotZSetting.getValue().floatValue());
        s.setBlockingScale(blockingScaleSetting.getValue().floatValue());
        s.setBlockingSpeed(blockingSpeedSetting.getValue().floatValue());
        s.setRightHandBlocking(rightHandBlockingSetting.getValue());
        s.setLeftHandBlocking(leftHandBlockingSetting.getValue());
        s.setFirstPersonBlocking(firstPersonBlockingSetting.getValue());
        s.setThirdPersonBlocking(thirdPersonBlockingSetting.getValue());

        s.setEnableOldBlockhit(enableOldBlockhitSetting.getValue());
        s.setBlockhitStyle(blockhitStyleSetting.getValue());
        s.setBlockhitSwingSpeed(blockhitSwingSpeedSetting.getValue().floatValue());
        s.setBlockhitSwingAngle(blockhitSwingAngleSetting.getValue().floatValue());
        s.setBlockhitOffsetX(blockhitOffsetXSetting.getValue().floatValue());
        s.setBlockhitOffsetY(blockhitOffsetYSetting.getValue().floatValue());
        s.setBlockhitRotation(blockhitRotationSetting.getValue().floatValue());
        s.setBlockhitScale(blockhitScaleSetting.getValue().floatValue());
        s.setBlockhitIntensity(blockhitIntensitySetting.getValue().floatValue());

        s.setEnableOldSwing(enableOldSwingSetting.getValue());
        s.setSwingStyle(swingStyleSetting.getValue());
        s.setSwingDuration(swingDurationSetting.getValue().intValue());
        s.setSwingSpeed(swingSpeedSetting.getValue().floatValue());
        s.setSwingAngle(swingAngleSetting.getValue().floatValue());
        s.setSwingIntensity(swingIntensitySetting.getValue().floatValue());
        s.setCustomSwingTransform(customSwingTransformSetting.getValue());
        s.setLeftHandSupport(leftHandSupportSetting.getValue());

        s.setEnableOldSneak(enableOldSneakSetting.getValue());
        s.setSneakModelPose(sneakModelPoseSetting.getValue());
        s.setSneakCameraAnimation(sneakCameraAnimationSetting.getValue());
        s.setSneakTransition(sneakTransitionSetting.getValue());
        s.setSneakTransitionSpeed(sneakTransitionSpeedSetting.getValue().floatValue());
        s.setSmoothSneak(smoothSneakSetting.getValue());
        s.setInstantCameraDown(instantCameraDownSetting.getValue());
        s.setSmoothCameraUp(smoothCameraUpSetting.getValue());
        s.setSneakFirstPerson(sneakFirstPersonSetting.getValue());
        s.setSneakThirdPerson(sneakThirdPersonSetting.getValue());

        s.setEnableOldFishingRod(enableOldFishingRodSetting.getValue());
        s.setRodAngle(rodAngleSetting.getValue().floatValue());
        s.setRodWhipAnimation(rodWhipAnimationSetting.getValue());

        s.setEnableOldBow(enableOldBowSetting.getValue());
        s.setBowLeftOffset(bowLeftOffsetSetting.getValue().floatValue());
        s.setBowTilt(bowTiltSetting.getValue().floatValue());

        s.setEnableOldEating(enableOldEatingSetting.getValue());
        s.setEnableOldDrinking(enableOldDrinkingSetting.getValue());
        s.setEatingBobbingIntensity(eatingBobbingIntensitySetting.getValue().floatValue());

        s.setEnableOldItemSwitching(enableOldItemSwitchingSetting.getValue());
        s.setSwitchingSpeed(switchingSpeedSetting.getValue().floatValue());
        s.setSwitchingIntensity(switchingIntensitySetting.getValue().floatValue());
        s.setSwitchingHorizontalOffset(switchingHorizontalOffsetSetting.getValue().floatValue());
        s.setSwitchingVerticalOffset(switchingVerticalOffsetSetting.getValue().floatValue());
        s.setSwitchingScale(switchingScaleSetting.getValue().floatValue());

        s.setEnableRedArmor(enableRedArmorSetting.getValue());
        s.setDamageTintColor(damageTintColorSetting.getValue());
        s.setTintIntensity(tintIntensitySetting.getValue().floatValue());
        s.setFlashDuration(flashDurationSetting.getValue().intValue());
        s.setSmoothFade(smoothFadeSetting.getValue());
        s.setArmorOnly(armorOnlySetting.getValue());
        s.setBodyAndArmor(bodyAndArmorSetting.getValue());

        s.setEnableOldHealth(enableOldHealthSetting.getValue());
        s.setClassicHeartStyle(classicHeartStyleSetting.getValue());
        s.setHeartFlash(heartFlashSetting.getValue());
        s.setHeartSpacing(heartSpacingSetting.getValue().intValue());
        s.setAbsorptionStyle(absorptionStyleSetting.getValue());
        s.setLowHealthAnimation(lowHealthAnimationSetting.getValue());
        s.setRegenerationAnimation(regenerationAnimationSetting.getValue());
        s.setCustomHeartScale(customHeartScaleSetting.getValue().floatValue());

        s.setResourcePackTransformMode(resourcePackModeSetting.getValue());

        // Sync category settings to manager
        CategoryTransform ct = ItemTransformManager.getInstance().getTransform(activeCategorySetting.getValue());
        ct.setEnabled(categoryEnabledSetting.getValue());
        TransformOffset off = ct.getOffset();
        off.setPosX(categoryPosXSetting.getValue().floatValue());
        off.setPosY(categoryPosYSetting.getValue().floatValue());
        off.setPosZ(categoryPosZSetting.getValue().floatValue());
        off.setRotX(categoryRotXSetting.getValue().floatValue());
        off.setRotY(categoryRotYSetting.getValue().floatValue());
        off.setRotZ(categoryRotZSetting.getValue().floatValue());
        off.setScaleX(categoryScaleXSetting.getValue().floatValue());
        off.setScaleY(categoryScaleYSetting.getValue().floatValue());
        off.setScaleZ(categoryScaleZSetting.getValue().floatValue());
    }

    /**
     * Pulls values from manager to UI settings.
     */
    public void syncFromSettings() {
        AnimationSettings s = AnimationProfileManager.getInstance().getCurrentSettings();
        presetSetting.setValue(s.getActivePreset());

        enableOldBlockingSetting.setValue(s.isEnableOldBlocking());
        blockingStyleSetting.setValue(s.getBlockingStyle());
        blockingPosXSetting.setValue((double) s.getBlockingPosX());
        blockingPosYSetting.setValue((double) s.getBlockingPosY());
        blockingPosZSetting.setValue((double) s.getBlockingPosZ());
        blockingRotXSetting.setValue((double) s.getBlockingRotX());
        blockingRotYSetting.setValue((double) s.getBlockingRotY());
        blockingRotZSetting.setValue((double) s.getBlockingRotZ());
        blockingScaleSetting.setValue((double) s.getBlockingScale());
        blockingSpeedSetting.setValue((double) s.getBlockingSpeed());
        rightHandBlockingSetting.setValue(s.isRightHandBlocking());
        leftHandBlockingSetting.setValue(s.isLeftHandBlocking());
        firstPersonBlockingSetting.setValue(s.isFirstPersonBlocking());
        thirdPersonBlockingSetting.setValue(s.isThirdPersonBlocking());

        enableOldBlockhitSetting.setValue(s.isEnableOldBlockhit());
        blockhitStyleSetting.setValue(s.getBlockhitStyle());
        blockhitSwingSpeedSetting.setValue((double) s.getBlockhitSwingSpeed());
        blockhitSwingAngleSetting.setValue((double) s.getBlockhitSwingAngle());
        blockhitOffsetXSetting.setValue((double) s.getBlockhitOffsetX());
        blockhitOffsetYSetting.setValue((double) s.getBlockhitOffsetY());
        blockhitRotationSetting.setValue((double) s.getBlockhitRotation());
        blockhitScaleSetting.setValue((double) s.getBlockhitScale());
        blockhitIntensitySetting.setValue((double) s.getBlockhitIntensity());

        enableOldSwingSetting.setValue(s.isEnableOldSwing());
        swingStyleSetting.setValue(s.getSwingStyle());
        swingDurationSetting.setValue((double) s.getSwingDuration());
        swingSpeedSetting.setValue((double) s.getSwingSpeed());
        swingAngleSetting.setValue((double) s.getSwingAngle());
        swingIntensitySetting.setValue((double) s.getSwingIntensity());
        customSwingTransformSetting.setValue(s.isCustomSwingTransform());
        leftHandSupportSetting.setValue(s.isLeftHandSupport());

        enableOldSneakSetting.setValue(s.isEnableOldSneak());
        sneakModelPoseSetting.setValue(s.isSneakModelPose());
        sneakCameraAnimationSetting.setValue(s.isSneakCameraAnimation());
        sneakTransitionSetting.setValue(s.getSneakTransition());
        sneakTransitionSpeedSetting.setValue((double) s.getSneakTransitionSpeed());
        smoothSneakSetting.setValue(s.isSmoothSneak());
        instantCameraDownSetting.setValue(s.isInstantCameraDown());
        smoothCameraUpSetting.setValue(s.isSmoothCameraUp());
        sneakFirstPersonSetting.setValue(s.isSneakFirstPerson());
        sneakThirdPersonSetting.setValue(s.isSneakThirdPerson());

        enableOldFishingRodSetting.setValue(s.isEnableOldFishingRod());
        rodAngleSetting.setValue((double) s.getRodAngle());
        rodWhipAnimationSetting.setValue(s.isRodWhipAnimation());

        enableOldBowSetting.setValue(s.isEnableOldBow());
        bowLeftOffsetSetting.setValue((double) s.getBowLeftOffset());
        bowTiltSetting.setValue((double) s.getBowTilt());

        enableOldEatingSetting.setValue(s.isEnableOldEating());
        enableOldDrinkingSetting.setValue(s.isEnableOldDrinking());
        eatingBobbingIntensitySetting.setValue((double) s.getEatingBobbingIntensity());

        enableOldItemSwitchingSetting.setValue(s.isEnableOldItemSwitching());
        switchingSpeedSetting.setValue((double) s.getSwitchingSpeed());
        switchingIntensitySetting.setValue((double) s.getSwitchingIntensity());
        switchingHorizontalOffsetSetting.setValue((double) s.getSwitchingHorizontalOffset());
        switchingVerticalOffsetSetting.setValue((double) s.getSwitchingVerticalOffset());
        switchingScaleSetting.setValue((double) s.getSwitchingScale());

        enableRedArmorSetting.setValue(s.isEnableRedArmor());
        damageTintColorSetting.setValue(s.getDamageTintColor());
        tintIntensitySetting.setValue((double) s.getTintIntensity());
        flashDurationSetting.setValue((double) s.getFlashDuration());
        smoothFadeSetting.setValue(s.isSmoothFade());
        armorOnlySetting.setValue(s.isArmorOnly());
        bodyAndArmorSetting.setValue(s.isBodyAndArmor());

        enableOldHealthSetting.setValue(s.isEnableOldHealth());
        classicHeartStyleSetting.setValue(s.isClassicHeartStyle());
        heartFlashSetting.setValue(s.isHeartFlash());
        heartSpacingSetting.setValue((double) s.getHeartSpacing());
        absorptionStyleSetting.setValue(s.isAbsorptionStyle());
        lowHealthAnimationSetting.setValue(s.isLowHealthAnimation());
        regenerationAnimationSetting.setValue(s.isRegenerationAnimation());
        customHeartScaleSetting.setValue((double) s.getCustomHeartScale());

        resourcePackModeSetting.setValue(s.getResourcePackTransformMode());
        syncCategoryFromManager();
    }

    public void syncCategoryFromManager() {
        CategoryTransform ct = ItemTransformManager.getInstance().getTransform(activeCategorySetting.getValue());
        categoryEnabledSetting.setValue(ct.isEnabled());
        TransformOffset off = ct.getOffset();
        categoryPosXSetting.setValue((double) off.getPosX());
        categoryPosYSetting.setValue((double) off.getPosY());
        categoryPosZSetting.setValue((double) off.getPosZ());
        categoryRotXSetting.setValue((double) off.getRotX());
        categoryRotYSetting.setValue((double) off.getRotY());
        categoryRotZSetting.setValue((double) off.getRotZ());
        categoryScaleXSetting.setValue((double) off.getScaleX());
        categoryScaleYSetting.setValue((double) off.getScaleY());
        categoryScaleZSetting.setValue((double) off.getScaleZ());
    }

    // Getters for settings
    public EnumSetting<AnimationPreset> getPresetSetting() { return presetSetting; }
    public BooleanSetting getEnableOldBlockingSetting() { return enableOldBlockingSetting; }
    public BooleanSetting getEnableOldBlockhitSetting() { return enableOldBlockhitSetting; }
    public BooleanSetting getEnableOldSwingSetting() { return enableOldSwingSetting; }
    public BooleanSetting getEnableOldSneakSetting() { return enableOldSneakSetting; }
    public BooleanSetting getEnableRedArmorSetting() { return enableRedArmorSetting; }
    public BooleanSetting getEnableOldHealthSetting() { return enableOldHealthSetting; }
}
