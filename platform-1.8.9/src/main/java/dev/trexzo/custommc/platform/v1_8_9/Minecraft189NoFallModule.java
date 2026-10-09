package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoFallModule
        implements Module {
    public static final String ID =
            "movement.noFall";
    public static final String AIRBORNE_ONLY_SETTING_ID =
            "movement.noFall.airborneOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "movement.noFall.pauseWhileSneaking";
    public static final String THRESHOLD_SETTING_ID =
            "movement.noFall.threshold";
    public static final double DEFAULT_THRESHOLD = 0.0D;
    public static final double MINIMUM_THRESHOLD = 0.0D;
    public static final double MAXIMUM_THRESHOLD = 10.0D;

    private final Setting<Double> threshold =
            new Setting<Double>(
                    THRESHOLD_SETTING_ID,
                    DEFAULT_THRESHOLD,
                    value -> value != null
                            && !Double.isNaN(value.doubleValue())
                            && !Double.isInfinite(value.doubleValue())
                            && value.doubleValue() >= MINIMUM_THRESHOLD
                            && value.doubleValue() <= MAXIMUM_THRESHOLD,
                    SettingCodecs.DOUBLE);

    private final Setting<Boolean> airborneOnly =
            new Setting<Boolean>(
                    AIRBORNE_ONLY_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> thresholdSetting() {
        return threshold;
    }

    public Setting<Boolean> airborneOnlySetting() {
        return airborneOnly;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
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
            final Minecraft189PlayerFallDistanceControl player) {
        apply(player, null);
    }

    synchronized void apply(
            final Minecraft189PlayerFallDistanceControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled || player == null) {
            return;
        }
        final boolean requireAirborne = airborneOnly.get().booleanValue();
        final boolean pauseSneak = pauseWhileSneaking.get().booleanValue();
        if ((requireAirborne || pauseSneak)
                && (movement == null || !movement.available()
                || (requireAirborne && movement.onGround())
                || (pauseSneak && movement.sneaking()))) {
            // Opt-in restrictions require mapped state. No fabricated
            // airborne or sneak state, and no write on failed authority.
            return;
        }

        final float distance =
                player.customMcFallDistance();
        final double configured =
                threshold.get().doubleValue();
        if (Math.abs(distance) > configured + 0.000001D) {
            player.customMcSetFallDistance(0.0F);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
