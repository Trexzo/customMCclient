package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Synthetic reference-identity and fail-closed checks; real source tested separately. */
final class Minecraft189ScaffoldNativeContextTest {
    private static final class Stack implements Minecraft189ItemStackAccess {
        boolean block = true;
        int count = 3;
        public String customMcDisplayName() { return "UNTRUSTED"; }
        public int customMcStackSize() { return count; }
        public int customMcItemDamage() { return 0; }
        public int customMcMaxDamage() { return 0; }
        public boolean customMcIsBlockItem() { return block; }
    }
    private static final class Hotbar implements Minecraft189InventoryHotbarItemsAccess {
        final Minecraft189ItemStackAccess[] stacks = new Minecraft189ItemStackAccess[36];
        int slot, mutations;
        public Minecraft189ItemStackAccess[] customMcHotbarItems(){return stacks;}
        public int customMcSelectedHotbarSlot(){return slot;}
        public void customMcSetSelectedHotbarSlot(int s){mutations++;slot=s;}
    }
    private static final class Player implements Minecraft189PlayerPositionAccess,
            Minecraft189PlayerInventoryAccess, Minecraft189PlayerMovementStateAccess,
            Minecraft189LocalPlayerSafeWalkMarker {
        double x=.5,y=100,z=.5;
        boolean grounded=true;
        final Hotbar hotbar=new Hotbar();
        public double customMcPositionX(){return x;}
        public double customMcPositionY(){return y;}
        public double customMcPositionZ(){return z;}
        public Minecraft189InventoryHotbarAccess customMcInventory(){return hotbar;}
        public boolean customMcOnGround(){return grounded;}
        public boolean customMcSneaking(){return false;}
        public boolean customMcSprinting(){return false;}
    }
    private static final class World implements Minecraft189ScaffoldWorldReadAccess,
            Minecraft189ScaffoldWorldRayTraceAccess {
        public int customMcScaffoldCellCode(int x,int y,int z) { return UNKNOWN; }
    }
    private static final class Cursor implements Minecraft189ScaffoldNativeBlockHitAccess { }
    private static final class Source implements Minecraft189ScaffoldNativeContextAccess {
        Object[] values;
        boolean throwOnRead;
        public Object[] customMcScaffoldNativeContext(){
            if (throwOnRead) throw new IllegalStateException("unavailable original");
            return values;
        }
    }
    private static Source source(){
        final Player p=new Player();
        p.hotbar.stacks[0]=new Stack();
        final Source src=new Source();
        src.values=new Object[]{p,new World(),new Cursor()};
        return src;
    }
    @Test void oneSourceReadPreservesAllThreeOriginalIdentities() {
        Source src=source();
        Minecraft189ScaffoldNativeContext.Witness witness =
                Minecraft189ScaffoldNativeContext.capture(src);
        assertNotNull(witness);
        assertSame(src.values[0],witness.player);
        assertSame(src.values[1],witness.world);
        assertSame(src.values[2],witness.nativeCursor);
        assertEquals(0,((Player)src.values[0]).hotbar.mutations);
        assertEquals(3,((Stack)((Player)src.values[0]).hotbar.stacks[0]).count);
        assertNull(((Cursor)src.values[2]).customMcScaffoldNativeBlockHit(),
                "context capture is not a fake positive BLOCK ray witness");
    }
    @Test void nullMalformedUntransformedAndMenuContextsAreRejected() {
        Source src=source();
        assertNull(Minecraft189ScaffoldNativeContext.capture(null));
        src.values=null;
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        src.values=new Object[]{new Player(),new World()};
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        src.values=new Object[]{new Player(),new World(),new Cursor(),new Object()};
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        src.values=new Object[]{null,null,null};
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        src.values=new Object[]{new Object(),new World(),new Cursor()};
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        src.values=new Object[]{new Player(),new Object(),new Cursor()};
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        src.values=new Object[]{new Player(),new World(),new Object()};
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        src.throwOnRead=true;
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
    }
    @Test void invalidOriginalInventoryWorldOrPositionCannotPass() {
        Source src=source();
        Player player=(Player)src.values[0];
        player.grounded=false;
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        player.grounded=true;
        player.hotbar.slot=9;
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        player.hotbar.slot=0;
        Stack stack=(Stack)player.hotbar.stacks[0];
        stack.block=false;
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        stack.block=true;
        stack.count=0;
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        stack.count=3;
        player.x=Double.NaN;
        assertNull(Minecraft189ScaffoldNativeContext.capture(src));
        player.x=.5;
        assertNotNull(Minecraft189ScaffoldNativeContext.capture(src));
    }
}
