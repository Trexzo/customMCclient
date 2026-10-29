package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ArrayListLayoutTest {
    @Test
    void ordinary1280x720PositionAndSettingsRemainPixelCompatible() {
        assertEquals(24.0F, Minecraft189ArrayListLayout.safeTop(24, 720), 0.001F);
        assertEquals(40.0F, Minecraft189ArrayListLayout.safeLeft(40, 1280), 0.001F);
        assertEquals(1240.0F,
                Minecraft189ArrayListLayout.safeRightEdge(40, 1280), 0.001F);
        assertTrue(Minecraft189ArrayListLayout.rowCapacity(720, 24) >= 5);
        assertEquals(5, Minecraft189ArrayListLayout.visibleRows(
                5, 128, 50, false));
        assertEquals(2, Minecraft189ArrayListLayout.visibleRows(
                5, 2, 50, true));
    }

    @Test
    void veryDenseActual854x480HudNeverRendersBelowBottomEdge() {
        final float top = Minecraft189ArrayListLayout.safeTop(24, 480);
        final int cap = Minecraft189ArrayListLayout.rowCapacity(480, top);
        assertTrue(cap > 0);
        assertEquals(cap, Minecraft189ArrayListLayout.visibleRows(
                85, 128, cap, false));
        final int rows = Minecraft189ArrayListLayout.visibleRows(
                85, 128, cap, true);
        assertEquals(cap - 1, rows);
        assertTrue(top + (rows + 1) * 12.0F <= 480 - 4.0F);
        assertEquals(0, Minecraft189ArrayListLayout.visibleRows(
                85, 128, 0, true));
        assertEquals(0, Minecraft189ArrayListLayout.rowCapacity(12, 10));
    }

    @Test
    void savedOffscreenPositionsRecoverWithoutOverwritingUserPreferences() {
        assertEquals(464.0F,
                Minecraft189ArrayListLayout.safeTop(4096, 480), 0.001F);
        assertEquals(846.0F,
                Minecraft189ArrayListLayout.safeRightEdge(4096, 854), 0.001F);
        assertEquals(8.0F,
                Minecraft189ArrayListLayout.safeLeft(4096, 854), 0.001F);
        assertEquals(1,
                Minecraft189ArrayListLayout.rowCapacity(480, 464));
    }

    @Test
    void longOriginalLabelGetsBoundedOnlyInPresentation() {
        final String label = "[Combat] A very long module name with full settings";
        final String fitted = Minecraft189ArrayListLayout.fitLabel(label, 96);
        assertTrue(fitted.endsWith("..."));
        assertTrue(Minecraft189HudTextMetrics.width(fitted) <= 96);
        assertTrue(fitted.length() < label.length());
        assertEquals(label, Minecraft189ArrayListLayout.fitLabel(label, 640));
        assertEquals("", Minecraft189ArrayListLayout.fitLabel(label, 2));
        assertFalse(Minecraft189ArrayListLayout.fitLabel(label, 8).isEmpty());
        assertThrows(IllegalArgumentException.class, () ->
                Minecraft189ArrayListLayout.visibleRows(1, -1, 5, true));
    }
}
