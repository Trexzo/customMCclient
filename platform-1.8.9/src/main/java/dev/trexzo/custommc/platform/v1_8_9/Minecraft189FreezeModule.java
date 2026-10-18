package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189FreezeModule
        implements Module {
    public static final String ID =
            "movement.freeze";
    public static final String FREEZE_HORIZONTAL_SETTING_ID =
            "movement.freeze.horizontal";
    public static final String FREEZE_VERTICAL_SETTING_ID =
            "movement.freeze.vertical";
    public static final String PAUSE_WHILE_CLICKGUI_SETTING_ID =
            "movement.freeze.pauseWhileClickGui";

    private final Setting<Boolean> freezeHorizontal = new Setting<Boolean>(
            FREEZE_HORIZONTAL_SETTING_ID, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> freezeVertical = new Setting<Boolean>(
            FREEZE_VERTICAL_SETTING_ID, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    // Opt-in: legacy Freeze remains a hard axis owner even under ClickGUI.
    // Users may instead allow the original player to retain its own motion
    // while CustomMC's actual ClickGUI has input focus.
    private final Setting<Boolean> pauseWhileClickGui =
            new Setting<Boolean>(
                    PAUSE_WHILE_CLICKGUI_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // Real native X/Y/Z getters can reenter module/settings callbacks.
    private long nativeReadEpoch;

    public Setting<Boolean> freezeHorizontalSetting() {
        return freezeHorizontal;
    }

    public Setting<Boolean> freezeVerticalSetting() {
        return freezeVertical;
    }

    public Setting<Boolean> pauseWhileClickGuiSetting() {
        return pauseWhileClickGui;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        nativeReadEpoch++;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeReadEpoch++;
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player) {
        apply(player, false);
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final boolean actualClickGuiOpen) {
        // When paused, keep Freeze's existing high-priority ownership in
        // HostRuntime so lower-priority motion modules cannot write through
        // it. This callback yields only the original native motion fields.
        if (!active() || player == null
                || (actualClickGuiOpen
                    && pauseWhileClickGui.get().booleanValue())) {
            return;
        }

        final long epoch = nativeReadEpoch;
        final boolean horizontal = freezeHorizontal.get().booleanValue();
        final boolean vertical = freezeVertical.get().booleanValue();
        final boolean guiPause = pauseWhileClickGui.get().booleanValue();
        if (horizontal) {
            final double x = player.customMcMotionX();
            if (!nativeReadOwned(epoch, horizontal, vertical, guiPause,
                    actualClickGuiOpen)) {
                return;
            }
            if (Double.compare(x, 0.0D) != 0) {
                player.customMcSetMotionX(0.0D);
                if (!nativeReadOwned(epoch, horizontal, vertical, guiPause,
                        actualClickGuiOpen)) {
                    return;
                }
            }
        }
        if (vertical) {
            final double y = player.customMcMotionY();
            if (!nativeReadOwned(epoch, horizontal, vertical, guiPause,
                    actualClickGuiOpen)) {
                return;
            }
            if (Double.compare(y, 0.0D) != 0) {
                player.customMcSetMotionY(0.0D);
                if (!nativeReadOwned(epoch, horizontal, vertical, guiPause,
                        actualClickGuiOpen)) {
                    return;
                }
            }
        }
        if (horizontal) {
            final double z = player.customMcMotionZ();
            if (!nativeReadOwned(epoch, horizontal, vertical, guiPause,
                    actualClickGuiOpen)) {
                return;
            }
            if (Double.compare(z, 0.0D) != 0) {
                player.customMcSetMotionZ(0.0D);
            }
        }
    }

    private boolean nativeReadOwned(final long epoch,
            final boolean horizontal, final boolean vertical,
            final boolean guiPause, final boolean actualClickGuiOpen) {
        return enabled && nativeReadEpoch == epoch
                && (horizontal || vertical)
                && freezeHorizontal.get().booleanValue() == horizontal
                && freezeVertical.get().booleanValue() == vertical
                && pauseWhileClickGui.get().booleanValue() == guiPause
                && !(actualClickGuiOpen && guiPause);
    }

    synchronized boolean active() {
        // Both disabled means Freeze no longer owns motion. This prevents
        // an inert Freeze toggle from masking Flight or other motion owners.
        return enabled && (freezeHorizontal.get().booleanValue()
                || freezeVertical.get().booleanValue());
    }
}
