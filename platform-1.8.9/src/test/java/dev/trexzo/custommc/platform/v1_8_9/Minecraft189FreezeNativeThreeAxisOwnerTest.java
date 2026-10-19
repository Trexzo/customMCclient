package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Genuine-shaped original X/Y/Z adapter, not Mojang binary gameplay proof. */
final class Minecraft189FreezeNativeThreeAxisOwnerTest {
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x=.8D,y=-.4D,z=.6D;
        int rx,ry,rz,wx,wy,wz;
        Runnable onReadX,onReadY,onReadZ,onWriteX,onWriteY,onWriteZ;
        boolean throwReadX,throwWriteX;
        private static void fire(Runnable callback){if(callback!=null)callback.run();}
        @Override public double customMcMotionX() {
            rx++;if(rx>9)throw new AssertionError("recursive original X read");
            final Runnable c=onReadX;onReadX=null;fire(c);
            if(throwReadX)throw new IllegalStateException("native X getter");
            return x;
        }
        @Override public double customMcMotionY() {
            ry++;if(ry>9)throw new AssertionError("recursive original Y read");
            final Runnable c=onReadY;onReadY=null;fire(c);
            return y;
        }
        @Override public double customMcMotionZ() {
            rz++;if(rz>9)throw new AssertionError("recursive original Z read");
            final Runnable c=onReadZ;onReadZ=null;fire(c);
            return z;
        }
        @Override public void customMcSetMotionX(double next) {
            wx++;if(wx>9)throw new AssertionError("recursive original X write");
            x=next;final Runnable c=onWriteX;onWriteX=null;fire(c);
            if(throwWriteX)throw new IllegalStateException("native X setter");
        }
        @Override public void customMcSetMotionY(double next) {
            wy++;if(wy>9)throw new AssertionError("recursive original Y write");
            y=next;final Runnable c=onWriteY;onWriteY=null;fire(c);
        }
        @Override public void customMcSetMotionZ(double next) {
            wz++;if(wz>9)throw new AssertionError("recursive original Z write");
            z=next;final Runnable c=onWriteZ;onWriteZ=null;fire(c);
        }
        int reads(){return rx+ry+rz;}
        int writes(){return wx+wy+wz;}
    }
    private static Minecraft189FreezeModule module(){
        final Minecraft189FreezeModule m=new Minecraft189FreezeModule();
        m.onEnable();return m;
    }
    @Test void originalThreeAxisFreezeWritesOneNativeZeroEach(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        m.apply(p);assertEquals(0D,p.x,1E-12);assertEquals(0D,p.y,1E-12);
        assertEquals(0D,p.z,1E-12);assertEquals(3,p.reads());assertEquals(3,p.writes());
        m.apply(p);assertEquals(3,p.writes());
    }
    @Test void samePlayerReentryInOriginalXGetterDoesNotReadTwice(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onReadX=()->m.apply(p);
        m.apply(p);assertEquals(3,p.reads());assertEquals(3,p.writes());
    }
    @Test void samePlayerReentryInOriginalYGetterDoesNotReadTwice(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onReadY=()->m.apply(p);
        m.apply(p);assertEquals(3,p.reads());assertEquals(3,p.writes());
    }
    @Test void samePlayerReentryInOriginalZGetterDoesNotReadTwice(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onReadZ=()->m.apply(p);
        m.apply(p);assertEquals(3,p.reads());assertEquals(3,p.writes());
    }
    @Test void samePlayerReentryInXSetterCannotDoubleZero(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onWriteX=()->m.apply(p);
        m.apply(p);assertEquals(1,p.wx);assertEquals(1,p.wy);assertEquals(1,p.wz);
    }
    @Test void samePlayerReentryInYSetterCannotDoubleZero(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onWriteY=()->m.apply(p);
        m.apply(p);assertEquals(1,p.wx);assertEquals(1,p.wy);assertEquals(1,p.wz);
    }
    @Test void samePlayerReentryInZSetterCannotDoubleZero(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onWriteZ=()->m.apply(p);
        m.apply(p);assertEquals(3,p.writes());
    }
    @Test void otherPlayerInXGetterRevokesAllOldAxes(){
        final Minecraft189FreezeModule m=module();final Player a=new Player(),b=new Player();
        a.onReadX=()->m.apply(b);
        m.apply(a);assertEquals(0,a.writes()+a.ry+a.rz+b.reads()+b.writes());
    }
    @Test void otherPlayerInYGetterDoesNotWriteStaleYOrZ(){
        final Minecraft189FreezeModule m=module();final Player a=new Player(),b=new Player();
        a.onReadY=()->m.apply(b);
        m.apply(a);assertEquals(1,a.wx);assertEquals(0,a.wy+a.wz+a.rz);
        assertEquals(0,b.reads()+b.writes());
    }
    @Test void otherPlayerInZGetterDoesNotWriteStaleZ(){
        final Minecraft189FreezeModule m=module();final Player a=new Player(),b=new Player();
        a.onReadZ=()->m.apply(b);
        m.apply(a);assertEquals(2,a.wx+a.wy);assertEquals(0,a.wz);
        assertEquals(0,b.reads()+b.writes());
    }
    @Test void anotherPlayerDuringOriginalXSetterPreventsYAndZ(){
        final Minecraft189FreezeModule m=module();final Player a=new Player(),b=new Player();
        a.onWriteX=()->m.apply(b);
        m.apply(a);assertEquals(0D,a.x,1E-12);
        assertEquals(1,a.wx);assertEquals(0,a.ry+a.rz+a.wy+a.wz+b.reads()+b.writes());
    }
    @Test void anotherPlayerDuringOriginalYSetterPreventsZ(){
        final Minecraft189FreezeModule m=module();final Player a=new Player(),b=new Player();
        a.onWriteY=()->m.apply(b);
        m.apply(a);assertEquals(0D,a.y,1E-12);assertEquals(0,a.rz+a.wz+b.reads()+b.writes());
    }
    @Test void anotherPlayerDuringOriginalZSetterCannotBorrowOriginalStack(){
        final Minecraft189FreezeModule m=module();final Player a=new Player(),b=new Player();
        a.onWriteZ=()->m.apply(b);
        m.apply(a);assertEquals(1,a.wz);assertEquals(0,b.reads()+b.writes());
    }
    @Test void originalGuiVetoInXGetterUnderOptInPauseRevokesXYZ(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        m.pauseWhileClickGuiSetting().set(Boolean.TRUE);
        p.onReadX=()->m.apply(p,true);
        m.apply(p,false);assertEquals(0,p.writes()+p.ry+p.rz);
    }
    @Test void originalGuiVetoInXSetterUnderOptInPauseStopsYZ(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        m.pauseWhileClickGuiSetting().set(Boolean.TRUE);
        p.onWriteX=()->m.apply(p,true);
        m.apply(p,false);assertEquals(1,p.wx);assertEquals(0,p.ry+p.rz+p.wy+p.wz);
    }
    @Test void defaultGuiPauseOffPreservesLegacyHardFreeze(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        assertFalse(m.pauseWhileClickGuiSetting().get());
        p.onReadX=()->m.apply(p,true);
        m.apply(p,false);assertEquals(3,p.writes());
    }
    @Test void sourceDisableDuringXGetterVetoesAllOriginalWrites(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onReadX=m::onDisable;
        m.apply(p);assertEquals(0,p.writes());assertFalse(m.active());
    }
    @Test void sourceOffOnInsideXGetterCannotInheritOldRead(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onReadX=()->{m.onDisable();m.onEnable();m.apply(p);};
        m.apply(p);assertEquals(0,p.writes());m.apply(p);assertEquals(3,p.writes());
    }
    @Test void horizontalPolicyEditInsideXGetterRejectsOldWrites(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onReadX=()->m.freezeHorizontalSetting().set(Boolean.FALSE);
        m.apply(p);assertEquals(0,p.writes());m.apply(p);assertEquals(1,p.wy);
    }
    @Test void verticalPolicyEditInsideYGetterRejectsOldYAndZ(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.onReadY=()->m.freezeVerticalSetting().set(Boolean.FALSE);
        m.apply(p);assertEquals(1,p.wx);assertEquals(0,p.wy+p.wz);
    }
    @Test void nativeXGetterExceptionReleasesLeaseWithoutWriting(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.throwReadX=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        assertEquals(0,p.writes());p.throwReadX=false;m.apply(p);
        assertEquals(3,p.writes());
    }
    @Test void nativeXSetterExceptionPreservesPartialVanillaZero(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        p.throwWriteX=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        assertEquals(0D,p.x,1E-12);assertEquals(-.4D,p.y,1E-12);
        assertEquals(0,p.wy+p.wz);p.throwWriteX=false;m.apply(p);
        assertEquals(1,p.wy);assertEquals(1,p.wz);
    }
    @Test void originalHorizontalOnlyNeverReadsOrWritesY(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        m.freezeVerticalSetting().set(Boolean.FALSE);
        m.apply(p);assertEquals(0,p.ry+p.wy);assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void originalVerticalOnlyNeverReadsOrWritesXZ(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        m.freezeHorizontalSetting().set(Boolean.FALSE);
        m.apply(p);assertEquals(0,p.rx+p.rz+p.wx+p.wz);assertEquals(1,p.ry);assertEquals(1,p.wy);
    }
    @Test void bothAxesOffRelinquishesAllOriginalMotionOwnership(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        m.freezeHorizontalSetting().set(Boolean.FALSE);
        m.freezeVerticalSetting().set(Boolean.FALSE);
        assertFalse(m.active());m.apply(p);assertEquals(0,p.reads()+p.writes());
    }
    @Test void actualGuiPauseKeepsVelocityAndPriorityUntilFocusReturns(){
        final Minecraft189FreezeModule m=module();final Player p=new Player();
        m.pauseWhileClickGuiSetting().set(Boolean.TRUE);
        m.apply(p,true);assertEquals(0,p.reads()+p.writes());assertTrue(m.active());
        m.apply(p,false);assertEquals(3,p.writes());
    }
}
