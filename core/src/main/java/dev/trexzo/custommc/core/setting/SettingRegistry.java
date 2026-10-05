package dev.trexzo.custommc.core.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class SettingRegistry {
    private final Map<String, Setting<?>> settings =
            new LinkedHashMap<String, Setting<?>>();

    public synchronized void register(final Setting<?> setting) {
        Objects.requireNonNull(setting, "setting");
        if (settings.containsKey(setting.id())) {
            throw new IllegalArgumentException(
                    "duplicate setting id: " + setting.id());
        }
        settings.put(setting.id(), setting);
    }

    public synchronized Setting<?> find(final String id) {
        return settings.get(Objects.requireNonNull(id, "id"));
    }

    public synchronized List<Setting<?>> snapshot() {
        return Collections.unmodifiableList(
                new ArrayList<Setting<?>>(settings.values()));
    }

    public synchronized Map<String, String> snapshotEncoded() {
        final Map<String, String> snapshot =
                new TreeMap<String, String>();

        for (Setting<?> setting : settings.values()) {
            if (setting.isPersistent()) {
                snapshot.put(setting.id(), setting.encode());
            }
        }

        return Collections.unmodifiableMap(snapshot);
    }

    public synchronized void applyEncoded(
            final Map<String, String> encoded,
            final UnknownSettingPolicy unknownPolicy) {
        Objects.requireNonNull(encoded, "encoded");
        Objects.requireNonNull(unknownPolicy, "unknownPolicy");

        final List<Runnable> prepared =
                new ArrayList<Runnable>();

        for (Map.Entry<String, String> entry : encoded.entrySet()) {
            final Setting<?> setting = settings.get(entry.getKey());
            if (setting == null) {
                if (unknownPolicy == UnknownSettingPolicy.REJECT) {
                    throw new IllegalArgumentException(
                            "unknown setting: " + entry.getKey());
                }
                continue;
            }
            if (!setting.isPersistent()) {
                throw new IllegalArgumentException(
                        "setting is not persistent: " + entry.getKey());
            }

            prepared.add(prepare(setting, entry.getValue()));
        }

        for (Runnable change : prepared) {
            change.run();
        }
    }

    private static <T> Runnable prepare(
            final Setting<T> setting,
            final String encoded) {
        final T decoded = setting.decode(encoded);
        return new Runnable() {
            @Override
            public void run() {
                setting.set(decoded);
            }
        };
    }
}
