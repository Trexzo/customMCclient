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

final class Minecraft189HurtTimeFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189HurtTimeModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final SettingRegistry.Registration showMeterSetting;
    private final SettingRegistry.Registration meterMaxSetting;
    private final SettingPresentationRegistry.Registration showMeterPresentation;
    private final SettingPresentationRegistry.Registration meterMaxPresentation;
    private final ModuleSettingRegistry.Registration showMeterBinding;
    private final ModuleSettingRegistry.Registration meterMaxBinding;
    private boolean closed;

    private Minecraft189HurtTimeFeature(
            final ModuleController controller,
            final Minecraft189HurtTimeModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final SettingRegistry.Registration showMeterSetting,
            final SettingRegistry.Registration meterMaxSetting,
            final SettingPresentationRegistry.Registration showMeterPresentation,
            final SettingPresentationRegistry.Registration meterMaxPresentation,
            final ModuleSettingRegistry.Registration showMeterBinding,
            final ModuleSettingRegistry.Registration meterMaxBinding) {
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
        this.showMeterSetting = showMeterSetting;
        this.meterMaxSetting = meterMaxSetting;
        this.showMeterPresentation = showMeterPresentation;
        this.meterMaxPresentation = meterMaxPresentation;
        this.showMeterBinding = showMeterBinding;
        this.meterMaxBinding = meterMaxBinding;
    }

    static Minecraft189HurtTimeFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189PlayerHurtTimeState hurtTimeState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189HurtTimeModule module =
                new Minecraft189HurtTimeModule(
                        hurtTimeState,
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
        SettingRegistry.Registration showMeterSetting = null;
        SettingRegistry.Registration meterMaxSetting = null;
        SettingPresentationRegistry.Registration showMeterPresentation = null;
        SettingPresentationRegistry.Registration meterMaxPresentation = null;
        ModuleSettingRegistry.Registration showMeterBinding = null;
        ModuleSettingRegistry.Registration meterMaxBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189HurtTimeModule.ID,
                                    "Hurt Time",
                                    "Shows the live local-player hurt-time counter.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    59));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            showMeterSetting = settings.register(module.showMeterSetting());
            meterMaxSetting = settings.register(module.meterMaxSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189HurtTimeModule.X_SETTING_ID,
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
                                    Minecraft189HurtTimeModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            showMeterPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HurtTimeModule.SHOW_METER_SETTING_ID,
                            "Show Meter", SettingValueKind.BOOLEAN, 20));
            meterMaxPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HurtTimeModule.METER_MAX_SETTING_ID,
                            "Meter Maximum", SettingValueKind.INTEGER, 30,
                            new SettingNumericSpec(1.0D,
                                    Minecraft189HurtTimeModule.MAXIMUM_METER_MAX,
                                    1.0D)));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189HurtTimeModule.ID,
                                    Minecraft189HurtTimeModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189HurtTimeModule.ID,
                                    Minecraft189HurtTimeModule.Y_SETTING_ID,
                                    10));

            showMeterBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HurtTimeModule.ID,
                            Minecraft189HurtTimeModule.SHOW_METER_SETTING_ID, 20));
            meterMaxBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HurtTimeModule.ID,
                            Minecraft189HurtTimeModule.METER_MAX_SETTING_ID, 30));

            return new Minecraft189HurtTimeFeature(
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
                    showMeterSetting,
                    meterMaxSetting,
                    showMeterPresentation,
                    meterMaxPresentation,
                    showMeterBinding,
                    meterMaxBinding);
        } catch (RuntimeException failure) {
            closeQuietly(meterMaxBinding, failure);
            closeQuietly(showMeterBinding, failure);
            closeQuietly(meterMaxPresentation, failure);
            closeQuietly(showMeterPresentation, failure);
            closeQuietly(meterMaxSetting, failure);
            closeQuietly(showMeterSetting, failure);
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

    Minecraft189HurtTimeModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "hurt-time feature is closed");
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
                    Minecraft189HurtTimeModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189HurtTimeModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(meterMaxBinding, failure);
        failure = close(showMeterBinding, failure);
        failure = close(meterMaxPresentation, failure);
        failure = close(showMeterPresentation, failure);
        failure = close(meterMaxSetting, failure);
        failure = close(showMeterSetting, failure);
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
        if (closeable == null) {
            return primary;
        }
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
                            "hurt-time feature close failed",
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
