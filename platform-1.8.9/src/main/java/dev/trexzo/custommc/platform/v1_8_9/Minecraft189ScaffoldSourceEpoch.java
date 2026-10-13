package dev.trexzo.custommc.platform.v1_8_9;

import java.lang.ref.WeakReference;

/**
 * V31: acceptance-only same-source receipt authority for original Scaffold.
 *
 * The pending placement receipt retains only primitive metadata and a source
 * generation number. Weak identity witnesses live here, not in the receipt;
 * they neither keep Minecraft/World/Player alive nor authorize a placement.
 * Any missing original source, disconnect, player replacement, World switch,
 * or collected witness permanently invalidates an earlier generation.
 */
final class Minecraft189ScaffoldSourceEpoch {
    private WeakReference<Object> minecraft = weakNull();
    private WeakReference<Object> world = weakNull();
    private WeakReference<Object> player = weakNull();
    private long generation;

    private static WeakReference<Object> weakNull() {
        return new WeakReference<Object>(null);
    }

    private void discardWitnesses() {
        minecraft.clear();
        world.clear();
        player.clear();
        minecraft = weakNull();
        world = weakNull();
        player = weakNull();
    }

    synchronized void invalidate() {
        discardWitnesses();
    }

    synchronized long observe(Object originalMinecraft) {
        if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeContextAccess)) {
            discardWitnesses();
            return 0L;
        }
        final Minecraft189ScaffoldNativeContext.Witness source =
                Minecraft189ScaffoldNativeContext.capture(
                        (Minecraft189ScaffoldNativeContextAccess) originalMinecraft);
        if (source == null) {
            discardWitnesses();
            return 0L;
        }
        if (generation == Long.MAX_VALUE) {
            // No wraparound: never allow an expired receipt to become valid.
            discardWitnesses();
            return 0L;
        }
        if (generation > 0 && minecraft.get() == originalMinecraft
                && world.get() == source.world && player.get() == source.player)
            return generation;
        discardWitnesses();
        minecraft = new WeakReference<Object>(originalMinecraft);
        world = new WeakReference<Object>(source.world);
        player = new WeakReference<Object>(source.player);
        return ++generation;
    }

    synchronized boolean confirmsSameSourceReadback(
            Object originalMinecraft, long expectedGeneration,
            Minecraft189ScaffoldNativePlacementAttempt.Result placement) {
        if (expectedGeneration <= 0 || placement == null
                || observe(originalMinecraft) != expectedGeneration)
            return false;
        if (!Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(originalMinecraft, placement))
            return false;
        // Repeat identity check after the World/inventory source reads.
        return observe(originalMinecraft) == expectedGeneration;
    }
}
