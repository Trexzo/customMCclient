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

final class Minecraft189LongJumpFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189LongJumpModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration speedSetting;
    private final SettingPresentationRegistry.Registration speedPresentation;
    private final ModuleSettingRegistry.Registration speedBinding;
    private final SettingRegistry.Registration preserveMomentumSetting;
    private final SettingPresentationRegistry.Registration preserveMomentumPresentation;
    private final ModuleSettingRegistry.Registration preserveMomentumBinding;
    private boolean closed;

    private Minecraft189LongJumpFeature(
            final ModuleController controller,
            final Minecraft189LongJumpModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration speedSetting,
            final SettingPresentationRegistry.Registration speedPresentation,
            final ModuleSettingRegistry.Registration speedBinding,
            final SettingRegistry.Registration preserveMomentumSetting,
            final SettingPresentationRegistry.Registration preserveMomentumPresentation,
            final ModuleSettingRegistry.Registration preserveMomentumBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.speedSetting = speedSetting;
        this.speedPresentation = speedPresentation;
        this.speedBinding = speedBinding;
        this.preserveMomentumSetting = preserveMomentumSetting;
        this.preserveMomentumPresentation = preserveMomentumPresentation;
        this.preserveMomentumBinding = preserveMomentumBinding;
    }

    static Minecraft189LongJumpFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState) {
        final Minecraft189LongJumpModule module =
                new Minecraft189LongJumpModule(
                        inputState);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration speedSetting = null;
        SettingPresentationRegistry.Registration speedPresentation = null;
        ModuleSettingRegistry.Registration speedBinding = null;
        SettingRegistry.Registration preserveMomentumSetting = null;
        SettingPresentationRegistry.Registration preserveMomentumPresentation = null;
        ModuleSettingRegistry.Registration preserveMomentumBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189LongJumpModule.ID,
                                    "Long Jump",
                                    "Adds a configurable yaw-relative horizontal boost to a fresh grounded jump.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    130));
            speedSetting =
                    settings.register(
                            module.speedSetting());
            preserveMomentumSetting = settings.register(
                    module.preserveHigherMomentumSetting());
            speedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189LongJumpModule.SPEED_SETTING_ID,
                                    "Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189LongJumpModule.MINIMUM_SPEED,
                                            Minecraft189LongJumpModule.MAXIMUM_SPEED,
                                            0.05D)));
            preserveMomentumPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189LongJumpModule.PRESERVE_MOMENTUM_SETTING_ID,
                            "Preserve Higher Momentum", SettingValueKind.BOOLEAN, 10));
            speedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189LongJumpModule.ID,
                                    Minecraft189LongJumpModule.SPEED_SETTING_ID,
                                    0));

            preserveMomentumBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189LongJumpModule.ID,
                            Minecraft189LongJumpModule.PRESERVE_MOMENTUM_SETTING_ID, 10));

            return new Minecraft189LongJumpFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    speedSetting,
                    speedPresentation,
                    speedBinding,
                    preserveMomentumSetting,
                    preserveMomentumPresentation,
                    preserveMomentumBinding);
        } catch (RuntimeException failure) {
            closeQuietly(preserveMomentumBinding, failure);
            closeQuietly(preserveMomentumPresentation, failure);
            closeQuietly(preserveMomentumSetting, failure);
            closeQuietly(speedBinding, failure);
            closeQuietly(speedPresentation, failure);
            closeQuietly(speedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189LongJumpModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "long-jump feature is closed");
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
                    Minecraft189LongJumpModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189LongJumpModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(preserveMomentumBinding, failure);
        failure = close(preserveMomentumPresentation, failure);
        failure = close(preserveMomentumSetting, failure);
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
                            "long-jump feature close failed",
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
