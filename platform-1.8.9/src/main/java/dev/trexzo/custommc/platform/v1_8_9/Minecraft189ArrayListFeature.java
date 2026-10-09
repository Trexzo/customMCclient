package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;

final class Minecraft189ArrayListFeature
        implements AutoCloseable {
    private final ModuleController controller;
    private final Minecraft189ArrayListModule module;
    private final ModuleRegistry.Registration moduleRegistration;
    private final ModulePresentationRegistry.Registration presentation;
    private final SettingRegistry.Registration xSetting;
    private final SettingRegistry.Registration ySetting;
    private final SettingRegistry.Registration showCategoriesSetting;
    private final SettingRegistry.Registration groupCategoriesSetting;
    private final SettingPresentationRegistry.Registration xPresentation;
    private final SettingPresentationRegistry.Registration yPresentation;
    private final SettingPresentationRegistry.Registration showCategoriesPresentation;
    private final SettingPresentationRegistry.Registration groupCategoriesPresentation;
    private final ModuleSettingRegistry.Registration xBinding;
    private final ModuleSettingRegistry.Registration yBinding;
    private final ModuleSettingRegistry.Registration showCategoriesBinding;
    private final ModuleSettingRegistry.Registration groupCategoriesBinding;
    private final SettingRegistry.Registration maxVisibleSetting;
    private final SettingRegistry.Registration showOverflowSetting;
    private final SettingPresentationRegistry.Registration maxVisiblePresentation;
    private final SettingPresentationRegistry.Registration showOverflowPresentation;
    private final ModuleSettingRegistry.Registration maxVisibleBinding;
    private final ModuleSettingRegistry.Registration showOverflowBinding;
    private boolean closed;

    private Minecraft189ArrayListFeature(
            final ModuleController controller,
            final Minecraft189ArrayListModule module,
            final ModuleRegistry.Registration moduleRegistration,
            final ModulePresentationRegistry.Registration presentation,
            final SettingRegistry.Registration xSetting,
            final SettingRegistry.Registration ySetting,
            final SettingRegistry.Registration showCategoriesSetting,
            final SettingRegistry.Registration groupCategoriesSetting,
            final SettingPresentationRegistry.Registration xPresentation,
            final SettingPresentationRegistry.Registration yPresentation,
            final SettingPresentationRegistry.Registration showCategoriesPresentation,
            final SettingPresentationRegistry.Registration groupCategoriesPresentation,
            final ModuleSettingRegistry.Registration xBinding,
            final ModuleSettingRegistry.Registration yBinding,
            final ModuleSettingRegistry.Registration showCategoriesBinding,
            final ModuleSettingRegistry.Registration groupCategoriesBinding,
            final SettingRegistry.Registration maxVisibleSetting,
            final SettingRegistry.Registration showOverflowSetting,
            final SettingPresentationRegistry.Registration maxVisiblePresentation,
            final SettingPresentationRegistry.Registration showOverflowPresentation,
            final ModuleSettingRegistry.Registration maxVisibleBinding,
            final ModuleSettingRegistry.Registration showOverflowBinding) {
        this.controller = controller;
        this.module = module;
        this.moduleRegistration = moduleRegistration;
        this.presentation = presentation;
        this.xSetting = xSetting;
        this.ySetting = ySetting;
        this.showCategoriesSetting = showCategoriesSetting;
        this.groupCategoriesSetting = groupCategoriesSetting;
        this.xPresentation = xPresentation;
        this.yPresentation = yPresentation;
        this.showCategoriesPresentation = showCategoriesPresentation;
        this.groupCategoriesPresentation = groupCategoriesPresentation;
        this.xBinding = xBinding;
        this.yBinding = yBinding;
        this.showCategoriesBinding = showCategoriesBinding;
        this.groupCategoriesBinding = groupCategoriesBinding;
        this.maxVisibleSetting = maxVisibleSetting;
        this.showOverflowSetting = showOverflowSetting;
        this.maxVisiblePresentation = maxVisiblePresentation;
        this.showOverflowPresentation = showOverflowPresentation;
        this.maxVisibleBinding = maxVisibleBinding;
        this.showOverflowBinding = showOverflowBinding;
    }

    static Minecraft189ArrayListFeature install(
            final ModuleRegistry modules,
            final ModuleController controller,
            final ModulePresentationRegistry presentations,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        Objects.requireNonNull(modules, "modules");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(presentations, "presentations");
        Objects.requireNonNull(moduleSettings, "moduleSettings");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(settingPresentations, "settingPresentations");

        final Minecraft189ArrayListModule module =
                new Minecraft189ArrayListModule(
                        modules,
                        controller,
                        presentations,
                        renderPipeline,
                        hostCallbacks);

        ModuleRegistry.Registration moduleRegistration = null;
        ModulePresentationRegistry.Registration presentation = null;
        SettingRegistry.Registration xSetting = null;
        SettingRegistry.Registration ySetting = null;
        SettingRegistry.Registration showCategoriesSetting = null;
        SettingRegistry.Registration groupCategoriesSetting = null;
        SettingPresentationRegistry.Registration xPresentation = null;
        SettingPresentationRegistry.Registration yPresentation = null;
        SettingPresentationRegistry.Registration showCategoriesPresentation = null;
        SettingPresentationRegistry.Registration groupCategoriesPresentation = null;
        ModuleSettingRegistry.Registration xBinding = null;
        ModuleSettingRegistry.Registration yBinding = null;
        ModuleSettingRegistry.Registration showCategoriesBinding = null;
        ModuleSettingRegistry.Registration groupCategoriesBinding = null;
        SettingRegistry.Registration maxVisibleSetting = null;
        SettingRegistry.Registration showOverflowSetting = null;
        SettingPresentationRegistry.Registration maxVisiblePresentation = null;
        SettingPresentationRegistry.Registration showOverflowPresentation = null;
        ModuleSettingRegistry.Registration maxVisibleBinding = null;
        ModuleSettingRegistry.Registration showOverflowBinding = null;

        try {
            moduleRegistration =
                    modules.register(module);
            presentation =
                    presentations.register(
                            new ModuleDescriptor(
                                    Minecraft189ArrayListModule.ID,
                                    "Array List",
                                    "Shows enabled modules in the HUD.",
                                    Minecraft189FeatureCatalog
                                            .VISUALS_CATEGORY_ID,
                                    10));

            xSetting =
                    settings.register(
                            module.xSetting());
            ySetting =
                    settings.register(
                            module.ySetting());
            showCategoriesSetting = settings.register(module.showCategoriesSetting());
            groupCategoriesSetting = settings.register(module.groupCategoriesSetting());
            maxVisibleSetting = settings.register(module.maxVisibleSetting());
            showOverflowSetting = settings.register(module.showOverflowSetting());

            xPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189ArrayListModule.X_SETTING_ID,
                                    "X",
                                    SettingValueKind.INTEGER,
                                    0,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));
            yPresentation =
                    settingPresentations.register(
                            new SettingDescriptor(
                                    Minecraft189ArrayListModule.Y_SETTING_ID,
                                    "Y",
                                    SettingValueKind.INTEGER,
                                    10,
                                    new SettingNumericSpec(
                                            0.0D,
                                            4096.0D,
                                            1.0D)));

            showCategoriesPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ArrayListModule.SHOW_CATEGORIES_SETTING_ID,
                            "Show Categories", SettingValueKind.BOOLEAN, 20));
            groupCategoriesPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ArrayListModule.GROUP_CATEGORIES_SETTING_ID,
                            "Group by Category", SettingValueKind.BOOLEAN, 30));

            maxVisiblePresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ArrayListModule.MAX_VISIBLE_SETTING_ID,
                            "Maximum Rows", SettingValueKind.INTEGER, 40,
                            new SettingNumericSpec(1.0D, 128.0D, 1.0D)));
            showOverflowPresentation = settingPresentations.register(
                    new SettingDescriptor(
                            Minecraft189ArrayListModule.SHOW_OVERFLOW_SETTING_ID,
                            "Show Overflow Count", SettingValueKind.BOOLEAN, 50));

            xBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189ArrayListModule.ID,
                                    Minecraft189ArrayListModule.X_SETTING_ID,
                                    0));
            yBinding =
                    moduleSettings.register(
                            new ModuleSettingBinding(
                                    Minecraft189ArrayListModule.ID,
                                    Minecraft189ArrayListModule.Y_SETTING_ID,
                                    10));

            showCategoriesBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ArrayListModule.ID,
                            Minecraft189ArrayListModule.SHOW_CATEGORIES_SETTING_ID, 20));
            groupCategoriesBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ArrayListModule.ID,
                            Minecraft189ArrayListModule.GROUP_CATEGORIES_SETTING_ID, 30));

            maxVisibleBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ArrayListModule.ID,
                            Minecraft189ArrayListModule.MAX_VISIBLE_SETTING_ID, 40));
            showOverflowBinding = moduleSettings.register(
                    new ModuleSettingBinding(
                            Minecraft189ArrayListModule.ID,
                            Minecraft189ArrayListModule.SHOW_OVERFLOW_SETTING_ID, 50));

            return new Minecraft189ArrayListFeature(
                    controller,
                    module,
                    moduleRegistration,
                    presentation,
                    xSetting,
                    ySetting,
                    showCategoriesSetting,
                    groupCategoriesSetting,
                    xPresentation,
                    yPresentation,
                    showCategoriesPresentation,
                    groupCategoriesPresentation,
                    xBinding,
                    yBinding,
                    showCategoriesBinding,
                    groupCategoriesBinding,
                    maxVisibleSetting,
                    showOverflowSetting,
                    maxVisiblePresentation,
                    showOverflowPresentation,
                    maxVisibleBinding,
                    showOverflowBinding);
        } catch (RuntimeException failure) {
            closeQuietly(showOverflowBinding, failure);
            closeQuietly(maxVisibleBinding, failure);
            closeQuietly(showOverflowPresentation, failure);
            closeQuietly(maxVisiblePresentation, failure);
            closeQuietly(showOverflowSetting, failure);
            closeQuietly(maxVisibleSetting, failure);
            closeQuietly(groupCategoriesBinding, failure);
            closeQuietly(showCategoriesBinding, failure);
            closeQuietly(yBinding, failure);
            closeQuietly(xBinding, failure);
            closeQuietly(groupCategoriesPresentation, failure);
            closeQuietly(showCategoriesPresentation, failure);
            closeQuietly(yPresentation, failure);
            closeQuietly(xPresentation, failure);
            closeQuietly(groupCategoriesSetting, failure);
            closeQuietly(showCategoriesSetting, failure);
            closeQuietly(ySetting, failure);
            closeQuietly(xSetting, failure);
            closeQuietly(presentation, failure);
            closeQuietly(moduleRegistration, failure);
            throw failure;
        }
    }

    Minecraft189ArrayListModule module() {
        if (closed) {
            throw new IllegalStateException(
                    "array-list feature is closed");
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
                    Minecraft189ArrayListModule.ID)
                    != ModuleState.DISABLED) {
                controller.disable(
                        Minecraft189ArrayListModule.ID);
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        failure = close(showOverflowBinding, failure);
        failure = close(maxVisibleBinding, failure);
        failure = close(showOverflowPresentation, failure);
        failure = close(maxVisiblePresentation, failure);
        failure = close(showOverflowSetting, failure);
        failure = close(maxVisibleSetting, failure);
        failure = close(groupCategoriesBinding, failure);
        failure = close(showCategoriesBinding, failure);
        failure = close(yBinding, failure);
        failure = close(xBinding, failure);
        failure = close(groupCategoriesPresentation, failure);
        failure = close(showCategoriesPresentation, failure);
        failure = close(yPresentation, failure);
        failure = close(xPresentation, failure);
        failure = close(groupCategoriesSetting, failure);
        failure = close(showCategoriesSetting, failure);
        failure = close(ySetting, failure);
        failure = close(xSetting, failure);
        failure = close(presentation, failure);
        failure = close(moduleRegistration, failure);

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
                            "array-list feature close failed",
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
