package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Optional client-side vanilla ray-pick distance override. Default off.
 * This does not claim server-authorized attacks or change attack packets.
 */
public final class Minecraft189ReachModule implements Module {
    public static final String ID = "combat.reach";
    public static final String DISTANCE = ID + ".distance";
    private final Setting<Double> reach = new Setting<Double>(
            DISTANCE, 3.0D,
            value -> value != null && Double.isFinite(value)
                    && value >= 3.0D && value <= 6.0D,
            SettingCodecs.DOUBLE);
    private boolean enabled;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public Setting<Double> distanceSetting() { return reach; }
    synchronized boolean active() { return enabled; }

    synchronized float raycastBlockDistance(final float nativeDistance) {
        return enabled ? reach.get().floatValue() : nativeDistance;
    }

    synchronized boolean raycastExtendedBranch(final boolean nativeExtended) {
        return nativeExtended || enabled;
    }

    synchronized double raycastExtendedDistance(final double nativeDistance) {
        return enabled ? reach.get() : nativeDistance;
    }
}
