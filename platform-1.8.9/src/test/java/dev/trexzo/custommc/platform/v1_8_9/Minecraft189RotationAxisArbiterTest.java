package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One actual native yaw/pitch writer per original rotation callback. */
final class Minecraft189RotationAxisArbiterTest {
    private static final class OriginalPlayer
            implements Minecraft189PlayerRotationControl {
        int yawWrites;
        int pitchWrites;
        Runnable onYaw;
        @Override public float customMcRotationYaw() { return 15.0F; }
        @Override public float customMcRotationPitch() { return 4.0F; }
        @Override public void customMcSetRotationYaw(final float value) {
            yawWrites++;
            if (onYaw != null) onYaw.run();
        }
        @Override public void customMcSetRotationPitch(final float value) {
            pitchWrites++;
        }
    }

    @Test
    void firstActualSetterClaimBlocksAnyOtherModuleBothAxes() {
        final Minecraft189RotationAxisArbiter arbiter =
                new Minecraft189RotationAxisArbiter();
        final OriginalPlayer source = new OriginalPlayer();
        arbiter.enter();
        try {
            final Minecraft189PlayerRotationControl spin =
                    arbiter.guardedControl(source,
                            Minecraft189RotationAxisArbiter.Owner.SPIN);
            final Minecraft189PlayerRotationControl aura =
                    arbiter.guardedControl(source,
                            Minecraft189RotationAxisArbiter.Owner.KILL_AURA);
            final Minecraft189PlayerRotationControl assist =
                    arbiter.guardedControl(source,
                            Minecraft189RotationAxisArbiter.Owner.AIM_ASSIST);
            final Minecraft189PlayerRotationControl jitter =
                    arbiter.guardedControl(source,
                            Minecraft189RotationAxisArbiter.Owner.JITTER);
            // No-op modules do NOT own the callback merely by construction.
            assertFalse(arbiter.ownedBy(Minecraft189RotationAxisArbiter.Owner.SPIN));
            assertEquals(15.0F, assist.customMcRotationYaw(), 0.0F);
            assist.customMcSetRotationYaw(10.0F);
            assertTrue(arbiter.ownedBy(Minecraft189RotationAxisArbiter.Owner.AIM_ASSIST));
            assertEquals(1, source.yawWrites);
            aura.customMcSetRotationPitch(3.0F);
            spin.customMcSetRotationYaw(8.0F);
            jitter.customMcSetRotationPitch(7.0F);
            assertEquals(1, source.yawWrites);
            assertEquals(0, source.pitchWrites);
            assist.customMcSetRotationPitch(1.0F);
            assertEquals(1, source.pitchWrites);
        } finally {
            arbiter.exit();
        }
    }

    @Test
    void returningFalseAfterYawCannotLetJitterWritePitch() {
        final Minecraft189RotationAxisArbiter arbiter =
                new Minecraft189RotationAxisArbiter();
        final OriginalPlayer source = new OriginalPlayer();
        arbiter.enter();
        final Minecraft189PlayerRotationControl assist =
                arbiter.guardedControl(source,
                        Minecraft189RotationAxisArbiter.Owner.AIM_ASSIST);
        final Minecraft189PlayerRotationControl jitter =
                arbiter.guardedControl(source,
                        Minecraft189RotationAxisArbiter.Owner.JITTER);
        // A native setter can synchronously disable AimAssist, leading
        // its apply() to return false even though it already wrote yaw.
        source.onYaw = () -> jitter.customMcSetRotationPitch(18.0F);
        assist.customMcSetRotationYaw(30.0F);
        assertTrue(arbiter.ownedBy(Minecraft189RotationAxisArbiter.Owner.AIM_ASSIST));
        assertEquals(0, source.pitchWrites);
        // A later fallback attempt in the same callback is also denied.
        jitter.customMcSetRotationYaw(90.0F);
        assertEquals(1, source.yawWrites);
        arbiter.exit();
    }

    @Test
    void noOpSpinAndAimAssistCanYieldToNextWriter() {
        final Minecraft189RotationAxisArbiter arbiter =
                new Minecraft189RotationAxisArbiter();
        final OriginalPlayer player = new OriginalPlayer();
        arbiter.enter();
        final Minecraft189PlayerRotationControl spin =
                arbiter.guardedControl(player,
                        Minecraft189RotationAxisArbiter.Owner.SPIN);
        final Minecraft189PlayerRotationControl assist =
                arbiter.guardedControl(player,
                        Minecraft189RotationAxisArbiter.Owner.AIM_ASSIST);
        final Minecraft189PlayerRotationControl jitter =
                arbiter.guardedControl(player,
                        Minecraft189RotationAxisArbiter.Owner.JITTER);
        // Reading original source and returning false without any setter
        // must not prohibit a later module's native yaw correction.
        assertEquals(4.0F, spin.customMcRotationPitch(), 0.0F);
        assertEquals(15.0F, assist.customMcRotationYaw(), 0.0F);
        jitter.customMcSetRotationYaw(17.0F);
        assertEquals(1, player.yawWrites);
        assertTrue(arbiter.ownedBy(Minecraft189RotationAxisArbiter.Owner.JITTER));
        arbiter.exit();
    }

    @Test
    void staleModuleWrappersNeverResurrectAfterExitOrNewSource() {
        final Minecraft189RotationAxisArbiter arbiter =
                new Minecraft189RotationAxisArbiter();
        final OriginalPlayer older = new OriginalPlayer();
        final OriginalPlayer current = new OriginalPlayer();
        arbiter.enter();
        final Minecraft189PlayerRotationControl stale =
                arbiter.guardedControl(older,
                        Minecraft189RotationAxisArbiter.Owner.KILL_AURA);
        arbiter.exit();
        stale.customMcSetRotationYaw(11.0F);
        assertEquals(0, older.yawWrites);

        arbiter.enter();
        final Minecraft189PlayerRotationControl fresh =
                arbiter.guardedControl(current,
                        Minecraft189RotationAxisArbiter.Owner.AIM_ASSIST);
        stale.customMcSetRotationPitch(3.0F);
        assertEquals(0, older.pitchWrites);
        fresh.customMcSetRotationYaw(3.0F);
        assertEquals(1, current.yawWrites);
        arbiter.exit();
    }

    @Test
    void failedNativeSetterStillPreventsOtherOwnerAfterException() {
        final Minecraft189RotationAxisArbiter arbiter =
                new Minecraft189RotationAxisArbiter();
        arbiter.enter();
        final Minecraft189PlayerRotationControl broken =
                arbiter.guardedControl(new Minecraft189PlayerRotationControl() {
                    @Override public float customMcRotationYaw() { return 0; }
                    @Override public void customMcSetRotationYaw(final float v) {
                        throw new IllegalStateException("original native setter failed");
                    }
                    @Override public void customMcSetRotationPitch(final float v) { }
                }, Minecraft189RotationAxisArbiter.Owner.SPIN);
        try {
            broken.customMcSetRotationYaw(20.0F);
        } catch (IllegalStateException expected) {
            assertEquals("original native setter failed", expected.getMessage());
        }
        assertTrue(arbiter.ownedBy(Minecraft189RotationAxisArbiter.Owner.SPIN));
        arbiter.exit();
    }
}
