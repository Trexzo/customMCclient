package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189MovementSpeedTracker {
    private static final double TICKS_PER_SECOND = 20.0D;

    private boolean previousAvailable;
    private double previousX;
    private double previousZ;
    private boolean speedAvailable;
    private double blocksPerSecond;

    public synchronized void sample(
            final double x,
            final double z) {
        requireFinite(
                x,
                "x");
        requireFinite(
                z,
                "z");

        if (previousAvailable) {
            final double deltaX =
                    x - previousX;
            final double deltaZ =
                    z - previousZ;
            blocksPerSecond =
                    Math.sqrt(
                            deltaX * deltaX
                                    + deltaZ * deltaZ)
                            * TICKS_PER_SECOND;
            speedAvailable = true;
        } else {
            blocksPerSecond = 0.0D;
            speedAvailable = false;
        }

        previousX = x;
        previousZ = z;
        previousAvailable = true;
    }

    public synchronized void clear() {
        previousAvailable = false;
        previousX = 0.0D;
        previousZ = 0.0D;
        speedAvailable = false;
        blocksPerSecond = 0.0D;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                speedAvailable,
                blocksPerSecond);
    }

    private static void requireFinite(
            final double value,
            final String name) {
        if (Double.isNaN(value)
                || Double.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }

    public static final class Snapshot {
        private final boolean available;
        private final double blocksPerSecond;

        private Snapshot(
                final boolean available,
                final double blocksPerSecond) {
            this.available = available;
            this.blocksPerSecond = blocksPerSecond;
        }

        public boolean available() {
            return available;
        }

        public double blocksPerSecond() {
            return blocksPerSecond;
        }
    }
}
