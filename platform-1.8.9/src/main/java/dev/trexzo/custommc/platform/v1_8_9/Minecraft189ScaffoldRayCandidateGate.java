package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V21: read-only consensus of three independently sourced prerequisites.
 *
 * A current World lease proposes a replaceable target and solid neighbour,
 * original MovingObjectPosition supplies a BLOCK hit/face/vector, and
 * original World.rayTraceBlocks must independently hit that exact support.
 * This class never registers Scaffold, changes rotation, calls the controller,
 * sends packets, changes inventory or mutates blocks.
 *
 * The caller MUST own a fresh same-tick original World lease and supply a
 * genuinely source-native eye position and current original crosshair hit.
 * Merely passing synthetic or stale arguments does not certify gameplay.
 */
final class Minecraft189ScaffoldRayCandidateGate {
    private Minecraft189ScaffoldRayCandidateGate() { }

    static Minecraft189ScaffoldSupportPlanner.Candidate verify(
            Minecraft189ScaffoldWorldLease lease,
            Minecraft189ScaffoldWorldReadAccess sourceWorld,
            Minecraft189PlayerPositionAccess localPlayer,
            Minecraft189ScaffoldNativeBlockHitAccess originalCrosshairHit,
            double nativeEyeY, double nativeReach) {
        if (lease == null || sourceWorld == null
                || !(sourceWorld instanceof Minecraft189ScaffoldWorldRayTraceAccess)
                || !(localPlayer instanceof Minecraft189LocalPlayerSafeWalkMarker)
                || !(localPlayer instanceof Minecraft189PlayerInventoryAccess)
                || !(localPlayer instanceof Minecraft189PlayerMovementStateAccess)
                || originalCrosshairHit == null)
            return null;
        try {
            final Minecraft189PlayerMovementStateAccess movement =
                    (Minecraft189PlayerMovementStateAccess)localPlayer;
            if (!movement.customMcOnGround()) return null;
            final Minecraft189InventoryHotbarAccess inventory =
                    ((Minecraft189PlayerInventoryAccess)localPlayer).customMcInventory();
            if (!(inventory instanceof Minecraft189InventoryHotbarItemsAccess))
                return null;
            final Minecraft189InventoryHotbarItemsAccess ownedInventory =
                    (Minecraft189InventoryHotbarItemsAccess)inventory;
            final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                    Minecraft189ScaffoldSelectedHotbar.from(ownedInventory);
            if (selected == null) return null;
            final double x = localPlayer.customMcPositionX();
            final double y = localPlayer.customMcPositionY();
            final double z = localPlayer.customMcPositionZ();
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || !Double.isFinite(nativeEyeY)
                    || nativeEyeY < y || nativeEyeY > y + 2.2D)
                return null;
            final Minecraft189ScaffoldSupportPlanner.Candidate candidate =
                    lease.choose(sourceWorld, true, true, x, y, z,
                            nativeEyeY, true, selected.sourceStackSize, nativeReach);
            if (candidate == null) return null;

            // Exactly one native hit snapshot: never authorize using two
            // potentially different reads of the mutable vanilla cursor.
            final double[] sourceHit = originalCrosshairHit.customMcScaffoldNativeBlockHit();
            if (sourceHit == null || sourceHit.length != 8) return null;
            final double[] hit = sourceHit.clone();
            final Minecraft189ScaffoldNativeBlockHitAccess frozenHit =
                    new Minecraft189ScaffoldNativeBlockHitAccess() {
                        @Override public double[] customMcScaffoldNativeBlockHit() {
                            return hit;
                        }
                    };
            if (!Minecraft189ScaffoldNativeFaceGate.matches(
                    frozenHit, candidate, x, nativeEyeY, z, nativeReach))
                return null;

            // Trace slightly past the original hit point, INTO the support
            // block, so vanilla World.rayTraceBlocks sees the clicked face.
            final double dx = hit[5] - x;
            final double dy = hit[6] - nativeEyeY;
            final double dz = hit[7] - z;
            final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (!Double.isFinite(distance) || distance <= 0.001D)
                return null;
            final double advance = 0.01D / distance;
            final int face;
            switch (candidate.face) {
                case UP:    face = 1; break;
                case NORTH: face = 2; break;
                case SOUTH: face = 3; break;
                case WEST:  face = 4; break;
                case EAST:  face = 5; break;
                default: return null;
            }
            if (!((Minecraft189ScaffoldWorldRayTraceAccess)sourceWorld)
                    .customMcScaffoldRayTraceSupport(
                            x, nativeEyeY, z,
                            hit[5] + dx * advance,
                            hit[6] + dy * advance,
                            hit[7] + dz * advance,
                            candidate.supportX, candidate.supportY,
                            candidate.supportZ, face))
                return null;

            // Both the inventory authority and lease must still be current
            // after native calls. Any changed source identity or item vetoes.
            if (((Minecraft189PlayerInventoryAccess)localPlayer).customMcInventory()
                    != inventory || !movement.customMcOnGround()
                    || Double.compare(x, localPlayer.customMcPositionX()) != 0
                    || Double.compare(y, localPlayer.customMcPositionY()) != 0
                    || Double.compare(z, localPlayer.customMcPositionZ()) != 0)
                return null;
            final Minecraft189ScaffoldSelectedHotbar.Evidence latest =
                    Minecraft189ScaffoldSelectedHotbar.from(ownedInventory);
            if (latest == null || latest.sourceItemStack != selected.sourceItemStack
                    || latest.sourceSlot != selected.sourceSlot
                    || latest.sourceStackSize != selected.sourceStackSize)
                return null;
            final Minecraft189ScaffoldSupportPlanner.Candidate current =
                    lease.choose(sourceWorld, true, true, x, y, z,
                            nativeEyeY, true, latest.sourceStackSize, nativeReach);
            if (current == null || current.generation != candidate.generation
                    || current.targetX != candidate.targetX
                    || current.targetY != candidate.targetY
                    || current.targetZ != candidate.targetZ
                    || current.supportX != candidate.supportX
                    || current.supportY != candidate.supportY
                    || current.supportZ != candidate.supportZ
                    || current.face != candidate.face)
                return null;
            return current;
        } catch (RuntimeException unavailableNativeEvidence) {
            return null;
        }
    }
}
