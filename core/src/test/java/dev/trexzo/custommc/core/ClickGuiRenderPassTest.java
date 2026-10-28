package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRenderer;
import dev.trexzo.custommc.core.ui.UiTheme;
import dev.trexzo.custommc.core.ui.UiThemeProvider;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.UiViewportProvider;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiRenderPass;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class ClickGuiRenderPassTest {
    @Test
    void closedGuiSkipsProvidersAndRenderer() {
        final ClickGuiModel model =
                new ClickGuiModel();
        final AtomicInteger viewportCalls =
                new AtomicInteger();
        final AtomicInteger themeCalls =
                new AtomicInteger();
        final AtomicInteger renderCalls =
                new AtomicInteger();

        final ClickGuiRenderPass pass =
                new ClickGuiRenderPass(
                        "click-gui",
                        500,
                        model,
                        frame -> {
                            viewportCalls.incrementAndGet();
                            return new UiViewport(
                                    1200,
                                    800,
                                    1.0F);
                        },
                        () -> {
                            themeCalls.incrementAndGet();
                            return UiThemes.darkDefault();
                        },
                        (frame, viewport, commands) ->
                                renderCalls.incrementAndGet());

        pass.render(new RenderFrame(1L, 0.0F));

        assertEquals(RenderStage.HUD, pass.stage());
        assertEquals(0, viewportCalls.get());
        assertEquals(0, themeCalls.get());
        assertEquals(0, renderCalls.get());
    }

    @Test
    void visualEvidenceReportsOnlyRealRenderedPageTransitionsWhenOptedIn() {
        final ClickGuiModel model = new ClickGuiModel();
        model.register(new ClickGuiPage("modules", "Modules", 0));
        model.register(new ClickGuiPage("settings", "Settings", 10));
        model.open();
        final AtomicInteger actualRenders = new AtomicInteger();
        final ClickGuiRenderPass pass = new ClickGuiRenderPass(
                "click-gui", 500, model,
                frame -> new UiViewport(854, 480, 1.0F),
                UiThemes::graphite,
                (frame, viewport, commands) -> actualRenders.incrementAndGet());
        final String property = "custommc.acceptance.reportNativeVisualPage";
        final String beforeFlag = System.getProperty(property);
        final PrintStream original = System.out;
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setProperty(property, "true");
            System.setOut(new PrintStream(captured, true));
            pass.render(new RenderFrame(1L, 0.0F));
            pass.render(new RenderFrame(2L, 0.0F));
            model.select("settings");
            pass.render(new RenderFrame(3L, 0.0F));
            model.close();
            pass.render(new RenderFrame(4L, 0.0F));
        } finally {
            System.setOut(original);
            if (beforeFlag == null) System.clearProperty(property);
            else System.setProperty(property, beforeFlag);
        }
        final String logs = new String(captured.toByteArray(),
                StandardCharsets.UTF_8);
        assertEquals(3, actualRenders.get());
        assertEquals(1, logs.split("CUSTOMMC_189_NATIVE_VISUAL_PAGE=modules", -1).length - 1);
        assertEquals(1, logs.split("CUSTOMMC_189_NATIVE_VISUAL_PAGE=settings", -1).length - 1);
    }

    @Test
    void openGuiComposesAndRendersThroughUiBoundary() {
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "combat",
                        "Combat",
                        0));
        model.open();

        final AtomicInteger renderCalls =
                new AtomicInteger();
        final AtomicLong frameSeen =
                new AtomicLong(-1L);
        final AtomicInteger commandCount =
                new AtomicInteger();

        final UiViewportProvider viewportProvider =
                new UiViewportProvider() {
                    @Override
                    public UiViewport viewport(
                            final RenderFrame frame) {
                        return new UiViewport(
                                1200,
                                800,
                                1.0F);
                    }
                };

        final UiThemeProvider themeProvider =
                new UiThemeProvider() {
                    @Override
                    public UiTheme theme() {
                        return UiThemes.darkDefault();
                    }
                };

        final UiRenderer renderer =
                new UiRenderer() {
                    @Override
                    public void render(
                            final RenderFrame frame,
                            final UiViewport viewport,
                            final List<UiDrawCommand> commands) {
                        renderCalls.incrementAndGet();
                        frameSeen.set(frame.frameIndex());
                        commandCount.set(commands.size());
                        assertEquals(
                                1200.0F,
                                viewport.logicalWidth());
                    }
                };

        final ClickGuiRenderPass pass =
                new ClickGuiRenderPass(
                        "click-gui",
                        500,
                        model,
                        viewportProvider,
                        themeProvider,
                        renderer);

        pass.render(new RenderFrame(42L, 0.5F));

        assertEquals(1, renderCalls.get());
        assertEquals(42L, frameSeen.get());
        assertFalse(commandCount.get() == 0);
    }
}
