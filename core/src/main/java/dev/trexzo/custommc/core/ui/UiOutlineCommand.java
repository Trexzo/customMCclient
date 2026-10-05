package dev.trexzo.custommc.core.ui;

public final class UiOutlineCommand
        implements UiDrawCommand {
    private final int layer;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float thickness;
    private final int argb;

    public UiOutlineCommand(
            final int layer,
            final float x,
            final float y,
            final float width,
            final float height,
            final float thickness,
            final int argb) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        requireExtent(width, "width");
        requireExtent(height, "height");
        requirePositive(thickness, "thickness");

        this.layer = layer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.thickness = thickness;
        this.argb = argb;
    }

    @Override
    public int layer() {
        return layer;
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

    public float thickness() {
        return thickness;
    }

    public int argb() {
        return argb;
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

    private static void requirePositive(
            final float value,
            final String name) {
        requireFinite(value, name);
        if (value <= 0.0F) {
            throw new IllegalArgumentException(
                    name + " must be positive");
        }
    }
}
