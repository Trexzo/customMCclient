package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;

public final class Minecraft189ArmorModule
        implements Module {
    public static final String ID =
            "render.armor";
    public static final String X_SETTING_ID =
            "render.armor.x";
    public static final String Y_SETTING_ID =
            "render.armor.y";
    public static final String RENDER_PASS_ID =
            "armor";

    private static final int PRIORITY = 129;
    private static final int TEXT_ARGB = 0xFFFFFFFF;

    private final Minecraft189PlayerArmorState armorState;
    private final RenderPipeline renderPipeline;
    private final LegacyUiHostCallbacks hostCallbacks;
    private final Setting<Integer> x =
            new Setting<Integer>(
                    X_SETTING_ID,
                    8,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> y =
            new Setting<Integer>(
                    Y_SETTING_ID,
                    212,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189ArmorModule(
            final Minecraft189PlayerArmorState armorState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.armorState =
                Objects.requireNonNull(
                        armorState,
                        "armorState");
        this.renderPipeline =
                Objects.requireNonNull(
                        renderPipeline,
                        "renderPipeline");
        this.hostCallbacks =
                Objects.requireNonNull(
                        hostCallbacks,
                        "hostCallbacks");
    }

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> xSetting() {
        return x;
    }

    public Setting<Integer> ySetting() {
        return y;
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "armor render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new ArmorRenderPass());
    }

    @Override
    public synchronized void onDisable() {
        if (renderRegistration == null) {
            return;
        }
        renderRegistration.close();
        renderRegistration = null;
    }

    synchronized boolean renderPassInstalled() {
        return renderRegistration != null
                && renderRegistration.active();
    }

    static String textFor(
            final Minecraft189PlayerArmorState.Snapshot armor) {
        Objects.requireNonNull(
                armor,
                "armor");
        if (!armor.hasDurabilityDetails()) {
            return "Armor: "
                    + armor.equippedCount()
                    + "/4 ["
                    + slot(
                            armor.helmet(),
                            "H")
                    + " "
                    + slot(
                            armor.chestplate(),
                            "C")
                    + " "
                    + slot(
                            armor.leggings(),
                            "L")
                    + " "
                    + slot(
                            armor.boots(),
                            "B")
                    + "]";
        }
        return "Armor: "
                + armor.equippedCount()
                + "/4 ["
                + slotWithDurability(
                        armor.helmet(),
                        "H",
                        armor.helmetDurability())
                + " | "
                + slotWithDurability(
                        armor.chestplate(),
                        "C",
                        armor.chestplateDurability())
                + " | "
                + slotWithDurability(
                        armor.leggings(),
                        "L",
                        armor.leggingsDurability())
                + " | "
                + slotWithDurability(
                        armor.boots(),
                        "B",
                        armor.bootsDurability())
                + "]";
    }

    private static String slot(
            final boolean equipped,
            final String label) {
        return equipped
                ? label
                : "-";
    }

    private static String slotWithDurability(
            final boolean equipped,
            final String label,
            final Minecraft189PlayerArmorState.SlotDurability durability) {
        if (!equipped) {
            return label + " -";
        }
        if (!durability.available()) {
            return label + " ?";
        }
        if (!durability.damageable()) {
            return label + " n/a";
        }
        return label
                + " "
                + durability.durabilityRemaining()
                + "/"
                + durability.maxDamage();
    }

    private final class ArmorRenderPass
            implements RenderPass {
        @Override
        public String id() {
            return RENDER_PASS_ID;
        }

        @Override
        public RenderStage stage() {
            return RenderStage.HUD;
        }

        @Override
        public int priority() {
            return PRIORITY;
        }

        @Override
        public void render(
                final RenderFrame frame) {
            Objects.requireNonNull(
                    frame,
                    "frame");
            final Minecraft189PlayerArmorState.Snapshot armor =
                    armorState.snapshot();
            if (!armor.available()) {
                return;
            }

            final UiViewport viewport =
                    new UiViewport(
                            hostCallbacks.framebufferWidth(),
                            hostCallbacks.framebufferHeight(),
                            hostCallbacks.uiScale());

            hostCallbacks.beginUi(viewport);
            RuntimeException failure = null;
            try {
                hostCallbacks.drawText(
                        UiFonts.DEFAULT,
                        x.get().floatValue(),
                        y.get().floatValue(),
                        textFor(
                                armor),
                        TEXT_ARGB);
            } catch (RuntimeException drawFailure) {
                failure = drawFailure;
                throw drawFailure;
            } finally {
                try {
                    hostCallbacks.endUi();
                } catch (RuntimeException closeFailure) {
                    if (failure != null) {
                        failure.addSuppressed(
                                closeFailure);
                    } else {
                        throw closeFailure;
                    }
                }
            }
        }
    }
}
