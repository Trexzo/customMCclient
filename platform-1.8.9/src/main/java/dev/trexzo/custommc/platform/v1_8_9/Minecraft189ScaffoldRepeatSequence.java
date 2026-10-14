package dev.trexzo.custommc.platform.v1_8_9;

import java.lang.ref.WeakReference;

/**
 * V36 isolated candidate for repeated native controller placement.
 *
 * NOT INSTALLED IN original runTick or a user setting yet. The production
 * V35 path remains exactly one placement attempt per module activation.
 * This module-owned primitive transaction must first pass dedicated-server
 * two-block / survival 8->7->6 proof in a later opt-in integration.
 *
 * Each step re-acquires original Minecraft World, player, cursor, controller
 * and selected ItemBlock. A second click is forbidden until the first
 * original WorldClient + exact one-item readback, a new supported target
 * coordinate and the bounded cadence are all satisfied. Such CLIENT readback
 * is not independent dedicated-server proof.
 */
final class Minecraft189ScaffoldRepeatSequence {
    private final Minecraft189ScaffoldPlacementCadence cadence =
            new Minecraft189ScaffoldPlacementCadence();
    private final Minecraft189ScaffoldSourceEpoch source =
            new Minecraft189ScaffoldSourceEpoch();
    private WeakReference<Object> selectedStack = new WeakReference<Object>(null);
    private long sourceGeneration;
    private long activation;
    private int selectedSlot;
    private int expectedCount;
    private boolean hasPriorTarget;
    private int priorX, priorY, priorZ;
    private Minecraft189ScaffoldNativePlacementAttempt.Result pending;

    synchronized boolean start(final Object originalMinecraft,
            final long activationGeneration, final long tick,
            final int maximumAttempts, final int minimumGapTicks) {
        invalidate();
        if (activationGeneration <= 0 || maximumAttempts < 2
                || maximumAttempts > 8 || minimumGapTicks < 2
                || minimumGapTicks > 100)
            return false;
        final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                selectedFromCurrentSource(originalMinecraft);
        if (selected == null || selected.sourceStackSize < maximumAttempts)
            return false;
        final long observed = source.observe(originalMinecraft);
        if (observed <= 0L || !cadence.arm(activationGeneration, tick,
                maximumAttempts, minimumGapTicks)) {
            invalidate();
            return false;
        }
        activation = activationGeneration;
        sourceGeneration = observed;
        selectedSlot = selected.sourceSlot;
        expectedCount = selected.sourceStackSize;
        selectedStack = new WeakReference<Object>(selected.sourceItemStack);
        return true;
    }

    /** Pure native source read with no controller, packet or World mutation. */
    private static Minecraft189ScaffoldSelectedHotbar.Evidence selectedFromCurrentSource(
            final Object originalMinecraft) {
        if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeContextAccess))
            return null;
        try {
            final Minecraft189ScaffoldNativeContext.Witness context =
                    Minecraft189ScaffoldNativeContext.capture(
                            (Minecraft189ScaffoldNativeContextAccess)originalMinecraft);
            if (context == null
                    || !(context.player instanceof Minecraft189PlayerInventoryAccess))
                return null;
            final Minecraft189InventoryHotbarAccess inventory =
                    ((Minecraft189PlayerInventoryAccess)context.player)
                            .customMcInventory();
            if (!(inventory instanceof Minecraft189InventoryHotbarItemsAccess))
                return null;
            return Minecraft189ScaffoldSelectedHotbar.from(
                    (Minecraft189InventoryHotbarItemsAccess)inventory);
        } catch (RuntimeException invalidOriginalSource) {
            return null;
        }
    }

    private boolean sameOwner(final Object originalMinecraft,
            final long activationGeneration) {
        if (activationGeneration <= 0 || activationGeneration != activation
                || sourceGeneration <= 0L
                || source.observe(originalMinecraft) != sourceGeneration) {
            invalidate();
            return false;
        }
        return true;
    }

    synchronized Minecraft189ScaffoldNativePlacementAttempt.Result tryNext(
            final Object originalMinecraft, final long activationGeneration,
            final long tick) {
        if (!sameOwner(originalMinecraft, activationGeneration)) return null;
        if (!cadence.mayAttempt(activationGeneration, tick)) return null;
        if (cadence.stopped()) { invalidate(); return null; }

        final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                selectedFromCurrentSource(originalMinecraft);
        if (selected == null || selected.sourceItemStack != selectedStack.get()
                || selected.sourceSlot != selectedSlot
                || selected.sourceStackSize != expectedCount) {
            invalidate();
            return null;
        }
        final Minecraft189ScaffoldOriginalCandidateProbe.Observation fresh =
                Minecraft189ScaffoldOriginalCandidateProbe.observe(originalMinecraft);
        if (fresh == null) return null;
        if (hasPriorTarget && fresh.targetX == priorX
                && fresh.targetY == priorY && fresh.targetZ == priorZ)
            return null; // Never re-click the already-confirmed original target.

        // This is the only synchronous original-controller side effect.
        // The underlying method independently revalidates the World lease,
        // raytrace, selected stack and controller immediately before clicking.
        final Minecraft189ScaffoldNativePlacementAttempt.Result attempt =
                Minecraft189ScaffoldNativePlacementAttempt.attempt(
                        originalMinecraft, true);
        if (attempt == null) return null;
        final boolean admitted = attempt.targetX == fresh.targetX
                && attempt.targetY == fresh.targetY
                && attempt.targetZ == fresh.targetZ
                && attempt.originalSlot == selectedSlot
                && attempt.originalStackSize == expectedCount
                && source.observe(originalMinecraft) == sourceGeneration
                && attempt.originalControllerAccepted;
        if (!cadence.controllerAttempt(activationGeneration, tick, admitted)
                || !admitted) {
            invalidate(); // False native controller / source switch consumes action.
            return attempt;
        }
        pending = attempt; // Primitive-only result, no original game references.
        return attempt;
    }

    synchronized boolean checkReceipt(final Object originalMinecraft,
            final long activationGeneration, final long tick) {
        if (!sameOwner(originalMinecraft, activationGeneration)) return false;
        // Advances the monotonic tick and expires any unacknowledged grant.
        cadence.mayAttempt(activationGeneration, tick);
        if (cadence.stopped()) { invalidate(); return false; }
        if (pending == null) return false;
        if (!source.confirmsSameSourceReadback(
                originalMinecraft, sourceGeneration, pending))
            return false; // A lagging original WorldClient may still catch up.

        final Minecraft189ScaffoldNativePlacementAttempt.Result proven = pending;
        if (!cadence.receipt(activationGeneration, tick, true)) {
            invalidate();
            return false;
        }
        hasPriorTarget = true;
        priorX = proven.targetX;
        priorY = proven.targetY;
        priorZ = proven.targetZ;
        expectedCount = proven.originalStackSize - 1;
        pending = null;
        return true;
    }

    synchronized boolean pending() { return pending != null && !cadence.stopped(); }
    synchronized int attemptedCount() { return cadence.attemptedCount(); }
    synchronized boolean stopped() { return cadence.stopped(); }

    synchronized void invalidate() {
        cadence.invalidate();
        source.invalidate();
        selectedStack.clear();
        selectedStack = new WeakReference<Object>(null);
        activation = sourceGeneration = 0L;
        pending = null;
        hasPriorTarget = false;
        priorX = priorY = priorZ = expectedCount = selectedSlot = 0;
    }
}
