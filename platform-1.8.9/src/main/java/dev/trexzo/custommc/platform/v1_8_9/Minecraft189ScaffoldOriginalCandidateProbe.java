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

    /**
     * Bounded acceptance-only rejection diagnostics; never used to qualify
     * or authorize a real placement. Independent of the normal probe result.
     * Print ONLY when the opt-in original-game V26 CI stage requests it.
     */
    static String diagnose(Object originalMinecraft) {
        try {
            if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeContextAccess))
                return "missing_original_minecraft_context_accessor";
            if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeControllerAccess))
                return "missing_original_controller_accessor";
            final Minecraft189ScaffoldNativeContext.Witness context =
                    Minecraft189ScaffoldNativeContext.capture(
                            (Minecraft189ScaffoldNativeContextAccess)originalMinecraft);
            if (context == null) return "missing_player_world_cursor_or_selected_item";
            final double x=context.player.customMcPositionX();
            final double y=context.player.customMcPositionY();
            final double z=context.player.customMcPositionZ();
            final double eye=Minecraft189ScaffoldSourceEye.currentEyeY(context.player);
            final double reach=Minecraft189ScaffoldSourceReach.conservativeReach(originalMinecraft);
            if (!Double.isFinite(eye) || !Double.isFinite(reach))
                return "missing_native_eye_or_controller_reach eye="+eye+" reach="+reach;
            final int tileX=(int)Math.floor(x);
            final int tileY=(int)Math.floor(y)-1;
            final int tileZ=(int)Math.floor(z);
            final int target=context.world.customMcScaffoldCellCode(tileX,tileY,tileZ);
            final int below=context.world.customMcScaffoldCellCode(tileX,tileY-1,tileZ);
            final int west=context.world.customMcScaffoldCellCode(tileX-1,tileY,tileZ);
            final Minecraft189ScaffoldWorldLease lease=new Minecraft189ScaffoldWorldLease();
            try {
                if (lease.open(context.world)==null)return "world_lease_unavailable";
                final Minecraft189PlayerMovementStateAccess movement =
                        (Minecraft189PlayerMovementStateAccess)context.player;
                final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                        Minecraft189ScaffoldSelectedHotbar.from(
                                (Minecraft189InventoryHotbarItemsAccess)
                                        ((Minecraft189PlayerInventoryAccess)context.player)
                                                .customMcInventory());
                final int count=selected==null?-1:selected.sourceStackSize;
                final Minecraft189ScaffoldSupportPlanner.Candidate candidate =
                        lease.choose(context.world,true,movement.customMcOnGround(),
                                x,y,z,eye,true,count,reach);
                final double[] hit=context.nativeCursor.customMcScaffoldNativeBlockHit();
                final String hitString=hit==null?"none":java.util.Arrays.toString(hit);
                final String facts=" pos="+x+","+y+","+z+" eye="+eye+" reach="+reach
                        +" ground="+movement.customMcOnGround()
                        +" stack="+count+" cells="+target+","+below+","+west
                        +" hit="+hitString;
                if(candidate==null)return "planner_rejected"+facts;
                if(!Minecraft189ScaffoldNativeFaceGate.matches(context.nativeCursor,
                        candidate,x,eye,z,reach))
                    return "native_cursor_face_or_plane_mismatch candidate="+candidate.face
                            +" support="+candidate.supportX+","+candidate.supportY+","
                            +candidate.supportZ+facts;
                if(Minecraft189ScaffoldRayCandidateGate.verify(lease,context.world,
                        context.player,context.nativeCursor,eye,reach)==null)
                    return "original_world_raytrace_or_lease_rejected"+facts;
                return "positive_candidate_already_available"+facts;
            } finally {
                lease.close();
            }
        } catch(RuntimeException unavailableNativeEvidence) {
            return "native_source_exception_"+unavailableNativeEvidence.getClass().getSimpleName();
        }
    }

    static Observation observe(Object originalMinecraft) {
        return observe(originalMinecraft, false);
    }

    static Observation observe(Object originalMinecraft, boolean allowAdjacentEdge) {
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
                            context.nativeCursor, eyeY, reach,
                            allowAdjacentEdge);
            return candidate == null ? null : new Observation(candidate, eyeY, reach);
        } catch (RuntimeException unavailableNativeSource) {
            return null;
        } finally {
            // The candidate and original World authority die at callback end.
            lease.close();
        }
    }
}
