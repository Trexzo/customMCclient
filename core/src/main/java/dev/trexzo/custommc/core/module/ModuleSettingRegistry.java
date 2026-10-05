package dev.trexzo.custommc.core.module;

import dev.trexzo.custommc.core.setting.SettingRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModuleSettingRegistry {
    private final ModuleRegistry modules;
    private final SettingRegistry settings;
    private final Map<String, Entry> bySettingId =
            new LinkedHashMap<String, Entry>();
    private long nextSequence;

    public ModuleSettingRegistry(
            final ModuleRegistry modules,
            final SettingRegistry settings) {
        this.modules = Objects.requireNonNull(
                modules,
                "modules");
        this.settings = Objects.requireNonNull(
                settings,
                "settings");
    }

    public synchronized Registration register(
            final ModuleSettingBinding binding) {
        Objects.requireNonNull(binding, "binding");

        if (modules.find(binding.moduleId()) == null) {
            throw new IllegalArgumentException(
                    "unknown module: "
                            + binding.moduleId());
        }
        if (settings.find(binding.settingId()) == null) {
            throw new IllegalArgumentException(
                    "unknown setting: "
                            + binding.settingId());
        }
        if (bySettingId.containsKey(
                binding.settingId())) {
            throw new IllegalArgumentException(
                    "setting already owned by a module: "
                            + binding.settingId());
        }

        final Entry entry =
                new Entry(
                        binding,
                        nextSequence++);
        bySettingId.put(
                binding.settingId(),
                entry);
        return new RegistrationImpl(
                this,
                entry);
    }

    public synchronized ModuleSettingBinding ownerOf(
            final String settingId) {
        final Entry entry =
                bySettingId.get(
                        Objects.requireNonNull(
                                settingId,
                                "settingId"));
        return entry == null
                ? null
                : entry.binding;
    }

    public synchronized List<ModuleSettingBinding> bindingsForModule(
            final String moduleId) {
        final String id =
                Objects.requireNonNull(
                        moduleId,
                        "moduleId");
        final List<Entry> entries =
                new ArrayList<Entry>();

        for (Entry entry : bySettingId.values()) {
            if (entry.binding.moduleId()
                    .equals(id)) {
                entries.add(entry);
            }
        }

        Collections.sort(
                entries,
                new Comparator<Entry>() {
                    @Override
                    public int compare(
                            final Entry left,
                            final Entry right) {
                        final int priority =
                                Integer.compare(
                                        left.binding.priority(),
                                        right.binding.priority());
                        if (priority != 0) {
                            return priority;
                        }
                        return Long.compare(
                                left.sequence,
                                right.sequence);
                    }
                });

        final List<ModuleSettingBinding> result =
                new ArrayList<ModuleSettingBinding>(
                        entries.size());
        for (Entry entry : entries) {
            result.add(entry.binding);
        }
        return Collections.unmodifiableList(result);
    }

    private synchronized void unregister(
            final Entry expected) {
        final Entry current =
                bySettingId.get(
                        expected.binding.settingId());
        if (current == expected) {
            bySettingId.remove(
                    expected.binding.settingId());
        }
    }

    public interface Registration extends AutoCloseable {
        ModuleSettingBinding binding();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final ModuleSettingRegistry registry;
        private final Entry entry;
        private boolean active = true;

        RegistrationImpl(
                final ModuleSettingRegistry registry,
                final Entry entry) {
            this.registry = registry;
            this.entry = entry;
        }

        @Override
        public ModuleSettingBinding binding() {
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
        private final ModuleSettingBinding binding;
        private final long sequence;

        Entry(
                final ModuleSettingBinding binding,
                final long sequence) {
            this.binding = binding;
            this.sequence = sequence;
        }
    }
}
