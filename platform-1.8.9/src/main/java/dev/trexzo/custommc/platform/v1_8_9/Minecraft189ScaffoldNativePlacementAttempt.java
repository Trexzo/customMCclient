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
    private Minecraft189ScaffoldNativePlacementAttempt() { }

    static Result attempt(Object minecraft, boolean explicitlyEnabled) {
        return attempt(minecraft, explicitlyEnabled, false);
    }

    static Result attempt(Object minecraft, boolean explicitlyEnabled,
            boolean allowAdjacentEdge) {
        if (!explicitlyEnabled
                || !(minecraft instanceof Minecraft189ScaffoldNativeContextAccess)
                || !(minecraft instanceof Minecraft189ScaffoldNativeControllerAccess))
            return null;
        final Minecraft189ScaffoldWorldLease lease =
                new Minecraft189ScaffoldWorldLease();
        try {
            final Minecraft189ScaffoldNativeContext.Witness context =
                    Minecraft189ScaffoldNativeContext.capture(
                            (Minecraft189ScaffoldNativeContextAccess) minecraft);
            if (context == null) return null;
            final Object controller =
                    ((Minecraft189ScaffoldNativeControllerAccess)minecraft)
                            .customMcScaffoldNativeController();
            if (!(controller instanceof Minecraft189ScaffoldVanillaPlaceAccess))
                return null;
            final double nativeEyeY =
                    Minecraft189ScaffoldSourceEye.currentEyeY(context.player);
            final double reach =
                    Minecraft189ScaffoldSourceReach.conservativeReach(minecraft);
            if (!Double.isFinite(nativeEyeY) || !Double.isFinite(reach)
                    || lease.open(context.world) == null)
                return null;
            final Minecraft189ScaffoldSupportPlanner.Candidate candidate =
                    Minecraft189ScaffoldRayCandidateGate.verify(
                            lease, context.world, context.player,
                            context.nativeCursor, nativeEyeY, reach,
                            allowAdjacentEdge);
            if (candidate == null) return null;
            final Minecraft189InventoryHotbarAccess inventory =
                    ((Minecraft189PlayerInventoryAccess)context.player).customMcInventory();
            if (!(inventory instanceof Minecraft189InventoryHotbarItemsAccess))
                return null;
            final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                    Minecraft189ScaffoldSelectedHotbar.from(
                            (Minecraft189InventoryHotbarItemsAccess)inventory);
            if (selected == null) return null;
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
                return null;
            final Minecraft189ScaffoldSelectedHotbar.Evidence last =
                    Minecraft189ScaffoldSelectedHotbar.from(
                            (Minecraft189InventoryHotbarItemsAccess)inventory);
            if (last == null || last.sourceItemStack != selected.sourceItemStack
                    || last.sourceSlot != selected.sourceSlot
                    || last.sourceStackSize != selected.sourceStackSize)
                return null;
            // The last check after native reads is only valid within the
            // same current tick and the same original World lease. The
            // native controller may return false or the server may veto.
            final boolean accepted =
                    ((Minecraft189ScaffoldVanillaPlaceAccess)controller)
                            .customMcOriginalRightClick(context.player, context.world,
                                    selected.sourceItemStack, context.nativeCursor);
            return new Result(candidate, selected.sourceStackSize,
                    selected.sourceSlot, accepted);
        } catch (RuntimeException invalidOrReplacedNativeSource) {
            return null;
        } finally {
            lease.close();
        }
    }
}
