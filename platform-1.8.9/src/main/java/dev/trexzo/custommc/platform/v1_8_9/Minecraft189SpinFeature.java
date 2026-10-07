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

final class Minecraft189SpinFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189SpinModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration yawSpeedSetting;
    private final SettingRegistry.Registration reverseSetting;
    private final SettingRegistry.Registration requireHoldSetting;
    private final SettingRegistry.Registration intervalSetting;
    private final SettingPresentationRegistry.Registration yawSpeedPresentation;
    private final SettingPresentationRegistry.Registration reversePresentation;
    private final SettingPresentationRegistry.Registration requireHoldPresentation;
    private final SettingPresentationRegistry.Registration intervalPresentation;
    private final ModuleSettingRegistry.Registration yawSpeedBinding;
    private final ModuleSettingRegistry.Registration reverseBinding;
    private final ModuleSettingRegistry.Registration requireHoldBinding;
    private final ModuleSettingRegistry.Registration intervalBinding;
    private boolean closed;

    private Minecraft189SpinFeature(
            final ModuleController controller,
            final Minecraft189SpinModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration yawSpeedSetting,
            final SettingRegistry.Registration reverseSetting,
            final SettingRegistry.Registration requireHoldSetting,
            final SettingRegistry.Registration intervalSetting,
            final SettingPresentationRegistry.Registration yawSpeedPresentation,
            final SettingPresentationRegistry.Registration reversePresentation,
            final SettingPresentationRegistry.Registration requireHoldPresentation,
            final SettingPresentationRegistry.Registration intervalPresentation,
            final ModuleSettingRegistry.Registration yawSpeedBinding,
            final ModuleSettingRegistry.Registration reverseBinding,
            final ModuleSettingRegistry.Registration requireHoldBinding,
            final ModuleSettingRegistry.Registration intervalBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.yawSpeedSetting = yawSpeedSetting;
        this.reverseSetting = reverseSetting;
        this.requireHoldSetting = requireHoldSetting;
        this.intervalSetting = intervalSetting;
        this.yawSpeedPresentation = yawSpeedPresentation;
        this.reversePresentation = reversePresentation;
        this.requireHoldPresentation = requireHoldPresentation;
        this.intervalPresentation = intervalPresentation;
        this.yawSpeedBinding = yawSpeedBinding;
        this.reverseBinding = reverseBinding;
        this.requireHoldBinding = requireHoldBinding;
        this.intervalBinding = intervalBinding;
    }

    static Minecraft189SpinFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189SpinModule module =
                new Minecraft189SpinModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration yawSpeedSetting = null;
        SettingRegistry.Registration reverseSetting = null;
        SettingRegistry.Registration requireHoldSetting = null;
        SettingRegistry.Registration intervalSetting = null;
        SettingPresentationRegistry.Registration yawSpeedPresentation = null;
        SettingPresentationRegistry.Registration reversePresentation = null;
        SettingPresentationRegistry.Registration requireHoldPresentation = null;
        SettingPresentationRegistry.Registration intervalPresentation = null;
        ModuleSettingRegistry.Registration yawSpeedBinding = null;
        ModuleSettingRegistry.Registration reverseBinding = null;
        ModuleSettingRegistry.Registration requireHoldBinding = null;
        ModuleSettingRegistry.Registration intervalBinding = null;
        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189SpinModule.ID,
                                    "Spin",
                                    "Continuously rotates mapped player yaw while enabled.",
                                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID,
                                    40));
            yawSpeedSetting =
                    settings.register(
                            module.yawSpeedSetting());
            reverseSetting =
                    settings.register(
                            module.reverseSetting());
            requireHoldSetting =
                    settings.register(
                            module.requireHoldSetting());
            intervalSetting =
                    settings.register(
                            module.intervalTicksSetting());
            yawSpeedPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189SpinModule.YAW_SPEED_SETTING_ID,
                                    "Yaw Speed",
                                    SettingValueKind.DOUBLE,
                                    0,
                                    new SettingNumericSpec(
                                            Minecraft189SpinModule.MINIMUM_YAW_SPEED,
                                            Minecraft189SpinModule.MAXIMUM_YAW_SPEED,
                                            1.0D)));
            reversePresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189SpinModule.REVERSE_SETTING_ID,
                                    "Reverse",
                                    SettingValueKind.BOOLEAN,
                                    10));
            requireHoldPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189SpinModule.REQUIRE_HOLD_SETTING_ID,
                                    "Require Hold",
                                    SettingValueKind.BOOLEAN,
                                    20));
            intervalPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189SpinModule.INTERVAL_SETTING_ID,
                                    "Interval",
                                    SettingValueKind.INTEGER,
                                    30,
                                    new SettingNumericSpec(
                                            Minecraft189SpinModule.MINIMUM_INTERVAL_TICKS,
                                            Minecraft189SpinModule.MAXIMUM_INTERVAL_TICKS,
                                            1.0D)));
            yawSpeedBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189SpinModule.ID,
                                    Minecraft189SpinModule.YAW_SPEED_SETTING_ID,
                                    0));
            reverseBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189SpinModule.ID,
                                    Minecraft189SpinModule.REVERSE_SETTING_ID,
                                    10));
            requireHoldBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189SpinModule.ID,
                                    Minecraft189SpinModule.REQUIRE_HOLD_SETTING_ID,
                                    20));
            intervalBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189SpinModule.ID,
                                    Minecraft189SpinModule.INTERVAL_SETTING_ID,
                                    30));

            return new Minecraft189SpinFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    yawSpeedSetting,
                    reverseSetting,
                    requireHoldSetting,
                    intervalSetting,
                    yawSpeedPresentation,
                    reversePresentation,
                    requireHoldPresentation,
                    intervalPresentation,
                    yawSpeedBinding,
                    reverseBinding,
                    requireHoldBinding,
                    intervalBinding);
        } catch (RuntimeException failure) {
            closeQuietly(intervalBinding, failure);
            closeQuietly(requireHoldBinding, failure);
            closeQuietly(reverseBinding, failure);
            closeQuietly(yawSpeedBinding, failure);
            closeQuietly(intervalPresentation, failure);
            closeQuietly(requireHoldPresentation, failure);
            closeQuietly(reversePresentation, failure);
            closeQuietly(yawSpeedPresentation, failure);
            closeQuietly(intervalSetting, failure);
            closeQuietly(requireHoldSetting, failure);
            closeQuietly(reverseSetting, failure);
            closeQuietly(yawSpeedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189SpinModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "spin feature is closed");
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
                    Minecraft189SpinModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189SpinModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(intervalBinding, failure);
        failure = close(requireHoldBinding, failure);
        failure = close(reverseBinding, failure);
        failure = close(yawSpeedBinding, failure);
        failure = close(intervalPresentation, failure);
        failure = close(requireHoldPresentation, failure);
        failure = close(reversePresentation, failure);
        failure = close(yawSpeedPresentation, failure);
        failure = close(intervalSetting, failure);
        failure = close(requireHoldSetting, failure);
        failure = close(reverseSetting, failure);
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
                            "spin feature close failed",
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
