package dev.trexzo.custommc.core.module;

import java.util.Objects;

public final class ModuleKeyChord {
    private final String keyId;
    private final boolean shift;
    private final boolean control;
    private final boolean alt;

    public ModuleKeyChord(
            final String keyId,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        this.keyId = requireKeyId(keyId);
        this.shift = shift;
        this.control = control;
        this.alt = alt;
    }

    public static ModuleKeyChord key(
            final String keyId) {
        return new ModuleKeyChord(
                keyId,
                false,
                false,
                false);
    }

    public String keyId() {
        return keyId;
    }

    public boolean shift() {
        return shift;
    }

    public boolean control() {
        return control;
    }

    public boolean alt() {
        return alt;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ModuleKeyChord)) {
            return false;
        }
        final ModuleKeyChord chord =
                (ModuleKeyChord) other;
        return shift == chord.shift
                && control == chord.control
                && alt == chord.alt
                && keyId.equals(chord.keyId);
    }

    @Override
    public int hashCode() {
        int result = keyId.hashCode();
        result = 31 * result + (shift ? 1 : 0);
        result = 31 * result + (control ? 1 : 0);
        result = 31 * result + (alt ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder value =
                new StringBuilder();
        if (control) {
            value.append("CTRL+");
        }
        if (shift) {
            value.append("SHIFT+");
        }
        if (alt) {
            value.append("ALT+");
        }
        value.append(keyId);
        return value.toString();
    }

    private static String requireKeyId(
            final String keyId) {
        Objects.requireNonNull(keyId, "keyId");
        final String value = keyId.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "keyId must not be blank");
        }
        return value;
    }
}
