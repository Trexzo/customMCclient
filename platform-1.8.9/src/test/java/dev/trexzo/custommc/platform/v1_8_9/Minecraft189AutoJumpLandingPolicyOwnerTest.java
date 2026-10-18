package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Source movement-policy cadence tests, not original Mojang game runtime. */
final class Minecraft189AutoJumpLandingPolicyOwnerTest {
    private static final class Source {
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        int jumps;
        final Minecraft189PlayerJumpControl player = () -> jumps++;
        void apply(Minecraft189AutoJumpModule module,
                boolean grounded, boolean sneaking, boolean sprinting,
                boolean forward, boolean anyMovement) {
            movement.update(grounded,sneaking,sprinting);
            module.apply(player,movement.snapshot(),forward,anyMovement,false);
        }
        void air(Minecraft189AutoJumpModule m) {
            apply(m,false,false,false,false,false);
        }
        void ground(Minecraft189AutoJumpModule m,
                boolean forward,boolean movementHeld,boolean sprinting) {
            apply(m,true,false,sprinting,forward,movementHeld);
        }
    }
    @Test void changingRequireForwardAfterGenuineLandingDoesNotSpendOldGroundContact(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.requireForwardSetting().set(true);m.onEnable();
        s.air(m);
        s.ground(m,false,false,false);
        assertEquals(0,s.jumps);
        m.requireForwardSetting().set(false);
        s.ground(m,false,false,false);
        s.ground(m,true,true,true);
        assertEquals(0,s.jumps);
        s.air(m);s.ground(m,false,false,false);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void changingRequireMovementAfterGenuineLandingDoesNotReplayOldContact(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.requireMovementSetting().set(true);m.onEnable();
        s.air(m);s.ground(m,false,false,false);
        m.requireMovementSetting().set(false);
        s.ground(m,false,false,false);
        assertEquals(0,s.jumps);
        s.air(m);s.ground(m,false,false,false);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void changingRequireNativeSprintAfterLandingDoesNotReplayOldContact(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.requireSprintingSetting().set(true);m.onEnable();
        s.air(m);s.ground(m,true,true,false);
        m.requireSprintingSetting().set(false);
        s.ground(m,true,true,true);
        assertEquals(0,s.jumps);
        s.air(m);s.ground(m,true,true,false);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void changingSneakGateDuringObservedLandingDelayRevokesOldContact(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.landingDelayTicksSetting().set(2);m.onEnable();
        s.air(m);s.ground(m,true,true,false);
        assertEquals(0,s.jumps);
        m.pauseWhileSneakingSetting().set(true);
        s.ground(m,true,true,false);
        s.ground(m,true,true,false);
        s.ground(m,true,true,false);
        assertEquals(0,s.jumps);
        s.air(m);
        s.ground(m,true,true,false);
        s.ground(m,true,true,false);
        s.ground(m,true,true,false);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void togglingForwardRequirementBeforeOldDelayEndsCannotReleaseIt(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.landingDelayTicksSetting().set(1);m.onEnable();
        s.air(m);s.ground(m,true,true,false);
        m.requireForwardSetting().set(true);
        s.ground(m,true,true,false);
        assertEquals(0,s.jumps);
        s.air(m);s.ground(m,true,true,false);
        s.ground(m,true,true,false);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void genuinePhysicalForwardPressWithoutPolicyEditMayQualifySameGroundContact(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.requireForwardSetting().set(true);m.onEnable();
        s.air(m);s.ground(m,false,false,false);
        assertEquals(0,s.jumps);
        s.ground(m,true,true,false);
        assertEquals(1,s.jumps);
        s.ground(m,true,true,false);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void genuineNativeSprintBecomingTrueWithoutPolicyEditCanQualifyContact(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.requireSprintingSetting().set(true);m.onEnable();
        s.air(m);s.ground(m,true,true,false);
        assertEquals(0,s.jumps);
        s.ground(m,true,true,true);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void userGuiFocusStillDisarmsOriginalLandingUntilNewAirGround(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();m.onEnable();
        s.air(m);
        s.movement.update(true,false,false);
        m.apply(s.player,s.movement.snapshot(),true,true,true);
        s.ground(m,true,true,false);
        assertEquals(0,s.jumps);
        s.air(m);s.ground(m,true,true,false);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void moduleRestartWithoutOldOriginalAirborneCannotSpendPriorLease(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.landingDelayTicksSetting().set(2);m.onEnable();
        s.air(m);s.ground(m,true,true,false);
        m.onDisable();m.onEnable();
        // Legacy initial-on-enable behavior remains: an initial grounded
        // source callback is eligible without a previous observed landing.
        s.ground(m,true,true,false);
        assertEquals(1,s.jumps);
        s.ground(m,true,true,false);
        assertEquals(1,s.jumps);
        m.onDisable();
    }
    @Test void newPhysicalAirContactAlwaysCapturesUpdatedExactSettings(){
        Minecraft189AutoJumpModule m=new Minecraft189AutoJumpModule();
        Source s=new Source();
        m.requireForwardSetting().set(true);m.onEnable();
        s.air(m);s.ground(m,false,false,false);
        m.requireForwardSetting().set(false);
        s.ground(m,false,false,false);
        assertEquals(0,s.jumps);
        s.air(m);s.ground(m,false,false,false);
        assertEquals(1,s.jumps);
        m.requireMovementSetting().set(true);
        s.air(m);s.ground(m,false,false,false);
        assertEquals(1,s.jumps);
        s.ground(m,false,true,false);
        assertEquals(2,s.jumps);
        m.onDisable();
    }
}
