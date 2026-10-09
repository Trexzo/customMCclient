package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;

final class Minecraft189NoWebFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189NoWebModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingRegistry.Registration requireSneakingSetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final SettingPresentationRegistry.Registration requireSneakingPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private final ModuleSettingRegistry.Registration requireSneakingBinding;
    private final SettingRegistry.Registration airborneOnlySetting;
    private final SettingPresentationRegistry.Registration airborneOnlyPresentation;
    private final ModuleSettingRegistry.Registration airborneOnlyBinding;
    private boolean closed;

    private Minecraft189NoWebFeature(
            final ModuleController controller,
            final Minecraft189NoWebModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingRegistry.Registration requireSneakingSetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final SettingPresentationRegistry.Registration requireSneakingPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding,
            final ModuleSettingRegistry.Registration requireSneakingBinding,
            final SettingRegistry.Registration airborneOnlySetting,
            final SettingPresentationRegistry.Registration airborneOnlyPresentation,
            final ModuleSettingRegistry.Registration airborneOnlyBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.groundOnlySetting = groundOnlySetting;
        this.requireSneakingSetting = requireSneakingSetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.requireSneakingPresentation = requireSneakingPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
        this.requireSneakingBinding = requireSneakingBinding;
        this.airborneOnlySetting = airborneOnlySetting;
        this.airborneOnlyPresentation = airborneOnlyPresentation;
        this.airborneOnlyBinding = airborneOnlyBinding;
    }

    static Minecraft189NoWebFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189NoWebModule module =
                new Minecraft189NoWebModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingRegistry.Registration requireSneakingSetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        SettingPresentationRegistry.Registration requireSneakingPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        ModuleSettingRegistry.Registration requireSneakingBinding = null;
        SettingRegistry.Registration airborneOnlySetting = null;
        SettingPresentationRegistry.Registration airborneOnlyPresentation = null;
        ModuleSettingRegistry.Registration airborneOnlyBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189NoWebModule.ID,
                                    "No Web",
                                    "Clears the local web-state flag while enabled.",
                                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID,
                                    60));

            groundOnlySetting = settings.register(module.groundOnlySetting());
            requireSneakingSetting = settings.register(
                    module.requireSneakingSetting());
            airborneOnlySetting = settings.register(module.airborneOnlySetting());
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoWebModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 10));
            requireSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoWebModule.REQUIRE_SNEAKING_SETTING_ID,
                            "Require Sneaking", SettingValueKind.BOOLEAN, 20));
            airborneOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoWebModule.AIRBORNE_ONLY_SETTING_ID,
                            "Airborne Only", SettingValueKind.BOOLEAN, 30));
            groundOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoWebModule.ID,
                            Minecraft189NoWebModule.GROUND_ONLY_SETTING_ID, 10));
            requireSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoWebModule.ID,
                            Minecraft189NoWebModule.REQUIRE_SNEAKING_SETTING_ID, 20));
            airborneOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoWebModule.ID,
                            Minecraft189NoWebModule.AIRBORNE_ONLY_SETTING_ID, 30));

            return new Minecraft189NoWebFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    groundOnlySetting,
                    requireSneakingSetting,
                    groundOnlyPresentation,
                    requireSneakingPresentation,
                    groundOnlyBinding,
                    requireSneakingBinding,
                    airborneOnlySetting,
                    airborneOnlyPresentation,
                    airborneOnlyBinding);
        } catch (RuntimeException failure) {
            closeQuietly(airborneOnlyBinding, failure);
            closeQuietly(airborneOnlyPresentation, failure);
            closeQuietly(airborneOnlySetting, failure);
            closeQuietly(requireSneakingBinding, failure);
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(requireSneakingPresentation, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(requireSneakingSetting, failure);
            closeQuietly(groundOnlySetting, failure);
            if (presentation != null) {
                presentation.close();
            }
            if (moduleRegistration != null) {
                moduleRegistration.close();
            }
            throw failure;
        }
    }

    Minecraft189NoWebModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "no-web feature is closed");
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
                    Minecraft189NoWebModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189NoWebModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(airborneOnlyBinding, failure);
        failure = close(airborneOnlyPresentation, failure);
        failure = close(airborneOnlySetting, failure);
        failure = close(requireSneakingBinding, failure);
        failure = close(groundOnlyBinding, failure);
        failure = close(requireSneakingPresentation, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(requireSneakingSetting, failure);
        failure = close(groundOnlySetting, failure);
        try {
            presentation.close();
        } catch (RuntimeException closeFailure) {
            failure = append(failure, closeFailure);
        }
        try {
            moduleRegistration.close();
        } catch (RuntimeException closeFailure) {
            failure = append(failure, closeFailure);
        }

        if (failure != null) {
            throw failure;
        }
    }

    private static RuntimeException close(
            final AutoCloseable item,
            final RuntimeException primary) {
        if (item == null) {
            return primary;
        }
        try {
            item.close();
            return primary;
        } catch (RuntimeException failure) {
            return append(primary, failure);
        } catch (Exception failure) {
            return append(primary, new IllegalStateException(
                    "no-web setting close failed", failure));
        }
    }

    private static void closeQuietly(
            final AutoCloseable item,
            final RuntimeException primary) {
        if (item == null) {
            return;
        }
        try {
            item.close();
        } catch (Exception failure) {
            primary.addSuppressed(failure);
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
