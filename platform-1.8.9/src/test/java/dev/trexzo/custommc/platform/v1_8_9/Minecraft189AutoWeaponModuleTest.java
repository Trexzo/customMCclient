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


    @Test void genuineSourceDurabilityAvoidsNearBrokenStrongestSword() {
        final Minecraft189AutoWeaponModule m = new Minecraft189AutoWeaponModule();
        final Inventory i = new Inventory();
        i.selected = 0;
        final Item diamond = new Item(true, 7F, 1);
        diamond.maxDurability = 1561;
        diamond.usedDurability = 1558; // three uses left
        final Item iron = new Item(true, 6F, 1);
        iron.maxDurability = 250;
        iron.usedDurability = 12;
        i.items[2] = diamond;
        i.items[6] = iron;
        m.onEnable();
        try {
            assertEquals(0, m.select(i, true, false));
            assertEquals(2, i.selected, "default remains strongest original sword");
            m.restore(i, 0);
            m.ignoreFragileSwordsSetting().set(Boolean.TRUE);
            assertEquals(0, m.select(i, true, false));
            assertEquals(6, i.selected,
                    "original almost-broken diamond sword must be skipped");
            assertTrue(m.ownsVerifiedSwordAtDispatch(i, 0));
            m.restore(i, 0);
            m.minRemainingDurabilitySetting().set(12);
            assertEquals(0, m.select(i, true, false));
            assertEquals(6, i.selected);
            iron.usedDurability = 247;
            assertFalse(m.ownsVerifiedSwordAtDispatch(i, 0),
                    "source native iron sword now has under 12 durability");
            m.restore(i, 0);
            assertEquals(0, i.selected);
            assertEquals(-1, m.select(i, true, false),
                    "no verified durable sword must not create hotbar lease");
            assertEquals(0, i.selected);
            m.minRemainingDurabilitySetting().set(1);
            assertEquals(0, m.select(i, true, false));
            assertEquals(2, i.selected,
                    "relaxed threshold may use genuine source 3-use diamond");
            m.restore(i, 0);
        } finally { m.onDisable(); }
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


    @Test void nativeEqualDamageChoosesDurableTiesOnlyWhenEnabled() {
        final Minecraft189AutoWeaponModule m = new Minecraft189AutoWeaponModule();
        final Inventory i = new Inventory();
        i.selected = 3;
        final Item worn = new Item(true, 7F, 1);
        worn.maxDurability = 1561;
        worn.usedDurability = 1557;
        final Item fresh = new Item(true, 7F, 1);
        fresh.maxDurability = 1561;
        fresh.usedDurability = 0;
        i.items[3] = worn;
        i.items[7] = fresh;
        m.onEnable();
        try {
            assertEquals(-1, m.select(i, true, false),
                    "legacy tied same-damage held source sword wins");
            m.preferDurableTiesSetting().set(Boolean.TRUE);
            assertEquals(3, m.select(i, true, false));
            assertEquals(7, i.selected,
                    "fresh native source sword beats worn same-base-damage held");
            assertTrue(m.ownsVerifiedSwordAtDispatch(i, 3));
            m.preferDurableTiesSetting().set(Boolean.FALSE);
            assertFalse(m.ownsVerifiedSwordAtDispatch(i, 3),
                    "settings changed during attack must revoke lease");
            m.restore(i, 3);
            assertEquals(3, i.selected);
            m.preferDurableTiesSetting().set(Boolean.TRUE);
            worn.usedDurability = 0;
            assertEquals(-1, m.select(i, true, false),
                    "tied original durability still favors human-held sword");
            worn.maxDurability = 0;
            assertEquals(3, m.select(i, true, false),
                    "invalid original durability excludes suspect sword");
            assertEquals(7, i.selected);
            m.restore(i, 3);
        } finally { m.onDisable(); }
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

    @Test void sameNativeSlotLeaseSurvivesReentrancyForeignRestoreAndDisable() {
        final Minecraft189AutoWeaponModule module = new Minecraft189AutoWeaponModule();
        final Inventory inventory = new Inventory();
        final Inventory foreign = new Inventory();
        inventory.selected = 2;
        foreign.selected = 5;
        inventory.items[7] = new Item(true, 8, 1);
        foreign.items[8] = new Item(true, 7, 1);
        module.onEnable();
        try {
            assertEquals(2, module.select(inventory, true, false));
            assertEquals(7, inventory.selected);
            assertEquals(-1, module.select(inventory, true, false),
                    "nested action cannot erase previous native item lease");
            assertEquals(-1, module.select(foreign, true, false),
                    "foreign inventory cannot supersede in-flight native attack");
            module.restore(foreign, 2);
            assertEquals(7, inventory.selected);
            module.onDisable();
            module.restore(inventory, 2);
            assertEquals(2, inventory.selected,
                    "normal synchronous finally restores after module disable");

            module.onEnable();
            assertEquals(2, module.select(inventory, true, false));
            inventory.selected = 4;
            module.restore(inventory, 2);
            assertEquals(4, inventory.selected,
                    "manual hotbar change wins after AutoWeapon selection");
            assertEquals(4, module.select(inventory, true, false),
                    "completed native lease permits another fresh selection");
            module.restore(inventory, 4);
            assertEquals(4, inventory.selected);
        } finally {
            module.onDisable();
        }
    }

    @Test void nativeSetterMutatesThenThrowsButOwnsExactRollback() {
        final Minecraft189AutoWeaponModule module = new Minecraft189AutoWeaponModule();
        final ThrowingInventory inventory = new ThrowingInventory();
        inventory.selected = 0;
        inventory.items[6] = new Item(true, 9, 1);
        module.onEnable();
        try {
            inventory.throwAfterMutation = true;
            assertThrows(IllegalStateException.class,
                    () -> module.select(inventory, true, false));
            assertEquals(0, inventory.selected,
                    "partial native setter failure restores the original slot");
            assertEquals(0, module.select(inventory, true, false),
                    "failure must not leave a zombie/reentrant hotbar lease");
            assertEquals(6, inventory.selected);
            module.restore(inventory, 0);
            assertEquals(0, inventory.selected);
        } finally {
            module.onDisable();
        }
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
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoWeaponModule.IGNORE_FRAGILE_SWORDS));
            f.module().ignoreFragileSwordsSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoWeaponModule.IGNORE_FRAGILE_SWORDS));
            assertEquals("8", settings.snapshotEncoded().get(
                    Minecraft189AutoWeaponModule.MIN_REMAINING_DURABILITY));
            f.module().minRemainingDurabilitySetting().set(Integer.valueOf(5));
            assertEquals("5", settings.snapshotEncoded().get(
                    Minecraft189AutoWeaponModule.MIN_REMAINING_DURABILITY));
            f.module().preferDurableTiesSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoWeaponModule.PREFER_DURABLE_TIES));
            controller.enable(Minecraft189AutoWeaponModule.ID);
        } finally {
            f.close();
            f.close();
        }
        assertNull(modules.find(Minecraft189AutoWeaponModule.ID));
        assertNull(settings.find(Minecraft189AutoWeaponModule.RESTORE));
    }


    @Test void originalItemScanCannotOverwriteReentrantUserHotbarChoice() {
        final Minecraft189AutoWeaponModule module = new Minecraft189AutoWeaponModule();
        final ReentrantInventory inventory = new ReentrantInventory();
        inventory.selected = 2;
        inventory.items[4] = new Item(true, 8.0F, 1);
        module.onEnable();
        try {
            inventory.afterItemRead = () -> inventory.selected = 7;
            assertEquals(-1, module.select(inventory, true, false));
            assertEquals(7, inventory.selected,
                    "reentrant original slot change outranks stale item scan");
            assertEquals(0, inventory.setterCalls,
                    "must not call native setter after old inventory evidence");
            inventory.selected = 2;
            assertEquals(2, module.select(inventory, true, false));
            assertEquals(4, inventory.selected);
            module.restore(inventory, 2);
            assertEquals(2, inventory.selected);
        } finally {
            module.onDisable();
        }
    }

    @Test void ignoredAndRedirectedOriginalWeaponSetterCannotClaimSwitch() {
        final Minecraft189AutoWeaponModule module = new Minecraft189AutoWeaponModule();
        final ReentrantInventory inventory = new ReentrantInventory();
        inventory.selected = 2;
        inventory.items[4] = new Item(true, 8.0F, 1);
        module.onEnable();
        try {
            inventory.ignoreNextSetter = true;
            assertEquals(-1, module.select(inventory, true, false));
            assertEquals(2, inventory.selected);
            inventory.redirectNextSetterTo = 7;
            assertEquals(-1, module.select(inventory, true, false));
            assertEquals(7, inventory.selected,
                    "do not steal a user-selected slot after native redirection");
            inventory.selected = 2;
            assertEquals(2, module.select(inventory, true, false),
                    "failed selection cannot leave another action's lease");
            module.restore(inventory, 2);
            assertEquals(2, inventory.selected);
        } finally {
            module.onDisable();
        }
    }

    @Test void moduleDisabledInsideNativeSwordSetterReturnsToOriginalItem() {
        final Minecraft189AutoWeaponModule module = new Minecraft189AutoWeaponModule();
        final ReentrantInventory inventory = new ReentrantInventory();
        inventory.selected = 2;
        inventory.items[4] = new Item(true, 8.0F, 1);
        module.onEnable();
        inventory.afterSetter = module::onDisable;
        assertEquals(-1, module.select(inventory, true, false));
        assertEquals(2, inventory.selected,
                "a disable during native setter must release its temporary sword");
        module.onEnable();
        try {
            assertEquals(2, module.select(inventory, true, false));
            module.restore(inventory, 2);
            assertEquals(2, inventory.selected);
        } finally {
            module.onDisable();
        }
    }



    @Test void nativeWeaponSetterReplacingSourceSwordFailsBeforeLeasePublication() {
        final Minecraft189AutoWeaponModule module = new Minecraft189AutoWeaponModule();
        final ReentrantInventory inventory = new ReentrantInventory();
        inventory.selected = 2;
        final Object originalNativeSword = new Object();
        inventory.items[4] = new Item(true, 8.0F, 1, originalNativeSword);
        module.onEnable();
        try {
            inventory.afterSetter = () ->
                    inventory.items[4] = new Item(false, 8.0F, 1, originalNativeSword);
            assertEquals(-1, module.select(inventory, true, false),
                    "original native setter replaced selected ItemSword");
            assertEquals(2, inventory.selected,
                    "stale sword selection rolls back before host publishes a lease");
            inventory.items[4] = new Item(true, 8.0F, 1, originalNativeSword);
            inventory.afterSetter = () ->
                    inventory.items[4] = new Item(true, 8.0F, 1, new Object());
            assertEquals(-1, module.select(inventory, true, false),
                    "another source Item singleton with same damage is not original");
            assertEquals(2, inventory.selected);
            inventory.items[4] = new Item(true, 8.0F, 1, originalNativeSword);
            assertEquals(2, module.select(inventory, true, false));
            assertEquals(4, inventory.selected);
            module.restore(inventory, 2);
            assertEquals(2, inventory.selected);
        } finally {
            module.onDisable();
        }
    }

    @Test void reentrantNativeReadAfterWeaponSetterPreservesUserSlotAndClearsLease() {
        final Minecraft189AutoWeaponModule module = new Minecraft189AutoWeaponModule();
        final ReentrantInventory inventory = new ReentrantInventory();
        inventory.selected = 2;
        inventory.items[4] = new Item(true, 8.0F, 1);
        module.onEnable();
        try {
            inventory.afterSetter = () ->
                    inventory.afterItemRead = () -> inventory.selected = 7;
            assertEquals(-1, module.select(inventory, true, false));
            assertEquals(7, inventory.selected,
                    "the user changed currentItem while source stack was read");
            assertEquals(7, module.select(inventory, true, false),
                    "failed reentrant source read leaves no zombie lease");
            assertEquals(4, inventory.selected);
            module.restore(inventory, 7);
            assertEquals(7, inventory.selected);
            // A fresh action with the original slot must still succeed.
            inventory.selected = 2;
            assertEquals(2, module.select(inventory, true, false));
            module.restore(inventory, 2);
            assertEquals(2, inventory.selected);
        } finally {
            module.onDisable();
        }
    }

    private static final class ReentrantInventory
            implements Minecraft189InventoryHotbarItemsAccess {
        int selected;
        int setterCalls;
        int redirectNextSetterTo = -1;
        boolean ignoreNextSetter;
        Runnable afterItemRead;
        Runnable afterSetter;
        Minecraft189ItemStackAccess[] items = new Minecraft189ItemStackAccess[9];

        @Override public int customMcSelectedHotbarSlot() { return selected; }

        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() {
            final Runnable callback = afterItemRead;
            afterItemRead = null;
            if (callback != null) callback.run();
            return items;
        }

        @Override public void customMcSetSelectedHotbarSlot(final int next) {
            setterCalls++;
            if (ignoreNextSetter) {
                ignoreNextSetter = false;
                return;
            }
            if (redirectNextSetterTo >= 0) {
                selected = redirectNextSetterTo;
                redirectNextSetterTo = -1;
                return;
            }
            selected = next;
            final Runnable callback = afterSetter;
            afterSetter = null;
            if (callback != null) callback.run();
        }
    }

    private static final class ThrowingInventory
            implements Minecraft189InventoryHotbarItemsAccess {
        int selected;
        boolean throwAfterMutation;
        Minecraft189ItemStackAccess[] items = new Minecraft189ItemStackAccess[9];
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() { return items; }
        @Override public void customMcSetSelectedHotbarSlot(final int next) {
            selected = next;
            if (throwAfterMutation) {
                throwAfterMutation = false;
                throw new IllegalStateException("real hotbar setter mutated before error");
            }
        }
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
        final Object originalNativeItem;
        int maxDurability;
        int usedDurability;
        Item(boolean sword, float damage, int count) {
            this(sword, damage, count, null);
        }
        Item(boolean sword, float damage, int count, Object nativeItem) {
            this.sword = sword;
            this.damage = damage;
            this.count = count;
            this.originalNativeItem = nativeItem;
        }
        @Override public String customMcDisplayName() { return "unnamed"; }
        @Override public int customMcStackSize() { return count; }
        @Override public int customMcItemDamage() { return usedDurability; }
        @Override public int customMcMaxDamage() { return maxDurability; }
        @Override public boolean customMcIsSword() { return sword; }
        @Override public Object customMcOriginalItemIdentity() {
            return originalNativeItem;
        }
        @Override public float customMcSwordBaseDamage() { return damage; }
    }
}
