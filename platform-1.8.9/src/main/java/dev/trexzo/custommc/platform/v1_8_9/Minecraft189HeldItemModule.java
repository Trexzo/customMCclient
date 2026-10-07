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

public final class Minecraft189HeldItemModule
        implements Module {
    public static final String ID =
            "render.heldItem";
    public static final String X_SETTING_ID =
            "render.heldItem.x";
    public static final String Y_SETTING_ID =
            "render.heldItem.y";
    public static final String RENDER_PASS_ID =
            "held-item";

    private static final int PRIORITY = 135;
    private static final int TEXT_ARGB = 0xFFFFFFFF;

    private final Minecraft189HeldItemState heldItemState;
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
                    340,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189HeldItemModule(
            final Minecraft189HeldItemState heldItemState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.heldItemState =
                Objects.requireNonNull(
                        heldItemState,
                        "heldItemState");
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
                    "held-item render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new HeldItemRenderPass());
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
            final Minecraft189HeldItemState.Snapshot held) {
        Objects.requireNonNull(
                held,
                "held");
        if (held.damageable()) {
            return "Held: "
                    + held.displayName()
                    + " | Dur: "
                    + held.durabilityRemaining()
                    + "/"
                    + held.maxDamage();
        }
        if (held.stackSize() > 1) {
            return "Held: "
                    + held.displayName()
                    + " x"
                    + held.stackSize();
        }
        return "Held: " + held.displayName();
    }

    private final class HeldItemRenderPass
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
            final Minecraft189HeldItemState.Snapshot held =
                    heldItemState.snapshot();
            if (!held.available()) {
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
                                held),
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
