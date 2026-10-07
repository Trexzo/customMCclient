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

final class Minecraft189SpeedMineFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189SpeedMineModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration progressSetting;
    private final SettingPresentationRegistry.Registration progressPresentation;
    private final ModuleSettingRegistry.Registration progressBinding;
    private boolean closed;

    private Minecraft189SpeedMineFeature(
            final ModuleController controller,
            final Minecraft189SpeedMineModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration progressSetting,
            final SettingPresentationRegistry.Registration progressPresentation,
            final ModuleSettingRegistry.Registration progressBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.progressSetting = progressSetting;
        this.progressPresentation = progressPresentation;
        this.progressBinding = progressBinding;
    }

    static Minecraft189SpeedMineFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189SpeedMineModule module =
                new Minecraft189SpeedMineModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration progressSetting = null;
        SettingPresentationRegistry.Registration progressPresentation = null;
        ModuleSettingRegistry.Registration progressBinding = null;

        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189SpeedMineModule.ID,
                                    "Speed Mine",
                                    "Raises active block-breaking progress to a configured minimum.",
                                    Minecraft189FeatureCatalog
                                            .PLAYER_CATEGORY_ID,
                                    20));
            progressSetting =
                    settings.register(
                            module.progressPercentSetting());
            progressPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189SpeedMineModule.PROGRESS_SETTING_ID,
                                    "Minimum Progress",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            0.0D,
                                            100.0D,
                                            1.0D)));
            progressBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189SpeedMineModule.ID,
                                    Minecraft189SpeedMineModule.PROGRESS_SETTING_ID,
                                    0));

            return new Minecraft189SpeedMineFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    progressSetting,
                    progressPresentation,
                    progressBinding);
        } catch (RuntimeException failure) {
            closeQuietly(
                    progressBinding,
                    failure);
            closeQuietly(
                    progressPresentation,
                    failure);
            closeQuietly(
                    progressSetting,
                    failure);
            closeQuietly(
                    presentation,
                    failure);
            closeQuietly(
                    moduleRegistration,
                    failure);
            throw failure;
        }
    }

    Minecraft189SpeedMineModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "speed-mine feature is closed");
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
                    Minecraft189SpeedMineModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189SpeedMineModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(
                progressBinding,
                failure);
        failure = close(
                progressPresentation,
                failure);
        failure = close(
                progressSetting,
                failure);
        failure = close(
                presentation,
                failure);
        failure = close(
                moduleRegistration,
                failure);

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
            return append(
                    primary,
                    failure);
        } catch (Exception failure) {
            return append(
                    primary,
                    new IllegalStateException(
                            "speed-mine feature close failed",
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
            primary.addSuppressed(
                    cleanupFailure);
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
