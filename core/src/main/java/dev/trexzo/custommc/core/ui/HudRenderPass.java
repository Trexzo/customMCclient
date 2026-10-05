package dev.trexzo.custommc.core.ui;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderStage;

import java.util.List;
import java.util.Objects;

public final class HudRenderPass implements RenderPass {
    private final String id;
    private final int priority;
    private final HudWidgetRegistry widgets;
    private final HudPlacementResolver placements;
    private final UiViewportProvider viewportProvider;
    private final UiRenderer renderer;
    private final HudComposer composer = new HudComposer();

    public HudRenderPass(
            final String id,
            final int priority,
            final HudWidgetRegistry widgets,
            final HudPlacementResolver placements,
            final UiViewportProvider viewportProvider,
            final UiRenderer renderer) {
        this.id = requireId(id);
        this.priority = priority;
        this.widgets = Objects.requireNonNull(
                widgets,
                "widgets");
        this.placements = Objects.requireNonNull(
                placements,
                "placements");
        this.viewportProvider = Objects.requireNonNull(
                viewportProvider,
                "viewportProvider");
        this.renderer = Objects.requireNonNull(
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
        final UiViewport viewport =
                Objects.requireNonNull(
                        viewportProvider.viewport(frame),
                        "viewportProvider.viewport");
        final List<UiDrawCommand> commands =
                composer.compose(
                        viewport,
                        widgets,
                        placements);

        renderer.render(
                frame,
                viewport,
                commands);
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "HUD render pass id must not be blank");
        }
        return id;
    }
}
