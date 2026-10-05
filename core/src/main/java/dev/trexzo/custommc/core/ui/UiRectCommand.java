package dev.trexzo.custommc.core.ui;

public final class UiRectCommand implements UiDrawCommand {
    private final int layer;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final int argb;

    public UiRectCommand(
            final int layer,
            final float x,
            final float y,
            final float width,
            final float height,
            final int argb) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        requireFinite(width, "width");
        requireFinite(height, "height");
        if (width < 0.0F || height < 0.0F) {
            throw new IllegalArgumentException(
                    "width and height must be non-negative");
        }

        this.layer = layer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
}
