package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import java.util.concurrent.ThreadLocalRandom;

/** Jump once on a fresh local-player hurt-time edge, never on every hurt tick. */
public final class Minecraft189JumpResetModule implements Module {
    public static final String ID = "combat.jumpReset";
    public static final String COOLDOWN_ID = ID + ".cooldownTicks";
    public static final String FORWARD_ID = ID + ".requireForward";
    public static final String SNEAK_ID = ID + ".pauseWhileSneaking";
    public static final String CHANCE_ID = ID + ".chancePercent";
    public static final String SPRINT_ID = ID + ".requireSprint";
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
    private final Setting<Integer> chance = new Setting<Integer>(
            CHANCE_ID, 100, value -> value != null && value >= 0 && value <= 100,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> sprint = new Setting<Boolean>(
            SPRINT_ID, Boolean.FALSE, value -> value != null,
            SettingCodecs.BOOLEAN);
    private boolean enabled;
    private boolean previousAvailable;
    private int previousHurtTime;
    private int cooldownRemaining;
    private int scheduledCooldown = cooldown.get();
    // Native player.customMcJump() may synchronously reenter the same
    // hurt callback (including when it throws). Never let an in-flight
    // source action dispatch an extra jump or replay its hurt edge.
    private boolean nativeJumpDispatching;
    // Genuine mapped EntityPlayer.jump() may synchronously reenter module
    // lifecycle and live ClickGUI eligibility. One original hurt edge
    // must not report success for a different module activation.
    private long nativeJumpOwnerEpoch;

    @Override
    public String id() { return ID; }

    public Setting<Integer> cooldownSetting() { return cooldown; }
    public Setting<Boolean> requireForwardSetting() { return forward; }
    public Setting<Boolean> pauseWhileSneakingSetting() { return pauseSneak; }
    public Setting<Integer> chanceSetting() { return chance; }
    public Setting<Boolean> requireSprintSetting() { return sprint; }

    @Override
    public synchronized void onEnable() {
        ++nativeJumpOwnerEpoch;
        enabled = true;
        clear();
    }

    @Override
    public synchronized void onDisable() {
        ++nativeJumpOwnerEpoch;
        enabled = false;
        clear();
    }

    synchronized boolean apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final Minecraft189PlayerHurtTimeState.Snapshot hurt,
            final boolean forwardHeld,
            final boolean suspended) {
        // Existing deterministic module tests/legacy unindexed callers do
        // not fabricate original EntityPlayerSP getter evidence. Production
        // host supplies the distinct strict original-native admission.
        return apply(player, movement, hurt, forwardHeld, suspended,
                () -> true);
    }

    synchronized boolean apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final Minecraft189PlayerHurtTimeState.Snapshot hurt,
            final boolean forwardHeld,
            final boolean suspended,
            final java.util.function.BooleanSupplier nativeAdmission) {
        if (!enabled || player == null || movement == null
                || nativeAdmission == null
                || !movement.available() || hurt == null
                || !hurt.available()) {
            if (!nativeJumpDispatching) clear();
            return false;
        }
        if (nativeJumpDispatching) {
            // A reentrant actual hurt observation must still be consumed,
            // not banked for a later callback after native dispatch exits.
            previousAvailable = true;
            previousHurtTime = hurt.hurtTime();
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
                || (pauseSneak.get() && movement.sneaking())
                || (sprint.get() && !movement.sprinting())) {
            return false;
        }

        // Consume the eligible hurt edge even when chance declines it.
        // Sampling occurs only after all authoritative gates have passed.
        if (chance.get() != 100 && ThreadLocalRandom.current().nextInt(100) >= chance.get()) {
            return false;
        }
        // Reserve the synchronous module lane before invoking mapped
        // native getters: even a rejected original getter may reenter the
        // scheduler with a newer hurt snapshot. Never bank a second edge.
        final long originalEpoch = nativeJumpOwnerEpoch;
        final Object[] originalPolicy = nativeJumpPolicy();
        nativeJumpDispatching = true;
        try {
            // V365: the genuine original player, current hurt-time, ground/
            // sneak/sprint and physical forward source must STILL own this
            // edge at the last dispatch boundary. A stale source veto
            // consumes the edge but must NOT spend cooldown or fake a jump.
            if (!nativeAdmission.getAsBoolean()
                    || !nativeJumpOwned(originalEpoch, originalPolicy))
                return false;
            // Commit cooldown BEFORE the real native call: the source
            // may physically jump and then throw. An exception may not
            // immediately replay the same input under another module.
            cooldownRemaining = configuredCooldown;
            player.customMcJump();
            if (!nativeJumpOwned(originalEpoch, originalPolicy)) {
                // The physical original jump may already have happened.
                // Never label it a CURRENT-owner success or allow the
                // original GUI policy to create stale future cooldown.
                if (nativeJumpOwnerEpoch == originalEpoch)
                    revokeOldPolicyCooldown();
                return false;
            }
            return true;
        } catch (RuntimeException | Error nativeFailure) {
            // Preserve existing consume-before-jump semantics when the
            // real native method mutates player then throws. Only GUI
            // edits in this SAME activation revoke old synthetic timers.
            if (nativeJumpOwnerEpoch == originalEpoch
                    && !java.util.Arrays.equals(
                            originalPolicy, nativeJumpPolicy()))
                revokeOldPolicyCooldown();
            throw nativeFailure;
        } finally {
            nativeJumpDispatching = false;
        }
    }

    private Object[] nativeJumpPolicy() {
        return new Object[]{
                cooldown.get(), forward.get(), pauseSneak.get(),
                chance.get(), sprint.get()
        };
    }

    private boolean nativeJumpOwned(final long epoch,
            final Object[] originalPolicy) {
        return enabled && nativeJumpOwnerEpoch == epoch
                && java.util.Arrays.equals(
                        originalPolicy, nativeJumpPolicy());
    }

    private void revokeOldPolicyCooldown() {
        cooldownRemaining = 0;
        scheduledCooldown = cooldown.get();
    }

    synchronized boolean active() { return enabled; }

    private void clear() {
        previousAvailable = false;
        previousHurtTime = 0;
        cooldownRemaining = 0;
        scheduledCooldown = cooldown.get();
    }
}
