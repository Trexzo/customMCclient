package dev.trexzo.custommc.platform.v1_8_9;

/** Exact 1.8.9 InventoryPlayer.mainInventory (field_70462_a). */
public interface Minecraft189InventoryHotbarItemsAccess
        extends Minecraft189InventoryHotbarControl {
    /** Returns a transformed ItemStack, or null for an absent/out-of-bounds slot. */
    Minecraft189ItemStackAccess[] customMcHotbarItems();
}
