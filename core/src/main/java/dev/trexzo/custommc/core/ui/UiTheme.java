package dev.trexzo.custommc.core.ui;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class UiTheme {
    private final String id;
    private final Map<UiColorRole, Integer> colors;

    public UiTheme(
            final String id,
            final Map<UiColorRole, Integer> colors) {
        this.id = requireId(id);
        Objects.requireNonNull(colors, "colors");

        final EnumMap<UiColorRole, Integer> copy =
                new EnumMap<UiColorRole, Integer>(
                        UiColorRole.class);
        for (UiColorRole role : UiColorRole.values()) {
            final Integer color =
                    colors.get(role);
            if (color == null) {
                throw new IllegalArgumentException(
                        "missing UI color role: " + role);
            }
            copy.put(role, color);
        }

        this.colors =
                Collections.unmodifiableMap(copy);
    }

    public String id() {
        return id;
    }

    public int color(final UiColorRole role) {
        return colors.get(
                Objects.requireNonNull(
                        role,
                        "role"));
    }

    public Map<UiColorRole, Integer> colors() {
        return colors;
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        final String value = id.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "theme id must not be blank");
        }
        return value;
    }
}
