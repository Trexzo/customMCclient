package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Native-shaped original-player adapter; full Mojang acceptance is separate. */
final class Minecraft189NoClipNativeBaselineOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot movement(boolean sneak) {
        final Minecraft189PlayerMovementState s=new Minecraft189PlayerMovementState();
        s.update(false,sneak,false);return s.snapshot();
    }
    private static final class Player implements Minecraft189PlayerNoClipControl {
        boolean clip;
        int reads,writes;
        Runnable onRead,onWrite;
        boolean throwRead,throwWrite;
        @Override public boolean customMcNoClip() {
            reads++;
            if(reads>16)throw new AssertionError("recursive original native noClip getter");
            final Runnable c=onRead;onRead=null;if(c!=null)c.run();
            if(throwRead)throw new IllegalStateException("original native getter");
            return clip;
        }
        @Override public void customMcSetNoClip(boolean value) {
            writes++;
            if(writes>16)throw new AssertionError("recursive original native noClip setter");
            clip=value;
            final Runnable c=onWrite;onWrite=null;if(c!=null)c.run();
            if(throwWrite)throw new IllegalStateException("original native setter");
        }
    }
    private static Minecraft189NoClipModule module() {
        final Minecraft189NoClipModule m=new Minecraft189NoClipModule();
        m.onEnable();return m;
    }
    @Test void originalFalseBaselineForcedThenActuallyRestoredOnDisable() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        m.apply(p);assertTrue(p.clip);assertEquals(1,p.writes);
        m.onDisable();assertTrue(m.restorePending());
        m.apply(p);assertFalse(p.clip);assertEquals(2,p.writes);
        assertFalse(m.restorePending());
    }
    @Test void originallyTrueNativeBaselineNeverForcedOrOverwritten() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.clip=true;m.apply(p);assertTrue(p.clip);assertEquals(0,p.writes);
        m.onDisable();m.apply(p);assertTrue(p.clip);assertEquals(0,p.writes);
    }
    @Test void reentrantSamePlayerDuringFirstGetterCannotRecaptureBaseline() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onRead=()->m.apply(p);
        m.apply(p);assertEquals(2,p.reads);assertEquals(1,p.writes);
        m.onDisable();m.apply(p);assertFalse(p.clip);
    }
    @Test void reentrantDifferentPlayerDuringFirstGetterCannotStealLease() {
        final Minecraft189NoClipModule m=module();
        final Player a=new Player(),b=new Player();
        a.onRead=()->m.apply(b);
        m.apply(a);assertFalse(a.clip);assertFalse(b.clip);
        assertEquals(1,a.reads);assertEquals(0,a.writes+b.reads+b.writes);
        m.apply(a);assertTrue(a.clip);
        m.onDisable();m.apply(a);assertFalse(a.clip);
    }
    @Test void reentrantNullPlayerDuringCaptureCannotSetOldNativeFlag() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onRead=()->m.apply((Minecraft189PlayerNoClipControl)null);
        m.apply(p);assertFalse(p.clip);assertEquals(0,p.writes);
        m.apply(p);assertTrue(p.clip);
    }
    @Test void samePlayerReentrantForceGetterDoesNotDuplicateNativeWrite() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        // The original first capture getter reenters; it does not borrow
        // an extra getter/write before the outer source owner advances.
        p.onRead=()->m.apply(p);
        m.apply(p);assertTrue(p.clip);assertEquals(1,p.writes);
    }
    @Test void originalSetterSamePlayerReentryCannotForceTwice() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onWrite=()->m.apply(p);
        m.apply(p);assertTrue(p.clip);assertEquals(1,p.writes);
    }
    @Test void anotherOriginalPlayerInsideForceSetterNeverGetsOldCollision() {
        final Minecraft189NoClipModule m=module();
        final Player a=new Player(),b=new Player();
        a.onWrite=()->m.apply(b);
        m.apply(a);assertTrue(a.clip);assertFalse(b.clip);
        assertEquals(1,a.writes);assertEquals(0,b.reads+b.writes);
        // No further borrowed source reads/writes happen this tick.
    }
    @Test void guiFocusDuringFirstBaselineGetterCannotForceTrue() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onRead=()->m.apply(p,movement(false),true);
        m.apply(p);assertFalse(p.clip);assertEquals(0,p.writes);
    }
    @Test void guiFocusDuringForceGetterLeavesOriginalFalse() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        // Two mapped original getters: first captures false, second
        // observes real native state for forcing true.
        p.onRead=()->{p.onRead=()->m.apply(p,movement(false),true);};
        m.apply(p);assertFalse(p.clip);assertEquals(0,p.writes);
    }
    @Test void guiFocusDuringForceSetterRestoresNativeFalseImmediately() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onWrite=()->m.apply(p,movement(false),true);
        m.apply(p);assertFalse(p.clip);assertEquals(2,p.writes);
        assertEquals(3,p.reads);
    }
    @Test void requireSneakLossDuringForceSetterRestoresNativeFalseImmediately() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        m.requireSneakingSetting().set(Boolean.TRUE);
        p.onWrite=()->m.apply(p,movement(false),false);
        m.apply(p,movement(true),false);
        assertFalse(p.clip);assertEquals(2,p.writes);
    }
    @Test void originalDisableDuringForceSetterRestoresOnNextNativeTick() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onWrite=m::onDisable;
        m.apply(p);assertTrue(p.clip);assertEquals(1,p.writes);
        assertTrue(m.restorePending());assertFalse(m.active());
        // Retain established genuine host contract: a pure OFF inside
        // native setter never fabricates an in-stack rollback.
        m.apply(p);assertFalse(p.clip);assertEquals(2,p.writes);
        assertFalse(m.restorePending());
    }
    @Test void originalOffOnInsideFirstGetterCannotBorrowNewActivation() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onRead=()->{m.onDisable();m.onEnable();m.apply(p);};
        m.apply(p);assertFalse(p.clip);assertEquals(0,p.writes);
        m.apply(p);assertTrue(p.clip);
        m.onDisable();m.apply(p);assertFalse(p.clip);
    }
    @Test void offOnInsideOriginalSetterKeepsFreshSourceBaseline() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onWrite=()->{m.onDisable();m.onEnable();m.apply(p);};
        m.apply(p);assertTrue(p.clip);assertEquals(1,p.writes);
        m.onDisable();m.apply(p);
        assertFalse(p.clip);assertEquals(2,p.writes);
    }
    @Test void originalGetterExceptionReleasesLeaseForNewTick() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.throwRead=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        p.throwRead=false;m.apply(p);assertTrue(p.clip);
        m.onDisable();m.apply(p);assertFalse(p.clip);
    }
    @Test void setterExceptionDoesNotInventVanillaRollback() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.throwWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        assertTrue(p.clip);assertEquals(1,p.writes);
        p.throwWrite=false;m.onDisable();m.apply(p);
        assertFalse(p.clip);assertEquals(2,p.writes);
    }
    @Test void setterExceptionWithGuiReentryIsNotPretendedSuccess() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.onWrite=()->m.apply(p,movement(false),true);
        p.throwWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        assertTrue(p.clip);assertEquals(1,p.writes);
        p.throwWrite=false;m.apply(p,movement(false),true);
        assertFalse(p.clip);assertEquals(2,p.writes);
    }
    @Test void differentPlayerInsideRestoreGetterRetainsPendingLease() {
        final Minecraft189NoClipModule m=module();final Player a=new Player(),b=new Player();
        m.apply(a);assertTrue(a.clip);m.onDisable();
        a.onRead=()->m.apply(b);
        m.apply(a);assertTrue(a.clip);assertTrue(m.restorePending());
        assertEquals(0,b.reads+b.writes);
        m.apply(a);assertFalse(a.clip);assertFalse(m.restorePending());
    }
    @Test void originalRespawnSourceNeverReceivesPriorPlayersFalse() {
        final Minecraft189NoClipModule m=module();
        final Player a=new Player(),b=new Player();b.clip=true;
        m.apply(a);m.onDisable();m.apply(b);
        assertEquals(0,b.writes);assertTrue(b.clip);
        assertFalse(m.restorePending());
    }
    @Test void originalGuiFocusReleasesOnlyModulesFalseBaseline() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        m.apply(p);assertTrue(p.clip);m.apply(p,movement(false),true);
        assertFalse(p.clip);assertEquals(2,p.writes);
        m.apply(p);assertTrue(p.clip);
    }
    @Test void originalTrueBaselineSurvivesRealGuiOpening() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        p.clip=true;m.apply(p);m.apply(p,movement(false),true);
        assertTrue(p.clip);assertEquals(0,p.writes);
    }
    @Test void configuredSneakRequiresKnownOriginalPlayerState() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        m.requireSneakingSetting().set(Boolean.TRUE);
        m.apply(p);assertFalse(p.clip);assertEquals(0,p.reads);
        m.apply(p,movement(false),false);assertFalse(p.clip);
        m.apply(p,movement(true),false);assertTrue(p.clip);
        m.apply(p,movement(false),false);assertFalse(p.clip);
    }
    @Test void disableThenEnableBeforeCallbackRetainsFirstFalseBaseline() {
        final Minecraft189NoClipModule m=module();final Player p=new Player();
        m.apply(p);m.onDisable();m.onEnable();m.apply(p);
        assertTrue(p.clip);assertEquals(1,p.writes);
        m.onDisable();m.apply(p);
        assertFalse(p.clip);assertEquals(2,p.writes);
    }
}
