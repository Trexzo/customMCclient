package dev.trexzo.custommc.platform.v1_8_9.ui;

import java.util.Arrays;
import java.util.Objects;

public final class LegacyUiGeometry {
    private final LegacyUiPrimitiveMode primitive;
    private final float[] coordinates;

    public LegacyUiGeometry(
            final LegacyUiPrimitiveMode primitive,
            final float[] coordinates) {
        this.primitive = Objects.requireNonNull(
                primitive,
                "primitive");
        Objects.requireNonNull(
                coordinates,
                "coordinates");

        if (coordinates.length == 0
                || coordinates.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "coordinates must contain one or more XY pairs");
        }

        validateVertexCount(
                this.primitive,
                coordinates.length / 2);

        this.coordinates =
                Arrays.copyOf(
                        coordinates,
                        coordinates.length);
        for (float coordinate : this.coordinates) {
            if (Float.isNaN(coordinate)
                    || Float.isInfinite(coordinate)) {
                throw new IllegalArgumentException(
                        "coordinates must be finite");
            }
        }
    }

    public LegacyUiPrimitiveMode primitive() {
        return primitive;
    }

    public int vertexCount() {
        return coordinates.length / 2;
    }

    public float x(final int vertexIndex) {
        return coordinates[index(vertexIndex)];
    }

    public float y(final int vertexIndex) {
        return coordinates[index(vertexIndex) + 1];
    }

    public float[] coordinates() {
        return Arrays.copyOf(
                coordinates,
                coordinates.length);
    }

    private static void validateVertexCount(
            final LegacyUiPrimitiveMode primitive,
            final int vertexCount) {
        switch (primitive) {
            case QUADS:
                if (vertexCount % 4 != 0) {
                    throw new IllegalArgumentException(
                            "QUADS requires a multiple of four vertices");
                }
                return;
            case TRIANGLE_FAN:
                if (vertexCount < 3) {
                    throw new IllegalArgumentException(
                            "TRIANGLE_FAN requires at least three vertices");
                }
                return;
            case LINE_LOOP:
                if (vertexCount < 3) {
                    throw new IllegalArgumentException(
                            "LINE_LOOP requires at least three vertices");
                }
                return;
            default:
                throw new IllegalArgumentException(
                        "unsupported primitive: " + primitive);
        }
    }

    private int index(final int vertexIndex) {
        if (vertexIndex < 0
                || vertexIndex >= vertexCount()) {
            throw new IndexOutOfBoundsException(
                    "vertexIndex=" + vertexIndex);
        }
        return vertexIndex * 2;
    }
}
