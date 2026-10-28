package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Source-native compact ghost-client scrollbar for an already scrollable
 * ClickGUI content region. Draw-only: the existing scroll controllers own
 * offsets, wheel input, module state and settings edits.
 *
 * The rail is reserved in the right-hand content margin, outside row hit
 * targets, so it cannot act as a fake setting or module toggle.
 */
final class UiContentScrollRail {
    private static final float TRACK_TOP = 49.0F;
    private static final float TRACK_BOTTOM = 13.0F;
    private static final float RIGHT_INSET = 9.0F;
    private static final float TRACK_WIDTH = 2.0F;
    private static final float THUMB_WIDTH = 3.0F;
    private static final float MIN_THUMB_HEIGHT = 27.0F;

    private UiContentScrollRail() {
    }

    static List<UiDrawCommand> compose(
            final UiBounds bounds,
            final float contentHeight,
            final float scrollOffset,
            final UiTheme theme) {
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(theme, "theme");
        requireNonNegativeFinite(contentHeight, "contentHeight");
        requireNonNegativeFinite(scrollOffset, "scrollOffset");

        if (bounds.width() < RIGHT_INSET * 2.0F + THUMB_WIDTH
                || bounds.height() <= TRACK_TOP + TRACK_BOTTOM + 24.0F
                || contentHeight <= bounds.height() + 0.5F) {
            return Collections.emptyList();
        }

        final float maxScroll = contentHeight - bounds.height();
        final float offset = Math.min(maxScroll, scrollOffset);
        final float railY = bounds.y() + TRACK_TOP;
        final float railHeight = bounds.height()
                - TRACK_TOP - TRACK_BOTTOM;
        final float railX = bounds.x() + bounds.width() - RIGHT_INSET;
        final float thumbHeight = Math.min(railHeight,
                Math.max(MIN_THUMB_HEIGHT,
                        railHeight * bounds.height() / contentHeight));
        final float travel = railHeight - thumbHeight;
        final float thumbY = railY + travel * offset / maxScroll;
        final List<UiDrawCommand> commands = new ArrayList<UiDrawCommand>(2);

        commands.add(new UiRoundedRectCommand(
                5, railX, railY, TRACK_WIDTH, railHeight,
                TRACK_WIDTH * 0.5F,
                theme.color(UiColorRole.BORDER)));
        commands.add(new UiRoundedRectCommand(
                6, railX - (THUMB_WIDTH - TRACK_WIDTH) * 0.5F,
                thumbY, THUMB_WIDTH, thumbHeight,
                THUMB_WIDTH * 0.5F,
                theme.color(UiColorRole.TEXT_MUTED)));
        return commands;
    }

    private static void requireNonNegativeFinite(
            final float value, final String name) {
        if (!Float.isFinite(value) || value < 0.0F) {
            throw new IllegalArgumentException(
                    name + " must be finite and non-negative");
        }
    }
}
