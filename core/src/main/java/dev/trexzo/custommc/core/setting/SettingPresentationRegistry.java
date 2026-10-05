package dev.trexzo.custommc.core.setting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class SettingPresentationRegistry {
    private final Map<String, SettingDescriptor> descriptors =
            new LinkedHashMap<String, SettingDescriptor>();

    public synchronized Registration register(
            final SettingDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");

        if (descriptors.containsKey(
                descriptor.settingId())) {
            throw new IllegalArgumentException(
                    "duplicate setting descriptor: "
                            + descriptor.settingId());
        }

        descriptors.put(
                descriptor.settingId(),
                descriptor);
        return new RegistrationImpl(
                this,
                descriptor);
    }

    public synchronized SettingDescriptor find(
            final String settingId) {
        return descriptors.get(
                Objects.requireNonNull(
                        settingId,
                        "settingId"));
    }

    private synchronized void unregister(
            final SettingDescriptor expected) {
        final SettingDescriptor current =
                descriptors.get(
                        expected.settingId());
        if (current == expected) {
            descriptors.remove(
                    expected.settingId());
        }
    }

    public interface Registration extends AutoCloseable {
        SettingDescriptor descriptor();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final SettingPresentationRegistry registry;
        private final SettingDescriptor descriptor;
        private boolean active = true;

        RegistrationImpl(
                final SettingPresentationRegistry registry,
                final SettingDescriptor descriptor) {
            this.registry = registry;
            this.descriptor = descriptor;
        }

        @Override
        public SettingDescriptor descriptor() {
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
