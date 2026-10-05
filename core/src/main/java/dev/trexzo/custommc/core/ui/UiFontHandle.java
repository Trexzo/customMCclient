package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class UiFontHandle {
    private final String id;

    public UiFontHandle(final String id) {
        Objects.requireNonNull(id, "id");
        final String value = id.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "font id must not be blank");
        }
        this.id = value;
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UiFontHandle)) {
            return false;
        }
        final UiFontHandle that =
                (UiFontHandle) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
