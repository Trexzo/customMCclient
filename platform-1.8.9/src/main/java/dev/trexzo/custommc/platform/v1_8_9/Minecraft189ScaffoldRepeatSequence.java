package dev.trexzo.custommc.platform.v1_8_9;

import java.lang.ref.WeakReference;

/**
 * Native controller repeat primitive, now used by V43/V54 bounded
 * production opt-ins after original 1.8.9 two/three-stone server proofs.
 *
 * V57 retains every previously RECEIPTED target for the whole activation,
 * not only its immediate predecessor. A later A -> B -> A native candidate
 * is never allowed, even after an external world change or block break.
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
    // Source identity survives a temporarily ungrounded/no-ray movement tick,
    // but real World/player replacement or disconnect revokes the activation.
    // The stricter source epoch is still independently checked for EVERY
    // controller action and its pending original WorldClient receipt.
    private final Minecraft189ScaffoldActivationSource identity =
            new Minecraft189ScaffoldActivationSource();
    // Pin the source-mapped *Item* singleton, not ItemStack wrapper identity.
    // Vanilla server synchronization may allocate a fresh ItemStack after an
    // acknowledged placement. Neither the selected slot, item subtype nor
    // item-damage metadata may change before the next native controller call.
    private WeakReference<Object> selectedItem = new WeakReference<Object>(null);
    private int selectedItemDamage;
    private long sourceGeneration;
    private long activation;
    private int selectedSlot;
    private int expectedCount;
    // Full per-activation history: forbids any A -> B -> A reuse, even if
    // the original World later observes A as replaceable again.
    private final Minecraft189ScaffoldConfirmedTargets confirmedTargets =
            new Minecraft189ScaffoldConfirmedTargets();
    private boolean hasPriorTarget;
    private int priorX, priorY, priorZ;
    private Minecraft189ScaffoldNativePlacementAttempt.Result pending;
    // V58 CI only reads this witness. It never changes controller admission.
    private boolean historicalVetoObserved;

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
        final Object exactItem;
        final int exactDamage;
        try {
            exactItem = selected.sourceItemStack.customMcOriginalItemIdentity();
            exactDamage = selected.sourceItemStack.customMcItemDamage();
        } catch (RuntimeException unsupportedNativeItem) {
            return false;
        }
        if (exactItem == null) return false;
        final long observed = source.observe(originalMinecraft);
        if (observed <= 0L
                || identity.observe(originalMinecraft)
                        != Minecraft189ScaffoldActivationSource.Verdict.SAME
                || !cadence.arm(activationGeneration, tick,
                        maximumAttempts, minimumGapTicks)) {
            invalidate();
            return false;
        }
        activation = activationGeneration;
        sourceGeneration = observed;
        selectedSlot = selected.sourceSlot;
        expectedCount = selected.sourceStackSize;
        selectedItemDamage = exactDamage;
        selectedItem = new WeakReference<Object>(exactItem);
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

    /**
     * Read only the currently selected original ItemStack without requiring
     * a grounded player or an already-admissible native mouse-over. Those
     * transient placement conditions may disappear while walking to block 2.
     * A changed/removed original item or selected slot still irrevocably
     * cancels the sequence before any further vanilla click.
     */
    private static Minecraft189ScaffoldSelectedHotbar.Evidence
            selectedWithoutPlacementReadiness(final Object originalMinecraft) {
        if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeContextAccess))
            return null;
        try {
            final Object[] original = ((Minecraft189ScaffoldNativeContextAccess)
                    originalMinecraft).customMcScaffoldNativeContext();
            if (original == null || original.length != 3
                    || !(original[0] instanceof Minecraft189PlayerInventoryAccess))
                return null;
            final Minecraft189InventoryHotbarAccess inventory =
                    ((Minecraft189PlayerInventoryAccess)original[0]).customMcInventory();
            if (!(inventory instanceof Minecraft189InventoryHotbarItemsAccess))
                return null;
            return Minecraft189ScaffoldSelectedHotbar.from(
                    (Minecraft189InventoryHotbarItemsAccess)inventory);
        } catch (RuntimeException unavailableOriginalInventory) {
            return null;
        }
    }

    private boolean sameOriginalSelectedItem(
            final Minecraft189ScaffoldSelectedHotbar.Evidence selected) {
        if (selected == null || selected.sourceSlot != selectedSlot)
            return false;
        try {
            final Object currentItem =
                    selected.sourceItemStack.customMcOriginalItemIdentity();
            return currentItem != null && currentItem == selectedItem.get()
                    && selected.sourceItemStack.customMcItemDamage()
                            == selectedItemDamage;
        } catch (RuntimeException unavailableNativeItem) {
            return false;
        }
    }

    private boolean sameOwner(final Object originalMinecraft,
            final long activationGeneration) {
        if (activationGeneration <= 0 || activationGeneration != activation
                || sourceGeneration <= 0L
                || identity.observe(originalMinecraft)
                        != Minecraft189ScaffoldActivationSource.Verdict.SAME) {
            invalidate();
            return false;
        }
        return true;
    }

    synchronized Minecraft189ScaffoldNativePlacementAttempt.Result tryNext(
            final Object originalMinecraft, final long activationGeneration,
            final long tick) {
        return tryNext(originalMinecraft, activationGeneration, tick, false);
    }

    synchronized Minecraft189ScaffoldNativePlacementAttempt.Result tryNext(
            final Object originalMinecraft, final long activationGeneration,
            final long tick, final boolean allowAdjacentEdge) {
        if (!sameOwner(originalMinecraft, activationGeneration)) return null;
        if (!cadence.mayAttempt(activationGeneration, tick)) return null;
        if (cadence.stopped()) { invalidate(); return null; }

        final Minecraft189ScaffoldSelectedHotbar.Evidence stillSelected =
                selectedWithoutPlacementReadiness(originalMinecraft);
        if (!sameOriginalSelectedItem(stillSelected)
                || stillSelected.sourceStackSize != expectedCount) {
            invalidate();
            return null;
        }
        // Grounded state and mouse-over can legitimately be transient while
        // walking. Wait with no action, never transfer the source lease.
        final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                selectedFromCurrentSource(originalMinecraft);
        if (selected == null) return null;
        final Minecraft189ScaffoldOriginalCandidateProbe.Observation fresh =
                Minecraft189ScaffoldOriginalCandidateProbe.observe(
                        originalMinecraft, allowAdjacentEdge);
        if (fresh == null) return null;
        if (confirmedTargets.contains(fresh.targetX, fresh.targetY, fresh.targetZ)) {
            historicalVetoObserved = true;
            return null; // Every prior confirmed target, not just the last.
        }

        // A movement tick may be ungrounded or have no original mouse-over.
        // It must not reset the exact Minecraft/World/player identity lease.
        // Once a genuinely NEW admissible target is available, re-establish
        // V31's strict full-readiness source generation before any click.
        final long readyGeneration = source.observe(originalMinecraft);
        if (readyGeneration <= 0L) {
            invalidate();
            return null;
        }
        sourceGeneration = readyGeneration;

        // This is the only synchronous original-controller side effect.
        // The underlying method independently revalidates the World lease,
        // raytrace, selected stack and controller immediately before clicking.
        final Minecraft189ScaffoldNativePlacementAttempt.Result attempt =
                Minecraft189ScaffoldNativePlacementAttempt.attempt(
                        originalMinecraft, true, allowAdjacentEdge);
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
        // The V29 World/stack readback proves exactly one debit, but it does
        // not compare actual Item identity. Require the ORIGINAL native Item
        // and damage to still match before accepting this sequence receipt.
        if (!sameOriginalSelectedItem(
                selectedWithoutPlacementReadiness(originalMinecraft))) {
            invalidate();
            return false;
        }

        final Minecraft189ScaffoldNativePlacementAttempt.Result proven = pending;
        if (!cadence.receipt(activationGeneration, tick, true)) {
            invalidate();
            return false;
        }
        if (!confirmedTargets.recordReceipt(
                proven.targetX, proven.targetY, proven.targetZ)) {
            invalidate(); // Duplicate or ninth receipt cannot extend a lease.
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

    /**
     * V37 acceptance diagnostics only. This read-only state inspection does
     * NOT renew source authority, advance cadence, consume a receipt, or
     * invoke the vanilla right-click controller. Any native evidence it
     * reports is discarded within this same tick.
     */
    synchronized String diagnose(final Object originalMinecraft) {
        final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                selectedWithoutPlacementReadiness(originalMinecraft);
        final String selectedState;
        if (selected == null) selectedState = "native_selected_stack_unavailable";
        else if (selected.sourceSlot != selectedSlot)
            selectedState = "original_selected_slot_changed";
        else if (!sameOriginalSelectedItem(selected))
            selectedState = "original_item_identity_or_damage_changed";
        else if (selected.sourceStackSize != expectedCount)
            selectedState = "original_stack_count_changed expected="
                    + expectedCount + " current=" + selected.sourceStackSize;
        else selectedState = "same_original_item_and_expected_count=" + expectedCount;
        final Minecraft189ScaffoldOriginalCandidateProbe.Observation candidate =
                Minecraft189ScaffoldOriginalCandidateProbe.observe(originalMinecraft);
        final String target;
        if (candidate == null) target = "no_original_native_candidate";
        else if (confirmedTargets.contains(
                candidate.targetX, candidate.targetY, candidate.targetZ))
            target = "already_confirmed_target="
                    + candidate.targetX + "," + candidate.targetY + "," + candidate.targetZ;
        else target = "fresh_target=" + candidate.targetX + ","
                + candidate.targetY + "," + candidate.targetZ;
        return "activation=" + activation
                + " sourceGeneration=" + sourceGeneration
                + " cadenceStopped=" + cadence.stopped()
                + " cadencePending=" + cadence.pending()
                + " attempts=" + cadence.attemptedCount()
                + " priorTarget=" + hasPriorTarget
                + " distinctConfirmedTargets=" + confirmedTargets.count()
                + " selected=" + selectedState
                + " candidate=" + target;
    }

    synchronized boolean historicalVetoObserved() { return historicalVetoObserved; }
    synchronized boolean pending() { return pending != null && !cadence.stopped(); }
    synchronized int attemptedCount() { return cadence.attemptedCount(); }
    synchronized boolean stopped() { return cadence.stopped(); }

    synchronized void invalidate() {
        cadence.invalidate();
        source.invalidate();
        identity.reset();
        selectedItem.clear();
        selectedItem = new WeakReference<Object>(null);
        selectedItemDamage = 0;
        activation = sourceGeneration = 0L;
        pending = null;
        confirmedTargets.reset();
        historicalVetoObserved = false;
        hasPriorTarget = false;
        priorX = priorY = priorZ = expectedCount = selectedSlot = 0;
    }
}
