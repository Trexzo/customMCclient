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

final class Minecraft189GlideFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189GlideModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration fallSpeedSetting;
    private final SettingRegistry.Registration requireSneakingSetting;
    private final SettingPresentationRegistry.Registration fallSpeedPresentation;
    private final SettingPresentationRegistry.Registration requireSneakingPresentation;
    private final ModuleSettingRegistry.Registration fallSpeedBinding;
    private final ModuleSettingRegistry.Registration requireSneakingBinding;
    private final SettingRegistry.Registration progressiveSetting;
    private final SettingRegistry.Registration decelerationStepSetting;
    private final SettingPresentationRegistry.Registration progressivePresentation;
    private final SettingPresentationRegistry.Registration decelerationStepPresentation;
    private final ModuleSettingRegistry.Registration progressiveBinding;
    private final ModuleSettingRegistry.Registration decelerationStepBinding;
    private final SettingRegistry.Registration activationDelaySetting;
    private final SettingPresentationRegistry.Registration activationDelayPresentation;
    private final ModuleSettingRegistry.Registration activationDelayBinding;
    private boolean closed;

    private Minecraft189GlideFeature(
            final ModuleController controller,
            final Minecraft189GlideModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration fallSpeedSetting,
            final SettingRegistry.Registration requireSneakingSetting,
            final SettingPresentationRegistry.Registration fallSpeedPresentation,
            final SettingPresentationRegistry.Registration requireSneakingPresentation,
            final ModuleSettingRegistry.Registration fallSpeedBinding,
            final ModuleSettingRegistry.Registration requireSneakingBinding,
            final SettingRegistry.Registration progressiveSetting,
            final SettingRegistry.Registration decelerationStepSetting,
            final SettingPresentationRegistry.Registration progressivePresentation,
            final SettingPresentationRegistry.Registration decelerationStepPresentation,
            final ModuleSettingRegistry.Registration progressiveBinding,
            final ModuleSettingRegistry.Registration decelerationStepBinding,
            final SettingRegistry.Registration activationDelaySetting,
            final SettingPresentationRegistry.Registration activationDelayPresentation,
            final ModuleSettingRegistry.Registration activationDelayBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.fallSpeedSetting = fallSpeedSetting;
        this.requireSneakingSetting = requireSneakingSetting;
        this.fallSpeedPresentation = fallSpeedPresentation;
        this.requireSneakingPresentation = requireSneakingPresentation;
        this.fallSpeedBinding = fallSpeedBinding;
        this.requireSneakingBinding = requireSneakingBinding;
        this.progressiveSetting = progressiveSetting;
        this.decelerationStepSetting = decelerationStepSetting;
        this.progressivePresentation = progressivePresentation;
        this.decelerationStepPresentation = decelerationStepPresentation;
        this.progressiveBinding = progressiveBinding;
        this.decelerationStepBinding = decelerationStepBinding;
        this.activationDelaySetting = activationDelaySetting;
        this.activationDelayPresentation = activationDelayPresentation;
        this.activationDelayBinding = activationDelayBinding;
    }

    static Minecraft189GlideFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189GlideModule module =
                new Minecraft189GlideModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration fallSpeedSetting = null;
        SettingRegistry.Registration requireSneakingSetting = null;
        SettingPresentationRegistry.Registration fallSpeedPresentation = null;
        SettingPresentationRegistry.Registration requireSneakingPresentation = null;
        ModuleSettingRegistry.Registration fallSpeedBinding = null;
        ModuleSettingRegistry.Registration requireSneakingBinding = null;
        SettingRegistry.Registration progressiveSetting = null;
        SettingRegistry.Registration decelerationStepSetting = null;
        SettingPresentationRegistry.Registration progressivePresentation = null;
        SettingPresentationRegistry.Registration decelerationStepPresentation = null;
        ModuleSettingRegistry.Registration progressiveBinding = null;
        ModuleSettingRegistry.Registration decelerationStepBinding = null;
        SettingRegistry.Registration activationDelaySetting = null;
        SettingPresentationRegistry.Registration activationDelayPresentation = null;
        ModuleSettingRegistry.Registration activationDelayBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189GlideModule.ID,
                                    "Glide",
                                    "Caps excessive downward motion while airborne.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    100));

            fallSpeedSetting =
                    settings.register(
                            module.fallSpeedSetting());
            requireSneakingSetting = settings.register(module.requireSneakingSetting());
            progressiveSetting = settings.register(
                    module.progressiveDecelerationSetting());
            decelerationStepSetting = settings.register(
                    module.decelerationStepSetting());
            activationDelaySetting = settings.register(
                    module.activationDelayTicksSetting());
            fallSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189GlideModule.FALL_SPEED_SETTING_ID,
                                    "Fall Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189GlideModule.MINIMUM_FALL_SPEED,
                                            Minecraft189GlideModule.MAXIMUM_FALL_SPEED,
                                            0.01D)));
            requireSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189GlideModule.REQUIRE_SNEAKING_SETTING_ID,
                            "Require Sneaking", SettingValueKind.BOOLEAN, 10));
            progressivePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189GlideModule.PROGRESSIVE_SETTING_ID,
                            "Progressive Deceleration", SettingValueKind.BOOLEAN, 20));
            decelerationStepPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189GlideModule.DECELERATION_STEP_SETTING_ID,
                            "Deceleration Step", SettingValueKind.DOUBLE, 30,
                            new SettingNumericSpec(
                                    Minecraft189GlideModule.MINIMUM_DECELERATION_STEP,
                                    Minecraft189GlideModule.MAXIMUM_DECELERATION_STEP,
                                    0.01D)));
            activationDelayPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189GlideModule.ACTIVATION_DELAY_SETTING_ID,
                            "Activation Delay Ticks", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(0.0D,
                                    Minecraft189GlideModule.MAXIMUM_ACTIVATION_DELAY_TICKS,
                                    1.0D)));
            fallSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189GlideModule.ID,
                                    Minecraft189GlideModule.FALL_SPEED_SETTING_ID,
                                    0));

            requireSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189GlideModule.ID,
                            Minecraft189GlideModule.REQUIRE_SNEAKING_SETTING_ID, 10));

            progressiveBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189GlideModule.ID,
                            Minecraft189GlideModule.PROGRESSIVE_SETTING_ID, 20));
            decelerationStepBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189GlideModule.ID,
                            Minecraft189GlideModule.DECELERATION_STEP_SETTING_ID, 30));

            activationDelayBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189GlideModule.ID,
                            Minecraft189GlideModule.ACTIVATION_DELAY_SETTING_ID, 40));

            return new Minecraft189GlideFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    fallSpeedSetting,
                    requireSneakingSetting,
                    fallSpeedPresentation,
                    requireSneakingPresentation,
                    fallSpeedBinding,
                    requireSneakingBinding,
                    progressiveSetting,
                    decelerationStepSetting,
                    progressivePresentation,
                    decelerationStepPresentation,
                    progressiveBinding,
                    decelerationStepBinding,
                    activationDelaySetting,
                    activationDelayPresentation,
                    activationDelayBinding);
        } catch (RuntimeException failure) {
            closeQuietly(activationDelayBinding, failure);
            closeQuietly(activationDelayPresentation, failure);
            closeQuietly(activationDelaySetting, failure);
            closeQuietly(decelerationStepBinding, failure);
            closeQuietly(progressiveBinding, failure);
            closeQuietly(decelerationStepPresentation, failure);
            closeQuietly(progressivePresentation, failure);
            closeQuietly(decelerationStepSetting, failure);
            closeQuietly(progressiveSetting, failure);
            closeQuietly(requireSneakingBinding, failure);
            closeQuietly(requireSneakingPresentation, failure);
            closeQuietly(requireSneakingSetting, failure);
            closeQuietly(fallSpeedBinding, failure);
            closeQuietly(fallSpeedPresentation, failure);
            closeQuietly(fallSpeedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189GlideModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "glide feature is closed");
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
                    Minecraft189GlideModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189GlideModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(activationDelayBinding, failure);
        failure = close(activationDelayPresentation, failure);
        failure = close(activationDelaySetting, failure);
        failure = close(decelerationStepBinding, failure);
        failure = close(progressiveBinding, failure);
        failure = close(decelerationStepPresentation, failure);
        failure = close(progressivePresentation, failure);
        failure = close(decelerationStepSetting, failure);
        failure = close(progressiveSetting, failure);
        failure = close(requireSneakingBinding, failure);
        failure = close(requireSneakingPresentation, failure);
        failure = close(requireSneakingSetting, failure);
        failure = close(fallSpeedBinding, failure);
        failure = close(fallSpeedPresentation, failure);
        failure = close(fallSpeedSetting, failure);
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
                            "glide feature close failed",
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
