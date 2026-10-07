package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Arrays;
import java.util.Objects;

public final class Minecraft189WorldEntityKindState {
    public static final int LIVING =
            1;
    public static final int PLAYER =
            1 << 1;
    public static final int LOCAL_PLAYER =
            1 << 2;

    private static final int ALLOWED_MASK =
            LIVING
                    | PLAYER
                    | LOCAL_PLAYER;

    private boolean available;
    private int[] kinds =
            new int[0];

    public synchronized void update(
            final int[] nextKinds) {
        final int[] next =
                Objects.requireNonNull(
                        nextKinds,
                        "nextKinds");
        final int[] copy =
                Arrays.copyOf(
                        next,
                        next.length);
        for (int index = 0;
                index < copy.length;
                index++) {
            validateKind(
                    copy[index],
                    index);
        }
        kinds = copy;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        kinds =
                new int[0];
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                kinds);
    }

    private static void validateKind(
            final int kind,
            final int index) {
        if ((kind & ~ALLOWED_MASK) != 0) {
            throw new IllegalArgumentException(
                    "entity kind at index "
                            + index
                            + " contains unknown flags");
        }
        if ((kind & PLAYER) != 0
                && (kind & LIVING) == 0) {
            throw new IllegalArgumentException(
                    "player entity kind at index "
                            + index
                            + " must also be living");
        }
        if ((kind & LOCAL_PLAYER) != 0
                && (kind & (PLAYER | LIVING))
                != (PLAYER | LIVING)) {
            throw new IllegalArgumentException(
                    "local-player entity kind at index "
                            + index
                            + " must also be player and living");
        }
    }

    public static final class Snapshot {
        private final boolean available;
        private final int[] kinds;

        private Snapshot(
                final boolean available,
                final int[] kinds) {
            this.available = available;
            this.kinds =
                    Arrays.copyOf(
                            kinds,
                            kinds.length);
        }

        public boolean available() {
            return available;
        }

        public int entityCount() {
            return kinds.length;
        }

        public int kindBits(
                final int entityIndex) {
            requireIndex(
                    entityIndex);
            return kinds[entityIndex];
        }

        public boolean living(
                final int entityIndex) {
            return hasFlag(
                    entityIndex,
                    LIVING);
        }

        public boolean player(
                final int entityIndex) {
            return hasFlag(
                    entityIndex,
                    PLAYER);
        }

        public boolean localPlayer(
                final int entityIndex) {
            return hasFlag(
                    entityIndex,
                    LOCAL_PLAYER);
        }

        public int[] kindBits() {
            return Arrays.copyOf(
                    kinds,
                    kinds.length);
        }

        private boolean hasFlag(
                final int entityIndex,
                final int flag) {
            return (kindBits(entityIndex)
                    & flag) != 0;
        }

        private void requireIndex(
                final int entityIndex) {
            if (entityIndex < 0
                    || entityIndex >= kinds.length) {
                throw new IndexOutOfBoundsException(
                        "entityIndex="
                                + entityIndex
                                + ", entityCount="
                                + kinds.length);
            }
        }
    }
}
