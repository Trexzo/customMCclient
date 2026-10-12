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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Directional Player ESP: HUD bearing indicators from real source-mapped
 * player positions and yaw. NOT a perspective projection or 3D box ESP.
 */
public final class Minecraft189PlayerIndicatorsModule implements Module {
    public static final String ID = "render.playerIndicators";
    public static final String RANGE = ID + ".range";
    public static final String DISTANCES = ID + ".distances";
    public static final String PASS_ID = "player-direction-indicators";
    private static final int MAX_MARKERS = 12;
    private final Minecraft189PlayerPositionState local;
    private final Minecraft189WorldEntityPositionState positions;
    private final Minecraft189WorldEntityKindState kinds;
    private final Minecraft189PlayerRotationState rotation;
    private final RenderPipeline pipeline;
    private final LegacyUiHostCallbacks host;
    private final Setting<Integer> range = new Setting<Integer>(
            RANGE, 64, v -> v != null && v >= 4 && v <= 128,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> distances = new Setting<Boolean>(
            DISTANCES, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private RenderPipeline.Registration registration;

    public Minecraft189PlayerIndicatorsModule(
            final Minecraft189PlayerPositionState local,
            final Minecraft189WorldEntityPositionState positions,
            final Minecraft189WorldEntityKindState kinds,
            final Minecraft189PlayerRotationState rotation,
            final RenderPipeline pipeline,
            final LegacyUiHostCallbacks host) {
        this.local = Objects.requireNonNull(local, "local");
        this.positions = Objects.requireNonNull(positions, "positions");
        this.kinds = Objects.requireNonNull(kinds, "kinds");
        this.rotation = Objects.requireNonNull(rotation, "rotation");
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
        this.host = Objects.requireNonNull(host, "host");
    }

    @Override public String id() { return ID; }
    public Setting<Integer> rangeSetting() { return range; }
    public Setting<Boolean> distancesSetting() { return distances; }
    @Override public synchronized void onEnable() {
        if (registration != null) throw new IllegalStateException("indicators already enabled");
        registration = pipeline.register(new IndicatorsPass());
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

    /**
     * Exact 1.8.9 yaw convention: 0 faces +Z, +90 faces -X.
     * Returns a screen-space COMPASS direction, not a world-to-screen box.
     * The circle radius is fixed in logical pixels and the distance is 3D.
     */
    static float[] bearing(final double dx, final double dz,
            final float yaw, final float radius) {
        if (!Double.isFinite(dx) || !Double.isFinite(dz)
                || !Float.isFinite(yaw) || !Float.isFinite(radius)
                || radius <= 0 || (dx == 0.0D && dz == 0.0D)) return null;
        final double angle = Math.toRadians(yaw);
        final double right = dx * Math.cos(angle) + dz * Math.sin(angle);
        final double ahead = -dx * Math.sin(angle) + dz * Math.cos(angle);
        final double length = Math.hypot(right, ahead);
        if (!(length > 0) || !Double.isFinite(length)) return null;
        return new float[]{(float)(radius * right / length),
                (float)(-radius * ahead / length)};
    }

    static List<Marker> select(final Minecraft189PlayerPositionState.Snapshot me,
            final Minecraft189WorldEntityPositionState.Snapshot all,
            final Minecraft189WorldEntityKindState.Snapshot types,
            final Minecraft189PlayerRotationState.Snapshot facing,
            final int range, final float radius) {
        if (me == null || all == null || types == null || facing == null
                || !me.available() || !all.available() || !types.available()
                || !facing.available() || all.entityCount() != types.entityCount()
                || range < 4 || range > 128 || !Float.isFinite(radius) || radius <= 0)
            return Collections.emptyList();
        final double maxSquared = (double)range * range;
        final List<Marker> out = new ArrayList<Marker>();
        for (int i = 0; i < all.entityCount() && out.size() < MAX_MARKERS; i++) {
            if (!types.player(i) || !types.living(i) || types.localPlayer(i))
                continue;
            final double dx = all.x(i) - me.x();
            final double dy = all.y(i) - me.y();
            final double dz = all.z(i) - me.z();
            final double d2 = dx * dx + dy * dy + dz * dz;
            if (!Double.isFinite(d2) || d2 > maxSquared || d2 <= 0) continue;
            final float[] b = bearing(dx, dz, facing.yaw(), radius);
            if (b != null) out.add(new Marker(i, b[0], b[1],
                    (float)Math.sqrt(d2), (float)dy));
        }
        return Collections.unmodifiableList(out);
    }

    static final class Marker {
        final int index;
        final float x, y, distance, elevation;
        Marker(final int index, final float x, final float y,
                final float distance, final float elevation) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.distance = distance;
            this.elevation = elevation;
        }
    }

    private final class IndicatorsPass implements RenderPass {
        @Override public String id() { return PASS_ID; }
        @Override public RenderStage stage() { return RenderStage.HUD; }
        @Override public int priority() { return 127; }
        @Override public void render(final RenderFrame frame) {
            Objects.requireNonNull(frame, "frame");
            final int fw = host.framebufferWidth();
            final int fh = host.framebufferHeight();
            final float scale = host.uiScale();
            if (fw < 100 || fh < 100 || !Float.isFinite(scale) || scale <= 0)
                return;
            final float width = fw / scale, height = fh / scale;
            final float radius = Math.min(width, height) * 0.27F;
            final List<Marker> markers = select(local.snapshot(), positions.snapshot(),
                    kinds.snapshot(), rotation.snapshot(),
                    range.get().intValue(), radius);
            if (markers.isEmpty()) return;
            host.beginUi(new UiViewport(fw, fh, scale));
            RuntimeException failure = null;
            try {
                for (final Marker marker : markers) {
                    final float x = width * 0.5F + marker.x;
                    final float y = height * 0.5F + marker.y;
                    final int color = marker.elevation > 2.0F ? 0xFFE391FF
                            : marker.elevation < -2.0F ? 0xFF6EA8FF : 0xFFFFB56B;
                    host.fillRoundedRect(x - 4, y - 4, 8, 8, 3, 0xD9131B25);
                    host.fillRoundedRect(x - 2, y - 2, 4, 4, 2, color);
                    if (distances.get().booleanValue())
                        host.drawText(UiFonts.DEFAULT, x + 7, y - 5,
                                String.format(Locale.ROOT, "%.0fm", marker.distance),
                                0xFFE5EDF5);
                }
            } catch (RuntimeException e) {
                failure = e;
                throw e;
            } finally {
                try {
                    host.endUi();
                } catch (RuntimeException e) {
                    if (failure != null) failure.addSuppressed(e);
                    else throw e;
                }
            }
        }
    }
}
