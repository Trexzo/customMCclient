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
    private final SettingRegistry.Registration sprintExpansionSetting;
    private final SettingRegistry.Registration sprintGapBonusSetting;
    private final SettingPresentationRegistry.Registration lengthPresentation;
    private final SettingPresentationRegistry.Registration gapPresentation;
    private final SettingPresentationRegistry.Registration thicknessPresentation;
    private final SettingPresentationRegistry.Registration dotPresentation;
    private final SettingPresentationRegistry.Registration sprintExpansionPresentation;
    private final SettingPresentationRegistry.Registration sprintGapBonusPresentation;
    private final ModuleSettingRegistry.Registration lengthBinding;
    private final ModuleSettingRegistry.Registration gapBinding;
    private final ModuleSettingRegistry.Registration thicknessBinding;
    private final ModuleSettingRegistry.Registration dotBinding;
    private final ModuleSettingRegistry.Registration sprintExpansionBinding;
    private final ModuleSettingRegistry.Registration sprintGapBonusBinding;
    private final SettingRegistry.Registration outlineSetting;
    private final SettingRegistry.Registration outlineSizeSetting;
    private final SettingPresentationRegistry.Registration outlinePresentation;
    private final SettingPresentationRegistry.Registration outlineSizePresentation;
    private final ModuleSettingRegistry.Registration outlineBinding;
    private final ModuleSettingRegistry.Registration outlineSizeBinding;
    private final SettingRegistry.Registration redSetting;
    private final SettingRegistry.Registration greenSetting;
    private final SettingRegistry.Registration blueSetting;
    private final SettingPresentationRegistry.Registration redPresentation;
    private final SettingPresentationRegistry.Registration greenPresentation;
    private final SettingPresentationRegistry.Registration bluePresentation;
    private final ModuleSettingRegistry.Registration redBinding;
    private final ModuleSettingRegistry.Registration greenBinding;
    private final ModuleSettingRegistry.Registration blueBinding;
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
            final SettingRegistry.Registration sprintExpansionSetting,
            final SettingRegistry.Registration sprintGapBonusSetting,
            final SettingPresentationRegistry.Registration lengthPresentation,
            final SettingPresentationRegistry.Registration gapPresentation,
            final SettingPresentationRegistry.Registration thicknessPresentation,
            final SettingPresentationRegistry.Registration dotPresentation,
            final SettingPresentationRegistry.Registration sprintExpansionPresentation,
            final SettingPresentationRegistry.Registration sprintGapBonusPresentation,
            final ModuleSettingRegistry.Registration lengthBinding,
            final ModuleSettingRegistry.Registration gapBinding,
            final ModuleSettingRegistry.Registration thicknessBinding,
            final ModuleSettingRegistry.Registration dotBinding,
            final ModuleSettingRegistry.Registration sprintExpansionBinding,
            final ModuleSettingRegistry.Registration sprintGapBonusBinding,
            final SettingRegistry.Registration outlineSetting,
            final SettingRegistry.Registration outlineSizeSetting,
            final SettingPresentationRegistry.Registration outlinePresentation,
            final SettingPresentationRegistry.Registration outlineSizePresentation,
            final ModuleSettingRegistry.Registration outlineBinding,
            final ModuleSettingRegistry.Registration outlineSizeBinding,
            final SettingRegistry.Registration redSetting,
            final SettingRegistry.Registration greenSetting,
            final SettingRegistry.Registration blueSetting,
            final SettingPresentationRegistry.Registration redPresentation,
            final SettingPresentationRegistry.Registration greenPresentation,
            final SettingPresentationRegistry.Registration bluePresentation,
            final ModuleSettingRegistry.Registration redBinding,
            final ModuleSettingRegistry.Registration greenBinding,
            final ModuleSettingRegistry.Registration blueBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.lengthSetting = lengthSetting;
        this.gapSetting = gapSetting;
        this.thicknessSetting = thicknessSetting;
        this.dotSetting = dotSetting;
        this.sprintExpansionSetting = sprintExpansionSetting;
        this.sprintGapBonusSetting = sprintGapBonusSetting;
        this.lengthPresentation = lengthPresentation;
        this.gapPresentation = gapPresentation;
        this.thicknessPresentation = thicknessPresentation;
        this.dotPresentation = dotPresentation;
        this.sprintExpansionPresentation = sprintExpansionPresentation;
        this.sprintGapBonusPresentation = sprintGapBonusPresentation;
        this.lengthBinding = lengthBinding;
        this.gapBinding = gapBinding;
        this.thicknessBinding = thicknessBinding;
        this.dotBinding = dotBinding;
        this.sprintExpansionBinding = sprintExpansionBinding;
        this.sprintGapBonusBinding = sprintGapBonusBinding;
        this.outlineSetting = outlineSetting;
        this.outlineSizeSetting = outlineSizeSetting;
        this.outlinePresentation = outlinePresentation;
        this.outlineSizePresentation = outlineSizePresentation;
        this.outlineBinding = outlineBinding;
        this.outlineSizeBinding = outlineSizeBinding;
        this.redSetting = redSetting;
        this.greenSetting = greenSetting;
        this.blueSetting = blueSetting;
        this.redPresentation = redPresentation;
        this.greenPresentation = greenPresentation;
        this.bluePresentation = bluePresentation;
        this.redBinding = redBinding;
        this.greenBinding = greenBinding;
        this.blueBinding = blueBinding;
    }

    static Minecraft189CrosshairFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189PlayerMovementState movementState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189CrosshairModule module =
                new Minecraft189CrosshairModule(
                        renderPipeline,
                        hostCallbacks,
                        movementState);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration lengthSetting = null;
        SettingRegistry.Registration gapSetting = null;
        SettingRegistry.Registration thicknessSetting = null;
        SettingRegistry.Registration dotSetting = null;
        SettingRegistry.Registration sprintExpansionSetting = null;
        SettingRegistry.Registration sprintGapBonusSetting = null;
        SettingPresentationRegistry.Registration lengthPresentation = null;
        SettingPresentationRegistry.Registration gapPresentation = null;
        SettingPresentationRegistry.Registration thicknessPresentation = null;
        SettingPresentationRegistry.Registration dotPresentation = null;
        SettingPresentationRegistry.Registration sprintExpansionPresentation = null;
        SettingPresentationRegistry.Registration sprintGapBonusPresentation = null;
        ModuleSettingRegistry.Registration lengthBinding = null;
        ModuleSettingRegistry.Registration gapBinding = null;
        ModuleSettingRegistry.Registration thicknessBinding = null;
        ModuleSettingRegistry.Registration dotBinding = null;
        ModuleSettingRegistry.Registration sprintExpansionBinding = null;
        ModuleSettingRegistry.Registration sprintGapBonusBinding = null;
        SettingRegistry.Registration outlineSetting = null;
        SettingRegistry.Registration outlineSizeSetting = null;
        SettingPresentationRegistry.Registration outlinePresentation = null;
        SettingPresentationRegistry.Registration outlineSizePresentation = null;
        ModuleSettingRegistry.Registration outlineBinding = null;
        ModuleSettingRegistry.Registration outlineSizeBinding = null;
        SettingRegistry.Registration redSetting = null;
        SettingRegistry.Registration greenSetting = null;
        SettingRegistry.Registration blueSetting = null;
        SettingPresentationRegistry.Registration redPresentation = null;
        SettingPresentationRegistry.Registration greenPresentation = null;
        SettingPresentationRegistry.Registration bluePresentation = null;
        ModuleSettingRegistry.Registration redBinding = null;
        ModuleSettingRegistry.Registration greenBinding = null;
        ModuleSettingRegistry.Registration blueBinding = null;

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
            sprintExpansionSetting = settings.register(module.sprintExpansionSetting());
            sprintGapBonusSetting = settings.register(module.sprintGapBonusSetting());
            outlineSetting = settings.register(module.outlineSetting());
            outlineSizeSetting = settings.register(module.outlineSizeSetting());
            redSetting = settings.register(module.redSetting());
            greenSetting = settings.register(module.greenSetting());
            blueSetting = settings.register(module.blueSetting());

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

            sprintExpansionPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189CrosshairModule.SPRINT_EXPANSION_SETTING_ID,
                            "Sprint Expansion", SettingValueKind.BOOLEAN, 40));
            sprintGapBonusPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189CrosshairModule.SPRINT_GAP_BONUS_SETTING_ID,
                            "Sprint Gap Bonus", SettingValueKind.INTEGER, 50,
                            new SettingNumericSpec(1.0D, 12.0D, 1.0D)));

            outlinePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189CrosshairModule.OUTLINE_SETTING_ID,
                            "Outline", SettingValueKind.BOOLEAN, 60));
            outlineSizePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189CrosshairModule.OUTLINE_SIZE_SETTING_ID,
                            "Outline Size", SettingValueKind.INTEGER, 70,
                            new SettingNumericSpec(1.0D, 3.0D, 1.0D)));

            redPresentation = settingPresentations.register(
                    new SettingDescriptor(Minecraft189CrosshairModule.RED_SETTING_ID,
                            "Red", SettingValueKind.INTEGER, 80,
                            new SettingNumericSpec(0.0D, 255.0D, 1.0D)));
            greenPresentation = settingPresentations.register(
                    new SettingDescriptor(Minecraft189CrosshairModule.GREEN_SETTING_ID,
                            "Green", SettingValueKind.INTEGER, 90,
                            new SettingNumericSpec(0.0D, 255.0D, 1.0D)));
            bluePresentation = settingPresentations.register(
                    new SettingDescriptor(Minecraft189CrosshairModule.BLUE_SETTING_ID,
                            "Blue", SettingValueKind.INTEGER, 100,
                            new SettingNumericSpec(0.0D, 255.0D, 1.0D)));
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

            sprintExpansionBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189CrosshairModule.ID,
                            Minecraft189CrosshairModule.SPRINT_EXPANSION_SETTING_ID, 40));
            sprintGapBonusBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189CrosshairModule.ID,
                            Minecraft189CrosshairModule.SPRINT_GAP_BONUS_SETTING_ID, 50));

            outlineBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189CrosshairModule.ID,
                            Minecraft189CrosshairModule.OUTLINE_SETTING_ID, 60));
            outlineSizeBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189CrosshairModule.ID,
                            Minecraft189CrosshairModule.OUTLINE_SIZE_SETTING_ID, 70));

            redBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189CrosshairModule.ID,
                    Minecraft189CrosshairModule.RED_SETTING_ID, 80));
            greenBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189CrosshairModule.ID,
                    Minecraft189CrosshairModule.GREEN_SETTING_ID, 90));
            blueBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189CrosshairModule.ID,
                    Minecraft189CrosshairModule.BLUE_SETTING_ID, 100));

            return new Minecraft189CrosshairFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    lengthSetting,
                    gapSetting,
                    thicknessSetting,
                    dotSetting,
                    sprintExpansionSetting,
                    sprintGapBonusSetting,
                    lengthPresentation,
                    gapPresentation,
                    thicknessPresentation,
                    dotPresentation,
                    sprintExpansionPresentation,
                    sprintGapBonusPresentation,
                    lengthBinding,
                    gapBinding,
                    thicknessBinding,
                    dotBinding,
                    sprintExpansionBinding,
                    sprintGapBonusBinding,
                    outlineSetting,
                    outlineSizeSetting,
                    outlinePresentation,
                    outlineSizePresentation,
                    outlineBinding,
                    outlineSizeBinding,
                    redSetting, greenSetting, blueSetting,
                    redPresentation, greenPresentation, bluePresentation,
                    redBinding, greenBinding, blueBinding);
        } catch (RuntimeException failure) {
            closeQuietly(blueBinding, failure);
            closeQuietly(greenBinding, failure);
            closeQuietly(redBinding, failure);
            closeQuietly(bluePresentation, failure);
            closeQuietly(greenPresentation, failure);
            closeQuietly(redPresentation, failure);
            closeQuietly(blueSetting, failure);
            closeQuietly(greenSetting, failure);
            closeQuietly(redSetting, failure);
            closeQuietly(outlineSizeBinding, failure);
            closeQuietly(outlineBinding, failure);
            closeQuietly(outlineSizePresentation, failure);
            closeQuietly(outlinePresentation, failure);
            closeQuietly(outlineSizeSetting, failure);
            closeQuietly(outlineSetting, failure);
            closeQuietly(sprintGapBonusBinding, failure);
            closeQuietly(sprintExpansionBinding, failure);
            closeQuietly(dotBinding, failure);
            closeQuietly(thicknessBinding, failure);
            closeQuietly(gapBinding, failure);
            closeQuietly(lengthBinding, failure);
            closeQuietly(sprintGapBonusPresentation, failure);
            closeQuietly(sprintExpansionPresentation, failure);
            closeQuietly(dotPresentation, failure);
            closeQuietly(thicknessPresentation, failure);
            closeQuietly(gapPresentation, failure);
            closeQuietly(lengthPresentation, failure);
            closeQuietly(sprintGapBonusSetting, failure);
            closeQuietly(sprintExpansionSetting, failure);
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

        failure = close(blueBinding, failure);
        failure = close(greenBinding, failure);
        failure = close(redBinding, failure);
        failure = close(bluePresentation, failure);
        failure = close(greenPresentation, failure);
        failure = close(redPresentation, failure);
        failure = close(blueSetting, failure);
        failure = close(greenSetting, failure);
        failure = close(redSetting, failure);
        failure = close(outlineSizeBinding, failure);
        failure = close(outlineBinding, failure);
        failure = close(outlineSizePresentation, failure);
        failure = close(outlinePresentation, failure);
        failure = close(outlineSizeSetting, failure);
        failure = close(outlineSetting, failure);
        failure = close(sprintGapBonusBinding, failure);
        failure = close(sprintExpansionBinding, failure);
        failure = close(dotBinding, failure);
        failure = close(thicknessBinding, failure);
        failure = close(gapBinding, failure);
        failure = close(lengthBinding, failure);
        failure = close(sprintGapBonusPresentation, failure);
        failure = close(sprintExpansionPresentation, failure);
        failure = close(dotPresentation, failure);
        failure = close(thicknessPresentation, failure);
        failure = close(gapPresentation, failure);
        failure = close(lengthPresentation, failure);
        failure = close(sprintGapBonusSetting, failure);
        failure = close(sprintExpansionSetting, failure);
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
