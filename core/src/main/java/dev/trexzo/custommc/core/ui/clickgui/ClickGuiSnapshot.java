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

    public ClickGuiSnapshot(
            final boolean open,
            final String selectedPageId,
            final String searchQuery,
            final List<ClickGuiPage> pages) {
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
}
