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

public final class Minecraft189HealthModule
        implements Module {
    public static final String ID =
            "render.health";
    public static final String X_SETTING_ID =
            "render.health.x";
    public static final String Y_SETTING_ID =
            "render.health.y";
    public static final String RENDER_PASS_ID =
            "health";

    private static final int PRIORITY = 128;
    private static final int TEXT_ARGB = 0xFFFFFFFF;

    private final Minecraft189PlayerHealthState healthState;
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
                    196,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189HealthModule(
            final Minecraft189PlayerHealthState healthState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.healthState =
                Objects.requireNonNull(
                        healthState,
                        "healthState");
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
                    "health render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new HealthRenderPass());
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

    private final class HealthRenderPass
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
            final Minecraft189PlayerHealthState.Snapshot health =
                    healthState.snapshot();
            if (!health.available()) {
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
                                "Health: %.1f / %.1f",
                                health.health(),
                                health.maxHealth()),
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
