package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerPositionState {
    private boolean available;
    private double x;
    private double y;
    private double z;

    public synchronized void update(
            final double nextX,
            final double nextY,
            final double nextZ) {
        requireFinite(
                nextX,
                "x");
        requireFinite(
                nextY,
                "y");
        requireFinite(
                nextZ,
                "z");
        x = nextX;
        y = nextY;
        z = nextZ;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        x = 0.0D;
        y = 0.0D;
        z = 0.0D;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                x,
                y,
                z);
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
        private final double x;
        private final double y;
        private final double z;

        private Snapshot(
                final boolean available,
                final double x,
                final double y,
                final double z) {
            this.available = available;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean available() {
            return available;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double z() {
            return z;
        }
    }
}
