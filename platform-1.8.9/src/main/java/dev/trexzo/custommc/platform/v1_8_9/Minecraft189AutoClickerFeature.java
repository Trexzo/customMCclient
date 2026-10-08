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

final class Minecraft189AutoClickerFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189AutoClickerModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration minSetting;
    private final SettingRegistry.Registration maxSetting;
    private final SettingRegistry.Registration requireForwardSetting;
    private final SettingRegistry.Registration requireHoldSetting;
    private final SettingPresentationRegistry.Registration minPresentation;
    private final SettingPresentationRegistry.Registration maxPresentation;
    private final SettingPresentationRegistry.Registration requireForwardPresentation;
    private final SettingPresentationRegistry.Registration requireHoldPresentation;
    private final ModuleSettingRegistry.Registration minBinding;
    private final ModuleSettingRegistry.Registration maxBinding;
    private final ModuleSettingRegistry.Registration requireForwardBinding;
    private final ModuleSettingRegistry.Registration requireHoldBinding;
    private boolean closed;

    private Minecraft189AutoClickerFeature(
            final ModuleController controller,
            final Minecraft189AutoClickerModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration minSetting,
            final SettingRegistry.Registration maxSetting,
            final SettingRegistry.Registration requireForwardSetting,
            final SettingRegistry.Registration requireHoldSetting,
            final SettingPresentationRegistry.Registration minPresentation,
            final SettingPresentationRegistry.Registration maxPresentation,
            final SettingPresentationRegistry.Registration requireForwardPresentation,
            final SettingPresentationRegistry.Registration requireHoldPresentation,
            final ModuleSettingRegistry.Registration minBinding,
            final ModuleSettingRegistry.Registration maxBinding,
            final ModuleSettingRegistry.Registration requireForwardBinding,
            final ModuleSettingRegistry.Registration requireHoldBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.minSetting = minSetting;
        this.maxSetting = maxSetting;
        this.requireForwardSetting = requireForwardSetting;
        this.requireHoldSetting = requireHoldSetting;
        this.minPresentation = minPresentation;
        this.maxPresentation = maxPresentation;
        this.requireForwardPresentation = requireForwardPresentation;
        this.requireHoldPresentation = requireHoldPresentation;
        this.minBinding = minBinding;
        this.maxBinding = maxBinding;
        this.requireForwardBinding = requireForwardBinding;
        this.requireHoldBinding = requireHoldBinding;
    }

    static Minecraft189AutoClickerFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations) {
        final Minecraft189AutoClickerModule module =
                new Minecraft189AutoClickerModule();

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration minSetting = null;
        SettingRegistry.Registration maxSetting = null;
        SettingRegistry.Registration requireForwardSetting = null;
        SettingRegistry.Registration requireHoldSetting = null;
        SettingPresentationRegistry.Registration minPresentation = null;
        SettingPresentationRegistry.Registration maxPresentation = null;
        SettingPresentationRegistry.Registration requireForwardPresentation = null;
        SettingPresentationRegistry.Registration requireHoldPresentation = null;
        ModuleSettingRegistry.Registration minBinding = null;
        ModuleSettingRegistry.Registration maxBinding = null;
        ModuleSettingRegistry.Registration requireForwardBinding = null;
        ModuleSettingRegistry.Registration requireHoldBinding = null;

        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AutoClickerModule.ID,
                                    "Auto Clicker",
                                    "Clicks at configurable CPS, optionally requiring the physical left mouse button.",
                                    Minecraft189FeatureCatalog
                                            .COMBAT_CATEGORY_ID,
                                    10));
            minSetting =
                    settings.register(
                            module.minCpsSetting());
            maxSetting =
                    settings.register(
                            module.maxCpsSetting());
            requireForwardSetting =
                    settings.register(
                            module.requireForwardSetting());
            requireHoldSetting =
                    settings.register(
                            module.requireHoldSetting());
            minPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.MIN_CPS_SETTING_ID,
                                    "Min CPS",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            1.0D,
                                            20.0D,
                                            1.0D)));
            maxPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.MAX_CPS_SETTING_ID,
                                    "Max CPS",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            1.0D,
                                            20.0D,
                                            1.0D)));
            requireForwardPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.REQUIRE_FORWARD_SETTING_ID,
                                    "Require Forward",
                                    SettingValueKind.BOOLEAN,
                                    15));
            requireHoldPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189AutoClickerModule.REQUIRE_HOLD_SETTING_ID,
                                    "Require Hold",
                                    SettingValueKind.BOOLEAN,
                                    20));
            minBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.MIN_CPS_SETTING_ID,
                                    0));
            maxBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.MAX_CPS_SETTING_ID,
                                    10));
            requireForwardBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.REQUIRE_FORWARD_SETTING_ID,
                                    15));
            requireHoldBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189AutoClickerModule.ID,
                                    Minecraft189AutoClickerModule.REQUIRE_HOLD_SETTING_ID,
                                    20));

            return new Minecraft189AutoClickerFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    minSetting,
                    maxSetting,
                    requireForwardSetting,
                    requireHoldSetting,
                    minPresentation,
                    maxPresentation,
                    requireForwardPresentation,
                    requireHoldPresentation,
                    minBinding,
                    maxBinding,
                    requireForwardBinding,
                    requireHoldBinding);
        } catch (RuntimeException failure) {
            closeQuietly(
                    requireHoldBinding,
                    failure);
            closeQuietly(
                    requireForwardBinding,
                    failure);
            closeQuietly(
                    maxBinding,
                    failure);
            closeQuietly(
                    minBinding,
                    failure);
            closeQuietly(
                    requireHoldPresentation,
                    failure);
            closeQuietly(
                    requireForwardPresentation,
                    failure);
            closeQuietly(
                    maxPresentation,
                    failure);
            closeQuietly(
                    minPresentation,
                    failure);
            closeQuietly(
                    requireHoldSetting,
                    failure);
            closeQuietly(
                    requireForwardSetting,
                    failure);
            closeQuietly(
                    maxSetting,
                    failure);
            closeQuietly(
                    minSetting,
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

    Minecraft189AutoClickerModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "auto-clicker feature is closed");
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
                    Minecraft189AutoClickerModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189AutoClickerModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(
                requireHoldBinding,
                failure);
        failure = close(
                requireForwardBinding,
                failure);
        failure = close(
                maxBinding,
                failure);
        failure = close(
                minBinding,
                failure);
        failure = close(
                requireHoldPresentation,
                failure);
        failure = close(
                requireForwardPresentation,
                failure);
        failure = close(
                maxPresentation,
                failure);
        failure = close(
                minPresentation,
                failure);
        failure = close(
                requireHoldSetting,
                failure);
        failure = close(
                requireForwardSetting,
                failure);
        failure = close(
                maxSetting,
                failure);
        failure = close(
                minSetting,
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
                            "auto-clicker feature close failed",
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
