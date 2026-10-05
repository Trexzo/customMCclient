package dev.trexzo.custommc.platform.event;

import java.util.Objects;

public final class ClientTickEvent {
    public enum Phase {
        START,
        END
    }

    private final Phase phase;
    private final long sequence;

    public ClientTickEvent(
            final Phase phase,
            final long sequence) {
        if (sequence < 0L) {
            throw new IllegalArgumentException(
                    "sequence must not be negative");
        }

        this.phase = Objects.requireNonNull(
                phase,
                "phase");
        this.sequence = sequence;
    }

    public Phase phase() {
        return phase;
    }

    public long sequence() {
        return sequence;
    }
}
