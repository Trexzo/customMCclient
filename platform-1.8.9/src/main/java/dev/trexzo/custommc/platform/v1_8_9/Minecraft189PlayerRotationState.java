package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerRotationState {
    private boolean available;
    private float yaw;
    private float pitch;

    public synchronized void update(
            final float nextYaw,
            final float nextPitch) {
        if (Float.isNaN(nextYaw)
                || Float.isInfinite(nextYaw)) {
            throw new IllegalArgumentException(
                    "yaw must be finite");
        }
        if (Float.isNaN(nextPitch)
                || Float.isInfinite(nextPitch)) {
            throw new IllegalArgumentException(
                    "pitch must be finite");
        }
        yaw = nextYaw;
        pitch = nextPitch;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        yaw = 0.0F;
        pitch = 0.0F;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                yaw,
                pitch);
    }

    public static final class Snapshot {
        private final boolean available;
        private final float yaw;
        private final float pitch;

        private Snapshot(
                final boolean available,
                final float yaw,
                final float pitch) {
            this.available = available;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public boolean available() {
            return available;
        }

        public float yaw() {
            return yaw;
        }

        public float pitch() {
            return pitch;
        }
    }
}
