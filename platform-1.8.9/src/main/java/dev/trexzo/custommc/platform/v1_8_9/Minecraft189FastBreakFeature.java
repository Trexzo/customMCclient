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

final class Minecraft189FastBreakFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FastBreakModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration delaySetting;
    private final SettingPresentationRegistry.Registration delayPresentation;
    private final ModuleSettingRegistry.Registration delayBinding;
    private boolean closed;

    private Minecraft189FastBreakFeature(
            final ModuleController controller,
            final Minecraft189FastBreakModule module,
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

    static Minecraft189FastBreakFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189FastBreakModule module =
                new Minecraft189FastBreakModule();

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
                                    Minecraft189FastBreakModule.ID,
                                    "Fast Break",
                                    "Controls the local block-hit delay.",
                                    Minecraft189FeatureCatalog
                                            .PLAYER_CATEGORY_ID,
                                    10));
            delaySetting =
                    settings.register(
                            module.delaySetting());
            delayPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189FastBreakModule.DELAY_SETTING_ID,
                                    "Delay",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189FastBreakModule.MINIMUM_DELAY,
                                            Minecraft189FastBreakModule.MAXIMUM_DELAY,
                                            1.0D)));
            delayBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189FastBreakModule.ID,
                                    Minecraft189FastBreakModule.DELAY_SETTING_ID,
                                    0));

            return new Minecraft189FastBreakFeature(
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

    Minecraft189FastBreakModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "fast-break feature is closed");
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
                    Minecraft189FastBreakModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189FastBreakModule.ID);
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
                            "fast-break feature close failed",
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
