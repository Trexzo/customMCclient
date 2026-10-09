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

final class Minecraft189BunnyHopFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189BunnyHopModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration speedSetting;
    private final SettingPresentationRegistry.Registration speedPresentation;
    private final ModuleSettingRegistry.Registration speedBinding;
    private final SettingRegistry.Registration smoothAccelerationSetting;
    private final SettingRegistry.Registration accelerationPercentSetting;
    private final SettingPresentationRegistry.Registration smoothAccelerationPresentation;
    private final SettingPresentationRegistry.Registration accelerationPercentPresentation;
    private final ModuleSettingRegistry.Registration smoothAccelerationBinding;
    private final ModuleSettingRegistry.Registration accelerationPercentBinding;
    private final SettingRegistry.Registration landingDelaySetting;
    private final SettingPresentationRegistry.Registration landingDelayPresentation;
    private final ModuleSettingRegistry.Registration landingDelayBinding;
    private boolean closed;

    private Minecraft189BunnyHopFeature(
            final ModuleController controller,
            final Minecraft189BunnyHopModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration speedSetting,
            final SettingPresentationRegistry.Registration speedPresentation,
            final ModuleSettingRegistry.Registration speedBinding,
            final SettingRegistry.Registration smoothAccelerationSetting,
            final SettingRegistry.Registration accelerationPercentSetting,
            final SettingPresentationRegistry.Registration smoothAccelerationPresentation,
            final SettingPresentationRegistry.Registration accelerationPercentPresentation,
            final ModuleSettingRegistry.Registration smoothAccelerationBinding,
            final ModuleSettingRegistry.Registration accelerationPercentBinding,
            final SettingRegistry.Registration landingDelaySetting,
            final SettingPresentationRegistry.Registration landingDelayPresentation,
            final ModuleSettingRegistry.Registration landingDelayBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.speedSetting = speedSetting;
        this.speedPresentation = speedPresentation;
        this.speedBinding = speedBinding;
        this.smoothAccelerationSetting = smoothAccelerationSetting;
        this.accelerationPercentSetting = accelerationPercentSetting;
        this.smoothAccelerationPresentation = smoothAccelerationPresentation;
        this.accelerationPercentPresentation = accelerationPercentPresentation;
        this.smoothAccelerationBinding = smoothAccelerationBinding;
        this.accelerationPercentBinding = accelerationPercentBinding;
        this.landingDelaySetting = landingDelaySetting;
        this.landingDelayPresentation = landingDelayPresentation;
        this.landingDelayBinding = landingDelayBinding;
    }

    static Minecraft189BunnyHopFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState) {
        final Minecraft189BunnyHopModule module =
                new Minecraft189BunnyHopModule(
                        inputState);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration speedSetting = null;
        SettingPresentationRegistry.Registration speedPresentation = null;
        ModuleSettingRegistry.Registration speedBinding = null;
        SettingRegistry.Registration smoothAccelerationSetting = null;
        SettingRegistry.Registration accelerationPercentSetting = null;
        SettingPresentationRegistry.Registration smoothAccelerationPresentation = null;
        SettingPresentationRegistry.Registration accelerationPercentPresentation = null;
        ModuleSettingRegistry.Registration smoothAccelerationBinding = null;
        ModuleSettingRegistry.Registration accelerationPercentBinding = null;
        SettingRegistry.Registration landingDelaySetting = null;
        SettingPresentationRegistry.Registration landingDelayPresentation = null;
        ModuleSettingRegistry.Registration landingDelayBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189BunnyHopModule.ID,
                                    "Bunny Hop",
                                    "Automatically jumps while moving and applies configurable yaw-relative horizontal speed.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    140));
            speedSetting =
                    settings.register(
                            module.speedSetting());
            smoothAccelerationSetting = settings.register(
                    module.smoothAccelerationSetting());
            accelerationPercentSetting = settings.register(
                    module.accelerationPercentSetting());
            landingDelaySetting = settings.register(module.landingDelayTicksSetting());
            speedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189BunnyHopModule.SPEED_SETTING_ID,
                                    "Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189BunnyHopModule.MINIMUM_SPEED,
                                            Minecraft189BunnyHopModule.MAXIMUM_SPEED,
                                            0.05D)));
            smoothAccelerationPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189BunnyHopModule.SMOOTH_ACCELERATION_SETTING_ID,
                            "Smooth Acceleration", SettingValueKind.BOOLEAN, 10));
            accelerationPercentPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189BunnyHopModule.ACCELERATION_PERCENT_SETTING_ID,
                            "Acceleration %", SettingValueKind.INTEGER, 20,
                            new SettingNumericSpec(10.0D, 100.0D, 5.0D)));
            landingDelayPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189BunnyHopModule.LANDING_DELAY_SETTING_ID,
                            "Landing Delay Ticks", SettingValueKind.INTEGER, 30,
                            new SettingNumericSpec(0.0D,
                                    Minecraft189BunnyHopModule.MAXIMUM_LANDING_DELAY_TICKS,
                                    1.0D)));
            speedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189BunnyHopModule.ID,
                                    Minecraft189BunnyHopModule.SPEED_SETTING_ID,
                                    0));

            smoothAccelerationBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189BunnyHopModule.ID,
                            Minecraft189BunnyHopModule.SMOOTH_ACCELERATION_SETTING_ID, 10));
            accelerationPercentBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189BunnyHopModule.ID,
                            Minecraft189BunnyHopModule.ACCELERATION_PERCENT_SETTING_ID, 20));

            landingDelayBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189BunnyHopModule.ID,
                            Minecraft189BunnyHopModule.LANDING_DELAY_SETTING_ID, 30));

            return new Minecraft189BunnyHopFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    speedSetting,
                    speedPresentation,
                    speedBinding,
                    smoothAccelerationSetting,
                    accelerationPercentSetting,
                    smoothAccelerationPresentation,
                    accelerationPercentPresentation,
                    smoothAccelerationBinding,
                    accelerationPercentBinding,
                    landingDelaySetting,
                    landingDelayPresentation,
                    landingDelayBinding);
        } catch (RuntimeException failure) {
            closeQuietly(landingDelayBinding, failure);
            closeQuietly(landingDelayPresentation, failure);
            closeQuietly(landingDelaySetting, failure);
            closeQuietly(accelerationPercentBinding, failure);
            closeQuietly(smoothAccelerationBinding, failure);
            closeQuietly(accelerationPercentPresentation, failure);
            closeQuietly(smoothAccelerationPresentation, failure);
            closeQuietly(accelerationPercentSetting, failure);
            closeQuietly(smoothAccelerationSetting, failure);
            closeQuietly(speedBinding, failure);
            closeQuietly(speedPresentation, failure);
            closeQuietly(speedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189BunnyHopModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "bunny-hop feature is closed");
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
                    Minecraft189BunnyHopModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189BunnyHopModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(landingDelayBinding, failure);
        failure = close(landingDelayPresentation, failure);
        failure = close(landingDelaySetting, failure);
        failure = close(accelerationPercentBinding, failure);
        failure = close(smoothAccelerationBinding, failure);
        failure = close(accelerationPercentPresentation, failure);
        failure = close(smoothAccelerationPresentation, failure);
        failure = close(accelerationPercentSetting, failure);
        failure = close(smoothAccelerationSetting, failure);
        failure = close(speedBinding, failure);
        failure = close(speedPresentation, failure);
        failure = close(speedSetting, failure);
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
                            "bunny-hop feature close failed",
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
