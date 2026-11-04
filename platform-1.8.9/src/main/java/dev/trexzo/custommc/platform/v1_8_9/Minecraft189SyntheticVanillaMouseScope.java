package dev.trexzo.custommc.platform.v1_8_9;

/**
 * R8: identify a synchronous synthetic call to vanilla Minecraft.clickMouse
 * without inventing a UUID for ordinary hold-mode Auto Clicker or changing
 * physical/manual mouse input.
 *
 * The original Minecraft 1.8.9 clickMouse->PlayerControllerMP.attackEntity
 * callback runs on the invoking client thread. Its ThreadLocal admission is
 * visible to that exact native controller call only, and always revoked by
 * try-with-resources even when a native getter throws or an attack is vetoed.
 * Nested original callbacks restore the previous scope; out-of-order closing
 * fails closed instead of reviving a stale permission.
 */
final class Minecraft189SyntheticVanillaMouseScope {
    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<Scope>();

    private Minecraft189SyntheticVanillaMouseScope() { }

    static boolean active() {
        return CURRENT.get() != null;
    }

    static Scope enter() {
        final Scope scope = new Scope(CURRENT.get());
        CURRENT.set(scope);
        return scope;
    }

    static final class Scope implements AutoCloseable {
        private final Scope prior;
        private boolean closed;

        private Scope(final Scope prior) {
            this.prior = prior;
        }

        /** The exact current original click frame, never a nested stale one. */
        boolean stillCurrent() {
            return !closed && CURRENT.get() == this;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            if (CURRENT.get() != this) {
                // Invalid nesting must not restore older controller credit.
                CURRENT.remove();
                return;
            }
            if (prior == null || prior.closed) {
                CURRENT.remove();
            } else {
                CURRENT.set(prior);
            }
        }
    }
}
