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
    private final SettingPresentationRegistry.Registration horizontalSpeedPresentation;
    private final SettingPresentationRegistry.Registration verticalSpeedPresentation;
    private final ModuleSettingRegistry.Registration horizontalSpeedBinding;
    private final ModuleSettingRegistry.Registration verticalSpeedBinding;
    private boolean closed;

    private Minecraft189FlightFeature(
            final ModuleController controller,
            final Minecraft189FlightModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration horizontalSpeedSetting,
            final SettingRegistry.Registration verticalSpeedSetting,
            final SettingPresentationRegistry.Registration horizontalSpeedPresentation,
            final SettingPresentationRegistry.Registration verticalSpeedPresentation,
            final ModuleSettingRegistry.Registration horizontalSpeedBinding,
            final ModuleSettingRegistry.Registration verticalSpeedBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.horizontalSpeedSetting = horizontalSpeedSetting;
        this.verticalSpeedSetting = verticalSpeedSetting;
        this.horizontalSpeedPresentation = horizontalSpeedPresentation;
        this.verticalSpeedPresentation = verticalSpeedPresentation;
        this.horizontalSpeedBinding = horizontalSpeedBinding;
        this.verticalSpeedBinding = verticalSpeedBinding;
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
        SettingPresentationRegistry.Registration horizontalSpeedPresentation = null;
        SettingPresentationRegistry.Registration verticalSpeedPresentation = null;
        ModuleSettingRegistry.Registration horizontalSpeedBinding = null;
        ModuleSettingRegistry.Registration verticalSpeedBinding = null;
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

            return new Minecraft189FlightFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    horizontalSpeedSetting,
                    verticalSpeedSetting,
                    horizontalSpeedPresentation,
                    verticalSpeedPresentation,
                    horizontalSpeedBinding,
                    verticalSpeedBinding);
        } catch (RuntimeException failure) {
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
