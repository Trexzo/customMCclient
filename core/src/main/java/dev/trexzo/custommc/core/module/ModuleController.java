package dev.trexzo.custommc.core.module;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ModuleController {
    private final ModuleRegistry registry;
    private final Map<Module, ModuleState> states =
            new LinkedHashMap<Module, ModuleState>();

    public ModuleController(final ModuleRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public synchronized ModuleState stateOf(final String id) {
        requireModule(id);
        final Module module = requireModule(id);
        final ModuleState state = states.get(module);
        return state == null ? ModuleState.DISABLED : state;
    }

    public synchronized void enable(final String id) {
        final Module module = requireModule(id);
        final ModuleState current = stateOf(id);

        if (current == ModuleState.ENABLED) {
            return;
        }
        if (current != ModuleState.DISABLED) {
            throw new IllegalStateException(
                    "cannot enable " + id + " from " + current);
        }

        states.put(module, ModuleState.ENABLING);
        try {
            module.onEnable();
            states.put(module, ModuleState.ENABLED);
        } catch (RuntimeException failure) {
            states.put(module, ModuleState.FAILED);
            throw new ModuleLifecycleException(
                    "module enable failed: " + id,
                    failure);
        }
    }

    public synchronized void disable(final String id) {
        final Module module = requireModule(id);
        final ModuleState current = stateOf(id);

        if (current == ModuleState.DISABLED) {
            return;
        }
        if (current != ModuleState.ENABLED
                && current != ModuleState.FAILED) {
            throw new IllegalStateException(
                    "cannot disable " + id + " from " + current);
        }

        states.put(module, ModuleState.DISABLING);
        try {
            module.onDisable();
            states.put(module, ModuleState.DISABLED);
        } catch (RuntimeException failure) {
            states.put(module, ModuleState.FAILED);
            throw new ModuleLifecycleException(
                    "module disable failed: " + id,
                    failure);
        }
    }

    private Module requireModule(final String id) {
        Objects.requireNonNull(id, "id");
        final Module module = registry.find(id);
        if (module == null) {
            throw new IllegalArgumentException(
                    "unknown module: " + id);
        }
        return module;
    }
}
