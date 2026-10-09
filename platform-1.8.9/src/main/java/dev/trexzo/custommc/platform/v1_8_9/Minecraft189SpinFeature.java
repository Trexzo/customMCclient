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
    private final SettingRegistry.Registration randomIntervalSetting;
    private final SettingRegistry.Registration intervalVariationSetting;
    private final SettingPresentationRegistry.Registration randomIntervalPresentation;
    private final SettingPresentationRegistry.Registration intervalVariationPresentation;
    private final ModuleSettingRegistry.Registration randomIntervalBinding;
    private final ModuleSettingRegistry.Registration intervalVariationBinding;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingRegistry.Registration pauseSneakSetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final SettingPresentationRegistry.Registration pauseSneakPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private final ModuleSettingRegistry.Registration pauseSneakBinding;
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
            final ModuleSettingRegistry.Registration intervalBinding,
            final SettingRegistry.Registration randomIntervalSetting,
            final SettingRegistry.Registration intervalVariationSetting,
            final SettingPresentationRegistry.Registration randomIntervalPresentation,
            final SettingPresentationRegistry.Registration intervalVariationPresentation,
            final ModuleSettingRegistry.Registration randomIntervalBinding,
            final ModuleSettingRegistry.Registration intervalVariationBinding,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingRegistry.Registration pauseSneakSetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final SettingPresentationRegistry.Registration pauseSneakPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding,
            final ModuleSettingRegistry.Registration pauseSneakBinding) {
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
        this.randomIntervalSetting = randomIntervalSetting;
        this.intervalVariationSetting = intervalVariationSetting;
        this.randomIntervalPresentation = randomIntervalPresentation;
        this.intervalVariationPresentation = intervalVariationPresentation;
        this.randomIntervalBinding = randomIntervalBinding;
        this.intervalVariationBinding = intervalVariationBinding;
        this.groundOnlySetting = groundOnlySetting;
        this.pauseSneakSetting = pauseSneakSetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.pauseSneakPresentation = pauseSneakPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
        this.pauseSneakBinding = pauseSneakBinding;
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
        SettingRegistry.Registration randomIntervalSetting = null;
        SettingRegistry.Registration intervalVariationSetting = null;
        SettingPresentationRegistry.Registration randomIntervalPresentation = null;
        SettingPresentationRegistry.Registration intervalVariationPresentation = null;
        ModuleSettingRegistry.Registration randomIntervalBinding = null;
        ModuleSettingRegistry.Registration intervalVariationBinding = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingRegistry.Registration pauseSneakSetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        SettingPresentationRegistry.Registration pauseSneakPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        ModuleSettingRegistry.Registration pauseSneakBinding = null;
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
            randomIntervalSetting = settings.register(module.randomIntervalSetting());
            intervalVariationSetting = settings.register(
                    module.intervalVariationTicksSetting());
            groundOnlySetting = settings.register(module.groundOnlySetting());
            pauseSneakSetting = settings.register(
                    module.pauseWhileSneakingSetting());
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
            randomIntervalPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189SpinModule.RANDOM_INTERVAL_SETTING_ID,
                            "Random Interval", SettingValueKind.BOOLEAN, 40));
            intervalVariationPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189SpinModule.INTERVAL_VARIATION_SETTING_ID,
                            "Interval Variation", SettingValueKind.INTEGER, 50,
                            new SettingNumericSpec(0.0D,
                                    Minecraft189SpinModule.MAXIMUM_INTERVAL_VARIATION_TICKS,
                                    1.0D)));
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(Minecraft189SpinModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 60));
            pauseSneakPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189SpinModule.PAUSE_WHILE_SNEAKING_SETTING_ID,
                            "Pause While Sneaking", SettingValueKind.BOOLEAN, 70));
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

            randomIntervalBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189SpinModule.ID,
                            Minecraft189SpinModule.RANDOM_INTERVAL_SETTING_ID, 40));
            intervalVariationBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189SpinModule.ID,
                            Minecraft189SpinModule.INTERVAL_VARIATION_SETTING_ID, 50));

            groundOnlyBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189SpinModule.ID,
                            Minecraft189SpinModule.GROUND_ONLY_SETTING_ID, 60));
            pauseSneakBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189SpinModule.ID,
                            Minecraft189SpinModule.PAUSE_WHILE_SNEAKING_SETTING_ID, 70));
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
                    intervalBinding,
                    randomIntervalSetting,
                    intervalVariationSetting,
                    randomIntervalPresentation,
                    intervalVariationPresentation,
                    randomIntervalBinding,
                    intervalVariationBinding,
                    groundOnlySetting,
                    pauseSneakSetting,
                    groundOnlyPresentation,
                    pauseSneakPresentation,
                    groundOnlyBinding,
                    pauseSneakBinding);
        } catch (RuntimeException failure) {
            closeQuietly(pauseSneakBinding, failure);
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(pauseSneakPresentation, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(pauseSneakSetting, failure);
            closeQuietly(groundOnlySetting, failure);
            closeQuietly(intervalVariationBinding, failure);
            closeQuietly(randomIntervalBinding, failure);
            closeQuietly(intervalVariationPresentation, failure);
            closeQuietly(randomIntervalPresentation, failure);
            closeQuietly(intervalVariationSetting, failure);
            closeQuietly(randomIntervalSetting, failure);
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

        failure = close(pauseSneakBinding, failure);
        failure = close(groundOnlyBinding, failure);
        failure = close(pauseSneakPresentation, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(pauseSneakSetting, failure);
        failure = close(groundOnlySetting, failure);
        failure = close(intervalVariationBinding, failure);
        failure = close(randomIntervalBinding, failure);
        failure = close(intervalVariationPresentation, failure);
        failure = close(randomIntervalPresentation, failure);
        failure = close(intervalVariationSetting, failure);
        failure = close(randomIntervalSetting, failure);
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
