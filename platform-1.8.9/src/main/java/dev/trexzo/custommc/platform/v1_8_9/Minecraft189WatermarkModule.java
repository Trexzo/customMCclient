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

public final class Minecraft189WatermarkModule
        implements Module {
    public static final String ID =
            "render.watermark";
    public static final String TEXT_SETTING_ID =
            "render.watermark.text";
    public static final String X_SETTING_ID =
            "render.watermark.x";
    public static final String Y_SETTING_ID =
            "render.watermark.y";
    public static final String RENDER_PASS_ID =
            "watermark";

    private static final int PRIORITY = 100;
    private static final int TEXT_ARGB = 0xFFFFFFFF;

    private final RenderPipeline renderPipeline;
    private final LegacyUiHostCallbacks hostCallbacks;
    private final Setting<String> text =
            new Setting<String>(
                    TEXT_SETTING_ID,
                    "CustomMC",
                    value -> !value.trim().isEmpty()
                            && value.length() <= 32,
                    SettingCodecs.STRING);
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
                    8,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189WatermarkModule(
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
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

    public Setting<String> textSetting() {
        return text;
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
                    "watermark render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new WatermarkRenderPass());
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

    private final class WatermarkRenderPass
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

            final UiViewport viewport =
                    new UiViewport(
                            hostCallbacks.framebufferWidth(),
                            hostCallbacks.framebufferHeight(),
                            hostCallbacks.uiScale());

            // S39: treat X/Y as user-owned saved preferences, but recover
            // visible insets on a smaller game window or different GUI scale.
            // Original default (8,8) and short text remain pixel-identical.
            final float logicalWidth = viewport.logicalWidth();
            final float logicalHeight = viewport.logicalHeight();
            final float left = Minecraft189ArrayListLayout.safeLeft(
                    x.get().floatValue(), logicalWidth);
            final float top = Minecraft189ArrayListLayout.safeTop(
                    y.get().floatValue(), logicalHeight);
            hostCallbacks.beginUi(viewport);
            RuntimeException failure = null;
            try {
                final float width = Math.max(0.0F,
                        logicalWidth - left - 4.0F);
                final String visible = Minecraft189ArrayListLayout.fitLabel(
                        text.get(), width,
                        label -> {
                            final int measured = hostCallbacks.measureText(label);
                            return measured >= 0 ? measured
                                    : Minecraft189HudTextMetrics.width(label);
                        });
                if (!visible.isEmpty() && width > 0.0F) {
                    // Clipping prevents native glyph shadows or unusual
                    // Unicode metrics from drawing beyond the viewport.
                    hostCallbacks.pushClip(left, Math.max(0.0F, top - 1.0F),
                            width, Math.min(11.0F, logicalHeight - top));
                    try {
                        hostCallbacks.drawText(UiFonts.DEFAULT,
                                left, top, visible, TEXT_ARGB);
                    } finally {
                        hostCallbacks.popClip();
                    }
                }
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
