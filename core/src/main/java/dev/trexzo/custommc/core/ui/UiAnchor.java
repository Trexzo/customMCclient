package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public enum UiAnchor {
    TOP_LEFT(0.0F, 0.0F),
    TOP_CENTER(0.5F, 0.0F),
    TOP_RIGHT(1.0F, 0.0F),
    CENTER_LEFT(0.0F, 0.5F),
    CENTER(0.5F, 0.5F),
    CENTER_RIGHT(1.0F, 0.5F),
    BOTTOM_LEFT(0.0F, 1.0F),
    BOTTOM_CENTER(0.5F, 1.0F),
    BOTTOM_RIGHT(1.0F, 1.0F);

    private final float horizontal;
    private final float vertical;

    UiAnchor(
            final float horizontal,
            final float vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public UiBounds resolve(
            final UiViewport viewport,
            final UiSize size,
            final float offsetX,
            final float offsetY) {
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(size, "size");
        requireFinite(offsetX, "offsetX");
        requireFinite(offsetY, "offsetY");

        final float x =
                (viewport.logicalWidth() - size.width())
                        * horizontal
                        + offsetX;
        final float y =
                (viewport.logicalHeight() - size.height())
                        * vertical
                        + offsetY;

        return new UiBounds(
                x,
                y,
                size.width(),
                size.height());
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
