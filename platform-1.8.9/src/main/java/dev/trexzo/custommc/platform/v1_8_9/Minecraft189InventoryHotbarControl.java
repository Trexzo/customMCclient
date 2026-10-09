package dev.trexzo.custommc.platform.v1_8_9;

/** Setter backed by pinned InventoryPlayer.currentItem / field_70461_c. */
public interface Minecraft189InventoryHotbarControl
        extends Minecraft189InventoryHotbarAccess {
    void customMcSetSelectedHotbarSlot(int zeroBasedSlot);
}
