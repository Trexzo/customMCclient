package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-shaped native callback tests; not Mojang binary evidence. */
final class Minecraft189AutoJumpNativeStackTest {
    private static Minecraft189PlayerMovementState.Snapshot state(
            boolean ground, boolean sneak, boolean sprint) {
        final Minecraft189PlayerMovementState s=new Minecraft189PlayerMovementState();
        s.update(ground,sneak,sprint);
        return s.snapshot();
    }
    private static final class Player implements Minecraft189PlayerJumpControl {
        int jumps;
        Runnable callback;
        boolean throwJump;
        @Override public void customMcJump() {
            jumps++;
            if(jumps>8)throw new AssertionError("recursive native jump");
            if(callback!=null){
                final Runnable r=callback;callback=null;r.run();
            }
            if(throwJump)throw new IllegalStateException("native jump after mutation");
        }
    }
    private static void land(Minecraft189AutoJumpModule m,Player p) {
        m.apply(p,state(false,false,false),true,true,false);
        m.apply(p,state(true,false,false),true,true,false);
    }
    @Test void originalAirToGroundLandingDispatchesOneRealJump() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.onEnable();land(m,p);
        assertEquals(1,p.jumps);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(1,p.jumps);
    }
    @Test void recursiveSameOriginalPlayerCannotSpendOldContactTwice() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.onEnable();
        p.callback=()->m.apply(p,state(true,false,false),true,true,false);
        land(m,p);assertEquals(1,p.jumps);
    }
    @Test void reentrantDifferentPlayerCannotBorrowNativeLanding() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player a=new Player(),b=new Player();m.onEnable();
        a.callback=()->m.apply(b,state(true,false,false),true,true,false);
        land(m,a);assertEquals(1,a.jumps);assertEquals(0,b.jumps);
    }
    @Test void restartInsideMappedJumpCannotDispatchNestedSecondJump() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.onEnable();
        p.callback=()->{m.onDisable();m.onEnable();
            m.apply(p,state(true,false,false),true,true,false);};
        land(m,p);assertEquals(1,p.jumps);
        // The new lifetime has no cached old air->ground observation.
        m.apply(p,state(false,false,false),true,true,false);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(2,p.jumps);
    }
    @Test void nativeExceptionCannotReplayConsumedGroundContact() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.onEnable();p.throwJump=true;
        assertThrows(IllegalStateException.class,()->land(m,p));
        assertEquals(1,p.jumps);
        p.throwJump=false;
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(1,p.jumps);
        land(m,p);assertEquals(2,p.jumps);
    }
    @Test void nativeExceptionAfterOffOnReleasesGuardForFreshLanding() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.onEnable();p.throwJump=true;
        p.callback=()->{m.onDisable();m.onEnable();};
        assertThrows(IllegalStateException.class,()->land(m,p));
        p.throwJump=false;
        land(m,p);assertEquals(2,p.jumps);
    }
    @Test void nestedOriginalGuiCallbackCannotReplayAcceptedJump() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.onEnable();
        p.callback=()->m.apply(p,state(true,false,false),true,true,true);
        land(m,p);assertEquals(1,p.jumps);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(1,p.jumps);
    }
    @Test void originalGuiFocusInvalidatesPendingLandingCredit() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.landingDelayTicksSetting().set(1);
        m.onEnable();m.apply(p,state(false,false,false),true,true,false);
        m.apply(p,state(true,false,false),true,true,false);
        m.apply(p,state(true,false,false),true,true,true);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(0,p.jumps);
        land(m,p);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(1,p.jumps);
    }
    @Test void nativeLandingDelaySpendsOneActualGroundCallback() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.landingDelayTicksSetting().set(2);
        m.onEnable();
        m.apply(p,state(false,false,false),true,true,false);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(0,p.jumps);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(0,p.jumps);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(1,p.jumps);
    }
    @Test void genuineForwardRequiredVetoesStationaryJump() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.requireForwardSetting().set(Boolean.TRUE);
        m.onEnable();
        m.apply(p,state(false,false,false),false,false,false);
        m.apply(p,state(true,false,false),false,false,false);
        assertEquals(0,p.jumps);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(1,p.jumps);
    }
    @Test void genuineSprintingRequiredVetoesWalkLanding() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.requireSprintingSetting().set(Boolean.TRUE);
        m.onEnable();
        m.apply(p,state(false,false,false),true,true,false);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(0,p.jumps);
        m.apply(p,state(true,false,true),true,true,false);
        assertEquals(1,p.jumps);
    }
    @Test void sneakPauseCancelsOldOriginalGroundContact() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        m.onEnable();
        m.apply(p,state(false,false,false),true,true,false);
        m.apply(p,state(true,true,false),true,true,false);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(0,p.jumps);
        land(m,p);assertEquals(1,p.jumps);
    }
    @Test void sourceUnavailableCannotSpendOldNativeLanding() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.onEnable();
        m.apply(p,state(false,false,false),true,true,false);
        m.apply(p,new Minecraft189PlayerMovementState().snapshot(),true,true,false);
        m.apply(p,state(true,false,false),true,true,false);
        assertEquals(0,p.jumps);
    }
    @Test void disabledModuleNeverDispatchesOriginalPlayerJump() {
        final Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        final Player p=new Player();m.onEnable();m.onDisable();
        land(m,p);assertEquals(0,p.jumps);assertFalse(m.active());
    }
}
