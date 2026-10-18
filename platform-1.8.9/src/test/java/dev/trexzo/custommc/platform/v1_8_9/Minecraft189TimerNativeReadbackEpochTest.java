package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Adversarial mapped 1.8.9 Timer interface, not Mojang binary runtime. */
final class Minecraft189TimerNativeReadbackEpochTest {
    private static final class NativeTimer implements Minecraft189TimerSpeedControl {
        float speed=1F;
        int reads, writes;
        Runnable onRead;
        @Override public float customMcTimerSpeed() {
            reads++;float old=speed;
            if(onRead!=null){Runnable a=onRead;onRead=null;a.run();}
            return old;
        }
        @Override public void customMcSetTimerSpeed(float v) {
            speed=v;writes++;
        }
    }
    private static Minecraft189PlayerMovementState.Snapshot source(
            boolean grounded, boolean sneak, boolean sprint) {
        Minecraft189PlayerMovementState m=new Minecraft189PlayerMovementState();
        m.update(grounded,sneak,sprint);
        return m.snapshot();
    }
    @Test void getterRestartsTimerAndOldCallbackDoesNotWriteSpeed() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();m.onEnable();
        m.speedPercentSetting().set(200);
        t.onRead=()->{m.onDisable();m.onEnable();};
        m.apply(t,source(true,false,false),false);
        assertEquals(0,t.writes);assertEquals(1F,t.speed,1E-6F);
        m.apply(t,source(true,false,false),false);
        assertEquals(2F,t.speed,1E-6F);
        m.onDisable();
    }
    @Test void nativeReadDisableImmediatelyRestoresPersistentClockToVanilla() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();m.onEnable();
        m.speedPercentSetting().set(250);m.apply(t);
        assertEquals(2.5F,t.speed,1E-6F);
        t.onRead=m::onDisable;
        m.apply(t);
        assertEquals(1F,t.speed,1E-6F);
        assertFalse(m.active());
    }
    @Test void nativeReadDisableRestoresNonfiniteWorldClock() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();t.speed=Float.NaN;
        m.onEnable();t.onRead=m::onDisable;
        m.apply(t);
        assertEquals(1F,t.speed,1E-6F);
        assertEquals(1,t.writes);
    }
    @Test void speedSettingEditedInsideGetterCannotCommitStaleClock() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();m.onEnable();
        m.speedPercentSetting().set(180);
        t.onRead=()->m.speedPercentSetting().set(160);
        m.apply(t);assertEquals(0,t.writes);
        m.apply(t);assertEquals(1.6F,t.speed,1E-6F);
        m.onDisable();
    }
    @Test void originalGetterGuiPausePolicyEditRevokesOldBoost() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();m.onEnable();
        m.speedPercentSetting().set(180);
        t.onRead=()->m.pauseClickGuiSetting().set(Boolean.TRUE);
        m.apply(t,source(true,false,false),true);
        assertEquals(0,t.writes);
        m.apply(t,source(true,false,false),true);
        assertEquals(1F,t.speed,1E-6F);
        m.apply(t,source(true,false,false),false);
        assertEquals(1.8F,t.speed,1E-6F);
        m.onDisable();
    }
    @Test void originalGetterSmoothingPolicyEditRevokesRamp() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();m.onEnable();
        m.speedPercentSetting().set(200);
        m.smoothTransitionSetting().set(Boolean.TRUE);
        t.onRead=()->m.transitionStepPercentSetting().set(10);
        m.apply(t);assertEquals(0,t.writes);
        m.apply(t);assertEquals(1.1F,t.speed,1E-6F);
        m.onDisable();
    }
    @Test void originalGetterAirborneOverrideEditStopsOldBoost() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();m.onEnable();
        m.speedPercentSetting().set(180);
        m.airborneSpeedPercentSetting().set(60);
        t.onRead=()->m.airborneOverrideSetting().set(Boolean.TRUE);
        m.apply(t,source(false,false,false),false);
        assertEquals(0,t.writes);
        m.apply(t,source(false,false,false),false);
        assertEquals(.6F,t.speed,1E-6F);
        m.onDisable();
    }
    @Test void groundGateChangeDuringMappedGetterRevokesAndFreshVanilla() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();m.onEnable();
        m.speedPercentSetting().set(200);
        t.onRead=()->m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(t,source(false,false,false),false);
        assertEquals(0,t.writes);
        m.apply(t,source(false,false,false),false);
        assertEquals(1F,t.speed,1E-6F);
        m.apply(t,source(true,false,false),false);
        assertEquals(2F,t.speed,1E-6F);
        m.onDisable();
    }
    @Test void defaultUnchangedTimerWritesOnceAndNeverRepeats() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();m.onEnable();
        m.speedPercentSetting().set(150);
        m.apply(t);m.apply(t);
        assertEquals(1.5F,t.speed,1E-6F);
        assertEquals(1,t.writes);
        m.onDisable();m.apply(t);
        assertEquals(1F,t.speed,1E-6F);
        assertEquals(2,t.writes);
    }
    @Test void disabledOriginalGetterMustRestoreOnlyVanillaClock() {
        Minecraft189TimerSpeedModule m=new Minecraft189TimerSpeedModule();
        NativeTimer t=new NativeTimer();t.speed=2F;
        m.apply(t);
        assertEquals(1F,t.speed,1E-6F);
        assertEquals(1,t.writes);
        m.apply(t);
        assertEquals(1,t.writes);
    }
}
