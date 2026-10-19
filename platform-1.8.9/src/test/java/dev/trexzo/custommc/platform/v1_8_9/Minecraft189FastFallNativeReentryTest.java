package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Source motion adapter tests: not a real Mojang 1.8.9 binary run. */
final class Minecraft189FastFallNativeReentryTest {
    private static Minecraft189PlayerMovementState.Snapshot state(
            final boolean grounded, final boolean sneaking) {
        final Minecraft189PlayerMovementState s =
                new Minecraft189PlayerMovementState();
        s.update(grounded, sneaking, false);
        return s.snapshot();
    }

    private static final class NativePlayer
            implements Minecraft189PlayerMotionControl {
        double y = -0.05D;
        int reads, writes;
        Runnable onRead, onWrite;
        boolean throwRead, throwAfterWrite;
        @Override public double customMcMotionX() { return 0D; }
        @Override public double customMcMotionZ() { return 0D; }
        @Override public void customMcSetMotionX(final double value) {}
        @Override public void customMcSetMotionZ(final double value) {}
        @Override public double customMcMotionY() {
            reads++;
            if (reads > 10) throw new AssertionError("recursive Y getter");
            if (onRead != null) {
                final Runnable callback = onRead;
                onRead = null;
                callback.run();
            }
            if (throwRead) throw new IllegalStateException("native Y read");
            return y;
        }
        @Override public void customMcSetMotionY(final double next) {
            writes++;
            if (writes > 10) throw new AssertionError("recursive Y setter");
            y = next;
            if (onWrite != null) {
                final Runnable callback = onWrite;
                onWrite = null;
                callback.run();
            }
            if (throwAfterWrite)
                throw new IllegalStateException("native Y setter");
        }
    }

