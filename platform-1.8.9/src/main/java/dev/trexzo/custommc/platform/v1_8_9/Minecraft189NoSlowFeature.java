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

final class Minecraft189NoSlowFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189NoSlowModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration speedSetting;
    private final SettingPresentationRegistry.Registration speedPresentation;
    private final ModuleSettingRegistry.Registration speedBinding;
    private final SettingRegistry.Registration airborneOverrideSetting;
    private final SettingRegistry.Registration airborneSpeedSetting;
    private final SettingPresentationRegistry.Registration airborneOverridePresentation;
    private final SettingPresentationRegistry.Registration airborneSpeedPresentation;
    private final ModuleSettingRegistry.Registration airborneOverrideBinding;
    private final ModuleSettingRegistry.Registration airborneSpeedBinding;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private boolean closed;

    private Minecraft189NoSlowFeature(
            final ModuleController controller,
            final Minecraft189NoSlowModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration speedSetting,
            final SettingPresentationRegistry.Registration speedPresentation,
            final ModuleSettingRegistry.Registration speedBinding,
            final SettingRegistry.Registration airborneOverrideSetting,
            final SettingRegistry.Registration airborneSpeedSetting,
            final SettingPresentationRegistry.Registration airborneOverridePresentation,
            final SettingPresentationRegistry.Registration airborneSpeedPresentation,
            final ModuleSettingRegistry.Registration airborneOverrideBinding,
            final ModuleSettingRegistry.Registration airborneSpeedBinding,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.speedSetting = speedSetting;
        this.speedPresentation = speedPresentation;
        this.speedBinding = speedBinding;
        this.airborneOverrideSetting = airborneOverrideSetting;
        this.airborneSpeedSetting = airborneSpeedSetting;
        this.airborneOverridePresentation = airborneOverridePresentation;
        this.airborneSpeedPresentation = airborneSpeedPresentation;
        this.airborneOverrideBinding = airborneOverrideBinding;
        this.airborneSpeedBinding = airborneSpeedBinding;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
    }

    static Minecraft189NoSlowFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189NoSlowModule module =
                new Minecraft189NoSlowModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration speedSetting = null;
        SettingPresentationRegistry.Registration speedPresentation = null;
        ModuleSettingRegistry.Registration speedBinding = null;
        SettingRegistry.Registration airborneOverrideSetting = null;
        SettingRegistry.Registration airborneSpeedSetting = null;
        SettingPresentationRegistry.Registration airborneOverridePresentation = null;
        SettingPresentationRegistry.Registration airborneSpeedPresentation = null;
        ModuleSettingRegistry.Registration airborneOverrideBinding = null;
        ModuleSettingRegistry.Registration airborneSpeedBinding = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189NoSlowModule.ID,
                                    "No Slow",
                                    "Controls movement speed while using an item.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    30));
            speedSetting =
                    settings.register(
                            module.speedPercentSetting());
            airborneOverrideSetting = settings.register(
                    module.airborneOverrideSetting());
            airborneSpeedSetting = settings.register(
                    module.airborneSpeedPercentSetting());
            pauseWhileSneakingSetting = settings.register(
                    module.pauseWhileSneakingSetting());
            speedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189NoSlowModule.SPEED_PERCENT_SETTING_ID,
                                    "Speed %",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189NoSlowModule.MINIMUM_SPEED_PERCENT,
                                            Minecraft189NoSlowModule.MAXIMUM_SPEED_PERCENT,
                                            5.0D)));
            airborneOverridePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoSlowModule.AIRBORNE_OVERRIDE_SETTING_ID,
                            "Airborne Override", SettingValueKind.BOOLEAN, 10));
            airborneSpeedPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoSlowModule.AIRBORNE_SPEED_SETTING_ID,
                            "Air Speed %", SettingValueKind.INTEGER, 20,
                            new SettingNumericSpec(
                                    Minecraft189NoSlowModule.MINIMUM_SPEED_PERCENT,
                                    Minecraft189NoSlowModule.MAXIMUM_SPEED_PERCENT, 5.0D)));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoSlowModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 30));
            speedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189NoSlowModule.ID,
                                    Minecraft189NoSlowModule.SPEED_PERCENT_SETTING_ID,
                                    0));

            airborneOverrideBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoSlowModule.ID,
                            Minecraft189NoSlowModule.AIRBORNE_OVERRIDE_SETTING_ID, 10));
            airborneSpeedBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoSlowModule.ID,
                            Minecraft189NoSlowModule.AIRBORNE_SPEED_SETTING_ID, 20));

            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoSlowModule.ID,
                            Minecraft189NoSlowModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 30));

            return new Minecraft189NoSlowFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    speedSetting,
                    speedPresentation,
                    speedBinding,
                    airborneOverrideSetting,
                    airborneSpeedSetting,
                    airborneOverridePresentation,
                    airborneSpeedPresentation,
                    airborneOverrideBinding,
                    airborneSpeedBinding,
                    pauseWhileSneakingSetting,
                    pauseWhileSneakingPresentation,
                    pauseWhileSneakingBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
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

    Minecraft189NoSlowModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "no-slow feature is closed");
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
                    Minecraft189NoSlowModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189NoSlowModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
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
                            "no-slow feature close failed",
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
