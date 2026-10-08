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

final class Minecraft189FastPlaceFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FastPlaceModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration delaySetting;
    private final SettingRegistry.Registration requireUseHeldSetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingPresentationRegistry.Registration delayPresentation;
    private final SettingPresentationRegistry.Registration requireUseHeldPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final ModuleSettingRegistry.Registration delayBinding;
    private final ModuleSettingRegistry.Registration requireUseHeldBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private final SettingRegistry.Registration airborneOverrideSetting;
    private final SettingRegistry.Registration airborneDelaySetting;
    private final SettingPresentationRegistry.Registration airborneOverridePresentation;
    private final SettingPresentationRegistry.Registration airborneDelayPresentation;
    private final ModuleSettingRegistry.Registration airborneOverrideBinding;
    private final ModuleSettingRegistry.Registration airborneDelayBinding;
    private boolean closed;

    private Minecraft189FastPlaceFeature(
            final ModuleController controller,
            final Minecraft189FastPlaceModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration delaySetting,
            final SettingRegistry.Registration requireUseHeldSetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingPresentationRegistry.Registration delayPresentation,
            final SettingPresentationRegistry.Registration requireUseHeldPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final ModuleSettingRegistry.Registration delayBinding,
            final ModuleSettingRegistry.Registration requireUseHeldBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding,
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
        this.requireUseHeldSetting = requireUseHeldSetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.delayPresentation = delayPresentation;
        this.requireUseHeldPresentation = requireUseHeldPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.delayBinding = delayBinding;
        this.requireUseHeldBinding = requireUseHeldBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
        this.airborneOverrideSetting = airborneOverrideSetting;
        this.airborneDelaySetting = airborneDelaySetting;
        this.airborneOverridePresentation = airborneOverridePresentation;
        this.airborneDelayPresentation = airborneDelayPresentation;
        this.airborneOverrideBinding = airborneOverrideBinding;
        this.airborneDelayBinding = airborneDelayBinding;
    }

    static Minecraft189FastPlaceFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189FastPlaceModule module =
                new Minecraft189FastPlaceModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration delaySetting = null;
        SettingRegistry.Registration requireUseHeldSetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingPresentationRegistry.Registration delayPresentation = null;
        SettingPresentationRegistry.Registration requireUseHeldPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        ModuleSettingRegistry.Registration delayBinding = null;
        ModuleSettingRegistry.Registration requireUseHeldBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
        SettingRegistry.Registration airborneOverrideSetting = null;
        SettingRegistry.Registration airborneDelaySetting = null;
        SettingPresentationRegistry.Registration airborneOverridePresentation = null;
        SettingPresentationRegistry.Registration airborneDelayPresentation = null;
        ModuleSettingRegistry.Registration airborneOverrideBinding = null;
        ModuleSettingRegistry.Registration airborneDelayBinding = null;

        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189FastPlaceModule.ID,
                                    "Fast Place",
                                    "Reduces the vanilla right-click placement delay.",
                                    Minecraft189FeatureCatalog
                                            .PLAYER_CATEGORY_ID,
                                    0));
            delaySetting =
                    settings.register(
                            module.delayTicksSetting());
            requireUseHeldSetting = settings.register(module.requireUseHeldSetting());
            pauseWhileSneakingSetting = settings.register(module.pauseWhileSneakingSetting());
            airborneOverrideSetting = settings.register(module.airborneOverrideSetting());
            airborneDelaySetting = settings.register(module.airborneDelaySetting());
            delayPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189FastPlaceModule.DELAY_SETTING_ID,
                                    "Delay",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4.0D,
                                            1.0D)));
            requireUseHeldPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastPlaceModule.REQUIRE_USE_HELD_SETTING_ID,
                            "Require Use Held", SettingValueKind.BOOLEAN, 10));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastPlaceModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 20));
            airborneOverridePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastPlaceModule.AIRBORNE_OVERRIDE_SETTING_ID,
                            "Airborne Override", SettingValueKind.BOOLEAN, 30));
            airborneDelayPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FastPlaceModule.AIRBORNE_DELAY_SETTING_ID,
                            "Airborne Delay", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(0.0D, 4.0D, 1.0D)));
            delayBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189FastPlaceModule.ID,
                                    Minecraft189FastPlaceModule.DELAY_SETTING_ID,
                                    0));

            requireUseHeldBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastPlaceModule.ID,
                            Minecraft189FastPlaceModule.REQUIRE_USE_HELD_SETTING_ID, 10));
            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastPlaceModule.ID,
                            Minecraft189FastPlaceModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 20));

            airborneOverrideBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastPlaceModule.ID,
                            Minecraft189FastPlaceModule.AIRBORNE_OVERRIDE_SETTING_ID, 30));
            airborneDelayBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FastPlaceModule.ID,
                            Minecraft189FastPlaceModule.AIRBORNE_DELAY_SETTING_ID, 40));

            return new Minecraft189FastPlaceFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    delaySetting,
                    requireUseHeldSetting,
                    pauseWhileSneakingSetting,
                    delayPresentation,
                    requireUseHeldPresentation,
                    pauseWhileSneakingPresentation,
                    delayBinding,
                    requireUseHeldBinding,
                    pauseWhileSneakingBinding,
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
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(requireUseHeldBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(requireUseHeldPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(requireUseHeldSetting, failure);
            closeQuietly(
                    delayBinding,
                    failure);
            closeQuietly(
                    delayPresentation,
                    failure);
            closeQuietly(
                    delaySetting,
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

    Minecraft189FastPlaceModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "fast-place feature is closed");
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
                    Minecraft189FastPlaceModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189FastPlaceModule.ID);
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
        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(requireUseHeldBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(requireUseHeldPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(requireUseHeldSetting, failure);
        failure = close(
                delayBinding,
                failure);
        failure = close(
                delayPresentation,
                failure);
        failure = close(
                delaySetting,
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
                            "fast-place feature close failed",
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
