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

final class Minecraft189TargetHudFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189TargetHudModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingRegistry.Registration compactSetting;
    private final SettingRegistry.Registration proximityMeterSetting;
    private final SettingRegistry.Registration proximityRangeSetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final SettingPresentationRegistry.Registration compactPresentation;
    private final SettingPresentationRegistry.Registration proximityMeterPresentation;
    private final SettingPresentationRegistry.Registration proximityRangePresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final ModuleSettingRegistry.Registration compactBinding;
    private final ModuleSettingRegistry.Registration proximityMeterBinding;
    private final ModuleSettingRegistry.Registration proximityRangeBinding;
    private final SettingRegistry.Registration showCoordinatesSetting;
    private final SettingPresentationRegistry.Registration showCoordinatesPresentation;
    private final ModuleSettingRegistry.Registration showCoordinatesBinding;
    private final SettingRegistry.Registration adaptiveAccentSetting;
    private final SettingPresentationRegistry.Registration adaptiveAccentPresentation;
    private final ModuleSettingRegistry.Registration adaptiveAccentBinding;
    private boolean closed;

    private Minecraft189TargetHudFeature(
            final ModuleController controller,
            final Minecraft189TargetHudModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingRegistry.Registration compactSetting,
            final SettingRegistry.Registration proximityMeterSetting,
            final SettingRegistry.Registration proximityRangeSetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final SettingPresentationRegistry.Registration compactPresentation,
            final SettingPresentationRegistry.Registration proximityMeterPresentation,
            final SettingPresentationRegistry.Registration proximityRangePresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final ModuleSettingRegistry.Registration compactBinding,
            final ModuleSettingRegistry.Registration proximityMeterBinding,
            final ModuleSettingRegistry.Registration proximityRangeBinding,
            final SettingRegistry.Registration showCoordinatesSetting,
            final SettingPresentationRegistry.Registration showCoordinatesPresentation,
            final ModuleSettingRegistry.Registration showCoordinatesBinding,
            final SettingRegistry.Registration adaptiveAccentSetting,
            final SettingPresentationRegistry.Registration adaptiveAccentPresentation,
            final ModuleSettingRegistry.Registration adaptiveAccentBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.compactSetting = compactSetting;
        this.proximityMeterSetting = proximityMeterSetting;
        this.proximityRangeSetting = proximityRangeSetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.compactPresentation = compactPresentation;
        this.proximityMeterPresentation = proximityMeterPresentation;
        this.proximityRangePresentation = proximityRangePresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.compactBinding = compactBinding;
        this.proximityMeterBinding = proximityMeterBinding;
        this.proximityRangeBinding = proximityRangeBinding;
        this.showCoordinatesSetting = showCoordinatesSetting;
        this.showCoordinatesPresentation = showCoordinatesPresentation;
        this.showCoordinatesBinding = showCoordinatesBinding;
        this.adaptiveAccentSetting = adaptiveAccentSetting;
        this.adaptiveAccentPresentation = adaptiveAccentPresentation;
        this.adaptiveAccentBinding = adaptiveAccentBinding;
    }

    static Minecraft189TargetHudFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189NearestPlayerTargetState nearest,
            final Minecraft189TargetRotationState rotationState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189TargetHudModule module =
                new Minecraft189TargetHudModule(
                        nearest, rotationState,
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingRegistry.Registration compactSetting = null;
        SettingRegistry.Registration proximityMeterSetting = null;
        SettingRegistry.Registration proximityRangeSetting = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        SettingPresentationRegistry.Registration compactPresentation = null;
        SettingPresentationRegistry.Registration proximityMeterPresentation = null;
        SettingPresentationRegistry.Registration proximityRangePresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        ModuleSettingRegistry.Registration compactBinding = null;
        ModuleSettingRegistry.Registration proximityMeterBinding = null;
        ModuleSettingRegistry.Registration proximityRangeBinding = null;
        SettingRegistry.Registration showCoordinatesSetting = null;
        SettingPresentationRegistry.Registration showCoordinatesPresentation = null;
        ModuleSettingRegistry.Registration showCoordinatesBinding = null;
        SettingRegistry.Registration adaptiveAccentSetting = null;
        SettingPresentationRegistry.Registration adaptiveAccentPresentation = null;
        ModuleSettingRegistry.Registration adaptiveAccentBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189TargetHudModule.ID,
                                    "Target HUD",
                                    "Shows the nearest mapped remote-player distance and direction.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    55));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            compactSetting = settings.register(module.compactSetting());
            proximityMeterSetting = settings.register(module.proximityMeterSetting());
            proximityRangeSetting = settings.register(module.proximityRangeSetting());
            showCoordinatesSetting = settings.register(module.showCoordinatesSetting());
            adaptiveAccentSetting = settings.register(module.adaptiveAccentSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189TargetHudModule.X_SETTING_ID,
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
                                    Minecraft189TargetHudModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            compactPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetHudModule.COMPACT_SETTING_ID,
                            "Compact",
                            SettingValueKind.BOOLEAN,
                            20));
            proximityMeterPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetHudModule.PROXIMITY_METER_SETTING_ID,
                            "Proximity Meter", SettingValueKind.BOOLEAN, 30));
            proximityRangePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetHudModule.PROXIMITY_RANGE_SETTING_ID,
                            "Meter Range (Blocks)", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(
                                    Minecraft189TargetHudModule.MINIMUM_PROXIMITY_RANGE,
                                    Minecraft189TargetHudModule.MAXIMUM_PROXIMITY_RANGE,
                                    1.0D)));
            showCoordinatesPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetHudModule.SHOW_COORDINATES_SETTING_ID,
                            "Show XYZ Coordinates", SettingValueKind.BOOLEAN, 50));
            adaptiveAccentPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetHudModule.ADAPTIVE_ACCENT_SETTING_ID,
                            "Adaptive Accent", SettingValueKind.BOOLEAN, 60));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189TargetHudModule.ID,
                                    Minecraft189TargetHudModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189TargetHudModule.ID,
                                    Minecraft189TargetHudModule.Y_SETTING_ID,
                                    10));

            compactBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TargetHudModule.ID,
                            Minecraft189TargetHudModule.COMPACT_SETTING_ID, 20));

            proximityMeterBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TargetHudModule.ID,
                            Minecraft189TargetHudModule.PROXIMITY_METER_SETTING_ID, 30));
            proximityRangeBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TargetHudModule.ID,
                            Minecraft189TargetHudModule.PROXIMITY_RANGE_SETTING_ID, 40));

            showCoordinatesBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TargetHudModule.ID,
                            Minecraft189TargetHudModule.SHOW_COORDINATES_SETTING_ID, 50));

            adaptiveAccentBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189TargetHudModule.ID,
                            Minecraft189TargetHudModule.ADAPTIVE_ACCENT_SETTING_ID, 60));
            return new Minecraft189TargetHudFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    compactSetting,
                    proximityMeterSetting,
                    proximityRangeSetting,
                    xPresentation,
                    yPresentation,
                    compactPresentation,
                    proximityMeterPresentation,
                    proximityRangePresentation,
                    xBinding,
                    yBinding,
                    compactBinding,
                    proximityMeterBinding,
                    proximityRangeBinding,
                    showCoordinatesSetting,
                    showCoordinatesPresentation,
                    showCoordinatesBinding,
                    adaptiveAccentSetting,
                    adaptiveAccentPresentation,
                    adaptiveAccentBinding);
        } catch (RuntimeException failure) {
            closeQuietly(adaptiveAccentBinding, failure);
            closeQuietly(adaptiveAccentPresentation, failure);
            closeQuietly(adaptiveAccentSetting, failure);
            closeQuietly(showCoordinatesBinding, failure);
            closeQuietly(showCoordinatesPresentation, failure);
            closeQuietly(showCoordinatesSetting, failure);
            closeQuietly(proximityRangeBinding, failure);
            closeQuietly(proximityMeterBinding, failure);
            closeQuietly(proximityRangePresentation, failure);
            closeQuietly(proximityMeterPresentation, failure);
            closeQuietly(proximityRangeSetting, failure);
            closeQuietly(proximityMeterSetting, failure);
            closeQuietly(compactBinding, failure);
            closeQuietly(yBinding, failure);
            closeQuietly(xBinding, failure);
            closeQuietly(compactPresentation, failure);
            closeQuietly(yPresentation, failure);
            closeQuietly(xPresentation, failure);
            closeQuietly(compactSetting, failure);
            closeQuietly(ySetting, failure);
            closeQuietly(xSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189TargetHudModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "target HUD feature is closed");
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
                    Minecraft189TargetHudModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189TargetHudModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(adaptiveAccentBinding, failure);
        failure = close(adaptiveAccentPresentation, failure);
        failure = close(adaptiveAccentSetting, failure);
        failure = close(showCoordinatesBinding, failure);
        failure = close(showCoordinatesPresentation, failure);
        failure = close(showCoordinatesSetting, failure);
        failure = close(proximityRangeBinding, failure);
        failure = close(proximityMeterBinding, failure);
        failure = close(proximityRangePresentation, failure);
        failure = close(proximityMeterPresentation, failure);
        failure = close(proximityRangeSetting, failure);
        failure = close(proximityMeterSetting, failure);
        failure = close(compactBinding, failure);
        failure = close(yBinding, failure);
        failure = close(xBinding, failure);
        failure = close(compactPresentation, failure);
        failure = close(yPresentation, failure);
        failure = close(xPresentation, failure);
        failure = close(compactSetting, failure);
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
                            "target HUD feature close failed",
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
