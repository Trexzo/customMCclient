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

public final class Minecraft189TargetHudModule implements Module {
    public static final String ID = "render.targetHud";
    public static final String X_SETTING_ID = "render.targetHud.x";
    public static final String Y_SETTING_ID = "render.targetHud.y";
    public static final String COMPACT_SETTING_ID = "render.targetHud.compact";
    public static final String PROXIMITY_METER_SETTING_ID =
            "render.targetHud.proximityMeter";
    public static final String PROXIMITY_RANGE_SETTING_ID =
            "render.targetHud.proximityRange";
    public static final String ADAPTIVE_ACCENT_SETTING_ID =
            "render.targetHud.adaptiveAccent";
    public static final String SHOW_COORDINATES_SETTING_ID =
            "render.targetHud.showCoordinates";
    public static final int DEFAULT_PROXIMITY_RANGE = 16;
    public static final int MINIMUM_PROXIMITY_RANGE = 1;
    public static final int MAXIMUM_PROXIMITY_RANGE = 64;
    private static final int PROXIMITY_TRACK_ARGB = 0xFF303D4A;
    private static final int PROXIMITY_FILL_ARGB = 0xFF70C9E8;
    private static final int PROXIMITY_CLOSE_ARGB = 0xFFFFB65C;
    public static final String RENDER_PASS_ID = "target-hud";
    private final Minecraft189NearestPlayerTargetState nearest;
    private final Minecraft189TargetRotationState direction;
    private final RenderPipeline pipeline;
    private final LegacyUiHostCallbacks graphics;
    private final Setting<Integer> x = new Setting<Integer>(
            X_SETTING_ID, 18, v -> v != null && v >= 0 && v <= 4096, SettingCodecs.INTEGER);
    private final Setting<Integer> y = new Setting<Integer>(
            Y_SETTING_ID, 210, v -> v != null && v >= 0 && v <= 4096, SettingCodecs.INTEGER);
    private final Setting<Boolean> compact = new Setting<Boolean>(
            COMPACT_SETTING_ID, Boolean.FALSE, v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> proximityMeter = new Setting<Boolean>(
            PROXIMITY_METER_SETTING_ID, Boolean.FALSE,
            v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> proximityRange = new Setting<Integer>(
            PROXIMITY_RANGE_SETTING_ID, DEFAULT_PROXIMITY_RANGE,
            v -> v != null && v >= MINIMUM_PROXIMITY_RANGE
                    && v <= MAXIMUM_PROXIMITY_RANGE,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> adaptiveAccent = new Setting<Boolean>(
            ADAPTIVE_ACCENT_SETTING_ID, Boolean.FALSE,
            v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> showCoordinates = new Setting<Boolean>(
            SHOW_COORDINATES_SETTING_ID, Boolean.FALSE,
            v -> v != null, SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration registration;

    public Minecraft189TargetHudModule(
            final Minecraft189NearestPlayerTargetState nearest,
            final Minecraft189TargetRotationState direction,
            final RenderPipeline pipeline,
            final LegacyUiHostCallbacks graphics) {
        this.nearest = Objects.requireNonNull(nearest, "nearest");
        this.direction = Objects.requireNonNull(direction, "direction");
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
        this.graphics = Objects.requireNonNull(graphics, "graphics");
    }

    @Override public String id() { return ID; }
    public Setting<Integer> xSetting() { return x; }
    public Setting<Integer> ySetting() { return y; }
    public Setting<Boolean> compactSetting() { return compact; }
    public Setting<Boolean> proximityMeterSetting() { return proximityMeter; }
    public Setting<Integer> proximityRangeSetting() { return proximityRange; }
    public Setting<Boolean> adaptiveAccentSetting() { return adaptiveAccent; }
    public Setting<Boolean> showCoordinatesSetting() { return showCoordinates; }

    static float proximityFraction(final double distance,
            final int range) {
        // A proximity indicator is not a line-of-sight, entity HP,
        // or combat-reach inference. Reject nonfinite/invalid positions.
        if (!Double.isFinite(distance) || distance < 0.0D
                || range < MINIMUM_PROXIMITY_RANGE
                || range > MAXIMUM_PROXIMITY_RANGE) {
            return 0.0F;
        }
        return (float) Math.max(0.0D,
                Math.min(1.0D, 1.0D - distance / range));
    }

    static int accentColor(final double distance, final int range,
            final boolean adaptive) {
        // Color encodes only already-certified player distance, never
        // target health, reach, line of sight, or identity.
        return adaptive && proximityFraction(distance, range) >= 0.75F
                ? PROXIMITY_CLOSE_ARGB : PROXIMITY_FILL_ARGB;
    }

    static int proximityFillColor(final float fraction) {
        return fraction >= 0.75F
                ? PROXIMITY_CLOSE_ARGB : PROXIMITY_FILL_ARGB;
    }

    @Override public synchronized void onEnable() {
        if (registration != null) {
            throw new IllegalStateException("Target HUD pass already installed");
        }
        registration = pipeline.register(new TargetHudPass());
    }

    @Override public synchronized void onDisable() {
        if (registration != null) {
            registration.close();
            registration = null;
        }
    }

    synchronized boolean renderPassInstalled() {
        return registration != null && registration.active();
    }

    static String distanceText(final Minecraft189NearestPlayerTargetState.Snapshot target) {
        return String.format(Locale.ROOT, "DIST  %.1fm", target.distance());
    }

    static String coordinateText(
            final Minecraft189NearestPlayerTargetState.Snapshot target) {
        if (target == null || !target.available() || !target.found()
                || !Double.isFinite(target.x())
                || !Double.isFinite(target.y())
                || !Double.isFinite(target.z())) {
            return null;
        }
        return String.format(Locale.ROOT, "XYZ  %.1f / %.1f / %.1f",
                target.x(), target.y(), target.z());
    }

    static String directionText(final Minecraft189TargetRotationState.Snapshot rotation) {
        // Avoid displaying -0 degrees when the exact target angles
        // carry IEEE-754 negative zero from atan2.
        return String.format(Locale.ROOT, "YAW  %d°    PITCH  %d°",
                Math.round(rotation.yaw()), Math.round(rotation.pitch()));
    }

    private final class TargetHudPass implements RenderPass {
        @Override public String id() { return RENDER_PASS_ID; }
        @Override public RenderStage stage() { return RenderStage.HUD; }
        @Override public int priority() { return 125; }

        @Override public void render(final RenderFrame frame) {
            Objects.requireNonNull(frame, "frame");
            final Minecraft189NearestPlayerTargetState.Snapshot target = nearest.snapshot();
            final Minecraft189TargetRotationState.Snapshot angle = direction.snapshot();
            if (!target.available() || !target.found() || !angle.available()) {
                return;
            }
            final UiViewport viewport = new UiViewport(
                    graphics.framebufferWidth(), graphics.framebufferHeight(), graphics.uiScale());
            final float left = x.get().floatValue();
            final float top = y.get().floatValue();
            final boolean small = compact.get().booleanValue();
            final String coordinates = showCoordinates.get().booleanValue()
                    ? coordinateText(target) : null;
            final boolean expanded = coordinates != null;
            final float width = expanded
                    ? (small ? 208.0F : 224.0F)
                    : (small ? 142.0F : 164.0F);
            final float height = expanded
                    ? (small ? 54.0F : 72.0F)
                    : (small ? 38.0F : 56.0F);
            graphics.beginUi(viewport);
            RuntimeException failure = null;
            try {
                graphics.fillRoundedRect(left, top, width, height, 6.0F, 0xE610141E);
                graphics.fillRoundedRect(left, top, 3.0F, height, 1.5F,
                        accentColor(target.distance(),
                                proximityRange.get().intValue(),
                                adaptiveAccent.get().booleanValue()));
                graphics.drawText(UiFonts.DEFAULT, left + 12, top + 7,
                        "TARGET  #" + (target.entityIndex() + 1), 0xFFFFFFFF);
                graphics.drawText(UiFonts.DEFAULT, left + 12, top + 23,
                        distanceText(target), 0xFFC4CBD5);
                if (!small) {
                    graphics.drawText(UiFonts.DEFAULT, left + 12, top + 39,
                            directionText(angle), 0xFF92A8BF);
                }
                if (coordinates != null) {
                    graphics.drawText(UiFonts.DEFAULT, left + 12,
                            top + (small ? 39.0F : 55.0F),
                            coordinates, 0xFF92A8BF);
                }
                if (proximityMeter.get().booleanValue()) {
                    final float trackLeft = left + 12.0F;
                    final float trackTop = top + height - 5.0F;
                    final float trackWidth = width - 24.0F;
                    final float fraction = proximityFraction(
                            target.distance(), proximityRange.get().intValue());
                    graphics.fillRect(trackLeft, trackTop,
                            trackWidth, 2.0F, PROXIMITY_TRACK_ARGB);
                    if (fraction > 0.0F) {
                        graphics.fillRect(trackLeft, trackTop,
                                trackWidth * fraction, 2.0F,
                                proximityFillColor(fraction));
                    }
                }
            } catch (RuntimeException e) {
                failure = e;
                throw e;
            } finally {
                try {
                    graphics.endUi();
                } catch (RuntimeException e) {
                    if (failure != null) failure.addSuppressed(e);
                    else throw e;
                }
            }
        }
    }
}
