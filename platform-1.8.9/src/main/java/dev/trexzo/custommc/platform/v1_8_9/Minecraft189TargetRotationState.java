package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189TargetRotationState {
    private boolean available;
    private int entityIndex = -1;
    private float yaw;
    private float pitch;
    private double distanceSquared;

    public synchronized void update(
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189NearestPlayerTargetState.Snapshot target) {
        if (local == null
                || target == null
                || !local.available()
                || !target.available()
                || !target.found()) {
            clear();
            return;
        }

        final double dx =
                target.x() - local.x();
        final double dy =
                target.y() - local.y();
        final double dz =
                target.z() - local.z();

        if (!finite(dx)
                || !finite(dy)
                || !finite(dz)) {
            clear();
            return;
        }

        final double horizontal =
                Math.hypot(
                        dx,
                        dz);
        if (!finite(horizontal)
                || horizontal == 0.0D) {
            clear();
            return;
        }

        double nextYaw =
                Math.toDegrees(
                        Math.atan2(
                                dz,
                                dx))
                        - 90.0D;
        nextYaw = normalizeYaw(nextYaw);

        final double nextPitch =
                -Math.toDegrees(
                        Math.atan2(
                                dy,
                                horizontal));

        if (!finite(nextYaw)
                || !finite(nextPitch)) {
            clear();
            return;
        }

        final double nextDistanceSquared =
                target.distanceSquared();
        if (!finite(nextDistanceSquared)
                || nextDistanceSquared < 0.0D) {
            clear();
            return;
        }

        entityIndex =
                target.entityIndex();
        yaw = (float) nextYaw;
        pitch = (float) nextPitch;
        distanceSquared = nextDistanceSquared;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        entityIndex = -1;
        yaw = 0.0F;
        pitch = 0.0F;
        distanceSquared = 0.0D;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                entityIndex,
                yaw,
                pitch,
                distanceSquared);
    }

    private static double normalizeYaw(
            final double yaw) {
        double normalized =
                yaw % 360.0D;
        if (normalized < -180.0D) {
            normalized += 360.0D;
        } else if (normalized >= 180.0D) {
            normalized -= 360.0D;
        }
        return normalized;
    }

    private static boolean finite(
            final double value) {
        return !Double.isNaN(value)
                && !Double.isInfinite(value);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int entityIndex;
        private final float yaw;
        private final float pitch;
        private final double distanceSquared;

        private Snapshot(
                final boolean available,
                final int entityIndex,
                final float yaw,
                final float pitch,
                final double distanceSquared) {
            this.available = available;
            this.entityIndex = entityIndex;
            this.yaw = yaw;
            this.pitch = pitch;
            this.distanceSquared = distanceSquared;
        }

        public boolean available() {
            return available;
        }

        public int entityIndex() {
            return entityIndex;
        }

        public float yaw() {
            return yaw;
        }

        public float pitch() {
            return pitch;
        }

        public double distanceSquared() {
            return distanceSquared;
        }

        public double distance() {
            return Math.sqrt(
                    distanceSquared);
        }
    }
}
