package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Source-derived player eye position, not the hardcoded 1.62F estimate.
 * Requires a transformed original local EntityPlayerSP inheriting the
 * original EntityPlayer.getEyeHeight() delegate (wn/aS()F).
 * NaN fails closed. Does not move/rotate camera or dispatch placement.
 */
final class Minecraft189ScaffoldSourceEye {
    private Minecraft189ScaffoldSourceEye() { }

    static double currentEyeY(Minecraft189PlayerPositionAccess player) {
        if (!(player instanceof Minecraft189LocalPlayerSafeWalkMarker)
                || !(player instanceof Minecraft189ScaffoldNativeEyeAccess))
            return Double.NaN;
        try {
            final double x = player.customMcPositionX();
            final double y = player.customMcPositionY();
            final double z = player.customMcPositionZ();
            final float height =
                    ((Minecraft189ScaffoldNativeEyeAccess)player)
                            .customMcOriginalEyeHeight();
            if (!Double.isFinite(x) || !Double.isFinite(y)
                    || !Double.isFinite(z) || !Float.isFinite(height)
                    || Math.abs(x) > 29999984.0D || Math.abs(z) > 29999984.0D
                    || y < 2.0D || y >= 255.0D
                    || height <= 0.0F || height > 2.2F)
                return Double.NaN;
            final double eye = y + height;
            return Double.isFinite(eye) ? eye : Double.NaN;
        } catch (RuntimeException unavailableNativeEye) {
            return Double.NaN;
        }
    }
}
