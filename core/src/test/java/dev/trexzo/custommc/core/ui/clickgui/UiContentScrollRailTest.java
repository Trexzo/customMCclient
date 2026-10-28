package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiContentScrollRailTest {
    private static final UiBounds CONTENT =
            new UiBounds(215.0F, 33.0F, 580.0F, 376.0F);

    @Test
    void scrollRailOnlyAppearsWhenThereIsOverflow() {
        assertTrue(UiContentScrollRail.compose(
                CONTENT, 375.0F, 0.0F, UiThemes.graphite()).isEmpty());
        assertTrue(UiContentScrollRail.compose(
                CONTENT, 376.0F, 0.0F, UiThemes.graphite()).isEmpty());
        assertTrue(UiContentScrollRail.compose(
                new UiBounds(215, 33, 13, 376),
                2000, 0, UiThemes.graphite()).isEmpty());
        assertTrue(UiContentScrollRail.compose(
                new UiBounds(215, 33, 580, 60),
                2000, 0, UiThemes.graphite()).isEmpty());
        assertEquals(2, UiContentScrollRail.compose(
                CONTENT, 1800.0F, 0.0F, UiThemes.graphite()).size());
    }

    @Test
    void thumbFollowsActualScrollOffsetAndStaysInsideViewport() {
        final float totalHeight = 1800.0F;
        final float maximum = totalHeight - CONTENT.height();
        final UiRoundedRectCommand first = thumb(0.0F, totalHeight);
        final UiRoundedRectCommand middle = thumb(maximum / 2.0F, totalHeight);
        final UiRoundedRectCommand last = thumb(maximum, totalHeight);
        final UiRoundedRectCommand over = thumb(maximum + 100.0F, totalHeight);
        final UiRoundedRectCommand track = track(0.0F, totalHeight);

        assertEquals(first.x(), middle.x(), 0.001F);
        assertEquals(first.width(), middle.width(), 0.001F);
        assertEquals(first.height(), middle.height(), 0.001F);
        assertTrue(first.y() < middle.y());
        assertTrue(middle.y() < last.y());
        assertEquals(last.y(), over.y(), 0.001F);
        assertEquals(track.y(), first.y(), 0.001F);
        assertEquals(track.y() + track.height(),
                last.y() + last.height(), 0.001F);
        assertTrue(track.x() > CONTENT.x() + CONTENT.width() - 18.0F);
        assertTrue(last.x() + last.width() <= CONTENT.x() + CONTENT.width());
        assertEquals(UiThemes.graphite().color(UiColorRole.TEXT_MUTED),
                last.argb());
        assertEquals(UiThemes.graphite().color(UiColorRole.BORDER),
                track.argb());
    }

    @Test
    void rejectsNonfiniteOrNegativeMeasurements() {
        assertThrows(IllegalArgumentException.class, () ->
                UiContentScrollRail.compose(CONTENT, Float.NaN,
                        0.0F, UiThemes.graphite()));
        assertThrows(IllegalArgumentException.class, () ->
                UiContentScrollRail.compose(CONTENT, -5.0F,
                        0.0F, UiThemes.graphite()));
        assertThrows(IllegalArgumentException.class, () ->
                UiContentScrollRail.compose(CONTENT, 1000.0F,
                        Float.POSITIVE_INFINITY, UiThemes.graphite()));
        assertThrows(IllegalArgumentException.class, () ->
                UiContentScrollRail.compose(CONTENT, 1000.0F,
                        -1.0F, UiThemes.graphite()));
    }

    private static UiRoundedRectCommand thumb(final float offset,
            final float totalHeight) {
        final List<UiDrawCommand> commands =
                UiContentScrollRail.compose(
                        CONTENT, totalHeight, offset, UiThemes.graphite());
        return (UiRoundedRectCommand) commands.get(1);
    }

    private static UiRoundedRectCommand track(final float offset,
            final float totalHeight) {
        final List<UiDrawCommand> commands =
                UiContentScrollRail.compose(
                        CONTENT, totalHeight, offset, UiThemes.graphite());
        return (UiRoundedRectCommand) commands.get(0);
    }
}
