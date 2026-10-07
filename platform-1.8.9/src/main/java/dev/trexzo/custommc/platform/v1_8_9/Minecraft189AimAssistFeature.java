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

final class Minecraft189AimAssistFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AimAssistModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration yawSpeedSetting;
    private final SettingRegistry.Registration pitchSpeedSetting;
    private final SettingRegistry.Registration requireHoldSetting;
    private final SettingPresentationRegistry.Registration yawSpeedPresentation;
    private final SettingPresentationRegistry.Registration pitchSpeedPresentation;
    private final SettingPresentationRegistry.Registration requireHoldPresentation;
    private final ModuleSettingRegistry.Registration yawSpeedBinding;
    private final ModuleSettingRegistry.Registration pitchSpeedBinding;
    private final ModuleSettingRegistry.Registration requireHoldBinding;
    private boolean closed;

    private Minecraft189AimAssistFeature(
            final ModuleController controller,
            final Minecraft189AimAssistModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration yawSpeedSetting,
            final SettingRegistry.Registration pitchSpeedSetting,
            final SettingRegistry.Registration requireHoldSetting,
            final SettingPresentationRegistry.Registration yawSpeedPresentation,
            final SettingPresentationRegistry.Registration pitchSpeedPresentation,
            final SettingPresentationRegistry.Registration requireHoldPresentation,
            final ModuleSettingRegistry.Registration yawSpeedBinding,
            final ModuleSettingRegistry.Registration pitchSpeedBinding,
            final ModuleSettingRegistry.Registration requireHoldBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.yawSpeedSetting = yawSpeedSetting;
        this.pitchSpeedSetting = pitchSpeedSetting;
        this.requireHoldSetting = requireHoldSetting;
        this.yawSpeedPresentation = yawSpeedPresentation;
        this.pitchSpeedPresentation = pitchSpeedPresentation;
        this.requireHoldPresentation = requireHoldPresentation;
        this.yawSpeedBinding = yawSpeedBinding;
        this.pitchSpeedBinding = pitchSpeedBinding;
        this.requireHoldBinding = requireHoldBinding;
    }

    static Minecraft189AimAssistFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AimAssistModule module =
                new Minecraft189AimAssistModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration yawSpeedSetting = null;
        SettingRegistry.Registration pitchSpeedSetting = null;
        SettingRegistry.Registration requireHoldSetting = null;
        SettingPresentationRegistry.Registration yawSpeedPresentation = null;
        SettingPresentationRegistry.Registration pitchSpeedPresentation = null;
        SettingPresentationRegistry.Registration requireHoldPresentation = null;
        ModuleSettingRegistry.Registration yawSpeedBinding = null;
        ModuleSettingRegistry.Registration pitchSpeedBinding = null;
        ModuleSettingRegistry.Registration requireHoldBinding = null;
        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AimAssistModule.ID,
                                    "Aim Assist",
                                    "Aims at the certified nearest remote player while attack is held.",
                                    Minecraft189FeatureCatalog
                                            .COMBAT_CATEGORY_ID,
                                    35));
            yawSpeedSetting =
                    settings.register(
                            module.yawSpeedSetting());
            pitchSpeedSetting =
                    settings.register(
                            module.pitchSpeedSetting());
            requireHoldSetting =
                    settings.register(
                            module.requireHoldSetting());
            yawSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.YAW_SPEED_SETTING_ID,
                                    "Yaw Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_SPEED,
                                            Minecraft189AimAssistModule.MAXIMUM_SPEED,
                                            0.5D)));
            pitchSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.PITCH_SPEED_SETTING_ID,
                                    "Pitch Speed",
                                    SettingValueKind.DOUBLE,
                                    10,
                                    new SettingNumericSpec(
                                            Minecraft189AimAssistModule.MINIMUM_SPEED,
                                            Minecraft189AimAssistModule.MAXIMUM_SPEED,
                                            0.5D)));
            requireHoldPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AimAssistModule.REQUIRE_HOLD_SETTING_ID,
                                    "Require Hold",
                                    SettingValueKind.BOOLEAN,
                                    20));
            yawSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.YAW_SPEED_SETTING_ID,
                                    0));
            pitchSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.PITCH_SPEED_SETTING_ID,
                                    10));
            requireHoldBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AimAssistModule.ID,
                                    Minecraft189AimAssistModule.REQUIRE_HOLD_SETTING_ID,
                                    20));

            return new Minecraft189AimAssistFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    yawSpeedSetting,
                    pitchSpeedSetting,
                    requireHoldSetting,
                    yawSpeedPresentation,
                    pitchSpeedPresentation,
                    requireHoldPresentation,
                    yawSpeedBinding,
                    pitchSpeedBinding,
                    requireHoldBinding);
        } catch (RuntimeException failure) {
            closeQuietly(requireHoldBinding, failure);
            closeQuietly(pitchSpeedBinding, failure);
            closeQuietly(yawSpeedBinding, failure);
            closeQuietly(requireHoldPresentation, failure);
            closeQuietly(pitchSpeedPresentation, failure);
            closeQuietly(yawSpeedPresentation, failure);
            closeQuietly(requireHoldSetting, failure);
            closeQuietly(pitchSpeedSetting, failure);
            closeQuietly(yawSpeedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189AimAssistModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "aim-assist feature is closed");
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
                    Minecraft189AimAssistModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AimAssistModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(requireHoldBinding, failure);
        failure = close(pitchSpeedBinding, failure);
        failure = close(yawSpeedBinding, failure);
        failure = close(requireHoldPresentation, failure);
        failure = close(pitchSpeedPresentation, failure);
        failure = close(yawSpeedPresentation, failure);
        failure = close(requireHoldSetting, failure);
        failure = close(pitchSpeedSetting, failure);
        failure = close(yawSpeedSetting, failure);
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
                            "aim-assist feature close failed",
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
