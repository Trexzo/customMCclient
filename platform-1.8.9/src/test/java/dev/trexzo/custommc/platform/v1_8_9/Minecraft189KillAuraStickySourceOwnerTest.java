package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Native World UUID lock, not list position or a nearest-distance proxy.
 * No synthetic controller, attack packet, or server health is invented.
 */
final class Minecraft189KillAuraStickySourceOwnerTest {
    private static final UUID LOCAL =
            UUID.fromString("00000000-0000-4000-8000-000000000009");
    private static final UUID A =
            UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID B =
            UUID.fromString("20000000-0000-4000-8000-000000000002");
    private static final int PLAYER =
            Minecraft189WorldEntityKindState.LIVING
            | Minecraft189WorldEntityKindState.PLAYER;
    private static final int SELF =
            PLAYER | Minecraft189WorldEntityKindState.LOCAL_PLAYER;

    private final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
    private final Minecraft189PlayerPositionState local =
            new Minecraft189PlayerPositionState();
    private final Minecraft189PlayerRotationState rotation =
            new Minecraft189PlayerRotationState();
    private final Minecraft189WorldEntityPositionState positions =
            new Minecraft189WorldEntityPositionState();
    private final Minecraft189WorldEntityKindState kinds =
            new Minecraft189WorldEntityKindState();
    private final Minecraft189WorldEntityCombatState combat =
            new Minecraft189WorldEntityCombatState();
    private final Minecraft189WorldEntityUuidState identities =
            new Minecraft189WorldEntityUuidState();
    private final Minecraft189NearestPlayerTargetState selected =
            new Minecraft189NearestPlayerTargetState();
    private final Minecraft189TargetRotationState targetRotation =
            new Minecraft189TargetRotationState();

    private void start() {
        aura.lockTargetSetting().set(Boolean.TRUE);
        aura.fovSetting().set(35.0D);
        aura.minCpsSetting().set(20);
        aura.maxCpsSetting().set(20);
        aura.pauseSneakSetting().set(Boolean.FALSE);
        aura.onEnable();
        local.update(0.0, 0.0, 0.0);
        rotation.update(-90.0F, 0.0F);
    }

    private void world(final UUID[] ids, final int[] bits,
            final int[] health, final double[] packed) {
        assertTrue(identities.update(ids));
        kinds.update(bits);
        combat.update(health);
        positions.update(packed);
    }

    private Minecraft189NearestPlayerTargetState.Snapshot tick() {
        Minecraft189KillAuraTargetSelector.select(
                aura, local.snapshot(), rotation.snapshot(),
                positions.snapshot(), kinds.snapshot(), combat.snapshot(),
                null, null, null, identities.snapshot(),
                null, null, selected);
        final Minecraft189NearestPlayerTargetState.Snapshot candidate =
                selected.snapshot();
        targetRotation.update(local.snapshot(), candidate);
        return candidate;
    }

    private void initialB() {
        world(new UUID[] {LOCAL, B}, new int[] {SELF, PLAYER},
                new int[] {1, 1}, new double[] {0, 0, 0, 2, 0, 0});
        final Minecraft189NearestPlayerTargetState.Snapshot first = tick();
        assertTrue(first.found());
        assertEquals(1, first.entityIndex());
        assertEquals(B, aura.lockedTargetUuid());
    }

    @Test
    void rejoinedCloserAAndReorderedBOutsideFovNeverStealsB() {
        start();
        initialB();
        // A's fresh original EntityPlayer occupies index 1 and is closer.
        // The same original UUID B has moved to index 2 outside yaw FOV.
        world(new UUID[] {LOCAL, A, B},
                new int[] {SELF, PLAYER, PLAYER},
                new int[] {1, 1, 1},
                new double[] {0, 0, 0, 1, 0, 0, 0, 0, 2});
        final Minecraft189NearestPlayerTargetState.Snapshot gap = tick();
        assertTrue(gap.available());
        assertFalse(gap.found());
        assertEquals(B, aura.lockedTargetUuid());

        final CountingOriginalPlayer player = new CountingOriginalPlayer();
        assertFalse(aura.aim(player, rotation.snapshot(),
                targetRotation.snapshot(), false, null));
        assertEquals(0, player.writes);
        assertEquals(B, aura.lockedTargetUuid());
        assertFalse(aura.shouldClick(true, 1, rotation.snapshot(),
                targetRotation.snapshot(), false, false, null, false));
        assertEquals(B, aura.lockedTargetUuid());

        // A physically remains closer and loaded, but B is again in FOV.
        rotation.update(0.0F, 0.0F);
        final Minecraft189NearestPlayerTargetState.Snapshot resumed = tick();
        assertTrue(resumed.found());
        assertEquals(2, resumed.entityIndex());
        assertEquals(B, aura.lockedTargetUuid());
        assertTrue(aura.aim(player, rotation.snapshot(),
                targetRotation.snapshot(), false, null));
        assertEquals(0, player.writes);
        assertTrue(aura.shouldClick(true, 2, rotation.snapshot(),
                targetRotation.snapshot(), false, false, null, false));
    }

