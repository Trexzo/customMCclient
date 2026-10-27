package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Coordinates only the OWNERSHIP of one synthetic combat callback.
 *
 * The original game owns actual clickMouse, attackEntity, rightClickMouse,
 * setSprinting, player hurt, packets and damage. Module-specific validation
 * (UUID, hit-time, source hotbar, GUI and movement) must remain independent.
 *
 * One callback may select EXACTLY ONE primary action, in bridge priority
 * POTION > ROD > MELEE. A native melee ATTEMPT may optionally proceed to
 * sword-use AutoBlock, but is never mislabeled as a confirmed server hit.
 *
 * A nested native callback cannot enter and consume the outer token.
 * A throwing or abandoned call returns to QUIESCENT through finally.
 */
final class Minecraft189CombatCycleArbiter {
    enum Phase {
        QUIESCENT, EVALUATING, POTION, ROD, MELEE_ATTEMPT, REBLOCK_ATTEMPT
    }

    enum Primary {
        POTION, ROD, MELEE
    }

    private Phase phase = Phase.QUIESCENT;
    private long generation;
    private long activeToken;

    synchronized long enter() {
        if (phase != Phase.QUIESCENT) return 0L;
        activeToken = ++generation;
        phase = Phase.EVALUATING;
        return activeToken;
    }

    synchronized boolean claimPrimary(final long token, final Primary action) {
        if (token == 0L || token != activeToken
                || phase != Phase.EVALUATING || action == null)
            return false;
        switch (action) {
            case POTION:
                phase = Phase.POTION;
                break;
            case ROD:
                phase = Phase.ROD;
                break;
            case MELEE:
                phase = Phase.MELEE_ATTEMPT;
                break;
            default:
                return false;
        }
        return true;
    }

    /** Reblock can only follow THIS callback's melee ATTEMPT, not rod/pot. */
    synchronized boolean claimReblock(final long token) {
        if (token == 0L || token != activeToken
                || phase != Phase.MELEE_ATTEMPT) return false;
        phase = Phase.REBLOCK_ATTEMPT;
        return true;
    }

    synchronized void leave(final long token) {
        if (token == 0L || token != activeToken) return;
        phase = Phase.QUIESCENT;
        activeToken = 0L;
    }

    /** Original runtime shutdown revokes even an in-flight native lease. */
    synchronized void revoke() {
        ++generation;
        activeToken = 0L;
        phase = Phase.QUIESCENT;
    }

    synchronized Phase phase() {
        return phase;
    }
}
