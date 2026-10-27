package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.UUID;

/**
 * Optional synthetic-click gate using real mapped enemy hurt-time.
 * Manual vanilla mouse input is never intercepted.
 */
public final class Minecraft189HitSelectModule implements Module {
    public static final String ID = "combat.hitSelect";
    public static final String MAX_HURT_TICKS_SETTING_ID =
            ID + ".maxTargetHurtTicks";
    public static final int MAXIMUM_HURT_TICKS = 20;

    private final Setting<Integer> maxTargetHurtTicks =
            new Setting<Integer>(
                    MAX_HURT_TICKS_SETTING_ID, 1,
                    value -> value != null && value >= 0
                            && value <= MAXIMUM_HURT_TICKS,
                    SettingCodecs.INTEGER);
    private boolean enabled;

    @Override
    public String id() { return ID; }

    public Setting<Integer> maxTargetHurtTicksSetting() {
        return maxTargetHurtTicks;
    }

    @Override
    public synchronized void onEnable() { enabled = true; }

    @Override
    public synchronized void onDisable() { enabled = false; }

    synchronized boolean active() { return enabled; }

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
        if (selectedOriginalUuid == null || verifiedCrosshairIndex < 0
                || currentIds == null || !currentIds.available()
                || currentKinds == null || !currentKinds.available()
                || currentCombat == null || !currentCombat.available()
                || currentIds.entityCount() != currentKinds.entityCount()
                || currentIds.entityCount() != currentCombat.entityCount()
                || verifiedCrosshairIndex >= currentIds.entityCount()
                || !selectedOriginalUuid.equals(
                        currentIds.at(verifiedCrosshairIndex))
                || !currentKinds.player(verifiedCrosshairIndex)
                || currentKinds.localPlayer(verifiedCrosshairIndex)) {
            return false;
        }
        return permits(verifiedCrosshairIndex, currentCombat);
    }

    synchronized boolean permits(
            final int verifiedCrosshairIndex,
            final Minecraft189WorldEntityCombatState.Snapshot state) {
        if (!enabled) return true;
        // Source-fail-closed: a nearby player is never equivalent to the
        // exact player hit by the vanilla raycast.
        return verifiedCrosshairIndex >= 0
                && state != null && state.available()
                && state.known(verifiedCrosshairIndex)
                && state.alive(verifiedCrosshairIndex)
                && state.hurtTime(verifiedCrosshairIndex)
                        <= maxTargetHurtTicks.get();
    }
}
