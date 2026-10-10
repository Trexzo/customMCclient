package dev.trexzo.custommc.platform.v1_8_9;

/** Native 1.8.9 local player's line-of-sight result in loadedEntityList order. */
public interface Minecraft189WorldEntityVisibilityAccess {
    /** Null without valid world/local player; -1 nonplayer, 0 hidden, 1 visible. */
    int[] customMcLoadedEntityVisibility();
}
