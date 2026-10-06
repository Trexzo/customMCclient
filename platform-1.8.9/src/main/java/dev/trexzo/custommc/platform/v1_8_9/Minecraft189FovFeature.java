package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;

final class Minecraft189FovFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189FovModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration valueSetting;
    private final SettingPresentationRegistry.Registration valuePresentation;
    private final ModuleSettingRegistry.Registration valueBinding;
    private boolean closed;

    private Minecraft189FovFeature(
            final ModuleController controller,
            final Minecraft189FovModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration valueSetting,
            final SettingPresentationRegistry.Registration valuePresentation,
            final ModuleSettingRegistry.Registration valueBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.valueSetting = valueSetting;
        this.valuePresentation = valuePresentation;
        this.valueBinding = valueBinding;
    }

    static Minecraft189FovFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final Minecraft189GuiSettingsAccess gameSettings,
            final EventBus events) {
        final Minecraft189FovModule module =
                new Minecraft189FovModule(
                        gameSettings,
                        events);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration valueSetting = null;
        SettingPresentationRegistry.Registration valuePresentation = null;
        ModuleSettingRegistry.Registration valueBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189FovModule.ID,
                                    "FOV Changer",
                                    "Overrides field of view while enabled and restores the previous value when disabled.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    80));
            valueSetting =
                    settings.register(
                            module.targetFovSetting());
            valuePresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189FovModule.VALUE_SETTING_ID,
                                    "FOV",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            30.0D,
                                            179.0D,
                                            1.0D)));
            valueBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189FovModule.ID,
                                    Minecraft189FovModule.VALUE_SETTING_ID,
                                    0));

            return new Minecraft189FovFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    valueSetting,
                    valuePresentation,
                    valueBinding);
        } catch (RuntimeException failure) {
            closeQuietly(
                    valueBinding,
                    failure);
            closeQuietly(
                    valuePresentation,
                    failure);
            closeQuietly(
                    valueSetting,
                    failure);
            closeQuietly(
                    presentation,
                    failure);
            closeQuietly(
                    moduleRegistration,
                    failure);
            throw failure;
        }
    }

    Minecraft189FovModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "FOV feature is closed");
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
                    Minecraft189FovModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189FovModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(
                valueBinding,
                failure);
        failure = close(
                valuePresentation,
                failure);
        failure = close(
                valueSetting,
                failure);
        failure = close(
                presentation,
                failure);
        failure = close(
                moduleRegistration,
                failure);

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
                            "FOV feature close failed",
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
