package dev.trexzo.custommc.core.ui.clickgui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Display-only projection of registered ClickGUI pages.
 *
 * Module Details remains a valid registered destination reached via module
 * dots or right-click. It is contextual, not a persistent root navigation
 * entry. The projection is shared by renderer, pointer and keyboard input.
 */
final class ClickGuiNavigationPages {
    private ClickGuiNavigationPages() {
    }

    static List<ClickGuiPage> visible(final ClickGuiSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        final List<ClickGuiPage> pages = new ArrayList<ClickGuiPage>();
        for (ClickGuiPage page : snapshot.pages()) {
            if (!ClickGuiRuntime.MODULE_DETAIL_PAGE_ID.equals(page.id())) {
                pages.add(page);
            }
        }
        return Collections.unmodifiableList(pages);
    }

    static String highlightedId(final ClickGuiSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        return ClickGuiRuntime.MODULE_DETAIL_PAGE_ID.equals(
                snapshot.selectedPageId())
                ? ClickGuiRuntime.MODULES_PAGE_ID
                : snapshot.selectedPageId();
    }
}
