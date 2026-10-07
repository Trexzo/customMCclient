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
    public static final double DEFAULT_DEGREES =
            0.50D;
    public static final double MINIMUM_DEGREES =
            0.0D;
    public static final double MAXIMUM_DEGREES =
            5.0D;

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

    private boolean enabled;
    private boolean positivePhase = true;

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

    @Override
    public synchronized void onEnable() {
        enabled = true;
        positivePhase = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        positivePhase = true;
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean leftButtonHeld) {
        if (!enabled
                || player == null
                || rotation == null
                || !rotation.available()
                || !leftButtonHeld) {
            positivePhase = true;
            return false;
        }

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
                (float) (currentYaw
                        + direction
                        * yawDegrees.get().doubleValue());
        final float targetPitch =
                clampPitch(
                        (float) (currentPitch
                                + direction
                                * pitchDegrees.get().doubleValue()));

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
