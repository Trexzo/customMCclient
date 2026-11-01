package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;

import java.util.Objects;

/**
 * Native ClickGUI's shared, pointer-only rail drag. No setting/module state
 * is touched: only the existing page-local scroll offset changes.
 *
 * A press on the thin visible rail gets a 12px non-visual corridor. Pressing
 * the thumb retains its physical grab position; pressing the empty track
 * seeks to the pointer with the thumb centered. The release ends capture
 * even when it arrives outside the content viewport.
 */
final class UiContentScrollDrag {
    private boolean dragging;
    private float grabY;

    boolean pointer(final UiPointerEvent event, final UiBounds bounds,
            final float contentHeight, final ClickGuiContentScrollState scroll) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(scroll, "scroll");
        if (event.action() == UiPointerAction.RELEASE) {
            final boolean wasDragging = dragging;
            dragging = false;
            return wasDragging;
        }
        if (event.action() == UiPointerAction.MOVE) {
            if (!dragging) return false;
            seek(event.y(), bounds, contentHeight, scroll);
            return true;
        }
        if (event.action() != UiPointerAction.PRESS
                || event.button() != UiPointerButton.LEFT) {
            return false;
        }
        // Never begin a new interaction if this page no longer overflows.
        final UiBounds hit = UiContentScrollRail.hitBounds(bounds, contentHeight);
        if (hit == null || !hit.contains(event.x(), event.y())) {
            return false;
        }
        final UiBounds thumb = UiContentScrollRail.thumbBounds(
                bounds, contentHeight,
                scroll.offset(contentHeight, bounds.height()));
        final boolean thumbHit = event.y() >= thumb.y()
                && event.y() <= thumb.y() + thumb.height();
        grabY = thumbHit ? event.y() - thumb.y()
                : thumb.height() * .5F;
        dragging = true;
        seek(event.y(), bounds, contentHeight, scroll);
        return true;
    }

    boolean dragging() {
        return dragging;
    }

    void cancel() {
        dragging = false;
        grabY = 0.0F;
    }

    private void seek(final float pointerY, final UiBounds bounds,
            final float contentHeight, final ClickGuiContentScrollState scroll) {
        scroll.setOffset(UiContentScrollRail.offsetForPointer(
                bounds, contentHeight, pointerY, grabY),
                contentHeight, bounds.height());
    }
}
