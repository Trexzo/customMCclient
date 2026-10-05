package dev.trexzo.custommc.core.module;

import java.util.Objects;

public final class ModuleSettingBinding {
    private final String moduleId;
    private final String settingId;
    private final int priority;

    public ModuleSettingBinding(
            final String moduleId,
            final String settingId,
            final int priority) {
        this.moduleId = requireText(
                moduleId,
                "moduleId");
        this.settingId = requireText(
                settingId,
                "settingId");
        this.priority = priority;
    }

    public String moduleId() {
        return moduleId;
    }

    public String settingId() {
        return settingId;
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
