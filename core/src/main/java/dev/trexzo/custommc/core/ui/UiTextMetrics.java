package dev.trexzo.custommc.core.ui;

public final class UiTextMetrics {
    private final float width;
    private final float height;
    private final float baseline;

    public UiTextMetrics(
            final float width,
            final float height,
            final float baseline) {
        requireExtent(width, "width");
        requireExtent(height, "height");
        requireExtent(baseline, "baseline");
        if (baseline > height) {
            throw new IllegalArgumentException(
                    "baseline must not exceed height");
        }
        this.width = width;
        this.height = height;
        this.baseline = baseline;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float baseline() {
        return baseline;
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
