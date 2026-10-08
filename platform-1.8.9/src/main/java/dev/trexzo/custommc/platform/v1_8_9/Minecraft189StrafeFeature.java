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

final class Minecraft189StrafeFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189StrafeModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration speedSetting;
    private final SettingRegistry.Registration smoothAccelerationSetting;
    private final SettingRegistry.Registration accelerationPercentSetting;
    private final SettingPresentationRegistry.Registration speedPresentation;
    private final SettingPresentationRegistry.Registration smoothAccelerationPresentation;
    private final SettingPresentationRegistry.Registration accelerationPercentPresentation;
    private final ModuleSettingRegistry.Registration speedBinding;
    private final ModuleSettingRegistry.Registration smoothAccelerationBinding;
    private final ModuleSettingRegistry.Registration accelerationPercentBinding;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private boolean closed;

    private Minecraft189StrafeFeature(
            final ModuleController controller,
            final Minecraft189StrafeModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration speedSetting,
            final SettingRegistry.Registration smoothAccelerationSetting,
            final SettingRegistry.Registration accelerationPercentSetting,
            final SettingPresentationRegistry.Registration speedPresentation,
            final SettingPresentationRegistry.Registration smoothAccelerationPresentation,
            final SettingPresentationRegistry.Registration accelerationPercentPresentation,
            final ModuleSettingRegistry.Registration speedBinding,
            final ModuleSettingRegistry.Registration smoothAccelerationBinding,
            final ModuleSettingRegistry.Registration accelerationPercentBinding,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.speedSetting = speedSetting;
        this.smoothAccelerationSetting = smoothAccelerationSetting;
        this.accelerationPercentSetting = accelerationPercentSetting;
        this.speedPresentation = speedPresentation;
        this.smoothAccelerationPresentation = smoothAccelerationPresentation;
        this.accelerationPercentPresentation = accelerationPercentPresentation;
        this.speedBinding = speedBinding;
        this.smoothAccelerationBinding = smoothAccelerationBinding;
        this.accelerationPercentBinding = accelerationPercentBinding;
        this.groundOnlySetting = groundOnlySetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
    }

    static Minecraft189StrafeFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState) {
        final Minecraft189StrafeModule module =
                new Minecraft189StrafeModule(
                        inputState);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration speedSetting = null;
        SettingRegistry.Registration smoothAccelerationSetting = null;
        SettingRegistry.Registration accelerationPercentSetting = null;
        SettingPresentationRegistry.Registration speedPresentation = null;
        SettingPresentationRegistry.Registration smoothAccelerationPresentation = null;
        SettingPresentationRegistry.Registration accelerationPercentPresentation = null;
        ModuleSettingRegistry.Registration speedBinding = null;
        ModuleSettingRegistry.Registration smoothAccelerationBinding = null;
        ModuleSettingRegistry.Registration accelerationPercentBinding = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189StrafeModule.ID,
                                    "Strafe",
                                    "Applies configurable yaw-relative horizontal motion while movement keys are held.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    90));

            speedSetting =
                    settings.register(
                            module.speedSetting());
            smoothAccelerationSetting = settings.register(
                    module.smoothAccelerationSetting());
            accelerationPercentSetting = settings.register(
                    module.accelerationPercentSetting());
            groundOnlySetting = settings.register(module.groundOnlySetting());
            pauseWhileSneakingSetting = settings.register(
                    module.pauseWhileSneakingSetting());
            speedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189StrafeModule.SPEED_SETTING_ID,
                                    "Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189StrafeModule.MINIMUM_SPEED,
                                            Minecraft189StrafeModule.MAXIMUM_SPEED,
                                            0.05D)));
            smoothAccelerationPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189StrafeModule.SMOOTH_ACCELERATION_SETTING_ID,
                            "Smooth Acceleration", SettingValueKind.BOOLEAN, 10));
            accelerationPercentPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189StrafeModule.ACCELERATION_PERCENT_SETTING_ID,
                            "Acceleration %", SettingValueKind.INTEGER, 20,
                            new SettingNumericSpec(10.0D, 100.0D, 5.0D)));
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189StrafeModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 30));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189StrafeModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 40));
            speedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189StrafeModule.ID,
                                    Minecraft189StrafeModule.SPEED_SETTING_ID,
                                    0));

            smoothAccelerationBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189StrafeModule.ID,
                            Minecraft189StrafeModule.SMOOTH_ACCELERATION_SETTING_ID, 10));
            accelerationPercentBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189StrafeModule.ID,
                            Minecraft189StrafeModule.ACCELERATION_PERCENT_SETTING_ID, 20));

            groundOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189StrafeModule.ID,
                            Minecraft189StrafeModule.GROUND_ONLY_SETTING_ID, 30));
            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189StrafeModule.ID,
                            Minecraft189StrafeModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 40));

            return new Minecraft189StrafeFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    speedSetting,
                    smoothAccelerationSetting,
                    accelerationPercentSetting,
                    speedPresentation,
                    smoothAccelerationPresentation,
                    accelerationPercentPresentation,
                    speedBinding,
                    smoothAccelerationBinding,
                    accelerationPercentBinding,
                    groundOnlySetting,
                    pauseWhileSneakingSetting,
                    groundOnlyPresentation,
                    pauseWhileSneakingPresentation,
                    groundOnlyBinding,
                    pauseWhileSneakingBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(groundOnlySetting, failure);
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

    Minecraft189StrafeModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "strafe feature is closed");
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
                    Minecraft189StrafeModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189StrafeModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(groundOnlyBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(groundOnlySetting, failure);
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
                            "strafe feature close failed",
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
