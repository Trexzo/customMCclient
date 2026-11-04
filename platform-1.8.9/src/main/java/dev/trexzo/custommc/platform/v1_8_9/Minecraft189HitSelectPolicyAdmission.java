package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V367 R5: immutable optional HitSelect policy at one accepted scheduler
 * boundary. The genuine current World UUID, native hurt-time, and actual
 * PlayerControllerMP entity are still separately validated by HitSelect's
 * original existing runtime/controller gates.
 *
 * Reentrant original-game getters may disable/re-enable HitSelect, switch
 * recovery policy or alter its hurt window after that first validation.
 * Only this exact module lifetime/policy can retain the old attack credit.
 * This snapshot never observes hurt-time, spends READY or issues a click.
 */
final class Minecraft189HitSelectPolicyAdmission {
    private final boolean enabled;
    private final long ownerEpoch;
    private final boolean requireRecovery;
    private final int minimum;
    private final int maximum;

    private Minecraft189HitSelectPolicyAdmission(
            final boolean enabled, final long ownerEpoch,
            final boolean requireRecovery, final int minimum,
            final int maximum) {
        this.enabled = enabled;
        this.ownerEpoch = ownerEpoch;
        this.requireRecovery = requireRecovery;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    static Minecraft189HitSelectPolicyAdmission capture(
            final boolean enabled, final long ownerEpoch,
            final boolean requireRecovery, final int minimum,
            final int maximum) {
        return new Minecraft189HitSelectPolicyAdmission(
                enabled, ownerEpoch, requireRecovery, minimum, maximum);
    }

    boolean matches(final Minecraft189HitSelectPolicyAdmission current) {
        return current != null && enabled == current.enabled
                && ownerEpoch == current.ownerEpoch
                && requireRecovery == current.requireRecovery
                && minimum == current.minimum
                && maximum == current.maximum;
    }
}
