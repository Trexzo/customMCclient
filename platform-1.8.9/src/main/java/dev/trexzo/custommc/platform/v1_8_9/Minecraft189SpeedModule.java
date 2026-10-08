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

public final class Minecraft189SpeedModule
        implements Module {
    public static final String ID =
            "render.speed";
    public static final String X_SETTING_ID =
            "render.speed.x";
    public static final String Y_SETTING_ID =
            "render.speed.y";
    public static final String SHOW_PEAK_SETTING_ID =
            "render.speed.showPeak";
    public static final String BLOCKS_PER_TICK_SETTING_ID =
            "render.speed.blocksPerTick";
    public static final String RENDER_PASS_ID =
            "speed";

    private static final int PRIORITY = 127;
    private static final int TEXT_ARGB = 0xFFFFFFFF;

    private final Minecraft189MovementSpeedTracker speedTracker;
    private final RenderPipeline renderPipeline;
    private final LegacyUiHostCallbacks hostCallbacks;
    private final Setting<Integer> x =
            new Setting<Integer>(
                    X_SETTING_ID,
                    8,
                    value -> value >= 0 && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> y =
            new Setting<Integer>(
                    Y_SETTING_ID,
                    164,
                    value -> value >= 0 && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> showPeak =
            new Setting<Boolean>(
                    SHOW_PEAK_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> blocksPerTick =
            new Setting<Boolean>(
                    BLOCKS_PER_TICK_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189SpeedModule(
            final Minecraft189MovementSpeedTracker speedTracker,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.speedTracker =
                Objects.requireNonNull(
                        speedTracker,
                        "speedTracker");
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

    public Setting<Boolean> showPeakSetting() {
        return showPeak;
    }

    public Setting<Boolean> blocksPerTickSetting() {
        return blocksPerTick;
    }

    static String textFor(
            final Minecraft189MovementSpeedTracker.Snapshot speed,
            final boolean useBlocksPerTick,
            final boolean includePeak) {
        Objects.requireNonNull(speed, "speed");
        final double unitScale = useBlocksPerTick ? 1.0D / 20.0D : 1.0D;
        final String unit = useBlocksPerTick ? "BPT" : "BPS";
        final String current = String.format(
                Locale.ROOT, "Speed: %.2f %s",
                speed.blocksPerSecond() * unitScale, unit);
        return includePeak
                ? current + String.format(
                        Locale.ROOT, " | Peak: %.2f %s",
                        speed.peakBlocksPerSecond() * unitScale, unit)
                : current;
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "speed render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new SpeedRenderPass());
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

    private final class SpeedRenderPass
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
            final Minecraft189MovementSpeedTracker.Snapshot speed =
                    speedTracker.snapshot();
            if (!speed.available()) {
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
                                speed,
                                blocksPerTick.get().booleanValue(),
                                showPeak.get().booleanValue()),
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
