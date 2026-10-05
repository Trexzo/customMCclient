package dev.trexzo.custommc.core.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class UiFocusManager {
    private final Map<String, UiFocusTarget> targets =
            new LinkedHashMap<String, UiFocusTarget>();
    private UiFocusTarget focused;

    public synchronized Registration register(
            final UiFocusTarget target) {
        Objects.requireNonNull(target, "target");
        final String id = requireId(target.id());

        if (targets.containsKey(id)) {
            throw new IllegalArgumentException(
                    "duplicate focus target id: " + id);
        }

        targets.put(id, target);
        return new RegistrationImpl(
                this,
                id,
                target);
    }

    public synchronized boolean requestFocus(
            final String id) {
        final UiFocusTarget next =
                targets.get(requireId(id));
        if (next == null) {
            return false;
        }
        if (focused == next) {
            return true;
        }

        final UiFocusTarget previous = focused;
        focused = next;

        if (previous != null) {
            previous.onFocusChanged(false);
        }
        next.onFocusChanged(true);
        return true;
    }

    public synchronized void clearFocus() {
        final UiFocusTarget previous = focused;
        focused = null;
        if (previous != null) {
            previous.onFocusChanged(false);
        }
    }

    public synchronized String focusedId() {
        return focused == null ? null : focused.id();
    }

    public synchronized boolean dispatchKey(
            final UiKeyEvent event) {
        Objects.requireNonNull(event, "event");
        return focused != null
                && focused.onKey(event);
    }

    private synchronized void unregister(
            final String id,
            final UiFocusTarget expected) {
        final UiFocusTarget current =
                targets.get(id);
        if (current != expected) {
            return;
        }

        if (focused == expected) {
            focused = null;
            expected.onFocusChanged(false);
        }
        targets.remove(id);
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        final String value = id.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "focus target id must not be blank");
        }
        return value;
    }

    public interface Registration extends AutoCloseable {
        UiFocusTarget target();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final UiFocusManager manager;
        private final String id;
        private final UiFocusTarget target;
        private boolean active = true;

        RegistrationImpl(
                final UiFocusManager manager,
                final String id,
                final UiFocusTarget target) {
            this.manager = manager;
            this.id = id;
            this.target = target;
        }

        @Override
        public UiFocusTarget target() {
            return target;
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
            manager.unregister(id, target);
        }
    }
}
