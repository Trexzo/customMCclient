package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Native-shaped source adapter; authentic Mojang validation is separate. */
final class Minecraft189AirJumpNativeReentryTest {
    private static Minecraft189PlayerMovementState.Snapshot movement(boolean grounded) {
        final Minecraft189PlayerMovementState state = new Minecraft189PlayerMovementState();
        state.update(grounded, false, false);
        return state.snapshot();
    }
    private static void space(Minecraft189InputState input, boolean down) {
        input.key(LegacyKeyboardCodes.SPACE, down);
    }
    private static final class Player implements Minecraft189PlayerJumpControl {
        int jumps;
        Runnable callback;
        boolean throwAfterNativeJump;
        @Override public void customMcJump() {
            jumps++;
            if (jumps > 8) throw new AssertionError("recursive native jump");
            if (callback != null) {
                final Runnable event = callback;
                callback = null;
                event.run();
            }
            if (throwAfterNativeJump) throw new IllegalStateException("native jump");
        }
    }
    @Test void originalAirborneSpaceEdgeStillJumpsOnce() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        m.apply(p,movement(false));assertEquals(1,p.jumps);
        m.apply(p,movement(false));assertEquals(1,p.jumps);
    }
    @Test void reentrantNativeJumpSamePlayerCannotDoubleJump() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.callback=()->m.apply(p,movement(false));
        m.apply(p,movement(false));assertEquals(1,p.jumps);
    }
    @Test void reentrantNativeJumpOtherPlayerCannotBorrowSpace() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player a=new Player(),b=new Player();m.onEnable();space(i,true);
        a.callback=()->m.apply(b,movement(false));
        m.apply(a,movement(false));assertEquals(1,a.jumps);
        assertEquals(0,b.jumps);
    }
    @Test void originalOffOnInsideMappedJumpCannotRecursivelyJump() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.callback=()->{m.onDisable();m.onEnable();m.apply(p,movement(false));};
        m.apply(p,movement(false));
        assertEquals(1,p.jumps);
        m.apply(p,movement(false));assertEquals(1,p.jumps);
    }
    @Test void newOwnerSamplesReleasedSpaceDuringRestartWithoutOldClobber() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.callback=()->{m.onDisable();space(i,false);m.onEnable();space(i,true);};
        m.apply(p,movement(false));m.apply(p,movement(false));
        assertEquals(2,p.jumps);
    }
    @Test void callbackSpaceReleaseStillEnablesGenuineNextEdge() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.callback=()->space(i,false);
        m.apply(p,movement(false));space(i,true);m.apply(p,movement(false));
        assertEquals(2,p.jumps);
    }
    @Test void nativeJumpThrowDoesNotPoisonSubsequentPhysicalPress() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.throwAfterNativeJump=true;
        assertThrows(IllegalStateException.class,()->m.apply(p,movement(false)));
        assertEquals(1,p.jumps);
        p.throwAfterNativeJump=false;space(i,false);m.apply(p,movement(false));
        space(i,true);m.apply(p,movement(false));assertEquals(2,p.jumps);
    }
    @Test void nativeThrowAfterOffOnDoesNotClobberFreshSourceOwner() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.callback=()->{m.onDisable();space(i,false);m.onEnable();};
        p.throwAfterNativeJump=true;
        assertThrows(IllegalStateException.class,()->m.apply(p,movement(false)));
        p.throwAfterNativeJump=false;space(i,true);
        m.apply(p,movement(false));assertEquals(2,p.jumps);
    }
    @Test void nestedNativeGuiCallbackCannotReplayOriginalSpace() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.callback=()->m.apply(p,movement(false),true);
        m.apply(p,movement(false));m.apply(p,movement(false));
        assertEquals(1,p.jumps);
    }
    @Test void physicalGuiFocusOwnsHeldSpaceUntilNewPress() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        m.apply(p,movement(false),true);
        m.apply(p,movement(false),false);
        assertEquals(0,p.jumps);
        space(i,false);m.apply(p,movement(false));
        space(i,true);m.apply(p,movement(false));assertEquals(1,p.jumps);
    }
    @Test void physicalGroundContactCannotInvokeAirJump() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        m.apply(p,movement(true));assertEquals(0,p.jumps);
        m.apply(p,movement(false));assertEquals(0,p.jumps);
    }
    @Test void originalMovementUnavailableNeverJumps() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        m.apply(p,new Minecraft189PlayerMovementState().snapshot());
        assertEquals(0,p.jumps);
    }
    @Test void disabledModuleNeverDispatchesOriginalJump() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();m.onDisable();space(i,true);
        m.apply(p,movement(false));assertEquals(0,p.jumps);
        assertFalse(m.active());
    }
    @Test void originalRepeatedSpaceHoldCannotJumpMultipleTimes() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirJumpModule m=new Minecraft189AirJumpModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        for(int j=0;j<4;j++)m.apply(p,movement(false));
        assertEquals(1,p.jumps);
        space(i,false);m.apply(p,movement(false));
        space(i,true);m.apply(p,movement(false));
        assertEquals(2,p.jumps);
    }
}
