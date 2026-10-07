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

final class Minecraft189TimerSpeedModuleTest {
    @Test
    void timerUsesConfiguredMultiplierAndRestoresVanillaWhenDisabled() {
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
                            Minecraft189TimerSpeedModule.ID));

            final TestTimer timer =
                    new TestTimer();
            timer.speed = 1.25F;
            runtime.timerSpeedControl(
                    timer);
            assertEquals(
                    1.0F,
                    timer.speed,
                    0.000001F);

            controller.enable(
                    Minecraft189TimerSpeedModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .timerSpeed()
                            .active());

            runtime.featureCatalog()
                    .timerSpeed()
                    .speedPercentSetting()
                    .set(150);
            assertEquals(
                    "150",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189TimerSpeedModule.SPEED_SETTING_ID));

            runtime.timerSpeedControl(
                    timer);
            assertEquals(
                    1.5F,
                    timer.speed,
                    0.000001F);

            runtime.featureCatalog()
                    .timerSpeed()
                    .speedPercentSetting()
                    .set(50);
            runtime.timerSpeedControl(
                    timer);
            assertEquals(
                    0.5F,
                    timer.speed,
                    0.000001F);

            controller.disable(
                    Minecraft189TimerSpeedModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .timerSpeed()
                            .active());
            runtime.timerSpeedControl(
                    timer);
            assertEquals(
                    1.0F,
                    timer.speed,
                    0.000001F);

            runtime.timerSpeedControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189TimerSpeedModule.ID));
        assertNull(
                settings.find(
                        Minecraft189TimerSpeedModule.SPEED_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
    }

    private static final class TestTimer
            implements Minecraft189TimerSpeedControl {
        private float speed = 1.0F;

        @Override
        public float customMcTimerSpeed() {
            return speed;
        }

        @Override
        public void customMcSetTimerSpeed(
                final float speed) {
            this.speed = speed;
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
