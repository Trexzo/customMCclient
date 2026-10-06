package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiRuntime;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiRuntimeTest {
    @Test
    void installWiresPagesInputRenderAndDetailNavigationAsOneRuntime() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("render.esp"));
        final ModuleController moduleController =
                new ModuleController(modules);

        final ModulePresentationRegistry modulePresentations =
                new ModulePresentationRegistry();
        modulePresentations.register(
                new ModuleDescriptor(
                        "render.esp",
                        "ESP",
                        "Highlights entities"));

        final SettingRegistry settings =
                new SettingRegistry();
        settings.register(
                new Setting<Boolean>(
                        "esp.enabled",
                        Boolean.FALSE,
                        value -> true,
                        SettingCodecs.BOOLEAN));

        final SettingPresentationRegistry settingPresentations =
                new SettingPresentationRegistry();
        settingPresentations.register(
                new SettingDescriptor(
                        "esp.enabled",
                        "ESP Enabled",
                        SettingValueKind.BOOLEAN,
                        0));

        final ModuleSettingRegistry moduleSettings =
                new ModuleSettingRegistry(
                        modules,
                        settings);
        moduleSettings.register(
                new ModuleSettingBinding(
                        "render.esp",
                        "esp.enabled",
                        0));

        final RenderPipeline pipeline =
                new RenderPipeline();
        final ServiceRegistry services =
                new ServiceRegistry();
        final AtomicInteger renderCalls =
                new AtomicInteger();
        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);

        final ClickGuiRuntime runtime =
                ClickGuiRuntime.install(
                        modules,
                        moduleController,
                        modulePresentations,
                        new ModuleCategoryRegistry(),
                        moduleSettings,
                        settings,
                        settingPresentations,
                        pipeline,
                        services,
                        frame -> viewport,
                        UiThemes::darkDefault,
                        (frame, uiViewport, commands) ->
                                renderCalls.incrementAndGet());

        assertSame(
                runtime.model(),
                services.require(
                        ClickGuiModel.class));
        assertSame(
                runtime.input(),
                services.require(
                        ClickGuiInputController.class));
        assertEquals(
                Arrays.asList(
                        ClickGuiRuntime.MODULES_PAGE_ID,
                        ClickGuiRuntime.SETTINGS_PAGE_ID,
                        ClickGuiRuntime.MODULE_DETAIL_PAGE_ID),
                Arrays.asList(
                        runtime.model().snapshot()
                                .pages().get(0).id(),
                        runtime.model().snapshot()
                                .pages().get(1).id(),
                        runtime.model().snapshot()
                                .pages().get(2).id()));
        assertNotNull(
                runtime.contents().find(
                        ClickGuiRuntime.MODULES_PAGE_ID));
        assertNotNull(
                runtime.contents().find(
                        ClickGuiRuntime.SETTINGS_PAGE_ID));
        assertNotNull(
                runtime.contents().find(
                        ClickGuiRuntime.MODULE_DETAIL_PAGE_ID));
        assertEquals(
                1,
                pipeline.snapshotFor(
                        RenderStage.HUD)
                        .size());
        assertSame(
                runtime.renderPass(),
                pipeline.snapshotFor(
                        RenderStage.HUD)
                        .get(0));

        runtime.model().open();
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        1L,
                        0.0F));
        assertEquals(
                1,
                renderCalls.get());

        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);
        assertTrue(
                runtime.input().pointer(
                        new UiPointerEvent(
                                layout.content().x()
                                        + 28.0F,
                                layout.content().y()
                                        + 62.0F,
                                UiPointerButton.RIGHT,
                                UiPointerAction.PRESS),
                        viewport));

        assertEquals(
                "render.esp",
                runtime.selection()
                        .selectedModuleId());
        assertEquals(
                ClickGuiRuntime.MODULE_DETAIL_PAGE_ID,
                runtime.model()
                        .snapshot()
                        .selectedPageId());

        runtime.close();

        assertTrue(runtime.closed());
        assertFalse(
                services.contains(
                        ClickGuiModel.class));
        assertFalse(
                services.contains(
                        ClickGuiInputController.class));
        assertTrue(
                pipeline.snapshotFor(
                        RenderStage.HUD)
                        .isEmpty());
        assertTrue(
                runtime.model()
                        .snapshot()
                        .pages()
                        .isEmpty());
        assertEquals(
                null,
                runtime.contents()
                        .find(
                                ClickGuiRuntime.MODULES_PAGE_ID));

        runtime.close();
        assertTrue(runtime.closed());
    }

    private static Module module(
            final String id) {
        return new Module() {
            @Override
            public String id() {
                return id;
            }
        };
    }
}
