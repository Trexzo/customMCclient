package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Bounded, deterministic per-HUD-frame entrance motion. Only presently
 * enabled modules survive into the snapshot: disabling is immediate and
 * cannot leave a ghost entry or simulated enabled module on screen.
 */
final class Minecraft189ArrayListMotion {
    private static final float FRAME_STEP = 0.22F;
    private final Map<String, Float> progress = new LinkedHashMap<String, Float>();
    private boolean initialized;

    synchronized Map<String, Float> advance(final List<String> enabledIds,
            final boolean animate) {
        Objects.requireNonNull(enabledIds, "enabledIds");
        final Set<String> live = new LinkedHashSet<String>(enabledIds);
        if (live.size() != enabledIds.size()) {
            throw new IllegalArgumentException("duplicate module ID");
        }
        progress.keySet().retainAll(live);
        final boolean instant = !initialized || !animate;
        for (String id : live) {
            Objects.requireNonNull(id, "enabled module id");
            final Float prior = progress.get(id);
            final float next = instant ? 1.0F
                    : prior == null ? FRAME_STEP
                    : Math.min(1.0F, prior.floatValue() + FRAME_STEP);
            progress.put(id, Float.valueOf(next));
        }
        initialized = true;
        return Collections.unmodifiableMap(
                new LinkedHashMap<String, Float>(progress));
    }

    synchronized void reset() {
        progress.clear();
        initialized = false;
    }

    static int withOpacity(final int color, final float progress) {
        final float phase = Math.max(0.0F, Math.min(1.0F, progress));
        final int alpha = (color >>> 24) & 255;
        final int faded = Math.round(alpha * phase);
        return (faded << 24) | (color & 0x00FFFFFF);
    }

    static float slide(final float phase) {
        return (1.0F - Math.max(0.0F, Math.min(1.0F, phase))) * 12.0F;
    }
}
