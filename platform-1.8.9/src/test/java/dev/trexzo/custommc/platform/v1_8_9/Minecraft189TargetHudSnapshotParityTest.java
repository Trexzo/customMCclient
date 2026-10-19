package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Exact source-selected player and original calculated yaw/pitch
 * must refer to one matching snapshot. Offline Java source adapter;
 * not a substitute for original Mojang rendering binary proof.
 */
final class Minecraft189TargetHudSnapshotParityTest {
    private static final class Source {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState world =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState rotation =
                new Minecraft189TargetRotationState();
        Source() {
            local.update(0D,64D,0D);
            world.update(new double[]{
                    0D,64D,0D, 3D,64D,4D, 0D,64D,8D
            });
            kinds.update(new int[]{
                    Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                    Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LIVING,
                    Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LIVING
            });
        }
        void select(int index) {
            target.update(local.snapshot(),world.snapshot(),kinds.snapshot(),
                    0D,40D,c->c.entityIndex()==index);
        }
        void updateRotation() {
            rotation.update(local.snapshot(),target.snapshot());
        }
        boolean matches() {
            return Minecraft189TargetHudModule.matchingSourceTarget(
                    target.snapshot(),rotation.snapshot());
        }
    }

    @Test void noActualWorldTargetCannotProduceHudDirectionCard() {
        Source s=new Source();
        assertFalse(s.matches());
    }
    @Test void selectedRealAWithMatchingRotationCanRenderTogether() {
        Source s=new Source();s.select(1);s.updateRotation();
        assertTrue(s.matches());
        assertEquals(25D,s.target.snapshot().distanceSquared(),0D);
    }
    @Test void selectedRealBWithMatchingRotationCanRenderTogether() {
        Source s=new Source();s.select(2);s.updateRotation();
        assertTrue(s.matches());
        assertEquals(64D,s.target.snapshot().distanceSquared(),0D);
    }
    @Test void newWorldBTargetCannotDisplayOldAOriginalDirection() {
        Source s=new Source();s.select(1);s.updateRotation();
        s.select(2);
        assertFalse(s.matches());
        assertEquals(2,s.target.snapshot().entityIndex());
        assertEquals(1,s.rotation.snapshot().entityIndex());
    }
    @Test void returningWorldAAfterOldBRotationMustRefreshDirection() {
        Source s=new Source();s.select(2);s.updateRotation();
        s.select(1);
        assertFalse(s.matches());
        s.updateRotation();
        assertTrue(s.matches());
    }
    @Test void sameLoadedIndexDifferentActualDistanceRejectsOldYawPitch() {
        Source s=new Source();s.select(1);s.updateRotation();
        s.world.update(new double[]{
                0D,64D,0D, 6D,64D,0D, 0D,64D,8D
        });
        s.select(1);
        assertFalse(s.matches());
        assertEquals(1,s.target.snapshot().entityIndex());
        assertEquals(36D,s.target.snapshot().distanceSquared(),0D);
        assertEquals(25D,s.rotation.snapshot().distanceSquared(),0D);
    }
    @Test void freshRotationForNewOriginalDistanceRestoresCard() {
        Source s=new Source();s.select(1);s.updateRotation();
        s.world.update(new double[]{
                0D,64D,0D, 6D,64D,0D, 0D,64D,8D
        });
        s.select(1);
        assertFalse(s.matches());
        s.updateRotation();
        assertTrue(s.matches());
    }
    @Test void originalTargetRevocationNeverDisplaysOldDirection() {
        Source s=new Source();s.select(1);s.updateRotation();
        s.target.clear();
        assertFalse(s.matches());
        assertFalse(s.target.snapshot().available());
        assertTrue(s.rotation.snapshot().available());
    }
    @Test void oldRotationClearedCannotBePairedWithCurrentTarget() {
        Source s=new Source();s.select(1);s.updateRotation();
        s.rotation.clear();
        assertFalse(s.matches());
        assertTrue(s.target.snapshot().found());
    }
    @Test void noEligibleSourcePlayersDoesNotDisplayHistoricalYaw() {
        Source s=new Source();s.select(1);s.updateRotation();
        s.select(25);
        assertFalse(s.matches());
        assertTrue(s.target.snapshot().available());
        assertFalse(s.target.snapshot().found());
    }
    @Test void sourceLocalPlayerMoveChangesOnlyRotationWhenIndexAndDistanceStillMatch() {
        Source s=new Source();s.select(1);s.updateRotation();
        // This field checks congruence of the two published source
        // snapshots, NOT independent native PlayerController UUID.
        assertTrue(s.matches());
        s.local.update(0D,65D,0D);
        // Source target not recalculated yet: its pair remains consistent
        // with its own old snapshot until a new real World scan arrives.
        assertTrue(s.matches());
        s.select(1);
        assertFalse(s.matches());
    }
    @Test void afterMovementRecalculationAndRotationSameTargetRemainsLegitimate() {
        Source s=new Source();s.select(1);s.updateRotation();
        s.local.update(0D,65D,0D);
        s.select(1);
        s.updateRotation();
        assertTrue(s.matches());
    }
    @Test void noMissingSnapshotCanEverPassHudSourceCongruence() {
        Source s=new Source();s.select(1);s.updateRotation();
        assertFalse(Minecraft189TargetHudModule.matchingSourceTarget(
                null,s.rotation.snapshot()));
        assertFalse(Minecraft189TargetHudModule.matchingSourceTarget(
                s.target.snapshot(),null));
    }
    @Test void normalPositionAndAnglesNeverModifyNativeSourceWorld() {
        Source s=new Source();s.select(1);s.updateRotation();
        double x=s.world.snapshot().x(1);
        double z=s.world.snapshot().z(1);
        for(int i=0;i<3;i++)assertTrue(s.matches());
        assertEquals(x,s.world.snapshot().x(1),0D);
        assertEquals(z,s.world.snapshot().z(1),0D);
    }
}
