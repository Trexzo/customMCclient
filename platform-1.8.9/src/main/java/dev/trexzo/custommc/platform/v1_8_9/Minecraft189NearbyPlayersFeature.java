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
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final SettingPresentationRegistry.Registration radiusPresentation;
    private final SettingPresentationRegistry.Registration showRadarPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final ModuleSettingRegistry.Registration radiusBinding;
    private final ModuleSettingRegistry.Registration showRadarBinding;
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
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final SettingPresentationRegistry.Registration radiusPresentation,
            final SettingPresentationRegistry.Registration showRadarPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final ModuleSettingRegistry.Registration radiusBinding,
            final ModuleSettingRegistry.Registration showRadarBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.radiusSetting = radiusSetting;
        this.showRadarSetting = showRadarSetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.radiusPresentation = radiusPresentation;
        this.showRadarPresentation = showRadarPresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.radiusBinding = radiusBinding;
        this.showRadarBinding = showRadarBinding;
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
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        SettingPresentationRegistry.Registration radiusPresentation = null;
        SettingPresentationRegistry.Registration showRadarPresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        ModuleSettingRegistry.Registration radiusBinding = null;
        ModuleSettingRegistry.Registration showRadarBinding = null;

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

            return new Minecraft189NearbyPlayersFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    radiusSetting,
                    showRadarSetting,
                    xPresentation,
                    yPresentation,
                    radiusPresentation,
                    showRadarPresentation,
                    xBinding,
                    yBinding,
                    radiusBinding,
                    showRadarBinding);
        } catch (RuntimeException failure) {
            closeQuietly(showRadarBinding, failure);
            closeQuietly(radiusBinding, failure);
            closeQuietly(yBinding, failure);
            closeQuietly(xBinding, failure);
            closeQuietly(showRadarPresentation, failure);
            closeQuietly(radiusPresentation, failure);
            closeQuietly(yPresentation, failure);
            closeQuietly(xPresentation, failure);
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

        failure = close(showRadarBinding, failure);
        failure = close(radiusBinding, failure);
        failure = close(yBinding, failure);
        failure = close(xBinding, failure);
        failure = close(showRadarPresentation, failure);
        failure = close(radiusPresentation, failure);
        failure = close(yPresentation, failure);
        failure = close(xPresentation, failure);
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
