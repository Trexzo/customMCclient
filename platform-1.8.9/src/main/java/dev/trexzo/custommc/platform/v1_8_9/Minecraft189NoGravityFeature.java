package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;

final class Minecraft189NoGravityFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189NoGravityModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration liftSpeedSetting;
    private final SettingPresentationRegistry.Registration liftSpeedPresentation;
    private final ModuleSettingRegistry.Registration liftSpeedBinding;
    private final SettingRegistry.Registration smoothLiftSetting;
    private final SettingRegistry.Registration liftStepSetting;
    private final SettingPresentationRegistry.Registration smoothLiftPresentation;
    private final SettingPresentationRegistry.Registration liftStepPresentation;
    private final ModuleSettingRegistry.Registration smoothLiftBinding;
    private final ModuleSettingRegistry.Registration liftStepBinding;
    private boolean closed;

    private Minecraft189NoGravityFeature(
            final ModuleController controller,
            final Minecraft189NoGravityModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration liftSpeedSetting,
            final SettingPresentationRegistry.Registration liftSpeedPresentation,
            final ModuleSettingRegistry.Registration liftSpeedBinding,
            final SettingRegistry.Registration smoothLiftSetting,
            final SettingRegistry.Registration liftStepSetting,
            final SettingPresentationRegistry.Registration smoothLiftPresentation,
            final SettingPresentationRegistry.Registration liftStepPresentation,
            final ModuleSettingRegistry.Registration smoothLiftBinding,
            final ModuleSettingRegistry.Registration liftStepBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.liftSpeedSetting = liftSpeedSetting;
        this.liftSpeedPresentation = liftSpeedPresentation;
        this.liftSpeedBinding = liftSpeedBinding;
        this.smoothLiftSetting = smoothLiftSetting;
        this.liftStepSetting = liftStepSetting;
        this.smoothLiftPresentation = smoothLiftPresentation;
        this.liftStepPresentation = liftStepPresentation;
        this.smoothLiftBinding = smoothLiftBinding;
        this.liftStepBinding = liftStepBinding;
    }

    static Minecraft189NoGravityFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189NoGravityModule module =
                new Minecraft189NoGravityModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration liftSpeedSetting = null;
        SettingPresentationRegistry.Registration liftSpeedPresentation = null;
        ModuleSettingRegistry.Registration liftSpeedBinding = null;
        SettingRegistry.Registration smoothLiftSetting = null;
        SettingRegistry.Registration liftStepSetting = null;
        SettingPresentationRegistry.Registration smoothLiftPresentation = null;
        SettingPresentationRegistry.Registration liftStepPresentation = null;
        ModuleSettingRegistry.Registration smoothLiftBinding = null;
        ModuleSettingRegistry.Registration liftStepBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189NoGravityModule.ID,
                                    "No Gravity",
                                    "Cancels downward airborne motion while preserving upward movement.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    180));
            liftSpeedSetting = settings.register(module.liftSpeedSetting());
            smoothLiftSetting = settings.register(module.smoothLiftSetting());
            liftStepSetting = settings.register(module.liftStepSetting());
            liftSpeedPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoGravityModule.LIFT_SPEED_SETTING_ID,
                            "Lift Speed", SettingValueKind.DOUBLE, 10,
                            new SettingNumericSpec(
                                    0.0D,
                                    Minecraft189NoGravityModule.MAXIMUM_LIFT_SPEED,
                                    0.01D)));
            smoothLiftPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoGravityModule.SMOOTH_LIFT_SETTING_ID,
                            "Smooth Lift", SettingValueKind.BOOLEAN, 20));
            liftStepPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoGravityModule.LIFT_STEP_SETTING_ID,
                            "Lift Step", SettingValueKind.DOUBLE, 30,
                            new SettingNumericSpec(
                                    Minecraft189NoGravityModule.MINIMUM_LIFT_STEP,
                                    Minecraft189NoGravityModule.MAXIMUM_LIFT_STEP,
                                    0.01D)));
            liftSpeedBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoGravityModule.ID,
                            Minecraft189NoGravityModule.LIFT_SPEED_SETTING_ID, 10));
            smoothLiftBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoGravityModule.ID,
                            Minecraft189NoGravityModule.SMOOTH_LIFT_SETTING_ID, 20));
            liftStepBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoGravityModule.ID,
                            Minecraft189NoGravityModule.LIFT_STEP_SETTING_ID, 30));
            return new Minecraft189NoGravityFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    liftSpeedSetting,
                    liftSpeedPresentation,
                    liftSpeedBinding,
                    smoothLiftSetting,
                    liftStepSetting,
                    smoothLiftPresentation,
                    liftStepPresentation,
                    smoothLiftBinding,
                    liftStepBinding);
        } catch (RuntimeException failure) {
            closeQuietly(liftStepBinding, failure);
            closeQuietly(smoothLiftBinding, failure);
            closeQuietly(liftStepPresentation, failure);
            closeQuietly(smoothLiftPresentation, failure);
            closeQuietly(liftStepSetting, failure);
            closeQuietly(smoothLiftSetting, failure);
            closeQuietly(liftSpeedBinding, failure);
            closeQuietly(liftSpeedPresentation, failure);
            closeQuietly(liftSpeedSetting, failure);
            if (presentation != null) {
                presentation.close();
            }
            if (moduleRegistration != null) {
                moduleRegistration.close();
            }
            throw failure;
        }
    }

    Minecraft189NoGravityModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "no-gravity feature is closed");
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
                    Minecraft189NoGravityModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189NoGravityModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(liftStepBinding, failure);
        failure = close(smoothLiftBinding, failure);
        failure = close(liftStepPresentation, failure);
        failure = close(smoothLiftPresentation, failure);
        failure = close(liftStepSetting, failure);
        failure = close(smoothLiftSetting, failure);
        failure = close(liftSpeedBinding, failure);
        failure = close(liftSpeedPresentation, failure);
        failure = close(liftSpeedSetting, failure);

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
            final AutoCloseable registration,
            final RuntimeException primary) {
        if (registration == null) {
            return primary;
        }
        try {
            registration.close();
            return primary;
        } catch (RuntimeException failure) {
            return append(primary, failure);
        } catch (Exception failure) {
            return append(primary, new IllegalStateException(
                    "no-gravity setting close failed", failure));
        }
    }

    private static void closeQuietly(
            final AutoCloseable registration,
            final RuntimeException primary) {
        if (registration == null) {
            return;
        }
        try {
            registration.close();
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
