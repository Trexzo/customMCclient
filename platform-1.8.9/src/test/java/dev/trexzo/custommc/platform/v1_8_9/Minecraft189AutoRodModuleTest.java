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

    @Test void registrationAndPersistenceAreClean() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189AutoRodFeature feature = Minecraft189AutoRodFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            feature.module().rodSlotSetting().set(9);
            assertEquals("9", settings.snapshotEncoded().get(Minecraft189AutoRodModule.ROD_SLOT));
            controller.enable(Minecraft189AutoRodModule.ID);
        } finally { feature.close(); feature.close(); }
        assertNull(modules.find(Minecraft189AutoRodModule.ID));
        assertNull(settings.find(Minecraft189AutoRodModule.ROD_SLOT));
    }
    private static final class Inventory implements Minecraft189InventoryHotbarControl {
        private int slot;
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int next) { slot = next; }
    }
}
