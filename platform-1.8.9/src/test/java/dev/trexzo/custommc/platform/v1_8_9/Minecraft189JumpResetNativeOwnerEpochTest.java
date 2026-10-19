package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Genuine source mapped jump callback lifetime; offline Java regressions. */
final class Minecraft189JumpResetNativeOwnerEpochTest {
    private static final class Source {
        final Minecraft189JumpResetModule module = new Minecraft189JumpResetModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHurtTimeState hurt = new Minecraft189PlayerHurtTimeState();
        int physicalJumps;
        Runnable duringNative;
        boolean throwAfterOriginalJump;
        final Minecraft189PlayerJumpControl player = () -> {
            physicalJumps++;
            final Runnable callback = duringNative;
            duringNative = null;
            if (callback != null) callback.run();
            if (throwAfterOriginalJump) {
                throwAfterOriginalJump = false;
                throw new IllegalStateException(
                        "original Minecraft player jump mutated and threw");
            }
        };
        Source() {
            module.cooldownSetting().set(0);
            module.onEnable();
            movement.update(true,false,true);
            hurt.update(0);
            assertFalse(apply());
        }
        boolean apply() {
            return module.apply(player,movement.snapshot(),
                    hurt.snapshot(),true,false);
        }
        boolean hit() {
            hurt.update(10);
            return apply();
        }
        boolean freshHit() {
            hurt.update(0);
            assertFalse(apply());
            return hit();
        }
    }

    @Test void stableOriginalJumpIsCountedOnceAndNextFreshHurtCanJump() {
        Source r = new Source();
        assertTrue(r.hit());
        assertEquals(1,r.physicalJumps);
        assertFalse(r.apply());
        assertTrue(r.freshHit());
        assertEquals(2,r.physicalJumps);
    }
    @Test void nativeJumpDisablingModuleCannotReportCurrentOwnerSuccess() {
        Source r = new Source();
        r.duringNative=r.module::onDisable;
        assertFalse(r.hit());
        assertEquals(1,r.physicalJumps);
        assertFalse(r.module.active());
        assertFalse(r.apply());
        assertEquals(1,r.physicalJumps);
    }
    @Test void nativeJumpOffOnDoesNotTransferOriginalHurtCredit() {
        Source r = new Source();
        r.duringNative=()->{r.module.onDisable();r.module.onEnable();};
        assertFalse(r.hit());
        assertEquals(1,r.physicalJumps);
        // New activation needs its own real local hurt-source baseline.
        assertFalse(r.apply());
        r.hurt.update(0);
        assertFalse(r.apply());
        assertTrue(r.hit());
        assertEquals(2,r.physicalJumps);
    }
    @Test void nativeJumpForwardGuiEditRevokesOldOriginalOwner() {
        Source r = new Source();
        r.duringNative=()->r.module.requireForwardSetting().set(false);
        assertFalse(r.hit());
        assertEquals(1,r.physicalJumps);
        assertTrue(r.freshHit());
        assertEquals(2,r.physicalJumps);
    }
    @Test void nativeJumpSneakingPolicyEditRevokesOldOwner() {
        Source r = new Source();
        r.duringNative=()->r.module.pauseWhileSneakingSetting().set(false);
        assertFalse(r.hit());
        assertEquals(1,r.physicalJumps);
        assertTrue(r.freshHit());
    }
    @Test void nativeJumpSprintGuiPolicyChangeRevokesOldOwner() {
        Source r = new Source();
        r.duringNative=()->r.module.requireSprintSetting().set(true);
        assertFalse(r.hit());
        assertEquals(1,r.physicalJumps);
        assertTrue(r.freshHit());
    }
    @Test void nativeJumpChanceGuiEditRevokesOldOwner() {
        Source r = new Source();
        r.duringNative=()->r.module.chanceSetting().set(0);
        assertFalse(r.hit());
        assertEquals(1,r.physicalJumps);
        assertFalse(r.freshHit());
        r.module.chanceSetting().set(100);
        assertTrue(r.freshHit());
        assertEquals(2,r.physicalJumps);
    }
    @Test void nativeJumpCooldownGuiEditCannotBankObsoleteCooldown() {
        Source r = new Source();
        r.module.cooldownSetting().set(5);
        r.duringNative=()->r.module.cooldownSetting().set(0);
        assertFalse(r.hit());
        assertEquals(1,r.physicalJumps);
        assertTrue(r.freshHit());
        assertEquals(2,r.physicalJumps);
    }
    @Test void nativeJumpThrowAfterPhysicalMutationStillConsumesOldHurt() {
        Source r = new Source();
        r.module.cooldownSetting().set(3);
        r.throwAfterOriginalJump = true;
        assertThrows(IllegalStateException.class,r::hit);
        assertEquals(1,r.physicalJumps);
        assertFalse(r.apply());
        assertFalse(r.freshHit(),
                "original physical jump and native exception must retain cooldown");
        assertEquals(1,r.physicalJumps);
    }
    @Test void originalJumpThrowsAfterGuiEditAndDisarmsOldCooldown() {
        Source r = new Source();
        r.module.cooldownSetting().set(5);
        r.throwAfterOriginalJump=true;
        r.duringNative=()->r.module.requireForwardSetting().set(false);
        assertThrows(IllegalStateException.class,r::hit);
        assertEquals(1,r.physicalJumps);
        assertTrue(r.freshHit(),
                "new original hurt is allowed under newly configured policy");
        assertEquals(2,r.physicalJumps);
    }
    @Test void genuineReentrantHurtCallbackCannotDispatchSecondNativeJump() {
        Source r = new Source();
        r.duringNative=()->{
            r.hurt.update(11);
            assertFalse(r.apply());
        };
        assertTrue(r.hit());
        assertEquals(1,r.physicalJumps);
        assertFalse(r.apply());
        assertEquals(1,r.physicalJumps);
        assertTrue(r.freshHit());
        assertEquals(2,r.physicalJumps);
    }
    @Test void reentrantModuleRestartWithNestedNativeHurtCannotJumpTwice() {
        Source r = new Source();
        r.duringNative=()->{
            r.module.onDisable();
            r.module.onEnable();
            r.hurt.update(11);
            assertFalse(r.apply());
        };
        assertFalse(r.hit());
        assertEquals(1,r.physicalJumps);
        assertFalse(r.apply());
        r.hurt.update(0);
        assertFalse(r.apply());
        assertTrue(r.hit());
        assertEquals(2,r.physicalJumps);
    }
    @Test void nativeOriginalGroundGateStillRejectsRealAirborneHit() {
        Source r = new Source();
        r.movement.update(false,false,true);
        assertFalse(r.hit());
        assertEquals(0,r.physicalJumps);
        r.movement.update(true,false,true);
        assertFalse(r.apply(),"ground recovery must not replay old hurt");
    }
    @Test void sourceSuspensionConsumesRealHurtWithoutBanking() {
        Source r = new Source();
        r.hurt.update(10);
        assertFalse(r.module.apply(r.player,r.movement.snapshot(),
                r.hurt.snapshot(),true,true));
        assertFalse(r.apply());
        assertEquals(0,r.physicalJumps);
        assertTrue(r.freshHit());
    }
}
