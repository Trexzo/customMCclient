package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerHungerState {
    private boolean available;
    private int foodLevel;
    private float saturationLevel;

    public synchronized void update(
            final int nextFoodLevel,
            final float nextSaturationLevel) {
        if (nextFoodLevel < 0
                || nextFoodLevel > 20) {
            throw new IllegalArgumentException(
                    "foodLevel must be in range 0..20");
        }
        if (Float.isNaN(nextSaturationLevel)
                || Float.isInfinite(nextSaturationLevel)
                || nextSaturationLevel < 0.0F
                || nextSaturationLevel > 20.0F) {
            throw new IllegalArgumentException(
                    "saturationLevel must be finite and in range 0..20");
        }
        foodLevel = nextFoodLevel;
        saturationLevel = nextSaturationLevel;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        foodLevel = 0;
        saturationLevel = 0.0F;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                foodLevel,
                saturationLevel);
    }

    public static final class Snapshot {
        private final boolean available;
        private final int foodLevel;
        private final float saturationLevel;

        private Snapshot(
                final boolean available,
                final int foodLevel,
                final float saturationLevel) {
            this.available = available;
            this.foodLevel = foodLevel;
            this.saturationLevel = saturationLevel;
        }

        public boolean available() {
            return available;
        }

        public int foodLevel() {
            return foodLevel;
        }

        public float saturationLevel() {
            return saturationLevel;
        }
    }
}
