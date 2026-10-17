package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Synchronous optional native pitch lease for a single verified AutoPot
 * right-click. The actual transformed original Entity.rotationPitch setter
 * already exists; no guessed packets, delayed timers, or fake orientation.
 */
final class Minecraft189AutoPotPitchLease {
    static final float SPLASH_PITCH = 85.0F;

    private final Minecraft189PlayerRotationControl owner;
    private final float oldPitch;
    private final boolean modified;
    private boolean active;
    private boolean valid;

    private Minecraft189AutoPotPitchLease(
            final Minecraft189PlayerRotationControl owner,
            final float oldPitch, final boolean modified,
            final boolean valid) {
        this.owner = owner;
        this.oldPitch = oldPitch;
        this.modified = modified;
        this.active = true;
        this.valid = valid;
    }

    static Minecraft189AutoPotPitchLease begin(
            final Minecraft189PlayerRotationControl owner) {
        if (owner == null)
            return new Minecraft189AutoPotPitchLease(
                    null, Float.NaN, false, false);
        final float initial = owner.customMcRotationPitch();
        if (!Float.isFinite(initial) || initial < -90.0F
                || initial > 90.0F)
            return new Minecraft189AutoPotPitchLease(
                    owner, initial, false, false);
        final boolean requiresChange = initial != SPLASH_PITCH;
        final Minecraft189AutoPotPitchLease lease =
                new Minecraft189AutoPotPitchLease(
                        owner, initial, requiresChange, false);
        if (!requiresChange) {
            lease.valid = true;
            return lease;
        }
        try {
            owner.customMcSetRotationPitch(SPLASH_PITCH);
            lease.valid = owner.customMcRotationPitch() == SPLASH_PITCH;
        } catch (RuntimeException failure) {
            // A broken original setter must cancel this AutoPot attempt,
            // not turn a local synthetic use attempt into a game crash.
            lease.valid = false;
            try {
                lease.restore();
            } catch (RuntimeException rollbackFailure) {
                // Never authorize use even if a native rollback also fails.
            }
            return lease;
        }
        if (!lease.valid) lease.restore();
        return lease;
    }

    boolean mayThrow(final Minecraft189PlayerRotationControl currentOwner) {
        return active && valid && owner != null
                && currentOwner == owner
                && owner.customMcRotationPitch() == SPLASH_PITCH;
    }

    /** Restore only the very pitch this action installed; do not overwrite
     * an intervening genuine user change or a different player on respawn. */
    void restore() {
        if (!active) return;
        active = false;
        if (!modified || owner == null) return;
        if (owner.customMcRotationPitch() == SPLASH_PITCH)
            owner.customMcSetRotationPitch(oldPitch);
    }

    float originalPitch() { return oldPitch; }
}
