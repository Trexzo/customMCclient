package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Explicit configured-slot AutoRod; never guesses which item is a rod.
 * A rod attempt owns exactly one synthetic use-item action instead of attack.
 */
public final class Minecraft189AutoRodModule implements Module {
    public static final String ID = "combat.autoRod";
    public static final String ROD_SLOT = ID + ".rodSlot";
    public static final String AUTO_DETECT_SLOT = ID + ".autoDetectSlot";
    public static final String PREFER_DURABLE_ROD = ID + ".preferDurableRod";
    public static final String PROTECT_ROD_DURABILITY = ID + ".protectRodDurability";
    public static final String MIN_REMAINING_DURABILITY = ID + ".minRemainingDurability";
    public static final String PRESERVE_ACTIVE_HOOK = ID + ".preserveActiveHook";
    public static final String COOLDOWN_TICKS = ID + ".cooldownTicks";
    public static final String REQUIRE_HOLD = ID + ".requireAttackHeld";
    public static final String PAUSE_RIGHT = ID + ".pauseWhileRightClicking";
    private final Setting<Integer> rodSlot = new Setting<Integer>(
            ROD_SLOT, 2, x -> x != null && x >= 1 && x <= 9,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> autoDetectSlot = new Setting<Boolean>(
            AUTO_DETECT_SLOT, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    // Off by default to preserve V131's source held-first policy.
    private final Setting<Boolean> preferDurableRod = new Setting<Boolean>(
            PREFER_DURABLE_ROD, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    // Off by default to preserve prior original source cast/reel behavior.
    // Off by default; no change to legacy source rod choice.
    private final Setting<Boolean> protectRodDurability = new Setting<Boolean>(
            PROTECT_ROD_DURABILITY, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    private final Setting<Integer> minimumRemainingDurability =
            new Setting<Integer>(MIN_REMAINING_DURABILITY, Integer.valueOf(3),
                    v -> v != null && v >= 1 && v <= 64,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> preserveActiveHook = new Setting<Boolean>(
            PRESERVE_ACTIVE_HOOK, Boolean.FALSE, v -> v != null,
            SettingCodecs.BOOLEAN);
    private final Setting<Integer> cooldown = new Setting<Integer>(
            COOLDOWN_TICKS, 30, x -> x != null && x >= 1 && x <= 200,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> requireHold = new Setting<Boolean>(
            REQUIRE_HOLD, Boolean.TRUE, x -> x != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseRight = new Setting<Boolean>(
            PAUSE_RIGHT, Boolean.TRUE, x -> x != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    private int remaining;
    private int activeCooldown;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() {
        enabled = true;
        remaining = 0;
        activeCooldown = cooldown.get();
    }
    @Override public synchronized void onDisable() {
        enabled = false;
        suspend();
    }
    public Setting<Integer> rodSlotSetting() { return rodSlot; }
    public Setting<Integer> cooldownTicksSetting() { return cooldown; }
    public Setting<Boolean> autoDetectSlotSetting() { return autoDetectSlot; }
    public Setting<Boolean> preferDurableRodSetting() { return preferDurableRod; }
    public Setting<Boolean> protectRodDurabilitySetting() {
        return protectRodDurability;
    }
    public Setting<Integer> minimumRemainingDurabilitySetting() {
        return minimumRemainingDurability;
    }
    public Setting<Boolean> preserveActiveHookSetting() { return preserveActiveHook; }
    public Setting<Boolean> requireHoldSetting() { return requireHold; }
    public Setting<Boolean> pauseRightSetting() { return pauseRight; }
    synchronized boolean active() { return enabled; }

    synchronized void suspend() { remaining = 0; activeCooldown = cooldown.get(); }

    synchronized boolean shouldUse(final boolean confirmedPlayer,
            final boolean leftHeld, final boolean rightHeld,
            final boolean guiOpen, final boolean hasInventory) {
        if (!enabled || guiOpen || !confirmedPlayer || !hasInventory
                || (requireHold.get() && !leftHeld)
                || (pauseRight.get() && rightHeld)) {
            suspend();
            return false;
        }
        if (activeCooldown != cooldown.get()) {
            remaining = 0;
            activeCooldown = cooldown.get();
        }
        if (remaining > 0) {
            remaining--;
            return false;
        }
        // Admission does not commit cooldown: item verification may still
        // reject the configured slot, or native use could throw.
        return true;
    }

    synchronized void commitUseAttempt() {
        if (enabled) {
            remaining = cooldown.get();
            activeCooldown = cooldown.get();
        }
    }

    /** Selects the user-configured slot for one synchronous native right click. */
    // Synchronous native hotbar lease. Persist the ACTUAL slot chosen for
    // this attempt: a UI setting change before finally/restore must not
    // strand the selected rod/potion or overwrite another actor's slot.
    private Minecraft189InventoryHotbarControl selectedInventory;
    private int selectedSlot = -1;
    private int previousSlot = -1;
    private boolean capturedRodProtection;
    private int capturedRodMinimum;
    // The in-flight native right click belongs to one explicit set of
    // persisted GUI settings, not only an old selected hotbar index.
    private int capturedConfiguredSlot;
    private boolean capturedAutoDetect;
    private boolean capturedDurabilityPriority;
    private boolean capturedPreserveHook;
    private boolean capturedRequireHold;
    private boolean capturedPauseRight;
    private int capturedCooldown;
    // Actual source ItemStack reference accepted after native type/count
    // checks. Another same-class rod arriving in the slot is not this cast.
    private Minecraft189ItemStackAccess verifiedSelectedItem;

    synchronized int selectSlot(final Minecraft189InventoryHotbarControl inventory) {
        return selectSlot(inventory, rodSlot.get().intValue() - 1);
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
        capturedRodProtection = protectRodDurability.get().booleanValue();
        capturedRodMinimum = minimumRemainingDurability.get().intValue();
        capturedConfiguredSlot = rodSlot.get().intValue();
        capturedAutoDetect = autoDetectSlot.get().booleanValue();
        capturedDurabilityPriority = preferDurableRod.get().booleanValue();
        capturedPreserveHook = preserveActiveHook.get().booleanValue();
        capturedRequireHold = requireHold.get().booleanValue();
        capturedPauseRight = pauseRight.get().booleanValue();
        capturedCooldown = cooldown.get().intValue();
        verifiedSelectedItem = null;
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
                && capturedRodProtection == protectRodDurability.get()
                && capturedRodMinimum == minimumRemainingDurability.get()
                && capturedConfiguredSlot == rodSlot.get()
                && capturedAutoDetect == autoDetectSlot.get()
                && capturedDurabilityPriority == preferDurableRod.get()
                && capturedPreserveHook == preserveActiveHook.get()
                && capturedRequireHold == requireHold.get()
                && capturedPauseRight == pauseRight.get()
                && capturedCooldown == cooldown.get()
                && original == previousSlot && selectedSlot >= 0
                && inventory.customMcSelectedHotbarSlot() == selectedSlot;
    }

    /**
     * Pin the actual original ItemStack only AFTER the source-class and
     * hotbar-membership verifier has approved it. A second module or
     * inventory sync replacing it with another valid rod revokes this cast.
     */
    synchronized boolean bindVerifiedItem(
            final Minecraft189InventoryHotbarControl inventory,
            final int original, final Minecraft189ItemStackAccess item) {
        if (item == null || verifiedSelectedItem != null
                || !ownsSelectedSlot(inventory, original))
            return false;
        verifiedSelectedItem = item;
        return ownsSelectedSlot(inventory, original);
    }

    synchronized boolean ownsVerifiedItem(
            final Minecraft189InventoryHotbarControl inventory,
            final int original, final Minecraft189ItemStackAccess item) {
        return item != null && verifiedSelectedItem == item
                && ownsSelectedSlot(inventory, original);
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
            capturedRodProtection = false;
            capturedRodMinimum = 0;
            capturedConfiguredSlot = 0;
            capturedAutoDetect = false;
            capturedDurabilityPriority = false;
            capturedPreserveHook = false;
            capturedRequireHold = false;
            capturedPauseRight = false;
            capturedCooldown = 0;
            verifiedSelectedItem = null;
        }
    }
}
