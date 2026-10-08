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

final class Minecraft189NearbyPlayersFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189NearbyPlayersModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingRegistry.Registration radiusSetting;
    private final SettingRegistry.Registration showRadarSetting;
    private final SettingRegistry.Registration northUpSetting;
    private final SettingRegistry.Registration heightColorsSetting;
    private final SettingRegistry.Registration highlightNearestSetting;
    private final SettingRegistry.Registration showNorthSetting;
    private final SettingRegistry.Registration showBearingSetting;
    private final SettingRegistry.Registration proximityWarningSetting;
    private final SettingRegistry.Registration warningDistanceSetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final SettingPresentationRegistry.Registration radiusPresentation;
    private final SettingPresentationRegistry.Registration showRadarPresentation;
    private final SettingPresentationRegistry.Registration northUpPresentation;
    private final SettingPresentationRegistry.Registration heightColorsPresentation;
    private final SettingPresentationRegistry.Registration highlightNearestPresentation;
    private final SettingPresentationRegistry.Registration showNorthPresentation;
    private final SettingPresentationRegistry.Registration showBearingPresentation;
    private final SettingPresentationRegistry.Registration proximityWarningPresentation;
    private final SettingPresentationRegistry.Registration warningDistancePresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final ModuleSettingRegistry.Registration radiusBinding;
    private final ModuleSettingRegistry.Registration showRadarBinding;
    private final ModuleSettingRegistry.Registration northUpBinding;
    private final ModuleSettingRegistry.Registration heightColorsBinding;
    private final ModuleSettingRegistry.Registration highlightNearestBinding;
    private final ModuleSettingRegistry.Registration showNorthBinding;
    private final ModuleSettingRegistry.Registration showBearingBinding;
    private final ModuleSettingRegistry.Registration proximityWarningBinding;
    private final ModuleSettingRegistry.Registration warningDistanceBinding;
    private boolean closed;

    private Minecraft189NearbyPlayersFeature(
            final ModuleController controller,
            final Minecraft189NearbyPlayersModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingRegistry.Registration radiusSetting,
            final SettingRegistry.Registration showRadarSetting,
            final SettingRegistry.Registration northUpSetting,
            final SettingRegistry.Registration heightColorsSetting,
            final SettingRegistry.Registration highlightNearestSetting,
            final SettingRegistry.Registration showNorthSetting,
            final SettingRegistry.Registration showBearingSetting,
            final SettingRegistry.Registration proximityWarningSetting,
            final SettingRegistry.Registration warningDistanceSetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final SettingPresentationRegistry.Registration radiusPresentation,
            final SettingPresentationRegistry.Registration showRadarPresentation,
            final SettingPresentationRegistry.Registration northUpPresentation,
            final SettingPresentationRegistry.Registration heightColorsPresentation,
            final SettingPresentationRegistry.Registration highlightNearestPresentation,
            final SettingPresentationRegistry.Registration showNorthPresentation,
            final SettingPresentationRegistry.Registration showBearingPresentation,
            final SettingPresentationRegistry.Registration proximityWarningPresentation,
            final SettingPresentationRegistry.Registration warningDistancePresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final ModuleSettingRegistry.Registration radiusBinding,
            final ModuleSettingRegistry.Registration showRadarBinding,
            final ModuleSettingRegistry.Registration northUpBinding,
            final ModuleSettingRegistry.Registration heightColorsBinding,
            final ModuleSettingRegistry.Registration highlightNearestBinding,
            final ModuleSettingRegistry.Registration showNorthBinding,
            final ModuleSettingRegistry.Registration showBearingBinding,
            final ModuleSettingRegistry.Registration proximityWarningBinding,
            final ModuleSettingRegistry.Registration warningDistanceBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.radiusSetting = radiusSetting;
        this.showRadarSetting = showRadarSetting;
        this.northUpSetting = northUpSetting;
        this.heightColorsSetting = heightColorsSetting;
        this.highlightNearestSetting = highlightNearestSetting;
        this.showNorthSetting = showNorthSetting;
        this.showBearingSetting = showBearingSetting;
        this.proximityWarningSetting = proximityWarningSetting;
        this.warningDistanceSetting = warningDistanceSetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.radiusPresentation = radiusPresentation;
        this.showRadarPresentation = showRadarPresentation;
        this.northUpPresentation = northUpPresentation;
        this.heightColorsPresentation = heightColorsPresentation;
        this.highlightNearestPresentation = highlightNearestPresentation;
        this.showNorthPresentation = showNorthPresentation;
        this.showBearingPresentation = showBearingPresentation;
        this.proximityWarningPresentation = proximityWarningPresentation;
        this.warningDistancePresentation = warningDistancePresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.radiusBinding = radiusBinding;
        this.showRadarBinding = showRadarBinding;
        this.northUpBinding = northUpBinding;
        this.heightColorsBinding = heightColorsBinding;
        this.highlightNearestBinding = highlightNearestBinding;
        this.showNorthBinding = showNorthBinding;
        this.showBearingBinding = showBearingBinding;
        this.proximityWarningBinding = proximityWarningBinding;
        this.warningDistanceBinding = warningDistanceBinding;
    }

    static Minecraft189NearbyPlayersFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189PlayerPositionState local,
            final Minecraft189WorldEntityPositionState entities,
            final Minecraft189WorldEntityKindState kinds,
            final Minecraft189PlayerRotationState rotation,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189NearbyPlayersModule module =
                new Minecraft189NearbyPlayersModule(
                        local, entities, kinds, rotation,
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingRegistry.Registration radiusSetting = null;
        SettingRegistry.Registration showRadarSetting = null;
        SettingRegistry.Registration northUpSetting = null;
        SettingRegistry.Registration heightColorsSetting = null;
        SettingRegistry.Registration highlightNearestSetting = null;
        SettingRegistry.Registration showNorthSetting = null;
        SettingRegistry.Registration showBearingSetting = null;
        SettingRegistry.Registration proximityWarningSetting = null;
        SettingRegistry.Registration warningDistanceSetting = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        SettingPresentationRegistry.Registration radiusPresentation = null;
        SettingPresentationRegistry.Registration showRadarPresentation = null;
        SettingPresentationRegistry.Registration northUpPresentation = null;
        SettingPresentationRegistry.Registration heightColorsPresentation = null;
        SettingPresentationRegistry.Registration highlightNearestPresentation = null;
        SettingPresentationRegistry.Registration showNorthPresentation = null;
        SettingPresentationRegistry.Registration showBearingPresentation = null;
        SettingPresentationRegistry.Registration proximityWarningPresentation = null;
        SettingPresentationRegistry.Registration warningDistancePresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        ModuleSettingRegistry.Registration radiusBinding = null;
        ModuleSettingRegistry.Registration showRadarBinding = null;
        ModuleSettingRegistry.Registration northUpBinding = null;
        ModuleSettingRegistry.Registration heightColorsBinding = null;
        ModuleSettingRegistry.Registration highlightNearestBinding = null;
        ModuleSettingRegistry.Registration showNorthBinding = null;
        ModuleSettingRegistry.Registration showBearingBinding = null;
        ModuleSettingRegistry.Registration proximityWarningBinding = null;
        ModuleSettingRegistry.Registration warningDistanceBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189NearbyPlayersModule.ID,
                                    "Nearby Players",
                                    "Counts mapped remote players inside a configurable radius.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    58));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            radiusSetting = settings.register(module.radiusSetting());
            showRadarSetting = settings.register(module.showRadarSetting());
            northUpSetting = settings.register(module.northUpSetting());
            heightColorsSetting = settings.register(module.heightColorsSetting());
            highlightNearestSetting = settings.register(module.highlightNearestSetting());
            showNorthSetting = settings.register(module.showNorthSetting());
            showBearingSetting = settings.register(module.showBearingSetting());
            proximityWarningSetting = settings.register(module.proximityWarningSetting());
            warningDistanceSetting = settings.register(module.warningDistanceSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189NearbyPlayersModule.X_SETTING_ID,
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
                                    Minecraft189NearbyPlayersModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            radiusPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.RADIUS_SETTING_ID,
                            "Radius",
                            SettingValueKind.INTEGER,
                            20,
                            new SettingNumericSpec(1.0D, 128.0D, 1.0D)));
            showRadarPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.SHOW_RADAR_SETTING_ID,
                            "2D Radar", SettingValueKind.BOOLEAN, 30));
            northUpPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.NORTH_UP_SETTING_ID,
                            "North Up", SettingValueKind.BOOLEAN, 40));
            heightColorsPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.HEIGHT_COLORS_SETTING_ID,
                            "Height Colors", SettingValueKind.BOOLEAN, 50));
            highlightNearestPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.HIGHLIGHT_NEAREST_SETTING_ID,
                            "Highlight Nearest", SettingValueKind.BOOLEAN, 60));
            showNorthPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.SHOW_NORTH_SETTING_ID,
                            "Show North", SettingValueKind.BOOLEAN, 70));
            showBearingPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.SHOW_BEARING_SETTING_ID,
                            "Nearest Bearing", SettingValueKind.BOOLEAN, 80));
            proximityWarningPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.PROXIMITY_WARNING_SETTING_ID,
                            "Proximity Warning", SettingValueKind.BOOLEAN, 90));
            warningDistancePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189NearbyPlayersModule.WARNING_DISTANCE_SETTING_ID,
                            "Warning Distance", SettingValueKind.INTEGER, 100,
                            new SettingNumericSpec(1.0D, 128.0D, 1.0D)));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189NearbyPlayersModule.ID,
                                    Minecraft189NearbyPlayersModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189NearbyPlayersModule.ID,
                                    Minecraft189NearbyPlayersModule.Y_SETTING_ID,
                                    10));

            radiusBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.RADIUS_SETTING_ID,
                            20));

            showRadarBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.SHOW_RADAR_SETTING_ID, 30));
            northUpBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.NORTH_UP_SETTING_ID, 40));
            heightColorsBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.HEIGHT_COLORS_SETTING_ID, 50));
            highlightNearestBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.HIGHLIGHT_NEAREST_SETTING_ID, 60));
            showNorthBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.SHOW_NORTH_SETTING_ID, 70));
            showBearingBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.SHOW_BEARING_SETTING_ID, 80));
            proximityWarningBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.PROXIMITY_WARNING_SETTING_ID, 90));
            warningDistanceBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189NearbyPlayersModule.ID,
                            Minecraft189NearbyPlayersModule.WARNING_DISTANCE_SETTING_ID, 100));

            return new Minecraft189NearbyPlayersFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    radiusSetting,
                    showRadarSetting,
                    northUpSetting,
                    heightColorsSetting,
                    highlightNearestSetting,
                    showNorthSetting,
                    showBearingSetting,
                    proximityWarningSetting,
                    warningDistanceSetting,
                    xPresentation,
                    yPresentation,
                    radiusPresentation,
                    showRadarPresentation,
                    northUpPresentation,
                    heightColorsPresentation,
                    highlightNearestPresentation,
                    showNorthPresentation,
                    showBearingPresentation,
                    proximityWarningPresentation,
                    warningDistancePresentation,
                    xBinding,
                    yBinding,
                    radiusBinding,
                    showRadarBinding,
                    northUpBinding,
                    heightColorsBinding,
                    highlightNearestBinding,
                    showNorthBinding,
                    showBearingBinding,
                    proximityWarningBinding,
                    warningDistanceBinding);
        } catch (RuntimeException failure) {
            closeQuietly(warningDistanceBinding, failure);
            closeQuietly(proximityWarningBinding, failure);
            closeQuietly(showBearingBinding, failure);
            closeQuietly(showNorthBinding, failure);
            closeQuietly(highlightNearestBinding, failure);
            closeQuietly(heightColorsBinding, failure);
            closeQuietly(northUpBinding, failure);
            closeQuietly(showRadarBinding, failure);
            closeQuietly(radiusBinding, failure);
            closeQuietly(yBinding, failure);
            closeQuietly(xBinding, failure);
            closeQuietly(warningDistancePresentation, failure);
            closeQuietly(proximityWarningPresentation, failure);
            closeQuietly(showBearingPresentation, failure);
            closeQuietly(showNorthPresentation, failure);
            closeQuietly(highlightNearestPresentation, failure);
            closeQuietly(heightColorsPresentation, failure);
            closeQuietly(northUpPresentation, failure);
            closeQuietly(showRadarPresentation, failure);
            closeQuietly(radiusPresentation, failure);
            closeQuietly(yPresentation, failure);
            closeQuietly(xPresentation, failure);
            closeQuietly(warningDistanceSetting, failure);
            closeQuietly(proximityWarningSetting, failure);
            closeQuietly(showBearingSetting, failure);
            closeQuietly(showNorthSetting, failure);
            closeQuietly(highlightNearestSetting, failure);
            closeQuietly(heightColorsSetting, failure);
            closeQuietly(northUpSetting, failure);
            closeQuietly(showRadarSetting, failure);
            closeQuietly(radiusSetting, failure);
            closeQuietly(ySetting, failure);
            closeQuietly(xSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189NearbyPlayersModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "nearby players feature is closed");
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
                    Minecraft189NearbyPlayersModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189NearbyPlayersModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(warningDistanceBinding, failure);
        failure = close(proximityWarningBinding, failure);
        failure = close(showBearingBinding, failure);
        failure = close(showNorthBinding, failure);
        failure = close(highlightNearestBinding, failure);
        failure = close(heightColorsBinding, failure);
        failure = close(northUpBinding, failure);
        failure = close(showRadarBinding, failure);
        failure = close(radiusBinding, failure);
        failure = close(yBinding, failure);
        failure = close(xBinding, failure);
        failure = close(warningDistancePresentation, failure);
        failure = close(proximityWarningPresentation, failure);
        failure = close(showBearingPresentation, failure);
        failure = close(showNorthPresentation, failure);
        failure = close(highlightNearestPresentation, failure);
        failure = close(heightColorsPresentation, failure);
        failure = close(northUpPresentation, failure);
        failure = close(showRadarPresentation, failure);
        failure = close(radiusPresentation, failure);
        failure = close(yPresentation, failure);
        failure = close(xPresentation, failure);
        failure = close(warningDistanceSetting, failure);
        failure = close(proximityWarningSetting, failure);
        failure = close(showBearingSetting, failure);
        failure = close(showNorthSetting, failure);
        failure = close(highlightNearestSetting, failure);
        failure = close(heightColorsSetting, failure);
        failure = close(northUpSetting, failure);
        failure = close(showRadarSetting, failure);
        failure = close(radiusSetting, failure);
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
                            "nearby players feature close failed",
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
