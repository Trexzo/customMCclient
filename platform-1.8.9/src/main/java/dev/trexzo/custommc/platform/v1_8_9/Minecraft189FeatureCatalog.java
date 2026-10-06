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

    private final ModuleController moduleController;
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
    private boolean closed;

    private Minecraft189FeatureCatalog(
            final ModuleController moduleController,
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
            final Minecraft189KeystrokesFeature keystrokesFeature) {
        this.moduleController = moduleController;
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

            return new Minecraft189FeatureCatalog(
                    moduleController,
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
                    keystrokesFeature);
        } catch (RuntimeException failure) {
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
        try {
            keystrokesFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        try {
            arrayListFeature.close();
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        try {
            if (moduleController.stateOf(
                    Minecraft189WatermarkModule.ID)
                    != ModuleState.DISABLED) {
                moduleController.disable(
                        Minecraft189WatermarkModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
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
