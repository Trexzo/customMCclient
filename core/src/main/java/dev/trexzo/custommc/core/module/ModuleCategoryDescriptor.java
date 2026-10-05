package dev.trexzo.custommc.core.module;

import java.util.Objects;

public final class ModuleCategoryDescriptor {
    private final String categoryId;
    private final String displayName;
    private final int priority;

    public ModuleCategoryDescriptor(
            final String categoryId,
            final String displayName,
            final int priority) {
        this.categoryId = requireText(
                categoryId,
                "categoryId");
        this.displayName = requireText(
                displayName,
                "displayName");
        this.priority = priority;
    }

    public String categoryId() {
        return categoryId;
    }

    public String displayName() {
        return displayName;
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
