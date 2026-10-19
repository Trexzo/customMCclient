package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Offline original-shaped native float adapter; not a Mojang binary run. */
final class Minecraft189NoFallNativeReentryTest {
    private static Minecraft189PlayerMovementState.Snapshot state(
            final boolean ground, final boolean sneak) {
        final Minecraft189PlayerMovementState s =
                new Minecraft189PlayerMovementState();
        s.update(ground, sneak, false);
        return s.snapshot();
    }

    private static final class NativePlayer
            implements Minecraft189PlayerFallDistanceControl {
        float distance = 4.0F;
        int reads, writes;
        Runnable onRead, onWrite;
        boolean throwRead, throwAfterWrite;
        @Override public float customMcFallDistance() {
            reads++;
            if (reads > 8) throw new AssertionError("recursive native getter");
            if (onRead != null) {
                final Runnable callback = onRead;
                onRead = null;
                callback.run();
            }
            if (throwRead) throw new IllegalStateException("native read");
            return distance;
        }
        @Override public void customMcSetFallDistance(final float next) {
            writes++;
            if (writes > 8) throw new AssertionError("recursive native setter");
            distance = next;
            if (onWrite != null) {
                final Runnable callback = onWrite;
                onWrite = null;
                callback.run();
            }
            if (throwAfterWrite)
                throw new IllegalStateException("native setter");
        }
    }

    @Test void positiveOriginalFallZerosOnce() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        m.apply(p);
        assertEquals(1, p.reads);
        assertEquals(1, p.writes);
        assertEquals(0F, p.distance, 0F);
    }

    @Test void originalGetterReentryNeverIssuesSecondNativeZero() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.onRead = () -> m.apply(p);
        m.apply(p);
        assertEquals(1, p.reads);
        assertEquals(1, p.writes);
    }

    @Test void originalSetterReentryNeverIssuesSecondNativeZero() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.onWrite = () -> m.apply(p);
        m.apply(p);
        assertEquals(1, p.reads);
        assertEquals(1, p.writes);
    }

    @Test void originalGetterCannotBorrowOtherPlayersFallDistance() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer a = new NativePlayer(), b = new NativePlayer();
        m.onEnable();
        a.onRead = () -> m.apply(b);
        m.apply(a);
        assertEquals(1, a.reads);
        assertEquals(0, a.writes + b.reads + b.writes);
        m.apply(b);
        assertEquals(1, b.writes);
    }

    @Test void originalSetterDoesNotZeroAnotherPlayersDistance() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer a = new NativePlayer(), b = new NativePlayer();
        m.onEnable();
        a.onWrite = () -> m.apply(b);
        m.apply(a);
        assertEquals(1, a.writes);
        assertEquals(0, b.reads + b.writes);
        assertEquals(4F, b.distance, 0F);
    }

    @Test void actualClickGuiInsideGetterVetoesOldZero() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.onRead = () -> m.apply(p, state(false,false), true);
        m.apply(p, state(false,false), false);
        assertEquals(0, p.writes);
        assertEquals(4F, p.distance, 0F);
    }

    @Test void airborneOnlyNestedGroundObservationRevokesOldZero() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.airborneOnlySetting().set(Boolean.TRUE);
        m.onEnable();
        p.onRead = () -> m.apply(p, state(true,false), false);
        m.apply(p, state(false,false), false);
        assertEquals(0, p.writes);
    }

    @Test void nestedSneakingVetoPreservesOriginalFallDistance() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        m.onEnable();
        p.onRead = () -> m.apply(p, state(false,true), false);
        m.apply(p, state(false,false), false);
        assertEquals(0, p.writes);
    }

    @Test void unavailableMappedMovementRevokesOnlyWhenPolicyRequiresIt() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        final Minecraft189PlayerMovementState unavailable =
                new Minecraft189PlayerMovementState();
        m.airborneOnlySetting().set(Boolean.TRUE);
        m.onEnable();
        p.onRead = () -> m.apply(p, unavailable.snapshot(), false);
        m.apply(p, state(false,false), false);
        assertEquals(0, p.writes);
        m.airborneOnlySetting().set(Boolean.FALSE);
        m.apply(p, unavailable.snapshot(), false);
        assertEquals(1, p.writes);
    }

    @Test void getterModuleOffOnInvalidatesPriorEpoch() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.onRead = () -> {m.onDisable();m.onEnable();};
        m.apply(p);
        assertEquals(0, p.writes);
        m.apply(p);
        assertEquals(1, p.writes);
    }

    @Test void getterThresholdEditCannotSpendOldPolicy() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.onRead = () -> m.thresholdSetting().set(5.0D);
        m.apply(p);
        assertEquals(0, p.writes);
        m.thresholdSetting().set(3.0D);
        m.apply(p);
        assertEquals(1, p.writes);
    }

    @Test void getterAirbornePolicyEditInvalidatesOldCallback() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.onRead = () -> m.airborneOnlySetting().set(Boolean.TRUE);
        m.apply(p, state(false,false), false);
        assertEquals(0, p.writes);
        m.apply(p, state(false,false), false);
        assertEquals(1, p.writes);
    }

    @Test void setterExceptionAfterActualZeroCannotPoisonNewTick() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.throwAfterWrite = true;
        assertThrows(IllegalStateException.class, () -> m.apply(p));
        assertEquals(0F, p.distance, 0F);
        assertEquals(1, p.writes);
        p.throwAfterWrite = false;
        p.distance = 7F; // Later vanilla source falling tick.
        m.apply(p);
        assertEquals(2, p.writes);
    }

    @Test void getterExceptionReleasesOldNativeOwner() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.throwRead = true;
        assertThrows(IllegalStateException.class, () -> m.apply(p));
        p.throwRead = false;
        m.apply(p);
        assertEquals(1, p.writes);
    }

    @Test void thresholdBoundaryNeverClearsEqualFallDistance() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.thresholdSetting().set(4.0D);
        m.onEnable();
        m.apply(p);
        assertEquals(0, p.writes);
        p.distance = 4.1F;
        m.apply(p);
        assertEquals(1, p.writes);
    }

    @Test void negativeAndNonfiniteNativeFallCannotAuthorizeZero() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        p.distance = -4F; m.apply(p);
        p.distance = Float.NaN; m.apply(p);
        p.distance = Float.POSITIVE_INFINITY; m.apply(p);
        assertEquals(0, p.writes);
        assertTrue(Float.isInfinite(p.distance));
    }

    @Test void originalGuiAndDisabledModuleNeverReadNativeFall() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.onEnable();
        m.apply(p, state(false,false), true);
        assertEquals(0, p.reads + p.writes);
        m.onDisable();
        m.apply(p);
        assertFalse(m.active());
        assertEquals(0, p.reads + p.writes);
    }

    @Test void optionalGroundAndSneakGatesStillRequireOriginalState() {
        final Minecraft189NoFallModule m = new Minecraft189NoFallModule();
        final NativePlayer p = new NativePlayer();
        m.airborneOnlySetting().set(Boolean.TRUE);
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        m.onEnable();
        m.apply(p, null, false);
        m.apply(p, state(true,false), false);
        m.apply(p, state(false,true), false);
        assertEquals(0, p.reads + p.writes);
        m.apply(p, state(false,false), false);
        assertEquals(1, p.writes);
    }
}
