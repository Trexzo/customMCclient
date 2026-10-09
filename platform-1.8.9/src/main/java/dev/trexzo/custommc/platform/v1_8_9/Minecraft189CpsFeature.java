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

final class Minecraft189CpsFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189CpsModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final SettingRegistry.Registration compactSetting;
    private final SettingRegistry.Registration showTotalSetting;
    private final SettingPresentationRegistry.Registration compactPresentation;
    private final SettingPresentationRegistry.Registration showTotalPresentation;
    private final ModuleSettingRegistry.Registration compactBinding;
    private final ModuleSettingRegistry.Registration showTotalBinding;
    private final SettingRegistry.Registration showBarsSetting;
    private final SettingRegistry.Registration barScaleSetting;
    private final SettingPresentationRegistry.Registration showBarsPresentation;
    private final SettingPresentationRegistry.Registration barScalePresentation;
    private final ModuleSettingRegistry.Registration showBarsBinding;
    private final ModuleSettingRegistry.Registration barScaleBinding;
    private boolean closed;

    private Minecraft189CpsFeature(
            final ModuleController controller,
            final Minecraft189CpsModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final SettingRegistry.Registration compactSetting,
            final SettingRegistry.Registration showTotalSetting,
            final SettingPresentationRegistry.Registration compactPresentation,
            final SettingPresentationRegistry.Registration showTotalPresentation,
            final ModuleSettingRegistry.Registration compactBinding,
            final ModuleSettingRegistry.Registration showTotalBinding,
            final SettingRegistry.Registration showBarsSetting,
            final SettingRegistry.Registration barScaleSetting,
            final SettingPresentationRegistry.Registration showBarsPresentation,
            final SettingPresentationRegistry.Registration barScalePresentation,
            final ModuleSettingRegistry.Registration showBarsBinding,
            final ModuleSettingRegistry.Registration barScaleBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.compactSetting = compactSetting;
        this.showTotalSetting = showTotalSetting;
        this.compactPresentation = compactPresentation;
        this.showTotalPresentation = showTotalPresentation;
        this.compactBinding = compactBinding;
        this.showTotalBinding = showTotalBinding;
        this.showBarsSetting = showBarsSetting;
        this.barScaleSetting = barScaleSetting;
        this.showBarsPresentation = showBarsPresentation;
        this.barScalePresentation = barScalePresentation;
        this.showBarsBinding = showBarsBinding;
        this.barScaleBinding = barScaleBinding;
    }

    static Minecraft189CpsFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189ClickRateTracker clickRateTracker,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189CpsModule module =
                new Minecraft189CpsModule(
                        clickRateTracker,
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        SettingRegistry.Registration compactSetting = null;
        SettingRegistry.Registration showTotalSetting = null;
        SettingPresentationRegistry.Registration compactPresentation = null;
        SettingPresentationRegistry.Registration showTotalPresentation = null;
        ModuleSettingRegistry.Registration compactBinding = null;
        ModuleSettingRegistry.Registration showTotalBinding = null;
        SettingRegistry.Registration showBarsSetting = null;
        SettingRegistry.Registration barScaleSetting = null;
        SettingPresentationRegistry.Registration showBarsPresentation = null;
        SettingPresentationRegistry.Registration barScalePresentation = null;
        ModuleSettingRegistry.Registration showBarsBinding = null;
        ModuleSettingRegistry.Registration barScaleBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189CpsModule.ID,
                                    "CPS",
                                    "Shows rolling left and right clicks per second.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    17));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            compactSetting = settings.register(module.compactSetting());
            showTotalSetting = settings.register(module.showTotalSetting());
            showBarsSetting = settings.register(module.showBarsSetting());
            barScaleSetting = settings.register(module.barScaleSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189CpsModule.X_SETTING_ID,
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
                                    Minecraft189CpsModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            compactPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189CpsModule.COMPACT_SETTING_ID,
                            "Compact", SettingValueKind.BOOLEAN, 20));
            showTotalPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189CpsModule.SHOW_TOTAL_SETTING_ID,
                            "Show Total", SettingValueKind.BOOLEAN, 30));
            showBarsPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189CpsModule.SHOW_BARS_SETTING_ID,
                            "Show CPS Bars", SettingValueKind.BOOLEAN, 40));
            barScalePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189CpsModule.BAR_SCALE_SETTING_ID,
                            "Bar Scale CPS", SettingValueKind.INTEGER, 50,
                            new SettingNumericSpec(1.0D,
                                    Minecraft189CpsModule.MAX_BAR_SCALE, 1.0D)));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189CpsModule.ID,
                                    Minecraft189CpsModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189CpsModule.ID,
                                    Minecraft189CpsModule.Y_SETTING_ID,
                                    10));

            compactBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189CpsModule.ID,
                            Minecraft189CpsModule.COMPACT_SETTING_ID, 20));
            showTotalBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189CpsModule.ID,
                            Minecraft189CpsModule.SHOW_TOTAL_SETTING_ID, 30));

            showBarsBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189CpsModule.ID,
                            Minecraft189CpsModule.SHOW_BARS_SETTING_ID, 40));
            barScaleBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189CpsModule.ID,
                            Minecraft189CpsModule.BAR_SCALE_SETTING_ID, 50));

            return new Minecraft189CpsFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    xPresentation,
                    yPresentation,
                    xBinding,
                    yBinding,
                    compactSetting,
                    showTotalSetting,
                    compactPresentation,
                    showTotalPresentation,
                    compactBinding,
                    showTotalBinding,
                    showBarsSetting,
                    barScaleSetting,
                    showBarsPresentation,
                    barScalePresentation,
                    showBarsBinding,
                    barScaleBinding);
        } catch (RuntimeException failure) {
            closeQuietly(barScaleBinding, failure);
            closeQuietly(showBarsBinding, failure);
            closeQuietly(barScalePresentation, failure);
            closeQuietly(showBarsPresentation, failure);
            closeQuietly(barScaleSetting, failure);
            closeQuietly(showBarsSetting, failure);
            closeQuietly(showTotalBinding, failure);
            closeQuietly(compactBinding, failure);
            closeQuietly(showTotalPresentation, failure);
            closeQuietly(compactPresentation, failure);
            closeQuietly(showTotalSetting, failure);
            closeQuietly(compactSetting, failure);
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

    Minecraft189CpsModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "cps feature is closed");
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
                    Minecraft189CpsModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189CpsModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(barScaleBinding, failure);
        failure = close(showBarsBinding, failure);
        failure = close(barScalePresentation, failure);
        failure = close(showBarsPresentation, failure);
        failure = close(barScaleSetting, failure);
        failure = close(showBarsSetting, failure);
        failure = close(showTotalBinding, failure);
        failure = close(compactBinding, failure);
        failure = close(showTotalPresentation, failure);
        failure = close(compactPresentation, failure);
        failure = close(showTotalSetting, failure);
        failure = close(compactSetting, failure);
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
                            "cps feature close failed",
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
