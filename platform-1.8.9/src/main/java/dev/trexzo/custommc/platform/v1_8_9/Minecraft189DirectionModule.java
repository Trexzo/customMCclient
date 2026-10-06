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

public final class Minecraft189DirectionModule
        implements Module {
    public static final String ID =
            "render.direction";
    public static final String X_SETTING_ID =
            "render.direction.x";
    public static final String Y_SETTING_ID =
            "render.direction.y";
    public static final String RENDER_PASS_ID =
            "direction";

    private static final int PRIORITY = 126;
    private static final int TEXT_ARGB = 0xFFFFFFFF;
    private static final String[] DIRECTIONS =
            new String[]{
                    "S",
                    "SW",
                    "W",
                    "NW",
                    "N",
                    "NE",
                    "E",
                    "SE"
            };

    private final Minecraft189PlayerRotationState rotationState;
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
                    180,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189DirectionModule(
            final Minecraft189PlayerRotationState rotationState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.rotationState =
                Objects.requireNonNull(
                        rotationState,
                        "rotationState");
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
                    "direction render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new DirectionRenderPass());
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

    private final class DirectionRenderPass
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
            final Minecraft189PlayerRotationState.Snapshot rotation =
                    rotationState.snapshot();
            if (!rotation.available()) {
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
                        textForYaw(
                                rotation.yaw()),
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

    static String textForYaw(
            final float yaw) {
        float normalized =
                yaw % 360.0F;
        if (normalized < 0.0F) {
            normalized += 360.0F;
        }

        final int directionIndex =
                ((int) Math.floor(
                        (normalized + 22.5F) / 45.0F))
                        % DIRECTIONS.length;
        int degrees =
                (int) Math.floor(
                        normalized + 0.5F);
        if (degrees == 360) {
            degrees = 0;
        }

        return "Direction: "
                + DIRECTIONS[directionIndex]
                + " ("
                + degrees
                + "\u00B0)";
    }
}
