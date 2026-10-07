package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Arrays;
import java.util.Objects;

public final class Minecraft189WorldEntityPositionState {
    private static final int VALUES_PER_ENTITY = 3;

    private boolean available;
    private double[] packedPositions =
            new double[0];

    public synchronized void update(
            final double[] nextPackedPositions) {
        final double[] next =
                Objects.requireNonNull(
                        nextPackedPositions,
                        "nextPackedPositions");
        if (next.length % VALUES_PER_ENTITY != 0) {
            throw new IllegalArgumentException(
                    "packed entity positions must contain x/y/z triples");
        }

        final double[] copy =
                Arrays.copyOf(
                        next,
                        next.length);
        for (int index = 0;
                index < copy.length;
                index++) {
            requireFinite(
                    copy[index],
                    index);
        }

        packedPositions = copy;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        packedPositions =
                new double[0];
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                packedPositions);
    }

    private static void requireFinite(
            final double value,
            final int index) {
        if (Double.isNaN(value)
                || Double.isInfinite(value)) {
            throw new IllegalArgumentException(
                    "packed entity position at index "
                            + index
                            + " must be finite");
        }
    }

    public static final class Snapshot {
        private final boolean available;
        private final double[] packedPositions;

        private Snapshot(
                final boolean available,
                final double[] packedPositions) {
            this.available = available;
            this.packedPositions =
                    Arrays.copyOf(
                            packedPositions,
                            packedPositions.length);
        }

        public boolean available() {
            return available;
        }

        public int entityCount() {
            return packedPositions.length
                    / VALUES_PER_ENTITY;
        }

        public double x(
                final int entityIndex) {
            return coordinate(
                    entityIndex,
                    0);
        }

        public double y(
                final int entityIndex) {
            return coordinate(
                    entityIndex,
                    1);
        }

        public double z(
                final int entityIndex) {
            return coordinate(
                    entityIndex,
                    2);
        }

        public double[] packedPositions() {
            return Arrays.copyOf(
                    packedPositions,
                    packedPositions.length);
        }

        private double coordinate(
                final int entityIndex,
                final int offset) {
            if (entityIndex < 0
                    || entityIndex >= entityCount()) {
                throw new IndexOutOfBoundsException(
                        "entityIndex="
                                + entityIndex
                                + ", entityCount="
                                + entityCount());
            }
            return packedPositions[
                    entityIndex * VALUES_PER_ENTITY
                            + offset];
        }
    }
}
