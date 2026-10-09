package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189AntiBotModuleTest {
    @Test void rejectsTabMissingAndUnknownWithoutFabricatingBotIdentity() {
        final Minecraft189AntiBotModule module = new Minecraft189AntiBotModule();
        final Minecraft189WorldEntityCombatState state = new Minecraft189WorldEntityCombatState();
        state.update(new int[] {-1, 1, 1 | 512, 1 | 512 | 256, 512 | 256});
        assertTrue(module.permits(-1, state.snapshot())); // Off by default
        module.onEnable();
        try {
            assertFalse(module.permits(-1, state.snapshot()));
            assertFalse(module.permits(0, state.snapshot())); // nonliving
            assertFalse(module.permits(1, state.snapshot())); // unknown client kind
            assertFalse(module.permits(2, state.snapshot())); // explicit missing tab info
            assertTrue(module.permits(3, state.snapshot())); // actual mapped info
            assertFalse(module.permits(4, state.snapshot())); // dead
            assertFalse(module.permits(100, state.snapshot()));
            module.allowUnknownSetting().set(true);
            assertTrue(module.permits(1, state.snapshot()));
            assertFalse(module.permits(2, state.snapshot())); // explicit miss remains vetoed
            assertFalse(module.permits(4, state.snapshot()));
            state.clear();
            assertFalse(module.permits(3, state.snapshot()));
        } finally { module.onDisable(); }
        assertTrue(module.permits(2, state.snapshot()));
    }

    @Test void registrationLifecyclePersistsAndCloses() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189AntiBotFeature feature = Minecraft189AntiBotFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AntiBotModule.ALLOW_UNKNOWN));
            feature.module().allowUnknownSetting().set(true);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AntiBotModule.ALLOW_UNKNOWN));
            controller.enable(Minecraft189AntiBotModule.ID);
        } finally { feature.close(); feature.close(); }
        assertNull(modules.find(Minecraft189AntiBotModule.ID));
        assertNull(settings.find(Minecraft189AntiBotModule.ALLOW_UNKNOWN));
    }
}
