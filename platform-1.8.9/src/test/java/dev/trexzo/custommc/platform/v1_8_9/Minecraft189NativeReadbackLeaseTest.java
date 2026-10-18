package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These are adversarial offline Java policy tests, not proof of native
 * Minecraft World movement, PvP packets or dedicated-server acceptance.
 * A genuine source getter can synchronously reenter module lifecycle or GUI
 * policy, and a callback approved before that change must not write.
 */
final class Minecraft189NativeReadbackLeaseTest {
    private static final class Web implements Minecraft189PlayerWebControl {
        boolean inWeb = true;
        int writes;
        int reads;
        Runnable onRead;

        @Override public boolean customMcInWeb() {
            reads++;
            final boolean original = inWeb;
            final Runnable callback = onRead;
            onRead = null;
            if (callback != null) callback.run();
            return original;
        }
        @Override public void customMcSetInWeb(boolean value) {
            writes++;
            inWeb = value;
        }
    }

    private static final class Fall implements Minecraft189PlayerFallDistanceControl {
        float distance = 6.0F;
        int reads;
        int writes;
        Runnable onRead;

        @Override public float customMcFallDistance() {
            reads++;
            final float original = distance;
            final Runnable callback = onRead;
            onRead = null;
            if (callback != null) callback.run();
            return original;
        }
        @Override public void customMcSetFallDistance(float value) {
            writes++;
            distance = value;
        }
    }

    private static final class Motion implements Minecraft189PlayerMotionControl {
        double y = -0.2D;
        int reads;
        int writes;
        Runnable onRead;

        @Override public double customMcMotionX() { return 0; }
        @Override public void customMcSetMotionX(double value) { }
        @Override public double customMcMotionY() {
            reads++;
            final double original = y;
            final Runnable callback = onRead;
            onRead = null;
            if (callback != null) callback.run();
            return original;
        }
        @Override public void customMcSetMotionY(double value) {
            writes++;
            y = value;
        }
        @Override public double customMcMotionZ() { return 0; }
        @Override public void customMcSetMotionZ(double value) { }
    }

    private static Minecraft189PlayerMovementState.Snapshot airborne() {
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        state.update(false, false, false);
        return state.snapshot();
    }

    @Test void webOriginalGetterRestartCannotSpendOldEnableEpoch() {
        final Minecraft189NoWebModule module = new Minecraft189NoWebModule();
        final Web web = new Web();
        module.onEnable();
        web.onRead = () -> { module.onDisable(); module.onEnable(); };
        module.apply(web);
        assertTrue(web.inWeb);
        assertEquals(0, web.writes);
        module.apply(web);
        assertFalse(web.inWeb);
        assertEquals(1, web.writes);
        module.onDisable();
    }

    @Test void webNativeReadChangingGuiPolicyRevokesCurrentWrite() {
        final Minecraft189NoWebModule module = new Minecraft189NoWebModule();
        final Web web = new Web();
        module.onEnable();
        web.onRead = () -> module.requireSneakingSetting().set(Boolean.TRUE);
        module.apply(web);
        assertTrue(web.inWeb);
        assertEquals(0, web.writes);
        module.apply(web); // Now requires real sneaking authority.
        assertEquals(0, web.writes);
        module.requireSneakingSetting().set(Boolean.FALSE);
        module.apply(web);
        assertFalse(web.inWeb);
        assertEquals(1, web.writes);
        module.onDisable();
    }

    @Test void fallOriginalGetterRestartCannotZeroNewLifetime() {
        final Minecraft189NoFallModule module = new Minecraft189NoFallModule();
        final Fall fall = new Fall();
        module.onEnable();
        fall.onRead = () -> { module.onDisable(); module.onEnable(); };
        module.apply(fall);
        assertEquals(6.0F, fall.distance);
        assertEquals(0, fall.writes);
        module.apply(fall);
        assertEquals(0.0F, fall.distance);
        assertEquals(1, fall.writes);
        module.onDisable();
    }

