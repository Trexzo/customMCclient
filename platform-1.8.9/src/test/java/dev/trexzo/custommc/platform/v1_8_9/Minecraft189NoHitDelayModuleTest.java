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

final class Minecraft189NoHitDelayModuleTest {
    @Test
    void noHitDelayClampsOnlyPositiveCounterAndOwnsCombatCategoryLifecycle() {
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
                            Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID));
            assertEquals(
                    "Combat",
                    categories.find(
                                    Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID)
                            .displayName());
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189NoHitDelayModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189NoHitDelayModule.DELAY_SETTING_ID));
            assertEquals(
                    Minecraft189NoHitDelayModule.DEFAULT_DELAY,
                    runtime.featureCatalog()
                            .noHitDelay()
                            .delaySetting()
                            .get()
                            .intValue());
            assertEquals(
                    7,
                    runtime.leftClickCounter(
                            7));

            controller.enable(
                    Minecraft189NoHitDelayModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .noHitDelay()
                            .active());
            assertEquals(
                    0,
                    runtime.leftClickCounter(
                            7));
            assertEquals(
                    0,
                    runtime.leftClickCounter(
                            0));
            assertEquals(
                    -1,
                    runtime.leftClickCounter(
                            -1));

            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoHitDelayModule.GROUND_ONLY_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoHitDelayModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            // Default-off parity: invalid/missing movement snapshots
            // must not change existing No Hit Delay behavior.
            assertFalse(runtime.playerMovementState().snapshot().available());
            assertEquals(0, runtime.leftClickCounter(7));

            runtime.featureCatalog().noHitDelay()
                    .groundOnlySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoHitDelayModule.GROUND_ONLY_SETTING_ID));
            assertEquals(7, runtime.leftClickCounter(7));
            runtime.playerMovementState().update(false, false, false);
            assertEquals(7, runtime.leftClickCounter(7));
            runtime.playerMovementState().update(true, false, false);
            assertEquals(0, runtime.leftClickCounter(7));
            runtime.playerMovementState().update(true, true, false);
            // Ground Only does not itself reject sneaking.
            assertEquals(0, runtime.leftClickCounter(7));

            runtime.featureCatalog().noHitDelay()
                    .pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoHitDelayModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            assertEquals(7, runtime.leftClickCounter(7));
            runtime.playerMovementState().update(true, false, true);
            assertEquals(0, runtime.leftClickCounter(7));
            // Independent pause gate works when Ground Only is OFF.
            runtime.featureCatalog().noHitDelay()
                    .groundOnlySetting().set(Boolean.FALSE);
            runtime.playerMovementState().update(false, true, false);
            assertEquals(7, runtime.leftClickCounter(7));
            runtime.playerMovementState().update(false, false, false);
            assertEquals(0, runtime.leftClickCounter(7));
            runtime.playerMovementState().clear();
            assertEquals(7, runtime.leftClickCounter(7));
            runtime.featureCatalog().noHitDelay()
                    .pauseWhileSneakingSetting().set(Boolean.FALSE);
            assertEquals(0, runtime.leftClickCounter(7));

            runtime.featureCatalog()
                    .noHitDelay()
                    .delaySetting()
                    .set(
                            3);
            assertEquals(
                    3,
                    runtime.leftClickCounter(
                            7));
            assertEquals(
                    3,
                    runtime.leftClickCounter(
                            3));
            assertEquals(
                    2,
                    runtime.leftClickCounter(
                            2));

            controller.disable(
                    Minecraft189NoHitDelayModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .noHitDelay()
                            .active());
            assertEquals(
                    7,
                    runtime.leftClickCounter(
                            7));
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189NoHitDelayModule.ID));
        assertNull(
                settings.find(
                        Minecraft189NoHitDelayModule.DELAY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189NoHitDelayModule.GROUND_ONLY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189NoHitDelayModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID));
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
