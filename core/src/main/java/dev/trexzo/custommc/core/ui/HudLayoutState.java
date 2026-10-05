package dev.trexzo.custommc.core.ui;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class HudLayoutState implements HudPlacementResolver {
    private final Map<String, HudPlacement> overrides =
            new TreeMap<String, HudPlacement>();

    public synchronized void set(
            final String widgetId,
            final HudPlacement placement) {
        overrides.put(
                requireId(widgetId),
                Objects.requireNonNull(placement, "placement"));
    }

    public synchronized void clear(final String widgetId) {
        overrides.remove(requireId(widgetId));
    }

    public synchronized HudPlacement overrideFor(
            final String widgetId) {
        return overrides.get(requireId(widgetId));
    }

    public synchronized Map<String, HudPlacement> snapshot() {
        return Collections.unmodifiableMap(
                new TreeMap<String, HudPlacement>(overrides));
    }

    @Override
    public synchronized HudPlacement placementFor(
            final HudWidget widget) {
        Objects.requireNonNull(widget, "widget");
        final HudPlacement override = overrides.get(widget.id());
        if (override != null) {
            return override;
        }
        return new HudPlacement(
                widget.anchor(),
                widget.offsetX(),
                widget.offsetY());
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "widgetId");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "widgetId must not be blank");
        }
        return id;
    }
}
