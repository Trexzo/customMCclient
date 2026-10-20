package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Source lease across the complete original EntityPlayerSP host motion
 * callback, rather than an individual movement module's native setter.
 * Nested different-player or newly focused-GUI callbacks revoke the
 * outer chain. A same-player callback never becomes an extra tick.
 */
final class Minecraft189MotionHostSourceLease {
    private boolean dispatching;
    private Minecraft189PlayerMotionControl player;
    private boolean guiAtEntry;
    private boolean revoked;

    boolean enter(final Minecraft189PlayerMotionControl source,
            final boolean guiFocused) {
        if (dispatching) {
            if (source != player || (!guiAtEntry && guiFocused)) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        player = source;
        guiAtEntry = guiFocused;
        revoked = false;
        return true;
    }

    boolean stillOwned(final boolean guiFocused) {
        if (!dispatching) {
            return false;
        }
        if (!guiAtEntry && guiFocused) {
            revoked = true;
        }
        return !revoked;
    }

    /**
     * Native getter values remain genuine original source observations.
     * Before EACH mapped setter, revalidate the host-wide source lease:
     * another original player or newly focused GUI may have revoked it
     * inside the preceding mapped setter. The old module must never
     * write its remaining axes in that case.
     */
    Minecraft189PlayerMotionControl guardedPlayer(
            final Minecraft189PlayerMotionControl original,
            final java.util.function.BooleanSupplier guiFocused) {
        if (original == null) {
            return null;
        }
        return new Minecraft189PlayerMotionControl() {
            @Override public double customMcMotionX() {
                return original.customMcMotionX();
            }
            @Override public double customMcMotionY() {
                return original.customMcMotionY();
            }
            @Override public double customMcMotionZ() {
                return original.customMcMotionZ();
            }
            @Override public void customMcSetMotionX(final double value) {
                if (stillOwned(guiFocused.getAsBoolean())) {
                    original.customMcSetMotionX(value);
                }
            }
            @Override public void customMcSetMotionY(final double value) {
                if (stillOwned(guiFocused.getAsBoolean())) {
                    original.customMcSetMotionY(value);
                }
            }
            @Override public void customMcSetMotionZ(final double value) {
                if (stillOwned(guiFocused.getAsBoolean())) {
                    original.customMcSetMotionZ(value);
                }
            }
        };
    }

    void exit() {
        player = null;
        guiAtEntry = false;
        revoked = false;
        dispatching = false;
    }
}
