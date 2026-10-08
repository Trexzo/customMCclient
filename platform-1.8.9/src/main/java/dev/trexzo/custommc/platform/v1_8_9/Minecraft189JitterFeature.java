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
    private final SettingRegistry.Registration yawEnabledSetting;
    private final SettingRegistry.Registration pitchEnabledSetting;
    private final SettingRegistry.Registration intervalSetting;
    private final SettingRegistry.Registration requireHoldSetting;
    private final SettingRegistry.Registration variableStrengthSetting;
    private final SettingRegistry.Registration strengthVariationSetting;
    private final SettingPresentationRegistry.Registration yawPresentation;
    private final SettingPresentationRegistry.Registration pitchPresentation;
    private final SettingPresentationRegistry.Registration yawEnabledPresentation;
    private final SettingPresentationRegistry.Registration pitchEnabledPresentation;
    private final SettingPresentationRegistry.Registration intervalPresentation;
    private final SettingPresentationRegistry.Registration requireHoldPresentation;
    private final SettingPresentationRegistry.Registration variableStrengthPresentation;
    private final SettingPresentationRegistry.Registration strengthVariationPresentation;
    private final ModuleSettingRegistry.Registration yawBinding;
    private final ModuleSettingRegistry.Registration pitchBinding;
    private final ModuleSettingRegistry.Registration yawEnabledBinding;
    private final ModuleSettingRegistry.Registration pitchEnabledBinding;
    private final ModuleSettingRegistry.Registration intervalBinding;
    private final ModuleSettingRegistry.Registration requireHoldBinding;
    private final ModuleSettingRegistry.Registration variableStrengthBinding;
    private final ModuleSettingRegistry.Registration strengthVariationBinding;
    private boolean closed;

    private Minecraft189JitterFeature(
            final ModuleController controller,
            final Minecraft189JitterModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration yawSetting,
            final SettingRegistry.Registration pitchSetting,
            final SettingRegistry.Registration yawEnabledSetting,
            final SettingRegistry.Registration pitchEnabledSetting,
            final SettingRegistry.Registration intervalSetting,
            final SettingRegistry.Registration requireHoldSetting,
            final SettingRegistry.Registration variableStrengthSetting,
            final SettingRegistry.Registration strengthVariationSetting,
            final SettingPresentationRegistry.Registration yawPresentation,
            final SettingPresentationRegistry.Registration pitchPresentation,
            final SettingPresentationRegistry.Registration yawEnabledPresentation,
            final SettingPresentationRegistry.Registration pitchEnabledPresentation,
            final SettingPresentationRegistry.Registration intervalPresentation,
            final SettingPresentationRegistry.Registration requireHoldPresentation,
            final SettingPresentationRegistry.Registration variableStrengthPresentation,
            final SettingPresentationRegistry.Registration strengthVariationPresentation,
            final ModuleSettingRegistry.Registration yawBinding,
            final ModuleSettingRegistry.Registration pitchBinding,
            final ModuleSettingRegistry.Registration yawEnabledBinding,
            final ModuleSettingRegistry.Registration pitchEnabledBinding,
            final ModuleSettingRegistry.Registration intervalBinding,
            final ModuleSettingRegistry.Registration requireHoldBinding,
            final ModuleSettingRegistry.Registration variableStrengthBinding,
            final ModuleSettingRegistry.Registration strengthVariationBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.yawSetting = yawSetting;
        this.pitchSetting = pitchSetting;
        this.yawEnabledSetting = yawEnabledSetting;
        this.pitchEnabledSetting = pitchEnabledSetting;
        this.intervalSetting = intervalSetting;
        this.requireHoldSetting = requireHoldSetting;
        this.variableStrengthSetting = variableStrengthSetting;
        this.strengthVariationSetting = strengthVariationSetting;
        this.yawPresentation = yawPresentation;
        this.pitchPresentation = pitchPresentation;
        this.yawEnabledPresentation = yawEnabledPresentation;
        this.pitchEnabledPresentation = pitchEnabledPresentation;
        this.intervalPresentation = intervalPresentation;
        this.requireHoldPresentation = requireHoldPresentation;
        this.variableStrengthPresentation = variableStrengthPresentation;
        this.strengthVariationPresentation = strengthVariationPresentation;
        this.yawBinding = yawBinding;
        this.pitchBinding = pitchBinding;
        this.yawEnabledBinding = yawEnabledBinding;
        this.pitchEnabledBinding = pitchEnabledBinding;
        this.intervalBinding = intervalBinding;
        this.requireHoldBinding = requireHoldBinding;
        this.variableStrengthBinding = variableStrengthBinding;
        this.strengthVariationBinding = strengthVariationBinding;
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
        SettingRegistry.Registration yawEnabledSetting = null;
        SettingRegistry.Registration pitchEnabledSetting = null;
        SettingRegistry.Registration intervalSetting = null;
        SettingRegistry.Registration requireHoldSetting = null;
        SettingRegistry.Registration variableStrengthSetting = null;
        SettingRegistry.Registration strengthVariationSetting = null;
        SettingPresentationRegistry.Registration yawPresentation = null;
        SettingPresentationRegistry.Registration pitchPresentation = null;
        SettingPresentationRegistry.Registration yawEnabledPresentation = null;
        SettingPresentationRegistry.Registration pitchEnabledPresentation = null;
        SettingPresentationRegistry.Registration intervalPresentation = null;
        SettingPresentationRegistry.Registration requireHoldPresentation = null;
        SettingPresentationRegistry.Registration variableStrengthPresentation = null;
        SettingPresentationRegistry.Registration strengthVariationPresentation = null;
        ModuleSettingRegistry.Registration yawBinding = null;
        ModuleSettingRegistry.Registration pitchBinding = null;
        ModuleSettingRegistry.Registration yawEnabledBinding = null;
        ModuleSettingRegistry.Registration pitchEnabledBinding = null;
        ModuleSettingRegistry.Registration intervalBinding = null;
        ModuleSettingRegistry.Registration requireHoldBinding = null;
        ModuleSettingRegistry.Registration variableStrengthBinding = null;
        ModuleSettingRegistry.Registration strengthVariationBinding = null;
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
            yawEnabledSetting =
                    settings.register(
                            module.yawEnabledSetting());
            pitchEnabledSetting =
                    settings.register(
                            module.pitchEnabledSetting());
            intervalSetting =
                    settings.register(
                            module.intervalTicksSetting());
            requireHoldSetting =
                    settings.register(
                            module.requireHoldSetting());
            variableStrengthSetting = settings.register(
                    module.variableStrengthSetting());
            strengthVariationSetting = settings.register(
                    module.strengthVariationPercentSetting());
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
            yawEnabledPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189JitterModule.YAW_ENABLED_SETTING_ID,
                                    "Yaw Axis",
                                    SettingValueKind.BOOLEAN,
                                    5));
            pitchEnabledPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189JitterModule.PITCH_ENABLED_SETTING_ID,
                                    "Pitch Axis",
                                    SettingValueKind.BOOLEAN,
                                    15));
            intervalPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189JitterModule.INTERVAL_SETTING_ID,
                                    "Interval",
                                    SettingValueKind.INTEGER,
                                    20,
                                    new SettingNumericSpec(
                                            Minecraft189JitterModule.MINIMUM_INTERVAL_TICKS,
                                            Minecraft189JitterModule.MAXIMUM_INTERVAL_TICKS,
                                            1.0D)));
            requireHoldPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189JitterModule.REQUIRE_HOLD_SETTING_ID,
                                    "Require Hold",
                                    SettingValueKind.BOOLEAN,
                                    30));
            variableStrengthPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189JitterModule.VARIABLE_STRENGTH_SETTING_ID,
                            "Variable Strength", SettingValueKind.BOOLEAN, 40));
            strengthVariationPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189JitterModule.STRENGTH_VARIATION_SETTING_ID,
                            "Strength Variation %", SettingValueKind.INTEGER, 50,
                            new SettingNumericSpec(0.0D, 100.0D, 5.0D)));
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
            yawEnabledBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189JitterModule.ID,
                                    Minecraft189JitterModule.YAW_ENABLED_SETTING_ID,
                                    5));
            pitchEnabledBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189JitterModule.ID,
                                    Minecraft189JitterModule.PITCH_ENABLED_SETTING_ID,
                                    15));
            intervalBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189JitterModule.ID,
                                    Minecraft189JitterModule.INTERVAL_SETTING_ID,
                                    20));
            requireHoldBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189JitterModule.ID,
                                    Minecraft189JitterModule.REQUIRE_HOLD_SETTING_ID,
                                    30));

            variableStrengthBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189JitterModule.ID,
                            Minecraft189JitterModule.VARIABLE_STRENGTH_SETTING_ID, 40));
            strengthVariationBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189JitterModule.ID,
                            Minecraft189JitterModule.STRENGTH_VARIATION_SETTING_ID, 50));

            return new Minecraft189JitterFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    yawSetting,
                    pitchSetting,
                    yawEnabledSetting,
                    pitchEnabledSetting,
                    intervalSetting,
                    requireHoldSetting,
                    variableStrengthSetting,
                    strengthVariationSetting,
                    yawPresentation,
                    pitchPresentation,
                    yawEnabledPresentation,
                    pitchEnabledPresentation,
                    intervalPresentation,
                    requireHoldPresentation,
                    variableStrengthPresentation,
                    strengthVariationPresentation,
                    yawBinding,
                    pitchBinding,
                    yawEnabledBinding,
                    pitchEnabledBinding,
                    intervalBinding,
                    requireHoldBinding,
                    variableStrengthBinding,
                    strengthVariationBinding);
        } catch (RuntimeException failure) {
            closeQuietly(strengthVariationBinding, failure);
            closeQuietly(variableStrengthBinding, failure);
            closeQuietly(strengthVariationPresentation, failure);
            closeQuietly(variableStrengthPresentation, failure);
            closeQuietly(strengthVariationSetting, failure);
            closeQuietly(variableStrengthSetting, failure);
            closeQuietly(requireHoldBinding, failure);
            closeQuietly(pitchEnabledBinding, failure);
            closeQuietly(yawEnabledBinding, failure);
            closeQuietly(intervalBinding, failure);
            closeQuietly(pitchBinding, failure);
            closeQuietly(yawBinding, failure);
            closeQuietly(requireHoldPresentation, failure);
            closeQuietly(intervalPresentation, failure);
            closeQuietly(pitchEnabledPresentation, failure);
            closeQuietly(yawEnabledPresentation, failure);
            closeQuietly(pitchPresentation, failure);
            closeQuietly(yawPresentation, failure);
            closeQuietly(requireHoldSetting, failure);
            closeQuietly(intervalSetting, failure);
            closeQuietly(pitchEnabledSetting, failure);
            closeQuietly(yawEnabledSetting, failure);
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

        failure = close(strengthVariationBinding, failure);
        failure = close(variableStrengthBinding, failure);
        failure = close(strengthVariationPresentation, failure);
        failure = close(variableStrengthPresentation, failure);
        failure = close(strengthVariationSetting, failure);
        failure = close(variableStrengthSetting, failure);
        failure = close(requireHoldBinding, failure);
        failure = close(intervalBinding, failure);
        failure = close(pitchEnabledBinding, failure);
        failure = close(yawEnabledBinding, failure);
        failure = close(pitchBinding, failure);
        failure = close(yawBinding, failure);
        failure = close(requireHoldPresentation, failure);
        failure = close(intervalPresentation, failure);
        failure = close(pitchEnabledPresentation, failure);
        failure = close(yawEnabledPresentation, failure);
        failure = close(pitchPresentation, failure);
        failure = close(yawPresentation, failure);
        failure = close(requireHoldSetting, failure);
        failure = close(intervalSetting, failure);
        failure = close(pitchEnabledSetting, failure);
        failure = close(yawEnabledSetting, failure);
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
