package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerRotationState {
    private boolean available;
    private float yaw;

    public synchronized void update(
            final float nextYaw) {
        if (Float.isNaN(nextYaw)
                || Float.isInfinite(nextYaw)) {
            throw new IllegalArgumentException(
                    "yaw must be finite");
        }
        yaw = nextYaw;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        yaw = 0.0F;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                yaw);
    }

    public static final class Snapshot {
        private final boolean available;
        private final float yaw;

        private Snapshot(
                final boolean available,
                final float yaw) {
            this.available = available;
            this.yaw = yaw;
        }

        public boolean available() {
            return available;
        }

        public float yaw() {
            return yaw;
        }
    }
}
