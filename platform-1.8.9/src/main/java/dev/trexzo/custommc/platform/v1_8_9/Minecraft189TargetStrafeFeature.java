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

final class Minecraft189TargetStrafeFeature implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189TargetStrafeModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration speedSetting;
    private final SettingPresentationRegistry.Registration speedPresentation;
    private final ModuleSettingRegistry.Registration speedBinding;
    private final SettingRegistry.Registration radiusSetting;
    private final SettingPresentationRegistry.Registration radiusPresentation;
    private final ModuleSettingRegistry.Registration radiusBinding;
    private final SettingRegistry.Registration maxDistanceSetting;
    private final SettingPresentationRegistry.Registration maxDistancePresentation;
    private final ModuleSettingRegistry.Registration maxDistanceBinding;
    private final SettingRegistry.Registration clockwiseSetting;
    private final SettingPresentationRegistry.Registration clockwisePresentation;
    private final ModuleSettingRegistry.Registration clockwiseBinding;
    private final SettingRegistry.Registration requireForwardSetting;
    private final SettingPresentationRegistry.Registration requireForwardPresentation;
    private final ModuleSettingRegistry.Registration requireForwardBinding;
    private final SettingRegistry.Registration groundOnlySetting;
    private final SettingPresentationRegistry.Registration groundOnlyPresentation;
    private final ModuleSettingRegistry.Registration groundOnlyBinding;
    private boolean closed;

    private Minecraft189TargetStrafeFeature(
            final ModuleController controller,
            final Minecraft189TargetStrafeModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration speedSetting,
            final SettingPresentationRegistry.Registration speedPresentation,
            final ModuleSettingRegistry.Registration speedBinding,
            final SettingRegistry.Registration radiusSetting,
            final SettingPresentationRegistry.Registration radiusPresentation,
            final ModuleSettingRegistry.Registration radiusBinding,
            final SettingRegistry.Registration maxDistanceSetting,
            final SettingPresentationRegistry.Registration maxDistancePresentation,
            final ModuleSettingRegistry.Registration maxDistanceBinding,
            final SettingRegistry.Registration clockwiseSetting,
            final SettingPresentationRegistry.Registration clockwisePresentation,
            final ModuleSettingRegistry.Registration clockwiseBinding,
            final SettingRegistry.Registration requireForwardSetting,
            final SettingPresentationRegistry.Registration requireForwardPresentation,
            final ModuleSettingRegistry.Registration requireForwardBinding,
            final SettingRegistry.Registration groundOnlySetting,
            final SettingPresentationRegistry.Registration groundOnlyPresentation,
            final ModuleSettingRegistry.Registration groundOnlyBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.speedSetting = speedSetting;
        this.speedPresentation = speedPresentation;
        this.speedBinding = speedBinding;
        this.radiusSetting = radiusSetting;
        this.radiusPresentation = radiusPresentation;
        this.radiusBinding = radiusBinding;
        this.maxDistanceSetting = maxDistanceSetting;
        this.maxDistancePresentation = maxDistancePresentation;
        this.maxDistanceBinding = maxDistanceBinding;
        this.clockwiseSetting = clockwiseSetting;
        this.clockwisePresentation = clockwisePresentation;
        this.clockwiseBinding = clockwiseBinding;
        this.requireForwardSetting = requireForwardSetting;
        this.requireForwardPresentation = requireForwardPresentation;
        this.requireForwardBinding = requireForwardBinding;
        this.groundOnlySetting = groundOnlySetting;
        this.groundOnlyPresentation = groundOnlyPresentation;
        this.groundOnlyBinding = groundOnlyBinding;
    }

    static Minecraft189TargetStrafeFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189TargetStrafeModule module = new Minecraft189TargetStrafeModule();
        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration speedSetting = null;
        SettingPresentationRegistry.Registration speedPresentation = null;
        ModuleSettingRegistry.Registration speedBinding = null;
        SettingRegistry.Registration radiusSetting = null;
        SettingPresentationRegistry.Registration radiusPresentation = null;
        ModuleSettingRegistry.Registration radiusBinding = null;
        SettingRegistry.Registration maxDistanceSetting = null;
        SettingPresentationRegistry.Registration maxDistancePresentation = null;
        ModuleSettingRegistry.Registration maxDistanceBinding = null;
        SettingRegistry.Registration clockwiseSetting = null;
        SettingPresentationRegistry.Registration clockwisePresentation = null;
        ModuleSettingRegistry.Registration clockwiseBinding = null;
        SettingRegistry.Registration requireForwardSetting = null;
        SettingPresentationRegistry.Registration requireForwardPresentation = null;
        ModuleSettingRegistry.Registration requireForwardBinding = null;
        SettingRegistry.Registration groundOnlySetting = null;
        SettingPresentationRegistry.Registration groundOnlyPresentation = null;
        ModuleSettingRegistry.Registration groundOnlyBinding = null;
        try {
            moduleRegistration = modules.register(module);
            presentation = presentations.register(new ModuleDescriptor(
                    Minecraft189TargetStrafeModule.ID, "Target Strafe",
                    "Orbits the nearest mapped player while moving; no wall detection.",
                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID, 95));
            speedSetting = settings.register(module.speedSetting());
            speedPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetStrafeModule.SPEED_SETTING_ID,
                            "Orbit Speed", SettingValueKind.DOUBLE, 0,
                            new SettingNumericSpec(0.05D, 1.0D, 0.05D)));
            speedBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189TargetStrafeModule.ID,
                    Minecraft189TargetStrafeModule.SPEED_SETTING_ID, 0));
            radiusSetting = settings.register(module.radiusSetting());
            radiusPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetStrafeModule.RADIUS_SETTING_ID,
                            "Orbit Radius", SettingValueKind.DOUBLE, 10,
                            new SettingNumericSpec(1.0D, 6.0D, 0.25D)));
            radiusBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189TargetStrafeModule.ID,
                    Minecraft189TargetStrafeModule.RADIUS_SETTING_ID, 10));
            maxDistanceSetting = settings.register(module.maxDistanceSetting());
            maxDistancePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetStrafeModule.MAX_DISTANCE_SETTING_ID,
                            "Target Range", SettingValueKind.DOUBLE, 20,
                            new SettingNumericSpec(2.0D, 12.0D, 0.5D)));
            maxDistanceBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189TargetStrafeModule.ID,
                    Minecraft189TargetStrafeModule.MAX_DISTANCE_SETTING_ID, 20));
            clockwiseSetting = settings.register(module.clockwiseSetting());
            clockwisePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetStrafeModule.CLOCKWISE_SETTING_ID,
                            "Clockwise", SettingValueKind.BOOLEAN, 30));
            clockwiseBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189TargetStrafeModule.ID,
                    Minecraft189TargetStrafeModule.CLOCKWISE_SETTING_ID, 30));
            requireForwardSetting = settings.register(module.requireForwardSetting());
            requireForwardPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetStrafeModule.REQUIRE_FORWARD_SETTING_ID,
                            "Require Forward (W)", SettingValueKind.BOOLEAN, 40));
            requireForwardBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189TargetStrafeModule.ID,
                    Minecraft189TargetStrafeModule.REQUIRE_FORWARD_SETTING_ID, 40));
            groundOnlySetting = settings.register(module.groundOnlySetting());
            groundOnlyPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189TargetStrafeModule.GROUND_ONLY_SETTING_ID,
                            "Ground Only", SettingValueKind.BOOLEAN, 50));
            groundOnlyBinding = moduleSettings.register(new ModuleSettingBinding(
                    Minecraft189TargetStrafeModule.ID,
                    Minecraft189TargetStrafeModule.GROUND_ONLY_SETTING_ID, 50));
            return new Minecraft189TargetStrafeFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    speedSetting,
                    speedPresentation,
                    speedBinding,
                    radiusSetting,
                    radiusPresentation,
                    radiusBinding,
                    maxDistanceSetting,
                    maxDistancePresentation,
                    maxDistanceBinding,
                    clockwiseSetting,
                    clockwisePresentation,
                    clockwiseBinding,
                    requireForwardSetting,
                    requireForwardPresentation,
                    requireForwardBinding,
                    groundOnlySetting,
                    groundOnlyPresentation,
                    groundOnlyBinding);
        } catch (RuntimeException failure) {
            closeQuietly(groundOnlyBinding, failure);
            closeQuietly(groundOnlyPresentation, failure);
            closeQuietly(groundOnlySetting, failure);
            closeQuietly(requireForwardBinding, failure);
            closeQuietly(requireForwardPresentation, failure);
            closeQuietly(requireForwardSetting, failure);
            closeQuietly(clockwiseBinding, failure);
            closeQuietly(clockwisePresentation, failure);
            closeQuietly(clockwiseSetting, failure);
            closeQuietly(maxDistanceBinding, failure);
            closeQuietly(maxDistancePresentation, failure);
            closeQuietly(maxDistanceSetting, failure);
            closeQuietly(radiusBinding, failure);
            closeQuietly(radiusPresentation, failure);
            closeQuietly(radiusSetting, failure);
            closeQuietly(speedBinding, failure);
            closeQuietly(speedPresentation, failure);
            closeQuietly(speedSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189TargetStrafeModule module() {
        if (closed) throw new IllegalStateException("target strafe feature closed");
        return module;
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        try {
            if (controller.stateOf(Minecraft189TargetStrafeModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(Minecraft189TargetStrafeModule.ID);
            }
        } catch (RuntimeException e) { failure = e; }
        failure = close(groundOnlyBinding, failure);
        failure = close(groundOnlyPresentation, failure);
        failure = close(groundOnlySetting, failure);
        failure = close(requireForwardBinding, failure);
        failure = close(requireForwardPresentation, failure);
        failure = close(requireForwardSetting, failure);
        failure = close(clockwiseBinding, failure);
        failure = close(clockwisePresentation, failure);
        failure = close(clockwiseSetting, failure);
        failure = close(maxDistanceBinding, failure);
        failure = close(maxDistancePresentation, failure);
        failure = close(maxDistanceSetting, failure);
        failure = close(radiusBinding, failure);
        failure = close(radiusPresentation, failure);
        failure = close(radiusSetting, failure);
        failure = close(speedBinding, failure);
        failure = close(speedPresentation, failure);
        failure = close(speedSetting, failure);
        failure = close(presentation, failure);
        failure = close(moduleRegistration, failure);
        if (failure != null) throw failure;
    }

    private static RuntimeException close(
            final AutoCloseable resource, final RuntimeException primary) {
        if (resource == null) return primary;
        try { resource.close(); return primary; }
        catch (RuntimeException failure) {
            if (primary == null) return failure;
            primary.addSuppressed(failure);
            return primary;
        } catch (Exception failure) {
            final RuntimeException wrapped = new IllegalStateException(
                    "Target Strafe feature cleanup failed", failure);
            if (primary == null) return wrapped;
            primary.addSuppressed(wrapped);
            return primary;
        }
    }

    private static void closeQuietly(
            final AutoCloseable resource, final RuntimeException primary) {
        if (resource == null) return;
        try { resource.close(); }
        catch (Exception failure) { primary.addSuppressed(failure); }
    }
}
