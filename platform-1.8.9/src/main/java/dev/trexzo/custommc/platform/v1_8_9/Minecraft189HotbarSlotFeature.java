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

final class Minecraft189HotbarSlotFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189HotbarSlotModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final SettingRegistry.Registration showStripSetting;
    private final SettingPresentationRegistry.Registration showStripPresentation;
    private final ModuleSettingRegistry.Registration showStripBinding;
    private boolean closed;

    private Minecraft189HotbarSlotFeature(
            final ModuleController controller,
            final Minecraft189HotbarSlotModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final SettingRegistry.Registration showStripSetting,
            final SettingPresentationRegistry.Registration showStripPresentation,
            final ModuleSettingRegistry.Registration showStripBinding) {
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
        this.showStripSetting = showStripSetting;
        this.showStripPresentation = showStripPresentation;
        this.showStripBinding = showStripBinding;
    }

    static Minecraft189HotbarSlotFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189HotbarSlotState hotbarSlotState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189HotbarSlotModule module =
                new Minecraft189HotbarSlotModule(
                        hotbarSlotState,
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
        SettingRegistry.Registration showStripSetting = null;
        SettingPresentationRegistry.Registration showStripPresentation = null;
        ModuleSettingRegistry.Registration showStripBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189HotbarSlotModule.ID,
                                    "Hotbar Slot",
                                    "Shows the currently selected hotbar slot.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    68));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            showStripSetting = settings.register(module.showStripSetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189HotbarSlotModule.X_SETTING_ID,
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
                                    Minecraft189HotbarSlotModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            showStripPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189HotbarSlotModule.SHOW_STRIP_SETTING_ID,
                            "Show Slot Strip", SettingValueKind.BOOLEAN, 20));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189HotbarSlotModule.ID,
                                    Minecraft189HotbarSlotModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189HotbarSlotModule.ID,
                                    Minecraft189HotbarSlotModule.Y_SETTING_ID,
                                    10));

            showStripBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189HotbarSlotModule.ID,
                            Minecraft189HotbarSlotModule.SHOW_STRIP_SETTING_ID, 20));
            return new Minecraft189HotbarSlotFeature(
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
                    showStripSetting,
                    showStripPresentation,
                    showStripBinding);
        } catch (RuntimeException failure) {
            closeQuietly(showStripBinding, failure);
            closeQuietly(showStripPresentation, failure);
            closeQuietly(showStripSetting, failure);
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

    Minecraft189HotbarSlotModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "hotbar-slot feature is closed");
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
                    Minecraft189HotbarSlotModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189HotbarSlotModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(showStripBinding, failure);
        failure = close(showStripPresentation, failure);
        failure = close(showStripSetting, failure);
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
                            "hotbar-slot feature close failed",
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
