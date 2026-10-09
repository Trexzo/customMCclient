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

final class Minecraft189NoClipFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189NoClipModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration requireSneakingSetting;
    private final SettingPresentationRegistry.Registration requireSneakingPresentation;
    private final ModuleSettingRegistry.Registration requireSneakingBinding;
    private boolean closed;

    private Minecraft189NoClipFeature(
            final ModuleController controller,
            final Minecraft189NoClipModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration requireSneakingSetting,
            final SettingPresentationRegistry.Registration requireSneakingPresentation,
            final ModuleSettingRegistry.Registration requireSneakingBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.requireSneakingSetting = requireSneakingSetting;
        this.requireSneakingPresentation = requireSneakingPresentation;
        this.requireSneakingBinding = requireSneakingBinding;
    }

    static Minecraft189NoClipFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189NoClipModule module =
                new Minecraft189NoClipModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration requireSneakingSetting = null;
        SettingPresentationRegistry.Registration requireSneakingPresentation = null;
        ModuleSettingRegistry.Registration requireSneakingBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189NoClipModule.ID,
                                    "No Clip",
                                    "Forces local no-clip collision state and restores its prior value.",
                                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID,
                                    70));

            requireSneakingSetting = settings.register(
                    module.requireSneakingSetting());
            requireSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoClipModule.REQUIRE_SNEAK_SETTING_ID,
                            "Hold Sneak", SettingValueKind.BOOLEAN, 10));
            requireSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoClipModule.ID,
                            Minecraft189NoClipModule.REQUIRE_SNEAK_SETTING_ID, 10));

            return new Minecraft189NoClipFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    requireSneakingSetting,
                    requireSneakingPresentation,
                    requireSneakingBinding);
        } catch (RuntimeException failure) {
            closeQuietly(requireSneakingBinding, failure);
            closeQuietly(requireSneakingPresentation, failure);
            closeQuietly(requireSneakingSetting, failure);
            if (presentation != null) {
                presentation.close();
            }
            if (moduleRegistration != null) {
                moduleRegistration.close();
            }
            throw failure;
        }
    }

    Minecraft189NoClipModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "no-clip feature is closed");
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
                    Minecraft189NoClipModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189NoClipModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(requireSneakingBinding, failure);
        failure = close(requireSneakingPresentation, failure);
        failure = close(requireSneakingSetting, failure);
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
                    "no-clip setting close failed", failure));
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
