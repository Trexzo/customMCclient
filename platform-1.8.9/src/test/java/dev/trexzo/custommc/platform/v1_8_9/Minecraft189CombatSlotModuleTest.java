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

    private static final class FakeInventory implements Minecraft189InventoryHotbarControl {
        private int slot;
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int next) { slot = next; }
    }
}
