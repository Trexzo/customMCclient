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
    public static final double DEFAULT_FALL_SPEED =
            0.30D;
    public static final double MINIMUM_FALL_SPEED =
            0.05D;
    public static final double MAXIMUM_FALL_SPEED =
            1.00D;

    private final Setting<Double> fallSpeed =
            new Setting<Double>(
                    FALL_SPEED_SETTING_ID,
                    DEFAULT_FALL_SPEED,
                    Minecraft189FastFallModule::validFallSpeed,
                    SettingCodecs.DOUBLE);
    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> fallSpeedSetting() {
        return fallSpeed;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        if (!enabled
                || suspended
                || player == null
                || movement == null
                || !movement.available()
                || movement.onGround()) {
            return;
        }

        final double currentMotionY =
                player.customMcMotionY();
        if (currentMotionY >= 0.0D) {
            return;
        }

        final double targetMotionY =
                -fallSpeed.get().doubleValue();
        if (currentMotionY > targetMotionY) {
            player.customMcSetMotionY(
                    targetMotionY);
        }
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
