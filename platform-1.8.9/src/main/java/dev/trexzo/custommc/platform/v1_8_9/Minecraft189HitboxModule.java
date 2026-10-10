package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Optional player-only ray-hitbox inflation at the vanilla renderer border site.
 * Never mutates Entity bounding boxes or server combat authority.
 */
public final class Minecraft189HitboxModule implements Module {
    public static final String ID = "combat.hitbox";
    public static final String EXTRA_BORDER = ID + ".extraBorder";
    private final Setting<Double> extraBorder = new Setting<Double>(
            EXTRA_BORDER, 0.2D,
            value -> value != null && Double.isFinite(value)
                    && value >= 0.0D && value <= 0.6D,
            SettingCodecs.DOUBLE);
    private boolean enabled;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public Setting<Double> extraBorderSetting() { return extraBorder; }
    synchronized boolean active() { return enabled; }

    synchronized float adjustNativeBorder(final boolean player,
            final float originalBorder) {
        if (!enabled || !player || !Float.isFinite(originalBorder)
                || originalBorder < 0.0F) return originalBorder;
        final double adjusted = originalBorder + extraBorder.get();
        return Double.isFinite(adjusted) && adjusted <= Float.MAX_VALUE
                ? (float) adjusted : originalBorder;
    }
}
