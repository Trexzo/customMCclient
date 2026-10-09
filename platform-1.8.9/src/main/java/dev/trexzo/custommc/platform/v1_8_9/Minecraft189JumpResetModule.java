package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/** Jump once on a fresh local-player hurt-time edge, never on every hurt tick. */
public final class Minecraft189JumpResetModule implements Module {
    public static final String ID = "combat.jumpReset";
    public static final String COOLDOWN_ID = ID + ".cooldownTicks";
    public static final String FORWARD_ID = ID + ".requireForward";
    public static final String SNEAK_ID = ID + ".pauseWhileSneaking";
    public static final int MAX_COOLDOWN = 20;

    private final Setting<Integer> cooldown = new Setting<Integer>(
            COOLDOWN_ID, 5,
            value -> value != null && value >= 0 && value <= MAX_COOLDOWN,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> forward = new Setting<Boolean>(
            FORWARD_ID, Boolean.TRUE, value -> value != null,
            SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseSneak = new Setting<Boolean>(
            SNEAK_ID, Boolean.TRUE, value -> value != null,
            SettingCodecs.BOOLEAN);
    private boolean enabled;
    private boolean previousAvailable;
    private int previousHurtTime;
    private int cooldownRemaining;
    private int scheduledCooldown = cooldown.get();

    @Override
    public String id() { return ID; }

    public Setting<Integer> cooldownSetting() { return cooldown; }
    public Setting<Boolean> requireForwardSetting() { return forward; }
    public Setting<Boolean> pauseWhileSneakingSetting() { return pauseSneak; }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        clear();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        clear();
    }

    synchronized boolean apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final Minecraft189PlayerHurtTimeState.Snapshot hurt,
            final boolean forwardHeld,
            final boolean suspended) {
        if (!enabled || player == null || movement == null
                || !movement.available() || hurt == null
                || !hurt.available()) {
            clear();
            return false;
        }
        final int value = hurt.hurtTime();
        final boolean freshHit = previousAvailable && value > previousHurtTime;
        previousAvailable = true;
        previousHurtTime = value;

        final int configuredCooldown = cooldown.get();
        if (scheduledCooldown != configuredCooldown) {
            scheduledCooldown = configuredCooldown;
            cooldownRemaining = 0;
        }
        final boolean cooling = cooldownRemaining > 0;
        if (cooling) cooldownRemaining--;

        if (!freshHit || cooling || suspended || !movement.onGround()
                || (forward.get() && !forwardHeld)
                || (pauseSneak.get() && movement.sneaking())) {
            return false;
        }

        player.customMcJump();
        cooldownRemaining = configuredCooldown;
        return true;
    }

    synchronized boolean active() { return enabled; }

    private void clear() {
        previousAvailable = false;
        previousHurtTime = 0;
        cooldownRemaining = 0;
        scheduledCooldown = cooldown.get();
    }
}
