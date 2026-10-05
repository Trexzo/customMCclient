package dev.trexzo.custommc.core.module;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ModuleCategoryRegistry {
    private final Map<String, ModuleCategoryDescriptor> descriptors =
            new LinkedHashMap<String, ModuleCategoryDescriptor>();

    public synchronized Registration register(
            final ModuleCategoryDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");

        if (descriptors.containsKey(
                descriptor.categoryId())) {
            throw new IllegalArgumentException(
                    "duplicate module category descriptor: "
                            + descriptor.categoryId());
        }

        descriptors.put(
                descriptor.categoryId(),
                descriptor);
        return new RegistrationImpl(
                this,
                descriptor);
    }

    public synchronized ModuleCategoryDescriptor find(
            final String categoryId) {
        return descriptors.get(
                Objects.requireNonNull(
                        categoryId,
                        "categoryId"));
    }

    private synchronized void unregister(
            final ModuleCategoryDescriptor expected) {
        final ModuleCategoryDescriptor current =
                descriptors.get(
                        expected.categoryId());
        if (current == expected) {
            descriptors.remove(
                    expected.categoryId());
        }
    }

    public interface Registration extends AutoCloseable {
        ModuleCategoryDescriptor descriptor();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final ModuleCategoryRegistry registry;
        private final ModuleCategoryDescriptor descriptor;
        private boolean active = true;

        RegistrationImpl(
                final ModuleCategoryRegistry registry,
                final ModuleCategoryDescriptor descriptor) {
            this.registry = registry;
            this.descriptor = descriptor;
        }

        @Override
        public ModuleCategoryDescriptor descriptor() {
            return descriptor;
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
            registry.unregister(descriptor);
        }
    }
}
