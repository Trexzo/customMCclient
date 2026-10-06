package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiGeometry;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiGeometryFactory;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiPrimitiveMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LegacyUiGeometryFactoryTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void rectangleAndOutlineUseStableClockwiseVertices() {
        final LegacyUiGeometryFactory factory =
                new LegacyUiGeometryFactory();

        final LegacyUiGeometry fill =
                factory.rectangle(
                        10.0F,
                        20.0F,
                        30.0F,
                        40.0F);
        final LegacyUiGeometry outline =
                factory.outlineRectangle(
                        10.0F,
                        20.0F,
                        30.0F,
                        40.0F);

        assertEquals(
                LegacyUiPrimitiveMode.QUADS,
                fill.primitive());
        assertEquals(
                LegacyUiPrimitiveMode.LINE_LOOP,
                outline.primitive());

        assertVertices(
                fill,
                new float[] {
                        10.0F, 20.0F,
                        40.0F, 20.0F,
                        40.0F, 60.0F,
                        10.0F, 60.0F
                });
        assertVertices(
                outline,
                new float[] {
                        10.0F, 20.0F,
                        40.0F, 20.0F,
                        40.0F, 60.0F,
                        10.0F, 60.0F
                });
    }

    @Test
    void zeroRadiusRoundedRectangleFallsBackToQuadGeometry() {
        final LegacyUiGeometry geometry =
                new LegacyUiGeometryFactory()
                        .roundedRectangle(
                                1.0F,
                                2.0F,
                                8.0F,
                                6.0F,
                                0.0F);

        assertEquals(
                LegacyUiPrimitiveMode.QUADS,
                geometry.primitive());
        assertVertices(
                geometry,
                new float[] {
                        1.0F, 2.0F,
                        9.0F, 2.0F,
                        9.0F, 8.0F,
                        1.0F, 8.0F
                });
    }

    @Test
    void oneSegmentRoundedRectangleProducesClosedTriangleFan() {
        final LegacyUiGeometry geometry =
                new LegacyUiGeometryFactory(1)
                        .roundedRectangle(
                                10.0F,
                                20.0F,
                                100.0F,
                                50.0F,
                                10.0F);

        assertEquals(
                LegacyUiPrimitiveMode.TRIANGLE_FAN,
                geometry.primitive());
        assertEquals(
                10,
                geometry.vertexCount());

        assertVertices(
                geometry,
                new float[] {
                        60.0F, 45.0F,
                        100.0F, 20.0F,
                        110.0F, 30.0F,
                        110.0F, 60.0F,
                        100.0F, 70.0F,
                        20.0F, 70.0F,
                        10.0F, 60.0F,
                        10.0F, 30.0F,
                        20.0F, 20.0F,
                        100.0F, 20.0F
                });
    }

    @Test
    void defaultRoundedRectangleHasDeterministicVertexCount() {
        final LegacyUiGeometryFactory factory =
                new LegacyUiGeometryFactory();
        final LegacyUiGeometry geometry =
                factory.roundedRectangle(
                        0.0F,
                        0.0F,
                        100.0F,
                        40.0F,
                        8.0F);

        assertEquals(
                LegacyUiGeometryFactory.DEFAULT_CORNER_SEGMENTS,
                factory.cornerSegments());
        assertEquals(
                38,
                geometry.vertexCount());
        assertEquals(
                geometry.x(1),
                geometry.x(
                        geometry.vertexCount() - 1),
                EPSILON);
        assertEquals(
                geometry.y(1),
                geometry.y(
                        geometry.vertexCount() - 1),
                EPSILON);
    }

    @Test
    void geometryDefensivelyCopiesCoordinatesAndChecksVertexBounds() {
        final float[] source =
                new float[] {
                        1.0F, 2.0F,
                        3.0F, 4.0F
                };
        final LegacyUiGeometry geometry =
                new LegacyUiGeometry(
                        LegacyUiPrimitiveMode.LINE_LOOP,
                        source);

        source[0] = 99.0F;
        assertEquals(
                1.0F,
                geometry.x(0),
                EPSILON);

        final float[] snapshot =
                geometry.coordinates();
        snapshot[0] = 77.0F;
        assertEquals(
                1.0F,
                geometry.x(0),
                EPSILON);

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> geometry.x(-1));
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> geometry.y(2));
    }

    @Test
    void invalidGeometryInputsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LegacyUiGeometryFactory(0));

        final LegacyUiGeometryFactory factory =
                new LegacyUiGeometryFactory();

        assertThrows(
                IllegalArgumentException.class,
                () -> factory.rectangle(
                        0.0F,
                        0.0F,
                        -1.0F,
                        1.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.roundedRectangle(
                        0.0F,
                        0.0F,
                        20.0F,
                        10.0F,
                        6.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> new LegacyUiGeometry(
                        LegacyUiPrimitiveMode.QUADS,
                        new float[] {
                                1.0F
                        }));
        assertThrows(
                IllegalArgumentException.class,
                () -> new LegacyUiGeometry(
                        LegacyUiPrimitiveMode.QUADS,
                        new float[] {
                                Float.NaN,
                                1.0F
                        }));
    }

    private static void assertVertices(
            final LegacyUiGeometry geometry,
            final float[] expected) {
        assertEquals(
                expected.length / 2,
                geometry.vertexCount());
        for (int index = 0;
             index < geometry.vertexCount();
             index++) {
            assertEquals(
                    expected[index * 2],
                    geometry.x(index),
                    EPSILON);
            assertEquals(
                    expected[index * 2 + 1],
                    geometry.y(index),
                    EPSILON);
        }
    }
}
