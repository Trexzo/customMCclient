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

final class Minecraft189HungerFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189HungerModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingRegistry.Registration showMetersSetting;
    private final SettingRegistry.Registration lowFoodAlertSetting;
    private final SettingRegistry.Registration lowFoodThresholdSetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final SettingPresentationRegistry.Registration showMetersPresentation;
    private final SettingPresentationRegistry.Registration lowFoodAlertPresentation;
    private final SettingPresentationRegistry.Registration lowFoodThresholdPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final ModuleSettingRegistry.Registration showMetersBinding;
    private final ModuleSettingRegistry.Registration lowFoodAlertBinding;
    private final ModuleSettingRegistry.Registration lowFoodThresholdBinding;
    private boolean closed;

    private Minecraft189HungerFeature(
            final ModuleController controller,
            final Minecraft189HungerModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingRegistry.Registration showMetersSetting,
            final SettingRegistry.Registration lowFoodAlertSetting,
            final SettingRegistry.Registration lowFoodThresholdSetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final SettingPresentationRegistry.Registration showMetersPresentation,
            final SettingPresentationRegistry.Registration lowFoodAlertPresentation,
            final SettingPresentationRegistry.Registration lowFoodThresholdPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final ModuleSettingRegistry.Registration showMetersBinding,
            final ModuleSettingRegistry.Registration lowFoodAlertBinding,
            final ModuleSettingRegistry.Registration lowFoodThresholdBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.showMetersSetting = showMetersSetting;
        this.lowFoodAlertSetting = lowFoodAlertSetting;
        this.lowFoodThresholdSetting = lowFoodThresholdSetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.showMetersPresentation = showMetersPresentation;
        this.lowFoodAlertPresentation = lowFoodAlertPresentation;
        this.lowFoodThresholdPresentation = lowFoodThresholdPresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.showMetersBinding = showMetersBinding;
        this.lowFoodAlertBinding = lowFoodAlertBinding;
        this.lowFoodThresholdBinding = lowFoodThresholdBinding;
    }

    static Minecraft189HungerFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189PlayerHungerState hungerState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189HungerModule module =
                new Minecraft189HungerModule(
                        hungerState,
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingRegistry.Registration showMetersSetting = null;
        SettingRegistry.Registration lowFoodAlertSetting = null;
        SettingRegistry.Registration lowFoodThresholdSetting = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        SettingPresentationRegistry.Registration showMetersPresentation = null;
        SettingPresentationRegistry.Registration lowFoodAlertPresentation = null;
        SettingPresentationRegistry.Registration lowFoodThresholdPresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        ModuleSettingRegistry.Registration showMetersBinding = null;
        ModuleSettingRegistry.Registration lowFoodAlertBinding = null;
        ModuleSettingRegistry.Registration lowFoodThresholdBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189HungerModule.ID,
                                    "Hunger",
                                    "Shows live food and saturation levels.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    60));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            showMetersSetting = settings.register(module.showMetersSetting());
            lowFoodAlertSetting = settings.register(module.lowFoodAlertSetting());
            lowFoodThresholdSetting = settings.register(
                    module.lowFoodThresholdSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189HungerModule.X_SETTING_ID,
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
                                    Minecraft189HungerModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            showMetersPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HungerModule.SHOW_METERS_SETTING_ID,
                            "Show Food/Saturation Bars", SettingValueKind.BOOLEAN, 20));
            lowFoodAlertPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HungerModule.LOW_FOOD_ALERT_SETTING_ID,
                            "Low Food Alert", SettingValueKind.BOOLEAN, 30));
            lowFoodThresholdPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HungerModule.LOW_FOOD_THRESHOLD_SETTING_ID,
                            "Low Food Threshold", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(
                                    Minecraft189HungerModule.MINIMUM_LOW_FOOD_THRESHOLD,
                                    Minecraft189HungerModule.MAXIMUM_LOW_FOOD_THRESHOLD,
                                    1.0D)));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189HungerModule.ID,
                                    Minecraft189HungerModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189HungerModule.ID,
                                    Minecraft189HungerModule.Y_SETTING_ID,
                                    10));

            showMetersBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HungerModule.ID,
                            Minecraft189HungerModule.SHOW_METERS_SETTING_ID, 20));
            lowFoodAlertBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HungerModule.ID,
                            Minecraft189HungerModule.LOW_FOOD_ALERT_SETTING_ID, 30));
            lowFoodThresholdBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HungerModule.ID,
                            Minecraft189HungerModule.LOW_FOOD_THRESHOLD_SETTING_ID, 40));

            return new Minecraft189HungerFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    showMetersSetting,
                    lowFoodAlertSetting,
                    lowFoodThresholdSetting,
                    xPresentation,
                    yPresentation,
                    showMetersPresentation,
                    lowFoodAlertPresentation,
                    lowFoodThresholdPresentation,
                    xBinding,
                    yBinding,
                    showMetersBinding,
                    lowFoodAlertBinding,
                    lowFoodThresholdBinding);
        } catch (RuntimeException failure) {
            closeQuietly(lowFoodThresholdBinding, failure);
            closeQuietly(lowFoodAlertBinding, failure);
            closeQuietly(showMetersBinding, failure);
            closeQuietly(lowFoodThresholdPresentation, failure);
            closeQuietly(lowFoodAlertPresentation, failure);
            closeQuietly(showMetersPresentation, failure);
            closeQuietly(lowFoodThresholdSetting, failure);
            closeQuietly(lowFoodAlertSetting, failure);
            closeQuietly(showMetersSetting, failure);
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

    Minecraft189HungerModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "hunger feature is closed");
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
                    Minecraft189HungerModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189HungerModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(lowFoodThresholdBinding, failure);
        failure = close(lowFoodAlertBinding, failure);
        failure = close(showMetersBinding, failure);
        failure = close(lowFoodThresholdPresentation, failure);
        failure = close(lowFoodAlertPresentation, failure);
        failure = close(showMetersPresentation, failure);
        failure = close(lowFoodThresholdSetting, failure);
        failure = close(lowFoodAlertSetting, failure);
        failure = close(showMetersSetting, failure);
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
                            "hunger feature close failed",
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
