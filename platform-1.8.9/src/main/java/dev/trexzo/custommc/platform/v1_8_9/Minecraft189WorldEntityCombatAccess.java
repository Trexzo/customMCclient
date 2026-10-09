package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Read-only per-loaded-entity combat evidence (index parity with
 * customMcLoadedEntityPositions / Kinds).
 * -1 unknown/nonliving, else (hurtTime << 1) | (health > 0 ? 1 : 0).
 */
public interface Minecraft189WorldEntityCombatAccess {
    int[] customMcLoadedEntityCombatStates();
}
