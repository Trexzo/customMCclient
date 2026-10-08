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
    public static final String RENDER_PASS_ID = "nearby-players";

    private final Minecraft189PlayerPositionState local;
    private final Minecraft189WorldEntityPositionState entities;
    private final Minecraft189WorldEntityKindState kinds;
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
    private RenderPipeline.Registration registration;

    public Minecraft189NearbyPlayersModule(
            final Minecraft189PlayerPositionState local,
            final Minecraft189WorldEntityPositionState entities,
            final Minecraft189WorldEntityKindState kinds,
            final RenderPipeline pipeline,
            final LegacyUiHostCallbacks graphics) {
        this.local = Objects.requireNonNull(local, "local");
        this.entities = Objects.requireNonNull(entities, "entities");
        this.kinds = Objects.requireNonNull(kinds, "kinds");
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
        this.graphics = Objects.requireNonNull(graphics, "graphics");
    }

    @Override public String id() { return ID; }
    public Setting<Integer> xSetting() { return x; }
    public Setting<Integer> ySetting() { return y; }
    public Setting<Integer> radiusSetting() { return radius; }

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
            return new NearbySnapshot(false, 0, 0.0D);
        }
        final double radiusSquared = (double) radius * radius;
        double nearestSquared = Double.POSITIVE_INFINITY;
        int count = 0;
        for (int index = 0; index < positions.entityCount(); index++) {
            if (!kinds.player(index) || kinds.localPlayer(index)) continue;
            final double dx = positions.x(index) - local.x();
            final double dy = positions.y(index) - local.y();
            final double dz = positions.z(index) - local.z();
            final double squared = dx * dx + dy * dy + dz * dz;
            if (!Double.isFinite(squared) || squared > radiusSquared) continue;
            count++;
            nearestSquared = Math.min(nearestSquared, squared);
        }
        return new NearbySnapshot(true, count,
                count == 0 ? 0.0D : Math.sqrt(nearestSquared));
    }

    static final class NearbySnapshot {
        private final boolean available;
        private final int count;
        private final double nearestDistance;
        private NearbySnapshot(
                final boolean available,
                final int count,
                final double nearestDistance) {
            this.available = available;
            this.count = count;
            this.nearestDistance = nearestDistance;
        }
        boolean available() { return available; }
        int count() { return count; }
        double nearestDistance() { return nearestDistance; }
    }

    private final class NearbyPlayersPass implements RenderPass {
        @Override public String id() { return RENDER_PASS_ID; }
        @Override public RenderStage stage() { return RenderStage.HUD; }
        @Override public int priority() { return 124; }

        @Override public void render(final RenderFrame frame) {
            Objects.requireNonNull(frame, "frame");
            final int range = radius.get().intValue();
            final NearbySnapshot nearby = countNearby(
                    local.snapshot(), entities.snapshot(), kinds.snapshot(), range);
            if (!nearby.available()) return;
            final UiViewport viewport = new UiViewport(
                    graphics.framebufferWidth(),
                    graphics.framebufferHeight(), graphics.uiScale());
            final float left = x.get().floatValue();
            final float top = y.get().floatValue();
            graphics.beginUi(viewport);
            RuntimeException failure = null;
            try {
                graphics.fillRoundedRect(left, top, 156.0F, 42.0F,
                        6.0F, 0xE610141E);
                graphics.fillRoundedRect(left, top, 3.0F, 42.0F,
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
