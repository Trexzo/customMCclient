package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189PlayerBoxPixelProbeTest {
    @Test void convertsNativeTopLeftUiStrokeToActualGlBottomLeftPixel() {
        assertArrayEquals(new int[]{420, 359},
                Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                        400, 100, 40, 80, 2, 1, 854, 480));
        assertArrayEquals(new int[]{440, 379},
                Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                        200, 50, 40, 80, 2, 2, 854, 480));
        assertNull(Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                Float.NaN, 30, 50, 70, 2, 1, 854, 480));
        assertNull(Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                1000, 30, 50, 70, 2, 1, 854, 480));
        assertNull(Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                30, 30, 1, 70, 2, 1, 854, 480));
    }

    @Test void matchesOpaqueCyanAndRejectsBackgroundOrUnderlay() {
        assertTrue(Minecraft189PlayerBoxPixelProbe.isForegroundColor(112, 217, 255));
        assertTrue(Minecraft189PlayerBoxPixelProbe.isForegroundColor(110, 220, 255));
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(16, 20, 34));
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(255, 255, 255));
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(0, 200, 255));
    }
}
