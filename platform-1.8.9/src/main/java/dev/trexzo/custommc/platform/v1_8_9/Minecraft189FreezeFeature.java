package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;

final class Minecraft189FreezeFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FreezeModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration horizontalSetting;
    private final SettingRegistry.Registration verticalSetting;
    private final SettingRegistry.Registration clickGuiSetting;
    private final SettingPresentationRegistry.Registration horizontalPresentation;
    private final SettingPresentationRegistry.Registration verticalPresentation;
    private final SettingPresentationRegistry.Registration clickGuiPresentation;
    private final ModuleSettingRegistry.Registration horizontalBinding;
    private final ModuleSettingRegistry.Registration verticalBinding;
    private final ModuleSettingRegistry.Registration clickGuiBinding;
    private boolean closed;

    private Minecraft189FreezeFeature(
            final ModuleController controller,
            final Minecraft189FreezeModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration horizontalSetting,
            final SettingRegistry.Registration verticalSetting,
            final SettingRegistry.Registration clickGuiSetting,
            final SettingPresentationRegistry.Registration horizontalPresentation,
            final SettingPresentationRegistry.Registration verticalPresentation,
            final SettingPresentationRegistry.Registration clickGuiPresentation,
            final ModuleSettingRegistry.Registration horizontalBinding,
            final ModuleSettingRegistry.Registration verticalBinding,
            final ModuleSettingRegistry.Registration clickGuiBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.horizontalSetting = horizontalSetting;
        this.verticalSetting = verticalSetting;
        this.clickGuiSetting = clickGuiSetting;
        this.horizontalPresentation = horizontalPresentation;
        this.verticalPresentation = verticalPresentation;
        this.clickGuiPresentation = clickGuiPresentation;
        this.horizontalBinding = horizontalBinding;
        this.verticalBinding = verticalBinding;
        this.clickGuiBinding = clickGuiBinding;
    }

    static Minecraft189FreezeFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189FreezeModule module =
                new Minecraft189FreezeModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration horizontalSetting = null;
        SettingRegistry.Registration verticalSetting = null;
        SettingRegistry.Registration clickGuiSetting = null;
        SettingPresentationRegistry.Registration horizontalPresentation = null;
        SettingPresentationRegistry.Registration verticalPresentation = null;
        SettingPresentationRegistry.Registration clickGuiPresentation = null;
        ModuleSettingRegistry.Registration horizontalBinding = null;
        ModuleSettingRegistry.Registration verticalBinding = null;
        ModuleSettingRegistry.Registration clickGuiBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189FreezeModule.ID,
                                    "Freeze",
                                    "Stops selected horizontal and/or vertical local mapped motion.",
                                    Minecraft189FeatureCatalog
                                            .MOVEMENT_CATEGORY_ID,
                                    120));
            horizontalSetting = settings.register(module.freezeHorizontalSetting());
            verticalSetting = settings.register(module.freezeVerticalSetting());
            clickGuiSetting = settings.register(module.pauseWhileClickGuiSetting());
            horizontalPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FreezeModule.FREEZE_HORIZONTAL_SETTING_ID,
                            "Freeze Horizontal (X/Z)", SettingValueKind.BOOLEAN, 0));
            verticalPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FreezeModule.FREEZE_VERTICAL_SETTING_ID,
                            "Freeze Vertical (Y)", SettingValueKind.BOOLEAN, 10));
            clickGuiPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189FreezeModule.PAUSE_WHILE_CLICKGUI_SETTING_ID,
                            "Pause During ClickGUI", SettingValueKind.BOOLEAN, 20));
            horizontalBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FreezeModule.ID,
                            Minecraft189FreezeModule.FREEZE_HORIZONTAL_SETTING_ID, 0));
            verticalBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FreezeModule.ID,
                            Minecraft189FreezeModule.FREEZE_VERTICAL_SETTING_ID, 10));
            clickGuiBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189FreezeModule.ID,
                            Minecraft189FreezeModule.PAUSE_WHILE_CLICKGUI_SETTING_ID, 20));

            return new Minecraft189FreezeFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    horizontalSetting,
                    verticalSetting,
                    clickGuiSetting,
                    horizontalPresentation,
                    verticalPresentation,
                    clickGuiPresentation,
                    horizontalBinding,
                    verticalBinding,
                    clickGuiBinding);
        } catch (RuntimeException failure) {
            closeQuietly(clickGuiBinding, failure);
            closeQuietly(clickGuiPresentation, failure);
            closeQuietly(clickGuiSetting, failure);
            closeQuietly(verticalBinding, failure);
            closeQuietly(horizontalBinding, failure);
            closeQuietly(verticalPresentation, failure);
            closeQuietly(horizontalPresentation, failure);
            closeQuietly(verticalSetting, failure);
            closeQuietly(horizontalSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189FreezeModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "freeze feature is closed");
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
                    Minecraft189FreezeModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189FreezeModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(clickGuiBinding, failure);
        failure = close(clickGuiPresentation, failure);
        failure = close(clickGuiSetting, failure);
        failure = close(verticalBinding, failure);
        failure = close(horizontalBinding, failure);
        failure = close(verticalPresentation, failure);
        failure = close(horizontalPresentation, failure);
        failure = close(verticalSetting, failure);
        failure = close(horizontalSetting, failure);

        try {
            presentation.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }
        try {
            moduleRegistration.close();
        } catch (RuntimeException closeFailure) {
            failure = append(
                    failure,
                    closeFailure);
        }

        if (failure != null) {
            throw failure;
        }
    }

    private static RuntimeException close(
            final AutoCloseable resource,
            final RuntimeException primary) {
        if (resource == null) return primary;
        try {
            resource.close();
            return primary;
        } catch (RuntimeException failure) {
            return append(primary, failure);
        } catch (Exception failure) {
            return append(primary,
                    new IllegalStateException("Freeze registration close failed", failure));
        }
    }

    private static void closeQuietly(
            final AutoCloseable resource,
            final RuntimeException primary) {
        if (resource == null) return;
        try {
            resource.close();
        } catch (Exception failure) {
            primary.addSuppressed(failure);
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
