package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldNativeTickParityTest {
    private static final class Stack implements Minecraft189ItemStackAccess {
        boolean isBlock = true;
        int count = 8;
        public String customMcDisplayName() { return "UNTRUSTED"; }
        public int customMcStackSize() { return count; }
        public int customMcItemDamage() { return 0; }
        public int customMcMaxDamage() { return 0; }
        public boolean customMcIsBlockItem() { return isBlock; }
    }
    private static final class Hotbar implements Minecraft189InventoryHotbarItemsAccess {
        final Minecraft189ItemStackAccess[] slots = new Minecraft189ItemStackAccess[36];
        int selected;
        int mutations;
        public Minecraft189ItemStackAccess[] customMcHotbarItems() { return slots; }
        public int customMcSelectedHotbarSlot() { return selected; }
        public void customMcSetSelectedHotbarSlot(int slot) { mutations++; selected=slot; }
    }
    private static final class Player implements Minecraft189PlayerPositionAccess,
            Minecraft189PlayerInventoryAccess, Minecraft189LocalPlayerSafeWalkMarker,
            Minecraft189PlayerMovementStateAccess {
        double x=0.5,y=100.0,z=0.5;
        boolean onGround=true;
        final Hotbar hotbar = new Hotbar();
        public double customMcPositionX(){return x;}
        public double customMcPositionY(){return y;}
        public double customMcPositionZ(){return z;}
        public Minecraft189InventoryHotbarAccess customMcInventory(){return hotbar;}
        public boolean customMcOnGround(){return onGround;}
        public boolean customMcSneaking(){return false;}
        public boolean customMcSprinting(){return false;}
    }
    private static final class World implements Minecraft189ScaffoldWorldReadAccess,
            Minecraft189WorldEntityPositionsAccess {
        int reads;
        boolean loaded=true;
        public int customMcScaffoldCellCode(int x,int y,int z) {
            reads++;
            return loaded && x==0 && y==99 && z==0 ? FULL_SOLID : UNKNOWN;
        }
        public double[] customMcLoadedEntityPositions(){return new double[0];}
    }
    private static final class ForeignPlayer implements Minecraft189PlayerPositionAccess {
        public double customMcPositionX(){return 0.5;}
        public double customMcPositionY(){return 100.0;}
        public double customMcPositionZ(){return 0.5;}
    }
    private static Minecraft189ScaffoldNativeTickParity ready(Player player,World world) {
        Minecraft189ScaffoldNativeTickParity collector =
                new Minecraft189ScaffoldNativeTickParity();
        collector.beginTick();
        collector.observePlayer(player);
        collector.observeInventory(player);
        collector.observeWorld(world);
        return collector;
    }

    @Test void exactSameOriginalPlayerWorldAndBlockStackProduceOneReadOnlyWitness() {
        Player player=new Player();
        Stack stack=new Stack();
        player.hotbar.slots[0]=stack;
        World world=new World();
        Minecraft189ScaffoldNativeTickParity collector=ready(player,world);
        Minecraft189ScaffoldNativeTickParity.Witness witness=collector.consume();
        assertNotNull(witness);
        assertEquals(1L,witness.tick);
        assertEquals(0,witness.selectedSlot);
        assertEquals(8,witness.originalStackSize);
        assertEquals(0,witness.playerX);
        assertEquals(99,witness.supportY);
        assertEquals(0,witness.playerZ);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID,
                witness.supportCell);
        assertNull(collector.consume(),"one witness per original game tick");
        assertEquals(0,player.hotbar.mutations,"Scaffold parity must not switch hotbar");
        assertEquals(8,stack.count,"Scaffold parity must not consume source stack");
        assertTrue(world.reads>0);
    }

    @Test void exactTickBoundaryInvalidatesPlayerHotbarAndWorldAuthority() {
        Player player=new Player();
        player.hotbar.slots[0]=new Stack();
        World world=new World();
        Minecraft189ScaffoldNativeTickParity collector=ready(player,world);
        collector.beginTick();
        assertNull(collector.consume(),"stale callback trio must be vetoed");
        collector.observeWorld(world);
        assertNull(collector.consume());
        collector.observePlayer(player);
        assertNull(collector.consume());
        collector.observeInventory(player);
        Minecraft189ScaffoldNativeTickParity.Witness witness=collector.consume();
        assertNotNull(witness);
        assertEquals(2L,witness.tick);
    }

    @Test void unrelatedInventoryOwnerAndForeignPlayerCannotBorrowNativeEvidence() {
        Player original=new Player(), impostor=new Player();
        original.hotbar.slots[0]=new Stack();
        impostor.hotbar.slots[0]=new Stack();
        World world=new World();
        Minecraft189ScaffoldNativeTickParity collector =
                new Minecraft189ScaffoldNativeTickParity();
        collector.beginTick();
        collector.observePlayer(original);
        collector.observeInventory(impostor);
        collector.observeWorld(world);
        assertNull(collector.consume());
        assertEquals(0,world.reads,"wrong inventory identity must veto world reads");
        collector.observeInventory(original);
        assertNotNull(collector.consume());
        collector.beginTick();
        collector.observePlayer(new ForeignPlayer());
        collector.observeInventory(original);
        collector.observeWorld(world);
        assertNull(collector.consume(),"unmarked nonlocal player cannot be treated as original");
    }

    @Test void unavailableGroundSupportStackAndPlayerFailClosed() {
        Player player=new Player();
        Stack stack=new Stack();
        player.hotbar.slots[0]=stack;
        World world=new World();
        Minecraft189ScaffoldNativeTickParity collector=ready(player,world);
        world.loaded=false;
        assertNull(collector.consume(),"unknown native chunk cannot authorize a witness");
        world.loaded=true;
        player.onGround=false;
        collector.observePlayer(player);
        assertNull(collector.consume(),"player not on ground");
        player.onGround=true;
        collector.observePlayer(player);
        stack.isBlock=false;
        assertNull(collector.consume(),"non-block native Item");
        stack.isBlock=true;
        stack.count=0;
        assertNull(collector.consume(),"empty ItemBlock");
        stack.count=8;
        player.x=Double.NaN;
        assertNull(collector.consume(),"nonfinite original position");
        player.x=0.5;
        collector.observePlayer(player);
        assertNotNull(collector.consume());
    }

    @Test void sameCycleWorldReplacementRequiresFreshSourceRead() {
        Player player=new Player();
        player.hotbar.slots[0]=new Stack();
        World one=new World(),two=new World();
        Minecraft189ScaffoldNativeTickParity collector=ready(player,one);
        collector.observeWorld(two);
        Minecraft189ScaffoldNativeTickParity.Witness witness=collector.consume();
        assertNotNull(witness);
        assertEquals(0,one.reads,"replaced World must never be read after replacement");
        assertTrue(two.reads>0);
        collector.beginTick();
        collector.observePlayer(player);
        collector.observeInventory(player);
        collector.observeWorld(null);
        assertNull(collector.consume());
    }
}
