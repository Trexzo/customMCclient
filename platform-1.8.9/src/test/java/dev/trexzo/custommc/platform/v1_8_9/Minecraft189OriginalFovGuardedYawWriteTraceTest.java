package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V287 tests the ACTUAL rotation source lease and opt-in recorder path.
 * Synthetic Java player fixture: no real Minecraft/server PASS.
 */
final class Minecraft189OriginalFovGuardedYawWriteTraceTest {
    private static final class Player implements Minecraft189PlayerRotationControl {
        float yaw;
        float pitch;
        int writes;
        boolean failAfterYawWrite;
        @Override public float customMcRotationYaw() { return yaw; }
        @Override public float customMcRotationPitch() { return pitch; }
        @Override public void customMcSetRotationYaw(final float v) {
            writes++;
            yaw = v;
            if (failAfterYawWrite)
                throw new IllegalStateException("real setter threw after mutation");
        }
        @Override public void customMcSetRotationPitch(final float v) { pitch = v; }
    }

    @Test void originalNativeSetterCompletesBeforeObserverRecordsSuccess() {
        final Minecraft189RotationHostSourceLease lease =
                new Minecraft189RotationHostSourceLease();
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        final Player original = new Player();
        assertTrue(lease.enter(original, false));
        final Minecraft189PlayerRotationControl control =
                lease.guardedControl(original, () -> false,
                        v -> {
                            assertEquals(v, original.yaw);
                            assertEquals(1, original.writes);
                            trace.afterOriginalNativeSetter(v);
                        });
        control.customMcSetRotationYaw(-10F);
        assertEquals(1, original.writes);
        assertEquals(1, trace.snapshot().completedWrites);
        assertEquals(-10F, trace.snapshot().lastCompletedYaw);
        lease.exit();
    }

    @Test void revokedSourceNeverInvokesNativeSetterOrObserver() {
        final Minecraft189RotationHostSourceLease lease =
                new Minecraft189RotationHostSourceLease();
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        final Player original = new Player();
        assertTrue(lease.enter(original, false));
        final Minecraft189PlayerRotationControl control =
                lease.guardedControl(original, () -> true,
                        trace::afterOriginalNativeSetter);
        control.customMcSetRotationYaw(-90F);
        assertEquals(0, original.writes);
        assertEquals(0, trace.snapshot().completedWrites);
        lease.exit();
    }

    @Test void nativeSetterExceptionNeverCountsAsCompletedWrite() {
        final Minecraft189RotationHostSourceLease lease =
                new Minecraft189RotationHostSourceLease();
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        final Player original = new Player();
        original.failAfterYawWrite = true;
        assertTrue(lease.enter(original, false));
        final Minecraft189PlayerRotationControl control =
                lease.guardedControl(original, () -> false,
                        trace::afterOriginalNativeSetter);
        assertThrows(IllegalStateException.class,
                () -> control.customMcSetRotationYaw(27F));
        // The genuine method mutated original yaw THEN threw. It is not
        // falsely reported as a successfully completed native call.
        assertEquals(1, original.writes);
        assertEquals(27F, original.yaw);
        assertEquals(0, trace.snapshot().completedWrites);
        lease.exit();
    }

    @Test void noObserverRetainsUnmodifiedGuardedSetterSemantics() {
        final Minecraft189RotationHostSourceLease lease =
                new Minecraft189RotationHostSourceLease();
        final Player original = new Player();
        assertTrue(lease.enter(original, false));
        lease.guardedControl(original, () -> false)
                .customMcSetRotationYaw(-25F);
        assertEquals(1, original.writes);
        assertEquals(-25F, original.yaw);
        lease.exit();
    }

    @Test void auraWindowAttributesOnlyNewSuccessfulWrites() {
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        trace.afterOriginalNativeSetter(10F); // preceding Spin or other module
        final int beforeAim = trace.completedWrites();
        trace.afterOriginalNativeSetter(-15F);
        trace.afterAuraAim(beforeAim);
        final Minecraft189OriginalFovGuardedYawWriteTrace.Snapshot s =
                trace.snapshot();
        assertEquals(2, s.completedWrites);
        assertEquals(1, s.auraAttributableWrites);
        assertEquals(-15F, s.lastAuraCompletedYaw);
        assertEquals(-15F, s.lastCompletedYaw);
    }

    @Test void auraReturnedWithoutNativeWriteDoesNotInventAttribution() {
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        trace.afterOriginalNativeSetter(10F);
        trace.afterAuraAim(trace.completedWrites());
        final Minecraft189OriginalFovGuardedYawWriteTrace.Snapshot s =
                trace.snapshot();
        assertEquals(1, s.completedWrites);
        assertEquals(0, s.auraAttributableWrites);
        assertTrue(Float.isNaN(s.lastAuraCompletedYaw));
    }

    @Test void deniedLeaseDoesNotContributeAnAuraYawWrite() {
        final Minecraft189RotationHostSourceLease lease =
                new Minecraft189RotationHostSourceLease();
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        final Player p = new Player();
        assertTrue(lease.enter(p, false));
        final int before = trace.completedWrites();
        lease.guardedControl(p, () -> true, trace::afterOriginalNativeSetter)
                .customMcSetRotationYaw(-180F);
        trace.afterAuraAim(before);
        assertEquals(0, p.writes);
        assertEquals(0, trace.snapshot().auraAttributableWrites);
        lease.exit();
    }

    @Test void staleOrNegativeBeforeCountNeverInventsAuraSuccess() {
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        trace.afterOriginalNativeSetter(-170F);
        trace.afterAuraAim(-1);
        trace.afterAuraAim(5);
        assertEquals(0, trace.snapshot().auraAttributableWrites);
    }

    @Test void signedYawValuesAreObservedNotClampedByDiagnostic() {
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        trace.afterOriginalNativeSetter(179F);
        trace.afterOriginalNativeSetter(-180F);
        trace.afterAuraAim(1);
        assertEquals(-180F, trace.snapshot().lastCompletedYaw);
        assertEquals(-180F, trace.snapshot().lastAuraCompletedYaw);
        assertEquals(2, trace.snapshot().completedWrites);
    }

    @Test void resetClearsAllOriginalSourceProvenance() {
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        trace.afterOriginalNativeSetter(-170F);
        trace.afterAuraAim(0);
        trace.reset();
        final Minecraft189OriginalFovGuardedYawWriteTrace.Snapshot s =
                trace.snapshot();
        assertEquals(0, s.completedWrites);
        assertEquals(0, s.auraAttributableWrites);
        assertTrue(Float.isNaN(s.lastCompletedYaw));
        assertTrue(Float.isNaN(s.lastAuraCompletedYaw));
    }

    @Test void offlineRecorderCanNeverClaimOriginalServerOrClientPass() {
        final Minecraft189OriginalFovGuardedYawWriteTrace trace =
                new Minecraft189OriginalFovGuardedYawWriteTrace();
        trace.afterOriginalNativeSetter(-180F);
        assertFalse(trace.snapshot().toString().contains("PASS=YES"));
    }
}
