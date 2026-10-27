package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189HudTextMetricsTest {
    @Test
    void namesProduceDifferentIntrinsicWidthsRatherThanFixedTiles() {
        assertEquals(48, Minecraft189HudTextMetrics.width("Watermark"));
        assertEquals(58, Minecraft189HudTextMetrics.width("No Hit Delay"));
        assertTrue(Minecraft189HudTextMetrics.width("No Hit Delay")
                > Minecraft189HudTextMetrics.width("Watermark"));
        assertEquals(0, Minecraft189HudTextMetrics.width(""));
    }

    @Test
    void legacyFormattingDoesNotOccupyVisualWidth() {
        assertEquals(Minecraft189HudTextMetrics.width("Watermark"),
                Minecraft189HudTextMetrics.width("\u00a7bWatermark"));
        assertEquals(4, Minecraft189HudTextMetrics.width(" "));
    }

    @Test
    void rightEdgeInsetRemainsFixedForDifferentLabelWidths() {
        final float screenWidth = 1280.0F;
        final float inset = 40.0F;
        final float shortX = Minecraft189ArrayListModule.rightAlignedTextX(
                screenWidth, inset, "Watermark");
        final float longX = Minecraft189ArrayListModule.rightAlignedTextX(
                screenWidth, inset, "No Hit Delay");
        assertEquals(1184.0F, shortX, .001F);
        assertEquals(1174.0F, longX, .001F);
        assertEquals(0.0F,
                (shortX + Minecraft189HudTextMetrics.width("Watermark"))
                - (longX + Minecraft189HudTextMetrics.width("No Hit Delay")),
                .001F);
    }
}
