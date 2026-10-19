package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** JUnit source-adapter tests, not proof from the Mojang binary. */
final class Minecraft189LowHopNativeOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot ground() {
        final Minecraft189PlayerMovementState s = new Minecraft189PlayerMovementState();
        s.update(true,false,false);return s.snapshot();
    }
    private static final class Player implements Minecraft189PlayerJumpControl,
            Minecraft189PlayerMotionControl {
        double y;
        int jumps,reads,writes;
        Runnable onJump,onRead,onWrite;
        boolean throwJump,throwRead,throwWrite;
        @Override public void customMcJump() {
            jumps++;if(jumps>6)throw new AssertionError("recursive native jump");
            y=0.42D;
            if(onJump!=null){final Runnable r=onJump;onJump=null;r.run();}
            if(throwJump)throw new IllegalStateException("native jump");
        }
        @Override public double customMcMotionY() {
            reads++;if(reads>6)throw new AssertionError("recursive native Y read");
            if(onRead!=null){final Runnable r=onRead;onRead=null;r.run();}
            if(throwRead)throw new IllegalStateException("native Y read");
            return y;
        }
        @Override public void customMcSetMotionY(double value) {
            writes++;y=value;
            if(onWrite!=null){final Runnable r=onWrite;onWrite=null;r.run();}
            if(throwWrite)throw new IllegalStateException("native Y setter");
        }
        @Override public double customMcMotionX(){return 0D;}
        @Override public double customMcMotionZ(){return 0D;}
        @Override public void customMcSetMotionX(double v){}
        @Override public void customMcSetMotionZ(double v){}
    }
    private static void space(Minecraft189InputState i, boolean b) {
        i.key(LegacyKeyboardCodes.SPACE,b);
    }
    @Test void genuineSourceSpaceProducesOneLowHopYAdjustment() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));
        assertTrue(m.applyMotion(p,false));
        assertEquals(1,p.jumps);assertEquals(1,p.writes);
        assertEquals(.25D,p.y,1E-12);
    }
    @Test void nativeJumpSamePlayerReentryCannotRepeatOneSpaceEdge() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.onJump=()->assertFalse(m.applyJump(p,ground(),false));
        assertTrue(m.applyJump(p,ground(),false));
        assertEquals(1,p.jumps);
    }
    @Test void nestedNativeJumpCannotBorrowAnotherPlayer() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player a=new Player(),b=new Player();m.onEnable();space(i,true);
        a.onJump=()->assertFalse(m.applyJump(b,ground(),false));
        assertTrue(m.applyJump(a,ground(),false));
        assertEquals(1,a.jumps);assertEquals(0,b.jumps);
    }
    @Test void offOnInsideMappedJumpCannotEnqueueOldAdjustment() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.onJump=()->{m.onDisable();m.onEnable();
            assertFalse(m.applyJump(p,ground(),false));};
        assertFalse(m.applyJump(p,ground(),false));
        assertEquals(1,p.jumps);assertFalse(m.applyMotion(p,false));
    }
    @Test void physicalSpaceReleaseInsideJumpPreservesAcceptedHop() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.onJump=()->space(i,false);
        assertTrue(m.applyJump(p,ground(),false));
        assertTrue(m.applyMotion(p,false));
        assertEquals(1,p.writes);
        space(i,true);assertTrue(m.applyJump(p,ground(),false));
        assertEquals(2,p.jumps);
    }
    @Test void nativeJumpThrowDoesNotPoisonSubsequentNewEdge() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);p.throwJump=true;
        assertThrows(IllegalStateException.class,()->m.applyJump(p,ground(),false));
        assertEquals(.42D,p.y,1E-12);
        p.throwJump=false;space(i,false);m.applyJump(p,ground(),false);
        space(i,true);assertTrue(m.applyJump(p,ground(),false));
        assertEquals(2,p.jumps);
    }
    @Test void jumpPolicyEditInsideOriginalCallbackRevokesAdjustment() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        p.onJump=()->m.requireMovementSetting().set(Boolean.TRUE);
        assertFalse(m.applyJump(p,ground(),false));
        assertFalse(m.applyMotion(p,false));
    }
    @Test void mappedYGetterReentryCannotLowerTwice() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onRead=()->assertFalse(m.applyMotion(p,false));
        assertTrue(m.applyMotion(p,false));
        assertEquals(1,p.reads);assertEquals(1,p.writes);
    }
    @Test void mappedYSetterReentryCannotLowerTwice() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onWrite=()->assertFalse(m.applyMotion(p,false));
        assertTrue(m.applyMotion(p,false));
        assertEquals(1,p.reads);assertEquals(1,p.writes);
    }
    @Test void anotherPlayerInNativeYReadRevokesPreviousSourceOwner() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player a=new Player(),b=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(a,ground(),false));
        a.onRead=()->assertFalse(m.applyMotion(b,false));
        assertFalse(m.applyMotion(a,false));
        assertEquals(0,a.writes+b.reads+b.writes);
    }
    @Test void anotherPlayerInNativeYSetterCannotBorrowAdjustment() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player a=new Player(),b=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(a,ground(),false));
        a.onWrite=()->assertFalse(m.applyMotion(b,false));
        assertFalse(m.applyMotion(a,false));
        assertEquals(1,a.writes);assertEquals(0,b.reads+b.writes);
    }
    @Test void suspensionDuringOriginalGetterRevokesLowering() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onRead=()->assertFalse(m.applyMotion(p,true));
        assertFalse(m.applyMotion(p,false));assertEquals(0,p.writes);
    }
    @Test void originalGetterOffOnRejectsPriorYLease() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onRead=()->{m.onDisable();m.onEnable();};
        assertFalse(m.applyMotion(p,false));assertEquals(0,p.writes);
    }
    @Test void originalGetterSpeedEditRejectsOutdatedLowHop() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onRead=()->m.verticalSpeedSetting().set(.30D);
        assertFalse(m.applyMotion(p,false));assertEquals(0,p.writes);
    }
    @Test void originalGetterExceptionReleasesMotionGuard() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));p.throwRead=true;
        assertThrows(IllegalStateException.class,()->m.applyMotion(p,false));
        p.throwRead=false;space(i,false);m.applyJump(p,ground(),false);
        space(i,true);assertTrue(m.applyJump(p,ground(),false));
        assertTrue(m.applyMotion(p,false));assertEquals(1,p.writes);
    }
    @Test void originalSetterExceptionNeverInventsYRollback() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));p.throwWrite=true;
        assertThrows(IllegalStateException.class,()->m.applyMotion(p,false));
        assertEquals(.25D,p.y,1E-12);p.throwWrite=false;
        space(i,false);m.applyJump(p,ground(),false);
        space(i,true);assertTrue(m.applyJump(p,ground(),false));
        assertTrue(m.applyMotion(p,false));assertEquals(2,p.writes);
    }
    @Test void nonfiniteOriginalYCannotAuthorizeFakeDownwardMotion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));p.y=Double.POSITIVE_INFINITY;
        assertFalse(m.applyMotion(p,false));assertEquals(0,p.writes);
    }
    @Test void alreadyLowerOriginalYRemainsAuthoritative() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));p.y=.10D;
        assertTrue(m.applyMotion(p,false));assertEquals(0,p.writes);
        assertEquals(.10D,p.y,1E-12);
    }
    @Test void heldSpaceCannotReplayActualJumpUntilReleased() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.onEnable();space(i,true);
        assertTrue(m.applyJump(p,ground(),false));
        assertFalse(m.applyJump(p,ground(),false));assertEquals(1,p.jumps);
        space(i,false);m.applyJump(p,ground(),false);
        space(i,true);assertTrue(m.applyJump(p,ground(),false));
        assertEquals(2,p.jumps);
    }
    @Test void requiresRealWASDWhenMovementPolicyOptedIn() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(i);
        final Player p=new Player();m.requireMovementSetting().set(Boolean.TRUE);
        m.onEnable();space(i,true);
        assertFalse(m.applyJump(p,ground(),false));
        space(i,false);m.applyJump(p,ground(),false);
        i.key(LegacyKeyboardCodes.A,true);space(i,true);
        assertTrue(m.applyJump(p,ground(),false));
        assertEquals(1,p.jumps);
    }
}
