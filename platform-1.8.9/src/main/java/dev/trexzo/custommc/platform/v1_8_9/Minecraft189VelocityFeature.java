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

final class Minecraft189VelocityFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189VelocityModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration horizontalSetting;
    private final SettingRegistry.Registration verticalSetting;
    private final SettingRegistry.Registration onlyWhileSprintingSetting;
    private final SettingRegistry.Registration airborneOverrideSetting;
    private final SettingRegistry.Registration airborneHorizontalSetting;
    private final SettingRegistry.Registration airborneVerticalSetting;
    private final SettingPresentationRegistry.Registration horizontalPresentation;
    private final SettingPresentationRegistry.Registration verticalPresentation;
    private final SettingPresentationRegistry.Registration onlyWhileSprintingPresentation;
    private final SettingPresentationRegistry.Registration airborneOverridePresentation;
    private final SettingPresentationRegistry.Registration airborneHorizontalPresentation;
    private final SettingPresentationRegistry.Registration airborneVerticalPresentation;
    private final ModuleSettingRegistry.Registration horizontalBinding;
    private final ModuleSettingRegistry.Registration verticalBinding;
    private final ModuleSettingRegistry.Registration onlyWhileSprintingBinding;
    private final ModuleSettingRegistry.Registration airborneOverrideBinding;
    private final ModuleSettingRegistry.Registration airborneHorizontalBinding;
    private final ModuleSettingRegistry.Registration airborneVerticalBinding;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingRegistry.Registration pauseWhileSneakingSetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private final ModuleSettingRegistry.Registration pauseWhileSneakingBinding;
    private final SettingRegistry.Registration airborneOnlySetting;
    private final SettingPresentationRegistry.Registration airborneOnlyPresentation;
    private final ModuleSettingRegistry.Registration airborneOnlyBinding;
    private boolean closed;

    private Minecraft189VelocityFeature(
            final ModuleController controller,
            final Minecraft189VelocityModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration horizontalSetting,
            final SettingRegistry.Registration verticalSetting,
            final SettingRegistry.Registration onlyWhileSprintingSetting,
            final SettingRegistry.Registration airborneOverrideSetting,
            final SettingRegistry.Registration airborneHorizontalSetting,
            final SettingRegistry.Registration airborneVerticalSetting,
            final SettingPresentationRegistry.Registration horizontalPresentation,
            final SettingPresentationRegistry.Registration verticalPresentation,
            final SettingPresentationRegistry.Registration onlyWhileSprintingPresentation,
            final SettingPresentationRegistry.Registration airborneOverridePresentation,
            final SettingPresentationRegistry.Registration airborneHorizontalPresentation,
            final SettingPresentationRegistry.Registration airborneVerticalPresentation,
            final ModuleSettingRegistry.Registration horizontalBinding,
            final ModuleSettingRegistry.Registration verticalBinding,
            final ModuleSettingRegistry.Registration onlyWhileSprintingBinding,
            final ModuleSettingRegistry.Registration airborneOverrideBinding,
            final ModuleSettingRegistry.Registration airborneHorizontalBinding,
            final ModuleSettingRegistry.Registration airborneVerticalBinding,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingRegistry.Registration pauseWhileSneakingSetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final SettingPresentationRegistry.Registration pauseWhileSneakingPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding,
            final ModuleSettingRegistry.Registration pauseWhileSneakingBinding,
            final SettingRegistry.Registration airborneOnlySetting,
            final SettingPresentationRegistry.Registration airborneOnlyPresentation,
            final ModuleSettingRegistry.Registration airborneOnlyBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.horizontalSetting = horizontalSetting;
        this.verticalSetting = verticalSetting;
        this.onlyWhileSprintingSetting = onlyWhileSprintingSetting;
        this.airborneOverrideSetting = airborneOverrideSetting;
        this.airborneHorizontalSetting = airborneHorizontalSetting;
        this.airborneVerticalSetting = airborneVerticalSetting;
        this.horizontalPresentation = horizontalPresentation;
        this.verticalPresentation = verticalPresentation;
        this.onlyWhileSprintingPresentation = onlyWhileSprintingPresentation;
        this.airborneOverridePresentation = airborneOverridePresentation;
        this.airborneHorizontalPresentation = airborneHorizontalPresentation;
        this.airborneVerticalPresentation = airborneVerticalPresentation;
        this.horizontalBinding = horizontalBinding;
        this.verticalBinding = verticalBinding;
        this.onlyWhileSprintingBinding = onlyWhileSprintingBinding;
        this.airborneOverrideBinding = airborneOverrideBinding;
        this.airborneHorizontalBinding = airborneHorizontalBinding;
        this.airborneVerticalBinding = airborneVerticalBinding;
        this.groundOnlySetting = groundOnlySetting;
        this.pauseWhileSneakingSetting = pauseWhileSneakingSetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.pauseWhileSneakingPresentation = pauseWhileSneakingPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
        this.pauseWhileSneakingBinding = pauseWhileSneakingBinding;
        this.airborneOnlySetting = airborneOnlySetting;
        this.airborneOnlyPresentation = airborneOnlyPresentation;
        this.airborneOnlyBinding = airborneOnlyBinding;
    }

    static Minecraft189VelocityFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189VelocityModule module =
                new Minecraft189VelocityModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration horizontalSetting = null;
        SettingRegistry.Registration verticalSetting = null;
        SettingRegistry.Registration onlyWhileSprintingSetting = null;
        SettingRegistry.Registration airborneOverrideSetting = null;
        SettingRegistry.Registration airborneHorizontalSetting = null;
        SettingRegistry.Registration airborneVerticalSetting = null;
        SettingPresentationRegistry.Registration horizontalPresentation = null;
        SettingPresentationRegistry.Registration verticalPresentation = null;
        SettingPresentationRegistry.Registration onlyWhileSprintingPresentation = null;
        SettingPresentationRegistry.Registration airborneOverridePresentation = null;
        SettingPresentationRegistry.Registration airborneHorizontalPresentation = null;
        SettingPresentationRegistry.Registration airborneVerticalPresentation = null;
        ModuleSettingRegistry.Registration horizontalBinding = null;
        ModuleSettingRegistry.Registration verticalBinding = null;
        ModuleSettingRegistry.Registration onlyWhileSprintingBinding = null;
        ModuleSettingRegistry.Registration airborneOverrideBinding = null;
        ModuleSettingRegistry.Registration airborneHorizontalBinding = null;
        ModuleSettingRegistry.Registration airborneVerticalBinding = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingRegistry.Registration pauseWhileSneakingSetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        SettingPresentationRegistry.Registration pauseWhileSneakingPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        ModuleSettingRegistry.Registration pauseWhileSneakingBinding = null;
        SettingRegistry.Registration airborneOnlySetting = null;
        SettingPresentationRegistry.Registration airborneOnlyPresentation = null;
        ModuleSettingRegistry.Registration airborneOnlyBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189VelocityModule.ID,
                                    "Velocity",
                                    "Scales incoming horizontal and vertical knockback from 0% cancellation through 200% amplification.",
                                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID,
                                    20));

            horizontalSetting =
                    settings.register(
                            module.horizontalPercentSetting());
            verticalSetting =
                    settings.register(
                            module.verticalPercentSetting());
            onlyWhileSprintingSetting = settings.register(
                    module.onlyWhileSprintingSetting());
            airborneOverrideSetting = settings.register(
                    module.airborneOverrideSetting());
            airborneHorizontalSetting = settings.register(
                    module.airborneHorizontalPercentSetting());
            airborneVerticalSetting = settings.register(
                    module.airborneVerticalPercentSetting());
            groundOnlySetting = settings.register(module.groundOnlySetting());
            airborneOnlySetting = settings.register(module.airborneOnlySetting());
            pauseWhileSneakingSetting = settings.register(
                    module.pauseWhileSneakingSetting());

            horizontalPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189VelocityModule.HORIZONTAL_SETTING_ID,
                                    "Horizontal %",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189VelocityModule.MINIMUM_PERCENT,
                                            Minecraft189VelocityModule.MAXIMUM_PERCENT,
                                            5.0D)));
            verticalPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189VelocityModule.VERTICAL_SETTING_ID,
                                    "Vertical %",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189VelocityModule.MINIMUM_PERCENT,
                                            Minecraft189VelocityModule.MAXIMUM_PERCENT,
                                            5.0D)));

            onlyWhileSprintingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189VelocityModule.ONLY_WHILE_SPRINTING_SETTING_ID,
                            "Only While Sprinting", SettingValueKind.BOOLEAN, 20));

            airborneOverridePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189VelocityModule.AIRBORNE_OVERRIDE_SETTING_ID,
                            "Airborne Override", SettingValueKind.BOOLEAN, 30));
            airborneHorizontalPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189VelocityModule.AIRBORNE_HORIZONTAL_SETTING_ID,
                            "Air Horizontal %", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(
                                    Minecraft189VelocityModule.MINIMUM_PERCENT,
                                    Minecraft189VelocityModule.MAXIMUM_PERCENT,
                                    5.0D)));
            airborneVerticalPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189VelocityModule.AIRBORNE_VERTICAL_SETTING_ID,
                            "Air Vertical %", SettingValueKind.INTEGER, 50,
                            new SettingNumericSpec(
                                    Minecraft189VelocityModule.MINIMUM_PERCENT,
                                    Minecraft189VelocityModule.MAXIMUM_PERCENT,
                                    5.0D)));

            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189VelocityModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 60));
            pauseWhileSneakingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189VelocityModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 70));
            airborneOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189VelocityModule.AIRBORNE_ONLY_SETTING_ID,
                            "Airborne Only", SettingValueKind.BOOLEAN, 80));
            horizontalBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189VelocityModule.ID,
                                    Minecraft189VelocityModule.HORIZONTAL_SETTING_ID,
                                    0));
            verticalBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189VelocityModule.ID,
                                    Minecraft189VelocityModule.VERTICAL_SETTING_ID,
                                    10));

            onlyWhileSprintingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189VelocityModule.ID,
                            Minecraft189VelocityModule.ONLY_WHILE_SPRINTING_SETTING_ID, 20));

            airborneOverrideBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189VelocityModule.ID,
                            Minecraft189VelocityModule.AIRBORNE_OVERRIDE_SETTING_ID, 30));
            airborneHorizontalBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189VelocityModule.ID,
                            Minecraft189VelocityModule.AIRBORNE_HORIZONTAL_SETTING_ID, 40));
            airborneVerticalBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189VelocityModule.ID,
                            Minecraft189VelocityModule.AIRBORNE_VERTICAL_SETTING_ID, 50));

            groundOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189VelocityModule.ID,
                            Minecraft189VelocityModule.GROUND_ONLY_SETTING_ID, 60));
            pauseWhileSneakingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189VelocityModule.ID,
                            Minecraft189VelocityModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 70));

            airborneOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189VelocityModule.ID,
                            Minecraft189VelocityModule.AIRBORNE_ONLY_SETTING_ID, 80));

            return new Minecraft189VelocityFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    horizontalSetting,
                    verticalSetting,
                    onlyWhileSprintingSetting,
                    airborneOverrideSetting,
                    airborneHorizontalSetting,
                    airborneVerticalSetting,
                    horizontalPresentation,
                    verticalPresentation,
                    onlyWhileSprintingPresentation,
                    airborneOverridePresentation,
                    airborneHorizontalPresentation,
                    airborneVerticalPresentation,
                    horizontalBinding,
                    verticalBinding,
                    onlyWhileSprintingBinding,
                    airborneOverrideBinding,
                    airborneHorizontalBinding,
                    airborneVerticalBinding,
                    groundOnlySetting,
                    pauseWhileSneakingSetting,
                    groundOnlyPresentation,
                    pauseWhileSneakingPresentation,
                    groundOnlyBinding,
                    pauseWhileSneakingBinding,
                    airborneOnlySetting,
                    airborneOnlyPresentation,
                    airborneOnlyBinding);
        } catch (RuntimeException failure) {
            closeQuietly(airborneOnlyBinding, failure);
            closeQuietly(airborneOnlyPresentation, failure);
            closeQuietly(airborneOnlySetting, failure);
            closeQuietly(pauseWhileSneakingBinding, failure);
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(pauseWhileSneakingPresentation, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(pauseWhileSneakingSetting, failure);
            closeQuietly(groundOnlySetting, failure);
            closeQuietly(airborneVerticalBinding, failure);
            closeQuietly(airborneHorizontalBinding, failure);
            closeQuietly(airborneOverrideBinding, failure);
            closeQuietly(airborneVerticalPresentation, failure);
            closeQuietly(airborneHorizontalPresentation, failure);
            closeQuietly(airborneOverridePresentation, failure);
            closeQuietly(airborneVerticalSetting, failure);
            closeQuietly(airborneHorizontalSetting, failure);
            closeQuietly(airborneOverrideSetting, failure);
            closeQuietly(onlyWhileSprintingBinding, failure);
            closeQuietly(verticalBinding, failure);
            closeQuietly(horizontalBinding, failure);
            closeQuietly(onlyWhileSprintingPresentation, failure);
            closeQuietly(verticalPresentation, failure);
            closeQuietly(horizontalPresentation, failure);
            closeQuietly(onlyWhileSprintingSetting, failure);
            closeQuietly(verticalSetting, failure);
            closeQuietly(horizontalSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189VelocityModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "velocity feature is closed");
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
                    Minecraft189VelocityModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189VelocityModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(airborneOnlyBinding, failure);
        failure = close(airborneOnlyPresentation, failure);
        failure = close(airborneOnlySetting, failure);
        failure = close(pauseWhileSneakingBinding, failure);
        failure = close(groundOnlyBinding, failure);
        failure = close(pauseWhileSneakingPresentation, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(pauseWhileSneakingSetting, failure);
        failure = close(groundOnlySetting, failure);
        failure = close(airborneVerticalBinding, failure);
        failure = close(airborneHorizontalBinding, failure);
        failure = close(airborneOverrideBinding, failure);
        failure = close(airborneVerticalPresentation, failure);
        failure = close(airborneHorizontalPresentation, failure);
        failure = close(airborneOverridePresentation, failure);
        failure = close(airborneVerticalSetting, failure);
        failure = close(airborneHorizontalSetting, failure);
        failure = close(airborneOverrideSetting, failure);
        failure = close(onlyWhileSprintingBinding, failure);
        failure = close(verticalBinding, failure);
        failure = close(horizontalBinding, failure);
        failure = close(onlyWhileSprintingPresentation, failure);
        failure = close(verticalPresentation, failure);
        failure = close(horizontalPresentation, failure);
        failure = close(onlyWhileSprintingSetting, failure);
        failure = close(verticalSetting, failure);
        failure = close(horizontalSetting, failure);
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
                            "velocity feature close failed",
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
