package dev.trexzo.custommc.core.module;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ModuleKeybindRegistry {
    private final ModuleRegistry modules;
    private final Map<String, Entry> byModuleId =
            new LinkedHashMap<String, Entry>();
    private final Map<ModuleKeyChord, Entry> byChord =
            new LinkedHashMap<ModuleKeyChord, Entry>();

    public ModuleKeybindRegistry(
            final ModuleRegistry modules) {
        this.modules = Objects.requireNonNull(
                modules,
                "modules");
    }

    public synchronized Registration register(
            final ModuleKeybind binding) {
        Objects.requireNonNull(binding, "binding");

        if (modules.find(binding.moduleId()) == null) {
            throw new IllegalArgumentException(
                    "unknown module: "
                            + binding.moduleId());
        }
        if (byModuleId.containsKey(
                binding.moduleId())) {
            throw new IllegalArgumentException(
                    "module already has a keybind: "
                            + binding.moduleId());
        }
        if (byChord.containsKey(
                binding.chord())) {
            throw new IllegalArgumentException(
                    "key chord already bound: "
                            + binding.chord());
        }

        final Entry entry =
                new Entry(binding);
        byModuleId.put(
                binding.moduleId(),
                entry);
        byChord.put(
                binding.chord(),
                entry);
        return new RegistrationImpl(
                this,
                entry);
    }

    public synchronized ModuleKeybind findByModule(
            final String moduleId) {
        final Entry entry =
                byModuleId.get(
                        Objects.requireNonNull(
                                moduleId,
                                "moduleId"));
        return entry == null
                ? null
                : entry.binding;
    }

    public synchronized ModuleKeybind findByChord(
            final ModuleKeyChord chord) {
        final Entry entry =
                byChord.get(
                        Objects.requireNonNull(
                                chord,
                                "chord"));
        return entry == null
                ? null
                : entry.binding;
    }

    private synchronized void unregister(
            final Entry expected) {
        final ModuleKeybind binding =
                expected.binding;
        final Entry moduleEntry =
                byModuleId.get(
                        binding.moduleId());
        if (moduleEntry == expected) {
            byModuleId.remove(
                    binding.moduleId());
        }

        final Entry chordEntry =
                byChord.get(
                        binding.chord());
        if (chordEntry == expected) {
            byChord.remove(
                    binding.chord());
        }
    }

    public interface Registration extends AutoCloseable {
        ModuleKeybind binding();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final ModuleKeybindRegistry registry;
        private final Entry entry;
        private boolean active = true;

        RegistrationImpl(
                final ModuleKeybindRegistry registry,
                final Entry entry) {
            this.registry = registry;
            this.entry = entry;
        }

        @Override
        public ModuleKeybind binding() {
            return entry.binding;
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
            registry.unregister(entry);
        }
    }

    private static final class Entry {
        private final ModuleKeybind binding;

        Entry(final ModuleKeybind binding) {
            this.binding = binding;
        }
    }
}
