package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoFallModule
        implements Module {
    public static final String ID =
            "movement.noFall";
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

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> thresholdSetting() {
        return threshold;
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
        if (!enabled || player == null) {
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
