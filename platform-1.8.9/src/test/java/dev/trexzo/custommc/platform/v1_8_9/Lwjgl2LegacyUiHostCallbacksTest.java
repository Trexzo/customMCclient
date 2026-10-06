package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyFramebufferRect;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyGlApi;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiGeometryFactory;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiPrimitiveMode;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiTextRenderer;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiViewportSource;
import dev.trexzo.custommc.platform.v1_8_9.ui.Lwjgl2LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Lwjgl2LegacyUiHostCallbacksTest {
    @Test
    void batchedShapesUseCertifiedGeometryAndSharedScissorState() {
        final RecordingGlApi gl =
                new RecordingGlApi();
        final RecordingTextRenderer text =
                new RecordingTextRenderer();
        final Lwjgl2LegacyUiHostCallbacks host =
                new Lwjgl2LegacyUiHostCallbacks(
                        new FixedViewportSource(
                                400,
                                300,
                                2.0F),
                        text,
                        gl,
                        new LegacyUiGeometryFactory(1));
        final UiViewport viewport =
                new UiViewport(
                        400,
                        300,
                        2.0F);

        host.beginUi(viewport);
        host.beginShapeBatch();
        host.fillRect(
                1.0F,
                2.0F,
                3.0F,
                4.0F,
                0xFF112233);
        host.fillRoundedRect(
                10.0F,
                20.0F,
                20.0F,
                10.0F,
                2.0F,
                0x80445566);
        host.endShapeBatch();

        host.pushClip(
                5.0F,
                6.0F,
                7.0F,
                8.0F);
        host.strokeRect(
                2.0F,
                3.0F,
                4.0F,
                5.0F,
                1.5F,
                0xFFFFFFFF);
        host.popClip();

        host.drawText(
                UiFonts.DEFAULT,
                9.0F,
                10.0F,
                "hello",
                0xFFAABBCC);
        host.endUi();

        assertFalse(host.frameOpen());
        assertFalse(host.shapeBatchOpen());
        assertEquals(
                1,
                gl.beginUiCalls);
        assertEquals(
                1,
                gl.endUiCalls);
        assertEquals(
                2,
                gl.prepareShapeCalls);
        assertEquals(
                1,
                gl.prepareTextCalls);
        assertEquals(
                Arrays.asList(
                        LegacyUiPrimitiveMode.QUADS,
                        LegacyUiPrimitiveMode.TRIANGLE_FAN,
                        LegacyUiPrimitiveMode.LINE_LOOP),
                gl.primitives);
        assertEquals(
                Arrays.asList(
                        Integer.valueOf(4),
                        Integer.valueOf(10),
                        Integer.valueOf(4)),
                gl.vertexCounts);
        assertEquals(
                Arrays.asList(
                        Integer.valueOf(0xFF112233),
                        Integer.valueOf(0x80445566),
                        Integer.valueOf(0xFFFFFFFF)),
                gl.colors);
        assertEquals(
                Arrays.asList(
                        Float.valueOf(1.5F)),
                gl.lineWidths);
        assertEquals(
                Arrays.asList(
                        new LegacyFramebufferRect(
                                10,
                                272,
                                14,
                                16)),
                gl.scissors);
        assertEquals(
                1,
                gl.disableScissorCalls);
        assertEquals(
                Arrays.asList(
                        "minecraft-default:9.0,10.0:hello:-5588020"),
                text.calls);
    }

    @Test
    void failedGeometrySubmissionStillEndsPrimitiveAndAllowsFrameCleanup() {
        final RecordingGlApi gl =
                new RecordingGlApi();
        gl.failVertexAt = 2;

        final Lwjgl2LegacyUiHostCallbacks host =
                new Lwjgl2LegacyUiHostCallbacks(
                        new FixedViewportSource(
                                800,
                                600,
                                1.0F),
                        new RecordingTextRenderer(),
                        gl,
                        new LegacyUiGeometryFactory());

        host.beginUi(
                new UiViewport(
                        800,
                        600,
                        1.0F));

        assertThrows(
                IllegalStateException.class,
                () -> host.fillRect(
                        1.0F,
                        2.0F,
                        3.0F,
                        4.0F,
                        0xFFFFFFFF));

        assertEquals(
                1,
                gl.endPrimitiveCalls);

        host.endUi();

        assertEquals(
                1,
                gl.endUiCalls);
        assertFalse(host.frameOpen());
    }

    @Test
    void endUiResetsScissorAndReportsLeakedBatchAfterCleanup() {
        final RecordingGlApi gl =
                new RecordingGlApi();
        final Lwjgl2LegacyUiHostCallbacks host =
                new Lwjgl2LegacyUiHostCallbacks(
                        new FixedViewportSource(
                                800,
                                600,
                                1.0F),
                        new RecordingTextRenderer(),
                        gl,
                        new LegacyUiGeometryFactory());

        host.beginUi(
                new UiViewport(
                        800,
                        600,
                        1.0F));
        host.pushClip(
                10.0F,
                20.0F,
                30.0F,
                40.0F);
        host.beginShapeBatch();

        assertThrows(
                IllegalStateException.class,
                host::endUi);

        assertEquals(
                1,
                gl.disableScissorCalls);
        assertEquals(
                1,
                gl.endUiCalls);
        assertFalse(host.frameOpen());
        assertFalse(host.shapeBatchOpen());
    }

    @Test
    void hostViewportDelegatesToInjectedLiveSource() {
        final MutableViewportSource source =
                new MutableViewportSource(
                        1200,
                        800,
                        1.0F);
        final Lwjgl2LegacyUiHostCallbacks host =
                new Lwjgl2LegacyUiHostCallbacks(
                        source,
                        new RecordingTextRenderer(),
                        new RecordingGlApi(),
                        new LegacyUiGeometryFactory());

        assertEquals(
                1200,
                host.framebufferWidth());
        assertEquals(
                800,
                host.framebufferHeight());
        assertEquals(
                1.0F,
                host.uiScale());

        source.width = 1600;
        source.height = 900;
        source.scale = 2.0F;

        assertEquals(
                1600,
                host.framebufferWidth());
        assertEquals(
                900,
                host.framebufferHeight());
        assertEquals(
                2.0F,
                host.uiScale());
    }

    private static final class FixedViewportSource
            implements LegacyUiViewportSource {
        private final int width;
        private final int height;
        private final float scale;

        FixedViewportSource(
                final int width,
                final int height,
                final float scale) {
            this.width = width;
            this.height = height;
            this.scale = scale;
        }

        @Override
        public int framebufferWidth() {
            return width;
        }

        @Override
        public int framebufferHeight() {
            return height;
        }

        @Override
        public float uiScale() {
            return scale;
        }
    }

    private static final class MutableViewportSource
            implements LegacyUiViewportSource {
        private int width;
        private int height;
        private float scale;

        MutableViewportSource(
                final int width,
                final int height,
                final float scale) {
            this.width = width;
            this.height = height;
            this.scale = scale;
        }

        @Override
        public int framebufferWidth() {
            return width;
        }

        @Override
        public int framebufferHeight() {
            return height;
        }

        @Override
        public float uiScale() {
            return scale;
        }
    }

    private static final class RecordingTextRenderer
            implements LegacyUiTextRenderer {
        private final List<String> calls =
                new ArrayList<String>();

        @Override
        public void drawText(
                final dev.trexzo.custommc.core.ui.UiFontHandle font,
                final float x,
                final float y,
                final String text,
                final int argb) {
            calls.add(
                    font.id()
                            + ":"
                            + x
                            + ","
                            + y
                            + ":"
                            + text
                            + ":"
                            + argb);
        }
    }

    private static final class RecordingGlApi
            implements LegacyGlApi {
        private int beginUiCalls;
        private int endUiCalls;
        private int prepareShapeCalls;
        private int prepareTextCalls;
        private int endPrimitiveCalls;
        private int disableScissorCalls;
        private int failVertexAt = -1;
        private int currentVertexCount;
        private int totalVertexCalls;

        private final List<LegacyUiPrimitiveMode> primitives =
                new ArrayList<LegacyUiPrimitiveMode>();
        private final List<Integer> vertexCounts =
                new ArrayList<Integer>();
        private final List<Integer> colors =
                new ArrayList<Integer>();
        private final List<Float> lineWidths =
                new ArrayList<Float>();
        private final List<LegacyFramebufferRect> scissors =
                new ArrayList<LegacyFramebufferRect>();

        @Override
        public void beginUi(
                final UiViewport viewport) {
            beginUiCalls++;
        }

        @Override
        public void endUi() {
            endUiCalls++;
        }

        @Override
        public void prepareShapeState() {
            prepareShapeCalls++;
        }

        @Override
        public void prepareTextState() {
            prepareTextCalls++;
        }

        @Override
        public void setColorArgb(
                final int argb) {
            colors.add(
                    Integer.valueOf(argb));
        }

        @Override
        public void setLineWidth(
                final float width) {
            lineWidths.add(
                    Float.valueOf(width));
        }

        @Override
        public void beginPrimitive(
                final LegacyUiPrimitiveMode primitive) {
            primitives.add(primitive);
            currentVertexCount = 0;
        }

        @Override
        public void vertex(
                final float x,
                final float y) {
            totalVertexCalls++;
            currentVertexCount++;
            if (totalVertexCalls == failVertexAt) {
                throw new IllegalStateException(
                        "vertex failure");
            }
        }

        @Override
        public void endPrimitive() {
            endPrimitiveCalls++;
            vertexCounts.add(
                    Integer.valueOf(
                            currentVertexCount));
        }

        @Override
        public void applyScissor(
                final LegacyFramebufferRect rect) {
            scissors.add(rect);
        }

        @Override
        public void disableScissor() {
            disableScissorCalls++;
        }
    }
}
