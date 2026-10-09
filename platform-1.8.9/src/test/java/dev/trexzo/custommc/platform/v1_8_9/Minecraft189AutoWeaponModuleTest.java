package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189AutoWeaponModuleTest {
    @Test void selectsHighestVerifiedSwordBaseDamage() {
        final Minecraft189AutoWeaponModule m = new Minecraft189AutoWeaponModule();
        final Inventory i = new Inventory();
        i.selected = 0;
        i.items[1] = new Item(true, 6, 1);
        i.items[3] = new Item(true, 7, 1);
        i.items[4] = new Item(true, 8, 1);
        i.items[6] = new Item(false, 100, 1); // diamond axe-looking non-sword
        i.items[7] = new Item(true, Float.NaN, 1);
        i.items[8] = new Item(true, 25, 0); // empty stack
        assertEquals(-1, m.select(i, true, false)); // disabled
        m.onEnable();
        assertEquals(-1, m.select(i, false, false));
        assertEquals(-1, m.select(i, true, true));
        assertEquals(0, m.select(i, true, false));
        assertEquals(4, i.selected);
        m.restore(i, 0);
        assertEquals(0, i.selected);
        assertEquals(0, m.select(i, true, false));
        i.selected = 3; // manual slot change between select and restore
        m.restore(i, 0);
        assertEquals(3, i.selected);
        m.onDisable();
        assertEquals(-1, m.select(i, true, false));
    }

    @Test void currentSwordWinsTieAndNoKnownSwordMakesNoChange() {
        final Minecraft189AutoWeaponModule m = new Minecraft189AutoWeaponModule();
        final Inventory i = new Inventory();
        i.items[1] = new Item(true, 7, 1);
        i.items[4] = new Item(true, 7, 1);
        i.selected = 4;
        m.onEnable();
        assertEquals(-1, m.select(i, true, false)); // existing 7-damage sword wins
        assertEquals(4, i.selected);
        i.selected = 8;
        assertEquals(8, m.select(i, true, false)); // lowest slot tied sword
        assertEquals(1, i.selected);
        m.restore(i, 8);
        assertEquals(8, i.selected);
        i.items[1] = new Item(false, 100, 1);
        i.items[4] = new Item(true, -1, 1);
        assertEquals(-1, m.select(i, true, false));
        assertEquals(8, i.selected);
        i.items = null;
        assertEquals(-1, m.select(i, true, false));
        i.items = new Minecraft189ItemStackAccess[4];
        assertEquals(-1, m.select(i, true, false));
        i.items = new Minecraft189ItemStackAccess[9];
        i.selected = 9;
        assertEquals(-1, m.select(i, true, false));
        i.selected = -1;
        assertEquals(-1, m.select(i, true, false));
        m.onDisable();
    }

    @Test void restoreOffAndOtherSlotModulesRemainSeparate() {
        final Minecraft189AutoWeaponModule m = new Minecraft189AutoWeaponModule();
        final Inventory i = new Inventory();
        i.selected = 0;
        i.items[1] = new Item(true, 8, 1);
        m.onEnable();
        m.restoreAfterAttackSetting().set(false);
        assertEquals(0, m.select(i, true, false));
        m.restore(i, 0);
        assertEquals(1, i.selected);
        m.requirePlayerHitSetting().set(false);
        i.selected = 0;
        assertEquals(0, m.select(i, false, false));
        m.restore(i, 0);
        assertEquals(1, i.selected);
        m.onDisable();
    }

    @Test void registersPersistsAndCloses() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189AutoWeaponFeature f = Minecraft189AutoWeaponFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings), settings,
                new SettingPresentationRegistry());
        try {
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoWeaponModule.RESTORE));
            f.module().restoreAfterAttackSetting().set(false);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoWeaponModule.RESTORE));
            controller.enable(Minecraft189AutoWeaponModule.ID);
        } finally {
            f.close();
            f.close();
        }
        assertNull(modules.find(Minecraft189AutoWeaponModule.ID));
        assertNull(settings.find(Minecraft189AutoWeaponModule.RESTORE));
    }

    private static final class Inventory implements Minecraft189InventoryHotbarItemsAccess {
        int selected;
        Minecraft189ItemStackAccess[] items = new Minecraft189ItemStackAccess[9];
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public void customMcSetSelectedHotbarSlot(int slot) { selected = slot; }
        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() { return items; }
    }

    private static final class Item implements Minecraft189ItemStackAccess {
        final boolean sword;
        final float damage;
        final int count;
        Item(boolean sword, float damage, int count) {
            this.sword = sword;
            this.damage = damage;
            this.count = count;
        }
        @Override public String customMcDisplayName() { return "unnamed"; }
        @Override public int customMcStackSize() { return count; }
        @Override public int customMcItemDamage() { return 0; }
        @Override public int customMcMaxDamage() { return 0; }
        @Override public boolean customMcIsSword() { return sword; }
        @Override public float customMcSwordBaseDamage() { return damage; }
    }
}
