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

public final class Minecraft189HungerModule
        implements Module {
    public static final String ID =
            "render.hunger";
    public static final String X_SETTING_ID =
            "render.hunger.x";
    public static final String Y_SETTING_ID =
            "render.hunger.y";
    public static final String SHOW_METERS_SETTING_ID =
            "render.hunger.showMeters";
    public static final String LOW_FOOD_ALERT_SETTING_ID =
            "render.hunger.lowFoodAlert";
    public static final String LOW_FOOD_THRESHOLD_SETTING_ID =
            "render.hunger.lowFoodThreshold";
    public static final int DEFAULT_LOW_FOOD_THRESHOLD = 6;
    public static final int MINIMUM_LOW_FOOD_THRESHOLD = 1;
    public static final int MAXIMUM_LOW_FOOD_THRESHOLD = 20;
    public static final String RENDER_PASS_ID =
            "hunger";

    private static final int PRIORITY = 130;
    private static final int TEXT_ARGB = 0xFFFFFFFF;
    private static final int LOW_FOOD_ARGB = 0xFFFFB65C;
    private static final int FOOD_ARGB = 0xFF7EC97A;
    private static final int SATURATION_ARGB = 0xFF69BFE8;
    private static final int TRACK_ARGB = 0xFF303D4A;
    static final float METER_WIDTH = 100.0F;
    static final float METER_HEIGHT = 3.0F;
    static final float FOOD_METER_OFFSET_Y = 12.0F;
    static final float SATURATION_METER_OFFSET_Y = 18.0F;

    private final Minecraft189PlayerHungerState hungerState;
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
                    228,
                    value -> value >= 0
                            && value <= 4096,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> showMeters = new Setting<Boolean>(
            SHOW_METERS_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> lowFoodAlert = new Setting<Boolean>(
            LOW_FOOD_ALERT_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> lowFoodThreshold = new Setting<Integer>(
            LOW_FOOD_THRESHOLD_SETTING_ID, DEFAULT_LOW_FOOD_THRESHOLD,
            value -> value != null
                    && value >= MINIMUM_LOW_FOOD_THRESHOLD
                    && value <= MAXIMUM_LOW_FOOD_THRESHOLD,
            SettingCodecs.INTEGER);
    private RenderPipeline.Registration renderRegistration;

    public Minecraft189HungerModule(
            final Minecraft189PlayerHungerState hungerState,
            final RenderPipeline renderPipeline,
            final LegacyUiHostCallbacks hostCallbacks) {
        this.hungerState =
                Objects.requireNonNull(
                        hungerState,
                        "hungerState");
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

    public Setting<Boolean> showMetersSetting() {
        return showMeters;
    }

    public Setting<Boolean> lowFoodAlertSetting() {
        return lowFoodAlert;
    }

    public Setting<Integer> lowFoodThresholdSetting() {
        return lowFoodThreshold;
    }

    static float meterFillWidth(final double value) {
        // Mapped food and saturation are bounded 0..20. Keep the render
        // helper fail-closed even for malformed values from other callers.
        if (!Double.isFinite(value)) {
            return 0.0F;
        }
        return (float) (Math.max(0.0D, Math.min(20.0D, value))
                / 20.0D * METER_WIDTH);
    }

    static boolean belowFoodThreshold(
            final Minecraft189PlayerHungerState.Snapshot hunger,
            final int threshold) {
        return hunger != null && hunger.available()
                && hunger.foodLevel() <= threshold;
    }

    @Override
    public synchronized void onEnable() {
        if (renderRegistration != null) {
            throw new IllegalStateException(
                    "hunger render pass already installed");
        }
        renderRegistration =
                renderPipeline.register(
                        new HungerRenderPass());
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
            final Minecraft189PlayerHungerState.Snapshot hunger) {
        Objects.requireNonNull(
                hunger,
                "hunger");
        return String.format(
                Locale.ROOT,
                "Hunger: %d/20 | Sat: %.1f",
                hunger.foodLevel(),
                hunger.saturationLevel());
    }

    private final class HungerRenderPass
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
            final Minecraft189PlayerHungerState.Snapshot hunger =
                    hungerState.snapshot();
            if (!hunger.available()) {
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
                final boolean lowFood = lowFoodAlert.get().booleanValue()
                        && belowFoodThreshold(
                                hunger, lowFoodThreshold.get().intValue());
                hostCallbacks.drawText(
                        UiFonts.DEFAULT,
                        left,
                        top,
                        textFor(hunger),
                        lowFood ? LOW_FOOD_ARGB : TEXT_ARGB);
                if (showMeters.get().booleanValue()) {
                    final float foodY = top + FOOD_METER_OFFSET_Y;
                    final float saturationY = top + SATURATION_METER_OFFSET_Y;
                    hostCallbacks.fillRect(left, foodY,
                            METER_WIDTH, METER_HEIGHT, TRACK_ARGB);
                    hostCallbacks.fillRect(left, saturationY,
                            METER_WIDTH, METER_HEIGHT, TRACK_ARGB);
                    final float foodWidth = meterFillWidth(hunger.foodLevel());
                    final float saturationWidth =
                            meterFillWidth(hunger.saturationLevel());
                    if (foodWidth > 0.0F) {
                        hostCallbacks.fillRect(left, foodY,
                                foodWidth, METER_HEIGHT,
                                lowFood ? LOW_FOOD_ARGB : FOOD_ARGB);
                    }
                    if (saturationWidth > 0.0F) {
                        hostCallbacks.fillRect(left, saturationY,
                                saturationWidth, METER_HEIGHT,
                                SATURATION_ARGB);
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
