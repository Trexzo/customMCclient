package dev.trexzo.custommc.platform.v1_8_9.ui;

public final class LegacyUiGeometryFactory {
    public static final int DEFAULT_CORNER_SEGMENTS = 8;

    private final int cornerSegments;

    public LegacyUiGeometryFactory() {
        this(DEFAULT_CORNER_SEGMENTS);
    }

    public LegacyUiGeometryFactory(
            final int cornerSegments) {
        if (cornerSegments < 1) {
            throw new IllegalArgumentException(
                    "cornerSegments must be positive");
        }
        this.cornerSegments = cornerSegments;
    }

    public int cornerSegments() {
        return cornerSegments;
    }

    public LegacyUiGeometry rectangle(
            final float x,
            final float y,
            final float width,
            final float height) {
        validateRect(
                x,
                y,
                width,
                height);

        return new LegacyUiGeometry(
                LegacyUiPrimitiveMode.QUADS,
                new float[] {
                        x, y,
                        x + width, y,
                        x + width, y + height,
                        x, y + height
                });
    }

    public LegacyUiGeometry outlineRectangle(
            final float x,
            final float y,
            final float width,
            final float height) {
        validateRect(
                x,
                y,
                width,
                height);

        return new LegacyUiGeometry(
                LegacyUiPrimitiveMode.LINE_LOOP,
                new float[] {
                        x, y,
                        x + width, y,
                        x + width, y + height,
                        x, y + height
                });
    }

    public LegacyUiGeometry roundedRectangle(
            final float x,
            final float y,
            final float width,
            final float height,
            final float radius) {
        validateRect(
                x,
                y,
                width,
                height);
        requireExtent(
                radius,
                "radius");

        final float maximumRadius =
                Math.min(
                        width,
                        height) * 0.5F;
        if (radius > maximumRadius) {
            throw new IllegalArgumentException(
                    "radius exceeds half the shortest edge");
        }

        if (radius == 0.0F) {
            return rectangle(
                    x,
                    y,
                    width,
                    height);
        }

        final int perimeterVertices =
                4 * (cornerSegments + 1);
        final float[] coordinates =
                new float[
                        (1 + perimeterVertices + 1) * 2];

        int offset = 0;
        offset = put(
                coordinates,
                offset,
                x + width * 0.5F,
                y + height * 0.5F);

        final int firstPerimeterOffset = offset;

        offset = appendArc(
                coordinates,
                offset,
                x + width - radius,
                y + radius,
                radius,
                -Math.PI * 0.5,
                0.0);
        offset = appendArc(
                coordinates,
                offset,
                x + width - radius,
                y + height - radius,
                radius,
                0.0,
                Math.PI * 0.5);
        offset = appendArc(
                coordinates,
                offset,
                x + radius,
                y + height - radius,
                radius,
                Math.PI * 0.5,
                Math.PI);
        offset = appendArc(
                coordinates,
                offset,
                x + radius,
                y + radius,
                radius,
                Math.PI,
                Math.PI * 1.5);

        put(
                coordinates,
                offset,
                coordinates[firstPerimeterOffset],
                coordinates[firstPerimeterOffset + 1]);

        return new LegacyUiGeometry(
                LegacyUiPrimitiveMode.TRIANGLE_FAN,
                coordinates);
    }

    private int appendArc(
            final float[] coordinates,
            final int startOffset,
            final float centerX,
            final float centerY,
            final float radius,
            final double startAngle,
            final double endAngle) {
        int offset = startOffset;
        final double step =
                (endAngle - startAngle)
                        / cornerSegments;

        for (int index = 0;
             index <= cornerSegments;
             index++) {
            final double angle =
                    startAngle
                            + step * index;
            offset = put(
                    coordinates,
                    offset,
                    centerX
                            + (float) Math.cos(angle)
                            * radius,
                    centerY
                            + (float) Math.sin(angle)
                            * radius);
        }

        return offset;
    }

    private static int put(
            final float[] coordinates,
            final int offset,
            final float x,
            final float y) {
        coordinates[offset] = x;
        coordinates[offset + 1] = y;
        return offset + 2;
    }

    private static void validateRect(
            final float x,
            final float y,
            final float width,
            final float height) {
        requireFinite(
                x,
                "x");
        requireFinite(
                y,
                "y");
        requireExtent(
                width,
                "width");
        requireExtent(
                height,
                "height");
    }

    private static void requireExtent(
            final float value,
            final String name) {
        requireFinite(
                value,
                name);
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
