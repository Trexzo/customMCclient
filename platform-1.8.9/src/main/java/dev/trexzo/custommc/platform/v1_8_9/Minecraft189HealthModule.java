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
    public static final String SHOW_PERCENT_SETTING_ID =
            "render.health.showPercent";
    public static final String SHOW_BAR_SETTING_ID =
            "render.health.showBar";
    public static final String BAR_WIDTH_SETTING_ID =
            "render.health.barWidth";
    public static final int DEFAULT_BAR_WIDTH = 120;
    public static final int MINIMUM_BAR_WIDTH = 40;
    public static final int MAXIMUM_BAR_WIDTH = 240;
    public static final String LOW_HEALTH_ALERT_SETTING_ID =
            "render.health.lowHealthAlert";
    public static final String LOW_HEALTH_THRESHOLD_SETTING_ID =
            "render.health.lowHealthThreshold";
    public static final String RENDER_PASS_ID =
            "health";

    private static final int PRIORITY = 128;
    private static final int TEXT_ARGB = 0xFFFFFFFF;
    private static final int LOW_HEALTH_ARGB = 0xFFFF6969;
    private static final int BAR_TRACK_ARGB = 0xBB26303A;
    private static final int BAR_FILL_ARGB = 0xFF70C9E8;
    private static final float BAR_HEIGHT = 4.0F;

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
    private final Setting<Boolean> showPercent =
            new Setting<Boolean>(
                    SHOW_PERCENT_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> showBar =
            new Setting<Boolean>(
                    SHOW_BAR_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> barWidth = new Setting<Integer>(
            BAR_WIDTH_SETTING_ID, DEFAULT_BAR_WIDTH,
            value -> value != null && value >= MINIMUM_BAR_WIDTH
                    && value <= MAXIMUM_BAR_WIDTH,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> lowHealthAlert =
            new Setting<Boolean>(
                    LOW_HEALTH_ALERT_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> lowHealthThreshold =
            new Setting<Integer>(
                    LOW_HEALTH_THRESHOLD_SETTING_ID,
                    30,
                    value -> value != null && value >= 1 && value <= 100,
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

    public Setting<Boolean> showPercentSetting() {
        return showPercent;
    }

    public Setting<Boolean> showBarSetting() {
        return showBar;
    }

    public Setting<Integer> barWidthSetting() {
        return barWidth;
    }

    public Setting<Boolean> lowHealthAlertSetting() {
        return lowHealthAlert;
    }

    public Setting<Integer> lowHealthThresholdSetting() {
        return lowHealthThreshold;
    }

    static String textFor(
            final Minecraft189PlayerHealthState.Snapshot health,
            final boolean includePercent) {
        Objects.requireNonNull(health, "health");
        final String value = String.format(
                Locale.ROOT,
                "Health: %.1f / %.1f",
                health.health(),
                health.maxHealth());
        return includePercent
                ? value + String.format(
                        Locale.ROOT, " (%.0f%%)",
                        (double) health.health() * 100.0D / health.maxHealth())
                : value;
    }

    static float healthBarFraction(
            final Minecraft189PlayerHealthState.Snapshot health) {
        if (health == null || !health.available()
                || !Float.isFinite(health.health())
                || !Float.isFinite(health.maxHealth())
                || health.maxHealth() <= 0.0F) {
            return 0.0F;
        }
        // Source health is finite, but may be outside [0, max] during
        // client transitions. Avoid oversized or negative rectangles.
        return (float) Math.max(0.0D, Math.min(1.0D,
                (double) health.health() / health.maxHealth()));
    }

    static boolean lowHealth(
            final Minecraft189PlayerHealthState.Snapshot health,
            final int thresholdPercent) {
        Objects.requireNonNull(health, "health");
        return health.available()
                && (double) health.health() * 100.0D
                        <= (double) health.maxHealth() * thresholdPercent;
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
                final float left = x.get().floatValue();
                final float top = y.get().floatValue();
                final boolean warn = lowHealthAlert.get().booleanValue()
                        && lowHealth(health,
                                lowHealthThreshold.get().intValue());
                hostCallbacks.drawText(
                        UiFonts.DEFAULT,
                        left, top,
                        textFor(health, showPercent.get().booleanValue()),
                        warn ? LOW_HEALTH_ARGB : TEXT_ARGB);
                if (showBar.get().booleanValue()) {
                    final float barY = top + 12.0F;
                    final float fraction = healthBarFraction(health);
                    final float width = barWidth.get().floatValue();
                    hostCallbacks.fillRect(left, barY,
                            width, BAR_HEIGHT, BAR_TRACK_ARGB);
                    if (fraction > 0.0F) {
                        hostCallbacks.fillRect(left, barY,
                                width * fraction, BAR_HEIGHT,
                                warn ? LOW_HEALTH_ARGB : BAR_FILL_ARGB);
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
