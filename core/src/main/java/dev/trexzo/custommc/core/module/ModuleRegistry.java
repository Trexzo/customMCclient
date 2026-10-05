package dev.trexzo.custommc.core.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ModuleRegistry {
    private final Map<String, Module> modules = new LinkedHashMap<String, Module>();

    public synchronized void register(final Module module) {
        Objects.requireNonNull(module, "module");
        final String id = Objects.requireNonNull(module.id(), "module.id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("module id must not be blank");
        }
        if (modules.containsKey(id)) {
            throw new IllegalArgumentException("duplicate module id: " + id);
        }
        modules.put(id, module);
    }

    public synchronized Module find(final String id) {
        return modules.get(Objects.requireNonNull(id, "id"));
    }

    public synchronized Collection<Module> snapshot() {
        return Collections.unmodifiableList(
                new ArrayList<Module>(modules.values()));
    }
}
