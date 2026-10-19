package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Native-shaped Java adapter regressions; full Mojang original CI separately. */
final class Minecraft189TimerSpeedNativeClockOwnerTest {
    private static final class Clock implements Minecraft189TimerSpeedControl {
        float speed=1F;int reads,writes;
        Runnable onRead,onWrite;
        boolean throwRead,throwWrite;
        @Override public float customMcTimerSpeed() {
            reads++;if(reads>12)throw new AssertionError("recursive original clock read");
            final Runnable r=onRead;onRead=null;if(r!=null)r.run();
            if(throwRead)throw new IllegalStateException("native Timer read");
            return speed;
        }
        @Override public void customMcSetTimerSpeed(float next) {
            writes++;if(writes>12)throw new AssertionError("recursive original clock set");
            speed=next;
            final Runnable r=onWrite;onWrite=null;if(r!=null)r.run();
            if(throwWrite)throw new IllegalStateException("native Timer write");
        }
    }
    private static Minecraft189TimerSpeedModule module(){
        final Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        m.speedPercentSetting().set(200);m.onEnable();return m;
    }
    private static Minecraft189PlayerMovementState.Snapshot movement(
            boolean ground,boolean sneak,boolean sprint){
        final Minecraft189PlayerMovementState m=new Minecraft189PlayerMovementState();
        m.update(ground,sneak,sprint);return m.snapshot();
    }
    @Test void genuineVanillaTimerAcceleratesOnceAndAvoidsRedundantWrites(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.apply(c);assertEquals(2F,c.speed,1E-6F);assertEquals(1,c.writes);
        m.apply(c);assertEquals(1,c.writes);assertEquals(2,c.reads);
    }
    @Test void nestedSameClockOriginalGetterCannotDoubleAccelerate(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.onRead=()->m.apply(c);
        m.apply(c);assertEquals(1,c.reads);assertEquals(1,c.writes);
    }
    @Test void nestedSameClockOriginalSetterCannotDoubleAccelerate(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.onWrite=()->m.apply(c);
        m.apply(c);assertEquals(1,c.reads);assertEquals(1,c.writes);
    }
    @Test void changedOriginalTimerInsideGetterRevokesOldClock(){
        final Minecraft189TimerSpeedModule m=module();final Clock a=new Clock(),b=new Clock();
        a.onRead=()->m.apply(b);
        m.apply(a);assertEquals(1F,a.speed,1E-6F);
        assertEquals(0,a.writes+b.reads+b.writes);
    }
    @Test void changedOriginalTimerInsideSetterDoesNotBorrowNewClock(){
        final Minecraft189TimerSpeedModule m=module();final Clock a=new Clock(),b=new Clock();
        a.onWrite=()->m.apply(b);
        m.apply(a);assertEquals(2F,a.speed,1E-6F);
        assertEquals(0,b.reads+b.writes);
    }
    @Test void nullOriginalTimerInsideGetterRevokesOldClock(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.onRead=()->m.apply((Minecraft189TimerSpeedControl)null);
        m.apply(c);assertEquals(1F,c.speed,1E-6F);assertEquals(0,c.writes);
    }
    @Test void optInGuiFocusDuringGetterRestoresPreviouslyBoostedClock(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.pauseClickGuiSetting().set(Boolean.TRUE);
        c.speed=2F;
        c.onRead=()->m.apply(c,movement(true,false,true),true);
        m.apply(c,movement(true,false,true),false);
        assertEquals(1F,c.speed,1E-6F);assertEquals(1,c.writes);
    }
    @Test void optInGuiFocusDuringSetterImmediatelyReleasesClock(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.pauseClickGuiSetting().set(Boolean.TRUE);
        c.onWrite=()->m.apply(c,movement(true,false,true),true);
        m.apply(c,movement(true,false,true),false);
        assertEquals(1F,c.speed,1E-6F);assertEquals(2,c.writes);
    }
    @Test void guiFocusWithDefaultPauseOffPreservesTimerAcceleration(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        assertFalse(m.pauseClickGuiSetting().get());
        c.onRead=()->m.apply(c,movement(true,false,true),true);
        m.apply(c,movement(true,false,true),false);
        assertEquals(2F,c.speed,1E-6F);assertEquals(1,c.writes);
    }
    @Test void disableDuringGetterRestoresNativeOneTimesClock(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.speed=2F;c.onRead=m::onDisable;
        m.apply(c);assertEquals(1F,c.speed,1E-6F);assertEquals(1,c.writes);
        assertFalse(m.active());
    }
    @Test void disableDuringSetterRestoresNativeOneTimesClock(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.onWrite=m::onDisable;
        m.apply(c);assertEquals(1F,c.speed,1E-6F);assertEquals(2,c.writes);
    }
    @Test void offOnInsideGetterNeverBorrowsFreshClockActivation(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.onRead=()->{m.onDisable();m.onEnable();m.apply(c);};
        m.apply(c);assertEquals(1F,c.speed,1E-6F);assertEquals(0,c.writes);
        m.apply(c);assertEquals(2F,c.speed,1E-6F);
    }
    @Test void offOnInsideSetterDoesNotClobberFreshClockActivation(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.onWrite=()->{m.onDisable();m.onEnable();m.apply(c);};
        m.apply(c);assertEquals(2F,c.speed,1E-6F);assertEquals(1,c.writes);
    }
    @Test void unknownOriginalSprintSourceRevokesBoostInsideGetter(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.sprintOnlySetting().set(Boolean.TRUE);
        c.speed=2F;c.onRead=()->m.apply(c,new Minecraft189PlayerMovementState().snapshot(),false);
        m.apply(c,movement(true,false,true),false);
        assertEquals(1F,c.speed,1E-6F);assertEquals(1,c.writes);
    }
    @Test void lostGenuineSprintDuringSetterImmediatelyReturnsOneTimes(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.sprintOnlySetting().set(Boolean.TRUE);
        c.onWrite=()->m.apply(c,movement(true,false,false),false);
        m.apply(c,movement(true,false,true),false);
        assertEquals(1F,c.speed,1E-6F);assertEquals(2,c.writes);
    }
    @Test void realSneakDuringGetterRevokesOptInClockBoost(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        c.speed=2F;c.onRead=()->m.apply(c,movement(true,true,false),false);
        m.apply(c,movement(true,false,false),false);
        assertEquals(1F,c.speed,1E-6F);
    }
    @Test void realAirborneDuringSetterRevokesGroundOnlyBoost(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.groundOnlySetting().set(Boolean.TRUE);
        c.onWrite=()->m.apply(c,movement(false,false,false),false);
        m.apply(c,movement(true,false,false),false);
        assertEquals(1F,c.speed,1E-6F);assertEquals(2,c.writes);
    }
    @Test void speedPolicyEditDuringGetterVetoesOldMultiplier(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.onRead=()->m.speedPercentSetting().set(150);
        m.apply(c);assertEquals(1F,c.speed,1E-6F);assertEquals(0,c.writes);
        m.apply(c);assertEquals(1.5F,c.speed,1E-6F);
    }
    @Test void guiPolicyEditDuringGetterNeverUsesObsoleteTarget(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.onRead=()->m.pauseClickGuiSetting().set(Boolean.TRUE);
        m.apply(c);assertEquals(0,c.writes);
        m.apply(c);assertEquals(2F,c.speed,1E-6F);
    }
    @Test void originalGetterExceptionReleasesLeaseForRealNextCallback(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.throwRead=true;
        assertThrows(IllegalStateException.class,()->m.apply(c));
        c.throwRead=false;m.apply(c);assertEquals(2F,c.speed,1E-6F);
    }
    @Test void originalSetterExceptionLeavesRealPartialAcceleration(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        c.throwWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(c));
        assertEquals(2F,c.speed,1E-6F);assertEquals(1,c.writes);
        c.throwWrite=false;m.onDisable();m.apply(c);
        assertEquals(1F,c.speed,1E-6F);
    }
    @Test void failedSetterAfterGuiReentryDoesNotFabricateRollback(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.pauseClickGuiSetting().set(Boolean.TRUE);
        c.throwWrite=true;c.onWrite=()->m.apply(c,movement(true,false,false),true);
        assertThrows(IllegalStateException.class,
                ()->m.apply(c,movement(true,false,false),false));
        assertEquals(2F,c.speed,1E-6F);assertEquals(1,c.writes);
        c.throwWrite=false;m.apply(c,movement(true,false,false),true);
        assertEquals(1F,c.speed,1E-6F);
    }
    @Test void realSmoothingUsesNativeClockReadbackPerCallback(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.smoothTransitionSetting().set(Boolean.TRUE);
        m.transitionStepPercentSetting().set(25);
        m.apply(c);assertEquals(1.25F,c.speed,1E-6F);
        m.apply(c);assertEquals(1.5F,c.speed,1E-6F);
        m.apply(c);assertEquals(1.75F,c.speed,1E-6F);
        m.apply(c);assertEquals(2F,c.speed,1E-6F);
    }
    @Test void missingOriginalGroundSourceFailsClosedOnlyWhenOptedIn(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        final Minecraft189PlayerMovementState unknown=new Minecraft189PlayerMovementState();
        m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(c,unknown.snapshot(),false);assertEquals(1F,c.speed,1E-6F);
        m.groundOnlySetting().set(Boolean.FALSE);
        m.apply(c,unknown.snapshot(),false);assertEquals(2F,c.speed,1E-6F);
    }
    @Test void disabledGenuineTimerRepairsNonfiniteNativeClock(){
        final Minecraft189TimerSpeedModule m=module();final Clock c=new Clock();
        m.onDisable();c.speed=Float.NaN;m.apply(c);
        assertEquals(1F,c.speed,1E-6F);assertEquals(1,c.writes);
    }
    @Test void defaultOneTimesTimerWithoutModuleNeedsNoWrite(){
        final Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        final Clock c=new Clock();m.apply(c);
        assertEquals(1F,c.speed,1E-6F);assertEquals(0,c.writes);
    }
}
