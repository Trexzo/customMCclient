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

final class Minecraft189NoHitDelayFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189NoHitDelayModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration delaySetting;
    private final SettingPresentationRegistry.Registration delayPresentation;
    private final ModuleSettingRegistry.Registration delayBinding;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingRegistry.Registration pauseSneakingSetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final SettingPresentationRegistry.Registration pauseSneakingPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private final ModuleSettingRegistry.Registration pauseSneakingBinding;
    private final SettingRegistry.Registration requireAttackHeldSetting;
    private final SettingPresentationRegistry.Registration requireAttackHeldPresentation;
    private final ModuleSettingRegistry.Registration requireAttackHeldBinding;
    private boolean closed;

    private Minecraft189NoHitDelayFeature(
            final ModuleController controller,
            final Minecraft189NoHitDelayModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration delaySetting,
            final SettingPresentationRegistry.Registration delayPresentation,
            final ModuleSettingRegistry.Registration delayBinding,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingRegistry.Registration pauseSneakingSetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final SettingPresentationRegistry.Registration pauseSneakingPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding,
            final ModuleSettingRegistry.Registration pauseSneakingBinding,
            final SettingRegistry.Registration requireAttackHeldSetting,
            final SettingPresentationRegistry.Registration requireAttackHeldPresentation,
            final ModuleSettingRegistry.Registration requireAttackHeldBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.delaySetting = delaySetting;
        this.delayPresentation = delayPresentation;
        this.delayBinding = delayBinding;
        this.groundOnlySetting = groundOnlySetting;
        this.pauseSneakingSetting = pauseSneakingSetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.pauseSneakingPresentation = pauseSneakingPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
        this.pauseSneakingBinding = pauseSneakingBinding;
        this.requireAttackHeldSetting = requireAttackHeldSetting;
        this.requireAttackHeldPresentation = requireAttackHeldPresentation;
        this.requireAttackHeldBinding = requireAttackHeldBinding;
    }

    static Minecraft189NoHitDelayFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189NoHitDelayModule module =
                new Minecraft189NoHitDelayModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration delaySetting = null;
        SettingPresentationRegistry.Registration delayPresentation = null;
        ModuleSettingRegistry.Registration delayBinding = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingRegistry.Registration pauseSneakingSetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        SettingPresentationRegistry.Registration pauseSneakingPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        ModuleSettingRegistry.Registration pauseSneakingBinding = null;
        SettingRegistry.Registration requireAttackHeldSetting = null;
        SettingPresentationRegistry.Registration requireAttackHeldPresentation = null;
        ModuleSettingRegistry.Registration requireAttackHeldBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189NoHitDelayModule.ID,
                                    "No Hit Delay",
                                    "Controls the positive vanilla left-click cooldown counter.",
                                    Minecraft189FeatureCatalog
                                            .COMBAT_CATEGORY_ID,
                                    0));
            delaySetting =
                    settings.register(
                            module.delaySetting());
            groundOnlySetting = settings.register(module.groundOnlySetting());
            pauseSneakingSetting = settings.register(
                    module.pauseWhileSneakingSetting());
            requireAttackHeldSetting = settings.register(
                    module.requireAttackHeldSetting());
            delayPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189NoHitDelayModule.DELAY_SETTING_ID,
                                    "Delay",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189NoHitDelayModule.MINIMUM_DELAY,
                                            Minecraft189NoHitDelayModule.MAXIMUM_DELAY,
                                            1.0D)));
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoHitDelayModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 10));
            pauseSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoHitDelayModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 20));
            requireAttackHeldPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NoHitDelayModule.REQUIRE_ATTACK_HELD_SETTING_ID,
                            "Require Attack Held", SettingValueKind.BOOLEAN, 30));
            delayBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189NoHitDelayModule.ID,
                                    Minecraft189NoHitDelayModule.DELAY_SETTING_ID,
                                    0));

            groundOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoHitDelayModule.ID,
                            Minecraft189NoHitDelayModule.GROUND_ONLY_SETTING_ID, 10));
            pauseSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoHitDelayModule.ID,
                            Minecraft189NoHitDelayModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 20));

            requireAttackHeldBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NoHitDelayModule.ID,
                            Minecraft189NoHitDelayModule.REQUIRE_ATTACK_HELD_SETTING_ID, 30));

            return new Minecraft189NoHitDelayFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    delaySetting,
                    delayPresentation,
                    delayBinding,
                    groundOnlySetting,
                    pauseSneakingSetting,
                    groundOnlyPresentation,
                    pauseSneakingPresentation,
                    groundOnlyBinding,
                    pauseSneakingBinding,
                    requireAttackHeldSetting,
                    requireAttackHeldPresentation,
                    requireAttackHeldBinding);
        } catch (RuntimeException failure) {
            closeQuietly(requireAttackHeldBinding, failure);
            closeQuietly(requireAttackHeldPresentation, failure);
            closeQuietly(requireAttackHeldSetting, failure);
            closeQuietly(pauseSneakingBinding, failure);
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(delayBinding, failure);
            closeQuietly(pauseSneakingPresentation, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(delayPresentation, failure);
            closeQuietly(pauseSneakingSetting, failure);
            closeQuietly(groundOnlySetting, failure);
            closeQuietly(delaySetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189NoHitDelayModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "no-hit-delay feature is closed");
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
                    Minecraft189NoHitDelayModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189NoHitDelayModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(requireAttackHeldBinding, failure);
        failure = close(requireAttackHeldPresentation, failure);
        failure = close(requireAttackHeldSetting, failure);
        failure = close(pauseSneakingBinding, failure);
        failure = close(groundOnlyBinding, failure);
        failure = close(delayBinding, failure);
        failure = close(pauseSneakingPresentation, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(delayPresentation, failure);
        failure = close(pauseSneakingSetting, failure);
        failure = close(groundOnlySetting, failure);
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
                            "no-hit-delay feature close failed",
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
