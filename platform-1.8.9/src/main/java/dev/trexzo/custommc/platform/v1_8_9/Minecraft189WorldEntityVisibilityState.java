package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Arrays;

/** One defensive, exact loadedEntityList snapshot of native LOS results. */
public final class Minecraft189WorldEntityVisibilityState {
    private boolean available;
    private int[] values = new int[0];

    public synchronized boolean update(final int[] raw) {
        if (raw == null) {
            clear();
            return false;
        }
        for (int value : raw) {
            if (value != -1 && value != 0 && value != 1) {
                clear();
                return false;
            }
        }
        values = Arrays.copyOf(raw, raw.length);
        available = true;
        return true;
    }

    public synchronized void clear() {
        values = new int[0];
        available = false;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(available, values);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int[] values;

        private Snapshot(final boolean available, final int[] values) {
            this.available = available;
            this.values = Arrays.copyOf(values, values.length);
        }

        public boolean available() { return available; }
        public int entityCount() { return values.length; }
        public boolean visible(final int index) {
            return available && index >= 0 && index < values.length
                    && values[index] == 1;
        }
        public boolean matches(final int[] live) {
            return available && live != null && Arrays.equals(values, live);
        }
    }
}