    @Test
    void hurtTimeVetoRetainsBButNeverSelectsCloserA() {
        start();
        aura.switchHurtTargetsSetting().set(Boolean.TRUE);
        aura.maxSwitchHurtTicksSetting().set(1);
        initialB();
        world(new UUID[] {LOCAL, A, B},
                new int[] {SELF, PLAYER, PLAYER},
                new int[] {1, 1, 11}, // real B hurtTime=5
                new double[] {0, 0, 0, 1, 0, 0, 2, 0, 0});
        final Minecraft189NearestPlayerTargetState.Snapshot blocked = tick();
        assertFalse(blocked.found());
        assertEquals(B, aura.lockedTargetUuid());
        assertFalse(aura.aim(new CountingOriginalPlayer(), rotation.snapshot(),
                targetRotation.snapshot(), false, null));
        assertEquals(B, aura.lockedTargetUuid());

        combat.update(new int[] {1, 1, 1});
        final Minecraft189NearestPlayerTargetState.Snapshot healed = tick();
        assertTrue(healed.found());
        assertEquals(2, healed.entityIndex());
        assertEquals(B, aura.lockedTargetUuid());
    }

    @Test
    void actualBUnloadOrOutOfRangeReleasesAndReselectsCurrentEligibleA() {
        start();
        initialB();
        world(new UUID[] {LOCAL, A}, new int[] {SELF, PLAYER},
                new int[] {1, 1},
                new double[] {0, 0, 0, 1, 0, 0});
        final Minecraft189NearestPlayerTargetState.Snapshot noB = tick();
        assertTrue(noB.found());
        assertEquals(A, aura.lockedTargetUuid());

        aura.rememberSelectedTarget(B);
        world(new UUID[] {LOCAL, A, B}, new int[] {SELF, PLAYER, PLAYER},
                new int[] {1, 1, 1},
                new double[] {0, 0, 0, 1, 0, 0, 8, 0, 0});
        final Minecraft189NearestPlayerTargetState.Snapshot outOfRange = tick();
        assertTrue(outOfRange.found());
        assertEquals(1, outOfRange.entityIndex());
        assertEquals(A, aura.lockedTargetUuid());
    }

    @Test
    void originalAntiBotDisallowanceReleasesBWithoutBypassingPolicy() {
        start();
        initialB();
        final Minecraft189AntiBotModule antiBot =
                new Minecraft189AntiBotModule();
        antiBot.onEnable();
        try {
            // Original native World proves B absent from network tab,
            // whereas newly loaded A has known, present network identity.
            world(new UUID[] {LOCAL, A, B},
                    new int[] {SELF, PLAYER, PLAYER},
                    new int[] {
                        1,
                        1 | Minecraft189WorldEntityCombatState.NETWORK_INFO_KNOWN
                          | Minecraft189WorldEntityCombatState.NETWORK_INFO_PRESENT,
                        1 | Minecraft189WorldEntityCombatState.NETWORK_INFO_KNOWN
                    },
                    new double[] {0, 0, 0, 1, 0, 0, 2, 0, 0});
            Minecraft189KillAuraTargetSelector.select(
                    aura, local.snapshot(), rotation.snapshot(),
                    positions.snapshot(), kinds.snapshot(), combat.snapshot(),
                    antiBot, null, null, identities.snapshot(),
                    null, null, selected);
            assertTrue(selected.snapshot().found());
            assertEquals(1, selected.snapshot().entityIndex());
            assertEquals(A, aura.lockedTargetUuid());
        } finally {
            antiBot.onDisable();
        }
    }

    @Test
    void originalWorldOrModuleLifecycleExplicitlyRevokesUuid() {
        start();
        initialB();
        aura.suspend();
        assertNull(aura.lockedTargetUuid());
        aura.rememberSelectedTarget(B);
        assertEquals(B, aura.lockedTargetUuid());
        aura.onDisable();
        assertNull(aura.lockedTargetUuid());
        aura.onEnable();
        assertNull(aura.lockedTargetUuid());
        aura.rememberSelectedTarget(B);
        assertEquals(B, aura.lockedTargetUuid());
        // HostRuntime calls this when original source changes or is revoked.
        aura.rememberSelectedTarget(null);
        assertNull(aura.lockedTargetUuid());
    }

    private static final class CountingOriginalPlayer
            implements Minecraft189PlayerRotationControl {
        int writes;
        public float customMcRotationYaw() { return 0.0F; }
        public float customMcRotationPitch() { return 0.0F; }
        public void customMcSetRotationYaw(final float yaw) { writes++; }
        public void customMcSetRotationPitch(final float pitch) { writes++; }
    }
}
