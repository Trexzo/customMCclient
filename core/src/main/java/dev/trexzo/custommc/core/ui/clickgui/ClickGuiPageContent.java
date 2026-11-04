package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiScrollEvent;

import java.util.List;

public interface ClickGuiPageContent {
    List<UiDrawCommand> compose(
            ClickGuiContentContext context);

    default boolean key(final UiKeyEvent event) {
        return false;
    }

    default boolean pointer(
            final ClickGuiContentInputContext context,
            final UiPointerEvent event) {
        return false;
    }

    /**
     * True only while a page owns an active pointer drag. The top-level
     * controller may then route MOVE outside the content rectangle until
     * RELEASE, without forwarding unowned hover to hidden controls.
     */
    default boolean capturesPointerDrag() {
        return false;
    }

    /**
     * Cancel only transient pointer capture (no setting or module rollback).
     * Used when a different page or GUI session supersedes the drag owner.
     */
    default void cancelPointerDrag() {
    }

    default boolean scroll(
            final ClickGuiContentInputContext context,
            final UiScrollEvent event) {
        return false;
    }
}
