package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class HudLayoutEntry {
    private final HudWidget widget;
    private final HudPlacement placement;
    private final UiBounds bounds;

    public HudLayoutEntry(
            final HudWidget widget,
            final HudPlacement placement,
            final UiBounds bounds) {
        this.widget = Objects.requireNonNull(widget, "widget");
        this.placement = Objects.requireNonNull(
                placement,
                "placement");
        this.bounds = Objects.requireNonNull(bounds, "bounds");
    }

    public HudWidget widget() {
        return widget;
    }

    public HudPlacement placement() {
        return placement;
    }

    public UiBounds bounds() {
        return bounds;
    }
}
