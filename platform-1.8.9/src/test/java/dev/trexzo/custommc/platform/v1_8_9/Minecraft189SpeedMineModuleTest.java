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
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
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
