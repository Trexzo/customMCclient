package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;

final class Minecraft189AutoSprintFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoSprintModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration requireForwardSetting;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingPresentationRegistry.Registration requireForwardPresentation;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final ModuleSettingRegistry.Registration requireForwardBinding;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private boolean closed;

    private Minecraft189AutoSprintFeature(
            final ModuleController controller,
            final Minecraft189AutoSprintModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration requireForwardSetting,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingPresentationRegistry.Registration requireForwardPresentation,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final ModuleSettingRegistry.Registration requireForwardBinding,
            final ModuleSettingRegistry.Registration groundOnlyBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.requireForwardSetting = requireForwardSetting;
        this.groundOnlySetting = groundOnlySetting;
        this.requireForwardPresentation = requireForwardPresentation;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.requireForwardBinding = requireForwardBinding;
        this.groundOnlyBinding = groundOnlyBinding;
    }

    static Minecraft189AutoSprintFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AutoSprintModule module =
                new Minecraft189AutoSprintModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration requireForwardSetting = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingPresentationRegistry.Registration requireForwardPresentation = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        ModuleSettingRegistry.Registration requireForwardBinding = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AutoSprintModule.ID,
                                    "Auto Sprint",
                                    "Keeps sprint enabled while possible without overriding sneak.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    0));
            requireForwardSetting =
                    settings.register(
                            module.requireForwardSetting());
            groundOnlySetting = settings.register(module.groundOnlySetting());
            requireForwardPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoSprintModule.REQUIRE_FORWARD_SETTING_ID,
                                    "Require Forward",
                                    SettingValueKind.BOOLEAN,
                                    0));
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoSprintModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 10));
            requireForwardBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoSprintModule.ID,
                                    Minecraft189AutoSprintModule.REQUIRE_FORWARD_SETTING_ID,
                                    0));
            groundOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189AutoSprintModule.ID,
                            Minecraft189AutoSprintModule.GROUND_ONLY_SETTING_ID, 10));

            return new Minecraft189AutoSprintFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    requireForwardSetting,
                    groundOnlySetting,
                    requireForwardPresentation,
                    groundOnlyPresentation,
                    requireForwardBinding,
                    groundOnlyBinding);
        } catch (RuntimeException failure) {
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(groundOnlySetting, failure);
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

    Minecraft189AutoSprintModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "auto-sprint feature is closed");
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
                    Minecraft189AutoSprintModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AutoSprintModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(groundOnlyBinding, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(groundOnlySetting, failure);
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
                            "auto-sprint feature close failed",
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
