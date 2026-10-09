package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189KeepSprintModuleTest {
    @Test void onlyRealBeforeAttackSprintCanBeRestored() {
        final Minecraft189KeepSprintModule module = new Minecraft189KeepSprintModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        movement.update(true, false, true);
        assertFalse(module.shouldRestore(movement.snapshot(), true,true,true,false,false));
        module.onEnable();
        assertTrue(module.shouldRestore(movement.snapshot(), true,true,true,false,false));
        assertFalse(module.shouldRestore(movement.snapshot(), false,true,true,false,false));
        assertFalse(module.shouldRestore(movement.snapshot(), true,false,true,false,false));
        assertFalse(module.shouldRestore(movement.snapshot(), true,true,false,false,false));
        assertFalse(module.shouldRestore(movement.snapshot(), true,true,true,true,false));
        assertFalse(module.shouldRestore(movement.snapshot(), true,true,true,false,true));
        movement.update(true, true, true);
        assertFalse(module.shouldRestore(movement.snapshot(), true,true,true,false,false));
        movement.update(true, false, true);
        module.requireForwardSetting().set(false);
        module.requirePlayerHitSetting().set(false);
        assertTrue(module.shouldRestore(movement.snapshot(), true,false,false,false,false));
        movement.clear();
        assertFalse(module.shouldRestore(movement.snapshot(), true,true,true,false,false));
        module.onDisable();
        assertFalse(module.shouldRestore(movement.snapshot(), true,true,true,false,false));
    }

    @Test void settingsPersistAndClose() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189KeepSprintFeature feature = Minecraft189KeepSprintFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189KeepSprintModule.REQUIRE_FORWARD));
            feature.module().requireForwardSetting().set(false);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189KeepSprintModule.REQUIRE_FORWARD));
            controller.enable(Minecraft189KeepSprintModule.ID);
        } finally {
            feature.close();
            feature.close();
        }
        assertNull(modules.find(Minecraft189KeepSprintModule.ID));
        assertNull(settings.find(Minecraft189KeepSprintModule.REQUIRE_FORWARD));
    }
}
