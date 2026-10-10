package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189HitSelectModuleTest {
    @Test
    void acceptsOnlyVerifiedLivePlayersOutsideHurtWindow() {
        final Minecraft189HitSelectModule module = new Minecraft189HitSelectModule();
        final Minecraft189WorldEntityCombatState state =
                new Minecraft189WorldEntityCombatState();
        state.update(new int[]{-1, 1, 3, 10, 11, 0});
        assertTrue(module.permits(-1, state.snapshot()));
        module.onEnable();
        try {
            assertFalse(module.permits(-1, state.snapshot()));
            assertFalse(module.permits(0, state.snapshot()));
            assertTrue(module.permits(1, state.snapshot()));
            assertTrue(module.permits(2, state.snapshot())); // hurt=1, alive
            assertFalse(module.permits(3, state.snapshot())); // hurt=5, dead
            assertFalse(module.permits(4, state.snapshot())); // hurt=5, alive
            assertFalse(module.permits(5, state.snapshot())); // dead
            assertFalse(module.permits(6, state.snapshot())); // stale index
            module.maxTargetHurtTicksSetting().set(5);
            assertTrue(module.permits(4, state.snapshot()));
            assertFalse(module.permits(3, state.snapshot()));
            state.clear();
            assertFalse(module.permits(1, state.snapshot()));
        } finally {
            module.onDisable();
        }
        assertTrue(module.permits(-1, state.snapshot()));
    }

    @Test
    void featureBindsPersistentSettingAndClosesExactly() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189HitSelectFeature feature =
                Minecraft189HitSelectFeature.install(
                        modules, controller, new ModulePresentationRegistry(),
                        new ModuleSettingRegistry(modules, settings),
                        settings, new SettingPresentationRegistry());
        try {
            assertEquals("1", settings.snapshotEncoded().get(
                    Minecraft189HitSelectModule.MAX_HURT_TICKS_SETTING_ID));
            feature.module().maxTargetHurtTicksSetting().set(3);
            assertEquals("3", settings.snapshotEncoded().get(
                    Minecraft189HitSelectModule.MAX_HURT_TICKS_SETTING_ID));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().maxTargetHurtTicksSetting().set(-1));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().maxTargetHurtTicksSetting().set(21));
            controller.enable(Minecraft189HitSelectModule.ID);
        } finally {
            feature.close();
            feature.close();
        }
        assertNull(modules.find(Minecraft189HitSelectModule.ID));
        assertNull(settings.find(Minecraft189HitSelectModule.MAX_HURT_TICKS_SETTING_ID));
    }
}
