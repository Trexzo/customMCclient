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

final class Minecraft189HealthFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189HealthModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingRegistry.Registration showPercentSetting;
    private final SettingRegistry.Registration lowHealthAlertSetting;
    private final SettingRegistry.Registration lowHealthThresholdSetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final SettingPresentationRegistry.Registration showPercentPresentation;
    private final SettingPresentationRegistry.Registration lowHealthAlertPresentation;
    private final SettingPresentationRegistry.Registration lowHealthThresholdPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final ModuleSettingRegistry.Registration showPercentBinding;
    private final ModuleSettingRegistry.Registration lowHealthAlertBinding;
    private final ModuleSettingRegistry.Registration lowHealthThresholdBinding;
    private final SettingRegistry.Registration showBarSetting;
    private final SettingPresentationRegistry.Registration showBarPresentation;
    private final ModuleSettingRegistry.Registration showBarBinding;
    private boolean closed;

    private Minecraft189HealthFeature(
            final ModuleController controller,
            final Minecraft189HealthModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingRegistry.Registration showPercentSetting,
            final SettingRegistry.Registration lowHealthAlertSetting,
            final SettingRegistry.Registration lowHealthThresholdSetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final SettingPresentationRegistry.Registration showPercentPresentation,
            final SettingPresentationRegistry.Registration lowHealthAlertPresentation,
            final SettingPresentationRegistry.Registration lowHealthThresholdPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final ModuleSettingRegistry.Registration showPercentBinding,
            final ModuleSettingRegistry.Registration lowHealthAlertBinding,
            final ModuleSettingRegistry.Registration lowHealthThresholdBinding,
            final SettingRegistry.Registration showBarSetting,
            final SettingPresentationRegistry.Registration showBarPresentation,
            final ModuleSettingRegistry.Registration showBarBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.showPercentSetting = showPercentSetting;
        this.lowHealthAlertSetting = lowHealthAlertSetting;
        this.lowHealthThresholdSetting = lowHealthThresholdSetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.showPercentPresentation = showPercentPresentation;
        this.lowHealthAlertPresentation = lowHealthAlertPresentation;
        this.lowHealthThresholdPresentation = lowHealthThresholdPresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.showPercentBinding = showPercentBinding;
        this.lowHealthAlertBinding = lowHealthAlertBinding;
        this.lowHealthThresholdBinding = lowHealthThresholdBinding;
        this.showBarSetting = showBarSetting;
        this.showBarPresentation = showBarPresentation;
        this.showBarBinding = showBarBinding;
    }

    static Minecraft189HealthFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189PlayerHealthState healthState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189HealthModule module =
                new Minecraft189HealthModule(
                        healthState,
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingRegistry.Registration showPercentSetting = null;
        SettingRegistry.Registration lowHealthAlertSetting = null;
        SettingRegistry.Registration lowHealthThresholdSetting = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        SettingPresentationRegistry.Registration showPercentPresentation = null;
        SettingPresentationRegistry.Registration lowHealthAlertPresentation = null;
        SettingPresentationRegistry.Registration lowHealthThresholdPresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        ModuleSettingRegistry.Registration showPercentBinding = null;
        ModuleSettingRegistry.Registration lowHealthAlertBinding = null;
        ModuleSettingRegistry.Registration lowHealthThresholdBinding = null;
        SettingRegistry.Registration showBarSetting = null;
        SettingPresentationRegistry.Registration showBarPresentation = null;
        ModuleSettingRegistry.Registration showBarBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189HealthModule.ID,
                                    "Health",
                                    "Shows live player health and maximum health.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    58));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            showPercentSetting = settings.register(module.showPercentSetting());
            lowHealthAlertSetting = settings.register(module.lowHealthAlertSetting());
            lowHealthThresholdSetting = settings.register(module.lowHealthThresholdSetting());
            showBarSetting = settings.register(module.showBarSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189HealthModule.X_SETTING_ID,
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
                                    Minecraft189HealthModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            showPercentPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HealthModule.SHOW_PERCENT_SETTING_ID,
                            "Show Percent", SettingValueKind.BOOLEAN, 20));
            lowHealthAlertPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HealthModule.LOW_HEALTH_ALERT_SETTING_ID,
                            "Low Health Alert", SettingValueKind.BOOLEAN, 30));
            lowHealthThresholdPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HealthModule.LOW_HEALTH_THRESHOLD_SETTING_ID,
                            "Low Health Threshold %", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(1.0D, 100.0D, 5.0D)));
            showBarPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HealthModule.SHOW_BAR_SETTING_ID,
                            "Show Health Bar", SettingValueKind.BOOLEAN, 50));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189HealthModule.ID,
                                    Minecraft189HealthModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189HealthModule.ID,
                                    Minecraft189HealthModule.Y_SETTING_ID,
                                    10));

            showPercentBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HealthModule.ID,
                            Minecraft189HealthModule.SHOW_PERCENT_SETTING_ID, 20));
            lowHealthAlertBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HealthModule.ID,
                            Minecraft189HealthModule.LOW_HEALTH_ALERT_SETTING_ID, 30));
            lowHealthThresholdBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HealthModule.ID,
                            Minecraft189HealthModule.LOW_HEALTH_THRESHOLD_SETTING_ID, 40));

            showBarBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HealthModule.ID,
                            Minecraft189HealthModule.SHOW_BAR_SETTING_ID, 50));
            return new Minecraft189HealthFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    showPercentSetting,
                    lowHealthAlertSetting,
                    lowHealthThresholdSetting,
                    xPresentation,
                    yPresentation,
                    showPercentPresentation,
                    lowHealthAlertPresentation,
                    lowHealthThresholdPresentation,
                    xBinding,
                    yBinding,
                    showPercentBinding,
                    lowHealthAlertBinding,
                    lowHealthThresholdBinding,
                    showBarSetting,
                    showBarPresentation,
                    showBarBinding);
        } catch (RuntimeException failure) {
            closeQuietly(showBarBinding, failure);
            closeQuietly(showBarPresentation, failure);
            closeQuietly(showBarSetting, failure);
            closeQuietly(lowHealthThresholdBinding, failure);
            closeQuietly(lowHealthAlertBinding, failure);
            closeQuietly(showPercentBinding, failure);
            closeQuietly(lowHealthThresholdPresentation, failure);
            closeQuietly(lowHealthAlertPresentation, failure);
            closeQuietly(showPercentPresentation, failure);
            closeQuietly(lowHealthThresholdSetting, failure);
            closeQuietly(lowHealthAlertSetting, failure);
            closeQuietly(showPercentSetting, failure);
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

    Minecraft189HealthModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "health feature is closed");
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
                    Minecraft189HealthModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189HealthModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(showBarBinding, failure);
        failure = close(showBarPresentation, failure);
        failure = close(showBarSetting, failure);
        failure = close(lowHealthThresholdBinding, failure);
        failure = close(lowHealthAlertBinding, failure);
        failure = close(showPercentBinding, failure);
        failure = close(lowHealthThresholdPresentation, failure);
        failure = close(lowHealthAlertPresentation, failure);
        failure = close(showPercentPresentation, failure);
        failure = close(lowHealthThresholdSetting, failure);
        failure = close(lowHealthAlertSetting, failure);
        failure = close(showPercentSetting, failure);
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
                            "health feature close failed",
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
