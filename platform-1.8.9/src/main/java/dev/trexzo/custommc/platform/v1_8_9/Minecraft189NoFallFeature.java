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
    private final SettingRegistry.Registration airborneOnlySetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingPresentationRegistry.Registration airborneOnlyPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final ModuleSettingRegistry.Registration airborneOnlyBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private boolean closed;

    private Minecraft189NoFallFeature(
            final ModuleController controller,
            final Minecraft189NoFallModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration thresholdSetting,
            final SettingPresentationRegistry.Registration thresholdPresentation,
            final ModuleSettingRegistry.Registration thresholdBinding,
            final SettingRegistry.Registration airborneOnlySetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingPresentationRegistry.Registration airborneOnlyPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final ModuleSettingRegistry.Registration airborneOnlyBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.thresholdSetting = thresholdSetting;
        this.thresholdPresentation = thresholdPresentation;
        this.thresholdBinding = thresholdBinding;
        this.airborneOnlySetting = airborneOnlySetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.airborneOnlyPresentation = airborneOnlyPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.airborneOnlyBinding = airborneOnlyBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
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
        SettingRegistry.Registration airborneOnlySetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingPresentationRegistry.Registration airborneOnlyPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        ModuleSettingRegistry.Registration airborneOnlyBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
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
            airborneOnlySetting = settings.register(module.airborneOnlySetting());
            pauseWhileSneakingSetting = settings.register(
                    module.pauseWhileSneakingSetting());
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
            airborneOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoFallModule.AIRBORNE_ONLY_SETTING_ID,
                            "Airborne Only", SettingValueKind.BOOLEAN, 10));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoFallModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 20));
            thresholdBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189NoFallModule.ID,
                                    Minecraft189NoFallModule.THRESHOLD_SETTING_ID,
                                    0));

            airborneOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoFallModule.ID,
                            Minecraft189NoFallModule.AIRBORNE_ONLY_SETTING_ID, 10));
            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoFallModule.ID,
                            Minecraft189NoFallModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 20));

            return new Minecraft189NoFallFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    thresholdSetting,
                    thresholdPresentation,
                    thresholdBinding,
                    airborneOnlySetting,
                    pauseWhileSneakingSetting,
                    airborneOnlyPresentation,
                    pauseWhileSneakingPresentation,
                    airborneOnlyBinding,
                    pauseWhileSneakingBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(airborneOnlyBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(airborneOnlyPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(airborneOnlySetting, failure);
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

        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(airborneOnlyBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(airborneOnlyPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(airborneOnlySetting, failure);
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