    @Test void positiveOriginalDescentWritesOnce() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        m.apply(p, state(false, false), false);
        assertEquals(1, p.reads);
        assertEquals(1, p.writes);
        assertEquals(-0.30D, p.y, 1E-10);
    }

    @Test void originalGetterReentryCannotDoubleCountOrWrite() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.onEnable();
        p.onRead = () -> m.apply(p, air, false);
        m.apply(p, air, false);
        assertEquals(1, p.reads);
        assertEquals(1, p.writes);
        assertEquals(-0.30D, p.y, 1E-10);
    }

    @Test void originalSetterReentryCannotDoubleWriteOrRead() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.onEnable();
        p.onWrite = () -> m.apply(p, air, false);
        m.apply(p, air, false);
        assertEquals(1, p.reads);
        assertEquals(1, p.writes);
    }

    @Test void nestedGetterCannotSpendAnExtraDelayedEligibilityTick() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.activationDelayTicksSetting().set(2);
        m.onEnable();
        p.onRead = () -> m.apply(p, air, false);
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(1, p.writes);
    }

    @Test void nestedSetterCannotAdvanceNextOriginalDelay() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.activationDelayTicksSetting().set(1);
        m.onEnable();
        m.apply(p, air, false);
        p.onWrite = () -> m.apply(p, air, false);
        m.apply(p, air, false);
        assertEquals(1, p.writes);
        p.y = -0.05D;
        m.apply(p, air, false);
        assertEquals(2, p.writes);
    }

    @Test void differentPlayerDuringNativeGetterRevokesOuterWrite() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer a = new NativePlayer(), b = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.onEnable();
        a.onRead = () -> m.apply(b, air, false);
        m.apply(a, air, false);
        assertEquals(1, a.reads);
        assertEquals(0, a.writes + b.reads + b.writes);
        m.apply(b, air, false);
        assertEquals(1, b.writes);
    }

    @Test void differentPlayerDuringNativeSetterCannotWriteNewPlayer() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer a = new NativePlayer(), b = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.onEnable();
        a.onWrite = () -> m.apply(b, air, false);
        m.apply(a, air, false);
        assertEquals(1, a.writes);
        assertEquals(0, b.reads + b.writes);
    }

    @Test void nestedActualGuiFocusRevokesPendingSource() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.activationDelayTicksSetting().set(1);
        m.onEnable();
        m.apply(p, air, false); // Credit 1/1.
        p.onRead = () -> m.apply(p, air, false, true);
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(0, p.writes); // Must rebuild 1/1 after UI.
        m.apply(p, air, false);
        assertEquals(1, p.writes);
    }

    @Test void nestedSuspensionRevokesPendingSource() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.onEnable();
        p.onRead = () -> m.apply(p, air, true);
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(1, p.writes);
    }

    @Test void nestedLandingRevokesOuterRead() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.onRead = () -> m.apply(p, state(true, false), false);
        m.apply(p, state(false, false), false);
        assertEquals(0, p.writes);
    }

    @Test void nestedMissingNativeMovementRevokesOuterRead() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState unavailable =
                new Minecraft189PlayerMovementState();
        m.onEnable();
        p.onRead = () -> m.apply(p, unavailable.snapshot(), false);
        m.apply(p, state(false, false), false);
        assertEquals(0, p.writes);
    }

    @Test void nestedPhysicalSneakPauseRevokesOldDescent() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        m.onEnable();
        p.onRead = () -> m.apply(p, state(false, true), false);
        m.apply(p, state(false, false), false);
        assertEquals(0, p.writes);
    }

    @Test void getterOffOnCannotSpendOldLifetimeDelay() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.activationDelayTicksSetting().set(1);
        m.onEnable();
        m.apply(p, air, false);
        p.onRead = () -> { m.onDisable(); m.onEnable(); };
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(1, p.writes);
    }

    @Test void getterPolicyEditCannotSpendOldCountdown() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.activationDelayTicksSetting().set(1);
        m.onEnable();
        m.apply(p, air, false);
        p.onRead = () -> m.fallSpeedSetting().set(0.5D);
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(-0.5D, p.y, 1E-10);
    }

    @Test void thrownNativeGetterReleasesGuard() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.throwRead = true;
        assertThrows(IllegalStateException.class,
                () -> m.apply(p, state(false, false), false));
        p.throwRead = false;
        m.apply(p, state(false, false), false);
        assertEquals(1, p.writes);
    }

    @Test void thrownNativeSetterCannotPoisonNextRealTick() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.throwAfterWrite = true;
        assertThrows(IllegalStateException.class,
                () -> m.apply(p, state(false, false), false));
        assertEquals(1, p.writes);
        assertEquals(-0.30D, p.y, 1E-10);
        p.throwAfterWrite = false;
        p.y = -0.05D; // Changed by next genuine original physics tick.
        m.apply(p, state(false, false), false);
        assertEquals(2, p.writes);
    }

    @Test void originalProgressiveRampPreservedUnderGuard() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        m.progressiveSetting().set(Boolean.TRUE);
        m.rampStepSetting().set(0.10D);
        m.onEnable();
        m.apply(p, state(false, false), false);
        assertEquals(-0.15D, p.y, 1E-10);
        m.apply(p, state(false, false), false);
        assertEquals(-0.25D, p.y, 1E-10);
        m.apply(p, state(false, false), false);
        assertEquals(-0.30D, p.y, 1E-10);
    }

    @Test void risingAndAlreadyFastRealMotionAreUntouched() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.y = 0.1D;
        m.apply(p, state(false, false), false);
        p.y = -0.7D;
        m.apply(p, state(false, false), false);
        p.y = Double.NaN;
        m.apply(p, state(false, false), false);
        assertEquals(0, p.writes);
    }

    @Test void ordinaryOriginalGuiNeverReadsOrConsumesPendingDelay() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState.Snapshot air = state(false, false);
        m.activationDelayTicksSetting().set(1);
        m.onEnable();
        m.apply(p, air, false);
        m.apply(p, air, false, true);
        assertEquals(1, p.reads);
        m.apply(p, air, false);
        assertEquals(0, p.writes);
        m.apply(p, air, false);
        assertEquals(1, p.writes);
    }

    @Test void disabledModuleNeverReadsNativeSource() {
        final Minecraft189FastFallModule m = new Minecraft189FastFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        m.onDisable();
        m.apply(p, state(false, false), false);
        assertEquals(0, p.reads + p.writes);
        assertFalse(m.active());
    }
}
