package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Java adapter regressions; neither mocks native Mojang game nor server PvP. */
final class Minecraft189NativeDescentReadbackEpochTest {
    private static final class Motion implements Minecraft189PlayerMotionControl {
        double y;
        int reads;
        int writes;
        Runnable onRead;
        Motion(double y) { this.y = y; }
        @Override public double customMcMotionX() { return 0D; }
        @Override public void customMcSetMotionX(double value) { }
        @Override public double customMcMotionY() {
            reads++;
            final double existing = y;
            final Runnable callback = onRead;
            onRead = null;
            if (callback != null) callback.run();
            return existing;
        }
        @Override public void customMcSetMotionY(double value) {
            writes++;
            y = value;
        }
        @Override public double customMcMotionZ() { return 0D; }
        @Override public void customMcSetMotionZ(double value) { }
    }

    private static Minecraft189PlayerMovementState.Snapshot airborne(boolean sneaking) {
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        state.update(false, sneaking, false);
        return state.snapshot();
    }

    @Test void fastFallOriginalGetterRestartRejectsOldDescentPermission() {
        final Minecraft189FastFallModule module = new Minecraft189FastFallModule();
        final Motion player = new Motion(-0.05D);
        module.onEnable();
        player.onRead = () -> { module.onDisable(); module.onEnable(); };
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        assertEquals(-0.05D, player.y);
        module.apply(player, airborne(false), false);
        assertEquals(-0.30D, player.y);
        assertEquals(1, player.writes);
        module.onDisable();
    }

    @Test void glideOriginalGetterRestartRejectsOldDescentPermission() {
        final Minecraft189GlideModule module = new Minecraft189GlideModule();
        final Motion player = new Motion(-0.45D);
        module.onEnable();
        player.onRead = () -> { module.onDisable(); module.onEnable(); };
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false);
        assertEquals(-0.08D, player.y);
        assertEquals(1, player.writes);
        module.onDisable();
    }

    @Test void fastFallSpeedGuiEditDuringNativeReadRevokesWrite() {
        final Minecraft189FastFallModule module = new Minecraft189FastFallModule();
        final Motion player = new Motion(-0.06D);
        module.onEnable();
        player.onRead = () -> module.fallSpeedSetting().set(0.60D);
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        assertEquals(-0.06D, player.y);
        module.apply(player, airborne(false), false);
        assertEquals(-0.60D, player.y);
        module.onDisable();
    }

    @Test void glideRequireSneakGuiEditDuringNativeReadRevokesWrite() {
        final Minecraft189GlideModule module = new Minecraft189GlideModule();
        final Motion player = new Motion(-0.45D);
        module.onEnable();
        player.onRead = () -> module.requireSneakingSetting().set(Boolean.TRUE);
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.requireSneakingSetting().set(Boolean.FALSE);
        module.apply(player, airborne(false), false);
        assertEquals(-0.08D, player.y);
        assertEquals(1, player.writes);
        module.onDisable();
    }

    @Test void fastFallDelayEditedMidReadDiscardsOldEligibilityCredit() {
        final Minecraft189FastFallModule module = new Minecraft189FastFallModule();
        final Motion player = new Motion(-0.05D);
        module.onEnable();
        module.activationDelayTicksSetting().set(1);
        player.onRead = () -> module.activationDelayTicksSetting().set(2);
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false); // 1 / 2
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false); // 2 / 2
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false); // fresh eligible
        assertEquals(1, player.writes);
        module.onDisable();
    }

    @Test void glideDelayEditedMidReadDiscardsOldEligibilityCredit() {
        final Minecraft189GlideModule module = new Minecraft189GlideModule();
        final Motion player = new Motion(-0.45D);
        module.onEnable();
        module.activationDelayTicksSetting().set(1);
        player.onRead = () -> module.activationDelayTicksSetting().set(2);
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false);
        assertEquals(1, player.writes);
        module.onDisable();
    }

    @Test void fastFallOriginalNativeGetterDisableDoesNotAdvanceNewDelay() {
        final Minecraft189FastFallModule module = new Minecraft189FastFallModule();
        final Motion player = new Motion(-0.04D);
        module.onEnable();
        module.activationDelayTicksSetting().set(1);
        player.onRead = module::onDisable;
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.onEnable();
        module.apply(player, airborne(false), false);
        assertEquals(0, player.writes);
        module.apply(player, airborne(false), false);
        assertEquals(1, player.writes);
        module.onDisable();
    }

    @Test void actualGuiFocusBreaksDescentAndSkipsOriginalGetter() {
        final Minecraft189FastFallModule fast = new Minecraft189FastFallModule();
        final Minecraft189GlideModule glide = new Minecraft189GlideModule();
        final Motion p = new Motion(-0.45D);
        fast.onEnable();
        glide.onEnable();
        try {
            fast.apply(p, airborne(false), false, true);
            glide.apply(p, airborne(false), false, true);
            assertEquals(0, p.reads);
            assertEquals(0, p.writes);
            fast.apply(p, airborne(false), true, false);
            glide.apply(p, airborne(false), true, false);
            assertEquals(0, p.reads);
            assertEquals(0, p.writes);
        } finally {
            fast.onDisable();
            glide.onDisable();
        }
    }
}
