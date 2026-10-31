package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongSupplier;

/**
 * Bounded, wall-clock entrance motion independent of HUD render FPS.
 * Only presently enabled modules survive into the snapshot: disabling
 * remains immediate and cannot leave ghost entries. No background tick,
 * sleep or synthetic enabled-module snapshots are involved.
 */
final class Minecraft189ArrayListMotion {
    private static final float FIRST_VISIBLE_PHASE = 0.22F;
    private static final long ENTER_DURATION_NANOS = 180_000_000L;
    private static final long MAX_ELAPSED_NANOS = 200_000_000L;
    private final Map<String, Float> progress = new LinkedHashMap<String, Float>();
    private final LongSupplier nanoClock;
    private boolean initialized;
    private long lastFrameNanos;

    Minecraft189ArrayListMotion() {
        this(System::nanoTime);
    }

    Minecraft189ArrayListMotion(final LongSupplier nanoClock) {
        this.nanoClock = Objects.requireNonNull(nanoClock, "nanoClock");
    }

    synchronized Map<String, Float> advance(final List<String> enabledIds,
            final boolean animate) {
        Objects.requireNonNull(enabledIds, "enabledIds");
        final Set<String> live = new LinkedHashSet<String>(enabledIds);
        if (live.size() != enabledIds.size()) {
            throw new IllegalArgumentException("duplicate module ID");
        }
        final long now = nanoClock.getAsLong();
        // System.nanoTime has an arbitrary origin and can wrap; a negative
        // difference never makes the animation run backward or jump.
        final long difference = now - lastFrameNanos;
        final long elapsed = initialized && difference > 0L
                ? Math.min(difference, MAX_ELAPSED_NANOS) : 0L;
        lastFrameNanos = now;
        progress.keySet().retainAll(live);
        final boolean instant = !initialized || !animate;
        for (String id : live) {
            Objects.requireNonNull(id, "enabled module id");
            final Float prior = progress.get(id);
            final float next = instant ? 1.0F
                    : prior == null ? FIRST_VISIBLE_PHASE
                    : Math.min(1.0F, prior.floatValue()
                            + (float) elapsed / (float) ENTER_DURATION_NANOS);
            progress.put(id, Float.valueOf(next));
        }
        initialized = true;
        return Collections.unmodifiableMap(
                new LinkedHashMap<String, Float>(progress));
    }

    synchronized void reset() {
        progress.clear();
        initialized = false;
        lastFrameNanos = 0L;
    }

    static int withOpacity(final int color, final float progress) {
        final float phase = Math.max(0.0F, Math.min(1.0F, progress));
        final int alpha = (color >>> 24) & 255;
        final int faded = Math.round(alpha * phase);
        return (faded << 24) | (color & 0x00FFFFFF);
    }

    static float slide(final float phase) {
        // Smoothstep retains 12px maximum and an exact zero at rest, but
        // avoids linear-position snapping as a label finishes entering.
        // Opacity and module state remain authoritative and unchanged.
        final float t = Math.max(0.0F, Math.min(1.0F, phase));
        final float eased = t * t * (3.0F - 2.0F * t);
        return (1.0F - eased) * 12.0F;
    }
}
