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

/** Lightweight, read-only nearby remote-player counter with no identity claims. */
public final class Minecraft189NearbyPlayersModule implements Module {
    public static final String ID = "render.nearbyPlayers";
    public static final String X_SETTING_ID = ID + ".x";
    public static final String Y_SETTING_ID = ID + ".y";
    public static final String RADIUS_SETTING_ID = ID + ".radius";
    public static final String SHOW_RADAR_SETTING_ID = ID + ".showRadar";
    public static final String NORTH_UP_SETTING_ID = ID + ".northUp";
    public static final String HEIGHT_COLORS_SETTING_ID = ID + ".heightColors";
    public static final String HIGHLIGHT_NEAREST_SETTING_ID = ID + ".highlightNearest";
    public static final String RENDER_PASS_ID = "nearby-players";

    private final Minecraft189PlayerPositionState local;
    private final Minecraft189WorldEntityPositionState entities;
    private final Minecraft189WorldEntityKindState kinds;
    private final Minecraft189PlayerRotationState rotation;
    private final RenderPipeline pipeline;
    private final LegacyUiHostCallbacks graphics;
    private final Setting<Integer> x = new Setting<Integer>(
            X_SETTING_ID, 18, v -> v != null && v >= 0 && v <= 4096,
            SettingCodecs.INTEGER);
    private final Setting<Integer> y = new Setting<Integer>(
            Y_SETTING_ID, 278, v -> v != null && v >= 0 && v <= 4096,
            SettingCodecs.INTEGER);
    private final Setting<Integer> radius = new Setting<Integer>(
            RADIUS_SETTING_ID, 32, v -> v != null && v >= 1 && v <= 128,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> showRadar = new Setting<Boolean>(
            SHOW_RADAR_SETTING_ID, Boolean.FALSE,
            v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> northUp = new Setting<Boolean>(
            NORTH_UP_SETTING_ID, Boolean.FALSE,
            v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> heightColors = new Setting<Boolean>(
            HEIGHT_COLORS_SETTING_ID, Boolean.FALSE,
            v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> highlightNearest = new Setting<Boolean>(
            HIGHLIGHT_NEAREST_SETTING_ID, Boolean.FALSE,
            v -> v != null, SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration registration;

    public Minecraft189NearbyPlayersModule(
            final Minecraft189PlayerPositionState local,
            final Minecraft189WorldEntityPositionState entities,
            final Minecraft189WorldEntityKindState kinds,
            final Minecraft189PlayerRotationState rotation,
            final RenderPipeline pipeline,
            final LegacyUiHostCallbacks graphics) {
        this.local = Objects.requireNonNull(local, "local");
        this.entities = Objects.requireNonNull(entities, "entities");
        this.kinds = Objects.requireNonNull(kinds, "kinds");
        this.rotation = Objects.requireNonNull(rotation, "rotation");
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
        this.graphics = Objects.requireNonNull(graphics, "graphics");
    }

    @Override public String id() { return ID; }
    public Setting<Integer> xSetting() { return x; }
    public Setting<Integer> ySetting() { return y; }
    public Setting<Integer> radiusSetting() { return radius; }
    public Setting<Boolean> showRadarSetting() { return showRadar; }
    public Setting<Boolean> northUpSetting() { return northUp; }
    public Setting<Boolean> heightColorsSetting() { return heightColors; }
    public Setting<Boolean> highlightNearestSetting() { return highlightNearest; }

    @Override public synchronized void onEnable() {
        if (registration != null) {
            throw new IllegalStateException("Nearby Players pass already registered");
        }
        registration = pipeline.register(new NearbyPlayersPass());
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

    static NearbySnapshot countNearby(
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final int radius) {
        if (local == null || positions == null || kinds == null
                || radius < 1 || radius > 128
                || !local.available() || !positions.available()
                || !kinds.available()
                || positions.entityCount() != kinds.entityCount()) {
            return new NearbySnapshot(false, 0, 0.0D, -1);
        }
        final double radiusSquared = (double) radius * radius;
        double nearestSquared = Double.POSITIVE_INFINITY;
        int nearestIndex = -1;
        int count = 0;
        for (int index = 0; index < positions.entityCount(); index++) {
            if (!kinds.player(index) || kinds.localPlayer(index)) continue;
            final double dx = positions.x(index) - local.x();
            final double dy = positions.y(index) - local.y();
            final double dz = positions.z(index) - local.z();
            final double squared = dx * dx + dy * dy + dz * dz;
            if (!Double.isFinite(squared) || squared > radiusSquared) continue;
            count++;
            if (squared < nearestSquared) {
                nearestSquared = squared;
                nearestIndex = index;
            }
        }
        return new NearbySnapshot(true, count,
                count == 0 ? 0.0D : Math.sqrt(nearestSquared), nearestIndex);
    }

    // Certified 1.8.9 yaw convention: zero faces +Z, positive yaw rotates
    // toward -X. Horizontal world offsets become camera-relative pixels.
    static float[] projectRadar(
            final double dx, final double dz,
            final float yaw, final int radius) {
        if (!Double.isFinite(dx) || !Double.isFinite(dz)
                || !Float.isFinite(yaw) || radius < 1 || radius > 128) {
            return null;
        }
        final double a = Math.toRadians(yaw);
        final double right = dx * Math.cos(a) + dz * Math.sin(a);
        final double ahead = -dx * Math.sin(a) + dz * Math.cos(a);
        final float px = (float) (right * 46.0D / radius);
        final float py = (float) (-ahead * 46.0D / radius);
        return Float.isFinite(px) && Float.isFinite(py)
                ? new float[]{px, py} : null;
    }

    /** Fixed world orientation: north (-Z) at top, east (+X) at right. */
    static float[] projectNorthUp(
            final double dx, final double dz, final int radius) {
        if (!Double.isFinite(dx) || !Double.isFinite(dz)
                || radius < 1 || radius > 128) {
            return null;
        }
        final float px = (float) (dx * 46.0D / radius);
        final float py = (float) (dz * 46.0D / radius);
        return Float.isFinite(px) && Float.isFinite(py)
                ? new float[]{px, py} : null;
    }

    /** Vertical colors use measured relative Y and do not infer visibility. */
    static int radarBlipColor(final double dy, final boolean heightColorsMode) {
        if (!heightColorsMode || !Double.isFinite(dy)) return 0xFFFFB56B;
        if (dy > 2.0D) return 0xFFE391FF; // Above by more than 2 blocks.
        if (dy < -2.0D) return 0xFF6EA8FF; // Below by more than 2 blocks.
        return 0xFFFFB56B; // Within 2 blocks vertically.
    }

    static final class NearbySnapshot {
        private final boolean available;
        private final int count;
        private final double nearestDistance;
        private final int nearestIndex;
        private NearbySnapshot(
                final boolean available,
                final int count,
                final double nearestDistance,
                final int nearestIndex) {
            this.available = available;
            this.count = count;
            this.nearestDistance = nearestDistance;
            this.nearestIndex = nearestIndex;
        }
        boolean available() { return available; }
        int count() { return count; }
        double nearestDistance() { return nearestDistance; }
        int nearestIndex() { return nearestIndex; }
    }

    private void drawRadar(
            final float x, final float y, final int range,
            final Minecraft189PlayerPositionState.Snapshot me,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot types,
            final float yaw, final boolean northUpMode,
            final boolean heightColorsMode,
            final boolean highlightNearestMode,
            final int nearestIndex) {
        final float centerX = x + 78.0F;
        final float centerY = y + 96.0F;
        graphics.fillRoundedRect(x + 30, y + 48, 96, 96, 4, 0xFF1B2634);
        graphics.fillRect(centerX - 0.5F, centerY - 46, 1, 92, 0xFF334357);
        graphics.fillRect(centerX - 46, centerY - 0.5F, 92, 1, 0xFF334357);
        final double maxSquared = (double) range * range;
        for (int i = 0; i < positions.entityCount(); i++) {
            if (!types.player(i) || types.localPlayer(i)) continue;
            final double dx = positions.x(i) - me.x();
            final double dy = positions.y(i) - me.y();
            final double dz = positions.z(i) - me.z();
            final double squared = dx * dx + dy * dy + dz * dz;
            if (!Double.isFinite(squared) || squared > maxSquared) continue;
            final float[] offset = northUpMode
                    ? projectNorthUp(dx, dz, range)
                    : projectRadar(dx, dz, yaw, range);
            if (offset != null) {
                if (highlightNearestMode && i == nearestIndex) {
                    graphics.fillRoundedRect(centerX + offset[0] - 4,
                            centerY + offset[1] - 4, 8, 8, 4, 0xFFF5F8FF);
                }
                graphics.fillRoundedRect(centerX + offset[0] - 2,
                        centerY + offset[1] - 2, 4, 4, 2,
                        radarBlipColor(dy, heightColorsMode));
            }
        }
        graphics.fillRoundedRect(centerX - 3, centerY - 3, 6, 6, 3, 0xFF70C9E8);
    }

    private final class NearbyPlayersPass implements RenderPass {
        @Override public String id() { return RENDER_PASS_ID; }
        @Override public RenderStage stage() { return RenderStage.HUD; }
        @Override public int priority() { return 124; }

        @Override public void render(final RenderFrame frame) {
            Objects.requireNonNull(frame, "frame");
            final int range = radius.get().intValue();
            final Minecraft189PlayerPositionState.Snapshot me = local.snapshot();
            final Minecraft189WorldEntityPositionState.Snapshot positions = entities.snapshot();
            final Minecraft189WorldEntityKindState.Snapshot types = kinds.snapshot();
            final NearbySnapshot nearby = countNearby(me, positions, types, range);
            if (!nearby.available()) return;
            final Minecraft189PlayerRotationState.Snapshot facing = rotation.snapshot();
            final boolean northUpMode = northUp.get().booleanValue();
            final boolean map = showRadar.get().booleanValue()
                    && (northUpMode || facing.available());
            final float height = map ? 152.0F : 42.0F;
            final UiViewport viewport = new UiViewport(
                    graphics.framebufferWidth(),
                    graphics.framebufferHeight(), graphics.uiScale());
            final float left = x.get().floatValue();
            final float top = y.get().floatValue();
            graphics.beginUi(viewport);
            RuntimeException failure = null;
            try {
                graphics.fillRoundedRect(left, top, 156.0F, height,
                        6.0F, 0xE610141E);
                graphics.fillRoundedRect(left, top, 3.0F, height,
                        1.5F, nearby.count() == 0 ? 0xFF667789 : 0xFFFFB56B);
                graphics.drawText(UiFonts.DEFAULT, left + 12.0F, top + 7.0F,
                        "PLAYERS  " + nearby.count() + " / " + range + "m",
                        0xFFFFFFFF);
                final String nearestLabel = nearby.count() == 0
                        ? "NEAREST  --"
                        : String.format(Locale.ROOT,
                                "NEAREST  %.1fm", nearby.nearestDistance());
                graphics.drawText(UiFonts.DEFAULT, left + 12.0F, top + 24.0F,
                        nearestLabel, 0xFFC4CBD5);
                if (map) drawRadar(left, top, range, me, positions, types,
                        facing.yaw(), northUpMode,
                        heightColors.get().booleanValue(),
                        highlightNearest.get().booleanValue(),
                        nearby.nearestIndex());
            } catch (RuntimeException renderFailure) {
                failure = renderFailure;
                throw renderFailure;
            } finally {
                try {
                    graphics.endUi();
                } catch (RuntimeException closeFailure) {
                    if (failure != null) failure.addSuppressed(closeFailure);
                    else throw closeFailure;
                }
            }
        }
    }
}
