package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;

final class Minecraft189AimAssistFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AimAssistModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration yawSpeedSetting;
    private final SettingRegistry.Registration pitchSpeedSetting;
    private final SettingRegistry.Registration requireSprintSetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingRegistry.Registration requireGroundSetting;
    private final SettingRegistry.Registration requireForwardSetting;
    private final SettingRegistry.Registration requireHoldSetting;
    private final SettingRegistry.Registration prioritizeCrosshairSetting;
    private final SettingRegistry.Registration minDistanceSetting;
    private final SettingRegistry.Registration maxDistanceSetting;
    private final SettingRegistry.Registration maxFovSetting;
    private final SettingRegistry.Registration maxPitchFovSetting;
    private final SettingRegistry.Registration yawOffsetSetting;
    private final SettingRegistry.Registration pitchOffsetSetting;
    private final SettingRegistry.Registration deadZoneSetting;
    private final SettingRegistry.Registration yawEnabledSetting;
    private final SettingRegistry.Registration pitchEnabledSetting;
    private final SettingRegistry.Registration angularEasingSetting;
    private final SettingRegistry.Registration easingStrengthSetting;
    private final SettingPresentationRegistry.Registration yawSpeedPresentation;
    private final SettingPresentationRegistry.Registration pitchSpeedPresentation;
    private final SettingPresentationRegistry.Registration requireSprintPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final SettingPresentationRegistry.Registration requireGroundPresentation;
    private final SettingPresentationRegistry.Registration requireForwardPresentation;
    private final SettingPresentationRegistry.Registration requireHoldPresentation;
    private final SettingPresentationRegistry.Registration prioritizeCrosshairPresentation;
    private final SettingPresentationRegistry.Registration minDistancePresentation;
    private final SettingPresentationRegistry.Registration maxDistancePresentation;
    private final SettingPresentationRegistry.Registration maxFovPresentation;
    private final SettingPresentationRegistry.Registration maxPitchFovPresentation;
    private final SettingPresentationRegistry.Registration yawOffsetPresentation;
    private final SettingPresentationRegistry.Registration pitchOffsetPresentation;
    private final SettingPresentationRegistry.Registration deadZonePresentation;
    private final SettingPresentationRegistry.Registration yawEnabledPresentation;
    private final SettingPresentationRegistry.Registration pitchEnabledPresentation;
    private final SettingPresentationRegistry.Registration angularEasingPresentation;
    private final SettingPresentationRegistry.Registration easingStrengthPresentation;
    private final ModuleSettingRegistry.Registration yawSpeedBinding;
    private final ModuleSettingRegistry.Registration pitchSpeedBinding;
    private final ModuleSettingRegistry.Registration requireSprintBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private final ModuleSettingRegistry.Registration requireGroundBinding;
    private final ModuleSettingRegistry.Registration requireForwardBinding;
    private final ModuleSettingRegistry.Registration requireHoldBinding;
    private final ModuleSettingRegistry.Registration prioritizeCrosshairBinding;
    private final ModuleSettingRegistry.Registration minDistanceBinding;
    private final ModuleSettingRegistry.Registration maxDistanceBinding;
    private final ModuleSettingRegistry.Registration maxFovBinding;
    private final ModuleSettingRegistry.Registration maxPitchFovBinding;
    private final ModuleSettingRegistry.Registration yawOffsetBinding;
    private final ModuleSettingRegistry.Registration pitchOffsetBinding;
    private final ModuleSettingRegistry.Registration deadZoneBinding;
    private final ModuleSettingRegistry.Registration yawEnabledBinding;
    private final ModuleSettingRegistry.Registration pitchEnabledBinding;
    private final ModuleSettingRegistry.Registration angularEasingBinding;
    private final ModuleSettingRegistry.Registration easingStrengthBinding;
    private final SettingRegistry.Registration combinedStepSetting;
    private final SettingRegistry.Registration maxCombinedStepSetting;
    private final SettingPresentationRegistry.Registration combinedStepPresentation;
    private final SettingPresentationRegistry.Registration maxCombinedStepPresentation;
    private final ModuleSettingRegistry.Registration combinedStepBinding;
    private final ModuleSettingRegistry.Registration maxCombinedStepBinding;
    private boolean closed;

    private Minecraft189AimAssistFeature(
            final ModuleController controller,
            final Minecraft189AimAssistModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration yawSpeedSetting,
            final SettingRegistry.Registration pitchSpeedSetting,
            final SettingRegistry.Registration requireSprintSetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingRegistry.Registration requireGroundSetting,
            final SettingRegistry.Registration requireForwardSetting,
            final SettingRegistry.Registration requireHoldSetting,
            final SettingRegistry.Registration prioritizeCrosshairSetting,
            final SettingRegistry.Registration minDistanceSetting,
            final SettingRegistry.Registration maxDistanceSetting,
            final SettingRegistry.Registration maxFovSetting,
            final SettingRegistry.Registration maxPitchFovSetting,
            final SettingRegistry.Registration yawOffsetSetting,
            final SettingRegistry.Registration pitchOffsetSetting,
            final SettingRegistry.Registration deadZoneSetting,
            final SettingRegistry.Registration yawEnabledSetting,
            final SettingRegistry.Registration pitchEnabledSetting,
            final SettingRegistry.Registration angularEasingSetting,
            final SettingRegistry.Registration easingStrengthSetting,
            final SettingPresentationRegistry.Registration yawSpeedPresentation,
            final SettingPresentationRegistry.Registration pitchSpeedPresentation,
            final SettingPresentationRegistry.Registration requireSprintPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final SettingPresentationRegistry.Registration requireGroundPresentation,
            final SettingPresentationRegistry.Registration requireForwardPresentation,
            final SettingPresentationRegistry.Registration requireHoldPresentation,
            final SettingPresentationRegistry.Registration prioritizeCrosshairPresentation,
            final SettingPresentationRegistry.Registration minDistancePresentation,
            final SettingPresentationRegistry.Registration maxDistancePresentation,
            final SettingPresentationRegistry.Registration maxFovPresentation,
            final SettingPresentationRegistry.Registration maxPitchFovPresentation,
            final SettingPresentationRegistry.Registration yawOffsetPresentation,
            final SettingPresentationRegistry.Registration pitchOffsetPresentation,
            final SettingPresentationRegistry.Registration deadZonePresentation,
            final SettingPresentationRegistry.Registration yawEnabledPresentation,
            final SettingPresentationRegistry.Registration pitchEnabledPresentation,
            final SettingPresentationRegistry.Registration angularEasingPresentation,
            final SettingPresentationRegistry.Registration easingStrengthPresentation,
            final ModuleSettingRegistry.Registration yawSpeedBinding,
            final ModuleSettingRegistry.Registration pitchSpeedBinding,
            final ModuleSettingRegistry.Registration requireSprintBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding,
            final ModuleSettingRegistry.Registration requireGroundBinding,
            final ModuleSettingRegistry.Registration requireForwardBinding,
            final ModuleSettingRegistry.Registration requireHoldBinding,
            final ModuleSettingRegistry.Registration prioritizeCrosshairBinding,
            final ModuleSettingRegistry.Registration minDistanceBinding,
            final ModuleSettingRegistry.Registration maxDistanceBinding,
            final ModuleSettingRegistry.Registration maxFovBinding,
            final ModuleSettingRegistry.Registration maxPitchFovBinding,
            final ModuleSettingRegistry.Registration yawOffsetBinding,
            final ModuleSettingRegistry.Registration pitchOffsetBinding,
            final ModuleSettingRegistry.Registration deadZoneBinding,
            final ModuleSettingRegistry.Registration yawEnabledBinding,
            final ModuleSettingRegistry.Registration pitchEnabledBinding,
            final ModuleSettingRegistry.Registration angularEasingBinding,
            final ModuleSettingRegistry.Registration easingStrengthBinding,
            final SettingRegistry.Registration combinedStepSetting,
            final SettingRegistry.Registration maxCombinedStepSetting,
            final SettingPresentationRegistry.Registration combinedStepPresentation,
            final SettingPresentationRegistry.Registration maxCombinedStepPresentation,
            final ModuleSettingRegistry.Registration combinedStepBinding,
            final ModuleSettingRegistry.Registration maxCombinedStepBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.yawSpeedSetting = yawSpeedSetting;
        this.pitchSpeedSetting = pitchSpeedSetting;
        this.requireSprintSetting = requireSprintSetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.requireGroundSetting = requireGroundSetting;
        this.requireForwardSetting = requireForwardSetting;
        this.requireHoldSetting = requireHoldSetting;
        this.prioritizeCrosshairSetting = prioritizeCrosshairSetting;
        this.minDistanceSetting = minDistanceSetting;
        this.maxDistanceSetting = maxDistanceSetting;
        this.maxFovSetting = maxFovSetting;
        this.maxPitchFovSetting = maxPitchFovSetting;
        this.yawOffsetSetting = yawOffsetSetting;
        this.pitchOffsetSetting = pitchOffsetSetting;
        this.deadZoneSetting = deadZoneSetting;
        this.yawEnabledSetting = yawEnabledSetting;
        this.pitchEnabledSetting = pitchEnabledSetting;
        this.angularEasingSetting = angularEasingSetting;
        this.easingStrengthSetting = easingStrengthSetting;
        this.yawSpeedPresentation = yawSpeedPresentation;
        this.pitchSpeedPresentation = pitchSpeedPresentation;
        this.requireSprintPresentation = requireSprintPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.requireGroundPresentation = requireGroundPresentation;
        this.requireForwardPresentation = requireForwardPresentation;
        this.requireHoldPresentation = requireHoldPresentation;
        this.prioritizeCrosshairPresentation = prioritizeCrosshairPresentation;
        this.minDistancePresentation = minDistancePresentation;
        this.maxDistancePresentation = maxDistancePresentation;
        this.maxFovPresentation = maxFovPresentation;
        this.maxPitchFovPresentation = maxPitchFovPresentation;
        this.yawOffsetPresentation = yawOffsetPresentation;
        this.pitchOffsetPresentation = pitchOffsetPresentation;
        this.deadZonePresentation = deadZonePresentation;
        this.yawEnabledPresentation = yawEnabledPresentation;
        this.pitchEnabledPresentation = pitchEnabledPresentation;
        this.angularEasingPresentation = angularEasingPresentation;
        this.easingStrengthPresentation = easingStrengthPresentation;
        this.yawSpeedBinding = yawSpeedBinding;
        this.pitchSpeedBinding = pitchSpeedBinding;
        this.requireSprintBinding = requireSprintBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
        this.requireGroundBinding = requireGroundBinding;
        this.requireForwardBinding = requireForwardBinding;
        this.requireHoldBinding = requireHoldBinding;
        this.prioritizeCrosshairBinding = prioritizeCrosshairBinding;
        this.minDistanceBinding = minDistanceBinding;
        this.maxDistanceBinding = maxDistanceBinding;
        this.maxFovBinding = maxFovBinding;
        this.maxPitchFovBinding = maxPitchFovBinding;
        this.yawOffsetBinding = yawOffsetBinding;
        this.pitchOffsetBinding = pitchOffsetBinding;
        this.deadZoneBinding = deadZoneBinding;
        this.yawEnabledBinding = yawEnabledBinding;
        this.pitchEnabledBinding = pitchEnabledBinding;
        this.angularEasingBinding = angularEasingBinding;
        this.easingStrengthBinding = easingStrengthBinding;
        this.combinedStepSetting = combinedStepSetting;
        this.maxCombinedStepSetting = maxCombinedStepSetting;
        this.combinedStepPresentation = combinedStepPresentation;
        this.maxCombinedStepPresentation = maxCombinedStepPresentation;
        this.combinedStepBinding = combinedStepBinding;
        this.maxCombinedStepBinding = maxCombinedStepBinding;
    }

    static Minecraft189AimAssistFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AimAssistModule module =
                new Minecraft189AimAssistModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration yawSpeedSetting = null;
        SettingRegistry.Registration pitchSpeedSetting = null;
        SettingRegistry.Registration requireSprintSetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingRegistry.Registration requireGroundSetting = null;
        SettingRegistry.Registration requireForwardSetting = null;
        SettingRegistry.Registration requireHoldSetting = null;
        SettingRegistry.Registration prioritizeCrosshairSetting = null;
        SettingRegistry.Registration minDistanceSetting = null;
        SettingRegistry.Registration maxDistanceSetting = null;
        SettingRegistry.Registration maxFovSetting = null;
        SettingRegistry.Registration maxPitchFovSetting = null;
        SettingRegistry.Registration yawOffsetSetting = null;
        SettingRegistry.Registration pitchOffsetSetting = null;
        SettingRegistry.Registration deadZoneSetting = null;
        SettingRegistry.Registration yawEnabledSetting = null;
        SettingRegistry.Registration pitchEnabledSetting = null;
        SettingRegistry.Registration angularEasingSetting = null;
        SettingRegistry.Registration easingStrengthSetting = null;
        SettingPresentationRegistry.Registration yawSpeedPresentation = null;
        SettingPresentationRegistry.Registration pitchSpeedPresentation = null;
        SettingPresentationRegistry.Registration requireSprintPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        SettingPresentationRegistry.Registration requireGroundPresentation = null;
        SettingPresentationRegistry.Registration requireForwardPresentation = null;
        SettingPresentationRegistry.Registration requireHoldPresentation = null;
        SettingPresentationRegistry.Registration prioritizeCrosshairPresentation = null;
        SettingPresentationRegistry.Registration minDistancePresentation = null;
        SettingPresentationRegistry.Registration maxDistancePresentation = null;
        SettingPresentationRegistry.Registration maxFovPresentation = null;
        SettingPresentationRegistry.Registration maxPitchFovPresentation = null;
        SettingPresentationRegistry.Registration yawOffsetPresentation = null;
        SettingPresentationRegistry.Registration pitchOffsetPresentation = null;
        SettingPresentationRegistry.Registration deadZonePresentation = null;
        SettingPresentationRegistry.Registration yawEnabledPresentation = null;
        SettingPresentationRegistry.Registration pitchEnabledPresentation = null;
        SettingPresentationRegistry.Registration angularEasingPresentation = null;
        SettingPresentationRegistry.Registration easingStrengthPresentation = null;
        ModuleSettingRegistry.Registration yawSpeedBinding = null;
        ModuleSettingRegistry.Registration pitchSpeedBinding = null;
        ModuleSettingRegistry.Registration requireSprintBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
        ModuleSettingRegistry.Registration requireGroundBinding = null;
        ModuleSettingRegistry.Registration requireForwardBinding = null;
        ModuleSettingRegistry.Registration requireHoldBinding = null;
        ModuleSettingRegistry.Registration prioritizeCrosshairBinding = null;
        ModuleSettingRegistry.Registration minDistanceBinding = null;
        ModuleSettingRegistry.Registration maxDistanceBinding = null;
        ModuleSettingRegistry.Registration maxFovBinding = null;
        ModuleSettingRegistry.Registration maxPitchFovBinding = null;
        ModuleSettingRegistry.Registration yawOffsetBinding = null;
        ModuleSettingRegistry.Registration pitchOffsetBinding = null;
        ModuleSettingRegistry.Registration deadZoneBinding = null;
        ModuleSettingRegistry.Registration yawEnabledBinding = null;
        ModuleSettingRegistry.Registration pitchEnabledBinding = null;
        ModuleSettingRegistry.Registration angularEasingBinding = null;
        ModuleSettingRegistry.Registration easingStrengthBinding = null;
        SettingRegistry.Registration combinedStepSetting = null;
        SettingRegistry.Registration maxCombinedStepSetting = null;
        SettingPresentationRegistry.Registration combinedStepPresentation = null;
        SettingPresentationRegistry.Registration maxCombinedStepPresentation = null;
        ModuleSettingRegistry.Registration combinedStepBinding = null;
        ModuleSettingRegistry.Registration maxCombinedStepBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AimAssistModule.ID,
                                    "Aim Assist",
                                    "Tracks the certified nearest remote player with configurable activation and range.",
                                    Minecraft189FeatureCatalog
                                            .COMBAT_CATEGORY_ID,
                                    35));
            yawSpeedSetting =
                    settings.register(
                            module.yawSpeedSetting());
            pitchSpeedSetting =
                    settings.register(
                            module.pitchSpeedSetting());
            requireSprintSetting =
                    settings.register(
                            module.requireSprintSetting());
            pauseWhileSneakingSetting =
                    settings.register(
                            module.pauseWhileSneakingSetting());
            requireGroundSetting =
                    settings.register(
                            module.requireGroundSetting());
            requireForwardSetting =
                    settings.register(
                            module.requireForwardSetting());
            requireHoldSetting =
                    settings.register(
                            module.requireHoldSetting());
            prioritizeCrosshairSetting =
                    settings.register(
                            module.prioritizeCrosshairSetting());
            minDistanceSetting =
                    settings.register(
                            module.minDistanceSetting());
            maxDistanceSetting =
                    settings.register(
                            module.maxDistanceSetting());
            maxFovSetting =
                    settings.register(
                            module.maxFovSetting());
            maxPitchFovSetting =
                    settings.register(
                            module.maxPitchFovSetting());
            yawOffsetSetting =
                    settings.register(
                            module.yawOffsetSetting());
            pitchOffsetSetting =
                    settings.register(
                            module.pitchOffsetSetting());
            deadZoneSetting =
                    settings.register(
                            module.deadZoneSetting());
            yawEnabledSetting =
                    settings.register(
                            module.yawEnabledSetting());
            pitchEnabledSetting =
                    settings.register(
                            module.pitchEnabledSetting());
            angularEasingSetting = settings.register(module.angularEasingSetting());
            easingStrengthSetting = settings.register(module.easingStrengthSetting());
            combinedStepSetting = settings.register(module.combinedStepSetting());
            maxCombinedStepSetting = settings.register(module.maxCombinedStepSetting());
            yawSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.YAW_SPEED_SETTING_ID,
                                    "Yaw Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_SPEED,
                                            Minecraft189AimAssistModule.MAXIMUM_SPEED,
                                            0.5D)));
            pitchSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.PITCH_SPEED_SETTING_ID,
                                    "Pitch Speed",
                                    SettingValueKind.DOUBLE,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_SPEED,
                                            Minecraft189AimAssistModule.MAXIMUM_SPEED,
                                            0.5D)));
            requireSprintPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.REQUIRE_SPRINT_SETTING_ID,
                                    "Require Sprint",
                                    SettingValueKind.BOOLEAN,
                                    19));
            pauseWhileSneakingPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                                    "Pause While Sneaking",
                                    SettingValueKind.BOOLEAN,
                                    16));
            requireGroundPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.REQUIRE_GROUND_SETTING_ID,
                                    "Require Ground",
                                    SettingValueKind.BOOLEAN,
                                    17));
            requireForwardPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.REQUIRE_FORWARD_SETTING_ID,
                                    "Require Forward",
                                    SettingValueKind.BOOLEAN,
                                    18));
            requireHoldPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.REQUIRE_HOLD_SETTING_ID,
                                    "Require Hold",
                                    SettingValueKind.BOOLEAN,
                                    20));
            prioritizeCrosshairPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.PRIORITIZE_CROSSHAIR_SETTING_ID,
                                    "Crosshair Priority",
                                    SettingValueKind.BOOLEAN,
                                    22));
            minDistancePresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.MIN_DISTANCE_SETTING_ID,
                                    "Min Distance",
                                    SettingValueKind.DOUBLE,
                                    25,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_MIN_DISTANCE,
                                            Minecraft189AimAssistModule.MAXIMUM_MIN_DISTANCE,
                                            0.5D)));
            maxDistancePresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.MAX_DISTANCE_SETTING_ID,
                                    "Max Distance",
                                    SettingValueKind.DOUBLE,
                                    30,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_MAX_DISTANCE,
                                            Minecraft189AimAssistModule.MAXIMUM_MAX_DISTANCE,
                                            0.5D)));
            maxFovPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.MAX_FOV_SETTING_ID,
                                    "Max FOV",
                                    SettingValueKind.DOUBLE,
                                    40,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_MAX_FOV,
                                            Minecraft189AimAssistModule.MAXIMUM_MAX_FOV,
                                            1.0D)));
            maxPitchFovPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.MAX_PITCH_FOV_SETTING_ID,
                                    "Max Pitch FOV",
                                    SettingValueKind.DOUBLE,
                                    42,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_MAX_PITCH_FOV,
                                            Minecraft189AimAssistModule.MAXIMUM_MAX_PITCH_FOV,
                                            1.0D)));
            yawOffsetPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.YAW_OFFSET_SETTING_ID,
                                    "Yaw Offset",
                                    SettingValueKind.DOUBLE,
                                    41,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_YAW_OFFSET,
                                            Minecraft189AimAssistModule.MAXIMUM_YAW_OFFSET,
                                            0.5D)));
            pitchOffsetPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.PITCH_OFFSET_SETTING_ID,
                                    "Pitch Offset",
                                    SettingValueKind.DOUBLE,
                                    44,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_PITCH_OFFSET,
                                            Minecraft189AimAssistModule.MAXIMUM_PITCH_OFFSET,
                                            0.5D)));
            deadZonePresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.DEAD_ZONE_SETTING_ID,
                                    "Dead Zone",
                                    SettingValueKind.DOUBLE,
                                    45,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_DEAD_ZONE,
                                            Minecraft189AimAssistModule.MAXIMUM_DEAD_ZONE,
                                            0.5D)));
            yawEnabledPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.YAW_ENABLED_SETTING_ID,
                                    "Yaw Enabled",
                                    SettingValueKind.BOOLEAN,
                                    50));
            pitchEnabledPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.PITCH_ENABLED_SETTING_ID,
                                    "Pitch Enabled",
                                    SettingValueKind.BOOLEAN,
                                    60));
            angularEasingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AimAssistModule.ANGULAR_EASING_SETTING_ID,
                            "Angular Easing", SettingValueKind.BOOLEAN, 70));
            easingStrengthPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AimAssistModule.EASING_STRENGTH_SETTING_ID,
                            "Easing Strength %", SettingValueKind.INTEGER, 80,
                            new SettingNumericSpec(
                                    Minecraft189AimAssistModule.MINIMUM_EASING_STRENGTH,
                                    Minecraft189AimAssistModule.MAXIMUM_EASING_STRENGTH,
                                    5.0D)));
            combinedStepPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AimAssistModule.COMBINED_STEP_SETTING_ID,
                            "Combined Step Cap", SettingValueKind.BOOLEAN, 90));
            maxCombinedStepPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AimAssistModule.MAX_COMBINED_STEP_SETTING_ID,
                            "Max Combined Step", SettingValueKind.DOUBLE, 100,
                            new SettingNumericSpec(
                                    Minecraft189AimAssistModule.MINIMUM_SPEED,
                                    Minecraft189AimAssistModule.MAXIMUM_SPEED,
                                    0.1D)));
            yawSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.YAW_SPEED_SETTING_ID,
                                    0));
            pitchSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.PITCH_SPEED_SETTING_ID,
                                    10));
            requireSprintBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.REQUIRE_SPRINT_SETTING_ID,
                                    19));
            pauseWhileSneakingBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                                    16));
            requireGroundBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.REQUIRE_GROUND_SETTING_ID,
                                    17));
            requireForwardBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.REQUIRE_FORWARD_SETTING_ID,
                                    18));
            requireHoldBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.REQUIRE_HOLD_SETTING_ID,
                                    20));
            prioritizeCrosshairBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.PRIORITIZE_CROSSHAIR_SETTING_ID,
                                    22));
            minDistanceBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.MIN_DISTANCE_SETTING_ID,
                                    25));
            maxDistanceBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.MAX_DISTANCE_SETTING_ID,
                                    30));
            maxFovBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.MAX_FOV_SETTING_ID,
                                    40));
            maxPitchFovBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.MAX_PITCH_FOV_SETTING_ID,
                                    42));
            yawOffsetBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.YAW_OFFSET_SETTING_ID,
                                    41));
            pitchOffsetBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.PITCH_OFFSET_SETTING_ID,
                                    44));
            deadZoneBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.DEAD_ZONE_SETTING_ID,
                                    45));
            yawEnabledBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.YAW_ENABLED_SETTING_ID,
                                    50));
            pitchEnabledBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.PITCH_ENABLED_SETTING_ID,
                                    60));

            angularEasingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AimAssistModule.ID,
                            Minecraft189AimAssistModule.ANGULAR_EASING_SETTING_ID, 70));
            easingStrengthBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AimAssistModule.ID,
                            Minecraft189AimAssistModule.EASING_STRENGTH_SETTING_ID, 80));

            combinedStepBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AimAssistModule.ID,
                            Minecraft189AimAssistModule.COMBINED_STEP_SETTING_ID, 90));
            maxCombinedStepBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AimAssistModule.ID,
                            Minecraft189AimAssistModule.MAX_COMBINED_STEP_SETTING_ID, 100));

            return new Minecraft189AimAssistFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    yawSpeedSetting,
                    pitchSpeedSetting,
                    requireSprintSetting,
                    pauseWhileSneakingSetting,
                    requireGroundSetting,
                    requireForwardSetting,
                    requireHoldSetting,
                    prioritizeCrosshairSetting,
                    minDistanceSetting,
                    maxDistanceSetting,
                    maxFovSetting,
                    maxPitchFovSetting,
                    yawOffsetSetting,
                    pitchOffsetSetting,
                    deadZoneSetting,
                    yawEnabledSetting,
                    pitchEnabledSetting,
                    angularEasingSetting,
                    easingStrengthSetting,
                    yawSpeedPresentation,
                    pitchSpeedPresentation,
                    requireSprintPresentation,
                    pauseWhileSneakingPresentation,
                    requireGroundPresentation,
                    requireForwardPresentation,
                    requireHoldPresentation,
                    prioritizeCrosshairPresentation,
                    minDistancePresentation,
                    maxDistancePresentation,
                    maxFovPresentation,
                    maxPitchFovPresentation,
                    yawOffsetPresentation,
                    pitchOffsetPresentation,
                    deadZonePresentation,
                    yawEnabledPresentation,
                    pitchEnabledPresentation,
                    angularEasingPresentation,
                    easingStrengthPresentation,
                    yawSpeedBinding,
                    pitchSpeedBinding,
                    requireSprintBinding,
                    pauseWhileSneakingBinding,
                    requireGroundBinding,
                    requireForwardBinding,
                    requireHoldBinding,
                    prioritizeCrosshairBinding,
                    minDistanceBinding,
                    maxDistanceBinding,
                    maxFovBinding,
                    maxPitchFovBinding,
                    yawOffsetBinding,
                    pitchOffsetBinding,
                    deadZoneBinding,
                    yawEnabledBinding,
                    pitchEnabledBinding,
                    angularEasingBinding,
                    easingStrengthBinding,
                    combinedStepSetting,
                    maxCombinedStepSetting,
                    combinedStepPresentation,
                    maxCombinedStepPresentation,
                    combinedStepBinding,
                    maxCombinedStepBinding);
        } catch (RuntimeException failure) {
            closeQuietly(maxCombinedStepBinding, failure);
            closeQuietly(combinedStepBinding, failure);
            closeQuietly(maxCombinedStepPresentation, failure);
            closeQuietly(combinedStepPresentation, failure);
            closeQuietly(maxCombinedStepSetting, failure);
            closeQuietly(combinedStepSetting, failure);
            closeQuietly(easingStrengthBinding, failure);
            closeQuietly(angularEasingBinding, failure);
            closeQuietly(easingStrengthPresentation, failure);
            closeQuietly(angularEasingPresentation, failure);
            closeQuietly(easingStrengthSetting, failure);
            closeQuietly(angularEasingSetting, failure);
            closeQuietly(pitchEnabledBinding, failure);
            closeQuietly(yawEnabledBinding, failure);
            closeQuietly(deadZoneBinding, failure);
            closeQuietly(pitchOffsetBinding, failure);
            closeQuietly(yawOffsetBinding, failure);
            closeQuietly(maxPitchFovBinding, failure);
            closeQuietly(maxFovBinding, failure);
            closeQuietly(maxDistanceBinding, failure);
            closeQuietly(minDistanceBinding, failure);
            closeQuietly(prioritizeCrosshairBinding, failure);
            closeQuietly(requireHoldBinding, failure);
            closeQuietly(requireForwardBinding, failure);
            closeQuietly(requireGroundBinding, failure);
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(requireSprintBinding, failure);
            closeQuietly(pitchSpeedBinding, failure);
            closeQuietly(yawSpeedBinding, failure);
            closeQuietly(pitchEnabledPresentation, failure);
            closeQuietly(yawEnabledPresentation, failure);
            closeQuietly(deadZonePresentation, failure);
            closeQuietly(pitchOffsetPresentation, failure);
            closeQuietly(yawOffsetPresentation, failure);
            closeQuietly(maxPitchFovPresentation, failure);
            closeQuietly(maxFovPresentation, failure);
            closeQuietly(maxDistancePresentation, failure);
            closeQuietly(minDistancePresentation, failure);
            closeQuietly(prioritizeCrosshairPresentation, failure);
            closeQuietly(requireHoldPresentation, failure);
            closeQuietly(requireForwardPresentation, failure);
            closeQuietly(requireGroundPresentation, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(requireSprintPresentation, failure);
            closeQuietly(pitchSpeedPresentation, failure);
            closeQuietly(yawSpeedPresentation, failure);
            closeQuietly(pitchEnabledSetting, failure);
            closeQuietly(yawEnabledSetting, failure);
            closeQuietly(deadZoneSetting, failure);
            closeQuietly(pitchOffsetSetting, failure);
            closeQuietly(yawOffsetSetting, failure);
            closeQuietly(maxPitchFovSetting, failure);
            closeQuietly(maxFovSetting, failure);
            closeQuietly(maxDistanceSetting, failure);
            closeQuietly(minDistanceSetting, failure);
            closeQuietly(prioritizeCrosshairSetting, failure);
            closeQuietly(requireHoldSetting, failure);
            closeQuietly(requireForwardSetting, failure);
            closeQuietly(requireGroundSetting, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(requireSprintSetting, failure);
            closeQuietly(pitchSpeedSetting, failure);
            closeQuietly(yawSpeedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189AimAssistModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "aim-assist feature is closed");
        }
        return module;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        RuntimeException failure = null;
        try {
            if (controller.stateOf(
                    Minecraft189AimAssistModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AimAssistModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(maxCombinedStepBinding, failure);
        failure = close(combinedStepBinding, failure);
        failure = close(maxCombinedStepPresentation, failure);
        failure = close(combinedStepPresentation, failure);
        failure = close(maxCombinedStepSetting, failure);
        failure = close(combinedStepSetting, failure);
        failure = close(easingStrengthBinding, failure);
        failure = close(angularEasingBinding, failure);
        failure = close(easingStrengthPresentation, failure);
        failure = close(angularEasingPresentation, failure);
        failure = close(easingStrengthSetting, failure);
        failure = close(angularEasingSetting, failure);
        failure = close(pitchEnabledBinding, failure);
        failure = close(yawEnabledBinding, failure);
        failure = close(deadZoneBinding, failure);
        failure = close(pitchOffsetBinding, failure);
        failure = close(yawOffsetBinding, failure);
        failure = close(maxPitchFovBinding, failure);
        failure = close(maxFovBinding, failure);
        failure = close(maxDistanceBinding, failure);
        failure = close(minDistanceBinding, failure);
        failure = close(prioritizeCrosshairBinding, failure);
        failure = close(requireHoldBinding, failure);
        failure = close(requireForwardBinding, failure);
        failure = close(requireGroundBinding, failure);
        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(requireSprintBinding, failure);
        failure = close(pitchSpeedBinding, failure);
        failure = close(yawSpeedBinding, failure);
        failure = close(pitchEnabledPresentation, failure);
        failure = close(yawEnabledPresentation, failure);
        failure = close(deadZonePresentation, failure);
        failure = close(pitchOffsetPresentation, failure);
        failure = close(yawOffsetPresentation, failure);
        failure = close(maxPitchFovPresentation, failure);
        failure = close(maxFovPresentation, failure);
        failure = close(maxDistancePresentation, failure);
        failure = close(minDistancePresentation, failure);
        failure = close(prioritizeCrosshairPresentation, failure);
        failure = close(requireHoldPresentation, failure);
        failure = close(requireForwardPresentation, failure);
        failure = close(requireGroundPresentation, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(requireSprintPresentation, failure);
        failure = close(pitchSpeedPresentation, failure);
        failure = close(yawSpeedPresentation, failure);
        failure = close(pitchEnabledSetting, failure);
        failure = close(yawEnabledSetting, failure);
        failure = close(deadZoneSetting, failure);
        failure = close(pitchOffsetSetting, failure);
        failure = close(yawOffsetSetting, failure);
        failure = close(maxPitchFovSetting, failure);
        failure = close(maxFovSetting, failure);
        failure = close(maxDistanceSetting, failure);
        failure = close(minDistanceSetting, failure);
        failure = close(prioritizeCrosshairSetting, failure);
        failure = close(requireHoldSetting, failure);
        failure = close(requireForwardSetting, failure);
        failure = close(requireGroundSetting, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(requireSprintSetting, failure);
        failure = close(pitchSpeedSetting, failure);
        failure = close(yawSpeedSetting, failure);
        failure = close(presentation, failure);
        failure = close(moduleRegistration, failure);

        if (failure != null) {
            throw failure;
        }
    }

    private static RuntimeException close(
            final AutoCloseable closeable,
            final RuntimeException primary) {
        if (closeable == null) {
            return primary;
        }
        try {
            closeable.close();
            return primary;
        } catch (RuntimeException failure) {
            return append(primary, failure);
        } catch (Exception failure) {
            return append(
                    primary,
                    new IllegalStateException(
                            "aim-assist feature close failed",
                            failure));
        }
    }

    private static void closeQuietly(
            final AutoCloseable closeable,
            final RuntimeException primary) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception cleanupFailure) {
            primary.addSuppressed(cleanupFailure);
        }
    }

    private static RuntimeException append(
            final RuntimeException primary,
            final RuntimeException next) {
        if (primary == null) {
            return next;
        }
        primary.addSuppressed(next);
        return primary;
    }
}
