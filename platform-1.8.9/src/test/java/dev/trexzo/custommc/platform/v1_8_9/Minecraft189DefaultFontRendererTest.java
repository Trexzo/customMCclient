package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189DefaultFontRenderer;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189FontRendererAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189DefaultFontRendererTest {
    @Test
    void defaultFontForwardsExactDrawArgumentsWithoutShadow() {
        final RecordingFontAccess access =
                new RecordingFontAccess();
        final Minecraft189DefaultFontRenderer renderer =
                new Minecraft189DefaultFontRenderer(
                        access);

        renderer.drawText(
                UiFonts.DEFAULT,
                12.5F,
                24.0F,
                "CustomMC",
                0xFFA1B2C3);

        assertEquals(
                "CustomMC",
                access.text);
        assertEquals(
                12.5F,
                access.x);
        assertEquals(
                24.0F,
                access.y);
        assertEquals(
                0xFFA1B2C3,
                access.argb);
        assertFalse(access.shadow);
        assertEquals(
                1,
                access.calls);
    }

    @Test
    void unknownSemanticFontIsRejectedBeforeTouchingMinecraftRenderer() {
        final RecordingFontAccess access =
                new RecordingFontAccess();
        final Minecraft189DefaultFontRenderer renderer =
                new Minecraft189DefaultFontRenderer(
                        access);

        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.drawText(
                        new UiFontHandle(
                                "custom-display"),
                        0.0F,
                        0.0F,
                        "ignored",
                        0xFFFFFFFF));

        assertEquals(
                0,
                access.calls);
    }

    private static final class RecordingFontAccess
            implements Minecraft189FontRendererAccess {
        private String text;
        private float x;
        private float y;
        private int argb;
        private boolean shadow;
        private int calls;

        @Override
        public int drawString(
                final String text,
                final float x,
                final float y,
                final int argb,
                final boolean shadow) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.argb = argb;
            this.shadow = shadow;
            calls++;
            return 0;
        }
    }
}
