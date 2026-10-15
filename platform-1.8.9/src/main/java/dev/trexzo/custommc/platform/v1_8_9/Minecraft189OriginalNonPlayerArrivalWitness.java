package dev.trexzo.custommc.platform.v1_8_9;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Acceptance-only immutable-identity observation of ORIGINAL Minecraft
 * loadedEntityList snapshots. Distinct new nonplayer UUIDs prove new native
 * entities even when older native nonplayers vanish in the same interval.
 *
 * An old count delta alone is NOT sufficient evidence of actual arrival:
 * comparing counts can spuriously lose +4 during unrelated unload/despawn.
 * A server summon command alone is also never proof of host observation.
 * No World, Entity, player, packet or mutable Minecraft object is retained.
 */
final class Minecraft189OriginalNonPlayerArrivalWitness {
    private final Set<UUID> baseline = new HashSet<UUID>();
    private boolean captured;

    boolean captureBaseline(final Minecraft189WorldEntityUuidState.Snapshot ids,
            final Minecraft189WorldEntityKindState.Snapshot kinds) {
        reset();
        if (!valid(ids, kinds)) return false;
        for (int i = 0; i < ids.entityCount(); i++) {
            if (!kinds.player(i)) {
                final UUID uuid = ids.at(i);
                if (uuid == null || !baseline.add(uuid)) {
                    reset();
                    return false;
                }
            }
        }
        captured = true;
        return true;
    }

    int countNewNativeNonPlayers(
            final Minecraft189WorldEntityUuidState.Snapshot ids,
            final Minecraft189WorldEntityKindState.Snapshot kinds) {
        if (!captured || !valid(ids, kinds)) return -1;
        int newlyArrived = 0;
        final Set<UUID> current = new HashSet<UUID>();
        for (int i = 0; i < ids.entityCount(); i++) {
            final UUID uuid = ids.at(i);
            if (uuid == null || !current.add(uuid))
                return -1;
            if (!kinds.player(i) && !baseline.contains(uuid))
                ++newlyArrived;
        }
        return newlyArrived;
    }

    boolean captured() { return captured; }

    int baselineSize() { return captured ? baseline.size() : -1; }

    void reset() {
        baseline.clear();
        captured = false;
    }

    private static boolean valid(
            final Minecraft189WorldEntityUuidState.Snapshot ids,
            final Minecraft189WorldEntityKindState.Snapshot kinds) {
        return ids != null && kinds != null && ids.available()
                && kinds.available()
                && ids.entityCount() == kinds.entityCount();
    }
}
