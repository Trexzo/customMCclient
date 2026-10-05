package dev.trexzo.custommc.core.setting;

import java.util.Objects;

public final class SettingDescriptor {
    private final String settingId;
    private final String label;
    private final SettingValueKind kind;
    private final int priority;

    public SettingDescriptor(
            final String settingId,
            final String label,
            final SettingValueKind kind,
            final int priority) {
        this.settingId = requireText(
                settingId,
                "settingId");
        this.label = requireText(
                label,
                "label");
        this.kind = Objects.requireNonNull(
                kind,
                "kind");
        this.priority = priority;
    }

    public String settingId() {
        return settingId;
    }

    public String label() {
        return label;
    }

    public SettingValueKind kind() {
        return kind;
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
