package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable-copy snapshot for mapped loadedEntityList combat evidence.
 * Nonliving/unknown = -1. Nonnegative codes carry alive and hurt-time.
 */
public final class Minecraft189WorldEntityCombatState {
    public static final int UNKNOWN = -1;
    public static final int MAX_HURT_TIME = 127;
    private boolean available;
    private int[] packed = new int[0];

    public synchronized void update(final int[] next) {
        Objects.requireNonNull(next, "next");
        final int[] copy = Arrays.copyOf(next, next.length);
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] < UNKNOWN || copy[i] > (MAX_HURT_TIME * 2 + 1)) {
                throw new IllegalArgumentException(
                        "invalid combat state at entity index " + i);
            }
        }
        packed = copy;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        packed = new int[0];
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(available, packed);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int[] packed;

        private Snapshot(final boolean available, final int[] source) {
            this.available = available;
            this.packed = Arrays.copyOf(source, source.length);
        }

        public boolean available() { return available; }
        public int entityCount() { return packed.length; }
        public boolean known(final int index) {
            return available && index >= 0 && index < packed.length
                    && packed[index] != UNKNOWN;
        }
        public boolean alive(final int index) {
            return known(index) && (packed[index] & 1) != 0;
        }
        /** Returns -1 when the entity is unknown or not in this snapshot. */
        public int hurtTime(final int index) {
            return known(index) ? packed[index] >>> 1 : UNKNOWN;
        }
        public int raw(final int index) {
            return known(index) ? packed[index] : UNKNOWN;
        }
    }
}
