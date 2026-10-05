package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiOutlineCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiGraphics;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189UiRenderer;
import dev.trexzo.custommc.platform.v1_8_9.ui.UnsupportedUiCommandException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189UiRendererTest {
    @Test
    void supportedCommandsTranslateInCommandOrder() {
        final List<String> calls =
                new ArrayList<String>();
        final Minecraft189UiRenderer renderer =
                new Minecraft189UiRenderer(
                        graphics(calls));

        renderer.render(
                new RenderFrame(3L, 0.5F),
                new UiViewport(800, 600, 2.0F),
                Arrays.<UiDrawCommand>asList(
                        new UiRectCommand(
                                0,
                                1.0F,
                                2.0F,
                                3.0F,
                                4.0F,
                                0xFF010203),
                        new UiRoundedRectCommand(
                                1,
                                10.0F,
                                11.0F,
                                40.0F,
                                20.0F,
                                5.0F,
                                0xFFFFFFFF),
                        new UiOutlineCommand(
                                2,
                                12.0F,
                                13.0F,
                                50.0F,
                                30.0F,
                                1.5F,
                                0xFF000000),
                        new UiTextCommand(
                                3,
                                5.0F,
                                6.0F,
                                "hello",
                                0xFFAABBCC)));

        assertEquals(
                Arrays.asList(
                        "begin:400.0x300.0",
                        "rect:1.0,2.0,3.0,4.0,-16711165",
                        "rounded:10.0,11.0,40.0,20.0,5.0,-1",
                        "outline:12.0,13.0,50.0,30.0,1.5,-16777216",
                        "text:5.0,6.0,hello,-5588020",
                        "end"),
                calls);
    }

    @Test
    void endRunsWhenCommandTranslationFails() {
        final List<String> calls =
                new ArrayList<String>();
        final Minecraft189UiRenderer renderer =
                new Minecraft189UiRenderer(
                        graphics(calls));

        final UiDrawCommand unsupported =
                new UiDrawCommand() {
                    @Override
                    public int layer() {
                        return 0;
                    }
                };

        assertThrows(
                UnsupportedUiCommandException.class,
                () -> renderer.render(
                        new RenderFrame(0L, 0.0F),
                        new UiViewport(800, 600, 1.0F),
                        Arrays.asList(unsupported)));

        assertEquals(
                Arrays.asList(
                        "begin:800.0x600.0",
                        "end"),
                calls);
    }

    private static LegacyUiGraphics graphics(
            final List<String> calls) {
        return new LegacyUiGraphics() {
            @Override
            public void begin(
                    final UiViewport viewport) {
                calls.add(
                        "begin:"
                                + viewport.logicalWidth()
                                + "x"
                                + viewport.logicalHeight());
            }

            @Override
            public void fillRect(
                    final float x,
                    final float y,
                    final float width,
                    final float height,
                    final int argb) {
                calls.add(
                        "rect:"
                                + x
                                + ","
                                + y
                                + ","
                                + width
                                + ","
                                + height
                                + ","
                                + argb);
            }

            @Override
            public void fillRoundedRect(
                    final float x,
                    final float y,
                    final float width,
                    final float height,
                    final float radius,
                    final int argb) {
                calls.add(
                        "rounded:"
                                + x
                                + ","
                                + y
                                + ","
                                + width
                                + ","
                                + height
                                + ","
                                + radius
                                + ","
                                + argb);
            }

            @Override
            public void strokeRect(
                    final float x,
                    final float y,
                    final float width,
                    final float height,
                    final float thickness,
                    final int argb) {
                calls.add(
                        "outline:"
                                + x
                                + ","
                                + y
                                + ","
                                + width
                                + ","
                                + height
                                + ","
                                + thickness
                                + ","
                                + argb);
            }

            @Override
            public void drawText(
                    final float x,
                    final float y,
                    final String text,
                    final int argb) {
                calls.add(
                        "text:"
                                + x
                                + ","
                                + y
                                + ","
                                + text
                                + ","
                                + argb);
            }

            @Override
            public void end() {
                calls.add("end");
            }
        };
    }
}
