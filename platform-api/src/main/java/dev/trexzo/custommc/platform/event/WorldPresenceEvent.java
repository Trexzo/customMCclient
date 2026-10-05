package dev.trexzo.custommc.platform.event;

public final class WorldPresenceEvent {
    private final long sequence;
    private final boolean present;

    public WorldPresenceEvent(
            final long sequence,
            final boolean present) {
        if (sequence < 0L) {
            throw new IllegalArgumentException(
                    "sequence must not be negative");
        }

        this.sequence = sequence;
        this.present = present;
    }

    public long sequence() {
        return sequence;
    }

    public boolean present() {
        return present;
    }
}
