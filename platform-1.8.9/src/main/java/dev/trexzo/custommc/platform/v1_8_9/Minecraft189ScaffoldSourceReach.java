package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Conservative read-only native block reach from original 1.8.9 controller.
 * Minecraft ave.c must be the source; transformed bda supplies the original
 * getBlockReachDistance() and extendedReach() implementations.
 *
 * Native Creative reach can exceed 4.5, but the current Scaffold planner
 * only authorizes <= 4.5. Never increase vanilla reach or send packets.
 */
final class Minecraft189ScaffoldSourceReach {
    private Minecraft189ScaffoldSourceReach() { }

    static double conservativeReach(Object originalMinecraft) {
        if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeControllerAccess))
            return Double.NaN;
        try {
            final Object nativeController =
                    ((Minecraft189ScaffoldNativeControllerAccess)originalMinecraft)
                            .customMcScaffoldNativeController();
            if (!(nativeController instanceof Minecraft189VanillaReachAccess))
                return Double.NaN;
            final Minecraft189VanillaReachAccess reach =
                    (Minecraft189VanillaReachAccess)nativeController;
            final float nativeDistance = reach.customMcVanillaBlockReachDistance();
            // Sample the native extended-reach owner as well. The conservative
            // planner cap remains enforced in both Creative and Survival.
            reach.customMcVanillaExtendedReach();
            if (!Float.isFinite(nativeDistance)
                    || nativeDistance <= 0.0F || nativeDistance > 6.0F)
                return Double.NaN;
            return Math.min(4.5D, (double)nativeDistance);
        } catch (RuntimeException unavailableSource) {
            return Double.NaN;
        }
    }
}
