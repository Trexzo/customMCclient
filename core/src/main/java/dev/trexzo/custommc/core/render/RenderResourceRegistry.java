package dev.trexzo.custommc.core.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RenderResourceRegistry implements AutoCloseable {
    private final Map<String, RenderResource> resources =
            new LinkedHashMap<String, RenderResource>();
    private boolean closed;

    public synchronized Registration register(
            final RenderResource resource) {
        requireOpen();
        Objects.requireNonNull(resource, "resource");
        final String id = requireId(resource.id());

        if (resources.containsKey(id)) {
            throw new IllegalArgumentException(
                    "duplicate render resource id: " + id);
        }

        resources.put(id, resource);
        return new RegistrationImpl(this, id, resource);
    }

    public synchronized RenderResource find(final String id) {
        return resources.get(Objects.requireNonNull(id, "id"));
    }

    public synchronized List<RenderResource> snapshot() {
        return Collections.unmodifiableList(
                new ArrayList<RenderResource>(resources.values()));
    }

    public synchronized boolean closed() {
        return closed;
    }

    @Override
    public void close() {
        final List<RenderResource> closing;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            closing = new ArrayList<RenderResource>(resources.values());
            resources.clear();
        }

        RenderResourceException combined = null;
        for (int index = closing.size() - 1; index >= 0; index--) {
            final RenderResource resource = closing.get(index);
            try {
                resource.close();
            } catch (RuntimeException failure) {
                if (combined == null) {
                    combined = new RenderResourceException(
                            "one or more render resources failed to close",
                            failure);
                } else {
                    combined.addSuppressed(failure);
                }
            }
        }

        if (combined != null) {
            throw combined;
        }
    }

    private void release(
            final String id,
            final RenderResource expected) {
        final RenderResource removed;
        synchronized (this) {
            final RenderResource current = resources.get(id);
            if (current != expected) {
                return;
            }
            removed = resources.remove(id);
        }

        removed.close();
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "render resource registry is closed");
        }
    }

    private static String requireId(final String id) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "render resource id must not be blank");
        }
        return id;
    }

    public interface Registration extends AutoCloseable {
        RenderResource resource();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final RenderResourceRegistry registry;
        private final String id;
        private final RenderResource resource;
        private boolean active = true;

        RegistrationImpl(
                final RenderResourceRegistry registry,
                final String id,
                final RenderResource resource) {
            this.registry = registry;
            this.id = id;
            this.resource = resource;
        }

        @Override
        public RenderResource resource() {
            return resource;
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

            registry.release(id, resource);
        }
    }
}
