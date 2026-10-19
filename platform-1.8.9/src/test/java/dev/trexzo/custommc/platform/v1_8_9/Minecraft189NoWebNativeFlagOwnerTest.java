package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Offline mapped source adapter tests; full original Mojang CI separate. */
final class Minecraft189NoWebNativeFlagOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot state(boolean ground,
            boolean sneaking) {
        final Minecraft189PlayerMovementState m = new Minecraft189PlayerMovementState();
        m.update(ground, sneaking, false);
        return m.snapshot();
    }
    private static final class Player implements Minecraft189PlayerWebControl {
        boolean inWeb = true;
        int reads,writes;
        Runnable onRead,onWrite;
        boolean failRead,failWrite;
        @Override public boolean customMcInWeb() {
            reads++;
            if(reads>8)throw new AssertionError("nested original web getter");
            final Runnable c=onRead;onRead=null;if(c!=null)c.run();
            if(failRead)throw new IllegalStateException("native web getter");
            return inWeb;
        }
        @Override public void customMcSetInWeb(boolean value) {
            writes++;
            if(writes>8)throw new AssertionError("nested original web setter");
            inWeb=value;
            final Runnable c=onWrite;onWrite=null;if(c!=null)c.run();
            if(failWrite)throw new IllegalStateException("native web setter");
        }
    }
    private static Minecraft189NoWebModule module() {
        final Minecraft189NoWebModule m=new Minecraft189NoWebModule();
        m.onEnable();return m;
    }
    @Test void genuineOriginalWebCollisionClearedOnce() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        m.apply(p);assertFalse(p.inWeb);assertEquals(1,p.reads);
        assertEquals(1,p.writes);m.apply(p);assertEquals(1,p.writes);
    }
    @Test void samePlayerNestedNativeGetterDoesNotDoubleClear() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        p.onRead=()->m.apply(p);
        m.apply(p);assertEquals(1,p.reads);assertEquals(1,p.writes);
        assertFalse(p.inWeb);
    }
    @Test void differentPlayerInsideGetterRevokesOldClear() {
        final Minecraft189NoWebModule m=module();final Player a=new Player(),b=new Player();
        a.onRead=()->m.apply(b);
        m.apply(a);assertTrue(a.inWeb);assertTrue(b.inWeb);
        assertEquals(0,a.writes+b.reads+b.writes);
    }
    @Test void originalGuiFocusDuringGetterRevokesClear() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        p.onRead=()->m.apply(p,state(true,false),true);
        m.apply(p);assertTrue(p.inWeb);assertEquals(0,p.writes);
    }
    @Test void nestedUnavailableMovementRevokesOptedInGroundClear() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        m.groundOnlySetting().set(Boolean.TRUE);
        p.onRead=()->m.apply(p,new Minecraft189PlayerMovementState().snapshot(),false);
        m.apply(p,state(true,false),false);
        assertTrue(p.inWeb);assertEquals(0,p.writes);
    }
    @Test void optedInGroundGateRevokedByNestedAirborneSnapshot() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        m.groundOnlySetting().set(Boolean.TRUE);
        p.onRead=()->m.apply(p,state(false,false),false);
        m.apply(p,state(true,false),false);
        assertTrue(p.inWeb);assertEquals(0,p.writes);
    }
    @Test void optedInSneakingGateRevokedByNestedUnsneak() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        m.requireSneakingSetting().set(Boolean.TRUE);
        p.onRead=()->m.apply(p,state(false,false),false);
        m.apply(p,state(false,true),false);
        assertTrue(p.inWeb);assertEquals(0,p.writes);
    }
    @Test void optedInAirOnlyRevokedByNestedGroundContact() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        m.airborneOnlySetting().set(Boolean.TRUE);
        p.onRead=()->m.apply(p,state(true,false),false);
        m.apply(p,state(false,false),false);
        assertTrue(p.inWeb);assertEquals(0,p.writes);
    }
    @Test void samePlayerNativeSetterReentryNeverWritesTwice() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        p.onWrite=()->m.apply(p);
        m.apply(p);assertEquals(1,p.reads);assertEquals(1,p.writes);
        assertFalse(p.inWeb);
    }
    @Test void differentPlayerDuringNativeSetterCannotBorrowItsWebFlag() {
        final Minecraft189NoWebModule m=module();final Player a=new Player(),b=new Player();
        a.onWrite=()->m.apply(b);
        m.apply(a);assertFalse(a.inWeb);assertTrue(b.inWeb);
        assertEquals(1,a.writes);assertEquals(0,b.reads+b.writes);
    }
    @Test void nativeGetterOffOnNeverClearsOldEpochFlag() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        p.onRead=()->{m.onDisable();m.onEnable();m.apply(p);};
        m.apply(p);assertTrue(p.inWeb);assertEquals(0,p.writes);
        m.apply(p);assertFalse(p.inWeb);assertEquals(1,p.writes);
    }
    @Test void nativeGetterGuiPolicyEditRevokesOldWebClear() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        p.onRead=()->m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(p);assertTrue(p.inWeb);assertEquals(0,p.writes);
    }
    @Test void nativeGetterThrowReleasesOwnershipForNextSourceTick() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        p.failRead=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        p.failRead=false;m.apply(p);
        assertFalse(p.inWeb);assertEquals(1,p.writes);
    }
    @Test void nativeSetterThrowCannotRollbackRealCollisionFlag() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        p.failWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        assertFalse(p.inWeb);assertEquals(1,p.writes);
        p.failWrite=false;p.inWeb=true;m.apply(p);
        assertFalse(p.inWeb);assertEquals(2,p.writes);
    }
    @Test void originalWebFlagFalseRequiresNoVanillaSetter() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        p.inWeb=false;m.apply(p);
        assertEquals(1,p.reads);assertEquals(0,p.writes);
    }
    @Test void actualGuiFocusNeverReadsOrClearsGenuineWebCollision() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        m.apply(p,state(true,false),true);
        assertTrue(p.inWeb);assertEquals(0,p.reads+p.writes);
    }
    @Test void contradictoryAirAndGroundSettingsCannotOverrideNativeFlag() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        m.groundOnlySetting().set(Boolean.TRUE);
        m.airborneOnlySetting().set(Boolean.TRUE);
        m.apply(p,state(true,false),false);
        assertTrue(p.inWeb);assertEquals(0,p.reads+p.writes);
    }
    @Test void disabledModuleNeverTouchesMappedOriginalFlag() {
        final Minecraft189NoWebModule m=module();final Player p=new Player();
        m.onDisable();m.apply(p);
        assertEquals(0,p.reads+p.writes);assertTrue(p.inWeb);
    }
}
