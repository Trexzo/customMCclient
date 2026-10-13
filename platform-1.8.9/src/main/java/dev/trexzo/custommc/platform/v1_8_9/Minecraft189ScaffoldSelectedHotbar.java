package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Strict read-only evidence for a genuine selected hotbar ItemBlock.
 * No selection changes, inventory clicks, packets or placement.
 */
final class Minecraft189ScaffoldSelectedHotbar {
    static final class Evidence {
        final int sourceSlot;
        final int sourceStackSize;
        final Minecraft189ItemStackAccess sourceItemStack;

        private Evidence(int slot, int stackSize, Minecraft189ItemStackAccess stack) {
            this.sourceSlot = slot;
            this.sourceStackSize = stackSize;
            this.sourceItemStack = stack;
        }
    }

    private Minecraft189ScaffoldSelectedHotbar() { }

    static Evidence from(final Minecraft189InventoryHotbarItemsAccess inventory) {
        if (inventory == null) return null;
        try {
            // Original Minecraft 1.8.9 InventoryPlayer.mainInventory has
            // 36 entries; ONLY indices 0..8 are the selected hotbar. The
            // source-pinned getter returns the full original 36-slot array,
            // never a 9-slot copy or an inventory GUI window index.
            final Minecraft189ItemStackAccess[] slots =
                    inventory.customMcHotbarItems();
            final int selected = inventory.customMcSelectedHotbarSlot();
            if (slots == null || slots.length != 36 || selected < 0 || selected >= 9)
                return null;
            final Minecraft189ItemStackAccess stack = slots[selected];
            if (stack == null || !stack.customMcIsBlockItem()
                    || stack.customMcStackSize() <= 0)
                return null;
            return new Evidence(selected, stack.customMcStackSize(), stack);
        } catch (RuntimeException unavailableNativeInventory) {
            return null;
        }
    }
}
