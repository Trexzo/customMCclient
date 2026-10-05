package dev.trexzo.custommc.core.ui.clickgui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ClickGuiSnapshot {
    private final boolean open;
    private final String selectedPageId;
    private final String searchQuery;
    private final List<ClickGuiPage> pages;
    private final float navigationScroll;

    public ClickGuiSnapshot(
            final boolean open,
            final String selectedPageId,
            final String searchQuery,
            final List<ClickGuiPage> pages) {
        this(
                open,
                selectedPageId,
                searchQuery,
                pages,
                0.0F);
    }

    public ClickGuiSnapshot(
            final boolean open,
            final String selectedPageId,
            final String searchQuery,
            final List<ClickGuiPage> pages,
            final float navigationScroll) {
        this.open = open;
        this.selectedPageId = selectedPageId;
        this.searchQuery =
                Objects.requireNonNull(
                        searchQuery,
                        "searchQuery");

        Objects.requireNonNull(pages, "pages");
        this.pages =
                Collections.unmodifiableList(
                        new ArrayList<ClickGuiPage>(pages));

        if (Float.isNaN(navigationScroll)
                || Float.isInfinite(navigationScroll)
                || navigationScroll < 0.0F) {
            throw new IllegalArgumentException(
                    "navigationScroll must be finite and non-negative");
        }
        this.navigationScroll = navigationScroll;
    }

    public boolean open() {
        return open;
    }

    public String selectedPageId() {
        return selectedPageId;
    }

    public String searchQuery() {
        return searchQuery;
    }

    public List<ClickGuiPage> pages() {
        return pages;
    }

    public float navigationScroll() {
        return navigationScroll;
    }
}
