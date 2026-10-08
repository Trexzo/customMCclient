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

final class Minecraft189DamageBoostFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189DamageBoostModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration multiplierSetting;
    private final SettingRegistry.Registration verticalMultiplierSetting;
    private final SettingPresentationRegistry.Registration multiplierPresentation;
    private final SettingPresentationRegistry.Registration verticalMultiplierPresentation;
    private final ModuleSettingRegistry.Registration multiplierBinding;
    private final ModuleSettingRegistry.Registration verticalMultiplierBinding;
    private final SettingRegistry.Registration capHorizontalSetting;
    private final SettingRegistry.Registration maxHorizontalSpeedSetting;
    private final SettingPresentationRegistry.Registration capHorizontalPresentation;
    private final SettingPresentationRegistry.Registration maxHorizontalSpeedPresentation;
    private final ModuleSettingRegistry.Registration capHorizontalBinding;
    private final ModuleSettingRegistry.Registration maxHorizontalSpeedBinding;
    private boolean closed;

    private Minecraft189DamageBoostFeature(
            final ModuleController controller,
            final Minecraft189DamageBoostModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration multiplierSetting,
            final SettingRegistry.Registration verticalMultiplierSetting,
            final SettingPresentationRegistry.Registration multiplierPresentation,
            final SettingPresentationRegistry.Registration verticalMultiplierPresentation,
            final ModuleSettingRegistry.Registration multiplierBinding,
            final ModuleSettingRegistry.Registration verticalMultiplierBinding,
            final SettingRegistry.Registration capHorizontalSetting,
            final SettingRegistry.Registration maxHorizontalSpeedSetting,
            final SettingPresentationRegistry.Registration capHorizontalPresentation,
            final SettingPresentationRegistry.Registration maxHorizontalSpeedPresentation,
            final ModuleSettingRegistry.Registration capHorizontalBinding,
            final ModuleSettingRegistry.Registration maxHorizontalSpeedBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.multiplierSetting = multiplierSetting;
        this.verticalMultiplierSetting = verticalMultiplierSetting;
        this.multiplierPresentation = multiplierPresentation;
        this.verticalMultiplierPresentation = verticalMultiplierPresentation;
        this.multiplierBinding = multiplierBinding;
        this.verticalMultiplierBinding = verticalMultiplierBinding;
        this.capHorizontalSetting = capHorizontalSetting;
        this.maxHorizontalSpeedSetting = maxHorizontalSpeedSetting;
        this.capHorizontalPresentation = capHorizontalPresentation;
        this.maxHorizontalSpeedPresentation = maxHorizontalSpeedPresentation;
        this.capHorizontalBinding = capHorizontalBinding;
        this.maxHorizontalSpeedBinding = maxHorizontalSpeedBinding;
    }

    static Minecraft189DamageBoostFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189DamageBoostModule module =
                new Minecraft189DamageBoostModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration multiplierSetting = null;
        SettingRegistry.Registration verticalMultiplierSetting = null;
        SettingPresentationRegistry.Registration multiplierPresentation = null;
        SettingPresentationRegistry.Registration verticalMultiplierPresentation = null;
        ModuleSettingRegistry.Registration multiplierBinding = null;
        ModuleSettingRegistry.Registration verticalMultiplierBinding = null;
        SettingRegistry.Registration capHorizontalSetting = null;
        SettingRegistry.Registration maxHorizontalSpeedSetting = null;
        SettingPresentationRegistry.Registration capHorizontalPresentation = null;
        SettingPresentationRegistry.Registration maxHorizontalSpeedPresentation = null;
        ModuleSettingRegistry.Registration capHorizontalBinding = null;
        ModuleSettingRegistry.Registration maxHorizontalSpeedBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189DamageBoostModule.ID,
                                    "Damage Boost",
                                    "Boosts horizontal and optional vertical motion once when local hurt time resets on a fresh hit.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    210));
            multiplierSetting =
                    settings.register(
                            module.multiplierSetting());
            verticalMultiplierSetting =
                    settings.register(
                            module.verticalMultiplierSetting());
            capHorizontalSetting = settings.register(module.capHorizontalSetting());
            maxHorizontalSpeedSetting = settings.register(
                    module.maxHorizontalSpeedSetting());
            multiplierPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189DamageBoostModule.MULTIPLIER_SETTING_ID,
                                    "Horizontal",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189DamageBoostModule.MINIMUM_MULTIPLIER,
                                            Minecraft189DamageBoostModule.MAXIMUM_MULTIPLIER,
                                            0.05D)));
            verticalMultiplierPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189DamageBoostModule.VERTICAL_MULTIPLIER_SETTING_ID,
                                    "Vertical",
                                    SettingValueKind.DOUBLE,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189DamageBoostModule.MINIMUM_MULTIPLIER,
                                            Minecraft189DamageBoostModule.MAXIMUM_MULTIPLIER,
                                            0.05D)));
            capHorizontalPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189DamageBoostModule.CAP_HORIZONTAL_SETTING_ID,
                            "Cap Horizontal Speed", SettingValueKind.BOOLEAN, 20));
            maxHorizontalSpeedPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189DamageBoostModule.MAX_HORIZONTAL_SPEED_SETTING_ID,
                            "Max Horizontal Speed", SettingValueKind.DOUBLE, 30,
                            new SettingNumericSpec(
                                    Minecraft189DamageBoostModule.MINIMUM_MAX_HORIZONTAL_SPEED,
                                    Minecraft189DamageBoostModule.MAXIMUM_MAX_HORIZONTAL_SPEED,
                                    0.05D)));
            multiplierBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189DamageBoostModule.ID,
                                    Minecraft189DamageBoostModule.MULTIPLIER_SETTING_ID,
                                    0));
            verticalMultiplierBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189DamageBoostModule.ID,
                                    Minecraft189DamageBoostModule.VERTICAL_MULTIPLIER_SETTING_ID,
                                    10));

            capHorizontalBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189DamageBoostModule.ID,
                            Minecraft189DamageBoostModule.CAP_HORIZONTAL_SETTING_ID, 20));
            maxHorizontalSpeedBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189DamageBoostModule.ID,
                            Minecraft189DamageBoostModule.MAX_HORIZONTAL_SPEED_SETTING_ID, 30));

            return new Minecraft189DamageBoostFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    multiplierSetting,
                    verticalMultiplierSetting,
                    multiplierPresentation,
                    verticalMultiplierPresentation,
                    multiplierBinding,
                    verticalMultiplierBinding,
                    capHorizontalSetting,
                    maxHorizontalSpeedSetting,
                    capHorizontalPresentation,
                    maxHorizontalSpeedPresentation,
                    capHorizontalBinding,
                    maxHorizontalSpeedBinding);
        } catch (RuntimeException failure) {
            closeQuietly(maxHorizontalSpeedBinding, failure);
            closeQuietly(capHorizontalBinding, failure);
            closeQuietly(maxHorizontalSpeedPresentation, failure);
            closeQuietly(capHorizontalPresentation, failure);
            closeQuietly(maxHorizontalSpeedSetting, failure);
            closeQuietly(capHorizontalSetting, failure);
            closeQuietly(verticalMultiplierBinding, failure);
            closeQuietly(multiplierBinding, failure);
            closeQuietly(verticalMultiplierPresentation, failure);
            closeQuietly(multiplierPresentation, failure);
            closeQuietly(verticalMultiplierSetting, failure);
            closeQuietly(multiplierSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189DamageBoostModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "damage-boost feature is closed");
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
                    Minecraft189DamageBoostModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189DamageBoostModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(maxHorizontalSpeedBinding, failure);
        failure = close(capHorizontalBinding, failure);
        failure = close(maxHorizontalSpeedPresentation, failure);
        failure = close(capHorizontalPresentation, failure);
        failure = close(maxHorizontalSpeedSetting, failure);
        failure = close(capHorizontalSetting, failure);
        failure = close(verticalMultiplierBinding, failure);
        failure = close(multiplierBinding, failure);
        failure = close(verticalMultiplierPresentation, failure);
        failure = close(multiplierPresentation, failure);
        failure = close(verticalMultiplierSetting, failure);
        failure = close(multiplierSetting, failure);
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
                            "damage-boost feature close failed",
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
