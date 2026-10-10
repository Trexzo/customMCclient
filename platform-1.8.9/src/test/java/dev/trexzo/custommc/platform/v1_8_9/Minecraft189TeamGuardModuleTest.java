package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189TeamGuardModuleTest {
    @Test void independentOfAntiBotAndFailsClosedOnUnknown() {
        final Minecraft189TeamGuardModule team = new Minecraft189TeamGuardModule();
        final Minecraft189AntiBotModule antiBot = new Minecraft189AntiBotModule();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        // [0] confirmed teammate; [1] confirmed enemy; [2] unknown team; [3] dead.
        combat.update(new int[]{
                1 | 2048 | 1024, 1 | 2048, 1, 2048
        });
        assertTrue(team.permits(0, combat.snapshot()));
        team.onEnable();
        try {
            assertFalse(antiBot.active());
            assertFalse(team.permits(0, combat.snapshot()));
            assertTrue(team.permits(1, combat.snapshot()));
            assertFalse(team.permits(2, combat.snapshot()));
            assertFalse(team.permits(3, combat.snapshot()));
            assertFalse(team.permits(-1, combat.snapshot()));
            assertFalse(team.permits(999, combat.snapshot()));
            combat.clear();
            assertFalse(team.permits(1, combat.snapshot()));
        } finally { team.onDisable(); }
        assertTrue(team.permits(0, combat.snapshot()));
    }

    @Test void lifecycleIsIndependentOfAntiBot() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final Minecraft189TeamGuardFeature team = Minecraft189TeamGuardFeature.install(
                modules, controller, new ModulePresentationRegistry());
        try {
            assertNotNull(modules.find(Minecraft189TeamGuardModule.ID));
            assertFalse(team.module().active());
            controller.enable(Minecraft189TeamGuardModule.ID);
            assertTrue(team.module().active());
        } finally { team.close(); team.close(); }
        assertNull(modules.find(Minecraft189TeamGuardModule.ID));
    }
}
