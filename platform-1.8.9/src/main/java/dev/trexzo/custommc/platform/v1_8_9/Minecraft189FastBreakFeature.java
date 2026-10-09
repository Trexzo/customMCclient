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

final class Minecraft189FastBreakFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FastBreakModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration delaySetting;
    private final SettingRegistry.Registration requireAttackHeldSetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingPresentationRegistry.Registration delayPresentation;
    private final SettingPresentationRegistry.Registration requireAttackHeldPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final ModuleSettingRegistry.Registration delayBinding;
    private final ModuleSettingRegistry.Registration requireAttackHeldBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private final SettingRegistry.Registration airborneOverrideSetting;
    private final SettingRegistry.Registration airborneDelaySetting;
    private final SettingPresentationRegistry.Registration airborneOverridePresentation;
    private final SettingPresentationRegistry.Registration airborneDelayPresentation;
    private final ModuleSettingRegistry.Registration airborneOverrideBinding;
    private final ModuleSettingRegistry.Registration airborneDelayBinding;
    private boolean closed;

    private Minecraft189FastBreakFeature(
            final ModuleController controller,
            final Minecraft189FastBreakModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration delaySetting,
            final SettingRegistry.Registration requireAttackHeldSetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingPresentationRegistry.Registration delayPresentation,
            final SettingPresentationRegistry.Registration requireAttackHeldPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final ModuleSettingRegistry.Registration delayBinding,
            final ModuleSettingRegistry.Registration requireAttackHeldBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding,
            final SettingRegistry.Registration airborneOverrideSetting,
            final SettingRegistry.Registration airborneDelaySetting,
            final SettingPresentationRegistry.Registration airborneOverridePresentation,
            final SettingPresentationRegistry.Registration airborneDelayPresentation,
            final ModuleSettingRegistry.Registration airborneOverrideBinding,
            final ModuleSettingRegistry.Registration airborneDelayBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.delaySetting = delaySetting;
        this.requireAttackHeldSetting = requireAttackHeldSetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.delayPresentation = delayPresentation;
        this.requireAttackHeldPresentation = requireAttackHeldPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.delayBinding = delayBinding;
        this.requireAttackHeldBinding = requireAttackHeldBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
        this.groundOnlySetting = groundOnlySetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
        this.airborneOverrideSetting = airborneOverrideSetting;
        this.airborneDelaySetting = airborneDelaySetting;
        this.airborneOverridePresentation = airborneOverridePresentation;
        this.airborneDelayPresentation = airborneDelayPresentation;
        this.airborneOverrideBinding = airborneOverrideBinding;
        this.airborneDelayBinding = airborneDelayBinding;
    }

    static Minecraft189FastBreakFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189FastBreakModule module =
                new Minecraft189FastBreakModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration delaySetting = null;
        SettingRegistry.Registration requireAttackHeldSetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingPresentationRegistry.Registration delayPresentation = null;
        SettingPresentationRegistry.Registration requireAttackHeldPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        ModuleSettingRegistry.Registration delayBinding = null;
        ModuleSettingRegistry.Registration requireAttackHeldBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        SettingRegistry.Registration airborneOverrideSetting = null;
        SettingRegistry.Registration airborneDelaySetting = null;
        SettingPresentationRegistry.Registration airborneOverridePresentation = null;
        SettingPresentationRegistry.Registration airborneDelayPresentation = null;
        ModuleSettingRegistry.Registration airborneOverrideBinding = null;
        ModuleSettingRegistry.Registration airborneDelayBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189FastBreakModule.ID,
                                    "Fast Break",
                                    "Controls the local block-hit delay.",
                                    Minecraft189FeatureCatalog
                                            .PLAYER_CATEGORY_ID,
                                    10));
            delaySetting =
                    settings.register(
                            module.delaySetting());
            requireAttackHeldSetting = settings.register(
                    module.requireAttackHeldSetting());
            pauseWhileSneakingSetting = settings.register(
                    module.pauseWhileSneakingSetting());
            groundOnlySetting = settings.register(module.groundOnlySetting());
            airborneOverrideSetting = settings.register(
                    module.airborneOverrideSetting());
            airborneDelaySetting = settings.register(
                    module.airborneDelaySetting());
            delayPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189FastBreakModule.DELAY_SETTING_ID,
                                    "Delay",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189FastBreakModule.MINIMUM_DELAY,
                                            Minecraft189FastBreakModule.MAXIMUM_DELAY,
                                            1.0D)));
            requireAttackHeldPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastBreakModule.REQUIRE_ATTACK_HELD_SETTING_ID,
                            "Require Attack Held", SettingValueKind.BOOLEAN, 10));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastBreakModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 20));
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastBreakModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 30));
            airborneOverridePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastBreakModule.AIRBORNE_OVERRIDE_SETTING_ID,
                            "Airborne Override", SettingValueKind.BOOLEAN, 40));
            airborneDelayPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastBreakModule.AIRBORNE_DELAY_SETTING_ID,
                            "Airborne Delay", SettingValueKind.INTEGER, 50,
                            new SettingNumericSpec(
                                    Minecraft189FastBreakModule.MINIMUM_DELAY,
                                    Minecraft189FastBreakModule.MAXIMUM_DELAY, 1.0D)));
            delayBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189FastBreakModule.ID,
                                    Minecraft189FastBreakModule.DELAY_SETTING_ID,
                                    0));

            requireAttackHeldBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastBreakModule.ID,
                            Minecraft189FastBreakModule.REQUIRE_ATTACK_HELD_SETTING_ID, 10));
            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastBreakModule.ID,
                            Minecraft189FastBreakModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 20));

            groundOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastBreakModule.ID,
                            Minecraft189FastBreakModule.GROUND_ONLY_SETTING_ID, 30));

            airborneOverrideBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastBreakModule.ID,
                            Minecraft189FastBreakModule.AIRBORNE_OVERRIDE_SETTING_ID, 40));
            airborneDelayBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastBreakModule.ID,
                            Minecraft189FastBreakModule.AIRBORNE_DELAY_SETTING_ID, 50));

            return new Minecraft189FastBreakFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    delaySetting,
                    requireAttackHeldSetting,
                    pauseWhileSneakingSetting,
                    delayPresentation,
                    requireAttackHeldPresentation,
                    pauseWhileSneakingPresentation,
                    delayBinding,
                    requireAttackHeldBinding,
                    pauseWhileSneakingBinding,
                    groundOnlySetting,
                    groundOnlyPresentation,
                    groundOnlyBinding,
                    airborneOverrideSetting,
                    airborneDelaySetting,
                    airborneOverridePresentation,
                    airborneDelayPresentation,
                    airborneOverrideBinding,
                    airborneDelayBinding);
        } catch (RuntimeException failure) {
            closeQuietly(airborneDelayBinding, failure);
            closeQuietly(airborneOverrideBinding, failure);
            closeQuietly(airborneDelayPresentation, failure);
            closeQuietly(airborneOverridePresentation, failure);
            closeQuietly(airborneDelaySetting, failure);
            closeQuietly(airborneOverrideSetting, failure);
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(groundOnlySetting, failure);
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(requireAttackHeldBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(requireAttackHeldPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(requireAttackHeldSetting, failure);
            closeQuietly(delayBinding, failure);
            closeQuietly(delayPresentation, failure);
            closeQuietly(delaySetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189FastBreakModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "fast-break feature is closed");
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
                    Minecraft189FastBreakModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189FastBreakModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(airborneDelayBinding, failure);
        failure = close(airborneOverrideBinding, failure);
        failure = close(airborneDelayPresentation, failure);
        failure = close(airborneOverridePresentation, failure);
        failure = close(airborneDelaySetting, failure);
        failure = close(airborneOverrideSetting, failure);
        failure = close(groundOnlyBinding, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(groundOnlySetting, failure);
        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(requireAttackHeldBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(requireAttackHeldPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(requireAttackHeldSetting, failure);
        failure = close(delayBinding, failure);
        failure = close(delayPresentation, failure);
        failure = close(delaySetting, failure);
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
                            "fast-break feature close failed",
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
