package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189DamageBoostModuleTest {
    @Test
    void damageBoostFiresOncePerFreshHurtTimeResetAndConsumesSuspendedHits() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                new SettingRegistry();
        final ModuleCategoryRegistry categories =
                new ModuleCategoryRegistry();
        final ServiceRegistry services =
                new ServiceRegistry();
        services.register(
                RenderPipeline.class,
                new RenderPipeline());

        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(
                new PlatformContext(
                        new EventBus(),
                        modules,
                        controller,
                        services));

        final Minecraft189HostRuntime runtime =
                Minecraft189HostRuntime.install(
                        platform,
                        new ModulePresentationRegistry(),
                        categories,
                        new ModuleSettingRegistry(
                                modules,
                                settings),
                        null,
                        null,
                        settings,
                        new SettingPresentationRegistry(),
                        new NoOpHost());

        try {
            assertNotNull(
                    categories.find(
                            Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189DamageBoostModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189DamageBoostModule.MULTIPLIER_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189DamageBoostModule.VERTICAL_MULTIPLIER_SETTING_ID));
            assertEquals(
                    Minecraft189DamageBoostModule.DEFAULT_VERTICAL_MULTIPLIER,
                    runtime.featureCatalog()
                            .damageBoost()
                            .verticalMultiplierSetting()
                            .get()
                            .doubleValue(),
                    0.000000001D);

            final TestPlayer player =
                    new TestPlayer();
            player.motionX = 0.40D;
            player.motionY = 0.30D;
            player.motionZ = -0.20D;

            controller.enable(
                    Minecraft189DamageBoostModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .damageBoost()
                            .active());

            runtime.playerHurtTimeState()
                    .update(
                            10);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.40D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    -0.20D,
                    player.motionZ,
                    0.000000001D);

            runtime.playerHurtTimeState()
                    .update(
                            9);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.40D,
                    player.motionX,
                    0.000000001D);

            runtime.playerHurtTimeState()
                    .update(
                            10);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.50D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.30D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    -0.25D,
                    player.motionZ,
                    0.000000001D);

            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.50D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    -0.25D,
                    player.motionZ,
                    0.000000001D);

            runtime.featureCatalog()
                    .damageBoost()
                    .multiplierSetting()
                    .set(
                            1.50D);
            runtime.featureCatalog()
                    .damageBoost()
                    .verticalMultiplierSetting()
                    .set(
                            2.00D);
            runtime.playerHurtTimeState()
                    .update(
                            8);
            runtime.playerMotionControl(
                    player);
            runtime.playerHurtTimeState()
                    .update(
                            10);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.75D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.60D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    -0.375D,
                    player.motionZ,
                    0.000000001D);

            runtime.playerHurtTimeState()
                    .update(
                            8);
            runtime.playerMotionControl(
                    player);
            controller.enable(
                    Minecraft189FlightModule.ID);
            player.motionX = 0.40D;
            player.motionZ = -0.20D;
            runtime.playerHurtTimeState()
                    .update(
                            10);
            runtime.playerMotionControl(
                    player);
            controller.disable(
                    Minecraft189FlightModule.ID);

            player.motionX = 0.40D;
            player.motionZ = -0.20D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.40D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    -0.20D,
                    player.motionZ,
                    0.000000001D);

            runtime.playerHurtTimeState()
                    .update(
                            8);
            runtime.playerMotionControl(
                    player);
            controller.enable(
                    Minecraft189FreezeModule.ID);
            player.motionX = 0.40D;
            player.motionZ = -0.20D;
            runtime.playerHurtTimeState()
                    .update(
                            10);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.0D,
                    player.motionZ,
                    0.000000001D);
            controller.disable(
                    Minecraft189FreezeModule.ID);

            player.motionX = 0.40D;
            player.motionZ = -0.20D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.40D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    -0.20D,
                    player.motionZ,
                    0.000000001D);

            controller.disable(
                    Minecraft189DamageBoostModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .damageBoost()
                            .active());

            runtime.playerMotionControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189DamageBoostModule.ID));
        assertNull(
                settings.find(
                        Minecraft189DamageBoostModule.MULTIPLIER_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189DamageBoostModule.VERTICAL_MULTIPLIER_SETTING_ID));
        assertNull(settings.find(
                Minecraft189DamageBoostModule.CAP_HORIZONTAL_SETTING_ID));
        assertNull(settings.find(
                Minecraft189DamageBoostModule.MAX_HORIZONTAL_SPEED_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void cappedDamageBoostLimitsOnlyExtraMomentumAndRetainsVerticalBehavior() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189DamageBoostModule boost =
                    runtime.featureCatalog().damageBoost();
            final TestPlayer player = new TestPlayer();
            boost.multiplierSetting().set(2.0D);
            boost.verticalMultiplierSetting().set(1.5D);
            assertFalse(boost.capHorizontalSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189DamageBoostModule.CAP_HORIZONTAL_SETTING_ID));
            assertEquals("0.7", settings.snapshotEncoded().get(
                    Minecraft189DamageBoostModule.MAX_HORIZONTAL_SPEED_SETTING_ID));
            controller.enable(Minecraft189DamageBoostModule.ID);

            // Default OFF preserves the unrestricted multiplier behavior.
            player.motionX = 0.30D;
            player.motionZ = 0.40D;
            player.motionY = 0.20D;
            runtime.playerHurtTimeState().update(8);
            runtime.playerMotionControl(player); // Prime observation.
            runtime.playerHurtTimeState().update(10);
            runtime.playerMotionControl(player);
            assertEquals(0.60D, player.motionX, 0.000000001D);
            assertEquals(0.80D, player.motionZ, 0.000000001D);
            assertEquals(0.30D, player.motionY, 0.000000001D);

            boost.capHorizontalSetting().set(Boolean.TRUE);
            boost.maxHorizontalSpeedSetting().set(0.75D);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189DamageBoostModule.CAP_HORIZONTAL_SETTING_ID));
            assertEquals("0.75", settings.snapshotEncoded().get(
                    Minecraft189DamageBoostModule.MAX_HORIZONTAL_SPEED_SETTING_ID));
            player.motionX = 0.30D;
            player.motionZ = 0.40D;
            player.motionY = 0.20D;
            runtime.playerHurtTimeState().update(8);
            runtime.playerMotionControl(player);
            runtime.playerHurtTimeState().update(10);
            runtime.playerMotionControl(player);
            assertEquals(0.45D, player.motionX, 0.000000001D);
            assertEquals(0.60D, player.motionZ, 0.000000001D);
            assertEquals(0.75D, Math.hypot(player.motionX, player.motionZ),
                    0.000000001D);
            assertEquals(0.30D, player.motionY, 0.000000001D);
            // Repeated callbacks for the same fresh-hit value do not multiply.
            runtime.playerMotionControl(player);
            assertEquals(0.45D, player.motionX, 0.000000001D);

            // Already-above-cap pre-hit momentum must never be reduced.
            player.motionX = 0.80D;
            player.motionZ = 0.60D;
            player.motionY = 0.20D;
            runtime.playerHurtTimeState().update(8);
            runtime.playerMotionControl(player);
            runtime.playerHurtTimeState().update(10);
            runtime.playerMotionControl(player);
            assertEquals(0.80D, player.motionX, 0.000000001D);
            assertEquals(0.60D, player.motionZ, 0.000000001D);
            assertEquals(0.30D, player.motionY, 0.000000001D);

            // Missing or malformed source horizontal motion is fail-closed
            // under the enabled cap, independently of finite vertical motion.
            player.motionX = Double.NaN;
            player.motionZ = 0.20D;
            player.motionY = 0.20D;
            runtime.playerHurtTimeState().update(8);
            runtime.playerMotionControl(player);
            runtime.playerHurtTimeState().update(10);
            runtime.playerMotionControl(player);
            assertTrue(Double.isNaN(player.motionX));
            assertEquals(0.20D, player.motionZ, 0.000000001D);
            assertEquals(0.30D, player.motionY, 0.000000001D);
            boost.maxHorizontalSpeedSetting().set(1.50D);
            boost.capHorizontalSetting().set(Boolean.FALSE);
            player.motionX = 0.40D;
            player.motionZ = 0.0D;
            runtime.playerHurtTimeState().update(8);
            runtime.playerMotionControl(player);
            runtime.playerHurtTimeState().update(10);
            runtime.playerMotionControl(player);
            assertEquals(0.80D, player.motionX, 0.000000001D);
            controller.disable(Minecraft189DamageBoostModule.ID);
            assertThrows(IllegalArgumentException.class,
                    () -> boost.maxHorizontalSpeedSetting().set(0.09D));
            assertThrows(IllegalArgumentException.class,
                    () -> boost.maxHorizontalSpeedSetting().set(5.01D));
            assertThrows(IllegalArgumentException.class,
                    () -> boost.maxHorizontalSpeedSetting().set(Double.NaN));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189DamageBoostModule.CAP_HORIZONTAL_SETTING_ID));
        assertNull(settings.find(
                Minecraft189DamageBoostModule.MAX_HORIZONTAL_SPEED_SETTING_ID));
        assertNull(modules.find(Minecraft189DamageBoostModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerMotionControl {
        private double motionX;
        private double motionY;
        private double motionZ;

        @Override
        public double customMcMotionX() {
            return motionX;
        }

        @Override
        public void customMcSetMotionX(
                final double motionX) {
            this.motionX = motionX;
        }

        @Override
        public double customMcMotionY() {
            return motionY;
        }

        @Override
        public void customMcSetMotionY(
                final double motionY) {
            this.motionY = motionY;
        }

        @Override
        public double customMcMotionZ() {
            return motionZ;
        }

        @Override
        public void customMcSetMotionZ(
                final double motionZ) {
            this.motionZ = motionZ;
        }
    }

    private static final class NoOpHost
            implements LegacyUiHostCallbacks {
        @Override
        public int framebufferWidth() {
            return 1280;
        }

        @Override
        public int framebufferHeight() {
            return 720;
        }

        @Override
        public float uiScale() {
            return 1.0F;
        }

        @Override
        public void beginUi(
                final UiViewport viewport) {
        }

        @Override
        public void fillRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final int argb) {
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
        }

        @Override
        public void strokeRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float thickness,
                final int argb) {
        }

        @Override
        public void pushClip(
                final float x,
                final float y,
                final float width,
                final float height) {
        }

        @Override
        public void popClip() {
        }

        @Override
        public void drawText(
                final UiFontHandle font,
                final float x,
                final float y,
                final String text,
                final int argb) {
        }

        @Override
        public void endUi() {
        }
    }
}
