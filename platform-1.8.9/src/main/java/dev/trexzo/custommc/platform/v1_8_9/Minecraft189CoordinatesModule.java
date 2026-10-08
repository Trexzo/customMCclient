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

public final class Minecraft189CoordinatesModule
        implements Module {
    public static final String ID =
            "render.coordinates";
    public static final String X_SETTING_ID =
            "render.coordinates.x";
    public static final String Y_SETTING_ID =
            "render.coordinates.y";
    public static final String SHOW_CHUNK_SETTING_ID =
            "render.coordinates.showChunk";
    public static final String RENDER_PASS_ID =
            "coordinates";

    private static final int PRIORITY = 125;
    private static final int TEXT_ARGB = 0xFFFFFFFF;

    private final Minecraft189PlayerPositionState positionState;
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
                    148,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> showChunk =
            new Setting<Boolean>(
                    SHOW_CHUNK_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189CoordinatesModule(
            final Minecraft189PlayerPositionState positionState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.positionState =
                Objects.requireNonNull(
                        positionState,
                        "positionState");
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

    public Setting<Boolean> showChunkSetting() {
        return showChunk;
    }

    static long chunkIndex(final double blockPosition) {
        return (long) Math.floor(blockPosition / 16.0D);
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "coordinates render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new CoordinatesRenderPass());
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

    private final class CoordinatesRenderPass
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
            final Minecraft189PlayerPositionState.Snapshot position =
                    positionState.snapshot();
            if (!position.available()) {
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
                        String.format(
                                Locale.ROOT,
                                "XYZ: %.1f / %.1f / %.1f",
                                position.x(),
                                position.y(),
                                position.z()),
                        TEXT_ARGB);
                if (showChunk.get().booleanValue()) {
                    hostCallbacks.drawText(
                            UiFonts.DEFAULT,
                            x.get().floatValue(),
                            y.get().floatValue() + 12.0F,
                            "CHUNK: " + chunkIndex(position.x())
                                    + " / " + chunkIndex(position.z()),
                            TEXT_ARGB);
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
