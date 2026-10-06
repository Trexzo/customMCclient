package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerHealthState {
    private boolean available;
    private float health;
    private float maxHealth;

    public synchronized void update(
            final float nextHealth,
            final float nextMaxHealth) {
        requireFinite(
                nextHealth,
                "health");
        requireFinite(
                nextMaxHealth,
                "maxHealth");
        if (nextMaxHealth <= 0.0F) {
            throw new IllegalArgumentException(
                    "maxHealth must be positive");
        }
        health = nextHealth;
        maxHealth = nextMaxHealth;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        health = 0.0F;
        maxHealth = 0.0F;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                health,
                maxHealth);
    }

    private static void requireFinite(
            final float value,
            final String name) {
        if (Float.isNaN(value)
                || Float.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }

    public static final class Snapshot {
        private final boolean available;
        private final float health;
        private final float maxHealth;

        private Snapshot(
                final boolean available,
                final float health,
                final float maxHealth) {
            this.available = available;
            this.health = health;
            this.maxHealth = maxHealth;
        }

        public boolean available() {
            return available;
        }

        public float health() {
            return health;
        }

        public float maxHealth() {
            return maxHealth;
        }
    }
}
