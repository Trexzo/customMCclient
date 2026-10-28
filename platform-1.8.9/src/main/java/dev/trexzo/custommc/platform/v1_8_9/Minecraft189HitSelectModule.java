package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Optional synthetic-click gate using real mapped enemy hurt-time.
 * Manual vanilla mouse input is never intercepted.
 */
public final class Minecraft189HitSelectModule implements Module {
    public static final String ID = "combat.hitSelect";
    public static final String MAX_HURT_TICKS_SETTING_ID =
            ID + ".maxTargetHurtTicks";
    public static final String MIN_HURT_TICKS_SETTING_ID =
            ID + ".minTargetHurtTicks";
    public static final String REQUIRE_RECOVERY_SETTING_ID =
            ID + ".requireObservedRecovery";
    public static final int MAXIMUM_HURT_TICKS = 20;

    private enum RecoveryState { READY, WAIT_FOR_HURT, WAIT_FOR_READY }

    private final Setting<Integer> maxTargetHurtTicks =
            new Setting<Integer>(
                    MAX_HURT_TICKS_SETTING_ID, 1,
                    value -> value != null && value >= 0
                            && value <= MAXIMUM_HURT_TICKS,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> minTargetHurtTicks =
            new Setting<Integer>(
                    MIN_HURT_TICKS_SETTING_ID, 0,
                    value -> value != null && value >= 0
                            && value <= MAXIMUM_HURT_TICKS,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> requireObservedRecovery =
            new Setting<Boolean>(
                    REQUIRE_RECOVERY_SETTING_ID, Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private boolean enabled;
    // A real World loadedEntityList getter can synchronously disable this
    // module or revoke GUI/source ownership before the final native click.
    private long nativeOwnerEpoch;
    private UUID observedOriginalUuid;
    // Preserve each original player's own recovery latch across A/B target
    // changes and list reorder; a temporary switch must not rearm A.
    private final LinkedHashMap<UUID, RecoveryState> recoveryByUuid =
            new LinkedHashMap<UUID, RecoveryState>();
    private static final int MAX_TRACKED_OWNERS = 16;
    private int observedMinimum = -1;
    private int observedMaximum = -1;
    private boolean previousRecoveryEnabled;

    @Override
    public String id() { return ID; }

    public Setting<Integer> maxTargetHurtTicksSetting() {
        return maxTargetHurtTicks;
    }

    public Setting<Integer> minTargetHurtTicksSetting() {
        return minTargetHurtTicks;
    }

    public Setting<Boolean> requireObservedRecoverySetting() {
        return requireObservedRecovery;
    }

    @Override
    public synchronized void onEnable() {
        ++nativeOwnerEpoch;
        enabled = true;
        forgetRecovery();
    }

    @Override
    public synchronized void onDisable() {
        ++nativeOwnerEpoch;
        enabled = false;
        forgetRecovery();
    }

    // GUI focus, source World replacement and shutdown must never carry
    // a pending original-UUID recovery sequence into the next world.
    synchronized void suspendForGui() {
        ++nativeOwnerEpoch;
        forgetRecovery();
    }
    synchronized void suspendForSource() {
        ++nativeOwnerEpoch;
        forgetRecovery();
    }

    synchronized long nativeOwnerEpoch() { return nativeOwnerEpoch; }
    synchronized boolean active() { return enabled; }

    /**
     * Read-only per-original-ray state observation, called once for each
     * native indexed attack scheduler callback, INCLUDING ticks on which
     * the AutoClicker/Aura/Trigger scheduler does not schedule an attack.
     * No packet or click is emitted here.
     */
    synchronized void observeOriginalOwner(
            final int crosshairIndex,
            final Minecraft189WorldEntityUuidState.Snapshot ids,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat) {
        if (!enabled || !requireObservedRecovery.get()) {
            forgetRecovery();
            return;
        }
        final int min = minTargetHurtTicks.get();
        final int max = maxTargetHurtTicks.get();
        if (previousRecoveryEnabled != requireObservedRecovery.get()
                || observedMinimum != min || observedMaximum != max) {
            ++nativeOwnerEpoch;
            forgetRecovery();
            previousRecoveryEnabled = true;
            observedMinimum = min;
            observedMaximum = max;
        }
        // Unavailable native evidence cannot move the recovery state
        // forward, or authorize any synthetic hit.
        if (!originalOwnerValid(crosshairIndex, ids, kinds, combat))
            return;
        final UUID current = ids.at(crosshairIndex);
        observedOriginalUuid = current;
        if (!recoveryByUuid.containsKey(current)) {
            if (recoveryByUuid.size() >= MAX_TRACKED_OWNERS) {
                final UUID oldest = recoveryByUuid.keySet().iterator().next();
                recoveryByUuid.remove(oldest);
            }
            recoveryByUuid.put(current, RecoveryState.READY);
        }
        if (min > max) return; // Contradictory live settings fail shut.
        final int hurt = combat.hurtTime(crosshairIndex);
        final RecoveryState stage = recoveryByUuid.get(current);
        if (stage == RecoveryState.WAIT_FOR_HURT && hurt > max) {
            recoveryByUuid.put(current, RecoveryState.WAIT_FOR_READY);
        } else if (stage == RecoveryState.WAIT_FOR_READY
                && hurt >= min && hurt <= max) {
            recoveryByUuid.put(current, RecoveryState.READY);
        }
    }

    /** Only called after all other native scheduler/source guards pass. */
    synchronized void noteScheduledOriginalClick(final UUID originalUuid) {
        if (enabled && requireObservedRecovery.get()
                && originalUuid != null
                && originalUuid.equals(observedOriginalUuid)
                && recoveryByUuid.get(originalUuid) == RecoveryState.READY) {
            recoveryByUuid.put(originalUuid, RecoveryState.WAIT_FOR_HURT);
        }
    }

    private void forgetRecovery() {
        observedOriginalUuid = null;
        recoveryByUuid.clear();
        observedMinimum = -1;
        observedMaximum = -1;
        previousRecoveryEnabled = false;
    }

    private static boolean originalOwnerValid(final int index,
            final Minecraft189WorldEntityUuidState.Snapshot ids,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat) {
        return index >= 0 && ids != null && ids.available()
                && kinds != null && kinds.available()
                && combat != null && combat.available()
                && ids.entityCount() == kinds.entityCount()
                && ids.entityCount() == combat.entityCount()
                && index < ids.entityCount()
                && ids.at(index) != null
                && kinds.player(index) && !kinds.localPlayer(index)
                && combat.known(index) && combat.alive(index);
    }

    /**
     * V338: final original synthetic melee admission. An earlier indexed
     * hurt-time snapshot is insufficient when the actual World player list,
     * ray, or hurt window changes before native controller dispatch.
     *
     * Only the SAME source UUID in a current real-player kind slot may
     * consume this verdict. Unknown snapshots fail shut. No attack is
     * queued or replayed, and manual vanilla clicks remain unchanged.
     */
    synchronized boolean permitsOriginalOwner(
            final int verifiedCrosshairIndex,
            final UUID selectedOriginalUuid,
            final Minecraft189WorldEntityUuidState.Snapshot currentIds,
            final Minecraft189WorldEntityKindState.Snapshot currentKinds,
            final Minecraft189WorldEntityCombatState.Snapshot currentCombat) {
        if (!enabled) return true;
        if (selectedOriginalUuid == null
                || !originalOwnerValid(verifiedCrosshairIndex, currentIds,
                    currentKinds, currentCombat)
                // Keep V338/V342's explicit final-source veto even though
                // originalOwnerValid also checks the same real player kind.
                || currentKinds.localPlayer(verifiedCrosshairIndex)
                || !selectedOriginalUuid.equals(
                        currentIds.at(verifiedCrosshairIndex)))
            return false;
        if (!hurtWindowPermits(verifiedCrosshairIndex, currentCombat))
            return false;
        return !requireObservedRecovery.get()
                || (selectedOriginalUuid.equals(observedOriginalUuid)
                        && recoveryByUuid.get(selectedOriginalUuid)
                                == RecoveryState.READY
                        && observedMinimum == minTargetHurtTicks.get()
                        && observedMaximum == maxTargetHurtTicks.get());
    }

    synchronized boolean permits(
            final int verifiedCrosshairIndex,
            final Minecraft189WorldEntityCombatState.Snapshot state) {
        if (!enabled) return true;
        // The opt-in recovery mode requires the real original UUID-bound
        // intake above. An index-only legacy caller cannot bypass it.
        return !requireObservedRecovery.get()
                && hurtWindowPermits(verifiedCrosshairIndex, state);
    }

    private boolean hurtWindowPermits(final int index,
            final Minecraft189WorldEntityCombatState.Snapshot state) {
        return index >= 0 && state != null && state.available()
                && state.known(index) && state.alive(index)
                && minTargetHurtTicks.get() <= maxTargetHurtTicks.get()
                && state.hurtTime(index) >= minTargetHurtTicks.get()
                && state.hurtTime(index) <= maxTargetHurtTicks.get();
    }
}
