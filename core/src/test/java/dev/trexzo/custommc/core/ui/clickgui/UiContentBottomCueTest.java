package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiContentBottomCueTest {
    private static final UiBounds CONTENT =
            new UiBounds(215.0F, 33.0F, 580.0F, 376.0F);

    @Test
    void maskAppearsOnlyWhileMoreRowsRemainBelowViewport() {
        assertTrue(UiContentBottomCue.compose(
                CONTENT, CONTENT.height(), 0, UiThemes.graphite()).isEmpty());
        assertTrue(UiContentBottomCue.compose(
                CONTENT, 500.0F, 124.0F, UiThemes.graphite()).isEmpty());
        assertTrue(UiContentBottomCue.compose(
                CONTENT, 500.0F, 500.0F, UiThemes.graphite()).isEmpty());
        assertTrue(UiContentBottomCue.compose(
                new UiBounds(1, 1, 60, 200), 1000, 0,
                UiThemes.graphite()).isEmpty());

        final List<UiDrawCommand> visible =
                UiContentBottomCue.compose(CONTENT, 1000, 0,
                        UiThemes.graphite());
        assertEquals(3, visible.size());
        final UiRectCommand mask = (UiRectCommand) visible.get(0);
        final UiRectCommand border = (UiRectCommand) visible.get(1);
        final UiTextCommand hint = (UiTextCommand) visible.get(2);
        assertEquals(CONTENT.x(), mask.x(), 0.001F);
        assertEquals(CONTENT.y() + CONTENT.height() - UiContentBottomCue.HEIGHT,
                mask.y(), 0.001F);
        assertEquals(CONTENT.width(), mask.width(), 0.001F);
        assertEquals(UiContentBottomCue.HEIGHT, mask.height(), 0.001F);
        assertEquals(mask.y(), border.y(), 0.001F);
        assertEquals("SCROLL FOR MORE", hint.text());
        assertEquals(UiThemes.graphite().color(UiColorRole.TEXT_MUTED),
                hint.argb());
    }

    @Test
    void footerPointerVetoEqualsItsRenderedGeometry() {
        final float height = 1000.0F;
        final float footerTop = CONTENT.y() + CONTENT.height()
                - UiContentBottomCue.HEIGHT;
        assertFalse(UiContentBottomCue.blocksPointer(
                CONTENT, height, 0, CONTENT.x() + 15, footerTop - 1));
        assertTrue(UiContentBottomCue.blocksPointer(
                CONTENT, height, 0, CONTENT.x() + 15, footerTop));
        assertTrue(UiContentBottomCue.blocksPointer(
                CONTENT, height, 28, CONTENT.x() + 15, footerTop + 5));
        assertFalse(UiContentBottomCue.blocksPointer(
                CONTENT, height, 28, CONTENT.x() - 1, footerTop + 5));
        assertFalse(UiContentBottomCue.blocksPointer(
                CONTENT, height, 28, CONTENT.x() + CONTENT.width(), footerTop + 5));
        assertFalse(UiContentBottomCue.blocksPointer(
                CONTENT, height, height - CONTENT.height(),
                CONTENT.x() + 15, footerTop + 5));
    }

    @Test
    void rejectsInvalidMeasurementBeforeDrawingOrPointerInput() {
        assertThrows(IllegalArgumentException.class, () ->
                UiContentBottomCue.compose(CONTENT, Float.NaN, 0, UiThemes.graphite()));
        assertThrows(IllegalArgumentException.class, () ->
                UiContentBottomCue.compose(CONTENT, 500, -2, UiThemes.graphite()));
        assertThrows(IllegalArgumentException.class, () ->
                UiContentBottomCue.blocksPointer(CONTENT, 800,
                        Float.POSITIVE_INFINITY, 300, 400));
    }
}
