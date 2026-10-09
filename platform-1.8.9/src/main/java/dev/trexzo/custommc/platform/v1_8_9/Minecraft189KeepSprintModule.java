package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Synthetic-attack sprint preservation. Uses the exact mapped sprint flag
 * and setter; no packet spoofing and no modifications to manual attacks.
 */
public final class Minecraft189KeepSprintModule implements Module {
    public static final String ID = "combat.keepSprint";
    public static final String REQUIRE_FORWARD = ID + ".requireForward";
    public static final String REQUIRE_PLAYER_HIT = ID + ".requirePlayerHit";
    public static final String PAUSE_SNEAK = ID + ".pauseWhileSneaking";

    private final Setting<Boolean> forward = new Setting<Boolean>(
            REQUIRE_FORWARD, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> playerHit = new Setting<Boolean>(
            REQUIRE_PLAYER_HIT, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseSneak = new Setting<Boolean>(
            PAUSE_SNEAK, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private boolean enabled;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public Setting<Boolean> requireForwardSetting() { return forward; }
    public Setting<Boolean> requirePlayerHitSetting() { return playerHit; }
    public Setting<Boolean> pauseWhileSneakingSetting() { return pauseSneak; }
    synchronized boolean active() { return enabled; }

    synchronized boolean shouldRestore(
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean wasSprintingImmediatelyBeforeAttack,
            final boolean forwardHeld,
            final boolean confirmedPlayerHit,
            final boolean wTapOwnsSprint,
            final boolean guiOpen) {
        return enabled && !guiOpen && !wTapOwnsSprint
                && wasSprintingImmediatelyBeforeAttack
                && movement != null && movement.available()
                && (!forward.get() || forwardHeld)
                && (!playerHit.get() || confirmedPlayerHit)
                && (!pauseSneak.get() || !movement.sneaking());
    }
}
