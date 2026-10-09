package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Exact source-backed AbstractClientPlayer#getPlayerInfo presence.
 * True means a mapped NetworkPlayerInfo exists; it does not prove a human.
 */
public interface Minecraft189PlayerTabInfoAccess {
    boolean customMcHasNetworkPlayerInfo();
}
