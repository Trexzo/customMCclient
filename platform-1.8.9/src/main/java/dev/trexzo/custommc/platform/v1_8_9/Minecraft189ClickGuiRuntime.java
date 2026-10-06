package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiThemeProvider;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiRuntime;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiGraphics;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiViewportSource;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189UiRenderer;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189UiViewportProvider;

import java.util.Objects;

public final class Minecraft189ClickGuiRuntime
        implements AutoCloseable {
    private final ClickGuiRuntime coreRuntime;
    private final Minecraft189UiViewportProvider viewportProvider;
    private final Minecraft189UiRenderer renderer;
    private boolean closed;

    private Minecraft189ClickGuiRuntime(
            final ClickGuiRuntime coreRuntime,
            final Minecraft189UiViewportProvider viewportProvider,
            final Minecraft189UiRenderer renderer) {
        this.coreRuntime = coreRuntime;
        this.viewportProvider = viewportProvider;
        this.renderer = renderer;
    }

    public static Minecraft189ClickGuiRuntime install(
            final Minecraft189Platform platform,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final LegacyUiViewportSource viewportSource,
            final LegacyUiGraphics graphics) {
        return install(
                platform,
                modulePresentations,
                moduleCategories,
                moduleSettings,
                null,
                settings,
                settingPresentations,
                viewportSource,
                UiThemes::darkDefault,
                graphics);
    }

    public static Minecraft189ClickGuiRuntime install(
            final Minecraft189Platform platform,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final ModuleKeybindRegistry moduleKeybinds,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final LegacyUiViewportSource viewportSource,
            final LegacyUiGraphics graphics) {
        return install(
                platform,
                modulePresentations,
                moduleCategories,
                moduleSettings,
                moduleKeybinds,
                settings,
                settingPresentations,
                viewportSource,
                UiThemes::darkDefault,
                graphics);
    }

    public static Minecraft189ClickGuiRuntime install(
            final Minecraft189Platform platform,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final LegacyUiViewportSource viewportSource,
            final UiThemeProvider themeProvider,
            final LegacyUiGraphics graphics) {
        return install(
                platform,
                modulePresentations,
                moduleCategories,
                moduleSettings,
                null,
                settings,
                settingPresentations,
                viewportSource,
                themeProvider,
                graphics);
    }

    public static Minecraft189ClickGuiRuntime install(
            final Minecraft189Platform platform,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final ModuleKeybindRegistry moduleKeybinds,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final LegacyUiViewportSource viewportSource,
            final UiThemeProvider themeProvider,
            final LegacyUiGraphics graphics) {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(modulePresentations, "modulePresentations");
        Objects.requireNonNull(moduleCategories, "moduleCategories");
        Objects.requireNonNull(moduleSettings, "moduleSettings");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(settingPresentations, "settingPresentations");
        Objects.requireNonNull(viewportSource, "viewportSource");
        Objects.requireNonNull(themeProvider, "themeProvider");
        Objects.requireNonNull(graphics, "graphics");

        final PlatformContext context =
                platform.requireContext();
        final RenderPipeline renderPipeline =
                context.services()
                        .require(RenderPipeline.class);
        final Minecraft189UiViewportProvider viewportProvider =
                new Minecraft189UiViewportProvider(
                        viewportSource);
        final Minecraft189UiRenderer renderer =
                new Minecraft189UiRenderer(
                        graphics);
        final ClickGuiRuntime coreRuntime =
                ClickGuiRuntime.install(
                        context.modules(),
                        context.moduleController(),
                        modulePresentations,
                        moduleCategories,
                        moduleSettings,
                        moduleKeybinds,
                        settings,
                        settingPresentations,
                        renderPipeline,
                        context.services(),
                        viewportProvider,
                        themeProvider,
                        renderer);

        return new Minecraft189ClickGuiRuntime(
                coreRuntime,
                viewportProvider,
                renderer);
    }

    public ClickGuiRuntime coreRuntime() {
        return coreRuntime;
    }

    public Minecraft189UiViewportProvider viewportProvider() {
        return viewportProvider;
    }

    public Minecraft189UiRenderer renderer() {
        return renderer;
    }

    public synchronized boolean closed() {
        return closed;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        coreRuntime.close();
    }
}
