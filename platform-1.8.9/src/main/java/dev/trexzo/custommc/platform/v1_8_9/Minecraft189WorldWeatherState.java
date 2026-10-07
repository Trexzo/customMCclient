package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189WorldWeatherState {
    private boolean available;
    private boolean raining;
    private boolean thundering;

    public synchronized void update(
            final boolean nextRaining,
            final boolean nextThundering) {
        raining = nextRaining;
        thundering = nextThundering;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        raining = false;
        thundering = false;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                raining,
                thundering);
    }

    public static final class Snapshot {
        private final boolean available;
        private final boolean raining;
        private final boolean thundering;

        private Snapshot(
                final boolean available,
                final boolean raining,
                final boolean thundering) {
            this.available = available;
            this.raining = raining;
            this.thundering = thundering;
        }

        public boolean available() {
            return available;
        }

        public boolean raining() {
            return raining;
        }

        public boolean thundering() {
            return thundering;
        }
    }
}
