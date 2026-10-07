package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerDimensionState {
    private boolean available;
    private int dimensionId;

    public synchronized void update(
            final int nextDimensionId) {
        dimensionId = nextDimensionId;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        dimensionId = 0;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                dimensionId);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int dimensionId;

        private Snapshot(
                final boolean available,
                final int dimensionId) {
            this.available = available;
            this.dimensionId = dimensionId;
        }

        public boolean available() {
            return available;
        }

        public int dimensionId() {
            return dimensionId;
        }
    }
}
