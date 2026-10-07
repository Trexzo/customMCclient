package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189ReverseStepModule
        implements Module {
    public static final String ID =
            "movement.reverseStep";
    public static final String SPEED_SETTING_ID =
            "movement.reverseStep.speed";
    public static final double DEFAULT_SPEED =
            0.50D;
    public static final double MINIMUM_SPEED =
            0.05D;
    public static final double MAXIMUM_SPEED =
            1.50D;

    private final Setting<Double> speed =
            new Setting<Double>(
                    SPEED_SETTING_ID,
                    DEFAULT_SPEED,
                    Minecraft189ReverseStepModule::validSpeed,
                    SettingCodecs.DOUBLE);
    private boolean enabled;
    private boolean previousAvailable;
    private boolean previousOnGround;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> speedSetting() {
        return speed;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        previousAvailable = false;
        previousOnGround = false;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        previousAvailable = false;
        previousOnGround = false;
    }

    synchronized boolean apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        if (movement == null
                || !movement.available()) {
            previousAvailable = false;
            previousOnGround = false;
            return false;
        }

        final boolean currentOnGround =
                movement.onGround();
        final boolean transitionedOffGround =
                previousAvailable
                        && previousOnGround
                        && !currentOnGround;
        previousAvailable = true;
        previousOnGround = currentOnGround;

        if (!enabled
                || suspended
                || player == null
                || !transitionedOffGround) {
            return false;
        }

        final double currentMotionY =
                player.customMcMotionY();
        if (currentMotionY > 0.0D) {
            return false;
        }

        final double targetMotionY =
                -speed.get().doubleValue();
        if (currentMotionY > targetMotionY) {
            player.customMcSetMotionY(
                    targetMotionY);
            return true;
        }
        return false;
    }

    synchronized boolean active() {
        return enabled;
    }

    private static boolean validSpeed(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_SPEED
                && value.doubleValue() <= MAXIMUM_SPEED;
    }
}
