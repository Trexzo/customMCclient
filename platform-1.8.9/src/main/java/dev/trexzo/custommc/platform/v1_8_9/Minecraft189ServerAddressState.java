package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;

public final class Minecraft189ServerAddressState {
    private boolean available;
    private String address;

    public synchronized void update(
            final String nextAddress) {
        final String normalized =
                Objects.requireNonNull(
                        nextAddress,
                        "nextAddress")
                        .trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "address must not be blank");
        }
        address = normalized;
        available = true;
    }

    public synchronized void clear() {
        available = false;
        address = null;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                available,
                address);
    }

    public static final class Snapshot {
        private final boolean available;
        private final String address;

        private Snapshot(
                final boolean available,
                final String address) {
            this.available = available;
            this.address = address;
        }

        public boolean available() {
            return available;
        }

        public String address() {
            return address;
        }
    }
}
