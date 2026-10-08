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

final class Minecraft189StepFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189StepModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration heightSetting;
    private final SettingPresentationRegistry.Registration heightPresentation;
    private final ModuleSettingRegistry.Registration heightBinding;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private boolean closed;

    private Minecraft189StepFeature(
            final ModuleController controller,
            final Minecraft189StepModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration heightSetting,
            final SettingPresentationRegistry.Registration heightPresentation,
            final ModuleSettingRegistry.Registration heightBinding,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.heightSetting = heightSetting;
        this.heightPresentation = heightPresentation;
        this.heightBinding = heightBinding;
        this.groundOnlySetting = groundOnlySetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
    }

    static Minecraft189StepFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189StepModule module =
                new Minecraft189StepModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration heightSetting = null;
        SettingPresentationRegistry.Registration heightPresentation = null;
        ModuleSettingRegistry.Registration heightBinding = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;

        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189StepModule.ID,
                                    "Step",
                                    "Raises the player's vanilla step height.",
                                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID,
                                    40));
            heightSetting =
                    settings.register(
                            module.heightPercentSetting());
            groundOnlySetting = settings.register(module.groundOnlySetting());
            pauseWhileSneakingSetting = settings.register(module.pauseWhileSneakingSetting());
            heightPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189StepModule.HEIGHT_SETTING_ID,
                                    "Height %",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            60.0D,
                                            250.0D,
                                            5.0D)));
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189StepModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 10));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189StepModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 20));
            heightBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189StepModule.ID,
                                    Minecraft189StepModule.HEIGHT_SETTING_ID,
                                    0));

            groundOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189StepModule.ID,
                            Minecraft189StepModule.GROUND_ONLY_SETTING_ID, 10));
            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189StepModule.ID,
                            Minecraft189StepModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 20));

            return new Minecraft189StepFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    heightSetting,
                    heightPresentation,
                    heightBinding,
                    groundOnlySetting,
                    pauseWhileSneakingSetting,
                    groundOnlyPresentation,
                    pauseWhileSneakingPresentation,
                    groundOnlyBinding,
                    pauseWhileSneakingBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(groundOnlySetting, failure);
            closeQuietly(heightBinding, failure);
            closeQuietly(heightPresentation, failure);
            closeQuietly(heightSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189StepModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "step feature is closed");
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
                    Minecraft189StepModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189StepModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(groundOnlyBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(groundOnlySetting, failure);
        failure = close(heightBinding, failure);
        failure = close(heightPresentation, failure);
        failure = close(heightSetting, failure);
        failure = close(presentation, failure);
        failure = close(moduleRegistration, failure);

        if (failure != null) {
            throw failure;
        }
    }

    private static RuntimeException close(
            final AutoCloseable closeable,
            final RuntimeException primary) {
        try {
            closeable.close();
            return primary;
        } catch (RuntimeException failure) {
            return append(primary, failure);
        } catch (Exception failure) {
            return append(
                    primary,
                    new IllegalStateException(
                            "step feature close failed",
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
