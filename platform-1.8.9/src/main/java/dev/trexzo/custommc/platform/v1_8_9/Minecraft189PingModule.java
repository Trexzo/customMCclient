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

import java.util.Locale;
import java.util.Objects;

public final class Minecraft189PingModule
        implements Module {
    public static final String ID =
            "render.ping";
    public static final String X_SETTING_ID =
            "render.ping.x";
    public static final String Y_SETTING_ID =
            "render.ping.y";
    public static final String RENDER_PASS_ID =
            "ping";
    public static final String COLOR_BY_LATENCY_SETTING_ID =
            "render.ping.colorByLatency";

    private static final int PRIORITY = 133;
    private static final int TEXT_ARGB = 0xFFFFFFFF;

    private final Minecraft189PlayerPingState pingState;
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
                    308,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> colorByLatency =
            new Setting<Boolean>(
                    COLOR_BY_LATENCY_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189PingModule(
            final Minecraft189PlayerPingState pingState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.pingState =
                Objects.requireNonNull(
                        pingState,
                        "pingState");
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

    public Setting<Boolean> colorByLatencySetting() {
        return colorByLatency;
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "ping render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new PingRenderPass());
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
            final Minecraft189PlayerPingState.Snapshot ping) {
        Objects.requireNonNull(
                ping,
                "ping");
        return String.format(
                Locale.ROOT,
                "Ping: %d ms",
                ping.milliseconds());
    }

    static int colorFor(final int milliseconds, final boolean enabled) {
        if (!enabled) {
            return TEXT_ARGB;
        }
        if (milliseconds < 100) {
            return 0xFF55FF55;
        }
        if (milliseconds < 200) {
            return 0xFFFFAA00;
        }
        return 0xFFFF5555;
    }

    private final class PingRenderPass
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
            final Minecraft189PlayerPingState.Snapshot ping =
                    pingState.snapshot();
            if (!ping.available()) {
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
                                ping),
                        colorFor(ping.milliseconds(),
                                colorByLatency.get().booleanValue()));
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
