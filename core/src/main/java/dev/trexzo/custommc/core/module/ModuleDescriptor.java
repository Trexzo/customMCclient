package dev.trexzo.custommc.core.module;

import java.util.Objects;

public final class ModuleDescriptor {
    private final String moduleId;
    private final String displayName;
    private final String description;

    public ModuleDescriptor(
            final String moduleId,
            final String displayName,
            final String description) {
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
