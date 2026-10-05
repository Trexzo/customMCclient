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
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(placements, "placements");

        final UiCommandBuffer commands =
                new UiCommandBuffer();

        for (HudWidget widget : registry.snapshot()) {
            final UiSize size = Objects.requireNonNull(
                    widget.measure(viewport),
                    "widget.measure");
            final HudPlacement placement =
                    Objects.requireNonNull(
                            placements.placementFor(widget),
                            "placements.placementFor");
            final UiBounds bounds =
                    placement.resolve(
                            viewport,
                            size);

            widget.draw(new HudDrawContext(
                    viewport,
                    bounds,
                    commands));
        }

        return commands.seal();
    }
}
