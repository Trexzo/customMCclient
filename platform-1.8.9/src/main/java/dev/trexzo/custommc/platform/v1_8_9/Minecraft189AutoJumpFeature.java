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

final class Minecraft189AutoJumpFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoJumpModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration requireForwardSetting;
    private final SettingRegistry.Registration landingDelaySetting;
    private final SettingPresentationRegistry.Registration requireForwardPresentation;
    private final SettingPresentationRegistry.Registration landingDelayPresentation;
    private final ModuleSettingRegistry.Registration requireForwardBinding;
    private final ModuleSettingRegistry.Registration landingDelayBinding;
    private boolean closed;

    private Minecraft189AutoJumpFeature(
            final ModuleController controller,
            final Minecraft189AutoJumpModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration requireForwardSetting,
            final SettingRegistry.Registration landingDelaySetting,
            final SettingPresentationRegistry.Registration requireForwardPresentation,
            final SettingPresentationRegistry.Registration landingDelayPresentation,
            final ModuleSettingRegistry.Registration requireForwardBinding,
            final ModuleSettingRegistry.Registration landingDelayBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.requireForwardSetting = requireForwardSetting;
        this.landingDelaySetting = landingDelaySetting;
        this.requireForwardPresentation = requireForwardPresentation;
        this.landingDelayPresentation = landingDelayPresentation;
        this.requireForwardBinding = requireForwardBinding;
        this.landingDelayBinding = landingDelayBinding;
    }

    static Minecraft189AutoJumpFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AutoJumpModule module =
                new Minecraft189AutoJumpModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration requireForwardSetting = null;
        SettingRegistry.Registration landingDelaySetting = null;
        SettingPresentationRegistry.Registration requireForwardPresentation = null;
        SettingPresentationRegistry.Registration landingDelayPresentation = null;
        ModuleSettingRegistry.Registration requireForwardBinding = null;
        ModuleSettingRegistry.Registration landingDelayBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AutoJumpModule.ID,
                                    "Auto Jump",
                                    "Jumps once on each grounded contact.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    10));
            requireForwardSetting =
                    settings.register(
                            module.requireForwardSetting());
            landingDelaySetting = settings.register(
                    module.landingDelayTicksSetting());
            requireForwardPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoJumpModule.REQUIRE_FORWARD_SETTING_ID,
                                    "Require Forward",
                                    SettingValueKind.BOOLEAN,
                                    0));
            landingDelayPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoJumpModule.LANDING_DELAY_SETTING_ID,
                            "Landing Delay Ticks",
                            SettingValueKind.INTEGER,
                            10,
                            new SettingNumericSpec(
                                    0.0D,
                                    Minecraft189AutoJumpModule.MAXIMUM_LANDING_DELAY_TICKS,
                                    1.0D)));
            requireForwardBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoJumpModule.ID,
                                    Minecraft189AutoJumpModule.REQUIRE_FORWARD_SETTING_ID,
                                    0));

            landingDelayBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AutoJumpModule.ID,
                            Minecraft189AutoJumpModule.LANDING_DELAY_SETTING_ID,
                            10));

            return new Minecraft189AutoJumpFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    requireForwardSetting,
                    landingDelaySetting,
                    requireForwardPresentation,
                    landingDelayPresentation,
                    requireForwardBinding,
                    landingDelayBinding);
        } catch (RuntimeException failure) {
            closeQuietly(landingDelayBinding, failure);
            closeQuietly(landingDelayPresentation, failure);
            closeQuietly(landingDelaySetting, failure);
            closeQuietly(requireForwardBinding, failure);
            closeQuietly(requireForwardPresentation, failure);
            closeQuietly(requireForwardSetting, failure);
            if (presentation != null) {
                presentation.close();
            }
            if (moduleRegistration != null) {
                moduleRegistration.close();
            }
            throw failure;
        }
    }

    Minecraft189AutoJumpModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "auto-jump feature is closed");
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
                    Minecraft189AutoJumpModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AutoJumpModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(landingDelayBinding, failure);
        failure = close(landingDelayPresentation, failure);
        failure = close(landingDelaySetting, failure);
        failure = close(requireForwardBinding, failure);
        failure = close(requireForwardPresentation, failure);
        failure = close(requireForwardSetting, failure);

        try {
            presentation.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }
        try {
            moduleRegistration.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

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
                            "auto-jump feature close failed",
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
