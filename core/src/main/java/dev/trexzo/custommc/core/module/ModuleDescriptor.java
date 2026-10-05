package dev.trexzo.custommc.core.module;

import java.util.Objects;

public final class ModuleDescriptor {
    public static final String DEFAULT_CATEGORY_ID =
            "general";

    private final String moduleId;
    private final String displayName;
    private final String description;
    private final String categoryId;
    private final int priority;

    public ModuleDescriptor(
            final String moduleId,
            final String displayName,
            final String description) {
        this(
                moduleId,
                displayName,
                description,
                DEFAULT_CATEGORY_ID,
                0);
    }

    public ModuleDescriptor(
            final String moduleId,
            final String displayName,
            final String description,
            final String categoryId,
            final int priority) {
        this.moduleId = requireText(
                moduleId,
                "moduleId");
        this.displayName = requireText(
                displayName,
                "displayName");
        this.description = Objects.requireNonNull(
                description,
                "description")
                .trim();
        this.categoryId = requireText(
                categoryId,
                "categoryId");
        this.priority = priority;
    }

    public String moduleId() {
        return moduleId;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public String categoryId() {
        return categoryId;
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
