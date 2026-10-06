package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyFramebufferRect;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyScissorStack;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiFramebufferMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyScissorMappingTest {
    @Test
    void logicalTopLeftBoundsMapConservativelyToBottomLeftFramebuffer() {
        final UiViewport viewport =
                new UiViewport(
                        1920,
                        1080,
                        2.0F);
        final LegacyFramebufferRect rect =
                new LegacyUiFramebufferMapper()
                        .scissor(
                                viewport,
                                new UiBounds(
                                        10.25F,
                                        20.25F,
                                        100.5F,
                                        50.5F));

        assertEquals(
                new LegacyFramebufferRect(
                        20,
                        938,
                        202,
                        102),
                rect);
    }

    @Test
    void mappingClampsPartialAndFullyOffscreenBounds() {
        final UiViewport viewport =
                new UiViewport(
                        1920,
                        1080,
                        2.0F);
        final LegacyUiFramebufferMapper mapper =
                new LegacyUiFramebufferMapper();

        assertEquals(
                new LegacyFramebufferRect(
                        0,
                        1040,
                        40,
                        40),
                mapper.scissor(
                        viewport,
                        new UiBounds(
                                -10.0F,
                                -20.0F,
                                30.0F,
                                40.0F)));

        assertEquals(
                new LegacyFramebufferRect(
                        1920,
                        980,
                        0,
                        20),
                mapper.scissor(
                        viewport,
                        new UiBounds(
                                1000.0F,
                                40.0F,
                                20.0F,
                                10.0F)));

        assertEquals(
                new LegacyFramebufferRect(
                        20,
                        0,
                        20,
                        0),
                mapper.scissor(
                        viewport,
                        new UiBounds(
                                10.0F,
                                600.0F,
                                10.0F,
                                10.0F)));
    }

    @Test
    void nestedStackIntersectsAndPopRestoresParent() {
        final LegacyScissorStack stack =
                new LegacyScissorStack(
                        new UiViewport(
                                1000,
                                800,
                                1.0F));

        final LegacyFramebufferRect parent =
                stack.push(
                        new UiBounds(
                                100.0F,
                                100.0F,
                                400.0F,
                                300.0F));
        assertEquals(
                new LegacyFramebufferRect(
                        100,
                        400,
                        400,
                        300),
                parent);

        final LegacyFramebufferRect child =
                stack.push(
                        new UiBounds(
                                300.0F,
                                250.0F,
                                400.0F,
                                300.0F));
        assertEquals(
                new LegacyFramebufferRect(
                        300,
                        400,
                        200,
                        150),
                child);
        assertEquals(
                2,
                stack.depth());
        assertEquals(
                child,
                stack.current());

        assertEquals(
                parent,
                stack.pop());
        assertEquals(
                1,
                stack.depth());

        assertNull(stack.pop());
        assertTrue(stack.empty());
        assertNull(stack.current());

        assertThrows(
                IllegalStateException.class,
                stack::pop);
    }

    @Test
    void framebufferRectIntersectionCanCollapseToEmpty() {
        final LegacyFramebufferRect left =
                new LegacyFramebufferRect(
                        10,
                        20,
                        30,
                        40);
        final LegacyFramebufferRect right =
                new LegacyFramebufferRect(
                        100,
                        200,
                        20,
                        30);

        assertEquals(
                new LegacyFramebufferRect(
                        100,
                        200,
                        0,
                        0),
                left.intersect(right));
    }
}
