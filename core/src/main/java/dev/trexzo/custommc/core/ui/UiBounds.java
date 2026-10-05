package dev.trexzo.custommc.core.ui;

public final class UiBounds {
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public UiBounds(
            final float x,
            final float y,
            final float width,
            final float height) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        requireExtent(width, "width");
        requireExtent(height, "height");

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public boolean contains(
            final float pointX,
            final float pointY) {
        requireFinite(pointX, "pointX");
        requireFinite(pointY, "pointY");
        return pointX >= x
                && pointY >= y
                && pointX < x + width
                && pointY < y + height;
    }

    private static void requireFinite(
            final float value,
            final String name) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }

    private static void requireExtent(
            final float value,
            final String name) {
        requireFinite(value, name);
        if (value < 0.0F) {
            throw new IllegalArgumentException(
                    name + " must be non-negative");
        }
    }
}
