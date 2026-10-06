package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiBatchHostCallbacks;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostBridge;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyUiHostBridgeTest {
    @Test
    void bridgeDelegatesViewportAndGuardedFrameOperations() {
        final RecordingCallbacks callbacks =
                new RecordingCallbacks();
        final LegacyUiHostBridge bridge =
                new LegacyUiHostBridge(
                        callbacks);
        final UiViewport viewport =
                new UiViewport(
                        1920,
                        1080,
                        2.0F);

        assertEquals(
                1920,
                bridge.framebufferWidth());
        assertEquals(
                1080,
                bridge.framebufferHeight());
        assertEquals(
                2.0F,
                bridge.uiScale());

        bridge.begin(viewport);
        bridge.fillRect(
                1.0F,
                2.0F,
                3.0F,
                4.0F,
                0xFF000000);
        bridge.pushClip(
                5.0F,
                6.0F,
                7.0F,
                8.0F);
        bridge.drawText(
                UiFonts.DEFAULT,
                9.0F,
                10.0F,
                "hello",
                0xFFFFFFFF);
        bridge.popClip();
        bridge.end();

        assertFalse(bridge.frameOpen());
        assertEquals(
                0,
                bridge.clipDepth());
        assertEquals(
                java.util.Arrays.asList(
                        "begin",
                        "fillRect",
                        "pushClip",
                        "drawText",
                        "popClip",
                        "end"),
                callbacks.events);
    }

    @Test
    void operationsRequireOneOpenFrameAndClipCannotUnderflow() {
        final LegacyUiHostBridge bridge =
                new LegacyUiHostBridge(
                        new RecordingCallbacks());
        final UiViewport viewport =
                new UiViewport(
                        1280,
                        720,
                        1.0F);

        assertThrows(
                IllegalStateException.class,
                () -> bridge.fillRect(
                        0.0F,
                        0.0F,
                        1.0F,
                        1.0F,
                        0));
        assertThrows(
                IllegalStateException.class,
                bridge::end);

        bridge.begin(viewport);

        assertThrows(
                IllegalStateException.class,
                () -> bridge.begin(viewport));
        assertThrows(
                IllegalStateException.class,
                bridge::popClip);

        bridge.end();
        assertFalse(bridge.frameOpen());
    }

    @Test
    void unbalancedEndUnwindsHostClipStateAndResetsBridge() {
        final RecordingCallbacks callbacks =
                new RecordingCallbacks();
        final LegacyUiHostBridge bridge =
                new LegacyUiHostBridge(
                        callbacks);
        final UiViewport viewport =
                new UiViewport(
                        1280,
                        720,
                        1.0F);

        bridge.begin(viewport);
        bridge.pushClip(
                0.0F,
                0.0F,
                100.0F,
                100.0F);
        bridge.pushClip(
                10.0F,
                10.0F,
                80.0F,
                80.0F);

        assertThrows(
                IllegalStateException.class,
                bridge::end);

        assertFalse(bridge.frameOpen());
        assertEquals(
                0,
                bridge.clipDepth());
        assertEquals(
                2,
                callbacks.popCount);
        assertTrue(
                callbacks.events.contains(
                        "end"));

        bridge.begin(viewport);
        bridge.end();

        assertFalse(bridge.frameOpen());
    }


    @Test
    void batchCapabilityIsExplicitAndLeakedBatchClosesBeforeFrameEnd() {
        final RecordingBatchCallbacks callbacks =
                new RecordingBatchCallbacks();
        final LegacyUiHostBridge bridge =
                new LegacyUiHostBridge(
                        callbacks);
        final UiViewport viewport =
                new UiViewport(
                        1280,
                        720,
                        1.0F);

        assertTrue(
                bridge.supportsShapeBatching());

        bridge.begin(viewport);
        bridge.beginShapeBatch();

        assertTrue(
                bridge.shapeBatchOpen());
        assertThrows(
                IllegalStateException.class,
                () -> bridge.pushClip(
                        0.0F,
                        0.0F,
                        10.0F,
                        10.0F));
        assertThrows(
                IllegalStateException.class,
                () -> bridge.drawText(
                        UiFonts.DEFAULT,
                        0.0F,
                        0.0F,
                        "blocked",
                        0));

        assertThrows(
                IllegalStateException.class,
                bridge::end);

        assertFalse(
                bridge.shapeBatchOpen());
        assertFalse(
                bridge.frameOpen());
        assertEquals(
                java.util.Arrays.asList(
                        "begin",
                        "batch-begin",
                        "batch-end",
                        "end"),
                callbacks.events);
    }

    @Test
    void nonBatchHostRejectsDirectBatchScope() {
        final LegacyUiHostBridge bridge =
                new LegacyUiHostBridge(
                        new RecordingCallbacks());
        bridge.begin(
                new UiViewport(
                        640,
                        480,
                        1.0F));

        assertFalse(
                bridge.supportsShapeBatching());
        assertThrows(
                IllegalStateException.class,
                bridge::beginShapeBatch);

        bridge.end();
    }


    private static final class RecordingBatchCallbacks
            implements LegacyUiBatchHostCallbacks {
        private final List<String> events =
                new ArrayList<String>();

        @Override
        public int framebufferWidth() {
            return 1280;
        }

        @Override
        public int framebufferHeight() {
            return 720;
        }

        @Override
        public float uiScale() {
            return 1.0F;
        }

        @Override
        public void beginUi(
                final UiViewport viewport) {
            events.add("begin");
        }

        @Override
        public void beginShapeBatch() {
            events.add("batch-begin");
        }

        @Override
        public void fillRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final int argb) {
            events.add("fillRect");
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
            events.add("fillRoundedRect");
        }

        @Override
        public void strokeRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float thickness,
                final int argb) {
            events.add("strokeRect");
        }

        @Override
        public void pushClip(
                final float x,
                final float y,
                final float width,
                final float height) {
            events.add("pushClip");
        }

        @Override
        public void popClip() {
            events.add("popClip");
        }

        @Override
        public void drawText(
                final UiFontHandle font,
                final float x,
                final float y,
                final String text,
                final int argb) {
            events.add("drawText");
        }

        @Override
        public void endShapeBatch() {
            events.add("batch-end");
        }

        @Override
        public void endUi() {
            events.add("end");
        }
    }

    private static final class RecordingCallbacks
            implements LegacyUiHostCallbacks {
        private final List<String> events =
                new ArrayList<String>();
        private int popCount;

        @Override
        public int framebufferWidth() {
            return 1920;
        }

        @Override
        public int framebufferHeight() {
            return 1080;
        }

        @Override
        public float uiScale() {
            return 2.0F;
        }

        @Override
        public void beginUi(
                final UiViewport viewport) {
            events.add("begin");
        }

        @Override
        public void fillRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final int argb) {
            events.add("fillRect");
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
            events.add("fillRoundedRect");
        }

        @Override
        public void strokeRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float thickness,
                final int argb) {
            events.add("strokeRect");
        }

        @Override
        public void pushClip(
                final float x,
                final float y,
                final float width,
                final float height) {
            events.add("pushClip");
        }

        @Override
        public void popClip() {
            events.add("popClip");
            popCount++;
        }

        @Override
        public void drawText(
                final UiFontHandle font,
                final float x,
                final float y,
                final String text,
                final int argb) {
            events.add("drawText");
        }

        @Override
        public void endUi() {
            events.add("end");
        }
    }
}
