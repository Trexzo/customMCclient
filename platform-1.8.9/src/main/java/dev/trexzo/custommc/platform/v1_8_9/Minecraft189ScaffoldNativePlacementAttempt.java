package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V28: one opt-in, synchronous original-controller placement attempt after a
 * full V21/V26 source-authority check. It is disabled without explicit
 * acceptance ownership and it never constructs raw C08 packets.
 *
 * A true controller return is only an attempted CLIENT placement; server
 * persistence and survival stack consumption must be verified independently.
 * The result contains no live source references or reusable World lease.
 */
final class Minecraft189ScaffoldNativePlacementAttempt {
    static final class Result {
        final int targetX, targetY, targetZ;
        final int supportX, supportY, supportZ;
        final Minecraft189ScaffoldSupportPlanner.Face face;
        final int originalStackSize;
        final int originalSlot;
        final boolean originalControllerAccepted;
        Result(Minecraft189ScaffoldSupportPlanner.Candidate source,
                int originalStackSize, int originalSlot, boolean accepted) {
            targetX=source.targetX;
            targetY=source.targetY;
            targetZ=source.targetZ;
            supportX=source.supportX;
            supportY=source.supportY;
            supportZ=source.supportZ;
            face=source.face;
            this.originalStackSize=originalStackSize;
            this.originalSlot=originalSlot;
            originalControllerAccepted=accepted;
        }
    }
    private static final Minecraft189OriginalScaffoldPlacementAttemptTrace
            originalV29Trace = new Minecraft189OriginalScaffoldPlacementAttemptTrace();

    private Minecraft189ScaffoldNativePlacementAttempt() { }

    static void resetOriginalV29Trace() { originalV29Trace.reset(); }

    static Minecraft189OriginalScaffoldPlacementAttemptTrace.Snapshot
            originalV29Trace() { return originalV29Trace.snapshot(); }

    private static Result originalV29Refused(final String reason) {
        if (Boolean.getBoolean("custommc.acceptance.requireOriginalScaffoldPlacement"))
            originalV29Trace.rejected(reason);
        return null;
    }

    static Result attempt(Object minecraft, boolean explicitlyEnabled) {
        return attempt(minecraft, explicitlyEnabled, false);
    }

    static Result attempt(Object minecraft, boolean explicitlyEnabled,
            boolean allowAdjacentEdge) {
        final boolean originalV29Diagnostic = Boolean.getBoolean(
                "custommc.acceptance.requireOriginalScaffoldPlacement");
        if (originalV29Diagnostic) originalV29Trace.started();
        if (!explicitlyEnabled
                || !(minecraft instanceof Minecraft189ScaffoldNativeContextAccess)
                || !(minecraft instanceof Minecraft189ScaffoldNativeControllerAccess))
            return originalV29Refused("original-source-interface-or-explicit-owner-missing");
        final Minecraft189ScaffoldWorldLease lease =
                new Minecraft189ScaffoldWorldLease();
        try {
            final Minecraft189ScaffoldNativeContext.Witness context =
                    Minecraft189ScaffoldNativeContext.capture(
                            (Minecraft189ScaffoldNativeContextAccess) minecraft);
            if (context == null)
                return originalV29Refused("original-player-world-cursor-or-item-context-unavailable");
            final Object controller =
                    ((Minecraft189ScaffoldNativeControllerAccess)minecraft)
                            .customMcScaffoldNativeController();
            if (!(controller instanceof Minecraft189ScaffoldVanillaPlaceAccess))
                return originalV29Refused("original-vanilla-controller-interface-unavailable");
            final double nativeEyeY =
                    Minecraft189ScaffoldSourceEye.currentEyeY(context.player);
            final double reach =
                    Minecraft189ScaffoldSourceReach.conservativeReach(minecraft);
            if (!Double.isFinite(nativeEyeY) || !Double.isFinite(reach)
                    || lease.open(context.world) == null)
                return originalV29Refused("original-eye-reach-or-world-lease-unavailable");
            final Minecraft189ScaffoldSupportPlanner.Candidate candidate =
                    Minecraft189ScaffoldRayCandidateGate.verify(
                            lease, context.world, context.player,
                            context.nativeCursor, nativeEyeY, reach,
                            allowAdjacentEdge);
            if (candidate == null)
                return originalV29Refused("original-source-cursor-face-ray-or-placement-candidate-rejected");
            final Minecraft189InventoryHotbarAccess inventory =
                    ((Minecraft189PlayerInventoryAccess)context.player).customMcInventory();
            if (!(inventory instanceof Minecraft189InventoryHotbarItemsAccess))
                return originalV29Refused("original-selected-inventory-interface-unavailable");
            final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                    Minecraft189ScaffoldSelectedHotbar.from(
                            (Minecraft189InventoryHotbarItemsAccess)inventory);
            if (selected == null)
                return originalV29Refused("original-selected-hotbar-block-unavailable");
            final Minecraft189ScaffoldNativeContext.Witness again =
                    Minecraft189ScaffoldNativeContext.capture(
                            (Minecraft189ScaffoldNativeContextAccess)minecraft);
            if (again == null || again.player != context.player
                    || again.world != context.world
                    || again.nativeCursor != context.nativeCursor
                    || ((Minecraft189ScaffoldNativeControllerAccess)minecraft)
                            .customMcScaffoldNativeController() != controller
                    || ((Minecraft189PlayerInventoryAccess)context.player)
                            .customMcInventory() != inventory)
                return originalV29Refused("original-same-tick-player-world-cursor-controller-or-inventory-changed");
            final Minecraft189ScaffoldSelectedHotbar.Evidence last =
                    Minecraft189ScaffoldSelectedHotbar.from(
                            (Minecraft189InventoryHotbarItemsAccess)inventory);
            if (last == null || last.sourceItemStack != selected.sourceItemStack
                    || last.sourceSlot != selected.sourceSlot
                    || last.sourceStackSize != selected.sourceStackSize)
                return originalV29Refused("original-hotbar-slot-stack-or-item-changed");
            // The last check after native reads is only valid within the
            // same current tick and the same original World lease. The
            // native controller may return false or the server may veto.
            final boolean accepted =
                    ((Minecraft189ScaffoldVanillaPlaceAccess)controller)
                            .customMcOriginalRightClick(context.player, context.world,
                                    selected.sourceItemStack, context.nativeCursor);
            if (originalV29Diagnostic)
                originalV29Trace.completed(accepted);
            return new Result(candidate, selected.sourceStackSize,
                    selected.sourceSlot, accepted);
        } catch (RuntimeException invalidOrReplacedNativeSource) {
            return originalV29Refused(
                    "original-native-exception-"
                        + invalidOrReplacedNativeSource.getClass().getSimpleName());
        } finally {
            lease.close();
        }
    }
}
