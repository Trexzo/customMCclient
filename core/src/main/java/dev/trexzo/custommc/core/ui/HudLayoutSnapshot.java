package dev.trexzo.custommc.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class HudLayoutSnapshot {
    private final List<HudLayoutEntry> entries;

    public HudLayoutSnapshot(
            final List<HudLayoutEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        this.entries = Collections.unmodifiableList(
                new ArrayList<HudLayoutEntry>(entries));
    }

    public List<HudLayoutEntry> entries() {
        return entries;
    }

    public HudLayoutEntry topmostAt(
            final float x,
            final float y) {
        for (int index = entries.size() - 1;
                index >= 0;
                index--) {
            final HudLayoutEntry entry = entries.get(index);
            if (entry.bounds().contains(x, y)) {
                return entry;
            }
        }
        return null;
    }
}
