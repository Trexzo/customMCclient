package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;

public final class Minecraft189HeldItemState {
    private boolean available;
    private String displayName;
    private int stackSize;
    private int itemDamage;
    private int maxDamage;

    public synchronized void update(
            final String nextDisplayName,
            final int nextStackSize,
            final int nextItemDamage,
            final int nextMaxDamage) {
        final String normalizedName =
                Objects.requireNonNull(
                        nextDisplayName,
                        "nextDisplayName")
                        .trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException(
                    "displayName must not be blank");
        }
        if (nextStackSize <= 0) {
            throw new IllegalArgumentException(
                    "stackSize must be positive");
        }
        if (nextItemDamage < 0) {
            throw new IllegalArgumentException(
                    "itemDamage must be non-negative");
        }
        if (nextMaxDamage < 0) {
            throw new IllegalArgumentException(
                    "maxDamage must be non-negative");
        }

        displayName = normalizedName;
        stackSize = nextStackSize;
        itemDamage = nextItemDamage;
        maxDamage = nextMaxDamage;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        displayName = null;
        stackSize = 0;
        itemDamage = 0;
        maxDamage = 0;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                displayName,
                stackSize,
                itemDamage,
                maxDamage);
    }

    public static final class Snapshot {
        private final boolean available;
        private final String displayName;
        private final int stackSize;
        private final int itemDamage;
        private final int maxDamage;

        private Snapshot(
                final boolean available,
                final String displayName,
                final int stackSize,
                final int itemDamage,
                final int maxDamage) {
            this.available = available;
            this.displayName = displayName;
            this.stackSize = stackSize;
            this.itemDamage = itemDamage;
            this.maxDamage = maxDamage;
        }

        public boolean available() {
            return available;
        }

        public String displayName() {
            return displayName;
        }

        public int stackSize() {
            return stackSize;
        }

        public int itemDamage() {
            return itemDamage;
        }

        public int maxDamage() {
            return maxDamage;
        }

        public boolean damageable() {
            return maxDamage > 0;
        }

        public int durabilityRemaining() {
            if (!damageable()) {
                return 0;
            }
            return Math.max(
                    0,
                    maxDamage - itemDamage);
        }
    }
}
