package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Read-only original Minecraft EntityPlayer.fishEntity presence.
 * Implemented by transforming the actual pinned 1.8.9 EntityPlayer class,
 * not by predicting a rod animation, packet, or last-use timer.
 */
public interface Minecraft189PlayerFishingHookAccess {
    boolean customMcHasFishingHook();
}
