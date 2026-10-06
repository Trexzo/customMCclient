package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyFramebufferRect;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyScissorStateController;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyScissorStateSink;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyScissorStateControllerTest {
    @Test
    void redundantNestedClipDoesNotReapplyEquivalentFramebufferState() {
        final RecordingSink sink =
                new RecordingSink();
        final LegacyScissorStateController controller =
                new LegacyScissorStateController(
                        new UiViewport(
                                1000,
                                800,
                                1.0F),
                        sink);

        final LegacyFramebufferRect parent =
                controller.push(
                        new UiBounds(
                                100.0F,
                                100.0F,
                                400.0F,
                                300.0F));

        assertEquals(
                1,
                sink.applied.size());
        assertEquals(
                parent,
                controller.applied());

        final LegacyFramebufferRect redundantChild =
                controller.push(
                        new UiBounds(
                                0.0F,
                                0.0F,
                                900.0F,
                                700.0F));

        assertEquals(
                parent,
                redundantChild);
        assertEquals(
                1,
                sink.applied.size());
        assertEquals(
                2,
                controller.depth());

        assertEquals(
                parent,
                controller.pop());
        assertEquals(
                1,
                sink.applied.size());
        assertEquals(
                1,
                controller.depth());

        assertNull(controller.pop());
        assertEquals(
                1,
                sink.disableCalls);
        assertFalse(controller.enabled());
    }

    @Test
    void changedChildAndParentRestoreEachEmitExactlyOneStateUpdate() {
        final RecordingSink sink =
                new RecordingSink();
        final LegacyScissorStateController controller =
                new LegacyScissorStateController(
                        new UiViewport(
                                1000,
                                800,
                                1.0F),
                        sink);

        final LegacyFramebufferRect parent =
                controller.push(
                        new UiBounds(
                                100.0F,
                                100.0F,
                                400.0F,
                                300.0F));
        final LegacyFramebufferRect child =
                controller.push(
                        new UiBounds(
                                300.0F,
                                250.0F,
                                400.0F,
                                300.0F));

        assertEquals(
                2,
                sink.applied.size());
        assertEquals(
                child,
                sink.applied.get(1));

        assertEquals(
                parent,
                controller.pop());
        assertEquals(
                3,
                sink.applied.size());
        assertEquals(
                parent,
                sink.applied.get(2));

        assertNull(controller.pop());
        assertEquals(
                1,
                sink.disableCalls);
    }

    @Test
    void resetDropsNestedStateWithOneDisableAndNoIntermediateReapply() {
        final RecordingSink sink =
                new RecordingSink();
        final LegacyScissorStateController controller =
                new LegacyScissorStateController(
                        new UiViewport(
                                1000,
                                800,
                                1.0F),
                        sink);

        controller.push(
                new UiBounds(
                        100.0F,
                        100.0F,
                        500.0F,
                        400.0F));
        controller.push(
                new UiBounds(
                        150.0F,
                        150.0F,
                        300.0F,
                        200.0F));
        controller.push(
                new UiBounds(
                        200.0F,
                        175.0F,
                        100.0F,
                        100.0F));

        assertEquals(
                3,
                sink.applied.size());

        controller.reset();

        assertEquals(
                3,
                sink.applied.size());
        assertEquals(
                1,
                sink.disableCalls);
        assertEquals(
                0,
                controller.depth());
        assertNull(controller.current());
        assertNull(controller.applied());
        assertFalse(controller.enabled());

        controller.reset();
        assertEquals(
                1,
                sink.disableCalls);
    }

    @Test
    void stackUnderflowStillFailsWithoutTouchingHostState() {
        final RecordingSink sink =
                new RecordingSink();
        final LegacyScissorStateController controller =
                new LegacyScissorStateController(
                        new UiViewport(
                                640,
                                480,
                                1.0F),
                        sink);

        assertThrows(
                IllegalStateException.class,
                controller::pop);

        assertTrue(sink.applied.isEmpty());
        assertEquals(
                0,
                sink.disableCalls);
    }

    private static final class RecordingSink
            implements LegacyScissorStateSink {
        private final List<LegacyFramebufferRect> applied =
                new ArrayList<LegacyFramebufferRect>();
        private int disableCalls;

        @Override
        public void apply(
                final LegacyFramebufferRect rect) {
            applied.add(rect);
        }

        @Override
        public void disable() {
            disableCalls++;
        }
    }
}
