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

final class Minecraft189ReverseStepFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189ReverseStepModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration speedSetting;
    private final SettingRegistry.Registration delayTicksSetting;
    private final SettingRegistry.Registration requireSneakingSetting;
    private final SettingPresentationRegistry.Registration speedPresentation;
    private final SettingPresentationRegistry.Registration delayTicksPresentation;
    private final SettingPresentationRegistry.Registration requireSneakingPresentation;
    private final ModuleSettingRegistry.Registration speedBinding;
    private final ModuleSettingRegistry.Registration delayTicksBinding;
    private final ModuleSettingRegistry.Registration requireSneakingBinding;
    private final SettingRegistry.Registration requireMovementSetting;
    private final SettingPresentationRegistry.Registration requireMovementPresentation;
    private final ModuleSettingRegistry.Registration requireMovementBinding;
    private boolean closed;

    private Minecraft189ReverseStepFeature(
            final ModuleController controller,
            final Minecraft189ReverseStepModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration speedSetting,
            final SettingRegistry.Registration delayTicksSetting,
            final SettingRegistry.Registration requireSneakingSetting,
            final SettingPresentationRegistry.Registration speedPresentation,
            final SettingPresentationRegistry.Registration delayTicksPresentation,
            final SettingPresentationRegistry.Registration requireSneakingPresentation,
            final ModuleSettingRegistry.Registration speedBinding,
            final ModuleSettingRegistry.Registration delayTicksBinding,
            final ModuleSettingRegistry.Registration requireSneakingBinding,
            final SettingRegistry.Registration requireMovementSetting,
            final SettingPresentationRegistry.Registration requireMovementPresentation,
            final ModuleSettingRegistry.Registration requireMovementBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.speedSetting = speedSetting;
        this.delayTicksSetting = delayTicksSetting;
        this.requireSneakingSetting = requireSneakingSetting;
        this.speedPresentation = speedPresentation;
        this.delayTicksPresentation = delayTicksPresentation;
        this.requireSneakingPresentation = requireSneakingPresentation;
        this.speedBinding = speedBinding;
        this.delayTicksBinding = delayTicksBinding;
        this.requireSneakingBinding = requireSneakingBinding;
        this.requireMovementSetting = requireMovementSetting;
        this.requireMovementPresentation = requireMovementPresentation;
        this.requireMovementBinding = requireMovementBinding;
    }

    static Minecraft189ReverseStepFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189ReverseStepModule module =
                new Minecraft189ReverseStepModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration speedSetting = null;
        SettingRegistry.Registration delayTicksSetting = null;
        SettingRegistry.Registration requireSneakingSetting = null;
        SettingPresentationRegistry.Registration speedPresentation = null;
        SettingPresentationRegistry.Registration delayTicksPresentation = null;
        SettingPresentationRegistry.Registration requireSneakingPresentation = null;
        ModuleSettingRegistry.Registration speedBinding = null;
        ModuleSettingRegistry.Registration delayTicksBinding = null;
        ModuleSettingRegistry.Registration requireSneakingBinding = null;
        SettingRegistry.Registration requireMovementSetting = null;
        SettingPresentationRegistry.Registration requireMovementPresentation = null;
        ModuleSettingRegistry.Registration requireMovementBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189ReverseStepModule.ID,
                                    "Reverse Step",
                                    "Snaps downward when walking off an edge without interfering with upward jumps.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    200));

            speedSetting =
                    settings.register(
                            module.speedSetting());
            delayTicksSetting = settings.register(module.delayTicksSetting());
            requireSneakingSetting = settings.register(
                    module.requireSneakingSetting());
            requireMovementSetting = settings.register(
                    module.requireMovementSetting());
            speedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189ReverseStepModule.SPEED_SETTING_ID,
                                    "Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189ReverseStepModule.MINIMUM_SPEED,
                                            Minecraft189ReverseStepModule.MAXIMUM_SPEED,
                                            0.05D)));
            delayTicksPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ReverseStepModule.DELAY_TICKS_SETTING_ID,
                            "Drop Delay Ticks", SettingValueKind.INTEGER, 10,
                            new SettingNumericSpec(
                                    Minecraft189ReverseStepModule.MINIMUM_DELAY_TICKS,
                                    Minecraft189ReverseStepModule.MAXIMUM_DELAY_TICKS,
                                    1.0D)));
            requireSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ReverseStepModule.REQUIRE_SNEAK_SETTING_ID,
                            "Require Sneaking", SettingValueKind.BOOLEAN, 20));
            requireMovementPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ReverseStepModule.REQUIRE_MOVEMENT_SETTING_ID,
                            "Require Movement (WASD)", SettingValueKind.BOOLEAN, 30));
            speedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189ReverseStepModule.ID,
                                    Minecraft189ReverseStepModule.SPEED_SETTING_ID,
                                    0));

            delayTicksBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ReverseStepModule.ID,
                            Minecraft189ReverseStepModule.DELAY_TICKS_SETTING_ID,
                            10));
            requireSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ReverseStepModule.ID,
                            Minecraft189ReverseStepModule.REQUIRE_SNEAK_SETTING_ID,
                            20));

            requireMovementBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ReverseStepModule.ID,
                            Minecraft189ReverseStepModule.REQUIRE_MOVEMENT_SETTING_ID, 30));

            return new Minecraft189ReverseStepFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    speedSetting,
                    delayTicksSetting,
                    requireSneakingSetting,
                    speedPresentation,
                    delayTicksPresentation,
                    requireSneakingPresentation,
                    speedBinding,
                    delayTicksBinding,
                    requireSneakingBinding,
                    requireMovementSetting,
                    requireMovementPresentation,
                    requireMovementBinding);
        } catch (RuntimeException failure) {
            closeQuietly(requireMovementBinding, failure);
            closeQuietly(requireMovementPresentation, failure);
            closeQuietly(requireMovementSetting, failure);
            closeQuietly(requireSneakingBinding, failure);
            closeQuietly(delayTicksBinding, failure);
            closeQuietly(requireSneakingPresentation, failure);
            closeQuietly(delayTicksPresentation, failure);
            closeQuietly(requireSneakingSetting, failure);
            closeQuietly(delayTicksSetting, failure);
            closeQuietly(speedBinding, failure);
            closeQuietly(speedPresentation, failure);
            closeQuietly(speedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189ReverseStepModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "reverse-step feature is closed");
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
                    Minecraft189ReverseStepModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189ReverseStepModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(requireMovementBinding, failure);
        failure = close(requireMovementPresentation, failure);
        failure = close(requireMovementSetting, failure);
        failure = close(requireSneakingBinding, failure);
        failure = close(delayTicksBinding, failure);
        failure = close(requireSneakingPresentation, failure);
        failure = close(delayTicksPresentation, failure);
        failure = close(requireSneakingSetting, failure);
        failure = close(delayTicksSetting, failure);
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
                            "reverse-step feature close failed",
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
