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

final class Minecraft189VelocityFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189VelocityModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration horizontalSetting;
    private final SettingRegistry.Registration verticalSetting;
    private final SettingPresentationRegistry.Registration horizontalPresentation;
    private final SettingPresentationRegistry.Registration verticalPresentation;
    private final ModuleSettingRegistry.Registration horizontalBinding;
    private final ModuleSettingRegistry.Registration verticalBinding;
    private boolean closed;

    private Minecraft189VelocityFeature(
            final ModuleController controller,
            final Minecraft189VelocityModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration horizontalSetting,
            final SettingRegistry.Registration verticalSetting,
            final SettingPresentationRegistry.Registration horizontalPresentation,
            final SettingPresentationRegistry.Registration verticalPresentation,
            final ModuleSettingRegistry.Registration horizontalBinding,
            final ModuleSettingRegistry.Registration verticalBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.horizontalSetting = horizontalSetting;
        this.verticalSetting = verticalSetting;
        this.horizontalPresentation = horizontalPresentation;
        this.verticalPresentation = verticalPresentation;
        this.horizontalBinding = horizontalBinding;
        this.verticalBinding = verticalBinding;
    }

    static Minecraft189VelocityFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189VelocityModule module =
                new Minecraft189VelocityModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration horizontalSetting = null;
        SettingRegistry.Registration verticalSetting = null;
        SettingPresentationRegistry.Registration horizontalPresentation = null;
        SettingPresentationRegistry.Registration verticalPresentation = null;
        ModuleSettingRegistry.Registration horizontalBinding = null;
        ModuleSettingRegistry.Registration verticalBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189VelocityModule.ID,
                                    "Velocity",
                                    "Scales incoming horizontal and vertical knockback from 0% cancellation through 200% amplification.",
                                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID,
                                    20));

            horizontalSetting =
                    settings.register(
                            module.horizontalPercentSetting());
            verticalSetting =
                    settings.register(
                            module.verticalPercentSetting());

            horizontalPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189VelocityModule.HORIZONTAL_SETTING_ID,
                                    "Horizontal %",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189VelocityModule.MINIMUM_PERCENT,
                                            Minecraft189VelocityModule.MAXIMUM_PERCENT,
                                            5.0D)));
            verticalPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189VelocityModule.VERTICAL_SETTING_ID,
                                    "Vertical %",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189VelocityModule.MINIMUM_PERCENT,
                                            Minecraft189VelocityModule.MAXIMUM_PERCENT,
                                            5.0D)));

            horizontalBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189VelocityModule.ID,
                                    Minecraft189VelocityModule.HORIZONTAL_SETTING_ID,
                                    0));
            verticalBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189VelocityModule.ID,
                                    Minecraft189VelocityModule.VERTICAL_SETTING_ID,
                                    10));

            return new Minecraft189VelocityFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    horizontalSetting,
                    verticalSetting,
                    horizontalPresentation,
                    verticalPresentation,
                    horizontalBinding,
                    verticalBinding);
        } catch (RuntimeException failure) {
            closeQuietly(verticalBinding, failure);
            closeQuietly(horizontalBinding, failure);
            closeQuietly(verticalPresentation, failure);
            closeQuietly(horizontalPresentation, failure);
            closeQuietly(verticalSetting, failure);
            closeQuietly(horizontalSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189VelocityModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "velocity feature is closed");
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
                    Minecraft189VelocityModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189VelocityModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(verticalBinding, failure);
        failure = close(horizontalBinding, failure);
        failure = close(verticalPresentation, failure);
        failure = close(horizontalPresentation, failure);
        failure = close(verticalSetting, failure);
        failure = close(horizontalSetting, failure);
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
                            "velocity feature close failed",
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
