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

final class Minecraft189FastBreakModuleTest {
    @Test
    void fastBreakWritesZeroOnlyWhileEnabledAndOwnsPlayerLifecycle() {
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
                            Minecraft189FastBreakModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189FastBreakModule.DELAY_SETTING_ID));
            assertEquals(
                    Minecraft189FastBreakModule.DEFAULT_DELAY,
                    runtime.featureCatalog()
                            .fastBreak()
                            .delaySetting()
                            .get()
                            .intValue());

            final TestController live =
                    new TestController();
            live.delay = 4;

            runtime.playerControllerBreakControl(
                    live);
            assertEquals(
                    4,
                    live.delay);
            assertEquals(
                    0,
                    live.setCalls);

            controller.enable(
                    Minecraft189FastBreakModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .fastBreak()
                            .active());

            runtime.playerControllerBreakControl(
                    live);
            assertEquals(
                    0,
                    live.delay);
            assertEquals(
                    1,
                    live.setCalls);

            runtime.featureCatalog()
                    .fastBreak()
                    .delaySetting()
                    .set(
                            2);
            live.delay = 3;
            runtime.playerControllerBreakControl(
                    live);
            assertEquals(
                    2,
                    live.delay);
            assertEquals(
                    2,
                    live.setCalls);

            controller.disable(
                    Minecraft189FastBreakModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .fastBreak()
                            .active());

            live.delay = 5;
            runtime.playerControllerBreakControl(
                    live);
            assertEquals(
                    5,
                    live.delay);
            assertEquals(
                    2,
                    live.setCalls);

            runtime.playerControllerBreakControl(
                    null);
            assertEquals(
                    2,
                    live.setCalls);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189FastBreakModule.ID));
        assertNull(
                settings.find(
                        Minecraft189FastBreakModule.DELAY_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
    }

    private static final class TestController
            implements Minecraft189BlockHitDelayControl {
        private int delay;
        private int setCalls;

        @Override
        public void customMcSetBlockHitDelay(
                final int delayTicks) {
            setCalls++;
            delay = delayTicks;
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
