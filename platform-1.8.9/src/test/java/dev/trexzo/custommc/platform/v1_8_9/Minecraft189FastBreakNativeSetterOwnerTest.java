package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-shaped setter callbacks; not a substitute for Mojang X11 CI. */
final class Minecraft189FastBreakNativeSetterOwnerTest {
    private static final class Controller implements Minecraft189BlockHitDelayControl {
        int delay=5,writes;
        Runnable onWrite;
        boolean failWrite;
        @Override public void customMcSetBlockHitDelay(int value) {
            writes++;if(writes>8)throw new AssertionError("recursive native cooldown setter");
            delay=value;
            final Runnable callback=onWrite;onWrite=null;
            if(callback!=null)callback.run();
            if(failWrite)throw new IllegalStateException("original cooldown setter");
        }
    }
    private static Minecraft189PlayerMovementState.Snapshot movement(
            boolean ground, boolean sneaking) {
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        state.update(ground,sneaking,false);return state.snapshot();
    }
    private static Minecraft189FastBreakModule module() {
        final Minecraft189FastBreakModule m=new Minecraft189FastBreakModule();
        m.onEnable();return m;
    }
    @Test void originalControllerSetsDefaultVanillaHitDelayOnce() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.apply(c);assertEquals(0,c.delay);assertEquals(1,c.writes);
    }
    @Test void originalSameControllerSetterReentryNeverDoubleWrites() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        c.onWrite=()->m.apply(c);
        m.apply(c);assertEquals(1,c.writes);assertEquals(0,c.delay);
    }
    @Test void originalAnotherControllerInSetterCannotSpendStack() {
        final Minecraft189FastBreakModule m=module();
        final Controller a=new Controller(),b=new Controller();
        a.onWrite=()->m.apply(b);
        m.apply(a);assertEquals(1,a.writes);assertEquals(0,b.writes);
        assertEquals(5,b.delay);
    }
    @Test void originalNullControllerInSetterCannotReenter() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        c.onWrite=()->m.apply((Minecraft189BlockHitDelayControl)null);
        m.apply(c);assertEquals(1,c.writes);
    }
    @Test void oldSetterOffOnDoesNotAllowNestedNewActivation() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        c.onWrite=()->{m.onDisable();m.onEnable();m.apply(c);};
        m.apply(c);assertEquals(1,c.writes);
        m.delaySetting().set(2);m.apply(c);
        assertEquals(2,c.writes);assertEquals(2,c.delay);
    }
    @Test void nativeSetterDisableDoesNotInjectSecondDelay() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        c.onWrite=()->{m.onDisable();m.apply(c);};
        m.apply(c);assertEquals(1,c.writes);assertFalse(m.active());
        m.apply(c);assertEquals(1,c.writes);
    }
    @Test void nativeSetterLivePolicyEditAppliesOnNextRealTickOnly() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        c.onWrite=()->{m.delaySetting().set(3);m.apply(c);};
        m.apply(c);assertEquals(1,c.writes);assertEquals(0,c.delay);
        m.apply(c);assertEquals(2,c.writes);assertEquals(3,c.delay);
    }
    @Test void mappedSetterThrowPreservesPartialOriginalCooldown() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        c.failWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(c));
        assertEquals(1,c.writes);assertEquals(0,c.delay);
        c.failWrite=false;m.delaySetting().set(2);
        m.apply(c);assertEquals(2,c.writes);assertEquals(2,c.delay);
    }
    @Test void mappedSetterThrowAfterReentryStillReleasesNativeGuard() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        c.onWrite=()->m.apply(c);c.failWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(c));
        assertEquals(1,c.writes);
        c.failWrite=false;m.apply(c);assertEquals(2,c.writes);
    }
    @Test void originalGuiFocusNeverSetsNativeDelay() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.apply(c,false,null,true);
        assertEquals(5,c.delay);assertEquals(0,c.writes);
    }
    @Test void originalGuiReentryInsideSetterNeverAddsNewWrite() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        c.onWrite=()->m.apply(c,true,movement(true,false),true);
        m.apply(c,true,movement(true,false),false);
        assertEquals(1,c.writes);
    }
    @Test void optedInAttackGateNeedsRealHeldAttack() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.requireAttackHeldSetting().set(Boolean.TRUE);
        m.apply(c);assertEquals(0,c.writes);
        m.apply(c,true,movement(true,false));assertEquals(1,c.writes);
    }
    @Test void optedInSneakingGateRejectsUnknownSource() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        m.apply(c);assertEquals(0,c.writes);
        m.apply(c,true,movement(true,true));assertEquals(0,c.writes);
        m.apply(c,true,movement(true,false));assertEquals(1,c.writes);
    }
    @Test void optedInGroundOnlyCannotInventMissingMovement() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(c,true,null);assertEquals(0,c.writes);
        m.apply(c,true,movement(false,false));assertEquals(0,c.writes);
        m.apply(c,true,movement(true,false));assertEquals(1,c.writes);
    }
    @Test void originalAirborneOverrideRequiresConfirmedNativeMovement() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.delaySetting().set(3);
        m.airborneOverrideSetting().set(Boolean.TRUE);
        m.airborneDelaySetting().set(1);
        m.apply(c);assertEquals(3,c.delay);
        m.apply(c,true,movement(false,false));assertEquals(1,c.delay);
        m.apply(c,true,movement(true,false));assertEquals(3,c.delay);
    }
    @Test void originalGroundOnlyBlocksAirborneOverrideEvenWhenEnabled() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.airborneOverrideSetting().set(Boolean.TRUE);
        m.airborneDelaySetting().set(1);
        m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(c,true,movement(false,false));assertEquals(0,c.writes);
        m.apply(c,true,movement(true,false));assertEquals(0,c.delay);
    }
    @Test void originalSetterNestedAnotherSourceOnlyWritesAfterReturn() {
        final Minecraft189FastBreakModule m=module();
        final Controller a=new Controller(),b=new Controller();
        a.onWrite=()->m.apply(b,true,movement(true,false),false);
        m.apply(a);assertEquals(0,b.writes);
        m.delaySetting().set(4);m.apply(b);
        assertEquals(1,b.writes);assertEquals(4,b.delay);
    }
    @Test void callbackWithLiveSneakChangeCannotInjectSecondWrite() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        c.onWrite=()->m.apply(c,true,movement(true,true),false);
        m.apply(c,true,movement(true,false),false);
        assertEquals(1,c.writes);
    }
    @Test void zeroAndFiveOriginalAllowedBoundariesPersist() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.delaySetting().set(5);m.apply(c);
        assertEquals(5,c.delay);
        m.delaySetting().set(0);m.apply(c);
        assertEquals(0,c.delay);assertEquals(2,c.writes);
    }
    @Test void disabledModuleAndNullControllerAreSourceNoops() {
        final Minecraft189FastBreakModule m=module();final Controller c=new Controller();
        m.apply((Minecraft189BlockHitDelayControl)null);assertEquals(0,c.writes);
        m.onDisable();m.apply(c);assertEquals(0,c.writes);
    }
}
