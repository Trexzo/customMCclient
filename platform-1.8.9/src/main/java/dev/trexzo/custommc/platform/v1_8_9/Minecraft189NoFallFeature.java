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

final class Minecraft189NoFallFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189NoFallModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration thresholdSetting;
    private final SettingPresentationRegistry.Registration thresholdPresentation;
    private final ModuleSettingRegistry.Registration thresholdBinding;
    private boolean closed;

    private Minecraft189NoFallFeature(
            final ModuleController controller,
            final Minecraft189NoFallModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration thresholdSetting,
            final SettingPresentationRegistry.Registration thresholdPresentation,
            final ModuleSettingRegistry.Registration thresholdBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.thresholdSetting = thresholdSetting;
        this.thresholdPresentation = thresholdPresentation;
        this.thresholdBinding = thresholdBinding;
    }

    static Minecraft189NoFallFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189NoFallModule module =
                new Minecraft189NoFallModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration thresholdSetting = null;
        SettingPresentationRegistry.Registration thresholdPresentation = null;
        ModuleSettingRegistry.Registration thresholdBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189NoFallModule.ID,
                                    "No Fall",
                                    "Clears local fall distance after a configurable threshold.",
                                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID,
                                    50));
            thresholdSetting =
                    settings.register(
                            module.thresholdSetting());
            thresholdPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189NoFallModule.THRESHOLD_SETTING_ID,
                                    "Threshold",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189NoFallModule.MINIMUM_THRESHOLD,
                                            Minecraft189NoFallModule.MAXIMUM_THRESHOLD,
                                            0.5D)));
            thresholdBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189NoFallModule.ID,
                                    Minecraft189NoFallModule.THRESHOLD_SETTING_ID,
                                    0));

            return new Minecraft189NoFallFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    thresholdSetting,
                    thresholdPresentation,
                    thresholdBinding);
        } catch (RuntimeException failure) {
            closeQuietly(thresholdBinding, failure);
            closeQuietly(thresholdPresentation, failure);
            closeQuietly(thresholdSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189NoFallModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "no-fall feature is closed");
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
                    Minecraft189NoFallModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189NoFallModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(thresholdBinding, failure);
        failure = close(thresholdPresentation, failure);
        failure = close(thresholdSetting, failure);
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
                            "no-fall feature close failed",
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
