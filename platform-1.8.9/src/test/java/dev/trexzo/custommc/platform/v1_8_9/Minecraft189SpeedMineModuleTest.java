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

final class Minecraft189SpeedMineModuleTest {
    @Test
    void speedMineRaisesOnlyActiveLowerProgressAndOwnsPlayerSettingsLifecycle() {
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
                            Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189SpeedMineModule.ID));

            final TestController live =
                    new TestController();
            live.hitting = true;
            live.progress = 0.25F;

            runtime.playerControllerMiningControl(
                    live);
            assertEquals(
                    0.25F,
                    live.progress,
                    0.000001F);
            assertEquals(
                    0,
                    live.setCalls);

            controller.enable(
                    Minecraft189SpeedMineModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .speedMine()
                            .active());

            runtime.playerControllerMiningControl(
                    live);
            assertEquals(
                    0.70F,
                    live.progress,
                    0.000001F);
            assertEquals(
                    1,
                    live.setCalls);

            live.progress = 0.90F;
            runtime.playerControllerMiningControl(
                    live);
            assertEquals(
                    0.90F,
                    live.progress,
                    0.000001F);
            assertEquals(
                    1,
                    live.setCalls);

            live.hitting = false;
            live.progress = 0.10F;
            runtime.playerControllerMiningControl(
                    live);
            assertEquals(
                    0.10F,
                    live.progress,
                    0.000001F);
            assertEquals(
                    1,
                    live.setCalls);

            runtime.featureCatalog()
                    .speedMine()
                    .progressPercentSetting()
                    .set(90);
            assertEquals(
                    "90",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189SpeedMineModule.PROGRESS_SETTING_ID));

            live.hitting = true;
            live.progress = 0.20F;
            runtime.playerControllerMiningControl(
                    live);
            assertEquals(
                    0.90F,
                    live.progress,
                    0.000001F);
            assertEquals(
                    2,
                    live.setCalls);

            controller.disable(
                    Minecraft189SpeedMineModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .speedMine()
                            .active());

            live.progress = 0.20F;
            runtime.playerControllerMiningControl(
                    live);
            assertEquals(
                    0.20F,
                    live.progress,
                    0.000001F);
            assertEquals(
                    2,
                    live.setCalls);

            runtime.playerControllerMiningControl(
                    null);
            assertEquals(
                    2,
                    live.setCalls);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189SpeedMineModule.ID));
        assertNull(
                settings.find(
                        Minecraft189SpeedMineModule.PROGRESS_SETTING_ID));
        assertNull(settings.find(
                Minecraft189SpeedMineModule.PROGRESSIVE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189SpeedMineModule.STEP_PERCENT_SETTING_ID));
        assertNull(settings.find(
                Minecraft189SpeedMineModule.REQUIRE_ATTACK_HELD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189SpeedMineModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
    }

    @Test
    void progressiveRampAdvancesGraduallyOnlyForActiveValidMiningProgress() {
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
            final Minecraft189SpeedMineModule mine =
                    runtime.featureCatalog().speedMine();
            assertFalse(mine.progressiveSetting().get().booleanValue());
            assertEquals(10, mine.stepPercentSetting().get().intValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.PROGRESSIVE_SETTING_ID));
            assertEquals("10", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.STEP_PERCENT_SETTING_ID));
            controller.enable(Minecraft189SpeedMineModule.ID);
            final TestController live = new TestController();
            live.hitting = true;
            live.progress = 0.25F;
            // Default-off: preserve existing instant-minimum behavior.
            runtime.playerControllerMiningControl(live);
            assertEquals(0.70F, live.progress, 0.000001F);
            assertEquals(1, live.setCalls);

            mine.progressiveSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.PROGRESSIVE_SETTING_ID));
            live.progress = 0.25F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.35F, live.progress, 0.000001F);
            assertEquals(2, live.setCalls);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.45F, live.progress, 0.000001F);
            assertEquals(3, live.setCalls);

            mine.stepPercentSetting().set(25);
            assertEquals("25", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.STEP_PERCENT_SETTING_ID));
            runtime.playerControllerMiningControl(live);
            assertEquals(0.70F, live.progress, 0.000001F);
            assertEquals(4, live.setCalls);
            runtime.playerControllerMiningControl(live);
            assertEquals(4, live.setCalls); // No duplicate when at cap.

            live.progress = 0.90F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.90F, live.progress, 0.000001F);
            assertEquals(4, live.setCalls); // Never reduce native progress.

            live.progress = Float.NaN;
            runtime.playerControllerMiningControl(live);
            assertEquals(4, live.setCalls);
            live.progress = Float.POSITIVE_INFINITY;
            runtime.playerControllerMiningControl(live);
            assertEquals(4, live.setCalls);
            live.progress = -0.5F;
            runtime.playerControllerMiningControl(live);
            assertEquals(4, live.setCalls);
            live.hitting = false;
            live.progress = 0.20F;
            runtime.playerControllerMiningControl(live);
            assertEquals(4, live.setCalls);

            live.hitting = true;
            mine.progressPercentSetting().set(50);
            live.progress = 0.45F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.50F, live.progress, 0.000001F);
            assertEquals(5, live.setCalls);
            mine.progressiveSetting().set(Boolean.FALSE);
            mine.progressPercentSetting().set(70);
            live.progress = 0.20F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.70F, live.progress, 0.000001F);
            assertEquals(6, live.setCalls);
            controller.disable(Minecraft189SpeedMineModule.ID);
            live.progress = 0.20F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.20F, live.progress, 0.000001F);
            assertEquals(6, live.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189SpeedMineModule.PROGRESSIVE_SETTING_ID));
        assertNull(settings.find(Minecraft189SpeedMineModule.STEP_PERCENT_SETTING_ID));
    }

    @Test
    void speedMineInputGatesRespectMappedAttackAndSneakState() {
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
            final Minecraft189SpeedMineModule mine =
                    runtime.featureCatalog().speedMine();
            final TestController live = new TestController();
            live.hitting = true;
            live.progress = 0.20F;
            assertFalse(mine.requireAttackHeldSetting().get().booleanValue());
            assertFalse(mine.pauseWhileSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.REQUIRE_ATTACK_HELD_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.PAUSE_WHILE_SNEAKING_SETTING_ID));

            controller.enable(Minecraft189SpeedMineModule.ID);
            // With both gates OFF, no key or movement snapshot is needed.
            runtime.playerControllerMiningControl(live);
            assertEquals(0.70F, live.progress, 0.000001F);
            assertEquals(1, live.setCalls);

            mine.requireAttackHeldSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.REQUIRE_ATTACK_HELD_SETTING_ID));
            live.progress = 0.10F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.10F, live.progress, 0.000001F);
            assertEquals(1, live.setCalls);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.70F, live.progress, 0.000001F);
            assertEquals(2, live.setCalls);

            mine.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            // Missing mapped movement state fails closed when gate is enabled.
            live.progress = 0.10F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.10F, live.progress, 0.000001F);
            runtime.playerMovementState().update(true, true, false);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.10F, live.progress, 0.000001F);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.70F, live.progress, 0.000001F);
            assertEquals(3, live.setCalls);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, false);
            live.progress = 0.20F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.20F, live.progress, 0.000001F);

            // Both gates also apply to progressive increments, with no
            // catch-up credits or writes while paused.
            mine.progressiveSetting().set(Boolean.TRUE);
            mine.progressPercentSetting().set(80);
            mine.stepPercentSetting().set(15);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            runtime.playerMovementState().update(false, true, false);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.20F, live.progress, 0.000001F);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.35F, live.progress, 0.000001F);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.50F, live.progress, 0.000001F);
            assertEquals(5, live.setCalls);

            runtime.playerMovementState().clear();
            runtime.playerControllerMiningControl(live);
            assertEquals(0.50F, live.progress, 0.000001F);
            mine.pauseWhileSneakingSetting().set(Boolean.FALSE);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.65F, live.progress, 0.000001F);
            mine.requireAttackHeldSetting().set(Boolean.FALSE);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, false);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.80F, live.progress, 0.000001F);
            assertEquals(7, live.setCalls);

            controller.disable(Minecraft189SpeedMineModule.ID);
            live.progress = 0.10F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.10F, live.progress, 0.000001F);
            assertEquals(7, live.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189SpeedMineModule.REQUIRE_ATTACK_HELD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189SpeedMineModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(modules.find(Minecraft189SpeedMineModule.ID));
    }

    @Test
    void speedMineLegacyApplyFailsClosedOnlyWhenOptInGatesRequireAuthority() {
        final Minecraft189SpeedMineModule module = new Minecraft189SpeedMineModule();
        final TestController live = new TestController();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        live.hitting = true;
        live.progress = 0.15F;
        module.onEnable();
        module.apply(live);  // Previously supported overload remains valid.
        assertEquals(0.70F, live.progress, 0.000001F);

        module.requireAttackHeldSetting().set(Boolean.TRUE);
        live.progress = 0.15F;
        module.apply(live);  // No attack authority -> no write.
        assertEquals(0.15F, live.progress, 0.000001F);
        module.apply(live, true, null);
        assertEquals(0.70F, live.progress, 0.000001F);

        module.pauseWhileSneakingSetting().set(Boolean.TRUE);
        live.progress = 0.15F;
        module.apply(live, true, null);
        assertEquals(0.15F, live.progress, 0.000001F);
        movement.update(true, true, false);
        module.apply(live, true, movement.snapshot());
        assertEquals(0.15F, live.progress, 0.000001F);
        movement.update(true, false, false);
        module.apply(live, true, movement.snapshot());
        assertEquals(0.70F, live.progress, 0.000001F);

        module.onDisable();
        live.progress = 0.15F;
        module.apply(live, true, movement.snapshot());
        assertEquals(0.15F, live.progress, 0.000001F);
        module.apply(null, true, movement.snapshot());
    }

    @Test
    void speedMineGroundOnlyGatesInstantAndProgressiveWrites() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(
                new EventBus(), modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(), new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189SpeedMineModule mine =
                    runtime.featureCatalog().speedMine();
            final TestController live = new TestController();
            live.hitting = true;
            controller.enable(Minecraft189SpeedMineModule.ID);
            assertFalse(mine.groundOnlySetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.GROUND_ONLY_SETTING_ID));
            live.progress = 0.20F;
            runtime.playerControllerMiningControl(live);
            assertEquals(0.70F, live.progress, 0.000001F);
            assertEquals(1, live.setCalls); // Default without movement state.
            mine.groundOnlySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189SpeedMineModule.GROUND_ONLY_SETTING_ID));
            live.progress = 0.20F;
            runtime.playerControllerMiningControl(live);
            assertEquals(1, live.setCalls); // Missing movement.
            runtime.playerMovementState().update(false, false, false);
            runtime.playerControllerMiningControl(live);
            assertEquals(1, live.setCalls); // Airborne.
            runtime.playerMovementState().update(true, true, false);
            runtime.playerControllerMiningControl(live);
            assertEquals(2, live.setCalls); // Grounded; sneaking by itself is allowed.
            assertEquals(0.70F, live.progress, 0.000001F);
            mine.pauseWhileSneakingSetting().set(Boolean.TRUE);
            live.progress = 0.20F;
            runtime.playerControllerMiningControl(live);
            assertEquals(2, live.setCalls); // Sneak gate remains independent.
            runtime.playerMovementState().update(true, false, false);
            mine.progressiveSetting().set(Boolean.TRUE);
            mine.stepPercentSetting().set(15);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.35F, live.progress, 0.000001F);
            assertEquals(3, live.setCalls);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerControllerMiningControl(live);
            assertEquals(3, live.setCalls);
            runtime.playerMovementState().clear();
            runtime.playerControllerMiningControl(live);
            assertEquals(3, live.setCalls);
            runtime.playerMovementState().update(true, false, true);
            runtime.playerControllerMiningControl(live);
            assertEquals(0.50F, live.progress, 0.000001F);
            assertEquals(4, live.setCalls); // No deferred ramp during pause.

            mine.apply(live); // Legacy overload fails closed with Ground Only.
            assertEquals(4, live.setCalls);
            mine.groundOnlySetting().set(Boolean.FALSE);
            mine.pauseWhileSneakingSetting().set(Boolean.FALSE);
            runtime.playerMovementState().clear();
            runtime.playerControllerMiningControl(live);
            assertEquals(0.65F, live.progress, 0.000001F);
            assertEquals(5, live.setCalls); // Legacy behavior restored.
            controller.disable(Minecraft189SpeedMineModule.ID);
            runtime.playerControllerMiningControl(live);
            assertEquals(5, live.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189SpeedMineModule.GROUND_ONLY_SETTING_ID));
        assertNull(modules.find(Minecraft189SpeedMineModule.ID));
    }

    private static final class TestController
            implements Minecraft189BlockMiningControl {
        private boolean hitting;
        private float progress;
        private int setCalls;

        @Override
        public boolean customMcIsHittingBlock() {
            return hitting;
        }

        @Override
        public float customMcBlockDamageProgress() {
            return progress;
        }

        @Override
        public void customMcSetBlockDamageProgress(
                final float progress) {
            setCalls++;
            this.progress = progress;
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
