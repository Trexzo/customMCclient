package dev.trexzo.custommc.core.ui;

public final class UiSize {
    private final float width;
    private final float height;

    public UiSize(
            final float width,
            final float height) {
        requireExtent(width, "width");
        requireExtent(height, "height");
        this.width = width;
        this.height = height;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    private static void requireExtent(
            final float value,
            final String name) {
        if (Float.isNaN(value)
                || Float.isInfinite(value)
                || value < 0.0F) {
            throw new IllegalArgumentException(
                    name + " must be finite and non-negative");
        }
    }
}
