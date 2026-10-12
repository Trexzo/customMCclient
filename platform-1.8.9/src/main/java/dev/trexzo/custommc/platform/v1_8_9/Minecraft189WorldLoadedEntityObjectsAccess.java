package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Read-only exact native World.loadedEntityList elements in list order.
 * New array, unchanged Entity object references; never invent a target.
 */
public interface Minecraft189WorldLoadedEntityObjectsAccess {
    Object[] customMcLoadedEntityObjects();
}
