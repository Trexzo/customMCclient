package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Synthetic fail-closed checks; genuine native-game certification is separate. */
final class Minecraft189ScaffoldRayCandidateGateTest {
    private static final class Stack implements Minecraft189ItemStackAccess {
        boolean block = true;
        int count = 8;
        public String customMcDisplayName(){ return "UNTRUSTED"; }
        public int customMcStackSize(){ return count; }
        public int customMcItemDamage(){ return 0; }
        public int customMcMaxDamage(){ return 0; }
        public boolean customMcIsBlockItem(){ return block; }
    }
    private static final class Hotbar implements Minecraft189InventoryHotbarItemsAccess {
        final Minecraft189ItemStackAccess[] slots = new Minecraft189ItemStackAccess[36];
        int selected, writes;
        public Minecraft189ItemStackAccess[] customMcHotbarItems(){return slots;}
        public int customMcSelectedHotbarSlot(){return selected;}
        public void customMcSetSelectedHotbarSlot(int s){ selected=s; writes++; }
    }
    private static final class Player implements Minecraft189PlayerPositionAccess,
            Minecraft189PlayerInventoryAccess, Minecraft189PlayerMovementStateAccess,
            Minecraft189LocalPlayerSafeWalkMarker {
        double x=.5,y=100,z=.5;
        boolean grounded=true;
        final Hotbar inv=new Hotbar();
        public double customMcPositionX(){return x;}
        public double customMcPositionY(){return y;}
        public double customMcPositionZ(){return z;}
        public Minecraft189InventoryHotbarAccess customMcInventory(){return inv;}
        public boolean customMcOnGround(){return grounded;}
        public boolean customMcSneaking(){return false;}
        public boolean customMcSprinting(){return false;}
    }
    private static final class World implements Minecraft189ScaffoldWorldReadAccess,
            Minecraft189ScaffoldWorldRayTraceAccess {
        int reads,rays;
        boolean loaded=true, rayValid=true, revokeDuringRay;
        Minecraft189ScaffoldWorldLease lease;
        public int customMcScaffoldCellCode(int x,int y,int z){
            reads++;
            if (!loaded) return UNKNOWN;
            if (x==0 && y==99 && z==0) return AIR;
            if (x==0 && y==98 && z==0) return FULL_SOLID;
            return UNKNOWN;
        }
        public boolean customMcScaffoldRayTraceSupport(
                double sx,double sy,double sz,double ex,double ey,double ez,
                int x,int y,int z,int face){
            rays++;
            if (revokeDuringRay) lease.invalidate();
            return rayValid && sx==.5 && sy==101.62 && sz==.5
                    && ex==.5 && ey>=98.5 && ey<=98.9 && ez==.5
                    && x==0 && y==98 && z==0 && face==1;
        }
    }
    private static final class Hit implements Minecraft189ScaffoldNativeBlockHitAccess {
        double[] sample={1,0,98,0,1,.5,99,.5};
        public double[] customMcScaffoldNativeBlockHit(){return sample;}
    }
    private static Minecraft189ScaffoldWorldLease opened(World w){
        Minecraft189ScaffoldWorldLease lease=new Minecraft189ScaffoldWorldLease();
        assertNotNull(lease.open(w));
        w.lease=lease;
        return lease;
    }
    private static Player player(){
        Player p=new Player();
        p.inv.slots[0]=new Stack();
        return p;
    }
    private static Minecraft189ScaffoldSupportPlanner.Candidate verify(
            Minecraft189ScaffoldWorldLease lease,World world,Player p,Hit h){
        return Minecraft189ScaffoldRayCandidateGate.verify(
                lease,world,p,h,101.62,4.5);
    }

    @Test void validSameCallbackCandidateNeedsBothIndependentNativeRaySources(){
        Player p=player(); World w=new World(); Hit h=new Hit();
        Minecraft189ScaffoldWorldLease lease=opened(w);
        Minecraft189ScaffoldSupportPlanner.Candidate candidate=verify(lease,w,p,h);
        assertNotNull(candidate);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Face.UP,candidate.face);
        assertEquals(0,candidate.targetX); assertEquals(99,candidate.targetY);
        assertEquals(98,candidate.supportY);
        assertEquals(1,w.rays);
        assertTrue(w.reads>0);
        assertEquals(0,p.inv.writes);
        assertEquals(8,((Stack)p.inv.slots[0]).count);
        assertNotNull(verify(lease,w,p,h),"read-only proof never places or consumes");
    }
    @Test void staleWorldUnavailableChunkAndWrongCursorCannotProduceCandidate(){
        Player p=player(); World w=new World(); Hit h=new Hit();
        Minecraft189ScaffoldWorldLease lease=opened(w);
        lease.invalidate();
        assertNull(verify(lease,w,p,h));
        assertEquals(0,w.rays);
        lease.open(w);
        w.loaded=false;
        assertNull(verify(lease,w,p,h));
        w.loaded=true;
        h.sample[4]=2;
        assertNull(verify(lease,w,p,h),"wrong native face must veto");
        h.sample=new double[]{1,0,98,0,1,.5,98.5,.5};
        assertNull(verify(lease,w,p,h),"wrong hit plane must veto");
        h.sample=new double[]{0,0,98,0,1,.5,99,.5};
        assertNull(verify(lease,w,p,h),"ENTITY or MISS cannot be BLOCK");
        h.sample=new double[]{1,0,98,0,1,.5,99,.5};
        w.rayValid=false;
        assertNull(verify(lease,w,p,h),"native World ray miss vetoes matching cursor");
    }
    @Test void worldRevocationDuringSourceRayAndInventoryChangesFailClosed(){
        Player p=player(); World w=new World(); Hit h=new Hit();
        Minecraft189ScaffoldWorldLease lease=opened(w);
        w.revokeDuringRay=true;
        assertNull(verify(lease,w,p,h),"lease revoked by World change");
        assertNull(verify(lease,w,p,h),"stale lease remains invalid");
        w.revokeDuringRay=false;
        lease.open(w);
        Stack stack=(Stack)p.inv.slots[0];
        stack.block=false;
        assertNull(verify(lease,w,p,h));
        stack.block=true; stack.count=0;
        assertNull(verify(lease,w,p,h));
        stack.count=8; p.inv.selected=9;
        assertNull(verify(lease,w,p,h),"GUI slot outside hotbar");
        p.inv.selected=0; p.grounded=false;
        assertNull(verify(lease,w,p,h));
        p.grounded=true;
        assertNull(Minecraft189ScaffoldRayCandidateGate.verify(
                lease,w,p,h,Double.NaN,4.5));
        assertNull(Minecraft189ScaffoldRayCandidateGate.verify(
                lease,w,p,h,101.62,5.0),"reach outside original bounds");
        assertNotNull(verify(lease,w,p,h));
    }
    @Test void missingOriginalWorldRayCapabilityOrCursorFailsClosed(){
        Player p=player(); World w=new World(); Hit h=new Hit();
        Minecraft189ScaffoldWorldLease lease=opened(w);
        assertNull(Minecraft189ScaffoldRayCandidateGate.verify(
                lease,w,p,null,101.62,4.5));
        assertNull(Minecraft189ScaffoldRayCandidateGate.verify(
                lease,w,p,h,100.0,4.5),"eye must match actual source hit reach");
        assertNull(Minecraft189ScaffoldRayCandidateGate.verify(
                null,w,p,h,101.62,4.5));
        assertNull(Minecraft189ScaffoldRayCandidateGate.verify(
                lease,null,p,h,101.62,4.5));
    }
}
