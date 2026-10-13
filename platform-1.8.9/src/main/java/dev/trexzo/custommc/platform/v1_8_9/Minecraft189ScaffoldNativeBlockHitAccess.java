package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Original Minecraft 1.8.9 MovingObjectPosition (auh) only. The transformed
 * original hit contains the game's own type, BlockPos, EnumFacing and Vec3.
 * Return layout: [isBlock(1/0), supportX, supportY, supportZ, faceIndex,
 *                 hitX, hitY, hitZ].
 *
 * The default provides NO source authority. Null/invalid natives throw inside
 * the original bytecode; the validation gate catches and fails closed.
 */
public interface Minecraft189ScaffoldNativeBlockHitAccess {
    default double[] customMcScaffoldNativeBlockHit() { return null; }
}
