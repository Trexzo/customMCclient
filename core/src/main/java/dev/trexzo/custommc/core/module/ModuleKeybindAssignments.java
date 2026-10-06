package dev.trexzo.custommc.core.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModuleKeybindAssignments
        implements AutoCloseable {
    private final ModuleKeybindRegistry registry;
    private final Map<String, ModuleKeybindRegistry.Registration> owned =
            new LinkedHashMap<String, ModuleKeybindRegistry.Registration>();
    private boolean closed;

    public ModuleKeybindAssignments(
            final ModuleKeybindRegistry registry) {
        this.registry = Objects.requireNonNull(
                registry,
                "registry");
    }

    public synchronized boolean bind(
            final String moduleId,
            final ModuleKeyChord chord) {
        requireOpen();

        final String id =
                requireModuleId(moduleId);
        Objects.requireNonNull(chord, "chord");

        final ModuleKeybind existing =
                registry.findByModule(id);
        final ModuleKeybindRegistry.Registration ownedRegistration =
                owned.get(id);

        if (existing != null
                && !isOwned(
                existing,
                ownedRegistration)) {
            throw new IllegalStateException(
                    "module keybind is externally owned: " + id);
        }

        if (existing != null
                && existing.chord().equals(chord)) {
            return false;
        }

        final ModuleKeybind chordOwner =
                registry.findByChord(chord);
        if (chordOwner != null
                && !id.equals(
                chordOwner.moduleId())) {
            throw new IllegalArgumentException(
                    "key chord already bound: " + chord);
        }

        if (ownedRegistration == null) {
            final ModuleKeybindRegistry.Registration next =
                    registry.register(
                            new ModuleKeybind(
                                    id,
                                    chord));
            owned.put(id, next);
            return true;
        }

        final ModuleKeybind previous =
                ownedRegistration.binding();
        ownedRegistration.close();
        owned.remove(id);

        try {
            final ModuleKeybindRegistry.Registration next =
                    registry.register(
                            new ModuleKeybind(
                                    id,
                                    chord));
            owned.put(id, next);
            return true;
        } catch (RuntimeException failure) {
            try {
                final ModuleKeybindRegistry.Registration restored =
                        registry.register(previous);
                owned.put(id, restored);
            } catch (RuntimeException restoreFailure) {
                failure.addSuppressed(
                        restoreFailure);
            }
            throw failure;
        }
    }

    public synchronized boolean unbind(
            final String moduleId) {
        requireOpen();

        final String id =
                requireModuleId(moduleId);
        final ModuleKeybind existing =
                registry.findByModule(id);
        final ModuleKeybindRegistry.Registration ownedRegistration =
                owned.get(id);

        if (existing == null) {
            if (ownedRegistration != null) {
                ownedRegistration.close();
                owned.remove(id);
            }
            return false;
        }

        if (!isOwned(
                existing,
                ownedRegistration)) {
            throw new IllegalStateException(
                    "module keybind is externally owned: " + id);
        }

        ownedRegistration.close();
        owned.remove(id);
        return true;
    }

    public synchronized boolean owns(
            final String moduleId) {
        requireOpen();

        final String id =
                requireModuleId(moduleId);
        final ModuleKeybindRegistry.Registration registration =
                owned.get(id);
        final ModuleKeybind existing =
                registry.findByModule(id);
        return existing != null
                && isOwned(
                existing,
                registration);
    }

    public synchronized ModuleKeybind binding(
            final String moduleId) {
        requireOpen();
        return registry.findByModule(
                requireModuleId(moduleId));
    }

    public synchronized boolean closed() {
        return closed;
    }

    @Override
    public void close() {
        final List<ModuleKeybindRegistry.Registration> registrations;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            registrations =
                    new ArrayList<ModuleKeybindRegistry.Registration>(
                            owned.values());
            owned.clear();
        }

        Collections.reverse(registrations);
        for (ModuleKeybindRegistry.Registration registration :
                registrations) {
            registration.close();
        }
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "module keybind assignments are closed");
        }
    }

    private static boolean isOwned(
            final ModuleKeybind existing,
            final ModuleKeybindRegistry.Registration registration) {
        return registration != null
                && registration.active()
                && registration.binding() == existing;
    }

    private static String requireModuleId(
            final String moduleId) {
        Objects.requireNonNull(moduleId, "moduleId");
        final String id = moduleId.trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException(
                    "moduleId must not be blank");
        }
        return id;
    }
}
