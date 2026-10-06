package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleCategoryDescriptor;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
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

import java.util.Objects;

public final class Minecraft189FeatureCatalog
        implements AutoCloseable {
    public static final String VISUALS_CATEGORY_ID =
            "visuals";

    private final ModuleRegistry modules;
    private final ModuleController moduleController;
    private final ModulePresentationRegistry modulePresentations;
    private final ModuleSettingRegistry moduleSettings;
    private final SettingRegistry settings;
    private final SettingPresentationRegistry settingPresentations;
    private final Minecraft189WatermarkModule watermark;
    private final ModuleRegistry.Registration watermarkRegistration;
    private final ModulePresentationRegistry.Registration watermarkPresentation;
    private final ModuleCategoryRegistry.Registration visualsCategory;
    private final SettingRegistry.Registration textSetting;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingPresentationRegistry.Registration textPresentation;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final ModuleSettingRegistry.Registration textBinding;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final Minecraft189ArrayListFeature arrayListFeature;
    private final Minecraft189KeystrokesFeature keystrokesFeature;
    private final Minecraft189FpsFeature fpsFeature;
    private final Minecraft189CpsFeature cpsFeature;
    private final Minecraft189CoordinatesFeature coordinatesFeature;
    private final Minecraft189SpeedFeature speedFeature;
    private Minecraft189FullbrightFeature fullbrightFeature;
    private Minecraft189FovFeature fovFeature;
    private Minecraft189NoBobbingFeature noBobbingFeature;
    private boolean closed;

    private Minecraft189FeatureCatalog(
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ModulePresentationRegistry modulePresentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189WatermarkModule watermark,
            final ModuleRegistry.Registration watermarkRegistration,
            final ModulePresentationRegistry.Registration watermarkPresentation,
            final ModuleCategoryRegistry.Registration visualsCategory,
            final SettingRegistry.Registration textSetting,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingPresentationRegistry.Registration textPresentation,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final ModuleSettingRegistry.Registration textBinding,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final Minecraft189ArrayListFeature arrayListFeature,
            final Minecraft189KeystrokesFeature keystrokesFeature,
            final Minecraft189FpsFeature fpsFeature,
            final Minecraft189CpsFeature cpsFeature,
            final Minecraft189CoordinatesFeature coordinatesFeature,
            final Minecraft189SpeedFeature speedFeature) {
        this.modules = modules;
        this.moduleController = moduleController;
        this.modulePresentations = modulePresentations;
        this.moduleSettings = moduleSettings;
        this.settings = settings;
        this.settingPresentations = settingPresentations;
        this.watermark = watermark;
        this.watermarkRegistration = watermarkRegistration;
        this.watermarkPresentation = watermarkPresentation;
        this.visualsCategory = visualsCategory;
        this.textSetting = textSetting;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.textPresentation = textPresentation;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.textBinding = textBinding;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.arrayListFeature = arrayListFeature;
        this.keystrokesFeature = keystrokesFeature;
        this.fpsFeature = fpsFeature;
        this.cpsFeature = cpsFeature;
        this.coordinatesFeature = coordinatesFeature;
        this.speedFeature = speedFeature;
    }

    public static Minecraft189FeatureCatalog install(
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ModulePresentationRegistry presentations,
            final ModuleCategoryRegistry categories,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState,
            final Minecraft189FrameRateTracker frameRateTracker,
            final Minecraft189ClickRateTracker clickRateTracker,
            final Minecraft189PlayerPositionState playerPositionState,
            final Minecraft189MovementSpeedTracker movementSpeedTracker,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        Objects.requireNonNull(modules, "modules");
        Objects.requireNonNull(moduleController, "moduleController");
        Objects.requireNonNull(presentations, "presentations");
        Objects.requireNonNull(categories, "categories");
        Objects.requireNonNull(moduleSettings, "moduleSettings");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(settingPresentations, "settingPresentations");
        Objects.requireNonNull(inputState, "inputState");
        Objects.requireNonNull(frameRateTracker, "frameRateTracker");
        Objects.requireNonNull(clickRateTracker, "clickRateTracker");
        Objects.requireNonNull(playerPositionState, "playerPositionState");
        Objects.requireNonNull(movementSpeedTracker, "movementSpeedTracker");
        Objects.requireNonNull(renderPipeline, "renderPipeline");
        Objects.requireNonNull(hostCallbacks, "hostCallbacks");

        ModuleCategoryRegistry.Registration category = null;
        ModuleRegistry.Registration module = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration textSetting = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingPresentationRegistry.Registration textPresentation = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        ModuleSettingRegistry.Registration textBinding = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        Minecraft189ArrayListFeature arrayListFeature = null;
        Minecraft189KeystrokesFeature keystrokesFeature = null;
        Minecraft189FpsFeature fpsFeature = null;
        Minecraft189CpsFeature cpsFeature = null;
        Minecraft189CoordinatesFeature coordinatesFeature = null;
        Minecraft189SpeedFeature speedFeature = null;

        final Minecraft189WatermarkModule watermark =
                new Minecraft189WatermarkModule(
                        renderPipeline,
                        hostCallbacks);
        try {
            category =
                    categories.register(
                            new ModuleCategoryDescriptor(
                                    VISUALS_CATEGORY_ID,
                                    "Visuals",
                                    30));
            module =
                    modules.register(
                            watermark);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189WatermarkModule.ID,
                                    "Watermark",
                                    "Shows a configurable client label in the HUD.",
                                    VISUALS_CATEGORY_ID,
                                    0));

            textSetting =
                    settings.register(
                            watermark.textSetting());
            xSetting =
                    settings.register(
                            watermark.xSetting());
            ySetting =
                    settings.register(
                            watermark.ySetting());

            textPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WatermarkModule.TEXT_SETTING_ID,
                                    "Text",
                                    SettingValueKind.TEXT,
                                    0));
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WatermarkModule.X_SETTING_ID,
                                    "X",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            yPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189WatermarkModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    20,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));

            textBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WatermarkModule.ID,
                                    Minecraft189WatermarkModule.TEXT_SETTING_ID,
                                    0));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WatermarkModule.ID,
                                    Minecraft189WatermarkModule.X_SETTING_ID,
                                    10));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189WatermarkModule.ID,
                                    Minecraft189WatermarkModule.Y_SETTING_ID,
                                    20));

            arrayListFeature =
                    Minecraft189ArrayListFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            renderPipeline,
                            hostCallbacks);

            keystrokesFeature =
                    Minecraft189KeystrokesFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState,
                            renderPipeline,
                            hostCallbacks);

            fpsFeature =
                    Minecraft189FpsFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            frameRateTracker,
                            renderPipeline,
                            hostCallbacks);

            cpsFeature =
                    Minecraft189CpsFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            clickRateTracker,
                            renderPipeline,
                            hostCallbacks);

            coordinatesFeature =
                    Minecraft189CoordinatesFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            playerPositionState,
                            renderPipeline,
                            hostCallbacks);

            speedFeature =
                    Minecraft189SpeedFeature.install(
                            modules,
                            moduleController,
                            presentations,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            movementSpeedTracker,
                            renderPipeline,
                            hostCallbacks);

            return new Minecraft189FeatureCatalog(
                    modules,
                    moduleController,
                    presentations,
                    moduleSettings,
                    settings,
                    settingPresentations,
                    watermark,
                    module,
                    presentation,
                    category,
                    textSetting,
                    xSetting,
                    ySetting,
                    textPresentation,
                    xPresentation,
                    yPresentation,
                    textBinding,
                    xBinding,
                    yBinding,
                    arrayListFeature,
                    keystrokesFeature,
                    fpsFeature,
                    cpsFeature,
                    coordinatesFeature,
                    speedFeature);
        } catch (RuntimeException failure) {
            closeQuietly(speedFeature, failure);
            closeQuietly(coordinatesFeature, failure);
            closeQuietly(cpsFeature, failure);
            closeQuietly(fpsFeature, failure);
            closeQuietly(keystrokesFeature, failure);
            closeQuietly(arrayListFeature, failure);
            closeQuietly(yBinding, failure);
            closeQuietly(xBinding, failure);
            closeQuietly(textBinding, failure);
            closeQuietly(yPresentation, failure);
            closeQuietly(xPresentation, failure);
            closeQuietly(textPresentation, failure);
            closeQuietly(ySetting, failure);
            closeQuietly(xSetting, failure);
            closeQuietly(textSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(module, failure);
            closeQuietly(category, failure);
            throw failure;
        }
    }

    public Minecraft189WatermarkModule watermark() {
        requireOpen();
        return watermark;
    }

    public Minecraft189ArrayListModule arrayList() {
        requireOpen();
        return arrayListFeature.module();
    }

    public Minecraft189KeystrokesModule keystrokes() {
        requireOpen();
        return keystrokesFeature.module();
    }

    public Minecraft189FpsModule fps() {
        requireOpen();
        return fpsFeature.module();
    }

    public Minecraft189CpsModule cps() {
        requireOpen();
        return cpsFeature.module();
    }

    public Minecraft189CoordinatesModule coordinates() {
        requireOpen();
        return coordinatesFeature.module();
    }

    public Minecraft189SpeedModule speed() {
        requireOpen();
        return speedFeature.module();
    }

    public synchronized Minecraft189FullbrightModule installFullbright(
            final dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess settings) {
        requireOpen();
        if (fullbrightFeature != null) {
            throw new IllegalStateException(
                    "fullbright feature already installed");
        }

        fullbrightFeature =
                Minecraft189FullbrightFeature.install(
                        modules,
                        moduleController,
                        modulePresentations,
                        java.util.Objects.requireNonNull(
                                settings,
                                "settings"));
        return fullbrightFeature.module();
    }

    public synchronized Minecraft189FovModule installFovChanger(
            final dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess gameSettings,
            final dev.trexzo.custommc.core.event.EventBus events) {
        requireOpen();
        if (fovFeature != null) {
            throw new IllegalStateException(
                    "FOV feature already installed");
        }

        fovFeature =
                Minecraft189FovFeature.install(
                        modules,
                        moduleController,
                        modulePresentations,
                        moduleSettings,
                        settings,
                        settingPresentations,
                        java.util.Objects.requireNonNull(
                                gameSettings,
                                "gameSettings"),
                        java.util.Objects.requireNonNull(
                                events,
                                "events"));
        return fovFeature.module();
    }

    public synchronized Minecraft189NoBobbingModule installNoBobbing(
            final dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess gameSettings) {
        requireOpen();
        if (noBobbingFeature != null) {
            throw new IllegalStateException(
                    "no-bobbing feature already installed");
        }

        noBobbingFeature =
                Minecraft189NoBobbingFeature.install(
                        modules,
                        moduleController,
                        modulePresentations,
                        java.util.Objects.requireNonNull(
                                gameSettings,
                                "gameSettings"));
        return noBobbingFeature.module();
    }

    public synchronized Minecraft189NoBobbingModule noBobbing() {
        requireOpen();
        if (noBobbingFeature == null) {
            throw new IllegalStateException(
                    "no-bobbing feature is not installed");
        }
        return noBobbingFeature.module();
    }

    public synchronized Minecraft189FovModule fovChanger() {
        requireOpen();
        if (fovFeature == null) {
            throw new IllegalStateException(
                    "FOV feature is not installed");
        }
        return fovFeature.module();
    }

    public synchronized Minecraft189FullbrightModule fullbright() {
        requireOpen();
        if (fullbrightFeature == null) {
            throw new IllegalStateException(
                    "fullbright feature is not installed");
        }
        return fullbrightFeature.module();
    }

    public synchronized boolean closed() {
        return closed;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }

        RuntimeException failure = null;
        if (noBobbingFeature != null) {
            try {
                noBobbingFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = closeFailure;
            }
        }

        if (fovFeature != null) {
            try {
                fovFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = append(
                        failure,
                        closeFailure);
            }
        }

        if (fullbrightFeature != null) {
            try {
                fullbrightFeature.close();
            } catch (RuntimeException closeFailure) {
                failure = append(
                        failure,
                        closeFailure);
            }
        }

        try {
            speedFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            coordinatesFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            cpsFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            fpsFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            keystrokesFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            arrayListFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        try {
            if (moduleController.stateOf(
                    Minecraft189WatermarkModule.ID)
                    != ModuleState.DISABLED) {
                moduleController.disable(
                        Minecraft189WatermarkModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        failure = close(yBinding, failure);
        failure = close(xBinding, failure);
        failure = close(textBinding, failure);
        failure = close(yPresentation, failure);
        failure = close(xPresentation, failure);
        failure = close(textPresentation, failure);
        failure = close(ySetting, failure);
        failure = close(xSetting, failure);
        failure = close(textSetting, failure);
        failure = close(watermarkPresentation, failure);
        failure = close(watermarkRegistration, failure);
        failure = close(visualsCategory, failure);

        if (failure != null) {
            throw failure;
        }
    }

    private synchronized void requireOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "Minecraft 1.8.9 feature catalog is closed");
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
                            "feature registration close failed",
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
