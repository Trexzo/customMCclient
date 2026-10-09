package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189VerifiedCombatItemSlotTest {
    @Test void onlyActualMappedRodTypeCanOwnRightClick() {
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        rod.onEnable();
        inventory.selected = 4;
        player.items[1] = new FakeItem(true, false);
        assertEquals(4, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, player));
        assertEquals(1, inventory.selected);
        rod.restoreSlot(inventory, 4);
        assertEquals(4, inventory.selected);

        player.items[1] = new FakeItem(false, true); // potion is not a rod
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, player));
        assertEquals(4, inventory.selected);

        player.items[1] = new FakeItem(false, false); // sword/other/unknown
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, player));
        assertEquals(4, inventory.selected);

        player.items[1] = null;
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, player));
        assertEquals(4, inventory.selected);
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectRod(
                rod, inventory, null));
        assertEquals(4, inventory.selected);
        rod.onDisable();
    }

    @Test void onlyActualPotionAndSafeRestoration() {
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        final Inventory inventory = new Inventory();
        final Player player = new Player(inventory);
        inventory.selected = 7;
        pot.onEnable();
        player.items[2] = new FakeItem(false, true);
        assertEquals(7, Minecraft189VerifiedCombatItemSlot.selectPotion(
                pot, inventory, player));
        assertEquals(2, inventory.selected);
        inventory.selected = 6; // user changed slot during use
        pot.restoreSlot(inventory, 7);
        assertEquals(6, inventory.selected);
        player.items[2] = new FakeItem(true, false);
        assertEquals(-1, Minecraft189VerifiedCombatItemSlot.selectPotion(
                pot, inventory, player));
        assertEquals(6, inventory.selected);
        pot.onDisable();
    }

    @Test void failureToReadNewlySelectedItemStillRestoresSlot() {
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        rod.onEnable();
        final Inventory inventory = new Inventory();
        inventory.selected = 5;
        final Minecraft189PlayerHeldItemAccess broken = () -> {
            throw new IllegalStateException("item access failed");
        };
        assertThrows(IllegalStateException.class,
                () -> Minecraft189VerifiedCombatItemSlot.selectRod(
                        rod, inventory, broken));
        assertEquals(5, inventory.selected);
        rod.onDisable();
    }

    private static final class Inventory implements Minecraft189InventoryHotbarControl {
        int selected;
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public void customMcSetSelectedHotbarSlot(final int slot) {
            selected = slot;
        }
    }

    private static final class Player implements Minecraft189PlayerHeldItemAccess {
        final Inventory inventory;
        final Minecraft189ItemStackAccess[] items = new Minecraft189ItemStackAccess[9];
        Player(final Inventory inventory) { this.inventory = inventory; }
        @Override public Minecraft189ItemStackAccess customMcHeldItem() {
            return items[inventory.selected];
        }
    }

    private static final class FakeItem implements Minecraft189ItemStackAccess {
        final boolean rod;
        final boolean potion;
        FakeItem(final boolean rod, final boolean potion) {
            this.rod = rod;
            this.potion = potion;
        }
        @Override public String customMcDisplayName() { return "untrusted"; }
        @Override public int customMcStackSize() { return 1; }
        @Override public int customMcItemDamage() { return 0; }
        @Override public int customMcMaxDamage() { return 0; }
        @Override public boolean customMcIsFishingRod() { return rod; }
        @Override public boolean customMcIsPotion() { return potion; }
    }
}
