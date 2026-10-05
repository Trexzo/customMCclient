package dev.trexzo.custommc.core.ui.clickgui;

final class ClickGuiContentScrollState {
    static final float SCROLL_STEP = 28.0F;

    private float offset;

    synchronized float offset(
            final float contentHeight,
            final float viewportHeight) {
        offset =
                clamp(
                        offset,
                        contentHeight,
                        viewportHeight);
        return offset;
    }

    synchronized void scroll(
            final float deltaY,
            final float contentHeight,
            final float viewportHeight) {
        requireFinite(deltaY, "deltaY");
        offset =
                clamp(
                        offset
                                - deltaY
                                * SCROLL_STEP,
                        contentHeight,
                        viewportHeight);
    }

    private static float clamp(
            final float value,
            final float contentHeight,
            final float viewportHeight) {
        requireNonNegativeFinite(
                contentHeight,
                "contentHeight");
        requireNonNegativeFinite(
                viewportHeight,
                "viewportHeight");

        return Math.max(
                0.0F,
                Math.min(
                        value,
                        Math.max(
                                0.0F,
                                contentHeight
                                        - viewportHeight)));
    }

    private static void requireNonNegativeFinite(
            final float value,
            final String name) {
        requireFinite(value, name);
        if (value < 0.0F) {
            throw new IllegalArgumentException(
                    name + " must be non-negative");
        }
    }

    private static void requireFinite(
            final float value,
            final String name) {
        if (Float.isNaN(value)
                || Float.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }
}
