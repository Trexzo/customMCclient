package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoGravityModule
        implements Module {
    public static final String ID =
            "movement.noGravity";
    public static final String SMOOTH_LIFT_SETTING_ID =
            "movement.noGravity.smoothLift";
    public static final String REQUIRE_JUMP_HELD_SETTING_ID =
            "movement.noGravity.requireJumpHeld";
    public static final String LIFT_STEP_SETTING_ID =
            "movement.noGravity.liftStep";
    public static final double DEFAULT_LIFT_STEP = 0.10D;
    public static final double MINIMUM_LIFT_STEP = 0.01D;
    public static final double MAXIMUM_LIFT_STEP = 0.50D;
    public static final String LIFT_SPEED_SETTING_ID =
            "movement.noGravity.liftSpeed";
    public static final double DEFAULT_LIFT_SPEED = 0.0D;
    public static final double MAXIMUM_LIFT_SPEED = 0.30D;

    private final Setting<Double> liftSpeed =
            new Setting<Double>(
                    LIFT_SPEED_SETTING_ID, DEFAULT_LIFT_SPEED,
                    value -> value != null && Double.isFinite(value.doubleValue())
                            && value >= 0.0D && value <= MAXIMUM_LIFT_SPEED,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> smoothLift =
            new Setting<Boolean>(
                    SMOOTH_LIFT_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Double> liftStep =
            new Setting<Double>(
                    LIFT_STEP_SETTING_ID, DEFAULT_LIFT_STEP,
                    value -> value != null && Double.isFinite(value.doubleValue())
                            && value >= MINIMUM_LIFT_STEP
                            && value <= MAXIMUM_LIFT_STEP,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> requireJumpHeld = new Setting<Boolean>(
            REQUIRE_JUMP_HELD_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // Invalidates callbacks that read a native source owner before restart.
    private long nativeReadEpoch;
    // The genuine EntityPlayer.motionY mapped getter/setter may invoke
    // the host's movement callback synchronously before vanilla state
    // advances. One original stack owns at most one native Y transaction.
    private boolean nativeMotionDispatching;
    private Minecraft189PlayerMotionControl nativeMotionPlayer;
    private boolean nativeMotionRevoked;

    public Setting<Double> liftSpeedSetting() {
        return liftSpeed;
    }

    public Setting<Boolean> smoothLiftSetting() {
        return smoothLift;
    }

    public Setting<Double> liftStepSetting() {
        return liftStep;
    }

    public Setting<Boolean> requireJumpHeldSetting() {
        return requireJumpHeld;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        nativeReadEpoch++;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeReadEpoch++;
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        // Legacy callers have no physical Space state.
        apply(player, movement, suspended, false);
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended,
            final boolean jumpHeld) {
        apply(player, movement, suspended, jumpHeld, false);
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended,
            final boolean jumpHeld,
            final boolean actualClickGuiOpen) {
        // Nested callbacks may not borrow the still-active native Y
        // getter or setter. A fresh observed player/focus/ground/Space
        // loss also cancels the outer motion transaction.
        if (nativeMotionDispatching) {
            if (player != nativeMotionPlayer || actualClickGuiOpen
                    || suspended || movement == null
                    || !movement.available() || movement.onGround()
                    || !ownsVertical(jumpHeld)) {
                nativeMotionRevoked = true;
            }
            return;
        }
        // The real GUI owns keyboard focus and mapped player Y-motion.
        // Keep the vertical priority reservation intact so lower-priority
        // FastFall / Glide modules cannot substitute a different synthetic
        // motion write on the same menu-owned callback.
        if (!ownsVertical(jumpHeld)
                || actualClickGuiOpen
                || suspended
                || player == null
                || movement == null
                || !movement.available()
                || movement.onGround()) {
            return;
        }

        nativeMotionDispatching = true;
        nativeMotionPlayer = player;
        nativeMotionRevoked = false;
        try {
            // Capture live settings and lifetime BEFORE the native Y-motion
            // read. That getter may synchronously restart this module or edit
            // settings; neither may spend the old permission after it returns.
            final long approvedEpoch = nativeReadEpoch;
            final double target = liftSpeed.get().doubleValue();
            final boolean smooth = smoothLift.get().booleanValue();
            final double step = liftStep.get().doubleValue();
            // The original zero-gravity behavior is exactly target=0.
            // A positive setting is an optional minimum upward speed:
            // never slow an existing faster upward jump.
            final double current = player.customMcMotionY();
            if (nativeMotionRevoked) return;
            if (!enabled || nativeReadEpoch != approvedEpoch
                    || !ownsVertical(jumpHeld)
                    || liftSpeed.get().doubleValue() != target
                    || smoothLift.get().booleanValue() != smooth
                    || liftStep.get().doubleValue() != step) {
                return;
            }
            if (current < target) {
                // Optional smoothing reduces the gap at most one step per
                // eligible callback. Every value derives from current real
                // vertical motion: no hidden accumulator or catch-up credit.
                // Invalid mapped speed is safely normalized to finite target.
                final double next = smooth
                        && Double.isFinite(current)
                                ? Math.min(target, current + step)
                                : target;
                if (next > current) {
                    player.customMcSetMotionY(next);
                }
            }
        } finally {
            // Never create a synthetic rollback of an original Y write
            // that completed before a mapped setter threw.
            nativeMotionPlayer = null;
            nativeMotionRevoked = false;
            nativeMotionDispatching = false;
        }
    }

    synchronized boolean active() {
        return enabled;
    }

    synchronized boolean ownsVertical(final boolean jumpHeld) {
        // Keep owner arbitration and actual writes governed by the same
        // physical Space input. Releasing Space yields to Fast Fall/Glide
        // instead of leaving an inert No Gravity owner in their way.
        return enabled && (!requireJumpHeld.get().booleanValue() || jumpHeld);
    }
}
