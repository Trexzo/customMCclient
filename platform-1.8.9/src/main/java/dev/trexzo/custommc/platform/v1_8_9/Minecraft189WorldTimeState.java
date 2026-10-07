package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189WorldTimeState {
    private static final long TICKS_PER_DAY = 24000L;
    private static final long DAY_START_OFFSET = 6000L;
    private static final long MINUTES_PER_DAY = 1440L;

    private boolean available;
    private long worldTime;

    public synchronized void update(
            final long nextWorldTime) {
        worldTime = nextWorldTime;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        worldTime = 0L;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                worldTime);
    }

    public static final class Snapshot {
        private final boolean available;
        private final long worldTime;

        private Snapshot(
                final boolean available,
                final long worldTime) {
            this.available = available;
            this.worldTime = worldTime;
        }

        public boolean available() {
            return available;
        }

        public long worldTime() {
            return worldTime;
        }

        public long timeOfDayTicks() {
            return Math.floorMod(
                    worldTime,
                    TICKS_PER_DAY);
        }

        public int hour() {
            return (int) (totalMinutes() / 60L);
        }

        public int minute() {
            return (int) (totalMinutes() % 60L);
        }

        private long totalMinutes() {
            final long shiftedTicks =
                    Math.floorMod(
                            timeOfDayTicks()
                                    + DAY_START_OFFSET,
                            TICKS_PER_DAY);
            return shiftedTicks
                    * MINUTES_PER_DAY
                    / TICKS_PER_DAY;
        }
    }
}
