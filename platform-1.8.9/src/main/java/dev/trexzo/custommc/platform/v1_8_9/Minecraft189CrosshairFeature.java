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

final class Minecraft189CrosshairFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189CrosshairModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration lengthSetting;
    private final SettingRegistry.Registration gapSetting;
    private final SettingRegistry.Registration thicknessSetting;
    private final SettingRegistry.Registration dotSetting;
    private final SettingPresentationRegistry.Registration lengthPresentation;
    private final SettingPresentationRegistry.Registration gapPresentation;
    private final SettingPresentationRegistry.Registration thicknessPresentation;
    private final SettingPresentationRegistry.Registration dotPresentation;
    private final ModuleSettingRegistry.Registration lengthBinding;
    private final ModuleSettingRegistry.Registration gapBinding;
    private final ModuleSettingRegistry.Registration thicknessBinding;
    private final ModuleSettingRegistry.Registration dotBinding;
    private boolean closed;

    private Minecraft189CrosshairFeature(
            final ModuleController controller,
            final Minecraft189CrosshairModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration lengthSetting,
            final SettingRegistry.Registration gapSetting,
            final SettingRegistry.Registration thicknessSetting,
            final SettingRegistry.Registration dotSetting,
            final SettingPresentationRegistry.Registration lengthPresentation,
            final SettingPresentationRegistry.Registration gapPresentation,
            final SettingPresentationRegistry.Registration thicknessPresentation,
            final SettingPresentationRegistry.Registration dotPresentation,
            final ModuleSettingRegistry.Registration lengthBinding,
            final ModuleSettingRegistry.Registration gapBinding,
            final ModuleSettingRegistry.Registration thicknessBinding,
            final ModuleSettingRegistry.Registration dotBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.lengthSetting = lengthSetting;
        this.gapSetting = gapSetting;
        this.thicknessSetting = thicknessSetting;
        this.dotSetting = dotSetting;
        this.lengthPresentation = lengthPresentation;
        this.gapPresentation = gapPresentation;
        this.thicknessPresentation = thicknessPresentation;
        this.dotPresentation = dotPresentation;
        this.lengthBinding = lengthBinding;
        this.gapBinding = gapBinding;
        this.thicknessBinding = thicknessBinding;
        this.dotBinding = dotBinding;
    }

    static Minecraft189CrosshairFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189CrosshairModule module =
                new Minecraft189CrosshairModule(
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration lengthSetting = null;
        SettingRegistry.Registration gapSetting = null;
        SettingRegistry.Registration thicknessSetting = null;
        SettingRegistry.Registration dotSetting = null;
        SettingPresentationRegistry.Registration lengthPresentation = null;
        SettingPresentationRegistry.Registration gapPresentation = null;
        SettingPresentationRegistry.Registration thicknessPresentation = null;
        SettingPresentationRegistry.Registration dotPresentation = null;
        ModuleSettingRegistry.Registration lengthBinding = null;
        ModuleSettingRegistry.Registration gapBinding = null;
        ModuleSettingRegistry.Registration thicknessBinding = null;
        ModuleSettingRegistry.Registration dotBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189CrosshairModule.ID,
                                    "Custom Crosshair",
                                    "Draws a configurable crosshair at screen center.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    60));

            lengthSetting =
                    settings.register(
                            module.lengthSetting());
            gapSetting =
                    settings.register(
                            module.gapSetting());
            thicknessSetting =
                    settings.register(
                            module.thicknessSetting());
            dotSetting =
                    settings.register(
                            module.dotSetting());

            lengthPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189CrosshairModule.LENGTH_SETTING_ID,
                                    "Length",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            1.0D,
                                            20.0D,
                                            1.0D)));
            gapPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189CrosshairModule.GAP_SETTING_ID,
                                    "Gap",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            12.0D,
                                            1.0D)));
            thicknessPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189CrosshairModule.THICKNESS_SETTING_ID,
                                    "Thickness",
                                    SettingValueKind.INTEGER,
                                    20,
                                    new SettingNumericSpec(
                                            1.0D,
                                            6.0D,
                                            1.0D)));
            dotPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189CrosshairModule.DOT_SETTING_ID,
                                    "Center Dot",
                                    SettingValueKind.BOOLEAN,
                                    30));

            lengthBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189CrosshairModule.ID,
                                    Minecraft189CrosshairModule.LENGTH_SETTING_ID,
                                    0));
            gapBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189CrosshairModule.ID,
                                    Minecraft189CrosshairModule.GAP_SETTING_ID,
                                    10));
            thicknessBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189CrosshairModule.ID,
                                    Minecraft189CrosshairModule.THICKNESS_SETTING_ID,
                                    20));
            dotBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189CrosshairModule.ID,
                                    Minecraft189CrosshairModule.DOT_SETTING_ID,
                                    30));

            return new Minecraft189CrosshairFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    lengthSetting,
                    gapSetting,
                    thicknessSetting,
                    dotSetting,
                    lengthPresentation,
                    gapPresentation,
                    thicknessPresentation,
                    dotPresentation,
                    lengthBinding,
                    gapBinding,
                    thicknessBinding,
                    dotBinding);
        } catch (RuntimeException failure) {
            closeQuietly(dotBinding, failure);
            closeQuietly(thicknessBinding, failure);
            closeQuietly(gapBinding, failure);
            closeQuietly(lengthBinding, failure);
            closeQuietly(dotPresentation, failure);
            closeQuietly(thicknessPresentation, failure);
            closeQuietly(gapPresentation, failure);
            closeQuietly(lengthPresentation, failure);
            closeQuietly(dotSetting, failure);
            closeQuietly(thicknessSetting, failure);
            closeQuietly(gapSetting, failure);
            closeQuietly(lengthSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189CrosshairModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "crosshair feature is closed");
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
                    Minecraft189CrosshairModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189CrosshairModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(dotBinding, failure);
        failure = close(thicknessBinding, failure);
        failure = close(gapBinding, failure);
        failure = close(lengthBinding, failure);
        failure = close(dotPresentation, failure);
        failure = close(thicknessPresentation, failure);
        failure = close(gapPresentation, failure);
        failure = close(lengthPresentation, failure);
        failure = close(dotSetting, failure);
        failure = close(thicknessSetting, failure);
        failure = close(gapSetting, failure);
        failure = close(lengthSetting, failure);
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
                            "crosshair feature close failed",
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
