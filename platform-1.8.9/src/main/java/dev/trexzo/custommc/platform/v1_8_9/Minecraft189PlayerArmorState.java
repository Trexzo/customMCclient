package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerArmorState {
    public static final int BOOTS_BIT = 1;
    public static final int LEGGINGS_BIT = 1 << 1;
    public static final int CHESTPLATE_BIT = 1 << 2;
    public static final int HELMET_BIT = 1 << 3;
    private static final int ALL_BITS =
            BOOTS_BIT
                    | LEGGINGS_BIT
                    | CHESTPLATE_BIT
                    | HELMET_BIT;

    private boolean available;
    private int mask;

    public synchronized void update(
            final boolean boots,
            final boolean leggings,
            final boolean chestplate,
            final boolean helmet) {
        int nextMask = 0;
        if (boots) {
            nextMask |= BOOTS_BIT;
        }
        if (leggings) {
            nextMask |= LEGGINGS_BIT;
        }
        if (chestplate) {
            nextMask |= CHESTPLATE_BIT;
        }
        if (helmet) {
            nextMask |= HELMET_BIT;
        }
        mask = nextMask;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        mask = 0;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                mask);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int mask;

        private Snapshot(
                final boolean available,
                final int mask) {
            if ((mask & ~ALL_BITS) != 0) {
                throw new IllegalArgumentException(
                        "armor mask contains unsupported bits");
            }
            this.available = available;
            this.mask = mask;
        }

        public boolean available() {
            return available;
        }

        public int mask() {
            return mask;
        }

        public boolean boots() {
            return (mask & BOOTS_BIT) != 0;
        }

        public boolean leggings() {
            return (mask & LEGGINGS_BIT) != 0;
        }

        public boolean chestplate() {
            return (mask & CHESTPLATE_BIT) != 0;
        }

        public boolean helmet() {
            return (mask & HELMET_BIT) != 0;
        }

        public int equippedCount() {
            return Integer.bitCount(mask);
        }
    }
}
