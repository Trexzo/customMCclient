package dev.trexzo.custommc.platform.v1_8_9;
import java.util.UUID;

/** Exact 1.8.9 Entity UUIDs in loadedEntityList order. */
public interface Minecraft189WorldEntityUuidAccess {
    /** Returns null on invalid or unavailable list/UUID evidence. */
    UUID[] customMcLoadedEntityUuids();
}
