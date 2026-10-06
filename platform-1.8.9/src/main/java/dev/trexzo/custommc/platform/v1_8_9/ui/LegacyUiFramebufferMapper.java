package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.Objects;

public final class LegacyUiFramebufferMapper {
    public LegacyFramebufferRect scissor(
            final UiViewport viewport,
            final UiBounds bounds) {
        Objects.requireNonNull(
                viewport,
                "viewport");
        Objects.requireNonNull(
                bounds,
                "bounds");

        final double scale =
                viewport.scale();

        final int left =
                clamp(
                        floorPixel(
                                bounds.x()
                                        * scale),
                        0,
                        viewport.pixelWidth());
        final int topFromTop =
                clamp(
                        floorPixel(
                                bounds.y()
                                        * scale),
                        0,
                        viewport.pixelHeight());
        final int right =
                clamp(
                        ceilPixel(
                                (bounds.x()
                                        + bounds.width())
                                        * scale),
                        0,
                        viewport.pixelWidth());
        final int bottomFromTop =
                clamp(
                        ceilPixel(
                                (bounds.y()
                                        + bounds.height())
                                        * scale),
                        0,
                        viewport.pixelHeight());

        final int normalizedRight =
                Math.max(
                        left,
                        right);
        final int normalizedBottomFromTop =
                Math.max(
                        topFromTop,
                        bottomFromTop);

        return new LegacyFramebufferRect(
                left,
                viewport.pixelHeight()
                        - normalizedBottomFromTop,
                normalizedRight - left,
                normalizedBottomFromTop
                        - topFromTop);
    }

    private static int floorPixel(
            final double value) {
        return (int) Math.floor(value);
    }

    private static int ceilPixel(
            final double value) {
        return (int) Math.ceil(value);
    }

    private static int clamp(
            final int value,
            final int minimum,
            final int maximum) {
        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value));
    }
}
