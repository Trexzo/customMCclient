package dev.trexzo.custommc.core.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ServiceRegistry {
    private final Map<Class<?>, Object> services =
            new LinkedHashMap<Class<?>, Object>();

    public synchronized <T> Registration<T> register(
            final Class<T> type,
            final T service) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(service, "service");

        if (!type.isInstance(service)) {
            throw new IllegalArgumentException(
                    "service does not implement " + type.getName());
        }
        if (services.containsKey(type)) {
            throw new IllegalArgumentException(
                    "duplicate service: " + type.getName());
        }

        services.put(type, service);
        return new RegistrationImpl<T>(
                this,
                type,
                service);
    }

    public synchronized <T> T require(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        final Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException(
                    "service not registered: " + type.getName());
        }
        return type.cast(service);
    }

    public synchronized boolean contains(final Class<?> type) {
        return services.containsKey(
                Objects.requireNonNull(type, "type"));
    }

    public synchronized Map<Class<?>, Object> snapshot() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<Class<?>, Object>(services));
    }

    private synchronized <T> void unregister(
            final Class<T> type,
            final T service) {
        if (services.get(type) == service) {
            services.remove(type);
        }
    }

    public interface Registration<T> extends AutoCloseable {
        T service();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl<T>
            implements Registration<T> {
        private final ServiceRegistry registry;
        private final Class<T> type;
        private final T service;
        private boolean active = true;

        RegistrationImpl(
                final ServiceRegistry registry,
                final Class<T> type,
                final T service) {
            this.registry = registry;
            this.type = type;
            this.service = service;
        }

        @Override
        public T service() {
            return service;
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

            registry.unregister(type, service);
        }
    }
}
