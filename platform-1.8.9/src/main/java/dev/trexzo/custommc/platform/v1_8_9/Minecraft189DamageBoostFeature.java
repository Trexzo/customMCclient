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
    private final SettingPresentationRegistry.Registration multiplierPresentation;
    private final ModuleSettingRegistry.Registration multiplierBinding;
    private boolean closed;

    private Minecraft189DamageBoostFeature(
            final ModuleController controller,
            final Minecraft189DamageBoostModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration multiplierSetting,
            final SettingPresentationRegistry.Registration multiplierPresentation,
            final ModuleSettingRegistry.Registration multiplierBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.multiplierSetting = multiplierSetting;
        this.multiplierPresentation = multiplierPresentation;
        this.multiplierBinding = multiplierBinding;
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
        SettingPresentationRegistry.Registration multiplierPresentation = null;
        ModuleSettingRegistry.Registration multiplierBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189DamageBoostModule.ID,
                                    "Damage Boost",
                                    "Boosts horizontal motion once when local hurt time resets on a fresh hit.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    210));
            multiplierSetting =
                    settings.register(
                            module.multiplierSetting());
            multiplierPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189DamageBoostModule.MULTIPLIER_SETTING_ID,
                                    "Multiplier",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189DamageBoostModule.MINIMUM_MULTIPLIER,
                                            Minecraft189DamageBoostModule.MAXIMUM_MULTIPLIER,
                                            0.05D)));
            multiplierBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189DamageBoostModule.ID,
                                    Minecraft189DamageBoostModule.MULTIPLIER_SETTING_ID,
                                    0));

            return new Minecraft189DamageBoostFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    multiplierSetting,
                    multiplierPresentation,
                    multiplierBinding);
        } catch (RuntimeException failure) {
            closeQuietly(multiplierBinding, failure);
            closeQuietly(multiplierPresentation, failure);
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

        failure = close(multiplierBinding, failure);
        failure = close(multiplierPresentation, failure);
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
