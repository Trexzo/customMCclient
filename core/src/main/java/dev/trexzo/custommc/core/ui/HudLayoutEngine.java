package dev.trexzo.custommc.core.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HudLayoutEngine {
    public HudLayoutSnapshot layout(
            final UiViewport viewport,
            final HudWidgetRegistry registry,
            final HudPlacementResolver placements) {
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(placements, "placements");

        final List<HudLayoutEntry> entries =
                new ArrayList<HudLayoutEntry>();

        for (HudWidget widget : registry.snapshot()) {
            final UiSize size = Objects.requireNonNull(
                    widget.measure(viewport),
                    "widget.measure");
            final HudPlacement placement =
                    Objects.requireNonNull(
                            placements.placementFor(widget),
                            "placements.placementFor");
            entries.add(new HudLayoutEntry(
                    widget,
                    placement,
                    placement.resolve(viewport, size)));
        }

        return new HudLayoutSnapshot(entries);
    }
}
