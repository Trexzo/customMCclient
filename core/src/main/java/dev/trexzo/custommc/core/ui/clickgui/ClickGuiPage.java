package dev.trexzo.custommc.core.ui.clickgui;

import java.util.Objects;

public final class ClickGuiPage {
    private final String id;
    private final String title;
    private final int priority;

    public ClickGuiPage(
            final String id,
            final String title,
            final int priority) {
        this.id = requireText(id, "id");
        this.title = requireText(title, "title");
        this.priority = priority;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public int priority() {
        return priority;
    }

    private static String requireText(
            final String value,
            final String name) {
        Objects.requireNonNull(value, name);
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(
                    name + " must not be blank");
        }
        return trimmed;
    }
}
