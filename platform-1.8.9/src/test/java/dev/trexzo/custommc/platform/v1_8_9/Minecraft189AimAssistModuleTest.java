package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189AimAssistModuleTest {
    @Test
    void aimAssistOwnsHeldTargetRotationAndLifecycle() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(
                        modules);
        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();

        final Minecraft189AimAssistFeature feature =
                Minecraft189AimAssistFeature.install(
                        modules,
                        controller,
                        presentations);
        try {
            final Minecraft189AimAssistModule module =
                    feature.module();
            final Minecraft189PlayerPositionState local =
                    new Minecraft189PlayerPositionState();
            final Minecraft189WorldEntityPositionState positions =
                    new Minecraft189WorldEntityPositionState();
            final Minecraft189WorldEntityKindState kinds =
                    new Minecraft189WorldEntityKindState();
            final Minecraft189NearestPlayerTargetState target =
                    new Minecraft189NearestPlayerTargetState();
            final Minecraft189TargetRotationState targetRotation =
                    new Minecraft189TargetRotationState();
            final Minecraft189PlayerRotationState currentRotation =
                    new Minecraft189PlayerRotationState();
            final TestPlayer player =
                    new TestPlayer(
                            30.0F,
                            10.0F);

            local.update(
                    0.0D,
                    0.0D,
                    0.0D);
            positions.update(
                    new double[]{
                            0.0D,
                            0.0D,
                            10.0D
                    });
            kinds.update(
                    new int[]{
                            Minecraft189WorldEntityKindState.LIVING
                                    | Minecraft189WorldEntityKindState.PLAYER
                    });
            target.update(
                    local.snapshot(),
                    positions.snapshot(),
                    kinds.snapshot());
            targetRotation.update(
                    local.snapshot(),
                    target.snapshot());

            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));

            controller.enable(
                    Minecraft189AimAssistModule.ID);
            assertTrue(
                    module.active());

            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            false));
            assertEquals(
                    30.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    10.0F,
                    player.pitch,
                    0.000001F);

            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    0.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    0.0F,
                    player.pitch,
                    0.000001F);
            assertEquals(
                    1,
                    player.yawWrites);
            assertEquals(
                    1,
                    player.pitchWrites);

            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    1,
                    player.yawWrites);
            assertEquals(
                    1,
                    player.pitchWrites);

            targetRotation.clear();
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));

            controller.disable(
                    Minecraft189AimAssistModule.ID);
            assertFalse(
                    module.active());
        } finally {
            feature.close();
        }

        assertNull(
                modules.find(
                        Minecraft189AimAssistModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerRotationControl {
        private float yaw;
        private float pitch;
        private int yawWrites;
        private int pitchWrites;

        private TestPlayer(
                final float yaw,
                final float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        @Override
        public float customMcRotationYaw() {
            return yaw;
        }

        @Override
        public float customMcRotationPitch() {
            return pitch;
        }

        @Override
        public void customMcSetRotationYaw(
                final float yaw) {
            this.yaw = yaw;
            yawWrites++;
        }

        @Override
        public void customMcSetRotationPitch(
                final float pitch) {
            this.pitch = pitch;
            pitchWrites++;
        }
    }
}
