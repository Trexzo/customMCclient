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

final class Minecraft189ArmorFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189ArmorModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingRegistry.Registration lowDurabilityWarningSetting;
    private final SettingRegistry.Registration warningPercentSetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final SettingPresentationRegistry.Registration lowDurabilityWarningPresentation;
    private final SettingPresentationRegistry.Registration warningPercentPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final ModuleSettingRegistry.Registration lowDurabilityWarningBinding;
    private final ModuleSettingRegistry.Registration warningPercentBinding;
    private final SettingRegistry.Registration compactSetting;
    private final SettingPresentationRegistry.Registration compactPresentation;
    private final ModuleSettingRegistry.Registration compactBinding;
    private boolean closed;

    private Minecraft189ArmorFeature(
            final ModuleController controller,
            final Minecraft189ArmorModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingRegistry.Registration lowDurabilityWarningSetting,
            final SettingRegistry.Registration warningPercentSetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final SettingPresentationRegistry.Registration lowDurabilityWarningPresentation,
            final SettingPresentationRegistry.Registration warningPercentPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final ModuleSettingRegistry.Registration lowDurabilityWarningBinding,
            final ModuleSettingRegistry.Registration warningPercentBinding,
            final SettingRegistry.Registration compactSetting,
            final SettingPresentationRegistry.Registration compactPresentation,
            final ModuleSettingRegistry.Registration compactBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.lowDurabilityWarningSetting = lowDurabilityWarningSetting;
        this.warningPercentSetting = warningPercentSetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.lowDurabilityWarningPresentation = lowDurabilityWarningPresentation;
        this.warningPercentPresentation = warningPercentPresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.lowDurabilityWarningBinding = lowDurabilityWarningBinding;
        this.warningPercentBinding = warningPercentBinding;
        this.compactSetting = compactSetting;
        this.compactPresentation = compactPresentation;
        this.compactBinding = compactBinding;
    }

    static Minecraft189ArmorFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189PlayerArmorState armorState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189ArmorModule module =
                new Minecraft189ArmorModule(
                        armorState,
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingRegistry.Registration lowDurabilityWarningSetting = null;
        SettingRegistry.Registration warningPercentSetting = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        SettingPresentationRegistry.Registration lowDurabilityWarningPresentation = null;
        SettingPresentationRegistry.Registration warningPercentPresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        ModuleSettingRegistry.Registration lowDurabilityWarningBinding = null;
        ModuleSettingRegistry.Registration warningPercentBinding = null;
        SettingRegistry.Registration compactSetting = null;
        SettingPresentationRegistry.Registration compactPresentation = null;
        ModuleSettingRegistry.Registration compactBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189ArmorModule.ID,
                                    "Armor",
                                    "Shows which armor slots are currently occupied.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    59));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            lowDurabilityWarningSetting = settings.register(
                    module.lowDurabilityWarningSetting());
            warningPercentSetting = settings.register(
                    module.warningPercentSetting());
            compactSetting = settings.register(module.compactSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189ArmorModule.X_SETTING_ID,
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
                                    Minecraft189ArmorModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            lowDurabilityWarningPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ArmorModule.LOW_DURABILITY_WARNING_SETTING_ID,
                            "Low Durability Warning", SettingValueKind.BOOLEAN, 20));
            warningPercentPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ArmorModule.WARNING_PERCENT_SETTING_ID,
                            "Warning Below %", SettingValueKind.INTEGER, 30,
                            new SettingNumericSpec(1.0D, 100.0D, 1.0D)));
            compactPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ArmorModule.COMPACT_SETTING_ID,
                            "Compact Durability", SettingValueKind.BOOLEAN, 40));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189ArmorModule.ID,
                                    Minecraft189ArmorModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189ArmorModule.ID,
                                    Minecraft189ArmorModule.Y_SETTING_ID,
                                    10));

            lowDurabilityWarningBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ArmorModule.ID,
                            Minecraft189ArmorModule.LOW_DURABILITY_WARNING_SETTING_ID, 20));
            warningPercentBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ArmorModule.ID,
                            Minecraft189ArmorModule.WARNING_PERCENT_SETTING_ID, 30));

            compactBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ArmorModule.ID,
                            Minecraft189ArmorModule.COMPACT_SETTING_ID, 40));

            return new Minecraft189ArmorFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    lowDurabilityWarningSetting,
                    warningPercentSetting,
                    xPresentation,
                    yPresentation,
                    lowDurabilityWarningPresentation,
                    warningPercentPresentation,
                    xBinding,
                    yBinding,
                    lowDurabilityWarningBinding,
                    warningPercentBinding,
                    compactSetting,
                    compactPresentation,
                    compactBinding);
        } catch (RuntimeException failure) {
            closeQuietly(compactBinding, failure);
            closeQuietly(compactPresentation, failure);
            closeQuietly(compactSetting, failure);
            closeQuietly(warningPercentBinding, failure);
            closeQuietly(lowDurabilityWarningBinding, failure);
            closeQuietly(warningPercentPresentation, failure);
            closeQuietly(lowDurabilityWarningPresentation, failure);
            closeQuietly(warningPercentSetting, failure);
            closeQuietly(lowDurabilityWarningSetting, failure);
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

    Minecraft189ArmorModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "armor feature is closed");
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
                    Minecraft189ArmorModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189ArmorModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(compactBinding, failure);
        failure = close(compactPresentation, failure);
        failure = close(compactSetting, failure);
        failure = close(warningPercentBinding, failure);
        failure = close(lowDurabilityWarningBinding, failure);
        failure = close(warningPercentPresentation, failure);
        failure = close(lowDurabilityWarningPresentation, failure);
        failure = close(warningPercentSetting, failure);
        failure = close(lowDurabilityWarningSetting, failure);
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
                            "armor feature close failed",
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
