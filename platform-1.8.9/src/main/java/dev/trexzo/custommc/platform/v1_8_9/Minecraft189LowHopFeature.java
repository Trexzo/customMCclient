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

final class Minecraft189LowHopFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189LowHopModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration verticalSpeedSetting;
    private final SettingPresentationRegistry.Registration verticalSpeedPresentation;
    private final ModuleSettingRegistry.Registration verticalSpeedBinding;
    private final SettingRegistry.Registration requireMovementSetting;
    private final SettingPresentationRegistry.Registration requireMovementPresentation;
    private final ModuleSettingRegistry.Registration requireMovementBinding;
    private boolean closed;

    private Minecraft189LowHopFeature(
            final ModuleController controller,
            final Minecraft189LowHopModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration verticalSpeedSetting,
            final SettingPresentationRegistry.Registration verticalSpeedPresentation,
            final ModuleSettingRegistry.Registration verticalSpeedBinding,
            final SettingRegistry.Registration requireMovementSetting,
            final SettingPresentationRegistry.Registration requireMovementPresentation,
            final ModuleSettingRegistry.Registration requireMovementBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.verticalSpeedSetting = verticalSpeedSetting;
        this.verticalSpeedPresentation = verticalSpeedPresentation;
        this.verticalSpeedBinding = verticalSpeedBinding;
        this.requireMovementSetting = requireMovementSetting;
        this.requireMovementPresentation = requireMovementPresentation;
        this.requireMovementBinding = requireMovementBinding;
    }

    static Minecraft189LowHopFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState) {
        final Minecraft189LowHopModule module =
                new Minecraft189LowHopModule(
                        inputState);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration verticalSpeedSetting = null;
        SettingPresentationRegistry.Registration verticalSpeedPresentation = null;
        ModuleSettingRegistry.Registration verticalSpeedBinding = null;
        SettingRegistry.Registration requireMovementSetting = null;
        SettingPresentationRegistry.Registration requireMovementPresentation = null;
        ModuleSettingRegistry.Registration requireMovementBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189LowHopModule.ID,
                                    "Low Hop",
                                    "Caps a fresh grounded jump to a configurable lower vertical speed.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    170));

            verticalSpeedSetting =
                    settings.register(
                            module.verticalSpeedSetting());
            requireMovementSetting = settings.register(
                    module.requireMovementSetting());
            verticalSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189LowHopModule.VERTICAL_SPEED_SETTING_ID,
                                    "Vertical Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189LowHopModule.MINIMUM_VERTICAL_SPEED,
                                            Minecraft189LowHopModule.MAXIMUM_VERTICAL_SPEED,
                                            0.01D)));
            requireMovementPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189LowHopModule.REQUIRE_MOVEMENT_SETTING_ID,
                            "Require Movement (WASD)", SettingValueKind.BOOLEAN, 10));
            verticalSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189LowHopModule.ID,
                                    Minecraft189LowHopModule.VERTICAL_SPEED_SETTING_ID,
                                    0));

            requireMovementBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189LowHopModule.ID,
                            Minecraft189LowHopModule.REQUIRE_MOVEMENT_SETTING_ID, 10));
            return new Minecraft189LowHopFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    verticalSpeedSetting,
                    verticalSpeedPresentation,
                    verticalSpeedBinding,
                    requireMovementSetting,
                    requireMovementPresentation,
                    requireMovementBinding);
        } catch (RuntimeException failure) {
            closeQuietly(requireMovementBinding, failure);
            closeQuietly(requireMovementPresentation, failure);
            closeQuietly(requireMovementSetting, failure);
            closeQuietly(verticalSpeedBinding, failure);
            closeQuietly(verticalSpeedPresentation, failure);
            closeQuietly(verticalSpeedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189LowHopModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "low-hop feature is closed");
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
                    Minecraft189LowHopModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189LowHopModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(requireMovementBinding, failure);
        failure = close(requireMovementPresentation, failure);
        failure = close(requireMovementSetting, failure);
        failure = close(verticalSpeedBinding, failure);
        failure = close(verticalSpeedPresentation, failure);
        failure = close(verticalSpeedSetting, failure);
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
                            "low-hop feature close failed",
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
