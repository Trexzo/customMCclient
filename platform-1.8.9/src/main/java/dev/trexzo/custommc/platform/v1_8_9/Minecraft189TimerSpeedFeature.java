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

final class Minecraft189TimerSpeedFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189TimerSpeedModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration speedSetting;
    private final SettingRegistry.Registration airborneOverrideSetting;
    private final SettingRegistry.Registration airborneSpeedSetting;
    private final SettingPresentationRegistry.Registration speedPresentation;
    private final SettingPresentationRegistry.Registration airborneOverridePresentation;
    private final SettingPresentationRegistry.Registration airborneSpeedPresentation;
    private final ModuleSettingRegistry.Registration speedBinding;
    private final ModuleSettingRegistry.Registration airborneOverrideBinding;
    private final ModuleSettingRegistry.Registration airborneSpeedBinding;
    private final SettingRegistry.Registration smoothTransitionSetting;
    private final SettingRegistry.Registration transitionStepSetting;
    private final SettingPresentationRegistry.Registration smoothTransitionPresentation;
    private final SettingPresentationRegistry.Registration transitionStepPresentation;
    private final ModuleSettingRegistry.Registration smoothTransitionBinding;
    private final ModuleSettingRegistry.Registration transitionStepBinding;
    private boolean closed;

    private Minecraft189TimerSpeedFeature(
            final ModuleController controller,
            final Minecraft189TimerSpeedModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration speedSetting,
            final SettingRegistry.Registration airborneOverrideSetting,
            final SettingRegistry.Registration airborneSpeedSetting,
            final SettingPresentationRegistry.Registration speedPresentation,
            final SettingPresentationRegistry.Registration airborneOverridePresentation,
            final SettingPresentationRegistry.Registration airborneSpeedPresentation,
            final ModuleSettingRegistry.Registration speedBinding,
            final ModuleSettingRegistry.Registration airborneOverrideBinding,
            final ModuleSettingRegistry.Registration airborneSpeedBinding,
            final SettingRegistry.Registration smoothTransitionSetting,
            final SettingRegistry.Registration transitionStepSetting,
            final SettingPresentationRegistry.Registration smoothTransitionPresentation,
            final SettingPresentationRegistry.Registration transitionStepPresentation,
            final ModuleSettingRegistry.Registration smoothTransitionBinding,
            final ModuleSettingRegistry.Registration transitionStepBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.speedSetting = speedSetting;
        this.airborneOverrideSetting = airborneOverrideSetting;
        this.airborneSpeedSetting = airborneSpeedSetting;
        this.speedPresentation = speedPresentation;
        this.airborneOverridePresentation = airborneOverridePresentation;
        this.airborneSpeedPresentation = airborneSpeedPresentation;
        this.speedBinding = speedBinding;
        this.airborneOverrideBinding = airborneOverrideBinding;
        this.airborneSpeedBinding = airborneSpeedBinding;
        this.smoothTransitionSetting = smoothTransitionSetting;
        this.transitionStepSetting = transitionStepSetting;
        this.smoothTransitionPresentation = smoothTransitionPresentation;
        this.transitionStepPresentation = transitionStepPresentation;
        this.smoothTransitionBinding = smoothTransitionBinding;
        this.transitionStepBinding = transitionStepBinding;
    }

    static Minecraft189TimerSpeedFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189TimerSpeedModule module =
                new Minecraft189TimerSpeedModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration speedSetting = null;
        SettingRegistry.Registration airborneOverrideSetting = null;
        SettingRegistry.Registration airborneSpeedSetting = null;
        SettingPresentationRegistry.Registration speedPresentation = null;
        SettingPresentationRegistry.Registration airborneOverridePresentation = null;
        SettingPresentationRegistry.Registration airborneSpeedPresentation = null;
        ModuleSettingRegistry.Registration speedBinding = null;
        ModuleSettingRegistry.Registration airborneOverrideBinding = null;
        ModuleSettingRegistry.Registration airborneSpeedBinding = null;
        SettingRegistry.Registration smoothTransitionSetting = null;
        SettingRegistry.Registration transitionStepSetting = null;
        SettingPresentationRegistry.Registration smoothTransitionPresentation = null;
        SettingPresentationRegistry.Registration transitionStepPresentation = null;
        ModuleSettingRegistry.Registration smoothTransitionBinding = null;
        ModuleSettingRegistry.Registration transitionStepBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189TimerSpeedModule.ID,
                                    "Timer",
                                    "Changes the client game-tick speed multiplier.",
                                    Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID,
                                    30));
            speedSetting =
                    settings.register(
                            module.speedPercentSetting());
            airborneOverrideSetting = settings.register(
                    module.airborneOverrideSetting());
            airborneSpeedSetting = settings.register(
                    module.airborneSpeedPercentSetting());
            smoothTransitionSetting = settings.register(module.smoothTransitionSetting());
            transitionStepSetting = settings.register(module.transitionStepPercentSetting());
            speedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189TimerSpeedModule.SPEED_SETTING_ID,
                                    "Speed %",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            10.0D,
                                            300.0D,
                                            5.0D)));
            airborneOverridePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TimerSpeedModule.AIRBORNE_OVERRIDE_SETTING_ID,
                            "Airborne Override", SettingValueKind.BOOLEAN, 10));
            airborneSpeedPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TimerSpeedModule.AIRBORNE_SPEED_SETTING_ID,
                            "Air Speed %", SettingValueKind.INTEGER, 20,
                            new SettingNumericSpec(
                                    Minecraft189TimerSpeedModule.MINIMUM_SPEED_PERCENT,
                                    Minecraft189TimerSpeedModule.MAXIMUM_SPEED_PERCENT,
                                    5.0D)));
            smoothTransitionPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TimerSpeedModule.SMOOTH_TRANSITION_SETTING_ID,
                            "Smooth Transition", SettingValueKind.BOOLEAN, 30));
            transitionStepPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TimerSpeedModule.TRANSITION_STEP_SETTING_ID,
                            "Transition Step %", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(
                                    Minecraft189TimerSpeedModule.MINIMUM_TRANSITION_STEP_PERCENT,
                                    Minecraft189TimerSpeedModule.MAXIMUM_TRANSITION_STEP_PERCENT,
                                    5.0D)));
            speedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189TimerSpeedModule.ID,
                                    Minecraft189TimerSpeedModule.SPEED_SETTING_ID,
                                    0));

            airborneOverrideBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TimerSpeedModule.ID,
                            Minecraft189TimerSpeedModule.AIRBORNE_OVERRIDE_SETTING_ID, 10));
            airborneSpeedBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TimerSpeedModule.ID,
                            Minecraft189TimerSpeedModule.AIRBORNE_SPEED_SETTING_ID, 20));

            smoothTransitionBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TimerSpeedModule.ID,
                            Minecraft189TimerSpeedModule.SMOOTH_TRANSITION_SETTING_ID, 30));
            transitionStepBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TimerSpeedModule.ID,
                            Minecraft189TimerSpeedModule.TRANSITION_STEP_SETTING_ID, 40));

            return new Minecraft189TimerSpeedFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    speedSetting,
                    airborneOverrideSetting,
                    airborneSpeedSetting,
                    speedPresentation,
                    airborneOverridePresentation,
                    airborneSpeedPresentation,
                    speedBinding,
                    airborneOverrideBinding,
                    airborneSpeedBinding,
                    smoothTransitionSetting,
                    transitionStepSetting,
                    smoothTransitionPresentation,
                    transitionStepPresentation,
                    smoothTransitionBinding,
                    transitionStepBinding);
        } catch (RuntimeException failure) {
            closeQuietly(transitionStepBinding, failure);
            closeQuietly(smoothTransitionBinding, failure);
            closeQuietly(transitionStepPresentation, failure);
            closeQuietly(smoothTransitionPresentation, failure);
            closeQuietly(transitionStepSetting, failure);
            closeQuietly(smoothTransitionSetting, failure);
            closeQuietly(airborneSpeedBinding, failure);
            closeQuietly(airborneOverrideBinding, failure);
            closeQuietly(airborneSpeedPresentation, failure);
            closeQuietly(airborneOverridePresentation, failure);
            closeQuietly(airborneSpeedSetting, failure);
            closeQuietly(airborneOverrideSetting, failure);
            closeQuietly(speedBinding, failure);
            closeQuietly(speedPresentation, failure);
            closeQuietly(speedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189TimerSpeedModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "timer-speed feature is closed");
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
                    Minecraft189TimerSpeedModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189TimerSpeedModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(transitionStepBinding, failure);
        failure = close(smoothTransitionBinding, failure);
        failure = close(transitionStepPresentation, failure);
        failure = close(smoothTransitionPresentation, failure);
        failure = close(transitionStepSetting, failure);
        failure = close(smoothTransitionSetting, failure);
        failure = close(airborneSpeedBinding, failure);
        failure = close(airborneOverrideBinding, failure);
        failure = close(airborneSpeedPresentation, failure);
        failure = close(airborneOverridePresentation, failure);
        failure = close(airborneSpeedSetting, failure);
        failure = close(airborneOverrideSetting, failure);
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
        try {
            closeable.close();
            return primary;
        } catch (RuntimeException failure) {
            return append(primary, failure);
        } catch (Exception failure) {
            return append(
                    primary,
                    new IllegalStateException(
                            "timer-speed feature close failed",
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
