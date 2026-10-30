package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRenderer;
import dev.trexzo.custommc.core.ui.UiTheme;
import dev.trexzo.custommc.core.ui.UiThemeProvider;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.UiViewportProvider;

import java.util.List;
import java.util.Objects;

public final class ClickGuiRenderPass
        implements RenderPass {
    private final String id;
    private final int priority;
    private final ClickGuiModel model;
    private final ClickGuiComposer composer;
    private final UiViewportProvider viewportProvider;
    private final UiThemeProvider themeProvider;
    private final UiRenderer renderer;
    // Only used by opt-in original 1.8.9 screenshot evidence.
    private String lastReportedVisualPage;
    private static final java.util.Set<String> MODULE_PAGE_NAMES =
            java.util.Collections.unmodifiableSet(
                    new java.util.HashSet<String>(
                            java.util.Arrays.asList(
                                    "modules", "settings", "module-detail")));

    public ClickGuiRenderPass(
            final String id,
            final int priority,
            final ClickGuiModel model,
            final UiViewportProvider viewportProvider,
            final UiThemeProvider themeProvider,
            final UiRenderer renderer) {
        this(
                id,
                priority,
                model,
                new ClickGuiComposer(),
                viewportProvider,
                themeProvider,
                renderer);
    }

    public ClickGuiRenderPass(
            final String id,
            final int priority,
            final ClickGuiModel model,
            final ClickGuiContentRegistry contentRegistry,
            final UiViewportProvider viewportProvider,
            final UiThemeProvider themeProvider,
            final UiRenderer renderer) {
        this(
                id,
                priority,
                model,
                new ClickGuiComposer(
                        contentRegistry),
                viewportProvider,
                themeProvider,
                renderer);
    }

    ClickGuiRenderPass(
            final String id,
            final int priority,
            final ClickGuiModel model,
            final ClickGuiComposer composer,
            final UiViewportProvider viewportProvider,
            final UiThemeProvider themeProvider,
            final UiRenderer renderer) {
        this.id = requireId(id);
        this.priority = priority;
        this.model =
                Objects.requireNonNull(
                        model,
                        "model");
        this.composer =
                Objects.requireNonNull(
                        composer,
                        "composer");
        this.viewportProvider =
                Objects.requireNonNull(
                        viewportProvider,
                        "viewportProvider");
        this.themeProvider =
                Objects.requireNonNull(
                        themeProvider,
                        "themeProvider");
        this.renderer =
                Objects.requireNonNull(
                        renderer,
                        "renderer");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RenderStage stage() {
        return RenderStage.HUD;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public void render(final RenderFrame frame) {
        Objects.requireNonNull(frame, "frame");

        final ClickGuiSnapshot snapshot =
                model.snapshot();
        if (!snapshot.open()) {
            lastReportedVisualPage = null;
            return;
        }

        final UiViewport viewport =
                Objects.requireNonNull(
                        viewportProvider.viewport(frame),
                        "viewportProvider.viewport");
        final UiTheme theme =
                Objects.requireNonNull(
                        themeProvider.theme(),
                        "themeProvider.theme");
        final List<UiDrawCommand> commands =
                composer.compose(
                        snapshot,
                        viewport,
                        theme);

        renderer.render(
                frame,
                viewport,
                commands);

        // Read-only original render-pass witness: this marker is emitted
        // only after the real renderer callback returns, and only when the
        // native screenshot harness explicitly opts in. It never changes
        // model selection, GUI focus, controller or world state.
        if (Boolean.getBoolean("custommc.acceptance.reportNativeVisualPage")) {
            final String page = snapshot.selectedPageId();
            if (page != null && !page.equals(lastReportedVisualPage)) {
                if (MODULE_PAGE_NAMES.contains(page)) {
                    System.out.println("CUSTOMMC_189_NATIVE_VISUAL_PAGE=" + page);
                    if ("modules".equals(page)
                            && Boolean.getBoolean(
                                    "custommc.acceptance.reportNativeHudVisual")) {
                        // Original renderer-owned viewport, not an
                        // X11/screenshot guess. This read-only witness
                        // drives the separate genuine physical click test.
                        System.out.println("CUSTOMMC_189_S31_ORIGINAL_UI_VIEWPORT="
                                + viewport.pixelWidth() + "x"
                                + viewport.pixelHeight()
                                + ",scale:" + viewport.scale());
                    }
                }
                lastReportedVisualPage = page;
            }
        }
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        final String value = id.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "ClickGUI render pass id must not be blank");
        }
        return value;
    }
}
