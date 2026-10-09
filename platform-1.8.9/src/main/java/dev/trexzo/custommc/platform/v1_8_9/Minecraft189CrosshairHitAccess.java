package dev.trexzo.custommc.platform.v1_8_9;

/** Read-only source-mapped vanilla ray-hit player evidence. */
public interface Minecraft189CrosshairHitAccess {
    boolean customMcCrosshairPlayerHit();

    /** Exact loadedEntityList index, or -1 when no verifiable player match. */
    default int customMcCrosshairPlayerIndex() { return -1; }
}
