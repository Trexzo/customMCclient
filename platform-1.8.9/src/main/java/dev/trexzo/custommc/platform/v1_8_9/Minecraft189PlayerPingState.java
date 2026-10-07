package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerPingState {
    private boolean available;
    private int milliseconds;

    public synchronized void update(
            final int nextMilliseconds) {
        if (nextMilliseconds < 0) {
            throw new IllegalArgumentException(
                    "milliseconds must be non-negative");
        }
        milliseconds = nextMilliseconds;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        milliseconds = 0;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                milliseconds);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int milliseconds;

        private Snapshot(
                final boolean available,
                final int milliseconds) {
            this.available = available;
            this.milliseconds = milliseconds;
        }

        public boolean available() {
            return available;
        }

        public int milliseconds() {
            return milliseconds;
        }
    }
}
