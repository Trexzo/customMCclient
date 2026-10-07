package dev.trexzo.custommc.platform.v1_8_9;

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

final class Minecraft189JitterFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189JitterModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration yawSetting;
    private final SettingRegistry.Registration pitchSetting;
    private final SettingPresentationRegistry.Registration yawPresentation;
    private final SettingPresentationRegistry.Registration pitchPresentation;
    private final ModuleSettingRegistry.Registration yawBinding;
    private final ModuleSettingRegistry.Registration pitchBinding;
    private boolean closed;

    private Minecraft189JitterFeature(
            final ModuleController controller,
            final Minecraft189JitterModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration yawSetting,
            final SettingRegistry.Registration pitchSetting,
            final SettingPresentationRegistry.Registration yawPresentation,
            final SettingPresentationRegistry.Registration pitchPresentation,
            final ModuleSettingRegistry.Registration yawBinding,
            final ModuleSettingRegistry.Registration pitchBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.yawSetting = yawSetting;
        this.pitchSetting = pitchSetting;
        this.yawPresentation = yawPresentation;
        this.pitchPresentation = pitchPresentation;
        this.yawBinding = yawBinding;
        this.pitchBinding = pitchBinding;
    }

    static Minecraft189JitterFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189JitterModule module =
                new Minecraft189JitterModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration yawSetting = null;
        SettingRegistry.Registration pitchSetting = null;
        SettingPresentationRegistry.Registration yawPresentation = null;
        SettingPresentationRegistry.Registration pitchPresentation = null;
        ModuleSettingRegistry.Registration yawBinding = null;
        ModuleSettingRegistry.Registration pitchBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189JitterModule.ID,
                                    "Jitter",
                                    "Alternates small yaw and pitch offsets while physical left click is held.",
                                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID,
                                    30));

            yawSetting =
                    settings.register(
                            module.yawDegreesSetting());
            pitchSetting =
                    settings.register(
                            module.pitchDegreesSetting());
            yawPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189JitterModule.YAW_SETTING_ID,
                                    "Yaw",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189JitterModule.MINIMUM_DEGREES,
                                            Minecraft189JitterModule.MAXIMUM_DEGREES,
                                            0.10D)));
            pitchPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189JitterModule.PITCH_SETTING_ID,
                                    "Pitch",
                                    SettingValueKind.DOUBLE,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189JitterModule.MINIMUM_DEGREES,
                                            Minecraft189JitterModule.MAXIMUM_DEGREES,
                                            0.10D)));
            yawBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189JitterModule.ID,
                                    Minecraft189JitterModule.YAW_SETTING_ID,
                                    0));
            pitchBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189JitterModule.ID,
                                    Minecraft189JitterModule.PITCH_SETTING_ID,
                                    10));

            return new Minecraft189JitterFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    yawSetting,
                    pitchSetting,
                    yawPresentation,
                    pitchPresentation,
                    yawBinding,
                    pitchBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pitchBinding, failure);
            closeQuietly(yawBinding, failure);
            closeQuietly(pitchPresentation, failure);
            closeQuietly(yawPresentation, failure);
            closeQuietly(pitchSetting, failure);
            closeQuietly(yawSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189JitterModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "jitter feature is closed");
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
                    Minecraft189JitterModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189JitterModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(pitchBinding, failure);
        failure = close(yawBinding, failure);
        failure = close(pitchPresentation, failure);
        failure = close(yawPresentation, failure);
        failure = close(pitchSetting, failure);
        failure = close(yawSetting, failure);
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
            return append(primary, failure);
        } catch (Exception failure) {
            return append(
                    primary,
                    new IllegalStateException(
                            "jitter feature close failed",
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
            primary.addSuppressed(cleanupFailure);
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
