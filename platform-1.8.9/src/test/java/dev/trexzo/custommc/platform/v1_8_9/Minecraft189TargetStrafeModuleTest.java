package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189TargetStrafeModuleTest {
    @Test void orbitsRealNearestPlayerAndYieldsToMotionOwners() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(), new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings), null, null,
                settings, new SettingPresentationRegistry(), new NoOpHost());
        try {
            final Minecraft189TargetStrafeModule orbit = runtime.featureCatalog().targetStrafe();
            final Motion player = new Motion();
            assertEquals("0.28", settings.snapshotEncoded().get(
                    Minecraft189TargetStrafeModule.SPEED_SETTING_ID));
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189TargetStrafeModule.REQUIRE_FORWARD_SETTING_ID));
            controller.enable(Minecraft189TargetStrafeModule.ID);
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            // No local-player or target authority: no synthetic movement.
            runtime.playerMotionControl(player);
            assertEquals(0, player.writes);
            runtime.playerPositionState().update(3.0D, 0.0D, 0.0D);
            final Minecraft189WorldEntityPositionState positions =
                    new Minecraft189WorldEntityPositionState();
            final Minecraft189WorldEntityKindState kinds =
                    new Minecraft189WorldEntityKindState();
            positions.update(new double[]{0.0D, 0.0D, 0.0D});
            kinds.update(new int[]{
                    Minecraft189WorldEntityKindState.LIVING
                    | Minecraft189WorldEntityKindState.PLAYER});
            runtime.nearestPlayerTargetState().update(
                    runtime.playerPositionState().snapshot(),
                    positions.snapshot(), kinds.snapshot());
            runtime.playerMotionControl(player);
            assertEquals(0.0D, player.x, 0.00000001D);
            assertEquals(0.28D, player.z, 0.00000001D); // Clockwise tangent.
            assertEquals(1, player.writes);
            orbit.clockwiseSetting().set(Boolean.FALSE);
            runtime.playerMotionControl(player);
            assertEquals(-0.28D, player.z, 0.00000001D); // Live reverse.
            orbit.clockwiseSetting().set(Boolean.TRUE);
            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            final int before = player.writes;
            runtime.playerMotionControl(player);
            assertEquals(before, player.writes); // No W.
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            orbit.radiusSetting().set(2.0D);
            runtime.playerMotionControl(player);
            assertTrue(player.x < 0.0D); // Pull toward target when too far.
            assertTrue(player.z > 0.0D);

            // Existing Flight has higher horizontal movement ownership.
            controller.enable(Minecraft189FlightModule.ID);
            runtime.playerMovementState().update(false, false, false);
            final int writesBeforeFlight = player.writes;
            runtime.playerMotionControl(player);
            assertTrue(player.writes >= writesBeforeFlight);
            assertFalse(orbit.apply(player, runtime.playerPositionState().snapshot(),
                    runtime.nearestPlayerTargetState().snapshot(), null, true, true));
            controller.disable(Minecraft189FlightModule.ID);
            orbit.groundOnlySetting().set(Boolean.TRUE);
            runtime.playerMovementState().update(false, false, false);
            final int beforeGround = player.writes;
            runtime.playerMotionControl(player);
            assertEquals(beforeGround, player.writes);
            // While gated the vanilla game can change horizontal motion.
            // Regaining an already-exact orbit is correctly a zero-write
            // no-op, so simulate observed drift before testing ownership.
            player.x = 0.0D;
            player.z = 0.0D;
            runtime.playerMovementState().update(true, false, false);
            runtime.playerMotionControl(player);
            assertTrue(player.writes > beforeGround);

            // Target disappearance, range and invalid/coincident positions fail closed.
            runtime.nearestPlayerTargetState().clear();
            final int beforeMissing = player.writes;
            runtime.playerMotionControl(player);
            assertEquals(beforeMissing, player.writes);
            orbit.groundOnlySetting().set(Boolean.FALSE);
            assertThrows(IllegalArgumentException.class,
                    () -> orbit.radiusSetting().set(0.0D));
            assertThrows(IllegalArgumentException.class,
                    () -> orbit.speedSetting().set(Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> orbit.maxDistanceSetting().set(13.0D));
            controller.disable(Minecraft189TargetStrafeModule.ID);
        } finally { runtime.close(); }
        assertNull(settings.find(Minecraft189TargetStrafeModule.SPEED_SETTING_ID));
        assertNull(settings.find(Minecraft189TargetStrafeModule.RADIUS_SETTING_ID));
        assertNull(settings.find(Minecraft189TargetStrafeModule.MAX_DISTANCE_SETTING_ID));
        assertNull(settings.find(Minecraft189TargetStrafeModule.CLOCKWISE_SETTING_ID));
        assertNull(settings.find(Minecraft189TargetStrafeModule.REQUIRE_FORWARD_SETTING_ID));
        assertNull(settings.find(Minecraft189TargetStrafeModule.GROUND_ONLY_SETTING_ID));
        assertNull(modules.find(Minecraft189TargetStrafeModule.ID));
    }

    private static final class Motion implements Minecraft189PlayerMotionControl {
        private double x, y, z;
        private int writes;
        @Override public double customMcMotionX() { return x; }
        @Override public double customMcMotionY() { return y; }
        @Override public double customMcMotionZ() { return z; }
        @Override public void customMcSetMotionX(double v) { x=v; writes++; }
        @Override public void customMcSetMotionY(double v) { y=v; }
        @Override public void customMcSetMotionZ(double v) { z=v; writes++; }
    }
    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth(){return 1280;}
        @Override public int framebufferHeight(){return 720;}
        @Override public float uiScale(){return 1.0F;}
        @Override public void beginUi(UiViewport v){}
        @Override public void fillRect(float x,float y,float w,float h,int c){}
        @Override public void fillRoundedRect(float x,float y,float w,float h,float r,int c){}
        @Override public void strokeRect(float x,float y,float w,float h,float t,int c){}
        @Override public void pushClip(float x,float y,float w,float h){}
        @Override public void popClip(){}
        @Override public void drawText(UiFontHandle f,float x,float y,String s,int c){}
        @Override public void endUi(){}
    }
}
