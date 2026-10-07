package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189WTapModuleTest {
    @Test
    void freshLeftPressResetsSprintOnceAndRearmsOnRelease() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(
                        modules);
        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();

        final Minecraft189WTapFeature feature =
                Minecraft189WTapFeature.install(
                        modules,
                        controller,
                        presentations);
        try {
            final Minecraft189WTapModule module =
                    feature.module();
            final Minecraft189PlayerMovementState state =
                    new Minecraft189PlayerMovementState();
            final TestPlayer player =
                    new TestPlayer();

            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertTrue(
                    player.sprinting);

            controller.enable(
                    Minecraft189WTapModule.ID);
            assertTrue(
                    module.active());

            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertTrue(
                    player.sprinting);

            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertFalse(
                    player.sprinting);

            player.sprinting = false;
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            state.update(
                    true,
                    false,
                    false);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertFalse(
                    player.sprinting);

            controller.disable(
                    Minecraft189WTapModule.ID);
            assertFalse(
                    module.active());
        } finally {
            feature.close();
        }

        assertNull(
                modules.find(
                        Minecraft189WTapModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerSprintControl {
        private boolean sprinting = true;

        @Override
        public void customMcSetSprinting(
                final boolean sprinting) {
            this.sprinting = sprinting;
        }
    }
}
