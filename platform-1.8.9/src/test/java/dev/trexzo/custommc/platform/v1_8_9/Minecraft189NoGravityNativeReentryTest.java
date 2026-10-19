package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Original-shaped motion adapter evidence; not a Mojang binary run. */
final class Minecraft189NoGravityNativeReentryTest {
    private static Minecraft189PlayerMovementState.Snapshot movement(
            final boolean ground) {
        final Minecraft189PlayerMovementState s =
                new Minecraft189PlayerMovementState();
        s.update(ground, false, false);
        return s.snapshot();
    }

    private static final class Player implements Minecraft189PlayerMotionControl {
        double y = -0.20D;
        int reads, writes;
        Runnable onRead, onWrite;
        boolean throwRead, throwWrite;
        @Override public double customMcMotionX() {return 0.0D;}
        @Override public double customMcMotionZ() {return 0.0D;}
        @Override public void customMcSetMotionX(double v) {}
        @Override public void customMcSetMotionZ(double v) {}
        @Override public double customMcMotionY() {
            reads++;
            if (reads > 10) throw new AssertionError("recursive native read");
            if (onRead != null) {
                final Runnable r = onRead; onRead = null; r.run();
            }
            if (throwRead) throw new IllegalStateException("getter failed");
            return y;
        }
        @Override public void customMcSetMotionY(double v) {
            writes++;
            if (writes > 10) throw new AssertionError("recursive native write");
            y = v;
            if (onWrite != null) {
                final Runnable r = onWrite; onWrite = null; r.run();
            }
            if (throwWrite) throw new IllegalStateException("setter failed");
        }
    }

    @Test void nativeSourceDescentProducesOneRealYWrite() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable(); m.apply(p, movement(false), false, false, false);
        assertEquals(1, p.reads); assertEquals(1, p.writes);
        assertEquals(0.0D, p.y, 0.0D);
    }

    @Test void reentrantOriginalGetterCannotRecursivelyLift() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        p.onRead = () -> m.apply(p, movement(false), false, false, false);
        m.apply(p, movement(false), false, false, false);
        assertEquals(1, p.reads); assertEquals(1, p.writes);
    }

    @Test void reentrantOriginalSetterCannotIssueSecondLift() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        p.onWrite = () -> m.apply(p, movement(false), false, false, false);
        m.apply(p, movement(false), false, false, false);
        assertEquals(1, p.reads); assertEquals(1, p.writes);
    }

    @Test void secondOriginalPlayerRevokesOuterGetterLease() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player a = new Player(), b = new Player();
        m.onEnable();
        a.onRead = () -> m.apply(b, movement(false), false, false, false);
        m.apply(a, movement(false), false, false, false);
        assertEquals(0, a.writes + b.reads + b.writes);
        m.apply(b, movement(false), false, false, false);
        assertEquals(1, b.writes);
    }

    @Test void secondOriginalPlayerCannotWriteDuringNativeSetter() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player a = new Player(), b = new Player();
        m.onEnable();
        a.onWrite = () -> m.apply(b, movement(false), false, false, false);
        m.apply(a, movement(false), false, false, false);
        assertEquals(1, a.writes); assertEquals(0, b.reads + b.writes);
    }

    @Test void actualGuiFocusInsideNativeReadCancelsOuterLift() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        p.onRead = () -> m.apply(p, movement(false), false, false, true);
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.writes);
    }

    @Test void originalLandingInsideNativeReadCancelsOuterLift() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        p.onRead = () -> m.apply(p, movement(true), false, false, false);
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.writes);
    }

    @Test void suspensionInsideGetterCancelsOldMovementSource() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        p.onRead = () -> m.apply(p, movement(false), true, false, false);
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.writes);
    }

    @Test void missingMappedMovementInsideGetterCannotAuthorizeLift() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        final Minecraft189PlayerMovementState missing =
                new Minecraft189PlayerMovementState();
        m.onEnable();
        p.onRead = () -> m.apply(p, missing.snapshot(), false, false, false);
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.writes);
    }

    @Test void releasedPhysicalSpaceCannotBorrowEarlierLift() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.requireJumpHeldSetting().set(Boolean.TRUE); m.onEnable();
        p.onRead = () -> m.apply(p, movement(false), false, false, false);
        m.apply(p, movement(false), false, true, false);
        assertEquals(0, p.writes);
        assertFalse(m.ownsVertical(false));
        assertTrue(m.ownsVertical(true));
    }

    @Test void offOnInNativeGetterRevokesPriorEpoch() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        p.onRead = () -> {m.onDisable(); m.onEnable();};
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.writes);
        m.apply(p, movement(false), false, false, false);
        assertEquals(1, p.writes);
    }

    @Test void liftSpeedEditedInsideGetterRevokesOldSettings() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        p.onRead = () -> m.liftSpeedSetting().set(0.2D);
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.writes);
        m.apply(p, movement(false), false, false, false);
        assertEquals(0.2D, p.y, 1E-12);
    }

    @Test void nativeReadExceptionReleasesGuardForNextRealTick() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable(); p.throwRead = true;
        assertThrows(IllegalStateException.class,
                () -> m.apply(p, movement(false), false, false, false));
        p.throwRead = false;
        m.apply(p, movement(false), false, false, false);
        assertEquals(1, p.writes);
    }

    @Test void nativeSetterPartialWriteThrowReleasesGuard() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable(); p.throwWrite = true;
        assertThrows(IllegalStateException.class,
                () -> m.apply(p, movement(false), false, false, false));
        assertEquals(0.0D, p.y, 0D);
        p.throwWrite = false; p.y = -0.2D;
        m.apply(p, movement(false), false, false, false);
        assertEquals(2, p.writes);
    }

    @Test void originalSmoothLiftStillUsesOneConfiguredStep() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        m.smoothLiftSetting().set(Boolean.TRUE);
        m.liftStepSetting().set(0.1D);
        m.apply(p, movement(false), false, false, false);
        assertEquals(-0.1D, p.y, 1E-12);
        m.apply(p, movement(false), false, false, false);
        assertEquals(0D, p.y, 1E-12);
    }

    @Test void genuineUpwardMotionDoesNotGetReduced() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable(); p.y = 0.45D;
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.writes);
        assertEquals(0.45D, p.y, 0D);
    }

    @Test void originalGuiAndDisabledModuleDoNotReadNativeY() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.onEnable();
        m.apply(p, movement(false), false, false, true);
        m.onDisable();
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.reads + p.writes);
    }

    @Test void retainedOriginalPriorityStillHonorsPhysicalSpace() {
        final Minecraft189NoGravityModule m = new Minecraft189NoGravityModule();
        final Player p = new Player();
        m.requireJumpHeldSetting().set(Boolean.TRUE);
        m.onEnable();
        m.apply(p, movement(false), false, false, false);
        assertEquals(0, p.reads);
        m.apply(p, movement(false), false, true, false);
        assertEquals(1, p.writes);
    }
}
