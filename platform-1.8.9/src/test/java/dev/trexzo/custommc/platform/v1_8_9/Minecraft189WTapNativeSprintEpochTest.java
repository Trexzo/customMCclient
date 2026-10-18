package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Source mapped setSprinting callback policy; not Mojang binary proof. */
final class Minecraft189WTapNativeSprintEpochTest {
    private static final class NativeSprint implements Minecraft189PlayerSprintControl {
        int writes;
        boolean sprint = true;
        Runnable onWrite;
        boolean failAfterWrite;
        @Override public void customMcSetSprinting(final boolean value) {
            writes++;
            sprint = value;
            final Runnable hook = onWrite;
            onWrite = null;
            if (hook != null) hook.run();
            if (failAfterWrite) {
                failAfterWrite = false;
                throw new IllegalStateException("vanilla setter threw after real mutation");
            }
        }
    }
    private static Minecraft189PlayerMovementState.Snapshot movement() {
        final Minecraft189PlayerMovementState m =
                new Minecraft189PlayerMovementState();
        m.update(true,false,true);
        return m.snapshot();
    }
    private static boolean apply(Minecraft189WTapModule m, NativeSprint p,
            boolean left) {
        return m.apply(p,movement(),left,true);
    }
    @Test void nativeOriginalSetterDisableDoesNotScheduleLaterOldReset() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(4);
        m.onEnable();
        p.onWrite = m::onDisable;
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
        assertFalse(m.active());
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
    }
    @Test void offOnReentryBeforeOriginalSprintSetterReturnsRevokesOldWindow() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(3);
        m.onEnable();
        p.onWrite = () -> {m.onDisable();m.onEnable();};
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
        // V207 preserves the physical held-attack edge. A new genuine
        // release then press, not callback replay, may spend a new reset.
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
        assertFalse(apply(m,p,false));
        assertTrue(apply(m,p,true));
        assertEquals(2,p.writes);
    }
    @Test void realGuiFocusDuringOriginalSprintSetterCancelsOldReset() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(4);
        m.onEnable();
        p.onWrite = () -> m.suspendForGui(true);
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
        assertFalse(apply(m,p,false));
        assertTrue(apply(m,p,true));
    }
    @Test void originalSetterChangingResetDurationDoesNotBankStaleTicks() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(4);
        m.onEnable();
        p.onWrite = () -> m.resetTicksSetting().set(2);
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
        assertFalse(apply(m,p,true));
        assertFalse(apply(m,p,false));
        assertTrue(apply(m,p,true));
        assertTrue(apply(m,p,true));
        assertFalse(apply(m,p,true));
        assertEquals(3,p.writes);
    }
    @Test void originalSetterChangingCooldownDoesNotCommitOldCooldown() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.cooldownTicksSetting().set(10);
        m.onEnable();
        p.onWrite = () -> m.cooldownTicksSetting().set(0);
        assertFalse(apply(m,p,true));
        assertFalse(apply(m,p,true));
        assertFalse(apply(m,p,false));
        assertTrue(apply(m,p,true));
    }
    @Test void originalSetterChangingForwardRequirementCancelsSyntheticWindow() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(3);
        m.onEnable();
        p.onWrite = () -> m.requireForwardSetting().set(true);
        assertFalse(apply(m,p,true));
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
    }
    @Test void originalSetterChangingNearbyRangeCancelsOriginalWindow() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(3);
        m.onEnable();
        p.onWrite = () -> m.maxPlayerDistanceSetting().set(6.0D);
        assertFalse(apply(m,p,true));
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
    }
    @Test void secondNativeSetterDisablingDuringMultiTickResetCannotReplayTail() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(4);
        m.onEnable();
        assertTrue(apply(m,p,true));
        p.onWrite = m::onDisable;
        assertFalse(apply(m,p,true));
        assertEquals(2,p.writes);
        assertFalse(apply(m,p,true));
        assertEquals(2,p.writes);
    }
    @Test void secondNativeSetterChangingSourcePolicyDisarmsLaterResetTick() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(4);
        m.onEnable();
        assertTrue(apply(m,p,true));
        p.onWrite = () -> m.pauseWhileSneakingSetting().set(true);
        assertFalse(apply(m,p,true));
        assertEquals(2,p.writes);
        assertFalse(apply(m,p,true));
        assertEquals(2,p.writes);
    }
    @Test void genuineOriginalSetterFailureAfterPartialWriteCannotReplayEdge() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(4);
        m.onEnable();
        p.failAfterWrite = true;
        assertThrows(IllegalStateException.class,()->apply(m,p,true));
        assertFalse(p.sprint);
        assertEquals(1,p.writes);
        assertFalse(apply(m,p,true));
        assertEquals(1,p.writes);
        assertFalse(apply(m,p,false));
        assertTrue(apply(m,p,true));
    }
    @Test void stableOriginalSprintSetterRetainsExistingMultiTickCadence() {
        final Minecraft189WTapModule m = new Minecraft189WTapModule();
        final NativeSprint p = new NativeSprint();
        m.resetTicksSetting().set(3);
        m.onEnable();
        assertTrue(apply(m,p,true));
        assertTrue(apply(m,p,true));
        assertTrue(apply(m,p,true));
        assertFalse(apply(m,p,true));
        assertEquals(3,p.writes);
    }
}
