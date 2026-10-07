package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189GlideModule
        implements Module {
    public static final String ID =
            "movement.glide";
    public static final String FALL_SPEED_SETTING_ID =
            "movement.glide.fallSpeed";
    public static final double DEFAULT_FALL_SPEED =
            0.08D;
    public static final double MINIMUM_FALL_SPEED =
            0.01D;
    public static final double MAXIMUM_FALL_SPEED =
            0.50D;

    private final Setting<Double> fallSpeed =
            new Setting<Double>(
                    FALL_SPEED_SETTING_ID,
                    DEFAULT_FALL_SPEED,
                    Minecraft189GlideModule::validFallSpeed,
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

        final double targetMotionY =
                -fallSpeed.get().doubleValue();
        if (player.customMcMotionY()
                < targetMotionY) {
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
