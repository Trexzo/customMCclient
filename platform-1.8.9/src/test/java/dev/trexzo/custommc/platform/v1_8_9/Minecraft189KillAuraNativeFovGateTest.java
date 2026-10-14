package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Offline source-native click veto; genuine dedicated-server FOV CI remains mandatory. */
final class Minecraft189KillAuraNativeFovGateTest {
    private static final int PLAYER =
            Minecraft189WorldEntityKindState.LIVING
            | Minecraft189WorldEntityKindState.PLAYER;
    private static final int SELF =
            PLAYER | Minecraft189WorldEntityKindState.LOCAL_PLAYER;

    private static boolean permits(double x, double z, float yaw, double fov) {
        Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        local.update(0, 10, 0);
        rotation.update(yaw, 0);
        positions.update(new double[]{0,10,0, x,10,z});
        kinds.update(new int[]{SELF, PLAYER});
        return Minecraft189KillAuraNativeFovGate.permits(
                1, fov, rotation.snapshot(), local.snapshot(),
                positions.snapshot(), kinds.snapshot());
    }

    @Test void originalRealFrontIsEligibleAndRearIsAlwaysVetoed() {
        assertTrue(permits(0, 2, 0, 90), "real player straight ahead");
        assertFalse(permits(0, -2, 0, 90), "genuine 180-degree rear target");
        assertFalse(permits(0, -2, 0, 179), "rear remains outside 179 degrees");
        assertTrue(permits(0, -2, 0, 180), "explicit 180-degree FOV permits rear");
        assertFalse(permits(2, 0, 0, 45), "90 degree side target outside 45 FOV");
        assertTrue(permits(2, 0, -90, 45), "side target when genuinely facing it");
    }

    @Test void yawWrapIsCorrectAtSignedOneEightyBoundary() {
        assertTrue(permits(0, -2, 179, 2), "rear yaw -180 equivalent to +180");
        assertTrue(permits(0, -2, -179, 2));
        assertFalse(permits(0, 2, 179, 90));
        assertFalse(permits(0, 0, 0, 180), "coincident position is not credible aim evidence");
    }

    @Test void untrustedIndexAndSourceStatesFailClosed() {
        Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0,0,0, 0,0,2});
        kinds.update(new int[]{SELF, PLAYER});
        assertFalse(Minecraft189KillAuraNativeFovGate.permits(
                -1, 90, rotation.snapshot(), local.snapshot(), positions.snapshot(), kinds.snapshot()));
        assertFalse(Minecraft189KillAuraNativeFovGate.permits(
                0, 90, rotation.snapshot(), local.snapshot(), positions.snapshot(), kinds.snapshot()),
                "native local-player index cannot own remote attack");
        assertFalse(Minecraft189KillAuraNativeFovGate.permits(
                2, 90, rotation.snapshot(), local.snapshot(), positions.snapshot(), kinds.snapshot()));
        assertFalse(Minecraft189KillAuraNativeFovGate.permits(
                1, Double.NaN, rotation.snapshot(), local.snapshot(), positions.snapshot(), kinds.snapshot()));
        assertFalse(Minecraft189KillAuraNativeFovGate.permits(
                1, 181, rotation.snapshot(), local.snapshot(), positions.snapshot(), kinds.snapshot()));
        positions.clear();
        assertFalse(Minecraft189KillAuraNativeFovGate.permits(
                1, 90, rotation.snapshot(), local.snapshot(), positions.snapshot(), kinds.snapshot()));
        positions.update(new double[]{0,0,0, 0,0,2});
        kinds.update(new int[]{SELF});
        assertFalse(Minecraft189KillAuraNativeFovGate.permits(
                1, 90, rotation.snapshot(), local.snapshot(), positions.snapshot(), kinds.snapshot()));
    }
}
