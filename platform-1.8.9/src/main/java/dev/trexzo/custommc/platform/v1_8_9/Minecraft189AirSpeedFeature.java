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

final class Minecraft189AirSpeedFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AirSpeedModule module;
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
    private boolean closed;

    private Minecraft189AirSpeedFeature(
            final ModuleController controller,
            final Minecraft189AirSpeedModule module,
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
            final ModuleSettingRegistry.Registration accelerationPercentBinding) {
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
    }

    static Minecraft189AirSpeedFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState) {
        final Minecraft189AirSpeedModule module =
                new Minecraft189AirSpeedModule(
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
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AirSpeedModule.ID,
                                    "Air Speed",
                                    "Applies configurable yaw-relative horizontal speed while airborne.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    190));
            speedSetting =
                    settings.register(
                            module.speedSetting());
            smoothAccelerationSetting = settings.register(
                    module.smoothAccelerationSetting());
            accelerationPercentSetting = settings.register(
                    module.accelerationPercentSetting());
            speedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AirSpeedModule.SPEED_SETTING_ID,
                                    "Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189AirSpeedModule.MINIMUM_SPEED,
                                            Minecraft189AirSpeedModule.MAXIMUM_SPEED,
                                            0.05D)));
            smoothAccelerationPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AirSpeedModule.SMOOTH_ACCELERATION_SETTING_ID,
                            "Smooth Acceleration", SettingValueKind.BOOLEAN, 10));
            accelerationPercentPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AirSpeedModule.ACCELERATION_PERCENT_SETTING_ID,
                            "Acceleration %", SettingValueKind.INTEGER, 20,
                            new SettingNumericSpec(10.0D, 100.0D, 5.0D)));
            speedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AirSpeedModule.ID,
                                    Minecraft189AirSpeedModule.SPEED_SETTING_ID,
                                    0));

            smoothAccelerationBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AirSpeedModule.ID,
                            Minecraft189AirSpeedModule.SMOOTH_ACCELERATION_SETTING_ID, 10));
            accelerationPercentBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AirSpeedModule.ID,
                            Minecraft189AirSpeedModule.ACCELERATION_PERCENT_SETTING_ID, 20));

            return new Minecraft189AirSpeedFeature(
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
                    accelerationPercentBinding);
        } catch (RuntimeException failure) {
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

    Minecraft189AirSpeedModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "air-speed feature is closed");
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
                    Minecraft189AirSpeedModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AirSpeedModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

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
                            "air-speed feature close failed",
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
