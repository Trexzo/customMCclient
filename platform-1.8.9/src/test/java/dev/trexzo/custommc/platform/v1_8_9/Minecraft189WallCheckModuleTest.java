package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189WallCheckModuleTest {
    @Test void onlyVisibleEntitiesPassWhenEnabled() {
        final Minecraft189WallCheckModule guard = new Minecraft189WallCheckModule();
        final Minecraft189WorldEntityVisibilityState state =
                new Minecraft189WorldEntityVisibilityState();
        assertTrue(guard.permits(0, null));
        assertTrue(state.update(new int[]{-1, 0, 1}));
        assertFalse(state.snapshot().visible(0));
        assertFalse(state.snapshot().visible(1));
        assertTrue(state.snapshot().visible(2));
        guard.onEnable();
        try {
            assertFalse(guard.permits(0, state.snapshot()));
            assertFalse(guard.permits(1, state.snapshot()));
            assertTrue(guard.permits(2, state.snapshot()));
            assertFalse(guard.permits(-1, state.snapshot()));
            assertFalse(guard.permits(3, state.snapshot()));
            assertFalse(guard.permits(2, null));
            final Minecraft189WorldEntityVisibilityState.Snapshot before =
                    state.snapshot();
            assertTrue(state.update(new int[]{-1, 1, 0}));
            assertFalse(before.matches(new int[]{-1, 1, 0}));
            assertFalse(guard.permits(2, state.snapshot()));
            assertTrue(guard.permits(1, state.snapshot()));
            assertFalse(state.update(new int[]{2}));
            assertFalse(guard.permits(0, state.snapshot()));
            assertFalse(state.update(null));
            assertFalse(guard.permits(0, state.snapshot()));
        } finally { guard.onDisable(); }
        assertTrue(guard.permits(1, state.snapshot()));
    }

    @Test void registeredModuleLifecycleAndCleanup() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final Minecraft189WallCheckFeature feature =
                Minecraft189WallCheckFeature.install(
                        modules, controller, new ModulePresentationRegistry());
        try {
            assertNotNull(modules.find(Minecraft189WallCheckModule.ID));
            assertFalse(feature.module().active());
            controller.enable(Minecraft189WallCheckModule.ID);
            assertTrue(feature.module().active());
        } finally { feature.close(); feature.close(); }
        assertNull(modules.find(Minecraft189WallCheckModule.ID));
    }
}
