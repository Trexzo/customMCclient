package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.module.ModuleState;

final class Minecraft189AutoSneakFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoSneakModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingRegistry.Registration pauseSprintingSetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final SettingPresentationRegistry.Registration pauseSprintingPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private final ModuleSettingRegistry.Registration pauseSprintingBinding;
    private boolean closed;

    private Minecraft189AutoSneakFeature(
            final ModuleController controller,
            final Minecraft189AutoSneakModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingRegistry.Registration pauseSprintingSetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final SettingPresentationRegistry.Registration pauseSprintingPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding,
            final ModuleSettingRegistry.Registration pauseSprintingBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.groundOnlySetting = groundOnlySetting;
        this.pauseSprintingSetting = pauseSprintingSetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.pauseSprintingPresentation = pauseSprintingPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
        this.pauseSprintingBinding = pauseSprintingBinding;
    }

    static Minecraft189AutoSneakFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AutoSneakModule module =
                new Minecraft189AutoSneakModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingRegistry.Registration pauseSprintingSetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        SettingPresentationRegistry.Registration pauseSprintingPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        ModuleSettingRegistry.Registration pauseSprintingBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AutoSneakModule.ID,
                                    "Auto Sneak",
                                    "Keeps sneak enabled without spoofing keyboard input.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    20));
            groundOnlySetting = settings.register(module.groundOnlySetting());
            pauseSprintingSetting = settings.register(module.pauseSprintingSetting());
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoSneakModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 0));
            pauseSprintingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189AutoSneakModule.PAUSE_SPRINTING_SETTING_ID,
                            "Pause While Sprinting", SettingValueKind.BOOLEAN, 10));
            groundOnlyBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189AutoSneakModule.ID,
                    Minecraft189AutoSneakModule.GROUND_ONLY_SETTING_ID, 0));
            pauseSprintingBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189AutoSneakModule.ID,
                    Minecraft189AutoSneakModule.PAUSE_SPRINTING_SETTING_ID, 10));

            return new Minecraft189AutoSneakFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    groundOnlySetting,
                    pauseSprintingSetting,
                    groundOnlyPresentation,
                    pauseSprintingPresentation,
                    groundOnlyBinding,
                    pauseSprintingBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pauseSprintingBinding, failure);
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(pauseSprintingPresentation, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(pauseSprintingSetting, failure);
            closeQuietly(groundOnlySetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189AutoSneakModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "auto-sneak feature is closed");
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
                    Minecraft189AutoSneakModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AutoSneakModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(pauseSprintingBinding, failure);
        failure = close(groundOnlyBinding, failure);
        failure = close(pauseSprintingPresentation, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(pauseSprintingSetting, failure);
        failure = close(groundOnlySetting, failure);

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
            final AutoCloseable resource,
            final RuntimeException primary) {
        if (resource == null) {
            return primary;
        }
        try {
            resource.close();
            return primary;
        } catch (RuntimeException failure) {
            return append(primary, failure);
        } catch (Exception failure) {
            return append(primary,
                    new IllegalStateException("auto-sneak feature close failed", failure));
        }
    }

    private static void closeQuietly(
            final AutoCloseable resource,
            final RuntimeException primary) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
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
