package dev.trexzo.custommc.core.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ServiceRegistry {
    private final Map<Class<?>, Object> services =
            new LinkedHashMap<Class<?>, Object>();

    public synchronized <T> void register(
            final Class<T> type,
            final T service) {
        registerManaged(type, service);
    }

    public synchronized <T> Registration registerManaged(
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
        return new RegistrationImpl(
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
        return services.containsKey(Objects.requireNonNull(type, "type"));
    }

    public synchronized Map<Class<?>, Object> snapshot() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<Class<?>, Object>(services));
    }

    private synchronized void unregister(
            final Class<?> type,
            final Object expected) {
        final Object current = services.get(type);
        if (current == expected) {
            services.remove(type);
        }
    }

    public interface Registration extends AutoCloseable {
        Class<?> type();

        Object service();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final ServiceRegistry registry;
        private final Class<?> type;
        private final Object service;
        private boolean active = true;

        RegistrationImpl(
                final ServiceRegistry registry,
                final Class<?> type,
                final Object service) {
            this.registry = registry;
            this.type = type;
            this.service = service;
        }

        @Override
        public Class<?> type() {
            return type;
        }

        @Override
        public Object service() {
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
