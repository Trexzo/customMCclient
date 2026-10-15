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

    private static final class OriginalPlayer implements Minecraft189PlayerInventoryAccess {
        final Minecraft189InventoryHotbarAccess inventory;
        OriginalPlayer(Minecraft189InventoryHotbarAccess inventory) {
            this.inventory = inventory;
        }
        @Override public Minecraft189InventoryHotbarAccess customMcInventory() {
            return inventory;
        }
    }

    @Test void v49MenuPrearmRequiresRealSelectedEightBlockGrantNotCreativeStartup() {
        final Inventory inv = new Inventory();
        final OriginalPlayer player = new OriginalPlayer(inv);
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(null, 8));
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(new Object(), 8));
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(player, 8),
                "empty startup inventory cannot arm the GUI-negative fixture");
        inv.slots[3] = new Stack(true, 8);
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(player, 8),
                "block in a NON-selected slot cannot authorize a native arm");
        inv.slots[2] = new Stack(true, 7);
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(player, 8),
                "server-debited seven-block stack is not the eight-grant baseline");
        inv.slots[2] = new Stack(false, 8);
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(player, 8),
                "non-block items do not grant placement authority");
        inv.slots[2] = new Stack(true, 8);
        assertTrue(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(player, 8),
                "real original selected 8-block stack proves grant readiness");
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(player, 9));
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(player, 0));
        assertEquals(0, inv.setterCalls,
                "neither test nor read-only readiness changes original hotbar selection");
        assertEquals(2, inv.selected);
        inv.throwOnRead = true;
        assertFalse(Minecraft189ScaffoldSelectedHotbar
                .hasGenuineSelectedBlockCount(player, 8),
                "unavailable native inventory must fail shut");
    }

    @Test void v49NoStartupTicksCarryIntoSixtyPostGrantGuiClearTicks() {
        final Inventory inventory = new Inventory();
        final OriginalPlayer nativePlayer = new OriginalPlayer(inventory);
        final Object minecraft = new Object(), world = new Object();
        final Minecraft189ScaffoldGuiClearStability gate =
                new Minecraft189ScaffoldGuiClearStability();
        // Real game may spend hundreds of ticks in startup World while no
        // native selected ItemBlock has been granted by dedicated server.
        for (int tick = 0; tick < 200; tick++) {
            assertFalse(gate.observe(minecraft, nativePlayer, world,
                    Minecraft189ScaffoldSelectedHotbar
                            .hasGenuineSelectedBlockCount(nativePlayer, 8)));
        }
        assertEquals(0, gate.witnessedClearTicks());
        inventory.slots[2] = new Stack(true, 8);
        for (int tick = 1; tick < 60; tick++) {
            assertFalse(gate.observe(minecraft, nativePlayer, world,
                    Minecraft189ScaffoldSelectedHotbar
                            .hasGenuineSelectedBlockCount(nativePlayer, 8)));
            assertEquals(tick, gate.witnessedClearTicks());
        }
        assertTrue(gate.observe(minecraft, nativePlayer, world,
                Minecraft189ScaffoldSelectedHotbar
                        .hasGenuineSelectedBlockCount(nativePlayer, 8)),
                "only sixty NEW post-grant original ticks permit the module arm");
        // A genuine Survival debit or menu-open transition revokes readiness.
        inventory.slots[2] = new Stack(true, 7);
        assertFalse(gate.observe(minecraft, nativePlayer, world,
                Minecraft189ScaffoldSelectedHotbar
                        .hasGenuineSelectedBlockCount(nativePlayer, 8)));
        assertEquals(0, gate.witnessedClearTicks());
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
