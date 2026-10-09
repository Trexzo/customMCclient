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

    private final Setting<Boolean> freezeHorizontal = new Setting<Boolean>(
            FREEZE_HORIZONTAL_SETTING_ID, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> freezeVertical = new Setting<Boolean>(
            FREEZE_VERTICAL_SETTING_ID, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;

    public Setting<Boolean> freezeHorizontalSetting() {
        return freezeHorizontal;
    }

    public Setting<Boolean> freezeVerticalSetting() {
        return freezeVertical;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player) {
        if (!active() || player == null) {
            return;
        }

        if (freezeHorizontal.get().booleanValue() && Double.compare(
                player.customMcMotionX(),
                0.0D) != 0) {
            player.customMcSetMotionX(
                    0.0D);
        }
        if (freezeVertical.get().booleanValue() && Double.compare(
                player.customMcMotionY(),
                0.0D) != 0) {
            player.customMcSetMotionY(
                    0.0D);
        }
        if (freezeHorizontal.get().booleanValue() && Double.compare(
                player.customMcMotionZ(),
                0.0D) != 0) {
            player.customMcSetMotionZ(
                    0.0D);
        }
    }

    synchronized boolean active() {
        // Both disabled means Freeze no longer owns motion. This prevents
        // an inert Freeze toggle from masking Flight or other motion owners.
        return enabled && (freezeHorizontal.get().booleanValue()
                || freezeVertical.get().booleanValue());
    }
}
