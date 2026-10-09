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

final class Minecraft189WTapFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189WTapModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration requireGroundSetting;
    private final SettingRegistry.Registration cooldownSetting;
    private final SettingRegistry.Registration resetTicksSetting;
    private final SettingRegistry.Registration requireForwardSetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingRegistry.Registration requireNearbyPlayerSetting;
    private final SettingRegistry.Registration maxPlayerDistanceSetting;
    private final SettingPresentationRegistry.Registration requireGroundPresentation;
    private final SettingPresentationRegistry.Registration cooldownPresentation;
    private final SettingPresentationRegistry.Registration resetTicksPresentation;
    private final SettingPresentationRegistry.Registration requireForwardPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final SettingPresentationRegistry.Registration requireNearbyPlayerPresentation;
    private final SettingPresentationRegistry.Registration maxPlayerDistancePresentation;
    private final ModuleSettingRegistry.Registration requireGroundBinding;
    private final ModuleSettingRegistry.Registration cooldownBinding;
    private final ModuleSettingRegistry.Registration resetTicksBinding;
    private final ModuleSettingRegistry.Registration requireForwardBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private final ModuleSettingRegistry.Registration requireNearbyPlayerBinding;
    private final ModuleSettingRegistry.Registration maxPlayerDistanceBinding;
    private final SettingRegistry.Registration cancelOnReleaseSetting;
    private final SettingPresentationRegistry.Registration cancelOnReleasePresentation;
    private final ModuleSettingRegistry.Registration cancelOnReleaseBinding;
    private final SettingRegistry.Registration minReleaseSetting;
    private final SettingPresentationRegistry.Registration minReleasePresentation;
    private final ModuleSettingRegistry.Registration minReleaseBinding;
    private boolean closed;

    private Minecraft189WTapFeature(
            final ModuleController controller,
            final Minecraft189WTapModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration requireGroundSetting,
            final SettingRegistry.Registration cooldownSetting,
            final SettingRegistry.Registration resetTicksSetting,
            final SettingRegistry.Registration requireForwardSetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingRegistry.Registration requireNearbyPlayerSetting,
            final SettingRegistry.Registration maxPlayerDistanceSetting,
            final SettingPresentationRegistry.Registration requireGroundPresentation,
            final SettingPresentationRegistry.Registration cooldownPresentation,
            final SettingPresentationRegistry.Registration resetTicksPresentation,
            final SettingPresentationRegistry.Registration requireForwardPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final SettingPresentationRegistry.Registration requireNearbyPlayerPresentation,
            final SettingPresentationRegistry.Registration maxPlayerDistancePresentation,
            final ModuleSettingRegistry.Registration requireGroundBinding,
            final ModuleSettingRegistry.Registration cooldownBinding,
            final ModuleSettingRegistry.Registration resetTicksBinding,
            final ModuleSettingRegistry.Registration requireForwardBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding,
            final ModuleSettingRegistry.Registration requireNearbyPlayerBinding,
            final ModuleSettingRegistry.Registration maxPlayerDistanceBinding,
            final SettingRegistry.Registration cancelOnReleaseSetting,
            final SettingPresentationRegistry.Registration cancelOnReleasePresentation,
            final ModuleSettingRegistry.Registration cancelOnReleaseBinding,
            final SettingRegistry.Registration minReleaseSetting,
            final SettingPresentationRegistry.Registration minReleasePresentation,
            final ModuleSettingRegistry.Registration minReleaseBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.requireGroundSetting = requireGroundSetting;
        this.cooldownSetting = cooldownSetting;
        this.resetTicksSetting = resetTicksSetting;
        this.requireForwardSetting = requireForwardSetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.requireNearbyPlayerSetting = requireNearbyPlayerSetting;
        this.maxPlayerDistanceSetting = maxPlayerDistanceSetting;
        this.requireGroundPresentation = requireGroundPresentation;
        this.cooldownPresentation = cooldownPresentation;
        this.resetTicksPresentation = resetTicksPresentation;
        this.requireForwardPresentation = requireForwardPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.requireNearbyPlayerPresentation = requireNearbyPlayerPresentation;
        this.maxPlayerDistancePresentation = maxPlayerDistancePresentation;
        this.requireGroundBinding = requireGroundBinding;
        this.cooldownBinding = cooldownBinding;
        this.resetTicksBinding = resetTicksBinding;
        this.requireForwardBinding = requireForwardBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
        this.requireNearbyPlayerBinding = requireNearbyPlayerBinding;
        this.maxPlayerDistanceBinding = maxPlayerDistanceBinding;
        this.cancelOnReleaseSetting = cancelOnReleaseSetting;
        this.cancelOnReleasePresentation = cancelOnReleasePresentation;
        this.cancelOnReleaseBinding = cancelOnReleaseBinding;
        this.minReleaseSetting = minReleaseSetting;
        this.minReleasePresentation = minReleasePresentation;
        this.minReleaseBinding = minReleaseBinding;
    }

    static Minecraft189WTapFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189WTapModule module =
                new Minecraft189WTapModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration requireGroundSetting = null;
        SettingRegistry.Registration cooldownSetting = null;
        SettingRegistry.Registration resetTicksSetting = null;
        SettingRegistry.Registration requireForwardSetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingRegistry.Registration requireNearbyPlayerSetting = null;
        SettingRegistry.Registration maxPlayerDistanceSetting = null;
        SettingPresentationRegistry.Registration requireGroundPresentation = null;
        SettingPresentationRegistry.Registration cooldownPresentation = null;
        SettingPresentationRegistry.Registration resetTicksPresentation = null;
        SettingPresentationRegistry.Registration requireForwardPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        SettingPresentationRegistry.Registration requireNearbyPlayerPresentation = null;
        SettingPresentationRegistry.Registration maxPlayerDistancePresentation = null;
        ModuleSettingRegistry.Registration requireGroundBinding = null;
        ModuleSettingRegistry.Registration cooldownBinding = null;
        ModuleSettingRegistry.Registration resetTicksBinding = null;
        ModuleSettingRegistry.Registration requireForwardBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
        ModuleSettingRegistry.Registration requireNearbyPlayerBinding = null;
        ModuleSettingRegistry.Registration maxPlayerDistanceBinding = null;
        SettingRegistry.Registration cancelOnReleaseSetting = null;
        SettingPresentationRegistry.Registration cancelOnReleasePresentation = null;
        ModuleSettingRegistry.Registration cancelOnReleaseBinding = null;
        SettingRegistry.Registration minReleaseSetting = null;
        SettingPresentationRegistry.Registration minReleasePresentation = null;
        ModuleSettingRegistry.Registration minReleaseBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189WTapModule.ID,
                                    "W-Tap",
                                    "Resets sprint for a configurable window on a fresh physical left-click press.",
                                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID,
                                    50));
            requireGroundSetting =
                    settings.register(
                            module.requireGroundSetting());
            cooldownSetting =
                    settings.register(
                            module.cooldownTicksSetting());
            resetTicksSetting =
                    settings.register(
                            module.resetTicksSetting());
            requireForwardSetting =
                    settings.register(
                            module.requireForwardSetting());
            pauseWhileSneakingSetting = settings.register(
                    module.pauseWhileSneakingSetting());
            requireNearbyPlayerSetting = settings.register(
                    module.requireNearbyPlayerSetting());
            maxPlayerDistanceSetting = settings.register(
                    module.maxPlayerDistanceSetting());
            cancelOnReleaseSetting = settings.register(
                    module.cancelResetOnReleaseSetting());
            minReleaseSetting = settings.register(module.minReleaseTicksSetting());
            requireGroundPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WTapModule.REQUIRE_GROUND_SETTING_ID,
                                    "Ground Only",
                                    SettingValueKind.BOOLEAN,
                                    0));
            cooldownPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WTapModule.COOLDOWN_TICKS_SETTING_ID,
                                    "Cooldown",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189WTapModule.MINIMUM_COOLDOWN_TICKS,
                                            Minecraft189WTapModule.MAXIMUM_COOLDOWN_TICKS,
                                            1.0D)));
            resetTicksPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WTapModule.RESET_TICKS_SETTING_ID,
                                    "Reset Ticks",
                                    SettingValueKind.INTEGER,
                                    20,
                                    new SettingNumericSpec(
                                            Minecraft189WTapModule.MINIMUM_RESET_TICKS,
                                            Minecraft189WTapModule.MAXIMUM_RESET_TICKS,
                                            1.0D)));
            requireForwardPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WTapModule.REQUIRE_FORWARD_SETTING_ID,
                                    "Require Forward",
                                    SettingValueKind.BOOLEAN,
                                    30));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189WTapModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 40));
            requireNearbyPlayerPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189WTapModule.REQUIRE_NEARBY_PLAYER_SETTING_ID,
                            "Require Nearby Player", SettingValueKind.BOOLEAN, 50));
            maxPlayerDistancePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189WTapModule.MAX_PLAYER_DISTANCE_SETTING_ID,
                            "Nearby Range", SettingValueKind.DOUBLE, 60,
                            new SettingNumericSpec(
                                    Minecraft189WTapModule.MINIMUM_MAX_PLAYER_DISTANCE,
                                    Minecraft189WTapModule.MAXIMUM_MAX_PLAYER_DISTANCE,
                                    0.5D)));
            cancelOnReleasePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189WTapModule.CANCEL_ON_RELEASE_SETTING_ID,
                            "Cancel Reset On Release", SettingValueKind.BOOLEAN, 70));
            minReleasePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189WTapModule.MIN_RELEASE_TICKS_SETTING_ID,
                            "Minimum Release Ticks", SettingValueKind.INTEGER, 80,
                            new SettingNumericSpec(0.0D,
                                    Minecraft189WTapModule.MAXIMUM_MIN_RELEASE_TICKS,
                                    1.0D)));
            requireGroundBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WTapModule.ID,
                                    Minecraft189WTapModule.REQUIRE_GROUND_SETTING_ID,
                                    0));
            cooldownBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WTapModule.ID,
                                    Minecraft189WTapModule.COOLDOWN_TICKS_SETTING_ID,
                                    10));
            resetTicksBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WTapModule.ID,
                                    Minecraft189WTapModule.RESET_TICKS_SETTING_ID,
                                    20));
            requireForwardBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WTapModule.ID,
                                    Minecraft189WTapModule.REQUIRE_FORWARD_SETTING_ID,
                                    30));

            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189WTapModule.ID,
                            Minecraft189WTapModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 40));

            requireNearbyPlayerBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189WTapModule.ID,
                            Minecraft189WTapModule.REQUIRE_NEARBY_PLAYER_SETTING_ID, 50));
            maxPlayerDistanceBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189WTapModule.ID,
                            Minecraft189WTapModule.MAX_PLAYER_DISTANCE_SETTING_ID, 60));

            cancelOnReleaseBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189WTapModule.ID,
                            Minecraft189WTapModule.CANCEL_ON_RELEASE_SETTING_ID, 70));

            minReleaseBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189WTapModule.ID,
                            Minecraft189WTapModule.MIN_RELEASE_TICKS_SETTING_ID,
                            80));

            return new Minecraft189WTapFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    requireGroundSetting,
                    cooldownSetting,
                    resetTicksSetting,
                    requireForwardSetting,
                    pauseWhileSneakingSetting,
                    requireNearbyPlayerSetting,
                    maxPlayerDistanceSetting,
                    requireGroundPresentation,
                    cooldownPresentation,
                    resetTicksPresentation,
                    requireForwardPresentation,
                    pauseWhileSneakingPresentation,
                    requireNearbyPlayerPresentation,
                    maxPlayerDistancePresentation,
                    requireGroundBinding,
                    cooldownBinding,
                    resetTicksBinding,
                    requireForwardBinding,
                    pauseWhileSneakingBinding,
                    requireNearbyPlayerBinding,
                    maxPlayerDistanceBinding,
                    cancelOnReleaseSetting,
                    cancelOnReleasePresentation,
                    cancelOnReleaseBinding,
                    minReleaseSetting,
                    minReleasePresentation,
                    minReleaseBinding);
        } catch (RuntimeException failure) {
            closeQuietly(minReleaseBinding, failure);
            closeQuietly(minReleasePresentation, failure);
            closeQuietly(minReleaseSetting, failure);
            closeQuietly(cancelOnReleaseBinding, failure);
            closeQuietly(cancelOnReleasePresentation, failure);
            closeQuietly(cancelOnReleaseSetting, failure);
            closeQuietly(maxPlayerDistanceBinding, failure);
            closeQuietly(requireNearbyPlayerBinding, failure);
            closeQuietly(maxPlayerDistancePresentation, failure);
            closeQuietly(requireNearbyPlayerPresentation, failure);
            closeQuietly(maxPlayerDistanceSetting, failure);
            closeQuietly(requireNearbyPlayerSetting, failure);
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(requireForwardBinding, failure);
            closeQuietly(resetTicksBinding, failure);
            closeQuietly(cooldownBinding, failure);
            closeQuietly(requireGroundBinding, failure);
            closeQuietly(requireForwardPresentation, failure);
            closeQuietly(resetTicksPresentation, failure);
            closeQuietly(cooldownPresentation, failure);
            closeQuietly(requireGroundPresentation, failure);
            closeQuietly(requireForwardSetting, failure);
            closeQuietly(resetTicksSetting, failure);
            closeQuietly(cooldownSetting, failure);
            closeQuietly(requireGroundSetting, failure);
            if (presentation != null) {
                presentation.close();
            }
            if (moduleRegistration != null) {
                moduleRegistration.close();
            }
            throw failure;
        }
    }

    Minecraft189WTapModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "w-tap feature is closed");
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
                    Minecraft189WTapModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189WTapModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(minReleaseBinding, failure);
        failure = close(minReleasePresentation, failure);
        failure = close(minReleaseSetting, failure);
        failure = close(cancelOnReleaseBinding, failure);
        failure = close(cancelOnReleasePresentation, failure);
        failure = close(cancelOnReleaseSetting, failure);
        failure = close(maxPlayerDistanceBinding, failure);
        failure = close(requireNearbyPlayerBinding, failure);
        failure = close(maxPlayerDistancePresentation, failure);
        failure = close(requireNearbyPlayerPresentation, failure);
        failure = close(maxPlayerDistanceSetting, failure);
        failure = close(requireNearbyPlayerSetting, failure);
        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(requireForwardBinding, failure);
        failure = close(resetTicksBinding, failure);
        failure = close(cooldownBinding, failure);
        failure = close(requireGroundBinding, failure);
        failure = close(requireForwardPresentation, failure);
        failure = close(resetTicksPresentation, failure);
        failure = close(cooldownPresentation, failure);
        failure = close(requireGroundPresentation, failure);
        failure = close(requireForwardSetting, failure);
        failure = close(resetTicksSetting, failure);
        failure = close(cooldownSetting, failure);
        failure = close(requireGroundSetting, failure);

        try {
            presentation.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }
        try {
            moduleRegistration.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

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
                            "w-tap feature close failed",
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
