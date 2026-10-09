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

public final class Minecraft189HurtTimeModule
        implements Module {
    public static final String ID =
            "render.hurtTime";
    public static final String X_SETTING_ID =
            "render.hurtTime.x";
    public static final String Y_SETTING_ID =
            "render.hurtTime.y";
    public static final String RENDER_PASS_ID =
            "hurtTime";
    public static final String SHOW_METER_SETTING_ID =
            "render.hurtTime.showMeter";
    public static final String METER_MAX_SETTING_ID =
            "render.hurtTime.meterMax";
    public static final int DEFAULT_METER_MAX = 10;
    public static final int MAXIMUM_METER_MAX = 40;

    private static final int PRIORITY = 129;
    private static final int TEXT_ARGB = 0xFFFFFFFF;
    private static final int METER_TRACK_ARGB = 0xFF303D4A;
    private static final int METER_FILL_ARGB = 0xFFFFB65C;
    private static final float METER_WIDTH = 84.0F;

    private final Minecraft189PlayerHurtTimeState hurtTimeState;
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
                    214,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> showMeter = new Setting<Boolean>(
            SHOW_METER_SETTING_ID, Boolean.FALSE, value -> value != null,
            SettingCodecs.BOOLEAN);
    private final Setting<Integer> meterMax = new Setting<Integer>(
            METER_MAX_SETTING_ID, DEFAULT_METER_MAX,
            value -> value != null && value >= 1
                    && value <= MAXIMUM_METER_MAX,
            SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189HurtTimeModule(
            final Minecraft189PlayerHurtTimeState hurtTimeState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.hurtTimeState =
                Objects.requireNonNull(
                        hurtTimeState,
                        "hurtTimeState");
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

    public Setting<Boolean> showMeterSetting() {
        return showMeter;
    }

    public Setting<Integer> meterMaxSetting() {
        return meterMax;
    }

    static float meterWidthFor(final int hurtTime, final int maximum) {
        if (maximum < 1 || maximum > MAXIMUM_METER_MAX) {
            return 0.0F;
        }
        // Display only the mapped local-player counter, not presumed
        // damage immunity, server tick timing, or a predicted hit.
        return (float) (METER_WIDTH * Math.min(1.0D,
                Math.max(0.0D, (double) hurtTime / maximum)));
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "hurt-time render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new HurtTimeRenderPass());
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

    private final class HurtTimeRenderPass
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
            final Minecraft189PlayerHurtTimeState.Snapshot hurtTime =
                    hurtTimeState.snapshot();
            if (!hurtTime.available()) {
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
                        "Hurt Time: " + hurtTime.hurtTime(),
                        TEXT_ARGB);
                if (showMeter.get().booleanValue()) {
                    final float left = x.get().floatValue();
                    final float top = y.get().floatValue() + 12.0F;
                    final float fill = meterWidthFor(hurtTime.hurtTime(),
                            meterMax.get().intValue());
                    hostCallbacks.fillRect(left, top, METER_WIDTH, 4.0F,
                            METER_TRACK_ARGB);
                    if (fill > 0.0F) {
                        hostCallbacks.fillRect(left, top, fill, 4.0F,
                                METER_FILL_ARGB);
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
