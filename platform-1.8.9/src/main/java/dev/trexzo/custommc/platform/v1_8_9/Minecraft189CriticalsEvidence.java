package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Same-host-tick motion and fall evidence. Independent samples are required:
 * do not infer a critical-compatible fall from an old motion snapshot.
 */
public final class Minecraft189CriticalsEvidence {
    private boolean fallKnown;
    private float fallDistance;
    private boolean motionKnown;
    private double verticalMotion;

    public synchronized void reset() {
        fallKnown = false;
        fallDistance = 0.0F;
        motionKnown = false;
        verticalMotion = 0.0D;
    }

    public synchronized void fall(final float distance) {
        fallKnown = Float.isFinite(distance) && distance >= 0.0F;
        fallDistance = fallKnown ? distance : 0.0F;
    }

    public synchronized void motion(final double motionY) {
        motionKnown = Double.isFinite(motionY);
        verticalMotion = motionKnown ? motionY : 0.0D;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(fallKnown && motionKnown, fallDistance, verticalMotion);
    }

    public static final class Snapshot {
        private final boolean available;
        private final float fallDistance;
        private final double motionY;

        private Snapshot(final boolean available,
                final float fallDistance, final double motionY) {
            this.available = available;
            this.fallDistance = fallDistance;
            this.motionY = motionY;
        }

        public boolean available() { return available; }
        public float fallDistance() { return fallDistance; }
        public double motionY() { return motionY; }
    }
}
