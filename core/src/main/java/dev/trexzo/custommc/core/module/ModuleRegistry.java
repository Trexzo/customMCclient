package dev.trexzo.custommc.core.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ModuleRegistry {
    private final Map<String, Module> modules = new LinkedHashMap<String, Module>();

    public synchronized Registration register(final Module module) {
        Objects.requireNonNull(module, "module");
        final String id = Objects.requireNonNull(module.id(), "module.id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("module id must not be blank");
        }
        if (modules.containsKey(id)) {
            throw new IllegalArgumentException("duplicate module id: " + id);
        }
        modules.put(id, module);
        return new RegistrationImpl(
                this,
                id,
                module);
    }

    public synchronized Module find(final String id) {
        return modules.get(Objects.requireNonNull(id, "id"));
    }

    public synchronized Collection<Module> snapshot() {
        return Collections.unmodifiableList(
                new ArrayList<Module>(modules.values()));
    }

    private synchronized void unregister(
            final String id,
            final Module expected) {
        final Module current =
                modules.get(id);
        if (current == expected) {
            modules.remove(id);
        }
    }

    public interface Registration extends AutoCloseable {
        Module module();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final ModuleRegistry registry;
        private final String id;
        private final Module module;
        private boolean active = true;

        RegistrationImpl(
                final ModuleRegistry registry,
                final String id,
                final Module module) {
            this.registry = registry;
            this.id = id;
            this.module = module;
        }

        @Override
        public Module module() {
            return module;
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
            registry.unregister(
                    id,
                    module);
        }
    }
}
