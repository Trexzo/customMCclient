package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189AutoRodModuleTest {
    @Test void cooldownGatesOneUseWithoutBankingInvalidTurns() {
        final Minecraft189AutoRodModule mod = new Minecraft189AutoRodModule();
        mod.cooldownTicksSetting().set(2);
        assertFalse(mod.shouldUse(true, true, false, false, true));
        mod.onEnable();
        assertFalse(mod.shouldUse(false, true, false, false, true));
        assertFalse(mod.shouldUse(true, false, false, false, true));
        assertFalse(mod.shouldUse(true, true, true, false, true));
        assertFalse(mod.shouldUse(true, true, false, true, true));
        assertFalse(mod.shouldUse(true, true, false, false, false));
        assertTrue(mod.shouldUse(true, true, false, false, true));
        // Item verification failed: another eligible attempt is immediate.
        assertTrue(mod.shouldUse(true, true, false, false, true));
        mod.commitUseAttempt(); // only after verified native use-item
        assertFalse(mod.shouldUse(true, true, false, false, true));
        assertFalse(mod.shouldUse(true, true, false, false, true));
        assertTrue(mod.shouldUse(true, true, false, false, true));
        mod.commitUseAttempt();
        assertFalse(mod.shouldUse(false, true, false, false, true));
        assertTrue(mod.shouldUse(true, true, false, false, true));
        mod.onDisable();
        assertFalse(mod.shouldUse(true, true, false, false, true));
    }

    @Test void slotSelectionAlwaysRestoresAndPreservesUserChanges() {
        final Minecraft189AutoRodModule mod = new Minecraft189AutoRodModule();
        final Inventory inventory = new Inventory();
        inventory.slot = 4;
        mod.onEnable();
        assertEquals(4, mod.selectSlot(inventory));
        assertEquals(1, inventory.slot);
        mod.restoreSlot(inventory, 4);
        assertEquals(4, inventory.slot);
        mod.selectSlot(inventory);
        inventory.slot = 5;
        mod.restoreSlot(inventory, 4);
        assertEquals(5, inventory.slot);
        inventory.slot = -1;
        assertEquals(-1, mod.selectSlot(inventory));
        inventory.slot = 9;
        assertEquals(-1, mod.selectSlot(inventory));
        assertThrows(IllegalArgumentException.class,
                () -> mod.rodSlotSetting().set(0));
        assertThrows(IllegalArgumentException.class,
                () -> mod.cooldownTicksSetting().set(201));
        mod.onDisable();
    }

    @Test void originalSlotLeaseSurvivesConfigChangeAndNeverOverwritesOtherOwner() {
        final Minecraft189AutoRodModule module = new Minecraft189AutoRodModule();
        final Inventory inventory = new Inventory();
        inventory.slot = 6;
        final Inventory foreign = new Inventory();
        foreign.slot = 7;
        module.onEnable();
        try {
            assertEquals(6, module.selectSlot(inventory));
            assertEquals(1, inventory.slot);
            assertEquals(-1, module.selectSlot(inventory),
                    "nested native item selection must not consume a second slot lease");
            module.rodSlotSetting().set(8);
            module.restoreSlot(foreign, 6);
            assertEquals(7, foreign.slot, "foreign inventory has no native ownership");
            module.restoreSlot(inventory, 6);
            assertEquals(6, inventory.slot,
                    "changing configured slot must never strand a prior native selection");

            module.rodSlotSetting().set(9);
            assertEquals(6, module.selectSlot(inventory));
            assertEquals(8, inventory.slot);
            inventory.slot = 4;
            module.restoreSlot(inventory, 6);
            assertEquals(4, inventory.slot,
                    "do not override a real human or other actor's hotbar change");

            inventory.slot = 3;
            assertEquals(3, module.selectSlot(inventory));
            module.onDisable();
            module.restoreSlot(inventory, 3);
            assertEquals(3, inventory.slot,
                    "disable before finally must preserve the synchronous slot rollback");
        } finally { module.onDisable(); }
    }

    @Test void partiallyAppliedNativeSelectionExceptionRestoresOwnedHotbar() {
        final Minecraft189AutoRodModule module = new Minecraft189AutoRodModule();
        module.onEnable();
        final ThrowingInventory inventory = new ThrowingInventory();
        inventory.slot = 5;
        inventory.throwAfterSelect = true;
        try {
            assertThrows(IllegalStateException.class,
                    () -> module.selectSlot(inventory));
            assertEquals(5, inventory.slot,
                    "native setter that mutates then throws must roll back its own slot");
            assertEquals(5, module.selectSlot(inventory),
                    "failed selection must not leave a zombie slot lease");
            module.restoreSlot(inventory, 5);
            assertEquals(5, inventory.slot);
        } finally { module.onDisable(); }
    }

    @Test void registrationAndPersistenceAreClean() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189AutoRodFeature feature = Minecraft189AutoRodFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoRodModule.PROTECT_ROD_DURABILITY));
            assertEquals("3", settings.snapshotEncoded().get(
                    Minecraft189AutoRodModule.MIN_REMAINING_DURABILITY));
            feature.module().protectRodDurabilitySetting().set(Boolean.TRUE);
            feature.module().minimumRemainingDurabilitySetting().set(5);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoRodModule.PROTECT_ROD_DURABILITY));
            assertEquals("5", settings.snapshotEncoded().get(
                    Minecraft189AutoRodModule.MIN_REMAINING_DURABILITY));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().minimumRemainingDurabilitySetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().minimumRemainingDurabilitySetting().set(65));
            feature.module().rodSlotSetting().set(9);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoRodModule.PREFER_DURABLE_ROD));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoRodModule.PRESERVE_ACTIVE_HOOK));
            feature.module().preserveActiveHookSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoRodModule.PRESERVE_ACTIVE_HOOK));
            feature.module().preferDurableRodSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoRodModule.PREFER_DURABLE_ROD));
            feature.module().autoDetectSlotSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(Minecraft189AutoRodModule.AUTO_DETECT_SLOT));
            assertEquals("9", settings.snapshotEncoded().get(Minecraft189AutoRodModule.ROD_SLOT));
            controller.enable(Minecraft189AutoRodModule.ID);
        } finally { feature.close(); feature.close(); }
        assertNull(modules.find(Minecraft189AutoRodModule.ID));
        assertNull(settings.find(Minecraft189AutoRodModule.ROD_SLOT));
    }
    @Test void everyRodUseSettingRevokesLeaseWithoutStealingUserHotbar() {
        final Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        final Inventory inventory = new Inventory();
        inventory.slot = 4;
        rod.onEnable();
        try {
            final Runnable[] changes = {
                () -> rod.rodSlotSetting().set(3),
                () -> rod.autoDetectSlotSetting().set(true),
                () -> rod.preferDurableRodSetting().set(true),
                () -> rod.protectRodDurabilitySetting().set(true),
                () -> rod.minimumRemainingDurabilitySetting().set(5),
                () -> rod.preserveActiveHookSetting().set(true),
                () -> rod.requireHoldSetting().set(false),
                () -> rod.pauseRightSetting().set(false),
                () -> rod.cooldownTicksSetting().set(45)
            };
            for (final Runnable change : changes) {
                assertEquals(4, rod.selectSlot(inventory));
                assertTrue(rod.ownsSelectedSlot(inventory, 4));
                change.run();
                assertFalse(rod.ownsSelectedSlot(inventory, 4),
                        "an in-flight right click must keep its original GUI policy");
                rod.restoreSlot(inventory, 4);
                assertEquals(4, inventory.slot);
            }
            assertEquals(4, rod.selectSlot(inventory));
            assertTrue(rod.ownsSelectedSlot(inventory, 4),
                    "a new admission with new settings may own a new action");
            rod.restoreSlot(inventory, 4);
        } finally { rod.onDisable(); }
    }

    @Test void nativeRODLeaseRejectsDisableReenableSameSourcePolicy() {
        final Minecraft189AutoRodModule mod = new Minecraft189AutoRodModule();
        final Inventory inv = new Inventory();
        inv.slot = 5;
        mod.onEnable();
        assertEquals(5, mod.selectSlot(inv));
        assertTrue(mod.ownsSelectedSlot(inv, 5));
        mod.onDisable();
        mod.onEnable();
        try {
            assertFalse(mod.ownsSelectedSlot(inv, 5),
                    "same original currentItem and GUI policy cannot carry prior module lifecycle");
            mod.restoreSlot(inv, 5);
            assertEquals(5, inv.slot, "original finally restores own old source slot");
            assertEquals(5, mod.selectSlot(inv));
            assertTrue(mod.ownsSelectedSlot(inv, 5),
                    "new original selection owns new enabled module epoch");
            mod.restoreSlot(inv, 5);
        } finally { mod.onDisable(); }
    }

    @Test void nativeRODSetterReentryMayNotPublishStaleUseItemPermission() {
        final Minecraft189AutoRodModule mod = new Minecraft189AutoRodModule();
        final ReentrantInventory inv = new ReentrantInventory();
        inv.slot = 4;
        mod.onEnable();
        try {
            inv.afterSetter = () -> mod.requireHoldSetting().set(false);
            assertEquals(-1, mod.selectSlot(inv),
                    "changed GUI policy during original setter revokes use");
            assertEquals(4, inv.slot, "only owned original source item restores");
            mod.requireHoldSetting().set(true);
            inv.afterSetter = () -> { mod.onDisable(); mod.onEnable(); };
            assertEquals(-1, mod.selectSlot(inv),
                    "native disable/reenable during hotbar setter revokes lease");
            assertEquals(4, inv.slot);
            assertEquals(4, mod.selectSlot(inv));
            assertTrue(mod.ownsSelectedSlot(inv, 4));
            mod.restoreSlot(inv, 4);
        } finally { mod.onDisable(); }
    }

    @Test void nativeRODCurrentItemGetterReentryMustBeRecheckedAtUse() {
        final Minecraft189AutoRodModule mod = new Minecraft189AutoRodModule();
        final ReentrantInventory inv = new ReentrantInventory();
        inv.slot = 6;
        mod.onEnable();
        try {
            assertEquals(6, mod.selectSlot(inv));
            inv.afterGetter = () -> mod.requireHoldSetting().set(false);
            assertFalse(mod.ownsSelectedSlot(inv, 6),
                    "native accessor may reenter a new policy during use");
            mod.restoreSlot(inv, 6);
            assertEquals(6, inv.slot);
            mod.requireHoldSetting().set(true);
            assertEquals(6, mod.selectSlot(inv));
            inv.afterGetter = () -> inv.slot = 7;
            assertFalse(mod.ownsSelectedSlot(inv, 6),
                    "original currentItem changed by player during the getter");
            mod.restoreSlot(inv, 6);
            assertEquals(7, inv.slot,
                    "never overwrite actual human-selected currentItem");
        } finally { mod.onDisable(); }
    }

    @Test void completedNativeRodUseCooldownCommitsExactlyOnceForOriginalLease() {
        final Minecraft189AutoRodModule mod = new Minecraft189AutoRodModule();
        final Inventory source = new Inventory();
        source.slot = 5;
        mod.onEnable();
        try {
            assertEquals(5, mod.selectSlot(source));
            assertEquals(1, source.slot);
            assertTrue(mod.ownsSelectedSlot(source, 5));
            assertTrue(mod.commitCompletedOriginalUse(source, 5),
                    "real completed original use spends exactly this action");
            assertFalse(mod.commitCompletedOriginalUse(source, 5),
                    "duplicate original callback cannot reuse cooldown token");
            mod.restoreSlot(source, 5);
            assertFalse(mod.commitCompletedOriginalUse(source, 5),
                    "restored source inventory is no longer owned");
            assertEquals(5, source.slot);
            assertEquals(5, mod.selectSlot(source));
            assertTrue(mod.commitCompletedOriginalUse(source, 5),
                    "independent genuine action has independent token");
            mod.restoreSlot(source, 5);
        } finally { mod.onDisable(); }
    }

    @Test void reactivatedRodModuleCannotBankOldOriginalUseCooldown() {
        final Minecraft189AutoRodModule mod = new Minecraft189AutoRodModule();
        final Inventory source = new Inventory();
        source.slot = 6;
        mod.onEnable();
        assertEquals(6, mod.selectSlot(source));
        mod.onDisable();
        mod.onEnable();
        try {
            assertFalse(mod.commitCompletedOriginalUse(source, 6),
                    "old native rightClick returning after OFF-ON must not debit new lifetime");
            mod.restoreSlot(source, 6);
            assertEquals(6, source.slot);
            assertEquals(6, mod.selectSlot(source));
            assertTrue(mod.commitCompletedOriginalUse(source, 6),
                    "new native rightClick can correctly bank current cooldown");
            mod.restoreSlot(source, 6);
        } finally { mod.onDisable(); }
    }

    @Test void wrongNativeRodOwnerOrSlotCannotSpendAnyOriginalCooldown() {
        final Minecraft189AutoRodModule mod = new Minecraft189AutoRodModule();
        final Inventory source = new Inventory();
        final Inventory other = new Inventory();
        source.slot = 4;
        other.slot = 4;
        mod.onEnable();
        try {
            assertFalse(mod.commitCompletedOriginalUse(source, 4),
                    "no original hotbar selection, no native use credit");
            assertEquals(4, mod.selectSlot(source));
            assertFalse(mod.commitCompletedOriginalUse(other, 4),
                    "foreign source player/inventory is never this native use");
            assertFalse(mod.commitCompletedOriginalUse(source, 3),
                    "wrong captured original source slot cannot commit");
            // The original right-click can genuinely consume a potion or
            // invoke a native hotbar callback; token ownership is pinned
            // to the source at admission rather than a post-use stack read.
            source.slot = 7;
            assertTrue(mod.commitCompletedOriginalUse(source, 4),
                    "completed action belongs to original source even after use-time slot change");
            mod.restoreSlot(source, 4);
            assertEquals(7, source.slot,
                    "real player-selected slot must not be overwritten");
        } finally { mod.onDisable(); }
    }

    private static final class ReentrantInventory
            implements Minecraft189InventoryHotbarControl {
        int slot;
        Runnable afterSetter;
        Runnable afterGetter;
        @Override public int customMcSelectedHotbarSlot() {
            final int original = slot;
            final Runnable callback = afterGetter;
            afterGetter = null;
            if (callback != null) callback.run();
            return original;
        }
        @Override public void customMcSetSelectedHotbarSlot(int next) {
            slot = next;
            final Runnable callback = afterSetter;
            afterSetter = null;
            if (callback != null) callback.run();
        }
    }

    private static final class ThrowingInventory
            implements Minecraft189InventoryHotbarControl {
        private int slot;
        private boolean throwAfterSelect;
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int next) {
            slot = next;
            if (throwAfterSelect) {
                throwAfterSelect = false;
                throw new IllegalStateException("native slot selection threw after setting slot");
            }
        }
    }

    private static final class Inventory implements Minecraft189InventoryHotbarControl {
        private int slot;
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int next) { slot = next; }
    }
}
