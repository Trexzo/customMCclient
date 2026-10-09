package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleCategoryDescriptor;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;

public final class Minecraft189FeatureCatalog
        implements AutoCloseable {
    public static final String COMBAT_CATEGORY_ID =
            "combat";
    public static final String MOVEMENT_CATEGORY_ID =
            "movement";
    public static final String PLAYER_CATEGORY_ID =
            "player";
    public static final String VISUALS_CATEGORY_ID =
            "visuals";

    private final ModuleRegistry modules;
    private final ModuleController moduleController;
    private final ModulePresentationRegistry modulePresentations;
    private final ModuleSettingRegistry moduleSettings;
    private final SettingRegistry settings;
    private final SettingPresentationRegistry settingPresentations;
    private final Minecraft189WatermarkModule watermark;
    private final ModuleRegistry.Registration watermarkRegistration;
    private final ModulePresentationRegistry.Registration watermarkPresentation;
    private final ModuleCategoryRegistry.Registration visualsCategory;
    private final ModuleCategoryRegistry.Registration movementCategory;
    private final ModuleCategoryRegistry.Registration playerCategory;
    private final ModuleCategoryRegistry.Registration combatCategory;
    private final SettingRegistry.Registration textSetting;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingPresentationRegistry.Registration textPresentation;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final ModuleSettingRegistry.Registration textBinding;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final Minecraft189ArrayListFeature arrayListFeature;
    private final Minecraft189KeystrokesFeature keystrokesFeature;
    private final Minecraft189FpsFeature fpsFeature;
    private final Minecraft189CpsFeature cpsFeature;
    private final Minecraft189CoordinatesFeature coordinatesFeature;
    private final Minecraft189DirectionFeature directionFeature;
    private final Minecraft189TargetHudFeature targetHudFeature;
    private final Minecraft189NearbyPlayersFeature nearbyPlayersFeature;
    private final Minecraft189DimensionFeature dimensionFeature;
    private final Minecraft189MovementStatusFeature movementStatusFeature;
    private final Minecraft189HealthFeature healthFeature;
    private final Minecraft189HurtTimeFeature hurtTimeFeature;
    private final Minecraft189ArmorFeature armorFeature;
    private final Minecraft189HungerFeature hungerFeature;
    private final Minecraft189PotionEffectsFeature potionEffectsFeature;
    private final Minecraft189ExperienceFeature experienceFeature;
    private final Minecraft189PingFeature pingFeature;
    private final Minecraft189HotbarSlotFeature hotbarSlotFeature;
    private final Minecraft189WorldTimeFeature worldTimeFeature;
    private final Minecraft189WeatherFeature weatherFeature;
    private final Minecraft189ServerFeature serverFeature;
    private final Minecraft189HeldItemFeature heldItemFeature;
    private final Minecraft189SpeedFeature speedFeature;
    private final Minecraft189CrosshairFeature crosshairFeature;
    private final Minecraft189AutoSprintFeature autoSprintFeature;
    private final Minecraft189AutoJumpFeature autoJumpFeature;
    private final Minecraft189AirJumpFeature airJumpFeature;
    private final Minecraft189AutoSneakFeature autoSneakFeature;
    private final Minecraft189NoSlowFeature noSlowFeature;
    private final Minecraft189StepFeature stepFeature;
    private final Minecraft189NoFallFeature noFallFeature;
    private final Minecraft189NoWebFeature noWebFeature;
    private final Minecraft189NoClipFeature noClipFeature;
    private final Minecraft189FlightFeature flightFeature;
    private final Minecraft189StrafeFeature strafeFeature;
    private final Minecraft189TargetStrafeFeature targetStrafeFeature;
    private final Minecraft189GlideFeature glideFeature;
    private final Minecraft189FastFallFeature fastFallFeature;
    private final Minecraft189NoGravityFeature noGravityFeature;
    private final Minecraft189ReverseStepFeature reverseStepFeature;
    private final Minecraft189FreezeFeature freezeFeature;
    private final Minecraft189LongJumpFeature longJumpFeature;
    private final Minecraft189BunnyHopFeature bunnyHopFeature;
    private final Minecraft189HighJumpFeature highJumpFeature;
    private final Minecraft189LowHopFeature lowHopFeature;
    private final Minecraft189MovementSpeedFeature movementSpeedFeature;
    private final Minecraft189AirSpeedFeature airSpeedFeature;
    private final Minecraft189DamageBoostFeature damageBoostFeature;
    private final Minecraft189FastPlaceFeature fastPlaceFeature;
    private final Minecraft189FastBreakFeature fastBreakFeature;
    private final Minecraft189SpeedMineFeature speedMineFeature;
    private final Minecraft189TimerSpeedFeature timerSpeedFeature;
    private final Minecraft189NoHitDelayFeature noHitDelayFeature;
    private final Minecraft189AutoClickerFeature autoClickerFeature;
    private final Minecraft189VelocityFeature velocityFeature;
    private final Minecraft189JitterFeature jitterFeature;
    private final Minecraft189AimAssistFeature aimAssistFeature;
    private final Minecraft189SpinFeature spinFeature;
    private final Minecraft189WTapFeature wTapFeature;
    private final Minecraft189JumpResetFeature jumpResetFeature;
    private final Minecraft189TriggerBotFeature triggerBotFeature;
    private Minecraft189FullbrightFeature fullbrightFeature;
    private Minecraft189FovFeature fovFeature;
    private Minecraft189NoBobbingFeature noBobbingFeature;
    private boolean closed;

    private Minecraft189FeatureCatalog(
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ModulePresentationRegistry modulePresentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189WatermarkModule watermark,
            final ModuleRegistry.Registration watermarkRegistration,
            final ModulePresentationRegistry.Registration watermarkPresentation,
            final ModuleCategoryRegistry.Registration visualsCategory,
            final ModuleCategoryRegistry.Registration movementCategory,
            final ModuleCategoryRegistry.Registration playerCategory,
            final ModuleCategoryRegistry.Registration combatCategory,
            final SettingRegistry.Registration textSetting,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingPresentationRegistry.Registration textPresentation,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final ModuleSettingRegistry.Registration textBinding,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final Minecraft189ArrayListFeature arrayListFeature,
            final Minecraft189KeystrokesFeature keystrokesFeature,
            final Minecraft189FpsFeature fpsFeature,
            final Minecraft189CpsFeature cpsFeature,
            final Minecraft189CoordinatesFeature coordinatesFeature,
            final Minecraft189DirectionFeature directionFeature,
            final Minecraft189TargetHudFeature targetHudFeature,
            final Minecraft189NearbyPlayersFeature nearbyPlayersFeature,
            final Minecraft189DimensionFeature dimensionFeature,
            final Minecraft189MovementStatusFeature movementStatusFeature,
            final Minecraft189HealthFeature healthFeature,
            final Minecraft189HurtTimeFeature hurtTimeFeature,
            final Minecraft189ArmorFeature armorFeature,
            final Minecraft189HungerFeature hungerFeature,
            final Minecraft189PotionEffectsFeature potionEffectsFeature,
            final Minecraft189ExperienceFeature experienceFeature,
            final Minecraft189PingFeature pingFeature,
            final Minecraft189HotbarSlotFeature hotbarSlotFeature,
            final Minecraft189WorldTimeFeature worldTimeFeature,
            final Minecraft189WeatherFeature weatherFeature,
            final Minecraft189ServerFeature serverFeature,
            final Minecraft189HeldItemFeature heldItemFeature,
            final Minecraft189SpeedFeature speedFeature,
            final Minecraft189CrosshairFeature crosshairFeature,
            final Minecraft189AutoSprintFeature autoSprintFeature,
            final Minecraft189AutoJumpFeature autoJumpFeature,
            final Minecraft189AirJumpFeature airJumpFeature,
            final Minecraft189AutoSneakFeature autoSneakFeature,
            final Minecraft189NoSlowFeature noSlowFeature,
            final Minecraft189StepFeature stepFeature,
            final Minecraft189NoFallFeature noFallFeature,
            final Minecraft189NoWebFeature noWebFeature,
            final Minecraft189NoClipFeature noClipFeature,
            final Minecraft189FlightFeature flightFeature,
            final Minecraft189StrafeFeature strafeFeature,
            final Minecraft189TargetStrafeFeature targetStrafeFeature,
            final Minecraft189GlideFeature glideFeature,
            final Minecraft189FastFallFeature fastFallFeature,
            final Minecraft189NoGravityFeature noGravityFeature,
            final Minecraft189ReverseStepFeature reverseStepFeature,
            final Minecraft189FreezeFeature freezeFeature,
            final Minecraft189LongJumpFeature longJumpFeature,
            final Minecraft189BunnyHopFeature bunnyHopFeature,
            final Minecraft189HighJumpFeature highJumpFeature,
            final Minecraft189LowHopFeature lowHopFeature,
            final Minecraft189MovementSpeedFeature movementSpeedFeature,
            final Minecraft189AirSpeedFeature airSpeedFeature,
            final Minecraft189DamageBoostFeature damageBoostFeature,
            final Minecraft189FastPlaceFeature fastPlaceFeature,
            final Minecraft189FastBreakFeature fastBreakFeature,
            final Minecraft189SpeedMineFeature speedMineFeature,
            final Minecraft189TimerSpeedFeature timerSpeedFeature,
            final Minecraft189NoHitDelayFeature noHitDelayFeature,
            final Minecraft189AutoClickerFeature autoClickerFeature,
            final Minecraft189VelocityFeature velocityFeature,
            final Minecraft189JitterFeature jitterFeature,
            final Minecraft189AimAssistFeature aimAssistFeature,
            final Minecraft189SpinFeature spinFeature,
            final Minecraft189WTapFeature wTapFeature,
            final Minecraft189JumpResetFeature jumpResetFeature,
            final Minecraft189TriggerBotFeature triggerBotFeature) {
        this.modules = modules;
        this.moduleController = moduleController;
        this.modulePresentations = modulePresentations;
        this.moduleSettings = moduleSettings;
        this.settings = settings;
        this.settingPresentations = settingPresentations;
        this.watermark = watermark;
        this.watermarkRegistration = watermarkRegistration;
        this.watermarkPresentation = watermarkPresentation;
        this.visualsCategory = visualsCategory;
        this.movementCategory = movementCategory;
        this.playerCategory = playerCategory;
        this.combatCategory = combatCategory;
        this.textSetting = textSetting;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.textPresentation = textPresentation;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.textBinding = textBinding;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.arrayListFeature = arrayListFeature;
        this.keystrokesFeature = keystrokesFeature;
        this.fpsFeature = fpsFeature;
        this.cpsFeature = cpsFeature;
        this.coordinatesFeature = coordinatesFeature;
        this.directionFeature = directionFeature;
        this.targetHudFeature = targetHudFeature;
        this.nearbyPlayersFeature = nearbyPlayersFeature;
        this.dimensionFeature = dimensionFeature;
        this.movementStatusFeature = movementStatusFeature;
        this.healthFeature = healthFeature;
        this.hurtTimeFeature = hurtTimeFeature;
        this.armorFeature = armorFeature;
        this.hungerFeature = hungerFeature;
        this.potionEffectsFeature = potionEffectsFeature;
        this.experienceFeature = experienceFeature;
        this.pingFeature = pingFeature;
        this.hotbarSlotFeature = hotbarSlotFeature;
        this.worldTimeFeature = worldTimeFeature;
        this.weatherFeature = weatherFeature;
        this.serverFeature = serverFeature;
        this.heldItemFeature = heldItemFeature;
        this.speedFeature = speedFeature;
        this.crosshairFeature = crosshairFeature;
        this.autoSprintFeature = autoSprintFeature;
        this.autoJumpFeature = autoJumpFeature;
        this.airJumpFeature = airJumpFeature;
        this.autoSneakFeature = autoSneakFeature;
        this.noSlowFeature = noSlowFeature;
        this.stepFeature = stepFeature;
        this.noFallFeature = noFallFeature;
        this.noWebFeature = noWebFeature;
        this.noClipFeature = noClipFeature;
        this.flightFeature = flightFeature;
        this.strafeFeature = strafeFeature;
        this.targetStrafeFeature = targetStrafeFeature;
        this.glideFeature = glideFeature;
        this.fastFallFeature = fastFallFeature;
        this.noGravityFeature = noGravityFeature;
        this.reverseStepFeature = reverseStepFeature;
        this.freezeFeature = freezeFeature;
        this.longJumpFeature = longJumpFeature;
        this.bunnyHopFeature = bunnyHopFeature;
        this.highJumpFeature = highJumpFeature;
        this.lowHopFeature = lowHopFeature;
        this.movementSpeedFeature = movementSpeedFeature;
        this.airSpeedFeature = airSpeedFeature;
        this.damageBoostFeature = damageBoostFeature;
        this.fastPlaceFeature = fastPlaceFeature;
        this.fastBreakFeature = fastBreakFeature;
        this.speedMineFeature = speedMineFeature;
        this.timerSpeedFeature = timerSpeedFeature;
        this.noHitDelayFeature = noHitDelayFeature;
        this.autoClickerFeature = autoClickerFeature;
        this.velocityFeature = velocityFeature;
        this.jitterFeature = jitterFeature;
        this.aimAssistFeature = aimAssistFeature;
        this.spinFeature = spinFeature;
        this.wTapFeature = wTapFeature;
        this.jumpResetFeature = jumpResetFeature;
        this.triggerBotFeature = triggerBotFeature;
    }

    public static Minecraft189FeatureCatalog install(
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ModulePresentationRegistry presentations,
            final ModuleCategoryRegistry categories,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState,
            final Minecraft189FrameRateTracker frameRateTracker,
            final Minecraft189ClickRateTracker clickRateTracker,
            final Minecraft189PlayerPositionState playerPositionState,
            final Minecraft189PlayerRotationState playerRotationState,
            final Minecraft189PlayerDimensionState playerDimensionState,
            final Minecraft189PlayerMovementState playerMovementState,
            final Minecraft189PlayerHealthState playerHealthState,
            final Minecraft189PlayerHurtTimeState playerHurtTimeState,
            final Minecraft189PlayerArmorState playerArmorState,
            final Minecraft189PlayerHungerState playerHungerState,
            final Minecraft189PlayerPotionEffectsState playerPotionEffectsState,
            final Minecraft189PlayerExperienceState playerExperienceState,
            final Minecraft189PlayerPingState playerPingState,
            final Minecraft189HotbarSlotState hotbarSlotState,
            final Minecraft189WorldTimeState worldTimeState,
            final Minecraft189WorldWeatherState worldWeatherState,
            final Minecraft189ServerAddressState serverAddressState,
            final Minecraft189HeldItemState heldItemState,
            final Minecraft189MovementSpeedTracker movementSpeedTracker,
            final Minecraft189NearestPlayerTargetState nearestPlayerTargetState,
            final Minecraft189TargetRotationState targetRotationState,
            final Minecraft189WorldEntityPositionState worldEntityPositionState,
            final Minecraft189WorldEntityKindState worldEntityKindState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        Objects.requireNonNull(modules, "modules");
        Objects.requireNonNull(moduleController, "moduleController");
        Objects.requireNonNull(presentations, "presentations");
        Objects.requireNonNull(categories, "categories");
        Objects.requireNonNull(moduleSettings, "moduleSettings");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(settingPresentations, "settingPresentations");
        Objects.requireNonNull(inputState, "inputState");
        Objects.requireNonNull(frameRateTracker, "frameRateTracker");
        Objects.requireNonNull(clickRateTracker, "clickRateTracker");
        Objects.requireNonNull(playerPositionState, "playerPositionState");
        Objects.requireNonNull(playerRotationState, "playerRotationState");
        Objects.requireNonNull(playerDimensionState, "playerDimensionState");
        Objects.requireNonNull(playerMovementState, "playerMovementState");
        Objects.requireNonNull(playerHealthState, "playerHealthState");
        Objects.requireNonNull(playerHurtTimeState, "playerHurtTimeState");
        Objects.requireNonNull(playerArmorState, "playerArmorState");
        Objects.requireNonNull(playerHungerState, "playerHungerState");
        Objects.requireNonNull(playerPotionEffectsState, "playerPotionEffectsState");
        Objects.requireNonNull(playerExperienceState, "playerExperienceState");
        Objects.requireNonNull(playerPingState, "playerPingState");
        Objects.requireNonNull(hotbarSlotState, "hotbarSlotState");
        Objects.requireNonNull(worldTimeState, "worldTimeState");
        Objects.requireNonNull(worldWeatherState, "worldWeatherState");
        Objects.requireNonNull(serverAddressState, "serverAddressState");
        Objects.requireNonNull(heldItemState, "heldItemState");
        Objects.requireNonNull(movementSpeedTracker, "movementSpeedTracker");
        Objects.requireNonNull(nearestPlayerTargetState, "nearestPlayerTargetState");
        Objects.requireNonNull(targetRotationState, "targetRotationState");
        Objects.requireNonNull(worldEntityPositionState, "worldEntityPositionState");
        Objects.requireNonNull(worldEntityKindState, "worldEntityKindState");
        Objects.requireNonNull(renderPipeline, "renderPipeline");
        Objects.requireNonNull(hostCallbacks, "hostCallbacks");

        ModuleCategoryRegistry.Registration category = null;
        ModuleCategoryRegistry.Registration movementCategory = null;
        ModuleCategoryRegistry.Registration playerCategory = null;
        ModuleCategoryRegistry.Registration combatCategory = null;
        ModuleRegistry.Registration module = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration textSetting = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingPresentationRegistry.Registration textPresentation = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        ModuleSettingRegistry.Registration textBinding = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        Minecraft189ArrayListFeature arrayListFeature = null;
        Minecraft189KeystrokesFeature keystrokesFeature = null;
        Minecraft189FpsFeature fpsFeature = null;
        Minecraft189CpsFeature cpsFeature = null;
        Minecraft189CoordinatesFeature coordinatesFeature = null;
        Minecraft189DirectionFeature directionFeature = null;
        Minecraft189TargetHudFeature targetHudFeature = null;
        Minecraft189NearbyPlayersFeature nearbyPlayersFeature = null;
        Minecraft189DimensionFeature dimensionFeature = null;
        Minecraft189MovementStatusFeature movementStatusFeature = null;
        Minecraft189HealthFeature healthFeature = null;
        Minecraft189HurtTimeFeature hurtTimeFeature = null;
        Minecraft189ArmorFeature armorFeature = null;
        Minecraft189HungerFeature hungerFeature = null;
        Minecraft189PotionEffectsFeature potionEffectsFeature = null;
        Minecraft189ExperienceFeature experienceFeature = null;
        Minecraft189PingFeature pingFeature = null;
        Minecraft189HotbarSlotFeature hotbarSlotFeature = null;
        Minecraft189WorldTimeFeature worldTimeFeature = null;
        Minecraft189WeatherFeature weatherFeature = null;
        Minecraft189ServerFeature serverFeature = null;
        Minecraft189HeldItemFeature heldItemFeature = null;
        Minecraft189SpeedFeature speedFeature = null;
        Minecraft189CrosshairFeature crosshairFeature = null;
        Minecraft189AutoSprintFeature autoSprintFeature = null;
        Minecraft189AutoJumpFeature autoJumpFeature = null;
        Minecraft189AirJumpFeature airJumpFeature = null;
        Minecraft189AutoSneakFeature autoSneakFeature = null;
        Minecraft189NoSlowFeature noSlowFeature = null;
        Minecraft189StepFeature stepFeature = null;
        Minecraft189NoFallFeature noFallFeature = null;
        Minecraft189NoWebFeature noWebFeature = null;
        Minecraft189NoClipFeature noClipFeature = null;
        Minecraft189FlightFeature flightFeature = null;
        Minecraft189StrafeFeature strafeFeature = null;
        Minecraft189TargetStrafeFeature targetStrafeFeature = null;
        Minecraft189GlideFeature glideFeature = null;
        Minecraft189FastFallFeature fastFallFeature = null;
        Minecraft189NoGravityFeature noGravityFeature = null;
        Minecraft189ReverseStepFeature reverseStepFeature = null;
        Minecraft189FreezeFeature freezeFeature = null;
        Minecraft189LongJumpFeature longJumpFeature = null;
        Minecraft189BunnyHopFeature bunnyHopFeature = null;
        Minecraft189HighJumpFeature highJumpFeature = null;
        Minecraft189LowHopFeature lowHopFeature = null;
        Minecraft189MovementSpeedFeature movementSpeedFeature = null;
        Minecraft189AirSpeedFeature airSpeedFeature = null;
        Minecraft189DamageBoostFeature damageBoostFeature = null;
        Minecraft189FastPlaceFeature fastPlaceFeature = null;
        Minecraft189FastBreakFeature fastBreakFeature = null;
        Minecraft189SpeedMineFeature speedMineFeature = null;
        Minecraft189TimerSpeedFeature timerSpeedFeature = null;
        Minecraft189NoHitDelayFeature noHitDelayFeature = null;
        Minecraft189AutoClickerFeature autoClickerFeature = null;
        Minecraft189VelocityFeature velocityFeature = null;
        Minecraft189JitterFeature jitterFeature = null;
        Minecraft189AimAssistFeature aimAssistFeature = null;
        Minecraft189SpinFeature spinFeature = null;
        Minecraft189WTapFeature wTapFeature = null;
        Minecraft189JumpResetFeature jumpResetFeature = null;
        Minecraft189TriggerBotFeature triggerBotFeature = null;

        final Minecraft189WatermarkModule watermark =
                new Minecraft189WatermarkModule(
                        renderPipeline,
                        hostCallbacks);
        try {
            combatCategory =
                    categories.register(
                            new ModuleCategoryDescriptor(
                                    COMBAT_CATEGORY_ID,
                                    "Combat",
                                    10));
            movementCategory =
                    categories.register(
                            new ModuleCategoryDescriptor(
                                    MOVEMENT_CATEGORY_ID,
                                    "Movement",
                                    20));
            playerCategory =
                    categories.register(
                            new ModuleCategoryDescriptor(
                                    PLAYER_CATEGORY_ID,
                                    "Player",
                                    25));
            category =
                    categories.register(
                            new ModuleCategoryDescriptor(
                                    VISUALS_CATEGORY_ID,
                                    "Visuals",
                                    30));
            module =
                    modules.register(
                            watermark);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189WatermarkModule.ID,
                                    "Watermark",
                                    "Shows a configurable client label in the HUD.",
                                    VISUALS_CATEGORY_ID,
                                    0));

            textSetting =
                    settings.register(
                            watermark.textSetting());
            xSetting =
                    settings.register(
                            watermark.xSetting());
            ySetting =
                    settings.register(
                            watermark.ySetting());

            textPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WatermarkModule.TEXT_SETTING_ID,
                                    "Text",
                                    SettingValueKind.TEXT,
                                    0));
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WatermarkModule.X_SETTING_ID,
                                    "X",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            yPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WatermarkModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    20,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));

            textBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WatermarkModule.ID,
                                    Minecraft189WatermarkModule.TEXT_SETTING_ID,
                                    0));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WatermarkModule.ID,
                                    Minecraft189WatermarkModule.X_SETTING_ID,
                                    10));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WatermarkModule.ID,
                                    Minecraft189WatermarkModule.Y_SETTING_ID,
                                    20));

            autoSprintFeature =
                    Minecraft189AutoSprintFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            autoJumpFeature =
                    Minecraft189AutoJumpFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            airJumpFeature =
                    Minecraft189AirJumpFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            inputState);

            autoSneakFeature =
                    Minecraft189AutoSneakFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            noSlowFeature =
                    Minecraft189NoSlowFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            stepFeature =
                    Minecraft189StepFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            noFallFeature =
                    Minecraft189NoFallFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            noWebFeature =
                    Minecraft189NoWebFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            noClipFeature =
                    Minecraft189NoClipFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            flightFeature =
                    Minecraft189FlightFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState);

            strafeFeature =
                    Minecraft189StrafeFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState);
            targetStrafeFeature = Minecraft189TargetStrafeFeature.install(
                    modules, moduleController, presentations, moduleSettings,
                    settings, settingPresentations);

            glideFeature =
                    Minecraft189GlideFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            fastFallFeature =
                    Minecraft189FastFallFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            noGravityFeature =
                    Minecraft189NoGravityFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            reverseStepFeature =
                    Minecraft189ReverseStepFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            freezeFeature =
                    Minecraft189FreezeFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            longJumpFeature =
                    Minecraft189LongJumpFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState);

            bunnyHopFeature =
                    Minecraft189BunnyHopFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState);

            highJumpFeature =
                    Minecraft189HighJumpFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState);

            lowHopFeature =
                    Minecraft189LowHopFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState);

            movementSpeedFeature =
                    Minecraft189MovementSpeedFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState);

            airSpeedFeature =
                    Minecraft189AirSpeedFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState);

            damageBoostFeature =
                    Minecraft189DamageBoostFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            fastPlaceFeature =
                    Minecraft189FastPlaceFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            fastBreakFeature =
                    Minecraft189FastBreakFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            speedMineFeature =
                    Minecraft189SpeedMineFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            timerSpeedFeature =
                    Minecraft189TimerSpeedFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            noHitDelayFeature =
                    Minecraft189NoHitDelayFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            autoClickerFeature =
                    Minecraft189AutoClickerFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            velocityFeature =
                    Minecraft189VelocityFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            jitterFeature =
                    Minecraft189JitterFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            aimAssistFeature =
                    Minecraft189AimAssistFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            spinFeature =
                    Minecraft189SpinFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            wTapFeature =
                    Minecraft189WTapFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations);

            jumpResetFeature = Minecraft189JumpResetFeature.install(
                    modules, moduleController, presentations, moduleSettings,
                    settings, settingPresentations);

            triggerBotFeature = Minecraft189TriggerBotFeature.install(
                    modules, moduleController, presentations, moduleSettings,
                    settings, settingPresentations);

            arrayListFeature =
                    Minecraft189ArrayListFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            renderPipeline,
                            hostCallbacks);

            keystrokesFeature =
                    Minecraft189KeystrokesFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState,
                            renderPipeline,
                            hostCallbacks);

            fpsFeature =
                    Minecraft189FpsFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            frameRateTracker,
                            renderPipeline,
                            hostCallbacks);

            cpsFeature =
                    Minecraft189CpsFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            clickRateTracker,
                            renderPipeline,
                            hostCallbacks);

            coordinatesFeature =
                    Minecraft189CoordinatesFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerPositionState,
                            renderPipeline,
                            hostCallbacks);

            directionFeature =
                    Minecraft189DirectionFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerRotationState,
                            renderPipeline,
                            hostCallbacks);

            targetHudFeature = Minecraft189TargetHudFeature.install(
                    modules,
                    moduleController,
                    presentations,
                    moduleSettings,
                    settings,
                    settingPresentations,
                    nearestPlayerTargetState,
                    targetRotationState,
                    renderPipeline,
                    hostCallbacks);

            nearbyPlayersFeature = Minecraft189NearbyPlayersFeature.install(
                    modules,
                    moduleController,
                    presentations,
                    moduleSettings,
                    settings,
                    settingPresentations,
                    playerPositionState,
                    worldEntityPositionState,
                    worldEntityKindState,
                    playerRotationState,
                    renderPipeline,
                    hostCallbacks);

            dimensionFeature =
                    Minecraft189DimensionFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerDimensionState,
                            renderPipeline,
                            hostCallbacks);

            movementStatusFeature =
                    Minecraft189MovementStatusFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerMovementState,
                            renderPipeline,
                            hostCallbacks);

            healthFeature =
                    Minecraft189HealthFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerHealthState,
                            renderPipeline,
                            hostCallbacks);

            hurtTimeFeature =
                    Minecraft189HurtTimeFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerHurtTimeState,
                            renderPipeline,
                            hostCallbacks);

            armorFeature =
                    Minecraft189ArmorFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerArmorState,
                            renderPipeline,
                            hostCallbacks);

            hungerFeature =
                    Minecraft189HungerFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerHungerState,
                            renderPipeline,
                            hostCallbacks);

            potionEffectsFeature =
                    Minecraft189PotionEffectsFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerPotionEffectsState,
                            renderPipeline,
                            hostCallbacks);

            experienceFeature =
                    Minecraft189ExperienceFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerExperienceState,
                            renderPipeline,
                            hostCallbacks);

            pingFeature =
                    Minecraft189PingFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerPingState,
                            renderPipeline,
                            hostCallbacks);

            hotbarSlotFeature =
                    Minecraft189HotbarSlotFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            hotbarSlotState,
                            renderPipeline,
                            hostCallbacks);

            worldTimeFeature =
                    Minecraft189WorldTimeFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            worldTimeState,
                            renderPipeline,
                            hostCallbacks);

            weatherFeature =
                    Minecraft189WeatherFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            worldWeatherState,
                            renderPipeline,
                            hostCallbacks);

            serverFeature =
                    Minecraft189ServerFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            serverAddressState,
                            renderPipeline,
                            hostCallbacks);

            heldItemFeature =
                    Minecraft189HeldItemFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            heldItemState,
                            renderPipeline,
                            hostCallbacks);

            speedFeature =
                    Minecraft189SpeedFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            movementSpeedTracker,
                            renderPipeline,
                            hostCallbacks);

            crosshairFeature =
                    Minecraft189CrosshairFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerMovementState,
                            renderPipeline,
                            hostCallbacks);

            return new Minecraft189FeatureCatalog(
                    modules,
                    moduleController,
                    presentations,
                    moduleSettings,
                    settings,
                    settingPresentations,
                    watermark,
                    module,
                    presentation,
                    category,
                    movementCategory,
                    playerCategory,
                    combatCategory,
                    textSetting,
                    xSetting,
                    ySetting,
                    textPresentation,
                    xPresentation,
                    yPresentation,
                    textBinding,
                    xBinding,
                    yBinding,
                    arrayListFeature,
                    keystrokesFeature,
                    fpsFeature,
                    cpsFeature,
                    coordinatesFeature,
                    directionFeature,
                    targetHudFeature,
                    nearbyPlayersFeature,
                    dimensionFeature,
                    movementStatusFeature,
                    healthFeature,
                    hurtTimeFeature,
                    armorFeature,
                    hungerFeature,
                    potionEffectsFeature,
                    experienceFeature,
                    pingFeature,
                    hotbarSlotFeature,
                    worldTimeFeature,
                    weatherFeature,
                    serverFeature,
                    heldItemFeature,
                    speedFeature,
                    crosshairFeature,
                    autoSprintFeature,
                    autoJumpFeature,
                    airJumpFeature,
                    autoSneakFeature,
                    noSlowFeature,
                    stepFeature,
                    noFallFeature,
                    noWebFeature,
                    noClipFeature,
                    flightFeature,
                    strafeFeature,
                    targetStrafeFeature,
                    glideFeature,
                    fastFallFeature,
                    noGravityFeature,
                    reverseStepFeature,
                    freezeFeature,
                    longJumpFeature,
                    bunnyHopFeature,
                    highJumpFeature,
                    lowHopFeature,
                    movementSpeedFeature,
                    airSpeedFeature,
                    damageBoostFeature,
                    fastPlaceFeature,
                    fastBreakFeature,
                    speedMineFeature,
                    timerSpeedFeature,
                    noHitDelayFeature,
                    autoClickerFeature,
                    velocityFeature,
                    jitterFeature,
                    aimAssistFeature,
                    spinFeature,
                    wTapFeature,
                    jumpResetFeature,
                    triggerBotFeature);
        } catch (RuntimeException failure) {
            closeQuietly(triggerBotFeature, failure);
            closeQuietly(jumpResetFeature, failure);
            closeQuietly(wTapFeature, failure);
            closeQuietly(spinFeature, failure);
            closeQuietly(aimAssistFeature, failure);
            closeQuietly(jitterFeature, failure);
            closeQuietly(velocityFeature, failure);
            closeQuietly(autoClickerFeature, failure);
            closeQuietly(noHitDelayFeature, failure);
            closeQuietly(timerSpeedFeature, failure);
            closeQuietly(speedMineFeature, failure);
            closeQuietly(fastBreakFeature, failure);
            closeQuietly(fastPlaceFeature, failure);
            closeQuietly(damageBoostFeature, failure);
            closeQuietly(airSpeedFeature, failure);
            closeQuietly(movementSpeedFeature, failure);
            closeQuietly(lowHopFeature, failure);
            closeQuietly(highJumpFeature, failure);
            closeQuietly(bunnyHopFeature, failure);
            closeQuietly(longJumpFeature, failure);
            closeQuietly(freezeFeature, failure);
            closeQuietly(reverseStepFeature, failure);
            closeQuietly(noGravityFeature, failure);
            closeQuietly(fastFallFeature, failure);
            closeQuietly(glideFeature, failure);
            closeQuietly(targetStrafeFeature, failure);
            closeQuietly(strafeFeature, failure);
            closeQuietly(flightFeature, failure);
            closeQuietly(noClipFeature, failure);
            closeQuietly(noWebFeature, failure);
            closeQuietly(noFallFeature, failure);
            closeQuietly(stepFeature, failure);
            closeQuietly(noSlowFeature, failure);
            closeQuietly(autoSneakFeature, failure);
            closeQuietly(airJumpFeature, failure);
            closeQuietly(autoJumpFeature, failure);
            closeQuietly(autoSprintFeature, failure);
            closeQuietly(crosshairFeature, failure);
            closeQuietly(speedFeature, failure);
            closeQuietly(heldItemFeature, failure);
            closeQuietly(serverFeature, failure);
            closeQuietly(weatherFeature, failure);
            closeQuietly(worldTimeFeature, failure);
            closeQuietly(hotbarSlotFeature, failure);
            closeQuietly(pingFeature, failure);
            closeQuietly(experienceFeature, failure);
            closeQuietly(potionEffectsFeature, failure);
            closeQuietly(hungerFeature, failure);
            closeQuietly(armorFeature, failure);
            closeQuietly(hurtTimeFeature, failure);
            closeQuietly(healthFeature, failure);
            closeQuietly(movementStatusFeature, failure);
            closeQuietly(dimensionFeature, failure);
            closeQuietly(nearbyPlayersFeature, failure);
            closeQuietly(targetHudFeature, failure);
            closeQuietly(directionFeature, failure);
            closeQuietly(coordinatesFeature, failure);
            closeQuietly(cpsFeature, failure);
            closeQuietly(fpsFeature, failure);
            closeQuietly(keystrokesFeature, failure);
            closeQuietly(arrayListFeature, failure);
            closeQuietly(yBinding, failure);
            closeQuietly(xBinding, failure);
            closeQuietly(textBinding, failure);
            closeQuietly(yPresentation, failure);
            closeQuietly(xPresentation, failure);
            closeQuietly(textPresentation, failure);
            closeQuietly(ySetting, failure);
            closeQuietly(xSetting, failure);
            closeQuietly(textSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(module, failure);
            closeQuietly(category, failure);
            closeQuietly(playerCategory, failure);
            closeQuietly(movementCategory, failure);
            closeQuietly(combatCategory, failure);
            throw failure;
        }
    }

    public Minecraft189WatermarkModule watermark() {
        requireOpen();
        return watermark;
    }

    public Minecraft189ArrayListModule arrayList() {
        requireOpen();
        return arrayListFeature.module();
    }

    public Minecraft189KeystrokesModule keystrokes() {
        requireOpen();
        return keystrokesFeature.module();
    }

    public Minecraft189FpsModule fps() {
        requireOpen();
        return fpsFeature.module();
    }

    public Minecraft189CpsModule cps() {
        requireOpen();
        return cpsFeature.module();
    }

    public Minecraft189CoordinatesModule coordinates() {
        requireOpen();
        return coordinatesFeature.module();
    }

    public Minecraft189DirectionModule direction() {
        requireOpen();
        return directionFeature.module();
    }

    public Minecraft189TargetHudModule targetHud() {
        requireOpen();
        return targetHudFeature.module();
    }

    public Minecraft189NearbyPlayersModule nearbyPlayers() {
        requireOpen();
        return nearbyPlayersFeature.module();
    }

    public Minecraft189DimensionModule dimension() {
        requireOpen();
        return dimensionFeature.module();
    }

    public Minecraft189MovementStatusModule movementStatus() {
        requireOpen();
        return movementStatusFeature.module();
    }

    public Minecraft189AutoSprintModule autoSprint() {
        requireOpen();
        return autoSprintFeature.module();
    }

    public Minecraft189AutoJumpModule autoJump() {
        requireOpen();
        return autoJumpFeature.module();
    }

    public Minecraft189AirJumpModule airJump() {
        requireOpen();
        return airJumpFeature.module();
    }

    public Minecraft189AutoSneakModule autoSneak() {
        requireOpen();
        return autoSneakFeature.module();
    }

    public Minecraft189NoSlowModule noSlow() {
        requireOpen();
        return noSlowFeature.module();
    }

    public Minecraft189StepModule step() {
        requireOpen();
        return stepFeature.module();
    }

    public Minecraft189NoFallModule noFall() {
        requireOpen();
        return noFallFeature.module();
    }

    public Minecraft189NoWebModule noWeb() {
        requireOpen();
        return noWebFeature.module();
    }

    public Minecraft189NoClipModule noClip() {
        requireOpen();
        return noClipFeature.module();
    }

    public Minecraft189FlightModule flight() {
        requireOpen();
        return flightFeature.module();
    }

    public Minecraft189StrafeModule strafe() {
        requireOpen();
        return strafeFeature.module();
    }

    public Minecraft189TargetStrafeModule targetStrafe() {
        requireOpen();
        return targetStrafeFeature.module();
    }

    public Minecraft189GlideModule glide() {
        requireOpen();
        return glideFeature.module();
    }

    public Minecraft189FastFallModule fastFall() {
        requireOpen();
        return fastFallFeature.module();
    }

    public Minecraft189NoGravityModule noGravity() {
        requireOpen();
        return noGravityFeature.module();
    }

    public Minecraft189ReverseStepModule reverseStep() {
        requireOpen();
        return reverseStepFeature.module();
    }

    public Minecraft189FreezeModule freeze() {
        requireOpen();
        return freezeFeature.module();
    }

    public Minecraft189LongJumpModule longJump() {
        requireOpen();
        return longJumpFeature.module();
    }

    public Minecraft189BunnyHopModule bunnyHop() {
        requireOpen();
        return bunnyHopFeature.module();
    }

    public Minecraft189HighJumpModule highJump() {
        requireOpen();
        return highJumpFeature.module();
    }

    public Minecraft189LowHopModule lowHop() {
        requireOpen();
        return lowHopFeature.module();
    }

    public Minecraft189MovementSpeedModule movementSpeed() {
        requireOpen();
        return movementSpeedFeature.module();
    }

    public Minecraft189AirSpeedModule airSpeed() {
        requireOpen();
        return airSpeedFeature.module();
    }

    public Minecraft189DamageBoostModule damageBoost() {
        requireOpen();
        return damageBoostFeature.module();
    }

    public Minecraft189FastPlaceModule fastPlace() {
        requireOpen();
        return fastPlaceFeature.module();
    }

    public Minecraft189FastBreakModule fastBreak() {
        requireOpen();
        return fastBreakFeature.module();
    }

    public Minecraft189SpeedMineModule speedMine() {
        requireOpen();
        return speedMineFeature.module();
    }

    public Minecraft189TimerSpeedModule timerSpeed() {
        requireOpen();
        return timerSpeedFeature.module();
    }

    public Minecraft189VelocityModule velocity() {
        requireOpen();
        return velocityFeature.module();
    }

    public Minecraft189NoHitDelayModule noHitDelay() {
        requireOpen();
        return noHitDelayFeature.module();
    }

    public Minecraft189AutoClickerModule autoClicker() {
        requireOpen();
        return autoClickerFeature.module();
    }

    public Minecraft189JitterModule jitter() {
        requireOpen();
        return jitterFeature.module();
    }

    public Minecraft189AimAssistModule aimAssist() {
        requireOpen();
        return aimAssistFeature.module();
    }

    public Minecraft189SpinModule spin() {
        requireOpen();
        return spinFeature.module();
    }

    public Minecraft189TriggerBotModule triggerBot() {
        requireOpen();
        return triggerBotFeature.module();
    }

    public Minecraft189JumpResetModule jumpReset() {
        requireOpen();
        return jumpResetFeature.module();
    }

    public Minecraft189WTapModule wTap() {
        requireOpen();
        return wTapFeature.module();
    }

    public Minecraft189HealthModule health() {
        requireOpen();
        return healthFeature.module();
    }

    public Minecraft189HurtTimeModule hurtTime() {
        requireOpen();
        return hurtTimeFeature.module();
    }

    public Minecraft189ArmorModule armor() {
        requireOpen();
        return armorFeature.module();
    }

    public Minecraft189HungerModule hunger() {
        requireOpen();
        return hungerFeature.module();
    }

    public Minecraft189PotionEffectsModule potionEffects() {
        requireOpen();
        return potionEffectsFeature.module();
    }

    public Minecraft189ExperienceModule experience() {
        requireOpen();
        return experienceFeature.module();
    }

    public Minecraft189PingModule ping() {
        requireOpen();
        return pingFeature.module();
    }

    public Minecraft189HotbarSlotModule hotbarSlot() {
        requireOpen();
        return hotbarSlotFeature.module();
    }

    public Minecraft189WorldTimeModule worldTime() {
        requireOpen();
        return worldTimeFeature.module();
    }

    public Minecraft189WeatherModule weather() {
        requireOpen();
        return weatherFeature.module();
    }

    public Minecraft189ServerModule server() {
        requireOpen();
        return serverFeature.module();
    }

    public Minecraft189HeldItemModule heldItem() {
        requireOpen();
        return heldItemFeature.module();
    }

    public Minecraft189SpeedModule speed() {
        requireOpen();
        return speedFeature.module();
    }

    public Minecraft189CrosshairModule crosshair() {
        requireOpen();
        return crosshairFeature.module();
    }

    public synchronized Minecraft189FullbrightModule installFullbright(
            final dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess settings) {
        requireOpen();
        if (fullbrightFeature != null) {
            throw new IllegalStateException(
                    "fullbright feature already installed");
        }

        fullbrightFeature =
                Minecraft189FullbrightFeature.install(
                        modules,
                        moduleController,
                        modulePresentations,
                        java.util.Objects.requireNonNull(
                                settings,
                                "settings"));
        return fullbrightFeature.module();
    }

    public synchronized Minecraft189FovModule installFovChanger(
            final dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess gameSettings,
            final dev.trexzo.custommc.core.event.EventBus events) {
        requireOpen();
        if (fovFeature != null) {
            throw new IllegalStateException(
                    "FOV feature already installed");
        }

        fovFeature =
                Minecraft189FovFeature.install(
                        modules,
                        moduleController,
                        modulePresentations,
                        moduleSettings,
                        settings,
                        settingPresentations,
                        java.util.Objects.requireNonNull(
                                gameSettings,
                                "gameSettings"),
                        java.util.Objects.requireNonNull(
                                events,
                                "events"));
        return fovFeature.module();
    }

    public synchronized Minecraft189NoBobbingModule installNoBobbing(
            final dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess gameSettings) {
        requireOpen();
        if (noBobbingFeature != null) {
            throw new IllegalStateException(
                    "no-bobbing feature already installed");
        }

        noBobbingFeature =
                Minecraft189NoBobbingFeature.install(
                        modules,
                        moduleController,
                        modulePresentations,
                        java.util.Objects.requireNonNull(
                                gameSettings,
                                "gameSettings"));
        return noBobbingFeature.module();
    }

    public synchronized Minecraft189NoBobbingModule noBobbing() {
        requireOpen();
        if (noBobbingFeature == null) {
            throw new IllegalStateException(
                    "no-bobbing feature is not installed");
        }
        return noBobbingFeature.module();
    }

    public synchronized Minecraft189FovModule fovChanger() {
        requireOpen();
        if (fovFeature == null) {
            throw new IllegalStateException(
                    "FOV feature is not installed");
        }
        return fovFeature.module();
    }

    public synchronized Minecraft189FullbrightModule fullbright() {
        requireOpen();
        if (fullbrightFeature == null) {
            throw new IllegalStateException(
                    "fullbright feature is not installed");
        }
        return fullbrightFeature.module();
    }

    public synchronized boolean closed() {
        return closed;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }

        RuntimeException failure = null;
        if (noBobbingFeature != null) {
            try {
                noBobbingFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = closeFailure;
            }
        }

        if (fovFeature != null) {
            try {
                fovFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = append(
                        failure,
                        closeFailure);
            }
        }

        try {
            crosshairFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        if (fullbrightFeature != null) {
            try {
                fullbrightFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = append(
                        failure,
                        closeFailure);
            }
        }

        try {
            triggerBotFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(failure, closeFailure);
        }

        try {
            jumpResetFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(failure, closeFailure);
        }

        try {
            wTapFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            spinFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            aimAssistFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            jitterFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            velocityFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            autoClickerFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            noHitDelayFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            timerSpeedFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            speedMineFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            fastBreakFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            fastPlaceFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            damageBoostFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            airSpeedFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            movementSpeedFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            lowHopFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            highJumpFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            bunnyHopFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            longJumpFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            freezeFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            reverseStepFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            noGravityFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            fastFallFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            glideFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            targetStrafeFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(failure, closeFailure);
        }

        try {
            strafeFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            flightFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            noClipFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            noWebFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            noFallFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            stepFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            noSlowFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            autoSneakFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            airJumpFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            autoJumpFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            autoSprintFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            speedFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            heldItemFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            serverFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            weatherFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            worldTimeFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            hotbarSlotFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            pingFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            experienceFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            potionEffectsFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            hungerFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            armorFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            hurtTimeFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            healthFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            movementStatusFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            dimensionFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            nearbyPlayersFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(failure, closeFailure);
        }

        try {
            targetHudFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(failure, closeFailure);
        }

        try {
            directionFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            coordinatesFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            cpsFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            fpsFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            keystrokesFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            arrayListFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            if (moduleController.stateOf(
                    Minecraft189WatermarkModule.ID)
                    != ModuleState.DISABLED) {
                moduleController.disable(
                        Minecraft189WatermarkModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        failure = close(yBinding, failure);
        failure = close(xBinding, failure);
        failure = close(textBinding, failure);
        failure = close(yPresentation, failure);
        failure = close(xPresentation, failure);
        failure = close(textPresentation, failure);
        failure = close(ySetting, failure);
        failure = close(xSetting, failure);
        failure = close(textSetting, failure);
        failure = close(watermarkPresentation, failure);
        failure = close(watermarkRegistration, failure);
        failure = close(visualsCategory, failure);
        failure = close(playerCategory, failure);
        failure = close(movementCategory, failure);
        failure = close(combatCategory, failure);

        if (failure != null) {
            throw failure;
        }
    }

    private synchronized void requireOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "Minecraft 1.8.9 feature catalog is closed");
        }
    }

    private static RuntimeException close(
            final AutoCloseable closeable,
            final RuntimeException primary) {
        try {
            closeable.close();
            return primary;
        } catch (RuntimeException failure) {
            return append(
                    primary,
                    failure);
        } catch (Exception failure) {
            return append(
                    primary,
                    new IllegalStateException(
                            "feature registration close failed",
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
            primary.addSuppressed(
                    cleanupFailure);
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
