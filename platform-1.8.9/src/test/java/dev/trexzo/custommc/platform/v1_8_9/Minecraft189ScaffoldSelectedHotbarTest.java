package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldSelectedHotbarTest {
    private static final class Stack implements Minecraft189ItemStackAccess {
        private final boolean isBlock;
        private final int count;
        Stack(boolean isBlock, int count) { this.isBlock = isBlock; this.count = count; }
        public String customMcDisplayName() { return "untrusted name"; }
        public int customMcStackSize() { return count; }
        public int customMcItemDamage() { return 0; }
        public int customMcMaxDamage() { return 0; }
        @Override public boolean customMcIsBlockItem() { return isBlock; }
    }

    private static final class Inventory implements Minecraft189InventoryHotbarItemsAccess {
        final Minecraft189ItemStackAccess[] slots =
                new Minecraft189ItemStackAccess[36];
        int selected = 2;
        boolean throwOnRead;
        int setterCalls;
        public Minecraft189ItemStackAccess[] customMcHotbarItems() {
            if (throwOnRead) throw new IllegalStateException("native world unloaded");
            return slots;
        }
        public int customMcSelectedHotbarSlot() { return selected; }
        public void customMcSetSelectedHotbarSlot(int slot) {
            setterCalls++;
            selected = slot;
        }
    }

    @Test void verifiesOnlyRealSelectedBlockStackAndNeverChangesSlot() {
        Inventory inv = new Inventory();
        inv.slots[2] = new Stack(true, 14);
        Minecraft189ScaffoldSelectedHotbar.Evidence e =
                Minecraft189ScaffoldSelectedHotbar.from(inv);
        assertNotNull(e);
        assertEquals(2, e.sourceSlot);
        assertEquals(14, e.sourceStackSize);
        assertSame(inv.slots[2], e.sourceItemStack);
        assertEquals(0, inv.setterCalls);
        assertEquals(2, inv.selected);
    }

    @Test void rejectsOtherItemTypesAndNeverBorrowsAnotherHotbarSlot() {
        Inventory inv = new Inventory();
        inv.slots[2] = new Stack(false, 64);
        inv.slots[3] = new Stack(true, 64);
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(inv));
        inv.slots[2] = null;
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(inv));
        inv.slots[2] = new Stack(true, 0);
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(inv));
        inv.slots[2] = new Stack(true, -1);
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(inv));
        assertEquals(0, inv.setterCalls);
    }

    @Test void staleMissingOrInvalidNativeHotbarCannotGenerateEvidence() {
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(null));
        Inventory inv = new Inventory();
        inv.slots[2] = new Stack(true, 32);
        inv.selected = -1;
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(inv));
        inv.selected = 9;
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(inv));
        inv.selected = 2;
        inv.throwOnRead = true;
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(inv));
        assertEquals(0, inv.setterCalls);
    }

    @Test void primitiveNameNeverQualifiesAsItemBlock() {
        Inventory inv = new Inventory();
        inv.slots[2] = new Minecraft189ItemStackAccess() {
            public String customMcDisplayName() { return "Stone"; }
            public int customMcStackSize() { return 64; }
            public int customMcItemDamage() { return 0; }
            public int customMcMaxDamage() { return 0; }
        };
        assertNull(Minecraft189ScaffoldSelectedHotbar.from(inv),
                "display-name heuristics must not create ItemBlock authority");
    }
}
