package dev.trexzo.custommc.core.module;

import java.util.Objects;

public final class ModuleKeybind {
    private final String moduleId;
    private final ModuleKeyChord chord;

    public ModuleKeybind(
            final String moduleId,
            final ModuleKeyChord chord) {
        this.moduleId = requireModuleId(moduleId);
        this.chord = Objects.requireNonNull(
                chord,
                "chord");
    }

    public String moduleId() {
        return moduleId;
    }

    public ModuleKeyChord chord() {
        return chord;
    }

    private static String requireModuleId(
            final String moduleId) {
        Objects.requireNonNull(moduleId, "moduleId");
        final String value = moduleId.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "moduleId must not be blank");
        }
        return value;
    }
}
