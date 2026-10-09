package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/** Optional AntiBot network-tab evidence gate; never guesses bot identity. */
public final class Minecraft189AntiBotModule implements Module {
    public static final String ID = "combat.antiBot";
    public static final String ALLOW_UNKNOWN = ID + ".allowUnknownClientPlayer";
    private final Setting<Boolean> allowUnknown = new Setting<Boolean>(
            ALLOW_UNKNOWN, Boolean.FALSE, value -> value != null,
            SettingCodecs.BOOLEAN);
    private boolean enabled;

    @Override public String id() { return ID; }
    @Override public synchronized void onEnable() { enabled = true; }
    @Override public synchronized void onDisable() { enabled = false; }
    public Setting<Boolean> allowUnknownSetting() { return allowUnknown; }
    synchronized boolean active() { return enabled; }

    synchronized boolean permits(
            final int candidateIndex,
            final Minecraft189WorldEntityCombatState.Snapshot combat) {
        if (!enabled) return true;
        if (candidateIndex < 0 || combat == null || !combat.available()
                || !combat.alive(candidateIndex)) return false;
        if (!combat.networkInfoKnown(candidateIndex)) {
            // Unknown raw EntityPlayer types are not proof of a tab-listed user.
            return allowUnknown.get();
        }
        return combat.networkInfoPresent(candidateIndex);
    }
}
