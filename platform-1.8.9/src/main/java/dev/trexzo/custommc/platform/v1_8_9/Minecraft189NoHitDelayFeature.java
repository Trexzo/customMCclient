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

final class Minecraft189NoHitDelayFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189NoHitDelayModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration delaySetting;
    private final SettingPresentationRegistry.Registration delayPresentation;
    private final ModuleSettingRegistry.Registration delayBinding;
    private boolean closed;

    private Minecraft189NoHitDelayFeature(
            final ModuleController controller,
            final Minecraft189NoHitDelayModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration delaySetting,
            final SettingPresentationRegistry.Registration delayPresentation,
            final ModuleSettingRegistry.Registration delayBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.delaySetting = delaySetting;
        this.delayPresentation = delayPresentation;
        this.delayBinding = delayBinding;
    }

    static Minecraft189NoHitDelayFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189NoHitDelayModule module =
                new Minecraft189NoHitDelayModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration delaySetting = null;
        SettingPresentationRegistry.Registration delayPresentation = null;
        ModuleSettingRegistry.Registration delayBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189NoHitDelayModule.ID,
                                    "No Hit Delay",
                                    "Controls the positive vanilla left-click cooldown counter.",
                                    Minecraft189FeatureCatalog
                                            .COMBAT_CATEGORY_ID,
                                    0));
            delaySetting =
                    settings.register(
                            module.delaySetting());
            delayPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189NoHitDelayModule.DELAY_SETTING_ID,
                                    "Delay",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189NoHitDelayModule.MINIMUM_DELAY,
                                            Minecraft189NoHitDelayModule.MAXIMUM_DELAY,
                                            1.0D)));
            delayBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189NoHitDelayModule.ID,
                                    Minecraft189NoHitDelayModule.DELAY_SETTING_ID,
                                    0));

            return new Minecraft189NoHitDelayFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    delaySetting,
                    delayPresentation,
                    delayBinding);
        } catch (RuntimeException failure) {
            closeQuietly(delayBinding, failure);
            closeQuietly(delayPresentation, failure);
            closeQuietly(delaySetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189NoHitDelayModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "no-hit-delay feature is closed");
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
                    Minecraft189NoHitDelayModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189NoHitDelayModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(delayBinding, failure);
        failure = close(delayPresentation, failure);
        failure = close(delaySetting, failure);
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
                            "no-hit-delay feature close failed",
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
