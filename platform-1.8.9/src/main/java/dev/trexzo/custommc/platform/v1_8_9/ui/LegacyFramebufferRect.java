package dev.trexzo.custommc.platform.v1_8_9.ui;

public final class LegacyFramebufferRect {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public LegacyFramebufferRect(
            final int x,
            final int y,
            final int width,
            final int height) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException(
                    "framebuffer coordinates must be non-negative");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException(
                    "framebuffer size must be non-negative");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public LegacyFramebufferRect intersect(
            final LegacyFramebufferRect other) {
        if (other == null) {
            throw new NullPointerException("other");
        }

        final int left =
                Math.max(x, other.x);
        final int bottom =
                Math.max(y, other.y);
        final int right =
                Math.min(
                        x + width,
                        other.x + other.width);
        final int top =
                Math.min(
                        y + height,
                        other.y + other.height);

        return new LegacyFramebufferRect(
                left,
                bottom,
                Math.max(0, right - left),
                Math.max(0, top - bottom));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LegacyFramebufferRect)) {
            return false;
        }
        final LegacyFramebufferRect rect =
                (LegacyFramebufferRect) other;
        return x == rect.x
                && y == rect.y
                && width == rect.width
                && height == rect.height;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        return "LegacyFramebufferRect{"
                + "x=" + x
                + ", y=" + y
                + ", width=" + width
                + ", height=" + height
                + '}';
    }
}
