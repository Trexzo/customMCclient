package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Single original-EntityPlayerSP rotation writer within one host callback.
 *
 * A module that attempted its first native yaw/pitch setter owns the
 * callback's remaining axis writes, even if its module subsequently returns
 * false after an ON/OFF, policy change or native setter exception.
 *
 * The original player/GUI source gate is separately enforced by
 * Minecraft189RotationHostSourceLease. The arbiter never synthesizes a yaw,
 * pitch, click, hit, packet or input event.
 */
final class Minecraft189RotationAxisArbiter {
    enum Owner { SPIN, KILL_AURA, AIM_ASSIST, JITTER }

    private Owner current;
    private boolean active;
    private long generation;

    synchronized void enter() {
        ++generation;
        current = null;
        active = true;
    }

    synchronized void exit() {
        ++generation;
        current = null;
        active = false;
    }

    synchronized boolean ownedBy(final Owner owner) {
        return active && current == owner;
    }

    private synchronized boolean claim(final Owner requested,
            final long token) {
        if (!active || token != generation || requested == null)
            return false;
        if (current != null && current != requested)
            return false;
        current = requested;
        return true;
    }

    Minecraft189PlayerRotationControl guardedControl(
            final Minecraft189PlayerRotationControl original,
            final Owner requested) {
        if (original == null || requested == null) return null;
        final long token;
        synchronized (this) {
            token = generation;
        }
        return new Minecraft189PlayerRotationControl() {
            @Override public float customMcRotationYaw() {
                return original.customMcRotationYaw();
            }
            @Override public float customMcRotationPitch() {
                return original.customMcRotationPitch();
            }
            @Override public void customMcSetRotationYaw(final float value) {
                // Claim before native callback: reentry and module disable
                // cannot cause the next module to write the other axis.
                if (claim(requested, token))
                    original.customMcSetRotationYaw(value);
            }
            @Override public void customMcSetRotationPitch(final float value) {
                if (claim(requested, token))
                    original.customMcSetRotationPitch(value);
            }
        };
    }
}
