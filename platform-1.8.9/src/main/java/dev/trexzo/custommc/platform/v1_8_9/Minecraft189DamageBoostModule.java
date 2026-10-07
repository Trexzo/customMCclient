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
    public static final double DEFAULT_MULTIPLIER =
            1.25D;
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
        final double currentX =
                player.customMcMotionX();
        final double currentZ =
                player.customMcMotionZ();
        final double targetX =
                cleanZero(
                        currentX * configuredMultiplier);
        final double targetZ =
                cleanZero(
                        currentZ * configuredMultiplier);

        boolean changed = false;
        if (Double.compare(
                currentX,
                targetX) != 0) {
            player.customMcSetMotionX(
                    targetX);
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
