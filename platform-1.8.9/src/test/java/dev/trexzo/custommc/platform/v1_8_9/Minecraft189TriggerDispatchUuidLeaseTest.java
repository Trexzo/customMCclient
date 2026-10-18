package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Strict original World player UUID equality across TriggerBot scheduling
 * and PlayerControllerMP dispatch; offline policy, not Mojang binary proof.
 */
final class Minecraft189TriggerDispatchUuidLeaseTest {
    private static final UUID A = new UUID(0L, 501L);
    private static final UUID B = new UUID(0L, 502L);

    @Test void originalSamePlayerSameActivationSpendsExactlyOneDispatch() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(A, 1L);
        assertTrue(lease.consume(A,1L,true,false));
        assertFalse(lease.consume(A,1L,true,false),
                "no double native attack from a single scheduled click");
    }
    @Test void indexRecycledFromOriginalAtoBDoesNotInheritASchedule() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(A, 1L);
        assertFalse(lease.consume(B,1L,true,false));
        assertFalse(lease.consume(A,1L,true,false),
                "old A cannot replay after the B-index veto");
    }
    @Test void originalTargetABABackingIndexSwitchNeverGainsPastCredit() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(A,2L);
        assertFalse(lease.consume(B,2L,true,false));
        lease.arm(B,2L);
        assertFalse(lease.consume(A,2L,true,false));
        lease.arm(A,2L);
        assertTrue(lease.consume(A,2L,true,false));
        assertFalse(lease.consume(B,2L,true,false));
    }
    @Test void moduleOffOnWhileNativeHotbarChangesCannotSpendOldUUID() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(A,4L);
        assertFalse(lease.consume(A,6L,true,false));
        assertFalse(lease.consume(A,4L,true,false));
    }
    @Test void triggerBotDisabledBeforeNativeControllerCannotAttack() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(A,2L);
        assertFalse(lease.consume(A,2L,false,false));
        assertFalse(lease.consume(A,2L,true,false));
    }
    @Test void clickGuiFocusOpeningDuringNativeSlotSwitchRevokesClick() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(B,3L);
        assertFalse(lease.consume(B,3L,true,true));
        assertFalse(lease.consume(B,3L,true,false));
    }
    @Test void newSyntheticSchedulerAttemptSupersedesOldNativeSource() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(A,1L);
        lease.arm(B,1L);
        assertFalse(lease.consume(A,1L,true,false));
        lease.arm(B,1L);
        assertTrue(lease.consume(B,1L,true,false));
    }
    @Test void noSourceUUIDCannotBeArmedOrApproved() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(null,1L);
        assertFalse(lease.consume(A,1L,true,false));
        lease.arm(A,1L);
        assertFalse(lease.consume(null,1L,true,false));
    }
    @Test void cancelledSourceLeaseCannotBeReplayedAfterSlotRestores() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(B,2L);
        lease.clear();
        assertFalse(lease.consume(B,2L,true,false));
        lease.arm(B,2L);
        assertTrue(lease.consume(B,2L,true,false));
    }
    @Test void laterRealTargetWithSameUUIDRequiresNewSourceAdmission() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(B,1L);
        assertTrue(lease.consume(B,1L,true,false));
        assertFalse(lease.consume(B,1L,true,false));
        lease.arm(B,1L);
        assertTrue(lease.consume(B,1L,true,false));
    }
    @Test void falseResultCannotBeRescuedByLateEnable() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(A,10L);
        assertFalse(lease.consume(A,10L,false,false));
        assertFalse(lease.consume(A,10L,true,false));
    }
    @Test void cancelledOldOriginalIndexNeverOverridesNewlyArmedB() {
        Minecraft189TriggerDispatchUuidLease lease =
                new Minecraft189TriggerDispatchUuidLease();
        lease.arm(A,1L);
        lease.clear();
        lease.arm(B,2L);
        assertFalse(lease.consume(A,1L,true,false));
        // Even a wrong first dispatch consumes B's one-shot lease.
        lease.arm(B,2L);
        assertTrue(lease.consume(B,2L,true,false));
    }
}
