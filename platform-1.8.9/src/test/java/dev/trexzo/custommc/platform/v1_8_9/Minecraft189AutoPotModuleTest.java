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
            assertEquals("25.0", settings.snapshotEncoded().get(Minecraft189AutoPotModule.HEALTH_THRESHOLD));
            controller.enable(Minecraft189AutoPotModule.ID);
        } finally { f.close(); f.close(); }
        assertNull(modules.find(Minecraft189AutoPotModule.ID));
        assertNull(settings.find(Minecraft189AutoPotModule.HEALTH_THRESHOLD));
    }

    private static final class Inventory implements Minecraft189InventoryHotbarControl {
        int slot;
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int next) { slot = next; }
    }
}
