package dev.trexzo.custommc.core.setting;

import java.util.Objects;

public final class SettingDescriptor {
    private final String settingId;
    private final String label;
    private final SettingValueKind kind;
    private final int priority;
    private final SettingNumericSpec numericSpec;

    public SettingDescriptor(
            final String settingId,
            final String label,
            final SettingValueKind kind,
            final int priority) {
        this(
                settingId,
                label,
                kind,
                priority,
                null);
    }

    public SettingDescriptor(
            final String settingId,
            final String label,
            final SettingValueKind kind,
            final int priority,
            final SettingNumericSpec numericSpec) {
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
        this.numericSpec = validateNumericSpec(
                this.kind,
                numericSpec);
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

    public SettingNumericSpec numericSpec() {
        return numericSpec;
    }

    private static SettingNumericSpec validateNumericSpec(
            final SettingValueKind kind,
            final SettingNumericSpec numericSpec) {
        if (numericSpec == null) {
            return null;
        }
        if (kind != SettingValueKind.INTEGER
                && kind != SettingValueKind.DOUBLE) {
            throw new IllegalArgumentException(
                    "numeric spec requires numeric setting kind");
        }
        if (kind == SettingValueKind.INTEGER
                && !numericSpec.integerCompatible()) {
            throw new IllegalArgumentException(
                    "integer setting requires integer-compatible numeric spec");
        }
        return numericSpec;
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
