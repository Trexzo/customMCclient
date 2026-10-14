package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Isolated geometry/authority regression, not real-game gameplay proof.
 * The real original Minecraft ledge and cursor stage remains mandatory in CI.
 */
final class Minecraft189ScaffoldOriginalCandidateProbeTest {
    private static final class Stack implements Minecraft189ItemStackAccess {
        int count=8;
        public String customMcDisplayName(){return "UNTRUSTED";}
        public int customMcStackSize(){return count;}
        public int customMcItemDamage(){return 0;}
        public int customMcMaxDamage(){return 0;}
        public boolean customMcIsBlockItem(){return true;}
    }
    private static final class Hotbar implements Minecraft189InventoryHotbarItemsAccess {
        final Minecraft189ItemStackAccess[] contents=new Minecraft189ItemStackAccess[36];
        int slot,writes;
        public Minecraft189ItemStackAccess[] customMcHotbarItems(){return contents;}
        public int customMcSelectedHotbarSlot(){return slot;}
        public void customMcSetSelectedHotbarSlot(int s){slot=s;writes++;}
    }
    private static final class Player implements Minecraft189PlayerPositionAccess,
            Minecraft189LocalPlayerSafeWalkMarker, Minecraft189PlayerMovementStateAccess,
            Minecraft189PlayerInventoryAccess, Minecraft189ScaffoldNativeEyeAccess {
        final Hotbar inventory=new Hotbar();
        double x=1.05,y=100,z=.5;
        boolean onGround=true;
        public double customMcPositionX(){return x;}
        public double customMcPositionY(){return y;}
        public double customMcPositionZ(){return z;}
        public boolean customMcOnGround(){return onGround;}
        public boolean customMcSneaking(){return false;}
        public boolean customMcSprinting(){return false;}
        public Minecraft189InventoryHotbarAccess customMcInventory(){return inventory;}
        public float customMcOriginalEyeHeight(){return 1.62F;}
    }
    private static final class World implements Minecraft189ScaffoldWorldReadAccess,
            Minecraft189ScaffoldWorldRayTraceAccess {
        boolean loaded=true,rayValid=true,placed,secondPlaced;
        int reads,rays;
        public int customMcScaffoldCellCode(int x,int y,int z){
            reads++;
            if(!loaded)return UNKNOWN;
            if(x==1&&y==99&&z==0)return placed?FULL_SOLID:AIR;
            if(x==2&&y==99&&z==0)return secondPlaced?FULL_SOLID:AIR;
            if(x==0&&y==99&&z==0)return FULL_SOLID;
            return UNKNOWN;
        }
        public boolean customMcScaffoldRayTraceSupport(
                double sx,double sy,double sz,double ex,double ey,double ez,
                int x,int y,int z,int face){
            rays++;
            return rayValid && y==99 && z==0 && face==5
                    && ey>99 && ey<100
                    && ((x==0 && sx==1.05D && ex<0.998D && ex>0.98D)
                     || (x==1 && sx==2.05D && ex<1.998D && ex>1.98D));
        }
    }
    private static final class Cursor implements Minecraft189ScaffoldNativeBlockHitAccess {
        double[] sample={1,0,99,0,5,1.0,99.5,.5};
        public double[] customMcScaffoldNativeBlockHit(){return sample;}
    }
    private static final class Controller implements Minecraft189VanillaReachAccess,
            Minecraft189ScaffoldVanillaPlaceAccess {
        int clicks;
        boolean originalReturn=true;
        Object lastPlayer,lastWorld,lastStack,lastHit;
        public float customMcVanillaBlockReachDistance(){return 5.0F;}
        public boolean customMcVanillaExtendedReach(){return true;}
        public boolean customMcOriginalRightClick(Object player,Object world,
                Object stack,Object hit) {
            clicks++;
            lastPlayer=player;
            lastWorld=world;
            lastStack=stack;
            lastHit=hit;
            return originalReturn;
        }
    }
    private static final class Minecraft implements Minecraft189ScaffoldNativeContextAccess,
            Minecraft189ScaffoldNativeControllerAccess {
        Player player=new Player();
        World world=new World();
        final Cursor cursor=new Cursor();
        Object controller=new Controller();
        public Object[] customMcScaffoldNativeContext(){
            return new Object[]{player,world,cursor};
        }
        public Object customMcScaffoldNativeController(){return controller;}
    }

