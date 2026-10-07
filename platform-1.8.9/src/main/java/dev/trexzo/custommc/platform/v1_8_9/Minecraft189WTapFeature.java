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

final class Minecraft189WTapFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189WTapModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration requireGroundSetting;
    private final SettingRegistry.Registration cooldownSetting;
    private final SettingPresentationRegistry.Registration requireGroundPresentation;
    private final SettingPresentationRegistry.Registration cooldownPresentation;
    private final ModuleSettingRegistry.Registration requireGroundBinding;
    private final ModuleSettingRegistry.Registration cooldownBinding;
    private boolean closed;

    private Minecraft189WTapFeature(
            final ModuleController controller,
            final Minecraft189WTapModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration requireGroundSetting,
            final SettingRegistry.Registration cooldownSetting,
            final SettingPresentationRegistry.Registration requireGroundPresentation,
            final SettingPresentationRegistry.Registration cooldownPresentation,
            final ModuleSettingRegistry.Registration requireGroundBinding,
            final ModuleSettingRegistry.Registration cooldownBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.requireGroundSetting = requireGroundSetting;
        this.cooldownSetting = cooldownSetting;
        this.requireGroundPresentation = requireGroundPresentation;
        this.cooldownPresentation = cooldownPresentation;
        this.requireGroundBinding = requireGroundBinding;
        this.cooldownBinding = cooldownBinding;
    }

    static Minecraft189WTapFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189WTapModule module =
                new Minecraft189WTapModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration requireGroundSetting = null;
        SettingRegistry.Registration cooldownSetting = null;
        SettingPresentationRegistry.Registration requireGroundPresentation = null;
        SettingPresentationRegistry.Registration cooldownPresentation = null;
        ModuleSettingRegistry.Registration requireGroundBinding = null;
        ModuleSettingRegistry.Registration cooldownBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189WTapModule.ID,
                                    "W-Tap",
                                    "Resets sprint for one tick on a fresh physical left-click press.",
                                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID,
                                    50));
            requireGroundSetting =
                    settings.register(
                            module.requireGroundSetting());
            cooldownSetting =
                    settings.register(
                            module.cooldownTicksSetting());
            requireGroundPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WTapModule.REQUIRE_GROUND_SETTING_ID,
                                    "Ground Only",
                                    SettingValueKind.BOOLEAN,
                                    0));
            cooldownPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WTapModule.COOLDOWN_TICKS_SETTING_ID,
                                    "Cooldown",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189WTapModule.MINIMUM_COOLDOWN_TICKS,
                                            Minecraft189WTapModule.MAXIMUM_COOLDOWN_TICKS,
                                            1.0D)));
            requireGroundBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WTapModule.ID,
                                    Minecraft189WTapModule.REQUIRE_GROUND_SETTING_ID,
                                    0));
            cooldownBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WTapModule.ID,
                                    Minecraft189WTapModule.COOLDOWN_TICKS_SETTING_ID,
                                    10));

            return new Minecraft189WTapFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    requireGroundSetting,
                    cooldownSetting,
                    requireGroundPresentation,
                    cooldownPresentation,
                    requireGroundBinding,
                    cooldownBinding);
        } catch (RuntimeException failure) {
            closeQuietly(cooldownBinding, failure);
            closeQuietly(requireGroundBinding, failure);
            closeQuietly(cooldownPresentation, failure);
            closeQuietly(requireGroundPresentation, failure);
            closeQuietly(cooldownSetting, failure);
            closeQuietly(requireGroundSetting, failure);
            if (presentation != null) {
                presentation.close();
            }
            if (moduleRegistration != null) {
                moduleRegistration.close();
            }
            throw failure;
        }
    }

    Minecraft189WTapModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "w-tap feature is closed");
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
                    Minecraft189WTapModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189WTapModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(cooldownBinding, failure);
        failure = close(requireGroundBinding, failure);
        failure = close(cooldownPresentation, failure);
        failure = close(requireGroundPresentation, failure);
        failure = close(cooldownSetting, failure);
        failure = close(requireGroundSetting, failure);

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
                            "w-tap feature close failed",
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
