package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189MovementSpeedTracker {
    private static final double TICKS_PER_SECOND = 20.0D;

    private boolean previousAvailable;
    private double previousX;
    private double previousZ;
    private boolean speedAvailable;
    private double blocksPerSecond;
    private double peakBlocksPerSecond;

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
            // Squaring valid real player coordinates first overflows
            // to Infinity even when the actual horizontal distance is
            // representable (e.g., deltaX=1e200). Math.hypot preserves
            // that genuine mapped position delta without bogus Infinity.
            // Extremely distant, still-finite player coordinates may
            // overflow subtraction or the final ticks/sec conversion:
            // expose no speed sample rather than nonfinite HUD text.
            final double distance = Math.hypot(deltaX, deltaZ);
            final double candidate = distance * TICKS_PER_SECOND;
            speedAvailable = Double.isFinite(candidate);
            blocksPerSecond = speedAvailable ? candidate : 0.0D;
            if (speedAvailable) {
                peakBlocksPerSecond = Math.max(
                        peakBlocksPerSecond,
                        blocksPerSecond);
            }
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
        peakBlocksPerSecond = 0.0D;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                speedAvailable,
                blocksPerSecond,
                peakBlocksPerSecond);
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
        private final double peakBlocksPerSecond;

        private Snapshot(
                final boolean available,
                final double blocksPerSecond,
                final double peakBlocksPerSecond) {
            this.available = available;
            this.blocksPerSecond = blocksPerSecond;
            this.peakBlocksPerSecond = peakBlocksPerSecond;
        }

        public boolean available() {
            return available;
        }

        public double blocksPerSecond() {
            return blocksPerSecond;
        }

        public double peakBlocksPerSecond() {
            return peakBlocksPerSecond;
        }
    }
}
