package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189SafeWalkModuleTest {
    private static final Object REMOTE = new Object();
    private static final class Local implements Minecraft189LocalPlayerSafeWalkMarker { }
    private final Local local = new Local();

    @Test void noVanillaSneakMutationWhenDisabled() {
        Minecraft189SafeWalkModule module = new Minecraft189SafeWalkModule();
        assertEquals("movement.safeWalk", module.id());
        assertFalse(module.active());
        assertFalse(module.effectiveSneaking(false, local));
        assertTrue(module.effectiveSneaking(true, local));
        assertTrue(module.effectiveSneaking(true, REMOTE));
        assertFalse(module.effectiveSneaking(false, null));
    }

    @Test void onlyVerifiedNativeLocalPlayerGetsVanillaCollisionEdgePolicy() {
        Minecraft189SafeWalkModule module = new Minecraft189SafeWalkModule();
        module.onEnable();
        assertTrue(module.active());
        assertTrue(module.effectiveSneaking(false, local));
        assertFalse(module.effectiveSneaking(false, REMOTE));
        assertFalse(module.effectiveSneaking(false, null));
        assertTrue(module.effectiveSneaking(true, REMOTE));
        module.onDisable();
        assertFalse(module.effectiveSneaking(false, local));
        assertTrue(module.effectiveSneaking(true, local));
    }
}
