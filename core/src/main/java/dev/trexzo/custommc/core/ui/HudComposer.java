package dev.trexzo.custommc.core.ui;

import java.util.List;
import java.util.Objects;

public final class HudComposer {
    private static final HudPlacementResolver DEFAULT_PLACEMENT =
            new HudPlacementResolver() {
                @Override
                public HudPlacement placementFor(
                        final HudWidget widget) {
                    return new HudPlacement(
                            widget.anchor(),
                            widget.offsetX(),
                            widget.offsetY());
                }
            };

    private final HudLayoutEngine layoutEngine =
            new HudLayoutEngine();

    public List<UiDrawCommand> compose(
            final UiViewport viewport,
            final HudWidgetRegistry registry) {
        return compose(
                viewport,
                registry,
                DEFAULT_PLACEMENT);
    }

    public List<UiDrawCommand> compose(
            final UiViewport viewport,
            final HudWidgetRegistry registry,
            final HudPlacementResolver placements) {
        final HudLayoutSnapshot layout =
                layoutEngine.layout(
                        viewport,
                        registry,
                        placements);
        return compose(layout, viewport);
    }

    public List<UiDrawCommand> compose(
            final HudLayoutSnapshot layout,
            final UiViewport viewport) {
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(viewport, "viewport");

        final UiCommandBuffer commands =
                new UiCommandBuffer();

        for (HudLayoutEntry entry : layout.entries()) {
            entry.widget().draw(new HudDrawContext(
                    viewport,
                    entry.bounds(),
                    commands));
        }

        return commands.seal();
    }
}
