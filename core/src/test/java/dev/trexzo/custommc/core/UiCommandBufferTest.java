package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiCommandBuffer;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiCommandBufferTest {
    @Test
    void sealOrdersByLayerAndPreservesInsertionWithinLayer() {
        final UiCommandBuffer buffer = new UiCommandBuffer();

        buffer.add(new UiTextCommand(
                10, 0.0F, 0.0F, "late-a", 0xFFFFFFFF));
        buffer.add(new UiRectCommand(
                0, 0.0F, 0.0F, 10.0F, 10.0F, 0xFF000000));
        buffer.add(new UiTextCommand(
                10, 0.0F, 0.0F, "late-b", 0xFFFFFFFF));

        final List<UiDrawCommand> commands = buffer.seal();

        assertTrue(commands.get(0) instanceof UiRectCommand);
        assertEquals(
                "late-a",
                ((UiTextCommand) commands.get(1)).text());
        assertEquals(
                "late-b",
                ((UiTextCommand) commands.get(2)).text());
    }

    @Test
    void sealIsIdempotentAndFreezesTheBuffer() {
        final UiCommandBuffer buffer = new UiCommandBuffer();
        buffer.add(new UiRectCommand(
                0, 1.0F, 2.0F, 3.0F, 4.0F, 0xFFFFFFFF));

        final List<UiDrawCommand> first = buffer.seal();
        final List<UiDrawCommand> second = buffer.seal();

        assertSame(first, second);
        assertTrue(buffer.sealed());
        assertThrows(
                IllegalStateException.class,
                () -> buffer.add(new UiTextCommand(
                        0, 0.0F, 0.0F, "too-late", 0xFFFFFFFF)));
    }

    @Test
    void geometryRejectsNonFiniteAndNegativeExtent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiRectCommand(
                        0,
                        Float.NaN,
                        0.0F,
                        1.0F,
                        1.0F,
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiRectCommand(
                        0,
                        0.0F,
                        0.0F,
                        -1.0F,
                        1.0F,
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiTextCommand(
                        0,
                        Float.POSITIVE_INFINITY,
                        0.0F,
                        "bad",
                        0));
    }
}
