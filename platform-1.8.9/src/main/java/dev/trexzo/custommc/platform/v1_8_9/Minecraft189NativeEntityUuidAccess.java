package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * Exact Minecraft 1.8.9 mapped native Entity.getUniqueID() exposed on
 * the actual Mojang attackEntity third argument. Read-only; never invents
 * or overrides an entity UUID.
 */
public interface Minecraft189NativeEntityUuidAccess {
    UUID customMcNativeEntityUuid();
}
