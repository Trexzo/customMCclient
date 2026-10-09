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
    public static final String PROGRESSIVE_SETTING_ID =
            "movement.glide.progressiveDeceleration";
    public static final String DECELERATION_STEP_SETTING_ID =
            "movement.glide.decelerationStep";
    public static final double DEFAULT_DECELERATION_STEP = 0.10D;
    public static final double MINIMUM_DECELERATION_STEP = 0.01D;
    public static final double MAXIMUM_DECELERATION_STEP = 0.50D;
    public static final String REQUIRE_SNEAKING_SETTING_ID =
            "movement.glide.requireSneaking";
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
    private final Setting<Boolean> requireSneaking =
            new Setting<Boolean>(
                    REQUIRE_SNEAKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> progressiveDeceleration =
            new Setting<Boolean>(
                    PROGRESSIVE_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Double> decelerationStep =
            new Setting<Double>(
                    DECELERATION_STEP_SETTING_ID,
                    DEFAULT_DECELERATION_STEP,
                    value -> value != null && Double.isFinite(value.doubleValue())
                            && value >= MINIMUM_DECELERATION_STEP
                            && value <= MAXIMUM_DECELERATION_STEP,
                    SettingCodecs.DOUBLE);
    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> fallSpeedSetting() {
        return fallSpeed;
    }

    public Setting<Boolean> requireSneakingSetting() {
        return requireSneaking;
    }

    public Setting<Boolean> progressiveDecelerationSetting() {
        return progressiveDeceleration;
    }

    public Setting<Double> decelerationStepSetting() {
        return decelerationStep;
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
                || movement.onGround()
                || (requireSneaking.get().booleanValue()
                        && !movement.sneaking())) {
            return;
        }

        final double targetMotionY =
                -fallSpeed.get().doubleValue();
        final double currentMotionY = player.customMcMotionY();
        if (Double.isFinite(currentMotionY)
                && currentMotionY < targetMotionY) {
            // An optional per-callback deceleration step avoids a
            // sudden jump from fast descent to the Glide cap. The next
            // value always derives from current mapped motion, never
            // from accumulated credit or a hidden scheduler.
            final double nextMotionY = progressiveDeceleration.get().booleanValue()
                    ? Math.min(targetMotionY,
                            currentMotionY + decelerationStep.get().doubleValue())
                    : targetMotionY;
            if (nextMotionY > currentMotionY) {
                player.customMcSetMotionY(nextMotionY);
            }
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
