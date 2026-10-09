package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Legitimate-air Criticals mode. This cannot force a server critical: it only
 * gates synthetic attacks until vanilla airborne/falling conditions exist.
 * No spoofed movement packets, onGround writes, or arbitrary damage.
 */
public final class Minecraft189CriticalsModule implements Module {
    public static final String ID = "combat.criticals";
    public static final String MIN_FALL_DISTANCE = ID + ".minimumFallDistance";
    public static final String REQUIRE_DESCENDING = ID + ".requireDescending";

    private final Setting<Double> minFall = new Setting<Double>(
            MIN_FALL_DISTANCE, 0.1D,
            value -> value != null && Double.isFinite(value)
                    && value >= 0.0D && value <= 3.0D,
            SettingCodecs.DOUBLE);
    private final Setting<Boolean> requireDescending = new Setting<Boolean>(
            REQUIRE_DESCENDING, Boolean.TRUE, value -> value != null,
            SettingCodecs.BOOLEAN);
    private boolean enabled;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public Setting<Double> minimumFallDistanceSetting() { return minFall; }
    public Setting<Boolean> requireDescendingSetting() { return requireDescending; }
    synchronized boolean active() { return enabled; }

    synchronized boolean permits(
            final Minecraft189PlayerMovementState.Snapshot movement,
            final Minecraft189CriticalsEvidence.Snapshot evidence,
            final boolean anotherMovementOwnerActive) {
        if (!enabled) return true;
        // Unknown and conflicting movement evidence always blocks a synthetic
        // attack, but does not interfere with ordinary Minecraft mouse input.
        if (anotherMovementOwnerActive || movement == null || !movement.available()
                || movement.onGround() || evidence == null || !evidence.available()
                || evidence.fallDistance() < minFall.get()) return false;
        return !requireDescending.get() || evidence.motionY() < 0.0D;
    }
}
