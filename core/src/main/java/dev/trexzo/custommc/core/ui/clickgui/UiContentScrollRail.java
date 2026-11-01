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
    private static final float TRACK_BOTTOM = 21.0F;
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

        final UiBounds track = trackBounds(bounds, contentHeight);
        if (track == null) {
            return Collections.emptyList();
        }
        final UiBounds thumb = thumbBounds(bounds, contentHeight, scrollOffset);
        final float railX = track.x();
        final float railY = track.y();
        final float railHeight = track.height();
        final float thumbHeight = thumb.height();
        final float thumbY = thumb.y();
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

    // The drawable rail stays exactly 2px wide, but receives a 12px
    // transparent hit corridor *outside* the row hitbox. It is not a
    // fake module toggle or an extra background.
    static UiBounds hitBounds(final UiBounds bounds, final float contentHeight) {
        final UiBounds track = trackBounds(bounds, contentHeight);
        return track == null ? null
                : new UiBounds(track.x() - 5.0F, track.y(),
                        12.0F, track.height());
    }

    static UiBounds trackBounds(
            final UiBounds bounds, final float contentHeight) {
        Objects.requireNonNull(bounds, "bounds");
        requireNonNegativeFinite(contentHeight, "contentHeight");
        if (bounds.width() < RIGHT_INSET * 2.0F + THUMB_WIDTH
                || bounds.height() <= TRACK_TOP + TRACK_BOTTOM + 24.0F
                || contentHeight <= bounds.height() + 0.5F) {
            return null;
        }
        return new UiBounds(bounds.x() + bounds.width() - RIGHT_INSET,
                bounds.y() + TRACK_TOP, TRACK_WIDTH,
                bounds.height() - TRACK_TOP - TRACK_BOTTOM);
    }

    static UiBounds thumbBounds(
            final UiBounds bounds, final float contentHeight,
            final float scrollOffset) {
        requireNonNegativeFinite(scrollOffset, "scrollOffset");
        final UiBounds track = trackBounds(bounds, contentHeight);
        if (track == null) return null;
        final float maximum = contentHeight - bounds.height();
        final float thumbHeight = Math.min(track.height(),
                Math.max(MIN_THUMB_HEIGHT,
                        track.height() * bounds.height() / contentHeight));
        final float travel = track.height() - thumbHeight;
        final float top = track.y() + travel
                * Math.min(maximum, scrollOffset) / maximum;
        return new UiBounds(track.x() - (THUMB_WIDTH - TRACK_WIDTH) * 0.5F,
                top, THUMB_WIDTH, thumbHeight);
    }

    static float offsetForPointer(
            final UiBounds bounds, final float contentHeight,
            final float pointerY, final float thumbGrabY) {
        final UiBounds track = trackBounds(bounds, contentHeight);
        if (track == null) return 0.0F;
        final UiBounds thumb = thumbBounds(bounds, contentHeight, 0.0F);
        final float travel = track.height() - thumb.height();
        if (travel <= 0.0F) return 0.0F;
        final float fraction = Math.max(0.0F, Math.min(1.0F,
                (pointerY - thumbGrabY - track.y()) / travel));
        return fraction * (contentHeight - bounds.height());
    }

    private static void requireNonNegativeFinite(
            final float value, final String name) {
        if (!Float.isFinite(value) || value < 0.0F) {
            throw new IllegalArgumentException(
                    name + " must be finite and non-negative");
        }
    }
}