    @Test void exactOriginalSourcesAgreeOnSupportedReplaceableEdge(){
        Minecraft game=new Minecraft();
        Stack stone=new Stack();
        game.player.inventory.contents[0]=stone;
        Minecraft189ScaffoldOriginalCandidateProbe.Observation found =
                Minecraft189ScaffoldOriginalCandidateProbe.observe(game);
        assertNotNull(found);
        assertEquals(1,found.targetX);
        assertEquals(99,found.targetY);
        assertEquals(0,found.targetZ);
        assertEquals(0,found.supportX);
        assertEquals(99,found.supportY);
        assertEquals(0,found.supportZ);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Face.EAST,found.face);
        assertEquals(4.5D,found.conservativeReach);
        assertTrue(game.world.reads>0);
        assertEquals(1,game.world.rays);
        assertEquals(0,game.player.inventory.writes);
        assertEquals(8,stone.count,"candidate observation must not place or consume");
    }
    @Test void nativeControllerIsInvokedOnlyForExplicitlyEnabledVerifiedSources(){
        Minecraft game=new Minecraft();
        Stack stone=new Stack();
        game.player.inventory.contents[0]=stone;
        Controller nativeController=(Controller)game.controller;
        assertNull(Minecraft189ScaffoldNativePlacementAttempt.attempt(game,false));
        assertEquals(0,nativeController.clicks,"disabled never reaches vanilla controller");
        Minecraft189ScaffoldNativePlacementAttempt.Result result =
                Minecraft189ScaffoldNativePlacementAttempt.attempt(game,true);
        assertNotNull(result);
        assertTrue(result.originalControllerAccepted);
        assertEquals(1,result.targetX);
        assertEquals(99,result.targetY);
        assertEquals(0,result.supportX);
        assertEquals(8,result.originalStackSize);
        assertEquals(1,nativeController.clicks);
        assertSame(game.player,nativeController.lastPlayer);
        assertSame(game.world,nativeController.lastWorld);
        assertSame(stone,nativeController.lastStack);
        assertSame(game.cursor,nativeController.lastHit);
        assertEquals(8,stone.count,"synthetic source never invents server stack consumption");
        assertEquals(0,game.player.inventory.writes);
        nativeController.originalReturn=false;
        Minecraft189ScaffoldNativePlacementAttempt.Result veto =
                Minecraft189ScaffoldNativePlacementAttempt.attempt(game,true);
        assertNotNull(veto);
        assertFalse(veto.originalControllerAccepted,
                "controller returning false is not server block proof");
        assertEquals(2,nativeController.clicks);
        game.world.rayValid=false;
        assertNull(Minecraft189ScaffoldNativePlacementAttempt.attempt(game,true));
        assertEquals(2,nativeController.clicks,"failed re-ray must not call controller");
        game.world.rayValid=true;
        game.cursor.sample[4]=4;
        assertNull(Minecraft189ScaffoldNativePlacementAttempt.attempt(game,true));
        assertEquals(2,nativeController.clicks,"wrong source face must not place");
        game.cursor.sample[4]=5;
        game.player.onGround=false;
        assertNull(Minecraft189ScaffoldNativePlacementAttempt.attempt(game,true));
        assertEquals(2,nativeController.clicks);
        game.player.onGround=true;
        game.player.inventory.contents[0]=null;
        assertNull(Minecraft189ScaffoldNativePlacementAttempt.attempt(game,true));
        assertEquals(2,nativeController.clicks);
    }

    @Test void originalClientReadbackNeedsBothPlacedTargetAndExactlyOneItemUsed(){
        Minecraft game=new Minecraft();
        Stack stack=new Stack();
        game.player.inventory.contents[0]=stack;
        Minecraft189ScaffoldNativePlacementAttempt.Result attempted =
                Minecraft189ScaffoldNativePlacementAttempt.attempt(game,true);
        assertNotNull(attempted);
        assertTrue(attempted.originalControllerAccepted);
        assertFalse(Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(game,attempted),
                "successful native controller return is NOT server outcome");
        game.world.placed=true;
        assertFalse(Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(game,attempted),
                "WorldClient stone alone cannot certify survival consumption");
        game.world.placed=false;
        stack.count=7;
        assertFalse(Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(game,attempted),
                "stack decrement alone cannot certify block placement");
        game.world.placed=true;
        assertTrue(Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(game,attempted));
        stack.count=6;
        assertFalse(Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(game,attempted),
                "two items consumed must not count as exactly one placement");
        stack.count=7;
        game.player.inventory.slot=1;
        game.player.inventory.contents[1]=new Stack();
        assertFalse(Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(game,attempted),
                "different selected hotbar source may not claim old debit");
    }

    @Test void sourceEpochForbidsCrossWorldAndCrossPlayerReceiptReuse(){
        Minecraft game=new Minecraft();
        Stack stone=new Stack();
        game.player.inventory.contents[0]=stone;
        Minecraft189ScaffoldSourceEpoch epoch=new Minecraft189ScaffoldSourceEpoch();
        long receiptGeneration=epoch.observe(game);
        assertTrue(receiptGeneration>0);
        assertEquals(receiptGeneration,epoch.observe(game));

        Minecraft189ScaffoldNativePlacementAttempt.Result receipt =
                Minecraft189ScaffoldNativePlacementAttempt.attempt(game,true);
        assertNotNull(receipt);
        assertTrue(receipt.originalControllerAccepted);
        stone.count=7;
        game.world.placed=true;
        assertTrue(epoch.confirmsSameSourceReadback(game,receiptGeneration,receipt),
                "same original World/player source can prove a real one-item receipt");

        final World originalWorld=game.world;
        game.world=new World();
        game.world.placed=true;
        assertTrue(Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(game,receipt),
                "identical block coordinates alone cannot identify original World");
        assertFalse(epoch.confirmsSameSourceReadback(game,receiptGeneration,receipt),
                "a different World with identical stone and inventory is not the same receipt");
        game.world=originalWorld;
        assertFalse(epoch.confirmsSameSourceReadback(game,receiptGeneration,receipt),
                "returning to a former World must not resurrect an expired receipt");

        long secondGeneration=epoch.observe(game);
        assertTrue(secondGeneration>receiptGeneration);
        game.player=new Player();
        Stack otherStack=new Stack();
        otherStack.count=7;
        game.player.inventory.contents[0]=otherStack;
        assertTrue(Minecraft189ScaffoldPlacementReadback
                .confirmsClientWorldAndSurvivalStack(game,receipt));
        assertFalse(epoch.confirmsSameSourceReadback(game,secondGeneration,receipt),
                "replacement player with matching inventory may not inherit receipt");
    }

    @Test void armedScaffoldWorldLeaseAllowsInitialLoadingThenRevokesDisconnect() {
        Minecraft game=new Minecraft();
        Minecraft189ScaffoldActivationSource lease=
                new Minecraft189ScaffoldActivationSource();
        World original=game.world;
        game.world=null;
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.WAITING,
                lease.observe(game),"initial menu without World cannot consume activation");
        game.world=original;
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.SAME,
                lease.observe(game));
        assertTrue(lease.witnessed());

        // Cursor, inventory and onGround must NOT define source identity:
        // place-readiness may legitimately change without replacing World.
        game.player.inventory.contents[0]=null;
        game.player.onGround=false;
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.SAME,
                lease.observe(game));

        game.world=null;
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.REVOKED,
                lease.observe(game),"disconnect cancels arm even before first click");
        game.world=original;
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.REVOKED,
                lease.observe(game),"same World returning cannot revive prior arm");

        lease.reset(); // only new explicit module activation
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.SAME,
                lease.observe(game));
        game.world=new World();
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.REVOKED,
                lease.observe(game),"World replacement cannot inherit old arm");
    }

    @Test void armedScaffoldPlayerOrMinecraftReplacementRevokesAuthority() {
        Minecraft game=new Minecraft();
        Minecraft189ScaffoldActivationSource lease=
                new Minecraft189ScaffoldActivationSource();
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.SAME,
                lease.observe(game));
        game.player=new Player();
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.REVOKED,
                lease.observe(game),"player replacement must invalidate armed one-shot");

        lease.reset();
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.SAME,
                lease.observe(game));
        Minecraft secondGame=new Minecraft();
        secondGame.player=game.player;
        secondGame.world=game.world;
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.REVOKED,
                lease.observe(secondGame),"new Minecraft host cannot inherit an arm");
        lease.reset();
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.WAITING,
                lease.observe(null));
        assertEquals(Minecraft189ScaffoldActivationSource.Verdict.SAME,
                lease.observe(secondGame));
    }

    @Test void realScaffoldModuleMustBeEnabledAndConsumesOnlyOneAttemptPerEnable(){
        Minecraft game=new Minecraft();
        Stack stone=new Stack();
        game.player.inventory.contents[0]=stone;
        Controller nativeController=(Controller)game.controller;
        Minecraft189ScaffoldModule module=new Minecraft189ScaffoldModule();
        assertEquals("movement.scaffold",module.id());
        assertFalse(module.active());
        assertFalse(module.armed());
        assertNull(module.attemptOnce(game));
        assertEquals(0,nativeController.clicks,"disabled module never calls vanilla");

        module.onEnable();
        assertTrue(module.active());
        assertTrue(module.armed());
        final long firstActivation=module.activation();
        assertTrue(firstActivation>0);
        game.world.rayValid=false;
        assertNull(module.attemptOnce(game),
                "invalid original ray must not consume the arm");
        assertTrue(module.armed());
        assertEquals(0,nativeController.clicks);
        game.world.rayValid=true;
        assertNotNull(module.attemptOnce(game));
        assertFalse(module.armed());
        assertEquals(1,nativeController.clicks);
        assertNull(module.attemptOnce(game));
        assertEquals(1,nativeController.clicks,
                "a second runTick cannot place again on one enable");
        // Direct duplicate lifecycle invocation cannot consume a second
        // grant. ModuleController also guards the same transition.
        module.onEnable();
        assertEquals(firstActivation,module.activation());
        assertFalse(module.armed());
        assertNull(module.attemptOnce(game));
        assertEquals(1,nativeController.clicks);

        module.onDisable();
        assertFalse(module.active());
        assertEquals(0L,module.activation());
        assertNull(module.attemptOnce(game));
        assertEquals(1,nativeController.clicks);
        module.onEnable();
        assertTrue(module.activation()>firstActivation,
                "new enable must never reuse an outstanding receipt generation");
        nativeController.originalReturn=false;
        Minecraft189ScaffoldNativePlacementAttempt.Result veto=module.attemptOnce(game);
        assertNotNull(veto);
        assertFalse(veto.originalControllerAccepted);
        assertFalse(module.armed(),
                "vanilla false is a consumed attempt, never a retry loop");
        assertEquals(2,nativeController.clicks);
    }

    @Test void v36ModuleOwnedSequenceRequiresGenuineFreshTargetAfterFirstReceipt() {
        Minecraft game=new Minecraft();
        Stack stone=new Stack();
        game.player.inventory.contents[0]=stone;
        Controller controller=(Controller)game.controller;
        Minecraft189ScaffoldModule module=new Minecraft189ScaffoldModule();
        module.onEnable();
        assertFalse(module.beginRepeatProof(game,10L,9,2),
                "a 9-block batch must not bypass the bounded eight-block maximum");
        assertTrue(module.beginRepeatProof(game,10L,2,2));
        assertFalse(module.armed(),"repeat proof consumes the normal V35 one-shot arm");
        assertNull(module.attemptOnce(game),"repeat and one-shot can never click together");
        Minecraft189ScaffoldNativePlacementAttempt.Result first =
                module.repeatProofAttempt(game,10L);
        assertNotNull(first);
        assertTrue(first.originalControllerAccepted);
        assertEquals(1,controller.clicks);
        assertNull(module.repeatProofAttempt(game,11L),
                "pending original receipt prevents a second vanilla click");
        assertFalse(module.repeatProofReceipt(game,11L),
                "successful controller return alone is no world/inventory receipt");
        game.world.placed=true;
        assertFalse(module.repeatProofReceipt(game,12L),
                "client World stone without exactly one consumed stone is not a receipt");
        stone.count=7;
        assertTrue(module.repeatProofReceipt(game,12L));
        assertEquals(1,module.repeatProofAttemptedCount());

        assertNull(module.repeatProofAttempt(game,13L),"pacing holds after receipt");
        assertNull(module.repeatProofAttempt(game,14L),
                "same original target cannot be re-clicked after the first receipt");
        assertEquals(1,controller.clicks);

        // Fresh original World/cursor/player and selected-stack evidence are
        // independently re-probed. No synthetic packet, inventory mutation,
        // click, rotation or block placement occurs inside the coordinator.
        game.player.x=2.05D;
        game.cursor.sample=new double[]{1,1,99,0,5,2.0D,99.5D,.5D};
        Minecraft189ScaffoldNativePlacementAttempt.Result second =
                module.repeatProofAttempt(game,15L);
        assertNotNull(second);
        assertEquals(2,second.targetX);
        assertEquals(1,second.supportX);
        assertEquals(7,second.originalStackSize);
        assertEquals(2,controller.clicks,"second controller call needs a DIFFERENT target");
        assertFalse(module.repeatProofReceipt(game,16L));
        game.world.secondPlaced=true;
        stone.count=6;
        assertTrue(module.repeatProofReceipt(game,17L));
        assertTrue(module.repeatProofStopped(),
                "two successful original client receipts exhaust the two-action cap");
        assertEquals(2,module.repeatProofAttemptedCount());
        assertNull(module.repeatProofAttempt(game,23L));
        assertEquals(2,controller.clicks);
        module.onDisable();
        assertNull(module.repeatProofAttempt(game,24L));
    }

    @Test void v37TransientAirborneTickRetainsExactWorldPlayerLeaseButDoesNotClick() {
        Minecraft game = new Minecraft();
        Stack stone = new Stack();
        game.player.inventory.contents[0] = stone;
        Controller controller = (Controller)game.controller;
        Minecraft189ScaffoldModule module = new Minecraft189ScaffoldModule();
        module.onEnable();
        assertTrue(module.beginRepeatProof(game, 1L, 2, 4));
        assertNotNull(module.repeatProofAttempt(game, 1L));
        game.world.placed = true;
        stone.count = 7;
        assertTrue(module.repeatProofReceipt(game, 2L));
        assertEquals(1, controller.clicks);

        // A genuine bridge walk can briefly be airborne or lack a valid
        // original mouse-over while transitioning between ledges. That
        // never authorizes another click, and must not lose the exact
        // original Minecraft/World/player identity lease either.
        game.player.onGround = false;
        assertNull(module.repeatProofAttempt(game, 6L));
        assertFalse(module.repeatProofStopped(),
                "temporary airborne movement cannot revoke unchanged source identity");
        assertEquals(1, controller.clicks);
        String waiting = module.repeatProofDiagnostics(game);
        assertTrue(waiting.contains("same_original_stack_and_expected_count=7"), waiting);
        assertEquals(1, controller.clicks);

        game.player.onGround = true;
        game.player.x = 2.05D;
        game.cursor.sample = new double[]{1,1,99,0,5,2.0D,99.5D,.5D};
        Minecraft189ScaffoldNativePlacementAttempt.Result next =
                module.repeatProofAttempt(game, 7L);
        assertNotNull(next, "fresh native ray and grounded player admit second placement");
        assertEquals(2, next.targetX);
        assertEquals(2, controller.clicks);
    }

    @Test void v37ReadOnlySecondStepDiagnosticDoesNotClickOrAdvanceCadence() {
        Minecraft game = new Minecraft();
        Stack original = new Stack();
        game.player.inventory.contents[0] = original;
        Controller controller = (Controller) game.controller;
        Minecraft189ScaffoldModule module = new Minecraft189ScaffoldModule();
        module.onEnable();
        assertTrue(module.beginRepeatProof(game, 5L, 2, 4));
        assertNotNull(module.repeatProofAttempt(game, 5L));
        game.world.placed = true;
        original.count = 7;
        assertTrue(module.repeatProofReceipt(game, 6L));
        assertEquals(1, controller.clicks);

        final String previousTarget = module.repeatProofDiagnostics(game);
        assertTrue(previousTarget.contains("attempts=1"));
        assertTrue(previousTarget.contains("same_original_stack_and_expected_count=7"));
        assertEquals(1, controller.clicks);
        assertFalse(module.repeatProofStopped(),
                "read-only diagnostic must not close the original transaction");

        game.player.x = 2.05D;
        game.cursor.sample = new double[]{1,1,99,0,5,2.0D,99.5D,.5D};
        final String next = module.repeatProofDiagnostics(game);
        assertTrue(next.contains("fresh_target=2,99,0"), next);
        assertEquals(1, controller.clicks,
                "a positive diagnostic is NEVER authorization for a controller call");
        assertEquals(1, module.repeatProofAttemptedCount());
        assertNotNull(module.repeatProofAttempt(game, 10L));
        assertEquals(2, controller.clicks);
    }

    @Test void v37ReadOnlyDiagnosticRevealsSourceItemIdentityChangeWithoutClick() {
        Minecraft game = new Minecraft();
        Stack original = new Stack();
        game.player.inventory.contents[0] = original;
        Controller controller = (Controller) game.controller;
        Minecraft189ScaffoldModule module = new Minecraft189ScaffoldModule();
        module.onEnable();
        assertTrue(module.beginRepeatProof(game, 5L, 2, 4));
        assertNotNull(module.repeatProofAttempt(game, 5L));
        game.world.placed = true;
        original.count = 7;
        assertTrue(module.repeatProofReceipt(game, 6L));
        game.player.inventory.contents[0] = new Stack();
        final String diagnostic = module.repeatProofDiagnostics(game);
        assertTrue(diagnostic.contains("original_stack_identity_changed"), diagnostic);
        assertEquals(1, controller.clicks);
        assertNull(module.repeatProofAttempt(game, 10L),
                "normal transaction still rejects replaced ItemStack identity");
        assertTrue(module.repeatProofStopped());
        assertEquals(1, controller.clicks);
    }

    @Test void v36SourceSwapFalseControllerOrDisableCancelsFutureRepeat() {
        Minecraft game=new Minecraft();
        Stack stone=new Stack();
        game.player.inventory.contents[0]=stone;
        Controller controller=(Controller)game.controller;
        Minecraft189ScaffoldModule module=new Minecraft189ScaffoldModule();
        module.onEnable();
        assertTrue(module.beginRepeatProof(game,1L,2,2));
        assertNotNull(module.repeatProofAttempt(game,1L));
        assertEquals(1,controller.clicks);
        game.world=new World(); // even identical coordinates may not migrate
        assertFalse(module.repeatProofReceipt(game,2L));
        assertTrue(module.repeatProofStopped());
        assertNull(module.repeatProofAttempt(game,10L));
        assertEquals(1,controller.clicks);

        module.onDisable();
        module.onEnable();
        game.player.inventory.contents[0]=new Stack();
        controller.originalReturn=false;
        assertTrue(module.beginRepeatProof(game,20L,2,2));
        Minecraft189ScaffoldNativePlacementAttempt.Result denied =
                module.repeatProofAttempt(game,20L);
        assertNotNull(denied);
        assertFalse(denied.originalControllerAccepted);
        assertTrue(module.repeatProofStopped(),"original controller veto consumes batch");
        assertFalse(module.repeatProofReceipt(game,21L));
        assertNull(module.repeatProofAttempt(game,22L));
        assertEquals(2,controller.clicks);

        module.onDisable();
        module.onEnable();
        controller.originalReturn=true;
        assertTrue(module.beginRepeatProof(game,30L,2,2));
        assertNotNull(module.repeatProofAttempt(game,30L));
        module.onDisable(); // no live receipt can outlive module disable
        assertTrue(module.repeatProofStopped());
        assertFalse(module.repeatProofReceipt(game,31L));
        assertNull(module.repeatProofAttempt(game,32L));
        assertEquals(3,controller.clicks);
    }

    @Test void missingSourceAndReconnectInvalidateOriginalGeneration(){
        Minecraft game=new Minecraft();
        game.player.inventory.contents[0]=new Stack();
        Minecraft189ScaffoldSourceEpoch epoch=new Minecraft189ScaffoldSourceEpoch();
        long first=epoch.observe(game);
        assertTrue(first>0);
        World original=game.world;
        game.world=null;
        assertEquals(0L,epoch.observe(game),
                "disconnect produces no valid original World source");
        game.world=original;
        long reconnected=epoch.observe(game);
        assertTrue(reconnected>first,
                "old original World reference after disconnect gets new generation");
        epoch.invalidate();
        assertTrue(epoch.observe(game)>reconnected,
                "owner-runtime close/reinstall cannot reuse a pending receipt generation");
        game.player.inventory.contents[0]=null;
        assertEquals(0L,epoch.observe(game),
                "missing native selected ItemBlock revokes pending source");
    }

    @Test void noOriginalItemWrongRayOrWorldMustVeto(){
        Minecraft game=new Minecraft();
        assertNull(Minecraft189ScaffoldOriginalCandidateProbe.observe(game));
        game.player.inventory.contents[0]=new Stack();
        game.cursor.sample[4]=1;
        assertNull(Minecraft189ScaffoldOriginalCandidateProbe.observe(game));
        game.cursor.sample[4]=5;
        game.cursor.sample[6]=99.05D;
        assertNull(Minecraft189ScaffoldOriginalCandidateProbe.observe(game),
                "native eye ray leaving solid support below stone must veto");
        game.cursor.sample[6]=99.5D;
        game.world.rayValid=false;
        assertNull(Minecraft189ScaffoldOriginalCandidateProbe.observe(game));
        game.world.rayValid=true;
        game.world.loaded=false;
        assertNull(Minecraft189ScaffoldOriginalCandidateProbe.observe(game));
        game.world.loaded=true;
        game.player.onGround=false;
        assertNull(Minecraft189ScaffoldOriginalCandidateProbe.observe(game));
        game.player.onGround=true;
        game.controller=new Object();
        assertNull(Minecraft189ScaffoldOriginalCandidateProbe.observe(game));
        game.controller=new Controller();
        assertNotNull(Minecraft189ScaffoldOriginalCandidateProbe.observe(game));
    }
}
