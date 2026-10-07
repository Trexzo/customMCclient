package dev.trexzo.custommc.platform.v1_8_9;

public final class Minecraft189PlayerMovementState {
    private boolean available;
    private boolean onGround;
    private boolean sneaking;
    private boolean sprinting;

    public synchronized void update(
            final boolean nextOnGround,
            final boolean nextSneaking,
            final boolean nextSprinting) {
        onGround = nextOnGround;
        sneaking = nextSneaking;
        sprinting = nextSprinting;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        onGround = false;
        sneaking = false;
        sprinting = false;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                onGround,
                sneaking,
                sprinting);
    }

    public static final class Snapshot {
        private final boolean available;
        private final boolean onGround;
        private final boolean sneaking;
        private final boolean sprinting;

        private Snapshot(
                final boolean available,
                final boolean onGround,
                final boolean sneaking,
                final boolean sprinting) {
            this.available = available;
            this.onGround = onGround;
            this.sneaking = sneaking;
            this.sprinting = sprinting;
        }

        public boolean available() {
            return available;
        }

        public boolean onGround() {
            return onGround;
        }

        public boolean sneaking() {
            return sneaking;
        }

        public boolean sprinting() {
            return sprinting;
        }
    }
}
