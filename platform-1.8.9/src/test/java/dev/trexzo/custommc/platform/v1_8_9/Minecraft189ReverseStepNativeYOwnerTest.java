package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original mapped player adapter; pinned Mojang native CI is independent. */
final class Minecraft189ReverseStepNativeYOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot state(
            boolean ground, boolean sneak) {
        final Minecraft189PlayerMovementState m=new Minecraft189PlayerMovementState();
        m.update(ground,sneak,false);return m.snapshot();
    }
    private static final class Player implements Minecraft189PlayerMotionControl {
        double y=0D;
        int ry,wy;
        Runnable onRead,onWrite;
        boolean failRead,failWrite;
        @Override public double customMcMotionY(){
            ry++;
            if(ry>12)throw new AssertionError("recursive original Y read");
            final Runnable c=onRead;onRead=null;if(c!=null)c.run();
            if(failRead)throw new IllegalStateException("original Y getter");
            return y;
        }
        @Override public void customMcSetMotionY(double next){
            wy++;
            if(wy>12)throw new AssertionError("recursive original Y setter");
            y=next;
            final Runnable c=onWrite;onWrite=null;if(c!=null)c.run();
            if(failWrite)throw new IllegalStateException("original Y setter");
        }
        @Override public double customMcMotionX(){return 0D;}
        @Override public void customMcSetMotionX(double x){}
        @Override public double customMcMotionZ(){return 0D;}
        @Override public void customMcSetMotionZ(double z){}
    }
    private static Minecraft189ReverseStepModule module(){
        final Minecraft189ReverseStepModule m=new Minecraft189ReverseStepModule();
        m.onEnable();return m;
    }
    private static boolean ground(Minecraft189ReverseStepModule m,Player p){
        return m.apply(p,state(true,false),false);
    }
    private static boolean air(Minecraft189ReverseStepModule m,Player p){
        return m.apply(p,state(false,false),false);
    }
    @Test void genuineGroundToAirTransitionWritesOriginalYOnce(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        assertFalse(ground(m,p));assertTrue(air(m,p));
        assertEquals(-.5D,p.y,1E-12);assertEquals(1,p.wy);
        assertFalse(air(m,p));assertEquals(1,p.wy);
    }
    @Test void nestedSamePlayerDuringYGetterCannotSpendSecondDeparture(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);
        p.onRead=()->assertFalse(air(m,p));
        assertTrue(air(m,p));assertEquals(1,p.ry);assertEquals(1,p.wy);
    }
    @Test void nestedDifferentPlayerDuringYGetterRevokesOldSource(){
        final Minecraft189ReverseStepModule m=module();final Player a=new Player(),b=new Player();
        ground(m,a);
        a.onRead=()->assertFalse(air(m,b));
        assertFalse(air(m,a));assertEquals(0,a.wy+b.ry+b.wy);
    }
    @Test void nestedNullPlayerDuringYGetterVetoesOldSource(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);
        p.onRead=()->assertFalse(m.apply(null,state(false,false),false));
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
    @Test void originalGuiOpenedDuringYGetterConsumesOldEdge(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);
        p.onRead=()->assertFalse(m.apply(p,state(false,false),false,false,true));
        assertFalse(air(m,p));assertEquals(0,p.wy);
        assertFalse(air(m,p));assertEquals(0,p.wy);
        ground(m,p);assertTrue(air(m,p));assertEquals(1,p.wy);
    }
    @Test void nestedSuspensionInsideGetterCancelsOldNativeDeparture(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.onRead=()->assertFalse(m.apply(p,state(false,false),true));
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
    @Test void nestedUnknownMovementInsideGetterCancelsNativeDeparture(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        final Minecraft189PlayerMovementState missing=new Minecraft189PlayerMovementState();
        ground(m,p);p.onRead=()->assertFalse(m.apply(p,missing.snapshot(),false));
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
    @Test void nativeGetterDisableInvalidatesOriginalDeparture(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.onRead=m::onDisable;
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
    @Test void nativeGetterOffOnCannotBorrowNewActivation(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.onRead=()->{m.onDisable();m.onEnable();assertFalse(air(m,p));};
        assertFalse(air(m,p));assertEquals(0,p.wy);
        assertFalse(air(m,p));ground(m,p);
        assertTrue(air(m,p));assertEquals(1,p.wy);
    }
    @Test void originalYGetterSpeedPolicyEditVetoesOldVelocity(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.onRead=()->m.speedSetting().set(.8D);
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
    @Test void originalYGetterDelayPolicyEditVetoesOldVelocity(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.onRead=()->m.delayTicksSetting().set(1);
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
    @Test void nativeGetterExceptionReleasesOriginalStack(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.failRead=true;
        assertThrows(IllegalStateException.class,()->air(m,p));
        p.failRead=false;assertFalse(air(m,p));
        ground(m,p);assertTrue(air(m,p));assertEquals(1,p.wy);
    }
    @Test void samePlayerSetterReentryCannotSpendAnotherDeparture(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.onWrite=()->assertFalse(air(m,p));
        assertTrue(air(m,p));assertEquals(1,p.ry);assertEquals(1,p.wy);
    }
    @Test void otherPlayerSetterCallbackRevokesSuccessButKeepsNativeY(){
        final Minecraft189ReverseStepModule m=module();
        final Player a=new Player(),b=new Player();
        ground(m,a);a.onWrite=()->assertFalse(air(m,b));
        assertFalse(air(m,a));assertEquals(-.5D,a.y,1E-12);
        assertEquals(1,a.wy);assertEquals(0,b.ry+b.wy);
    }
    @Test void nestedGuiDuringSetterDoesNotFakeOriginalVelocityRollback(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);
        p.onWrite=()->assertFalse(m.apply(p,state(false,false),false,false,true));
        assertFalse(air(m,p));assertEquals(-.5D,p.y,1E-12);
        assertEquals(1,p.wy);
    }
    @Test void nativeSetterDisableCannotReportOldOwnerSuccess(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.onWrite=m::onDisable;
        assertFalse(air(m,p));assertEquals(-.5D,p.y,1E-12);
        assertFalse(m.active());
    }
    @Test void nativeSetterExceptionPreservesRealPartialYWrite(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.failWrite=true;
        assertThrows(IllegalStateException.class,()->air(m,p));
        assertEquals(-.5D,p.y,1E-12);assertEquals(1,p.wy);
        p.failWrite=false;assertFalse(air(m,p));
        ground(m,p);p.y=0D;assertTrue(air(m,p));
    }
    @Test void genuineTwoCallbackDelayCountsActualEligibleAirborneTicks(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        m.delayTicksSetting().set(2);
        ground(m,p);assertFalse(air(m,p));assertFalse(air(m,p));
        assertTrue(air(m,p));assertEquals(-.5D,p.y,1E-12);
        assertEquals(1,p.wy);
    }
    @Test void realGuiFocusCancelsOldDelayedDeparture(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        m.delayTicksSetting().set(2);
        ground(m,p);assertFalse(air(m,p));
        assertFalse(m.apply(p,state(false,false),false,false,true));
        assertFalse(air(m,p));assertFalse(air(m,p));
        assertEquals(0,p.wy);
    }
    @Test void originalSneakRequirementCancelsIneligibleDeparture(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        m.requireSneakingSetting().set(Boolean.TRUE);
        ground(m,p);assertFalse(air(m,p));assertEquals(0,p.wy);
        m.apply(p,state(true,true),false);
        assertTrue(m.apply(p,state(false,true),false));
        assertEquals(1,p.wy);
    }
    @Test void originalMovementInputRequirementRequiresRealMovementHeld(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        m.requireMovementSetting().set(Boolean.TRUE);
        ground(m,p);
        assertFalse(air(m,p));assertEquals(0,p.wy);
        m.apply(p,state(true,false),false,true);
        assertTrue(m.apply(p,state(false,false),false,true));
        assertEquals(1,p.wy);
    }
    @Test void originalUpwardVelocityNeverForcesReverseStep(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.y=.42D;
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
    @Test void fasterOriginalDownwardVelocityIsNeverOverwritten(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.y=-.8D;
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
    @Test void nonfiniteMappedOriginalYNeverForcesSyntheticVelocity(){
        final Minecraft189ReverseStepModule m=module();final Player p=new Player();
        ground(m,p);p.y=Double.NaN;
        assertFalse(air(m,p));assertEquals(0,p.wy);
    }
}
