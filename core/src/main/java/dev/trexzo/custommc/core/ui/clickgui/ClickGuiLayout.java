package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;

import java.util.Objects;

public final class ClickGuiLayout {
    private final UiBounds root;
    private final UiBounds sidebar;
    private final UiBounds search;
    private final UiBounds navigation;
    private final UiBounds content;

    public ClickGuiLayout(
            final UiBounds root,
            final UiBounds sidebar,
            final UiBounds search,
            final UiBounds navigation,
            final UiBounds content) {
        this.root = Objects.requireNonNull(root, "root");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.search = Objects.requireNonNull(search, "search");
        this.navigation = Objects.requireNonNull(
                navigation,
                "navigation");
        this.content = Objects.requireNonNull(
                content,
                "content");
    }

    public UiBounds root() {
        return root;
    }

    public UiBounds sidebar() {
        return sidebar;
    }

    public UiBounds search() {
        return search;
    }

    public UiBounds navigation() {
        return navigation;
    }

    public UiBounds content() {
        return content;
    }
}
