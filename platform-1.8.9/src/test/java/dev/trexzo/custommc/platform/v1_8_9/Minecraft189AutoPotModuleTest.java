package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189AutoPotModuleTest {
    @Test void thresholdCooldownAndInvalidEvidenceAreFailClosed() {
        final Minecraft189AutoPotModule mod = new Minecraft189AutoPotModule();
        final Minecraft189PlayerHealthState health = new Minecraft189PlayerHealthState();
        assertFalse(mod.shouldUse(health.snapshot(), false, false, false, true));
        mod.cooldownTicksSetting().set(2);
        mod.onEnable();
        assertFalse(mod.shouldUse(health.snapshot(), false, false, false, true));
        health.update(8, 20);
        assertFalse(mod.shouldUse(health.snapshot(), false, false, false, true));
        health.update(7, 20);
        assertTrue(mod.shouldUse(health.snapshot(), false, false, false, true));
        // Missing/wrong selected potion must not spend scheduler cooldown.
        assertTrue(mod.shouldUse(health.snapshot(), false, false, false, true));
        mod.commitUseAttempt();
        assertFalse(mod.shouldUse(health.snapshot(), false, false, false, true));
        assertFalse(mod.shouldUse(health.snapshot(), false, false, false, true));
        assertTrue(mod.shouldUse(health.snapshot(), false, false, false, true));
        assertFalse(mod.shouldUse(health.snapshot(), false, true, false, true));
        assertFalse(mod.shouldUse(health.snapshot(), false, false, true, true));
        assertFalse(mod.shouldUse(health.snapshot(), false, false, false, false));
        mod.requireCombatTargetSetting().set(true);
        assertFalse(mod.shouldUse(health.snapshot(), false, false, false, true));
        assertTrue(mod.shouldUse(health.snapshot(), true, false, false, true));
        health.update(0, 20);
        assertFalse(mod.shouldUse(health.snapshot(), true, false, false, true));
        mod.onDisable();
    }

    @Test void slotRestoreDoesNotOverrideOtherActor() {
        final Minecraft189AutoPotModule mod = new Minecraft189AutoPotModule();
        final Inventory inv = new Inventory();
        inv.slot = 7;
        mod.onEnable();
        assertEquals(7, mod.selectSlot(inv));
        assertEquals(2, inv.slot);
        mod.restoreSlot(inv, 7);
        assertEquals(7, inv.slot);
        assertEquals(7, mod.selectSlot(inv));
        inv.slot = 5;
        mod.restoreSlot(inv, 7);
        assertEquals(5, inv.slot);
        inv.slot = -1;
        assertEquals(-1, mod.selectSlot(inv));
        assertThrows(IllegalArgumentException.class,
                () -> mod.potSlotSetting().set(0));
        assertThrows(IllegalArgumentException.class,
                () -> mod.healthThresholdSetting().set(100D));
        mod.onDisable();
    }

    @Test void originalSlotLeaseSurvivesConfigChangeAndNeverOverwritesOtherOwner() {
        final Minecraft189AutoPotModule module = new Minecraft189AutoPotModule();
        final Inventory inventory = new Inventory();
        inventory.slot = 6;
        final Inventory foreign = new Inventory();
        foreign.slot = 7;
        module.onEnable();
        try {
            assertEquals(6, module.selectSlot(inventory));
            assertEquals(2, inventory.slot);
            assertEquals(-1, module.selectSlot(inventory),
                    "nested native item selection must not consume a second slot lease");
            module.potSlotSetting().set(8);
            module.restoreSlot(foreign, 6);
            assertEquals(7, foreign.slot, "foreign inventory has no native ownership");
            module.restoreSlot(inventory, 6);
            assertEquals(6, inventory.slot,
                    "changing configured slot must never strand a prior native selection");

            module.potSlotSetting().set(9);
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
        final Minecraft189AutoPotModule module = new Minecraft189AutoPotModule();
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

    @Test void persistsAndUnregistersSettings() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189AutoPotFeature f = Minecraft189AutoPotFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            assertEquals("35.0", settings.snapshotEncoded().get(Minecraft189AutoPotModule.HEALTH_THRESHOLD));
            f.module().healthThresholdSetting().set(25D);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoPotModule.PREFER_IMMEDIATE_HEALING));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoPotModule.AVOID_ACTIVE_REGENERATION));
            f.module().avoidActiveRegenerationSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoPotModule.AVOID_ACTIVE_REGENERATION));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoPotModule.AIM_DOWN_FOR_SPLASH));
            f.module().aimDownForSplashSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoPotModule.AIM_DOWN_FOR_SPLASH));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoPotModule.CONSERVE_INSTANT_HEALING_II));
            f.module().conserveInstantHealingIISetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoPotModule.CONSERVE_INSTANT_HEALING_II));
            f.module().preferImmediateHealingSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoPotModule.PREFER_IMMEDIATE_HEALING));
            f.module().autoDetectSlotSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(Minecraft189AutoPotModule.AUTO_DETECT_SLOT));
            assertEquals("25.0", settings.snapshotEncoded().get(Minecraft189AutoPotModule.HEALTH_THRESHOLD));
            controller.enable(Minecraft189AutoPotModule.ID);
        } finally { f.close(); f.close(); }
        assertNull(modules.find(Minecraft189AutoPotModule.ID));
        assertNull(settings.find(Minecraft189AutoPotModule.HEALTH_THRESHOLD));
    }

    @Test void allPotionUseSettingsRevokeOldLeaseButAllowOwnedRestoration() {
        final Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        final Inventory inventory = new Inventory();
        inventory.slot = 4;
        pot.onEnable();
        try {
            final Runnable[] changes = {
                () -> pot.potSlotSetting().set(4),
                () -> pot.autoDetectSlotSetting().set(true),
                () -> pot.preferImmediateHealingSetting().set(true),
                () -> pot.conserveInstantHealingIISetting().set(true),
                () -> pot.avoidActiveRegenerationSetting().set(true),
                () -> pot.aimDownForSplashSetting().set(true),
                () -> pot.healthThresholdSetting().set(25.0D),
                () -> pot.requireCombatTargetSetting().set(true),
                () -> pot.cooldownTicksSetting().set(140)
            };
            for (final Runnable change : changes) {
                assertEquals(4, pot.selectSlot(inventory));
                assertTrue(pot.ownsSelectedSlot(inventory, 4));
                change.run();
                assertFalse(pot.ownsSelectedSlot(inventory, 4),
                        "changed source healing policy must revoke stale right click");
                pot.restoreSlot(inventory, 4);
                assertEquals(4, inventory.slot);
            }
            assertEquals(4, pot.selectSlot(inventory));
            assertTrue(pot.ownsSelectedSlot(inventory, 4));
            pot.restoreSlot(inventory, 4);
        } finally { pot.onDisable(); }
    }

    @Test void nativePOTLeaseRejectsDisableReenableSameSourcePolicy() {
        final Minecraft189AutoPotModule mod = new Minecraft189AutoPotModule();
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

    @Test void nativePOTSetterReentryMayNotPublishStaleUseItemPermission() {
        final Minecraft189AutoPotModule mod = new Minecraft189AutoPotModule();
        final ReentrantInventory inv = new ReentrantInventory();
        inv.slot = 4;
        mod.onEnable();
        try {
            inv.afterSetter = () -> mod.requireCombatTargetSetting().set(true);
            assertEquals(-1, mod.selectSlot(inv),
                    "changed GUI policy during original setter revokes use");
            assertEquals(4, inv.slot, "only owned original source item restores");
            mod.requireCombatTargetSetting().set(false);
            inv.afterSetter = () -> { mod.onDisable(); mod.onEnable(); };
            assertEquals(-1, mod.selectSlot(inv),
                    "native disable/reenable during hotbar setter revokes lease");
            assertEquals(4, inv.slot);
            assertEquals(4, mod.selectSlot(inv));
            assertTrue(mod.ownsSelectedSlot(inv, 4));
            mod.restoreSlot(inv, 4);
        } finally { mod.onDisable(); }
    }

    @Test void nativePOTCurrentItemGetterReentryMustBeRecheckedAtUse() {
        final Minecraft189AutoPotModule mod = new Minecraft189AutoPotModule();
        final ReentrantInventory inv = new ReentrantInventory();
        inv.slot = 6;
        mod.onEnable();
        try {
            assertEquals(6, mod.selectSlot(inv));
            inv.afterGetter = () -> mod.requireCombatTargetSetting().set(true);
            assertFalse(mod.ownsSelectedSlot(inv, 6),
                    "native accessor may reenter a new policy during use");
            mod.restoreSlot(inv, 6);
            assertEquals(6, inv.slot);
            mod.requireCombatTargetSetting().set(false);
            assertEquals(6, mod.selectSlot(inv));
            inv.afterGetter = () -> inv.slot = 7;
            assertFalse(mod.ownsSelectedSlot(inv, 6),
                    "original currentItem changed by player during the getter");
            mod.restoreSlot(inv, 6);
            assertEquals(7, inv.slot,
                    "never overwrite actual human-selected currentItem");
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
        int slot;
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int next) { slot = next; }
    }
}
