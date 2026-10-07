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

final class Minecraft189GlideFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189GlideModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration fallSpeedSetting;
    private final SettingPresentationRegistry.Registration fallSpeedPresentation;
    private final ModuleSettingRegistry.Registration fallSpeedBinding;
    private boolean closed;

    private Minecraft189GlideFeature(
            final ModuleController controller,
            final Minecraft189GlideModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration fallSpeedSetting,
            final SettingPresentationRegistry.Registration fallSpeedPresentation,
            final ModuleSettingRegistry.Registration fallSpeedBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.fallSpeedSetting = fallSpeedSetting;
        this.fallSpeedPresentation = fallSpeedPresentation;
        this.fallSpeedBinding = fallSpeedBinding;
    }

    static Minecraft189GlideFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189GlideModule module =
                new Minecraft189GlideModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration fallSpeedSetting = null;
        SettingPresentationRegistry.Registration fallSpeedPresentation = null;
        ModuleSettingRegistry.Registration fallSpeedBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189GlideModule.ID,
                                    "Glide",
                                    "Caps excessive downward motion while airborne.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    100));

            fallSpeedSetting =
                    settings.register(
                            module.fallSpeedSetting());
            fallSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189GlideModule.FALL_SPEED_SETTING_ID,
                                    "Fall Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189GlideModule.MINIMUM_FALL_SPEED,
                                            Minecraft189GlideModule.MAXIMUM_FALL_SPEED,
                                            0.01D)));
            fallSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189GlideModule.ID,
                                    Minecraft189GlideModule.FALL_SPEED_SETTING_ID,
                                    0));

            return new Minecraft189GlideFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    fallSpeedSetting,
                    fallSpeedPresentation,
                    fallSpeedBinding);
        } catch (RuntimeException failure) {
            closeQuietly(fallSpeedBinding, failure);
            closeQuietly(fallSpeedPresentation, failure);
            closeQuietly(fallSpeedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189GlideModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "glide feature is closed");
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
                    Minecraft189GlideModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189GlideModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(fallSpeedBinding, failure);
        failure = close(fallSpeedPresentation, failure);
        failure = close(fallSpeedSetting, failure);
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
                            "glide feature close failed",
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
