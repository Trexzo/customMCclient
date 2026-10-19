package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Offline native Y-source adapter tests, not Minecraft original CI. */
final class Minecraft189GlideNativeReentryTest {
    private static Minecraft189PlayerMovementState.Snapshot state(
            final boolean ground, final boolean sneak) {
        final Minecraft189PlayerMovementState s =
                new Minecraft189PlayerMovementState();
        s.update(ground, sneak, false);
        return s.snapshot();
    }
    private static final class Player implements Minecraft189PlayerMotionControl {
        double y = -0.40D;
        int reads, writes;
        Runnable onRead, onWrite;
        boolean throwRead, throwWrite;
        @Override public double customMcMotionX() {return 0D;}
        @Override public double customMcMotionZ() {return 0D;}
        @Override public void customMcSetMotionX(double v) {}
        @Override public void customMcSetMotionZ(double v) {}
        @Override public double customMcMotionY() {
            reads++;
            if (reads > 10) throw new AssertionError("recursive native read");
            if (onRead != null) {
                final Runnable cb = onRead; onRead = null; cb.run();
            }
            if (throwRead) throw new IllegalStateException("mapped getter");
            return y;
        }
        @Override public void customMcSetMotionY(double v) {
            writes++;
            if (writes > 10) throw new AssertionError("recursive native write");
            y = v;
            if (onWrite != null) {
                final Runnable cb = onWrite; onWrite = null; cb.run();
            }
            if (throwWrite) throw new IllegalStateException("mapped setter");
        }
    }
    @Test void originalValidDescentDeceleratesOnce() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.onEnable();
        m.apply(p, state(false,false), false);
        assertEquals(1,p.reads);assertEquals(1,p.writes);
        assertEquals(-0.08D,p.y,1E-12);
    }
    @Test void nestedOriginalGetterCannotDoubleWrite() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.onEnable();
        p.onRead=()->m.apply(p,state(false,false),false);
        m.apply(p,state(false,false),false);
        assertEquals(1,p.reads);assertEquals(1,p.writes);
    }
    @Test void nestedOriginalSetterCannotDoubleWrite() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.onEnable();
        p.onWrite=()->m.apply(p,state(false,false),false);
        m.apply(p,state(false,false),false);
        assertEquals(1,p.reads);assertEquals(1,p.writes);
    }
    @Test void nativeGetterNestedCallbackCannotSpendExtraDelay() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.activationDelayTicksSetting().set(2);
        m.onEnable();
        p.onRead=()->m.apply(p,state(false,false),false);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(1,p.writes);
    }
    @Test void nativeSetterNestedCallbackCannotSpendNextDelay() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.activationDelayTicksSetting().set(1);
        m.onEnable();
        m.apply(p,state(false,false),false);
        p.onWrite=()->m.apply(p,state(false,false),false);
        m.apply(p,state(false,false),false);
        assertEquals(1,p.writes);
        p.y=-0.4D;
        m.apply(p,state(false,false),false);
        assertEquals(2,p.writes);
    }
    @Test void changedNativePlayerInGetterRevokesOuterWrite() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player a = new Player(), b = new Player(); m.onEnable();
        a.onRead=()->m.apply(b,state(false,false),false);
        m.apply(a,state(false,false),false);
        assertEquals(0,a.writes+b.reads+b.writes);
        m.apply(b,state(false,false),false);
        assertEquals(1,b.writes);
    }
    @Test void nestedNativeSetterNeverWritesAnotherPlayer() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player a = new Player(), b = new Player(); m.onEnable();
        a.onWrite=()->m.apply(b,state(false,false),false);
        m.apply(a,state(false,false),false);
        assertEquals(1,a.writes);assertEquals(0,b.reads+b.writes);
    }
    @Test void nestedGuiOpeningDiscardsPendingGlideDelay() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.activationDelayTicksSetting().set(1);
        m.onEnable();m.apply(p,state(false,false),false);
        p.onRead=()->m.apply(p,state(false,false),false,true);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(1,p.writes);
    }
    @Test void nestedOriginalLandingRevokesPriorDescent() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.onEnable();
        p.onRead=()->m.apply(p,state(true,false),false);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
    }
    @Test void nestedSuspensionRevokesOriginalYWrite() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.onEnable();
        p.onRead=()->m.apply(p,state(false,false),true);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
    }
    @Test void nestedUnavailableMovementRevokesSource() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.onEnable();
        final Minecraft189PlayerMovementState unknown =
                new Minecraft189PlayerMovementState();
        p.onRead=()->m.apply(p,unknown.snapshot(),false);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
    }
    @Test void sneakGateLostInsideGetterRevokesOuterWrite() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.requireSneakingSetting().set(Boolean.TRUE);
        m.onEnable();
        p.onRead=()->m.apply(p,state(false,false),false);
        m.apply(p,state(false,true),false);
        assertEquals(0,p.writes);
    }
    @Test void offOnInGetterCannotSpendOldLifetimeDelay() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.activationDelayTicksSetting().set(1);
        m.onEnable();m.apply(p,state(false,false),false);
        p.onRead=()->{m.onDisable();m.onEnable();};
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(1,p.writes);
    }
    @Test void originalGuiSpeedEditCannotSpendOldDescent() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player(); m.activationDelayTicksSetting().set(1);
        m.onEnable();m.apply(p,state(false,false),false);
        p.onRead=()->m.fallSpeedSetting().set(0.2D);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(-0.2D,p.y,1E-12);
    }
    @Test void originalGetterThrowReleasesGuardForLaterTick() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player();m.onEnable();p.throwRead=true;
        assertThrows(IllegalStateException.class,
            ()->m.apply(p,state(false,false),false));
        p.throwRead=false;m.apply(p,state(false,false),false);
        assertEquals(1,p.writes);
    }
    @Test void originalSetterThrowDoesNotPoisonNextNativeTick() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player();m.onEnable();p.throwWrite=true;
        assertThrows(IllegalStateException.class,
            ()->m.apply(p,state(false,false),false));
        assertEquals(1,p.writes);
        p.throwWrite=false;p.y=-0.4D;
        m.apply(p,state(false,false),false);
        assertEquals(2,p.writes);
    }
    @Test void originalProgressiveDecelerationRemainsBounded() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player();
        m.progressiveDecelerationSetting().set(Boolean.TRUE);
        m.decelerationStepSetting().set(0.10D);
        m.onEnable();
        m.apply(p,state(false,false),false);
        assertEquals(-0.30D,p.y,1E-12);
        m.apply(p,state(false,false),false);
        assertEquals(-0.20D,p.y,1E-12);
        m.apply(p,state(false,false),false);
        assertEquals(-0.10D,p.y,1E-12);
        m.apply(p,state(false,false),false);
        assertEquals(-0.08D,p.y,1E-12);
    }
    @Test void realSlowDescentAndNonfiniteMotionCannotBeAdjusted() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player();m.onEnable();
        p.y=-0.03D;m.apply(p,state(false,false),false);
        p.y=Double.NaN;m.apply(p,state(false,false),false);
        p.y=Double.NEGATIVE_INFINITY;m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
    }
    @Test void originalGuiFocusResetsPendingDelayWithoutNativeRead() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player();m.activationDelayTicksSetting().set(1);
        m.onEnable();m.apply(p,state(false,false),false);
        m.apply(p,state(false,false),false,true);
        assertEquals(1,p.reads);
        m.apply(p,state(false,false),false);
        assertEquals(0,p.writes);
        m.apply(p,state(false,false),false);
        assertEquals(1,p.writes);
    }
    @Test void disabledSourceNeverReadsOrWritesNativeY() {
        final Minecraft189GlideModule m = new Minecraft189GlideModule();
        final Player p = new Player();m.onEnable();m.onDisable();
        m.apply(p,state(false,false),false);
        assertEquals(0,p.reads+p.writes);
        assertFalse(m.active());
    }
}
