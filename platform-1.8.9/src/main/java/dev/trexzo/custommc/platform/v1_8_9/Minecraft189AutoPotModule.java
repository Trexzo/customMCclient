package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Configured-slot healing-item attempt at a real mapped health threshold.
 * Item class and server cast/heal outcomes are not inferred from display name.
 */
public final class Minecraft189AutoPotModule implements Module {
    public static final String ID = "combat.autoPot";
    public static final String POT_SLOT = ID + ".potSlot";
    public static final String HEALTH_THRESHOLD = ID + ".healthPercent";
    public static final String COOLDOWN_TICKS = ID + ".cooldownTicks";
    public static final String REQUIRE_TARGET = ID + ".requireCombatTarget";
    private final Setting<Integer> potSlot = new Setting<Integer>(
            POT_SLOT, 3, x -> x != null && x >= 1 && x <= 9,
            SettingCodecs.INTEGER);
    private final Setting<Double> healthThreshold = new Setting<Double>(
            HEALTH_THRESHOLD, 35.0D,
            x -> x != null && Double.isFinite(x) && x >= 1.0D && x <= 99.0D,
            SettingCodecs.DOUBLE);
    private final Setting<Integer> cooldown = new Setting<Integer>(
            COOLDOWN_TICKS, 120, x -> x != null && x >= 1 && x <= 1200,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> requireTarget = new Setting<Boolean>(
            REQUIRE_TARGET, Boolean.FALSE, x -> x != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    private int remaining;
    private int knownCooldown;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() {
        enabled = true;
        reset();
    }
    @Override public synchronized void onDisable() {
        enabled = false;
        reset();
    }
    public Setting<Integer> potSlotSetting() { return potSlot; }
    public Setting<Double> healthThresholdSetting() { return healthThreshold; }
    public Setting<Integer> cooldownTicksSetting() { return cooldown; }
    public Setting<Boolean> requireCombatTargetSetting() { return requireTarget; }
    synchronized boolean active() { return enabled; }

    synchronized void suspend() { reset(); }

    synchronized boolean shouldUse(
            final Minecraft189PlayerHealthState.Snapshot health,
            final boolean targetVerified,
            final boolean rightHeld,
            final boolean guiOpen,
            final boolean inventoryAvailable) {
        if (!enabled || guiOpen || rightHeld || !inventoryAvailable
                || (requireTarget.get() && !targetVerified)
                || health == null || !health.available()
                || !Float.isFinite(health.health())
                || !Float.isFinite(health.maxHealth())
                || health.health() <= 0.0F || health.maxHealth() <= 0.0F
                || health.health() >= health.maxHealth()) {
            reset();
            return false;
        }
        if (100.0D * health.health() / health.maxHealth()
                > healthThreshold.get()) {
            reset();
            return false;
        }
        if (knownCooldown != cooldown.get()) reset();
        if (remaining > 0) {
            remaining--;
            return false;
        }
        remaining = cooldown.get();
        return true;
    }

    synchronized int selectSlot(final Minecraft189InventoryHotbarControl inventory) {
        if (!enabled || inventory == null) return -1;
        final int previous = inventory.customMcSelectedHotbarSlot();
        if (previous < 0 || previous > 8) return -1;
        inventory.customMcSetSelectedHotbarSlot(potSlot.get() - 1);
        return previous;
    }

    synchronized void restoreSlot(final Minecraft189InventoryHotbarControl inventory,
            final int original) {
        if (inventory == null || original < 0 || original > 8) return;
        if (inventory.customMcSelectedHotbarSlot() == potSlot.get() - 1)
            inventory.customMcSetSelectedHotbarSlot(original);
    }

    private void reset() {
        remaining = 0;
        knownCooldown = cooldown.get();
    }
}
