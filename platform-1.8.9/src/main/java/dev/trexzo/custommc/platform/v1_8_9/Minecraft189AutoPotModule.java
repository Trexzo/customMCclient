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
    public static final String AUTO_DETECT_SLOT = ID + ".autoDetectSlot";
    public static final String PREFER_IMMEDIATE_HEALING =
            ID + ".preferImmediateHealing";
    public static final String CONSERVE_INSTANT_HEALING_II =
            ID + ".conserveInstantHealingII";
    public static final String HEALTH_THRESHOLD = ID + ".healthPercent";
    public static final String AVOID_ACTIVE_REGENERATION =
            ID + ".avoidActiveRegeneration";
    public static final String AIM_DOWN_FOR_SPLASH =
            ID + ".aimDownForSplash";
    public static final String COOLDOWN_TICKS = ID + ".cooldownTicks";
    public static final String REQUIRE_TARGET = ID + ".requireCombatTarget";
    private final Setting<Integer> potSlot = new Setting<Integer>(
            POT_SLOT, 3, x -> x != null && x >= 1 && x <= 9,
            SettingCodecs.INTEGER);
    private final Setting<Double> healthThreshold = new Setting<Double>(
            HEALTH_THRESHOLD, 35.0D,
            x -> x != null && Double.isFinite(x) && x >= 1.0D && x <= 99.0D,
            SettingCodecs.DOUBLE);
    private final Setting<Boolean> autoDetectSlot = new Setting<Boolean>(
            AUTO_DETECT_SLOT, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    // Defaults off: preserve V131 already-held / first matching slot behavior.
    private final Setting<Boolean> preferImmediateHealing = new Setting<Boolean>(
            PREFER_IMMEDIATE_HEALING, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    // Default off. With source Auto Detect + Prefer Instant Healing,
    // prefer Instant Health I when the original player lacks <=4 HP.
    private final Setting<Boolean> conserveInstantHealingII =
            new Setting<Boolean>(CONSERVE_INSTANT_HEALING_II, Boolean.FALSE,
                    v -> v != null, SettingCodecs.BOOLEAN);
    // Default off: preserve legacy potion use even with an active effect.
    private final Setting<Boolean> avoidActiveRegeneration = new Setting<Boolean>(
            AVOID_ACTIVE_REGENERATION, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    // Opt-in only: existing native right-click pitch remains default.
    private final Setting<Boolean> aimDownForSplash = new Setting<Boolean>(
            AIM_DOWN_FOR_SPLASH, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
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
    public Setting<Boolean> autoDetectSlotSetting() { return autoDetectSlot; }
    public Setting<Boolean> preferImmediateHealingSetting() {
        return preferImmediateHealing;
    }
    public Setting<Boolean> conserveInstantHealingIISetting() {
        return conserveInstantHealingII;
    }
    public Setting<Boolean> avoidActiveRegenerationSetting() {
        return avoidActiveRegeneration;
    }
    public Setting<Boolean> aimDownForSplashSetting() {
        return aimDownForSplash;
    }
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
        // A real mapped use-item attempt is required to begin cooldown.
        // Unknown/wrong item evidence must never spend cooldown credit.
        return true;
    }

    synchronized void commitUseAttempt() {
        if (enabled) {
            remaining = cooldown.get();
            knownCooldown = cooldown.get();
        }
    }

    // Synchronous native hotbar lease. Persist the ACTUAL slot chosen for
    // this attempt: a UI setting change before finally/restore must not
    // strand the selected rod/potion or overwrite another actor's slot.
    private Minecraft189InventoryHotbarControl selectedInventory;
    private int selectedSlot = -1;
    private int previousSlot = -1;

    synchronized int selectSlot(final Minecraft189InventoryHotbarControl inventory) {
        return selectSlot(inventory, potSlot.get().intValue() - 1);
    }

    /** Exact candidate is captured for one native currentItem lease. */
    synchronized int selectSlot(final Minecraft189InventoryHotbarControl inventory,
            final int target) {
        if (!enabled || inventory == null || selectedInventory != null
                || target < 0 || target > 8) return -1;
        final int original = inventory.customMcSelectedHotbarSlot();
        if (original < 0 || original > 8) return -1;
        // Record exact slot ownership before invoking the native setter.
        // A reentrant GUI setting change never changes this lease.
        selectedInventory = inventory;
        selectedSlot = target;
        previousSlot = original;
        try {
            inventory.customMcSetSelectedHotbarSlot(target);
            // An original setter may silently ignore the request,
            // reenter a user hotbar change, or disable the module.
            // Selection must never publish a successful lease unless
            // the actual source currentItem and module still agree.
            if (!enabled || inventory.customMcSelectedHotbarSlot() != target) {
                restoreSlot(inventory, original);
                return -1;
            }
        } catch (RuntimeException failure) {
            try {
                restoreSlot(inventory, original);
            } catch (RuntimeException rollback) {
                failure.addSuppressed(rollback);
            }
            throw failure;
        }
        return original;
    }


    /**
     * The real hotbar slot must still be the one this module selected for
     * this exact attempt. A reentrant user action can change currentItem
     * during the native held-item read; that cannot authorize a synthetic
     * use of an unrelated item, even if its type happens to match.
     */
    synchronized boolean ownsSelectedSlot(
            final Minecraft189InventoryHotbarControl inventory,
            final int original) {
        return enabled && inventory != null && inventory == selectedInventory
                && original == previousSlot && selectedSlot >= 0
                && inventory.customMcSelectedHotbarSlot() == selectedSlot;
    }

    synchronized void restoreSlot(final Minecraft189InventoryHotbarControl inventory,
            final int original) {
        if (inventory == null || inventory != selectedInventory
                || original != previousSlot || original < 0 || original > 8)
            return;
        try {
            // Do not overwrite a real user/another module slot change.
            // Compare the original target captured at select time, NOT the
            // mutable ClickGUI configured target slot.
            if (inventory.customMcSelectedHotbarSlot() == selectedSlot
                    && selectedSlot != original)
                inventory.customMcSetSelectedHotbarSlot(original);
        } finally {
            selectedInventory = null;
            selectedSlot = -1;
            previousSlot = -1;
        }
    }

    private void reset() {
        remaining = 0;
        knownCooldown = cooldown.get();
    }
}
