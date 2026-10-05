package dev.trexzo.custommc.core.ui;

public final class UiScrollEvent {
    private final float x;
    private final float y;
    private final float deltaY;

    public UiScrollEvent(
            final float x,
            final float y,
            final float deltaY) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        requireFinite(deltaY, "deltaY");
        if (deltaY == 0.0F) {
            throw new IllegalArgumentException(
                    "deltaY must be non-zero");
        }

        this.x = x;
        this.y = y;
        this.deltaY = deltaY;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float deltaY() {
        return deltaY;
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
