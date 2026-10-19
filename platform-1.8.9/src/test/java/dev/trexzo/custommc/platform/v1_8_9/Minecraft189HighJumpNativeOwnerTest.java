package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Offline mapped-source callback tests, NOT genuine Minecraft binary. */
final class Minecraft189HighJumpNativeOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot ground() {
        final Minecraft189PlayerMovementState s = new Minecraft189PlayerMovementState();
        s.update(true, false, false);
        return s.snapshot();
    }
    private static final class Player implements Minecraft189PlayerJumpControl,
            Minecraft189PlayerMotionControl {
        double y;
        int jumps, reads, writes;
        Runnable onJump, onRead, onWrite;
        boolean throwJump, throwRead, throwWrite;
        @Override public void customMcJump() {
            jumps++;
            if (jumps > 6) throw new AssertionError("recursive native jump");
            y = 0.42D;
            if (onJump != null) {
                final Runnable callback = onJump;onJump = null;callback.run();
            }
            if (throwJump) throw new IllegalStateException("mapped jump");
        }
        @Override public double customMcMotionY() {
            reads++;
            if (reads > 6) throw new AssertionError("recursive native Y read");
            if (onRead != null) {
                final Runnable callback = onRead;onRead = null;callback.run();
            }
            if (throwRead) throw new IllegalStateException("mapped Y getter");
            return y;
        }
        @Override public void customMcSetMotionY(double value) {
            writes++;y=value;
            if (onWrite != null) {
                final Runnable callback=onWrite;onWrite=null;callback.run();
            }
            if (throwWrite) throw new IllegalStateException("mapped Y setter");
        }
        @Override public double customMcMotionX() {return 0D;}
        @Override public double customMcMotionZ() {return 0D;}
        @Override public void customMcSetMotionX(double x) {}
        @Override public void customMcSetMotionZ(double z) {}
    }
    private static void space(Minecraft189InputState input, boolean value) {
        input.key(LegacyKeyboardCodes.SPACE,value);
    }
    @Test void realSourceSpaceEdgeProducesOneJumpThenOneBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        assertEquals(1,p.jumps);assertTrue(m.applyMotion(p,false));
        assertEquals(1,p.reads);assertEquals(1,p.writes);
        assertEquals(0.7D,p.y,1E-12);
    }
    @Test void nestedOriginalJumpCannotUseSamePhysicalSpaceEdgeTwice() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        p.onJump=()->assertFalse(m.applyJump(p,ground(),false));
        assertTrue(m.applyJump(p,ground(),false));
        assertEquals(1,p.jumps);
        assertTrue(m.applyMotion(p,false));assertEquals(1,p.writes);
    }
    @Test void nestedOtherPlayerCannotBorrowJumpSpaceEdge() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player a = new Player(),b = new Player();m.onEnable();space(input,true);
        a.onJump=()->assertFalse(m.applyJump(b,ground(),false));
        assertTrue(m.applyJump(a,ground(),false));
        assertEquals(1,a.jumps);assertEquals(0,b.jumps);
    }
    @Test void offOnDuringNativeJumpRevokesOldBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        p.onJump=()->{m.onDisable();m.onEnable();
            assertFalse(m.applyJump(p,ground(),false));};
        assertFalse(m.applyJump(p,ground(),false));
        assertEquals(1,p.jumps);assertFalse(m.applyMotion(p,false));
    }
    @Test void releasedSpaceInsideNativeJumpKeepsAcceptedJumpAndFreshEdge() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        p.onJump=()->space(input,false);
        assertTrue(m.applyJump(p,ground(),false));
        assertEquals(1,p.jumps);assertTrue(m.applyMotion(p,false));
        assertEquals(1,p.writes);
        space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        assertEquals(2,p.jumps);
    }
    @Test void nativeJumpExceptionReleasesStackGuardWithoutFakeUndo() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        p.throwJump=true;
        assertThrows(IllegalStateException.class,
            ()->m.applyJump(p,ground(),false));
        assertEquals(1,p.jumps);assertEquals(.42D,p.y,1E-12);
        p.throwJump=false;space(input,false);
        assertFalse(m.applyJump(p,ground(),false));
        space(input,true);assertTrue(m.applyJump(p,ground(),false));
        assertEquals(2,p.jumps);
    }
    @Test void movingPolicyEditDuringJumpRevokesOldEdge() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        p.onJump=()->m.requireMovementSetting().set(Boolean.TRUE);
        assertFalse(m.applyJump(p,ground(),false));
        assertFalse(m.applyMotion(p,false));
    }
    @Test void nestedMappedBoostGetterCannotSpendPendingTwice() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onRead=()->assertFalse(m.applyMotion(p,false));
        assertTrue(m.applyMotion(p,false));
        assertEquals(1,p.reads);assertEquals(1,p.writes);
    }
    @Test void nestedMappedBoostSetterCannotWriteTwice() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onWrite=()->assertFalse(m.applyMotion(p,false));
        assertTrue(m.applyMotion(p,false));
        assertEquals(1,p.reads);assertEquals(1,p.writes);
    }
    @Test void anotherOriginalPlayerInMappedGetterRevokesOldBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player a = new Player(),b=new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(a,ground(),false));
        a.onRead=()->assertFalse(m.applyMotion(b,false));
        assertFalse(m.applyMotion(a,false));
        assertEquals(0,a.writes+b.reads+b.writes);
    }
    @Test void anotherOriginalPlayerInMappedSetterCannotBorrowBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player a = new Player(),b=new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(a,ground(),false));
        a.onWrite=()->assertFalse(m.applyMotion(b,false));
        assertFalse(m.applyMotion(a,false));
        assertEquals(1,a.writes);assertEquals(0,b.reads+b.writes);
    }
    @Test void nestedSuspensionRevokesOuterMappedBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onRead=()->assertFalse(m.applyMotion(p,true));
        assertFalse(m.applyMotion(p,false));
        assertEquals(0,p.writes);
    }
    @Test void nativeGetterModuleRestartRevokesPendingBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onRead=()->{m.onDisable();m.onEnable();};
        assertFalse(m.applyMotion(p,false));
        assertEquals(0,p.writes);
    }
    @Test void nativeGetterSpeedEditRevokesOldBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.onRead=()->m.verticalSpeedSetting().set(0.95D);
        assertFalse(m.applyMotion(p,false));
        assertEquals(0,p.writes);
    }
    @Test void nativeGetterExceptionReleasesBoostDispatchGuard() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));p.throwRead=true;
        assertThrows(IllegalStateException.class,()->m.applyMotion(p,false));
        p.throwRead=false;space(input,false);m.applyJump(p,ground(),false);
        space(input,true);assertTrue(m.applyJump(p,ground(),false));
        assertTrue(m.applyMotion(p,false));assertEquals(1,p.writes);
    }
    @Test void nativeSetterExceptionDoesNotPoisonNextRealJump() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));p.throwWrite=true;
        assertThrows(IllegalStateException.class,()->m.applyMotion(p,false));
        assertEquals(.7D,p.y,1E-12);
        p.throwWrite=false;space(input,false);m.applyJump(p,ground(),false);
        space(input,true);assertTrue(m.applyJump(p,ground(),false));
        assertTrue(m.applyMotion(p,false));assertEquals(2,p.writes);
    }
    @Test void nonfiniteOriginalYCannotAuthorizeNewBoost() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        p.y=Double.NEGATIVE_INFINITY;
        assertFalse(m.applyMotion(p,false));assertEquals(0,p.writes);
    }
    @Test void fasterOriginalUpwardMotionRetainsGenuineY() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));p.y=1.1D;
        assertTrue(m.applyMotion(p,false));assertEquals(0,p.writes);
        assertEquals(1.1D,p.y,0D);
    }
    @Test void heldSpaceCannotProduceSecondOriginalJump() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.onEnable();space(input,true);
        assertTrue(m.applyJump(p,ground(),false));
        assertFalse(m.applyJump(p,ground(),false));assertEquals(1,p.jumps);
        space(input,false);assertFalse(m.applyJump(p,ground(),false));
        space(input,true);assertTrue(m.applyJump(p,ground(),false));
        assertEquals(2,p.jumps);
    }
    @Test void originalMovementSettingNeedsRealWASD() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189HighJumpModule m = new Minecraft189HighJumpModule(input);
        final Player p = new Player();m.requireMovementSetting().set(Boolean.TRUE);
        m.onEnable();space(input,true);
        assertFalse(m.applyJump(p,ground(),false));assertEquals(0,p.jumps);
        space(input,false);m.applyJump(p,ground(),false);
        input.key(LegacyKeyboardCodes.W,true);space(input,true);
        assertTrue(m.applyJump(p,ground(),false));assertEquals(1,p.jumps);
    }
}