    @Test void fallChangingThresholdDuringOriginalGetterRevokesOldPermission() {
        final Minecraft189NoFallModule module = new Minecraft189NoFallModule();
        final Fall fall = new Fall();
        module.onEnable();
        module.thresholdSetting().set(1.0D);
        fall.onRead = () -> module.thresholdSetting().set(7.0D);
        module.apply(fall);
        assertEquals(0, fall.writes);
        module.apply(fall); // Fresh setting legitimately vetoes distance 6.
        assertEquals(0, fall.writes);
        module.thresholdSetting().set(1.0D);
        module.apply(fall);
        assertEquals(1, fall.writes);
        module.onDisable();
    }

    @Test void gravityOriginalMotionGetterRestartCannotWriteOldEpoch() {
        final Minecraft189NoGravityModule module = new Minecraft189NoGravityModule();
        final Motion motion = new Motion();
        module.onEnable();
        motion.onRead = () -> { module.onDisable(); module.onEnable(); };
        module.apply(motion, airborne(), false);
        assertEquals(-0.2D, motion.y, 0.00001D);
        assertEquals(0, motion.writes);
        module.apply(motion, airborne(), false);
        assertEquals(0.0D, motion.y, 0.00001D);
        assertEquals(1, motion.writes);
        module.onDisable();
    }

    @Test void gravityGuiSpeedMutationDuringOriginalGetterRevokesOldLift() {
        final Minecraft189NoGravityModule module = new Minecraft189NoGravityModule();
        final Motion motion = new Motion();
        module.onEnable();
        module.liftSpeedSetting().set(0.1D);
        motion.onRead = () -> module.liftSpeedSetting().set(0.2D);
        module.apply(motion, airborne(), false);
        assertEquals(0, motion.writes);
        module.apply(motion, airborne(), false);
        assertEquals(0.2D, motion.y, 0.00001D);
        assertEquals(1, motion.writes);
        module.onDisable();
    }


    @Test void gravityNativeReadEnablingPhysicalSpaceRequirementRevokesOldWrite() {
        final Minecraft189NoGravityModule module = new Minecraft189NoGravityModule();
        final Motion motion = new Motion();
        module.onEnable();
        motion.onRead = () -> module.requireJumpHeldSetting().set(Boolean.TRUE);
        module.apply(motion, airborne(), false, false);
        assertEquals(0, motion.writes);
        module.apply(motion, airborne(), false, false);
        assertEquals(0, motion.writes);
        module.apply(motion, airborne(), false, true);
        assertEquals(1, motion.writes);
        assertEquals(0.0D, motion.y, 0.00001D);
        module.onDisable();
    }

    @Test void fallSourceGetterDisablingModuleNeverZeroesNativeDistance() {
        final Minecraft189NoFallModule module = new Minecraft189NoFallModule();
        final Fall fall = new Fall();
        module.onEnable();
        fall.onRead = module::onDisable;
        module.apply(fall);
        assertEquals(0, fall.writes);
        assertEquals(6.0F, fall.distance);
        module.apply(fall);
        assertEquals(0, fall.writes);
        module.onEnable();
        module.apply(fall);
        assertEquals(1, fall.writes);
        module.onDisable();
    }

    @Test void originalUiAndConfiguredMovementVetoPreventGetterEvaluation() {
        final Minecraft189NoWebModule web = new Minecraft189NoWebModule();
        final Minecraft189NoFallModule fall = new Minecraft189NoFallModule();
        final Minecraft189NoGravityModule gravity = new Minecraft189NoGravityModule();
        final Web w = new Web();
        final Fall f = new Fall();
        final Motion m = new Motion();
        web.onEnable();
        fall.onEnable();
        gravity.onEnable();
        web.apply(w, null, true);
        fall.apply(f, null, true);
        gravity.apply(m, airborne(), false, false, true);
        assertEquals(0, w.writes);
        assertEquals(0, f.writes);
        assertEquals(0, m.writes);
        assertEquals(0, w.reads);
        assertEquals(0, f.reads);
        assertEquals(0, m.reads);
        web.onDisable();
        fall.onDisable();
        gravity.onDisable();
    }
}
