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

final class Minecraft189SpeedMineFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189SpeedMineModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration progressSetting;
    private final SettingRegistry.Registration progressiveSetting;
    private final SettingRegistry.Registration stepPercentSetting;
    private final SettingRegistry.Registration requireAttackHeldSetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingPresentationRegistry.Registration progressPresentation;
    private final SettingPresentationRegistry.Registration progressivePresentation;
    private final SettingPresentationRegistry.Registration stepPercentPresentation;
    private final SettingPresentationRegistry.Registration requireAttackHeldPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final ModuleSettingRegistry.Registration progressBinding;
    private final ModuleSettingRegistry.Registration progressiveBinding;
    private final ModuleSettingRegistry.Registration stepPercentBinding;
    private final ModuleSettingRegistry.Registration requireAttackHeldBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private boolean closed;

    private Minecraft189SpeedMineFeature(
            final ModuleController controller,
            final Minecraft189SpeedMineModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration progressSetting,
            final SettingRegistry.Registration progressiveSetting,
            final SettingRegistry.Registration stepPercentSetting,
            final SettingRegistry.Registration requireAttackHeldSetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingPresentationRegistry.Registration progressPresentation,
            final SettingPresentationRegistry.Registration progressivePresentation,
            final SettingPresentationRegistry.Registration stepPercentPresentation,
            final SettingPresentationRegistry.Registration requireAttackHeldPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final ModuleSettingRegistry.Registration progressBinding,
            final ModuleSettingRegistry.Registration progressiveBinding,
            final ModuleSettingRegistry.Registration stepPercentBinding,
            final ModuleSettingRegistry.Registration requireAttackHeldBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.progressSetting = progressSetting;
        this.progressiveSetting = progressiveSetting;
        this.stepPercentSetting = stepPercentSetting;
        this.requireAttackHeldSetting = requireAttackHeldSetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.progressPresentation = progressPresentation;
        this.progressivePresentation = progressivePresentation;
        this.stepPercentPresentation = stepPercentPresentation;
        this.requireAttackHeldPresentation = requireAttackHeldPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.progressBinding = progressBinding;
        this.progressiveBinding = progressiveBinding;
        this.stepPercentBinding = stepPercentBinding;
        this.requireAttackHeldBinding = requireAttackHeldBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
    }

    static Minecraft189SpeedMineFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189SpeedMineModule module =
                new Minecraft189SpeedMineModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration progressSetting = null;
        SettingRegistry.Registration progressiveSetting = null;
        SettingRegistry.Registration stepPercentSetting = null;
        SettingRegistry.Registration requireAttackHeldSetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingPresentationRegistry.Registration progressPresentation = null;
        SettingPresentationRegistry.Registration progressivePresentation = null;
        SettingPresentationRegistry.Registration stepPercentPresentation = null;
        SettingPresentationRegistry.Registration requireAttackHeldPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        ModuleSettingRegistry.Registration progressBinding = null;
        ModuleSettingRegistry.Registration progressiveBinding = null;
        ModuleSettingRegistry.Registration stepPercentBinding = null;
        ModuleSettingRegistry.Registration requireAttackHeldBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;

        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189SpeedMineModule.ID,
                                    "Speed Mine",
                                    "Raises active block-breaking progress to a configured minimum.",
                                    Minecraft189FeatureCatalog
                                            .PLAYER_CATEGORY_ID,
                                    20));
            progressSetting =
                    settings.register(
                            module.progressPercentSetting());
            progressiveSetting = settings.register(module.progressiveSetting());
            stepPercentSetting = settings.register(module.stepPercentSetting());
            requireAttackHeldSetting = settings.register(module.requireAttackHeldSetting());
            pauseWhileSneakingSetting = settings.register(module.pauseWhileSneakingSetting());
            progressPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189SpeedMineModule.PROGRESS_SETTING_ID,
                                    "Minimum Progress",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            0.0D,
                                            100.0D,
                                            1.0D)));
            progressivePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189SpeedMineModule.PROGRESSIVE_SETTING_ID,
                            "Progressive Mode", SettingValueKind.BOOLEAN, 10));
            stepPercentPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189SpeedMineModule.STEP_PERCENT_SETTING_ID,
                            "Ramp Step", SettingValueKind.INTEGER, 20,
                            new SettingNumericSpec(1.0D, 50.0D, 1.0D)));
            requireAttackHeldPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189SpeedMineModule.REQUIRE_ATTACK_HELD_SETTING_ID,
                            "Require Attack Held", SettingValueKind.BOOLEAN, 30));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189SpeedMineModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 40));
            progressBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189SpeedMineModule.ID,
                                    Minecraft189SpeedMineModule.PROGRESS_SETTING_ID,
                                    0));

            progressiveBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189SpeedMineModule.ID,
                            Minecraft189SpeedMineModule.PROGRESSIVE_SETTING_ID, 10));
            stepPercentBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189SpeedMineModule.ID,
                            Minecraft189SpeedMineModule.STEP_PERCENT_SETTING_ID, 20));

            requireAttackHeldBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189SpeedMineModule.ID,
                            Minecraft189SpeedMineModule.REQUIRE_ATTACK_HELD_SETTING_ID, 30));
            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189SpeedMineModule.ID,
                            Minecraft189SpeedMineModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 40));

            return new Minecraft189SpeedMineFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    progressSetting,
                    progressiveSetting,
                    stepPercentSetting,
                    requireAttackHeldSetting,
                    pauseWhileSneakingSetting,
                    progressPresentation,
                    progressivePresentation,
                    stepPercentPresentation,
                    requireAttackHeldPresentation,
                    pauseWhileSneakingPresentation,
                    progressBinding,
                    progressiveBinding,
                    stepPercentBinding,
                    requireAttackHeldBinding,
                    pauseWhileSneakingBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(requireAttackHeldBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(requireAttackHeldPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(requireAttackHeldSetting, failure);
            closeQuietly(stepPercentBinding, failure);
            closeQuietly(progressiveBinding, failure);
            closeQuietly(stepPercentPresentation, failure);
            closeQuietly(progressivePresentation, failure);
            closeQuietly(stepPercentSetting, failure);
            closeQuietly(progressiveSetting, failure);
            closeQuietly(
                    progressBinding,
                    failure);
            closeQuietly(
                    progressPresentation,
                    failure);
            closeQuietly(
                    progressSetting,
                    failure);
            closeQuietly(
                    presentation,
                    failure);
            closeQuietly(
                    moduleRegistration,
                    failure);
            throw failure;
        }
    }

    Minecraft189SpeedMineModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "speed-mine feature is closed");
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
                    Minecraft189SpeedMineModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189SpeedMineModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(requireAttackHeldBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(requireAttackHeldPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(requireAttackHeldSetting, failure);
        failure = close(stepPercentBinding, failure);
        failure = close(progressiveBinding, failure);
        failure = close(stepPercentPresentation, failure);
        failure = close(progressivePresentation, failure);
        failure = close(stepPercentSetting, failure);
        failure = close(progressiveSetting, failure);
        failure = close(
                progressBinding,
                failure);
        failure = close(
                progressPresentation,
                failure);
        failure = close(
                progressSetting,
                failure);
        failure = close(
                presentation,
                failure);
        failure = close(
                moduleRegistration,
                failure);

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
            return append(
                    primary,
                    failure);
        } catch (Exception failure) {
            return append(
                    primary,
                    new IllegalStateException(
                            "speed-mine feature close failed",
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
            primary.addSuppressed(
                    cleanupFailure);
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
