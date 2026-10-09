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

final class Minecraft189FastFallFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FastFallModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration fallSpeedSetting;
    private final SettingRegistry.Registration progressiveSetting;
    private final SettingRegistry.Registration rampStepSetting;
    private final SettingPresentationRegistry.Registration fallSpeedPresentation;
    private final SettingPresentationRegistry.Registration progressivePresentation;
    private final SettingPresentationRegistry.Registration rampStepPresentation;
    private final ModuleSettingRegistry.Registration fallSpeedBinding;
    private final ModuleSettingRegistry.Registration progressiveBinding;
    private final ModuleSettingRegistry.Registration rampStepBinding;
    private final SettingRegistry.Registration activationDelaySetting;
    private final SettingPresentationRegistry.Registration activationDelayPresentation;
    private final ModuleSettingRegistry.Registration activationDelayBinding;
    private boolean closed;

    private Minecraft189FastFallFeature(
            final ModuleController controller,
            final Minecraft189FastFallModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration fallSpeedSetting,
            final SettingRegistry.Registration progressiveSetting,
            final SettingRegistry.Registration rampStepSetting,
            final SettingPresentationRegistry.Registration fallSpeedPresentation,
            final SettingPresentationRegistry.Registration progressivePresentation,
            final SettingPresentationRegistry.Registration rampStepPresentation,
            final ModuleSettingRegistry.Registration fallSpeedBinding,
            final ModuleSettingRegistry.Registration progressiveBinding,
            final ModuleSettingRegistry.Registration rampStepBinding,
            final SettingRegistry.Registration activationDelaySetting,
            final SettingPresentationRegistry.Registration activationDelayPresentation,
            final ModuleSettingRegistry.Registration activationDelayBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.fallSpeedSetting = fallSpeedSetting;
        this.progressiveSetting = progressiveSetting;
        this.rampStepSetting = rampStepSetting;
        this.fallSpeedPresentation = fallSpeedPresentation;
        this.progressivePresentation = progressivePresentation;
        this.rampStepPresentation = rampStepPresentation;
        this.fallSpeedBinding = fallSpeedBinding;
        this.progressiveBinding = progressiveBinding;
        this.rampStepBinding = rampStepBinding;
        this.activationDelaySetting = activationDelaySetting;
        this.activationDelayPresentation = activationDelayPresentation;
        this.activationDelayBinding = activationDelayBinding;
    }

    static Minecraft189FastFallFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189FastFallModule module =
                new Minecraft189FastFallModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration fallSpeedSetting = null;
        SettingRegistry.Registration progressiveSetting = null;
        SettingRegistry.Registration rampStepSetting = null;
        SettingPresentationRegistry.Registration fallSpeedPresentation = null;
        SettingPresentationRegistry.Registration progressivePresentation = null;
        SettingPresentationRegistry.Registration rampStepPresentation = null;
        ModuleSettingRegistry.Registration fallSpeedBinding = null;
        ModuleSettingRegistry.Registration progressiveBinding = null;
        ModuleSettingRegistry.Registration rampStepBinding = null;
        SettingRegistry.Registration activationDelaySetting = null;
        SettingPresentationRegistry.Registration activationDelayPresentation = null;
        ModuleSettingRegistry.Registration activationDelayBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189FastFallModule.ID,
                                    "Fast Fall",
                                    "Accelerates gentle airborne descent to a configurable minimum downward speed.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    110));

            fallSpeedSetting =
                    settings.register(
                            module.fallSpeedSetting());
            progressiveSetting = settings.register(module.progressiveSetting());
            rampStepSetting = settings.register(module.rampStepSetting());
            activationDelaySetting = settings.register(
                    module.activationDelayTicksSetting());
            fallSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189FastFallModule.FALL_SPEED_SETTING_ID,
                                    "Fall Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189FastFallModule.MINIMUM_FALL_SPEED,
                                            Minecraft189FastFallModule.MAXIMUM_FALL_SPEED,
                                            0.05D)));
            progressivePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastFallModule.PROGRESSIVE_SETTING_ID,
                            "Progressive Fall", SettingValueKind.BOOLEAN, 10));
            rampStepPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastFallModule.RAMP_STEP_SETTING_ID,
                            "Fall Ramp Step", SettingValueKind.DOUBLE, 20,
                            new SettingNumericSpec(
                                    Minecraft189FastFallModule.MINIMUM_RAMP_STEP,
                                    Minecraft189FastFallModule.MAXIMUM_RAMP_STEP,
                                    0.01D)));
            activationDelayPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastFallModule.ACTIVATION_DELAY_SETTING_ID,
                            "Delay Ticks", SettingValueKind.INTEGER, 30,
                            new SettingNumericSpec(0.0D,
                                    Minecraft189FastFallModule.MAXIMUM_ACTIVATION_DELAY_TICKS,
                                    1.0D)));
            fallSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189FastFallModule.ID,
                                    Minecraft189FastFallModule.FALL_SPEED_SETTING_ID,
                                    0));

            progressiveBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastFallModule.ID,
                            Minecraft189FastFallModule.PROGRESSIVE_SETTING_ID, 10));
            rampStepBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastFallModule.ID,
                            Minecraft189FastFallModule.RAMP_STEP_SETTING_ID, 20));

            activationDelayBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastFallModule.ID,
                            Minecraft189FastFallModule.ACTIVATION_DELAY_SETTING_ID, 30));

            return new Minecraft189FastFallFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    fallSpeedSetting,
                    progressiveSetting,
                    rampStepSetting,
                    fallSpeedPresentation,
                    progressivePresentation,
                    rampStepPresentation,
                    fallSpeedBinding,
                    progressiveBinding,
                    rampStepBinding,
                    activationDelaySetting,
                    activationDelayPresentation,
                    activationDelayBinding);
        } catch (RuntimeException failure) {
            closeQuietly(activationDelayBinding, failure);
            closeQuietly(activationDelayPresentation, failure);
            closeQuietly(activationDelaySetting, failure);
            closeQuietly(rampStepBinding, failure);
            closeQuietly(progressiveBinding, failure);
            closeQuietly(rampStepPresentation, failure);
            closeQuietly(progressivePresentation, failure);
            closeQuietly(rampStepSetting, failure);
            closeQuietly(progressiveSetting, failure);
            closeQuietly(fallSpeedBinding, failure);
            closeQuietly(fallSpeedPresentation, failure);
            closeQuietly(fallSpeedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189FastFallModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "fast-fall feature is closed");
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
                    Minecraft189FastFallModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189FastFallModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(activationDelayBinding, failure);
        failure = close(activationDelayPresentation, failure);
        failure = close(activationDelaySetting, failure);
        failure = close(rampStepBinding, failure);
        failure = close(progressiveBinding, failure);
        failure = close(rampStepPresentation, failure);
        failure = close(progressivePresentation, failure);
        failure = close(rampStepSetting, failure);
        failure = close(progressiveSetting, failure);
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
                            "fast-fall feature close failed",
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
