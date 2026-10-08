package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189NearestPlayerTargetState {
    private boolean available;
    private boolean found;
    private int entityIndex = -1;
    private double x;
    private double y;
    private double z;
    private double distanceSquared;

    public synchronized void update(
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds) {
        update(local, positions, kinds, 0.0D, Double.MAX_VALUE);
    }

    public synchronized void update(
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final double minimumDistance,
            final double maximumDistance) {
        if (!Double.isFinite(minimumDistance)
                || !Double.isFinite(maximumDistance)
                || minimumDistance < 0.0D
                || minimumDistance > maximumDistance
                || local == null
                || positions == null
                || kinds == null
                || !local.available()
                || !positions.available()
                || !kinds.available()
                || positions.entityCount()
                != kinds.entityCount()) {
            clear();
            return;
        }

        final double minimumDistanceSquared =
                minimumDistance * minimumDistance;
        final double maximumDistanceSquared =
                maximumDistance * maximumDistance;
        int bestIndex = -1;
        double bestDistanceSquared =
                Double.POSITIVE_INFINITY;
        double bestX = 0.0D;
        double bestY = 0.0D;
        double bestZ = 0.0D;

        for (int index = 0;
                index < positions.entityCount();
                index++) {
            if (!kinds.player(index)
                    || kinds.localPlayer(index)) {
                continue;
            }

            final double candidateX =
                    positions.x(index);
            final double candidateY =
                    positions.y(index);
            final double candidateZ =
                    positions.z(index);
            final double dx =
                    candidateX - local.x();
            final double dy =
                    candidateY - local.y();
            final double dz =
                    candidateZ - local.z();
            final double candidateDistanceSquared =
                    dx * dx
                            + dy * dy
                            + dz * dz;

            if (candidateDistanceSquared >= minimumDistanceSquared
                    && candidateDistanceSquared <= maximumDistanceSquared
                    && candidateDistanceSquared < bestDistanceSquared) {
                bestIndex = index;
                bestDistanceSquared =
                        candidateDistanceSquared;
                bestX = candidateX;
                bestY = candidateY;
                bestZ = candidateZ;
            }
        }

        available = true;
        found = bestIndex >= 0;
        entityIndex = bestIndex;
        if (found) {
            x = bestX;
            y = bestY;
            z = bestZ;
            distanceSquared =
                    bestDistanceSquared;
        } else {
            x = 0.0D;
            y = 0.0D;
            z = 0.0D;
            distanceSquared = 0.0D;
        }
    }

    public synchronized void clear() {
        available = false;
        found = false;
        entityIndex = -1;
        x = 0.0D;
        y = 0.0D;
        z = 0.0D;
        distanceSquared = 0.0D;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                found,
                entityIndex,
                x,
                y,
                z,
                distanceSquared);
    }

    public static final class Snapshot {
        private final boolean available;
        private final boolean found;
        private final int entityIndex;
        private final double x;
        private final double y;
        private final double z;
        private final double distanceSquared;

        private Snapshot(
                final boolean available,
                final boolean found,
                final int entityIndex,
                final double x,
                final double y,
                final double z,
                final double distanceSquared) {
            this.available = available;
            this.found = found;
            this.entityIndex = entityIndex;
            this.x = x;
            this.y = y;
            this.z = z;
            this.distanceSquared =
                    distanceSquared;
        }

        public boolean available() {
            return available;
        }

        public boolean found() {
            return found;
        }

        public int entityIndex() {
            return entityIndex;
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

        public double distanceSquared() {
            return distanceSquared;
        }

        public double distance() {
            return Math.sqrt(
                    distanceSquared);
        }
    }
}
