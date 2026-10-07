package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189JitterModule
        implements Module {
    public static final String ID =
            "combat.jitter";
    public static final String YAW_SETTING_ID =
            "combat.jitter.yawDegrees";
    public static final String PITCH_SETTING_ID =
            "combat.jitter.pitchDegrees";
    public static final String YAW_ENABLED_SETTING_ID =
            "combat.jitter.yawEnabled";
    public static final String PITCH_ENABLED_SETTING_ID =
            "combat.jitter.pitchEnabled";
    public static final String INTERVAL_SETTING_ID =
            "combat.jitter.intervalTicks";
    public static final String REQUIRE_HOLD_SETTING_ID =
            "combat.jitter.requireHold";
    public static final double DEFAULT_DEGREES =
            0.50D;
    public static final double MINIMUM_DEGREES =
            0.0D;
    public static final double MAXIMUM_DEGREES =
            5.0D;
    public static final int DEFAULT_INTERVAL_TICKS =
            1;
    public static final int MINIMUM_INTERVAL_TICKS =
            1;
    public static final int MAXIMUM_INTERVAL_TICKS =
            10;

    private final Setting<Double> yawDegrees =
            new Setting<Double>(
                    YAW_SETTING_ID,
                    DEFAULT_DEGREES,
                    Minecraft189JitterModule::validDegrees,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> pitchDegrees =
            new Setting<Double>(
                    PITCH_SETTING_ID,
                    DEFAULT_DEGREES,
                    Minecraft189JitterModule::validDegrees,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> yawEnabled =
            new Setting<Boolean>(
                    YAW_ENABLED_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pitchEnabled =
            new Setting<Boolean>(
                    PITCH_ENABLED_SETTING_ID,
                    Boolean.TRUE,
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
    private final Setting<Boolean> requireHold =
            new Setting<Boolean>(
                    REQUIRE_HOLD_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private boolean enabled;
    private boolean positivePhase = true;
    private int ticksUntilNext;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> yawDegreesSetting() {
        return yawDegrees;
    }

    public Setting<Double> pitchDegreesSetting() {
        return pitchDegrees;
    }

    public Setting<Boolean> yawEnabledSetting() {
        return yawEnabled;
    }

    public Setting<Boolean> pitchEnabledSetting() {
        return pitchEnabled;
    }

    public Setting<Integer> intervalTicksSetting() {
        return intervalTicks;
    }

    public Setting<Boolean> requireHoldSetting() {
        return requireHold;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetCadence();
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
        final boolean yawAxisEnabled =
                yawEnabled.get().booleanValue();
        final boolean pitchAxisEnabled =
                pitchEnabled.get().booleanValue();
        if (!enabled
                || player == null
                || rotation == null
                || !rotation.available()
                || (!yawAxisEnabled
                        && !pitchAxisEnabled)
                || (requireHold.get().booleanValue()
                        && !leftButtonHeld)) {
            resetCadence();
            return false;
        }

        if (ticksUntilNext > 0) {
            ticksUntilNext--;
            return false;
        }

        final int configuredInterval =
                intervalTicks.get().intValue();
        ticksUntilNext =
                configuredInterval - 1;

        final double direction =
                positivePhase
                        ? 1.0D
                        : -1.0D;
        positivePhase = !positivePhase;

        final float currentYaw =
                rotation.yaw();
        final float currentPitch =
                rotation.pitch();
        final float targetYaw =
                yawAxisEnabled
                        ? (float) (currentYaw
                                + direction
                                * yawDegrees.get().doubleValue())
                        : currentYaw;
        final float targetPitch =
                pitchAxisEnabled
                        ? clampPitch(
                                (float) (currentPitch
                                        + direction
                                        * pitchDegrees.get().doubleValue()))
                        : currentPitch;

        boolean changed = false;
        if (Float.compare(
                currentYaw,
                targetYaw) != 0) {
            player.customMcSetRotationYaw(
                    targetYaw);
            changed = true;
        }
        if (Float.compare(
                currentPitch,
                targetPitch) != 0) {
            player.customMcSetRotationPitch(
                    targetPitch);
            changed = true;
        }
        return changed;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetCadence() {
        positivePhase = true;
        ticksUntilNext = 0;
    }

    private static float clampPitch(
            final float pitch) {
        if (pitch < -90.0F) {
            return -90.0F;
        }
        if (pitch > 90.0F) {
            return 90.0F;
        }
        return pitch;
    }

    private static boolean validDegrees(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_DEGREES
                && value.doubleValue() <= MAXIMUM_DEGREES;
    }
}
