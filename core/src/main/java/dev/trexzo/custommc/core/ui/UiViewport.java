package dev.trexzo.custommc.core.ui;

public final class UiViewport {
    private final int pixelWidth;
    private final int pixelHeight;
    private final float scale;

    public UiViewport(
            final int pixelWidth,
            final int pixelHeight,
            final float scale) {
        if (pixelWidth <= 0 || pixelHeight <= 0) {
            throw new IllegalArgumentException(
                    "viewport dimensions must be positive");
        }
        if (Float.isNaN(scale)
                || Float.isInfinite(scale)
                || scale <= 0.0F) {
            throw new IllegalArgumentException(
                    "scale must be finite and positive");
        }

        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.scale = scale;
    }

    public int pixelWidth() {
        return pixelWidth;
    }

    public int pixelHeight() {
        return pixelHeight;
    }

    public float scale() {
        return scale;
    }

    public float logicalWidth() {
        return pixelWidth / scale;
    }

    public float logicalHeight() {
        return pixelHeight / scale;
    }
}
