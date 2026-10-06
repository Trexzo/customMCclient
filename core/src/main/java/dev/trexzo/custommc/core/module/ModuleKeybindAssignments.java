package dev.trexzo.custommc.core.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    public synchronized Map<String, ModuleKeyChord>
    snapshotOwnedBindings() {
        requireOpen();

        final Map<String, ModuleKeyChord> snapshot =
                new LinkedHashMap<String, ModuleKeyChord>();
        for (Map.Entry<String, ModuleKeybindRegistry.Registration> entry :
                owned.entrySet()) {
            final ModuleKeybindRegistry.Registration registration =
                    entry.getValue();
            final ModuleKeybind binding =
                    registration.binding();
            if (registration.active()
                    && registry.findByModule(
                    entry.getKey()) == binding) {
                snapshot.put(
                        entry.getKey(),
                        binding.chord());
            }
        }
        return Collections.unmodifiableMap(snapshot);
    }

    public synchronized boolean replaceOwnedBindings(
            final Map<String, ModuleKeyChord> nextBindings) {
        requireOpen();
        Objects.requireNonNull(
                nextBindings,
                "nextBindings");

        final Map<String, ModuleKeyChord> normalized =
                normalize(nextBindings);
        final Map<String, ModuleKeyChord> previous =
                new LinkedHashMap<String, ModuleKeyChord>(
                        snapshotOwnedBindings());

        if (previous.equals(normalized)) {
            return false;
        }

        preflight(normalized);

        closeOwnedRegistrations();
        owned.clear();

        try {
            registerAll(normalized);
            return true;
        } catch (RuntimeException failure) {
            closeOwnedRegistrations();
            owned.clear();

            try {
                registerAll(previous);
            } catch (RuntimeException restoreFailure) {
                failure.addSuppressed(
                        restoreFailure);
            }
            throw failure;
        }
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

    private Map<String, ModuleKeyChord> normalize(
            final Map<String, ModuleKeyChord> bindings) {
        final Map<String, ModuleKeyChord> normalized =
                new LinkedHashMap<String, ModuleKeyChord>();
        final Set<ModuleKeyChord> chords =
                new HashSet<ModuleKeyChord>();

        for (Map.Entry<String, ModuleKeyChord> entry :
                bindings.entrySet()) {
            final String id =
                    requireModuleId(
                            entry.getKey());
            final ModuleKeyChord chord =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "keybind chord");

            if (normalized.containsKey(id)) {
                throw new IllegalArgumentException(
                        "duplicate normalized module id: " + id);
            }
            if (!chords.add(chord)) {
                throw new IllegalArgumentException(
                        "duplicate key chord in assignment set: "
                                + chord);
            }
            normalized.put(id, chord);
        }
        return normalized;
    }

    private void preflight(
            final Map<String, ModuleKeyChord> bindings) {
        for (Map.Entry<String, ModuleKeyChord> entry :
                bindings.entrySet()) {
            final String id =
                    entry.getKey();
            final ModuleKeyChord chord =
                    entry.getValue();

            final ModuleKeybind existing =
                    registry.findByModule(id);
            final ModuleKeybindRegistry.Registration registration =
                    owned.get(id);
            if (existing != null
                    && !isOwned(
                    existing,
                    registration)) {
                throw new IllegalStateException(
                        "module keybind is externally owned: " + id);
            }

            final ModuleKeybind chordOwner =
                    registry.findByChord(chord);
            if (chordOwner == null
                    || id.equals(
                    chordOwner.moduleId())) {
                continue;
            }

            final ModuleKeybindRegistry.Registration ownerRegistration =
                    owned.get(
                            chordOwner.moduleId());
            if (!isOwned(
                    chordOwner,
                    ownerRegistration)) {
                throw new IllegalArgumentException(
                        "key chord already bound: " + chord);
            }
        }
    }

    private void registerAll(
            final Map<String, ModuleKeyChord> bindings) {
        for (Map.Entry<String, ModuleKeyChord> entry :
                bindings.entrySet()) {
            final ModuleKeybindRegistry.Registration registration =
                    registry.register(
                            new ModuleKeybind(
                                    entry.getKey(),
                                    entry.getValue()));
            owned.put(
                    entry.getKey(),
                    registration);
        }
    }

    private void closeOwnedRegistrations() {
        final List<ModuleKeybindRegistry.Registration> registrations =
                new ArrayList<ModuleKeybindRegistry.Registration>(
                        owned.values());
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
