package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Genuine mapped player.setSprinting(true) callback reentrancy; offline
 * source adapter assertions, not Mojang binary runtime proof.
 */
final class Minecraft189AutoSprintNestedNativeSetterTest {
    private static final class Harness {
        final Minecraft189AutoSprintModule module =
                new Minecraft189AutoSprintModule();
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        int writes, depth, maximumDepth;
        boolean physicalSprint;
        Runnable onWrite;
        boolean throwAfterWrite;
        final Minecraft189PlayerSprintControl nativePlayer = value -> {
            writes++;
            depth++;
            maximumDepth=Math.max(maximumDepth,depth);
            try {
                Runnable callback=onWrite;
                onWrite=null;
                if(callback!=null)callback.run();
                physicalSprint=value;
                if(throwAfterWrite) {
                    throwAfterWrite=false;
                    throw new IllegalStateException("original native setter mutated then threw");
                }
            } finally { depth--; }
        };
        Harness() {
            module.onEnable();
            state.update(true,false,false);
        }
        void tick(boolean w, boolean wasd, boolean gui) {
            module.apply(nativePlayer,state.snapshot(),w,wasd,gui);
        }
        void reenter(boolean w,boolean wasd,boolean gui) {
            onWrite=()->tick(w,wasd,gui);
        }
    }
    @Test void stableOriginalSprintingNeedsOneNativeSetter() {
        Harness h=new Harness();
        h.tick(false,false,false);
        assertEquals(1,h.writes);
        assertTrue(h.physicalSprint);
        assertEquals(1,h.maximumDepth);
    }
    @Test void originalSetterReentersSameHostWithoutInfiniteRecursion() {
        Harness h=new Harness();
        h.reenter(true,true,false);
        h.tick(true,true,false);
        assertEquals(1,h.writes);
        assertEquals(1,h.maximumDepth);
    }
    @Test void originalSetterReentersWithDifferentRealWASDButNoSecondWrite() {
        Harness h=new Harness();
        h.reenter(false,true,false);
        h.tick(true,true,false);
        assertEquals(1,h.writes);
    }
    @Test void nativeSetterOffOnCannotProduceNestedSprintFromNewActivation() {
        Harness h=new Harness();
        h.onWrite=()->{
            h.module.onDisable();
            h.module.onEnable();
            h.tick(true,true,false);
        };
        h.tick(true,true,false);
        assertEquals(1,h.writes);
        assertTrue(h.physicalSprint);
    }
    @Test void disabledDuringOriginalSetterDoesNotForceVanillaSprintFalse() {
        Harness h=new Harness();
        h.onWrite=h.module::onDisable;
        h.tick(true,true,false);
        assertTrue(h.physicalSprint);
        assertEquals(1,h.writes);
        h.tick(true,true,false);
        assertEquals(1,h.writes);
    }
    @Test void genuineNativeSetterThrowsAfterWriteClearsDispatchGuard() {
        Harness h=new Harness();
        h.throwAfterWrite=true;
        assertThrows(IllegalStateException.class,()->h.tick(true,true,false));
        assertEquals(1,h.writes);
        assertTrue(h.physicalSprint);
        h.tick(true,true,false);
        assertEquals(2,h.writes);
    }
    @Test void nestedNativeSetterFailureStillReleasesOriginalGuard() {
        Harness h=new Harness();
        h.throwAfterWrite=true;
        h.reenter(true,true,false);
        assertThrows(IllegalStateException.class,()->h.tick(true,true,false));
        assertEquals(1,h.writes);
        h.tick(true,true,false);
        assertEquals(2,h.writes);
    }
    @Test void genuineClickGuiVetoIsStillAbsolute() {
        Harness h=new Harness();
        h.tick(true,true,true);
        assertEquals(0,h.writes);
        h.tick(true,true,false);
        assertEquals(1,h.writes);
    }
    @Test void physicalSneakRemainsAVetoWithoutInventionOfSprint() {
        Harness h=new Harness();
        h.state.update(true,true,false);
        h.tick(true,true,false);
        assertEquals(0,h.writes);
    }
    @Test void groundOnlyRequiresActuallyObservedNativeGrounded() {
        Harness h=new Harness();
        h.module.groundOnlySetting().set(true);
        h.state.update(false,false,false);
        h.tick(true,true,false);
        assertEquals(0,h.writes);
        h.state.update(true,false,false);
        h.tick(true,true,false);
        assertEquals(1,h.writes);
    }
    @Test void requireForwardCannotBeSubstitutedWithSidewaysKeys() {
        Harness h=new Harness();
        h.module.requireForwardSetting().set(true);
        h.tick(false,true,false);
        assertEquals(0,h.writes);
        h.tick(true,true,false);
        assertEquals(1,h.writes);
    }
    @Test void requireMovementUsesTruePhysicalWASDOnly() {
        Harness h=new Harness();
        h.module.requireMovementSetting().set(true);
        h.tick(false,false,false);
        assertEquals(0,h.writes);
        h.tick(false,true,false);
        assertEquals(1,h.writes);
    }
    @Test void originalSprintingSnapshotStillPreventsRepeatNativeWrites() {
        Harness h=new Harness();
        h.tick(true,true,false);
        h.state.update(true,false,true);
        h.tick(true,true,false);
        assertEquals(1,h.writes);
    }
    @Test void hostCallbackAfterOriginalReturnsMayLegitimatelySetSprintAgain() {
        Harness h=new Harness();
        h.reenter(true,true,false);
        h.tick(true,true,false);
        assertEquals(1,h.writes);
        h.state.update(true,false,false);
        h.tick(true,true,false);
        assertEquals(2,h.writes);
    }
}
