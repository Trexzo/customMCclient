package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerHurtTimeState {
    private boolean available;
    private int hurtTime;

    public synchronized void update(
            final int nextHurtTime) {
        if (nextHurtTime < 0) {
            throw new IllegalArgumentException(
                    "hurtTime must not be negative");
        }
        hurtTime = nextHurtTime;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        hurtTime = 0;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                hurtTime);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int hurtTime;

        private Snapshot(
                final boolean available,
                final int hurtTime) {
            this.available = available;
            this.hurtTime = hurtTime;
        }

        public boolean available() {
            return available;
        }

        public int hurtTime() {
            return hurtTime;
        }
    }
}
