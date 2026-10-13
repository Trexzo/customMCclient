package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Observation-only join of three original Minecraft callback owners.
 *
 * The local player, its inventory and the loaded World must all publish
 * within ONE explicit game-tick epoch. No evidence survives the next epoch.
 * This class never moves the player, swaps slots or dispatches placement.
 */
final class Minecraft189ScaffoldNativeTickParity {
    static final class Witness {
        final long tick;
        final int selectedSlot;
        final int originalStackSize;
        final int playerX, supportY, playerZ;
        final Minecraft189ScaffoldSupportPlanner.Cell supportCell;

        Witness(long tick, int slot, int stackSize, int x, int y, int z,
                Minecraft189ScaffoldSupportPlanner.Cell cell) {
            this.tick = tick;
            this.selectedSlot = slot;
            this.originalStackSize = stackSize;
            this.playerX = x;
            this.supportY = y;
            this.playerZ = z;
            this.supportCell = cell;
        }
    }

    private final Minecraft189ScaffoldWorldLease lease =
            new Minecraft189ScaffoldWorldLease();
    private long tick;
    private Minecraft189PlayerPositionAccess player;
    private Minecraft189PlayerInventoryAccess inventoryPlayer;
    private Minecraft189ScaffoldWorldReadAccess world;
    private boolean originalOnGround;
    private boolean emitted;

    synchronized void beginTick() {
        lease.invalidate();
        player = null;
        inventoryPlayer = null;
        world = null;
        originalOnGround = false;
        emitted = false;
        if (tick != Long.MAX_VALUE) tick++;
    }

    synchronized void observePlayer(final Minecraft189PlayerPositionAccess source) {
        player = null;
        originalOnGround = false;
        if (source instanceof Minecraft189LocalPlayerSafeWalkMarker
                && source instanceof Minecraft189PlayerMovementStateAccess) {
            try {
                final double x = source.customMcPositionX();
                final double y = source.customMcPositionY();
                final double z = source.customMcPositionZ();
                final boolean ground =
                        ((Minecraft189PlayerMovementStateAccess)source).customMcOnGround();
                if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)
                        && Math.abs(x) < 29999984 && Math.abs(z) < 29999984
                        && y >= 2.0D && y < 255.0D && ground) {
                    player = source;
                    originalOnGround = true;
                }
            } catch (RuntimeException unavailablePlayer) {
                player = null;
            }
        }
    }

    synchronized void observeInventory(final Minecraft189PlayerInventoryAccess source) {
        inventoryPlayer = source;
    }

    synchronized void observeWorld(final Minecraft189WorldEntityPositionsAccess source) {
        world = source instanceof Minecraft189ScaffoldWorldReadAccess
                ? (Minecraft189ScaffoldWorldReadAccess)source : null;
        lease.invalidate();
    }

    /**
     * A successful witness contains source-native support evidence only, NOT
     * a placement candidate. Null source, cross-tick, wrong player identity,
     * absent original block stack, detached World and air floor all veto it.
     */
    synchronized Witness consume() {
        if (tick <= 0 || emitted || !originalOnGround || player == null
                || inventoryPlayer == null || world == null
                || player != inventoryPlayer)
            return null;
        try {
            final Minecraft189InventoryHotbarAccess hotbar =
                    inventoryPlayer.customMcInventory();
            if (!(hotbar instanceof Minecraft189InventoryHotbarItemsAccess))
                return null;
            final Minecraft189ScaffoldSelectedHotbar.Evidence item =
                    Minecraft189ScaffoldSelectedHotbar.from(
                            (Minecraft189InventoryHotbarItemsAccess)hotbar);
            if (item == null) return null;
            final double px = player.customMcPositionX();
            final double py = player.customMcPositionY();
            final double pz = player.customMcPositionZ();
            if (!Double.isFinite(px) || !Double.isFinite(py) || !Double.isFinite(pz)
                    || Math.abs(px) >= 29999984.0D || Math.abs(pz) >= 29999984.0D
                    || py < 2.0D || py >= 255.0D)
                return null;
            final int x = (int)Math.floor(px);
            final int y = (int)Math.floor(py) - 1;
            final int z = (int)Math.floor(pz);
            final Minecraft189ScaffoldSupportPlanner.WorldEvidence owned = lease.open(world);
            if (owned == null) return null;
            if (owned.cell(x,y,z) != Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID)
                return null;
            final Witness found = new Witness(tick,item.sourceSlot,
                    item.sourceStackSize,x,y,z,
                    Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID);
            emitted = true;
            return found;
        } catch (RuntimeException unavailableNativeSource) {
            return null;
        } finally {
            // No World reference can be retained past this observation.
            lease.invalidate();
        }
    }
}
