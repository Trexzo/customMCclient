package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189HotbarSlotState {
    public static final int SLOT_COUNT = 9;

    private boolean available;
    private int zeroBasedSlot;

    public synchronized void update(
            final int nextZeroBasedSlot) {
        if (nextZeroBasedSlot < 0
                || nextZeroBasedSlot >= SLOT_COUNT) {
            throw new IllegalArgumentException(
                    "hotbar slot must be in [0,8]");
        }
        zeroBasedSlot = nextZeroBasedSlot;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        zeroBasedSlot = 0;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                zeroBasedSlot);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int zeroBasedSlot;

        private Snapshot(
                final boolean available,
                final int zeroBasedSlot) {
            this.available = available;
            this.zeroBasedSlot = zeroBasedSlot;
        }

        public boolean available() {
            return available;
        }

        public int zeroBasedSlot() {
            return zeroBasedSlot;
        }

        public int displaySlot() {
            return zeroBasedSlot + 1;
        }
    }
}
