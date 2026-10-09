package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public final class Minecraft189SpinModule
        implements Module {
    public static final String ID =
            "combat.spin";
    public static final String YAW_SPEED_SETTING_ID =
            "combat.spin.yawSpeed";
    public static final String REVERSE_SETTING_ID =
            "combat.spin.reverse";
    public static final String GROUND_ONLY_SETTING_ID =
            "combat.spin.groundOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "combat.spin.pauseWhileSneaking";
    public static final String REQUIRE_HOLD_SETTING_ID =
            "combat.spin.requireHold";
    public static final String INTERVAL_SETTING_ID =
            "combat.spin.intervalTicks";
    public static final String RANDOM_INTERVAL_SETTING_ID =
            "combat.spin.randomInterval";
    public static final String INTERVAL_VARIATION_SETTING_ID =
            "combat.spin.intervalVariationTicks";
    public static final int DEFAULT_INTERVAL_VARIATION_TICKS = 2;
    public static final int MAXIMUM_INTERVAL_VARIATION_TICKS = 5;
    public static final double DEFAULT_YAW_SPEED =
            20.0D;
    public static final double MINIMUM_YAW_SPEED =
            1.0D;
    public static final double MAXIMUM_YAW_SPEED =
            180.0D;
    public static final int DEFAULT_INTERVAL_TICKS =
            1;
    public static final int MINIMUM_INTERVAL_TICKS =
            1;
    public static final int MAXIMUM_INTERVAL_TICKS =
            10;

    private final Setting<Double> yawSpeed =
            new Setting<Double>(
                    YAW_SPEED_SETTING_ID,
                    DEFAULT_YAW_SPEED,
                    Minecraft189SpinModule::validYawSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> reverse =
            new Setting<Boolean>(
                    REVERSE_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireHold =
            new Setting<Boolean>(
                    REQUIRE_HOLD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> intervalTicks =
            new Setting<Integer>(
                    INTERVAL_SETTING_ID,
                    DEFAULT_INTERVAL_TICKS,
                    value -> value != null
                            && value >= MINIMUM_INTERVAL_TICKS
                            && value <= MAXIMUM_INTERVAL_TICKS,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> groundOnly =
            new Setting<Boolean>(GROUND_ONLY_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);

    private final Setting<Boolean> randomInterval =
            new Setting<Boolean>(
                    RANDOM_INTERVAL_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> intervalVariationTicks =
            new Setting<Integer>(
                    INTERVAL_VARIATION_SETTING_ID,
                    DEFAULT_INTERVAL_VARIATION_TICKS,
                    value -> value != null && value >= 0
                            && value <= MAXIMUM_INTERVAL_VARIATION_TICKS,
                    SettingCodecs.INTEGER);
    private final DoubleSupplier randomUnit;
    private boolean enabled;
    private int ticksUntilNext;
    private boolean previousRandomInterval;
    private int previousVariationTicks;
    private int previousIntervalTicks;

    public Minecraft189SpinModule() {
        this(() -> ThreadLocalRandom.current().nextDouble());
    }

    Minecraft189SpinModule(final DoubleSupplier randomUnit) {
        this.randomUnit = Objects.requireNonNull(randomUnit, "randomUnit");
    }

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> yawSpeedSetting() {
        return yawSpeed;
    }

    public Setting<Boolean> reverseSetting() {
        return reverse;
    }

    public Setting<Boolean> requireHoldSetting() {
        return requireHold;
    }

    public Setting<Integer> intervalTicksSetting() {
        return intervalTicks;
    }

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    public Setting<Boolean> randomIntervalSetting() {
        return randomInterval;
    }

    public Setting<Integer> intervalVariationTicksSetting() {
        return intervalVariationTicks;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetCadence();
        previousRandomInterval = randomInterval.get().booleanValue();
        previousVariationTicks = intervalVariationTicks.get().intValue();
        previousIntervalTicks = intervalTicks.get().intValue();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetCadence();
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean leftButtonHeld) {
        // Original callers cannot assert a movement snapshot.
        return apply(player, rotation, leftButtonHeld, null);
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean leftButtonHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled
                || player == null
                || rotation == null
                || !rotation.available()
                || (requireHold.get().booleanValue()
                        && !leftButtonHeld)
                || ((groundOnly.get().booleanValue()
                        || pauseWhileSneaking.get().booleanValue())
                        && (movement == null || !movement.available()
                        || (groundOnly.get().booleanValue()
                                && !movement.onGround())
                        || (pauseWhileSneaking.get().booleanValue()
                                && movement.sneaking())))) {
            resetCadence();
            return false;
        }

        final boolean varying = randomInterval.get().booleanValue();
        final int variation = intervalVariationTicks.get().intValue();
        final int configuredInterval = intervalTicks.get().intValue();
        if (varying != previousRandomInterval
                || (varying && (variation != previousVariationTicks
                        || configuredInterval != previousIntervalTicks))) {
            resetCadence();
        }
        previousRandomInterval = varying;
        previousVariationTicks = variation;
        previousIntervalTicks = configuredInterval;

        if (ticksUntilNext > 0) {
            ticksUntilNext--;
            return false;
        }

        // Fixed mode stays identical to legacy scheduling: no random
        // sample is read, and changing the base interval mid-countdown
        // does not restart it. Variation is opt-in.
        ticksUntilNext = sampleInterval(
                configuredInterval, varying, variation) - 1;

        final float currentYaw =
                rotation.yaw();
        final double direction =
                reverse.get().booleanValue()
                        ? -1.0D
                        : 1.0D;
        final float targetYaw =
                wrapYaw(
                        (float) (currentYaw
                                + direction
                                * yawSpeed.get().doubleValue()));
        if (Float.compare(
                currentYaw,
                targetYaw) == 0) {
            return false;
        }

        player.customMcSetRotationYaw(
                targetYaw);
        return true;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetCadence() {
        ticksUntilNext = 0;
    }

    private int sampleInterval(
            final int interval,
            final boolean varying,
            final int variation) {
        if (!varying || variation == 0) {
            return interval;
        }
        final double raw = randomUnit.getAsDouble();
        // Invalid injected samples neutralize the offset rather than
        // escaping 1..10 bounds or corrupting rotation cadence.
        final double sample = Double.isFinite(raw) && raw >= 0.0D && raw < 1.0D
                ? raw : 0.5D;
        final int offset = (int) Math.floor(
                sample * (variation * 2 + 1)) - variation;
        return Math.max(MINIMUM_INTERVAL_TICKS,
                Math.min(MAXIMUM_INTERVAL_TICKS, interval + offset));
    }

    private static float wrapYaw(
            final float yaw) {
        float wrapped =
                yaw % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    private static boolean validYawSpeed(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_YAW_SPEED
                && value.doubleValue() <= MAXIMUM_YAW_SPEED;
    }
}
