package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V26 observational proof only. Joins the original Minecraft player, World,
 * live native mouse-over hit, original eye-height method and original
 * controller reach in one synchronous host callback.
 *
 * No placement candidate or World lease escapes this method. A positive
 * observation is NOT a permission to click, spoof camera rotation, send C08,
 * consume inventory or change vanilla World.
 */
final class Minecraft189ScaffoldOriginalCandidateProbe {
    static final class Observation {
        final int targetX, targetY, targetZ;
        final int supportX, supportY, supportZ;
        final Minecraft189ScaffoldSupportPlanner.Face face;
        final double nativeEyeY;
        final double conservativeReach;

        private Observation(
                Minecraft189ScaffoldSupportPlanner.Candidate candidate,
                double eyeY, double reach) {
            this.targetX = candidate.targetX;
            this.targetY = candidate.targetY;
            this.targetZ = candidate.targetZ;
            this.supportX = candidate.supportX;
            this.supportY = candidate.supportY;
            this.supportZ = candidate.supportZ;
            this.face = candidate.face;
            this.nativeEyeY = eyeY;
            this.conservativeReach = reach;
        }
    }

    private Minecraft189ScaffoldOriginalCandidateProbe() { }

    static Observation observe(Object originalMinecraft) {
        if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeContextAccess)
                || !(originalMinecraft instanceof Minecraft189ScaffoldNativeControllerAccess))
            return null;
        final Minecraft189ScaffoldNativeContext.Witness context =
                Minecraft189ScaffoldNativeContext.capture(
                        (Minecraft189ScaffoldNativeContextAccess)originalMinecraft);
        if (context == null) return null;
        final double eyeY = Minecraft189ScaffoldSourceEye.currentEyeY(context.player);
        final double reach =
                Minecraft189ScaffoldSourceReach.conservativeReach(originalMinecraft);
        if (!Double.isFinite(eyeY) || !Double.isFinite(reach)) return null;
        final Minecraft189ScaffoldWorldLease lease =
                new Minecraft189ScaffoldWorldLease();
        try {
            if (lease.open(context.world) == null) return null;
            final Minecraft189ScaffoldSupportPlanner.Candidate candidate =
                    Minecraft189ScaffoldRayCandidateGate.verify(
                            lease, context.world, context.player,
                            context.nativeCursor, eyeY, reach);
            return candidate == null ? null : new Observation(candidate, eyeY, reach);
        } catch (RuntimeException unavailableNativeSource) {
            return null;
        } finally {
            // The candidate and original World authority die at callback end.
            lease.close();
        }
    }
}
