package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189CombatSlotModuleTest {
    @Test void switchesOnlyWithAuthorityAndRestoresUnlessPlayerChangedSlot() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final FakeInventory inv = new FakeInventory();
        inv.slot = 2;
        module.preferredSlotSetting().set(5);
        assertEquals(-1, module.select(inv, true, false));
        module.onEnable();
        assertEquals(-1, module.select(inv, false, false));
        assertEquals(-1, module.select(inv, true, true));
        assertEquals(2, inv.slot);
        assertEquals(2, module.select(inv, true, false));
        assertEquals(4, inv.slot);
        module.restore(inv, 2);
        assertEquals(2, inv.slot);
        assertEquals(2, module.select(inv, true, false));
        inv.slot = 7;
        module.restore(inv, 2);
        assertEquals(7, inv.slot); // preserve actual user's slot change
        module.restoreAfterClickSetting().set(false);
        module.restore(inv, 2);
        assertEquals(7, inv.slot);
        module.requirePlayerHitSetting().set(false);
        assertEquals(7, module.select(inv, false, false));
        assertEquals(4, inv.slot);
        module.onDisable();
        assertEquals(-1, module.select(inv, true, false));
    }

    @Test void invalidSlotsFailClosedAndSettingsPersist() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final FakeInventory inv = new FakeInventory();
        module.onEnable();
        inv.slot = -1;
        assertEquals(-1, module.select(inv, true, false));
        inv.slot = 9;
        assertEquals(-1, module.select(inv, true, false));
        assertThrows(IllegalArgumentException.class,
                () -> module.preferredSlotSetting().set(0));
        assertThrows(IllegalArgumentException.class,
                () -> module.preferredSlotSetting().set(10));
        module.onDisable();

        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189CombatSlotFeature feature = Minecraft189CombatSlotFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            feature.module().preferredSlotSetting().set(9);
            assertEquals("9", settings.snapshotEncoded().get(
                    Minecraft189CombatSlotModule.PREFERRED_SLOT));
            controller.enable(Minecraft189CombatSlotModule.ID);
        } finally {
            feature.close();
            feature.close();
        }
        assertNull(modules.find(Minecraft189CombatSlotModule.ID));
        assertNull(settings.find(Minecraft189CombatSlotModule.PREFERRED_SLOT));
    }


    @Test void originalCombatSlotLeaseSurvivesConfigChangeDisableAndNestedActions() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final FakeInventory inventory = new FakeInventory();
        final FakeInventory foreign = new FakeInventory();
        inventory.slot = 2;
        foreign.slot = 4;
        module.preferredSlotSetting().set(5);
        module.onEnable();
        assertEquals(2, module.select(inventory, true, false));
        assertEquals(4, inventory.slot);
        assertEquals(-1, module.select(inventory, true, false),
                "nested synthetic action must not replace the first lease");
        module.preferredSlotSetting().set(8);
        module.restore(foreign, 2);
        module.restore(inventory, 5);
        assertEquals(4, inventory.slot,
                "wrong inventory or previous-slot token must never restore");
        module.onDisable();
        module.restore(inventory, 2);
        assertEquals(2, inventory.slot,
                "in-flight finally must restore even when GUI disables module");

        module.onEnable();
        module.preferredSlotSetting().set(5);
        assertEquals(2, module.select(inventory, true, false));
        module.preferredSlotSetting().set(8);
        inventory.slot = 7; // real user changed hotbar to the new config slot
        module.restore(inventory, 2);
        assertEquals(7, inventory.slot,
                "changing preferredSlot must not steal the user's new slot");
        inventory.slot = 2;
        module.preferredSlotSetting().set(5);
        assertEquals(2, module.select(inventory, true, false),
                "released original lease must allow another legal action");
        module.restore(inventory, 2);
        assertEquals(2, inventory.slot);
        module.onDisable();
    }

    @Test void partialNativeSetterThrowRollsBackExactlyOwnedSlot() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final AdversarialInventory inventory = new AdversarialInventory();
        inventory.slot = 3;
        module.preferredSlotSetting().set(6);
        module.onEnable();
        inventory.throwAfterMutation = true;
        assertThrows(IllegalStateException.class,
                () -> module.select(inventory, true, false));
        assertEquals(3, inventory.slot);
        assertEquals(3, module.select(inventory, true, false),
                "throwing setter must not leave a zombie active lease");
        assertEquals(5, inventory.slot);
        module.restore(inventory, 3);
        assertEquals(3, inventory.slot);
        module.onDisable();
    }

    @Test void ignoredAndReentrantNativeSelectionCannotClaimSlotLease() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final AdversarialInventory inventory = new AdversarialInventory();
        inventory.slot = 2;
        module.preferredSlotSetting().set(5);
        module.onEnable();
        inventory.ignoreNextSetter = true;
        assertEquals(-1, module.select(inventory, true, false));
        assertEquals(2, inventory.slot);
        inventory.redirectNextSetterTo = 7;
        assertEquals(-1, module.select(inventory, true, false));
        assertEquals(7, inventory.slot,
                "a user-selected slot is never rewritten by a failed lease");
        inventory.slot = 2;
        assertEquals(2, module.select(inventory, true, false));
        assertEquals(4, inventory.slot);
        module.restore(inventory, 2);
        assertEquals(2, inventory.slot);
        module.onDisable();
    }

    @Test void nativeDispatchRequiresSameLiveSlotAndExplicitPolicy() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final FakeInventory source = new FakeInventory();
        source.slot = 2;
        module.preferredSlotSetting().set(5);
        module.onEnable();
        assertEquals(2, module.select(source, true, false));
        assertTrue(module.ownsVerifiedSlotAtDispatch(source, 2));
        assertFalse(module.ownsVerifiedSlotAtDispatch(new FakeInventory(), 2));
        assertFalse(module.ownsVerifiedSlotAtDispatch(source, 3));
        source.slot = 7; // Real user currentItem change, not source-authorized.
        assertFalse(module.ownsVerifiedSlotAtDispatch(source, 2));
        source.slot = 4;
        module.preferredSlotSetting().set(8);
        assertFalse(module.ownsVerifiedSlotAtDispatch(source, 2),
                "old selected native slot cannot spend changed GUI policy");
        module.restore(source, 2);
        assertEquals(2, source.slot, "a rejected dispatch still restores owned temporary slot");
        module.preferredSlotSetting().set(5);
        assertEquals(2, module.select(source, true, false));
        assertTrue(module.ownsVerifiedSlotAtDispatch(source, 2));
        module.requirePlayerHitSetting().set(false);
        assertFalse(module.ownsVerifiedSlotAtDispatch(source, 2),
                "new RequirePlayerHit policy revokes earlier click permission");
        module.restore(source, 2);
        assertEquals(2, source.slot);
        module.onDisable();
    }

    @Test void originalModuleDisableReenableMustNotReuseOldAcceptedClick() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final FakeInventory source = new FakeInventory();
        source.slot = 1;
        module.preferredSlotSetting().set(5);
        module.onEnable();
        assertEquals(1, module.select(source, true, false));
        assertTrue(module.ownsVerifiedSlotAtDispatch(source, 1));
        module.onDisable();
        module.onEnable(); // Same final enabled state, different lifetime.
        assertFalse(module.ownsVerifiedSlotAtDispatch(source, 1));
        module.restore(source, 1);
        assertEquals(1, source.slot);
        assertEquals(1, module.select(source, true, false));
        assertTrue(module.ownsVerifiedSlotAtDispatch(source, 1),
                "new observed native click must get fresh lifecycle");
        module.restore(source, 1);
        module.onDisable();
    }

    @Test void changedPolicyDuringNativeSlotSetterCancelsAndRestoresOnlyOwnedSlot() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final AdversarialInventory inv = new AdversarialInventory();
        inv.slot = 2;
        module.preferredSlotSetting().set(5);
        module.onEnable();
        inv.afterSetter = () -> module.preferredSlotSetting().set(8);
        assertEquals(-1, module.select(inv, true, false),
                "source setter cannot publish stale selected slot policy");
        assertEquals(2, inv.slot, "original user's slot restored after stale grant");
        module.preferredSlotSetting().set(5);
        inv.afterSetter = () -> module.requirePlayerHitSetting().set(false);
        assertEquals(-1, module.select(inv, true, false));
        assertEquals(2, inv.slot);
        module.requirePlayerHitSetting().set(true);
        inv.afterSetter = () -> { module.onDisable(); module.onEnable(); };
        assertEquals(-1, module.select(inv, true, false),
                "off/on source lifecycle during setter cannot adopt click");
        assertEquals(2, inv.slot);
        assertEquals(2, module.select(inv, true, false));
        assertTrue(module.ownsVerifiedSlotAtDispatch(inv, 2));
        module.restore(inv, 2);
        module.onDisable();
    }

    @Test void sourceCurrentItemGetterReentryRevokesNativeDispatchPermission() {
        final Minecraft189CombatSlotModule module = new Minecraft189CombatSlotModule();
        final AdversarialInventory inv = new AdversarialInventory();
        inv.slot = 1;
        module.preferredSlotSetting().set(4);
        module.onEnable();
        assertEquals(1, module.select(inv, true, false));
        inv.afterRead = () -> module.preferredSlotSetting().set(5);
        assertFalse(module.ownsVerifiedSlotAtDispatch(inv, 1),
                "native getter can mutate GUI policy during validation");
        module.restore(inv, 1);
        assertEquals(1, inv.slot);
        module.preferredSlotSetting().set(4);
        assertEquals(1, module.select(inv, true, false));
        inv.afterRead = () -> inv.slot = 7;
        assertFalse(module.ownsVerifiedSlotAtDispatch(inv, 1),
                "native getter can redirect original currentItem");
        module.restore(inv, 1);
        assertEquals(7, inv.slot, "human currentItem choice is never overwritten");
        module.onDisable();
    }

    private static final class AdversarialInventory
            implements Minecraft189InventoryHotbarControl {
        private int slot;
        private boolean throwAfterMutation;
        private boolean ignoreNextSetter;
        private int redirectNextSetterTo = -1;
        private Runnable afterSetter;
        private Runnable afterRead;

        @Override public int customMcSelectedHotbarSlot() {
            final int actual = slot;
            if (afterRead != null) {
                final Runnable action = afterRead;
                afterRead = null;
                action.run();
            }
            return actual;
        }

        @Override public void customMcSetSelectedHotbarSlot(final int next) {
            if (ignoreNextSetter) {
                ignoreNextSetter = false;
                return;
            }
            if (redirectNextSetterTo >= 0) {
                slot = redirectNextSetterTo;
                redirectNextSetterTo = -1;
                return;
            }
            slot = next;
            if (afterSetter != null) {
                final Runnable action = afterSetter;
                afterSetter = null;
                action.run();
            }
            if (throwAfterMutation) {
                throwAfterMutation = false;
                throw new IllegalStateException("native selection mutated then threw");
            }
        }
    }

    private static final class FakeInventory implements Minecraft189InventoryHotbarControl {
        private int slot;
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int next) { slot = next; }
    }
}
