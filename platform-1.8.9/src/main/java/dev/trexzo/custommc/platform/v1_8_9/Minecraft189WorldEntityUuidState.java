package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

/**
 * Immutable-copy identity authority for one native loadedEntityList sample.
 * Never invent or collapse missing/duplicate UUIDs into positional identity.
 */
public final class Minecraft189WorldEntityUuidState {
    private boolean available;
    private UUID[] ids = new UUID[0];

    public synchronized boolean update(final UUID[] supplied) {
        if (supplied == null) {
            clear();
            return false;
        }
        final UUID[] copy = Arrays.copyOf(supplied, supplied.length);
        final HashSet<UUID> unique = new HashSet<UUID>();
        for (UUID id : copy) {
            if (id == null || !unique.add(id)) {
                clear();
                return false;
            }
        }
        ids = copy;
        available = true;
        return true;
    }

    public synchronized void clear() {
        ids = new UUID[0];
        available = false;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(available, ids);
    }

    public static final class Snapshot {
        private final boolean available;
        private final UUID[] ids;
        private Snapshot(final boolean available, final UUID[] ids) {
            this.available = available;
            this.ids = Arrays.copyOf(ids, ids.length);
        }
        public boolean available() { return available; }
        public int entityCount() { return ids.length; }
        public UUID at(final int index) {
            return available && index >= 0 && index < ids.length ? ids[index] : null;
        }
        /** Confirms the list is still exactly the same sequence of identities. */
        public boolean matches(final UUID[] current) {
            return available && current != null && Arrays.equals(ids, current);
        }
    }
}
