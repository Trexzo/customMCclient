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

final class Minecraft189AutoClickerFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoClickerModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration minSetting;
    private final SettingRegistry.Registration maxSetting;
    private final SettingRegistry.Registration pauseWhileRightClickingSetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingRegistry.Registration requireNearbyPlayerSetting;
    private final SettingRegistry.Registration maxPlayerDistanceSetting;
    private final SettingRegistry.Registration requireForwardSetting;
    private final SettingRegistry.Registration requireHoldSetting;
    private final SettingRegistry.Registration rampUpSetting;
    private final SettingRegistry.Registration rampUpTicksSetting;
    private final SettingPresentationRegistry.Registration minPresentation;
    private final SettingPresentationRegistry.Registration maxPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileRightClickingPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final SettingPresentationRegistry.Registration requireNearbyPlayerPresentation;
    private final SettingPresentationRegistry.Registration maxPlayerDistancePresentation;
    private final SettingPresentationRegistry.Registration requireForwardPresentation;
    private final SettingPresentationRegistry.Registration requireHoldPresentation;
    private final SettingPresentationRegistry.Registration rampUpPresentation;
    private final SettingPresentationRegistry.Registration rampUpTicksPresentation;
    private final ModuleSettingRegistry.Registration minBinding;
    private final ModuleSettingRegistry.Registration maxBinding;
    private final ModuleSettingRegistry.Registration pauseWhileRightClickingBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private final ModuleSettingRegistry.Registration requireNearbyPlayerBinding;
    private final ModuleSettingRegistry.Registration maxPlayerDistanceBinding;
    private final ModuleSettingRegistry.Registration requireForwardBinding;
    private final ModuleSettingRegistry.Registration requireHoldBinding;
    private final ModuleSettingRegistry.Registration rampUpBinding;
    private final ModuleSettingRegistry.Registration rampUpTicksBinding;
    private boolean closed;

    private Minecraft189AutoClickerFeature(
            final ModuleController controller,
            final Minecraft189AutoClickerModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration minSetting,
            final SettingRegistry.Registration maxSetting,
            final SettingRegistry.Registration pauseWhileRightClickingSetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingRegistry.Registration requireNearbyPlayerSetting,
            final SettingRegistry.Registration maxPlayerDistanceSetting,
            final SettingRegistry.Registration requireForwardSetting,
            final SettingRegistry.Registration requireHoldSetting,
            final SettingRegistry.Registration rampUpSetting,
            final SettingRegistry.Registration rampUpTicksSetting,
            final SettingPresentationRegistry.Registration minPresentation,
            final SettingPresentationRegistry.Registration maxPresentation,
            final SettingPresentationRegistry.Registration pauseWhileRightClickingPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final SettingPresentationRegistry.Registration requireNearbyPlayerPresentation,
            final SettingPresentationRegistry.Registration maxPlayerDistancePresentation,
            final SettingPresentationRegistry.Registration requireForwardPresentation,
            final SettingPresentationRegistry.Registration requireHoldPresentation,
            final SettingPresentationRegistry.Registration rampUpPresentation,
            final SettingPresentationRegistry.Registration rampUpTicksPresentation,
            final ModuleSettingRegistry.Registration minBinding,
            final ModuleSettingRegistry.Registration maxBinding,
            final ModuleSettingRegistry.Registration pauseWhileRightClickingBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding,
            final ModuleSettingRegistry.Registration requireNearbyPlayerBinding,
            final ModuleSettingRegistry.Registration maxPlayerDistanceBinding,
            final ModuleSettingRegistry.Registration requireForwardBinding,
            final ModuleSettingRegistry.Registration requireHoldBinding,
            final ModuleSettingRegistry.Registration rampUpBinding,
            final ModuleSettingRegistry.Registration rampUpTicksBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.minSetting = minSetting;
        this.maxSetting = maxSetting;
        this.pauseWhileRightClickingSetting = pauseWhileRightClickingSetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.requireNearbyPlayerSetting = requireNearbyPlayerSetting;
        this.maxPlayerDistanceSetting = maxPlayerDistanceSetting;
        this.requireForwardSetting = requireForwardSetting;
        this.requireHoldSetting = requireHoldSetting;
        this.rampUpSetting = rampUpSetting;
        this.rampUpTicksSetting = rampUpTicksSetting;
        this.minPresentation = minPresentation;
        this.maxPresentation = maxPresentation;
        this.pauseWhileRightClickingPresentation = pauseWhileRightClickingPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.requireNearbyPlayerPresentation = requireNearbyPlayerPresentation;
        this.maxPlayerDistancePresentation = maxPlayerDistancePresentation;
        this.requireForwardPresentation = requireForwardPresentation;
        this.requireHoldPresentation = requireHoldPresentation;
        this.rampUpPresentation = rampUpPresentation;
        this.rampUpTicksPresentation = rampUpTicksPresentation;
        this.minBinding = minBinding;
        this.maxBinding = maxBinding;
        this.pauseWhileRightClickingBinding = pauseWhileRightClickingBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
        this.requireNearbyPlayerBinding = requireNearbyPlayerBinding;
        this.maxPlayerDistanceBinding = maxPlayerDistanceBinding;
        this.requireForwardBinding = requireForwardBinding;
        this.requireHoldBinding = requireHoldBinding;
        this.rampUpBinding = rampUpBinding;
        this.rampUpTicksBinding = rampUpTicksBinding;
    }

    static Minecraft189AutoClickerFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AutoClickerModule module =
                new Minecraft189AutoClickerModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration minSetting = null;
        SettingRegistry.Registration maxSetting = null;
        SettingRegistry.Registration pauseWhileRightClickingSetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingRegistry.Registration requireNearbyPlayerSetting = null;
        SettingRegistry.Registration maxPlayerDistanceSetting = null;
        SettingRegistry.Registration requireForwardSetting = null;
        SettingRegistry.Registration requireHoldSetting = null;
        SettingRegistry.Registration rampUpSetting = null;
        SettingRegistry.Registration rampUpTicksSetting = null;
        SettingPresentationRegistry.Registration minPresentation = null;
        SettingPresentationRegistry.Registration maxPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileRightClickingPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        SettingPresentationRegistry.Registration requireNearbyPlayerPresentation = null;
        SettingPresentationRegistry.Registration maxPlayerDistancePresentation = null;
        SettingPresentationRegistry.Registration requireForwardPresentation = null;
        SettingPresentationRegistry.Registration requireHoldPresentation = null;
        SettingPresentationRegistry.Registration rampUpPresentation = null;
        SettingPresentationRegistry.Registration rampUpTicksPresentation = null;
        ModuleSettingRegistry.Registration minBinding = null;
        ModuleSettingRegistry.Registration maxBinding = null;
        ModuleSettingRegistry.Registration pauseWhileRightClickingBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
        ModuleSettingRegistry.Registration requireNearbyPlayerBinding = null;
        ModuleSettingRegistry.Registration maxPlayerDistanceBinding = null;
        ModuleSettingRegistry.Registration requireForwardBinding = null;
        ModuleSettingRegistry.Registration requireHoldBinding = null;
        ModuleSettingRegistry.Registration rampUpBinding = null;
        ModuleSettingRegistry.Registration rampUpTicksBinding = null;

        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AutoClickerModule.ID,
                                    "Auto Clicker",
                                    "Clicks at configurable CPS, optionally requiring the physical left mouse button.",
                                    Minecraft189FeatureCatalog
                                            .COMBAT_CATEGORY_ID,
                                    10));
            minSetting =
                    settings.register(
                            module.minCpsSetting());
            maxSetting =
                    settings.register(
                            module.maxCpsSetting());
            pauseWhileRightClickingSetting =
                    settings.register(
                            module.pauseWhileRightClickingSetting());
            pauseWhileSneakingSetting = settings.register(module.pauseWhileSneakingSetting());
            requireNearbyPlayerSetting =
                    settings.register(module.requireNearbyPlayerSetting());
            maxPlayerDistanceSetting =
                    settings.register(module.maxPlayerDistanceSetting());
            requireForwardSetting =
                    settings.register(
                            module.requireForwardSetting());
            requireHoldSetting =
                    settings.register(
                            module.requireHoldSetting());
            rampUpSetting = settings.register(module.rampUpSetting());
            rampUpTicksSetting = settings.register(module.rampUpTicksSetting());
            minPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.MIN_CPS_SETTING_ID,
                                    "Min CPS",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            1.0D,
                                            20.0D,
                                            1.0D)));
            maxPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.MAX_CPS_SETTING_ID,
                                    "Max CPS",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            1.0D,
                                            20.0D,
                                            1.0D)));
            pauseWhileRightClickingPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.PAUSE_WHILE_RIGHT_CLICKING_SETTING_ID,
                                    "Pause While Right Clicking",
                                    SettingValueKind.BOOLEAN,
                                    25));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoClickerModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 27));
            requireNearbyPlayerPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoClickerModule.REQUIRE_NEARBY_PLAYER_SETTING_ID,
                            "Require Nearby Player", SettingValueKind.BOOLEAN, 30));
            maxPlayerDistancePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoClickerModule.MAX_PLAYER_DISTANCE_SETTING_ID,
                            "Nearby Range", SettingValueKind.DOUBLE, 40,
                            new SettingNumericSpec(0.5D, 16.0D, 0.5D)));
            requireForwardPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.REQUIRE_FORWARD_SETTING_ID,
                                    "Require Forward",
                                    SettingValueKind.BOOLEAN,
                                    15));
            requireHoldPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.REQUIRE_HOLD_SETTING_ID,
                                    "Require Hold",
                                    SettingValueKind.BOOLEAN,
                                    20));
            rampUpPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoClickerModule.RAMP_UP_SETTING_ID,
                            "CPS Ramp-Up", SettingValueKind.BOOLEAN, 50));
            rampUpTicksPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoClickerModule.RAMP_UP_TICKS_SETTING_ID,
                            "Ramp-Up Ticks", SettingValueKind.INTEGER, 60,
                            new SettingNumericSpec(
                                    Minecraft189AutoClickerModule.MINIMUM_RAMP_UP_TICKS,
                                    Minecraft189AutoClickerModule.MAXIMUM_RAMP_UP_TICKS,
                                    1.0D)));
            minBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.MIN_CPS_SETTING_ID,
                                    0));
            maxBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.MAX_CPS_SETTING_ID,
                                    10));
            pauseWhileRightClickingBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.PAUSE_WHILE_RIGHT_CLICKING_SETTING_ID,
                                    25));
            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AutoClickerModule.ID,
                            Minecraft189AutoClickerModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 27));
            requireNearbyPlayerBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AutoClickerModule.ID,
                            Minecraft189AutoClickerModule.REQUIRE_NEARBY_PLAYER_SETTING_ID, 30));
            maxPlayerDistanceBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AutoClickerModule.ID,
                            Minecraft189AutoClickerModule.MAX_PLAYER_DISTANCE_SETTING_ID, 40));
            requireForwardBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.REQUIRE_FORWARD_SETTING_ID,
                                    15));
            requireHoldBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.REQUIRE_HOLD_SETTING_ID,
                                    20));

            rampUpBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AutoClickerModule.ID,
                            Minecraft189AutoClickerModule.RAMP_UP_SETTING_ID, 50));
            rampUpTicksBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AutoClickerModule.ID,
                            Minecraft189AutoClickerModule.RAMP_UP_TICKS_SETTING_ID, 60));

            return new Minecraft189AutoClickerFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    minSetting,
                    maxSetting,
                    pauseWhileRightClickingSetting,
                    pauseWhileSneakingSetting,
                    requireNearbyPlayerSetting,
                    maxPlayerDistanceSetting,
                    requireForwardSetting,
                    requireHoldSetting,
                    rampUpSetting,
                    rampUpTicksSetting,
                    minPresentation,
                    maxPresentation,
                    pauseWhileRightClickingPresentation,
                    pauseWhileSneakingPresentation,
                    requireNearbyPlayerPresentation,
                    maxPlayerDistancePresentation,
                    requireForwardPresentation,
                    requireHoldPresentation,
                    rampUpPresentation,
                    rampUpTicksPresentation,
                    minBinding,
                    maxBinding,
                    pauseWhileRightClickingBinding,
                    pauseWhileSneakingBinding,
                    requireNearbyPlayerBinding,
                    maxPlayerDistanceBinding,
                    requireForwardBinding,
                    requireHoldBinding,
                    rampUpBinding,
                    rampUpTicksBinding);
        } catch (RuntimeException failure) {
            closeQuietly(rampUpTicksBinding, failure);
            closeQuietly(rampUpBinding, failure);
            closeQuietly(rampUpTicksPresentation, failure);
            closeQuietly(rampUpPresentation, failure);
            closeQuietly(rampUpTicksSetting, failure);
            closeQuietly(rampUpSetting, failure);
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(
                    requireHoldBinding,
                    failure);
            closeQuietly(
                    maxPlayerDistanceBinding, failure);
            closeQuietly(
                    requireNearbyPlayerBinding, failure);
            closeQuietly(
                    requireForwardBinding,
                    failure);
            closeQuietly(
                    pauseWhileRightClickingBinding,
                    failure);
            closeQuietly(
                    maxBinding,
                    failure);
            closeQuietly(
                    minBinding,
                    failure);
            closeQuietly(
                    requireHoldPresentation,
                    failure);
            closeQuietly(
                    maxPlayerDistancePresentation, failure);
            closeQuietly(
                    requireNearbyPlayerPresentation, failure);
            closeQuietly(
                    requireForwardPresentation,
                    failure);
            closeQuietly(
                    pauseWhileRightClickingPresentation,
                    failure);
            closeQuietly(
                    maxPresentation,
                    failure);
            closeQuietly(
                    minPresentation,
                    failure);
            closeQuietly(
                    requireHoldSetting,
                    failure);
            closeQuietly(
                    maxPlayerDistanceSetting, failure);
            closeQuietly(
                    requireNearbyPlayerSetting, failure);
            closeQuietly(
                    requireForwardSetting,
                    failure);
            closeQuietly(
                    pauseWhileRightClickingSetting,
                    failure);
            closeQuietly(
                    maxSetting,
                    failure);
            closeQuietly(
                    minSetting,
                    failure);
            closeQuietly(
                    presentation,
                    failure);
            closeQuietly(
                    moduleRegistration,
                    failure);
            throw failure;
        }
    }

    Minecraft189AutoClickerModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "auto-clicker feature is closed");
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
                    Minecraft189AutoClickerModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AutoClickerModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(
                requireHoldBinding,
                failure);
        failure = close(maxPlayerDistanceBinding, failure);
        failure = close(requireNearbyPlayerBinding, failure);
        failure = close(
                requireForwardBinding,
                failure);
        failure = close(
                pauseWhileRightClickingBinding,
                failure);
        failure = close(
                maxBinding,
                failure);
        failure = close(
                minBinding,
                failure);
        failure = close(
                requireHoldPresentation,
                failure);
        failure = close(maxPlayerDistancePresentation, failure);
        failure = close(requireNearbyPlayerPresentation, failure);
        failure = close(
                requireForwardPresentation,
                failure);
        failure = close(
                pauseWhileRightClickingPresentation,
                failure);
        failure = close(
                maxPresentation,
                failure);
        failure = close(
                minPresentation,
                failure);
        failure = close(
                requireHoldSetting,
                failure);
        failure = close(maxPlayerDistanceSetting, failure);
        failure = close(requireNearbyPlayerSetting, failure);
        failure = close(
                requireForwardSetting,
                failure);
        failure = close(
                pauseWhileRightClickingSetting,
                failure);
        failure = close(
                maxSetting,
                failure);
        failure = close(
                minSetting,
                failure);
        failure = close(
                presentation,
                failure);
        failure = close(
                moduleRegistration,
                failure);

        if (failure != null) {
            throw failure;
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
                            "auto-clicker feature close failed",
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
