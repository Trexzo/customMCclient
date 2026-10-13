package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V29 observational after-action readback, not server-authoritative proof.
 * Accepts only fresh original Minecraft WorldClient and selected ItemBlock.
 * A later dedicated-server probe must independently certify persistence.
 */
final class Minecraft189ScaffoldPlacementReadback {
    private Minecraft189ScaffoldPlacementReadback() { }

    static boolean confirmsClientWorldAndSurvivalStack(
            Object originalMinecraft,
            Minecraft189ScaffoldNativePlacementAttempt.Result attempt) {
        if (attempt == null || !attempt.originalControllerAccepted
                || !(originalMinecraft instanceof Minecraft189ScaffoldNativeContextAccess))
            return false;
        try {
            final Minecraft189ScaffoldNativeContext.Witness state =
                    Minecraft189ScaffoldNativeContext.capture(
                            (Minecraft189ScaffoldNativeContextAccess)originalMinecraft);
            if (state == null
                    || state.world.customMcScaffoldCellCode(
                            attempt.targetX,attempt.targetY,attempt.targetZ)
                                    != Minecraft189ScaffoldWorldReadAccess.FULL_SOLID
                    || state.world.customMcScaffoldCellCode(
                            attempt.supportX,attempt.supportY,attempt.supportZ)
                                    != Minecraft189ScaffoldWorldReadAccess.FULL_SOLID)
                return false;
            final Minecraft189InventoryHotbarAccess selected =
                    ((Minecraft189PlayerInventoryAccess)state.player)
                            .customMcInventory();
            if (!(selected instanceof Minecraft189InventoryHotbarItemsAccess))
                return false;
            final Minecraft189ScaffoldSelectedHotbar.Evidence evidence =
                    Minecraft189ScaffoldSelectedHotbar.from(
                            (Minecraft189InventoryHotbarItemsAccess) selected);
            return evidence != null
                    && evidence.sourceSlot == attempt.originalSlot
                    && evidence.sourceStackSize == attempt.originalStackSize - 1;
        } catch (RuntimeException unavailableSource) {
            return false;
        }
    }
}
