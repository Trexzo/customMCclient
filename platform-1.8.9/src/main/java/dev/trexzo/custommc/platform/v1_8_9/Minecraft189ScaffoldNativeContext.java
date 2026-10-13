package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V22 read-only classification of three references read together from
 * original Minecraft. This is NOT a game-tick lease or gameplay authorization.
 * Never retain Witness across tick, world replacement, or disconnect.
 * A future tick-owned gate must source-verify native eye position and reach.
 */
final class Minecraft189ScaffoldNativeContext {
    static final class Witness {
        final Minecraft189PlayerPositionAccess player;
        final Minecraft189ScaffoldWorldReadAccess world;
        final Minecraft189ScaffoldNativeBlockHitAccess nativeCursor;

        private Witness(Minecraft189PlayerPositionAccess player,
                        Minecraft189ScaffoldWorldReadAccess world,
                        Minecraft189ScaffoldNativeBlockHitAccess nativeCursor) {
            this.player = player;
            this.world = world;
            this.nativeCursor = nativeCursor;
        }
    }

    private Minecraft189ScaffoldNativeContext() { }

    static Witness capture(Minecraft189ScaffoldNativeContextAccess originalMinecraft) {
        if (originalMinecraft == null) return null;
        try {
            final Object[] values = originalMinecraft.customMcScaffoldNativeContext();
            if (values == null || values.length != 3
                    || !(values[0] instanceof Minecraft189LocalPlayerSafeWalkMarker)
                    || !(values[0] instanceof Minecraft189PlayerPositionAccess)
                    || !(values[0] instanceof Minecraft189PlayerInventoryAccess)
                    || !(values[0] instanceof Minecraft189PlayerMovementStateAccess)
                    || !(values[1] instanceof Minecraft189ScaffoldWorldReadAccess)
                    || !(values[1] instanceof Minecraft189ScaffoldWorldRayTraceAccess)
                    || !(values[2] instanceof Minecraft189ScaffoldNativeBlockHitAccess))
                return null;
            final Minecraft189PlayerPositionAccess player =
                    (Minecraft189PlayerPositionAccess) values[0];
            if (!((Minecraft189PlayerMovementStateAccess)player).customMcOnGround())
                return null;
            final Minecraft189InventoryHotbarAccess hotbar =
                    ((Minecraft189PlayerInventoryAccess)player).customMcInventory();
            if (!(hotbar instanceof Minecraft189InventoryHotbarItemsAccess)
                    || Minecraft189ScaffoldSelectedHotbar.from(
                            (Minecraft189InventoryHotbarItemsAccess)hotbar) == null)
                return null;
            if (!Double.isFinite(player.customMcPositionX())
                    || !Double.isFinite(player.customMcPositionY())
                    || !Double.isFinite(player.customMcPositionZ()))
                return null;
            // The original MOP is not interpreted as an authoritative BLOCK
            // here. The independent V21 native-hit and World gates do that.
            return new Witness(
                    player,
                    (Minecraft189ScaffoldWorldReadAccess) values[1],
                    (Minecraft189ScaffoldNativeBlockHitAccess) values[2]);
        } catch (RuntimeException sourceUnavailable) {
            return null;
        }
    }
}
