package dev.trexzo.custommc.core.module;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ModulePresentationRegistry {
    private final Map<String, ModuleDescriptor> descriptors =
            new LinkedHashMap<String, ModuleDescriptor>();

    public synchronized Registration register(
            final ModuleDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");

        if (descriptors.containsKey(
                descriptor.moduleId())) {
            throw new IllegalArgumentException(
                    "duplicate module descriptor: "
                            + descriptor.moduleId());
        }

        descriptors.put(
                descriptor.moduleId(),
                descriptor);
        return new RegistrationImpl(
                this,
                descriptor);
    }

    public synchronized ModuleDescriptor find(
            final String moduleId) {
        return descriptors.get(
                Objects.requireNonNull(
                        moduleId,
                        "moduleId"));
    }

    private synchronized void unregister(
            final ModuleDescriptor expected) {
        final ModuleDescriptor current =
                descriptors.get(
                        expected.moduleId());
        if (current == expected) {
            descriptors.remove(
                    expected.moduleId());
        }
    }

    public interface Registration extends AutoCloseable {
        ModuleDescriptor descriptor();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final ModulePresentationRegistry registry;
        private final ModuleDescriptor descriptor;
        private boolean active = true;

        RegistrationImpl(
                final ModulePresentationRegistry registry,
                final ModuleDescriptor descriptor) {
            this.registry = registry;
            this.descriptor = descriptor;
        }

        @Override
        public ModuleDescriptor descriptor() {
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
