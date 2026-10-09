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

final class Minecraft189FlightFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FlightModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration horizontalSpeedSetting;
    private final SettingRegistry.Registration verticalSpeedSetting;
    private final SettingRegistry.Registration sprintBoostSetting;
    private final SettingRegistry.Registration sprintMultiplierSetting;
    private final SettingPresentationRegistry.Registration horizontalSpeedPresentation;
    private final SettingPresentationRegistry.Registration verticalSpeedPresentation;
    private final SettingPresentationRegistry.Registration sprintBoostPresentation;
    private final SettingPresentationRegistry.Registration sprintMultiplierPresentation;
    private final ModuleSettingRegistry.Registration horizontalSpeedBinding;
    private final ModuleSettingRegistry.Registration verticalSpeedBinding;
    private final ModuleSettingRegistry.Registration sprintBoostBinding;
    private final ModuleSettingRegistry.Registration sprintMultiplierBinding;
    private final SettingRegistry.Registration smoothVerticalSetting;
    private final SettingRegistry.Registration verticalStepSetting;
    private final SettingPresentationRegistry.Registration smoothVerticalPresentation;
    private final SettingPresentationRegistry.Registration verticalStepPresentation;
    private final ModuleSettingRegistry.Registration smoothVerticalBinding;
    private final ModuleSettingRegistry.Registration verticalStepBinding;
    private boolean closed;

    private Minecraft189FlightFeature(
            final ModuleController controller,
            final Minecraft189FlightModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration horizontalSpeedSetting,
            final SettingRegistry.Registration verticalSpeedSetting,
            final SettingRegistry.Registration sprintBoostSetting,
            final SettingRegistry.Registration sprintMultiplierSetting,
            final SettingPresentationRegistry.Registration horizontalSpeedPresentation,
            final SettingPresentationRegistry.Registration verticalSpeedPresentation,
            final SettingPresentationRegistry.Registration sprintBoostPresentation,
            final SettingPresentationRegistry.Registration sprintMultiplierPresentation,
            final ModuleSettingRegistry.Registration horizontalSpeedBinding,
            final ModuleSettingRegistry.Registration verticalSpeedBinding,
            final ModuleSettingRegistry.Registration sprintBoostBinding,
            final ModuleSettingRegistry.Registration sprintMultiplierBinding,
            final SettingRegistry.Registration smoothVerticalSetting,
            final SettingRegistry.Registration verticalStepSetting,
            final SettingPresentationRegistry.Registration smoothVerticalPresentation,
            final SettingPresentationRegistry.Registration verticalStepPresentation,
            final ModuleSettingRegistry.Registration smoothVerticalBinding,
            final ModuleSettingRegistry.Registration verticalStepBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.horizontalSpeedSetting = horizontalSpeedSetting;
        this.verticalSpeedSetting = verticalSpeedSetting;
        this.sprintBoostSetting = sprintBoostSetting;
        this.sprintMultiplierSetting = sprintMultiplierSetting;
        this.horizontalSpeedPresentation = horizontalSpeedPresentation;
        this.verticalSpeedPresentation = verticalSpeedPresentation;
        this.sprintBoostPresentation = sprintBoostPresentation;
        this.sprintMultiplierPresentation = sprintMultiplierPresentation;
        this.horizontalSpeedBinding = horizontalSpeedBinding;
        this.verticalSpeedBinding = verticalSpeedBinding;
        this.sprintBoostBinding = sprintBoostBinding;
        this.sprintMultiplierBinding = sprintMultiplierBinding;
        this.smoothVerticalSetting = smoothVerticalSetting;
        this.verticalStepSetting = verticalStepSetting;
        this.smoothVerticalPresentation = smoothVerticalPresentation;
        this.verticalStepPresentation = verticalStepPresentation;
        this.smoothVerticalBinding = smoothVerticalBinding;
        this.verticalStepBinding = verticalStepBinding;
    }

    static Minecraft189FlightFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState) {
        final Minecraft189FlightModule module =
                new Minecraft189FlightModule(
                        inputState);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration horizontalSpeedSetting = null;
        SettingRegistry.Registration verticalSpeedSetting = null;
        SettingRegistry.Registration sprintBoostSetting = null;
        SettingRegistry.Registration sprintMultiplierSetting = null;
        SettingPresentationRegistry.Registration horizontalSpeedPresentation = null;
        SettingPresentationRegistry.Registration verticalSpeedPresentation = null;
        SettingPresentationRegistry.Registration sprintBoostPresentation = null;
        SettingPresentationRegistry.Registration sprintMultiplierPresentation = null;
        ModuleSettingRegistry.Registration horizontalSpeedBinding = null;
        ModuleSettingRegistry.Registration verticalSpeedBinding = null;
        ModuleSettingRegistry.Registration sprintBoostBinding = null;
        ModuleSettingRegistry.Registration sprintMultiplierBinding = null;
        SettingRegistry.Registration smoothVerticalSetting = null;
        SettingRegistry.Registration verticalStepSetting = null;
        SettingPresentationRegistry.Registration smoothVerticalPresentation = null;
        SettingPresentationRegistry.Registration verticalStepPresentation = null;
        ModuleSettingRegistry.Registration smoothVerticalBinding = null;
        ModuleSettingRegistry.Registration verticalStepBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189FlightModule.ID,
                                    "Flight",
                                    "Controls mapped yaw-relative horizontal and vertical motion.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    80));

            horizontalSpeedSetting =
                    settings.register(
                            module.horizontalSpeedSetting());
            verticalSpeedSetting =
                    settings.register(
                            module.verticalSpeedSetting());
            sprintBoostSetting = settings.register(module.sprintBoostSetting());
            sprintMultiplierSetting = settings.register(module.sprintMultiplierSetting());
            smoothVerticalSetting = settings.register(module.smoothVerticalSetting());
            verticalStepSetting = settings.register(module.verticalStepSetting());

            horizontalSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189FlightModule.HORIZONTAL_SPEED_SETTING_ID,
                                    "Horizontal Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189FlightModule.MINIMUM_SPEED,
                                            Minecraft189FlightModule.MAXIMUM_SPEED,
                                            0.05D)));
            verticalSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189FlightModule.VERTICAL_SPEED_SETTING_ID,
                                    "Vertical Speed",
                                    SettingValueKind.DOUBLE,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189FlightModule.MINIMUM_SPEED,
                                            Minecraft189FlightModule.MAXIMUM_SPEED,
                                            0.05D)));

            sprintBoostPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FlightModule.SPRINT_BOOST_SETTING_ID,
                            "Sprint Boost", SettingValueKind.BOOLEAN, 20));
            sprintMultiplierPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FlightModule.SPRINT_MULTIPLIER_SETTING_ID,
                            "Sprint Multiplier", SettingValueKind.DOUBLE, 30,
                            new SettingNumericSpec(
                                    Minecraft189FlightModule.MINIMUM_SPRINT_MULTIPLIER,
                                    Minecraft189FlightModule.MAXIMUM_SPRINT_MULTIPLIER,
                                    0.10D)));
            smoothVerticalPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FlightModule.SMOOTH_VERTICAL_SETTING_ID,
                            "Smooth Vertical", SettingValueKind.BOOLEAN, 40));
            verticalStepPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FlightModule.VERTICAL_STEP_SETTING_ID,
                            "Vertical Step", SettingValueKind.DOUBLE, 50,
                            new SettingNumericSpec(
                                    Minecraft189FlightModule.MINIMUM_VERTICAL_STEP,
                                    Minecraft189FlightModule.MAXIMUM_VERTICAL_STEP, 0.01D)));
            horizontalSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189FlightModule.ID,
                                    Minecraft189FlightModule.HORIZONTAL_SPEED_SETTING_ID,
                                    0));
            verticalSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189FlightModule.ID,
                                    Minecraft189FlightModule.VERTICAL_SPEED_SETTING_ID,
                                    10));

            sprintBoostBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FlightModule.ID,
                            Minecraft189FlightModule.SPRINT_BOOST_SETTING_ID, 20));
            sprintMultiplierBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FlightModule.ID,
                            Minecraft189FlightModule.SPRINT_MULTIPLIER_SETTING_ID, 30));

            smoothVerticalBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FlightModule.ID,
                            Minecraft189FlightModule.SMOOTH_VERTICAL_SETTING_ID, 40));
            verticalStepBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FlightModule.ID,
                            Minecraft189FlightModule.VERTICAL_STEP_SETTING_ID, 50));

            return new Minecraft189FlightFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    horizontalSpeedSetting,
                    verticalSpeedSetting,
                    sprintBoostSetting,
                    sprintMultiplierSetting,
                    horizontalSpeedPresentation,
                    verticalSpeedPresentation,
                    sprintBoostPresentation,
                    sprintMultiplierPresentation,
                    horizontalSpeedBinding,
                    verticalSpeedBinding,
                    sprintBoostBinding,
                    sprintMultiplierBinding,
                    smoothVerticalSetting,
                    verticalStepSetting,
                    smoothVerticalPresentation,
                    verticalStepPresentation,
                    smoothVerticalBinding,
                    verticalStepBinding);
        } catch (RuntimeException failure) {
            closeQuietly(verticalStepBinding, failure);
            closeQuietly(smoothVerticalBinding, failure);
            closeQuietly(verticalStepPresentation, failure);
            closeQuietly(smoothVerticalPresentation, failure);
            closeQuietly(verticalStepSetting, failure);
            closeQuietly(smoothVerticalSetting, failure);
            closeQuietly(sprintMultiplierBinding, failure);
            closeQuietly(sprintBoostBinding, failure);
            closeQuietly(sprintMultiplierPresentation, failure);
            closeQuietly(sprintBoostPresentation, failure);
            closeQuietly(sprintMultiplierSetting, failure);
            closeQuietly(sprintBoostSetting, failure);
            closeQuietly(verticalSpeedBinding, failure);
            closeQuietly(horizontalSpeedBinding, failure);
            closeQuietly(verticalSpeedPresentation, failure);
            closeQuietly(horizontalSpeedPresentation, failure);
            closeQuietly(verticalSpeedSetting, failure);
            closeQuietly(horizontalSpeedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189FlightModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "flight feature is closed");
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
                    Minecraft189FlightModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189FlightModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(verticalStepBinding, failure);
        failure = close(smoothVerticalBinding, failure);
        failure = close(verticalStepPresentation, failure);
        failure = close(smoothVerticalPresentation, failure);
        failure = close(verticalStepSetting, failure);
        failure = close(smoothVerticalSetting, failure);
        failure = close(sprintMultiplierBinding, failure);
        failure = close(sprintBoostBinding, failure);
        failure = close(sprintMultiplierPresentation, failure);
        failure = close(sprintBoostPresentation, failure);
        failure = close(sprintMultiplierSetting, failure);
        failure = close(sprintBoostSetting, failure);
        failure = close(verticalSpeedBinding, failure);
        failure = close(horizontalSpeedBinding, failure);
        failure = close(verticalSpeedPresentation, failure);
        failure = close(horizontalSpeedPresentation, failure);
        failure = close(verticalSpeedSetting, failure);
        failure = close(horizontalSpeedSetting, failure);
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
                            "flight feature close failed",
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
