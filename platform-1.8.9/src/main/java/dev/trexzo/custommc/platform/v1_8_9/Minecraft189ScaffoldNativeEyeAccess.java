package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Read-only original EntityPlayer.getEyeHeight() source authority.
 * Implemented only by transformed original Minecraft 1.8.9 wn.
 * Default must fail closed on unsupported/untransformed players.
 */
public interface Minecraft189ScaffoldNativeEyeAccess {
    default float customMcOriginalEyeHeight() { return Float.NaN; }
}
