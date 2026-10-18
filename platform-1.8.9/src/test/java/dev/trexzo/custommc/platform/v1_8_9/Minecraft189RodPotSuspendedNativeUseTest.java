package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Real original currentItem/native rightClick admission boundaries.
 * Offline source-policy adapter only, not pinned Mojang runtime proof.
 */
final class Minecraft189RodPotSuspendedNativeUseTest {
    private static final class NativeInventory
            implements Minecraft189InventoryHotbarControl {
        int slot = 5;
        Runnable duringGetter;
        Runnable duringSetter;
        @Override public int customMcSelectedHotbarSlot() {
            final int old = slot;
            Runnable callback = duringGetter;
            duringGetter = null;
            if (callback != null) callback.run();
            return old;
        }
        @Override public void customMcSetSelectedHotbarSlot(int value) {
            slot = value;
            Runnable callback = duringSetter;
            duringSetter = null;
            if (callback != null) callback.run();
        }
    }

    @Test void suspendedRodCannotSpendCompletedOriginalUseInOldLease() {
        Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        NativeInventory inv = new NativeInventory();
        rod.onEnable();
        assertEquals(5,rod.selectSlot(inv));
        rod.suspend();
        assertFalse(rod.commitCompletedOriginalUse(inv,5),
                "GUI/combat suspension revokes old native rod action");
        rod.restoreSlot(inv,5);
        assertEquals(5,inv.slot);
        assertEquals(5,rod.selectSlot(inv));
        assertTrue(rod.commitCompletedOriginalUse(inv,5));
        rod.restoreSlot(inv,5);
        rod.onDisable();
    }

    @Test void suspendedPotionCannotSpendCompletedOriginalUseInOldLease() {
        Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        NativeInventory inv = new NativeInventory();
        pot.onEnable();
        assertEquals(5,pot.selectSlot(inv));
        pot.suspend();
        assertFalse(pot.commitCompletedOriginalUse(inv,5));
        pot.restoreSlot(inv,5);
        assertEquals(5,inv.slot);
        assertEquals(5,pot.selectSlot(inv));
        assertTrue(pot.commitCompletedOriginalUse(inv,5));
        pot.restoreSlot(inv,5);
        pot.onDisable();
    }

    @Test void realRodSlotSetterSuspendingAdmissionRestoresPriorItem() {
        Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        NativeInventory inv = new NativeInventory();
        rod.onEnable();
        inv.duringSetter = rod::suspend;
        assertEquals(-1,rod.selectSlot(inv));
        assertEquals(5,inv.slot);
        assertEquals(5,rod.selectSlot(inv));
        rod.restoreSlot(inv,5);
        rod.onDisable();
    }

    @Test void realPotSlotSetterSuspendingAdmissionRestoresPriorItem() {
        Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        NativeInventory inv = new NativeInventory();
        pot.onEnable();
        inv.duringSetter = pot::suspend;
        assertEquals(-1,pot.selectSlot(inv));
        assertEquals(5,inv.slot);
        assertEquals(5,pot.selectSlot(inv));
        pot.restoreSlot(inv,5);
        pot.onDisable();
    }

    @Test void rodSourceSlotGetterAfterSelectionCanRevokeOldLease() {
        Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        NativeInventory inv = new NativeInventory();
        rod.onEnable();
        assertEquals(5,rod.selectSlot(inv));
        inv.duringGetter = rod::suspend;
        assertFalse(rod.ownsSelectedSlot(inv,5));
        assertFalse(rod.commitCompletedOriginalUse(inv,5));
        rod.restoreSlot(inv,5);
        assertEquals(5,inv.slot);
        rod.onDisable();
    }

    @Test void potionSourceSlotGetterAfterSelectionCanRevokeOldLease() {
        Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        NativeInventory inv = new NativeInventory();
        pot.onEnable();
        assertEquals(5,pot.selectSlot(inv));
        inv.duringGetter = pot::suspend;
        assertFalse(pot.ownsSelectedSlot(inv,5));
        assertFalse(pot.commitCompletedOriginalUse(inv,5));
        pot.restoreSlot(inv,5);
        assertEquals(5,inv.slot);
        pot.onDisable();
    }

    @Test void rodNativeSuspensionDoesNotDisableModuleItself() {
        Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        rod.onEnable();
        rod.suspend();
        assertTrue(rod.active());
        assertTrue(rod.shouldUse(true,true,false,false,true));
        rod.onDisable();
    }

    @Test void potionNativeSuspensionDoesNotDisableModuleItself() {
        Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        pot.onEnable();
        pot.suspend();
        assertTrue(pot.active());
        pot.onDisable();
    }

    @Test void rodOldCallbackCannotCommitAfterTwoSuspensions() {
        Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        NativeInventory inv = new NativeInventory();
        rod.onEnable();
        assertEquals(5,rod.selectSlot(inv));
        rod.suspend();
        rod.suspend();
        assertFalse(rod.commitCompletedOriginalUse(inv,5));
        rod.restoreSlot(inv,5);
        assertEquals(5,inv.slot);
        rod.onDisable();
    }

    @Test void potOldCallbackCannotCommitAfterTwoSuspensions() {
        Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        NativeInventory inv = new NativeInventory();
        pot.onEnable();
        assertEquals(5,pot.selectSlot(inv));
        pot.suspend();
        pot.suspend();
        assertFalse(pot.commitCompletedOriginalUse(inv,5));
        pot.restoreSlot(inv,5);
        assertEquals(5,inv.slot);
        pot.onDisable();
    }

    @Test void actualHumanRodHotbarChangeStillSurvivesRestoreAfterSuspend() {
        Minecraft189AutoRodModule rod = new Minecraft189AutoRodModule();
        NativeInventory inv = new NativeInventory();
        rod.onEnable();
        assertEquals(5,rod.selectSlot(inv));
        rod.suspend();
        inv.slot=7;
        rod.restoreSlot(inv,5);
        assertEquals(7,inv.slot);
        rod.onDisable();
    }

    @Test void actualHumanPotionHotbarChangeStillSurvivesRestoreAfterSuspend() {
        Minecraft189AutoPotModule pot = new Minecraft189AutoPotModule();
        NativeInventory inv = new NativeInventory();
        pot.onEnable();
        assertEquals(5,pot.selectSlot(inv));
        pot.suspend();
        inv.slot=7;
        pot.restoreSlot(inv,5);
        assertEquals(7,inv.slot);
        pot.onDisable();
    }
}
