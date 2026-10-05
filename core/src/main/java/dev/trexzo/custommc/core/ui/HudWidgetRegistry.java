package dev.trexzo.custommc.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HudWidgetRegistry {
    private static final Comparator<HudWidget> ORDER =
            new Comparator<HudWidget>() {
                @Override
                public int compare(
                        final HudWidget left,
                        final HudWidget right) {
                    final int priority = Integer.compare(
                            left.priority(),
                            right.priority());
                    if (priority != 0) {
                        return priority;
                    }
                    return left.id().compareTo(right.id());
                }
            };

    private final Map<String, HudWidget> widgets =
            new LinkedHashMap<String, HudWidget>();
    private List<HudWidget> plan =
            Collections.emptyList();

    public synchronized Registration register(
            final HudWidget widget) {
        Objects.requireNonNull(widget, "widget");
        final String id = requireId(widget.id());
        Objects.requireNonNull(widget.anchor(), "widget.anchor");

        if (widgets.containsKey(id)) {
            throw new IllegalArgumentException(
                    "duplicate HUD widget id: " + id);
        }

        widgets.put(id, widget);
        rebuild();

        return new RegistrationImpl(
                this,
                id,
                widget);
    }

    public synchronized List<HudWidget> snapshot() {
        return plan;
    }

    private synchronized void unregister(
            final String id,
            final HudWidget expected) {
        final HudWidget current = widgets.get(id);
        if (current != expected) {
            return;
        }

        widgets.remove(id);
        rebuild();
    }

    private void rebuild() {
        final List<HudWidget> next =
                new ArrayList<HudWidget>(widgets.values());
        Collections.sort(next, ORDER);
        plan = Collections.unmodifiableList(next);
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "HUD widget id must not be blank");
        }
        return id;
    }

    public interface Registration extends AutoCloseable {
        HudWidget widget();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final HudWidgetRegistry registry;
        private final String id;
        private final HudWidget widget;
        private boolean active = true;

        RegistrationImpl(
                final HudWidgetRegistry registry,
                final String id,
                final HudWidget widget) {
            this.registry = registry;
            this.id = id;
            this.widget = widget;
        }

        @Override
        public HudWidget widget() {
            return widget;
        }

        @Override
        public synchronized boolean active() {
            return active;
        }

        @Override
        public void close() {
            synchronized (this) {
                if (!active) {
                    return;
                }
                active = false;
            }

            registry.unregister(id, widget);
        }
    }
}
