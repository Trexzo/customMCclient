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
    private final SettingPresentationRegistry.Registration speedPresentation;
    private final ModuleSettingRegistry.Registration speedBinding;
    private boolean closed;

    private Minecraft189TimerSpeedFeature(
            final ModuleController controller,
            final Minecraft189TimerSpeedModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration speedSetting,
            final SettingPresentationRegistry.Registration speedPresentation,
            final ModuleSettingRegistry.Registration speedBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.speedSetting = speedSetting;
        this.speedPresentation = speedPresentation;
        this.speedBinding = speedBinding;
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
        SettingPresentationRegistry.Registration speedPresentation = null;
        ModuleSettingRegistry.Registration speedBinding = null;

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
            speedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189TimerSpeedModule.ID,
                                    Minecraft189TimerSpeedModule.SPEED_SETTING_ID,
                                    0));

            return new Minecraft189TimerSpeedFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    speedSetting,
                    speedPresentation,
                    speedBinding);
        } catch (RuntimeException failure) {
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
