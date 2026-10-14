package dev.trexzo.custommc.platform.v1_8_9;

import java.lang.ref.WeakReference;

/**
 * CI-only readiness for a real inventory-open negative fixture.
 *
 * One GUI-clear tick immediately after joining a World is NOT evidence of a
 * settled original client: vanilla may open or replace a loading screen on
 * the following tick. This observes the exact native host/World/player
 * identity for a consecutive period, with weak references and no game
 * actions. Any missing source, opened/unknown GUI or identity change
 * resets the window. It never authorizes an original controller click.
 */
final class Minecraft189ScaffoldGuiClearStability {
    static final int MIN_STABLE_TICKS = 60;

    private WeakReference<Object> owner = new WeakReference<Object>(null);
    private WeakReference<Object> player = new WeakReference<Object>(null);
    private WeakReference<Object> world = new WeakReference<Object>(null);
    private int clearTicks;

    synchronized boolean observe(final Object originalMinecraft,
            final Object originalPlayer, final Object originalWorld,
            final boolean exactlyClear) {
        if (!exactlyClear || originalMinecraft == null
                || originalPlayer == null || originalWorld == null) {
            reset();
            return false;
        }
        if (owner.get() != originalMinecraft || player.get() != originalPlayer
                || world.get() != originalWorld) {
            owner = new WeakReference<Object>(originalMinecraft);
            player = new WeakReference<Object>(originalPlayer);
            world = new WeakReference<Object>(originalWorld);
            clearTicks = 1;
        } else if (clearTicks < MIN_STABLE_TICKS) {
            clearTicks++;
        }
        return clearTicks >= MIN_STABLE_TICKS;
    }

    synchronized int witnessedClearTicks() {
        return clearTicks;
    }

    synchronized void reset() {
        owner.clear();
        player.clear();
        world.clear();
        owner = new WeakReference<Object>(null);
        player = new WeakReference<Object>(null);
        world = new WeakReference<Object>(null);
        clearTicks = 0;
    }
}
