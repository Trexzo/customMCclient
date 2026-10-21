package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V286: real production selector, Aura aim and native click FOV gate.
 * Offline snapshots are synthetic and never stand in for Mojang GUI evidence.
 */
final class Minecraft189OriginalFovAuraRotationBudgetTest {
    private static final int PLAYER = Minecraft189WorldEntityKindState.LIVING
            | Minecraft189WorldEntityKindState.PLAYER;
    private static final int SELF = PLAYER
            | Minecraft189WorldEntityKindState.LOCAL_PLAYER;

    private static final class Player implements Minecraft189PlayerRotationControl {
        float yaw, pitch;
        int yawWrites, pitchWrites;
        Player(float yaw) { this.yaw = yaw; }
        @Override public float customMcRotationYaw() { return yaw; }
        @Override public float customMcRotationPitch() { return pitch; }
        @Override public void customMcSetRotationYaw(float v) {
            yawWrites++;
            yaw = v;
        }
        @Override public void customMcSetRotationPitch(float v) {
            pitchWrites++;
            pitch = v;
        }
    }

    private static final class Fixture {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState angle = new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState move = new Minecraft189PlayerMovementState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat =
                new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target =
                new Minecraft189TargetRotationState();

        Fixture() {
            local.update(0, 0, 0);
            positions.update(new double[]{0, 0, 0, 0, 0, -2});
            kinds.update(new int[]{SELF, PLAYER});
            combat.update(new int[]{1, 1});
            move.update(true, false, false);
            aura.onEnable();
        }

        void sourceYaw(float yaw) {
            angle.update(yaw, 0);
        }

        void select() {
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    angle.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), nearest);
            target.update(local.snapshot(), nearest.snapshot());
        }

        boolean aim(Player player) {
            return aura.aim(player, angle.snapshot(), target.snapshot(),
                    false, move.snapshot());
        }

        boolean nativeClickFov() {
            return Minecraft189KillAuraNativeFovGate.permits(1,
                    aura.fovSetting().get(), angle.snapshot(), local.snapshot(),
                    positions.snapshot(), kinds.snapshot());
        }
    }

    @Test void originalRearSourceNeverGetsSelectedAtDefaultNinety() {
        final Fixture f = new Fixture();
        f.sourceYaw(0);
        f.select();
        assertFalse(f.nearest.snapshot().found());
        assertFalse(f.target.snapshot().available());
        assertFalse(f.nativeClickFov());
    }

    @Test void stalePreviouslySelectedRearTargetCannotStartAimFromZeroYaw() {
        final Fixture f = new Fixture();
        f.sourceYaw(-180);
        f.select();
        assertEquals(1, f.target.snapshot().entityIndex());
        final Player player = new Player(0);
        f.sourceYaw(0);
        assertFalse(f.aim(player), "real Aura aim rechecks its own default 90 FOV");
        assertEquals(0, player.yawWrites);
        assertEquals(0, player.pitchWrites);
        assertEquals(0F, player.yaw);
        assertFalse(f.nativeClickFov());
    }

    @Test void originalDefaultAngularStepLimitsOneAcceptedAimToTenDegrees() {
        final Fixture f = new Fixture();
        final Player player = new Player(-100);
        f.sourceYaw(player.yaw);
        f.select();
        assertTrue(f.nearest.snapshot().found(), "rear becomes selectable at native -100 yaw");
        assertTrue(f.nativeClickFov());
        assertTrue(f.aim(player));
        assertEquals(-110F, player.yaw, 0.0001F);
        assertEquals(1, player.yawWrites);
        assertEquals(0, player.pitchWrites);
        assertEquals(10D, f.aura.angularStepSetting().get(), 0.00001D);
    }

    @Test void oneHundredEightyTurnRequiresMultipleEligibleRealAuraCallbacks() {
        final Fixture f = new Fixture();
        final Player player = new Player(-100);
        for (int i = 0; i < 8; i++) {
            final float previous = player.yaw;
            f.sourceYaw(previous);
            f.select();
            assertTrue(f.nearest.snapshot().found());
            assertTrue(f.aim(player));
            assertTrue(Math.abs(player.yaw - previous) <= 10.00001F);
        }
        assertEquals(-180F, player.yaw, 0.00001F);
        assertEquals(8, player.yawWrites);
        assertEquals(0, player.pitchWrites);
    }

    @Test void widerStepAloneCannotAuthorizeAnOutOfFovRearTarget() {
        final Fixture f = new Fixture();
        final Player player = new Player(0);
        f.aura.angularStepSetting().set(180.0D);
        f.sourceYaw(-180);
        f.select();
        assertEquals(1, f.target.snapshot().entityIndex());
        f.sourceYaw(0);
        assertFalse(f.aim(player));
        assertEquals(0, player.yawWrites);
        assertFalse(f.nativeClickFov());
    }

    @Test void explicitNewSourceYawChangesFovPredicateWithoutAnyAuraSetter() {
        final Fixture f = new Fixture();
        f.sourceYaw(0);
        f.select();
        assertFalse(f.nativeClickFov());
        f.sourceYaw(-180); // externally supplied ORIGINAL source, no Aura aim call
        f.select();
        assertTrue(f.nativeClickFov());
        assertEquals(1, f.nearest.snapshot().entityIndex());
    }

    @Test void outOfReachRearCannotBeRotatedIntoByAura() {
        final Fixture f = new Fixture();
        final Player player = new Player(-180);
        f.positions.update(new double[]{0, 0, 0, 0, 0, -4});
        f.sourceYaw(-180);
        f.select();
        assertFalse(f.nearest.snapshot().found());
        assertFalse(f.aim(player));
        assertEquals(0, player.yawWrites);
    }

    @Test void missingCombatSourceCannotAuthorizeAimThroughCachedTarget() {
        final Fixture f = new Fixture();
        final Player player = new Player(-180);
        f.sourceYaw(-180);
        f.select();
        assertTrue(f.target.snapshot().available());
        f.combat.clear();
        f.select();
        assertFalse(f.target.snapshot().available());
        assertFalse(f.aim(player));
        assertEquals(0, player.yawWrites);
    }

    @Test void nativeYawWrapNearMinus180RequiresOnlySmallEligibleMove() {
        final Fixture f = new Fixture();
        final Player player = new Player(179F);
        f.sourceYaw(player.yaw);
        f.select();
        assertTrue(f.nearest.snapshot().found());
        assertTrue(f.nativeClickFov());
        assertTrue(f.aim(player));
        assertEquals(-180F, player.yaw, 0.0001F);
        assertEquals(1, player.yawWrites);
    }

    @Test void originalSourceRotationEvidenceIsNotNativeServerProof() {
        final Fixture f = new Fixture();
        f.sourceYaw(0);
        f.select();
        assertFalse(f.target.snapshot().toString()
                .contains("CUSTOMMC_OFFICIAL_189"));
        assertFalse(f.nativeClickFov());
    }
}
