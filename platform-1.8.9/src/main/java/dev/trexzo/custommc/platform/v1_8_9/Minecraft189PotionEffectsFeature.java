package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

final class Minecraft189PotionEffectsFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189PotionEffectsModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingRegistry.Registration sortByExpirySetting;
    private final SettingRegistry.Registration expiryAlertSetting;
    private final SettingRegistry.Registration expiryThresholdSetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final SettingPresentationRegistry.Registration sortByExpiryPresentation;
    private final SettingPresentationRegistry.Registration expiryAlertPresentation;
    private final SettingPresentationRegistry.Registration expiryThresholdPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final ModuleSettingRegistry.Registration sortByExpiryBinding;
    private final ModuleSettingRegistry.Registration expiryAlertBinding;
    private final ModuleSettingRegistry.Registration expiryThresholdBinding;
    private boolean closed;

    private Minecraft189PotionEffectsFeature(
            final ModuleController controller,
            final Minecraft189PotionEffectsModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingRegistry.Registration sortByExpirySetting,
            final SettingRegistry.Registration expiryAlertSetting,
            final SettingRegistry.Registration expiryThresholdSetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final SettingPresentationRegistry.Registration sortByExpiryPresentation,
            final SettingPresentationRegistry.Registration expiryAlertPresentation,
            final SettingPresentationRegistry.Registration expiryThresholdPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final ModuleSettingRegistry.Registration sortByExpiryBinding,
            final ModuleSettingRegistry.Registration expiryAlertBinding,
            final ModuleSettingRegistry.Registration expiryThresholdBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.sortByExpirySetting = sortByExpirySetting;
        this.expiryAlertSetting = expiryAlertSetting;
        this.expiryThresholdSetting = expiryThresholdSetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.sortByExpiryPresentation = sortByExpiryPresentation;
        this.expiryAlertPresentation = expiryAlertPresentation;
        this.expiryThresholdPresentation = expiryThresholdPresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.sortByExpiryBinding = sortByExpiryBinding;
        this.expiryAlertBinding = expiryAlertBinding;
        this.expiryThresholdBinding = expiryThresholdBinding;
    }

    static Minecraft189PotionEffectsFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189PlayerPotionEffectsState potionEffectsState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189PotionEffectsModule module =
                new Minecraft189PotionEffectsModule(
                        potionEffectsState,
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingRegistry.Registration sortByExpirySetting = null;
        SettingRegistry.Registration expiryAlertSetting = null;
        SettingRegistry.Registration expiryThresholdSetting = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        SettingPresentationRegistry.Registration sortByExpiryPresentation = null;
        SettingPresentationRegistry.Registration expiryAlertPresentation = null;
        SettingPresentationRegistry.Registration expiryThresholdPresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        ModuleSettingRegistry.Registration sortByExpiryBinding = null;
        ModuleSettingRegistry.Registration expiryAlertBinding = null;
        ModuleSettingRegistry.Registration expiryThresholdBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189PotionEffectsModule.ID,
                                    "Potion Effects",
                                    "Shows active potion-effect names, levels and durations.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    61));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            sortByExpirySetting = settings.register(module.sortByExpirySetting());
            expiryAlertSetting = settings.register(module.expiryAlertSetting());
            expiryThresholdSetting = settings.register(
                    module.expiryThresholdSecondsSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189PotionEffectsModule.X_SETTING_ID,
                                    "X",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            yPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189PotionEffectsModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            sortByExpiryPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189PotionEffectsModule.SORT_BY_EXPIRY_SETTING_ID,
                            "Soonest First", SettingValueKind.BOOLEAN, 20));
            expiryAlertPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189PotionEffectsModule.EXPIRY_ALERT_SETTING_ID,
                            "Expiry Alert", SettingValueKind.BOOLEAN, 30));
            expiryThresholdPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189PotionEffectsModule.EXPIRY_THRESHOLD_SETTING_ID,
                            "Alert Within (Seconds)", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(
                                    1.0D,
                                    Minecraft189PotionEffectsModule.MAXIMUM_EXPIRY_THRESHOLD_SECONDS,
                                    5.0D)));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189PotionEffectsModule.ID,
                                    Minecraft189PotionEffectsModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189PotionEffectsModule.ID,
                                    Minecraft189PotionEffectsModule.Y_SETTING_ID,
                                    10));

            sortByExpiryBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189PotionEffectsModule.ID,
                            Minecraft189PotionEffectsModule.SORT_BY_EXPIRY_SETTING_ID, 20));
            expiryAlertBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189PotionEffectsModule.ID,
                            Minecraft189PotionEffectsModule.EXPIRY_ALERT_SETTING_ID, 30));
            expiryThresholdBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189PotionEffectsModule.ID,
                            Minecraft189PotionEffectsModule.EXPIRY_THRESHOLD_SETTING_ID, 40));

            return new Minecraft189PotionEffectsFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    sortByExpirySetting,
                    expiryAlertSetting,
                    expiryThresholdSetting,
                    xPresentation,
                    yPresentation,
                    sortByExpiryPresentation,
                    expiryAlertPresentation,
                    expiryThresholdPresentation,
                    xBinding,
                    yBinding,
                    sortByExpiryBinding,
                    expiryAlertBinding,
                    expiryThresholdBinding);
        } catch (RuntimeException failure) {
            closeQuietly(expiryThresholdBinding, failure);
            closeQuietly(expiryAlertBinding, failure);
            closeQuietly(sortByExpiryBinding, failure);
            closeQuietly(expiryThresholdPresentation, failure);
            closeQuietly(expiryAlertPresentation, failure);
            closeQuietly(sortByExpiryPresentation, failure);
            closeQuietly(expiryThresholdSetting, failure);
            closeQuietly(expiryAlertSetting, failure);
            closeQuietly(sortByExpirySetting, failure);
            closeQuietly(yBinding, failure);
            closeQuietly(xBinding, failure);
            closeQuietly(yPresentation, failure);
            closeQuietly(xPresentation, failure);
            closeQuietly(ySetting, failure);
            closeQuietly(xSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189PotionEffectsModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "potion-effects feature is closed");
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
                    Minecraft189PotionEffectsModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189PotionEffectsModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(expiryThresholdBinding, failure);
        failure = close(expiryAlertBinding, failure);
        failure = close(sortByExpiryBinding, failure);
        failure = close(expiryThresholdPresentation, failure);
        failure = close(expiryAlertPresentation, failure);
        failure = close(sortByExpiryPresentation, failure);
        failure = close(expiryThresholdSetting, failure);
        failure = close(expiryAlertSetting, failure);
        failure = close(sortByExpirySetting, failure);
        failure = close(yBinding, failure);
        failure = close(xBinding, failure);
        failure = close(yPresentation, failure);
        failure = close(xPresentation, failure);
        failure = close(ySetting, failure);
        failure = close(xSetting, failure);
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
            return append(
                    primary,
                    failure);
        } catch (Exception failure) {
            return append(
                    primary,
                    new IllegalStateException(
                            "potion-effects feature close failed",
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
