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

final class Minecraft189KeystrokesFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189KeystrokesModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final SettingRegistry.Registration showSpaceSetting;
    private final SettingRegistry.Registration showShiftSetting;
    private final SettingPresentationRegistry.Registration showSpacePresentation;
    private final SettingPresentationRegistry.Registration showShiftPresentation;
    private final ModuleSettingRegistry.Registration showSpaceBinding;
    private final ModuleSettingRegistry.Registration showShiftBinding;
    private final SettingRegistry.Registration idleOpacitySetting;
    private final SettingRegistry.Registration pressedOpacitySetting;
    private final SettingPresentationRegistry.Registration idleOpacityPresentation;
    private final SettingPresentationRegistry.Registration pressedOpacityPresentation;
    private final ModuleSettingRegistry.Registration idleOpacityBinding;
    private final ModuleSettingRegistry.Registration pressedOpacityBinding;
    private boolean closed;

    private Minecraft189KeystrokesFeature(
            final ModuleController controller,
            final Minecraft189KeystrokesModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final SettingRegistry.Registration showSpaceSetting,
            final SettingRegistry.Registration showShiftSetting,
            final SettingPresentationRegistry.Registration showSpacePresentation,
            final SettingPresentationRegistry.Registration showShiftPresentation,
            final ModuleSettingRegistry.Registration showSpaceBinding,
            final ModuleSettingRegistry.Registration showShiftBinding,
            final SettingRegistry.Registration idleOpacitySetting,
            final SettingRegistry.Registration pressedOpacitySetting,
            final SettingPresentationRegistry.Registration idleOpacityPresentation,
            final SettingPresentationRegistry.Registration pressedOpacityPresentation,
            final ModuleSettingRegistry.Registration idleOpacityBinding,
            final ModuleSettingRegistry.Registration pressedOpacityBinding) {
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
        this.showSpaceSetting = showSpaceSetting;
        this.showShiftSetting = showShiftSetting;
        this.showSpacePresentation = showSpacePresentation;
        this.showShiftPresentation = showShiftPresentation;
        this.showSpaceBinding = showSpaceBinding;
        this.showShiftBinding = showShiftBinding;
        this.idleOpacitySetting = idleOpacitySetting;
        this.pressedOpacitySetting = pressedOpacitySetting;
        this.idleOpacityPresentation = idleOpacityPresentation;
        this.pressedOpacityPresentation = pressedOpacityPresentation;
        this.idleOpacityBinding = idleOpacityBinding;
        this.pressedOpacityBinding = pressedOpacityBinding;
    }

    static Minecraft189KeystrokesFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189InputState inputState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        final Minecraft189KeystrokesModule module =
                new Minecraft189KeystrokesModule(
                        inputState,
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
        SettingRegistry.Registration showSpaceSetting = null;
        SettingRegistry.Registration showShiftSetting = null;
        SettingPresentationRegistry.Registration showSpacePresentation = null;
        SettingPresentationRegistry.Registration showShiftPresentation = null;
        ModuleSettingRegistry.Registration showSpaceBinding = null;
        ModuleSettingRegistry.Registration showShiftBinding = null;
        SettingRegistry.Registration idleOpacitySetting = null;
        SettingRegistry.Registration pressedOpacitySetting = null;
        SettingPresentationRegistry.Registration idleOpacityPresentation = null;
        SettingPresentationRegistry.Registration pressedOpacityPresentation = null;
        ModuleSettingRegistry.Registration idleOpacityBinding = null;
        ModuleSettingRegistry.Registration pressedOpacityBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189KeystrokesModule.ID,
                                    "Keystrokes",
                                    "Shows live WASD and mouse-button input.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    20));
            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            showSpaceSetting = settings.register(module.showSpaceSetting());
            showShiftSetting = settings.register(module.showShiftSetting());
            idleOpacitySetting = settings.register(module.idleOpacitySetting());
            pressedOpacitySetting = settings.register(module.pressedOpacitySetting());
            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189KeystrokesModule.X_SETTING_ID,
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
                                    Minecraft189KeystrokesModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            showSpacePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189KeystrokesModule.SHOW_SPACE_SETTING_ID,
                            "Show Space", SettingValueKind.BOOLEAN, 20));
            showShiftPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189KeystrokesModule.SHOW_SHIFT_SETTING_ID,
                            "Show Shift", SettingValueKind.BOOLEAN, 30));
            idleOpacityPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189KeystrokesModule.IDLE_OPACITY_SETTING_ID,
                            "Idle Opacity", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(0.0D, 255.0D, 1.0D)));
            pressedOpacityPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189KeystrokesModule.PRESSED_OPACITY_SETTING_ID,
                            "Pressed Opacity", SettingValueKind.INTEGER, 50,
                            new SettingNumericSpec(0.0D, 255.0D, 1.0D)));
            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189KeystrokesModule.ID,
                                    Minecraft189KeystrokesModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189KeystrokesModule.ID,
                                    Minecraft189KeystrokesModule.Y_SETTING_ID,
                                    10));

            showSpaceBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189KeystrokesModule.ID,
                            Minecraft189KeystrokesModule.SHOW_SPACE_SETTING_ID, 20));
            showShiftBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189KeystrokesModule.ID,
                            Minecraft189KeystrokesModule.SHOW_SHIFT_SETTING_ID, 30));
            idleOpacityBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189KeystrokesModule.ID,
                            Minecraft189KeystrokesModule.IDLE_OPACITY_SETTING_ID, 40));
            pressedOpacityBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189KeystrokesModule.ID,
                            Minecraft189KeystrokesModule.PRESSED_OPACITY_SETTING_ID, 50));
            return new Minecraft189KeystrokesFeature(
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
                    showSpaceSetting,
                    showShiftSetting,
                    showSpacePresentation,
                    showShiftPresentation,
                    showSpaceBinding,
                    showShiftBinding,
                    idleOpacitySetting,
                    pressedOpacitySetting,
                    idleOpacityPresentation,
                    pressedOpacityPresentation,
                    idleOpacityBinding,
                    pressedOpacityBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pressedOpacityBinding, failure);
            closeQuietly(idleOpacityBinding, failure);
            closeQuietly(pressedOpacityPresentation, failure);
            closeQuietly(idleOpacityPresentation, failure);
            closeQuietly(pressedOpacitySetting, failure);
            closeQuietly(idleOpacitySetting, failure);
            closeQuietly(showShiftBinding, failure);
            closeQuietly(showSpaceBinding, failure);
            closeQuietly(showShiftPresentation, failure);
            closeQuietly(showSpacePresentation, failure);
            closeQuietly(showShiftSetting, failure);
            closeQuietly(showSpaceSetting, failure);
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

    Minecraft189KeystrokesModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "keystrokes feature is closed");
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
                    Minecraft189KeystrokesModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189KeystrokesModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(pressedOpacityBinding, failure);
        failure = close(idleOpacityBinding, failure);
        failure = close(pressedOpacityPresentation, failure);
        failure = close(idleOpacityPresentation, failure);
        failure = close(pressedOpacitySetting, failure);
        failure = close(idleOpacitySetting, failure);
        failure = close(showShiftBinding, failure);
        failure = close(showSpaceBinding, failure);
        failure = close(showShiftPresentation, failure);
        failure = close(showSpacePresentation, failure);
        failure = close(showShiftSetting, failure);
        failure = close(showSpaceSetting, failure);
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
                            "keystrokes feature close failed",
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
