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
    private final SettingPresentationRegistry.Registration minPresentation;
    private final SettingPresentationRegistry.Registration maxPresentation;
    private final ModuleSettingRegistry.Registration minBinding;
    private final ModuleSettingRegistry.Registration maxBinding;
    private boolean closed;

    private Minecraft189AutoClickerFeature(
            final ModuleController controller,
            final Minecraft189AutoClickerModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration minSetting,
            final SettingRegistry.Registration maxSetting,
            final SettingPresentationRegistry.Registration minPresentation,
            final SettingPresentationRegistry.Registration maxPresentation,
            final ModuleSettingRegistry.Registration minBinding,
            final ModuleSettingRegistry.Registration maxBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.minSetting = minSetting;
        this.maxSetting = maxSetting;
        this.minPresentation = minPresentation;
        this.maxPresentation = maxPresentation;
        this.minBinding = minBinding;
        this.maxBinding = maxBinding;
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
        SettingPresentationRegistry.Registration minPresentation = null;
        SettingPresentationRegistry.Registration maxPresentation = null;
        ModuleSettingRegistry.Registration minBinding = null;
        ModuleSettingRegistry.Registration maxBinding = null;

        try {
            moduleRegistration =
                    modules.register(
                            module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189AutoClickerModule.ID,
                                    "Auto Clicker",
                                    "Clicks while the physical left mouse button is held.",
                                    Minecraft189FeatureCatalog
                                            .COMBAT_CATEGORY_ID,
                                    10));
            minSetting =
                    settings.register(
                            module.minCpsSetting());
            maxSetting =
                    settings.register(
                            module.maxCpsSetting());
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

            return new Minecraft189AutoClickerFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    minSetting,
                    maxSetting,
                    minPresentation,
                    maxPresentation,
                    minBinding,
                    maxBinding);
        } catch (RuntimeException failure) {
            closeQuietly(
                    maxBinding,
                    failure);
            closeQuietly(
                    minBinding,
                    failure);
            closeQuietly(
                    maxPresentation,
                    failure);
            closeQuietly(
                    minPresentation,
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
                maxBinding,
                failure);
        failure = close(
                minBinding,
                failure);
        failure = close(
                maxPresentation,
                failure);
        failure = close(
                minPresentation,
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
