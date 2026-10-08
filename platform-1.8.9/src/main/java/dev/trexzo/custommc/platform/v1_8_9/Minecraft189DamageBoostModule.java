package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189DamageBoostModule
        implements Module {
    public static final String ID =
            "movement.damageBoost";
    public static final String MULTIPLIER_SETTING_ID =
            "movement.damageBoost.multiplier";
    public static final String VERTICAL_MULTIPLIER_SETTING_ID =
            "movement.damageBoost.verticalMultiplier";
    public static final String CAP_HORIZONTAL_SETTING_ID =
            "movement.damageBoost.capHorizontal";
    public static final String MAX_HORIZONTAL_SPEED_SETTING_ID =
            "movement.damageBoost.maxHorizontalSpeed";
    public static final double DEFAULT_MAX_HORIZONTAL_SPEED = 0.70D;
    public static final double MINIMUM_MAX_HORIZONTAL_SPEED = 0.10D;
    public static final double MAXIMUM_MAX_HORIZONTAL_SPEED = 5.00D;
    public static final double DEFAULT_MULTIPLIER =
            1.25D;
    public static final double DEFAULT_VERTICAL_MULTIPLIER =
            1.0D;
    public static final double MINIMUM_MULTIPLIER =
            1.0D;
    public static final double MAXIMUM_MULTIPLIER =
            3.0D;

    private final Setting<Double> multiplier =
            new Setting<Double>(
                    MULTIPLIER_SETTING_ID,
                    DEFAULT_MULTIPLIER,
                    Minecraft189DamageBoostModule::validMultiplier,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> verticalMultiplier =
            new Setting<Double>(
                    VERTICAL_MULTIPLIER_SETTING_ID,
                    DEFAULT_VERTICAL_MULTIPLIER,
                    Minecraft189DamageBoostModule::validMultiplier,
                    SettingCodecs.DOUBLE);

    private final Setting<Boolean> capHorizontal =
            new Setting<Boolean>(
                    CAP_HORIZONTAL_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Double> maxHorizontalSpeed =
            new Setting<Double>(
                    MAX_HORIZONTAL_SPEED_SETTING_ID,
                    DEFAULT_MAX_HORIZONTAL_SPEED,
                    value -> value != null && Double.isFinite(value)
                            && value >= MINIMUM_MAX_HORIZONTAL_SPEED
                            && value <= MAXIMUM_MAX_HORIZONTAL_SPEED,
                    SettingCodecs.DOUBLE);
    private boolean enabled;
    private boolean previousAvailable;
    private int previousHurtTime;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> multiplierSetting() {
        return multiplier;
    }

    public Setting<Double> verticalMultiplierSetting() {
        return verticalMultiplier;
    }

    public Setting<Boolean> capHorizontalSetting() {
        return capHorizontal;
    }

    public Setting<Double> maxHorizontalSpeedSetting() {
        return maxHorizontalSpeed;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetObservation();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetObservation();
    }

    synchronized boolean apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerHurtTimeState.Snapshot hurtTime,
            final boolean suspended) {
        if (hurtTime == null
                || !hurtTime.available()) {
            resetObservation();
            return false;
        }

        final int currentHurtTime =
                hurtTime.hurtTime();
        final boolean freshHit =
                previousAvailable
                        && currentHurtTime > previousHurtTime;
        previousAvailable = true;
        previousHurtTime = currentHurtTime;

        if (!enabled
                || suspended
                || player == null
                || !freshHit) {
            return false;
        }

        final double configuredMultiplier =
                multiplier.get().doubleValue();
        final double configuredVerticalMultiplier =
                verticalMultiplier.get().doubleValue();
        final double currentX =
                player.customMcMotionX();
        final double currentY =
                player.customMcMotionY();
        final double currentZ =
                player.customMcMotionZ();
        double targetX =
                cleanZero(currentX * configuredMultiplier);
        final double targetY =
                cleanZero(currentY * configuredVerticalMultiplier);
        double targetZ =
                cleanZero(currentZ * configuredMultiplier);

        if (capHorizontal.get().booleanValue()) {
            // Cap only the *additional* momentum from this boost.
            // Never slow pre-existing horizontal motion above the cap.
            // If real mapped velocity is malformed, fail closed on
            // horizontal writes without affecting the vertical owner.
            final double currentMagnitude = Math.hypot(currentX, currentZ);
            final double boostedMagnitude = Math.hypot(targetX, targetZ);
            final double cap = maxHorizontalSpeed.get().doubleValue();
            if (!Double.isFinite(currentMagnitude)
                    || !Double.isFinite(boostedMagnitude)
                    || currentMagnitude >= cap) {
                targetX = currentX;
                targetZ = currentZ;
            } else if (boostedMagnitude > cap) {
                final double fraction = cap / boostedMagnitude;
                targetX = cleanZero(targetX * fraction);
                targetZ = cleanZero(targetZ * fraction);
            }
        }

        boolean changed = false;
        if (Double.compare(
                currentX,
                targetX) != 0) {
            player.customMcSetMotionX(
                    targetX);
            changed = true;
        }
        if (Double.compare(
                currentY,
                targetY) != 0) {
            player.customMcSetMotionY(
                    targetY);
            changed = true;
        }
        if (Double.compare(
                currentZ,
                targetZ) != 0) {
            player.customMcSetMotionZ(
                    targetZ);
            changed = true;
        }
        return changed;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetObservation() {
        previousAvailable = false;
        previousHurtTime = 0;
    }

    private static boolean validMultiplier(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_MULTIPLIER
                && value.doubleValue() <= MAXIMUM_MULTIPLIER;
    }

    private static double cleanZero(
            final double value) {
        return Math.abs(value) < 0.000000001D
                ? 0.0D
                : value;
    }
}
