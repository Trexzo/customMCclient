package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiRenderer;
import dev.trexzo.custommc.core.ui.UiThemeProvider;
import dev.trexzo.custommc.core.ui.UiViewportProvider;

import java.util.Objects;

public final class ClickGuiRuntime
        implements AutoCloseable {
    public static final String MODULES_PAGE_ID =
            "modules";
    public static final String SETTINGS_PAGE_ID =
            "settings";
    public static final String MODULE_DETAIL_PAGE_ID =
            "module-detail";
    public static final String RENDER_PASS_ID =
            "click-gui";

    private static final int MODULES_PAGE_PRIORITY = 0;
    private static final int SETTINGS_PAGE_PRIORITY = 10;
    private static final int MODULE_DETAIL_PAGE_PRIORITY = 20;
    private static final int RENDER_PASS_PRIORITY = 500;

    private final ClickGuiModel model;
    private final ClickGuiContentRegistry contents;
    private final ModuleSelectionModel selection;
    private final UiFocusManager focusManager;
    private final ClickGuiInputController input;
    private final ClickGuiRenderPass renderPass;

    private final ClickGuiModel.Registration modulesPage;
    private final ClickGuiModel.Registration settingsPage;
    private final ClickGuiModel.Registration detailPage;
    private final ClickGuiContentRegistry.Registration modulesContent;
    private final ClickGuiContentRegistry.Registration settingsContent;
    private final ClickGuiContentRegistry.Registration detailContent;
    private final RenderPipeline.Registration renderRegistration;
    private final ServiceRegistry.Registration modelService;
    private final ServiceRegistry.Registration inputService;

    private boolean closed;

    private ClickGuiRuntime(
            final ClickGuiModel model,
            final ClickGuiContentRegistry contents,
            final ModuleSelectionModel selection,
            final UiFocusManager focusManager,
            final ClickGuiInputController input,
            final ClickGuiRenderPass renderPass,
            final ClickGuiModel.Registration modulesPage,
            final ClickGuiModel.Registration settingsPage,
            final ClickGuiModel.Registration detailPage,
            final ClickGuiContentRegistry.Registration modulesContent,
            final ClickGuiContentRegistry.Registration settingsContent,
            final ClickGuiContentRegistry.Registration detailContent,
            final RenderPipeline.Registration renderRegistration,
            final ServiceRegistry.Registration modelService,
            final ServiceRegistry.Registration inputService) {
        this.model = model;
        this.contents = contents;
        this.selection = selection;
        this.focusManager = focusManager;
        this.input = input;
        this.renderPass = renderPass;
        this.modulesPage = modulesPage;
        this.settingsPage = settingsPage;
        this.detailPage = detailPage;
        this.modulesContent = modulesContent;
        this.settingsContent = settingsContent;
        this.detailContent = detailContent;
        this.renderRegistration = renderRegistration;
        this.modelService = modelService;
        this.inputService = inputService;
    }

    public static ClickGuiRuntime install(
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final RenderPipeline renderPipeline,
            final ServiceRegistry services,
            final UiViewportProvider viewportProvider,
            final UiThemeProvider themeProvider,
            final UiRenderer renderer) {
        return install(
                modules,
                moduleController,
                modulePresentations,
                moduleCategories,
                moduleSettings,
                null,
                null,
                settings,
                settingPresentations,
                renderPipeline,
                services,
                viewportProvider,
                themeProvider,
                renderer);
    }

    public static ClickGuiRuntime install(
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final ModuleKeybindRegistry moduleKeybinds,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final RenderPipeline renderPipeline,
            final ServiceRegistry services,
            final UiViewportProvider viewportProvider,
            final UiThemeProvider themeProvider,
            final UiRenderer renderer) {
        return install(
                modules,
                moduleController,
                modulePresentations,
                moduleCategories,
                moduleSettings,
                moduleKeybinds,
                null,
                settings,
                settingPresentations,
                renderPipeline,
                services,
                viewportProvider,
                themeProvider,
                renderer);
    }

    public static ClickGuiRuntime install(
            final ModuleRegistry modules,
            final ModuleController moduleController,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final ModuleKeybindRegistry moduleKeybinds,
            final ModuleKeybindAssignments moduleKeybindAssignments,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final RenderPipeline renderPipeline,
            final ServiceRegistry services,
            final UiViewportProvider viewportProvider,
            final UiThemeProvider themeProvider,
            final UiRenderer renderer) {
        Objects.requireNonNull(modules, "modules");
        Objects.requireNonNull(moduleController, "moduleController");
        Objects.requireNonNull(modulePresentations, "modulePresentations");
        Objects.requireNonNull(moduleCategories, "moduleCategories");
        Objects.requireNonNull(moduleSettings, "moduleSettings");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(settingPresentations, "settingPresentations");
        Objects.requireNonNull(renderPipeline, "renderPipeline");
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(viewportProvider, "viewportProvider");
        Objects.requireNonNull(themeProvider, "themeProvider");
        Objects.requireNonNull(renderer, "renderer");

        preflight(renderPipeline, services);

        final ClickGuiModel model =
                new ClickGuiModel();
        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        final ModuleSelectionModel selection =
                new ModuleSelectionModel(modules);
        final UiFocusManager focusManager =
                new UiFocusManager();

        final ModuleListPageContent modulesContentValue =
                new ModuleListPageContent(
                        modules,
                        moduleController,
                        modulePresentations,
                        moduleCategories,
                        selection,
                        MODULE_DETAIL_PAGE_ID);
        final SettingListPageContent settingsContentValue =
                new SettingListPageContent(
                        settings,
                        settingPresentations);
        final ModuleDetailPageContent detailContentValue =
                new ModuleDetailPageContent(
                        selection,
                        moduleController,
                        modulePresentations,
                        moduleSettings,
                        moduleKeybinds,
                        moduleKeybindAssignments,
                        settings,
                        settingPresentations);
        final ClickGuiInputController input =
                new ClickGuiInputController(
                        model,
                        contents,
                        focusManager);
        final ClickGuiRenderPass renderPass =
                new ClickGuiRenderPass(
                        RENDER_PASS_ID,
                        RENDER_PASS_PRIORITY,
                        model,
                        contents,
                        viewportProvider,
                        themeProvider,
                        renderer);

        ClickGuiModel.Registration modulesPage = null;
        ClickGuiModel.Registration settingsPage = null;
        ClickGuiModel.Registration detailPage = null;
        ClickGuiContentRegistry.Registration modulesContent = null;
        ClickGuiContentRegistry.Registration settingsContent = null;
        ClickGuiContentRegistry.Registration detailContent = null;
        RenderPipeline.Registration renderRegistration = null;
        ServiceRegistry.Registration modelService = null;
        ServiceRegistry.Registration inputService = null;

        try {
            modulesPage =
                    model.register(
                            new ClickGuiPage(
                                    MODULES_PAGE_ID,
                                    "Modules",
                                    MODULES_PAGE_PRIORITY));
            settingsPage =
                    model.register(
                            new ClickGuiPage(
                                    SETTINGS_PAGE_ID,
                                    "Settings",
                                    SETTINGS_PAGE_PRIORITY));
            detailPage =
                    model.register(
                            new ClickGuiPage(
                                    MODULE_DETAIL_PAGE_ID,
                                    "Module Details",
                                    MODULE_DETAIL_PAGE_PRIORITY));

            modulesContent =
                    contents.register(
                            MODULES_PAGE_ID,
                            modulesContentValue);
            settingsContent =
                    contents.register(
                            SETTINGS_PAGE_ID,
                            settingsContentValue);
            detailContent =
                    contents.register(
                            MODULE_DETAIL_PAGE_ID,
                            detailContentValue);

            renderRegistration =
                    renderPipeline.register(
                            renderPass);
            modelService =
                    services.registerManaged(
                            ClickGuiModel.class,
                            model);
            inputService =
                    services.registerManaged(
                            ClickGuiInputController.class,
                            input);

            return new ClickGuiRuntime(
                    model,
                    contents,
                    selection,
                    focusManager,
                    input,
                    renderPass,
                    modulesPage,
                    settingsPage,
                    detailPage,
                    modulesContent,
                    settingsContent,
                    detailContent,
                    renderRegistration,
                    modelService,
                    inputService);
        } catch (RuntimeException failure) {
            if (inputService != null) {
                inputService.close();
            }
            if (modelService != null) {
                modelService.close();
            }
            input.close();
            if (renderRegistration != null) {
                renderRegistration.close();
            }
            if (detailContent != null) {
                detailContent.close();
            }
            if (settingsContent != null) {
                settingsContent.close();
            }
            if (modulesContent != null) {
                modulesContent.close();
            }
            if (detailPage != null) {
                detailPage.close();
            }
            if (settingsPage != null) {
                settingsPage.close();
            }
            if (modulesPage != null) {
                modulesPage.close();
            }
            throw failure;
        }
    }

    public ClickGuiModel model() {
        return model;
    }

    public ClickGuiContentRegistry contents() {
        return contents;
    }

    public ModuleSelectionModel selection() {
        return selection;
    }

    public UiFocusManager focusManager() {
        return focusManager;
    }

    public ClickGuiInputController input() {
        return input;
    }

    public ClickGuiRenderPass renderPass() {
        return renderPass;
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

        model.close();
        inputService.close();
        modelService.close();
        input.close();
        renderRegistration.close();
        detailContent.close();
        settingsContent.close();
        modulesContent.close();
        detailPage.close();
        settingsPage.close();
        modulesPage.close();
        selection.clear();
    }

    private static void preflight(
            final RenderPipeline renderPipeline,
            final ServiceRegistry services) {
        if (services.contains(ClickGuiModel.class)
                || services.contains(
                ClickGuiInputController.class)) {
            throw new IllegalArgumentException(
                    "ClickGUI services already installed");
        }

        for (RenderPass pass :
                renderPipeline.snapshotFor(
                        RenderStage.HUD)) {
            if (RENDER_PASS_ID.equals(
                    pass.id())) {
                throw new IllegalArgumentException(
                        "ClickGUI render pass already installed");
            }
        }
    }
}
