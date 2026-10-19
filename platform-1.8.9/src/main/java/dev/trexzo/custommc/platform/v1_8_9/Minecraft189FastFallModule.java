package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189FastFallModule
        implements Module {
    public static final String ID =
            "movement.fastFall";
    public static final String FALL_SPEED_SETTING_ID =
            "movement.fastFall.fallSpeed";
    public static final String PROGRESSIVE_SETTING_ID =
            "movement.fastFall.progressive";
    public static final String ACTIVATION_DELAY_SETTING_ID =
            "movement.fastFall.activationDelayTicks";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "movement.fastFall.pauseWhileSneaking";
    public static final int DEFAULT_ACTIVATION_DELAY_TICKS = 0;
    public static final int MAXIMUM_ACTIVATION_DELAY_TICKS = 10;
    public static final String RAMP_STEP_SETTING_ID =
            "movement.fastFall.rampStep";
    public static final double DEFAULT_FALL_SPEED =
            0.30D;
    public static final double MINIMUM_FALL_SPEED =
            0.05D;
    public static final double MAXIMUM_FALL_SPEED =
            1.00D;
    public static final double DEFAULT_RAMP_STEP = 0.05D;
    public static final double MINIMUM_RAMP_STEP = 0.01D;
    public static final double MAXIMUM_RAMP_STEP = 0.50D;

    private final Setting<Double> fallSpeed =
            new Setting<Double>(
                    FALL_SPEED_SETTING_ID,
                    DEFAULT_FALL_SPEED,
                    Minecraft189FastFallModule::validFallSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> progressive =
            new Setting<Boolean>(
                    PROGRESSIVE_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Double> rampStep =
            new Setting<Double>(
                    RAMP_STEP_SETTING_ID,
                    DEFAULT_RAMP_STEP,
                    value -> value != null
                            && Double.isFinite(value.doubleValue())
                            && value.doubleValue() >= MINIMUM_RAMP_STEP
                            && value.doubleValue() <= MAXIMUM_RAMP_STEP,
                    SettingCodecs.DOUBLE);
    private final Setting<Integer> activationDelayTicks =
            new Setting<Integer>(
                    ACTIVATION_DELAY_SETTING_ID,
                    DEFAULT_ACTIVATION_DELAY_TICKS,
                    value -> value != null && value >= 0
                            && value <= MAXIMUM_ACTIVATION_DELAY_TICKS,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> pauseWhileSneaking = new Setting<Boolean>(
            PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // Revokes a native motion read when module lifecycle reenters mid-getter.
    private long nativeReadEpoch;
    private int eligibleDescentCallbacks;
    private int observedActivationDelay;
    // Mapped EntityPlayer.motionY access can synchronously reenter the host.
    // Only one native movement callback may own an active read/write stack,
    // regardless of OFF->ON or the pending activation countdown.
    private boolean nativeMotionDispatching;
    private Minecraft189PlayerMotionControl nativeMotionPlayer;
    private boolean nativeMotionRevoked;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> fallSpeedSetting() {
        return fallSpeed;
    }

    public Setting<Boolean> progressiveSetting() {
        return progressive;
    }

    public Setting<Double> rampStepSetting() {
        return rampStep;
    }

    public Setting<Integer> activationDelayTicksSetting() {
        return activationDelayTicks;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        nativeReadEpoch++;
        resetActivationDelay();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeReadEpoch++;
        resetActivationDelay();
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        apply(player, movement, suspended, false);
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended,
            final boolean actualClickGuiOpen) {
        // A mapped Y getter or setter can reenter this same movement callback
        // before the source tick has finished. Nested callbacks must never
        // spend another descent tick or issue a second original Y write.
        // A changed player or actual GUI/suspension/grounded permission
        // additionally revokes the outer pending motion transaction.
        if (nativeMotionDispatching) {
            if (!enabled || player != nativeMotionPlayer
                    || actualClickGuiOpen || suspended || movement == null
                    || !movement.available() || movement.onGround()
                    || (pauseWhileSneaking.get().booleanValue()
                            && movement.sneaking())) {
                nativeMotionRevoked = true;
            }
            return;
        }
        // An actual original GUI focus change breaks consecutive descent
        // evidence. Discard delayed eligibility rather than releasing a
        // pre-UI fall into an immediate synthetic motion write on close.
        if (!enabled
                || actualClickGuiOpen
                || suspended
                || player == null
                || movement == null
                || !movement.available()
                || movement.onGround()
                || (pauseWhileSneaking.get().booleanValue()
                        && movement.sneaking())) {
            resetActivationDelay();
            return;
        }

        nativeMotionDispatching = true;
        nativeMotionPlayer = player;
        nativeMotionRevoked = false;
        try {
            // A real mapped getter can synchronously restart this module or
            // change persisted GUI policy. Capture permission BEFORE reading it.
            final long approvedEpoch = nativeReadEpoch;
            final int approvedDelay = activationDelayTicks.get().intValue();
            final boolean approvedPauseSneaking =
                    pauseWhileSneaking.get().booleanValue();
            final double approvedFallSpeed = fallSpeed.get().doubleValue();
            final boolean approvedProgressive = progressive.get().booleanValue();
            final double approvedRampStep = rampStep.get().doubleValue();
            final double currentMotionY = player.customMcMotionY();
            if (nativeMotionRevoked) {
                // A nested original host callback has changed source motion
                // ownership or GUI/physical eligibility. No old read credit.
                resetActivationDelay();
                return;
            }
            if (!enabled || nativeReadEpoch != approvedEpoch) {
                // onEnable/onDisable already reset the new lifetime delay.
                return;
            }
            if (activationDelayTicks.get().intValue() != approvedDelay
                    || pauseWhileSneaking.get().booleanValue()
                        != approvedPauseSneaking
                    || fallSpeed.get().doubleValue() != approvedFallSpeed
                    || progressive.get().booleanValue() != approvedProgressive
                    || rampStep.get().doubleValue() != approvedRampStep) {
                // A GUI edit invalidates any previously accumulated descent
                // callbacks; the next real source call starts a fresh policy.
                resetActivationDelay();
                return;
            }
            // Unknown vertical motion must not produce a synthetic write.
            // Preserve the existing no-write behavior for upward and stationary
            // motion, and for descent already at or faster than the target.
            if (!Double.isFinite(currentMotionY) || currentMotionY >= 0.0D) {
                resetActivationDelay();
                return;
            }

            final int delay = approvedDelay;
            if (delay != observedActivationDelay) {
                eligibleDescentCallbacks = 0;
                observedActivationDelay = delay;
            }
            if (eligibleDescentCallbacks < delay) {
                eligibleDescentCallbacks++;
                return;
            }

            final double targetMotionY = -approvedFallSpeed;
            if (currentMotionY > targetMotionY) {
                final double nextMotionY = approvedProgressive
                        ? Math.max(targetMotionY,
                                currentMotionY - approvedRampStep)
                        : targetMotionY;
                if (nextMotionY < currentMotionY) {
                    player.customMcSetMotionY(nextMotionY);
                }
            }
        } finally {
            // A genuine mapped getter/setter can mutate native motion and
            // then throw. Do not fabricate a rollback; release only our
            // same-stack dispatch guard.
            nativeMotionPlayer = null;
            nativeMotionRevoked = false;
            nativeMotionDispatching = false;
        }
    }

    private void resetActivationDelay() {
        eligibleDescentCallbacks = 0;
        observedActivationDelay = activationDelayTicks.get().intValue();
    }

    synchronized boolean active() {
        return enabled;
    }

    private static boolean validFallSpeed(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_FALL_SPEED
                && value.doubleValue() <= MAXIMUM_FALL_SPEED;
    }
}
