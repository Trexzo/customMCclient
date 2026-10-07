package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerExperienceState {
    private boolean available;
    private int level;
    private int total;
    private float progress;
    private int barCap;

    public synchronized void update(
            final int nextLevel,
            final int nextTotal,
            final float nextProgress,
            final int nextBarCap) {
        if (nextLevel < 0) {
            throw new IllegalArgumentException(
                    "level must be non-negative");
        }
        if (nextTotal < 0) {
            throw new IllegalArgumentException(
                    "total must be non-negative");
        }
        if (Float.isNaN(nextProgress)
                || Float.isInfinite(nextProgress)
                || nextProgress < 0.0F
                || nextProgress > 1.0F) {
            throw new IllegalArgumentException(
                    "progress must be finite and in range 0..1");
        }
        if (nextBarCap <= 0) {
            throw new IllegalArgumentException(
                    "barCap must be positive");
        }
        level = nextLevel;
        total = nextTotal;
        progress = nextProgress;
        barCap = nextBarCap;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        level = 0;
        total = 0;
        progress = 0.0F;
        barCap = 0;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                level,
                total,
                progress,
                barCap);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int level;
        private final int total;
        private final float progress;
        private final int barCap;

        private Snapshot(
                final boolean available,
                final int level,
                final int total,
                final float progress,
                final int barCap) {
            this.available = available;
            this.level = level;
            this.total = total;
            this.progress = progress;
            this.barCap = barCap;
        }

        public boolean available() {
            return available;
        }

        public int level() {
            return level;
        }

        public int total() {
            return total;
        }

        public float progress() {
            return progress;
        }

        public int barCap() {
            return barCap;
        }

        public int progressPoints() {
            return Math.round(
                    progress * barCap);
        }
    }
}
