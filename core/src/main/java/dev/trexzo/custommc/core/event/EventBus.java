package dev.trexzo.custommc.core.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> listeners =
            new LinkedHashMap<Class<?>, List<Consumer<?>>>();

    public synchronized <T> Subscription subscribe(
            final Class<T> eventType,
            final Consumer<? super T> listener) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(listener, "listener");

        List<Consumer<?>> bucket = listeners.get(eventType);
        if (bucket == null) {
            bucket = new ArrayList<Consumer<?>>();
            listeners.put(eventType, bucket);
        }
        bucket.add(listener);

        final List<Consumer<?>> capturedBucket = bucket;
        return new Subscription() {
            private boolean active = true;

            @Override
            public synchronized void close() {
                if (!active) {
                    return;
                }
                synchronized (EventBus.this) {
                    capturedBucket.remove(listener);
                    if (capturedBucket.isEmpty()) {
                        listeners.remove(eventType);
                    }
                }
                active = false;
            }
        };
    }

    public <T> void publish(final T event) {
        Objects.requireNonNull(event, "event");

        final List<Consumer<?>> snapshot;
        synchronized (this) {
            final List<Consumer<?>> bucket = listeners.get(event.getClass());
            if (bucket == null || bucket.isEmpty()) {
                return;
            }
            snapshot = new ArrayList<Consumer<?>>(bucket);
        }

        for (Consumer<?> raw : snapshot) {
            @SuppressWarnings("unchecked")
            final Consumer<T> listener = (Consumer<T>) raw;
            listener.accept(event);
        }
    }

    public interface Subscription extends AutoCloseable {
        @Override
        void close();
    }
}
