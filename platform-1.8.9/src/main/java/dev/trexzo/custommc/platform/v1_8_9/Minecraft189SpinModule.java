package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189SpinModule
        implements Module {
    public static final String ID =
            "combat.spin";
    public static final String YAW_SPEED_SETTING_ID =
            "combat.spin.yawSpeed";
    public static final String REVERSE_SETTING_ID =
            "combat.spin.reverse";
    public static final String REQUIRE_HOLD_SETTING_ID =
            "combat.spin.requireHold";
    public static final double DEFAULT_YAW_SPEED =
            20.0D;
    public static final double MINIMUM_YAW_SPEED =
            1.0D;
    public static final double MAXIMUM_YAW_SPEED =
            180.0D;

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

    private boolean enabled;

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

    @Override
    public synchronized void onEnable() {
        enabled = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean leftButtonHeld) {
        if (!enabled
                || player == null
                || rotation == null
                || !rotation.available()
                || (requireHold.get().booleanValue()
                        && !leftButtonHeld)) {
            return false;
        }

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
