package dev.trexzo.custommc.platform.v1_8_9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Real world-render-frame player AABB projection evidence. Not a HUD draw.
 * Every entity/UUID/kind tuple must refer to the same native loaded list.
 */
final class Minecraft189ProjectedPlayerFrame {
    private static final int MAX_ENTITIES = 16384;
    private static final int MAX_VISIBLE_PLAYERS = 64;
    private final long frame;
    private final List<Player> visible;
    private final int originalRemotePlayers;

    private Minecraft189ProjectedPlayerFrame(
            final long frame, final List<Player> visible,
            final int originalRemotePlayers) {
        this.frame = frame;
        this.visible = Collections.unmodifiableList(new ArrayList<Player>(visible));
        this.originalRemotePlayers = originalRemotePlayers;
    }
    long frame() { return frame; }
    int remoteCount() { return originalRemotePlayers; }
    List<Player> visible() { return visible; }

    static Minecraft189ProjectedPlayerFrame collect(final long frame,
            final Minecraft189WorldCameraFrameState.Snapshot camera,
            final Object[] objects, final int[] kinds, final UUID[] ids) {
        if (frame < 0 || camera == null || !camera.originAvailable()
                || camera.frame() != frame || objects == null || kinds == null
                || ids == null || objects.length != kinds.length
                || objects.length != ids.length || objects.length > MAX_ENTITIES)
            return null;
        final HashSet<UUID> unique = new HashSet<UUID>();
        final List<Player> visible = new ArrayList<Player>();
        int remote = 0;
        for (int i = 0; i < objects.length; i++) {
            final Object obj = objects[i];
            final int kind = kinds[i];
            if (obj == null || ids[i] == null || !unique.add(ids[i])
                    || (kind & ~(Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER
                            | Minecraft189WorldEntityKindState.LOCAL_PLAYER)) != 0
                    || !(obj instanceof Minecraft189NativeEntityUuidAccess)
                    || !ids[i].equals(((Minecraft189NativeEntityUuidAccess)obj)
                            .customMcNativeEntityUuid()))
                return null;
            final boolean player = (kind & Minecraft189WorldEntityKindState.PLAYER) != 0;
            final boolean living = (kind & Minecraft189WorldEntityKindState.LIVING) != 0;
            final boolean local = (kind & Minecraft189WorldEntityKindState.LOCAL_PLAYER) != 0;
            if ((player && !living) || (local && !player)) return null;
            if (!player || local) continue;
            remote++;
            if (!(obj instanceof Minecraft189EntityHitboxBoundsAccess)
                    || !(obj instanceof Minecraft189PlayerPositionAccess)) return null;
            final double[] aabb =
                    ((Minecraft189EntityHitboxBoundsAccess)obj).customMcHitboxBounds();
            final Minecraft189PlayerPositionAccess pos =
                    (Minecraft189PlayerPositionAccess)obj;
            if (aabb == null || aabb.length != 6) return null;
            final double[] current = {
                    pos.customMcPositionX(), pos.customMcPositionY(), pos.customMcPositionZ()
            };
            final double[] prev = {
                    pos.customMcLastTickPositionX(),
                    pos.customMcLastTickPositionY(),
                    pos.customMcLastTickPositionZ()
            };
            final double[] origin = {
                    camera.originX(), camera.originY(), camera.originZ()
            };
            final double[] shifted = new double[6];
            for (int axis = 0; axis < 3; axis++) {
                if (!Double.isFinite(current[axis])
                        || !Double.isFinite(origin[axis])) return null;
                final double interpolated = Minecraft189WorldCameraFrameState.lerp(
                        prev[axis], current[axis], camera.partialTicks());
                if (!Double.isFinite(interpolated)) return null;
                final double shift = interpolated - current[axis] - origin[axis];
                shifted[axis] = aabb[axis] + shift;
                shifted[axis+3] = aabb[axis+3] + shift;
                if (!Double.isFinite(shifted[axis])
                        || !Double.isFinite(shifted[axis+3])) return null;
            }
            final Minecraft189WorldToScreenProjection.Rect rect =
                    Minecraft189WorldToScreenProjection.projectAabb(
                            shifted, camera.view(), camera.projection(), camera.viewport());
            if (rect != null && visible.size() < MAX_VISIBLE_PLAYERS)
                visible.add(new Player(ids[i], rect));
        }
        return new Minecraft189ProjectedPlayerFrame(frame, visible, remote);
    }

    static final class Player {
        final UUID uuid;
        final Minecraft189WorldToScreenProjection.Rect rect;
        Player(final UUID uuid, final Minecraft189WorldToScreenProjection.Rect rect) {
            this.uuid = uuid;
            this.rect = rect;
        }
    }
}
