package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class HudPlacement {
    private final UiAnchor anchor;
    private final float offsetX;
    private final float offsetY;

    public HudPlacement(
            final UiAnchor anchor,
            final float offsetX,
            final float offsetY) {
        this.anchor = Objects.requireNonNull(anchor, "anchor");
        requireFinite(offsetX, "offsetX");
        requireFinite(offsetY, "offsetY");
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public UiAnchor anchor() {
        return anchor;
    }

    public float offsetX() {
        return offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    public UiBounds resolve(
            final UiViewport viewport,
            final UiSize size) {
        return anchor.resolve(
                viewport,
                size,
                offsetX,
                offsetY);
    }

    private static void requireFinite(
            final float value,
            final String name) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }
}
